package com.oreilly.springdata.batch.config;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class BatchConfiguration implements ResourceLoaderAware {

	private ResourceLoader resourceLoader;
	
	@Bean
	public MultiResourceItemReader reader() {
		
		MultiResourceItemReader reader = new MultiResourceItemReader();
		Resource[] resources = new Resource[] {resourceLoader.getResource("file:/tmp/import/products-*") };
		reader.setResources(resources);
		
		FlatFileItemReader fileReader = new FlatFileItemReader();
		fileReader.setLinesToSkip(1);
		
		DefaultLineMapper lineMapper = new DefaultLineMapper();
		
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setNames(new String[] { "ID", "NAME", "DESCRIPTION", "PRICE" });
		
		FieldSetMapper fieldSetMapper = new PassThroughFieldSetMapper();
		
		lineMapper.setLineTokenizer(tokenizer);
		lineMapper.setFieldSetMapper(fieldSetMapper);
		
		fileReader.setLineMapper(lineMapper);
		
		reader.setDelegate(fileReader);
		
		return reader;
	}
	
	public MultiResourceItemReader fluentReader() {
		ItemReaderBuilder b = new ItemReaderBuilder();
		ItemReader flatFileReader = b.flatFileItemReader( b.delimitedLineTokenizer("ID,NAME,DESCRIPTION,PRICE"), 
														  b.passThroughFieldSetMapper()	 );
		return b.multiResourceItemReader(flatFileReader, "file:/tmp/*");
	
	}
	
	public MultiResourceItemReader morefluentReader() {
		ItemReaderBuilder b = new ItemReaderBuilder();
		
		return b.multiResourceItemReader("file:/tmp/*", 
										 b.flatFileItemReader( b.delimitedLineTokenizer("ID,NAME,DESCRIPTION,PRICE"), 
												 			   b.passThroughFieldSetMapper()	 ));
	
	}
	

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
	
}
