package com.yanan.frame.jdb.builder;

import java.sql.ResultSet;

import com.yanan.frame.jdb.mapper.PreparedSql;

public interface ResultBuilder {
	<T> T build(PreparedSql sql,ResultSet set);
}