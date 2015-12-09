/**
 * 
 */
package com.acme.module.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author fgadrat010515
 *
 */
public class RuleArtefact implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2718314529244277606L;
	/*****************************/
	String nameSpace = null;
	public String getNameSpace() {return this.nameSpace;}
	public void setNameSpace(String nameSpace) {this.nameSpace = nameSpace;}
	/*****************************/
	String name = null;
	public String getName() {return this.name;}
	public void setName(String name) {this.name = name;}
	
	/*****************************/
	RuleArtefactType type = null;
	public RuleArtefactType getType() {return this.type;}
	public void setType(RuleArtefactType type) {this.type = type;}
	/*****************************/
	byte[] content = null;
	public byte[] getContent() {return this.content;}
	public void setContent(byte[] content) {this.content = content;}
	/*****************************/
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RuleArtefact [");
		if (nameSpace != null) {
			builder.append("nameSpace=");
			builder.append(nameSpace);
			builder.append(", ");
		}
		if (name != null) {
			builder.append("name=");
			builder.append(name);
			builder.append(", ");
		}
		if (type != null) {
			builder.append("type=");
			builder.append(type);
			builder.append(", ");
		}
		if (content != null) {
			builder.append("content=");
			builder.append(Arrays.toString(content));
		}
		builder.append("]");
		return builder.toString();
	}
}
