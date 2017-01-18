package com.acme.module.db;

import java.io.Serializable;
import java.util.List;

public interface PersistedModuleInterface<T , Module extends Serializable> {
    void persist(T entity);

    void update(T entity);

    T findById(Module module);

    void delete(T entity);

    List<T> findAll();

    void deleteAll();
}
