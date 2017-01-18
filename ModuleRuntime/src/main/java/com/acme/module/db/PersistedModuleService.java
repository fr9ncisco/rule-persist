package com.acme.module.db;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.acme.module.db.model.PersistedModule;
import com.acme.module.exception.ModuleException;
import com.acme.module.model.Module;

/**
 * Service to Handle persistence in DB for PersistedModule.<br>
 * 
 *  
 *
 */
public class PersistedModuleService {
	
	private PersistedModuleDao persistedModuleDao;

	/**
	 * Default constructor
	 */
	public PersistedModuleService(){
		persistedModuleDao = new PersistedModuleDao();
	}
	
	/**
	 * 
	 * @param module
	 * @param ruleArchive
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void persistRuleArchive(Module module, File ruleArchive) throws FileNotFoundException, IOException {
		if(module==null)
			throw new ModuleException("Module description is null, rulearchive can not be persisted");
		PersistedModule pmodule = new PersistedModule(module.getGroupId(),module.getArtefactId(),module.getVersionId());
		byte[] content = readFileAsByteArray(ruleArchive);
		pmodule.setContent(content);
		persist(pmodule);
	}

	/**
	 * Reads a file from file system as a byte Array<br>
	 * @param file :  the file to read
	 * @return byte[] : the content of the file as a byte array
	 * @throws IOException
	 */
	public byte[] readFileAsByteArray(File file) throws IOException, FileNotFoundException {
		
		if(file==null)
			throw new FileNotFoundException("File is null");

		byte[] fileContent = new byte[(int) file.length()];
		InputStream input = null;
		try {
			int totalBytesRead = 0;
			input = new BufferedInputStream(new FileInputStream(file));
			while (totalBytesRead < fileContent.length) {
				int bytesRemaining = fileContent.length - totalBytesRead;
				int bytesRead = input.read(fileContent, totalBytesRead,
						bytesRemaining);
				if (bytesRead > 0) {
					totalBytesRead = totalBytesRead + bytesRead;
				}
			}
		} catch (FileNotFoundException fnfe) {
			throw fnfe;
		} catch (IOException ioe) {
			throw ioe;
		}
		finally {
			try {
				if(input!= null)
					input.close();
			} catch (IOException e) {
				//swallow this
			}
		}
		return fileContent;
	}

	/**
	 * Persists a PersistedModule
	 * @param entity the PersisteModule to persist
	 */
	public void persist(PersistedModule entity) {
		persistedModuleDao.openCurrentSessionwithTransaction();
		persistedModuleDao.persist(entity);
		persistedModuleDao.closeCurrentSessionwithTransaction();
	}

	/**
	 * Updates a persisted Module<br>
	 * Only the content of the PersistedModule may be updated
	 * @param entity
	 */
	public void update(PersistedModule entity) {
		persistedModuleDao.openCurrentSessionwithTransaction();
		persistedModuleDao.update(entity);
		persistedModuleDao.closeCurrentSessionwithTransaction();
	}

	/**
	 * Retrieves a PersistedModule from its description
	 * @param module
	 * @return
	 */
	public PersistedModule findById(Module module) {
		persistedModuleDao.openCurrentSessionwithTransaction();
		PersistedModule persistedModule = persistedModuleDao.findById(module);
		persistedModuleDao.closeCurrentSessionwithTransaction();
		return persistedModule;
	}

	/**
	 * Deletes a PersistedModule 
	 * @param module the module descriptor
	 */
	public void delete(Module module) {
		persistedModuleDao.openCurrentSessionwithTransaction();
		PersistedModule persistedModule = persistedModuleDao.findById(module);
		persistedModuleDao.delete(persistedModule);
		persistedModuleDao.closeCurrentSessionwithTransaction();

	}
	
	/**
	 * Retrieves the list of all PersistedModule that are persisted in DB
	 * @return
	 */
	public List<PersistedModule> findAll() {
		persistedModuleDao.openCurrentSession();
		List<PersistedModule> persistedModules = persistedModuleDao.findAll();
		persistedModuleDao.closeCurrentSession();
		return persistedModules;
	}

	/**
	 * Deletes all PersistedModule in DB 
	 */
	public void deleteAll() {
		persistedModuleDao.openCurrentSessionwithTransaction();
		persistedModuleDao.deleteAll();
		persistedModuleDao.closeCurrentSessionwithTransaction();
	}
	
	/**
	 * Necessary  Method to clean up resources.
	 */
	public void  cleanUp(){
		persistedModuleDao.cleanUp();
	}
}
