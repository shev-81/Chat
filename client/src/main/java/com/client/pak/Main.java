package com.client.pak;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import message.Message;

/**
 * The FX class of the application, Launches the UI of the application
 * according to the description of the application scene described in
 * sample.xml file.
 */
public class Main extends Application {

    /**
     * The loader of the XML file describing the UI.
     */
    private FXMLLoader loader;

    /**
     * Variable {@link Controller Controller}
     */
    private Controller controller;

    /**
     * The main stage of the application.
     */
    public static Stage pStage;

    /**
     * Executed at application startup, saves references to in class variables
     * Primary Stage and Controller.
     * @param primaryStage The main Stage of the FX application.
     * @throws Exception It may occur when working with the loader.
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
     * Returns the Primary Stage of the application.
     * @return Primary Stage
     */
    public static Stage getpStage() {
        return pStage;
    }

    /**
     * It is executed before closing the application, sends a message
     * about disconnecting the client and closes the network connection.
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
     * Launches the application.
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}