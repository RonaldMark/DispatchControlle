package service;

import database.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.Logging;

/**
 *
 * @author ronald.langat
 */
public class DroneService {

    private final Logging logger;

    public DroneService() {
        logger = new Logging();
    }

    public int registerDrone(JSONObject data) {
        String serialNumber = data.getString("serial_number");
        String model = data.getString("model");
        String weightLimit = data.getString("weight_limit");

        String sql = "  INSERT INTO drone (serial_number,model,weight_limit,battery_capacity,state) \n"
                + " VALUES ('" + serialNumber + "','" + model + "','" + weightLimit + "','100','IDLE');";

        DBConnection conn = new DBConnection();
        int i = 0;
        try {
            i = conn.executeQuery(sql);
        } catch (Exception ex) {
            // log error to a file
            logger.applicationLog(logger.logPreString() + "Error registerDrone - " + ex.getMessage() + "\n\n", "", 3);
        } finally {
            conn.closeConn();
        }
        return i;
    }

    public JSONObject getDrone(String serialNumber) {
        String sql = "SELECT drone_id,serial_number,model,weight_limit,battery_capacity,state FROM drone WHERE serial_number='" + serialNumber + "'";

        DBConnection conn = new DBConnection();
        JSONObject result = new JSONObject();

        try {
            ResultSet rs = conn.query_all(sql);

            while (rs.next()) {
                // Put results in json object
                result.put("drone_id", rs.getInt("drone_id"));
                result.put("serial_number", serialNumber);
                result.put("model", rs.getString("model"));
                result.put("weight_limit", rs.getString("weight_limit"));
                result.put("battery_capacity", rs.getString("battery_capacity"));
                result.put("state", rs.getString("state"));
            }
        } catch (SQLException e) {
            logger.applicationLog(logger.logPreString() + "Error getDrone  - " + e.getMessage() + "\n\n", "", 3);
        } finally {
            conn.closeConn();
        }

        return result;
    }

    public JSONObject getAllDrone() {
        String sql = "SELECT drone_id,serial_number,model,weight_limit,battery_capacity,state FROM drone";

        DBConnection conn = new DBConnection();

        JSONArray resultArray = new JSONArray();
        int count = 0;
        try {
            ResultSet rs = conn.query_all(sql);

            while (rs.next()) {
                count = count + 1;
                JSONObject result = new JSONObject();
                // Put results in json object
                result.put("drone_id", rs.getInt("drone_id"));
                result.put("serial_number", rs.getString("serial_number"));
                result.put("model", rs.getString("model"));
                result.put("weight_limit", rs.getString("weight_limit"));
                result.put("battery_capacity", rs.getString("battery_capacity"));
                result.put("state", rs.getString("state"));

                resultArray.put(result);
            }
        } catch (SQLException e) {
            logger.applicationLog(logger.logPreString() + "Error getAllDrone - " + e.getMessage() + "\n\n", "", 3);
        } finally {
            conn.closeConn();
        }
        JSONObject response = new JSONObject();
        response.put("list", resultArray);
        response.put("total", count);
        return response;
    }

    public JSONObject getAllDroneAvailableForLoading() {
        String sql = "SELECT drone_id,serial_number,model,weight_limit,battery_capacity,state FROM drone  WHERE state='IDLE'";

        DBConnection conn = new DBConnection();

        JSONArray resultArray = new JSONArray();

        int count = 0;
        try {
            ResultSet rs = conn.query_all(sql);

            while (rs.next()) {
                count = count + 1;
                JSONObject result = new JSONObject();
                // Put results in json object
                result.put("drone_id", rs.getInt("drone_id"));
                result.put("serial_number", rs.getString("serial_number"));
                result.put("model", rs.getString("model"));
                result.put("weight_limit", rs.getString("weight_limit"));
                result.put("battery_capacity", rs.getString("battery_capacity"));
                result.put("state", rs.getString("state"));

                resultArray.put(result);
            }
        } catch (SQLException e) {
            logger.applicationLog(logger.logPreString() + "Error getAllDroneAvailableForLoading - " + e.getMessage() + "\n\n", "", 3);
        } finally {
            conn.closeConn();
        }
        JSONObject response = new JSONObject();
        response.put("list", resultArray);
        response.put("available", count);
        return response;
    }

