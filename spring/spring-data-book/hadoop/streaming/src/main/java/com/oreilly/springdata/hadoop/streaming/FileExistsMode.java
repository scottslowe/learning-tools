package com.oreilly.springdata.hadoop.streaming;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * When writing file, this enumeration indicates what action shall be taken in
 * case the destination file already exists.
 * 
 * @author Gunnar Hillert
 * 
 */
public enum FileExistsMode {

	/**
	 * Raise an exception in case the file to be written already exists.
	 */
	FAIL,

	/**
	 * If the file already exists, do nothing.
	 */
	IGNORE,

	/**
	 * If the file already exists, replace it.
	 */
	REPLACE;

	/**
	 * For a given non-null and not-empty input string, this method returns the
	 * corresponding {@link FileExistsMode}. If it cannot be determined, an
	 * {@link IllegalStateException} is thrown.
	 * 
	 * @param fileExistsModeAsString
	 *            Must neither be null nor empty
	 */
	public static FileExistsMode getForString(String fileExistsModeAsString) {

		Assert.hasText(fileExistsModeAsString,
				"'fileExistsModeAsString' must neither be null nor empty.");

		final FileExistsMode[] fileExistsModeValues = FileExistsMode.values();

		for (FileExistsMode fileExistsMode : fileExistsModeValues) {
			if (fileExistsModeAsString.equalsIgnoreCase(fileExistsMode.name())) {
				return fileExistsMode;
			}
		}

		throw new IllegalArgumentException("Invalid fileExistsMode '"
				+ fileExistsModeAsString
				+ "'. The (case-insensitive) supported values are: "
				+ StringUtils.arrayToCommaDelimitedString(fileExistsModeValues));

	}

}
