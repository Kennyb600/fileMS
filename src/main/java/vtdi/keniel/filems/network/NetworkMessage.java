package vtdi.keniel.filems.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.Serializable;

/**
 * A wrapper class to encapsulate commands and data sent over TCP/IP streams.
 */
public class NetworkMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(NetworkMessage.class);

    // Define the specific operations the Client can ask the Server to perform
    public enum Command {
        INSERT_CASE,
        UPDATE_CASE,
        DELETE_CASE,
        FIND_CASE_BY_ID,
        GET_ALL_CASES,
        RESPONSE_OK,     // Sent by server upon success
        RESPONSE_ERROR   // Sent by server upon failure
    }

    private Command command;
    private Object payload; // Object allows us to send a CourtCase, a String, or a List

    public NetworkMessage(Command command, Object payload) {
        try {
            if (command == null) {
                throw new IllegalArgumentException("NetworkMessage Command cannot be null.");
            }
            this.command = command;
            this.payload = payload;
            logger.info("NetworkMessage constructed with command: " + command);
        } catch (Exception e) {
            logger.fatal("Failed to instantiate NetworkMessage: " + e.getMessage(), e);
            this.command = Command.RESPONSE_ERROR;
            this.payload = "Internal Message Construction Error";
        }
    }

    // Getters
    public Command getCommand() {
        return command;
    }

    public Object getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "NetworkMessage{" +
                "command=" + command +
                ", payload=" + (payload != null ? payload.getClass().getSimpleName() : "null") +
                '}';
    }
}