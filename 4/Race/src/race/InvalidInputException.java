package race;

/**
 * Signals that the race description is malformed or contains invalid data.
 *
 * <p>The message always tells what is wrong and, where it makes sense, in
 * which line of the input, so it can be shown to the user as is.</p>
 */
public class InvalidInputException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception about the input as a whole.
     *
     * @param message the description of the problem
     */
    public InvalidInputException(String message) {
        super(message);
    }

    /**
     * Creates an exception about the input as a whole, keeping the cause.
     *
     * @param message the description of the problem
     * @param cause the exception that revealed the problem
     */
    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an exception about one line of the input. The line number
     * becomes part of the message.
     *
     * @param lineNumber the number of the invalid line, starting from 1
     * @param message the description of the problem
     * @param cause the exception that revealed the problem
     */
    public InvalidInputException(int lineNumber, String message, Throwable cause) {
        super("Line " + lineNumber + ": " + message, cause);
    }

}
