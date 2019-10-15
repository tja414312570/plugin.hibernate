package com.YaNan.frame.jdb.orm;

import java.sql.ResultSet;
import java.util.List;

import com.YaNan.frame.jdb.fragment.SqlFragment;
/**
 * Orm构建接口
 * @author yanan
 *
 */
public interface OrmBuilder {
	/**
	 * 构建一个list类型的数据
	 * @param resultSet
	 * @param sqlFragment
	 * @return
	 */
	List<Object> builder(ResultSet resultSet,SqlFragment sqlFragment);
}
