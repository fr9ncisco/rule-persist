package com.acme.module.db;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import com.acme.module.db.model.PersistedModuleId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.acme.module.db.PersistedModuleService;
import com.acme.module.db.model.PersistedModule;
import com.acme.module.model.Module;

public class PersistedModuleServiceTest {

	public static final String MODULE1_GROUPID="com.acme.tools.rules";
	public static final String MODULE1_ARTEFACTID="hello";
	public static final String MODULE1_VERSIONID="1.0.2-SNAPSHOT";
	
	public static final String MODULE2_GROUPID="com.acme.tools.rules";
	public static final String MODULE2_ARTEFACTID="hello";
	public static final String MODULE2_VERSIONID="1.0.1-SNAPSHOOT";

	public static final String VALID_ARCHIVE_FILE = "/Users/Francois/Desktop/dev/rule-persist/ModuleArchiver/target/generated-1.0.0-SNAPSHOOT.jar";
	public static final String INVALID_ARCHIVE_FILE = "dede.jar";

	PersistedModuleService pms;
	
	@Before
	public void setUp() throws Exception {
		pms = new PersistedModuleService();
	}

	@After
	public void tearDown() throws Exception {
		pms.cleanUp();
		pms = null;
	}

	@Test
	public void testPersistRuleArchive() {
		pms.deleteAll();

		Module module = new Module(MODULE1_GROUPID,MODULE1_ARTEFACTID,MODULE1_VERSIONID);

		// invalid file will generate an exception
		try {
			pms.persistRuleArchive(module, new File(INVALID_ARCHIVE_FILE));
		} catch (Exception e) {
			assertTrue(e.getClass().equals(java.io.FileNotFoundException.class));
		}

		// null file will generate an exception
		try {
			pms.persistRuleArchive(module, null);
		} catch (Exception e) {
			assertTrue(e.getClass().equals(java.io.FileNotFoundException.class));
		}

		// null module will generate an exception
		try {
			pms.persistRuleArchive(null, new File(INVALID_ARCHIVE_FILE));
		} catch (Exception e) {
			assertTrue(e.getClass().equals(com.acme.module.exception.ModuleException.class));
		}

		// valid module and valid file will not generate an exception
		try {
			pms.persistRuleArchive(module, new File(VALID_ARCHIVE_FILE));

		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception is expected here : "+e.getMessage());
		} finally {
			pms.delete(module);
		}
	}

	@Test
	public void testRead() {
		
		// null file will generate an exception
		try {
			pms.readFileAsByteArray(null);
		} catch (Exception e) {
			assertTrue(e.getClass().equals(java.io.FileNotFoundException.class));
		}
		
		// invalid file will generate an exception
		try {
			pms.readFileAsByteArray(new File(INVALID_ARCHIVE_FILE));
		} catch (Exception e) {
			assertTrue(e.getClass().equals(java.io.FileNotFoundException.class));
		}
		
		// valid file will be red with normal byte length
		try {
			byte[] content = pms.readFileAsByteArray(new File(VALID_ARCHIVE_FILE));
			assertTrue(content != null);
			assertTrue(content.length>0);
			File fs =  new File(VALID_ARCHIVE_FILE);
			assertTrue(content.length == fs.length());
		} catch (Exception e) {
			fail("No exception is expected here : "+e.getMessage());
		}
	}

