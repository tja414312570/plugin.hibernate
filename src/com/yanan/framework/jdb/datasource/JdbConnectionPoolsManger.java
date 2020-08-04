package com.yanan.framework.jdb.datasource;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbConnectionPoolsManger {
	static Logger logger = LoggerFactory.getLogger(JdbConnectionPoolsManger.class);
	private final static ConnectionPoolRefreshService connectionPoolRefreshService = new ConnectionPoolRefreshService();
	private volatile static ConcurrentHashMap<DataSource,ConnectionPools> connectionMap = new ConcurrentHashMap<>();
	public static ConnectionPools getJdbConnectionPools(DefaultDataSource dataSource) {
		ConnectionPools connectionPools = connectionMap.get(dataSource);
		if(connectionPools == null) {
			connectionPools  = new ConnectionPools(dataSource);
			connectionMap.put(dataSource, connectionPools);
			connectionPoolRefreshService.addConnectionPool(connectionPools);
		}
		return connectionMap.get(dataSource);
	}
	/**
	 * 连接池刷新服务类,该类用于提供连接池的刷洗服务
	 * @author yanan
	 *
	 */
	static class ConnectionPoolRefreshService implements Runnable{
		 private Thread connectionPoolRefreshThread;//用于提供服务的线程
		 volatile transient boolean keepAlive = false;
		 private int delay = 1000;//每次数据连接池刷新时间  
		 private int timeout = 1000*60;//当所有连接为空时刷新服务等待时间 默认1 min  //用于提高效率，降低新建对象的性能开销
		 private int sleepType = 0;//睡眠类型   0 ==> 间隔睡眠   1 ==> 守护睡眠
		 private List<ConnectionPools> connectionPoolsQueue = new LinkedList<ConnectionPools>();
		 ConnectionPoolRefreshService(){};
		 public void addConnectionPool(ConnectionPools connectionPool){
			if(!this.connectionPoolsQueue.contains(connectionPool)){
				synchronized (connectionPoolsQueue) {
					if(!this.connectionPoolsQueue.contains(connectionPool)){
						connectionPoolsQueue.add(connectionPool);
					}
				}
			}
			if(connectionPoolRefreshThread==null){//保证单线程
				synchronized (this) {
					if(connectionPoolRefreshThread==null){
						keepAlive=true;
						connectionPoolRefreshThread = new Thread(this);
						connectionPoolRefreshThread.setName("yanan_jdb_connection_manager");
						connectionPoolRefreshThread.setDaemon(true);
						connectionPoolRefreshThread.start();
					}
				}
			}else{
				if(sleepType==1){
					keepAlive=true;
					connectionPoolRefreshThread.interrupt();
				}
			}
		 }
		 /**
		  * 关闭刷新监控线程
		  */
		 public void shutdown(){
			 if(keepAlive)
				 keepAlive=false;
			 if(connectionPoolRefreshThread!=null&&connectionPoolRefreshThread.isAlive())
				 connectionPoolRefreshThread.interrupt();
			 if(connectionPoolRefreshThread!=null)
				 connectionPoolRefreshThread=null;
		 }
		 /**
		  * 销毁刷新监控线程
		  */
		 public void destory(){
			 if(connectionPoolsQueue!=null){
				 connectionPoolsQueue.clear();
				 connectionPoolsQueue=null;
			 }
			 this.shutdown();
		 }
		 /**
		  * 移除连接池
		  */
		 public void removeConnectionPool(ConnectionPools connectionPool){
			 if(connectionPoolsQueue!=null)
				connectionPoolsQueue.remove(connectionPool);
		}
		 @Override
		public void run() {
			while(keepAlive){
				try {
					Iterator<ConnectionPools> iterator = connectionPoolsQueue.iterator();
					ConnectionPools connectionPool;
					long now = System.currentTimeMillis();
					while(iterator.hasNext()){
						connectionPool = iterator.next();
						connectionPool.refresh();
						List<Lock> locks = connectionPool.getWaitList();
						if(locks.size()>0) {
							Iterator<Lock> lockIterator = locks.iterator();
							while(lockIterator.hasNext()) {
								Lock lock = lockIterator.next();
								if(lock.getTimes()+connectionPool.getDataSource().getWait_times()>=now)
									lock.interrupt(new ConnectionWaitTimeout());
							}
						}
//						logger.debug("connection pools usag:"+connectionPool.getDataSource().getId()+":"+connectionPool.getAllNum()+":"+connectionPool.getFreeNum()+":"+locks.size());
					}
					sleepType = 0;
					Thread.sleep(delay);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			connectionPoolRefreshThread=null;
		}
		public int getDelay() {
			return delay;
		}
		public void setDelay(int delay) {
			this.delay = delay;
		}
		public int getTimeout() {
			return timeout;
		}
		public void setTimeout(int timeout) {
			this.timeout = timeout;
		}
	}
}