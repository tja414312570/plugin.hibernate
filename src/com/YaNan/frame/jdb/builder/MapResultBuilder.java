package com.YaNan.frame.jdb.builder;

import java.sql.ResultSet;

import com.YaNan.frame.jdb.database.mapper.PreparedSql;
import com.YaNan.frame.plugin.annotations.Register;

@Register
public class MapResultBuilder implements ResultBuilder{
	@Override
	public <T> T build(PreparedSql sql, ResultSet set) {
		
		return null;
	}

}
