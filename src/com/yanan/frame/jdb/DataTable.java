package com.yanan.frame.jdb;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.frame.jdb.DBInterface.mySqlInterface;
import com.yanan.frame.jdb.annotation.Column;
import com.yanan.frame.jdb.annotation.Tab;
import com.yanan.frame.jdb.cache.Class2TabMappingCache;
import com.yanan.frame.jdb.cache.QueryCache;
import com.yanan.frame.jdb.operate.BatchInsert;
import com.yanan.frame.jdb.operate.Create;
import com.yanan.frame.jdb.operate.Delete;
import com.yanan.frame.jdb.operate.Insert;
import com.yanan.frame.jdb.operate.Query;
import com.yanan.frame.jdb.operate.Update;
import com.yanan.utils.reflect.AppClassLoader;
import com.yanan.utils.string.StringUtil;

public class DataTable implements mySqlInterface {
	private Field AIField;
	private boolean autoUpdate;
	private String charset;
	private Class<?> dataTablesClass;
	private String collate;
	private Map<String, String> columns = new LinkedHashMap<String, String>();
	private DataSource dataSource;
	private String include;
	private boolean isMust;
	private AppClassLoader loader;
	private Map<Field, DBColumn> map = new LinkedHashMap<Field, DBColumn>();
	private Map<String, DBColumn> nameMap = new HashMap<String, DBColumn>();
	private String name;
	private String schmel;
	private Object dataTablesObject;
	private Field Primary_key;
	private Map<String, ResultSet> session = new HashMap<String, ResultSet>();
	private String value;
	private boolean exist;
	private final Logger log = LoggerFactory.getLogger( DataTable.class);
	private String[] columnsArray;

	public Object getDataTablesObject() {
		return dataTablesObject;
	}

	public void setDataTablesObject(Object dataTablesObject) {
		this.dataTablesObject = dataTablesObject;
	}

	/**
	 * 默认构造器，需要传入一个Class《？》的class 构造器会默认从DataBase中获得connection
	 * 同时该构造器会对dataTablesClass进行处理，进行class与Field的映射
	 * 
	 * @param dataTablesClass mapping class
	 */
	public DataTable(Class<?> dataTablesClass) {
		this(new AppClassLoader(dataTablesClass).getLoadedObject());
	}
	
