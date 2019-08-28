package com.YaNan.frame.hibernate.orm;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.hibernate.database.DBColumn;
import com.YaNan.frame.hibernate.database.DBTab;
import com.YaNan.frame.hibernate.database.fragment.SqlFragment;
import com.YaNan.frame.plugin.annotations.Register;
import com.YaNan.frame.utils.reflect.ClassLoader;

@Register(priority=Integer.MAX_VALUE)
public class DefaultOrmBuilder implements OrmBuilder{
	StringBuilder stringBuffer = new StringBuilder();
	private Logger log = LoggerFactory.getLogger(DefaultOrmBuilder.class);
	@Override
	public List<Object> builder(ResultSet resultSet, SqlFragment sqlFragment) {
		try {
			List<Object> results = new ArrayList<Object>();
			//1获取返回值类型
			Class<?> resultType = sqlFragment.getResultTypeClass();
			//2判断类型时否是List map 等聚合函数
			if(ClassLoader.implementsOf(resultType, Map.class)){
				this.wrapperMap(resultSet, results,resultType);
			}else{
				if(ClassLoader.isBaseType(resultType))
					while(resultSet.next())
						results.add(ClassLoader.castType( resultSet.getObject(1), resultType));
				else
					this.wrapperBean(resultSet,results,resultType);
			}
			return results;
		} catch (SQLException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("failed to wrapper the result set!",e);
		}
	}
	private void wrapperBean(ResultSet resultSet, List<Object> result, Class<?> resultType) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, SQLException {
		DBTab tab = new DBTab(resultType);
		Iterator<DBColumn> columnIterator = tab.getDBColumns().values().iterator();
		stringBuffer.setLength(0);
		DBColumn[] colNameArray = new DBColumn[tab.getDBColumns().values().size()];
		int i =0;
		while(columnIterator.hasNext()){
			DBColumn dbColumn = columnIterator.next();
			colNameArray[i++] = dbColumn;
			String columnName = dbColumn.getName();
			if(log.isDebugEnabled()) {
				stringBuffer.append(columnName);
				if(columnIterator.hasNext())
					stringBuffer.append(",");
			}
		}
		if(log.isDebugEnabled())
			log.debug(stringBuffer.toString());
		while (resultSet.next()) {
			//可以使用PlugsHandler代理类，实现aop。但对Gson序列化有影响
//			Object beanInstance = PlugsFactory.getPlugsInstance(resultType);
			ClassLoader loader = new ClassLoader(resultType);
			stringBuffer.setLength(0);
			for(i=0;i< colNameArray.length;i++) {
				DBColumn column = colNameArray[i];
				Field field = column.getField();
				Object object = resultSet.getObject(column.getName());
				if(log.isDebugEnabled()) {
					stringBuffer.append(object);
					if(i < colNameArray.length-1)
						stringBuffer.append(",");
				}
				if(object==null)
					continue;
				loader.set(field,ClassLoader.castType(object,field.getType()));
			}
			if(log.isDebugEnabled())
				log.debug(stringBuffer.toString());
			result.add(loader.getLoadedObject());
		}
	}
	private void wrapperMap(ResultSet resultSet,List<Object> results, Class<?> resultType) throws SQLException {
		ResultSetMetaData metaData = resultSet.getMetaData();
		String[] colNameArray = this.getColumnName(metaData);
		stringBuffer.setLength(0);
		while (resultSet.next()) {
			Map<String,Object> map = new HashMap<String,Object>();
			for(int i = 0 ;i<colNameArray.length;i++){
				Object result = resultSet.getObject(i+1);
				if(log.isDebugEnabled()) {
					stringBuffer.append(result);
					if(i < colNameArray.length-1)
						stringBuffer.append(",");
				}
				map.put(colNameArray[i], result);
			}
			if(log.isDebugEnabled())
				log.debug(stringBuffer.toString());
			results.add(map);
		}
	}
	private String[] getColumnName(ResultSetMetaData metaData) throws SQLException{
		int colCount = metaData.getColumnCount();
		stringBuffer.setLength(0);
		String[] colNameArray = new String[colCount];
		for(int i = 0;i<colCount;i++) {
			colNameArray[i] = metaData.getColumnLabel(i+1);
			if(log.isDebugEnabled()) {
				stringBuffer.append(colNameArray[i]);
				if(i < colNameArray.length-1)
					stringBuffer.append(",");
			}
		}
		if(log.isDebugEnabled())
			log.debug(stringBuffer.toString());
		return colNameArray;
	}
}
