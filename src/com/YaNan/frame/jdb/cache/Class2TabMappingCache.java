package com.YaNan.frame.jdb.cache;

import java.util.HashMap;
import java.util.Map;

import com.YaNan.frame.jdb.DataTable;

public class Class2TabMappingCache {
	private static Class2TabMappingCache dbManager;
	private static Map<Class<?>, DataTable> map = new HashMap<Class<?>, DataTable>();

	public static Map<Class<?>, DataTable> getDBTabelsMap() {
		return map;
	}

	public Class2TabMappingCache getManager() {
		if (dbManager == null)
			dbManager = new Class2TabMappingCache();
		return dbManager;
	}

	public static DataTable getDBTab(Class<?> cls) {
		if (map.containsKey(cls))
			return map.get(cls);
		DataTable tab = new DataTable(cls);
		map.put(tab.getDataTablesClass(), tab);
		return tab;
	}

	public static DataTable getDBTab(Object obj) {
		if (map.containsKey(obj.getClass()))
			return map.get(obj.getClass());
		DataTable tab = new DataTable(obj);
		map.put(tab.getDataTablesClass(), tab);
		return tab;
	}

	public static boolean hasTab(Class<?> cls) {
		return map.containsKey(cls);

	}

	public static boolean hasTab(Object obj) {
		return map.containsKey(obj.getClass());
	}

	public static void addTab(DataTable tab) {
		map.put(tab.getDataTablesClass(), tab);
	}
}
