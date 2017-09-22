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
package org.spdx.compare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spdx.licenseTemplate.ILicenseTemplateOutputHandler;
import org.spdx.licenseTemplate.LicenseTemplateRule;

/**
 * Compares the output of a parsed license template to text.  The method matches is called after
 * the document is parsed to determine if the text matches.
 * @author Gary O'Neall
 *
 */
public class CompareTemplateOutputHandler implements
		ILicenseTemplateOutputHandler {
	
	class LineColumn {
		private int line;
		private int column;
		private int len;
		
		public LineColumn(int line, int column,int len) {
			this.line = line;
			this.column = column;
		}

		public int getLine() {
			return line;
		}

		public void setLine(int line) {
			this.line = line;
		}

		public int getColumn() {
			return column;
		}

		public void setColumn(int column) {
			this.column = column;
		}

		public int getLen() {
			return len;
		}

		public void setLen(int len) {
			this.len = len;
		}		
	}
	
	public class DifferenceDescription {
		private boolean differenceFound;
		private String differenceMessage;
		private List<LineColumn> differences;
		
		public DifferenceDescription(boolean differenceFound, String differenceMessage, List<LineColumn> differences) {
			this.differenceFound = differenceFound;
			this.differenceMessage = differenceMessage;
			this.differences = differences;
		}

		public boolean isDifferenceFound() {
			return differenceFound;
		}

		public void setDifferenceFound(boolean differenceFound) {
			this.differenceFound = differenceFound;
		}

		public String getDifferenceMessage() {
			return differenceMessage;
		}

		public void setDifferenceMessage(String differenceMessage) {
			this.differenceMessage = differenceMessage;
		}

		public List<LineColumn> getDifferences() {
			return differences;
		}

		public void setDifferences(List<LineColumn> differences) {
			this.differences = differences;
		}
		
	}
	
	String compareText = "";
	boolean differenceFound = false;
	String[] compareTokens = new String[0];
	int compareTokenCounter = 0;
	String nextCompareToken = null;
	String differenceExplanation = "No difference found";
	List<LineColumn> differences = new ArrayList<LineColumn>();
	StringBuilder optionalText = new StringBuilder();
	Map<Integer, LineColumn> tokenToLocation = new HashMap<Integer, LineColumn>();

	
	/**
	 * @param compareText Text to compare the parsed SPDX license template to
	 * @throws IOException This is not to be expected since we are using StringReaders
	 */
	public CompareTemplateOutputHandler(String compareText) throws IOException {
		this.compareText = compareText;
		List<String> tokens = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(compareText));
			int currentLine = 1;
			int currentToken = 0;
			String line = reader.readLine();
			Pattern delimPattern = Pattern.compile(LicenseCompareHelper.TOKEN_DELIM);
			while (line != null) {
				Matcher lineMatcher = delimPattern.matcher(line);
				int lastColMatched = 0;
				while (lineMatcher.find()) {
					String token = line.substring(lastColMatched, lineMatcher.start());
					if (token.length() > 0) {
						tokens.add(token);
						tokenToLocation.put(currentToken, new LineColumn(currentLine, lastColMatched, token.length()));
						currentToken++;
					}
					lastColMatched = lineMatcher.end();
				}
				if (lastColMatched < line.length()) {
					String token = line.substring(lastColMatched, line.length());
					tokens.add(token);
					tokenToLocation.put(currentToken, new LineColumn(currentLine, lastColMatched, token.length()));
					currentToken++;
				}
				currentLine++;
				line = reader.readLine();
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		this.compareTokens = tokens.toArray(new String[tokens.size()]);
		compareTokenCounter = 0;
		nextCompareToken = LicenseCompareHelper.getTokenAt(compareTokens, compareTokenCounter++);
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#optionalText(java.lang.String)
	 */
	@Override
	public void optionalText(String text) {
		this.optionalText.append(' ');
		this.optionalText.append(text);
	}

	/**
	 * compare the text to the compare text at the current location
	 * @param text
	 * @return true if the text is equivalent
	 */
	protected boolean textEquivalent(String text) {
		String[] textTokens = text.split(LicenseCompareHelper.TOKEN_DELIM);
		int textTokenCounter = 0;
		String nextTextToken = LicenseCompareHelper.getTokenAt(textTokens, textTokenCounter++);
		while (nextTextToken != null) {
			if (this.nextCompareToken == null) {
				// end of compare text stream
				while (nextTextToken != null && LicenseCompareHelper.canSkip(nextTextToken)) {
					nextTextToken = LicenseCompareHelper.getTokenAt(textTokens, textTokenCounter++);
				}
				if (nextTextToken != null) {
					return false;	// there is more stuff in the compare license text, so not equiv.
				}
			} else if (LicenseCompareHelper.tokensEquivalent(nextTextToken, this.nextCompareToken)) { 
				// just move onto the next set of tokens
				nextTextToken = LicenseCompareHelper.getTokenAt(textTokens, textTokenCounter++);
				this.nextCompareToken = LicenseCompareHelper.getTokenAt(this.compareTokens, this.compareTokenCounter++);
			} else {
				// see if we can skip through some compare tokens to find a match
				while (this.nextCompareToken != null && LicenseCompareHelper.canSkip(this.nextCompareToken)) {
					this.nextCompareToken = LicenseCompareHelper.getTokenAt(this.compareTokens, this.compareTokenCounter++);
				}
				// just to be sure, skip forward on the text
				while (nextTextToken != null && LicenseCompareHelper.canSkip(nextTextToken)) {
					nextTextToken = LicenseCompareHelper.getTokenAt(textTokens, textTokenCounter++);
				}
				if (!LicenseCompareHelper.tokensEquivalent(this.nextCompareToken, nextTextToken)) {
					return false;
				} else {
					nextTextToken = LicenseCompareHelper.getTokenAt(textTokens, textTokenCounter++);
					this.nextCompareToken = LicenseCompareHelper.getTokenAt(this.compareTokens, this.compareTokenCounter++);
				}
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#normalText(java.lang.String)
	 */
	@Override
	public void normalText(String text) {
		if (differenceFound) {
			return;
		}
		if (!textEquivalent(text)) {
			this.differenceFound = true;
			if (this.nextCompareToken == null) {
				LineColumn lastLineColumn = tokenToLocation.get(this.compareTokens.length-1);
				// create a zero length location at the end of the file
				addDifference("End of compare text encountered before the end of the license template",
						new LineColumn(lastLineColumn.getLine(), lastLineColumn.getColumn()+lastLineColumn.getLen(),0));
			} else {
				addDifference("Difference found in normal text",tokenToLocation.get(this.compareTokenCounter));
			}
		}
	}
	
	private void addDifference(String msg, LineColumn location) {
		this.differenceExplanation = msg + " starting at line #"+
				String.valueOf(location.getLine())+ " column #" +
				String.valueOf(location.getColumn())+"\""+
				this.nextCompareToken+"\".";
		this.differences.add(location);
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#variableRule(org.spdx.licenseTemplate.LicenseTemplateRule)
	 */
	@Override
	public void variableRule(LicenseTemplateRule rule) {
		if (differenceFound) {
			return;
		}
		String remainingText = buildRemainingCompareText();
		Pattern matchPattern = Pattern.compile(rule.getMatch());
		Matcher matcher = matchPattern.matcher(remainingText);
		if (!matcher.find()) {
			this.differenceFound = true;
			addDifference("Variable text rule "+rule.getName()+" did not match the compare text",
					tokenToLocation.get(this.compareTokenCounter));
		} else if (matcher.start() > 0) {		
			this.differenceFound = true;
			addDifference("Extra text \""+ 
			remainingText.substring(0, matcher.start()) +
					"\" found before the variable text rule "+rule.getName(),
					tokenToLocation.get(this.compareTokenCounter));
		} else {
			// advance the token counter
			String textAfterMatch = remainingText.substring(matcher.end()).trim();
			if (textAfterMatch.trim().isEmpty()) {
				// at the end
				this.nextCompareToken = null;
				this.compareTokenCounter = this.compareTokens.length;
			} else {
				// need to calculate
				String[] tokensAfterMatch = textAfterMatch.split(LicenseCompareHelper.TOKEN_DELIM);
				this.compareTokenCounter = this.compareTokens.length - tokensAfterMatch.length;
				this.nextCompareToken = this.compareTokens[this.compareTokenCounter++];
				if (!this.nextCompareToken.equals(tokensAfterMatch[0]) && 
						(tokensAfterMatch.length > 1 && !this.nextCompareToken.equals(tokensAfterMatch[1]))) {
					this.nextCompareToken = this.compareTokens[this.compareTokenCounter++];
					if (!this.nextCompareToken.equals(tokensAfterMatch[0]) && 
							(tokensAfterMatch.length > 1 && !this.nextCompareToken.equals(tokensAfterMatch[1]))) {
						this.differenceFound = true;
						addDifference("Missmatched text found after end of variable rule" + rule.getName(),
								tokenToLocation.get(this.nextCompareToken));
					}
				}
			}
		}
	}

	/**
	 * Builds a string from the remaining tokens in the compare string
	 * @return
	 */
	private String buildRemainingCompareText() {
		//TODO: Most of the matches are greedy (e.g. .+), need to limit the compare text
		// to only include up to the tokens that match after
		StringBuilder sb = new StringBuilder();
		if (this.nextCompareToken != null) {
			sb.append(this.nextCompareToken);

			for (int i = this.compareTokenCounter; i < this.compareTokens.length; i++) {
				sb.append(' ');
				sb.append(this.compareTokens[i]);
			}
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#beginOptional(org.spdx.licenseTemplate.LicenseTemplateRule)
	 */
	@Override
	public void beginOptional(LicenseTemplateRule rule) {
		if (differenceFound) {
			return;
		}
		this.optionalText.setLength(0);
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#endOptional(org.spdx.licenseTemplate.LicenseTemplateRule)
	 */
	@Override
	public void endOptional(LicenseTemplateRule rule) {
		if (differenceFound) {
			return;
		}
		String saveNextComparisonToken = nextCompareToken;
		int saveCompareTokenCounter = compareTokenCounter;
		String saveDifferenceExplanation = this.differenceExplanation;
		List<LineColumn> saveDifferences = new ArrayList<LineColumn>();
		Collections.copy(saveDifferences, this.differences);
		if (!textEquivalent(this.optionalText.toString())) {
			// reset counters
			this.nextCompareToken = saveNextComparisonToken;
			this.compareTokenCounter = saveCompareTokenCounter;
			this.differenceExplanation = saveDifferenceExplanation;
			Collections.copy(this.differences, saveDifferences);
		}	
	}

	/**
	 * @return
	 */
	public boolean matches() {
		return !differenceFound;
	}
	
	/**
	 * @return details on the differences found
	 */
	public DifferenceDescription getDifferences() {
		return new DifferenceDescription(differenceFound, this.differenceExplanation, this.differences);
	}

}
