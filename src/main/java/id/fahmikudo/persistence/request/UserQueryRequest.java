package id.fahmikudo.persistence.request;

import id.fahmikudo.persistence.common.BaseRequest;
import id.fahmikudo.persistence.common.table.Table;
import id.fahmikudo.persistence.entity.User;
import id.fahmikudo.persistence.mapper.UserRowMapper;
import id.fahmikudo.persistence.session.JdbcSession;
import lombok.Getter;

import java.util.List;

/**
 * UserRequest - Request object for User queries
 *
 * This class holds query filters and provides a fluent API.
 * The MySQLBuilder is held internally and accessed via getBuilder().
 */
@Getter
public class UserQueryRequest extends BaseRequest<User> {

    private static final Table USER_TABLE = new Table("users", "u", User.class);

    // Filter fields
    private String username;
    private String email;
    private List<Long> ids;
    private List<String> usernames;
    private Boolean active;
    private String role;
    private String searchKeyword;

    /**
     * Constructor - Initialize with session
     */
    public UserQueryRequest(JdbcSession session) {
        super(USER_TABLE, session, new UserRowMapper());
    }

    /**
     * Apply filters to the builder
     * Call this before executing queries
     */
    public UserQueryRequest applyQuery() {
        var builder = getBuilder();

        if (ids != null && !ids.isEmpty()) {
            builder.andIn("id", ids);
        }

        if (username != null) {
            builder.andEquals("username", username);
        }

        if (usernames != null && !usernames.isEmpty()) {
            builder.andIn("username", usernames);
        }

        if (email != null) {
            builder.andEquals("email", email);
        }

        if (active != null) {
            builder.andEquals("active", active);
        }

        if (role != null) {
            builder.andEquals("role", role);
        }

        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            builder.search("username", searchKeyword);
            builder.search("email", searchKeyword);
            builder.search("first_name", searchKeyword);
            builder.search("last_name", searchKeyword);
        }

        return this;
    }

    // ============ FLUENT SETTERS ============

    public UserQueryRequest username(String username) {
        this.username = username;
        return this;
    }

    public UserQueryRequest email(String email) {
        this.email = email;
        return this;
    }

    public UserQueryRequest ids(List<Long> ids) {
        this.ids = ids;
        return this;
    }

    public UserQueryRequest usernames(List<String> usernames) {
        this.usernames = usernames;
        return this;
    }

    public UserQueryRequest active(Boolean active) {
        this.active = active;
        return this;
    }

    public UserQueryRequest role(String role) {
        this.role = role;
        return this;
    }

    public UserQueryRequest searchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
        return this;
    }

    // ============ GETTERS ============

}

