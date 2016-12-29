package com.acme.module.management;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acme.module.db.PersistedModuleService;
import com.acme.module.db.model.PersistedModule;
import com.acme.module.db.model.PersistedModuleId;
import com.acme.module.jmx.ModuleManagerMXBean;
/**
 * ModuleManager is a MXBean, that maintain a set of PersistedModule.<br>
 * This set is updated when :<br>
 * - method <b>loadModulesFromDb</b> is invoked(it loads all PersistedModule from an underlying database)<br>
 * - new PersistedModule instances are added to the set, since last call to <b>loadModulesFromDb</b><br>. 
 * when it looks in db for persisted modules
 *
 */
public class ModuleManager implements ModuleManagerMXBean {

	static Logger logger = LoggerFactory.getLogger(ModuleManager.class);
	private final Set<PersistedModule> loadedModules = new HashSet<PersistedModule>();

	public void loadModulesFromDb() {
		PersistedModuleService pms = new PersistedModuleService();
		List<PersistedModule> persistedModules = pms.findAll();
		if(persistedModules.size()>0){
			if(logger.isDebugEnabled())
				logger.debug("loaded <"+persistedModules.size()+"> modules");
			loadedModules.addAll(persistedModules);
		}
	}
	
	public final String[] getLoadedModulesAsStrings() {
		List<String> result = new ArrayList<String>();
		Iterator<PersistedModule> iterator  = loadedModules.iterator();
		while(iterator.hasNext()){
			PersistedModule pm = iterator.next();
			PersistedModuleId pmid=pm.getPersistedModuleId();
			String name = pmid.getGroupId()+"."+pmid.getArtefactId()+"."+pmid.getVersionId();
			result.add(name);
		}
		return result.toArray(new String[result.size()]);
	}
	
	public final PersistedModule[] getLoadedModules() {
		List<PersistedModule> result = new ArrayList<PersistedModule>();
		Iterator<PersistedModule> iterator  = loadedModules.iterator();
		while(iterator.hasNext()){
			PersistedModule pModule = iterator.next();
			if(logger.isDebugEnabled())
				logger.debug("Module <"+pModule+"> is loaded");
			result.add(pModule);
		}
		return result.toArray(new PersistedModule[result.size()]);
	}
}
