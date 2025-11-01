package id.fahmikudo.persistence.common.querybuilder;

import id.fahmikudo.persistence.common.table.Table;

/**
 * JoinClause - Represents JOIN clause for SQL queries
 */
public record JoinClause(JoinType joinType, Table table, String onCondition) {

    public enum JoinType {
        INNER("INNER JOIN"),
        LEFT("LEFT JOIN"),
        RIGHT("RIGHT JOIN"),
        FULL("FULL OUTER JOIN");

        public final String code;

        JoinType(String code) {
            this.code = code;
        }
    }
}

