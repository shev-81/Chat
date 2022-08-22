package com.client.pak;

import com.client.pak.services.ReaderMessages;
import javafx.application.Platform;
import message.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Класс описывающий соединение с сервером.
 */
public class Connection implements Runnable {

    /**
     * Адресс сервера.
     */
    private final String SERVER_ADDR = "localhost";

    /**
     * Порт сервера.
     */
    private final int SERVER_PORT = 8189;

    /**
     * Таймер закрытия сетевого соединения.
     */
    public static final int TIME_COUNT = 120000;

    /**
     * Сокет соединения.
     */
    private Socket socket;

    /**
     * Входящий поток.
     */
    private ObjectInputStream in;

    /**
     * Исходящий поток.
     */
    private ObjectOutputStream out;

    /**
     *  Контроллер приложения.
     * @see Controller
     */
    private final Controller controller;

    /**
     * Ридер сообщений.
     * @see ReaderMessages
     */
    private final ReaderMessages readerMessages;

    /**
     * Конструктор сохраняет ссылку на контроллер приложения, создает Ридер
     * сообщений и открвает сетевое соединение.
     * @param controller Контроллер приложения.
     *
     */
    public Connection(Controller controller) {
        this.controller = controller;
        this.readerMessages = new ReaderMessages(controller, this);
        openConnection();
    }

    /**
     * Запускает в отдельном потоке последовательно 2 цикла чтения входящих сообщений.
     * {@link #autorizQuestion() autorizQuestion()} и {@link #readMsg() readMsg()}.
     */
    @Override
    public void run() {
        autorizQuestion();
        readMsg();
    }

    /**
     * Открывает сетевое соединения и два потока ввода вывода. Запускает таймер блокировки
     * сетевого соединения. При прохождении авторизации таймер блокировки станет бесконечным.
     * При не прохождении авторизации и бездействии в течении 2 минут сетевое соединение будет закрыто.
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
     * Запускает цикл проверки авторизации для пользователя.
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
     * Запускает цикл чтения входящих сообщений. (Запускается после успешного прохождения авторизации.)
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
     * Посылает сообщение
     * @param message Сообщение.
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
     * Закрывает потоки ввода-вывода из сокета и сам сокет.
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
