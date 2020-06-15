package com.YaNan.frame.jdb;

public class SqlExecution extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1405284856755021367L;

	public SqlExecution(Throwable t) {
		super(t);
	}
	public SqlExecution(String message,Throwable t) {
		super(message,t);
	}
	public SqlExecution(String message) {
		super(message);
	}
}
