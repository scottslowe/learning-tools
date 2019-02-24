package com.oreilly.springdata.hadoop.pig;

import org.springframework.integration.annotation.Header;
import org.springframework.integration.annotation.ServiceActivator;

public class PasswordService {
	
	private PasswordRepository passwordRepository;
	
	public PasswordService(PasswordRepository passwordRepository) {
		this.passwordRepository = passwordRepository;
	}
	
	@ServiceActivator
	public void process(@Header("hdfs_path") String inputDir) {
		passwordRepository.processPasswordFile(inputDir);
	}
}
