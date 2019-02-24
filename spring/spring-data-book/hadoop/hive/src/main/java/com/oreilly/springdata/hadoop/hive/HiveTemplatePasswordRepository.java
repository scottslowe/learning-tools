package com.oreilly.springdata.hadoop.hive;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.hadoop.hive.HiveOperations;
import org.springframework.stereotype.Repository;

@Repository
public class HiveTemplatePasswordRepository implements PasswordRepository {

	private @Value("${hive.table}") String tableName;
	
	private HiveOperations hiveOperations;
	
	@Autowired
	public HiveTemplatePasswordRepository(HiveOperations hiveOperations) {
		this.hiveOperations = hiveOperations;
	}
	
	@Override
	public Long count() {
		return hiveOperations.queryForLong("select count(*) from " + tableName);
	}

	@Override
	public void processPasswordFile(String inputFile) {
		Map parameters = new HashMap();
		parameters.put("inputFile", inputFile);
		hiveOperations.query("classpath:password-analysis.hql", parameters);	
	}

}
