public class MessageStoC_PM extends MessageStoC_Chat{
    private String toUser;
    public String username;
    public MessageStoC_PM(String username, String msg, String toUser) {
        super(username, msg);
        this.username = username;
        this.toUser = toUser;
    }
    public String toString() {
        return "PM from " + username + ": " + msg;
    }
    public String getToUser() {return this.toUser;}
}
