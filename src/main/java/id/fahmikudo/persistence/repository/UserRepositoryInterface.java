package id.fahmikudo.persistence.repository;

import id.fahmikudo.persistence.common.repository.CrudRepository;
import id.fahmikudo.persistence.entity.User;
import id.fahmikudo.persistence.request.UserRequest;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryInterface extends CrudRepository<User, UserRequest> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findActiveUsers();
    List<User> findByRole(String role);
}

