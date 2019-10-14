package com.YaNan.frame.jdb.database.DBInterface;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YaNan.frame.jdb.database.DataTable;

public abstract class OperateImplement {
	/**
	 * 数据结构
	 */
	protected DataTable dataTables;
	/**
	 * 参数列表
	 */
	protected List<Object> parameters = new LinkedList<Object>();
	/**
	 * 预编译生成sql
	 */
	protected String preparedSql;
	public String getPreparedSql() {
		if(this.preparedSql==null)
			this.preparedSql = this.create();
		return preparedSql;
	}
	public void setPreparedSql(String preparedSql) {
		this.preparedSql = preparedSql;
	}
	public abstract String create();
	public DataTable getDbTab() {
		return dataTables;
	}
	public void setDbTab(DataTable dataTables) {
		this.dataTables = dataTables;
	}
	public String hashString(){
		return this.hashCode()+"";
	}
	public List<Object> getParameters() {
		return parameters;
	}
	public void addParameters(Object parameter){
		parameters.add(parameter);
	}
	public void setParameters(List<Object> parameters) {
		this.parameters = parameters;
	}
}
