package cs455.overlay.transport;


import cs455.overlay.node.Node;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * This is our receiver thread that will receive the information sent to it. It needs to be running in a thread
 * since we'll be potentially sending information from multiple sources at the same time, and we need to be listening
 * to incoming message essentially at all times.
 */
public class TCPReceiverThread implements Runnable {

    // Our current node instance that is running on this thread
    private Node node;
    // The socket that the node is communicating with
    private Socket socket;
    // The stream that will read from the socket's input stream
    private DataInputStream din;

    /**
     * The TCPReceiver's constructor that assigns:
     * - The node class variable with the current node instance.
     * - The socket class variable with the current socket our node is communicating with.
     * - The din class variable with the input stream of the socket, so we can receive messages from it.
     *
     * @param node This is our current node object, which is either the registry, or a messaging node
     * @param socket The socket that the current node is communicating with
     * @throws IOException Our socket's input stream could potentially be null, so we need to have this
     */
    public TCPReceiverThread(Node node, Socket socket) throws IOException {
        this.node = node;
        this.socket = socket;
        din = new DataInputStream(socket.getInputStream());
    }

    // This is what the thread will execute
    public void run() {

        int dataLength;

            // Pro Tip: This while loop actually makes it so that we are ALWAYS listening for connections
            while (true) {
                try {
                    // Read the first int, which is the length of the information being received
                    dataLength = din.readInt();
                    byte[] data = new byte[dataLength];
                    // data now has the data being received, starting with the integer 'type' of message it is
                    din.readFully(data, 0, dataLength);

                    // Create the singleton instance of our EventFactory
                    EventFactory factory = EventFactory.getInstance();
                    Event event = factory.createEvent(data);

                    // Now we will tell the instance of this node to call onEvent on the event we created in
                    // our factory. It will send this to the correct node, and can obtain the info in event
                    this.node.onEvent(event, socket);

                } catch (IOException e) {
                    break;
                }
            }
    }
}
