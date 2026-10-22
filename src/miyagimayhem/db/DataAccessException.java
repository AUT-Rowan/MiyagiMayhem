package miyagimayhem.db;

/**
 * Thrown when something goes wrong talking to the database. It is unchecked
 * so the logic layer is not cluttered with SQLException handling, and the
 * GUI catches it in one place to show a friendly message.
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
