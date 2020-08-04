package com.yanan.framework.jdb.operate;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.framework.jdb.DataTable;
import com.yanan.framework.jdb.DBInterface.OperateImplement;
import com.yanan.framework.jdb.cache.Class2TabMappingCache;
import com.yanan.utils.reflect.AppClassLoader;

/**
 * 该类用于提供给DATab的query一个查询的SQL语句的生成方法 提过一个构造器，传入一个DBTab型的表对象，应为他需要使用DBTab context
 * 
 * @author Administrator
 *
 */
public class Update extends OperateImplement {
	private Map<String, String> map = new HashMap<String, String>();
	private List<String> condition = new ArrayList<String>();
	private Map<String, Object> updateList = new LinkedHashMap<String, Object>();
	//参数存储规则==>列==》参数
	private final Logger log = LoggerFactory.getLogger( Query.class);

	public Update(DataTable dataTables, Object object) {
		this.setDbTab(dataTables);
		this.preparedUpdateColumn(object);
	}
	/**
	 * prepared updates columns data
	 * @param object obj
	 */
	public void preparedUpdateColumn(Object object){
		AppClassLoader loader = new AppClassLoader(object);
		Iterator<Field> fI = this.getDbTab().getFieldMap().keySet().iterator();
		while (fI.hasNext()) {
			Field field = fI.next();
			try {
				Object value = loader.get(field);
				if (value != null){
					String columnName=this.dataTables.getFieldMap().get(field).getName();
					if(columnName!=null){
						int point = columnName.indexOf(".");
						if(point>-1){
							columnName = columnName.substring(0, point);
							if(!columnName.equals(this.dataTables.getSimpleName()))
								continue;
						}
						this.updateList.put(columnName,value);
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | SecurityException
					| NoSuchMethodException e) {
				log.error(e.getMessage(),e);
			}
		}
	}
	/**
	 * 更新所有域(不包括null字段)
	 * 
	 * @param object obj
	 */
	public Update(Object object) {
		this.dataTables = Class2TabMappingCache.getDBTab(object.getClass());
		
		this.preparedUpdateColumn(object);
	}

	/**
	 * 
	 * @param object obj
	 * @param updateNull 是否更新空值
	 */
	public Update(Object object, boolean updateNull) {
		this.dataTables = Class2TabMappingCache.getDBTab(object.getClass());
		this.preparedUpdateColumn(object);
	}

	/**
	 * 更新数据库，后面接要更新的域
	 * 
	 * @param object obj
	 * @param fields field array
	 */
	public Update(Object object, String... fields) {
		try {
			this.dataTables = Class2TabMappingCache.getDBTab(object.getClass());
			AppClassLoader loader = new AppClassLoader(object);
			for (String strField : fields) {
				Field field = this.getDbTab().getDataTablesClass().getDeclaredField(strField);
				Object value = loader.get(field);
				if (value != null)
					this.updateList.put(this.getDbTab().getDBColumn(field).getName(),value);
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}

	/**
	 * 更新数据库,此类必须使用addField方法，对更新的数据设置；
	 * 
	 * @param cls class
	 */
	public Update(Class<?> cls) {
		this.dataTables = Class2TabMappingCache.getDBTab(cls);
	}

	public Update(Class<?> cls, String... fields) {
		this.dataTables = Class2TabMappingCache.getDBTab(cls);
	}

	/**
	 * 设置要更新的数据库字段
	 * 
	 * @param updateList update list
	 * @return update object
	 * @throws SecurityException ex
	 * @throws NoSuchFieldException ex
	 */
	public Update setFields(Map<String, Object> updateList) throws NoSuchFieldException, SecurityException {
		if (!this.updateList.isEmpty())
			this.updateList.clear();
		Iterator<String> iterator = updateList.keySet().iterator();
		while (iterator.hasNext()) {
			String field = iterator.next();
			String column = this.getDbTab().getDBColumn(field).getName();
			this.updateList.put(column, updateList.get(field));
			this.updateList = updateList;
		}
		return this;
	}

	/**
	 * 要更新的数据库的字段
	 * 
	 * @param field field
	 * @param value value
	 * @return update object
	 * @throws SecurityException ex
	 * @throws NoSuchFieldException ex
 	 */
	public Update setField(String field, Object value) throws NoSuchFieldException, SecurityException {
		String column = this.getDbTab().getDBColumn(field).getName();
		this.updateList.put(column,value);
		return this;
	}

	/**
	 * 添加要更新的域
	 * 
	 * @param field field
	 * @param value value
	 * @return update object
	 */
	public Update setColumn(String field, Object value) {
		this.updateList.put(field,value);
		return this;
	}

	/**
	 * 设置要更新的域
	 * 
	 * @param updateList update list
	 * @return update object
	 */
	public Update setColumns(Map<String, Object> updateList) throws NoSuchFieldException, SecurityException {
		this.updateList = updateList;
		return this;
	}

	public Update addCondition(Field field, String condition) {
		if(condition==null)
			throw new RuntimeException("update condition is null at column "+field);
		this.map.put(getDbTab().getDBColumn(field).getName(), condition);
		return this;
	}

	public Update addCondition(Field field, Object condition) {
		if(condition==null)
			throw new RuntimeException("update condition is null at column "+field);
		this.map.put(getDbTab().getDBColumn(field).getName(), condition.toString());
		return this;
	}

	public Update addColumnCondition(String column, Object condition) {
		if(condition==null)
			throw new RuntimeException("update condition is null at column "+column);
		this.map.put(column, condition.toString());
		return this;
	}

	public Update addCondition(String field, Object condition) {
		if(condition==null)
			throw new RuntimeException("update condition is null at column "+field);
		this.map.put(getDbTab().getDBColumn(field).getName(), condition.toString());
		return this;
	}

	public Update addConditionCommand(String condition) {
		this.condition.add(condition);
		return this;
	}

	public String create() {
		this.parameters.clear();
		StringBuilder sb = new StringBuilder("UPDATE ").append(this.getDbTab().getName()).append(" SET ");
		Iterator<String> iterator = this.updateList.keySet().iterator();
		while (iterator.hasNext()) {
			String column = iterator.next();
			sb.append(column).append(" = ?").append(iterator.hasNext() ? ',' : "");
			this.parameters.add(updateList.get(column));
		}
		if (this.map.size() != 0) {
			sb.append(" WHERE ");
			Iterator<String> i = this.map.keySet().iterator();
			while (i.hasNext()) {
				String s = i.next();
				sb.append(s).append(" = ?").append(i.hasNext() ? " AND " : "");
				this.parameters.add(this.map.get(s));
			}
		}
		if (this.condition.size() != 0) {
			sb.append(this.map.size() == 0 ? " WHERE " : " AND ");
			Iterator<String> i = this.condition.iterator();
			while (i.hasNext())
				sb.append(i.next()).append(i.hasNext() ? " AND " : "");
		}
		return sb.toString();
	}

	public int update() {
		int rows = this.getDbTab().update(this);
		log.debug("prepared sql:" + this.getPreparedSql());
		log.debug("prepared parameter:" + parameters);
		log.debug("update rows: "+rows);
		return rows;
	}

	/**
	 * 移除要更新的域
	 * 
	 * @param fields field array
	 * @return update object
	 */
	public Update removeFields(String... fields) {
		for (String field : fields) {
			String column = this.getDbTab().getDBColumn(field).getName();
			this.updateList.remove(column);
		}
		return this;
	}

	/**
	 * 移除要更新的域
	 * 
	 * @param field field
	 * @return update object
	 */
	public Update removeField(String field) {
		String column = this.getDbTab().getDBColumn(field).getName();
		this.updateList.remove(column);
		return this;
	}

	/**
	 * 移除要更新的域
	 * 
	 * @param columns column array
	 * @return update object
	 */
	public Update removeColumns(String... columns) {
		for (String column : columns)
			this.updateList.remove(column);
		return this;
	}

	/**
	 * 移除要更新的列
	 * @param column  column
	 * @return update object
	 */
	public Update removeColumn(String column) {
		this.updateList.remove(column);
		return this;
	}

	/**
	 * 设置域的表达式
	 * 
	 * @param field field
	 * @param value value
	 * @return update object
	 */
	public Update setFieldExpression(String field, Object value) {
		String column = this.getDbTab().getDBColumn(field).getName();
		this.updateList.put(column, value);
		return this;
	}

	/**
	 * 设置列的表达式
	 * 
	 * @param field field
	 * @param value value
	 * @return update object
	 */
	public Update setColumnExpression(String field, Object value) {
		this.updateList.put(field, value);
		return this;
	}

	/**
	 * 设置列的Case表达式
	 * 
	 * @param cases  case
	 * @return update object
	 */
	public Update setColumnExpression(Case cases) {
		this.updateList.put(cases.getColumn(), cases.create());
		return this;
	}
}