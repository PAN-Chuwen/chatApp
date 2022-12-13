import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {
    Socket socket;
    String serverAddr = "localhost";
    int serverPort = 8888;
    LinkedBlockingQueue<Object> messages;
    ConnectToServerThread connectToServerThread;
    MessageHandlingThread messageHandlingThread;
    UserInputThread userInputThread;

    public Client() {
        messages = new LinkedBlockingQueue<>();
        try {
            socket = new Socket(serverAddr, serverPort);
            System.out.println("Client: " + socket);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        connectToServerThread = new ConnectToServerThread();
        messageHandlingThread = new MessageHandlingThread();
        userInputThread = new UserInputThread();

        connectToServerThread.start();
        messageHandlingThread.start();
        userInputThread.start();
    }

    /* main entrance */
    public static void main(String[] args) {
        new Client(); // create new client
    }

    /* methods (utilities) */
    void sendToServer(Object obj) {
        connectToServerThread.write(obj);
    }

    /* Subclasses */

    class ConnectToServerThread extends Thread {
        ObjectInputStream in;
        ObjectOutputStream out;

        // no need to specify arg for socket
        public ConnectToServerThread() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            while (true) {
                try {
                    Object obj = in.readObject();
                    messages.put(obj);
                } catch (EOFException e) { // client will throw EOF if we close server
                    System.out.println("Lost connection with the server."); 
                    break; // exit
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void write(Object msg) {
            try {
                out.writeObject(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class MessageHandlingThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    // blocked until there's new message
                    Object msg = messages.take();
                    // we can do some handling here
                    System.out.println(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class UserInputThread extends Thread {
        @Override
        public void run() {
            Scanner sc = new Scanner(System.in); // never closed
            // read input(Object) from user and send it to server
            while (true) {
                System.out.println("input your new message:");
                String s = sc.nextLine(); // blocked until next user input
                sendToServer(s);
            }
        }
    }
}
