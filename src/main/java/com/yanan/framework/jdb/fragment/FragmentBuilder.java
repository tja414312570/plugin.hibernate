package com.yanan.framework.jdb.fragment;

import com.yanan.framework.plugin.annotations.Service;

@Service
public interface FragmentBuilder {
	public final static String PREFIX = "<";
	public final static String SPLITSUFFIX = " ";
	public final static String SUFFIX = "</";
	public final static String SPLITPREFIX = ">";
	/**
	 * 构架FragmentSet
	 * @param wrapper 
	 */
	void build(Object wrapper);
	/**
	 * 准备sql
	 * @param objects obj
	 * @return 准备片段
	 */
	PreparedFragment prepared(Object objects);
}