package com.yanan.framework.jdb.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yanan.framework.jdb.JDBContext;
import com.yanan.framework.jdb.SqlSession;
import com.yanan.framework.jdb.exception.SqlExecuteException;
import com.yanan.framework.jdb.fragment.SqlFragment;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.utils.reflect.ParameterUtils;
import com.yanan.utils.reflect.cache.ClassHelper;
/**
 * 框架默认sqlsession的实现类
 * @author yanan
 *
 */
@Register
public class DefaultSqlSessionExecuter implements SqlSession{
	private JDBContext context;
	public DefaultSqlSessionExecuter(JDBContext context) {
		super();
		this.context = context;
	}
	/**
	 * 从数据库中查询数据
	 * 除非为java基础数据类型和String，否则参数只有第一个有效，无须再mapper中定义参数类型
	 * !该查询条件表明满足该语句的数据在数据库中最多只有一条，否则会抛出异常
	 */
	@Override
	public <T> T selectOne(String sqlId, Object... parameters) {
		Object parameter = checkParams(parameters);
		SqlFragment frag = context.getSqlFragmentManger().getSqlFragment(sqlId);
		PreparedSql pre = frag.getPreparedSql(parameter);
		try {
			return pre.queryOne();
		} catch (SQLException e) {
			throw new SqlExecuteException("faild to execute query \""+sqlId+"\"",e);
		}
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object checkParams(Object... params) {
		if(params!=null && params.length>1) {
//			for(int i = 0;i<params.length-1;i++) {
//				for(int j = i+1;j<params.length;j++) {
//					if(params[j] != null && params[i] != null && !com.yanan.utils.reflect.AppClassLoader.isBaseType(params[i].getClass()) && params[j].getClass().equals(params[i].getClass())) {
//						throw new JDBSqlExecuteException("could not build parameter map");
//					}
//				}
//			}
			Map<String,Object> paramMap = new HashMap<>();;
			for(int i = 0;i<params.length;i++) {
				if(i==0 && params[0] != null) {
					if(com.yanan.utils.reflect.
							ReflectUtils.implementsOf(params[0].getClass(), Map.class))
					paramMap.putAll((Map)params[0]);
					if(params[i] == null ||
							ParameterUtils.isBaseType(params[0].getClass())){
						paramMap.put("parameter_"+i, params[i]);
					}else {
						Field[] fields = ClassHelper.getClassHelper(params[0].getClass()).getAllFields();
						com.yanan.utils.reflect.
						AppClassLoader loader =new com.yanan.utils.reflect.
								AppClassLoader(params[0]);
						for(Field field : fields) {
							try {
								paramMap.put(field.getName(), loader.get(field));
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
									| NoSuchMethodException | SecurityException e) {
								throw new SqlExecuteException("failed to build param map for field "+field,e);
							}
						}
						
					}
				}else {
					if(params[i] == null ||
							ParameterUtils.isBaseType(params[0].getClass())){
						paramMap.put("parameter_"+i, params[i]);
					}else {
						paramMap.put(params[i].getClass().getSimpleName(), params[i]);
					}
				}
			}
			return paramMap;
		}else
			return params == null ? null:params[0];
	}
	/**
	 * 从数据库中查询结果集，需要从mapper中定义返回类型，返回类型为一个list或其实现类。
	 * 除非为java基础数据类型和String，否则参数只有第一个有效，无须再mapper中定义参数类型
	 */
	@Override
	public <T> List<T> selectList(String sqlId, Object... params) {
		Object parameter = checkParams(params);
		SqlFragment frag = context.getSqlFragmentManger().getSqlFragment(sqlId);
		PreparedSql pre = frag.getPreparedSql(parameter);
		try {
			return pre.query();
		} catch (SQLException e) {
			throw new SqlExecuteException("faild to execute query \""+sqlId+"\"",e);
		}
	}
	@Override
	public <T> T insert(String sqlId, Object... parameters) {
		Object parameter = checkParams(parameters);
		SqlFragment frag = context.getSqlFragmentManger().getSqlFragment(sqlId);
		PreparedSql pre = frag.getPreparedSql(parameter);
		try {
			return pre.insert();
		} catch (SQLException e) {
			throw new SqlExecuteException("faild to execute query \""+sqlId+"\"",e);
		}
	}
	@Override
	public <T> List<T> insertBatch(String sqlId, Object... parameters) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public <T> T update(String sqlId, Object... parameters) {
		Object parameter = checkParams(parameters);
		SqlFragment frag = context.getSqlFragmentManger().getSqlFragment(sqlId);
		PreparedSql pre = frag.getPreparedSql(parameter);
		try {
			return pre.update();
		} catch (SQLException e) {
			throw new SqlExecuteException("faild to execute query \""+sqlId+"\"",e);
		}
	}
	@Override
	public int delete(String sqlId, Object... parameters) {
		Object parameter = checkParams(parameters);
		SqlFragment frag = context.getSqlFragmentManger().getSqlFragment(sqlId);
		PreparedSql pre = frag.getPreparedSql(parameter);
		try {
			return pre.update();
		} catch (SQLException e) {
			throw new SqlExecuteException("faild to execute query \""+sqlId+"\"",e);
		}
	}
	@Override
	public JDBContext getContext() {
		return this.context;
	}

}