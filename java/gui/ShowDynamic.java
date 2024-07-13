package gui;

import API.api;
import error.ErrorHandler;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import static Animation.LoadingTask.showLoadingPopup;
import static Animation.LoadingTask.showLoadingPopup2;
import static gui.DroneDynamicsApp.DroneDynamics;
import static gui.DroneDynamicsApp.id;
import static gui.DroneSimulatorGUI.*;

/**
 * The ShowDynamic class manages the display of real-time drone dynamics information.
 * It includes functionalities for searching, displaying details, and navigating through drone data.
 * Responsibilities:
 * - Displays real-time drone dynamics information in a scrollable layout.
 * - Supports searching and filtering drone IDs.
 * - Provides navigation controls (Next, Previous, Last) to browse through data pages.
 * - Handles refreshing of drone data and updating UI accordingly.
 * - Creates a dashboard toolbar with navigation and refresh options.
 * - Displays drone details including ID, status, battery, speed, and location.
 * - Provides a link to open drone location on Google Maps.
 */


public class ShowDynamic {
    private Map<String, DroneDynamics> droneDataMap;
    private Label idLabel;
    private Label timeLabel;
    private Label statusLabel;
    private Label batteryLabel;
    private Label speedLabel;
    private Label yawLabel;
    private Label pitchLabel;
    private Label rollLabel;
    private Label longitudeLabel;
    private Label latitudeLabel;
    private Label timestampLabel;
    private Label lastSeenLabel;
    private int offset = 0;
    private static final int LIMIT = 10;
    private static final int MAX_OFFSET = 2140;
    private ChoiceBox<String> choiceBox;
    private ChoiceBox<Integer> numberChoiceBox;
    private Hyperlink googleMapsLink;
    private static int totalDrones = 0;
    private ImageView droneImageView;
    private List<String> allDroneIDs;
    private final AtomicBoolean cancelFetch = new AtomicBoolean(false);
    ObservableList<String> droneIds = FXCollections.observableArrayList();
    private CompletableFuture<Void> currentFetchTask;

