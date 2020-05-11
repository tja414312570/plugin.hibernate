package com.YaNan.frame.jdb.entity;

import java.util.List;

import com.YaNan.frame.utils.beans.xml.Attribute;
import com.YaNan.frame.utils.beans.xml.Ignore;
import com.YaNan.frame.utils.beans.xml.NodeName;
import com.YaNan.frame.utils.beans.xml.Value;
import com.YaNan.frame.utils.beans.xml.XmlResource;

public abstract class BaseMapping extends TagSupport{
	@XmlResource
	protected String xmlFile;
	@NodeName
	protected String node;
	@Attribute
	protected String id;
	@Attribute
	protected String resultType;
	@Attribute
	protected String parameterType;
	@Value
	protected String content;
	protected WrapperMapping wrapperMapping;
	@Ignore
	protected BaseMapping parentMapping;
	public List<TagSupport> getTags() {
		return tags;
	}
	public void setTags(List<TagSupport> tags) {
		this.tags = tags;
	}
	public String getXml() {
		return xml;
	}
	public void setXml(String xml) {
		this.xml = xml.replace("<![CDATA[", "").replace("]]>", "");
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public String getResultType() {
		return resultType;
	}
	public void setResultType(String resultType) {
		this.resultType = resultType;
	}
	public String getParameterType() {
		return parameterType;
	}
	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getXmlFile() {
		return xmlFile;
	}

	public void setXmlFile(String xmlFile) {
		this.xmlFile = xmlFile;
	}
	public WrapperMapping getWrapperMapping() {
		return wrapperMapping;
	}
	public void setWrapperMapping(WrapperMapping wrapperMapping) {
		this.wrapperMapping = wrapperMapping;
	}
	public BaseMapping getParentMapping() {
		return parentMapping;
	}
	public void setParentMapping(BaseMapping parentMapping) {
		this.parentMapping = parentMapping;
	}
}
