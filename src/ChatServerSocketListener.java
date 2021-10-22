import java.io.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.net.Socket;

public class ChatServerSocketListener  implements Runnable {
    private Socket socket;

    private ClientConnectionData client;
    private List<ClientConnectionData> clientList;
    private static List<String> clientNamesList = new ArrayList<>();

    private FileOutputStream fos = new FileOutputStream(new File("chatlog.txt"));

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

    /**
     * Broadcasts a message to all clients connected to the server.
     */
    public void broadcast(Message m, ClientConnectionData skipClient) {
        try {
            System.out.println("broadcasting: " + m);
            byte[] bytes = ("broadcasting: " + m).getBytes();
            fos.write(bytes);
            for (ClientConnectionData c : clientList){
                // if c equals skipClient, then c.
                // or if c hasn't set a userName yet (still joining the server)
                if ((c.getUserName()!= null)){
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

            MessageCtoS_Join joinMessage = (MessageCtoS_Join)in.readObject();
            if(clientNamesList.size() != 0) {
                while (clientNamesList.contains(joinMessage.userName)) {

                            MessageStoC_Error m = new MessageStoC_Error(joinMessage.userName);
                            System.out.println("Error: dup username");
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
                }
                else if (msg instanceof MessageCtoS_Chat) {
                    processChatMessage((MessageCtoS_Chat) msg);
                }
                else {
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
            } catch (IOException ex) {}
        }
    }
        
}