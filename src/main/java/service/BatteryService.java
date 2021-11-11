/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import database.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.Logging;

/**
 *
 * @author ronald.langat
 */
public class BatteryService {

    private final Logging logger;

    public BatteryService() {
        logger = new Logging();
    }

    public void checkDronesBatteryLevels() {
        /// Get all drones
        String sql = "SELECT drone_id,serial_number,model,weight_limit,battery_capacity,state FROM drone";

        DBConnection conn = new DBConnection();
        try {
            ResultSet rs = conn.query_all(sql);

            int maximum = 100;
            int threshold = 25;
            int minimum = 0;

            int newCapacity = threshold;
            while (rs.next()) {
                String serialNumber = rs.getString("serial_number");
                int capacity = rs.getInt("battery_capacity");

                if (capacity <= maximum) {
                    newCapacity = capacity - 10;
                }

                if (newCapacity <= minimum) {
                    newCapacity = maximum;
                }

                // Update anf log in db
                DroneService ds = new DroneService();
                ds.updateBatteryCapacity(serialNumber, capacity, newCapacity);

                // log in file
                logger.applicationLog(logger.logPreString() + "Battery - " + serialNumber + " - PC: " + capacity + " - CC:" + newCapacity + "\n\n", "", 5);
            }
        } catch (SQLException e) {
            logger.applicationLog(logger.logPreString() + "Error checkDronesBatteryLevels - " + e.getMessage() + "\n\n", "", 3);
        } finally {
            conn.closeConn();
        }

    }
}
