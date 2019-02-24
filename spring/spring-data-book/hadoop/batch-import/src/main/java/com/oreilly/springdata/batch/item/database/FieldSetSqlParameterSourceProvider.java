package com.oreilly.springdata.batch.item.database;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.springframework.batch.item.database.ItemSqlParameterSourceProvider;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class FieldSetSqlParameterSourceProvider implements
		ItemSqlParameterSourceProvider<FieldSet> {

	@Override
	public SqlParameterSource createSqlParameterSource(FieldSet item) {
		MapSqlParameterSource source = new MapSqlParameterSource();

		Properties props = item.getProperties();
		Set<String> keys = new HashSet(props.keySet());
		for (String key : keys) {
			source.addValue(key, props.get(key));
		}
		
		return source;
	}

}
