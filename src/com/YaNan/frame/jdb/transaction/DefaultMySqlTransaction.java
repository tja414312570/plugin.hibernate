package com.YaNan.frame.jdb.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.plugin.ProxyModel;
import com.YaNan.frame.plugin.annotations.Register;
import com.YaNan.frame.transaction.AbstractTransaction;
import com.YaNan.frame.transaction.TransactionDefined;
import com.YaNan.frame.transaction.exception.TransactionExecuteException;

/**
 * Mysql事物实现
 * @author yanan
 *
 */
@Register(register=AbstractTransaction.class,model=ProxyModel.CGLIB,priority=-1)
public class DefaultMySqlTransaction extends AbstractTransaction{
	public DefaultMySqlTransaction(TransactionDefined transactionDefined) {
		super(transactionDefined);
	}

	/**
	 * 链接列表
	 */
	private List<Connection> connectionList = new ArrayList<>();
	/**
	 * 日志
	 */
	private Logger logger = LoggerFactory.getLogger(DefaultMySqlTransaction.class);
	
	@Override
	public void commit() {
		logger.debug("(^_^)commit transaction : "+this);
		logger.debug("manager connection's : "+connectionList);
		for(Connection connection : connectionList) {
			try {
				connection.commit();
				connection.setAutoCommit(true);
				connection.close();
			} catch (SQLException e) {
				logger.error("failed to commit transaction",e);
				throw new TransactionExecuteException(e);
			}
		}
	}
	@Override
	public void rollback() {
		logger.debug("(@_@)rollback transaction : "+this);
		logger.debug("manager connection's: "+connectionList);
		for(Connection connection : connectionList) {
			try {
				connection.rollback();
				connection.setAutoCommit(true);
				connection.close();
			} catch (SQLException e) {
				logger.error("failed to rollback transaction",e);
				throw new TransactionExecuteException(e);
			}
		}
	}
	@Override
	public void manager(Object managerObject) {
		Connection connection = (Connection) managerObject;
		logger.debug("transaction "+this+" add connection manager "+connection);
		try {
			connection.setAutoCommit(false);
			switch(transactionDefined.getTransactionLevel()) {
			case TRANSACTION_DEFAULT:
				break;
			case TRANSACTION_READ_COMMITTED:
				connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
				break;
			case TRANSACTION_READ_UNCOMMITTED:
				connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
				break;
			case TRANSACTION_REPEATABLE_READ:
				connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
				break;
			case TRANSACTION_SERIALIZABLE:
				connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
				break;
			default:
				break;
			}
			connectionList.add(connection);
		} catch (SQLException e) {
			logger.error("failed to manager sql connection",e);
		}
	}

}
