package com.server.pak;

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
 * Класс запуска сервера, транспортная система обмена сообщениями основана на обмене
 * объектами между клиентом и сервером, используется сериализация и десериализация
 * объектов сообщений. (Java IO). Сервер в своей работе использует: Сервис авторизации
 * пользователей, Слушателя подсоединившегося клиента.
 * @see AuthService
 * @see ClientHandler
 */
@Data
public class ServerApp {

    /**
     * Логер проекта настройки определены в файле log4j2.xml папка ресурсов.
     */
    private static final Logger LOGGER = LogManager.getLogger(ServerApp.class);

    /**
     * Список подсоединившихся слушателей клиентов.
     * @see ClientHandler
     */
    private ArrayList<ClientHandler> clients;

    /**
     * Переменная для сохранения сокета сервера.
     */
    private Socket socket = null;

    /**
     * Сервис авторизации пользователей.
     * @see AuthService
     */
    private AuthService authService;

    /**
     * Конструктор создает объекты: AuthService, ExecutorService, ServerSocket
     * и запускает внутренний цикл ждущий соединения клиента в методе - serverSocket.accept().
     * При успешном подключении для клиента через ExecutorService выделяется новый поток и
     * создается слушатель ClientHandler.
     * @see AuthService
     * @see ExecutorService
     * @see ServerSocket
     * @see ClientHandler
     *
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
     * Возвращает список имен клиентов сервера.
     * @return Массив строк с именами клиентов.
     */
    public String[] getClientsList() {
        StringBuilder clientsList = new StringBuilder();
        for (ClientHandler client : clients) {
            clientsList.append(client.getName() + " ");
        }
        String[] parts = clientsList.toString().trim().split("\\s+");
        return parts;
    }

    /**
     * Возвращает слушатель клиента по его имени.
     * @param name Имя клиента.
     * @return Слушатель клиента.
     */
    public ClientHandler getClient(String name) {
        for (ClientHandler client : clients) {
            if (client.getName().equals(name))
                return client;
        }
        return null;
    }

    /**
     * Делает рассылку сообщения всем клиентам сервера.
     * @param message Объект сообщения.
     * @see Message
     */
    public synchronized void sendAll(Message message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    /**
     * Проверяет занят ли ник пользователя на сервере.
     * @param nickName Ник пользователя.
     * @return Ответ занят ли Ник пользователя.
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
     * Подписывает нового слушателя клиента в список слушателей сервера.
     * @param o Слушатель клиента.
     * @see ClientHandler
     */
    public synchronized void subscribe(ClientHandler o) {
        clients.add(o);
    }

    /**
     * Отписывает слушатель клиента из списка слушателей с сервера.
     * @param o Слушатель клиента.
     * @see ClientHandler
     */
    public synchronized void unSubscribe(ClientHandler o) {
        clients.remove(o);
    }
}
