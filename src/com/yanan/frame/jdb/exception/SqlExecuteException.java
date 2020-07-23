package com.yanan.frame.jdb.exception;

public class SqlExecuteException extends RuntimeException {
	public SqlExecuteException(String msg, Throwable e) {
		super(msg,e);
	}
	public SqlExecuteException(String msg) {
		super(msg);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -687270719454286150L;

}