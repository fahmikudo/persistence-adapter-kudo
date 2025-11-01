package id.fahmikudo.persistence.config;

import id.fahmikudo.persistence.common.table.Table;
import id.fahmikudo.persistence.common.querybuilder.JoinClause;
import id.fahmikudo.persistence.common.querybuilder.OrderColumn;
import id.fahmikudo.persistence.session.JdbcSession;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * MySQLQueryBuilder - Fluent API for building and executing SQL queries with Spring JDBC
 * Replaces Hibernate's SQLQuery with Spring JDBC NamedParameterJdbcTemplate
 */
public class MySQLQueryBuilder<T> {

    private static final String PREFIX_OBJECT_PARAMETER = "objectParameter_";
    private static final String PREFIX_COLLECTION_PARAMETER = "collectionParameter_";

    private final Table table;
    private final JdbcSession session;
    private final RowMapper<T> rowMapper;

    private final Set<String> selectedColumns = new LinkedHashSet<>();

    private final List<String> andClauses = new ArrayList<>();
    private final List<Set<String>> andOrClauses = new ArrayList<>();
    private final List<String> searchClauses = new ArrayList<>();

    private final Map<String, Object> objectParameterById = new HashMap<>();
    private final Map<String, Collection<?>> collectionParameterById = new HashMap<>();

    private final List<JoinClause> joinClauses = new ArrayList<>();

    private final Set<String> groupByColumns = new LinkedHashSet<>();
    private final Set<String> orderByColumns = new LinkedHashSet<>();

    private Integer offset;
    private Integer limit;

    public MySQLQueryBuilder(Table table, JdbcSession session, RowMapper<T> rowMapper) {
        this.table = table;
        this.session = session;
        this.rowMapper = rowMapper;
    }

    // ============ EXECUTION METHODS ============

    public List<T> select() {
        String queryString = getSelectQueryString();
        MapSqlParameterSource parameters = buildParameters();

        return session.getNamedParameterJdbcTemplate()
                .query(queryString, parameters, rowMapper);
    }

    public int count() {
        String queryString = getCountQueryString();
        MapSqlParameterSource parameters = buildParameters();

        Long totalRecord = session.getNamedParameterJdbcTemplate()
                .queryForObject(queryString, parameters, Long.class);

        return totalRecord != null ? totalRecord.intValue() : 0;
    }

    // ============ PARAMETER MANAGEMENT ============

    private MapSqlParameterSource buildParameters() {
        MapSqlParameterSource params = new MapSqlParameterSource();
        objectParameterById.forEach(params::addValue);
        collectionParameterById.forEach(params::addValue);
        return params;
    }

    private String getObjectParameterId(Object parameter) {
        String parameterId = PREFIX_OBJECT_PARAMETER + objectParameterById.size();
        objectParameterById.put(parameterId, parameter);
        return parameterId;
    }

    private String getCollectionParameterId(Collection<?> parameter) {
        String parameterId = PREFIX_COLLECTION_PARAMETER + collectionParameterById.size();
        collectionParameterById.put(parameterId, parameter);
        return parameterId;
    }

    private String getColumnOf(String column) {
        return table.alias() + "." + column;
    }

    private String getColumnOf(String tableAlias, String column) {
        return tableAlias + "." + column;
    }

    // ============ CLAUSE BUILDERS ============

    private String getObjectClause(String column, String operator, Object parameter) {
        if (isNotEmpty(column) && isNotEmpty(operator) && parameter != null) {
            return "(" + getColumnOf(column) + " " + operator + " :" + getObjectParameterId(parameter) + ")";
        }
        return "";
    }

    private String getCollectionClause(String column, String operator, Collection<?> parameters) {
        if (isNotEmpty(column) && isNotEmpty(operator) && !CollectionUtils.isEmpty(parameters)) {
            return "(" + getColumnOf(column) + " " + operator + " (:" + getCollectionParameterId(parameters) + "))";
        }
        return "";
    }

