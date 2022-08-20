package com.server.pak;

import java.sql.SQLException;

/**
 * Интерфейс предназначенный определить основные методы работы с базой данных.
 */
public interface AuthService {

    /**
     * Должен определить соединение с Базой данных.
     */
    void start();

    /**
     * Должен определять закрытие соединения с Базой данных.
     */
    void stop();

    /**
     * Должен возвращать Имя пользователя по его логину и паролю.
     * @param login Логин пользователя.
     * @param pass Пароль пользователя.
     * @return Имя пользователя.
     */
    String getNickByLoginPass(String login, String pass);

    /**
     * Должен определять регистрация нового пользователя.
     * @param part Имя пользователя.
     * @param part1 Логин пользователя.
     * @param part2 Пароль пользователя.
     * @return true при успешной регистарции.
     */
    boolean registerNewUser(String part, String part1, String part2);

    /**
     * Должен определить как будет обновлено имя пользователя.
     * @param newName Новое имя пользователя.
     * @param oldName Старое имя пользователя.
     * @return true при успешном обновлении.
     * @throws SQLException
     */
    boolean updateNickName(String newName,String oldName);
}
