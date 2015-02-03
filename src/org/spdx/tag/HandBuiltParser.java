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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import antlr.RecognitionException;

/**
 * I'm hoping this is a temporary solution.  This is a hand built parser to parse
 * SPDX tag files.  It replaces the current ANTL based parser which has a defect
 * where any lines starting with a text ending with a : is treated as a tag even
 * if it is in <text> </text>
 * 
 * The interface is similar to the generated ANTLR code
 * @author Gary O'Neall
 *
 */
public class HandBuiltParser {
	
	private static final String END_TEXT = "</text>";
	private static final String START_TEXT = "<text>";
	Pattern tagPattern = Pattern.compile("^\\w+:");
	private TagValueBehavior buildDocument;
	private InputStream textInput;

	/**
	 * Creates a parser for an Input stream.
	 * The input stream must not use any comments.
	 * @param textInput
	 */
	public HandBuiltParser(InputStream textInput) {
		this.textInput = textInput;
	}

	/**
	 * @param buildDocument
	 */
	public void setBehavior(TagValueBehavior buildDocument) {
		this.buildDocument = buildDocument;
	}

	/**
	 * parses the data
	 * @throws Exception 
	 */
	public void data() throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(textInput));

		try {
			boolean inTextBlock = false;
			String tag = "";
			String value = "";
			String nextLine = br.readLine();
			while (nextLine != null) {
				if (inTextBlock) {
					int endText = nextLine.indexOf(END_TEXT);
					if (endText >= 0) {
						value = value + "\n" + nextLine.substring(0, endText).trim();
						inTextBlock = false;	//NOTE: we are skipping any text after the </text>
						this.buildDocument.buildDocument(tag, value);
						tag = "";
						value = "";
					} else {
						value = value + "\n" + nextLine;
					}
				} else {
					// not in a text block
					Matcher tagMatcher = this.tagPattern.matcher(nextLine);
					if (tagMatcher.find()) {
						tag = tagMatcher.group();
						int startText = nextLine.indexOf(START_TEXT);
						if (startText > 0) {
							value = nextLine.substring(startText + START_TEXT.length()).trim();
							if (value.contains(END_TEXT)) {
								value = value.substring(0, value.indexOf(END_TEXT)).trim();
								this.buildDocument.buildDocument(tag, value);
								tag = "";
								value = "";
							} else {
								inTextBlock = true;
							}
						} else {
							value = nextLine.substring(tag.length()).trim();
							this.buildDocument.buildDocument(tag, value);
							tag = "";
							value = "";
							
						}
					} else {
						// note - we just ignore any lines that do not start with a tag
					}
				}
				nextLine = br.readLine();
			}
			if (inTextBlock) {
				throw(new RecognitionException("Un terminted text block.  Expecting "+END_TEXT));
			}
			this.buildDocument.exit();
		} finally {
			br.close();
		}
	}

	
}
