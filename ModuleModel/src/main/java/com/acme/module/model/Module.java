package com.acme.module.model;

//import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Module implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6265325723915601721L;
	/*****************************/
//	@ConstructorProperties({"groupId", "artefactId", "versionId"}) 
	public Module(String groupId, String artefactId,String versionId ){
		this();
		this.gav = new Gav(groupId, artefactId,versionId);
	}
	private Module(){}
	/*****************************/
	private Gav gav;
	public Gav getGav() {return gav;}
	public void setGav(Gav gav) {this.gav = gav;}
	/*****************************/
	public String getGroupId() {return this.gav.getGroupId();}
	public void setGroupId(String groupId) {this.gav.setGroupId(groupId);}
	/*****************************/
	public String getArtefactId() {return this.gav.getArtefactId();}
	public void setArtefactId(String artefactId) {this.gav.setArtefactId(artefactId);}
	/*****************************/
	public String getVersionId() {return this.gav.getVersionId();}
	public void setVersionId(String versionId) {this.gav.setVersionId(versionId);}
	/*****************************/
	Map<String,Base> bases = new HashMap<String,Base>(); 		// bases organized with their names  
	public Map<String, Base> getBases() {return this.bases;}
	public void setBases(Map<String, Base> bases) {this.bases = bases;}
	/*****************************/
	Set<RuleArtefact> artefacts = new HashSet<RuleArtefact>(); 	// rules that are contained in this module
	public Set<RuleArtefact> getArtefacts() {return this.artefacts;}
	public void setArtefacts(Set<RuleArtefact> artefacts) {this.artefacts = artefacts;}
	/*****************************/

	public String getName(){
		return getGroupId()+"."+getArtefactId()+"-"+getVersionId();
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Module [");
		if (gav != null) {
			builder.append("gav=");
			builder.append(gav);
			builder.append(", ");
		}
		if (bases != null) {
			builder.append("bases=");
			builder.append(bases);
		}
		builder.append("]");
		return builder.toString();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gav == null) ? 0 : gav.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Module)) {
			return false;
		}
		Module other = (Module) obj;
		if (gav == null) {
			if (other.gav != null) {
				return false;
			}
		} else if (!gav.equals(other.gav)) {
			return false;
		}
		return true;
	}
}
