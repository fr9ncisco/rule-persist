package com.acme.module.properties;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import com.acme.module.model.Base;
import com.acme.module.model.Module;
import com.acme.module.model.Session;
import com.acme.module.model.SessionType;

/**
 * This class parses a modules.property file looking combined properties.
 * Some properties may have comma separated values.
 * When parsing is done, generated modules and parsing errors are available.
 * 
 *
 */
public class ModulePropertyLoader {
	
	private static final String ALL_MODULES_KEYS = "all.modules";
	
	private static final String GROUPID_MODULE_SUFFIX = ".groupid";
	private static final String ARTEFACTID_MODULE_SUFFIX = ".artefactid";
	private static final String VERSIONID_MODULE_SUFFIX = ".versionid";

	private static final String BASES_KEYS_SUFFIX = ".bases";
	private static final String PACKAGES_KEYS_SUFFIX = ".packages";
	private static final String SESSION_KEY_SUFFIX = ".session";
	private static final String SESSION_TYPE_SUFFIX = ".type";
	private static final char COMA =',';
	private final Properties properties;
	private final List<String> errors = new ArrayList<String>();
	private final List<String> warnings = new ArrayList<String>();
	private boolean isErrorRaised = false;
	private Set<Module> modules = new HashSet<Module>();

	/**
	 * 
	 * @param props
	 */
	public ModulePropertyLoader(Properties props){
		if(props==null){
			throw (new InvalidParameterException("Module properties cannot be null! Rules cannot be built"));
		}
		this.properties = props;
	}
	
	/**
	 * 
	 */
	public void processProperties(){
		List<String> moduleKeys = getModuleKeys();
		if(moduleKeys.isEmpty()){
			setError("No Rule Module key defined, check rule modules property file");
			return;
		}
		// check for necessary module properties
		for(String modulekey :moduleKeys ){
			// without errors, we add the module
			Module module = loadModule(modulekey);
			if(!isErrorRaised){
				modules.add(module);
			}
			else{
				// there were errors
				isErrorRaised = false;	// reset module parsing status so we might load other modules 
			}
		}
	}
	
	
	/**
	 * Retrieves the list of Module Names from properties as a List of Strings
	 * @return
	 */
	private List<String> getModuleKeys(){
		String modulesKeys = getProperty(ALL_MODULES_KEYS);
		if(modulesKeys != null && !"".equals(modulesKeys))
			return splitKeys(modulesKeys);
		else{
			setError("Check rule module file presence");	
			return new ArrayList<String>();
		}
	}

	/**
	 * Splits a comma separated list of values into a List of Strings
	 * @param stringKeys
	 * @return
	 */
	private List<String> splitKeys(final String stringKeys) {
		ArrayList<String> keys = new ArrayList<String>();
		String searchKeysString = stringKeys.toString().trim();
		StringTokenizer st = new StringTokenizer(searchKeysString,""+COMA);
		while(st.hasMoreElements()){
			String value =((String) st.nextElement()).trim() ;
			if(!"".equals(value))
				keys.add(value);
		}
		return keys;
	}

	/**
	 * 
	 * @param moduleKey
	 * @return
	 */
	private Module loadModule(String moduleKey){
		String groupId = getProperty(moduleKey+GROUPID_MODULE_SUFFIX);
		if(groupId == null){
			setError("groupid property is not defined for module <"+moduleKey+">, therefore this module can't be loaded");
		}
		String artefactId = getProperty(moduleKey+ARTEFACTID_MODULE_SUFFIX);
		if(artefactId == null){
			setError("artefactid property is not defined for module <"+moduleKey+">, therefore this module can't be loaded");
		}
		String versionId = getProperty(moduleKey+VERSIONID_MODULE_SUFFIX);
		if(versionId == null){
			setError("versionid property is not defined for module <"+moduleKey+">, therefore this module can't be loaded");
		}
		if((groupId != null)&&(artefactId != null)&&(versionId != null)){
			Module module = new Module(groupId, artefactId,versionId);
			List<String> baseKeys = getBaseKeysForModule(moduleKey);
			if(baseKeys.isEmpty()){
				setError("Check rule property module file, no base defined for module <"+moduleKey+">");
			}
			else{
				for(String baseKey: baseKeys){
					Base base = loadBaseForModule(moduleKey, baseKey);
					if(base!=null){
						module.getBases().put(baseKey, base);
					}
				}
			}
			return module;
		}
		else{
			return null;
		}
	}
	/**
	 * 
	 * @param moduleKey
	 * @return
	 */
	private List<String> getBaseKeysForModule(String moduleKey){
		String baseKeys = getProperty(moduleKey+BASES_KEYS_SUFFIX);
		if(baseKeys!=null&&!"".equals(baseKeys))
			return splitKeys(baseKeys);
		return new ArrayList<String>();
	}
	
	
	/**
	 * 
	 * @param moduleKey
	 * @param baseKey
	 * @return
	 */
	private Base loadBaseForModule(String moduleKey,String baseKey){
		Base base = new Base();
		base.setName(baseKey);
		List<String> packs = getPackagesForBase(moduleKey,baseKey);
		if(packs.isEmpty()){
			setError("Check property file, no packages defined for module<"+moduleKey+"> and base<"+baseKey+">");
		}
		else base.setPackages(packs);
		base.setSession(getSessionForBase(moduleKey,baseKey));
		return base;
	}
	
	/**
	 * 
	 * @param moduleKey
	 * @param baseKey
	 * @return
	 */
	private List<String> getPackagesForBase(String moduleKey,String baseKey){
		String packages = getProperty(moduleKey+"."+baseKey+PACKAGES_KEYS_SUFFIX);

		if(packages!=null&&!"".equals(packages) )
			return splitKeys(packages);
		return new ArrayList<String>();
	}
	
	/**
	 * 
	 * @param moduleKey
	 * @param baseKey
	 * @return
	 */
	private Session getSessionForBase(String moduleKey,String baseKey){
		String sessionkey =  getProperty(moduleKey+"."+baseKey+SESSION_KEY_SUFFIX);
		if(sessionkey == null|| sessionkey==""){
			setError("Check rule module file, session is undefined for module <"+moduleKey+"> and base <"+baseKey+">");
			return null;
		}
		String sessionType =  getProperty(moduleKey+"."+baseKey+"."+sessionkey+SESSION_TYPE_SUFFIX);
		SessionType sessionTypeValue = checkSessionType(sessionType);
		if(sessionTypeValue == null){
			setError("Check rule module file, session type is undefined or malformed for module <"+moduleKey+"> and base <"+baseKey+">");
			return null;
		}
		Session session = new Session();
		session.setType(sessionTypeValue);
		session.setName(sessionkey);
		return session;
	}

	/**
	 * 
	 * @param sessionType
	 * @return
	 */
	private SessionType checkSessionType(String sessionType) {
		return SessionType.valueOf(sessionType);
	}

	/**
	 * 
	 * @param error
	 */
	void setError(String error){
		 errors.add(error);
		 isErrorRaised = true;
	 }

	private String getProperty(String key) {
		String value = properties.getProperty(key);
		if(value == null){
			warnings.add("Property <"+key+"> does not exists in rule module property file");
			return null;
		}
		return value.trim();
	}

	/**
	 * @return the errors
	 */
	public final List<String> getErrors() {
		return errors;
	}

	/**
	 * @return the warnings
	 */
	public final List<String> getWarnings() {
		return warnings;
	}

	/**
	 * @return the modules
	 */
	public final Set<Module> getModules() {
		return modules;
	}
	
}
