package com.YaNan.frame.jdb.datasource;

import java.util.concurrent.locks.LockSupport;

public class Lock {
	/**
	 * 锁持有线程
	 */
	private Thread lockThread;
	/**
	 * 锁的时间
	 */
	private long times; 
	/**
	 * 锁定
	 */
	private volatile boolean locked = true;
	private Throwable interruptCause;
	public long getTimes() {
		return times;
	}

	public Thread getLockThread() {
		return lockThread;
	}

	public Lock(Thread lockThread) {
		times = System.currentTimeMillis();
		this.lockThread = lockThread;
	}
	/**
	 * 锁定
	 * @return 
	 * @throws Throwable 
	 */
	public boolean await() throws Throwable {
		if(!this.locked){
			LockSupport.park(this);
			if(interruptCause != null)
				throw interruptCause;
			return true;
		}else {
			return false;
		}
	}
	/**
	 * 释放锁
	 */
	public void signal() {
		locked = false;
		LockSupport.unpark(lockThread);
	}
	public void interrupt(Throwable cause) {
			lockThread.interrupt();
			locked = false;
			this.interruptCause = cause;
	}
	public static Lock getLock(Thread threadLock) {
		return new Lock(threadLock);
	}

	public boolean isLocked() {
		return locked;
	}
}