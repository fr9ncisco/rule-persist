package com.acme.module.management;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;

import org.kie.api.KieServices;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acme.module.db.model.PersistedModule;
import com.acme.module.jmx.ServiceManagerMXBean;
import com.acme.module.model.Gav;
import com.acme.module.model.Module;

public class ServiceManager implements ServiceManagerMXBean{

	private static final ServiceManager instance = new ServiceManager();
	private static final List<RuleService> registeredServices = new ArrayList<RuleService>();
	static Logger logger = LoggerFactory.getLogger(ServiceManager.class);
	
	public static ServiceManager getInstance(){
		return instance;
	}
	
	private ServiceManager() {
	} 
	
	/**
	 * Update only containers who need to be updated
	 */
	public void updateRegisteredKieContainers(){
		
		// we need to filter out modules
		// we will filter them out based on latest version information
		List<PersistedModule> latestModules = getLatestPersistedModules();

		udpateKieRepository(latestModules);
		
		// for each registered service, 
		// verify if service KieContainer needs to be updated : ie not in the latestPersistedModule list
		for(RuleService ruleService:registeredServices){
			Module pm = getModuleFromReleaseId(ruleService.getKieContainer().getReleaseId());
			if(logger.isInfoEnabled())
				logger.info("Getting module <"+pm.getName()+"> for releasID<"+ruleService.getKieContainer().getReleaseId().toExternalForm()+">");
			if(!latestModules.contains(pm)){
				ReleaseId releaseId = getLatestReleaseIdForModule(pm,latestModules);
				if(logger.isInfoEnabled())
					logger.info("updating service from <"+ruleService.getKieContainer().getReleaseId().toExternalForm()+"> to <"+releaseId.toExternalForm()+">");
				ruleService.getKieContainer().updateToVersion(releaseId);
			}
		}
	}
	
	private void udpateKieRepository(List<PersistedModule> latestModules) {
		KieServices kieServices = KieServices.Factory.get();
		KieRepository kieRepository = kieServices.getRepository();
		for(PersistedModule pm : latestModules){
			kieRepository.addKieModule(kieServices.getResources().newByteArrayResource(pm.getContent()));
		}
	}

	private ReleaseId getLatestReleaseIdForModule(Module module,
			List<PersistedModule> latestModules) {
		
		Iterator<PersistedModule> it = latestModules.iterator();
		while(it.hasNext()){
			PersistedModule pm = it.next();
			
			if(module.getGroupId().equals(pm.getPersistedModuleId().getGroupId())
					&&module.getArtefactId().equals(pm.getPersistedModuleId().getArtefactId()))
				return createReleaseIdFromModule(pm);
		}
		return null;
	}

	private ReleaseId createReleaseIdFromModule(PersistedModule pm) {
		ReleaseId releaseId = KieServices.Factory.get().newReleaseId(pm.getPersistedModuleId().getGroupId(), pm.getPersistedModuleId().getArtefactId(), pm.getPersistedModuleId().getVersionId());
		return releaseId;
	}

	
	private Module getModuleFromReleaseId(ReleaseId releaseId) {
		Module module = new Module(releaseId.getGroupId(), releaseId.getArtifactId(), releaseId.getVersion());
		return module;
	}

