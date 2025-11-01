package id.fahmikudo.persistence.common;

import id.fahmikudo.persistence.common.querybuilder.OrderColumn;
import id.fahmikudo.persistence.common.table.Table;
import id.fahmikudo.persistence.config.MySQLQueryBuilder;
import id.fahmikudo.persistence.session.JdbcSession;
import lombok.Getter;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collection;

/**
 * BaseRequest - Base class for building dynamic queries
 *
 * This class holds a MySQLBuilder instance and provides fluent setters.
 * Repositories simply call request.getBuilder() to execute queries.
 *
 * @param <T> Entity type
 */
@Getter
public abstract class BaseRequest<T> {

    /**
     * -- GETTER --
     *  Get the query builder
     */
    private final MySQLQueryBuilder<T> builder;
    private Integer offset;
    private Integer limit;
    private Collection<String> selectColumns;
    private Collection<String> groupColumns;
    private Collection<OrderColumn> orderColumns;

    /**
     * Constructor - Initialize with table and session
     */
    protected BaseRequest(Table table, JdbcSession session, RowMapper<T> rowMapper) {
        this.builder = new MySQLQueryBuilder<>(table, session, rowMapper);
    }

    // ============ FLUENT SETTERS ============

    public BaseRequest<T> setOffset(Integer offset) {
        this.offset = offset;
        if (offset != null) {
            builder.setOffset(offset);
        }
        return this;
    }

    public BaseRequest<T> setLimit(Integer limit) {
        this.limit = limit;
        if (limit != null) {
            builder.setLimit(limit);
        }
        return this;
    }

    public BaseRequest<T> setSelectColumns(Collection<String> selectColumns) {
        this.selectColumns = selectColumns;
        if (selectColumns != null && !selectColumns.isEmpty()) {
            builder.selectColumns(selectColumns);
        }
        return this;
    }

    public BaseRequest<T> setGroupColumns(Collection<String> groupColumns) {
        this.groupColumns = groupColumns;
        if (groupColumns != null && !groupColumns.isEmpty()) {
            builder.groupBy(groupColumns);
        }
        return this;
    }

    public void setOrderColumns(Collection<OrderColumn> orderColumns) {
        this.orderColumns = orderColumns;
        if (orderColumns != null && !orderColumns.isEmpty()) {
            builder.orderBy(orderColumns);
        }
    }

}

