import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    int serverPort = 8888;
    ServerSocket server;
    ArrayList<ClientThread> clientThreadList;
    ListenThread listenThread;
    MessageHandlingThread messageHandlingThread;
    LinkedBlockingQueue<Object> messages;

    public Server() {
        try {
            clientThreadList = new ArrayList<>();
            server = new ServerSocket(serverPort);
            System.out.println("Server running: " + server);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // init threads and run them
        listenThread = new ListenThread();
        messageHandlingThread = new MessageHandlingThread();
        listenThread.start();
        messageHandlingThread.start();
    }

    public static void main(String args[]) {
        new Server(); // run the server, the constructor would run all sub-threads.
    }

    /* Methods (utilities) */
    public void sendToAll(Object msg) {
        for (ClientThread clientThread : clientThreadList) {
            sendToOne(msg, clientThread);
        }
    }

    public void sendToOne(Object msg, ClientThread clientThread) {
        clientThread.write(msg);
    }

    /* Subclasses */

    /*
     * ListenThread is in charge of establishing connections with new clients,
     * when a new connection is established, a new ClientThread will be created and
     * added to clientThreadList
     */
    class ListenThread extends Thread {
        @Override
        public void run() {
            while (true) { // always listening to new clients
                try {
                    // blocked until a new client connects to server
                    Socket socket = server.accept();
                    System.out.println("New connection established: " + socket);
                    // create new thread to get message from the new client, and keep the loop going
                    ClientThread curClientThread = new ClientThread(socket);
                    // add it to threadList
                    clientThreadList.add(curClientThread);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /*
     * ClientThread is in charge of reading objects from A SPECIFIC client,
     * via ObjectInputStream,
     * and put it in Messages
     */
    class ClientThread extends Thread {

        Socket socket;
        ObjectInputStream in;
        ObjectOutputStream out;

        public ClientThread(Socket socket) throws IOException {
            this.socket = socket;
            try {
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            while (true) {
                try {
                    Object object = in.readObject(); // read from client
                    messages.put(object); // and put into messsages, manipulated in messageHandlingThread
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // write to client
        void write(Object msg) {
            try {
                out.writeObject(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * MessageHandlingThread is in charge of handling objects stored in Messages
     * current version sends message to all clients regardless of its content
     */
    class MessageHandlingThread extends Thread {
        Object message;

        @Override
        public void run() {
            while (true) {
                try {
                    // blocked until there's new message
                    message = messages.take();
                    sendToAll(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
