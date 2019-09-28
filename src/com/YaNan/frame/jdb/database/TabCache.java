package com.YaNan.frame.jdb.database;

import java.util.HashMap;
import java.util.Map;

public class TabCache {
	private static TabCache dbManager;
	private static Map<String, DBTab> map = new HashMap<String, DBTab>();

	public TabCache getManager() {
		if (dbManager == null)
			dbManager = new TabCache();
		return dbManager;
	}

	@SuppressWarnings("unlikely-arg-type")
	public static DBTab getDBTab(Class<?> cls) {
		if (map.containsKey(cls))
			return map.get(cls);
		DBTab tab = new DBTab(cls);
		map.put(tab.getName(), tab);
		return tab;
	}

	@SuppressWarnings("unlikely-arg-type")
	public static DBTab getDBTab(Object obj) {
		if (map.containsKey(obj.getClass()))
			return map.get(obj.getClass());
		DBTab tab = new DBTab(obj);
		map.put(tab.getName(), tab);
		return tab;
	}

	@SuppressWarnings("unlikely-arg-type")
	public static boolean hasTab(Class<?> cls) {
		return map.containsKey(cls);

	}

	@SuppressWarnings("unlikely-arg-type")
	public static boolean hasTab(Object obj) {
		return map.containsKey(obj.getClass());
	}

	public static void addTab(DBTab tab) {
		map.put(tab.getName(), tab);
	}
}
