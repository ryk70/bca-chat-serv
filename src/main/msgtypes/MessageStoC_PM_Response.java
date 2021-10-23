package main.msgtypes;

public class MessageStoC_PM_Response extends Message {
    private final String toUser;
    private final String msg;

    public MessageStoC_PM_Response(String toUser, String msg) {
        this.toUser = toUser;
        this.msg = msg;
    }

    public String toString() {
        return "TO " + toUser + ": " + msg;
    }
}
