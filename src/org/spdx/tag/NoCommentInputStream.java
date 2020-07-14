/**
 * Copyright (c) 2013 Source Auditor Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
*/
package org.spdx.tag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gary O'Neall
 *
 * Input stream which filters out any SPDX tag/value comments
 * Any new line which begins with a # is skipped until the end of line except
 * if it is within a <text> </text> wrapper
 *
 */
public class NoCommentInputStream extends InputStream {

	static final Logger logger = LoggerFactory.getLogger(NoCommentInputStream.class.getName());
	private static final CharSequence START_TEXT_TAG = "<text>";
	private static final CharSequence END_TEXT_TAG = "</text>";
	private static final char COMMENT_CHAR = '#';
	private InputStream inputStream;
	private InputStreamReader reader;
	private BufferedReader bufferedReader;
	private String currentLine;
	private int bytesIndex;
	private byte[] currentBytes;
	boolean inText = false;
	private int currentLineNo = 0;

	/**
	 * @param in Input stream containing the commented data
	 * @throws IOException
	 */
	public NoCommentInputStream(InputStream in) throws IOException {
		this.inputStream = in;
		this.reader = new InputStreamReader(inputStream, "UTF-8");
		this.bufferedReader = new BufferedReader(reader);
		readNextLine();
	}

	/**
	 * Reads the next line in the input stream, skipping empty lines and comments as necessary.
	 * @throws IOException
	 */
	private void readNextLine() throws IOException {
		do {
			currentLine = bufferedReader.readLine();
			if (currentLine == null) {
				return;
			}
			currentLineNo++;
		} while (!inText && (currentLine.length() == 0 || currentLine.charAt(0) == COMMENT_CHAR));

		if (inText) {
			if (currentLine.contains(END_TEXT_TAG)) {
				inText = false;
			}
		} else {
			if (currentLine.contains(START_TEXT_TAG) && !currentLine.contains(END_TEXT_TAG)) {
				inText = true;
			}
		}

		bytesIndex = 0;
		currentBytes = currentLine.getBytes("UTF-8");
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		// Exit early on EOF.
		if (currentLine == null) {
			return -1;
		}

		// Fill the buffer if we ran out of bytes.
		if (bytesIndex >= currentBytes.length) {
			readNextLine();
			if (currentLine == null) {
				return -1;
			} else {
				// Before returning bytes from the newly filled buffer, return the new
				// line character that readLine() embezzled.
				return (int)'\n';
			}
		}

		// Return the current byte from the buffer and increment the index.
		return (int)currentBytes[bytesIndex++];
	}

	@Override
	public void close() {
		if (this.bufferedReader != null) {
			try {
				this.bufferedReader.close();
			} catch (IOException e) {
				logger.error("IO Error closing buffered reader: "+e.getMessage());
			}
		}
		if (this.reader != null) {
			try {
				this.reader.close();
			} catch (IOException e) {
				logger.error("IO Error closing reader: "+e.getMessage());
			}
		}
		if (this.inputStream != null) {
			try {
				this.inputStream.close();
			} catch (IOException e) {
				logger.error("IO Error closing input stream: "+e.getMessage());
			}
		}
	}

	/**
	 * Searches for the string in the file stored in FileLine and returns the line no. if found.
	 * @param string to be searched in the file
	 */
	public int getCurrentLineNo(){
		return currentLineNo;
	}

	public String readLine() throws IOException {
		if (bytesIndex >= currentBytes.length) {
			readNextLine();
		}
		if (currentLine == null) {
			return null;
		}
		String retval = currentLine.substring(bytesIndex);
		bytesIndex = currentBytes.length;
		return retval;
	}

}
