package com.oreilly.springdata.hadoop.pig;

import java.util.Collection;

public interface PasswordRepository {

	public abstract void processPasswordFile(String inputFile);

	public abstract void processPasswordFiles(Collection<String> inputFiles);

}