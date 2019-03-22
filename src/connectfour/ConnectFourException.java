package connectfour;

/**
 * A custom exception thrown from any of the Connect Four classes if something
 * goes wrong.
 *
 * @author James Heloitis @ RIT CS
 * @author Sean Strout @ RIT CS
 */
public class ConnectFourException extends Exception {
    /**
     * Convenience constructor to create a new {@link ConnectFourException}
     * with an error message.
     *
     * @param message The error message associated with the exception.
     */
    public ConnectFourException(String message) {
        super(message);
    }

    /**
     * Convenience constructor to create a new {@link ConnectFourException}
     * as a result of some other exception.
     *
     * @param cause The root cause of the exception.
     */
    public ConnectFourException(Throwable cause) {
        super(cause);
    }

    /**
     * * Convenience constructor to create a new {@link ConnectFourException}
     * as a result of some other exception.
     *
     * @param message The message associated with the exception.
     * @param cause The root cause of the exception.
     */
    public ConnectFourException(String message, Throwable cause) {
        super(message, cause);
    }
}
