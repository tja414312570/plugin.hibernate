package com.yanan.framework.jdb.entity;

import com.yanan.utils.beans.xml.Attribute;

public class When extends TagSupport{

	@Attribute
	private String test;
	
	@Attribute(name="break")
	private boolean breaks;

	public String getTest() {
		return test;
	}

	public void setTest(String test) {
		this.test = test;
	}
	public boolean isBreaks() {
		return breaks;
	}

	public void setBreaks(boolean breaks) {
		this.breaks = breaks;
	}
	@Override
	public String toString() {
		return "When [test=" + test + ", breaks=" + breaks + ", xml=" + xml + ", value=" + value + ", tags=" + tags
				+ "]";
	}
	
}