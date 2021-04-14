package com.yanan.framework.jdb.exception;

import com.yanan.framework.transaction.exception.TransactionsException;

public class SqlExecuteException extends TransactionsException {
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