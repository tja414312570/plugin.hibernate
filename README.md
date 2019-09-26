# plugin.hibernate 数据库组件
特点
* 对象化数据库编程（Query，Update，Insert，Delete等）
* 事务支持
* 类mybatis的xml方式的sql
* 内置连接池管理
* 支持sqlsession调用
* 支持接口调用方式
* 多参数支持（参数默认按配置文件中参数循序）
* ORM支持
# 2019-09-26:
* 修改包结构
* 新增注解事物支持 @Transactions
```java
package com.YaNan.frame.hibernate.database.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.SQLException;

/**
 * 事物注解
 * @author yanan
 *
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactions {
	/**
	 * 回滚异常
	 * @return
	 */
	Class<?>[] value() default {SQLException.class};
	
	/**
	 * 事物传播行为
	 */
	TransactionPropagion propagion() default TransactionPropagion.PROPAGATION_REQUIRED;
	
	/**
	 * 事物隔离级别
	 */
	TransactionIsolocation isolocation() default TransactionIsolocation.TRANSACTION_DEFAULT;
}
```
* 可扩展的事物实现，已mysql为例
```java
package com.YaNan.frame.hibernate.database.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.hibernate.database.transaction.exception.TransactionExecuteException;
import com.YaNan.frame.plugin.ProxyModel;
import com.YaNan.frame.plugin.annotations.Register;

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
```
* 抽象事物隔离级别
```java
package com.YaNan.frame.hibernate.database.transaction;

/**
 * 事物隔离级别
 * @author yanan
 *
 */
public enum TransactionIsolocation {
	/**
	 * 使用默认事物
	 */
	TRANSACTION_DEFAULT,
	/**
	 * 未提交读 :脏读，不可重复读，虚读都有可能发生 
	 */
	TRANSACTION_READ_UNCOMMITTED,
	/**
	 * 已提交读 :避免脏读。但是不可重复读和虚读有可能发生 
	 */
	TRANSACTION_READ_COMMITTED,
	/**
	 * 可重复读 :避免脏读和不可重复读.但是虚读有可能发生. 
	 */
	TRANSACTION_REPEATABLE_READ,
	/**
	 * 序列化 :避免以上所有读问题.
	 */
	TRANSACTION_SERIALIZABLE;
}
```
* 抽象事物传播行为
```java
package com.YaNan.frame.hibernate.database.transaction;

/**
 * 事物传播行为
 * @author yanan
 *
 */
public enum TransactionPropagion {
	//* 保证在同一个事务中 
	/**
	 * 支持当前事务，如果不存在 就新建一个(默认) 
	 */
	PROPAGATION_REQUIRED,
	/**
	 * 支持当前事务，如果不存在，就不使用事务 
	 */
	PROPAGATION_SUPPORTS,
	/**
	 *支持当前事务，如果不存在，抛出异常 
	 */
	PROPAGATION_MANDATORY,
	//* 保证没有在同一个事务中 
	/**
	 * 如果有事务存在，挂起当前事务，创建一个新的事务 
	 */
	PROPAGATION_REQUIRES,
	/**
	 * 以非事务方式运行，如果有事务存在，挂起当前事务 
	 */
	PROPAGATION_NOT_SUPPORTED,
	/**
	 * 以非事务方式运行，如果有事务存在，抛出异常 
	 */
	PROPAGATION_NEVER,
	/**
	 * 如果当前事务存在，则嵌套事务执行
	 */
	PROPAGATION_NESTED,
}
```
* 简单测试结果
```java
public static void main(String[] args) throws SQLException {
		System.out.println(new File("/Volumes/GENERAL/git/plugin.hibernate/target/test-classes/hibernate.xml").exists());
		DBFactory.getDBFactory().init();
		Test s = PlugsFactory.getPlugsInstance(Test.class);
		System.out.println(s);
		s.testTransaction();
	}
  import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.YaNan.frame.hibernate.database.SqlSession;
import com.YaNan.frame.hibernate.database.transaction.TransactionIsolocation;
import com.YaNan.frame.hibernate.database.transaction.TransactionPropagion;
import com.YaNan.frame.hibernate.database.transaction.Transactions;
import com.YaNan.frame.plugin.annotations.Service;

public class Test {
	
	@Service
	private SqlSession sqlSession;
	
	@Transactions(propagion = TransactionPropagion.PROPAGATION_NESTED,isolocation = TransactionIsolocation.TRANSACTION_READ_UNCOMMITTED)
	public int testInnerTransaction() throws SQLException {
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		Map<String,String> u = new HashMap<String,String>();
		u.put("id", "1025");
		u.put("name", "1025name");
		list.add(u);
		testInnerTransaction3();
		testInnerTransaction2();
		return sqlSession.insert("com.YaNan.debug.test.insert2", list);
	}

	@Transactions(isolocation = TransactionIsolocation.TRANSACTION_READ_UNCOMMITTED)
	public int testInnerTransaction3() throws SQLException {
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		Map<String,String> u = new HashMap<String,String>();
		u.put("id", "1025");
		u.put("name", "1025name");
		list.add(u);
		return sqlSession.insert("com.YaNan.debug.test.insert", list);
	}

	@Transactions(propagion = TransactionPropagion.PROPAGATION_REQUIRES,isolocation = TransactionIsolocation.TRANSACTION_READ_UNCOMMITTED)
	public int testInnerTransaction2() throws SQLException {
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		Map<String,String> u = new HashMap<String,String>();
		u.put("id", "1025");
		u.put("name", "1025name");
		list.add(u);
		return sqlSession.insert("com.YaNan.debug.test.insert2", list);
	}
	@Transactions(isolocation = TransactionIsolocation.TRANSACTION_READ_COMMITTED)
	public int testTransaction() throws SQLException {
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		Map<String,String> u = new HashMap<String,String>();
		u.put("id", "1025");
		u.put("name", "1025name");
		list.add(u);
		sqlSession.insert("com.YaNan.debug.test.insert", list);
		return this.testInnerTransaction();
	}
}
```
结果
```java
[2019-09-26 11:28:34] [DEBUG] [TransactionMethodHandler:36] : create transaction at execute method [public int Test.testTransaction() throws java.sql.SQLException] transaction [com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@3cd3e762]
[2019-09-26 11:28:34] [DEBUG] [DefaultMySqlTransaction:64] : transaction com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@3cd3e762 add connection manager com.mysql.jdbc.JDBC4Connection@72e5a8e
[2019-09-26 11:28:34] [DEBUG] [PreparedSql:126] : prepared sql:insert into test ( id , name ) values  ( ? , ? ) 
[2019-09-26 11:28:34] [DEBUG] [PreparedSql:127] : prepared parameter:[1025, 1025name]
[2019-09-26 11:28:34] [DEBUG] [PreparedSql:141] : execute result:false,generatedKey:1025
[2019-09-26 11:28:34] [DEBUG] [TransactionMethodHandler:36] : create transaction at execute method [public int Test.testInnerTransaction() throws java.sql.SQLException] transaction [com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@272113c4]
[2019-09-26 11:28:34] [DEBUG] [TransactionMethodHandler:36] : create transaction at execute method [public int Test.testInnerTransaction3() throws java.sql.SQLException] transaction [com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@272113c4]
[2019-09-26 11:28:34] [DEBUG] [DefaultMySqlTransaction:64] : transaction com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@272113c4 add connection manager com.mysql.jdbc.JDBC4Connection@2d52216b
[2019-09-26 11:28:34] [DEBUG] [PreparedSql:126] : prepared sql:insert into test ( id , name ) values  ( ? , ? ) 
[2019-09-26 11:28:34] [DEBUG] [PreparedSql:127] : prepared parameter:[1025, 1025name]
[2019-09-26 11:28:34] [DEBUG] [PreparedSql:141] : execute result:false,generatedKey:1025
[2019-09-26 11:28:34] [DEBUG] [TransactionMethodHandler:41] : transaction execute completed at method [testInnerTransaction3] transaction [com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@272113c4]
[2019-09-26 11:28:34] [DEBUG] [TransactionMethodHandler:36] : create transaction at execute method [public int Test.testInnerTransaction2() throws java.sql.SQLException] transaction [com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@534a5a98]
[2019-09-26 11:28:34] [DEBUG] [DefaultMySqlTransaction:64] : transaction com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@534a5a98 add connection manager com.mysql.jdbc.JDBC4Connection@72e5a8e
[2019-09-26 11:28:34] [DEBUG] [PreparedSql:126] : prepared sql:insert into test2 ( id , name ) values  ( ? , ? ) 
[2019-09-26 11:28:34] [DEBUG] [PreparedSql:127] : prepared parameter:[1025, 1025name]
[2019-09-26 11:28:34] [DEBUG] [TransactionMethodHandler:61] : transaction execute exception when execute [testInnerTransaction2] transaction [com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@534a5a98]
[2019-09-26 11:28:34] [DEBUG] [TransactionMethodHandler:67] : transaction rollback when execute [testInnerTransaction2] transaction [com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@534a5a98]
[2019-09-26 11:28:34] [DEBUG] [DefaultMySqlTransaction:50] : (@_@)rollback transaction : com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@534a5a98
[2019-09-26 11:28:34] [DEBUG] [DefaultMySqlTransaction:51] : manager connection's: [com.mysql.jdbc.JDBC4Connection@72e5a8e]
[2019-09-26 11:28:34] [DEBUG] [DefaultMySqlTransaction:64] : transaction com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@272113c4 add connection manager com.mysql.jdbc.JDBC4Connection@2d52216b
[2019-09-26 11:28:34] [DEBUG] [PreparedSql:126] : prepared sql:insert into test2 ( id , name ) values  ( ? , ? ) 
[2019-09-26 11:28:34] [DEBUG] [PreparedSql:127] : prepared parameter:[1025, 1025name]
[2019-09-26 11:28:34] [DEBUG] [TransactionMethodHandler:61] : transaction execute exception when execute [testInnerTransaction] transaction [com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@272113c4]
[2019-09-26 11:28:34] [DEBUG] [TransactionMethodHandler:67] : transaction rollback when execute [testInnerTransaction] transaction [com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@272113c4]
[2019-09-26 11:28:34] [DEBUG] [TransactionMethodHandler:61] : transaction execute exception when execute [testTransaction] transaction [com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@3cd3e762]
[2019-09-26 11:28:34] [DEBUG] [TransactionMethodHandler:67] : transaction rollback when execute [testTransaction] transaction [com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@3cd3e762]
[2019-09-26 11:28:34] [DEBUG] [DefaultMySqlTransaction:50] : (@_@)rollback transaction : com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@3cd3e762
[2019-09-26 11:28:34] [DEBUG] [DefaultMySqlTransaction:51] : manager connection's: [com.mysql.jdbc.JDBC4Connection@72e5a8e]
[2019-09-26 11:28:34] [DEBUG] [DefaultMySqlTransaction:50] : (@_@)rollback transaction : com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@272113c4
[2019-09-26 11:28:34] [DEBUG] [DefaultMySqlTransaction:51] : manager connection's: [com.mysql.jdbc.JDBC4Connection@2d52216b, com.mysql.jdbc.JDBC4Connection@2d52216b]
[2019-09-26 11:28:34] [DEBUG] [TransactionManager:132] : all transaction completed at transaction com.YaNan.frame.hibernate.database.transaction.DefaultMySqlTransaction$$EnhancerByCGLIB$$c90888f9@3cd3e762
Exception in thread "main" com.YaNan.frame.hibernate.database.exception.SqlExecuteException: faild to execute query "com.YaNan.debug.test.insert2"
Caused by: com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException: Duplicate entry '1025' for key 'PRIMARY'
```
