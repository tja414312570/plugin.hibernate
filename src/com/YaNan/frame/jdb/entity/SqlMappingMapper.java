package com.YaNan.frame.jdb.entity;

import java.util.List;

import com.YaNan.frame.jdb.fragment.SqlFragment;

/**
 * sql映射mapper
 * @author yanan
 *
 */
public class SqlMappingMapper {
	//数据库
	private String datasource;
	//命名空间 
	private String namespace;
	//里面包含的语句片段
	private List<SqlFragment> sqlFragmentList;
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public List<SqlFragment> getSqlFragmentList() {
		return sqlFragmentList;
	}
	public void setSqlFragmentList(List<SqlFragment> sqlFragmentList) {
		this.sqlFragmentList = sqlFragmentList;
	}
	public SqlMappingMapper(String datasource, String namespace) {
		super();
		this.datasource = datasource;
		this.namespace = namespace;
	}
	public SqlMappingMapper() {
		super();
	}
	@Override
	public String toString() {
		return "SqlMappingMapper [database=" + datasource + ", namespace=" + namespace + ", sqlFragmentList="
				+ sqlFragmentList + "]";
	}
	public String getDatasource() {
		return datasource;
	}
	public void setDatasource(String datasource) {
		this.datasource = datasource;
	}
	
}
