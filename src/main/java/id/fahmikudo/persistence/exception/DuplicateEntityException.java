package id.fahmikudo.persistence.exception;

/**
 * Exception thrown when attempting to create a duplicate entity
 */
public class DuplicateEntityException extends PersistenceException {

    private final String fieldName;
    private final Object fieldValue;

    public DuplicateEntityException(String fieldName, Object fieldValue) {
        super(String.format("Duplicate entity found with %s: %s", fieldName, fieldValue));
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public DuplicateEntityException(String message) {
        super(message);
        this.fieldName = null;
        this.fieldValue = null;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}

