package com.acme.module.db;

import java.io.Serializable;
import java.util.List;

public interface PersistedModuleInterface<T , Module extends Serializable> {
	public void persist(T entity);
	public void update(T entity);
	public T findById(Module module);
	public void delete(T entity);
	public List<T> findAll();
	public void deleteAll();
}
