package com.server.pak;

import java.sql.SQLException;

/**
 * Интерфейс предназначенный определить основные методы для работы с подключаемой базой данных.
 */
public interface AuthService {
    void start();
    void stop();
    String getNickByLoginPass(String login, String pass);
    boolean registerNewUser(String part, String part1, String part2);
    boolean updateNickName(String newName,String oldName) throws SQLException;
}