    public ShowDynamic() {
        // Initialize all drone ID dynamics  from 1 to 2145
        allDroneIDs = new ArrayList<>();
        for (int i = 1; i <= 2145; i++) {
            allDroneIDs.add(String.valueOf(i));
        }
    }
    public void showDynamicPage(Stage primaryStage) {
        primaryStage.setTitle("Drone Dynamics Information");

        droneDataMap = new HashMap<>();
        VBox dashboard = createDashboardDynamic(primaryStage);

        TextField searchField = new TextField();
        searchField.setPromptText("Search Drone ID");
        searchField.setPrefWidth(50);
        searchField.setOnKeyPressed(event ->{
            if (event.getCode() == KeyCode.ENTER) {
                String row= searchField.getText();
                if (row =="0"||row.isEmpty()||Integer.parseInt(row)>2145){
                    showAutoClosingErrorPopup(primaryStage, "we can't find this number ", "ERROR", " Please try other one.");
                }else {
                    droneDataMap.clear();
                    choiceBox.getItems().clear();
                    offset=Integer.parseInt(row);
                    totalDrones=offset-1 ;
                    try {
                        showLoadingPopup2();
                        refreshDroneData(numberChoiceBox.getValue(), offset-1, true);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        choiceBox = new ChoiceBox<>();
        choiceBox.setPrefWidth(200);
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                showDroneDetails(newValue);
            }

        });


        numberChoiceBox = new ChoiceBox<>();
        ObservableList<Integer> numbers = FXCollections.observableArrayList();
        for (int i = 31; i <= 80; i++) {
            numbers.add(i);
        }
        numberChoiceBox.setItems(numbers);
        numberChoiceBox.setValue(31);
        numberChoiceBox.setPrefWidth(100);
        numberChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                choiceBox.getItems().clear();
                droneDataMap.clear();
                // Cancel ongoing fetch operation
                if (currentFetchTask != null) {
                    cancelFetch.set(true);
                }
                choiceBox.getItems().clear();
                totalDrones = 0;
                offset = 0;
                try {
                    refreshDroneData(newValue, offset, true);
                    showLoadingPopup2();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        VBox droneDetails = new VBox(20);
        droneDetails.setPadding(new Insets(20));
        droneDetails.setAlignment(Pos.BASELINE_LEFT);

        droneImageView = new ImageView();
        droneImageView.setFitWidth(500);
        droneImageView.setFitHeight(500);
        droneImageView.setPreserveRatio(true);

        VBox detailsBox = new VBox(10);

        idLabel = createIconLabel("/image/id.png", "Drone ID: ");
        timeLabel = createIconLabel("/image/time.png", "Time: ");
        statusLabel = createIconLabel("/image/status.png", "Status: ");
        batteryLabel = createIconLabel("/image/battery.png", "Battery: ");
        speedLabel = createIconLabel("/image/speed.png", "Speed: ");
        yawLabel = createIconLabel("/image/yaw.png", "Yaw: ");
        pitchLabel = createIconLabel("/image/pitch.png", "Pitch: ");
        rollLabel = createIconLabel("/image/roll.png", "Roll: ");
        longitudeLabel = createIconLabel("/image/longitude.png", "Longitude: ");
        latitudeLabel = createIconLabel("/image/latitude.png", "Latitude: ");
        lastSeenLabel = createIconLabel("/image/timestamp.png", "Last Seen: ");
        Label googleMapsLabel = createIconLabel("/image/google-maps.png", "Open in Maps to see the Location: ");
        googleMapsLink = new Hyperlink();
        googleMapsLink.setOnAction(event -> {
            String url = googleMapsLink.getText();
            if (!url.isEmpty()) {
                try {
                    DroneSimulatorGUI.getHostServicesInstance().showDocument(url);
                } catch (IllegalStateException e) {
                    System.err.println("HostServices not initialized: " + e.getMessage());
                }
            }
        });

        detailsBox.getChildren().addAll(idLabel, timeLabel, statusLabel, batteryLabel, speedLabel, yawLabel, pitchLabel, rollLabel, longitudeLabel, latitudeLabel, lastSeenLabel, googleMapsLabel, googleMapsLink);

        HBox combinedBox = new HBox(20, detailsBox, droneImageView);
        combinedBox.setAlignment(Pos.CENTER_LEFT);

        droneDetails.getChildren().add(combinedBox);

        ScrollPane scrollPane = new ScrollPane(droneDetails);
        scrollPane.setFitToWidth(true);

        Button btnRefresh = new Button("Refresh");
        btnRefresh.setOnAction(event -> {
            try {
                refreshDroneData(numberChoiceBox.getValue(), offset, false); // Do not reset choice box
            } catch (IOException e) {
                ErrorHandler.handleIOException(e);
            }
        });

        Button btnNext = new Button("Next");
        btnNext.setOnAction(e -> {
            if (offset + LIMIT <= MAX_OFFSET) {
                offset += LIMIT;
                choiceBox.getItems().clear();
                droneDataMap.clear();
                totalDrones+=10;
                try {
                    refreshDroneData(numberChoiceBox.getValue(), offset, true);// Reset choice box
                    showLoadingPopup2();
                } catch (IOException ex) {
                    ErrorHandler.handleIOException(ex);
                }
            }else {System.out.println("Invalid credentials");
                showAutoClosingErrorPopup(primaryStage, "This is already the Last page", "ERROR", " Please try other page.");
            }
        });

        Button btnPrevious = new Button("Previous");
        btnPrevious.setOnAction(e -> {
            if (offset - LIMIT >= 0) {
                offset -= LIMIT;
                choiceBox.getItems().clear();
                droneDataMap.clear();
                try {
                    if (totalDrones==2140){totalDrones-=10;}
                    else totalDrones -= 10;
                    refreshDroneData(numberChoiceBox.getValue(), offset, true);
                    showLoadingPopup2();
                } catch (IOException ex) {
                    ErrorHandler.handleIOException(ex);
                }
            }else {System.out.println("Invalid credentials");
                showAutoClosingErrorPopup(primaryStage, "This is already the first page", "ERROR", " Please try other page.");
            }
        });

        Button btnLast = new Button("Last");
        btnLast.setOnAction(e -> {
            offset = 2140;
            totalDrones=2140;
            choiceBox.getItems().clear();
            droneDataMap.clear();
            try {
                showLoadingPopup2();
                refreshDroneData(numberChoiceBox.getValue(), offset, true);
            } catch (IOException ex) {
                ErrorHandler.handleIOException(ex);
            }
        });

        HBox nextButtonBox = new HBox(10, btnPrevious, btnNext, btnLast);
        nextButtonBox.setAlignment(Pos.BOTTOM_LEFT);
        nextButtonBox.setPadding(new Insets(20));

        VBox mainLayout = new VBox(10, dashboard, searchField, choiceBox, numberChoiceBox, scrollPane, nextButtonBox);
        mainLayout.setSpacing(10);
        mainLayout.setPadding(new Insets(10));

        ScrollPane mainScrollPane = new ScrollPane(mainLayout);
        mainScrollPane.setFitToWidth(true);

        BorderPane root = new BorderPane();
        root.setCenter(mainScrollPane);

        Scene dynamicScene = new Scene(root, 1300, 1200);
        primaryStage.centerOnScreen();
        primaryStage.setScene(dynamicScene);
        primaryStage.show();

        try {
            refreshDroneData(numberChoiceBox.getValue(), offset, true);
            showLoadingPopup2();
        } catch (IOException e) {
            ErrorHandler.handleIOException(e);
        }

    }


    static void showAutoClosingErrorPopup(Stage primaryStage, String title, String headerText, String contentText) {
        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox popupContent = new VBox(10);
        popupContent.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: black; -fx-border-width: 1;");
        popupContent.setAlignment(Pos.CENTER);

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-weight: bold;");

        Label lblHeader = new Label(headerText);
        lblHeader.setStyle("-fx-text-fill: red;");

        Label lblContent = new Label(contentText);

        popupContent.getChildren().addAll(lblTitle, lblHeader, lblContent);
        popup.getContent().add(popupContent);

        // Show the popup in the center of the primary stage
        popup.show(primaryStage);

        // Close the popup after 1 second
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> popup.hide());
        delay.play();
    }

    private VBox createDashboardDynamic(Stage primaryStage) {
        MenuButton btnMenu = setMenuButtonGraphics("Menu", "/image/menu.png");

        MenuItem dynamicItem = new MenuItem("Drone Dynamic");
        styleMenuItem(dynamicItem);
        dynamicItem.setOnAction(event -> new ShowDynamic().showDynamicPage(primaryStage));


        MenuItem catalogueItem = new MenuItem("Drone Catalogue");
        styleMenuItem(catalogueItem);
        catalogueItem.setOnAction(event -> {
            try {
                new ShowCatalogue().showCataloguePage(primaryStage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        MenuItem historyItem = new MenuItem("Drone History");
        styleMenuItem(historyItem);
        historyItem.setOnAction(event -> {
            try {
                new ShowHistory().showHistoryPage(primaryStage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        btnMenu.getItems().addAll(dynamicItem, catalogueItem, historyItem);

        Button btnLogout = createToolbarButton("Logout", "/image/Logout.png");
        btnLogout.setOnAction(event -> showLoginPage(primaryStage));

        Button btnRefresh = createToolbarButton("Refresh", "/image/refresh.png");
        btnRefresh.setOnAction(event -> {
            showLoadingPopup();
            try {
                refreshDroneData(numberChoiceBox.getValue(), offset, true);
                showDynamicPage(primaryStage);
                primaryStage.centerOnScreen();
            } catch (IOException e) {
                ErrorHandler.handleIOException(e);
            }
        });


        Button btnBack = createToolbarButton("back", "/image/back.png");
        btnBack.setOnAction(event -> {
            totalDrones = 0;
            primaryStage.centerOnScreen();
            showMenu(primaryStage);
        });

        Button btnGame = createToolbarButton("Game", "/image/game.png");
        btnGame.setOnAction(e -> {
            startGame(primaryStage);
            primaryStage.centerOnScreen();
        });

        HBox hbox = new HBox(btnMenu, btnLogout, btnRefresh, btnBack,btnGame);
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(150);
        hbox.setPadding(new Insets(5));

        ToolBar toolbar = new ToolBar();
        toolbar.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        toolbar.getItems().add(hbox);
        toolbar.setOpacity(1.0);

        VBox vbox = new VBox(toolbar);
        vbox.setSpacing(0);

        return vbox;
    }

    private void showDroneDetails(String droneId) {
        if (droneDataMap.containsKey(droneId)) {
            DroneDynamics drone = droneDataMap.get(droneId);
            if (drone != null) {
                idLabel.setText("Drone ID: " + drone.getDrone());
                timeLabel.setText("Time Stamp: " + drone.getTimestamp());
                statusLabel.setText("Status: " + drone.getStatus());
                batteryLabel.setText("Battery: " + drone.getBatteryStatus()+ "%");
                speedLabel.setText("Speed: " + drone.getSpeed() + " Km/h");
                yawLabel.setText("Yaw: " + drone.getAlignYaw());
                pitchLabel.setText("Pitch: " + drone.getAlignPitch());
                rollLabel.setText("Roll: " + drone.getAlignRoll());
                longitudeLabel.setText("Longitude: " + drone.getLongitude());
                latitudeLabel.setText("Latitude: " + drone.getLatitude());
                lastSeenLabel.setText("Last Seen: " + drone.getLastSeen());

                String latitude = String.valueOf(drone.getLatitude());
                String longitude = String.valueOf(drone.getLongitude());
                String googleMapsUrl = String.format("https://www.google.com/maps/search/?api=1&query=%s,%s", latitude, longitude);
                googleMapsLink.setText(googleMapsUrl);

                String batteryImagePath = getBatteryImagePath(drone.getBatteryStatus());
                Image batteryImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(batteryImagePath)));
                ImageView batteryImageView = new ImageView(batteryImage);
                batteryImageView.setFitWidth(50);
                batteryImageView.setFitHeight(50);
                batteryImageView.setPreserveRatio(true);
                batteryLabel.setGraphic(batteryImageView);

                String imagePath = "/imagedrone/" + drone.getDrone() + ".png";
                Image droneImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
                droneImageView.setImage(droneImage);
            }
        }
    }




    private Label createIconLabel(String iconFileName, String text) {
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconFileName)));
        ImageView iconView = new ImageView(icon);
        Label label = new Label(text, iconView);
        iconView.setFitWidth(50);
        iconView.setFitHeight(50);
        iconView.setPreserveRatio(true);
        iconView.setSmooth(true);
        label.setContentDisplay(ContentDisplay.LEFT);
        return label;
    }

    private void refreshDroneData(int numberOfDrones, int offset, boolean resetChoiceBox) throws IOException {

        if (droneDataMap.isEmpty()) {  // Cancel ongoing fetch operation
            if (currentFetchTask != null) {
                cancelFetch.set(true);
            }
            // Clear previous drone data
            droneDataMap.clear();
            choiceBox.getItems().clear();
            // Start new fetch operation
            currentFetchTask = CompletableFuture.runAsync(() -> {
                try {
                    fetchAndProcessData(numberOfDrones, offset, resetChoiceBox);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    cancelFetch.set(false);
                }
            });
        }else {
            // If not empty, update UI with existing data
            Platform.runLater(() -> {
                choiceBox.getItems().addAll(droneDataMap.keySet());
                // Optionally, update UI with the first drone's details
                if (!droneDataMap.isEmpty()) {
                    showDroneDetails(droneDataMap.keySet().iterator().next());
                }
            });
        }
    }



    private void fetchAndProcessData(int number, int offset, boolean resetChoiceBox) throws IOException {
        String endpoint = "/api/" + number + "/dynamics/";
        String domain = "http://dronesim.facets-labs.com";
        String token = "Token 40a9557fac747f55c11ad20c85caac1d43654911";
        String agent = "Louay";

        api myApi = new api(endpoint, domain, token, agent);
        myApi.createConnection(endpoint + "?limit=" + LIMIT + "&offset=" + offset);
        String response = myApi.retrieveResponse();

        JSONArray drones = new JSONObject(response).getJSONArray("results");
        for (int i = 0; i < drones.length(); i++) {
            JSONObject droneJson = drones.getJSONObject(i);
            int id = id(droneJson.getString("drone"));
            int Battery = DroneDynamicsApp.Battery(droneJson.getString("drone"), droneJson.getInt("battery_status"));

            DroneDynamics droneDynamics = new DroneDynamics(
                    id,
                    droneJson.getString("timestamp"),
                    droneJson.getInt("speed"),
                    droneJson.getDouble("align_roll"),
                    droneJson.getDouble("align_pitch"),
                    droneJson.getDouble("align_yaw"),
                    droneJson.getDouble("longitude"),
                    droneJson.getDouble("latitude"),
                    Battery,
                    droneJson.getString("last_seen"),
                    droneJson.getString("status")
            );
            if (resetChoiceBox) {
                int droneNumber = totalDrones + i + 1;
                choiceBox.getItems().addAll(String.valueOf(droneNumber));
            }

            String droneNumber = String.valueOf(totalDrones + i + 1);
            droneDataMap.put(droneNumber, droneDynamics);
        }

        // Save data to JSON file
        String jsonString = drones.toString();
        String filename = number + ".json";
        saveJsonToFile(filename, jsonString);
    }

    private void saveJsonToFile(String filename, String jsonString) throws IOException {
        Path filePath = Paths.get(filename);
        JSONArray newArray = new JSONArray(jsonString);

        if (Files.exists(filePath)) {
            // Read existing content
            String existingJsonString = new String(Files.readAllBytes(filePath));
            JSONArray existingArray = new JSONArray(existingJsonString);

            // Add only new items that don't already exist
            for (int i = 0; i < newArray.length(); i++) {
                JSONObject newObject = newArray.getJSONObject(i);
                boolean exists = false;

                // Assuming 'id' is a unique identifier for the drone object
                for (int j = 0; j < existingArray.length(); j++) {
                    JSONObject existingObject = existingArray.getJSONObject(j);
                    if (Objects.equals(newObject.getString("last_seen"), existingObject.getString("last_seen"))) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    existingArray.put(newObject);
                }
            }

            // Convert combined JSONArray back to String
            jsonString = existingArray.toString();
        }

        // Write combined content (existing + new) to file
        Files.write(filePath, jsonString.getBytes(), StandardOpenOption.CREATE);
    }


    private String getBatteryImagePath(int batteryStatus) {
        if (batteryStatus >= 80) {
            return "/image/battery1.png";
        } else if (batteryStatus >= 50) {
            return "/image/battery2.png";
        } else if (batteryStatus >= 20) {
            return "/image/battery3.png";
        } else if (batteryStatus >= 10) {
            return "/image/battery4.png";
        } else {
            return "/image/battery5.png";
        }
    }





}