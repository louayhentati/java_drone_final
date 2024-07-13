package gui;

import Animation.*;
import Login.*;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.InputStream;

import static Animation.LoadingTask.showLoadingPopup;

/**
 * The DroneSimulatorGUI class implements a JavaFX application for managing and interacting
 * with drone simulation and information. It includes functionalities for logging in, displaying
 * a main menu with various options, starting a drone simulation game, and providing information
 * about drone regulations and applications.
 * <p>
 * The class initializes and manages different scenes including:
 * - Login scene for authenticating users.
 * - Main menu scene with options for viewing drone dynamics, catalog, and history.
 * - Game scene for playing a drone simulation game.
 * - Detailed information about drone regulations and flight applications.
 * <p>
 * It uses JavaFX UI components such as Button, TextField, PasswordField, and ScrollPane for
 * user interaction. Images and animations are integrated using ImageView and custom animation
 * classes (e.g., DroneAnimation, Game).
 * <p>
 * External dependencies include custom classes for handling login (LoginManager), animations
 * (DroneAnimation), and game mechanics (Game).
 * <p>
 * The application utilizes HostServices to handle external web links and supports dynamic
 * loading and refreshing of content through buttons like Refresh and Menu.
 * <p>
 * Note: Ensure proper image paths are set for buttons and background images.
 */

public class DroneSimulatorGUI extends Application {
    private static Scene gameScene;
    private static Game game;
    private Scene mainScene;
    private Scene loginScene;
    private Stage primaryStage;
    private static int totalDrones = 0;
    private static HostServices hostServices;
    private java.awt.Label txtUsername;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Drone Application");
        hostServices = getHostServices(); // Initialize HostServices

        // Initialize and show the login page first
        showLoginPage(primaryStage);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private static VBox createDashboard(Stage primaryStage) {
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
        btnLogout.setOnAction(e-> showLoginPage(primaryStage));

        Button btnRefresh = createToolbarButton("Refresh", "/image/refresh.png");
        btnRefresh.setOnAction(e -> {
            showLoadingPopup();
            primaryStage.centerOnScreen();
            showMenu(primaryStage);
        });

        Button btnGame = createToolbarButton("Game", "/image/game.png");
        btnGame.setOnAction(e -> {
                startGame(primaryStage);
        primaryStage.centerOnScreen();
        });


        HBox hbox = new HBox(btnMenu, btnLogout, btnRefresh,btnGame);
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(200); // Set spacing between buttons
        hbox.setPadding(new Insets(5)); // Add padding around the HBox

        // Create a ToolBar and add the HBox to it
        ToolBar toolbar = new ToolBar();
        toolbar.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        toolbar.getItems().add(hbox);
        toolbar.setOpacity(1.0); // Set opacity of the toolbar


        // Add the VBox to the VBox containing the toolbar
        VBox vbox = new VBox(toolbar);
        vbox.setSpacing(10); // Add some space between toolbar and submenu items

        return vbox;
    }


    public static HostServices getHostServicesInstance() {
        if (hostServices == null) {
            throw new IllegalStateException("HostServices has not been initialized yet.");
        }
        return hostServices;
    }
    public static MenuButton setMenuButtonGraphics(String text, String imagePath) {
        MenuButton menuButton = new MenuButton(text);
        menuButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: black; -fx-font-size: 14px;");
        menuButton.setGraphicTextGap(10); // Set gap between text and image
        menuButton.setPadding(new Insets(5)); // Add padding inside the button

        // Load images
        InputStream inputStream = DroneSimulatorGUI.class.getResourceAsStream(imagePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Image not found: " + imagePath);
        }
        Image image = new Image(inputStream);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        menuButton.setGraphic(imageView);
        return menuButton;
    }

    static void styleMenuItem(MenuItem menuItem) {
        menuItem.setStyle("-fx-background-color: lightblue; -fx-text-fill: black; -fx-font-size: 14px;");
    }

