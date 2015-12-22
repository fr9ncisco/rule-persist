package com.acme.module.jmx;

public interface ServiceManagerMXBean {
	/**
	 *  Updates all registered Rule services/ KieContainers with latest available JARed Module
	 */
	 @DisplayName("Opeartion: updateServices")
	public void updateRegisteredKieContainers();
	 
	 /**
	  * Allows to know which services name are registered<br>
	  * For Information only : can be shown in JConsole for example.<br>
	  * @return 
	  */
	 public String[] getRegisteredServicesNames(); 
}
