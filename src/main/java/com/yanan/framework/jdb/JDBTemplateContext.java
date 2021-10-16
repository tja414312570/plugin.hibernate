package com.yanan.framework.jdb;

import java.util.Arrays;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.framework.plugin.autowired.property.Property;
import com.yanan.utils.asserts.Assert;
import com.yanan.utils.resource.scanner.PackageScanner;


public class JDBTemplateContext {
	@Property("")
	private String[] dirs;
	public String[] getDirs() {
		return dirs;
	}
	public void setDirs(String[] dirs) {
		this.dirs = dirs;
	}
	private DataSource dataSource;
	private static final Logger logger = LoggerFactory.getLogger(JDBTemplateContext.class);
	public JDBTemplateContext(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	void init() {
		Assert.isNotNull(dataSource, "the data source is null");
		logger.debug("scan jdb template path "+Arrays.toString(dirs));
		if(dirs != null && dirs.length >0) {
			for(String dir : dirs) {
				PackageScanner packageScanner = new PackageScanner(dir);
				packageScanner.doScanner(clazz->{
					System.out.println(clazz);
				});
			}
		}
	}

}