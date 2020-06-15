package com.YaNan.frame.jdb.mapper;

import java.lang.reflect.Method;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.jdb.SqlSession;
import com.YaNan.frame.plugin.Plug;
import com.YaNan.frame.plugin.PlugsFactory;
import com.YaNan.frame.plugin.RegisterDescription;
import com.YaNan.frame.plugin.annotations.Register;
import com.YaNan.frame.plugin.autowired.property.Property;
import com.YaNan.frame.plugin.handler.InvokeHandler;
import com.YaNan.frame.utils.PathMatcher;

/**
 * Mapper代理
 * @author yanan
 *
 */
@Register(init="execute")
public class MapperInterfaceProxyBuilder {
	/**
	 * 上下文
	 */
	private SqlSession sqlSession;
	private static final Logger logger = LoggerFactory.getLogger(MapperInterfaceProxyBuilder.class);
	public MapperInterfaceProxyBuilder(SqlSession sqlSession) {
		super();
		this.sqlSession = sqlSession;
	}
	private InvokeHandler generalMapperInterfaceProxy;
	@Property("classpath:")
	private String scanPath[];
	public void execute() {
		generalMapperInterfaceProxy = PlugsFactory.getPlugsInstanceByParamType(GeneralMapperInterfaceProxy.class,new Class<?>[] {SqlSession.class},sqlSession);
		RegisterDescription register = new RegisterDescription(GeneralMapperInterfaceProxy.class);
		//创建一个此注册器的代理容器
		register.createProxyContainer();
		//从组件工厂获取所有的组件，就不必要重新扫描整个类了
		Map<Class<?>, Plug> plugs = PlugsFactory.getInstance().getAllPlugs();
		for(Plug plug : plugs.values()){
			// 查找具有Sql注解的接口
			if(isCurrentProxy(plug)){
				logger.debug("prepared interface class "+plug.getDescription().getPlugClass().getName());
				//将生成的注册描述添加到接口组件
				plug.addRegister(register);
				//设置默认实例为此实例的目标对象的本类实现
				Object proxy =PlugsFactory.getPlugsInstanceByInsClass(plug.getDescription().getPlugClass(),
						GeneralMapperInterfaceProxy.class,sqlSession);
				//对接口方法进行代理，代理对象为本身，目的是为了拦截方法的执行
				for(Method method : plug.getDescription().getPlugClass().getMethods()){
					register.addMethodHandler(method, generalMapperInterfaceProxy);
				}
				//生成代理容器中实例的key
				int hash = RegisterDescription.hash(plug.getDescription().getPlugClass());
				//将代理对象保存到代理容器，则在调用接口的实例实际访问到自己类，其实就是为了给接口一个实例，具体有没有实现其接口并不关心
				register.getProxyContainer().put(hash, proxy);
			}
		}
	}
	private boolean isCurrentProxy(Plug plug) {
		if(scanPath != null) {
			boolean is = false;
			for(String reg : scanPath) {
				if(PathMatcher.match(reg, plug.getDescription().getPlugClass().getName()).isMatch()) {
					is = true;
					break;
				}
			}
			if(!is) {
				return false;
			}
		}
		return this.sqlSession.getContext().hasNamespace(plug.getDescription().getPlugClass().getName());
	}
}
