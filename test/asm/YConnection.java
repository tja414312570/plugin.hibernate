package asm;

import com.YaNan.frame.jdb.database.ConnectionPools;

/**
 * 连接接口
 * @author yanan
 *
 */
public interface YConnection {
	/**
	 * 设置连接池
	 * @param connectionPools
	 */
	void setConnectionPool(ConnectionPools connectionPools);
	/**
	 * 关闭链接
	 */
	void releaseConnection();
}
