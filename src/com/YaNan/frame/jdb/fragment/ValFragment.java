package com.YaNan.frame.jdb.fragment;

import com.YaNan.frame.jdb.cache.GeneralCache;
import com.YaNan.frame.jdb.entity.Val;
import com.YaNan.frame.jdb.exception.JDBContextInitException;
import com.YaNan.frame.plugin.ProxyModel;
import com.YaNan.frame.plugin.annotations.Register;
import com.YaNan.frame.utils.asserts.Assert;

@Register(attribute = "*.Val.fragment", model = ProxyModel.CGLIB, signlTon = false)
public class ValFragment extends FragmentSet implements FragmentBuilder {
	private String id;
	private VarFragment varFragment;
	//	参数列表
	@Override
	public PreparedFragment prepared(Object object) {
		Val val = (Val) this.tagSupport;
		if(val.getId()!=null&&!val.getId().trim().equals("")){
			this.id = this.sqlFragment.getId()+"."+val.getId();
		}else if(val.getValue()!=null&&!val.getValue().trim().equals("")){
			this.id = this.sqlFragment.getId()+"."+val.getValue();
		}else{
			throw new JDBContextInitException("mapper variable \""+this.sqlFragment.getId()+"\" not id attr at file "+this.sqlFragment.getBaseMapping().getXmlFile());
		}
		varFragment = GeneralCache.getCache().get(this.id);
		Assert.isNull(varFragment, new JDBContextInitException("mapper variable \""+id+"\" could not be found at wrap id \""
				+this.sqlFragment.getId()+"\" at file "
				+this.sqlFragment.getBaseMapping().getXmlFile()));
		//构建子项目
		return varFragment.preparedVar(object);
//		return super.prepared(object);
	}
	//构建sql片段
	@Override
	public void build(Object wrapper) {
//		if(id.indexOf(".")==-1) {
//			try{
//				 System.out.println("id:"+id);
//				this.sql = this.context.getSqlFragmentManger().getSqlFragment(this.id);
//			}catch (Exception e) {
//			}
//		}
		
		super.build(wrapper);
		return;
//		super.build(wrapper);
	}
}
