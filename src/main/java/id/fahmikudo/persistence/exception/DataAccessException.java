package id.fahmikudo.persistence.exception;

/**
 * Exception thrown when a database access error occurs
 */
public class DataAccessException extends PersistenceException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

