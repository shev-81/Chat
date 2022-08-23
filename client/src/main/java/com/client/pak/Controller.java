package com.client.pak;

import com.client.pak.render.Bubble;
import com.client.pak.render.CellRenderer;
import com.client.pak.render.MessagePane;
import com.client.pak.render.UserCell;
import com.client.pak.services.FileWorker;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import lombok.Data;
import message.*;

import java.net.URL;
import java.util.*;

/**
 * The application controller class. Binds all application methods that are executed from
 * user actions in the application UI. Contains a link to the current network connection
 * to the server, the user name received after registration.
 */
@Data
public class Controller implements Initializable {

    /**
     * Network connection.
     */
    private static Connection connection = null;

    /**
     * Username.
     */
    private String myName;

    /**
     * The list of users to draw in the FX model of the application.
     */
    private ObservableList<UserCell> listUserModel;

    /**
     * A list of objects describing the user's status.
     */
    private List<UserCell> userList;

    /**
     * A panel map for individual messages with other users,
     * a separate correspondence panel is created for each user and
     * placed in this map by user name.
     */
    private Map<String, GridPane> messagePanes;

    /**
     * The name of the panel currently used for communication.
     * It is determined by which user is communicating with. i.e.,
     * this field contains the name of another user connected to
     * the communication.
     */
    private String useNowPane;

    /**
     * A variable link to the file management service.
     */
    private FileWorker fileWorker;

    /**
     * Registration panel.
     */
    @FXML
    GridPane regPane;

    /**
     * Login input field, when registering a new user.
     */
    @FXML
    TextField regLogin;

    /**
     * Password input field, when registering a new user.
     */
    @FXML
    PasswordField regPassword;

    /**
     * The password re-entry field, when registering a new user.
     */
    @FXML
    PasswordField regPasswordRep;

    /**
     * The name input field, when registering a new user.
     */
    @FXML
    TextField regName;

    /**
     * The label of the registration panel message output.
     */
    @FXML
    Label regMessage;

    /**
     * ScrollPane panel.
     */
    @FXML
    ScrollPane scrollPane;

    /**
     * The list is the representation of FX in the user's UI.
     */
    @FXML
    ListView<UserCell> listFx;

    /**
     * A text field for entering a message.
     */
    @FXML
    TextField textField;

    /**
     * A text field for entering the status.
     */
    @FXML
    TextField status;

    /**
     * The main panel.
     */
    @FXML
    HBox chatPane;

    /**
     * A panel for correspondence.
     */
    @FXML
    GridPane chat;

    /**
     * A field for entering a login during authorization.
     */
    @FXML
    TextField authLogin;

    /**
     * A field for entering a password during authorization.
     */
    @FXML
    PasswordField authPassword;

    /**
     * Authorization Panel.
     */
    @FXML
    GridPane authPane;

    /**
     * A label on the authorization panel.
     */
    @FXML
    Label authMessage;

    /**
     * The panel for changing the user's nickname.
     */
    @FXML
    GridPane setPane;

    /**
     * The field for entering a new user name.
     */
    @FXML
    TextField setName;

    /**
     * The label of the message output on the user name change panel.
     */
    @FXML
    Label setMessage;

    /**
     * Initializes the visibility of the User Authorization panel, creates
     * a panel map for connecting users.
     * Creates {@link FileWorker FileWorker}.
     * @param location  an unused parameter from an inherited interface.
     * @param resources an unused parameter from an inherited interface.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        changeStageToAuth();
        scrollPane.setFitToWidth(true);
        messagePanes = new HashMap();
        messagePanes.put("Общий чат", chat);
        useNowPane = "Общий чат";
        userList = new ArrayList<>();
        fileWorker = new FileWorker(this);
    }

    /**
     * Reacts to pressing the send message button. Displays a message on
     * the user's screen, saves the text of the message to the user's
     * history file, sends a message depending on which panel the message
     * is sent to the user with whom the correspondence is going on.
     */
    @FXML
    public void SendButton() {
        if (!textField.getText().trim().isEmpty()) {
            String strFromClient = textField.getText();
            Bubble chatMessage = new Bubble(myName, strFromClient, "");
            messagePanes.get(useNowPane).setHalignment(chatMessage, HPos.RIGHT);
            messagePanes.get(useNowPane).setValignment(chatMessage, VPos.BOTTOM);
            Platform.runLater(() -> {
                messagePanes.get(useNowPane).addRow(messagePanes.get(useNowPane).getRowCount(), chatMessage);
                scrollDown();
            });
            Message message = new Message();
            if (useNowPane.equals("Общий чат")) {
                message.setType(Message.MessageType.UMESSAGE);
            } else {
                message.setType(Message.MessageType.PERSONAL);
                message.setToNameU(useNowPane);
            }
            message.setNameU(myName);
            message.setText(strFromClient);
            fileWorker.saveMsgToFile(myName + " " + strFromClient);
            connection.sendMessage(message);
            textField.clear();
            textField.requestFocus();
        }
    }

