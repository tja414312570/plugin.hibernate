package com.YaNan.frame.jdb.entity;


import com.YaNan.frame.utils.beans.xml.Attribute;

public class Val extends TagSupport{
	@Attribute
	private String id;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Val [id=" + id + "]";
	}
}
