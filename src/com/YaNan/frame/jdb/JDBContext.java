package com.yanan.frame.jdb;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.async.StrandedTaskReporting;
import com.yanan.frame.jdb.cache.Class2TabMappingCache;
import com.yanan.frame.jdb.entity.BaseMapping;
import com.yanan.frame.jdb.entity.SqlFragmentManger;
import com.yanan.frame.jdb.entity.WrapperMapping;
import com.yanan.frame.jdb.fragment.FragmentBuilder;
import com.yanan.frame.jdb.fragment.SqlFragment;
import com.yanan.utils.beans.xml.XMLHelper;
import com.yanan.frame.plugin.PlugsFactory;
import com.yanan.frame.plugin.annotations.Register;
import com.yanan.frame.plugin.autowired.property.Property;
import com.yanan.frame.plugin.decoder.ResourceDecoder;
import com.yanan.frame.plugin.decoder.StandAbstractResourceDecoder;
import com.yanan.frame.plugin.handler.PlugsHandler;
import com.yanan.utils.resource.AbstractResourceEntry;
import com.yanan.utils.resource.InputStreamResource;
import com.yanan.utils.resource.Resource;
import com.yanan.utils.resource.ResourceManager;
import com.yanan.utils.resource.scanner.PackageScanner;

@Register(afterInstance = "init")
public class JDBContext {
	private static final Logger logger = LoggerFactory.getLogger(JDBContext.class);

	/**
	 * Mapper  位置
	 */
	@Property("jdb.mapper.path")
	private String[] mapperLocations;
	private Map<String, BaseMapping> wrapMap = new HashMap<String, BaseMapping>();
	/**
	 * 扫描资源路径
	 */
	@Property("jdb.template.package")
	private String[] scanPather;

	private List<String> nameSpaces = new ArrayList<String>();
	/**
	 * 数据源
	 */
	private DataSource dataSource;
	public JDBContext(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

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

	public Map<String, BaseMapping> getWrapMap() {
		return wrapMap;
	}

	public void setWrapMap(Map<String, BaseMapping> wrapMap) {
		this.wrapMap = wrapMap;
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

	@SuppressWarnings("unchecked")
	public void init() {
		// 获取mapper配置
		InputStream inputStreamSource = JDBContext.class.getResourceAsStream("./conf/plugin.yc");
		Resource resource = new InputStreamResource("plugin.yc",JDBContext.class.getResource("./conf/plugin.yc").getPath(),inputStreamSource);
		ResourceDecoder<Resource> resourceDecoder = 
				PlugsFactory.getPluginsInstanceByAttributeStrict(ResourceDecoder.class, AbstractResourceEntry.class.getSimpleName());
		resourceDecoder.decodeResource(PlugsFactory.getInstance(), resource);
//		PlugsFactory.getInstance().refresh();
		logger.debug("init hibernate configure!");
		String[] wrappers = mapperLocations;
		if (wrappers == null || wrappers.length == 0)
			return;
		buildMappingTable();
		// 获取所有的wrapper xml文件
		List<AbstractResourceEntry> files = ResourceManager.getResourceList(wrappers[0]);
		logger.debug("get wrap file num : " + files.size());
		files.forEach((file) -> {
			logger.debug("scan wrap file : " + file.getName());
			XMLHelper helper = new XMLHelper(file, WrapperMapping.class);
			List<WrapperMapping> wrapps = helper.read();
			if (wrapps != null && wrapps.size() != 0) {
				List<BaseMapping> baseMapping = wrapps.get(0).getBaseMappings();
				String namespace = wrapps.get(0).getNamespace();
				nameSpaces.add(namespace);
				baseMapping.forEach((mapping) -> {
					mapping.setWrapperMapping(wrapps.get(0));
//					ClassHelper classHelper =ClassHelper.getClassHelper(mapping.getClass());
//					if (classHelper != null) {
//						Method[] methods = classHelper.getDeclaredMethods();
//						boolean find = false;
//						for (Method method : methods) {
//							if (method.getName().equals(mapping.getId())) {
//								find = true;
//								continue;
//							}
//						}
//						if (!find)
//							throw new JDBContextInitException(
//									"wrapper method \"" + mapping.getId() + "\" at interface class \"" + namespace
//											+ "\" is not exists ! at file \"" + file.getPath() + "\"");
//					}
					String sqlId = namespace + "." + mapping.getId();
					wrapMap.put(sqlId, mapping);
					logger.debug("found wrap id " + sqlId + " ; content : " + mapping.getContent().trim());
				});
			}
		});
		this.wrapMap.values().forEach((baseMapping) -> this.buildFragment(baseMapping));
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
		PlugsHandler handler = PlugsFactory.getPluginsHandler(mapping);
		FragmentBuilder fragmentBuilder = PlugsFactory.getPluginsInstanceByAttributeStrict(FragmentBuilder.class,
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
		if(this.scanPather != null && this.scanPather.length != 0)
			logger.debug("jdb template path "+Arrays.toString(this.scanPather));
		if(this.scanPather != null && this.scanPather.length != 0) {
			PackageScanner scanner = new PackageScanner();
			for(String path : this.scanPather) {
				scanner.addScanPath(ResourceManager.getPathExress(path));
			}
			scanner.doScanner((Class<?> cls) -> {
				if (cls.getAnnotation(com.yanan.frame.jdb.annotation.Tab.class) != null) {
					logger.debug("scan template class:" + cls.getName());
					DataTable table = new DataTable(cls);
					table.setDataSource(dataSource);
					table.init();
					Class2TabMappingCache.addTab(table);
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