public class MessageCtoS_PM extends MessageCtoS_Chat{
    private String toUser;
    public MessageCtoS_PM(String msg, String toUser){
        super(msg);
        this.toUser = toUser;
    }
    public String getToUser() {
        return toUser;
    }
}
