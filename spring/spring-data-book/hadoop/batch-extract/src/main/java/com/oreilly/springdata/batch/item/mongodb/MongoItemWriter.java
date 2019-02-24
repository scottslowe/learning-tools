package com.oreilly.springdata.batch.item.mongodb;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.mongodb.core.MongoOperations;

public class MongoItemWriter implements ItemWriter<Object> , InitializingBean {

	private MongoOperations mongoOperations;
	
	private String collectionName = "/data";
	
	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public MongoItemWriter(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}
	
	@Override
	public void write(List<? extends Object> items) throws Exception {
		mongoOperations.insert(items, collectionName);		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
        if(mongoOperations.collectionExists(collectionName) == false)
        {
        	mongoOperations.createCollection(collectionName);
        }
	}

}
