package main;

public class MessageStoC_Error extends Message {
    public String userName;

    public MessageStoC_Error(String userName) {
        this.userName = userName;
    }

    public String toString() {
        return String.format("\"%s\" is already taken or is illegal (has space). Please pick another username.", this.userName);
    }
}
