/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import static main.App.logger;
import org.json.JSONException;
import org.json.JSONObject;
import service.DroneService;
import service.Enumerations;
import service.ValidationService;

/**
 *
 * @author ronald.langat
 */
public class ServiceController extends AbstractVerticle {

    EventBus eventBus;

    @Override
    public void start(Future<Void> startApp) throws Exception {
        System.out.println("ServiceController DeploymentID: " + vertx.getOrCreateContext().deploymentID());

        // Initiate event Bus instance
        eventBus = vertx.eventBus();

        //route to various services/actors/verticles
        eventBus.consumer("1001", this::registerController);
        eventBus.consumer("1003", this::changeDroneStatus);
        eventBus.consumer("1005", this::getAllDrones);
        eventBus.consumer("1007", this::getDronesAvailableForLoading);
        eventBus.consumer("1009", this::getDroneBatteryLevel);
        eventBus.consumer("1011", this::loadDrone);
        eventBus.consumer("1013", this::checkLoadedmedication);
        eventBus.consumer("1015", this::completeDroneDelivery);
        eventBus.consumer("1017", this::getEventLog);
    }

    public void registerController(Message<Object> message) {
        JSONObject response = new JSONObject();
        try {
            JSONObject data = new JSONObject(message.body().toString());

            // pass validation
            ValidationService validity = new ValidationService();
            JSONObject validation = validity.doRegistrationValidation(data);

            if ("pass".equalsIgnoreCase(validation.getString("validation"))) {
                DroneService ds = new DroneService();
                int status = ds.registerDrone(data);

                if (status == 1) {
                    response.put("respose_code", "000");
                    response.put("response_message", "Drone registered successfully.");
                    message.reply(response.toString());
                } else {
                    response.put("respose_code", "001");
                    response.put("response_message", "Failed! Drone could not be registered.");
                    message.reply(response.toString());
                }
            } else {
                message.reply(validation.toString());
            }
        } catch (JSONException e) {
            response.put("respose_code", "001");
            response.put("response_message", "Failed.");
            message.reply(response.toString());
        }
    }

    public void changeDroneStatus(Message<Object> message) {
        JSONObject response = new JSONObject();
        try {
            JSONObject data = new JSONObject(message.body().toString());
            String serialNumber = data.getString("serial_number");

            // check if drone exists
            if (ValidationService.checkDroneExistence(serialNumber)) {
                DroneService ds = new DroneService();
                JSONObject drone = ds.getDrone(serialNumber);
                String currentState = drone.getString("state");
                String nextState = DroneService.getNextDroneState(currentState);

                //update table
                int status = ds.updateDroneStatus(serialNumber, nextState);
                if (status == 1) {
                    response.put("respose_code", "000");
                    response.put("serial_number", serialNumber);
                    response.put("previous_state", currentState);
                    response.put("current_state", nextState);
                    response.put("response_message", "Drone status changed.");
                    message.reply(response.toString());
                } else {
                    response.put("respose_code", "001");
                    response.put("response_message", "Failed! Drone state could not be updated.");
                    message.reply(response.toString());
                }
                message.reply(drone.toString());
            } else {
                response.put("respose_code", "001");
                response.put("response_message", "We could not find entered drone! Check SN and try again!");
                message.reply(response.toString());
            }

        } catch (JSONException e) {
            logger.applicationLog(logger.logPreString() + " changeDroneStatus - " + e.getMessage() + "\n\n", "", 3);
            response.put("respose_code", "001");
            response.put("response_message", "Failed.");
            message.reply(response.toString());
        }
    }

    public void getAllDrones(Message<Object> message) {
        JSONObject response = new JSONObject();
        try {
            DroneService ds = new DroneService();
            JSONObject list = ds.getAllDrone();
            message.reply(list.toString());
        } catch (JSONException e) {
            logger.applicationLog(logger.logPreString() + " getAllDrones - " + e.getMessage() + "\n\n", "", 3);
            response.put("respose_code", "001");
            response.put("response_message", "Failed.");
            message.reply(response.toString());
        }
    }

    public void getDronesAvailableForLoading(Message<Object> message) {
        JSONObject response = new JSONObject();
        try {
            DroneService ds = new DroneService();
            JSONObject list = ds.getAllDroneAvailableForLoading();
            message.reply(list.toString());
        } catch (JSONException e) {
            logger.applicationLog(logger.logPreString() + " get Available Drone - " + e.getMessage() + "\n\n", "", 3);
            response.put("respose_code", "001");
            response.put("response_message", "Failed.");
            message.reply(response.toString());
        }
    }