    // ============ COMPARISON OPERATORS ============

    public String equals(String column, Object parameter) {
        return getObjectClause(column, "=", parameter);
    }

    public String notEquals(String column, Object parameter) {
        return getObjectClause(column, "!=", parameter);
    }

    public String moreThan(String column, Object parameter) {
        return getObjectClause(column, ">", parameter);
    }

    public String lessThan(String column, Object parameter) {
        return getObjectClause(column, "<", parameter);
    }

    public String moreThanEquals(String column, Object parameter) {
        return getObjectClause(column, ">=", parameter);
    }

    public String lessThanEquals(String column, Object parameter) {
        return getObjectClause(column, "<=", parameter);
    }

    public String like(String column, String parameter) {
        if (isNotEmpty(column) && parameter != null) {
            return "(" + getColumnOf(column) + " LIKE :" + getObjectParameterId(parameter) + ")";
        }
        return "";
    }

    public String in(String column, Collection<?> parameters) {
        return getCollectionClause(column, "IN", parameters);
    }

    public String notIn(String column, Collection<?> parameters) {
        return getCollectionClause(column, "NOT IN", parameters);
    }

    // ============ AND CONDITIONS ============

    public void and(String clause) {
        if (isNotEmpty(clause)) {
            andClauses.add(clause);
        }
    }

    public void andEquals(String column, Object parameter) {
        String clause = equals(column, parameter);
        if (isNotEmpty(clause)) {
            and(clause);
        }
    }

    public void andNotEquals(String column, Object parameter) {
        String clause = notEquals(column, parameter);
        if (isNotEmpty(clause)) {
            and(clause);
        }
    }

    public void andMoreThan(String column, Object parameter) {
        String clause = moreThan(column, parameter);
        if (isNotEmpty(clause)) {
            and(clause);
        }
    }

    public void andMoreThanEquals(String column, Object parameter) {
        String clause = moreThanEquals(column, parameter);
        if (isNotEmpty(clause)) {
            and(clause);
        }
    }

    public void andLessThan(String column, Object parameter) {
        String clause = lessThan(column, parameter);
        if (isNotEmpty(clause)) {
            and(clause);
        }
    }

    public void andLessThanEquals(String column, Object parameter) {
        String clause = lessThanEquals(column, parameter);
        if (isNotEmpty(clause)) {
            and(clause);
        }
    }

    public void andLike(String column, String parameter) {
        String clause = like(column, parameter);
        if (isNotEmpty(clause)) {
            and(clause);
        }
    }

    public void andIn(String column, Collection<?> parameters) {
        String clause = in(column, parameters);
        if (isNotEmpty(clause)) {
            and(clause);
        }
    }

    public void andNotIn(String column, Collection<?> parameters) {
        String clause = notIn(column, parameters);
        if (isNotEmpty(clause)) {
            and(clause);
        }
    }

    public void andOr(Collection<String> orClauses) {
        if (!CollectionUtils.isEmpty(orClauses)) {
            andOrClauses.add(new HashSet<>(orClauses));
        }
    }

    // ============ SEARCH & GROUPING ============

    public void search(String column, String keyword) {
        if (isNotEmpty(column) && isNotEmpty(keyword)) {
            searchClauses.add(like(column, "%" + keyword + "%"));
        }
    }

    public void groupBy(String column) {
        if (isNotEmpty(column)) {
            groupByColumns.add(getColumnOf(column));
        }
    }

    public void groupBy(Collection<String> columns) {
        if (!CollectionUtils.isEmpty(columns)) {
            columns.forEach(this::groupBy);
        }
    }

    // ============ ORDER BY ============

    public void orderBy(OrderColumn orderColumn) {
        if (orderColumn != null && isNotEmpty(orderColumn.column())) {
            orderByColumns.add(getColumnOf(orderColumn.column()) + " " + orderColumn.order().code);
        }
    }

    public void orderBy(Collection<OrderColumn> orderColumns) {
        if (!CollectionUtils.isEmpty(orderColumns)) {
            orderColumns.forEach(this::orderBy);
        }
    }