	/**
	 * 
	 * @return
	 */
	private List<PersistedModule> getLatestPersistedModules() {
		 MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
		 ObjectName name = null;
		 List<PersistedModule> pModules = new ArrayList<PersistedModule>();
		 try {
			name = new ObjectName("com.acme.module.jmx:type=ModuleManager");
			if(!mbs.isRegistered(name)){
				// TODO FGT : throw an exception
			}
			else{
				// Decode what we get from JMX server :
				// A PersistedModule is a Composite data
				// Here we get an array of those with LoadedModules
				CompositeData[]  cda = (CompositeData[])mbs.getAttribute(name, "LoadedModules");

				// Iterate over each PersistedModule i.e. composite data
				for(CompositeData cd : cda){

					//content : byte[] is a simple type 
					byte[] content  = (byte[]) cd.get("content");

					// PersistedModuleId is a Composite data
					CompositeData cd_pmid= (CompositeData) cd.get("persistedModuleId");
					
					// a PersistedModuleId has simple type attributes
					String groupId = (String)cd_pmid.get("groupId");
					String artefactId = (String)cd_pmid.get("artefactId");
					String versionId = (String)cd_pmid.get("versionId");

					// Now we can instantiate a PersistedModule and add it to the list we have to return
					PersistedModule pm = new PersistedModule(groupId, artefactId, versionId);
					pm.setContent(content); 
					pModules.add(pm);
					
				}
				pModules = filterLatestPersistedModules(pModules);
				return pModules;
			}
		} 
		catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (AttributeNotFoundException e) {
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		} catch (MBeanException e) {
			e.printStackTrace();
		} catch (ReflectionException e) {
			e.printStackTrace();
		} 
		return pModules;
	}

	private List<PersistedModule> filterLatestPersistedModules(
			List<PersistedModule> pModules) {
		if(logger.isDebugEnabled())
			logger.debug("AVANT SORT <"+pModules+">");
		Collections.sort( pModules, new Comparator<PersistedModule>() {
			public int compare(PersistedModule o1, PersistedModule o2) {
				Gav gav1 = o1.toGav();
				Gav gav2 = o2.toGav();
				if(gav1.getGroupId().equals(gav2.getGroupId())){
					if(gav1.getArtefactId().equals(gav2.getArtefactId())){
						return gav1.getVersionId().compareTo(gav2.getVersionId());
					}					
					else
						// TODO FGT: create a better comparator to compare SNAPSHOT and release(i.e. 1.2.5 and 1.2.5-SNAPSHOT) if needed
						return gav1.getArtefactId().compareTo(gav2.getArtefactId());
				}
				else
					return gav1.getGroupId().compareTo(gav2.getGroupId());
			}
		});
		if(logger.isDebugEnabled())
			logger.debug("APRES SORT <"+pModules+">");
		Collections.reverse(pModules);
		if(logger.isDebugEnabled())
			logger.debug("APRES REVERSE <"+pModules+">");
		pModules = removeDuplicatesAndKeepLatest(pModules);
		if(logger.isDebugEnabled())
			logger.debug("APRES REMOVE DUPLICATES <"+pModules+">");
		return pModules;
	}

	private List<PersistedModule> removeDuplicatesAndKeepLatest(
			List<PersistedModule> pModules) {
		Iterator<PersistedModule> it = pModules.iterator();
		PersistedModule predecessor = null;
		while(it.hasNext()){
			if(predecessor == null){
				predecessor = it.next();
			}
			else{
				PersistedModule current = it.next();
				if(predecessor.getPersistedModuleId().getGroupId().equals(current.getPersistedModuleId().getGroupId())){
					if(predecessor.getPersistedModuleId().getArtefactId().equals(current.getPersistedModuleId().getArtefactId())){
						if(logger.isDebugEnabled())
							logger.debug("FOUND DUPLICATES predecessor<"+predecessor+"> current<"+current+">");
						it.remove();
					}
					else
						predecessor = current;
				}
				else
					predecessor = current;
			}
		}
		return pModules;
	}

	public void registerKieContainer(RuleService service){
		if(service !=null){
			if(!registeredServices.contains(service)){
				registeredServices.add(service);
				if(logger.isDebugEnabled())
					logger.debug("Registered service <"+service.getName()+"> !!!");
			}
		}
		else
			throw new IllegalArgumentException("Rule Service cannot be null");
	}



	public String[] getRegisteredServicesNames() {
		int index = 0;
		String[] names = new String[registeredServices.size()];
		for(RuleService service : registeredServices){
			names[index++] = service.getName();
		}
		return names;
	}
}
