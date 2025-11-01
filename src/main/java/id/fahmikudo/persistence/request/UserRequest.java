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
public class UserRequest extends BaseRequest<User> {

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
    public UserRequest(JdbcSession session) {
        super(USER_TABLE, session, new UserRowMapper());
    }

    /**
     * Apply filters to the builder
     * Call this before executing queries
     */
    public UserRequest applyFilters() {
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

    public UserRequest username(String username) {
        this.username = username;
        return this;
    }

    public UserRequest email(String email) {
        this.email = email;
        return this;
    }

    public UserRequest ids(List<Long> ids) {
        this.ids = ids;
        return this;
    }

    public UserRequest usernames(List<String> usernames) {
        this.usernames = usernames;
        return this;
    }

    public UserRequest active(Boolean active) {
        this.active = active;
        return this;
    }

    public UserRequest role(String role) {
        this.role = role;
        return this;
    }

    public UserRequest searchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
        return this;
    }

    // ============ GETTERS ============

}

