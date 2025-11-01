package id.fahmikudo.persistence.common;

import id.fahmikudo.persistence.common.table.Table;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

public class EntitySaver {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final Map<Class<?>, EntityMetadata> metadataCache = new HashMap<>();

    public EntitySaver(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Save or update an entity based on whether ID is null
     *
     * @param entity The entity to save
     * @param table The table metadata
     * @param <T> Entity type
     * @return The saved entity with ID populated
     */
    public <T> T save(T entity, Table table) {
        EntityMetadata metadata = getOrCreateMetadata(entity.getClass(), table);

        try {
            Object idValue = metadata.idField.get(entity);

            if (idValue == null) {
                return insert(entity, metadata);
            } else {
                return update(entity, metadata);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save entity: " + e.getMessage(), e);
        }
    }

    /**
     * Save or update multiple entities in a batch
     *
     * @param entities List of entities to save
     * @param table The table metadata
     * @param <T> Entity type
     * @return List of saved entities with IDs populated
     */
    public <T> List<T> saveAll(List<T> entities, Table table) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        EntityMetadata metadata = getOrCreateMetadata(entities.getFirst().getClass(), table);
        List<T> savedEntities = new ArrayList<>();

        try {
            for (T entity : entities) {
                Object idValue = metadata.idField.get(entity);

                if (idValue == null) {
                    savedEntities.add(insert(entity, metadata));
                } else {
                    savedEntities.add(update(entity, metadata));
                }
            }

            return savedEntities;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save entities: " + e.getMessage(), e);
        }
    }


    /**
     * Insert a new entity
     */
    private <T> T insert(T entity, EntityMetadata metadata) throws Exception {
        MapSqlParameterSource params = new MapSqlParameterSource();
        List<String> columns = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();

        // Auto-populate audit fields if they exist and are null
        setAuditFieldsForInsert(entity, metadata);

        // Build INSERT statement
        for (FieldInfo fieldInfo : metadata.fields) {
            if (fieldInfo.isId) continue; // Skip ID field (auto-generated)

            Object value = fieldInfo.field.get(entity);
            columns.add(fieldInfo.columnName);
            placeholders.add(":" + fieldInfo.paramName);
            params.addValue(fieldInfo.paramName, value);
        }

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                metadata.tableName,
                String.join(", ", columns),
                String.join(", ", placeholders));

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder);

        // Set generated ID back to entity
        Number key = keyHolder.getKey();
        if (key != null) {
            metadata.idField.set(entity, key.longValue());
        }

        return entity;
    }

    /**
     * Update an existing entity
     */
    private <T> T update(T entity, EntityMetadata metadata) throws Exception {
        MapSqlParameterSource params = new MapSqlParameterSource();
        List<String> setClauses = new ArrayList<>();

        // Auto-populate update audit fields if they exist
        setAuditFieldsForUpdate(entity, metadata);

        // Build UPDATE statement
        for (FieldInfo fieldInfo : metadata.fields) {
            if (fieldInfo.isId) {
                params.addValue(fieldInfo.paramName, fieldInfo.field.get(entity));
                continue;
            }

            Object value = fieldInfo.field.get(entity);
            setClauses.add(fieldInfo.columnName + " = :" + fieldInfo.paramName);
            params.addValue(fieldInfo.paramName, value);
        }

        String sql = String.format("UPDATE %s SET %s WHERE %s = :%s",
                metadata.tableName,
                String.join(", ", setClauses),
                metadata.idColumnName,
                metadata.idParamName);

        jdbcTemplate.update(sql, params);
        return entity;
    }

    /**
     * Auto-populate audit fields for insert operations
     */
    private <T> void setAuditFieldsForInsert(T entity, EntityMetadata metadata) throws Exception {
        for (FieldInfo fieldInfo : metadata.fields) {
            Object value = fieldInfo.field.get(entity);

            // Set createdAt if field exists and is null
            if ("createdAt".equals(fieldInfo.field.getName()) && value == null) {
                if (fieldInfo.field.getType() == LocalDateTime.class) {
                    fieldInfo.field.set(entity, LocalDateTime.now());
                }
            }
        }
    }

    /**
     * Auto-populate audit fields for update operations
     */
    private <T> void setAuditFieldsForUpdate(T entity, EntityMetadata metadata) throws Exception {
        for (FieldInfo fieldInfo : metadata.fields) {
            // Set updatedAt if field exists
            if ("updatedAt".equals(fieldInfo.field.getName())) {
                if (fieldInfo.field.getType() == LocalDateTime.class) {
                    fieldInfo.field.set(entity, LocalDateTime.now());
                }
            }
        }
    }

    /**
     * Get or create metadata for an entity class (cached for performance)
     */
    private EntityMetadata getOrCreateMetadata(Class<?> entityClass, Table table) {
        return metadataCache.computeIfAbsent(entityClass, clazz -> {
            EntityMetadata metadata = new EntityMetadata();
            metadata.tableName = table.name();

            Field idField = null;
            List<FieldInfo> fields = new ArrayList<>();

            for (Field field : getAllFields(entityClass)) {
                field.setAccessible(true);

                FieldInfo fieldInfo = new FieldInfo();
                fieldInfo.field = field;
                fieldInfo.columnName = toSnakeCase(field.getName());
                fieldInfo.paramName = field.getName();

                // Detect ID field by name convention (id field)
                if ("id".equals(field.getName())) {
                    fieldInfo.isId = true;
                    idField = field;
                    metadata.idColumnName = fieldInfo.columnName;
                    metadata.idParamName = fieldInfo.paramName;
                }

                fields.add(fieldInfo);
            }

            if (idField == null) {
                throw new IllegalArgumentException("No 'id' field found in entity: " + entityClass.getName());
            }

            metadata.idField = idField;
            metadata.fields = fields;

            return metadata;
        });
    }

    /**
     * Get all fields including inherited ones
     */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * Convert camelCase to snake_case
     */
    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    // ============ METADATA CLASSES ============

    private static class EntityMetadata {
        String tableName;
        String idColumnName;
        String idParamName;
        Field idField;
        List<FieldInfo> fields;
    }

    private static class FieldInfo {
        Field field;
        String columnName;
        String paramName;
        boolean isId;
    }
}

