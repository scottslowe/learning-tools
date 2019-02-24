package com.oreilly.springdata.hadoop.hive;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.service.HiveClient;
import org.apache.hadoop.hive.service.HiveServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.hadoop.hive.HiveClientFactory;
import org.springframework.stereotype.Repository;

@Repository
public class HivePasswordRepository implements PasswordRepository {

	private static final Log logger = LogFactory
			.getLog(HivePasswordRepository.class);

	private HiveClientFactory hiveClientFactory;

	private @Value("${hive.table}")
	String tableName;

	private @Value("${hive.host}")
	String host;

	private @Value("${hive.port}")
	int port;

	@Autowired
	public HivePasswordRepository(HiveClientFactory hiveClientFactory) {
		this.hiveClientFactory = hiveClientFactory;
	}
	
	@Override
	public Long count() {
		HiveClient hiveClient = createHiveClient();
		try {
			hiveClient.execute("select count(*) from " + tableName);
			return Long.parseLong(hiveClient.fetchOne());
			// checked exceptions
		} catch (HiveServerException ex) {
			throw translateExcpetion(ex);
		} catch (org.apache.thrift.TException tex) {
			throw translateExcpetion(tex);
		} finally {
			try {
				hiveClient.shutdown();
			} catch (org.apache.thrift.TException tex) {
				logger.debug(
						"Unexpected exception on shutting down HiveClient", tex);
			}
		}
	}
	
	@Override
	public void processPasswordFile(String inputFile) {
		//
	}

	protected HiveClient createHiveClient() {
		return hiveClientFactory.getHiveClient();
	}

	private RuntimeException translateExcpetion(Exception ex) {
		return new RuntimeException(ex);
	}


}
