package message;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * Класс сообщений пользователя.
 */
@Data
public class Message implements Serializable {

    /**
     * Переменная для сериализации и десериализации.
     */
    private static final long serialVersionUID = 9176873029745254542L;

    /**
     * Переменная тип сообщения.
     */
    public enum MessageType {
        AUTHOK, AUTHNO, CONECTED, DISCONECTED, CHANGENAME, PERSONAL, UMESSAGE,
        AUTH, END, REGUSER, STATUS
    }

    /**
     * Пеерменная текущей даты.
     */
    private Date date;

    /**
     * Перменная Имя клиента, кто посылает сообщение.
     */
    private String nameU;

    /**
     * Переменная Имя клиента, которому адресованно сообщение.
     */
    private String toNameU;

    /**
     * Переменная содержащая текст сообщения.
     */
    private String text;

    /**
     * Переменная Логин пользователя.
     */
    private String login;

    /**
     * Переменная Пароль пользователя.
     */
    private String pass;

    /**
     * Переменная тип сообщения.
     */
    private MessageType type;

    /**
     * Переменная - список пользователей.
     */
    private String [] usersList;

    /**
     * Пустой конструктор определяет текущую дату для сообщения.
     */
    public Message() {
        this.date = new Date();
    }

    /**
     * Типизированный контруктор определяет {@link MessageType тип сообщения}.
     * @param type тип сообщения.
     */
    public Message(MessageType type) {
        this.date = new Date();
        this.type = type;
    }
}
