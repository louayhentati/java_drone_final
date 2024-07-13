package Animation;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * The LoadingTask class provides a utility for displaying a loading popup with a progress bar.
 * It simulates a loading process using a timeline animation.
 *Responsibilities:
 *Displays a modal loading popup with a progress bar and label.
 *Simulates a loading process and closes the popup when loading is complete.
 *Centers the popup on the screen.
 */

public class LoadingTask {

    public static void showLoadingPopup() {
        Stage loadingStage = new Stage();
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.setTitle("Loading...");

        // Create a progress bar
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);

        // Label for displaying text
        Label loadingLabel = new Label("Loading...");

        // StackPane to hold progress bar and label
        StackPane loadingPane = new StackPane();
        loadingPane.getChildren().addAll(progressBar, loadingLabel);

        Scene loadingScene = new Scene(loadingPane, 300, 100);
        loadingStage.setScene(loadingScene);

        centerStageOnScreen(loadingStage);
        loadingStage.show();

        // Simulate loading process with a timeline
        Duration duration = Duration.seconds(1);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
                new KeyFrame(duration, event -> {
                    loadingStage.close(); // Close the stage when done
                }, new KeyValue(progressBar.progressProperty(), 1))
        );
        timeline.play();
    }


    public static void showLoadingPopup2() {
        Stage loadingStage = new Stage();
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.setTitle("Loading...");


        loadingStage.setOnCloseRequest(event -> {
            event.consume(); // Consume the event to prevent closing
        });

        // Create a progress bar
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);

        // Label for displaying text
        Label loadingLabel = new Label("Loading...");

        // StackPane to hold progress bar and label
        StackPane loadingPane = new StackPane();
        loadingPane.getChildren().addAll(progressBar, loadingLabel);

        Scene loadingScene = new Scene(loadingPane, 300, 100);
        loadingStage.setScene(loadingScene);

        centerStageOnScreen(loadingStage);
        loadingStage.show();

        // Simulate loading process with a timeline
        Duration duration = Duration.seconds(2);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
                new KeyFrame(duration, event -> {
                    loadingStage.close(); // Close the stage when done
                }, new KeyValue(progressBar.progressProperty(), 1))
        );
        timeline.play();
    }

    private static void centerStageOnScreen(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(500);
        stage.setY(500);
    }
}