	@Test
	public void testPersist() {
		
		PersistedModule pm = null;
		Module module = new Module(MODULE1_GROUPID, MODULE1_ARTEFACTID, MODULE1_VERSIONID);

		// valid module description will be persisted as PersistedModule with content set
		try {
			pm = new PersistedModule(MODULE1_GROUPID, MODULE1_ARTEFACTID, MODULE1_VERSIONID);
			pm.setContent(pms.readFileAsByteArray(new File(VALID_ARCHIVE_FILE)));
			pms.persist(pm);
			
		} catch (Exception e) {
			fail("No exception is expected here : "+e.getMessage());
		}
		finally{
			//pms.findById(module);
			PersistedModule pm2;
			pm2 = pms.findById(module);

			assertTrue(pm2.equals(pm));
			assertTrue(pm2.getPersistedModuleId().equals(pm.getPersistedModuleId()));
			pms.delete(module);
		}

		// invalid module description will not be persisted
		try {
			pm = new PersistedModule(null, MODULE1_ARTEFACTID, MODULE1_VERSIONID);
			pms.persist(pm);
		} catch (Exception e) {
			assertTrue(e.getClass().equals(org.hibernate.exception.ConstraintViolationException.class));
		}

		// invalid module description will not be persisted
		try {
			pm = new PersistedModule(MODULE1_GROUPID, null, MODULE1_VERSIONID);
			pms.persist(pm);
		} catch (Exception e) {
			assertTrue(e.getClass().equals(org.hibernate.exception.ConstraintViolationException.class));
		}

		// invalid module description will not be persisted
		try {
			pm = new PersistedModule(MODULE1_GROUPID, MODULE1_ARTEFACTID, null);
			pms.persist(pm);
		} catch (Exception e) {
			assertTrue(e.getClass().equals(org.hibernate.exception.ConstraintViolationException.class));
		}
		
		// invalid module description will not be persisted
		try {
			pm = new PersistedModule(MODULE1_GROUPID, MODULE1_ARTEFACTID, null);
			pms.persist(pm);
		} catch (Exception e) {
			assertTrue(e.getClass().equals(org.hibernate.exception.ConstraintViolationException.class));
		}

		// valid module description will be persisted as PersistedModule with content set
		try {
			pm = new PersistedModule(MODULE1_GROUPID, MODULE1_ARTEFACTID, MODULE1_VERSIONID);
			pm.setContent(pms.readFileAsByteArray(new File(VALID_ARCHIVE_FILE)));
			pms.persist(pm);
			
		} catch (Exception e) {
			fail("No exception is expected here : "+e.getMessage());
		}
		finally{
			pms.findById(module);
			PersistedModule pm2 = pms.findById(module);
			assertTrue(pm2.getContent() != null);
			assertTrue(pm2.equals(pm));
			assertTrue(pm2.getPersistedModuleId().equals(pm.getPersistedModuleId()));
			pms.delete(module);
		}
	}

	@Test
	public void testUpdate() {
		
		Module module = new Module(MODULE1_GROUPID,MODULE1_ARTEFACTID,MODULE1_VERSIONID);


		PersistedModule pm = null;

		// update with a new content
		try {
			// persist a pm without content
			pm = new PersistedModule(MODULE1_GROUPID, MODULE1_ARTEFACTID, MODULE1_VERSIONID);
			pm.setContent(pms.readFileAsByteArray(new File(VALID_ARCHIVE_FILE)));

			pms.persist(pm);
			pms.findById(module);
			byte[] content = pms.readFileAsByteArray(new File(VALID_ARCHIVE_FILE));
			
			// set content and update
			PersistedModule pm1 = pms.findById(module);
			pm1.setContent(content);
			pms.update(pm1);
			
			PersistedModule pm2 = pms.findById(module);
			assertTrue(pm2.getContent()!=null);
			assertTrue(pm2.getContent().length == content.length);
			assertTrue(pm2.equals(pm));
			assertTrue(pm2.getPersistedModuleId().equals(pm.getPersistedModuleId()));
		} catch (Exception e) {
			fail("No exception is expected here : "+e.getMessage());
		}
		finally{
			pms.delete(module);
		}
	}

	@Test
	public void testFindById() {
		Module module = new Module(MODULE1_GROUPID,MODULE1_ARTEFACTID,MODULE1_VERSIONID);
		PersistedModule pm = null;

		// find by ID a persisted module
		try {
			// persist a pm
			pm = new PersistedModule(MODULE1_GROUPID, MODULE1_ARTEFACTID, MODULE1_VERSIONID);
			pm.setContent(pms.readFileAsByteArray(new File(VALID_ARCHIVE_FILE)));
			pms.persist(pm);
			pms.findById(module);
			PersistedModule pm2 = pms.findById(module);
			assertTrue(pm2.equals(pm));
			assertTrue(pm2.getPersistedModuleId().equals(pm.getPersistedModuleId()));
		} catch (Exception e) {
			fail("No exception is expected here : "+e.getMessage());
		}
		finally{
			pms.delete(module);
		}

		// find by ID a non persisted module will return null
		try {
			pms.findById(module);
			PersistedModule pm2 = pms.findById(module);
			assertTrue(pm2 == null);
		} catch (Exception e) {
			fail("No exception is expected here : "+e.getMessage());
		}
	}

