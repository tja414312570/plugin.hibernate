package com.YaNan.frame.jdb;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.jdb.entity.BaseMapping;
import com.YaNan.frame.jdb.entity.SqlFragmentManger;
import com.YaNan.frame.jdb.entity.WrapperMapping;
import com.YaNan.frame.jdb.exception.JDBContextInitException;
import com.YaNan.frame.jdb.fragment.FragmentBuilder;
import com.YaNan.frame.jdb.fragment.SqlFragment;
import com.YaNan.frame.jdb.mapper.SqlSessionMapper;
import com.YaNan.frame.plugin.PlugsFactory;
import com.YaNan.frame.plugin.PlugsFactory.STREAM_TYPT;
import com.YaNan.frame.plugin.annotations.Register;
import com.YaNan.frame.plugin.annotations.Service;
import com.YaNan.frame.plugin.autowired.property.Property;
import com.YaNan.frame.plugin.handler.PlugsHandler;
import com.YaNan.frame.utils.beans.xml.XMLHelper;
import com.YaNan.frame.utils.reflect.cache.ClassHelper;
import com.YaNan.frame.utils.resource.PackageScanner;
import com.YaNan.frame.utils.resource.ResourceManager;
import com.YaNan.frame.utils.resource.PackageScanner.ClassInter;

@Register(method = "init")
public class JDBContext {
	private static final Logger logger = LoggerFactory.getLogger(JDBContext.class);

	/**
	 * Mapper  位置
	 */
	@Property("jdb.mapperLocations")
	private String[] mapperLocations;
	private Map<String, BaseMapping> wrapMap = new HashMap<String, BaseMapping>();
	/**
	 * 扫描资源路径
	 */
	@Property("jdb.scanPather")
	private String[] scanPather;

	private List<String> nameSpaces = new ArrayList<String>();
	/**
	 * 数据源
	 */
	@Service
	private DataSource dataSource;
	/**
	 * Sql片段容器
	 */
	private SqlFragmentManger sqlFragmentManger;
	
	public SqlFragmentManger getSqlFragmentManger() {
		return sqlFragmentManger;
	}

	public void setSqlFragmentManger(SqlFragmentManger sqlFragmentManger) {
		this.sqlFragmentManger = sqlFragmentManger;
	}

	private SqlSessionMapper sqlSessionMapper;
	public Map<String, BaseMapping> getWrapMap() {
		return wrapMap;
	}

	public void setWrapMap(Map<String, BaseMapping> wrapMap) {
		this.wrapMap = wrapMap;
	}

	public SqlSessionMapper getSqlSessionMapper() {
		return sqlSessionMapper;
	}

	public void setSqlSessionMapper(SqlSessionMapper sqlSessionMapper) {
		this.sqlSessionMapper = sqlSessionMapper;
	}

	public String[] getScanPather() {
		return scanPather;
	}

	public void setScanPather(String[] scanPather) {
		this.scanPather = scanPather;
	}

	private Properties configurationProperties;
	

	public String[] getMapperLocations() {
		return mapperLocations;
	}

