package com.yanan.framework.jdb.fragment;

import com.yanan.framework.jdb.entity.Default;
import com.yanan.framework.plugin.ProxyModel;
import com.yanan.framework.plugin.annotations.Register;

@Register(attribute = "*.Default.fragment", model = ProxyModel.CGLIB, signlTon = false)
public class DefaultFragment extends FragmentSet implements FragmentBuilder {
	private Default defaults;
	//	参数列表
	@Override
	public PreparedFragment prepared(Object object) {
		return super.prepared(object);
	}
	//构建sql片段
	@Override
	public void build(Object wrapper) {
		defaults = (Default) this.tagSupport;
		super.build(wrapper);
	}
}