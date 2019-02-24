package com.oreilly.springdata.hadoop.hive;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.hadoop.hive.HiveOperations;
import org.springframework.data.hadoop.hive.HiveScript;
import org.springframework.util.Assert;

public class AnalysisService implements ResourceLoaderAware {

	private HiveOperations hiveOperations;
	private String scriptResource;
	private String hiveContribJar;
	private String localInPath;
	private ResourceLoader resourceLoader;
	
	public void setLocalInPath(String localInPath) {
		this.localInPath = localInPath;
	}

	public AnalysisService(HiveOperations hiveOperations) {
		Assert.notNull(hiveOperations);
		this.hiveOperations = hiveOperations;
	}
	
	public void setScriptResource(String scriptResource) {
		this.scriptResource = scriptResource;
	}
	
	public void setHiveContribJar(String hiveContribJar) {
		this.hiveContribJar = hiveContribJar;
	}


	public void performAnalysis() throws Exception {
		Map parameters = new HashMap();
		parameters.put("hiveContribJar", hiveContribJar);
		parameters.put("localInPath", localInPath);
		//hiveOperations.query(scriptResource, parameters);
		Resource res = resourceLoader.getResource(scriptResource);
		hiveOperations.executeScript(new HiveScript(res,parameters)  );
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
}
