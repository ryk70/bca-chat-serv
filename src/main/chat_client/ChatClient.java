package main.chat_client;// import com.sun.security.ntlm.Client;
// import sun.awt.windows.WPrinterJob;

import main.ANSI_colors;
import main.msgtypes.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private final Socket socket;
    private final ObjectOutputStream socketOut;
    private final ObjectInputStream socketIn;
    private ChatClientSocketListener ccsl;
    private Thread thread;


    public ChatClient(String ip, int port) throws Exception {
        socket = new Socket(ip, port);
        socketOut = new ObjectOutputStream(socket.getOutputStream());
        socketIn = new ObjectInputStream(socket.getInputStream());
    }

    public static void main(String[] args) throws Exception {
        Scanner userInput = new Scanner(System.in);
        System.out.println("What's the server IP? ");
        String serverip = userInput.nextLine();

        System.out.println("What's the server port? ");
        int port = userInput.nextInt();
        userInput.nextLine();

        ChatClient cc = new ChatClient(serverip, port);

        cc.startListener();
        cc.mainLoop(userInput);

        userInput.close();
        cc.closeSockets();
    }

    // start a thread to listen for messages from the server
    private void startListener() {
        ccsl = new ChatClientSocketListener(socketIn);
        thread = new Thread(ccsl);
        thread.start();
    }

    private void sendMessage(Message m) throws Exception {
        socketOut.writeObject(m);
//        socketOut.flush();
    }

    private void mainLoop(Scanner in) throws Exception {
        System.out.print("Chat sessions has started - enter a user name: ");
        String name = in.nextLine().trim();

        sendMessage(new MessageCtoS_Join(name));

        String line = in.nextLine().trim();
        while (!line.toLowerCase().startsWith("/quit")) {
            if (line.toLowerCase().startsWith("/pm")) {
                String[] lineArr = line.split(" ");
                sendMessage(new MessageCtoS_PM(line.substring(line.indexOf(lineArr[1])
                        + lineArr[1].length() + 1), lineArr[1]));
            } else if (line.toLowerCase().startsWith("/hl")) {
                sendMessage(new MessageCtoS_Chat(
                        ANSI_colors.RED_BACKGROUND_BRIGHT
                                + line.substring(4)
                                + ANSI_colors.RESET));
            } else if (line.toLowerCase().startsWith("/help")) {
                System.out.print("/quit : Terminate connection with server\n" +
                        "/pm {username} : Send a private message to the given user\n" +
                        "/hl {text} : Highlight your message\n");
            } else
                sendMessage(new MessageCtoS_Chat(line));
            line = in.nextLine().trim();
        }
        if (!line.toLowerCase().startsWith("/quit")) {
            sendMessage(new MessageCtoS_Quit());
        }
    }

    private void closeSockets() throws Exception {
        socketIn.close();
        socketOut.close();
        socket.close();
    }

}
