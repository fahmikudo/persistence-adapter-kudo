package id.fahmikudo.persistence.exception;

import lombok.Getter;

/**
 * Exception thrown when an entity is not found in the database
 */
@Getter
public class EntityNotFoundException extends PersistenceException {

    private final Class<?> entityClass;
    private final Object identifier;

    public EntityNotFoundException(Class<?> entityClass, Object identifier) {
        super(String.format("Entity %s not found with identifier: %s",
            entityClass.getSimpleName(), identifier));
        this.entityClass = entityClass;
        this.identifier = identifier;
    }

    public EntityNotFoundException(String message) {
        super(message);
        this.entityClass = null;
        this.identifier = null;
    }

}

