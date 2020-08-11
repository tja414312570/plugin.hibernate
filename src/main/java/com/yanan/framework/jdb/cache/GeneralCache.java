package com.yanan.framework.jdb.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GeneralCache {
	private static GeneralCache generalCache;
	private Map<Integer,String> cache = new HashMap<Integer,String>();
	private ConcurrentHashMap<String, Object> safeCache = new ConcurrentHashMap<String, Object>();
	public static GeneralCache getCache(){
		if(generalCache==null)
			synchronized (SqlCache.class) {
				if(generalCache==null)
					generalCache=new GeneralCache();
			}
		return generalCache;
	}
	@SuppressWarnings("unchecked")
	public <T> T set(String key,Object value) {
		return (T) this.safeCache.put(key, value);
	}
	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		return (T) this.safeCache.get(key);
	}
	public String getSql(int ident){
		return this.cache.get(ident);
	}
	public String addSql(int ident,String sql){
		return this.cache.put(ident,sql);
	}
}