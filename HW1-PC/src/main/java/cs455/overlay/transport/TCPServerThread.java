package cs455.overlay.transport;


import cs455.overlay.node.Node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The TCPServerThread class is a thread that starts a server using the ServerSocket passed to it either
 * from the Registry node or a messaging node. The thread is needed for accepting connections with other
 * nodes, and for starting a new TCPReceiverThread for listening to new connections as well.
 */
public class TCPServerThread implements Runnable {

    private ServerSocket serverSocket;
    private Node node;

    /**
     * This constructor takes the current node and the ServerSocket of said node.
     *
     * @param node We are assigning our node class variable as the current node instance that has created
     *             this thread
     * @param serverSocket We are assigning the serverSocket class variable as the ServerSocket that was
     *                     passed in from the registry or messaging node
     */
    public TCPServerThread(Node node, ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.node = node;
    }

    // This is what the thread will execute
    public void run() {

        Socket clientSocket;

        try {

            while (true) {
                clientSocket = serverSocket.accept();
                System.out.println("Node connected!");


                // Practice with threads. always have the thread listen. or something
                TCPReceiverThread always_listen = new TCPReceiverThread(node, clientSocket);
                Thread thread0 = new Thread(always_listen);
                thread0.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
