/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import database.DBConnection;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;

/**
 *
 * @author ronald.langat
 */
public class ValidationService {

    public JSONObject doRegistrationValidation(JSONObject data) {
        JSONObject result = new JSONObject();

        if (data.has("serial_number") && data.has("model") && data.has("weight_limit")) {

            if (!data.getString("serial_number").isEmpty() && !data.getString("model").isEmpty() && !data.getString("weight_limit").isEmpty()) {

                // validate serial number
                int serialNumberLength = data.getString("serial_number").length();

                if (serialNumberLength > 100) {
                    result.put("validation", "fail");
                    result.put("msg_type", "1210");
                    result.put("response", "001");
                    result.put("response_description", "Max length serial number is 100!");
                    return result;
                } else {
                    //model (Lightweight, Middleweight, Cruiserweight, Heavyweight);
                    if (isValidModel(data.getString("model"))) {

                        // validate limit
                        int weightLimit = Integer.parseInt(data.getString("weight_limit"));
                        if (weightLimit > 500) {
                            result.put("validation", "fail");
                            result.put("msg_type", "1210");
                            result.put("response", "001");
                            result.put("response_description", "Max weight limit is 500gr!");
                            return result;
                        } else {
                            if (checkDroneExistence(data.getString("serial_number"))) {
                                result.put("validation", "fail");
                                result.put("msg_type", "1210");
                                result.put("response", "001");
                                result.put("response_description", "Failed. Drone with serial number " + data.getString("serial_number") + " already exists!");
                                return result;
                            } else {
                                // if it reaches here then validation passed
                                result.put("validation", "pass");
                                return result;
                            }
                        }

                    } else {
                        result.put("validation", "fail");
                        result.put("msg_type", "1210");
                        result.put("response", "001");
                        result.put("response_description", "Invalid model input!");
                        return result;
                    }
                }

            } else {
                result.put("validation", "fail");
                result.put("msg_type", "1210");
                result.put("response", "001");
                result.put("response_description", "Serial Number, Model and Weight Limit cannot be null or empty!");
                return result;
            }
        } else {
            result.put("validation", "fail");
            result.put("msg_type", "1210");
            result.put("response", "001");
            result.put("response_description", "Serial Number, Model and Weight Limit are mandatory");
            return result;
        }
    }

    public JSONObject validateLoading(JSONObject data) {
        JSONObject response = new JSONObject();
        String serialNumber = data.getString("serial_number");

        if (data.has("medication") && data.has("delivery")) {

            // get medication object
            JSONObject medication = data.getJSONObject("medication");
            if (medication.has("name") && medication.has("weight") && medication.has("code")) {
                String medName = medication.getString("name");
                String weight = medication.getString("weight");
                String code = medication.getString("code");

                // get delivery object
                JSONObject delivery = data.getJSONObject("delivery");
                if (delivery.has("from_location") && delivery.has("to_location") && delivery.has("loader_name") && delivery.has("loader_contact")) {
                    // check Medication name
                    String medNameRegex = "^[A-Za-z_-][A-Za-z0-9_-]*$";

                    if (checkRegex(medName, medNameRegex)) {

                        String medCodeRegex = "^[A-Z_][A-Z0-9_]*$";
                        if (checkRegex(code, medCodeRegex)) {

                            // get drone
                            DroneService ds = new DroneService();
                            JSONObject drone = ds.getDrone(serialNumber);
                            String maxWeight = drone.getString("weight_limit");
                            BigDecimal dbWeight = new BigDecimal(weight);
                            BigDecimal dbMaxWeight = new BigDecimal(maxWeight);

                            //validate weight 
                            if (dbMaxWeight.compareTo(dbWeight) >= 0) {

                                //validate battery level
                                String batteryCapacity = drone.getString("battery_capacity");
                                int dbBatteryCapacity = Integer.valueOf(batteryCapacity);
                                if (dbBatteryCapacity >= 25) {

                                    //check status if IDLE
                                    String state = drone.getString("state");
                                    if ("IDLE".equalsIgnoreCase(state)) {

                                        //
                                        response.put("validation", "pass");
                                        return response;
                                    } else {
                                        response.put("validation", "fail");
                                        response.put("msg_type", "1210");
                                        response.put("respose_code", "001");
                                        response.put("response_message", "Drone of serial number " + serialNumber + " is not available!. Can only load IDLE drones");
                                        return response;
                                    }
                                } else {
                                    response.put("validation", "fail");
                                    response.put("msg_type", "1210");
                                    response.put("respose_code", "001");
                                    response.put("response_message", "Cannot load the drone. Low battery capacity Level!");
                                    return response;
                                }
                            } else {
                                response.put("validation", "fail");
                                response.put("msg_type", "1210");
                                response.put("respose_code", "001");
                                response.put("response_message", "Max limit of " + maxWeight + " exceeded!");
                                return response;
                            }

                        } else {
                            response.put("validation", "fail");
                            response.put("msg_type", "1210");
                            response.put("respose_code", "001");
                            response.put("response_message", "Invalid medication Code input! (allowed only upper case letters, underscore and numbers)");
                            return response;
                        }
                    } else {
                        response.put("validation", "fail");
                        response.put("msg_type", "1210");
                        response.put("respose_code", "001");
                        response.put("response_message", "Invalid medication Name input!(allowed only letters, numbers, hyphen,  underscore)");
                        return response;
                    }
                } else {
                    // missing fields
                    response.put("validation", "fail");
                    response.put("msg_type", "1210");
                    response.put("respose_code", "001");
                    response.put("response_message", "delivery from, to, loader name and contacts are mandatory fields. Check and try again");
                    return response;
                }
            } else {
                // missing fields
                response.put("validation", "fail");
                response.put("msg_type", "1210");
                response.put("respose_code", "001");
                response.put("response_message", "Medication name, weight and code are mandatory fields. Check and try again");
                return response;
            }

        } else {
            response.put("validation", "fail");
            response.put("msg_type", "1210");
            response.put("respose_code", "001");
            response.put("response_message", "Missing mandatory fields. Check and try again");
            return response;
        }
    }

    public static boolean checkDroneExistence(String serialNumber) {
        DBConnection conn = new DBConnection();
        boolean exists = false;
        try {
            String sql = "SELECT COUNT(serial_number) as counts FROM drone WHERE serial_number ='" + serialNumber + "'";
            ResultSet rs = conn.query_all(sql);
            int count = 0;
            while (rs.next()) {
                count = rs.getInt("counts");
            }

            // check if there are rows
            if (count > 0) {
                exists = true;
            }
        } catch (SQLException e) {
        } finally {
            conn.closeConn();
        }
        return exists;
    }

    public static boolean isValidModel(String model) {
        for (Enumerations.MODEL m : Enumerations.MODEL.values()) {
            if (m.name().equals(model)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkRegex(String word, String regex) {
        boolean match = false;

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(word);

        match = matcher.matches();
        return match;
    }
}
