package id.fahmikudo.persistence.common.repository;

import id.fahmikudo.persistence.common.BaseRequest;

import java.util.List;
import java.util.Optional;

/**
 * CrudRepository - Base repository interface for CRUD operations
 *
 * Simplified pattern where Request holds the MySQLBuilder.
 * Repositories just call request.getBuilder() to execute queries.
 *
 * @param <T> Entity type
 * @param <R> Request type that extends BaseRequest
 */
public interface CrudRepository<T, R extends BaseRequest<T>> {

    // Find operations
    T save(T entity) throws Exception;

    Optional<List<T>> find(R request) throws Exception;

    int count(R request) throws Exception;
}

