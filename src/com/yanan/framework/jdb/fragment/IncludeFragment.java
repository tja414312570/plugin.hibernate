package com.yanan.framework.jdb.fragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.framework.jdb.JDBContext;
import com.yanan.framework.jdb.entity.BaseMapping;
import com.yanan.framework.jdb.entity.Include;
import com.yanan.framework.jdb.exception.JDBContextInitException;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.ProxyModel;

@Register(attribute = "*.Include.fragment", model = ProxyModel.CGLIB, signlTon = false)
public class IncludeFragment extends FragmentSet implements FragmentBuilder {
	private static final Logger logger = LoggerFactory.getLogger(JDBContext.class);
	// 逻辑表达式
	private String id;
	private SqlFragment sql;
	//	参数列表
	@Override
	public PreparedFragment prepared(Object object) {
		return super.prepared(object);
	}
	//构建sql片段
	@Override
	public void build(Object wrapper) {
		Include include = (Include) this.tagSupport;
		if(include.getId()!=null&&!include.getId().trim().equals("")){
			this.id = include.getId();
		}else if(include.getValue()!=null&&!include.getValue().trim().equals("")){
			this.id = include.getValue();
		}else{
			throw new JDBContextInitException("mapper \""+this.sqlFragment.getId()+"\" not id attr at file "+this.sqlFragment.getBaseMapping().getXmlFile());
		}
//		if(id.indexOf(".")==-1) {
//			try{
//				 System.out.println("id:"+id);
//				this.sql = this.context.getSqlFragmentManger().getSqlFragment(this.id);
//			}catch (Exception e) {
//			}
//		}
		if(sql==null){
			String nid = id;
			BaseMapping mapping =this.context.getWrapper(nid);
			String ids = "["+id+"]";
			if(mapping==null && id.indexOf(".")==-1) {
				nid = this.sqlFragment.getBaseMapping().getWrapperMapping().getNamespace()+"."+id;
				mapping =this.context.getWrapper(nid);
				ids += ","+"["+nid+"]";
			}
			if(this.sql == null && this.getSqlFragment().getBaseMapping().getParentMapping()!= null) {
				nid = this.sqlFragment.getBaseMapping().getParentMapping().getWrapperMapping().getNamespace()+"."+id;
				mapping =this.context.getWrapper(nid);
				ids += ","+"["+nid+"]";
			}
			if(mapping==null)
				if(this.getSqlFragment().getBaseMapping().getParentMapping() == null)
					throw new JDBContextInitException("mapper \""+ids+"\" could not be found at wrap id \""
						+this.sqlFragment.getId()+"\" at file "
						+this.sqlFragment.getBaseMapping().getXmlFile());
				else
					throw new JDBContextInitException("mapper \""+ids+"\" could not be found at wrap id \""
							+this.sqlFragment.getId()+"\" at file "
							+this.sqlFragment.getBaseMapping().getXmlFile()+",at parent file "
							+this.getSqlFragment().getBaseMapping().getParentMapping().getXmlFile());
			logger.debug("sql fragment \""+this.getSqlFragment().getBaseMapping().getId()+"\" child fragment use id ["+nid+"]");
			mapping.setParentMapping(this.getSqlFragment().getBaseMapping());
			this.sql = this.context.buildFragment(mapping);
		}
		for(String args : sql.getArguments()){
			this.sqlFragment.addParameter(args);
		}
		this.childSet = sql.fragemntSet;
		super.build(wrapper);
	}

}