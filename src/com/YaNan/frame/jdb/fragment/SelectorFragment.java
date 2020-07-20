package com.yanan.frame.jdb.fragment;

import java.util.List;

import com.yanan.frame.jdb.entity.SelectorMapping;
import com.yanan.frame.jdb.entity.TagSupport;
import com.yanan.frame.jdb.mapper.PreparedSql;
import com.yanan.frame.plugin.PlugsFactory;
import com.yanan.frame.plugin.ProxyModel;
import com.yanan.frame.plugin.annotations.Register;
import com.yanan.frame.plugin.handler.PlugsHandler;

/**
 * 
 * @author yanan
 * @version 20181009
 */
@Register(attribute = "*.SelectorMapping.root", model = ProxyModel.CGLIB, signlTon = false)
public class SelectorFragment extends SqlFragment implements FragmentBuilder {
	private SelectorMapping selectMapping;

	@Override
	public PreparedFragment prepared(Object object) {
		super.prepared(object);
		if (this.fragemntSet != null)
			return this.fragemntSet.prepared(object);
		return null;
	}

	@Override
	public void build(Object wrapper) {
		super.build(wrapper);
		this.selectMapping = (SelectorMapping) wrapper;
		// create a root FragmentSet
		String sql = this.baseMapping.getXml();
		// 去掉外面的标签,并重组sql语句,不能删除空格
		int index = sql.indexOf(SPLITPREFIX);
		int endex = sql.lastIndexOf(SUFFIX);
		sql = sql.substring(index + 1, endex);
		// 建立一个临时的FragmentSet
		FragmentSet currentFragmentSet;
		FragmentSet preFragmentSet = null;
		// 分隔片段
		// 获取sql里的动态语句标签//并对最外层分隔
		List<TagSupport> tags = selectMapping.getTags();
		if (tags.size() == 0)// 如果没有动态标签
		{
			this.fragemntSet = currentFragmentSet = (FragmentSet) PlugsFactory
					.getPluginsInstanceByAttributeStrict(FragmentBuilder.class, "DEFAULT.fragment");
			currentFragmentSet.setXml(this.baseMapping.getXml());
			currentFragmentSet.setContext(getContext());
			currentFragmentSet.setValue(this.baseMapping.getContent());
			currentFragmentSet.setSqlFragment(this);
			currentFragmentSet.build(null);
		} else {
			for (TagSupport tag : tags) {
				// 获得TagSupport的类型
				PlugsHandler plugsHandler = PlugsFactory.getPluginsHandler(tag);
				Class<?> tagClass = plugsHandler.getProxyClass();
				// 截取类容
				int predex = sql.indexOf(tag.getXml());
				int len = tag.getXml().length();
				String preffix = sql.substring(0, predex);
				if (preffix != null && !preffix.trim().equals("")) {
					currentFragmentSet = (FragmentSet) PlugsFactory
							.getPluginsInstanceByAttributeStrict(FragmentBuilder.class, "DEFAULT.fragment");
					currentFragmentSet.setXml(preffix);
					currentFragmentSet.setValue(preffix);
					currentFragmentSet.setContext(getContext());
					currentFragmentSet.setSqlFragment(this);
					currentFragmentSet.build(null);
					if (this.fragemntSet == null)
						this.fragemntSet = currentFragmentSet;
					if (preFragmentSet != null)
						preFragmentSet.setNextSet(currentFragmentSet);
					preFragmentSet = currentFragmentSet;
				}
				// 根据类型获取对应FragmentSet
				currentFragmentSet = (FragmentSet) PlugsFactory.getPluginsInstanceByAttributeStrict(FragmentBuilder.class,
						tagClass.getName() + ".fragment");
				// 判断根FragmentSet是否为空
				if (this.fragemntSet == null)
					this.fragemntSet = currentFragmentSet;
				if (preFragmentSet != null)
					preFragmentSet.setNextSet(currentFragmentSet);
				preFragmentSet = currentFragmentSet;
				currentFragmentSet.setXml(tag.getXml());
				currentFragmentSet.setValue(tag.getValue());
				currentFragmentSet.setTagSupport(tag);
				currentFragmentSet.setSqlFragment(this);
				currentFragmentSet.setContext(getContext());
				currentFragmentSet.build(null);
				sql = sql.substring(predex + len);
			}
			// 截取类容
			if (sql != null && !sql.trim().equals("")) {
				currentFragmentSet = (FragmentSet) PlugsFactory.getPluginsInstanceByAttributeStrict(FragmentBuilder.class,
						"DEFAULT.fragment");
				currentFragmentSet.setXml(sql);
				currentFragmentSet.setValue(sql);
				currentFragmentSet.setSqlFragment(this);
				currentFragmentSet.setContext(getContext());
				currentFragmentSet.build(null);
				if (this.fragemntSet == null)
					this.fragemntSet = currentFragmentSet;
				if (preFragmentSet != null)
					preFragmentSet.setNextSet(currentFragmentSet);
				preFragmentSet = currentFragmentSet;
			}
		}
	}

	public PreparedSql getPreparedSql(Object parameter) {
		PreparedFragment preparedFragment = this.prepared(parameter);
		PreparedSql preparedSql = new PreparedSql(preparedFragment.getSql(), preparedFragment.getVariable(), this);
		return preparedSql;
	}
}