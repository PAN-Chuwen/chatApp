import java.io.Serializable;

public class Message implements Serializable, Comparable<Message> {
    String userName;
    Object messageContent;
    long time;

    public Message(String userName, Object messageContent) {
        this.userName = userName;
        this.messageContent = messageContent;
        this.time = System.currentTimeMillis();
    }

    public Message(String userName, Object messageContent, long time) {
        this.userName = userName;
        this.messageContent = messageContent;
        this.time = time;
    }

    public String getUserName() {
        return userName;
    }

    public Object getMessageContent() {
        return messageContent;
    }

    public void print() {
        System.out.print("(" + userName + ")" + ": " + messageContent);
    }

    /* the earliest message will be on the top of priority queue */
    @Override
    public int compareTo(Message m) {
        int comparisonResult = Long.compare(m.time, time);
        return comparisonResult;
    }
}