    /**
     * Reacts to clicking the exit chat button. Sends a message to the
     * server about disconnecting the user. Clears the user lists of the
     * session being closed. Closes the network connection and switches
     * the panel from the chat to the authorization panel.
     */
    @FXML
    public void sendDisconnect() {
        connection.sendMessage(new Message(Message.MessageType.END));
        userList.clear();
        listFx.refresh();
        connection.closeConnection();
        connection = null;
        myName = " ";
        changeStageToAuth();
    }

    /**
     * Switches to the user name change panel.
     */
    @FXML
    public void changeStageToSet() {
        Platform.runLater(() -> {
            setName.clear();
            setMessage.setVisible(false);
        });
        authPane.setVisible(false);
        regPane.setVisible(false);
        chatPane.setVisible(false);
        setPane.setVisible(true);
    }

    /**
     * Sends a new user status.
     */
    @FXML
    public void sendStatus() {
        if (!status.getText().isBlank()) {
            Message message = new Message();
            message.setType(Message.MessageType.STATUS);
            message.setText(status.getText());
            message.setNameU(myName);
            connection.sendMessage(message);
            status.clear();
        }
    }

    /**
     * Log in to the chat after entering your username and password
     * on the authorization panel. Opens the connection if it is not
     * already there. Sends a message to the server with a username
     * and password.
     */
    @FXML
    public void enterChat() {
        if (authLogin.getText().isEmpty() || authPassword.getText().isEmpty()) {
            authMessage.setText("Enter login and password");
            authMessage.setVisible(true);
        } else {
            if (connection == null) {
                connection = new Connection(this);
                new Thread(connection).start();
            }
            Message message = new Message(Message.MessageType.AUTH);
            message.setLogin(authLogin.getText());
            message.setPass(authPassword.getText());
            connection.sendMessage(message);
        }
    }

    /**
     * Changes the current scene to the authorization scene.
     */
    public void changeStageToAuth() {
        Platform.runLater(() -> {
            authLogin.clear();
            authPassword.clear();
        });
        authPane.setVisible(true);
        authMessage.setVisible(false);
        regPane.setVisible(false);
        chatPane.setVisible(false);
        setPane.setVisible(false);
    }

    /**
     * Changes the current scene to a chat scene.
     */
    public void changeStageToChat() {
        chatPane.setVisible(true);
        authPane.setVisible(false);
        regPane.setVisible(false);
        setPane.setVisible(false);
    }

    /**
     * Changes the current scene to the registration scene.
     */
    public void changeStageToReg() {
        Platform.runLater(() -> {
            regLogin.clear();
            regPassword.clear();
            regPasswordRep.clear();
            regName.clear();
        });
        regPane.setVisible(true);
        regMessage.setVisible(false);
        authPane.setVisible(false);
        chatPane.setVisible(false);
        setPane.setVisible(false);
    }

    /**
     * Fills the list in the UI with user names.
     * @param parts an array with a list of user names on the network.
     */
    public void loadListUsers(String[] parts) {
        listUserModel = null;
        userList.add(new UserCell("Общий чат", "посетителей"));
        for (String part : parts) {
            if (part.equals(myName)) {
                continue;
            }
            userList.add(new UserCell(part, "On line"));
            messagePanes.put(part, new MessagePane(part));
        }
        listUserModel = FXCollections.observableArrayList(userList);
        Platform.runLater(() -> {
            listFx.setItems(listUserModel);
            listFx.setCellFactory(new CellRenderer());
        });
    }

