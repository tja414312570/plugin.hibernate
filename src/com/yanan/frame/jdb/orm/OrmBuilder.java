package com.yanan.frame.jdb.orm;

import java.sql.ResultSet;
import java.util.List;

import com.yanan.frame.jdb.fragment.SqlFragment;
/**
 * Orm构建接口
 * @author yanan
 *
 */
public interface OrmBuilder {
	/**
	 * 构建一个list类型的数据
	 * @param resultSet 结果集合
	 * @param sqlFragment sql片段
	 * @return 对象集合
	 */
	List<Object> builder(ResultSet resultSet,SqlFragment sqlFragment);
}