	@Test
	public void testDelete() {

		Module module = new Module(MODULE1_GROUPID,MODULE1_ARTEFACTID,MODULE1_VERSIONID);

		PersistedModule pm = null;

		// delete a persisted module
		try {
			pm = new PersistedModule(MODULE1_GROUPID, MODULE1_ARTEFACTID, MODULE1_VERSIONID);
			pm.setContent(pms.readFileAsByteArray(new File(VALID_ARCHIVE_FILE)));
			pms.persist(pm);
			pms.delete(module);
			PersistedModule pm2 = pms.findById(module);
			assertTrue(pm2 == null);
		} catch (Exception e) {
			fail("No exception is expected here : "+e.getMessage());
		}
		
		// delete a non persisted module
		try {
			pms.delete(module);
		} catch (Exception e) {
			assertTrue(e.getClass().equals(java.lang.IllegalArgumentException.class));
		}
	}

	@Test
	public void testFindAll() {
		Module module = new Module(MODULE1_GROUPID,MODULE1_ARTEFACTID,MODULE1_VERSIONID);
		Module module2 = new Module(MODULE2_GROUPID,MODULE2_ARTEFACTID,MODULE2_VERSIONID);
		
		PersistedModule pm = null;
		PersistedModule pm2 = null;
		
		try {
			pm = new PersistedModule(MODULE1_GROUPID, MODULE1_ARTEFACTID, MODULE1_VERSIONID);
			pm.setContent(pms.readFileAsByteArray(new File(VALID_ARCHIVE_FILE)));
			pms.persist(pm);
			
			pm2 = new PersistedModule(MODULE2_GROUPID, MODULE2_ARTEFACTID, MODULE2_VERSIONID);
			pm2.setContent(pms.readFileAsByteArray(new File(VALID_ARCHIVE_FILE)));
			pms.persist(pm2);
			
			List<PersistedModule> listPModules= pms.findAll();
			assertTrue(listPModules.size()==2);
			assertTrue(listPModules.contains(pm2));
			assertTrue(listPModules.contains(pm));

			pms.delete(module);
			pms.delete(module2);
			listPModules= pms.findAll();
			assertTrue(listPModules.size()==0);
			assertTrue(!listPModules.contains(pm2));
			assertTrue(!listPModules.contains(pm));
		} catch (Exception e) {
			fail("No exception is expected here : "+e.getMessage());
		}
	}

	@Test
	public void testDeleteAll() {
		
		PersistedModule pm = null;
		PersistedModule pm2 = null;
		
		try {
			pm = new PersistedModule(MODULE1_GROUPID, MODULE1_ARTEFACTID, MODULE1_VERSIONID);
			pm.setContent(pms.readFileAsByteArray(new File(VALID_ARCHIVE_FILE)));
			pms.persist(pm);
			
			pm2 = new PersistedModule(MODULE2_GROUPID, MODULE2_ARTEFACTID, MODULE2_VERSIONID);
			pm2.setContent(pms.readFileAsByteArray(new File(VALID_ARCHIVE_FILE)));
			pms.persist(pm2);
			
			List<PersistedModule> listPModules= pms.findAll();
			assertTrue(listPModules.size()==2);
			assertTrue(listPModules.contains(pm2));
			assertTrue(listPModules.contains(pm));

			pms.deleteAll();

			listPModules= pms.findAll();
			assertTrue(listPModules.size()==0);
			assertTrue(!listPModules.contains(pm2));
			assertTrue(!listPModules.contains(pm));
		} catch (Exception e) {
			fail("No exception is expected here : "+e.getMessage());
		}
	}
}