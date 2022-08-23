package com.client.pak.services;

import com.client.pak.Connection;
import com.client.pak.Controller;
import com.client.pak.Main;
import com.client.pak.render.Bubble;
import com.client.pak.render.CellRenderer;
import com.client.pak.render.MessagePane;
import com.client.pak.render.UserCell;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.layout.GridPane;
import message.*;
import java.net.SocketException;

/**
 * A class that defines methods for reading messages and processing
 * them, depending on their types.
 */
public class ReaderMessages {

    /**
     * Variable {@link Controller Controller}.
     */
    private final Controller controller;

    /**
     * Variable {@link Connection Connection}.
     */
    private final Connection connection;

    /**
     * The constructor stores references to  {@link Controller Controller} and {@link Connection Connection}.
     * @param controller Controller.
     * @param connection Connection to the network.
     */
    public ReaderMessages(Controller controller, Connection connection) {
        this.controller = controller;
        this.connection = connection;
    }

    /**
     * Calls the processing method corresponding to the message type.
     * @param message Message.
     * @return true. If necessary, exit the authorization cycle throws false.
     * @throws SocketException It may appear when accessing the socket.
     */
    public boolean read(Message message) throws SocketException {
        switch (message.getType()) {
            case AUTHOK: return aouthOk (message);
            case AUTHNO: aouthNo(); break;
            case CONECTED: connected(message); break;
            case DISCONECTED: disconnected(message); break;
            case CHANGENAME: changeName(message); break;
            case PERSONAL: personal(message); break;
            case UMESSAGE: uMessage(message); break;
            case STATUS: status(message); break;
        }
        return true;
    }

    /**
     * Accepts a message object with the status "AUTHOK"
     * @param message Message.
     * @return boolean if authorization is successful, the condition for exiting the registration verification cycle returns "false".
     * @see Connection autorizQuestion()
     * @throws SocketException if there is an error with the socket.
     */
    private boolean aouthOk (Message message) throws SocketException {
        controller.setMyName(message.getNameU());
        Platform.runLater(() -> Main.getpStage().setTitle("Net-chat:  " + controller.getMyName()));
        controller.loadListUsers(message.getUsersList());
        connection.getSocket().setSoTimeout(0);
        controller.changeStageToChat();
        return false;
    }

    /**
     * Accepts a message object with the status "AUTHNO".
     * @throws SocketException may occur when accessing a socket.
     */
    private void aouthNo () throws SocketException {
        connection.getSocket().setSoTimeout(Connection.TIME_COUNT);
        controller.wrongUser();
    }

    /**
     * Performs the initial filling of the chat window with information.
     * The name of the chat window is determined (corresponds to the
     * user name), a panel for correspondence is created for the newly
     * connected user.
     * @param message Message.
     */
    private void connected (Message message){
        if (message.getNameU().equals(controller.getMyName())) {
            return;
        }
        Platform.runLater(() -> addUserInListFx(message.getNameU()));
        Bubble chatMessage = new Bubble(message.getNameU() + " присоединяется к чату.");
        GridPane.setHalignment(chatMessage, HPos.CENTER);
        Platform.runLater(() -> {
            controller.getMessagePanes().put(message.getNameU(), new MessagePane(message.getNameU()));
            controller.getMessagePanes().get("Общий чат").addRow(controller.getMessagePanes().get("Общий чат").getRowCount(), chatMessage);
            controller.scrollDown();
        });
    }

    /**
     * When receiving a message that a third-party user has left the chat,
     * deletes the entry about him from the list of users.
     * @param message Message.
     */
    private void disconnected (Message message){
        Platform.runLater(() -> controller.removeUser(message.getNameU()));
        Bubble chatMessage = new Bubble(message.getNameU() + " покидает чат.");
        GridPane.setHalignment(chatMessage, HPos.CENTER);
        Platform.runLater(() -> {
            controller.getMessagePanes().get("Общий чат").addRow(controller.getMessagePanes().get("Общий чат").getRowCount(), chatMessage);
            controller.scrollDown();
        });
    }

    /**
     * Changes the user name, removes the old user from the list of users
     * and adds the same one with a new name. Deletes the message panel
     * from the old user and creates a new one for the user with a new name.
     * @param message Message.
     */
    private void changeName (Message message){
        if(!message.getNameU().equals(controller.getMyName())){
            Bubble chatMessage = new Bubble(message.getNameU() + " сменил имя на - " + message.getToNameU());
            GridPane.setHalignment(chatMessage, HPos.CENTER);
            Platform.runLater(() -> {
                controller.removeUser(message.getNameU());
                controller.getMessagePanes().remove(message.getNameU());
                addUserInListFx(message.getToNameU());
                controller.getMessagePanes().put(message.getToNameU(), new MessagePane(message.getToNameU()));
                controller.getMessagePanes().get("Общий чат").addRow(controller.getMessagePanes().get("Общий чат").getRowCount(), chatMessage);
                controller.scrollDown();
            });
        }else{
            Platform.runLater(() -> {
                controller.getMessagePanes().remove(message.getNameU());
                controller.getMessagePanes().put(message.getToNameU(), new MessagePane(message.getToNameU()));
                controller.setMyName(message.getToNameU());
            });
        }
    }

    /**
     * Processes a personal message.
     * @param message Message.
     */
    private void personal (Message message){
        Bubble chatMessage = new Bubble(message.getNameU(), message.getText(), "");
        GridPane.setHalignment(chatMessage, HPos.LEFT);
        GridPane usePaneChat = controller.getMessagePanes().get(message.getNameU());
        Platform.runLater(() -> {
            usePaneChat.addRow(usePaneChat.getRowCount(), chatMessage);
            controller.scrollDown();
        });
    }

    /**
     * Processes a message addressed to everyone.
     * @param message Message.
     */
    private void uMessage (Message message){
        if(message.getNameU().equals(controller.getMyName())){
            return;
        }
        Bubble chatMessage = new Bubble(message.getNameU(), message.getText(), "");
        GridPane.setHalignment(chatMessage, HPos.LEFT);
        Platform.runLater(() -> {
            controller.getMessagePanes().get("Общий чат").addRow(controller.getMessagePanes().get("Общий чат").getRowCount(), chatMessage);
            controller.scrollDown();
            controller.getFileWorker().saveMsgToFile(message.getNameU()+" "+message.getText());
        });
    }

    /**
     * Processes the message about the user status change.
     * @param message Message.
     */
    private void status (Message message){
        if(message.getNameU().equals(controller.getMyName())){
            Platform.runLater(() -> controller.getStatus().setText(message.getText()));
        }else{
            controller.updateUsersListStatus(message);
        }
    }

    /**
     * Adds a user by his name to the FX model list.
     * @param userName Username.
     */
    private void addUserInListFx(String userName) {
        Platform.runLater(() -> {
            controller.getUserList().add(new UserCell(userName, "On line"));
            ObservableList<UserCell> users = FXCollections.observableArrayList(controller.getUserList());
            controller.getListFx().setItems(users);
            controller.getListFx().setCellFactory(new CellRenderer());
        });
    }
}
