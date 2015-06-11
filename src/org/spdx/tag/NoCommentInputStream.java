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

import org.apache.log4j.Logger;

/**
 * @author Gary O'Neall
 * 
 * Input stream which filters out any SPDX tag/value comments
 * Any new line which begins with a # is skipped until the end of line except
 * if it is within a <text> </text> wrapper
 *
 */
public class NoCommentInputStream extends InputStream {
	
	static final Logger logger = Logger.getLogger(NoCommentInputStream.class.getName());
	private static final CharSequence START_TEXT_TAG = "<text>";
	private static final CharSequence END_TEXT_TAG = "</text>";
	private static final char COMMENT_CHAR = '#';
	private InputStream inputStream;
	private InputStreamReader reader;
	private BufferedReader bufferedReader;
	private int lineIndex = 1;
	private String currentLine = "";
	boolean eof = false;
	boolean inText = false;
	

	/**
	 * @param in Input stream containing the commented data
	 * @throws IOException 
	 */
	public NoCommentInputStream(InputStream in) throws IOException {
		this.inputStream = in;
		this.reader = new InputStreamReader(inputStream);
		this.bufferedReader = new BufferedReader(reader);
		readNextLine();
	}

	/**
	 * reads the next line in the input stream skipping when necessary
	 * @throws IOException 
	 */
	private void readNextLine() throws IOException {
		if (eof) {
			return;
		}
		currentLine = bufferedReader.readLine();
		while (currentLine != null && !inText && 
				(currentLine.length() == 0 || currentLine.charAt(0) == COMMENT_CHAR)) {
			currentLine = bufferedReader.readLine();
		}
		if (currentLine == null) {
			eof = true;
			return;
		}
		if (inText) {
			if (containsEndText(currentLine)) {
				inText = false;
			}
		} else {
			if (containsStartText(currentLine)) {
				inText = true;
			}
		}
		this.lineIndex = 0;
	}

	/**
	 * Return true if str contains the end text tag
	 * @param str
	 * @return
	 */
	private boolean containsEndText(String str) {
		return str.contains(END_TEXT_TAG);
	}

	/**
	 * Returns true if str contains the start text tag
	 * @param str
	 * @return
	 */
	private boolean containsStartText(String str) {
		return str.contains(START_TEXT_TAG);
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		while (!eof && this.lineIndex >= this.currentLine.length()) {
			readNextLine();
			if (eof) {
				return -1;
			} else {
				return (int)'\n';
			}
		}
		if (eof) {
			return -1;
		}
		return (int)currentLine.charAt(this.lineIndex++);
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
	
}
