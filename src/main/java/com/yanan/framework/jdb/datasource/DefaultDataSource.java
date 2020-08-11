package com.yanan.framework.jdb.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;

import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.autowired.property.Property;

/**
 * 默认数据连接池
 * @author yanan
 *
 */
@Register(afterInstance="init",register=DataSource.class)
public class DefaultDataSource implements DataSource{
	static org.slf4j.Logger logger = LoggerFactory.getLogger(DefaultDataSource.class);
	ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<Connection>();
	/**
	 * 数据库属性
	 */
	private Properties driverProperties;
	private ConnectionPools connectionPools;
	//数据源ID
	@Property("jdb.id")
	private String id;
	//基本数据
	@Property("jdb.url")
	private String url;
	@Property("jdb.username")
	private String username;
	@Property("jdb.password")
	private String password;
	@Property("jdb.driver")
	private String driver;
	//框架本身属性
	@Property("jdb.max_connection")
	private int max_connection = 4;
	@Property(value = "jdb.min_connection")
	private int min_connection = 2;
	@Property("jdb.add_connection")
	private int add_connection = 2;
	@Property("jdb.test_connection")
	private boolean test_connection = false;
	@Property("jdb.test_sql")
	private String test_sql = "";
	@Property("jdb.wait_times")
	private int wait_times;
	@Property("jdb.login_timeout")
	private int login_timeout;
	
	@Override
	public String toString() {
		return "DefaultDataSource [driverProperties=" + driverProperties + ", connectionPools=" + connectionPools
				+ ", id=" + id + ", url=" + url + ", username=" + username + ", password=" + password + ", driver="
				+ driver + ", max_connection=" + max_connection + ", min_connection=" + min_connection
				+ ", add_connection=" + add_connection + ", test_connection=" + test_connection + ", test_sql="
				+ test_sql + ", wait_times=" + wait_times + ", login_timeout=" + login_timeout + "]";
	}
	public DefaultDataSource() {
		connectionPools = JdbConnectionPoolsManger.getJdbConnectionPools(this);
	}
	public void deregistDriver() throws SQLException {
		for (Enumeration<java.sql.Driver> e = DriverManager.getDrivers(); e.hasMoreElements();) {
			Driver driver = (Driver) e.nextElement();
			if (driver.getClass().getClassLoader() == getClass().getClassLoader()) {
				DriverManager.deregisterDriver(driver);
			}
		}
	}
	@Override
	public Connection getConnection() throws SQLException {
		Connection connection = connectionPools.getConnection();
        return connection;
	}
	public void init() throws SQLException, ClassNotFoundException {
		logger.debug("init datasource ["+this.id+"]");
		//写入属性
		if(this.driverProperties == null) {
			driverProperties = new Properties();
			driverProperties.put("user", this.username);
			driverProperties.put("password", this.password);
		}
		logger.debug(this.toString());
		//注册驱动
		Class.forName(this.driver);
		//初始化连接池
		connectionPools.initial();
	}
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		Connection connection = connectionPools.getConnection();
		return connection;
	}
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		this.login_timeout = seconds;
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return login_timeout;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}
	public Properties getDriverProperties() {
		return driverProperties;
	}
	public void setDriverProperties(Properties driverProperties) {
		this.driverProperties = driverProperties;
	}
	public ConnectionPools getConnectionPools() {
		return connectionPools;
	}
	public void setConnectionPools(ConnectionPools connectionPools) {
		this.connectionPools = connectionPools;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
	public int getMax_connection() {
		return max_connection;
	}
	public void setMax_connection(int max_connection) {
		this.max_connection = max_connection;
	}
	public int getMin_connection() {
		return min_connection;
	}
	public void setMin_connection(int min_connection) {
		this.min_connection = min_connection;
	}
	public int getAdd_connection() {
		return add_connection;
	}
	public void setAdd_connection(int add_connection) {
		this.add_connection = add_connection;
	}
	public boolean isTest_connection() {
		return test_connection;
	}
	public void setTest_connection(boolean test_connection) {
		this.test_connection = test_connection;
	}
	public String getTest_sql() {
		return test_sql;
	}
	public void setTest_sql(String test_sql) {
		this.test_sql = test_sql;
	}
	public long getWait_times() {
		return wait_times;
	}
	public void setWait_times(int wait_times) {
		this.wait_times = wait_times;
	}


}