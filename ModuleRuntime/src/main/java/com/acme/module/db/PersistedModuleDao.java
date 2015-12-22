package com.acme.module.db;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import com.acme.module.db.model.PersistedModule;
import com.acme.module.model.Module;

/**
 * This class offers necessary methods to handle Database persistence for Modules.
 *
 */
public class PersistedModuleDao implements
		PersistedModuleInterface<PersistedModule, Module> {

	private SessionFactory sessionFactory; 
	
	private Session currentSession;
	private Transaction currentTransaction;

	/**
	 * Persists a new Persisted Module<br>
	 * May throw some exception in case the unique PK does already exist<br>
	 */
	public void persist(PersistedModule entity) {
		getCurrentSession().save(entity);
	}

	/**
	 * Updates a PersistedModule.<br>
	 * Note : Only the content(byte array) may be updated.
	 * 
	 */
	public void update(PersistedModule entity) {
		getCurrentSession().update(entity);
	}

	/**
	 * Gets a specific Persisted Module identified by Module description<br>
	 * @return the persisted module if exists or null otherwise
	 */
	@SuppressWarnings("rawtypes")
	public PersistedModule findById(Module module) {
		PersistedModule pModule = null; 
		Iterator persistedModuleIterator  = getCurrentSession().createQuery("from PersistedModule").iterate();
		while(persistedModuleIterator.hasNext()){
			PersistedModule pm = (PersistedModule)persistedModuleIterator.next();
			if(	pm.getPersistedModuleId().getArtefactId().equals(module.getArtefactId())
				&&pm.getPersistedModuleId().getGroupId().equals(module.getGroupId())
				&&pm.getPersistedModuleId().getVersionId().equals(module.getVersionId())){
					pModule = pm;
				}
		}
		return pModule;
	}

	/**
	 * Deletes a persisted Module
	 */
	public void delete(PersistedModule entity) {
		getCurrentSession().delete(entity);
	}

	/**
	 * Gets all persisted Modules
	 */
	@SuppressWarnings("unchecked")
	public List<PersistedModule> findAll() {
		List<PersistedModule> pModules = (List<PersistedModule>) getCurrentSession()
				.createQuery("from PersistedModule").list();
		return pModules;
	}

	/**
	 * Delete all Modules
	 */
	public void deleteAll() {
		List<PersistedModule> pModules = findAll();
		for (PersistedModule pModule : pModules) {
			delete(pModule);
		}
	}

	/**
	 * Create a session and sets it as current session<br>
	 * @return
	 */
	public Session openCurrentSession() {
		currentSession = getSessionFactory().openSession();
		return currentSession;
	}

	/**
	 * Open a session and sets it as current sessionand starts a transaction<br>
	 * @return the current session
	 */
	public Session openCurrentSessionwithTransaction() {
		currentSession = getSessionFactory().openSession();
		currentTransaction = currentSession.beginTransaction();
		return currentSession;
	}

	/**
	 * Closes the current session<br>
	 */
	public void closeCurrentSession() {
		getCurrentSession().close();
	}

	/**
	 * Commit changes and close current session<br>
	 */
	public void closeCurrentSessionwithTransaction() {
		getCurrentTransaction().commit();
		getCurrentSession().close();
	}

	/**
	 * This methods create the session factory if this does not exists<br> 
	 * and return the session factory<br> 
	 * @return The session  Factory
	 */
	private SessionFactory getSessionFactory() {
		if(sessionFactory!=null)
			return sessionFactory;
		
		Configuration configuration = new Configuration().configure();
		StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties());
		sessionFactory = configuration.buildSessionFactory(builder.build());
		return sessionFactory;
	}

	/**
	 * Returns current session<br>
	 * @return current session 
	 */
	public Session getCurrentSession() {
		return currentSession;
	}

	/**
	 * Sets current session<br>
	 * @param currentSession
	 */
	public void setCurrentSession(Session currentSession) {
		this.currentSession = currentSession;
	}

	/**
	 * Get current Transaction<br>
	 * @return the current transaction
	 */
	public Transaction getCurrentTransaction() {
		return currentTransaction;
	}

	/**
	 * Sets the current transaction
	 * @param currentTransaction
	 */
	public void setCurrentTransaction(Transaction currentTransaction) {
		this.currentTransaction = currentTransaction;
	}

	/**
	 * This methods cleans up the the session pool<br>
	 * Whenever the DAO is no more needed, this methods must be called to free up resources<br>
	 */
	public void cleanUp() {
		getSessionFactory().close();
		sessionFactory = null;
	}
}
