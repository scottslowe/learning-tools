package com.oreilly.springdata.hadoop.pig;

import java.util.Collection;
import java.util.Properties;

import org.springframework.data.hadoop.pig.PigOperations;
import org.springframework.data.hadoop.util.PathUtils;
import org.springframework.util.Assert;

public class PigPasswordRepository implements PasswordRepository {

	private PigOperations pigOperations;
	
	private String pigScript = "classpath:password-analysis.pig";
	
	public PigPasswordRepository(PigOperations pigOperations) {
		Assert.notNull(pigOperations);
		this.pigOperations = pigOperations;
	}
		
	public void setPigScript(String pigScript) {
		this.pigScript = pigScript;
	}
	
	@Override
	public void processPasswordFile(String inputFile) {
		Assert.notNull(inputFile);
		String outputDir = 
				PathUtils.format("/data/password-repo/output/%1$tY/%1$tm/%1$td/%1$tH/%1$tM/%1$tS");
		Properties scriptParameters = new Properties();
		scriptParameters.put("inputDir", inputFile);
		scriptParameters.put("outputDir", outputDir);		
		pigOperations.executeScript(pigScript, scriptParameters);
	}
	
	@Override
	public void processPasswordFiles(Collection<String> inputFiles) {
		for (String inputFile : inputFiles) {
			processPasswordFile(inputFile);
		}
	}
}
