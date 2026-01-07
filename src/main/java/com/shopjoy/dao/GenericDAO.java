package com.shopjoy.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Generic Data Access Object interface for CRUD operations.
 *
 * @param <T>  Entity type
 * @param <ID> Identifier type (e.g., Integer, Long)
 */
public interface GenericDAO<T, ID> {

    /**
     * Find an entity by its identifier.
     *
     * @param id the identifier of the entity
     * @return the found entity or null if not found
     * @throws SQLException if a database access error occurs
     */
    T findById(ID id) throws SQLException;

    /**
     * Retrieve all entities of this type.
     *
     * @return list of all entities (may be empty)
     * @throws SQLException if a database access error occurs
     */
    List<T> findAll() throws SQLException;

    /**
     * Persist a new entity in the database. Implementations should set
     * any generated identifier on the passed entity and return it.
     *
     * @param entity entity to save
     * @return the saved entity (with generated ID populated when applicable)
     * @throws SQLException if a database access error occurs
     */
    T save(T entity) throws SQLException;

    /**
     * Update an existing entity in the database.
     *
     * @param entity entity with updated values
     * @return the updated entity
     * @throws SQLException if a database access error occurs
     */
    T update(T entity) throws SQLException;

    /**
     * Delete an entity by its identifier.
     *
     * @param id identifier of entity to delete
     * @return true if deletion succeeded (row existed), false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean delete(ID id) throws SQLException;

    /**
     * Count total entities of this type in the database.
     *
     * @return the total count
     * @throws SQLException if a database access error occurs
     */
    long count() throws SQLException;
}
