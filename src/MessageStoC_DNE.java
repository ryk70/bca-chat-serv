public class MessageStoC_DNE extends Message {
    private String toUser;
    public MessageStoC_DNE(String toUser) {
        this.toUser = toUser;
    }
    public String toString(){return this.toUser + "does not exist. Message was not sent.";}
}
