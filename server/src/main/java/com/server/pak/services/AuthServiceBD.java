package com.server.pak.services;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that works with a user Database. Designed to
 * perform CRUD operations on user records.
 */
public class AuthServiceBD implements AuthService {

    /**
     * The logger variable.
     */
    private static final Logger LOGGER = LogManager.getLogger(AuthServiceBD.class);

    /**
     * A list that stores a list of all users from the database.
     */
    private final List<User> listUser;

    /**
     * Connection to the database.
     */
    private static Connection connection;

    /**
     * A variable for working with the database.
     */
    private static Statement stmt;

    /**
     * User class.
     */
    private class User {

        /**
         * The user name variable.
         */
        private String name;

        /**
         * The user login variable.
         */
        private String login;

        /**
         * The user's password variable.
         */
        private String pass;

        /**
         * Parameterized constructor for creating a user object with
         * @param name Username.
         * @param login User login.
         * @param pass User password.
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
     * The constructor starts a connection to the Database and
     * loads the list of users.
     * Uses {@link #start start()} and {@link #loadUsers loadUsers()}  methods.
     */
    public AuthServiceBD() {
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
     * Registers a new user, and throws an exception if it is impossible.
     * @param nickName Username..
     * @param login User login.
     * @param pass The user's password.
     * @return true if the registration was successful.
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
     * Updates the User Name in the Database and the list of users on the
     * server, and throws an exception if it is impossible.
     * @param newName New user name.
     * @param oldName The old user name.
     * @return true if the update was successful.
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
     * Loads all users from the Database to the list of users on the
     * server.
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
     * Connects the connection to the Database, and creates a statement
     * object.
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
     * Closes the Database connection.
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
     * Returns the user's Name by his Username and Password.
     * @param login User login.
     * @param pass The user's password.
     * @return Username.
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
