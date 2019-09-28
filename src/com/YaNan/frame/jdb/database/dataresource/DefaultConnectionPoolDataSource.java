package com.YaNan.frame.jdb.database.dataresource;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

public class DefaultConnectionPoolDataSource implements ConnectionPoolDataSource{
	/**
	 * 数据库属性
	 */
	private Properties driverProperties;
	/**
	 * 数据库地址
	 */
	private String url;

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	@Override
	public PooledConnection getPooledConnection() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PooledConnection getPooledConnection(String user, String password) throws SQLException {
		return null;
	}


}
