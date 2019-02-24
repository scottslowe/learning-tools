package com.oreilly.springdata.integration.ip.syslog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;
import org.springframework.integration.ip.tcp.serializer.SoftEndOfStreamException;

/**
 * Reads data in an InputStream to a byte[]; data must be terminated by \n
 * (not included in resulting byte[]).
 * Writes a byte[] to an OutputStream and adds \n.
 *
 * @author Gary Russell
 * @since 2.0
 */
public class ByteArrayLfSerializer extends AbstractByteArraySerializer {

	private static final byte[] LF = "\n".getBytes();

	/**
	 * Reads the data in the inputstream to a byte[]. Data must be terminated
	 * by LF (\n). Throws a {@link SoftEndOfStreamException} if the stream
	 * is closed immediately after the \n (i.e. no data is in the process of
	 * being read).
	 */
	public byte[] deserialize(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[this.maxMessageSize];
		int n = 0;
		int bite;
		if (logger.isDebugEnabled()) {
			logger.debug("Available to read:" + inputStream.available());
		}
		while (true) {
			bite = inputStream.read();
//			logger.debug("Read:" + (char) bite);
			if (bite < 0 && n == 0) {
				throw new SoftEndOfStreamException("Stream closed between payloads");
			}
			checkClosure(bite);
			if (n > 0 && bite == '\n') {
				break;
			}
			buffer[n++] = (byte) bite;
			if (n >= this.maxMessageSize) {
				throw new IOException("LF not found before max message length: "
						+ this.maxMessageSize);
			}
		};
		byte[] assembledData = new byte[n-1];
		System.arraycopy(buffer, 0, assembledData, 0, n-1);
		return assembledData;
	}

	/**
	 * Writes the byte[] to the stream and appends \n.
	 */
	public void serialize(byte[] bytes, OutputStream outputStream) throws IOException {
		outputStream.write(bytes);
		outputStream.write(LF);
		outputStream.flush();
	}

}
