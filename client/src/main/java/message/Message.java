package message;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * The user's message class.
 */
@Data
public class Message implements Serializable {

    /**
     * Variable for serialization and deserialization.
     */
    private static final long serialVersionUID = 9176873029745254542L;

    /**
     * Variable message type.
     */
    public enum MessageType {
        AUTHOK, AUTHNO, CONECTED, DISCONECTED, CHANGENAME, PERSONAL, UMESSAGE,
        AUTH, END, REGUSER, STATUS
    }

    /**
     * Variable of the current date.
     */
    private Date date;

    /**
     * The variable is the name of the client who is sending the message.
     */
    private String nameU;

    /**
     * The variable is the name of the client to whom the message is
     * addressed.
     */
    private String toNameU;

    /**
     * A variable containing the message text.
     */
    private String text;

    /**
     * The User Login variable.
     */
    private String login;

    /**
     * The user's Password variable.
     */
    private String pass;

    /**
     * Variable message type.
     */
    private MessageType type;

    /**
     * The variable is a list of users.
     */
    private String [] usersList;

    /**
     * An empty constructor defines the current date for the message.
     */
    public Message() {
        this.date = new Date();
    }

    /**
     * The typed constructor defines the {@link MessageType message type}.
     * @param type message type.
     */
    public Message(MessageType type) {
        this.date = new Date();
        this.type = type;
    }
}