    public void getDroneBatteryLevel(Message<Object> message) {
        JSONObject response = new JSONObject();
        try {
            JSONObject data = new JSONObject(message.body().toString());

            String serialNumber = data.getString("serial_number");

            if (ValidationService.checkDroneExistence(serialNumber)) {
                DroneService ds = new DroneService();
                JSONObject result = ds.getDroneBatteryLevel(serialNumber);
                message.reply(result.toString());

            } else {
                response.put("respose_code", "001");
                response.put("response_message", "We could not find entered drone! Check SN and try again!");
                message.reply(response.toString());
            }

        } catch (JSONException e) {
            logger.applicationLog(logger.logPreString() + " Drone Battery Level - " + e.getMessage() + "\n\n", "", 3);
            response.put("respose_code", "001");
            response.put("response_message", "Failed.");
            message.reply(response.toString());
        }
    }

    public void loadDrone(Message<Object> message) {
        JSONObject response = new JSONObject();
        try {
            JSONObject data = new JSONObject(message.body().toString());
            String serialNumber = data.getString("serial_number");

            if (ValidationService.checkDroneExistence(serialNumber)) {

                //validate logging
                ValidationService vs = new ValidationService();
                JSONObject validation = vs.validateLoading(data);
                if ("pass".equalsIgnoreCase(validation.getString("validation"))) {
                    // save drone action
                    DroneService ds = new DroneService();
                    int status = ds.saveLoadedMedication(data);
                    if (status == 1) {
                        response.put("respose_code", "000");
                        response.put("response_message", "Drone loaded successfully.");
                        message.reply(response.toString());
                    } else {
                        response.put("respose_code", "001");
                        response.put("response_message", "Failed! Drone could not be loaded.");
                        message.reply(response.toString());
                    }

                } else {
                    message.reply(validation.toString());
                }

            } else {
                response.put("respose_code", "001");
                response.put("response_message", "We could not find entered drone! Check SN and try again!");
                message.reply(response.toString());
            }
        } catch (JSONException e) {
            logger.applicationLog(logger.logPreString() + " Load drone - " + e.getMessage() + "\n\n", "", 3);
            response.put("respose_code", "001");
            response.put("response_message", "Failed.");
            message.reply(response.toString());
        }
    }

    public void checkLoadedmedication(Message<Object> message) {
        JSONObject response = new JSONObject();
        try {
            JSONObject data = new JSONObject(message.body().toString());
            String serialNumber = data.getString("serial_number");

            if (ValidationService.checkDroneExistence(serialNumber)) {
                DroneService ds = new DroneService();
                JSONObject result = ds.checkLoadedMedication(serialNumber);
                message.reply(result.toString());

            } else {
                response.put("respose_code", "001");
                response.put("response_message", "We could not find entered drone! Check SN and try again!");
                message.reply(response.toString());
            }

        } catch (JSONException e) {
            logger.applicationLog(logger.logPreString() + " checkLoadedmedication - " + e.getMessage() + "\n\n", "", 3);
            response.put("respose_code", "001");
            response.put("response_message", "Failed.");
            message.reply(response.toString());
        }
    }

    public void completeDroneDelivery(Message<Object> message) {
        JSONObject response = new JSONObject();
        try {
            JSONObject data = new JSONObject(message.body().toString());
            String serialNumber = data.getString("serial_number");

            // check if drone exists
            if (ValidationService.checkDroneExistence(serialNumber)) {
                DroneService ds = new DroneService();
                JSONObject drone = ds.getDrone(serialNumber);
                String currentState = drone.getString("state");

                String nextState = String.valueOf(Enumerations.STATE.IDLE);

                //update table
                int status = ds.completeDroneDelivery(serialNumber, nextState);
                if (status == 1) {
                    response.put("respose_code", "000");
                    response.put("serial_number", serialNumber);
                    response.put("previous_state", currentState);
                    response.put("current_state", nextState);
                    response.put("response_message", "Drone status changed.");
                    message.reply(response.toString());
                } else {
                    response.put("respose_code", "001");
                    response.put("response_message", "Failed! Drone state could not be updated.");
                    message.reply(response.toString());
                }
                message.reply(drone.toString());
            } else {
                response.put("respose_code", "001");
                response.put("response_message", "We could not find entered drone! Check SN and try again!");
                message.reply(response.toString());
            }

        } catch (JSONException e) {
            logger.applicationLog(logger.logPreString() + " completeDroneDelivery - " + e.getMessage() + "\n\n", "", 3);
            response.put("respose_code", "001");
            response.put("response_message", "Failed.");
            message.reply(response.toString());
        }
    }

    public void getEventLog(Message<Object> message) {

        JSONObject data = new JSONObject(message.body().toString());

        JSONObject response = new JSONObject();

        try {
            String serialNumber = data.getString("serial_number");
            DroneService ds = new DroneService();
            JSONObject list = ds.getEventLogList(serialNumber);
            message.reply(list.toString());

        } catch (JSONException e) {
            logger.applicationLog(logger.logPreString() + " getEventLog - " + e.getMessage() + "\n\n", "", 3);
            response.put("respose_code", "001");
            response.put("response_message", "Failed.");
            message.reply(response.toString());
        }
    }
}
