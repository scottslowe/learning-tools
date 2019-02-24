package com.oreilly.springdata.batch.item.mongodb;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.core.convert.converter.Converter;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class FieldSetConverter implements Converter<FieldSet, DBObject> {

	public DBObject convert(FieldSet fieldSet) {
		DBObject dbo = new BasicDBObject();
		Properties props = fieldSet.getProperties();
		Set<String> keys = new HashSet(props.keySet());
		for (String key : keys) {
			if (key.compareToIgnoreCase("id") == 0) {
				dbo.put("_id", props.get(key));
			} else {
				dbo.put(key, props.get(key));
			}
		}
		return dbo;
	}
}
