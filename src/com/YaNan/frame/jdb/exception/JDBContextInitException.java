package com.YaNan.frame.jdb.exception;

public class JDBContextInitException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1729117431961497757L;
	
	public JDBContextInitException(String msg){
		super(msg);
	}
	public JDBContextInitException(String msg,Throwable t){
		super(msg,t);
	}
	public JDBContextInitException(Throwable t){
		super(t);
	}
}
