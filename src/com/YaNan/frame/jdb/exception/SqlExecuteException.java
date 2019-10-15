package com.YaNan.frame.jdb.exception;

import java.sql.SQLException;

public class SqlExecuteException extends RuntimeException {
	public SqlExecuteException(String msg, SQLException e) {
		super(msg,e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -687270719454286150L;

}
