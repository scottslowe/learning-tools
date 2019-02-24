package com.oreilly.springdata.hadoop.filepolling;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mortbay.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.fs.FsShell;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHandlingException;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.message.GenericMessage;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.FileCopyUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/hadoop-context.xml" })
public class FsShellWritingMessageHandlerTests {

	static final String DEFAULT_ENCODING = "UTF-8";

	static final String SAMPLE_CONTENT = "HelloWorld";

	private File sourceFile;

	private FsShellWritingMessageHandler handler;

	private String outputDirectory = "/user/mpollack/tmp/output";

	@Autowired
	private Configuration configuration;

	private FsShell fsShell;

	@Rule
	public TemporaryFolder temp = new TemporaryFolder() {
		public void create() throws IOException {
			super.create();
			sourceFile = temp.newFile("sourceFile.txt");
			Log.info("Generated source file = " + sourceFile);
			FileCopyUtils.copy(SAMPLE_CONTENT.getBytes(DEFAULT_ENCODING),
					new FileOutputStream(sourceFile, false));
		}
	};

	@Before
	public void init() {
		// TODO need to create a way to get a tmp directory in HDFS
		handler = new FsShellWritingMessageHandler(outputDirectory,
				configuration);
		fsShell = new FsShell(configuration);
		fsShell.rmr(outputDirectory);
	}

	@Test(expected = MessageHandlingException.class)
	public void unsupportedType() throws Exception {
		handler.handleMessage(new GenericMessage<Integer>(99));

		Collection<FileStatus> fileStatusCollection = fsShell
				.ls(outputDirectory);
		assertTrue(fileStatusCollection.isEmpty());
	}

	@Test
	public void filePayloadCopiedToNewFile() throws Exception {
		Message<?> message = MessageBuilder.withPayload(sourceFile).build();
		QueueChannel output = new QueueChannel();
		handler.setOutputChannel(output);
		handler.handleMessage(message);
		Message<?> result = output.receive(0);
		assertFileContentIsMatching(result);
	}

	@Test
	public void deleteFilesFalseByDefault() throws Exception {
		QueueChannel output = new QueueChannel();
		handler.setOutputChannel(output);
		Message<?> message = MessageBuilder.withPayload(sourceFile).build();
		handler.handleMessage(message);
		Message<?> result = output.receive(0);
		assertFileContentIsMatching(result);
		assertTrue(sourceFile.exists());
	}

	@Test
	public void deleteFilesTrueWithFilePayload() throws Exception {
		QueueChannel output = new QueueChannel();
		handler.setDeleteSourceFiles(true);
		handler.setOutputChannel(output);
		Message<?> message = MessageBuilder.withPayload(sourceFile).build();
		handler.handleMessage(message);
		Message<?> result = output.receive(0);
		assertFileContentIsMatching(result);
		assertFalse(sourceFile.exists());
	}

	void assertFileContentIsMatching(Message<?> result) throws IOException,
			UnsupportedEncodingException {
		assertThat(result, is(notNullValue()));

		assertThat(result.getPayload(), is(instanceOf(Path.class)));
		Path destFile = (Path) result.getPayload();
		assertNotSame(destFile, sourceFile);
		String uri = destFile.toUri().toString();
		assertThat(fsShell.test(uri), is(true));

		FileSystem hdpFileSystem = destFile.getFileSystem(configuration);
		assertThat(hdpFileSystem.isFile(destFile), is(true));

		Collection<String> content = fsShell.text(uri);
		assertThat(content.isEmpty(), is(false));
		assertThat(content.iterator().next(), is(SAMPLE_CONTENT));

	}

}
