package com.acme.module.builder;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.Set;

import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.KieFileSystemImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel.KieSessionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acme.module.model.Base;
import com.acme.module.model.Module;
import com.acme.module.model.RuleArtefact;
import com.acme.module.model.SessionType;
//import com.sun.xml.internal.bind.v2.TODO;

/**
 *This class is responsible for building rule archives and saving them on the file system.<br>
 *For that, it uses Archive description obtained from a Module Object that has been<br> 
 *- Initialized from a module.properties file with the help of {@link com.acme.module.properties.ModulePropertyLoader}<br> 
 *- loaded from GIT with the help of {@link TODO}
 *
 *
 */
public class ModuleBuilder {

	static Logger logger = LoggerFactory.getLogger(ModuleBuilder.class);

	private static final String K_MODULE_FILE_PATH = "META-INF/kmodule.xml";
	private static final String POM_PROPERTIES_FILE_PATH = "META-INF/maven/pom.properties";
	private static final String BASE_RULE_FILE_PATH ="src/main/resources/"; 
	
	
	/**
	 * This method generates a rule archive from a module object into a target directory.<br>
	 * However, before its starts its processing it checks for inconsistencies<br> 
	 * in module or targetDirectory Objects and my return an <strong>InvalidParameterException</strong> with a motivation String<br>
	 * 
	 * @param module
	 * @param targetDirectory
	 * @return true in successful, false otherwise
	 */
	public boolean generateArchiveAndSaveToFile(Module module, File targetDirectory){

		String errors = null;
		if((errors = checkParametersForErrors(module,targetDirectory))!=null){
			throw new InvalidParameterException(errors);
		}
		
		ReleaseId releaseId = createReleaseIdFromModule(module);
		KieModuleModel kieModuleModel = createKieModuleModelFromModule(module);
		KieFileSystem kieFileSystem = KieServices.Factory.get().newKieFileSystem();

		fillKieFileSystem(kieFileSystem,kieModuleModel, releaseId,module );
		if(compileVfsModule(kieFileSystem)){
			generateJARArchiveFromKieFileSystem(kieFileSystem, module, targetDirectory);
		}
		else{
			logger.error("Rules for module<"+module+"> did not compile, check log for more information");
			return false;
		}
		return true;
	}

	/**
	 *  This method checks parameters for generating a rule archive,<br> 
	 *  if these params are not well defined, an error will be returned as a string<br>
	 * @param module
	 * @param targetDirectory
	 * @return a string that contains first error that can be caught while checking module object grape<br> 
	 * or target Directory inconsistency
	 */
	private String checkParametersForErrors(Module module, File targetDirectory) {
		if(module == null){
			return "Module object is null, rule archive can not be built";
		}else{
			if(module.getArtefactId()==null)
				return "Module artefactId is null, rule archive can not be built";
			if(module.getGroupId()==null)
				return "Module groupId is null, rule archive can not be built";
			if(module.getVersionId()==null)
				return "Module versionId is null, rule archive can not be built";
			if(module.getArtefacts()==null||module.getArtefacts().isEmpty()){
				return "No rule artefacts has been loaded from GIT, rule archive can not be built";
			}else{
				if(module.getBases().isEmpty()){
					return "No Base have been defined for Module <"
							+ module.getGroupId()+","
							+ module.getArtefactId()+","
							+ module.getVersionId()+">, rule archive can not be built";
				}else{
					Set<String> baseNames = module.getBases().keySet();
					for(String baseName :baseNames ){
						Base base =  module.getBases().get(baseName);
						if(base.getPackages().isEmpty()){
							return "No packages have been defined for base<"+baseName+"> for Module <"
											+ module.getGroupId()+","
											+ module.getArtefactId()+","
											+ module.getVersionId()+">, rule archive can not be built";
						}else{
							if(base.getSession()==null){
								return "No session have been defined for base<"+baseName+"> for Module <"
										+ module.getGroupId()+","
										+ module.getArtefactId()+","
										+ module.getVersionId()+">, rule archive can not be built";
							}else{
								if(base.getSession().getName()==null||"".equals(base.getSession().getName())){
									return"No session name have been defined for base<"+baseName+"> for Module <"
											+ module.getGroupId()+","
											+ module.getArtefactId()+","
											+ module.getVersionId()+">, rule archive can not be built";
								}
							}
						}
					}
				}
			}
		}
		if(targetDirectory==null){
			return "Target Directory is null, rule archive can not be built";
		}
		logger.debug("No error found");
		return null;
	}

