package id.fahmikudo.persistence.common.table;

/**
 * Table - Represents database table metadata
 */
public record Table(String name, String alias, Class<?> persistenceClass) {
}

