package cs455.overlay.node;


import cs455.overlay.node.Node;
import cs455.overlay.wireformats.Commands;
import cs455.overlay.wireformats.Event;
import cs455.overlay.transport.TCPServerThread;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


/**
 * The Registry class is the initial server that starts up and begins listening for messaging nodes to
 * connect to it so it can create the messaging node overlay between the nodes. This overlay dictates how
 * many other nodes each node will connect to and send messages to. It will keep track of what nodes have
 * joined and what ports are being used for each one.
 */
public class Registry implements Node {

    // This is the ServerSocket that our server will be using when it is created
    private static ServerSocket serverSocket;

    // This is the HashMap with the string ip address of the messaging node and the port that it is
    // listening for connections on.
    private ArrayList<String> messaging_node_info = new ArrayList<>();

    /**
     * This onEvent function will receive our register or deregister objects from the event that was
     * created in our EventFactory. Depending on what the initial byte is, we can register a messaging node
     * (if it hasn't registered already) or deregister a node.
     *
     * @param event This event object contains either our register or deregister information
     * @throws IOException When we unmarshall our bytes, it could be null, giving us a null pointer exception
     */
    public void onEvent(Event event, Socket socket) throws IOException {

        event.unmarshalledBytes();
        int type = event.getTYPE();

        String ip_address = event.getIP_ADDRESS();
        int port = event.getPORT_NUMBER();

        // If the type is 0, we have a register request
        // If the type is 1, we have a deregister request
        switch(type) {
            case 0: register_request(ip_address, port, socket); break;
            case 1: deregister_request(ip_address, port, socket); break;
            default:
                System.out.println("Oops!");
        }
    }

    private synchronized void deregister_request(String ip_address, int port_number, Socket socket) throws IOException {

        DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
        int index_of_address = -1;
        int index_of_port = -1;
        int ip_address_found = 0;
        int port_found = 0;
        String ip = "";
        String port = "";

        for (String info : messaging_node_info) {
            if (info.equals(ip_address)) {
                ip_address_found = 1;
                index_of_address = messaging_node_info.indexOf(info);
                ip = info;
                continue;
            }
            if (ip_address_found == 1) {
                if (info.equals(Integer.toString(port_number))) {
                   port_found = 1;
                   index_of_port = messaging_node_info.indexOf(info);
                   port = info;
                   break;
                }
            } else {
                break;
            }
        }
        if (index_of_address != index_of_port - 1 && ip.equals(ip_address) &&
                port.equals(Integer.toString(port_number))) {
            if (ip_address_found == 1 && port_found == 1) {
                messaging_node_info.remove(index_of_port);
                messaging_node_info.remove(index_of_port - 1);
            }
        }
        if (index_of_address == index_of_port - 1 && ip.equals(ip_address) &&
                port.equals(Integer.toString(port_number))) {
            messaging_node_info.remove(index_of_port);
            messaging_node_info.remove(index_of_port - 1);
        }
        byte success = 1;
        dout.writeByte(success);
    }

    /**
     * This little helper function looks through the HashMap and determines if the register that's sending
     * this request is already registered with the registry. If it has, complain and exit. If it hasn't, add
     * the node's information to the HashMap.
     *
     * @param ip_address The messaging node's ip address
     * @param port_number The messaging node's port number that it's listening in on
     */
    private synchronized void register_request(String ip_address, int port_number, Socket socket) throws IOException {

        DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
        byte[] success_message;
        byte success = 1;

        messaging_node_info.add(ip_address);
        messaging_node_info.add(Integer.toString(port_number));

        String MESSAGE = "Registration request successful."
                + " The overlay is currently comprised of ["
                + messaging_node_info.size() / 2 + "] messaging node(s)";
        success_message = MESSAGE.getBytes();


        dout.writeByte(success);
        dout.writeInt(MESSAGE.length());
        dout.write(success_message, 0, MESSAGE.length());
    }

    /**
     * Where it all starts. The registry will begin on the specified port given by the user (and if
     * not, then the program will complain) and start listening for messaging nodes to connect to it.
     *
     * @param args The initial arguments given to the process when it is started. Since this is the
     *             registry, it will be the port to start the registry on
     */
    public static void main(String[] args) {

        // If a port number is not specified, let the user know how to run the class
        if (args.length != 1) {
            System.err.println("Usage: Must specify port number to start the Registry on");
            System.exit(0);
        }
        // Put port number into a variable
        int portNumber = Integer.parseInt(args[0]);

        // Try to start the server socket on the given port, and complain if it didn't start correctly
        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Waiting to receive connections...");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Here we're creating an instance of Registry so our thread's know what node is being used
        Registry registry_node = new Registry();

        // Call the connect method to get ready to receive connections from messaging nodes
        TCPServerThread server = new TCPServerThread(registry_node, serverSocket);
        Thread thread0 = new Thread(server);
        thread0.start();

        Commands command = new Commands();
        Thread command_thread = new Thread(command);
        command_thread.start();
    }
}
