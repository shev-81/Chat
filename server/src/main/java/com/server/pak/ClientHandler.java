package com.server.pak;

import com.server.pak.services.ReaderMessages;
import lombok.Data;
import message.Message;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Client listener class, an object of this class is defined for each new
 * client connected to the server. The created object of this class is executed
 * in a separate thread. The main task of the listener is to receive messages and transfer
 * them to Reader Messages for processing.
 * @see ServerApp
 * @see java.util.concurrent.ExecutorService
 * @see ReaderMessages
 */
@Data
public class ClientHandler {

    /**
     * The logger variable.
     */
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);

    /**
     * The server socket variable.
     */
    private Socket socket;

    /**
     * An input stream variable for reading incoming objects.
     */
    private ObjectInputStream in;

    /**
     * An output stream variable for writing objects.
     */
    private ObjectOutputStream out;

    /**
     * A variable for storing the listener name.
     */
    private String name;

    /**
     * Variable server reference - {@link ServerApp Server}.
     */
    private ServerApp server;

    /**
     * Message Reader Variable - {@link ReaderMessages ReaderMessages}.
     */
    private ReaderMessages readerMessages;

    /**
     * When creating an object, the constructor saves references to server
     * objects and its socket, creates two I/O streams at the socket,
     * creates a Message Reader, runs two cycles sequentially client
     * authentication {@link #autentification autentification()} and
     * message reading {@link #readMessages readMessages()}.
     * @param server The server.
     * @param socket The socket of the connection with the client.
     * @throws IOException When an exception occurs, {@link #closeConnection сloseConnection()}
     * is called, which closes the I/O streams and the socket.
     */

    public ClientHandler(ServerApp server, Socket socket) throws IOException {
        try {
            this.server = server;
            this.socket = socket;
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            this.name = "";
            this.readerMessages = new ReaderMessages(server);
            autentification();
            readMessages();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.info("[Server]: Соединение c клиентом разорванно.");
            closeConnection();
        }
    }

    /**
     * Client authentication. The message object is passed to
     * {@link ReaderMessages to the Reader} for processing.
     * @throws IOException
     * @throws ClassNotFoundException they may occur when working with the I/O stream at the socket.
     */
    private void autentification() throws IOException, ClassNotFoundException{
        boolean chek = true;
        while (chek) {
            Message message = (Message) in.readObject();
            chek = readerMessages.read(message, this);
        }
    }

    /**
     * Reading incoming message objects. The messages are passed to {@link ReaderMessages Ридеру}
     * for further processing. When exceptions occur, {@link #closeConnection closeConnection()}
     */
    private void readMessages(){
        boolean chek = true;
        try {
            while (chek) {
                Message message = (Message) in.readObject();
                chek = readerMessages.read(message, this);
            }
        }catch (Exception exception){
            LOGGER.error(exception.toString());
            closeConnection();
        }
    }

    /**
     * Sends a message to the socket output stream.
     * @param message Message.
     */
    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.reset();
        } catch (IOException e) {
            LOGGER.throwing(Level.WARN, e);
        }
    }

    /**
     * Sends a private message to the client if it is registered on the server.
     * @param message Message.
     */
    public void sendPrivateMessage(Message message) {
        if (server.isNickBusy(message.getToNameU())) {
            server.getClient(message.getToNameU()).sendMessage(message);
        } else {
            LOGGER.info("[Server]: " + message.getToNameU() + " not in network");
        }
    }

    /**
     * Closes the current connection of the I/O streams and the socket.
     */
    public void closeConnection() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            LOGGER.throwing(Level.WARN, e);
        }
    }
}
