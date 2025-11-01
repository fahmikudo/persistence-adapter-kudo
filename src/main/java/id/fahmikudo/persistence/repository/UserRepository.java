package id.fahmikudo.persistence.repository;

import id.fahmikudo.persistence.common.querybuilder.OrderColumn;
import id.fahmikudo.persistence.common.table.Table;
import id.fahmikudo.persistence.entity.User;
import id.fahmikudo.persistence.request.UserRequest;
import id.fahmikudo.persistence.session.JdbcSession;
import lombok.Getter;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Getter
@Repository
public class UserRepository implements UserRepositoryInterface {

    private static final Table USER_TABLE = new Table("users", "u", User.class);

    private final JdbcSession session;

    public UserRepository(JdbcSession session) {
        this.session = session;
    }

    // ============ CRUD OPERATIONS (from CrudRepository) ============

    @Override
    public User save(User entity) throws Exception {
        return session.save(entity, USER_TABLE);
    }

    @Override
    public Optional<List<User>> find(UserRequest request) throws Exception {
        request.applyFilters(); // Apply filters to builder
        return Optional.ofNullable(request.getBuilder().select());
    }

    @Override
    public int count(UserRequest request) throws Exception {
        request.applyFilters(); // Apply filters to builder
        return request.getBuilder().count();
    }

    // ============ CUSTOM METHODS ============

    @Override
    public Optional<User> findByUsername(String username) {
        var request = new UserRequest(session)
                .username(username)
                .applyFilters();

        List<User> results = request.getBuilder().select();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        var request = new UserRequest(session)
                .email(email)
                .applyFilters();

        List<User> results = request.getBuilder().select();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    @Override
    public List<User> findActiveUsers() {
        var request = new UserRequest(session)
                .active(true)
                .applyFilters();
        request.setOrderColumns(List.of(
                new OrderColumn("created_at", OrderColumn.Order.DESC)
        ));

        return request.getBuilder().select();
    }

    @Override
    public List<User> findByRole(String role) {
        var request = new UserRequest(session)
                .role(role)
                .active(true)
                .applyFilters();
        request.setOrderColumns(List.of(
                new OrderColumn("username", OrderColumn.Order.ASC)
        ));

        return request.getBuilder().select();
    }
}

