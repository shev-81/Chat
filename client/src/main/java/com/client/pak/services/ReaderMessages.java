package com.client.pak.services;

import com.client.pak.Connection;
import com.client.pak.Controller;
import com.client.pak.Main;
import com.client.pak.render.Bubble;
import com.client.pak.render.CellRenderer;
import com.client.pak.render.MessgePane;
import com.client.pak.render.UserCell;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.layout.GridPane;
import message.*;
import java.net.SocketException;

/**
 * Класс, определяющий методы чтения сообщений и их обработки, в зависимости от их типов.
 */
public class ReaderMessages {

    /**
     * Переменная {@link Controller Controller}.
     */
    private final Controller controller;

    /**
     * Переменная {@link Connection Connection}.
     */
    private final Connection connection;

    /**
     * Переменная {@link Bubble Bubble}.
     */
    private Bubble chatMessage;

    /**
     * Переменная тип сообщения {@link Message.MessageType MessageType}.
     */
    private Message.MessageType type;

    /**
     * Конструктор сохраняет в себе ссылки на  {@link Controller Controller} и {@link Connection Connection}.
     * @param controller Контроллер.
     * @param connection Соединение с сетью.
     */
    public ReaderMessages(Controller controller, Connection connection) {
        this.controller = controller;
        this.connection = connection;
    }

    public boolean read(Message message) throws SocketException {
        type = message.getType();
        switch (type) {
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
     * Принимает объект сообщения со статусом "AUTHOK"
     * @param message Сообщение.
     * @return boolean если авторизация успешна возвращает "false" условие выхода из цикла проверки регистрации.
     * @see Connection autorizQuestion()
     * @throws SocketException если возникнет ошибка с сокетом.
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
     * Принимает объект сообщения со статусом "AUTHNO"
     * @throws SocketException может возникать при обращении к сокету.
     */
    private void aouthNo () throws SocketException {
        connection.getSocket().setSoTimeout(Connection.TIME_COUNT);
        controller.wrongUser();
    }

    private void connected (Message message){
        if (message.getNameU().equals(controller.getMyName())) {
            return;
        }
        Platform.runLater(() -> addUserInListFx(message.getNameU()));
        chatMessage = new Bubble(message.getNameU() + " присоединяется к чату.");
        GridPane.setHalignment(chatMessage, HPos.CENTER);
        Platform.runLater(() -> {
            controller.getMessagePanes().put(message.getNameU(), new MessgePane(message.getNameU()));
            controller.getMessagePanes().get("Общий чат").addRow(controller.getMessagePanes().get("Общий чат").getRowCount(), chatMessage);
            controller.scrollDown();
        });
    }

    private void disconnected (Message message){
        Platform.runLater(() -> controller.removeUsers(message.getNameU()));
        chatMessage = new Bubble(message.getNameU() + " покидает чат.");
        GridPane.setHalignment(chatMessage, HPos.CENTER);
        Platform.runLater(() -> {
            controller.getMessagePanes().get("Общий чат").addRow(controller.getMessagePanes().get("Общий чат").getRowCount(), chatMessage);
            controller.scrollDown();
        });
    }

    private void changeName (Message message){
        if(!message.getNameU().equals(controller.getMyName())){
            chatMessage = new Bubble(message.getNameU() + " сменил имя на - " + message.getToNameU());
            GridPane.setHalignment(chatMessage, HPos.CENTER);
            Platform.runLater(() -> {
                controller.removeUsers(message.getNameU());
                controller.getMessagePanes().remove(message.getNameU());
                addUserInListFx(message.getToNameU());
                controller.getMessagePanes().put(message.getToNameU(), new MessgePane(message.getToNameU()));
                controller.getMessagePanes().get("Общий чат").addRow(controller.getMessagePanes().get("Общий чат").getRowCount(), chatMessage);
                controller.scrollDown();
            });
        }else{
            Platform.runLater(() -> {
                controller.getMessagePanes().remove(message.getNameU());
                controller.getMessagePanes().put(message.getToNameU(), new MessgePane(message.getToNameU()));
                controller.setMyName(message.getToNameU());
            });
        }
    }

    private void personal (Message message){
        chatMessage = new Bubble(message.getNameU(), message.getText(), "");
        GridPane.setHalignment(chatMessage, HPos.LEFT);
        GridPane usePaneChat = controller.getMessagePanes().get(message.getNameU());
        Platform.runLater(() -> {
            usePaneChat.addRow(usePaneChat.getRowCount(), chatMessage);
            controller.scrollDown();
        });
    }

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

    private void status (Message message){
        if(message.getNameU().equals(controller.getMyName())){
            Platform.runLater(() -> controller.getStatus().setText(message.getText()));
        }else{
            controller.upDateUserList(message);
        }
    }

    private void addUserInListFx(String userName) {
        Platform.runLater(() -> {
            controller.getUserList().add(new UserCell(userName, "On line"));
            ObservableList<UserCell> users = FXCollections.observableArrayList(controller.getUserList());
            controller.getListFx().setItems(users);
            controller.getListFx().setCellFactory(new CellRenderer());
        });
    }
}
