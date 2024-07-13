package Login;

import java.util.HashMap;
import java.util.Map;

/**
 * The LoginManager class handles user authentication by validating credentials.
 * It includes predefined credentials for a specific group and validates user inputs
 * against these credentials.
 * Responsibilities:
 * Stores predefined credentials for users.
 * Validates input credentials against stored credentials.
 */

public class LoginManager {
    private static class Credentials {
        String username;
        String password;

        Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    private final Map<String, Credentials> credentialsMap = new HashMap<>();

    public LoginManager() {
        // Populate with some predefined credentials for "group9"
        credentialsMap.put("Louay", new Credentials("Louay", "pass1"));
        credentialsMap.put("Hamza", new Credentials("Hamza", "pass2"));
        credentialsMap.put("Yasir", new Credentials("Yasir", "pass3"));
        credentialsMap.put("Mohamed", new Credentials("Mohamed", "pass4"));
        credentialsMap.put("", new Credentials("", ""));
    }

    public boolean validate(String group, String username, String password) {
        if ("group9".equals(group)) {
            Credentials creds = credentialsMap.get(username);
            return creds != null && (creds.username.equals(username) && creds.password.equals(password));
        }
        return false;
    }
}
