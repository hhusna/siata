package viewcontroller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import model.User;
import service.UserService;

import java.io.IOException;

public class LoginController {

    private UserService userService = new UserService();
    private User user = new User();

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    public void login(ActionEvent e) {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();

            user.setUsername(username);
            user.setPassword(password);

            userService.login(user);
        } catch (Exception err) {
            err.printStackTrace();
        }

    }
}