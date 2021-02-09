# plugin.hibernate 数据库组件
特点
* 对象化数据库编程（Query，Update，Insert，Delete等）
* 事务支持
* Mapper和Wrapper支持
* 内置连接池管理
* 支持sqlsession
* 支持接口调用方式
* 多参数支持（SqlSession参数默认按配置文件中参数循序，接口中可以使用@Param指定参数名）
* ORM支持
* 提供一个默认的MySql的事物实现
* 架构本身对[Plugin.Core](https://github.com/tja414312570/plugin.core)依赖，架构内部高度解耦
# 2021-02-09:
* 修复fragment builder ，当某个标签对应的fragmentSet不存在时，默认指向FragmentSet导致的问题
* 将标签支持扩展为可注册标签支持，终于可以不用改框架就扩展标签了
* 新增 case-when-default标签支持
# 2019-09-26:
* 修改包结构
* 新增注解事物支持 @Transactions
# 2019-10-16:
* 独立出数据源模块和事物模块，使得架构耦合度更低
* 重新设计架构依赖关系，支持多数据源，多上下文。
* 接口映射文件中新增@Param注解，用于指定接口的参数名
* 当前xml中支持的标签（select,insert,sql,update,delete,if,trim,foreach,include)
# 2019-10-21:
* 放弃了当初的可变参数的传参方式，虽然可变参数处理逻辑更复杂，但不可控bug永远存在，当前FragmentSet中仅能使用单个参数，虽然接口中同样提供可变参数，但会被组装成Map参数，其中直接调用SqlSession的传参时多个参数(Pojo类型除外)封装成Parameter_(n)的形式，Mapper接口如果没有@Param注解时根据Parameter.getName()作为Map的Key
# 2019-10-22:
* includ标签的子包含sql支持引用父类的sql,当include中的id没有指定命名空间时，搜索空间循序为当前mapper>父类mapper;
* 新增mapper文件类型为ref（wrapper标签属性ref=true)，被修饰的mapper文件中的sql将不会寻址include标签，可以把sql看做赋值，include为使用值
# 2020-05-11:
* 新增var和val两个注解，var用以定义变量，val用以使用变量,要使用的变量必须在val之前定义，同时var的作用域仅为当前的SqlFragment上下文;与include和sql不同，var和val将更灵活，也更安全
* 修改读取xml的使用方法和部分解析逻辑
* when-case-if
```xml
<wrapper namespace="testSql" database="YaNan_Demo">
	<sql id="tablename">student2</sql>
	<select id="case" >
		<case>
	        <when test="uname!=null and uname!=''">
	            and uname like concat('%',#{uname},'%')
	        </when>
			<!-- 当 break 为 true时，条件满足不会忘后面执行，默认为 false -->
	        <when test="usex!=null and usex!=''" break = "true">
	            and usex=#{usex}
	        </when>
	        <default>
	            and uid  <![CDATA[>]]>  10
	        </default>
	    </case>
	</select>
</wrapper>
```
```java
Map<String,String> params = new HashMap<String,String>();
		params.put("uname", "test username");
		params.put("usex", "test usex");
SqlFragment sqlFragment = sqlFragmentManager.getSqlFragment("testSql.case");
PreparedSql preparedSql = sqlFragment.getPreparedSql(params);
System.out.println(preparedSql.getSql());
System.out.println(preparedSql.getParameter());


-----log-----
and uname like concat('%',?,'%') and usex=?
[test username, test usex]
```

* 变量支持var与val
```xml
<?xml version="1.0" encoding="UTF-8"?>
<wrapper namespace="TestSql">

	<sql id="generalQuery">
		select * from <val id="tableName"/>
	</sql>
	<select id="test" resultType="map">
		<var id="tableName">demo</var>
		<include id="generalQuery"/>
	</select>
	<select id="test1" resultType="map">
		<var id="tableName">student
			<if test = 'name!=null'>name like '%'||#{name}||'%'</if>
		</var>
		<include id="generalQuery"/>
	</select>
</wrapper>
```
```java
PreparedSql [sql=select * from student where name like '%'||?||'%', parameter=[1024], sqlFragment=com.YaNan.frame.jdb.fragment.SelectorFragment$$EnhancerByCGLIB$$bafd3d4e@81d9a72]
PreparedSql [sql=select * from demo, parameter=[], sqlFragment=com.YaNan.frame.jdb.fragment.SelectorFragment$$EnhancerByCGLIB$$bafd3d4e@747f281]
```
* 父子引用 include标签
```xml
child.xml
<wrapper namespace="child" ref="true">
	<sql id="pages">
		rowid in (
		select rid from  (
		select rownum rn,rid from (
		select rowid rid,cid from
		<!--此处引用父类定义的tablename，即parent.tablename-->
		<include id="tablename"></include>
		order by cid desc)
		where rownum <![CDATA[ < ]]> #{RowBounds.limit})
		where rn <![CDATA[ > ]]> #{RowBounds.offset})
	</sql>
</wrapper>	
parent.xml
<wrapper namespace="parent">
	<sql id="tablename">student</sql>
	<select id="test" resultType="map">
		SELECT * FROM yanan_account.test where
		<include id="child.pages"></include>
	</select>
</wrapper>
```
![avatar](https://ufomedia.oss-cn-beijing.aliyuncs.com/WX20191014-173332.png)
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
