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
 * Класс слушателя клиента, объект этого класса определяется для каждого нового
 * клиента подключившегося к серверу. Созданный объект этого класса выполняется
 * в отдельном потоке. Основная задача слушателя в получении сообщений и передече
 * их на обработку в ReaderMessages.
 * @see ServerApp
 * @see java.util.concurrent.ExecutorService
 * @see ReaderMessages
 */
@Data
public class ClientHandler {

    /**
     *  Переменная логера.
     */
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);

    /**
     * Переменная сокет сервера.
     */
    private Socket socket;

    /**
     * Переменная потока ввода для чтения входящих объектов.
     */
    private ObjectInputStream in;

    /**
     * Переменная потока вывода для записи объектов.
     */
    private ObjectOutputStream out;

    /**
     * Переменная для сохранения имени слушателя.
     */
    private String name;

    /**
     * Переменная ссылка на сервер - {@link ServerApp Server}.
     */
    private ServerApp server;

    /**
     * Переменная Ридера сообщений - {@link ReaderMessages ReaderMessages}.
     */
    private ReaderMessages readerMessages;

    /**
     * Конструктор при сроздании объекта сохраняет ссылки на объекты сервера и его сокет,
     * создает два потока ввода вывода у сокета, создает Ридер сообщений, запускает последовательно два цикла
     * аутентификации клиента {@link #autentification autentification()} и чтения сообщений {@link #readMessages readMessages()}.
     * @param server Сервер.
     * @param socket Сокет соединения с клиентом.
     * При возникновении исключений вызывается {@link #closeConnection closeConnection()}
     * который закрывает потоки ввода вывода и сокет.
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
     * Аутентификация клиента. Объект сообщения передается {@link ReaderMessages Ридеру} для обработки.
     * @throws IOException
     * @throws ClassNotFoundException могут возникнуть при работе спотоком ввода вывода у сокета.
     */
    private void autentification() throws IOException, ClassNotFoundException{
        boolean chek = true;
        while (chek) {
            Message message = (Message) in.readObject();
            chek = readerMessages.read(message, this);
        }
    }

    /**
     * Чтение входящих объектов сообщений. Сообщения передаются {@link ReaderMessages Ридеру} сообщений для последующей обработки.
     * При возникновении исключений вызывается {@link #closeConnection closeConnection()}
     *
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
     * Посылает сообщение в поток вывода сокета.
     * @param message Сообщение.
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
     * Посылает приватное сообщение клиенту если он зарегестрирован на сервере.
     * @param message Сообщение.
     */
    public void sendPrivateMessage(Message message) {
        if (server.isNickBusy(message.getToNameU())) {
            server.getClient(message.getToNameU()).sendMessage(message);
        } else {
            LOGGER.info("[Server]: " + message.getToNameU() + " not in network");
        }
    }

    /**
     * Закрывает текущее соединение потоков ввода вывода и сокета.
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
