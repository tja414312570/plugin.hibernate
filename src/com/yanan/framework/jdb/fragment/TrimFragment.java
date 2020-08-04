package com.yanan.framework.jdb.fragment;

import com.yanan.framework.jdb.entity.Trim;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.PlugsFactory;
import com.yanan.framework.plugin.ProxyModel;

/**
 * 用于处理Trim标签产生的sql片段
 * @author yanan
 *
 */
@Register(attribute = "*.Trim.fragment", model = ProxyModel.CGLIB, signlTon = false)
public class TrimFragment extends FragmentSet implements FragmentBuilder {
	// 逻辑表达式
	private Trim trim;

	// 参数列表
	private String preparedSql(String sql) {
		sql = sql.trim();
		if (!sql.equals("")) {
			// 先处理替换的字符
			if (trim.getPrefixoverride() != null) {
				String[] strs = trim.getPrefixoverride().trim().split(" ");
				for (String str : strs) {
					if (!str.equals("")) {
						int prePosition = sql.indexOf(str);
						if (prePosition == 0) {
							sql = sql.substring(str.length());
						}
					}
				}
			}
			if (trim.getSuffixoverride() != null) {
				String[] strs = trim.getPrefixoverride().trim().split(" ");
				for (String str : strs) {
					if (!str.equals("")) {
						int prePosition = sql.indexOf(str);
						if (prePosition + str.length() == sql.length()) {
							sql = sql.substring(0, prePosition);
						}
					}
				}
			}
			if(!sql.trim().equals("")){
				if (trim.getPrefix() != null)
					sql = trim.getPrefix() + " " + sql;
				if (trim.getSuffix() != null)
					sql = sql + " " + trim.getSuffix();
			}
		}
		return sql;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PreparedFragment prepared(Object object) {
		PreparedFragment preparedFragment = PlugsFactory.getPluginsInstance(PreparedFragment.class);
		if (this.nextSet != null && this.childSet != null) {
			PreparedFragment child = this.childSet.prepared(object);
			PreparedFragment next = this.nextSet.prepared(object);
			preparedFragment.setSql(this.preparedSql(child.getSql()) + next.getSql());
			preparedFragment.addParameter(child.getArguments(), next.getArguments());
			preparedFragment.addAllVariable(child.getVariable());
			preparedFragment.addAllVariable(next.getVariable());
		} else if (this.childSet != null) {
			PreparedFragment child = this.childSet.prepared(object);
			preparedFragment.setSql(this.preparedSql(child.getSql()));
			preparedFragment.addParameter(child.getArguments());
			preparedFragment.addAllVariable(child.getVariable());
		} else {
			preparedFragment = super.prepared(object);
		}
		return preparedFragment;
	}

	// 构建sql片段
	@Override
	public void build(Object wrapper) {
		this.trim = (Trim) this.tagSupport;
		super.build(wrapper);
	}

}