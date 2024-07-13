package gui;

import API.api;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The DroneDynamicsApp class provides methods for interacting with an API to retrieve
 * dynamic information about drones, such as their ID, battery status, and current dynamics.
 * It also includes a nested DroneDynamics class that encapsulates drone dynamics attributes
 * using JavaFX properties for easy integration with UI components.
 * <p>
 * The class includes methods for:
 * -id: Retrieving the ID of a drone from a given drone type URL.
 * -Battery: Calculating and returning the battery percentage based on current power and battery capacity.
 * <p>
 * The nested DroneDynamics class represents drone dynamics with attributes
 * <p>
 * External dependencies include an API wrapper (api) for making HTTP requests and handling JSON responses.
 * The API endpoint and authentication details are specified within the initializeApi method.
 */

public class DroneDynamicsApp  {

    public static Integer id(String dronetypeUrl) throws IOException {
        // Extract the relevant part of the URL
        String[] urlParts = dronetypeUrl.split("/");
        String apiEndpoint = "/api/drones/" + urlParts[urlParts.length - 1]+"/";
        // Initialize the API connection
        api myApi4 = new api("/api/drones/", "http://dronesim.facets-labs.com", "Token 40a9557fac747f55c11ad20c85caac1d43654911", "Louay");
        myApi4.createConnection(apiEndpoint);
        String response4 = myApi4.retrieveResponse();
        // Parse the JSON response
        JSONObject responseObject = new JSONObject(response4);
        // Extract manufacturer and typename
        Integer id = responseObject.getInt("id");
        return id;
    }
    public static int Battery(String dronetypeUrl,int power) throws IOException {

        String[] urlParts = dronetypeUrl.split("/");
        String apiEndpoint = "/api/drones/" + urlParts[urlParts.length - 1]+"/";
        // Initialize the API connection
        api myApi4 = new api("/api/drones/", "http://dronesim.facets-labs.com", "Token 40a9557fac747f55c11ad20c85caac1d43654911", "Louay");
        myApi4.createConnection(apiEndpoint);
        String response4 = myApi4.retrieveResponse();
        // Parse the JSON response
        JSONObject responseObject = new JSONObject(response4);
        // Extract manufacturer and typename
        String dronetypeurl1 = responseObject.getString("dronetype");

        String[] urlParts2 = dronetypeurl1.split("/");

        String apiEndpoint2 = "/api/dronetypes/" + urlParts2[urlParts2.length - 1]+"/";
        // Initialize the API connection
        api myApi5 = new api("/api/dronetypes/", "http://dronesim.facets-labs.com", "Token 40a9557fac747f55c11ad20c85caac1d43654911", "Louay");
        myApi5.createConnection(apiEndpoint2);
        String response5 = myApi5.retrieveResponse();
        // Parse the JSON response
        JSONObject responseObject1 = new JSONObject(response5);

        int battery_capacity = responseObject1.getInt("battery_capacity");

        if (battery_capacity == 0) {
            // Handle division by zero or invalid battery capacity appropriately
            return 0;
        }

        float batteryPercentage = ((float) power * 100 / battery_capacity);
        return Math.round(batteryPercentage);
    }



    public static class DroneDynamics {
        private final SimpleIntegerProperty drone;
        private final SimpleStringProperty timestamp;
        private final SimpleIntegerProperty speed;
        private final SimpleDoubleProperty alignRoll;
        private final SimpleDoubleProperty alignPitch;
        private final SimpleDoubleProperty alignYaw;
        private final SimpleDoubleProperty longitude;
        private final SimpleDoubleProperty latitude;
        private final SimpleIntegerProperty batteryStatus;
        private final SimpleStringProperty lastSeen;
        private final SimpleStringProperty status;


        public DroneDynamics(int drone, String timestamp, int speed, double alignRoll, double alignPitch, double alignYaw, double longitude, double latitude, int batteryStatus, String lastSeen, String status ) {
            this.drone = new SimpleIntegerProperty(drone);
            this.timestamp = new SimpleStringProperty(timestamp);
            this.speed = new SimpleIntegerProperty(speed);
            this.alignRoll = new SimpleDoubleProperty(alignRoll);
            this.alignPitch = new SimpleDoubleProperty(alignPitch);
            this.alignYaw = new SimpleDoubleProperty(alignYaw);
            this.longitude = new SimpleDoubleProperty(longitude);
            this.latitude = new SimpleDoubleProperty(latitude);
            this.batteryStatus = new SimpleIntegerProperty(batteryStatus);
            this.lastSeen = new SimpleStringProperty(lastSeen);
            this.status = new SimpleStringProperty(status);

        }


        public int getDrone() {
            return drone.get();
        }

        public String getTimestamp() {
            LocalDateTime dateTime = LocalDateTime.parse(timestamp.get(), DateTimeFormatter.ISO_DATE_TIME);

            // Define a date-time formatter with the desired format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMMM-dd    HH : mm : ss . SSSSSS");

            // Format the current date-time and return the formatted string
            return dateTime.format(formatter);
        }

        public int getSpeed() {
            return speed.get();
        }

        public double getAlignRoll() {
            return alignRoll.get();
        }

        public double getAlignPitch() {
            return alignPitch.get();
        }

        public double getAlignYaw() {
            return alignYaw.get();
        }

        public double getLongitude() {
            return longitude.get();
        }

        public double getLatitude() {
            return latitude.get();
        }

        public int getBatteryStatus() {
            return batteryStatus.get();
        }

        public String getLastSeen() {
            LocalDateTime dateTime = LocalDateTime.parse(lastSeen.get(), DateTimeFormatter.ISO_DATE_TIME);

            // Define a date-time formatter with the desired format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMMM-dd     HH : mm : ss . SSSSSS");

            // Format the current date-time and return the formatted string
            return dateTime.format(formatter);
        }

        public String getStatus() {
            return status.get();
        }
    }

}