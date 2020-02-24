package cs455.overlay.wireformats;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.transport.TCPSender;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Commands implements Runnable {

    private Scanner scanner = new Scanner(System.in);
    private MessagingNode node;
    private ServerSocket server_socket;
    private Socket registry_socket;
    private int portNum;

    public Commands(MessagingNode node, ServerSocket server_socket,
                    Socket registry_socket, int portNum) {
        this.node = node;
        this.server_socket = server_socket;
        this.registry_socket = registry_socket;
        this.portNum = portNum;
    }

    public Commands() {}

    public void run() {

        String line, command;
        String[] line_split;
        DataInputStream din;
        byte SUCCESS_OR_FAILURE;
        int number_of_connections = 0;

        while (true) {

            line = scanner.nextLine();
            line_split = line.split(" ");
            command = line_split[0];

            if (line_split.length > 1) {
                number_of_connections = Integer.parseInt(line_split[1]);
            }

            switch (command) {
                case "exit-overlay":
                try {
                    TCPSender send_deregister_request = new TCPSender(registry_socket);
                    Deregister deregister_node =
                            new Deregister(server_socket.getInetAddress().getLocalHost().toString(), portNum);
                    send_deregister_request.sendData(deregister_node.marshalledBytes());

                    din = new DataInputStream(registry_socket.getInputStream());

                    SUCCESS_OR_FAILURE = din.readByte();

                    if (SUCCESS_OR_FAILURE == 1) {
                        System.out.println("Messaging Node successfully de-registered!");
                        System.exit(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } break;
                case "setup-overlay":
                    System.out.printf("Overlay now configured with %d connections between nodes. (Theoretically)\n",
                            number_of_connections);
            }
        }
    }
}