	/**
	 * This methods fills the virtual file system with needed informations such as :<br>
	 * - pom.xml file in  root fileSystem<br>
	 * - kmodule.xml in META-INF directory<br>
	 * - pom.properties in META-INF/maven/ directory<br>
	 * - all rules artifacts in their src/main/resources/<strong>name space</strong> directory<br>
	 * @param kieFileSystem
	 * @param kieModuleModel 
	 * @param releaseId
	 * @param module
	 */
	private void fillKieFileSystem(KieFileSystem kieFileSystem,
			KieModuleModel kieModuleModel, ReleaseId releaseId, Module module) {

		kieFileSystem.generateAndWritePomXML(releaseId);
		kieFileSystem.write(K_MODULE_FILE_PATH,kieModuleModel.toXML());
		kieFileSystem.write(POM_PROPERTIES_FILE_PATH,createPomPropertiesContent(module));

		// write rules
		for(RuleArtefact ruleArtefact : module.getArtefacts()){
			writeArtefactToKieFilesystem(kieFileSystem, ruleArtefact);
		}
	}
	
	/**
	 * This methods writes rule artefacts to virtual file system
	 * @param kieFileSystem
	 * @param ruleArtefact
	 */
	private void writeArtefactToKieFilesystem(KieFileSystem kieFileSystem,
			RuleArtefact ruleArtefact) {
		kieFileSystem.write(BASE_RULE_FILE_PATH 
				+ruleArtefact.getNameSpace().replace('.', '/')
				+ "/"+ruleArtefact.getName()+"."+ruleArtefact.getType().getExtension(),
				ruleArtefact.getContent());
	}
	
	/**
	 * Create a ReleaseId object from KIeServices factory that will identify a ruleset in KieServices repository.
	 * @param module
	 * @return
	 */
	private ReleaseId createReleaseIdFromModule(Module module){
		return KieServices.Factory.get().newReleaseId(module.getGroupId(), module.getArtefactId(), module.getVersionId());
	}

	
	/**
	 * Create a property file content for pom.property file that needs to be found in rule archive
	 * @param module
	 * @return
	 */
	private static String createPomPropertiesContent(Module module){
		StringBuilder sb = new StringBuilder();
		sb.append("groupId="+module.getGroupId()+"\r");
		sb.append("artifactId="+module.getArtefactId()+"\r");
		sb.append("version="+module.getVersionId()+"\r");
		return sb.toString();
	}

	/**
	 * This method creates the KieModuleModel.<br> 
	 * This model is used to describe KieBase(s) and KieSession(s) that are described in kmodule.xml file<br>
	 * <strong>NOTE:</strong> Base class in Module class does not allow KieBase compositions.<br>
	 * @param module
	 * @return
	 */
	private KieModuleModel createKieModuleModelFromModule(Module module){
		KieModuleModel kieModuleModel = KieServices.Factory.get().newKieModuleModel();
		Set<String> baseNames = module.getBases().keySet();
		for(String baseName :baseNames ){
			if(module.getBases().containsKey(baseName)){
				Base base = module.getBases().get(baseName);
				KieBaseModel kieBaseModel = kieModuleModel.newKieBaseModel(baseName)
						.setEqualsBehavior(null)
						.setEventProcessingMode(null)
						.setScope(null)
						.setDeclarativeAgenda(null);
				for(String pack : base.getPackages()){
					kieBaseModel.addPackage(pack);
				}
				kieBaseModel.newKieSessionModel(base.getSession().getName())
				.setType(getKieSessionType(base.getSession().getType()))
				.setBeliefSystem(null);
			}
		}
		return kieModuleModel;
	}
	
	/**
	 * This method maps KieSessionType from SessionType enums
	 * @param type
	 * @return always a KieSessionType, by default it is STATELESS
	 */
	private KieSessionType getKieSessionType(SessionType type) {
		for(KieSessionType kieSessionType : KieSessionType.values()){
			if(kieSessionType.toString().equalsIgnoreCase(type.toString()))
				return kieSessionType;
		}
		logger.error("Session type is not well formed <"+type+">, setting it to STATELESS");
		return KieSessionType.STATELESS;// by default
	}

	/**
	 * This method compiles rules that were added in memory file system.<br>
	 * Models that are imported in rules must be in the class path so that imports may be solved at runtime<br>
	 * @param kieFileSystem
	 * @return
	 */
	private boolean compileVfsModule(KieFileSystem kieFileSystem){
		KieBuilder  kieBuilder = KieServices.Factory.get().newKieBuilder(kieFileSystem);
		kieBuilder.buildAll();
		if (kieBuilder.getResults().hasMessages(Level.ERROR)) {
			logger.error("Unable to build KieModule\n"+ kieBuilder.getResults().toString());
			return false;
		}
		return true;
	} 
	
	/**
	 * This method creates an Jar rule Archive for KieFileSystem into targetDir directory on the local fileSystem
	 * @param kieFileSystem
	 * @param module
	 * @param targetDir
	 */
	private static void generateJARArchiveFromKieFileSystem(
			KieFileSystem kieFileSystem, Module module, File targetDir) {
		MemoryFileSystem mfs = ((KieFileSystemImpl) kieFileSystem).asMemoryFileSystem();
		mfs.writeAsJar(targetDir,module.getArtefactId()+ "-" +module.getVersionId());
	}
}
