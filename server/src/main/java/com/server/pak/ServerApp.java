package com.server.pak;

import com.server.pak.services.AuthService;
import com.server.pak.services.AuthServiceBD;
import lombok.Data;
import message.Message;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server startup class, the messaging transport system is based on the exchange
 *  of objects between the client and the server, serialization and deserialization
 *  of message objects is used. (Java IO). The server uses in its work:
 *  The User Authorization Service, the Listener of the connected client.
 * @see AuthService
 * @see ClientHandler
 */
@Data
public class ServerApp {

    /**
     * The project logger settings are defined in the file log4j2.xml resource folder.
     */
    private static final Logger LOGGER = LogManager.getLogger(ServerApp.class);

    /**
     * List of connected client listeners.
     * @see ClientHandler
     */
    private ArrayList<ClientHandler> clients;

    /**
     * A variable for saving the server socket.
     */
    private Socket socket = null;

    /**
     * User authorization service.
     * @see AuthService
     */
    private AuthService authService;

    /**
     * The constructor creates objects: AuthService, ExecutorService,
     * ServerSocket and starts an internal loop waiting for a client
     * connection in the - ServerSocket.accept() method. Upon successful
     * connection, a new thread is allocated for the client via
     * ExecutorService and a ClientHandler listener is created.
     * @see AuthService
     * @see ExecutorService
     * @see ServerSocket
     * @see ClientHandler
     */
    ServerApp() {
        this.clients = new ArrayList<>();
        this.authService = new AuthServiceBD();
        ExecutorService service = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            while (true) {
                LOGGER.info("Server wait connected User.");
                socket = serverSocket.accept();
                LOGGER.info("User connected.");
                service.execute(() -> {
                    try {
                        new ClientHandler(this, socket);
                    } catch (IOException e) {
                        LOGGER.throwing(Level.FATAL, e);
                    }
                });
            }
        } catch (IOException e) {
            LOGGER.throwing(Level.FATAL, e);
        } finally {
            LOGGER.info("Server is offline.");
            authService.stop();
            service.shutdown();
        }
    }

    /**
     * Returns a list of server client names.
     * @return Array of strings with client names.
     */
    public String[] getClientsList() {
        StringBuilder clientsList = new StringBuilder();
        for (ClientHandler client : clients) {
            clientsList.append(client.getName()).append(" ");
        }
        return clientsList.toString().trim().split("\\s+");
    }

    /**
     * Returns the listener of the client by its name.
     * @param name The client's name.
     * @return The client's listener.
     */
    public ClientHandler getClient(String name) {
        for (ClientHandler client : clients) {
            if (client.getName().equals(name))
                return client;
        }
        return null;
    }

    /**
     * Sends a message to all clients of the server.
     * @param message The message object.
     * @see Message
     */
    public synchronized void sendAll(Message message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    /**
     * Checks whether the user's nickname is busy on the server.
     * @param nickName User's nickname.
     * @return The answer is whether the user's nickname is busy.
     */
    public boolean isNickBusy(String nickName) {
        for (ClientHandler client : clients) {
            if (client.getName().equals(nickName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Signs a new client listener to the list of server listeners.
     * @param o The client's listener.
     * @see ClientHandler
     */
    public synchronized void subscribe(ClientHandler o) {
        clients.add(o);
    }

    /**
     * Unsubscribes the client listener from the list of listeners from the server.
     * @param o The client's listener.
     * @see ClientHandler
     */
    public synchronized void unSubscribe(ClientHandler o) {
        clients.remove(o);
    }
}
