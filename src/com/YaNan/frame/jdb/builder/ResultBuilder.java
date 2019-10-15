package com.YaNan.frame.jdb.builder;

import java.sql.ResultSet;

import com.YaNan.frame.jdb.mapper.PreparedSql;

public interface ResultBuilder {
	<T> T build(PreparedSql sql,ResultSet set);
}
