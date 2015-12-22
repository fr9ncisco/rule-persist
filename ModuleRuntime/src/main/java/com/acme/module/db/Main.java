package com.acme.module.db;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.acme.module.db.model.PersistedModule;

/**
 * Allows to create the database schema, with a dummy record
 * ....A hammer to kill a fly
 * 
 */
public class Main {
	public static void main(String[] args)throws Exception {

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPAService");
	    EntityManager em = emf.createEntityManager();
	    em.getTransaction().begin();

	    PersistedModule pm = new PersistedModule("titi", "dede", "tata");

	    em.persist(pm);
	    em.getTransaction().commit();
	    em.close();
	    emf.close();
	}

}
