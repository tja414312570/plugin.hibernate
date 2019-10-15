package com.YaNan.frame.jdb;

import java.util.HashMap;
import java.util.Map;

public class TabCache {
	private static TabCache dbManager;
	private static Map<String, DataTable> map = new HashMap<String, DataTable>();

	public TabCache getManager() {
		if (dbManager == null)
			dbManager = new TabCache();
		return dbManager;
	}

	@SuppressWarnings("unlikely-arg-type")
	public static DataTable getDBTab(Class<?> cls) {
		if (map.containsKey(cls))
			return map.get(cls);
		DataTable tab = new DataTable(cls);
		map.put(tab.getName(), tab);
		return tab;
	}

	@SuppressWarnings("unlikely-arg-type")
	public static DataTable getDBTab(Object obj) {
		if (map.containsKey(obj.getClass()))
			return map.get(obj.getClass());
		DataTable tab = new DataTable(obj);
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

	public static void addTab(DataTable tab) {
		map.put(tab.getName(), tab);
	}
}