    // ============ JOIN OPERATIONS ============

    /**
     * Add an INNER JOIN clause
     * @param joinTable The table to join
     * @param leftColumn Column from the main table
     * @param rightColumn Column from the join table
     */
    public void innerJoin(Table joinTable, String leftColumn, String rightColumn) {
        join(JoinClause.JoinType.INNER, joinTable, leftColumn, rightColumn);
    }

    /**
     * Add a LEFT JOIN clause
     * @param joinTable The table to join
     * @param leftColumn Column from the main table
     * @param rightColumn Column from the join table
     */
    public void leftJoin(Table joinTable, String leftColumn, String rightColumn) {
        join(JoinClause.JoinType.LEFT, joinTable, leftColumn, rightColumn);
    }

    /**
     * Add a RIGHT JOIN clause
     * @param joinTable The table to join
     * @param leftColumn Column from the main table
     * @param rightColumn Column from the join table
     */
    public void rightJoin(Table joinTable, String leftColumn, String rightColumn) {
        join(JoinClause.JoinType.RIGHT, joinTable, leftColumn, rightColumn);
    }

    /**
     * Add a FULL OUTER JOIN clause
     * @param joinTable The table to join
     * @param leftColumn Column from the main table
     * @param rightColumn Column from the join table
     */
    public void fullJoin(Table joinTable, String leftColumn, String rightColumn) {
        join(JoinClause.JoinType.FULL, joinTable, leftColumn, rightColumn);
    }

    /**
     * Add a custom JOIN clause with custom ON condition
     * @param joinType Type of JOIN (INNER, LEFT, RIGHT, FULL)
     * @param joinTable The table to join
     * @param onCondition Custom ON condition (e.g., "t1.id = t2.user_id AND t2.status = 'ACTIVE'")
     */
    public void join(JoinClause.JoinType joinType, Table joinTable, String onCondition) {
        if (joinTable != null && isNotEmpty(onCondition)) {
            joinClauses.add(new JoinClause(joinType, joinTable, onCondition));
        }
    }

    /**
     * Add a JOIN clause with simple column equality condition
     */
    private void join(JoinClause.JoinType joinType, Table joinTable, String leftColumn, String rightColumn) {
        if (joinTable != null && isNotEmpty(leftColumn) && isNotEmpty(rightColumn)) {
            String onCondition = getColumnOf(table.alias(), leftColumn) + " = " +
                                 getColumnOf(joinTable.alias(), rightColumn);
            joinClauses.add(new JoinClause(joinType, joinTable, onCondition));
        }
    }

    /**
     * Select column from joined table
     * @param tableAlias Alias of the table (main or joined)
     * @param column Column name
     */
    public void selectColumnFrom(String tableAlias, String column) {
        if (isNotEmpty(tableAlias) && isNotEmpty(column)) {
            this.selectedColumns.add(tableAlias + "." + column);
        }
    }

    /**
     * Select column from joined table with custom alias
     * @param tableAlias Alias of the table (main or joined)
     * @param column Column name
     * @param columnAlias Custom alias for the column in result set
     */
    public void selectColumnFrom(String tableAlias, String column, String columnAlias) {
        if (isNotEmpty(tableAlias) && isNotEmpty(column) && isNotEmpty(columnAlias)) {
            this.selectedColumns.add(tableAlias + "." + column + " AS " + columnAlias);
        }
    }

    // ============ SELECT COLUMNS ============

    public void selectColumn(String column) {
        if (isNotEmpty(column)) {
            this.selectedColumns.add(column);
        }
    }

    public void selectColumns(Collection<String> columns) {
        if (!CollectionUtils.isEmpty(columns)) {
            this.selectedColumns.addAll(columns);
        }
    }

    // ============ QUERY STRING BUILDERS ============

    public String getCountQueryString() {
        return getQueryString("SELECT COUNT(" + getSelectedColumnsString(true) + ")");
    }

