package com.acme.module.management;

import org.kie.api.runtime.KieContainer;

/**
 * Defines a specific behavior for Services based on Rules<br>
 * This is required for JMX for instance where a RuleServices is registered<br>
 * So that its KieContainer can be updated.<br>
 *  
 *
 */
public interface RuleService {

	/**
	 * This allows to retrieve the container associated to a RuleService.<br>
	 * The primary purpose is to update KieContainer rules so that hot deployment is possible. 
	 * @return the current container of the service
	 */
	public KieContainer getKieContainer();

	/**
	 * Retrieve service's name, A name should be unique so that it means something <br>
	 * Although, names are for information only, <br>
	 * and do not play a technical role, a null pointer may be returned<br> 
	 *  
	 * @return the name on the rule service
	 */
	public String getName();
	
}
