package com.server.pak.services;

import com.server.pak.ClientHandler;
import com.server.pak.ServerApp;
import message.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A class that defines methods for reading messages and processing them,
 *  depending on their types.
 * @see Message
 * @see ServerApp
 */
public class ReaderMessages {

    /**
     * The project logger settings are defined in the file log4j2.xml
     * resource folder.
     */
    private static final Logger LOGGER = LogManager.getLogger(ReaderMessages.class);

    /**
     * A variable for saving the server.
     */
    private final ServerApp server;

    /**
     * The reader's constructor stores a link to the server.
     * @param server Server
     */
    public ReaderMessages(ServerApp server) {
        this.server = server;
    }

    /**
     * Receives a message and passes it for processing, depending on
     * the type of message.
     * @param message Message.
     * @param clientHandler The listener.
     * @return True, but it is also possible to be false if you need to
     * exit the authorization cycle.
     */
    public boolean read(Message message, ClientHandler clientHandler){
        switch (message.getType()) {
            case AUTH: return auth(message, clientHandler);
            case REGUSER: return regUser(message, clientHandler);
            case END: end(clientHandler); break;
            case CHANGENAME: changeName(message, clientHandler); break;
            case PERSONAL: personal(message, clientHandler); break;
            case UMESSAGE: uMessage (message); break;
            case STATUS: status(message); break;
        }
        return true;
    }

    /**
     * Performs user authorization by checking the database, as well as the
     * current session on the server.
     * @param message Message.
     * @param clientHandler The listener.
     * @return true if authorization is successful.
     */
    public boolean auth(Message message, ClientHandler clientHandler){
        String nickName;
        try {
            nickName = server.getAuthService().getNickByLoginPass(message.getLogin(), message.getPass());
        } catch (Exception e) {
            nickName = null;
        }
        if (nickName != null) {
            if (!server.isNickBusy(nickName)) {
                clientHandler.setName(nickName);
                server.subscribe(clientHandler);
                message = new Message(Message.MessageType.AUTHOK);
                message.setNameU(nickName);
                message.setUsersList(server.getClientsList());
                clientHandler.sendMessage(message);
                message = new Message(Message.MessageType.CONECTED);
                message.setNameU(nickName);
                server.sendAll(message);
                LOGGER.info("[Server]: " + nickName + " авторизовался.");
                return false;
            } else {
                clientHandler.sendMessage(new Message(Message.MessageType.AUTHNO));
                return true;
            }
        } else {
            clientHandler.sendMessage(new Message(Message.MessageType.AUTHNO));
            return true;
        }
    }

    /**
     * Registers a client listener on the server, sends a message to all
     * listeners about connecting a new user, and sends an updated list
     * of server users to all clients.
     * @param message Message.
     * @param clientHandler The listener.
     * @return false if the user's registration failed in the registration service.
     */
    public boolean regUser(Message message, ClientHandler clientHandler){
        if (server.getAuthService().registerNewUser(message.getNameU(), message.getLogin(), message.getPass())) {
            clientHandler.setName(message.getNameU());
            message.setType(Message.MessageType.CONECTED);
            server.sendAll(message);
            server.subscribe(clientHandler);
            message.setType(Message.MessageType.AUTHOK);
            message.setUsersList(server.getClientsList());
            clientHandler.sendMessage(message);
            return false;
        } else {
            LOGGER.info("[Server]: регистрация нового пользователя не прошла");
            message.setType(Message.MessageType.AUTHNO);
            clientHandler.sendMessage(message);
            return true;
        }
    }

    /**
     * Unsubscribes the listener from the subscription from the server, sends a message
     * to all listeners about leaving the client's network.
     * @param clientHandler The listener.
     */
    public void end(ClientHandler clientHandler){
        server.unSubscribe(clientHandler);
        Message message = new Message(Message.MessageType.DISCONECTED);
        message.setNameU(clientHandler.getName());
        server.sendAll(message);
        LOGGER.info("[Server]: " + clientHandler.getName() + " disconnected!");
    }

    /**
     * Changes the name of the client, saving the changes in the Database,
     * the Name of the listener on the server and notifying all listeners
     * of the server about the changes.
     * @param message Message.
     * @param clientHandler The listener.
     */
    public void changeName(Message message, ClientHandler clientHandler){
        LOGGER.info("[Server]: " + message.getNameU() + " запросил на смену имени на " + message.getToNameU());
        boolean rezult;
        rezult = server.getAuthService().updateNickName(message.getToNameU(), message.getNameU());
        if (rezult) {
            clientHandler.setName(message.getToNameU());
            server.sendAll(message);
            LOGGER.info("[Server]: Запрос на смену имени с " + message.getNameU() + " на " + message.getToNameU() + " УДОВЛЕТВОРЕН");
        } else {
            LOGGER.info("[Server]: Запрос на смену имени с " + message.getNameU() + " на " + message.getToNameU() + " НЕ УДОВЛЕТВОРЕН");
        }
    }

    /**
     * Sends a personal message.
     * @param message Message.
     * @param clientHandler The listener.
     */
    public void personal(Message message, ClientHandler clientHandler){
        clientHandler.sendPrivateMessage(message);
    }

    /**
     * Makes a mass mailing.
     * @param message Message.
     */
    public void uMessage(Message message){
        server.sendAll(message);
    }

    /**
     * Makes a mass mailing about a change in the user's status.
     * @param message Message.
     */
    public void status (Message message){
        server.sendAll(message);
    }
}