	public void init() {
		// 获取mapper配置
		InputStream pluginConf = JDBContext.class.getResourceAsStream("./conf/plugin.conf");
		PlugsFactory.getInstance().addPlugs(pluginConf, STREAM_TYPT.CONF, null);
		if (!PlugsFactory.getInstance().isAvailable())
			PlugsFactory.init();
		this.sqlSessionMapper = new SqlSessionMapper(this);
		logger.debug("init hibernate configure!");
		String[] wrappers = mapperLocations;
		if (wrappers == null || wrappers.length == 0)
			return;
		buildMappingTable();
		// 获取所有的wrapper xml文件
		List<File> files = ResourceManager.getResource(wrappers[0]);
		logger.debug("get wrap file num : " + files.size());
		Iterator<File> fileIterator = files.iterator();
		while (fileIterator.hasNext()) {
			File file = fileIterator.next();
			logger.debug("scan wrap file : " + file.getAbsolutePath());
			XMLHelper helper = new XMLHelper(file, WrapperMapping.class);
			List<WrapperMapping> wrapps = helper.read();
			if (wrapps != null && wrapps.size() != 0) {
				List<BaseMapping> baseMapping = wrapps.get(0).getBaseMappings();
				Iterator<BaseMapping> mappingIterator = baseMapping.iterator();
				String namespace = wrapps.get(0).getNamespace();
				nameSpaces.add(namespace);
				ClassHelper classHelper = null;
				while (mappingIterator.hasNext()) {
					BaseMapping mapping = mappingIterator.next();
					mapping.setWrapperMapping(wrapps.get(0));
					if (classHelper != null) {
						Method[] methods = classHelper.getDeclaredMethods();
						boolean find = false;
						for (Method method : methods) {
							if (method.getName().equals(mapping.getId())) {
								find = true;
								continue;
							}
						}
						if (!find)
							throw new JDBContextInitException(
									"wrapper method \"" + mapping.getId() + "\" at interface class \"" + namespace
											+ "\" is not exists ! at file \"" + file.getAbsolutePath() + "\"");
					}
					String sqlId = namespace + "." + mapping.getId();
					wrapMap.put(sqlId, mapping);
					logger.debug("found wrap id " + sqlId + " ; content : " + mapping.getContent().trim());
				}
			}
		}
		Iterator<BaseMapping> iterator = this.wrapMap.values().iterator();
		while (iterator.hasNext())
			this.buildFragment(iterator.next());
	}

	public SqlFragment buildFragment(BaseMapping mapping) {
		if(sqlFragmentManger == null) {
			synchronized (this) {
				if(sqlFragmentManger == null) {
					sqlFragmentManger = new SqlFragmentManger(this);
				}
			}
		}
		SqlFragment sqlFragment = null;
		PlugsHandler handler = PlugsFactory.getPlugsHandler(mapping);
		FragmentBuilder fragmentBuilder = PlugsFactory.getPlugsInstanceByAttributeStrict(FragmentBuilder.class,
				handler.getProxyClass().getName() + ".root");
		logger.debug("build " + mapping.getNode().toUpperCase() + " wrapper fragment , wrapper id : \""
				+ mapping.getWrapperMapping().getNamespace() + "." + mapping.getId() + "\" ,ref : "+mapping.getWrapperMapping().isRef());
		sqlFragment = (SqlFragment) fragmentBuilder;
		sqlFragment.setContext(this);
		if(!mapping.getWrapperMapping().isRef() || mapping.getParentMapping() != null) {
			fragmentBuilder.build(mapping);
			if(mapping.getParentMapping() == null)
				sqlFragmentManger.addWarp(sqlFragment);
		}
		return sqlFragment;
	}
	public void buildMappingTable() {
		if(this.scanPather != null && this.scanPather.length != 0) {
			PackageScanner scanner = new PackageScanner();
			for(String path : this.scanPather) {
				path =  ResourceManager.getPathExress(path);
				scanner.addScanPath(path);
			}
			scanner.doScanner(new ClassInter() {
				@Override
				public void find(Class<?> cls) {
					if (cls.getAnnotation(com.YaNan.frame.jdb.annotation.Tab.class) != null) {
						logger.debug("scan hibernate class:" + cls.getName());
						DataTable table = new DataTable(cls);
						table.setDataSource(dataSource);
						table.init();
					}
				}
			});
		}
	}
	public void setMapperLocations(String[] mapperLocations) {
		this.mapperLocations = mapperLocations;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Properties getConfigurationProperties() {
		return configurationProperties;
	}

	public void setConfigurationProperties(Properties configurationProperties) {
		this.configurationProperties = configurationProperties;
	}

	public static Logger getLogger() {
		return logger;
	}

	@Override
	public String toString() {
		return "HibernateBuilder [mapperLocations=" + Arrays.toString(mapperLocations) + ", dataSource=" + dataSource
				+ ", configurationProperties=" + configurationProperties + ", scanPather=" + Arrays.toString(scanPather)
				+ "]";
	}

	public BaseMapping getWrapper(String id) {
		return this.wrapMap.get(id);
	}
	public boolean hasNamespace(String namespace) {
		return this.nameSpaces.contains(namespace);
	}

}
