package com.YaNan.frame.jdb.database.dataresource;

public class ConnectionWaitTimeout extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -247966526142452591L;
	public ConnectionWaitTimeout() {
		super("get connection waiting timeout at "+Thread.currentThread());
	}

}
