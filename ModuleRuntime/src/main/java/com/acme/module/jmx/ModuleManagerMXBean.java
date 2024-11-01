package com.acme.module.jmx;

import com.acme.module.db.model.PersistedModule;

public interface ModuleManagerMXBean {

	/**
	 * Reload modules from DB
	 */
	 @DisplayName("Operation: loadModulesFromDb")
	 void loadModulesFromDb();

	 /**
	  * Get the list of modules of already loaded modules
	  * @return
	  */
	 @DisplayName("GETTER: getLoadedModules")
	 PersistedModule[] getLoadedModules();

//	/**
//	 * Get the associated persisted module from Module description
//	 * @param module
//	 * @return
//	 */
//	@DisplayName("GETTER: getPersistedModule") 
//	public PersistedModule getPersistedModule(Module module);
	
	/**
	 * 
	 * @return
	 */
	@DisplayName("GETTER: getLoadedModules")
	String[] getLoadedModulesAsStrings();
}
