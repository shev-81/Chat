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
     * Конструктор сохраняет в себе ссылки на  {@link Controller Controller} и {@link Connection Connection}.
     * @param controller Контроллер.
     * @param connection Соединение с сетью.
     */
    public ReaderMessages(Controller controller, Connection connection) {
        this.controller = controller;
        this.connection = connection;
    }

    /**
     * Вызывает метод обработки, соответствующий типу сообщения.
     * @param message Сообщение.
     * @return true. Но при необходимости выйти из цикла прохождения авторизации кидает false.
     * @throws SocketException может появится при обращениею к сокету.
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

    /**
     * Выполняет первичнео наполнение информацией окна чата. Определяется имя окна чата (соответствует имени пользователя),
     * создается панель для переписки для нового подсоединившегося пользователя.
     * @param message Сообщение.
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
     * При получении сообщения, что сторонний пользователь покинул чат, удаляет запись о нем из списка пользователей.
     * @param message Сообщение.
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
     * Изменяет имя пользователя, убирает старого пользователя из списка пользователей и добавляет этого же с новым именем.
     * Удаляет панель сообщений от старого пользователя и создает новую для пользователя с новым именем.
     * @param message Сообщение.
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
     * Обрабатывает персональное сообщение.
     * @param message Сообщение.
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
     * Обрабатывет сообщение адресованное всем.
     * @param message Сообщение.
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
     * Обрабатывает сообщение о смене статуса пользователя.
     * @param message Сообщение.
     */
    private void status (Message message){
        if(message.getNameU().equals(controller.getMyName())){
            Platform.runLater(() -> controller.getStatus().setText(message.getText()));
        }else{
            controller.updateUsersListStatus(message);
        }
    }

    /**
     * Добавляет пользователя по его имени в модель список FX.
     * @param userName Имя пользователя.
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
