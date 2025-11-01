package id.fahmikudo.persistence.common.querybuilder;

/**
 * OrderColumn - Represents ORDER BY clause
 */
public record OrderColumn(String column, Order order) {

    public enum Order {
        ASC("ASC"),
        DESC("DESC");

        public final String code;

        Order(String code) {
            this.code = code;
        }
    }
}

