package com.yanan.framework.jdb.entity;

import java.util.List;

import com.yanan.utils.beans.xml.AsXml;
import com.yanan.utils.beans.xml.Mapping;
import com.yanan.utils.beans.xml.MappingGroup;
import com.yanan.utils.beans.xml.Value;

public class TagSupport{
	@AsXml
	protected String xml;
	@Value
	protected String value;
	@MappingGroup(support = TagSupport.class,
			value = {
			@Mapping(node = "var", target = Var.class),
			@Mapping(node = "val", target = Val.class),
			@Mapping(node = "trim", target = Trim.class),
			@Mapping(node = "if", target = IF.class),
			@Mapping(node = "foreach", target = ForEach.class),
			@Mapping(node = "include", target = Include.class),
			@Mapping(node = "case", target = Case.class),
			@Mapping(node = "when", target = When.class),
			@Mapping(node = "default", target = Default.class) 
			}
	)
	protected List<TagSupport> tags;
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getXml() {
		return xml;
	}
	public void setXml(String xml) {
		this.xml = xml.replace("<![CDATA[", "").replace("]]>", "");
	}
	public List<TagSupport> getTags() {
		return tags;
	}
	public void setTags(List<TagSupport> tags) {
		this.tags = tags;
	}
	@Override
	public String toString() {
		return "TagSupport [xml=" + xml + ", value=" + value + ", tags=" + tags + "]";
	}
}