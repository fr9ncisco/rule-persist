package com.acme.module.db.model;

import java.beans.ConstructorProperties;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * This class holds GAV information and serve a PK for {@link PersistedModule} objects.<br>
 * This models a OneToOne relation with {@link PersistedModule}.<br>
 * */

@Embeddable
public class PersistedModuleId implements Serializable{

	private static final long serialVersionUID = 3326123999325198170L;
	
	/*****************************/
	@Column(name="groupId", nullable=false, length = 64 )
	private String groupId;		// groupid maven style
	public String getGroupId() {return groupId;}
	public void setGroupId(String groupId) {this.groupId = groupId;}
	/*****************************/
	@Column(name="artefactId", nullable=false, length = 64)
	private String artefactId; 	// artefact id maven style
	public String getArtefactId() {return artefactId;}
	public void setArtefactId(String artefactId) {this.artefactId = artefactId;}
	/*****************************/
	@Column(name="versionId", nullable=false, length = 64)
	private String versionId; 	// version ID maven style
	public String getVersionId() {return versionId;}
	public void setVersionId(String versionId) {this.versionId = versionId;}
	/*****************************/
	
	public PersistedModuleId(){}
	
	@ConstructorProperties({"groupId", "artefactId", "versionId"})
	public PersistedModuleId(String groupId,String artefactId,String versionId){
		this.groupId = groupId;
		this.artefactId = artefactId;
		this.versionId = versionId;
	}
	/*****************************/

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((artefactId == null) ? 0 : artefactId.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result
				+ ((versionId == null) ? 0 : versionId.hashCode());
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
		if (!(obj instanceof PersistedModuleId)) {
			return false;
		}
		PersistedModuleId other = (PersistedModuleId) obj;
		if (artefactId == null) {
			if (other.artefactId != null) {
				return false;
			}
		} else if (!artefactId.equals(other.artefactId)) {
			return false;
		}
		if (groupId == null) {
			if (other.groupId != null) {
				return false;
			}
		} else if (!groupId.equals(other.groupId)) {
			return false;
		}
		if (versionId == null) {
			if (other.versionId != null) {
				return false;
			}
		} else if (!versionId.equals(other.versionId)) {
			return false;
		}
		return true;
	}
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		if (groupId != null) {
			builder.append("groupId=");
			builder.append(groupId);
			builder.append(", ");
		}
		if (artefactId != null) {
			builder.append("artefactId=");
			builder.append(artefactId);
			builder.append(", ");
		}
		if (versionId != null) {
			builder.append("versionId=");
			builder.append(versionId);
		}
		builder.append("]");
		return builder.toString();
	}

}
