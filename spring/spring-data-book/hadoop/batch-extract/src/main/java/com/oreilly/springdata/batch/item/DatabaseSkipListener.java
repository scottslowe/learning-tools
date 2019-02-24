/**
 * 
 */
package com.oreilly.springdata.batch.item;

import javax.sql.DataSource;

import org.springframework.batch.core.listener.SkipListenerSupport;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author acogoluegnes
 *
 */
public class DatabaseSkipListener extends SkipListenerSupport  {

	private JdbcTemplate jdbcTemplate;
	private MultiResourceItemReader multiResourceItemReader;
	
	public DatabaseSkipListener(DataSource datasource) {
		this.jdbcTemplate = new JdbcTemplate(datasource);
		//this.multiResourceItemReader = reader;
	}
	
	@Override
	public void onSkipInRead(Throwable t) {
		if(t instanceof FlatFileParseException) {
			FlatFileParseException ffpe = (FlatFileParseException) t;
			jdbcTemplate.update(
				"insert into skipped_product (line,line_number) values (?,?)",
				ffpe.getInput(),ffpe.getLineNumber()
				//multiResourceItemReader.getCurrentResource().getFilename(), ffpe.getInput(),ffpe.getLineNumber()
			);
		}
	}
	
}
