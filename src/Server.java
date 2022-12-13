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
    ArrayList<ConnectToClientThread> connectToClientThreadList;
    LinkedBlockingQueue<Object> messages;
    ListenThread listenThread;
    MessageHandlingThread messageHandlingThread;

    public Server() {
        connectToClientThreadList = new ArrayList<>();
        messages = new LinkedBlockingQueue<>();

        // socket initialization
        try {
            server = new ServerSocket(serverPort);
            System.out.println("Server running: " + server);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //  init threads and run them
        listenThread = new ListenThread();
        messageHandlingThread = new MessageHandlingThread();
        listenThread.start();
        messageHandlingThread.start();
    }

    /* main entrance */
    public static void main(String args[]) {
        new Server(); // run the server, the constructor would run all sub-threads.
    }

    /* Methods (utilities) */
    public void sendToAll(Object msg) {
        for (ConnectToClientThread connectToClientThread : connectToClientThreadList) {
            sendToOne(msg, connectToClientThread);
        }
    }

    public void sendToOne(Object msg, ConnectToClientThread connectToClientThread) {
        connectToClientThread.write(msg);
    }

    /* Subclasses */

    /*
     * ListenThread is in charge of establishing connections with new clients,
     * when a new connection is established, a new ConnectToClientThread will be
     * created and
     * added to connectToClientThreadList
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
                    ConnectToClientThread curConnectToClientThread = new ConnectToClientThread(socket);
                    // add it to threadList
                    connectToClientThreadList.add(curConnectToClientThread);
                    curConnectToClientThread.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /*
     * ConnectToClientThread is in charge of reading objects from A SPECIFIC client,
     * via ObjectInputStream and ObjectOutputStream.
     * 
     * Note that ConnectToClientThread only reads objects from inputStream to
     * messages,
     * and provides utility to write to outputStream. The actual manipulation of
     * messages happens in MessageHandlingThread
     */
    class ConnectToClientThread extends Thread {

        Socket socket;
        ObjectInputStream in;
        ObjectOutputStream out;

        public ConnectToClientThread(Socket socket) throws IOException {
            this.socket = socket;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            try {
                out.writeObject("Input your user name:\n");
                while (true) {
                        Object obj = in.readObject(); // read from client
                        messages.put(obj); // and put into messsages, manipulated in messageHandlingThread
                }
            } catch (Exception e) {
                e.printStackTrace();
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

        @Override
        public void run() {
            while (true) {
                try {
                    // blocked until there's new message
                    System.out.println("waiting for new message:");
                    Object message = messages.take();
                    // we can do some handling here
                    System.out.println(message);
                    sendToAll(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
