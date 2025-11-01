package id.fahmikudo.persistence.session;

import id.fahmikudo.persistence.common.EntitySaver;
import id.fahmikudo.persistence.common.table.Table;
import lombok.Getter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;

/**
 * JdbcSession - Replaces Hibernate Session with Spring JDBC
 */
public class JdbcSession {
    @Getter
    private final JdbcTemplate jdbcTemplate;
    @Getter
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final PlatformTransactionManager transactionManager;
    private final EntitySaver entitySaver;
    private TransactionStatus transactionStatus;

    public JdbcSession(DataSource dataSource, PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.transactionManager = transactionManager;
        this.entitySaver = new EntitySaver(namedParameterJdbcTemplate);
    }

    /**
     * Save or update an entity (similar to Hibernate's session.save())
     *
     * @param entity The entity to save
     * @param table The table metadata
     * @param <T> Entity type
     * @return The saved entity with ID populated
     */
    public <T> T save(T entity, Table table) {
        return entitySaver.save(entity, table);
    }

    public void beginTransaction() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        this.transactionStatus = transactionManager.getTransaction(def);
    }

    public void commit() {
        if (transactionStatus != null && !transactionStatus.isCompleted()) {
            transactionManager.commit(transactionStatus);
        }
    }

    public void rollback() {
        if (transactionStatus != null && !transactionStatus.isCompleted()) {
            transactionManager.rollback(transactionStatus);
        }
    }

    public void close() {
        // Spring JDBC handles connection management automatically
        if (transactionStatus != null && !transactionStatus.isCompleted()) {
            rollback();
        }
    }
}

