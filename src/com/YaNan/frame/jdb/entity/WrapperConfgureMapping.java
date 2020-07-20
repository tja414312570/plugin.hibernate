package com.yanan.frame.jdb.entity;

import java.util.Arrays;

import com.yanan.frame.jdb.DataBaseConfigure;
import com.yanan.utils.beans.xml.Element;

@Element(name="/Hibernate")
public class WrapperConfgureMapping {
	private String[] wrapper;
	@Element(name="dataBase")
	private DataBaseConfigure[] dataBases;
	@Override
	public String toString() {
		return "WrapperConfgureMapping [wrapper=" + Arrays.toString(wrapper) + ", database=" + Arrays.toString(dataBases) + "]";
	}
	public String[] getWrapper() {
		return wrapper;
	}
	public void setWrapper(String[] wrapper) {
		this.wrapper = wrapper;
	}
	public DataBaseConfigure[] getDataBases() {
		return dataBases;
	}
	public void setDataBases(DataBaseConfigure[] dataBases) {
		this.dataBases = dataBases;
	}
	
}