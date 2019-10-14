package com.YaNan.frame.jdb.database.mapper;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.jdb.builder.HibernateBuilder;
import com.YaNan.frame.jdb.database.DataTable;
import com.YaNan.frame.jdb.database.entity.BaseMapping;
import com.YaNan.frame.jdb.database.entity.SqlFragmentManger;
import com.YaNan.frame.jdb.database.entity.WrapperMapping;
import com.YaNan.frame.jdb.database.exception.HibernateInitException;
import com.YaNan.frame.jdb.database.fragment.FragmentBuilder;
import com.YaNan.frame.jdb.database.fragment.SqlFragment;
import com.YaNan.frame.plugin.PlugsFactory;
import com.YaNan.frame.plugin.PlugsFactory.STREAM_TYPT;
import com.YaNan.frame.plugin.handler.PlugsHandler;
import com.YaNan.frame.utils.beans.xml.XMLHelper;
import com.YaNan.frame.utils.reflect.cache.ClassHelper;
import com.YaNan.frame.utils.resource.PackageScanner;
import com.YaNan.frame.utils.resource.ResourceManager;
import com.YaNan.frame.utils.resource.PackageScanner.ClassInter;

public class SqlSessionMapper {
	private Map<String, BaseMapping> wrapMap = new HashMap<String, BaseMapping>();
	private HibernateBuilder builder;
	private static final Logger logger = LoggerFactory.getLogger(HibernateBuilder.class);
	public SqlSessionMapper(HibernateBuilder builder) {
		this.builder = builder;
	}
	public BaseMapping getSqlMapping(String id) {
		return wrapMap.get(id);
	}
	public void init() {
		// 获取mapper配置
		InputStream pluginConf = HibernateBuilder.class.getResourceAsStream("./conf/plugin.conf");
		PlugsFactory.getInstance().addPlugs(pluginConf, STREAM_TYPT.CONF, null);
		if (!PlugsFactory.getInstance().isAvailable())
			PlugsFactory.init();
		logger.debug("init hibernate configure!");
		String[] wrappers = builder.getMapperLocations();
		if (wrappers == null || wrappers.length == 0)
			return;
		System.out.println(Arrays.toString(wrappers));
		// 获取所有的wrapper xml文件
		List<File> files = ResourceManager.getResource(wrappers[0]);
		System.out.println(files);
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
							throw new HibernateInitException(
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
		SqlFragment sqlFragment = null;
		try {
			sqlFragment = SqlFragmentManger
					.getSqlFragment(mapping.getWrapperMapping().getNamespace() + "." + mapping.getId());
		} catch (Exception e) {
		}
		if (sqlFragment == null) {
			PlugsHandler handler = PlugsFactory.getPlugsHandler(mapping);
			FragmentBuilder fragmentBuilder = PlugsFactory.getPlugsInstanceByAttributeStrict(FragmentBuilder.class,
					handler.getProxyClass().getName() + ".root");
			logger.debug("build " + mapping.getNode().toUpperCase() + " wrapper fragment , wrapper id : \""
					+ mapping.getWrapperMapping().getNamespace() + "." + mapping.getId() + "\" ;");
			sqlFragment = (SqlFragment) fragmentBuilder;
			sqlFragment.setContext(builder);
			fragmentBuilder.build(mapping);
			SqlFragmentManger.addWarp(sqlFragment);
		}
		return sqlFragment;
	}
	public void buildMappingTable() {
		if(builder.getScanPather() != null && builder.getScanPather().length != 0) {
			PackageScanner scanner = new PackageScanner();
			for(String path : builder.getScanPather()) {
				path =  ResourceManager.getPathExress(path);
				scanner.addScanPath(path);
			}
			scanner.doScanner(new ClassInter() {
				@Override
				public void find(Class<?> cls) {
					if (cls.getAnnotation(com.YaNan.frame.jdb.database.annotation.Tab.class) != null) {
						logger.debug("scan hibernate class:" + cls.getName());
						DataTable table = new DataTable(cls);
						table.setDataSource(builder.getDataSource());
						table.init();
						System.out.println(table);
					}
				}
			});
		}
	}
}
