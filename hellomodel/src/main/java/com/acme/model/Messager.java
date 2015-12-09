package com.acme.model;


/**
 *
 * This is a business model sample class....
 *
 */
public class Messager {
	public static final int HELLO = 0;
	public static final int GOODBYE = 1;
	private String message;
	private int status;

	public String getMessage() {
		return this.message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getStatus() {
		return this.status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Messager [");
		if (message != null) {
			builder.append("message=");
			builder.append(message);
			builder.append(", ");
		}
		builder.append("status=");
		builder.append(status);
		builder.append("]");
		return builder.toString();
	}
}

