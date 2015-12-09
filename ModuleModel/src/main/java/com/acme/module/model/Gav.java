package com.acme.module.model;

import java.io.Serializable;

public class Gav implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4911126475986599159L;
	String groupId;
	String artefactId;
	String versionId;

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtefactId() {
		return artefactId;
	}

	public void setArtefactId(String artefactId) {
		this.artefactId = artefactId;
	}

	public String getVersionId() {
		return versionId;
	}

	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}

	public Gav(String groupId, String artefactId, String versionId) {
		this();
		if(groupId == null|| artefactId == null ||versionId== null||"".equals(groupId)||"".equals(artefactId)||"".equals(versionId))
			throw new IllegalArgumentException("groupId, artefactId and versionId cannot be null or empty");
		
		this.groupId = groupId;
		this.artefactId = artefactId;
		this.versionId = versionId;
	}
	
	private Gav() {
	}

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
		if (!(obj instanceof Gav)) {
			return false;
		}
		Gav other = (Gav) obj;
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
}
