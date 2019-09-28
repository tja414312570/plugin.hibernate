package com.YaNan.frame.jdb.builder;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.plugin.annotations.Register;
import com.YaNan.frame.plugin.annotations.Service;
import com.YaNan.frame.plugin.autowired.property.Property;

@Register(method = "init")
public class HibernateBuilder {
	private static final Logger logger = LoggerFactory.getLogger(HibernateBuilder.class);

	@Property("jdb.configLocation")
	private File[] mapperLocations;
	
	/**
	 * 数据源
	 */
	@Service
	private DataSource dataSource;

	private Properties configurationProperties;
	/**
	 * 扫描资源路径
	 */
	@Property("jdb.scanPather")
	private String[] scanPather;
	public File[] getMapperLocations() {
		return mapperLocations;
	}

	public void setMapperLocations(File[] mapperLocations) {
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

}
