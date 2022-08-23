package com.server.pak.services;

import java.sql.SQLException;

/**
 * An interface designed to define the basic methods of working
 * with the database.
 */
public interface AuthService {

    /**
     * Must determine the connection to the Database.
     */
    void start();

    /**
     * Must determine the closure of the Database connection.
     */
    void stop();

    /**
     * Must return the user Name by his username and password.
     * @param login User login.
     * @param pass The user's password.
     * @return Username.
     */
    String getNickByLoginPass(String login, String pass);

    /**
     * Must determine the registration of a new user.
     * @param part Username.
     * @param part1 User login.
     * @param part2 The user's password.
     * @return true upon successful registration.
     */
    boolean registerNewUser(String part, String part1, String part2);

    /**
     * Must determine how the username will be updated.
     * @param newName Username.
     * @param oldName The old user name.
     * @return true if the update is successful.
     */
    boolean updateNickName(String newName,String oldName);
}
