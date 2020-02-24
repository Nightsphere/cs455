package cs455.overlay.wireformats;

import java.io.*;

public class Deregister implements Event {

    private int TYPE;
    private String NODE_IP_ADDRESS;
    private int NODE_PORT_NUMBER;

    // Our byte array for the marshalled bytes
    private byte[] marshalledBytes;

    public Deregister(String node_IP, int node_PORT) {
        this.NODE_IP_ADDRESS = node_IP;
        this.NODE_PORT_NUMBER = node_PORT;
    }

    public Deregister(byte[] marshalledBytes) { this.marshalledBytes = marshalledBytes; }

    public byte[] marshalledBytes() throws IOException {

        int REGISTER_REQUEST = 1;

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

    public void unmarshalledBytes() throws IOException {

        ByteArrayInputStream baInputStream =
                new ByteArrayInputStream(marshalledBytes);
        DataInputStream din =
                new DataInputStream(new BufferedInputStream(baInputStream));

        TYPE = din.readInt();

        int string_length = din.readInt();
        byte[] string_bytes = new byte[string_length];
        din.readFully(string_bytes);

        NODE_IP_ADDRESS = new String(string_bytes);

        NODE_PORT_NUMBER = din.readInt();

        baInputStream.close();
        din.close();
    }

    public int getTYPE() {
        return this.TYPE;
    }

    public String getIP_ADDRESS() {
        return this.NODE_IP_ADDRESS;
    }

    public int getPORT_NUMBER() {
        return this.NODE_PORT_NUMBER;
    }

}
