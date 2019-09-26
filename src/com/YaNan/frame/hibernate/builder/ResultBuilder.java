package com.YaNan.frame.hibernate.builder;

import java.sql.ResultSet;

import com.YaNan.frame.hibernate.database.mapper.PreparedSql;

public interface ResultBuilder {
	<T> T build(PreparedSql sql,ResultSet set);
}
