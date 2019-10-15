package com.YaNan.frame.jdb.datasource;


public class Lock {
	/**
	 * 锁持有对象
	 */
	private Object lockObject;
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

	public Object getLockObject() {
		return lockObject;
	}

	public Lock(Object lockObject) {
		times = System.currentTimeMillis();
		this.lockObject = lockObject;
	}
	/**
	 * 锁定
	 * @throws Throwable 
	 */
	public void lock() throws Throwable {
		synchronized (this) {
				if(locked)
					this.wait();
			if(interruptCause != null)
				throw interruptCause;
		}
	}
	/**
	 * 释放锁
	 */
	public void unLock() {
		synchronized (this) {
			locked = false;
			this.notifyAll();
		}
	}
	public void interrupt(Throwable cause) {
		synchronized (this) {
			locked = false;
			this.interruptCause = cause;
			this.notifyAll();
		}
	}
	public static Lock getLock(Object lockObject) {
		return new Lock(lockObject);
	}

	public boolean isLocked() {
		return locked;
	}
}
