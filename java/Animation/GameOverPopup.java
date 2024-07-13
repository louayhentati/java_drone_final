package Animation;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static gui.DroneSimulatorGUI.createToolbarButton;
import static gui.DroneSimulatorGUI.showMenu;

/**
 * The GameOverPopup class manages the display of a game over popup window.
 * It shows the player's score, high score, and provides options to restart the game or return to the main menu.
 * Responsibilities:
 * Displays a popup window when the game is over.
 *Shows the player's current score and the highest score achieved.
 * Provides a button to restart the game.
 * Provides a button to return to the main menu.
 *Displays a special message if the score is 2 or less.
 */

public class GameOverPopup {
    private static int highScore = 0;

    public static void display(int score, Stage primaryStage, Runnable restartGame) {
        if (score > highScore) {
            highScore = score;
        }

        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Game Over");
        window.setMinWidth(300);
        window.setMinHeight(200);

        Label scoreLabel = new Label("Score: " + score);
        Label highScoreLabel = new Label("High Score: " + highScore);

        Label noobLabel = score <= 2 ? new Label("You are a Noob! Score: " + score) : new Label();

        Button repeatButton = createToolbarButton("Repeat", "/image/refresh.png");
        repeatButton.setOnAction(e -> {
            restartGame.run();
            window.close();
        });

        Button menuButton = createToolbarButton("Menu", "/image/menu.png");
        menuButton.setOnAction(e -> {
            showMenu(primaryStage);
            primaryStage.centerOnScreen();
            window.close();
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(scoreLabel, highScoreLabel, noobLabel, repeatButton, menuButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        window.setScene(scene);

        Platform.runLater(() -> window.showAndWait());
    }
}