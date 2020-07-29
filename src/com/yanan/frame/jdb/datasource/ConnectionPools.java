package com.yanan.frame.jdb.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库连接池，用于管理数据库连接，提供数据库连接的初始化、获取、释放
 * <p>注意！！数据库使用完毕后，记得关闭ProxyConnectionPoolRefreshService服务，否则导致ProxyConnectionPoolRefreshService服务延迟关闭
 * <p>具体关闭时间和设置的连接池空闲时等待时间有关（参数timeout）
 * <p>通过ProxyConnectionPoolRefreshService.destory()或DBFactory.getDBFactory().destory()可销毁该线程
 * <p>2018-6-20~2018-6-21 把连接池刷新从释放中独立出来，新添加一个连接池刷新逻辑服务，该服务使得连接刷新
 * <p>从连接释放中独立出来，提高数据吞吐能力，同时该服务会自动启动和销毁。
 * <p>2018-6-18 ~ 2018-6-20 优化连接池刷新、释放、获取、添加逻辑，降低各种锁的粒度，提高运行速度，降低运行内存，
 * <p>2016-?-？~ 2018-6-17 添加数据库连接池用于管理所有DataBase中的数据连接
 * <p>20190103 修改连接池管理进程为守护线程
 * 
 * @author yanan
 *
 */
public class ConnectionPools implements PooledConnection{
	 private List<ProxyConnection> all = null; // 存放连接池中数据库连接的向量 , 初始时为 null 
     private Queue<ProxyConnection> free = null; //空闲的连接 队列
     private Logger log = LoggerFactory.getLogger(ConnectionPools.class);
     private ConcurrentLinkedQueue<Lock> lockList = new ConcurrentLinkedQueue<Lock>();
     private ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
     private ReadLock readLock = reentrantReadWriteLock.readLock();
     private WriteLock writeLock = reentrantReadWriteLock.writeLock();
     public List<Lock> getWaitList() {
		return new ArrayList<Lock>(lockList);
	}
	private DefaultDataSource dataSource;
		public DefaultDataSource getDataSource() {
		return dataSource;
	}
		public ConnectionPools(DefaultDataSource dataSource) {
	    	 this.dataSource = dataSource;
	    	 this.all = new ArrayList<ProxyConnection>();
	    	 this.free = new ConcurrentLinkedQueue<ProxyConnection>();
		}
	private ProxyConnection createConnection() throws SQLException {
		Properties props = new Properties();
        if (dataSource.getDriverProperties() != null) {
        	props.putAll(dataSource.getDriverProperties());
        }
       DriverManager.setLoginTimeout(this.dataSource.getLoginTimeout());
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
     public List<ProxyConnection> getAllProxyConnections(){
    	 return this.all;
     }
     public List<ProxyConnection> getFreeProxyConnections(){
    	 return new ArrayList<ProxyConnection>(this.free);
     }
	/**
	 * 初始化连接池
	 * 从DataBase中创建一个ProxyConnection并保存在all与free中
	 * @throws SQLException  ex
	 */
	void initial() throws SQLException{
		try {
			writeLock.lock();
			log.debug("initial connection pools for datasource ["+this.dataSource.getId()+"]");
			this.create(this.dataSource.getMin_connection());
		}finally {
			writeLock.unlock();
		}
		
	}
	private void create(int num) throws SQLException{
		try {
			writeLock.lock();
			for(int i=0;i< num;i++){
				ProxyConnection connection = createConnection();
				this.all.add(connection);
				this.free.add(connection);
			}
		}finally {
			writeLock.unlock();
		}
		
	}
	/**
	 * 自动增加连接
	 * @throws SQLException  ex
	 */
	private void increase() throws SQLException{
		try {
			writeLock.lock();
			if(this.all.size()<this.dataSource.getMax_connection()){
				this.create(this.dataSource.getAdd_connection());
			}
		}finally {
			writeLock.unlock();
		}
	}
	/**
	 * 关闭连接
	 * 如果空闲连接数量超过初始化数量，则自动关闭多于的连接
	 * 刷新为归还链接时触发
	 */
	public void refresh(){
		if(this.free.size()>this.getDataSource().getMin_connection()){
			try {
				readLock.lock();
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
			}finally{
				readLock.unlock();
			}
		}
	}
	/**
	 * 获取可用的连接
	 * @return ProxyConnection 代理连接
	 * @throws SQLException a SQL exception
	 */
	public ProxyConnection getConnection() throws SQLException{
		ProxyConnection connection;
		try {
			writeLock.lock();
			if(this.free.size()==0){
				if(this.all.size()>=this.dataSource.getMax_connection()){//如果没有空闲连接，首先判断当前所有的连接数量
						//获取一个锁
					Lock lock = Lock.getLock(Thread.currentThread());
					lockList.add(lock);
					try {
						lock.await();
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
			connection = this.free.poll();
		}finally {
			writeLock.unlock();
		}
		return connection;
	}
	/**
	 * 归还连接
	 * @param connection a proxy connection
	 */
	public void release(ProxyConnection connection){
		if(connection!=null){
			try {
				writeLock.lock();
				if(free.contains(connection))
					return;
				this.free.add(connection);
				if(lockList.isEmpty())
					return;
				lockList.poll().signal();
			}finally {
				writeLock.unlock();
			}
		}
	}
	/**
	 * 关闭连接池
	 */
	public void destory(){
		//清空free
		if(this.free!=null){
			this.free.clear();
			this.free = null;
		}
		//关闭所有连接
		if(this.all!=null){
			this.all.forEach((proxyConnection) -> {
				try {
					proxyConnection.destory();
				} catch (SQLException e) {
					log.error("failed to close connection "+proxyConnection,e);
					e.printStackTrace();
				}
			});
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
		this.destory();
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