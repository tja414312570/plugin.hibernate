package com.yanan.framework.jdb.fragment;

import com.yanan.framework.jdb.entity.Case;
import com.yanan.framework.plugin.ProxyModel;
import com.yanan.framework.plugin.annotations.Register;

@Register(attribute = "*.Case.fragment", model = ProxyModel.CGLIB, signlTon = false)
public class CaseFragment extends FragmentSet implements FragmentBuilder {
	private Case cases;
	//	参数列表
	@Override
	public PreparedFragment prepared(Object object) {
		return super.prepared(object);
	}
	//构建sql片段
	@Override
	public void build(Object wrapper) {
		this.cases = (Case) this.tagSupport;
		super.build(wrapper);
		System.out.println(cases);
	}
}