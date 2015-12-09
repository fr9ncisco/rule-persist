package com.acme.module.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Base implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5867353018121395430L;
	/*****************************/
	private String name = null;
	public String getName() {return this.name;}
	public void setName(String name) {this.name = name;}
	/*****************************/
	Session session = null;
	public Session getSession() {return this.session;}
	public void setSession(Session session) {this.session = session;}
	/*****************************/
	List<String>packages  = new  ArrayList<String>();
	public List<String> getPackages() {return this.packages;}
	public void setPackages(List<String> packages) {this.packages = packages;}
	/*****************************/
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Base [");
		if (session != null) {
			builder.append("session=");
			builder.append(session);
			builder.append(", ");
		}
		if (packages != null) {
			builder.append("packages=");
			builder.append(packages);
		}
		builder.append("]");
		return builder.toString();
	}
}
