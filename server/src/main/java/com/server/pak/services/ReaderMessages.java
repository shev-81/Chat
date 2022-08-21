package com.server.pak.services;

import com.server.pak.ClientHandler;
import com.server.pak.ServerApp;
import message.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Класс, определяющий методы чтения сообщений и их обработки, в зависимости от их типов.
 * @see Message
 * @see ServerApp
 */
public class ReaderMessages {

    /**
     * Логер проекта настройки определены в файле log4j2.xml папка ресурсов.
     */
    private static final Logger LOGGER = LogManager.getLogger(ReaderMessages.class);

    /**
     * Переменная для сохранения сервера.
     */
    private final ServerApp server;

    /**
     * Конструктор ридера сохраняет в себе ссылку на сервер.
     * @param server Сервер
     */
    public ReaderMessages(ServerApp server) {
        this.server = server;
    }

    /**
     * Получает сообщение и передает его на обработку в зависимости от типа сообщения.
     * @param message Сообщение.
     * @param clientHandler Слушатель.
     * @return Истинно, но возможно и ложно если требуется выйти из цикла авторизации.
     */
    public boolean read(Message message, ClientHandler clientHandler){
        Message.MessageType type = message.getType();
        switch (type) {
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
     * Проводит авторизацию пользователя, проверяя базу данных, а так же текущую сессию на сервере.
     * @param message Сообщение.
     * @param clientHandler Слушатель.
     * @return true если авторизация пройдена успешно.
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
     * Регистрирует слушатель клиента на сервере, рассылает всем слушателям сообщение о
     * подключении нового пользователя и высылает всем клиентам обновленный список пользователей сервера.
     * @param message Сообщение.
     * @param clientHandler Слушатель.
     * @return false - если регистрация пользователя не прошла в сервисе регистрации.
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
     * Отписывает слушателя из подписки с сервера, рассылает всем слушателям сообщение
     * о выходе из сети клиента.
     * @param clientHandler  Слушатель.
     */
    public void end(ClientHandler clientHandler){
        server.unSubscribe(clientHandler);
        Message message = new Message(Message.MessageType.DISCONECTED);
        message.setNameU(clientHandler.getName());
        server.sendAll(message);
        LOGGER.info("[Server]: " + clientHandler.getName() + " disconnected!");
    }

    /**
     * Изменяет имя клиента, сохраняя изменения в Базе Данных, Имя у слушателя на сервере
     * и оповещая всех слушателей сервера об изменениях.
     * @param message Сообщение.
     * @param clientHandler Слушатель.
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
     * Посылает персональное сообщение.
     * @param message Сообщение.
     * @param clientHandler Слушатель.
     */
    public void personal(Message message, ClientHandler clientHandler){
        clientHandler.sendPrivateMessage(message);
    }

    /**
     * Делает массовую рассылку.
     * @param message Сообщение.
     */
    public void uMessage(Message message){
        server.sendAll(message);
    }

    /**
     * Делает массовую рассылку об изменении статуса пользователя.
     * @param message Сообщение.
     */
    public void status (Message message){
        server.sendAll(message);
    }
}
