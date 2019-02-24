package com.oreilly.springdata.batch.config;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

public class ItemReaderBuilder {

	public FlatFileItemReader flatFileItemReader(DelimitedLineTokenizer delimitedLineTokenizer, PassThroughFieldSetMapper passThroughFieldSetMapper) {
		// TODO Auto-generated method stub
		return null;
	}

	public PassThroughFieldSetMapper passThroughFieldSetMapper() {
		// TODO Auto-generated method stub
		return null;
	}

	public DelimitedLineTokenizer delimitedLineTokenizer(String nanes) {
		// TODO Auto-generated method stub
		return null;
	}

	public MultiResourceItemReader multiResourceItemReader(
			ItemReader flatFileReader, String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public MultiResourceItemReader multiResourceItemReader(String string,
			FlatFileItemReader flatFileItemReader) {
		// TODO Auto-generated method stub
		return null;
	}

}
