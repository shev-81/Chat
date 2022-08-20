package com.server.pak;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс работающий с Базой Данных пользователей. Предназначен для выполнения CRUD операций над записями пользователей.
 */
public class AuthServiceBD implements AuthService {

    /**
     * Переменная логера.
     */
    private static final Logger LOGGER = LogManager.getLogger(AuthServiceBD.class);

    /**
     * Список хранящий список всех пользователей из БД.
     */
    private final List<User> listUser;

    /**
     * Соединение с БД.
     */
    private static Connection connection;

    /**
     * Переменная для работы с БД.
     */
    private static Statement stmt;

    /**
     * Класс пользователя.
     */
    private class User {

        /**
         * Переменная имя пользователя.
         */
        private String name;

        /**
         * Переменаня логин пользователя.
         */
        private String login;

        /**
         * Пременная пароль пользователя.
         */
        private String pass;

        /**
         * Параметризированный конструктор для создания объекта пользователя с
         * @param name Имя пользователя.
         * @param login Логин пользователя.
         * @param pass Пароль пользователя.
         */
        public User(String name, String login, String pass) {
            this.name = name;
            this.login = login;
            this.pass = pass;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * Конструктор запускает соединение с Базой данных и подгружает список пользователей.
     * Использует {@link #start start()} и {@link #loadUsers loadUsers()} методы
     */
    AuthServiceBD() {
        listUser = new ArrayList<>();
        try {
            start();
            loadUsers();
            LOGGER.info("Загрузили пользователей из БД AuthServiceBD");
        } catch (SQLException e) {
            LOGGER.throwing(Level.ERROR, e);
        } catch (Exception e) {
            LOGGER.throwing(Level.FATAL, e);
        }
    }

    /**
     * Регистрирует нового пользователя, а при невозможности бросает исключение.
     * @param nickName Имя пользователя.
     * @param login Логин пользователя.
     * @param pass Пароль пользователя.
     * @return true если регистрация прошла успешно.
     */
    @Override
    public boolean registerNewUser(String nickName, String login, String pass) {
        int result = 0;
        try {
            result = stmt.executeUpdate("INSERT INTO users (NickName, login, pass) VALUES ('" + nickName + "','" + login + "','" + pass + "');");
            listUser.add(new User(nickName, login, pass));
        } catch (SQLException e) {
            LOGGER.throwing(Level.ERROR, e);
        }
        return result > 0;
    }

    /**
     * Обновляет Имя пользователя в Базе данных и списке пользователей на сервере, а при невозможности бросает исключение.
     * @param newName Новое имя пользователя.
     * @param oldName Старое имя пользователя.
     * @return true если обновление прошло успешно.
     * @throws SQLException при ошибке запроса.
     */
    @Override
    public boolean updateNickName(String newName, String oldName) {
        int result = 0;
        try {
            result = stmt.executeUpdate("UPDATE users SET NickName = '" + newName + "' WHERE NickName = '" + oldName + "';");
            if (result > 0) {
                for (int i = 0; i < listUser.size(); i++) {
                    if (listUser.get(i).getName().equals(oldName)) {
                        listUser.get(i).setName(newName);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Ошибка в смене имени пользователя");
        }
        return result > 0;
    }

    /**
     * Загружает из Базы данных всех пользователей в список пользователей на сервере.
     * @throws SQLException при ошибке запроса.
     */
    public void loadUsers() throws SQLException {
        try (ResultSet rs = stmt.executeQuery("SELECT * FROM users;")) {
            while (rs.next()) {
                listUser.add(new User(
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)
                ));
            }
        }
    }

    /**
     * Подключает соединение к Базе данных, и создает объект стэйтмент.
     * @throws SQLException при невозможности подключиться.
     */
    @Override
    public void start() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:userschat.db");
            stmt = connection.createStatement();
        } catch (SQLException e) {
            LOGGER.throwing(Level.ERROR, e);
            throw new RuntimeException("Не возможно подключиться к БД.");
        }
    }

    /**
     * Закрывает соединение с Базой данных.
     */
    @Override
    public void stop() {
        try {
            if (stmt != null)
                stmt.close();
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            LOGGER.throwing(Level.ERROR, e);
        }
    }

    /**
     * Возвращает Имя пользователя по его Логину и Паролю.
     * @param login Логин пользователя.
     * @param pass Пароль пользователя.
     * @return Имя пользователя.
     */
    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (User user : listUser) {
            if (user.login.equals(login) && user.pass.equals(pass))
                return user.name;
        }
        return null;
    }
}
