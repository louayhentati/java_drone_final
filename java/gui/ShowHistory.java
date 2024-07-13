package gui;

import API.api;
import error.ErrorHandler;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import static Animation.LoadingTask.showLoadingPopup;
import static Animation.LoadingTask.showLoadingPopup2;
import static gui.DroneSimulatorGUI.*;
import static gui.ShowDynamic.showAutoClosingErrorPopup;

/**
 * The ShowHistory class manages the display of historical drone data.
 * It includes functionalities for browsing through paginated drone data,
 * displaying detailed information, and navigating between pages.
 * Responsibilities:
 *     Displays historical drone data in a tabular format.
 *     Supports pagination with navigation controls (Next, Previous, Last) to browse through data pages.
 *     Handles refreshing of drone data and updating the UI accordingly.
 *     Creates a dashboard toolbar with navigation, menu, logout, and refresh options.
 *     Displays drone details including ID, drone type, manufacturer, created date, serial number, carriage weight, and carriage type.
 */

public class ShowHistory {
    private int offset = 0;
    private static final int LIMIT = 10;
    private static final int MAX_OFFSET = 50;
    private CompletableFuture<Void> currentFetchTask;
    ObservableList<DroneApp.Drone> lastAddedItems = null;

    void showHistoryPage(Stage primaryStage) throws IOException {
        VBox daschbord = createDashboardHistory(primaryStage);

        TextField searchManufacter = new TextField();
        searchManufacter.setPromptText("Search Manufacter");
        searchManufacter.setPrefWidth(150);

        TextField searchDroneType = new TextField();
        searchDroneType.setPromptText("Search Drone Type");
        searchDroneType.setPrefWidth(150);

        TableView<DroneApp.Drone> table = new TableView<>();

        TableColumn<DroneApp.Drone, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<DroneApp.Drone, String> dronetypeColumn = new TableColumn<>("Drone Type");
        dronetypeColumn.setCellValueFactory(new PropertyValueFactory<>("dronetype"));

        TableColumn<DroneApp.Drone, String> dronemanufacturerColumn = new TableColumn<>("Drone Manufacturer");
        dronemanufacturerColumn.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));

        TableColumn<DroneApp.Drone, String> createdColumn = new TableColumn<>("Created");
        createdColumn.setCellValueFactory(new PropertyValueFactory<>("created"));

        TableColumn<DroneApp.Drone, String> serialnumberColumn = new TableColumn<>("Serial Number");
        serialnumberColumn.setCellValueFactory(new PropertyValueFactory<>("serialnumber"));

        TableColumn<DroneApp.Drone, Integer> carriageWeightColumn = new TableColumn<>("Carriage Weight");
        carriageWeightColumn.setCellValueFactory(new PropertyValueFactory<>("carriageWeight"));

        TableColumn<DroneApp.Drone, String> carriageTypeColumn = new TableColumn<>("Carriage Type");
        carriageTypeColumn.setCellValueFactory(new PropertyValueFactory<>("carriageType"));

        table.getColumns().addAll(idColumn, dronetypeColumn, dronemanufacturerColumn, createdColumn, serialnumberColumn, carriageWeightColumn, carriageTypeColumn);

        fetchAndPopulateData(table, 10,offset);

        idColumn.setPrefWidth(50);
        dronetypeColumn.setPrefWidth(200);
        dronemanufacturerColumn.setPrefWidth(200);
        createdColumn.setPrefWidth(200);
        serialnumberColumn.setPrefWidth(250);
        carriageWeightColumn.setPrefWidth(100);
        carriageTypeColumn.setPrefWidth(150);

        table.setPrefHeight(275);

        searchManufacter.setOnKeyPressed(keyEvent ->{
            if (keyEvent.getCode() == KeyCode.ENTER) {
                if (searchManufacter.getText().isEmpty() && searchDroneType.getText().isEmpty()){
                    showAutoClosingErrorPopup(primaryStage, "we can't find this filter ", "ERROR", " Please try other one.");
                }else {
                    offset = 0;
                    fetchAndPopulateData(table,60 ,offset, searchManufacter.getText(),searchDroneType.getText());
                }


            }
        });

        searchDroneType.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                if (searchDroneType.getText().isEmpty() && searchManufacter.getText().isEmpty()) {
                    showAutoClosingErrorPopup(primaryStage, "Cannot find filter", "ERROR", "Please try another one.");
                } else {
                    offset = 0;
                    fetchAndPopulateData(table, 60, offset, searchManufacter.getText(), searchDroneType.getText());
                }
            }
        });

        Button btnNext = new Button("Next");
        btnNext.setOnAction(e -> {
            if (offset + LIMIT <= MAX_OFFSET) {
                offset += LIMIT;
                showLoadingPopup2();
                fetchAndPopulateData(table, 10,offset);
            } else {
                System.out.println("Invalid next page");
                showAutoClosingErrorPopup(primaryStage, "This is already the Last page", "ERROR", " Please try other page.");
            }
        });

        Button btnPrevious = new Button("Previous");
        btnPrevious.setOnAction(e -> {
            if (offset - LIMIT >= 0) {
                offset -= LIMIT;
                showLoadingPopup2();
                fetchAndPopulateData(table, 10,offset);
            } else {
                System.out.println("Invalid button, already at last page from prvious");
                showAutoClosingErrorPopup(primaryStage, "This is already the First page", "ERROR", "Please try the previous page.");
            }
        });

        Button btnLast = new Button("Last");
        btnLast.setOnAction(e -> {
            if (offset != MAX_OFFSET) {
                offset = MAX_OFFSET;
                showLoadingPopup2();
                fetchAndPopulateData(table, 10,offset);
            } else {
                System.out.println("Invalid button, already at last page");
                showAutoClosingErrorPopup(primaryStage, "This is already the Last page", "ERROR", " Please try the previous page.");
            }
        });

        HBox nextButtonBox = new HBox(10, btnPrevious, btnNext, btnLast);
        nextButtonBox.setAlignment(Pos.BOTTOM_LEFT);
        nextButtonBox.setPadding(new Insets(20));

        VBox vbox = new VBox(daschbord,searchManufacter,searchDroneType,table, nextButtonBox);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10));

        Scene historyScene = new Scene(vbox, 1300, 800);
        primaryStage.centerOnScreen();
        primaryStage.setScene(historyScene);
        primaryStage.show();
        showLoadingPopup2();
    }

    private void fetchAndPopulateData(TableView<DroneApp.Drone> table,int limit ,int offset, String manufacturerFilter,String droneTypeFilter) {
        if (currentFetchTask != null && !currentFetchTask.isDone()) {
            currentFetchTask.cancel(true);
        }

        currentFetchTask = CompletableFuture.runAsync(() -> {
            String endpoint = "/api/drone/";
            String domain = "http://dronesim.facets-labs.com";
            String token = "Token 40a9557fac747f55c11ad20c85caac1d43654911";
            String agent = "Louay";

            api myApi = new api(endpoint, domain, token, agent);

            try {
                myApi.createConnection("/api/drones/?limit=" + limit + "&offset=" + offset);
                String response = myApi.retrieveResponse();

                JSONArray drones = new JSONObject(response).getJSONArray("results");
                ObservableList<DroneApp.Drone> dronesList = table.getItems();
                dronesList.clear();

                for (int i = 0; i < drones.length(); i++) {
                    JSONObject drone = drones.getJSONObject(i);
                    String dronetypeUrl = drone.getString("dronetype");
                    String dronetypeName = DroneApp.NameDrone(dronetypeUrl);
                    String dronemanufacturer = DroneApp.manufacturerDrone(dronetypeUrl);

                    if (manufacturerFilter == null || dronemanufacturer.equalsIgnoreCase(manufacturerFilter) ||
                            (dronemanufacturer.startsWith(manufacturerFilter) &&
                            dronetypeName.startsWith(droneTypeFilter)) ||
                            (droneTypeFilter == null || dronetypeName.equalsIgnoreCase(droneTypeFilter))) {
                        dronesList.add(new DroneApp.Drone(
                                drone.getInt("id"),
                                dronetypeName,
                                dronemanufacturer,
                                drone.getString("created"),
                                drone.getString("serialnumber"),
                                drone.getInt("carriage_weight"),
                                drone.getString("carriage_type")));
                    }
                }
            } catch (IOException ex) {
                ErrorHandler.handleException(ex);
            }
        });
    }

    private void fetchAndPopulateData(TableView<DroneApp.Drone> table,int limit ,int offset) {
        fetchAndPopulateData(table, 10,offset, null,null);
    }


    private VBox createDashboardHistory(Stage primaryStage) {
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
        btnLogout.setOnAction(e -> showLoginPage(primaryStage));

        Button btnRefresh = createToolbarButton("Refresh", "/image/refresh.png");
        btnRefresh.setOnAction(e -> {
            showLoadingPopup();
            try {
                showHistoryPage(primaryStage);
            } catch (IOException er) {
                throw new RuntimeException(er);
            }
        });

        Button btnBack = createToolbarButton("back", "/image/back.png");
        btnBack.setOnAction(e -> {
            showMenu(primaryStage);
            primaryStage.centerOnScreen();
        });

        Button btnGame = createToolbarButton("Game", "/image/game.png");
        btnGame.setOnAction(e -> {
            startGame(primaryStage);
            primaryStage.centerOnScreen();
        });

        HBox hbox = new HBox(btnMenu, btnLogout, btnRefresh, btnBack, btnGame);
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
}
