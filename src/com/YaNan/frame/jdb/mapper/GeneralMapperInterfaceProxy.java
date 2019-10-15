package com.YaNan.frame.jdb.mapper;

import java.util.List;

import com.YaNan.frame.jdb.JDBContext;
import com.YaNan.frame.jdb.SqlSession;
import com.YaNan.frame.jdb.entity.BaseMapping;
import com.YaNan.frame.plugin.PlugsFactory;
import com.YaNan.frame.plugin.ProxyModel;
import com.YaNan.frame.plugin.annotations.Register;
import com.YaNan.frame.plugin.handler.InvokeHandler;
import com.YaNan.frame.plugin.handler.MethodHandler;

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
		//从映射中获取sqlId对应的映射，并通过映射获取SQL的类型，对应增删查改
		BaseMapping mapping = context.getWrapper(sqlId);
		if(mapping==null)
			throw new RuntimeException("could not found sql wrapper id \""+method+"\" at namespace \""+clzz+"\"");
		if(mapping.getNode().trim().toLowerCase().equals("select")){
			if(com.YaNan.frame.utils.reflect.ClassLoader.implementsOf(methodHandler.getMethod().getReturnType(), List.class)){
				methodHandler.interrupt(sqlSession.selectList(sqlId, methodHandler.getParameters()));
			}else{
				methodHandler.interrupt(sqlSession.selectOne(sqlId, methodHandler.getParameters()));
			}
		}else if(mapping.getNode().trim().toLowerCase().equals("insert")){
			methodHandler.interrupt(sqlSession.insert(sqlId, methodHandler.getParameters()));
		}else if(mapping.getNode().trim().toLowerCase().equals("updaete")){
			methodHandler.interrupt(sqlSession.update(sqlId, methodHandler.getParameters()));
		}else if(mapping.getNode().trim().toLowerCase().equals("delete")){
			methodHandler.interrupt(sqlSession.delete(sqlId, methodHandler.getParameters()));
		}
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