    public String getSelectQueryString() {
        return getQueryString("SELECT " + getSelectedColumnsString(false));
    }

    private String getQueryString(String selectQueryString) {
        StringJoiner joiner = new StringJoiner(" ");
        joiner.add(selectQueryString);
        joiner.add("FROM");
        joiner.add(table.name());
        joiner.add(table.alias());

        String joinString = getJoinString();
        if (isNotEmpty(joinString)) {
            joiner.add(joinString);
        }

        String clausesString = getClausesString();
        if (isNotEmpty(clausesString)) {
            joiner.add(clausesString);
        }

        String groupByString = getGroupByString();
        if (isNotEmpty(groupByString)) {
            joiner.add(groupByString);
        }

        String orderByString = getOrderByString();
        if (isNotEmpty(orderByString)) {
            joiner.add(orderByString);
        }

        // Add pagination at SQL level
        if (limit != null && limit > 0) {
            joiner.add("LIMIT " + limit);
        }
        if (offset != null && offset >= 0) {
            joiner.add("OFFSET " + offset);
        }

        return joiner.toString();
    }

    private String getSelectedColumnsString(boolean isCount) {
        if (CollectionUtils.isEmpty(selectedColumns)) {
            return "*";
        }

        StringJoiner joiner = new StringJoiner(", ");
        selectedColumns.forEach(selectedColumn -> joiner.add(constructSelectQuery(selectedColumn, isCount)));

        return "DISTINCT " + joiner;
    }

    private String constructSelectQuery(String selectedColumn, boolean isCount) {
        String result = getColumnOf(selectedColumn);
        if (!isCount) {
            result += " AS " + getAliasOfColumn(selectedColumn);
        }
        return result;
    }

    private String getAliasOfColumn(String column) {
        // Convert snake_case to camelCase
        String[] parts = column.split("_");
        StringBuilder alias = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            alias.append(parts[i].substring(0, 1).toUpperCase())
                    .append(parts[i].substring(1).toLowerCase());
        }
        return alias.toString();
    }

    private String getClausesString() {
        List<String> clauses = new ArrayList<>();

        if (!CollectionUtils.isEmpty(andClauses)) {
            clauses.add(String.join(" AND ", andClauses));
        }

        if (!CollectionUtils.isEmpty(andOrClauses)) {
            clauses.add(getAndOrClausesString());
        }

        if (!CollectionUtils.isEmpty(searchClauses)) {
            clauses.add("(" + String.join(" OR ", searchClauses) + ")");
        }

        if (!CollectionUtils.isEmpty(clauses)) {
            return "WHERE " + String.join(" AND ", clauses);
        }
        return "";
    }

    private String getAndOrClausesString() {
        List<String> andClauses = new ArrayList<>();
        andOrClauses.forEach(orClauses -> {
            if (!CollectionUtils.isEmpty(orClauses)) {
                andClauses.add("(" + String.join(" OR ", orClauses) + ")");
            }
        });
        return String.join(" AND ", andClauses);
    }

    private String getGroupByString() {
        if (!CollectionUtils.isEmpty(groupByColumns)) {
            return "GROUP BY " + String.join(", ", groupByColumns);
        }
        return "";
    }

    private String getOrderByString() {
        if (!CollectionUtils.isEmpty(orderByColumns)) {
            return "ORDER BY " + String.join(", ", orderByColumns);
        }
        return "";
    }

    private String getJoinString() {
        if (!CollectionUtils.isEmpty(joinClauses)) {
            StringJoiner joiner = new StringJoiner(" ");
            joinClauses.forEach(joinClause -> {
                joiner.add(joinClause.joinType().code);
                joiner.add(joinClause.table().name());
                joiner.add(joinClause.table().alias());
                joiner.add("ON");
                joiner.add(joinClause.onCondition());
            });
            return joiner.toString();
        }
        return "";
    }

    // ============ UTILITY METHODS ============

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    // ============ UTILITY METHODS ============

    private boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
