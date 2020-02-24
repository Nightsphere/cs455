package cs455.overlay.wireformats;


import java.io.IOException;

public interface Event {

    // This marshalls the request into an array of bytes to send over the wire
    byte[] marshalledBytes() throws IOException;

    // This takes the byte array and unmarshalls it so we can load into variables and do what we wish with it
    void unmarshalledBytes() throws IOException;

    int getTYPE();

    String getIP_ADDRESS();

    int getPORT_NUMBER();
}
