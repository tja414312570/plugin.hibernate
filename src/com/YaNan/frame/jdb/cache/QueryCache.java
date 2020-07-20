package com.yanan.frame.jdb.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * 整体缓存结构 数据库==》表==》Sql==》参数==》结果
 * 
 * @author yanan
 *
 */
public class QueryCache {
	private static QueryCache queryCache;
	private Map<String, TableCache> dataBaseCache = new Hashtable<String, TableCache>();

	/**
	 * 表缓存
	 * 
	 * @author yanan
	 *
	 */
	static class TableCache {
		private String databaseName;

		public TableCache(String databaseName) {
			this.databaseName = databaseName;
		}

		// 存储 数据库名 --> 表缓存
		private Map<String, SqlCache> cache = new Hashtable<String, SqlCache>();

		public SqlCache get(String key) {
			return cache.get(key);
		}

		public String getDatabaseName() {
			return databaseName;
		}

		public void setDatabaseName(String databaseName) {
			this.databaseName = databaseName;
		}

		public void put(String key, SqlCache value) {
			this.cache.put(key, value);
		}

		public void cleanAll() {
			Collection<SqlCache> caches = this.cache.values();
			for(SqlCache child : caches) {
				child.cleanAll();
			}
			cache.clear();
		}

		public void remove(String key) {
			this.cache.remove(key);
		}

		public void clean(String table) {
			SqlCache child = this.cache.get(table);
			if(child != null) {
				child.cleanAll();
				this.cache.remove(table);
			}
		}

		@Override
		public String toString() {
			return "TableCache [databaseName=" + databaseName + ", cache=" + cache + "]";
		}
	}

	/**
	 * sql缓存
	 * 
	 * @author yanan
	 *
	 */
	static class SqlCache {
		private String tableName;

		public SqlCache(String tableName) {
			this.tableName = tableName;
		}

		public void cleanAll() {
			Collection<ResultCache> caches = this.cache.values();
			for(ResultCache child : caches) {
				child.cleanAll();
			}
			cache.clear();
		}

		// 存储 sql --> 参数缓存
		private Map<String, ResultCache> cache = new Hashtable<String, ResultCache>();

		public ResultCache get(String key) {
			return cache.get(key);
		}

		public String getTableName() {
			return tableName;
		}

		public void setTableName(String tableName) {
			this.tableName = tableName;
		}

		public void put(String key, ResultCache value) {
			this.cache.put(key, value);
		}
		public void remove(String key) {
			this.cache.remove(key);
		}
		public void clean(String key) {
			ResultCache child = this.cache.get(key);
			if(child != null) {
				child.cleanAll();
				this.cache.remove(key);
			}
		}

		@Override
		public String toString() {
			return "SqlCache [tableName=" + tableName + ", cache=" + cache + "]";
		}
	}

	/**
	 * 结果集缓存
	 * 
	 * @author yanan
	 *
	 */
	static class ResultCache {
		private String sql;

		public ResultCache(String sql) {
			this.sql = sql;
		}

		public void cleanAll() {
			cache.clear();
		}

		private Map<String, Object> cache = new Hashtable<String, Object>();

		public static String hash(List<Object> objects) {
			if (objects == null)
				return "null";
			StringBuilder sb = new StringBuilder();
			for (Object object : objects) {
				sb.append(object);
			}
			return sb.toString();
		}

		@SuppressWarnings("unchecked")
		public <T> List<T> get(List<Object> parameters) {
			return (List<T>) cache.get(hash(parameters));
		}

		public boolean has(List<Object> parameters) {
			return cache.containsKey(hash(parameters));
		}

		public String getSql() {
			return sql;
		}

		public void setSql(String sql) {
			this.sql = sql;
		}

		public void put(List<Object> parameter, Object value) {
			this.cache.put(hash(parameter), value);
		}
		public void remove(List<Object> parameters) {
			this.cache.remove(hash(parameters));
		}

		@Override
		public String toString() {
			return "ResultCache [sql=" + sql + ", cache=" + cache + "]";
		}
	}

	// tableName ==>sql
	public static QueryCache getCache() {
		if (queryCache == null)
			synchronized (QueryCache.class) {
				if (queryCache == null)
					queryCache = new QueryCache();
			}
		return queryCache;
	}

	@Override
	public String toString() {
		return "QueryCache [dataBaseCache=" + dataBaseCache + "]";
	}

	public <T> List<T> getQuery(String database, String tableName, String sql, List<Object> parameter) {
		// 根据数据库缓存拿到表缓存
		TableCache tableCache = dataBaseCache.get(database);
		if (tableCache == null)
			return null;
		// 根据表缓存拿到sql缓存
		SqlCache sqlCache = tableCache.get(tableName);
		if (sqlCache == null)
			return null;
		// 根据sql缓存查询参数缓存
		ResultCache resultCache = sqlCache.get(sql);
		if (resultCache == null)
			return null;
		// 根据参数拿到查询结果缓存
		List<T> result = resultCache.get(parameter);
		if (result == null && resultCache.has(parameter))
			return new ArrayList<T>();
		return result;
	}

	public <T> void addCache(String database, String tableName, String sql, List<Object> parameter, Object object) {
		// 根据数据库缓存拿到表缓存
		TableCache tableCache = dataBaseCache.get(database);
		if (tableCache == null) {
			tableCache = new TableCache(database);
			dataBaseCache.put(database, tableCache);
		}
		// 根据表缓存拿到sql缓存
		SqlCache sqlCache = tableCache.get(tableName);
		if (sqlCache == null) {
			sqlCache = new SqlCache(tableName);
			tableCache.put(tableName, sqlCache);
		}
		// 根据sql缓存查询参数缓存
		ResultCache resultCache = sqlCache.get(sql);
		if (resultCache == null) {
			resultCache = new ResultCache(sql);
			sqlCache.put(sql, resultCache);
		}
		// 根据参数拿到查询结果缓存
		resultCache.put(parameter, object);
	}

	public void cleanAllCache() {
		Collection<TableCache> tableCaches = dataBaseCache.values();
		for(TableCache tableCache : tableCaches) {
			tableCache.cleanAll();
		}
		dataBaseCache.clear();
	}

	public void cleanDataBaseCache(String name) {
		TableCache child = dataBaseCache.get(name);
		if(child != null)
			child.cleanAll();
		dataBaseCache.remove(name);
	}
	public void cleanTable(String dataBase,String table) {
		TableCache tableCache = dataBaseCache.get(dataBase);
		if(tableCache == null)
			return;
		tableCache.clean(table);
	}
	public void cleanSql(String dataBase,String table,String sql) {
		TableCache tableCache = dataBaseCache.get(dataBase);
		if(tableCache == null)
			return;
		SqlCache sqlCache = tableCache.get(table);
		if(sqlCache == null)
			return;
		sqlCache.clean(sql);
	}
	public void cleanResult(String dataBase,String table,String sql,List<Object> parameters) {
		TableCache tableCache = dataBaseCache.get(dataBase);
		if(tableCache == null)
			return;
		SqlCache sqlCache = tableCache.get(table);
		if(sqlCache == null)
			return;
		ResultCache resultCache = sqlCache.get(sql);
		if(resultCache == null)
			return;
		resultCache.remove(parameters);
	}
}