package gui;

import API.api;
import error.ErrorHandler;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static Animation.LoadingTask.showLoadingPopup;
import static gui.DroneSimulatorGUI.*;

/**
 * The ShowCatalogue class displays a catalog of drone types fetched from an API.
 * It includes a TableView to display drone attributes and a toolbar for navigation.
 * The data is fetched asynchronously from the API and displayed in the table.
 * This is what this class Do:
 * Displays the catalogue page with a TableView populated with drone types.
 * Fetches drone type data from an API and populates the TableView with the retrieved data.
 * Creates the dashboard toolbar for the catalogue page.
 * Creates a sub-button for the submenu in the dashboard toolbar.
 *Interface to handle actions for sub-buttons in the dashboard toolbar.
 *
 */

public class ShowCatalogue {
    void showCataloguePage(Stage primaryStage) throws IOException {
        VBox dashboard = createDashboardCatalogue(primaryStage);

        TableView<DroneTypeApp.DroneType> table = new TableView<>();

        TableColumn<DroneTypeApp.DroneType, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<DroneTypeApp.DroneType, String> manufacturerColumn = new TableColumn<>("Manufacturer");
        manufacturerColumn.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));

        TableColumn<DroneTypeApp.DroneType, String> typenameColumn = new TableColumn<>("Type Name");
        typenameColumn.setCellValueFactory(new PropertyValueFactory<>("typename"));

        TableColumn<DroneTypeApp.DroneType, Integer> weightColumn = new TableColumn<>("Weight (g)");
        weightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));

        TableColumn<DroneTypeApp.DroneType, Integer> maxSpeedColumn = new TableColumn<>("Max Speed (km/h)");
        maxSpeedColumn.setCellValueFactory(new PropertyValueFactory<>("maxSpeed"));

        TableColumn<DroneTypeApp.DroneType, Integer> batteryCapacityColumn = new TableColumn<>("Battery Capacity (mAh)");
        batteryCapacityColumn.setCellValueFactory(new PropertyValueFactory<>("batteryCapacity"));

        TableColumn<DroneTypeApp.DroneType, Integer> controlRangeColumn = new TableColumn<>("Control Range (m)");
        controlRangeColumn.setCellValueFactory(new PropertyValueFactory<>("controlRange"));

        TableColumn<DroneTypeApp.DroneType, Integer> maxCarriageColumn = new TableColumn<>("Max Carriage (g)");
        maxCarriageColumn.setCellValueFactory(new PropertyValueFactory<>("maxCarriage"));

        table.getColumns().addAll(idColumn, manufacturerColumn, typenameColumn, weightColumn, maxSpeedColumn, batteryCapacityColumn, controlRangeColumn, maxCarriageColumn);

        // Set preferred column widths for better spacing
        idColumn.setPrefWidth(60);
        manufacturerColumn.setPrefWidth(200);
        typenameColumn.setPrefWidth(300);
        weightColumn.setPrefWidth(160);
        maxSpeedColumn.setPrefWidth(155);
        batteryCapacityColumn.setPrefWidth(150);
        controlRangeColumn.setPrefWidth(150);
        maxCarriageColumn.setPrefWidth(100);

        table.setPrefHeight(510);
        // Set initial sorting by ID column
        idColumn.setSortType(TableColumn.SortType.ASCENDING);
        table.getSortOrder().add(idColumn);

        // Fetch new data from API asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                fetchAndPopulateTable(table);
                // Apply sorting once data is fetched

            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        });

        VBox vbox = new VBox(dashboard, table);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10));

        Scene catalogueScene = new Scene(vbox, 1300, 1200);
        primaryStage.centerOnScreen();
        primaryStage.setScene(catalogueScene);
        primaryStage.show();
    }

    private void fetchAndPopulateTable(TableView<DroneTypeApp.DroneType> table) throws IOException {
        String endpoint = "/api/dronetypes/";
        String domain = "http://dronesim.facets-labs.com";
        String token = "Token 40a9557fac747f55c11ad20c85caac1d43654911";
        String agent = "Louay";

        api myApi1 = new api(endpoint, domain, token, agent);
        myApi1.createConnection("/api/dronetypes/?limit=20");
        String response1 = myApi1.retrieveResponse();

        JSONArray drones = new JSONObject(response1).getJSONArray("results");

        // Clear previous items in the table
        Platform.runLater(() -> table.getItems().clear());

        for (int i = 0; i < drones.length(); i++) {
            JSONObject drone = drones.getJSONObject(i);
            DroneTypeApp.DroneType droneType = new DroneTypeApp.DroneType(
                    drone.getInt("id"),
                    drone.getString("manufacturer"),
                    drone.getString("typename"),
                    drone.getInt("weight"),
                    drone.getInt("max_speed"),
                    drone.getInt("battery_capacity"),
                    drone.getInt("control_range"),
                    drone.getInt("max_carriage")
            );

            // Add items to the table on the JavaFX Application Thread
            Platform.runLater(() -> table.getItems().add(droneType));
        }
    }

    private  VBox createDashboardCatalogue(Stage primaryStage) {
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
                showCataloguePage(primaryStage);
                primaryStage.centerOnScreen();
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        });

        Button btnBack = createToolbarButton("back", "/image/back.png");
        btnBack.setOnAction(event -> {
            primaryStage.centerOnScreen();
            DroneSimulatorGUI.showMenu(primaryStage);
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


}