    public static Button createToolbarButton(String text, String imagePath) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: black; -fx-font-size: 14px;");
        button.setGraphicTextGap(10); // Set gap between text and image
        button.setPadding(new Insets(5)); // Add padding inside the button

        ImageView imageView = new ImageView(new Image(imagePath));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        button.setGraphic(imageView);

        return button;
    }

    public static Button createSubButton(String text, Stage primaryStage, SubButtonActionHandler actionHandler) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: lightblue; -fx-border-color: black; -fx-text-fill: black; -fx-font-size: 14px;");
        button.setPadding(new Insets(0)); // Add padding inside the button
        button.setMaxWidth(150); // Make the button stretch to fill width
        totalDrones = 0;
        button.setOnAction(d -> {
            try {
                actionHandler.handle(primaryStage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return button;
    }

    public static void showLoginPage(Stage primaryStage) {
        LoginManager loginManager = new LoginManager();

        Label lblGroupName = new Label("Group Name:");
        TextField txtGroupName = new TextField();

        Label lblUsername = new Label("Username:");
        TextField txtUsername = new TextField();

        Label lblPassword = new Label("Password:");
        PasswordField txtPassword = new PasswordField();

        Button btnLogin = new Button("Login");
        btnLogin.setOnAction(e -> {
            String groupName = txtGroupName.getText();
            String username = txtUsername.getText();
            String password = txtPassword.getText();

            if (loginManager.validate(groupName, username, password)) {
                // Transition to menu scene upon successful login

                showMenu(primaryStage);
                primaryStage.centerOnScreen();
            } else {
                System.out.println("Invalid credentials");
                // Optionally show an alert dialog or error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Error");
                alert.setHeaderText(null);
                alert.setContentText("Invalid credentials. Please try again.");
                alert.showAndWait();
            }
        });

        VBox loginFields = new VBox(10, lblGroupName, txtGroupName, lblUsername, txtUsername, lblPassword, txtPassword, btnLogin);
        loginFields.setPadding(new Insets(20));
        loginFields.setAlignment(Pos.CENTER_RIGHT);
        loginFields.setMaxWidth(400);



        // Load the image
        ImageView imageView = new ImageView(new Image("/image/drone.jpg"));
        imageView.setFitWidth(600);
        imageView.setFitHeight(400);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // Create an HBox to hold the image and the login fields
        HBox loginBox = new HBox(imageView, loginFields);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(20));

        Scene loginScene = new Scene(loginBox, 800, 400);
        primaryStage.centerOnScreen();
        primaryStage.setScene(loginScene);
    }

    public static void showMenu(Stage primaryStage) {
        VBox dashboard = createDashboard(primaryStage);
        // Text content
        Text title = new Text("Project Group9 by Louay Hentati / Barhoud Hamza / Jassir Badrash / Mohamed Osman");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        title.setFill(Color.GREY);

        // Create DroneAnimation instance and get its ImageView
        DroneAnimation droneAnimation = new DroneAnimation();
        ImageView droneImageView = droneAnimation.getDroneImageView();
        droneAnimation.playAnimation(); // Start the animation
        droneImageView.setLayoutX(400);
        droneImageView.setLayoutY(-400);

        Image image = new Image("/image/allowed_to_fly2.png"); // Update the path to your image
        ImageView imageView1 = new ImageView(image);
        imageView1.setFitWidth(550);
        imageView1.setFitHeight(550);

        // Image
        Image image2 = new Image("/image/fly_zone2.png"); // Update the path to your image
        ImageView imageView2 = new ImageView(image2);
        imageView2.setFitWidth(550);
        imageView2.setFitHeight(550);

        // Pane for positioning the image in the top-left corner
        Pane imagePane = new Pane();
        imagePane.getChildren().addAll(imageView1, imageView2, droneImageView);
        imageView1.setLayoutX(600);
        imageView1.setLayoutY(-900);

        imageView2.setLayoutX(600);
        imageView2.setLayoutY(-400);

        // Make the image pane mouse transparent
        imagePane.setMouseTransparent(true);

        // Text content
        Text title1 = new Text("Where am I allowed to fly?\n\n");
        title1.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        title1.setFill(Color.LIGHTBLUE);

        Text text1 = new Text(
                "On the digital platform for unmanned aviation,      \n" +
                        " which we developed together with the German Federal    \n" +
                        "Ministry for Digital and Transport, all relevant information  \n" +
                        "and rules for unmanned aviation under the German Aviation   \n" +
                        "Regulation has been centrally compiled on one website (German only).   \n\n"
        );
        text1.setFont(Font.font("Arial", FontWeight.LIGHT, 15));
        Text text2 = new Text(
                "\nWe have find an app for private drone pilots in cooperation with\n" +
                        " the software specialist Unifly.On an interactive map, the user can see\n" +
                        " where they are allowed to fly and what they have to pay attention to.\n " +
                        "The use of the app is free of charge.\n\n"

        );
        text2.setFont(Font.font("Arial", FontWeight.LIGHT, 15));

        // Text content
        Text title2 = new Text("\n\n\n\nApplications and approvals \n");
        title2.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        title2.setFill(Color.LIGHTBLUE);

        Text text3 = new Text(
                "When drones get too close to aircraft, they pose a danger to manned aircraft. \n" +
                        "That is why particularly strict rules apply around airports. Are you planning\n" +
                        " a drone flight in the vicinity of an airport? You can find out \n" +
                        "here who you need to ask for permission – and what else there is to bear in mind.\n\n\n\n\n\n\n\n\n"

        );
        text3.setFont(Font.font("Arial", FontWeight.LIGHT, 15));

        Text text4 = new Text("\n\n\n\n\n\n");
        // Hyperlinks
        Hyperlink dipulLink1 = new Hyperlink("www.dipul.de");
        dipulLink1.setOnAction(e -> getHostServicesInstance().showDocument("http://www.dipul.de"));

        Hyperlink droniqLink2 = new Hyperlink("Droniq App");
        droniqLink2.setOnAction(e -> getHostServicesInstance().showDocument("http://www.droniq.de"));

        Hyperlink droniqLink3 = new Hyperlink("Airport and control Zones");
        droniqLink3.setOnAction(e -> getHostServicesInstance().showDocument("https://www.dfs.de/homepage/en/drone-flight/applications-and-approvals/"));


        // Layout for image and text
        VBox imageTextContainer = new VBox(20);
        imageTextContainer.setPadding(new Insets(15));
        imageTextContainer.setAlignment(Pos.TOP_LEFT);
        imageTextContainer.getChildren().addAll(new VBox(title, title1, text1, dipulLink1, text2, droniqLink2, title2, text3), imagePane, droniqLink3);

        // ScrollPane to make the content scrollable
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(imageTextContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        BorderPane root = new BorderPane();
        root.setTop(dashboard);
        root.setCenter(scrollPane);

        Scene menuScene = new Scene(root, 1300, 1200);
        primaryStage.centerOnScreen();
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }


    interface SubButtonActionHandler {
        void handle(Stage primaryStage) throws IOException;
    }

    //Game ..........................


    static void startGame(Stage primaryStage) {
        game = new Game(() -> gameOver(primaryStage));
        gameScene = new Scene(game.createContent());
        gameScene.setOnKeyPressed(game::handleKeyPress);
        gameScene.setOnKeyReleased(game::handleKeyRelease);

        primaryStage.setScene(gameScene);
        game.startGameLoop();
    }

    private static void gameOver(Stage primaryStage) {
        GameOverPopup.display(game.getScore(), primaryStage, () -> startGame(primaryStage));
    }
    public static void getMenuScene(Stage primaryStage) {
        primaryStage.centerOnScreen();
        showMenu(primaryStage);
    }



    public static void main(String[] args) {
        launch();
    }
}
