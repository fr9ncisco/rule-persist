package com.acme.module.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;


public class ModuleException extends RuntimeException {
	
	private static final long serialVersionUID = 3964210342581425162L;
	
	public static final char[] NO_CHAR = new char[0];
	public static final String EMPTY_STRING = new String(NO_CHAR);
	
	private String stackTrace;
	private Throwable throwable;

	public ModuleException() {
	}

	public ModuleException(String message) {
		super(message);
		this.stackTrace = message;
	}

	public ModuleException(String message, Throwable cause) {
		super(message + "; nested exception is " + cause.getMessage());
		this.throwable = cause;
		StringWriter out = new StringWriter();
		cause.printStackTrace(new PrintWriter(out));
		this.stackTrace = out.toString();
	}

	public String getMessage(){
		return super.getMessage()==null?EMPTY_STRING:super.getMessage();
	}
	
	/**
	 * Return the root cause of this exception.
	 * May be null
	 * @return Throwable
	 */
	public Throwable getCause() {
		return throwable;
	}

	public String toString() {
		return this.getMessage();
	}

	public void printStackTrace() {
		System.err.print(this.stackTrace);
	}

	public void printStackTrace(PrintStream out) {
		printStackTrace(new PrintWriter(out));
	}

	public void printStackTrace(PrintWriter out) {
		out.print(this.stackTrace);
	}
}
