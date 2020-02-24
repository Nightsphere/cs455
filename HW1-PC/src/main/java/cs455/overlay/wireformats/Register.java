package cs455.overlay.wireformats;


import java.io.*;

public class Register implements Event {

    // These two variables are for giving the registry the messaging node's ip address and port number
    // that it's listening on
    private String NODE_IP_ADDRESS;
    private int NODE_PORT_NUMBER;

    // These three variables are what the registry takes and unmarshalls the byte array into
    private int TYPE;
    private String IP_ADDRESS;
    private int PORT_NUMBER;

    // Our byte array for the marshalled bytes
    private byte[] marshalledBytes;

    /**
     * This constructor will be used by the messaging nodes, where we will be sending the type,
     * the node's IP, and the port that the node is listening on.
     *
     * @param node_IP The target messaging node's ip address to send to the registry
     * @param node_PORT The target messaging node's port number that it's listening on
     */
    public Register(String node_IP, int node_PORT) {
        this.NODE_IP_ADDRESS = node_IP;
        this.NODE_PORT_NUMBER = node_PORT;
    }

    /**
     * This constructor will be used by the registry to take the marshalled byte array sent by the
     * messaging node to be unmarshalled so we can appropriately assign the correct variables and
     * add them to the 'list' of nodes that have registered.
     *
     * @param marshalledBytes The byte array of info sent from the messaging node to the registry
     */
    public Register(byte[] marshalledBytes) { this.marshalledBytes = marshalledBytes; }


    /**
     * The marshalledBytes function writes the registry request to a byte array to be sent over the
     * wire to the registry.
     *
     * @return This function returns the registry request we wish to send to the registry
     * @throws IOException The DataOutputStream writes various variables, but we include this
     * IOException in case what it's trying to write is null. In that case, we'd get a NullPointerException
     */
    public synchronized byte[] marshalledBytes() throws IOException {

        int REGISTER_REQUEST = 0;

        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout =
                new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(REGISTER_REQUEST);

        byte[] IP_ADDRESS_BYTES = NODE_IP_ADDRESS.getBytes();
        int elementLength = IP_ADDRESS_BYTES.length;

        dout.writeInt(elementLength);
        dout.write(IP_ADDRESS_BYTES);
        dout.writeInt(NODE_PORT_NUMBER);
        dout.flush();

        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }


    /**
     * The unmarshalledBytes function takes the marshalled bytes array sent from the messaging node, reads
     * it and assigns the variables so we can use them in the registry.
     * @throws IOException Like the previous function, the DataInputStream has a chance to read in
     * something that is null, throwing a NullPointerException
     */
    public synchronized void unmarshalledBytes() throws IOException {

        ByteArrayInputStream baInputStream =
                new ByteArrayInputStream(marshalledBytes);
        DataInputStream din =
                new DataInputStream(new BufferedInputStream(baInputStream));

        TYPE = din.readInt();

        int string_length = din.readInt();
        byte[] string_bytes = new byte[string_length];
        din.readFully(string_bytes);

        IP_ADDRESS = new String(string_bytes);

        PORT_NUMBER = din.readInt();

        baInputStream.close();
        din.close();
    }

    // These are the getters for the variables
    // 1. TYPE - The type of message being sent (0, since this is a registry request
    // 2. IP_ADDRESS - The IP address of the messaging node
    // 3. PORT_NUMBER - The port number the messaging node is listening in on for more connections

    public int getTYPE() { return this.TYPE; }

    public String getIP_ADDRESS() { return this.IP_ADDRESS; }

    public int getPORT_NUMBER() { return this.PORT_NUMBER; }

}
