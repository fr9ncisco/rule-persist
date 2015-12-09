package com.acme.module.model;

import java.io.Serializable;


public class Session implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6708005653429946130L;
	/*****************************/
	String name = null;
	public String getName() {return this.name;}
	public void setName(String name) {this.name = name;}
	/*****************************/
	SessionType type = SessionType.Stateless; // by default
	public SessionType getType() {return this.type;}
	public void setType(SessionType type) {this.type = type;}
	/*****************************/
	SessionClock clock = null;
	public SessionClock getClock() {return this.clock;}
	public void setClock(SessionClock clock) {this.clock = clock;}
	/*****************************/
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Session [");
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
		if (clock != null) {
			builder.append("clock=");
			builder.append(clock);
		}
		builder.append("]");
		return builder.toString();
	}
}
