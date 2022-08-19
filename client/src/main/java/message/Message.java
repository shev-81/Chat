package message;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class Message implements Serializable {
    private static final long serialVersionUID = 9176873029745254542L;
    public enum MessageType {
        AUTHOK, AUTHNO, CONECTED, DISCONECTED, CHANGENAME, PERSONAL, UMESSAGE,
        AUTH, END, REGUSER, STATUS
    }

    private Date date;
    private String nameU;
    private String toNameU;
    private String text;
    private String login;
    private String pass;
    private MessageType type;
    private String [] usersList;

    public Message() {
        this.date = new Date();
    }

    public Message(MessageType type) {
        this.date = new Date();
        this.type = type;
    }
}