	DataTable(com.yanan.frame.jdb.entity.Tab tabEntity)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		this(new AppClassLoader(tabEntity.getCLASS()).getLoadedClass());

	}

	DataTable(Object obj) {
		this.dataTablesClass = obj.getClass();
		this.dataTablesObject = obj;
		this.loader = new AppClassLoader(obj);
		try {
			Tab tab = this.loader.getLoadedClass().getAnnotation(Tab.class);
			if (tab == null) {
				Class<?> sCls = this.dataTablesClass.getSuperclass();
				tab = sCls.getAnnotation(Tab.class);
				if (tab != null) {
					this.dataTablesClass = sCls;
				}
			}
			// 如果表缓存中有当前类得表
			if (Class2TabMappingCache.hasTab(this.dataTablesClass)) {
				this.Clone(Class2TabMappingCache.getDBTab(this.dataTablesClass), obj);
				if (tab != null) {
					this.setName(tab.name().equals("") ? this.dataTablesClass.getSimpleName()
							: tab.name());
					try {
						if (!this.exist &&  this.isMust) {
							if (this.exists())
								this.exist = true;
							else if (this.create())
								this.exist = true;
						}
					} catch (Exception e) {
						log.error("the datatable [" + this.name + "] is't init ,please try to init database! at class ["
								+ this.dataTablesClass.getName() + "]",e);
					}
				}
				// 如果表缓存中不存在当前表
			} else {
				// 重新解析数据表
				log.debug(
						"================================================================================================================");
				log.debug("current class：" + dataTablesClass.getName());
				// 如果当前类有Tab注解
				if (tab != null) {
					this.setInclude(tab.include());
					this.setMust(tab.isMust());
					this.setName(tab.name().equals("") ? dataTablesClass.getSimpleName()
							:tab.name());
					this.setValue(tab.value());
					if(this.name.indexOf(".") != -1) {
						this.schmel = this.name.substring(0,this.name.indexOf("."));
					}
					this.setAutoUpdate(tab.autoUpdate());
					if (!tab.charset().equals(""))
						this.setCharset(tab.charset());
					if (!tab.collate().equals(""))
						this.setCollate(tab.collate());
				} else {
					log.debug("not annotion configure,try to set default");
					this.setInclude("");
					this.setMust(false);
					this.setName(dataTablesClass.getSimpleName());
					this.setValue("");
					this.setAutoUpdate(false);
				}
				this.setMap(dataTablesClass);
			}
		}catch(Exception e) {
			throw new DataTableInitException("failed to build datatable mapping",e);
		}
	}
	public void init() {
		Connection connection = null;
		try {
			if(schmel == null || schmel.isEmpty()) {
				connection = this.dataSource.getConnection();
				this.schmel = connection.getCatalog();
				if(schmel == null || schmel.isEmpty()) {
					throw new DataTableInitException("could not found schmel for mapping table class "+this.getDataTablesClass());
				}
			}
			Class2TabMappingCache.addTab(this);
			if (!this.exist && this.isMust) {
				if (this.exists())
					this.exist = true;
				else if (this.create())
					this.exist = true;
			}
		} catch (Throwable e) {
			throw new DataTableInitException("failed to init datatable for class "+this.getDataTablesClass(),e);
		}finally {
			if(connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					log.error("failed to close connection data table init " );
					log.error(e.getMessage(),e);
				}
		}
	}
	public void addColumn(Map<String, String> columns) {
		Iterator<String> i = columns.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			this.columns.put(key, columns.get(key));
		}

	}

	public void addColumn(String column, String type) {
		this.columns.put(column, type);
	}

	private void Clone(DataTable tab, Object obj) {
		this.dataSource = tab.dataSource;
		this.dataTablesObject = obj;
		this.name = tab.name;
		this.isMust = tab.isMust;
		this.include = tab.include;
		this.value = tab.value;
		this.dataTablesClass = tab.dataTablesClass;
		this.map = tab.map;
		this.session = tab.session;
		this.AIField = tab.AIField;
		this.Primary_key = tab.Primary_key;
		this.charset = tab.charset;
		this.collate = tab.collate;
		this.exist = tab.exist;
		this.nameMap = tab.nameMap;
		this.columnsArray = tab.columnsArray;
	}

	public boolean create() throws SQLException {
		return create(new Create(this)) > 0;
	}

	public int create(Create create) {
		String sql = create.create();
		return SqlExecutor.execute((connection) -> {
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			return preparedStatement.executeUpdate();
		}, this, sql,null);
	}
	public int delete() {
		String sql = "DROP TABLE " + this.getName();
		return SqlExecutor.execute((connection) -> {
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			return preparedStatement.executeUpdate();
		}, this, sql,null);
	}

	public int delete(Delete delete) {
		String sql = delete.create();
		return SqlExecutor.execute((connection) -> {
			PreparedStatement ps = connection.prepareStatement(sql);
			if (ps != null) {
				this.preparedParameter(ps, delete.getParameters());
				ps.execute();
				QueryCache.getCache().cleanTable(this.getSchmel(), this.getName());// 清理查询缓存
			}
			return ps.executeUpdate();
		}, this, sql,null);
	}

	public boolean delete(Delete delete, Connection connection) throws SQLException {
		connection.prepareStatement(delete.create());
		QueryCache.getCache().cleanTable(this.getSchmel(),this.getName());
		connection.close();
		return true;
	}

	public boolean exists() throws SQLException {
		String table = this.name.substring(this.name.indexOf(".")+1);
		String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='" 
				+ schmel
				+ "' AND TABLE_NAME='"
				+ table
				+ "'";
		return SqlExecutor.execute((connection)->{
			PreparedStatement ps =connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			boolean exist = rs.next();
			rs.close();
			ps.close();
			return exist;
		},this,sql,null);
	}

	private String FilterSql(String sql) {

		return sql.replace("\\", "/");
	}

	public Field getAIField() {
		return AIField;
	}

	public String getCharset() {
		return charset;
	}

	public Class<?> getDataTablesClass() {
		return dataTablesClass;
	}

	public String getCollate() {
		return collate;
	}

	public Map<String, String> getColumns() {
		return columns;
	}

	/**
	 * getDBColumn
	 * 
	 * @param field 属性
	 * @return 映射定义
	 */
	public DBColumn getDBColumn(Field field) {
		if (this.map.containsKey(field))
			return this.map.get(field);
		log.debug("--------------------------------------------------------------------------------------------------");
		log.debug("current field:" + field.getName());
		Column column = field.getAnnotation(Column.class);
		if (column != null && column.ignore() == true) {
			log.debug(field.getName() + " ignore,jump decode the field!");
			return null;
		}
		if (column != null) {
			if (column.Auto_Increment()) {
				this.AIField = field;
				this.Primary_key = field;
			} else if (column.Primary_Key())
				this.Primary_key = field;
			DBColumn db = new DBColumn(field, column);
			this.map.put(field, db);
			this.nameMap.put(db.getName(), db);
			return db;
		}
		DBColumn db = new DBColumn(field);
		this.map.put(field, db);
		this.nameMap.put(db.getName(), db);
		return db;
	}

	public DBColumn getDBColumn(String field) {
		Field f = this.loader.getDeclaredField(field);
		if (f == null)
			throw new RuntimeException("could not find field "+field+" at class" + this.dataTablesClass.getName());
		return getDBColumn(f);
	}
	public DBColumn getDBColumnByColumn(String column){
		return this.nameMap.get(column);
	}

	public int insert(Insert insert) {
		String sql = insert.create();
		return SqlExecutor.execute((connection)->{
			int gk = -1;
			PreparedStatement ps = connection.prepareStatement(insert.create(), java.sql.Statement.RETURN_GENERATED_KEYS);
			if (ps != null) {
				this.preparedParameter(ps,insert.getParameters());
				ps.execute();
				QueryCache.getCache().cleanTable(this.getSchmel(),this.getName());
				ResultSet rs = ps.getGeneratedKeys();
				if (this.AIField != null && rs.next())
					gk = rs.getInt(1);
				else
					gk = 0;
				rs.close();
				ps.close();
			}
			return gk;
		},this,sql,insert.getParameters());
	}
	
	public Object batchInsert(BatchInsert insert,boolean large) {
		String sql = insert.create();
		return SqlExecutor.execute((connection)->{
			Object executeResult = null;
			PreparedStatement ps = connection.prepareStatement(insert.create(), java.sql.Statement.RETURN_GENERATED_KEYS);
			if (ps != null) {
				this.preparedBatchParameter(ps,insert.getParameters(),insert.getColumns().size());
				if(!large)
					executeResult = ps.executeBatch();
				else
					executeResult = ps.executeLargeBatch();
				QueryCache.getCache().cleanTable(this.getSchmel(),this.getName());
				ps.close();
			}
			return executeResult;
		},this,sql,insert.getParameters());
	}
	/**
	 * 预备参数
	 * @param preparedStatement 预备参数
	 * @param parameters 参数
	 * @param columnNum 列数
	 * @throws SQLException ex
	 */
	private void preparedBatchParameter(PreparedStatement preparedStatement, List<Object> parameters, int columnNum) throws SQLException {
		Iterator<Object> iterator = parameters.iterator();
		while (iterator.hasNext()){
			Object[] columnValues = (Object[]) iterator.next();
			for(int j = 0;j < (columnNum<columnValues.length?columnNum:columnValues.length);j++)
				preparedStatement.setObject(j+1, j<columnValues.length?columnValues[j]:null);
			preparedStatement.addBatch();
		}
	}
	/**
	 * 准备参数
	 * @param preparedStatement ps
	 * @param parameters 参数
	 * @throws SQLException ex
	 */
	private void preparedParameter(PreparedStatement preparedStatement,List<Object> parameters) throws SQLException {
		Iterator<Object> iterator = parameters.iterator();
		int i = 0;
		while (iterator.hasNext())
			preparedStatement.setObject(++i, iterator.next());
	}

	public int insert(Insert insert, Connection connection)
			throws IllegalArgumentException, IllegalAccessException, SecurityException, SQLException {
		if (this.dataSource == null)
			throw new RuntimeException("DataTable mapping class " + this.dataTablesClass.getName()
					+ " datasource is null,try to configure the @Tab attribute dataSource ");
		int gk = -1;
		PreparedStatement ps = connection.prepareStatement(insert.create(), 
				java.sql.Statement.RETURN_GENERATED_KEYS);
		if (ps != null) {
			this.preparedParameter(ps,insert.getParameters());
			ps.execute();
			QueryCache.getCache().cleanTable(this.getSchmel(),this.getName());
			ResultSet rs = ps.getGeneratedKeys();
			if (this.AIField != null && rs.next())
				gk = rs.getInt(1);
			else
				gk = 0;
			rs.close();
			ps.close();
		}
		return gk;
	}

	/**
	 * 
	 * @param insert insert
	 * @param obj object
	 * @return object
	 * @throws SQLException ex
	 * @throws IllegalArgumentException ex
	 * @throws IllegalAccessException ex
	 */
	public Object insert(Insert insert, Object obj)
			throws SQLException, IllegalArgumentException, IllegalAccessException {
		String sql = insert.create();
		return SqlExecutor.execute((connection)->{
			PreparedStatement ps = connection.prepareStatement(insert.create());
			QueryCache.getCache().cleanTable(this.getSchmel(),this.getName());
			ResultSet rs = ps.getGeneratedKeys();
			AppClassLoader loader = new AppClassLoader(obj);
			try {
				if (this.AIField != null && rs.next())
					loader.set(AIField, rs.getInt(1));
			} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
				log.error(e.getMessage(),e);
			}
			rs.close();
			ps.close();
			return obj;
		},this,sql,insert.getParameters());
	}

	public Object insertOrUpdate(Insert insert) throws Exception {
		if (this.autoUpdate) {
			if (this.AIField == null) {
				throw new Exception("When a primary key is empty does not automatically update table");
			}
			Query query = new Query(insert.getObj());
			query.addField(this.AIField);
			if (query.query().size() == 1) {
				Update update = new Update(insert.getObj());
				update.addCondition(this.AIField.getName(), this.AIField.get(insert.getObj()));
				update.update();
				query = new Query(insert.getObj());
				return query.query().get(0);
			} else {
				return insert(insert);
			}
		}
		return insert(insert);
	}

	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	public boolean isMust() {
		return isMust;
	}

	public Iterator<Field> iterator() {
		return this.map.keySet().iterator();
	}

	public List<Object> query(Connection connection, String sql) throws SQLException, InstantiationException,
			IllegalAccessException, NoSuchFieldException, SecurityException, IllegalArgumentException {
		PreparedStatement ps = connection.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		List<Object> dataTablesObjects = new ArrayList<Object>();
		while (rs.next()) {
			loader = new AppClassLoader(dataTablesClass, true);
			 this.map.keySet().forEach(field->{
				 try {
					String columnName = this.map.get(field) == null ? field.getName() : this.map.get(field).getName();
					if (columnName.contains("."))
						columnName = columnName.substring(columnName.lastIndexOf(".") + 1);
					if (rs.getObject(columnName) != null)
						loader.set(field, rs.getObject(columnName));
				} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | SecurityException | SQLException e) {
					e.printStackTrace();
				}
			 });
			dataTablesObjects.add(loader.getLoadedObject());
		}
		return dataTablesObjects;
	}

	public List<Object> query(Query query) throws SQLException, InstantiationException, IllegalAccessException,
			NoSuchFieldException, SecurityException, IllegalArgumentException {
		return this.query(query, false);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> query(Query query, boolean mapping) {
		if (this.dataSource == null){
			Query subQuery = query.getSubQuery();
			while(subQuery!=null&&(this.dataSource=subQuery.getDbTab().getDataSource())==null)
				subQuery = subQuery.getSubQuery();
		}
		String sql = query.create();
		List<T> dataTablesObjects = null;
		if(query.isEnableCache() && (dataTablesObjects = QueryCache.getCache().getQuery(query.getDbTab().getSchmel(),
					query.getDbTab().getName(), sql, query.getParameters())) != null) {
			return dataTablesObjects;
		}
		return SqlExecutor.execute((connection)->{
			List<T> result = new ArrayList<T>();
			PreparedStatement ps = connection.prepareStatement(sql);
			preparedParameter(ps, query.getParameters());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				loader = new AppClassLoader(this.dataTablesClass);
				Iterator<Field> iterator = query.getFieldMap().keySet().iterator();
				while (iterator.hasNext()) {
					Field field = iterator.next();
					try {
						String columnName = this.map.get(field) == null ? field.getName()
								: this.map.get(field).getName();
						if (columnName.contains(".") && mapping)
							columnName = columnName.substring(columnName.lastIndexOf(".") + 1);
						if (rs!=null&&rs.getObject(columnName) != null)
							loader.set(field, rs.getObject(columnName));
					} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException
							| IllegalArgumentException | SecurityException | SQLException e) {
						log.error("sql:" + query.create());
						log.error(e.getMessage(),e);
						continue;
					}
				}
				result.add((T) loader.getLoadedObject());
			}
			rs.close();
			ps.close();
			QueryCache.getCache().addCache(this.getSchmel(),this.getName(), sql,query.getParameters(), result);
			return result;
		},this,sql,query.getParameters());
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> query(String sql) {
		return SqlExecutor.execute((connection)->{
			List<T> result = new ArrayList<T>();
			PreparedStatement ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				loader = new AppClassLoader(this.dataTablesClass);
				Iterator<Field> iterator = this.map.keySet().iterator();
				while (iterator.hasNext()) {
					Field field = iterator.next();
					try {
						String columnName = this.map.get(field) == null ? field.getName()
								: this.map.get(field).getName();
						if (columnName.contains("."))
							columnName = columnName.substring(columnName.lastIndexOf(".") + 1);
						if (rs.getObject(columnName) != null)
							loader.set(field, rs.getObject(columnName));
					} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException
							| IllegalArgumentException | SecurityException | SQLException e) {
						log.error("sql:" + sql);
						log.error(e.getMessage(),e);
						continue;
					}
				}
				result.add((T) loader.getLoadedObject());
			}
			rs.close();
			ps.close();
			return result;
		},this,sql,null);
	}

	public List<Object> query(Query query, Connection connection) throws SQLException, InstantiationException,
			IllegalAccessException, NoSuchFieldException, SecurityException, IllegalArgumentException {
		if (this.dataSource == null)
			throw new RuntimeException("DataTable mapping class " + this.dataTablesClass.getName()
					+ " datasource is null,try to configure the @Tab attribute dataSource ");
		PreparedStatement ps = (PreparedStatement) connection
				.prepareStatement(query.create());
		preparedParameter(ps, query.getParameters());
		ResultSet rs = ps.executeQuery();
		List<Object> dataTablesObjects = new ArrayList<Object>();
		while (rs.next()) {
			loader = new AppClassLoader(dataTablesClass, true);
			Iterator<Field> iterator = this.map.keySet().iterator();
			while (iterator.hasNext()) {
				Field field = iterator.next();
				try {
					String columnName = this.map.get(field) == null ? field.getName() : this.map.get(field).getName();
					if (columnName.contains("."))
						columnName = columnName.substring(columnName.lastIndexOf(".") + 1);
					if (rs.getObject(columnName) != null)
						loader.set(field, rs.getObject(columnName));
				} catch (InvocationTargetException | NoSuchMethodException e) {
					e.printStackTrace();
				}
			}
			dataTablesObjects.add(loader.getLoadedObject());
		}
		rs.close();
		return dataTablesObjects;
	}

	private void setMap(Class<?> dataTablesClass) {
		DBColumn dbColumn;
		for (Field field : dataTablesClass.getDeclaredFields()) {
			dbColumn = getDBColumn(field);
			if (dbColumn != null) {
				this.map.put(field, dbColumn);
			}
		}
		this.columnsArray = new String[this.map.size()];
		Iterator<DBColumn> iterator = this.map.values().iterator();
		int i = 0;
		while(iterator.hasNext()){
			this.columnsArray[i++] = iterator.next().getName();
		}
	}
	public String[] getColumnNameArray(){
		return this.columnsArray;
	}
	public List<Object> showTab() {
		String sql = "SELECT * FROM " + this.name;
		return SqlExecutor.execute((connection)->{
			List<Object> objs = new ArrayList<Object>();
			PreparedStatement ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Object obj = this.dataTablesClass.newInstance();
				Iterator<Field> i = this.iterator();
				while (i.hasNext()) {
					Field f = i.next();
					f.setAccessible(true);
					try {
						f.set(obj, rs.getObject(this.map.get(f).getName()));
					} catch (IllegalArgumentException | IllegalAccessException e) {
						f.set(obj, Boolean.parseBoolean((String) rs.getObject(this.map.get(f).getName())));
					}
				}
				objs.add(obj);
			}
			rs.close();
			ps.close();
			return objs;
		},this,sql,null);
	}

	public int update(Update update) {
		String sql = update.create();
		sql = FilterSql(sql);
		int start = 7;
		int end = sql.indexOf(" ", 7);
		String sub = sql.substring(start, end);
		String sqL = sql.replaceFirst(sub, this.name);
		return SqlExecutor.execute((connection)->{
			PreparedStatement ps= connection.prepareStatement(sqL);
			if (ps != null) {
				this.preparedParameter(ps,update.getParameters());
				ps.execute();
				QueryCache.getCache().cleanTable(this.getSchmel(),this.getName());// 清理查询缓存
			}
			return ps.executeUpdate();
		},this,sqL,null);
	}

	public int update(Update update, Connection connection) throws SQLException {
		if (this.dataSource == null)
			throw new RuntimeException("DataTable mapping class " + this.dataTablesClass.getName()
					+ " datasource is null,try to configure the @Tab attribute dataSource ");
		String sql = update.create();
		sql = FilterSql(sql);
		int start = 7;
		int end = sql.indexOf(" ", 7);
		String sub = sql.substring(start, end);
		sql = sql.replaceFirst(sub, this.name);
		QueryCache.getCache().cleanTable(this.getSchmel(),this.getName());// 清理查询缓存
		PreparedStatement ps = connection.prepareStatement(sql);
		return ps.executeUpdate();
	}

	public void setInclude(String include) {
		log.debug("contain orthers xml file" + include);
		this.include = include;
	}

	public void setLoader(AppClassLoader loader) {
		this.loader = loader;
	}

	public void setMap(Map<Field, DBColumn> map) {
		this.map = map;
	}

	public void setAIField(Field aIField) {
		AIField = aIField;
	}

	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public void setDataTablesClass(Class<?> dataTablesClass) {
		this.dataTablesClass = dataTablesClass;
	}

	public void setCollate(String collate) {
		this.collate = collate;
	}

	public void setColumns(Map<String, String> columns) {
		this.columns = columns;
	}

	public void setMust(boolean isMust) {
		this.isMust = isMust;
	}

	public void setName(String name) {
		if (this.dataTablesObject != null) {
			name = StringUtil.decodeVar(name, this.dataTablesObject);
		}
		this.name = name;
	}

	public void setPrimary_key(Field primary_key) {
		Primary_key = primary_key;
	}

	public void setValue(String value) {
		if (this.dataTablesObject != null) {
			value = StringUtil.decodeVar(value, this.dataTablesObject);
		}
		this.value = value;
	}

	public Map<Field, DBColumn> getDBColumns() {
		return this.map;
	}

	public Map<Field, DBColumn> getFieldMap() {
		return map;
	}

	public String getInclude() {
		return include;
	}

	public AppClassLoader getLoader() {
		return loader;
	}

	public String getName() {
		return name;
	}
	public String getSimpleName() {
		return name==null?null:name.substring(0, name.indexOf("."));
	}
	public Field getPrimary_key() {
		return Primary_key;
	}

	public String getValue() {
		return value;
	}

	public void setLoaderObject(Object object) {
		this.loader = new AppClassLoader(object);
	}

	public String getSchmel() {
		return schmel;
	}

	public void setSchmel(String schmel) {
		this.schmel = schmel;
	}
}