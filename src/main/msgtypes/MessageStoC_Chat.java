package main.msgtypes;

public class MessageStoC_Chat extends Message {
    public String userName;
    public String msg;

    public MessageStoC_Chat(String userName, String msg) {
        this.userName = userName;
        this.msg = msg;
    }

    public String toString() {
        return "Chat main.msgtypes.Message from " + userName + ": " + msg;
    }
}