    /**
     * Sends data for registering a new user. Having previously
     * checked the availability and compliance of the entered data.
     */
    public void register() {
        if (regLogin.getText().isEmpty() || regPassword.getText().isEmpty() ||
                regPasswordRep.getText().isEmpty() || regName.getText().isEmpty()) {
            regMessage.setTextFill(Color.RED);
            regMessage.setText("Enter login, password and name");
            regMessage.setVisible(true);
            return;
        }
        if (!regPassword.getText().equals(regPasswordRep.getText())) {
            regMessage.setTextFill(Color.RED);
            regMessage.setText("Passwords do not match");
            regMessage.setVisible(true);
            return;
        }
        if (connection == null) {
            connection = new Connection(this);
            new Thread(connection).start();
        }
        Message message = new Message(Message.MessageType.REGUSER);
        message.setNameU(regName.getText());
        message.setLogin(regLogin.getText());
        message.setPass(regPassword.getText());
        connection.sendMessage(message);
    }

    /**
     * Moves the focus to the last chat entry.
     */
    public void scrollDown() {
        final Timeline timeline = new Timeline();
        final KeyValue kv = new KeyValue(scrollPane.vvalueProperty(), 1.0);
        final KeyFrame kf = new KeyFrame(Duration.millis(500), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    /**
     * Switches the chat panel to the panel of the selected user.
     */
    public void moseClickOnListItem() {
        try {
            String nameUser = listFx.getSelectionModel().getSelectedItem().getName();
            scrollPane.setContent(messagePanes.get(nameUser));
            useNowPane = nameUser;
        } catch (NullPointerException ignored) {
        }
    }

    /**
     * Changes the old username to a new one.
     */
    public void saveAccChanges() {
        if (!setName.getText().isEmpty()) {
            new Thread(() -> {
                try {
                    if (!setName.getText().trim().isEmpty() && !setName.getText().trim().equals(myName)) {
                        for (UserCell userInList : userList) {
                            if (setName.getText().trim().equals(userInList.getName())) {
                                Platform.runLater(() -> {
                                    setMessage.setTextFill(Color.RED);
                                    setMessage.setText("Error change name.");
                                    setMessage.setVisible(true);
                                });
                                return;
                            }
                        }
                        Platform.runLater(() -> Main.getpStage().setTitle("Net-chat:  " + setName.getText()));
                        Message message = new Message(Message.MessageType.CHANGENAME);
                        message.setNameU(myName);
                        message.setToNameU(setName.getText());
                        connection.sendMessage(message);
                        Platform.runLater(() -> {
                            setMessage.setTextFill(Color.GREEN);
                            setMessage.setText("Name change accepted.");
                            setMessage.setVisible(true);
                        });
                    } else {
                        Platform.runLater(() -> {
                            setMessage.setTextFill(Color.RED);
                            setMessage.setText("Enter new name.");
                            setMessage.setVisible(true);
                        });
                    }
                } catch (NullPointerException e) {
                    Platform.runLater(() -> {
                        setMessage.setTextFill(Color.RED);
                        setMessage.setText("Can't change name.");
                        setMessage.setVisible(true);
                    });
                }
            }).start();
        }
    }

    /**
     * Called when the username or password is incorrect.
     */
    public void wrongUser() {
        Platform.runLater(() -> authMessage.setText("Wrong login or password"));
        authMessage.setVisible(true);
    }

    /**
     * Removes a user from the list in the UI.
     * @param userName Username.
     */
    public void removeUser(String userName) {
        for (int i = 0; i < userList.size(); i++) {
            UserCell userCell = userList.get(i);
            if (userCell.getName().equals(userName)) {
                userList.remove(i);
            }
        }
        Platform.runLater(() -> {
            ObservableList<UserCell> users = FXCollections.observableArrayList(userList);
            listFx.setItems(users);
            listFx.setCellFactory(new CellRenderer());
        });
    }

    /**
     * Updates statuses in the list of UI users.
     * @param message Message.
     */
    public void updateUsersListStatus(Message message) {
        for (UserCell u : userList) {
            if (u.getName().equals(message.getNameU())) {
                u.setStatus(message.getText());
            }
        }
        listFx.refresh();
    }

    public Connection getConnection() {
        return connection;
    }
}
