package Animation;

import javafx.animation.PathTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import static javafx.application.Application.launch;

/**
 * The DroneAnimation class handles the creation and animation of a drone image along a specified path.
 * This includes setting up the drone image, defining its path, and starting the animation.
 *Responsibilities:
 *Initialize and configure the drone image.
 *Create and configure a path transition for the drone image.
 *Provide a method to start the animation.
 */

public class DroneAnimation {

    private ImageView droneImageView;
    private PathTransition pathTransition;

    public DroneAnimation() {
        initializeDroneImage();
        createPathTransition();
    }

    private void initializeDroneImage() {
        Image droneImage = new Image("/image/drone2.png"); // Replace with the path to your drone image
        droneImageView = new ImageView(droneImage);
        droneImageView.setFitWidth(150); // Adjust the size as needed
        droneImageView.setPreserveRatio(true);
        droneImageView.setX(400); // Start in the center horizontally
        droneImageView.setY(275); // Start in the center vertically
    }

    private void createPathTransition() {
        // Create SVGPath for heart shape
        SVGPath svg = new SVGPath();
        svg.setFill(Color.TRANSPARENT);
        svg.setStrokeWidth(1.0);
        svg.setStroke(Color.BLACK);
        svg.setContent("M 400,500 "
                + "C 400,500 350,400 250,300 "
                + "C 150,200 100,150 100,100 "
                + "C 100,50 150,0 200,0 "
                + "C 250,0 300,50 300,100 "
                + "C 300,150 250,200 150,300 "
                + "C 50,400 0,500 0,500 "
                + "C 0,500 50,600 150,700 "
                + "C 250,800 300,850 400,950 "
                + "C 500,850 550,800 650,700 "
                + "C 750,600 800,500 800,500 Z"+"M 787.49,150 C 787.49,203.36 755.56,247.27 712.27,269.5 S 622.17,290.34 582.67,279.16" +
                "S 508.78,246.56 480,223.91 424.93,174.93 400,150 348.85,98.79 320,76.09 S 256.91,32.03 217.33,20.84 " +
                "S 130.62,8.48 87.73,30.5 12.51,96.64 12.51,150 44.44,247.27 87.73,269.5 S 177.83,290.34 217.33,279.16 " +
                "S 291.22,246.56 320,223.91 375.07,174.93 400,150 451.15,98.79 480,76.09 S 543.09,32.03 582.67,20.84 "  +
                "S 669.38,8.48 712.27,30.5 787.49,96.64 787.49,150 z");

        // Create PathTransition for the drone image
        Path path = new Path();
        path.getElements().addAll(new MoveTo(400, 500), new LineTo(400, 300));

        pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.seconds(8)); // Duration of one cycle
        pathTransition.setPath(svg);
        pathTransition.setNode(droneImageView);
        pathTransition.setOrientation(PathTransition.OrientationType.NONE);
        pathTransition.setCycleCount(PathTransition.INDEFINITE); // Repeat indefinitely
        pathTransition.setAutoReverse(true); // Move back and forth
    }
    public ImageView getDroneImageView() {
        return droneImageView;
    }
    public void playAnimation() {
        pathTransition.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}