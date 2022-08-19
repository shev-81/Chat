package com.client.pak;

import com.client.pak.services.FileWorker;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import lombok.Data;
import message.*;

import java.net.URL;
import java.util.*;

@Data
public class Controller implements Initializable {

    private Connection connection;
    private String strFromClient;
    private String myName;
    private ObservableList<UserCell> listUserModel;
    private List<UserCell> userList;
    private Map<String, GridPane> messagePanes;
    private String useNowPane;
    private Message message;
    private FileWorker fileWorker;

    @FXML
    GridPane regPane;
    @FXML
    TextField regLogin;
    @FXML
    PasswordField regPassword;
    @FXML
    PasswordField regPasswordRep;
    @FXML
    TextField regName;
    @FXML
    Label regMessage;

    @FXML
    ScrollPane scrollPane;
    @FXML
    ListView<UserCell> listFx;
    @FXML
    TextField textField;
    @FXML
    TextField status;
    @FXML
    HBox chatPane;
    @FXML
    GridPane chat;

    @FXML
    TextField authLogin;
    @FXML
    PasswordField authPassword;
    @FXML
    GridPane authPane;
    @FXML
    Label authMessage;

    @FXML
    GridPane setPane;
    @FXML
    TextField setName;
    @FXML
    Label setMessage;

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

    @FXML
    public void SendButton() {
        if (!textField.getText().trim().isEmpty()) {
            strFromClient = textField.getText();
            Bubble chatMessage = new Bubble(myName, strFromClient, "");
            messagePanes.get(useNowPane).setHalignment(chatMessage, HPos.RIGHT);
            messagePanes.get(useNowPane).setValignment(chatMessage, VPos.BOTTOM);
            Platform.runLater(() -> {
                messagePanes.get(useNowPane).addRow(messagePanes.get(useNowPane).getRowCount(), chatMessage);
                scrollDown();
            });
            if (useNowPane.equals("Общий чат")) {
                message = new Message(Message.MessageType.UMESSAGE);
            } else {
                message = new Message(Message.MessageType.PERSONAL);
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

    @FXML
    public void sendDisconnect(MouseEvent mouseEvent) {
        connection.sendMessage(new Message(Message.MessageType.END));
        userList.clear();
        listFx.refresh();
        connection.closeConnection();
        connection = null;
        myName = " ";
        changeStageToAuth();
    }

    @FXML
    public void changeStageToSet(MouseEvent mouseEvent) {
        Platform.runLater(() -> {
            setName.clear();
            setMessage.setVisible(false);
        });
        authPane.setVisible(false);
        regPane.setVisible(false);
        chatPane.setVisible(false);
        setPane.setVisible(true);
    }

    @FXML
    public void sendStatus() {
        if (!status.getText().strip().isEmpty()) {
            Message message = new Message();
            message.setType(Message.MessageType.STATUS);
            message.setText(status.getText());
            message.setNameU(myName);
            connection.sendMessage(message);
            status.clear();
        }
    }

    @FXML
    public void enterChat(ActionEvent event) {
        if (connection == null) {
            connection = new Connection(this);
            new Thread(connection).start();
        }
        if (authLogin.getText().isEmpty() || authPassword.getText().isEmpty()) {
            authMessage.setText("Enter login and password");
            authMessage.setVisible(true);
        } else {
            message = new Message(Message.MessageType.AUTH);
            message.setLogin(authLogin.getText());
            message.setPass(authPassword.getText());
            connection.sendMessage(message);
        }
    }

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

    public void changeStageToChat() {
        chatPane.setVisible(true);
        authPane.setVisible(false);
        regPane.setVisible(false);
        setPane.setVisible(false);
    }

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

    public void loadListUsers(String[] parts) {
        listUserModel = null;
        userList.add(new UserCell("Общий чат", "посетителей"));
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals(myName)) {
                continue;
            }
            userList.add(new UserCell(parts[i], "On line"));
            messagePanes.put(parts[i], new MessgePane(parts[i]));
        }
        listUserModel = FXCollections.observableArrayList(userList);
        Platform.runLater(() -> {
            listFx.setItems(listUserModel);
            listFx.setCellFactory(new CellRenderer());
        });
    }

    public void register() {
        if (connection == null) {
            connection = new Connection(this);
            new Thread(connection).start();
        }
        if (regLogin.getText().isEmpty() || regPassword.getText().isEmpty() ||
                regPasswordRep.getText().isEmpty() || regName.getText().isEmpty()) {
            regMessage.setTextFill(Color.RED);
            regMessage.setText("Enter login, password and name");
            regMessage.setVisible(true);
        } else if (!regPassword.getText().equals(regPasswordRep.getText())) {
            regMessage.setTextFill(Color.RED);
            regMessage.setText("Passwords do not match");
            regMessage.setVisible(true);
        } else {
            message = new Message(Message.MessageType.REGUSER);
            message.setNameU(regName.getText());
            message.setLogin(regLogin.getText());
            message.setPass(regPassword.getText());
            connection.sendMessage(message);
        }
    }

    public void scrollDown() {
        final Timeline timeline = new Timeline();
        final KeyValue kv = new KeyValue(scrollPane.vvalueProperty(), 1.0);
        final KeyFrame kf = new KeyFrame(Duration.millis(500), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    public void moseClickOnListItem(MouseEvent mouseEvent) {
        try {
            String nameUser = listFx.getSelectionModel().getSelectedItem().getName();
            scrollPane.setContent(messagePanes.get(nameUser));
            useNowPane = nameUser;
        } catch (NullPointerException e) {
        }
    }

    public void saveAccChanges(ActionEvent event) {
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
                        message = new Message(Message.MessageType.CHANGENAME);
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

    public void wrongUser() {
        Platform.runLater(() -> authMessage.setText("Wrong login or password"));
        authMessage.setVisible(true);
    }

    public void removeUsers(String userName) {
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

    public void upDateUserList(Message message) {
        for (UserCell u : userList) {
            if (u.getName().equals(message.getNameU())) {
                u.setStatus(message.getText());
            }
        }
        listFx.refresh();
    }
}
