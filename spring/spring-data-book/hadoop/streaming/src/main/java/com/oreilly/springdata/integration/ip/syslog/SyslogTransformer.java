package com.oreilly.springdata.integration.ip.syslog;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

/**
 * @author Gary Russell
 * @since 2.2
 *
 */
public class SyslogTransformer {

	public static final String FACILITY = "FACILITY";

	public static final String SEVERITY = "SEVERITY";

	public static final String TIMESAMP = "TIMESTAMP";

	public static final String HOST = "HOST";

	public static final String TAG = "TAG";

	public static final String MESSAGE = "MESSAGE";

	public static final String UNDECODED = "UNDECODED";

	private final Pattern pattern = Pattern.compile("<([^>]+)>(.{15}) ([^ ]+) ([^:]+): (.*)");

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss");

	public Message<Map<String, Object>> transform(Message<byte[]> message) {
		String payload;
		try {
			payload = new String(message.getPayload(), "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			payload = new String(message.getPayload());
		}
		Map<String, Object> map = new HashMap<String, Object>();
		Matcher matcher = pattern.matcher(payload);
		if (matcher.matches()) {
			String facilityString = matcher.group(1);
			int facility = Integer.parseInt(facilityString);
			int severity = facility & 0x7;
			facility = facility >> 3;
			map.put(FACILITY, facility);
			map.put(SEVERITY, severity);
			String timestamp = matcher.group(2);
			Date date;
			try {
				date = this.dateFormat.parse(timestamp);
				Calendar calendar = Calendar.getInstance();
				int year = calendar.get(Calendar.YEAR);
				calendar.setTime(date);
				calendar.set(Calendar.YEAR, year);
				//TODO handle midnight on Dec 31
				map.put(TIMESAMP, calendar.getTime());
			}
			catch (Exception e) {
				map.put(TIMESAMP, timestamp);
			}
			map.put(HOST, matcher.group(3));
			map.put(TAG, matcher.group(4));
			map.put(MESSAGE, matcher.group(5));
		}
		else {
			map.put(UNDECODED, payload);
		}
		return MessageBuilder.withPayload(map)
				.copyHeaders(message.getHeaders())
				.build();
	}
}
