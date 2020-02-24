package cs455.overlay.wireformats;


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EventFactory {
    
    private static EventFactory ourInstance = new EventFactory();

    public static EventFactory getInstance() {
        return ourInstance;
    }

    private EventFactory() {}

    public Event createEvent(byte[] data_to_marshall) throws IOException {

        int TYPE;

        ByteArrayInputStream baInputStream =
                new ByteArrayInputStream(data_to_marshall);
        DataInputStream din =
                new DataInputStream(new BufferedInputStream(baInputStream));

        TYPE = din.readInt();

        switch (TYPE) {
            case 0: return new Register(data_to_marshall);
            case 1: return new Deregister(data_to_marshall);
            default: System.err.println("Invalid type given!");
            System.exit(0);
        }
        return null;
    }
}
