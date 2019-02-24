package com.oreilly.springdata.hadoop.hive;


public interface PasswordRepository {

	Long count();
	
	void processPasswordFile(String inputFile);

}