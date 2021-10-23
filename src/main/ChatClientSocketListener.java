package main;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class ChatClientSocketListener implements Runnable {
    public static List<String> usernames = new ArrayList<>();
    public Message msg;
    private final ObjectInputStream socketIn;

    public ChatClientSocketListener(ObjectInputStream socketIn) {
        this.socketIn = socketIn;
    }

    private void processChatMessage(MessageStoC_Chat m) {
        System.out.println(m.userName + ": " + m.msg);
    }

    private void processWelcomeMessage(MessageStoC_Welcome m) {
        System.out.println(m.userName + " joined the server!");
        ChatClientSocketListener.usernames.add(m.userName);
    }

    private void processExitMessage(MessageStoC_Exit m) {
        System.out.println(m.userName + " left the server!");
    }

    private void processErrorMessage(MessageStoC_Error m) {
        System.out.println(m);
    }

    private void processErrorMessage(MessageStoC_DNE m) {
        System.out.println(m);
    }

    private void processPM(MessageStoC_PM m) {
        System.out.println("FROM " + m.username + ": " + m.msg);
    }

    private void processPM_Response(MessageStoC_PM_Response m) {
        System.out.println(m.toString());
    }

    @Override
    public void run() {
        try {
            while (true) {
                msg = (Message) socketIn.readObject();

                if (msg instanceof MessageStoC_Welcome) {
                    processWelcomeMessage((MessageStoC_Welcome) msg);
                } else if (msg instanceof MessageStoC_PM) {
                    processPM((MessageStoC_PM) msg);
                } else if (msg instanceof MessageStoC_PM_Response) {
                    processPM_Response((MessageStoC_PM_Response) msg);
                } else if (msg instanceof MessageStoC_Chat) {
                    processChatMessage((MessageStoC_Chat) msg);
                } else if (msg instanceof MessageStoC_Exit) {
                    processExitMessage((MessageStoC_Exit) msg);
                } else if (msg instanceof MessageStoC_Error) {
                    processErrorMessage((MessageStoC_Error) msg);
                } else if (msg instanceof MessageStoC_DNE) {
                    processErrorMessage((MessageStoC_DNE) msg);
                } else {
                    System.out.println("Unhandled message type: " + msg.getClass());
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception caught in listener - " + ex);
        } finally {
            System.out.println("Client Listener exiting");
        }
    }
}
