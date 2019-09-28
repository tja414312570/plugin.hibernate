package com.YaNan.frame.jdb.database.dataresource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库连接池，用于管理数据库连接，提供数据库连接的初始化、获取、释放</br>
 * 注意！！数据库连接获取并使用之后，记得关闭PreparedStatement和ResultSet，否则导致连接池刷新逻辑失效，ProxyConnection不需要关闭</br>
 * 注意！！数据库使用完毕后，记得关闭ProxyConnectionPoolRefreshService服务，否则导致ProxyConnectionPoolRefreshService服务延迟关闭</br>
 * 具体关闭时间和设置的连接池空闲时等待时间有关（参数timeout）</br>
 * 通过ProxyConnectionPoolRefreshService.destory()或DBFactory.getDBFactory().destory()可销毁该线程</br>
 * 2018-6-20~2018-6-21 把连接池刷新从释放中独立出来，新添加一个连接池刷新逻辑服务，该服务使得连接刷新</br>
 * 从连接释放中独立出来，提高数据吞吐能力，同时该服务会自动启动和销毁。</br>
 * 2018-6-18 ~ 2018-6-20 优化连接池刷新、释放、获取、添加逻辑，降低各种锁的粒度，提高运行速度，降低运行内存，</br>
 * 2016-?-？~ 2018-6-17 添加数据库连接池用于管理所有DataBase中的数据连接</br>
 * 20190103 修改连接池管理进程为守护线程
 * 
 * @author yanan
 *
 */
public class ConnectionPools implements PooledConnection{
	 private Vector<ProxyConnection> all = null; // 存放连接池中数据库连接的向量 , 初始时为 null 
     private Vector<ProxyConnection> free = null; //空闲的连接
     private Logger log = LoggerFactory.getLogger(ConnectionPools.class);
     private ConcurrentLinkedQueue<Lock> lockList = new ConcurrentLinkedQueue<Lock>();
     public List<Lock> getWaitList() {
		return new ArrayList<Lock>(lockList);
	}
	private DefaultDataSource dataSource;
		public DefaultDataSource getDataSource() {
		return dataSource;
	}
		public ConnectionPools(DefaultDataSource dataSource) {
	    	 this.dataSource = dataSource;
	    	 this.all = new Vector<ProxyConnection>();
	    	 this.free = new Vector<ProxyConnection>();
		}
	private ProxyConnection createConnection() throws SQLException {
		Properties props = new Properties();
        if (dataSource.getDriverProperties() != null) {
        	props.putAll(dataSource.getDriverProperties());
        } 
       Connection connection = DriverManager.getConnection(dataSource.getUrl(),props);
       if(this.dataSource.isTest_connection()) {
    	   if(this.dataSource.getTest_sql() == null) 
    		   throw new NullPointerException("property test_connection = true must set property for test_sql");
    	   try {
    		   PreparedStatement ps = connection.prepareStatement(this.dataSource.getTest_sql());
        	   ResultSet rs = ps.executeQuery();
        	   if(!rs.next())
        		  throw new ConnectionTestException(this.dataSource.getId());
    	   }catch(Throwable t) {
    		   throw new ConnectionTestException(this.dataSource.getId(),t);
    	   }
    	   
       }
       return new ProxyConnection(connection, this);
	}
     public Vector<ProxyConnection> getAllProxyConnections(){
    	 return this.all;
     }
     public Vector<ProxyConnection> getFreeProxyConnections(){
    	 return this.free;
     }
	/**
	 * 初始化连接池
	 * 从DataBase中创建一个ProxyConnection并保存在all与free中
	 * @throws SQLException 
	 */
	synchronized void initial() throws SQLException{
		log.debug("initial connection pools for datasource ["+this.dataSource.getId()+"]");
		this.create(this.dataSource.getMin_connection());
	}
	private synchronized void create(int num) throws SQLException{
		for(int i=0;i< num;i++){
			ProxyConnection connection = createConnection();
			this.all.add(connection);
			this.free.add(connection);
		}
	}
	/**
	 * 自动增加连接
	 * @throws SQLException 
	 */
	private synchronized void increase() throws SQLException{
		if(this.all.size()<this.dataSource.getMax_connection()){
			this.create(this.dataSource.getAdd_connection());
		}
	}
	/**
	 * 关闭连接
	 * 如果空闲连接数量超过初始化数量，则自动关闭多于的连接
	 * 刷新为归还链接时触发
	 */
	public void refresh(){
		if(this.free.size()>this.getDataSource().getMin_connection()){
			synchronized (free) {
				if(this.free.size()>this.getDataSource().getMin_connection()){
					ProxyConnection connection;
					Iterator<ProxyConnection> iterator = this.free.iterator();
					while(iterator.hasNext()){
							try {
								connection = iterator.next();
								if(connection.getAutoCommit()){//找到没有使用的连接关闭
									connection.destory();
									this.all.remove(connection);
									iterator.remove();
								}
								if(this.free.size()<=this.getDataSource().getMin_connection())//当空闲链接为最低链接数时，不在关闭链接
									break;
							} catch (SQLException e) {
								log.error(e.getMessage(),e);
						}
					}
				}
			}
		}
	}
	/**
	 * 获取可用的连接
	 * @return
	 * @throws SQLException 
	 */
	public ProxyConnection getConnection() throws SQLException{
		ProxyConnection connection;
		synchronized (this) {
			if(this.free.size()==0){
					if(this.all.size()>=this.dataSource.getMax_connection()){//如果没有空闲连接，首先判断当前所有的连接数量
							//获取一个锁
						Lock lock = Lock.getLock(Thread.currentThread());
						lockList.add(lock);
						try {
							lock.lock();
						} catch ( Throwable e) {
							new SQLException("connection get exception!", e);
						}finally{
							lockList.remove(lock);
						}
						return this.getConnection();
					}else{
						this.increase();
					}
			}//存在空闲连接
			connection = this.free.get(0);
			this.free.remove(0);
		}
		return connection;
	}
	/**
	 * 归还连接
	 */
	public void release(ProxyConnection connection){
		if(connection!=null){
			synchronized (free) {
				this.free.add(connection);
				if(!lockList.isEmpty()) {
					lockList.poll().unLock();
				}
			}
		}
	}
	/**
	 * 关闭连接池
	 * @return
	 */
	public void destory(){
		//清空free
		if(this.free!=null){
			this.free.clear();
			this.free = null;
		}
		//关闭所有连接
		if(this.all!=null){
			Enumeration<ProxyConnection> elements = this.all.elements();
			while(elements.hasMoreElements()){
				try {
					elements.nextElement().destory();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			this.all.clear();
			this.all = null;
			JdbConnectionPoolsManger.getJdbConnectionPools(dataSource);
		}
	}
	public int getFreeNum() {
		return this.free.size();
	}
	public int getAllNum(){
		return this.all.size();
	}
	
	@Override
	public void close() throws SQLException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void addConnectionEventListener(ConnectionEventListener listener) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void removeConnectionEventListener(ConnectionEventListener listener) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void addStatementEventListener(StatementEventListener listener) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void removeStatementEventListener(StatementEventListener listener) {
		// TODO Auto-generated method stub
		
	}

}