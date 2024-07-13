package error;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

/**
 * ErrorHandler class for handling exceptions and displaying error messages to the user.
 */
public class ErrorHandler {

    private static final Logger logger = LogManager.getLogger(ErrorHandler.class);

    /**
     * Handles IOException by logging the error and displaying an error message.
     *
     * @param e the IOException to handle
     */
    public static void handleIOException(IOException e) {
        logError(e);
        showErrorPopup("IO Error", "An error occurred while performing an IO operation.");
    }

    /**
     * Logs an error.
     *
     * @param e the Exception to log
     */
    public static void logError(Exception e) {
        logger.error("An error occurred", e);
    }

    /**
     * Shows a simplified error popup with the specified title and message.
     *
     * @param title   the title of the error popup
     * @param message the message of the error popup
     */
    public static void showErrorPopup(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(message);
            alert.showAndWait();
        });
    }

    /**
     * Handles any generic exception by logging the error and displaying an error message.
     *
     * @param e the Exception to handle
     */
    public static void handleException(Exception e) {
        logError(e);
        showErrorPopup("Error", "An unexpected error occurred.");
    }
}
