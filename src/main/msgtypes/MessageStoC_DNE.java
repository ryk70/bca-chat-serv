package main.msgtypes;

public class MessageStoC_DNE extends Message {
    private final String toUser;

    public MessageStoC_DNE(String toUser) {
        this.toUser = toUser;
    }

    public String toString() {
        return this.toUser + "does not exist. main.msgtypes.Message was not sent.";
    }
}
