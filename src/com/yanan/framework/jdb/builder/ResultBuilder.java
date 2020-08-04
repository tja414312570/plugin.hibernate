package com.yanan.framework.jdb.builder;

import java.sql.ResultSet;

import com.yanan.framework.jdb.mapper.PreparedSql;

public interface ResultBuilder {
	<T> T build(PreparedSql sql,ResultSet set);
}