    public JSONObject getDroneBatteryLevel(String serialNumber) {
        String sql = "SELECT battery_capacity FROM drone  WHERE serial_number='" + serialNumber + "'";

        DBConnection conn = new DBConnection();
        JSONObject result = new JSONObject();
        try {
            ResultSet rs = conn.query_all(sql);

            while (rs.next()) {
                // Put results in json object
                result.put("battery_capacity", rs.getString("battery_capacity"));
            }
        } catch (SQLException e) {
            logger.applicationLog(logger.logPreString() + "Error getDroneBatteryLevel  - " + e.getMessage() + "\n\n", "", 3);
        } finally {
            conn.closeConn();
        }
        result.put("serial_number", serialNumber);
        return result;
    }

    public int updateDroneStatus(String serialNumber, String newState) {

        String sql = "UPDATE drone set state ='" + newState + "' WHERE serial_number='" + serialNumber + "'";

        DBConnection conn = new DBConnection();
        int i = 0;
        try {
            i = conn.executeQuery(sql);
        } catch (Exception ex) {
            // log error to a file
            logger.applicationLog(logger.logPreString() + "Error updateDroneStatus  - " + ex.getMessage() + "\n\n", "", 3);
        } finally {
            conn.closeConn();
        }
        return i;
    }

    public int updateBatteryCapacity(String serialNumber, int previous, int current) {

        // update drone table
        String sql = "UPDATE drone set battery_capacity ='" + current + "' WHERE serial_number='" + serialNumber + "'";

        //create record in event Log
        String sqlEventLog = "INSERT INTO event_log (serial_number,previous_capacity,battery_capacity) "
                + "values ('" + serialNumber + "','" + previous + "','" + current + "')";

        DBConnection conn = new DBConnection();
        int i = 0;
        try {
            conn.executeQuery(sqlEventLog);
            i = conn.executeQuery(sql);
        } catch (Exception ex) {
            // log error to a file
            logger.applicationLog(logger.logPreString() + "Error updateBatteryCapacity  - " + ex.getMessage() + "\n\n", "", 3);
        } finally {
            conn.closeConn();
        }
        return i;
    }

    public int completeDroneDelivery(String serialNumber, String newState) {

        String sql = "UPDATE drone set state ='" + newState + "' WHERE serial_number='" + serialNumber + "'";
        String sqlAct = "UPDATE drone_actions set action_status ='COMPLETED' WHERE serial_number='" + serialNumber + "'";

        DBConnection conn = new DBConnection();
        int i = 0;
        try {
            conn.executeQuery(sqlAct);
            i = conn.executeQuery(sql);
        } catch (Exception ex) {
            // log error to a file
            logger.applicationLog(logger.logPreString() + "Error completeDroneDelivery  - " + ex.getMessage() + "\n\n", "", 3);
        } finally {
            conn.closeConn();
        }
        return i;
    }

