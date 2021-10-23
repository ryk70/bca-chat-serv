package main.chat_server;

import main.msgtypes.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class ChatServerSocketListener implements Runnable {
    private static final List<String> clientNamesList = new ArrayList<>();
    private final Socket socket;
    private ClientConnectionData client;
    private final List<ClientConnectionData> clientList;
    private final FileOutputStream fos = new FileOutputStream(new File("chatlog.txt"));

    public ChatServerSocketListener(Socket socket, List<ClientConnectionData> clientList) throws FileNotFoundException {
        this.socket = socket;
        this.clientList = clientList;
    }

    private void setup() throws Exception {
        ObjectOutputStream socketOut = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream socketIn = new ObjectInputStream(socket.getInputStream());
        String name = socket.getInetAddress().getHostName();

        client = new ClientConnectionData(socket, socketIn, socketOut, name);
        clientList.add(client);

        System.out.println("added client " + name);

    }

    private void processChatMessage(MessageCtoS_Chat m) {
        System.out.println("Chat received from " + client.getUserName() + " - broadcasting");
        broadcast(new MessageStoC_Chat(client.getUserName(), m.msg), client);
    }

    private void processPM(MessageCtoS_PM m) {
        System.out.println("PM received from " + client.getUserName() + " - sending to " + m.getToUser());
        sendPM(new MessageStoC_PM(client.getUserName(), m.msg, m.getToUser()));
    }

    public void sendPM(MessageStoC_PM m) {
        try {
            ClientConnectionData c = getCCD(m.getToUser());
            System.out.println(client.getUserName() + " PMing " + m.getToUser() + ": " + m);
            if (c == null) {
                Message m2 = new MessageStoC_DNE(m.getToUser());
                client.getOut().writeObject(m2);
            } else {
                c.getOut().writeObject(m);
                client.getOut().writeObject(new MessageStoC_PM_Response(m.getToUser(), m.msg));
            }
        } catch (Exception ex) {
            System.out.println("PM caught exception: " + ex);
            ex.printStackTrace();
        }
    }

    private ClientConnectionData getCCD(String toUser) {
        for (ClientConnectionData c : clientList) {
            if (c.getUserName().equals(toUser)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Broadcasts a message to all clients connected to the server.
     */
    public void broadcast(Message m, ClientConnectionData skipClient) {
        try {
            System.out.println("broadcasting: " + m);
            byte[] bytes = ("broadcasting: " + m).getBytes();
            fos.write(bytes);
            for (ClientConnectionData c : clientList) {
                // if c equals skipClient, then c.
                // or if c hasn't set a userName yet (still joining the server)
                if ((c.getUserName() != null)) {
                    c.getOut().writeObject(m);
                }
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            setup();
            ObjectInputStream in = client.getInput();

            MessageCtoS_Join joinMessage = (MessageCtoS_Join) in.readObject();
            if (clientNamesList.size() != 0) {
                while (clientNamesList.contains(joinMessage.userName) || joinMessage.userName.contains(" ")) {
                    MessageStoC_Error m = new MessageStoC_Error(joinMessage.userName);
                    if (joinMessage.userName.contains(" ")) {
                        System.out.println("Error: username contains space");
                    } else if (clientNamesList.contains(joinMessage.userName)) {
                        System.out.println("Error: dupe username");
                    }
                    client.getOut().writeObject(m);
                    joinMessage = (MessageCtoS_Join) in.readObject();
                }
            }

            client.setUserName(joinMessage.userName);
            clientNamesList.add(client.getUserName());
            System.out.println("Added: " + clientNamesList.toString());
            broadcast(new MessageStoC_Welcome(joinMessage.userName), client);

            while (true) {
                Message msg = (Message) in.readObject();
                if (msg instanceof MessageCtoS_Quit) {
                    break;
                } else if (msg instanceof MessageCtoS_PM) {
                    processPM((MessageCtoS_PM) msg);
                } else if (msg instanceof MessageCtoS_Chat) {
                    processChatMessage((MessageCtoS_Chat) msg);
                } else {
                    System.out.println("Unhandled message type: " + msg.getClass());
                }
            }
        } catch (Exception ex) {
            if (ex instanceof SocketException) {
                System.out.println("Caught socket ex for " +
                        client.getName());
            } else {
                System.out.println(ex);
                ex.printStackTrace();
            }
        } finally {
            //Remove client from clientList
            clientList.remove(client);

            // Notify everyone that the user left.
            broadcast(new MessageStoC_Exit(client.getUserName()), client);

            try {
                client.getSocket().close();
            } catch (IOException ex) {
            }
        }
    }

}
