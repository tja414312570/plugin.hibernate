package com.yanan.frame.jdb;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlExecutor {
	private static final Logger log = LoggerFactory.getLogger( SqlExecutor.class);
	static interface SqlExecutorFunction<T>{
		T execute(Connection connection) throws Exception ;
	}
	public static <T> T execute(SqlExecutorFunction<T> function,DataTable dataTable,String sql,Object parameter){
		if (dataTable.getDataSource() == null)
			throw new RuntimeException("DataTable mapping class " + dataTable.getDataTablesClass().getName()
					+ " datasource is null,try to configure the @Tab attribute dataSource ");
		Connection connection = null;
		try {
			log.debug("prepared to execute sql:" + sql 
					+ (parameter == null?"":"\r\n"+"parameter:" + parameter));
			connection = dataTable.getDataSource().getConnection();
			return function.execute(connection);
		}catch(Exception e){
			log.error("failed to execute sql:" + sql 
					+ (parameter == null?"":"\r\n"+"parameter:" + parameter)
					, e);
			throw new SqlExecution(e);
		}finally {
			try {
				if(connection != null)
					connection.close();
			} catch (SQLException e) {
				log.error("failed to close connection after execute sql:" +sql);
				throw new SqlExecution(e);
			}
		}
	}
	public static <T> T executeNoThrow(SqlExecutorFunction<T> function,DataTable dataTable,String sql,Object parameter,T defaultReturn) {
		try {
			return SqlExecutor.execute(function,dataTable,sql,parameter);
		} catch (Throwable e) {
			
		}
		return defaultReturn;
	}

}