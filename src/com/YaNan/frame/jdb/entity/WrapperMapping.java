package com.YaNan.frame.jdb.entity;

import java.util.List;

import com.YaNan.frame.utils.beans.xml.Attribute;
import com.YaNan.frame.utils.beans.xml.ENCODEING;
import com.YaNan.frame.utils.beans.xml.Element;
import com.YaNan.frame.utils.beans.xml.Encode;
import com.YaNan.frame.utils.beans.xml.FieldType;
import com.YaNan.frame.utils.beans.xml.FieldTypes;
import com.YaNan.frame.utils.beans.xml.Mapping;

@Encode(ENCODEING.UTF16)
@Element(name="wrapper")
@FieldType(FieldTypes.ALL)
public class WrapperMapping{
	@Attribute
	private String namespace;
	@Attribute
	private String database;
	@Attribute
	private boolean ref;
	@Mapping(node = "select", target = SelectorMapping.class)
	@Mapping(node = "insert", target = SelectorMapping.class)
	@Mapping(node = "update", target = SelectorMapping.class)
	@Mapping(node = "delete", target = SelectorMapping.class)
	@Mapping(node = "sql", target = SelectorMapping.class)
	private List<BaseMapping> baseMappings;
	public boolean isRef() {
		return ref;
	}
	public void setRef(boolean ref) {
		this.ref = ref;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public String getDatabase() {
		return database;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
	@Override
	public String toString() {
		return "WrapperMapping [namespace=" + namespace + ", database=" + database + ", baseMappings=" + baseMappings + "]";
	}
	public List<BaseMapping> getBaseMappings() {
		return baseMappings;
	}
	public void setBaseMappings(List<BaseMapping> baseMappings) {
		this.baseMappings = baseMappings;
	}
}
