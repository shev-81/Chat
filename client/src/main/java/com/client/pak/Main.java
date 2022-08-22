package com.client.pak;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import message.Message;

/**
 * Класс FX приложения, Запускает UI приложения по описанию сцены
 * приложения описанной в sample.fxml файле.
 */
public class Main extends Application {

    /**
     * Загрузчик FXML файла описывающего UI.
     */
    private FXMLLoader loader;

    /**
     * Переменная {@link Controller Controller}
     */
    private Controller controller;

    /**
     * Главная сцена приложения.
     */
    public static Stage pStage;

    /**
     * Выполняется при запуске приложения, сохраняет в переменных класса ссылки на
     * Primary Stage и Controller.
     * @param primaryStage Основная Stage приложения FX.
     * @throws Exception Может возникать при работе с loader'ом.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        pStage = primaryStage;
        loader = new FXMLLoader(getClass().getResource("/sample.fxml"));
        controller = loader.getController();
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Net-chat:");
        primaryStage.show();
    }

    /**
     * Возвращает Primary Stage приложения.
     * @return Primary Stage
     */
    public static Stage getpStage() {
        return pStage;
    }

    /**
     * Выполняется перед закрытием приложения, посылает сообщение
     * об отключении клиента и закрывает сетевое соединение.
     */
    @Override
    public void stop() {
        try{
            controller = loader.getController();
            controller.getConnection().sendMessage(new Message(Message.MessageType.END));
            controller.getConnection().closeConnection();
        }catch (NullPointerException e){}
    }

    /**
     * Запускает приложение.
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}