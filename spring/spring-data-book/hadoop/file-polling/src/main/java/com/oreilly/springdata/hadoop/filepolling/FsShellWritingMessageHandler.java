package com.oreilly.springdata.hadoop.filepolling;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MapAccessor;
import org.springframework.data.hadoop.fs.FsShell;
import org.springframework.data.hadoop.util.PathUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHandlingException;
import org.springframework.integration.file.DefaultFileNameGenerator;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.util.LockRegistry;
import org.springframework.util.Assert;

public class FsShellWritingMessageHandler extends
		AbstractReplyProducingMessageHandler {

	private volatile FileExistsMode fileExistsMode = FileExistsMode.REPLACE;

	private static final Log log = LogFactory
			.getLog(FsShellWritingMessageHandler.class);

	private volatile FileNameGenerator fileNameGenerator = new DefaultFileNameGenerator();

	private final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

	private final Expression destinationDirectoryExpression;

	private volatile boolean autoCreateDirectory = true;

	private volatile boolean deleteSourceFiles;

	private volatile boolean expectReply = false;

	private Configuration configuration;

	private FsShell fsShell;
	
	private volatile boolean generateDestinationDirectory = true;
	
	private volatile String destinationDirectoryFormat = "%1$tY/%1$tm/%1$td/%1$tH/%1$tM/%1$tS";

	/**
	 * Constructor which sets the {@link #destinationDirectoryExpression} using
	 * a {@link LiteralExpression}.
	 * 
	 * @param destinationDirectory
	 *            Must not be null
	 * @see #FsShellWritingMessageHandler(Expression)
	 */
	public FsShellWritingMessageHandler(String destinationDirectory,
			Configuration configuration) {
		Assert.notNull(destinationDirectory,
				"Destination directory must not be null.");		
		this.destinationDirectoryExpression = new LiteralExpression(
				destinationDirectory);
		createFsShell(configuration);
	}

	/**
	 * Constructor which sets the {@link #destinationDirectoryExpression}.
	 * 
	 * @param destinationDirectoryExpression
	 *            Must not be null
	 * @see #FileWritingMessageHandler(String)
	 */
	public FsShellWritingMessageHandler(
			Expression destinationDirectoryExpression) {
		Assert.notNull(destinationDirectoryExpression,
				"Destination directory expression must not be null.");
		this.destinationDirectoryExpression = destinationDirectoryExpression;
		createFsShell(configuration);
	}

	private void createFsShell(Configuration configuration) {
		Assert.notNull(configuration, "Hadoop Configuration must not be null.");
		this.configuration = configuration;
		fsShell = new FsShell(configuration);
	}

	/**
	 * Provide the {@link FileNameGenerator} strategy to use when generating the
	 * destination file's name.
	 */
	public void setFileNameGenerator(FileNameGenerator fileNameGenerator) {
		Assert.notNull(fileNameGenerator, "FileNameGenerator must not be null");
		this.fileNameGenerator = fileNameGenerator;
	}

	/**
	 * Specify whether to delete source Files after writing to the destination
	 * directory. The default is <em>false</em>. When set to <em>true</em>, it
	 * will only have an effect if the inbound Message has a File payload or a
	 * {@link FileHeaders#ORIGINAL_FILE} header value containing either a File
	 * instance or a String representing the original file path.
	 */
	public void setDeleteSourceFiles(boolean deleteSourceFiles) {
		this.deleteSourceFiles = deleteSourceFiles;
	}

	/**
	 * Will set the {@link FileExistsMode} that specifies what will happen in
	 * case the destination exists. For example {@link FileExistsMode#APPEND}
	 * instructs this handler to append data to the existing file rather then
	 * creating a new file for each {@link Message}.
	 * 
	 * If set to {@link FileExistsMode#APPEND}, the adapter will also create a
	 * real instance of the {@link LockRegistry} to ensure that there is no
	 * collisions when multiple threads are writing to the same file.
	 * 
	 * Otherwise the LockRegistry is set to {@link PassThruLockRegistry} which
	 * has no effect.
	 * 
	 * @param fileExistsMode
	 *            Must not be null
	 */
	public void setFileExistsMode(FileExistsMode fileExistsMode) {
		Assert.notNull(fileExistsMode, "'fileExistsMode' must not be null.");
		this.fileExistsMode = fileExistsMode;
	}

	/**
	 * Specify whether a reply Message is expected. If not, this handler will
	 * simply return null for a successful response or throw an Exception for a
	 * non-successful response. The default is true.
	 */
	public void setExpectReply(boolean expectReply) {
		this.expectReply = expectReply;
	}
	
	public void setGenerateDestinationDirectory(boolean generateDestinationDirectory) {
		this.generateDestinationDirectory = generateDestinationDirectory;
	}

	public void setDestinationDirectoryFormat(String destinationDirectoryFormat) {
		this.destinationDirectoryFormat = destinationDirectoryFormat;
	}

	@Override
	public final void onInit() {

		Assert.notNull(configuration, "Hadoop configuration must not be null");

		fsShell = new FsShell(configuration);

		this.evaluationContext.addPropertyAccessor(new MapAccessor());

		final BeanFactory beanFactory = this.getBeanFactory();

		if (beanFactory != null) {
			this.evaluationContext.setBeanResolver(new BeanFactoryResolver(
					beanFactory));
		}

		if (this.destinationDirectoryExpression instanceof LiteralExpression) {
			final Path directory = new Path(
					this.destinationDirectoryExpression.getValue(
							this.evaluationContext, null, String.class));
			validateDestinationDirectory(directory, this.autoCreateDirectory);
		}

	}

	private void validateDestinationDirectory(Path destinationDirectory,
			boolean autoCreateDirectory) {
		// TODO
	}

	@Override
	protected Object handleRequestMessage(Message<?> requestMessage) {
		Assert.notNull(requestMessage, "message must not be null");
		Object payload = requestMessage.getPayload();
		Assert.notNull(payload, "message payload must not be null");
		String generatedFileName = this.fileNameGenerator
				.generateFileName(requestMessage);
		File originalFileFromHeader = this
				.retrieveOriginalFileFromHeader(requestMessage);

		final Path destinationDirectoryToUse = evaluateDestinationDirectoryExpression(requestMessage);
		Path resultFile = new Path(destinationDirectoryToUse, generatedFileName);

		boolean resultFileExists = fsShell.test(resultFile.toUri().toString());

		if (FileExistsMode.FAIL.equals(this.fileExistsMode) && resultFileExists) {
			throw new MessageHandlingException(requestMessage,
					"The destination file already exists at '"
							+ resultFile.toString() + "'.");
		}

		final boolean ignore = FileExistsMode.IGNORE
				.equals(this.fileExistsMode) && resultFileExists;

		if (!ignore) {

			try {
				if (payload instanceof File) {
					resultFile = this.handleFileMessage((File) payload,
							resultFile, resultFileExists);
				} else {
					throw new IllegalArgumentException(
							"unsupported Message payload type ["
									+ payload.getClass().getName() + "]");
				}
			} catch (Exception e) {
				throw new MessageHandlingException(requestMessage,
						"failed to write Message payload to file", e);
			}
		}

		if (!this.expectReply) {
			return null;
		}

		if (resultFile != null) {
			if (originalFileFromHeader == null && payload instanceof File) {
				return MessageBuilder.withPayload(resultFile).setHeader(
						FileHeaders.ORIGINAL_FILE, payload);
			}
		}
		return resultFile;
	}

	/**
	 * Retrieves the File instance from the {@link FileHeaders#ORIGINAL_FILE}
	 * header if available. If the value is not a File instance or a String
	 * representation of a file path, this will return <code>null</code>.
	 */
	private File retrieveOriginalFileFromHeader(Message<?> message) {
		Object value = message.getHeaders().get(FileHeaders.ORIGINAL_FILE);
		if (value instanceof File) {
			return (File) value;
		}
		if (value instanceof String) {
			return new File((String) value);
		}
		return null;
	}

	private Path handleFileMessage(final File sourceFile, Path resultFile,
			boolean resultFileExists) {

		if (FileExistsMode.REPLACE.equals(this.fileExistsMode)
				&& resultFileExists) {
			fsShell.rm(resultFile.toString());
		}
		log.info("sourceFile = " + sourceFile.getAbsolutePath());
		log.info("resultFile = " + resultFile.toString());
		fsShell.copyFromLocal(sourceFile.getAbsolutePath(),
				resultFile.toString());
		cleanUpAfterCopy(sourceFile);
		return resultFile;
	}

	private void cleanUpAfterCopy(File originalFile) {

		if (this.deleteSourceFiles && originalFile != null) {
			originalFile.delete();
		}
	}

	private Path evaluateDestinationDirectoryExpression(Message<?> message) {

		final Path destinationDirectory;

		final Object destinationDirectoryToUse = this.destinationDirectoryExpression
				.getValue(this.evaluationContext, message);

		if (destinationDirectoryToUse == null) {
			throw new IllegalStateException(
					String.format(
							"The provided "
									+ "destinationDirectoryExpression (%s) must not resolve to null.",
							this.destinationDirectoryExpression
									.getExpressionString()));
		} else if (destinationDirectoryToUse instanceof String) {

			String destinationDirectoryPath = (String) destinationDirectoryToUse;

			Assert.hasText(
					destinationDirectoryPath,
					String.format(
							"Unable to resolve destination directory name for the provided Expression '%s'.",
							this.destinationDirectoryExpression
									.getExpressionString()));
			if (this.generateDestinationDirectory) {
				destinationDirectoryPath = destinationDirectoryPath + "/" + PathUtils.format(this.destinationDirectoryFormat);
			}
			destinationDirectory = new Path(destinationDirectoryPath);
		} else if (destinationDirectoryToUse instanceof Path) {
			destinationDirectory = (Path) destinationDirectoryToUse;
		} else {
			throw new IllegalStateException(String.format("The provided "
					+ "destinationDirectoryExpression (%s) must be of type "
					+ "java.io.File or be a String.",
					this.destinationDirectoryExpression.getExpressionString()));
		}

		validateDestinationDirectory(destinationDirectory,
				this.autoCreateDirectory);
		return destinationDirectory;
	}

}
