package com.yanan.framework.jdb.builder;

import java.sql.ResultSet;

import com.yanan.framework.jdb.mapper.PreparedSql;
import com.yanan.framework.plugin.annotations.Register;

@Register
public class MapResultBuilder implements ResultBuilder{
	@Override
	public <T> T build(PreparedSql sql, ResultSet set) {
		
		return null;
	}

}