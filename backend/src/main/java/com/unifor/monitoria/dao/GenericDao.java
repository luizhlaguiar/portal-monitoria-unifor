package com.unifor.monitoria.dao;

import java.util.List;
import java.util.Optional;

/**
 * Contrato CRUD comum a todos os DAOs. Cada DAO de entidade estende esta interface
 * e acrescenta apenas as consultas especificas dele.
 */
public interface GenericDao<T, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    boolean existsById(ID id);

    long count();

    void delete(T entity);

    void deleteById(ID id);
}