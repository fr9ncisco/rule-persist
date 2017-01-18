package com.acme.module.db.model;

import java.beans.ConstructorProperties;
import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.acme.module.model.Gav;

import static javax.persistence.FetchType.EAGER;

/**
 * 
 * A Persisted Module holds the version of the module,<br>
 * as well as the binary content of the module.<br>
 * 
 *   Persisted means that it is stored in a DB<br>
 *   , with Primary key made with GAV information<br>
 *   that is stored in {@link PersistedModuleId}<br>
 * 
 *
 */
@Entity
@Table(name = "T_ARCHIVE_MODULE" )
public class PersistedModule implements Serializable{
	
	private static final long serialVersionUID = 2266250896585158884L;
	/*****************************/
	@EmbeddedId
	private PersistedModuleId persistedModuleId;
	/*****************************/
	@Lob
	@Basic(fetch = EAGER)
	@Column(name = "content", columnDefinition = "LOB NOT NULL")
	private byte[] content = null;
	
	public PersistedModule(){}

	@ConstructorProperties({"groupId", "artefactId", "versionId"})
	public PersistedModule(String groupId, String artefactId, String versionId){
		this.persistedModuleId = new PersistedModuleId(groupId, artefactId, versionId);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		if (persistedModuleId != null) {
			builder.append(persistedModuleId);
		}
		builder.append("]");
		return builder.toString();
	}

	public PersistedModuleId getPersistedModuleId() {return persistedModuleId;}

	public void setPersistedModuleId(PersistedModuleId persistedModuleId) {this.persistedModuleId = persistedModuleId;}
	
	public byte[] getContent() {return this.content;}
	public void setContent(byte[] content) {this.content = content;}

	/*****************************/
	public Gav toGav(){
		return new Gav(persistedModuleId.getGroupId(),persistedModuleId.getArtefactId(),persistedModuleId.getVersionId());
	} 
	 
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((persistedModuleId == null) ? 0 : persistedModuleId
						.hashCode());
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
		if (!(obj instanceof PersistedModule)) {
			return false;
		}
		PersistedModule other = (PersistedModule) obj;
		if (persistedModuleId == null) {
			if (other.persistedModuleId != null) {
				return false;
			}
		} else if (!persistedModuleId.equals(other.persistedModuleId)) {
			return false;
		}
		return true;
	}
}
