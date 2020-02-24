package cs455.overlay.node;


import cs455.overlay.wireformats.Commands;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.Register;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class MessagingNode implements Node {

    // The socket to the registry
    private static Socket registry_socket;
    // The ServerSocket that we're starting this messaging node's server on
    private static ServerSocket server_socket;
    // The port number that this messaging node is listening on for connections
    private static int portNum;

    public void onEvent(Event event, Socket socket) throws IOException {

        int type = event.getTYPE();
        System.out.println(type);
    }

    private static void hey_listen(MessagingNode messaging_node) {

        boolean possible_port = true;
        portNum = 21234;

        while (possible_port) {
            try {
                server_socket = new ServerSocket(portNum);

                TCPServerThread server = new TCPServerThread(messaging_node, server_socket);
                Thread thread0 = new Thread(server);
                thread0.start();

                possible_port = false;
            } catch (BindException e) {
                portNum++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The connect function takes in the registry's address and port number, and this messaging node
     * connects to it so we can register and go from there.
     *
     * @param address The address of the registry that we're connecting to
     * @param port The port number that the registry is listening on
     */
    private static void connect_to_registry(String address, int port) {

        try {
            registry_socket = new Socket(address, port);
            System.out.println("Connected to " + registry_socket.getInetAddress().getHostName() + "!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void connect_to_node() {

    }

    private static void register_node() {

        DataInputStream din;
        byte SUCCESS_OR_FAILURE;
        int MESSAGE_LENGTH;

        try {
            TCPSender send_register_request = new TCPSender(registry_socket);
            Register register_node =
                    new Register(server_socket.getInetAddress().getLocalHost().toString(), portNum);
            send_register_request.sendData(register_node.marshalledBytes());

            din = new DataInputStream(registry_socket.getInputStream());

            SUCCESS_OR_FAILURE = din.readByte();
            MESSAGE_LENGTH = din.readInt();
            byte[] MESSAGE = new byte[MESSAGE_LENGTH];
            din.readFully(MESSAGE, 0, MESSAGE_LENGTH);

            if (SUCCESS_OR_FAILURE == 1) {
                System.out.println(new String(MESSAGE));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Where it all begins for the messaging node. It will connect to the registry, and then other
     * messaging nodes so they can send integers back and forth to each other.
     *
     * @param args The commands given from the user upon beginning the messaging node. In this case,
     *             the registry's address (name) and the port number it's listening on.
     */
    public static void main(String[] args) {

        // If we aren't given 2 arguments in the form of the registry address and the port the registry
        // is listening to, complain and exit.
        if (args.length != 2) {
            System.err.println("Usage: 2 arguments needed. Registry IP and port number to connect to");
            System.exit(0);
        }

        String server_name = args[0];
        int port = Integer.parseInt(args[1]);

        // This messaging node's instance so we know which node is doing stuff
        MessagingNode messaging_node = new MessagingNode();

        // Have the messaging node connect to the registry
        connect_to_registry(server_name, port);

        // Have the messaging node listen for other messaging nodes
        hey_listen(messaging_node);

        register_node();

        Commands command = new Commands(messaging_node, server_socket, registry_socket, portNum);
        Thread command_thread = new Thread(command);
        command_thread.start();

        ////////////WE STOP HERE\\\\\\\\\\\
        while (true) {

        }
    }
}