    public int saveLoadedMedication(JSONObject data) {
        String serialNumber = data.getString("serial_number");
        String dispatchId = data.getString("dispatch_id");

        // medication
        JSONObject medication = data.getJSONObject("medication");
        String medName = medication.getString("name");
        String weight = medication.getString("weight");
        String code = medication.getString("code");

        // develivery
        JSONObject delivery = data.getJSONObject("delivery");
        String fromLocation = delivery.getString("from_location");
        String toLocation = delivery.getString("to_location");
        String loaderName = delivery.getString("loader_name");
        String loaderContact = delivery.getString("loader_contact");

        String sql = "INSERT INTO drone_actions (dispatch_id,serial_number,med_name,med_code,med_weight,from_location,to_location,loader_name,loader_contact,action_status) "
                + "VALUES ('" + dispatchId + "','" + serialNumber + "','" + medName + "','" + code + "','" + weight + "','" + fromLocation + "','" + toLocation + "','" + loaderName + "','" + loaderContact + "','LOADED')";

        DBConnection conn = new DBConnection();
        int i = 0;
        try {
            i = conn.executeQuery(sql);
            // update drone state to LOADED
            if (i > 0) {
                updateDroneStatus(serialNumber, String.valueOf(Enumerations.STATE.LOADED));
            }
        } catch (Exception ex) {
            // log error to a file
            logger.applicationLog(logger.logPreString() + "Error saveLoadedMedication  - " + ex.getMessage() + "\n\n", "", 3);
        } finally {
            conn.closeConn();
        }
        return i;
    }

    public JSONObject checkLoadedMedication(String serialNumber) {
        String sql = "SELECT med_name,med_code,med_weight,med_image FROM drone_actions  WHERE serial_number='" + serialNumber + "' AND action_status='LOADED'";

        DBConnection conn = new DBConnection();
        JSONObject response = new JSONObject();
        JSONObject result = new JSONObject();
        try {
            ResultSet rs = conn.query_all(sql);

            while (rs.next()) {
                // Put results in json object
                result.put("name", rs.getString("med_name"));
                result.put("code", rs.getString("med_code"));
                result.put("weight", rs.getString("med_weight"));
                result.put("image", rs.getString("med_image"));
            }
        } catch (SQLException e) {
            logger.applicationLog(logger.logPreString() + "Error checkLoadedMedication  - " + e.getMessage() + "\n\n", "", 3);
        } finally {
            conn.closeConn();
        }

        response.put("serial_number", serialNumber);

        if (result.isEmpty()) {
            response.put("respose_code", "001");
            response.put("response_message", "Drone is not loaded");
        } else {
            response.put("respose_code", "000");
            response.put("response_message", "success");
            response.put("medication", result);
        }

        return response;
    }

    public JSONObject getEventLogList(String serialNumber) {

        String sql = "SELECT serial_number,previous_capacity,battery_capacity,created_date FROM event_log  WHERE serial_number='" + serialNumber + "'";

        DBConnection conn = new DBConnection();
        JSONArray resultArray = new JSONArray();

        try {
            ResultSet rs = conn.query_all(sql);

            while (rs.next()) {
                JSONObject result = new JSONObject();
                // Put results in json object
                result.put("serial_number", rs.getString("serial_number"));
                result.put("previous_capacity", rs.getString("previous_capacity"));
                result.put("battery_capacity", rs.getString("battery_capacity"));
                result.put("created_date", rs.getTimestamp("created_date"));
                resultArray.put(result);
            }
        } catch (SQLException e) {
            logger.applicationLog(logger.logPreString() + "Error getEventLogList - " + e.getMessage() + "\n\n", "", 3);
        } finally {
            conn.closeConn();
        }
        JSONObject response = new JSONObject();
        response.put("logs", resultArray);
        return response;
    }

    public static String getNextDroneState(String currentState) {
        String nextState = currentState;
        switch (currentState) {
            case "IDLE":
                nextState = "" + Enumerations.STATE.LOADING;
                break;
            case "LOADING":
                nextState = "" + Enumerations.STATE.LOADED;
                break;
            case "LOADED":
                nextState = "" + Enumerations.STATE.DELIVERING;
                break;
            case "DELIVERING":
                nextState = "" + Enumerations.STATE.DELIVERED;
                break;
            case "DELIVERED":
                nextState = "" + Enumerations.STATE.RETURNING;
                break;
            case "RETURNING":
                nextState = "" + Enumerations.STATE.IDLE;
                break;
        }

        return nextState;
    }
}
