package com.yanan.frame.jdb.builder;

import java.sql.ResultSet;

import com.yanan.frame.jdb.mapper.PreparedSql;
import com.yanan.frame.plugin.annotations.Register;

@Register
public class MapResultBuilder implements ResultBuilder{
	@Override
	public <T> T build(PreparedSql sql, ResultSet set) {
		
		return null;
	}

}