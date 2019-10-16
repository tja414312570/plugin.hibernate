package com.YaNan.frame.jdb.mapper;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.YaNan.frame.jdb.JDBContext;
import com.YaNan.frame.jdb.SqlSession;
import com.YaNan.frame.jdb.entity.BaseMapping;
import com.YaNan.frame.jdb.mapper.annotations.Param;
import com.YaNan.frame.plugin.PlugsFactory;
import com.YaNan.frame.plugin.ProxyModel;
import com.YaNan.frame.plugin.annotations.Register;
import com.YaNan.frame.plugin.handler.InvokeHandler;
import com.YaNan.frame.plugin.handler.MethodHandler;
import com.YaNan.frame.utils.reflect.cache.ClassHelper;
import com.YaNan.frame.utils.reflect.cache.MethodHelper;
import com.YaNan.frame.utils.reflect.cache.ParameterHelper;

/**
 * 通用Sql映射接口调用实现
 * @author yanan
 *
 */
@Register(priority=1,model = ProxyModel.JDK)
public class GeneralMapperInterfaceProxy implements InvokeHandler{
	private JDBContext context;
	public GeneralMapperInterfaceProxy(JDBContext context) {
		super();
		this.context = context;
	}
	/**
	 * sql的接口进行拦截，才能知道具体调用了接口哪个方法
	 * 对接口方法进行分析，调用具体的sql语句，然后执行sql，什么orm之类的返回出去
	 */
	public void before(MethodHandler methodHandler) {
		//获取SqlSession
		SqlSession sqlSession = PlugsFactory.getPlugsInstance(SqlSession.class);
		//获取类名和方法并组装为sqlId
		String clzz = methodHandler.getPlugsProxy().getInterfaceClass().getName();
		String method = methodHandler.getMethod().getName();
		String sqlId=clzz+"."+method;
		Object[] parameter = null; 
		//此部分代码用于判断是否接口参数中使用了@Param注解
		parameter = decodeParamerters(methodHandler, parameter);
		if(parameter == null)
			parameter = methodHandler.getParameters();
		//从映射中获取sqlId对应的映射，并通过映射获取SQL的类型，对应增删查改
		BaseMapping mapping = context.getWrapper(sqlId);
		if(mapping==null)
			throw new RuntimeException("could not found sql mapper id \""+method+"\" at namespace \""+clzz+"\"");
		if(mapping.getNode().trim().toLowerCase().equals("select")){
			if(com.YaNan.frame.utils.reflect.ClassLoader.implementsOf(methodHandler.getMethod().getReturnType(), List.class)){
				methodHandler.interrupt(sqlSession.selectList(sqlId, parameter));
			}else{
				methodHandler.interrupt(sqlSession.selectOne(sqlId, parameter));
			}
		}else if(mapping.getNode().trim().toLowerCase().equals("insert")){
			methodHandler.interrupt(sqlSession.insert(sqlId, parameter));
		}else if(mapping.getNode().trim().toLowerCase().equals("updaete")){
			methodHandler.interrupt(sqlSession.update(sqlId, parameter));
		}else if(mapping.getNode().trim().toLowerCase().equals("delete")){
			methodHandler.interrupt(sqlSession.delete(sqlId, parameter));
		}
	}
	/**
	 * 查询参数是否有注解，有Param注解则将参数组装成Map返回。
	 * @param methodHandler
	 * @param parameter
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object[] decodeParamerters(MethodHandler methodHandler, Object[] parameter) {
		ClassHelper classHelper = ClassHelper.getClassHelper(methodHandler.getPlugsProxy().getInterfaceClass());
		MethodHelper methodHelper = classHelper.getMethodHelper(methodHandler.getMethod());
		Parameter[] actParameters = methodHelper.getParameters();
		for(int i = 0;i<actParameters.length ; i++) {
			ParameterHelper parameterHelper = methodHelper.getParmeterHelper(actParameters[i]);
			Param param = parameterHelper.getAnnotation(Param.class);
			if(param != null) {
				if(parameter == null) {
					parameter = new Object[1];
					parameter[0] = new HashMap<String,Object>();
				}
				((Map<String,Object>)parameter[0]).put(param.value(), methodHandler.getParameters()[i]);
			}
		}
		return parameter;
	}

	@Override
	public void after(MethodHandler methodHandler) {
	}

	@Override
	public void error(MethodHandler methodHandler, Throwable e) {
		// TODO Auto-generated method stub
		
	}
	public JDBContext getContext() {
		return context;
	}
}
