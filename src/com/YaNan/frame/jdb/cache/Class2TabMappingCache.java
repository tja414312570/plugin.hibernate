package com.yanan.frame.jdb.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yanan.frame.jdb.DataTable;
import com.yanan.frame.jdb.exception.SqlExecuteException;

public class Class2TabMappingCache {
	private static Class2TabMappingCache dbManager;
	private static Map<Class<?>, DataTable> map = new ConcurrentHashMap<Class<?>, DataTable>();
	private static Map<Class<?>, DataTable> orm = new HashMap<Class<?>, DataTable>();

	public static Map<Class<?>, DataTable> getDBTabelsMap() {
		return map;
	}

	public Class2TabMappingCache getManager() {
		if (dbManager == null)
			dbManager = new Class2TabMappingCache();
		return dbManager;
	}

	public static DataTable getDBTab(Class<?> cls) {
		DataTable dataTable = map.get(cls);
		if (dataTable == null) 
			throw new SqlExecuteException("could not found data table template for class "+cls);
		return dataTable;
	}

	public static boolean hasTab(Class<?> cls) {
		return map.containsKey(cls);

	}

	public static void addTab(DataTable tab) {
		map.put(tab.getDataTablesClass(), tab);
	}

	public static DataTable getDBTab4Orm(Class<?> resultType) {
		DataTable table = orm.get(resultType);
		if (table != null)
			return table;
		table = map.get(resultType);
		if (table != null)
			return table;
		table = new DataTable(resultType);
		orm.put(table.getDataTablesClass(), table);
		return table;
	}
}