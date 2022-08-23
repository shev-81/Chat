package com.client.pak;

import com.client.pak.services.ReaderMessages;
import javafx.application.Platform;
import message.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * A class describing the connection to the server.
 */
public class Connection implements Runnable {

    /**
     * Server address.
     */
    private final String SERVER_ADDR = "localhost";

    /**
     * Server port.
     */
    private final int SERVER_PORT = 8189;

    /**
     * Timer for closing the network connection.
     */
    public static final int TIME_COUNT = 120000;

    /**
     * Connection socket.
     */
    private Socket socket;

    /**
     * Incoming stream.
     */
    private ObjectInputStream in;

    /**
     * Outgoing flow.
     */
    private ObjectOutputStream out;

    /**
     * Application controller.
     * @see Controller
     */
    private final Controller controller;

    /**
     * Message reader.
     * @see ReaderMessages
     */
    private final ReaderMessages readerMessages;

    /**
     * The constructor saves a link to the application controller, creates
     * a Message Reader and opens a network connection.
     * @param controller Application controller.
     */
    public Connection(Controller controller) {
        this.controller = controller;
        this.readerMessages = new ReaderMessages(controller, this);
        openConnection();
    }

    /**
     * Starts 2 consecutive cycles of reading incoming messages in a separate thread.
     * {@link #autorizQuestion() autorizQuestion()} and  {@link #readMsg() readMsg()}.
     */
    @Override
    public void run() {
        autorizQuestion();
        readMsg();
    }

    /**
     * Opens a network connection and two I/O streams. Starts the
     * network connection blocking timer. When authorization is
     * completed, the lock timer will become infinite. If authorization
     * fails and the network connection is inactive for 2 minutes,
     * the network connection will be closed.
     */
    private void openConnection() {
        try {
            socket = new Socket(SERVER_ADDR, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            socket.setSoTimeout(TIME_COUNT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the authorization verification cycle for the user.
     */
    public void autorizQuestion() {
        try {
            boolean chek = true;
            while (chek) {
                chek = readerMessages.read((Message) in.readObject());
            }
            Platform.runLater(() -> controller.getFileWorker().loadAllMsg());
        } catch (IOException | ClassNotFoundException e) {
            controller.changeStageToAuth();
            closeConnection();
        }
    }

    /**
     * Starts a cycle of reading incoming messages.
     * (It starts after successful authorization.)
     */
    public void readMsg() {
        try {
            boolean chek = true;
            while (chek) {
                chek = readerMessages.read((Message) in.readObject());
            }
        } catch (IOException | ClassNotFoundException e) {
            controller.changeStageToAuth();
            closeConnection();
        }
    }

    /**
     * Sends a message.
     * @param message Message.
     */
    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    /**
     * Closes the I/O streams from the socket and the socket itself.
     */
    public void closeConnection() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
