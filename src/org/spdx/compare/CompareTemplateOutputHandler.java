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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spdx.licenseTemplate.ILicenseTemplateOutputHandler;
import org.spdx.licenseTemplate.LicenseTemplateRule;
import org.spdx.licenseTemplate.LicenseTemplateRule.RuleType;
import org.spdx.rdfparser.license.LicenseParserException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Compares the output of a parsed license template to text.  The method matches is called after
 * the document is parsed to determine if the text matches.
 * @author Gary O'Neall
 *
 */
public class CompareTemplateOutputHandler implements
		ILicenseTemplateOutputHandler {
	private static final int MAX_NEXT_NORMAL_TEXT_SEARCH_LENGTH = 15;	// Maximum number of tokens to compare when searching for a normal text match
	private static final int MIN_TOKENS_NORMAL_TEXT_SEARCH = 3;	// Minimum number of tokens to match of normal text to match after a variable block to bound greedy regex var text
	
	class ParseInstruction {
		LicenseTemplateRule rule;
		String text;
		List<ParseInstruction> subInstructions;
		ParseInstruction parent;

		private boolean skip = false;	// skip this instruction in matching
		private boolean skipFirstTextToken = false;	// skip the first text token
		private DifferenceDescription lastOptionalDifference = null;
		
		ParseInstruction(LicenseTemplateRule rule, String text, ParseInstruction parent) {
			this.rule = rule;
			this.text = text;
			this.subInstructions = Lists.newArrayList();
			this.parent = parent;
		}
		
		@Override 
		public String toString() {
			if (this.rule != null) {
				return this.rule.toString();
			} else if (this.text != null) {
				String retval = "TEXT: '";
				if (this.text.length() > 10) {
					retval = retval + this.text.substring(0, 10) + "...'";
				} else {
					retval = retval + this.text + "'";
				}
				return retval;
			}else {
				return "NONE";
			}
		}

		/**
		 * @return the rule
		 */
		public LicenseTemplateRule getRule() {
			return rule;
		}

		/**
		 * @param rule the rule to set
		 */
		public void setRule(LicenseTemplateRule rule) {
			this.rule = rule;
		}

		/**
		 * @return the text
		 */
		public String getText() {
			return text;
		}

		/**
		 * @param optionalText the text to set
		 */
		public void setText(String text) {
			this.text = text;
		}

		/**
		 * Add the instruction to the list of sub-instructions
		 * @param instruction
		 */
		public void addSubInstruction(ParseInstruction instruction) {

			if (instruction.getRule() != null && RuleType.VARIABLE.equals(instruction.getRule().getType()) &&
					subInstructions.size() > 0 && 
					subInstructions.get(subInstructions.size()-1).getRule() != null &&
							RuleType.VARIABLE.equals(subInstructions.get(subInstructions.size()-1).getRule().getType())) {
				// Maybe this is a little bit of a hack, but merge any var instructions so that
				// the match will work
				LicenseTemplateRule lastRule = subInstructions.get(subInstructions.size()-1).getRule();
				lastRule.setMatch("("+lastRule.getMatch()+")\\s*("+instruction.getRule().getMatch()+")");
				lastRule.setName("combined-"+lastRule.getName()+"-"+instruction.getRule().getName());
				lastRule.setOriginal(lastRule.getOriginal() + " " + lastRule.getOriginal());
			} else {
				instruction.setParent(this);
				this.subInstructions.add(instruction);
			}
		}

		/**
		 * @return the parent
		 */
		public ParseInstruction getParent() {
			return parent;
		}

		/**
		 * @param parent the parent to set
		 */
		public void setParent(ParseInstruction parent) {
			this.parent = parent;
		}

		/**
		 * @return the subInstructions
		 */
		public List<ParseInstruction> getSubInstructions() {
			return subInstructions;
		}

		/**
		 * @return true iff there are only text instructions as sub instructions
		 */
		public boolean onlyText() {
			if (this.subInstructions.size() < 1) {
				return false;
			}
			for (ParseInstruction subInstr:this.subInstructions) {
				if (subInstr.getText() == null) {
					return false;
				}
			}
			return true;
		}

		public String toText() {
			StringBuilder sb = new StringBuilder();
			for (ParseInstruction subInstr:this.subInstructions) {
				if (subInstr.getText() != null) {
					sb.append(subInstr.getText());
				}
			}
			return sb.toString();
		}
		
		/**
		 * Attempt to match this instruction against a tokenized array
		 * @param matchTokens Tokens to match the instruction against
		 * @param startToken Index of the tokens to start the match
		 * @param endToken Last index of the tokens to use in the match
		 * @param originalText Original text used go generate the matchTokens
		 * @param differenceDescription Description of differences found
		 * @param nextNormalText if there is a nextOptionalText, this would be the normal text that follows the optional text
		 * @return Next token index after the match or -1 if no match was found
		 * @throws LicenseParserException 
		 */
		public int match(String[] matchTokens, int startToken, int endToken, String originalText,
				DifferenceDescription differences, Map<Integer, LineColumn> tokenToLocation) throws LicenseParserException {
			return match(matchTokens, startToken, endToken, originalText, differences, tokenToLocation, false);
		}
		
		/**
		 * Attempt to match this instruction against a tokenized array
		 * @param matchTokens Tokens to match the instruction against
		 * @param startToken Index of the tokens to start the match
		 * @param endToken Last index of the tokens to use in the match
		 * @param originalText Original text used go generate the matchTokens
		 * @param differenceDescription Description of differences found
		 * @param nextNormalText if there is a nextOptionalText, this would be the normal text that follows the optional text
		 * @param ignoreOptionalDifferences if true, don't record any optional differences
		 * @return Next token index after the match or -1 if no match was found
		 * @throws LicenseParserException 
		 */
		public int match(String[] matchTokens, int startToken, int endToken, String originalText,
				DifferenceDescription differences, Map<Integer, LineColumn> tokenToLocation, boolean ignoreOptionalDifferences) throws LicenseParserException {
			if (this.skip) {
				return startToken;
			}
			int nextToken = startToken;
			if (this.rule == null) {
				if (this.text != null) {
					Map<Integer, LineColumn> textLocations = new HashMap<Integer, LineColumn>();
					String[] textTokens = LicenseCompareHelper.tokenizeLicenseText(LicenseCompareHelper.normalizeText(text), textLocations);
					if (this.skipFirstTextToken) {
						textTokens = Arrays.copyOfRange(textTokens, 1, textTokens.length);
					}
					nextToken = compareText(textTokens, matchTokens, nextToken, endToken, this);
					if (nextToken < 0) {
						int errorLocation = -nextToken;
						differences.addDifference(tokenToLocation.get(errorLocation), LicenseCompareHelper.getTokenAt(matchTokens, errorLocation), 
										"Normal text of license does not match", text, null, getLastOptionalDifference());
					}
					if (this.subInstructions.size() > 0) {
						throw new LicenseParserException("License template parser error.  Sub expressions are not allows for plain text.");
					}
				} else {
					// just process the sub instructions
					for (ParseInstruction sub:subInstructions) {
						nextToken = sub.match(matchTokens, nextToken, endToken, originalText, differences, 
								tokenToLocation, ignoreOptionalDifferences);
						if (nextToken < 0) {
							return nextToken;
						}
					}
				}

			} else if (this.rule.getType().equals(RuleType.BEGIN_OPTIONAL)) {
				if (this.getText() != null) {
					throw new LicenseParserException("License template parser error - can not have text associated with a begin optional rule");
				}
				if (this.onlyText() || this.parent == null) {
					// optimization, don't go through the effort to subset the text
					for (ParseInstruction sub:subInstructions) {
						DifferenceDescription optionalDifference = new DifferenceDescription();
						nextToken = sub.match(matchTokens, nextToken, endToken, originalText, 
								optionalDifference, tokenToLocation);
						if (nextToken < 0) {
							if (!ignoreOptionalDifferences) {
								setLastOptionalDifference(optionalDifference);
							}					
							return startToken;	// the optional text didn't match, just return the start token
						}
					}
				} else {
					List<Integer> matchingNormalTextStartTokens = this.parent.findNextNonVarTextStartTokens(this, matchTokens, 
							startToken, endToken, originalText, differences, tokenToLocation);
					nextToken = matchOptional(matchingNormalTextStartTokens, matchTokens, 
							nextToken, endToken, originalText, differences, tokenToLocation, ignoreOptionalDifferences);
				}
			} else if (this.rule.getType().equals(RuleType.VARIABLE)) {
				List<Integer> matchingNormalTextStartTokens = this.parent.findNextNonVarTextStartTokens(this, matchTokens, 
						startToken, endToken, originalText, differences, tokenToLocation);
				nextToken = matchVariable(matchingNormalTextStartTokens, matchTokens, 
						nextToken, endToken, originalText, differences, tokenToLocation);
			} else {
				throw new LicenseParserException("Unexpected parser state - instruction is not root, optional, variable or text");
			}
			return nextToken;
		}

		/**
		 * Match to an optional rule
		 * @param optionalInstruction Optional Instruction
		 * @param matchingStartTokens List of indexes for the start tokens for the next normal text
		 * @param matchTokens Tokens to match against
		 * @param startToken Index of the first token to search for the match
		 * @param endToken Index of the last token to search for the match
		 * @param originalText Original text used go generate the matchTokens
		 * @param differences Any differences found
		 * @param tokenToLocation Map of token index to line/column where the token was found in the original text
		 *  @param ignoreOptionalDifferences if true, don't record any optional differences
		 * @return the index of the token after the find or -1 if the text did not match
		 * @throws LicenseParserException 
		 */
		private int matchOptional(List<Integer> matchingStartTokens,
				String[] matchTokens, int startToken, int endToken, String originalText,
				DifferenceDescription differences, Map<Integer, LineColumn> tokenToLocation, boolean ignoreOptionalDifferences) throws LicenseParserException {
			for (int matchingStartToken:matchingStartTokens) {
				DifferenceDescription matchDifferences = new DifferenceDescription();
				int matchLocation = startToken;
				for (ParseInstruction sub:subInstructions) {
					matchLocation = sub.match(matchTokens, matchLocation, matchingStartToken-1, originalText, 
							matchDifferences, tokenToLocation);
					if (matchLocation < 0) {
						break;
					}
				}
				if (matchLocation > 0) {
					return matchLocation;	// found a match
				} else if (!ignoreOptionalDifferences) {				
					setLastOptionalDifference(matchDifferences);
				}
			}
			// We didn't find any matches, return the original start token
			return startToken;
		}

		/**
		 * Find the indexes that match the matching optional or first normal text within the sub-instructions
		 * @param afterChild the child after which to start searching for the first normal text
		 * @param matchTokens Tokens used to match the text against
		 * @param startToken Start of the match tokens to begin the search
		 * @param endToken End of the match tokens to end the search
		 * @param originalText original text that created the match tokens
		 * @param differences Information on any differences found
		 * @param tokenToLocation Map of match token indexes to line/column locations
		 * @return List of indexes for the start tokens for the next non variable text that matches
		 * @throws LicenseParserException 
		 */
		private List<Integer> findNextNonVarTextStartTokens(ParseInstruction afterChild,
				String[] matchTokens, int startToken, int endToken, String originalText,
				DifferenceDescription differences, Map<Integer, LineColumn> tokenToLocation) throws LicenseParserException {
			List<Integer> retval = new ArrayList<Integer>();
			// We find the first index to start our search
			int indexOfChild = subInstructions.indexOf(afterChild);
			if (indexOfChild < 0) {
				throw new LicenseParserException("Template Parser Error: Could not locate sub instruction");
			}
			int startSubinstructionIndex = indexOfChild + 1;
			if (startSubinstructionIndex >= subInstructions.size()) {
				// no start tokens found
				// Set return value to the end
				retval.add(endToken+1);
				return retval;
			}
			int firstNormalTextIndex = -1;	// initial value for not yet found
			// keep track of all optional rules prior to the first solid normal text since the optional
			// rules can provide a valid result
			List<Integer> leadingOptionalSubInstructions = Lists.newArrayList();
			int i = startSubinstructionIndex;
			while (i < subInstructions.size() && firstNormalTextIndex < 0) {
				LicenseTemplateRule subInstructionRule = subInstructions.get(i).getRule();
				if (subInstructionRule != null && subInstructionRule.getType() == RuleType.BEGIN_OPTIONAL) {
					leadingOptionalSubInstructions.add(i);
				} else if (subInstructions.get(i).getText() != null) {
					firstNormalTextIndex = i;
				}
				i++;
			}
			int nextMatchingStart = startToken;
			// Go through the preceding optional rules.  If there enough token matches, add it to the result list
			for (int optionalSub:leadingOptionalSubInstructions) {
				DifferenceDescription tempDiffDescription = new DifferenceDescription();
				int nextOptMatchingStart = nextMatchingStart;
				int optTokenAfterMatch = subInstructions.get(optionalSub).match(matchTokens, nextOptMatchingStart, endToken, originalText, tempDiffDescription, tokenToLocation, true);
				while (optTokenAfterMatch <= nextOptMatchingStart && -optTokenAfterMatch <= endToken 
						&& !tempDiffDescription.differenceFound && nextOptMatchingStart <= endToken) {
					// while we didn't find a match
					nextOptMatchingStart++;
					optTokenAfterMatch = subInstructions.get(optionalSub).match(matchTokens, nextOptMatchingStart, endToken, originalText, tempDiffDescription, tokenToLocation, true);
				}
				if (optTokenAfterMatch > 0 && !tempDiffDescription.differenceFound && nextOptMatchingStart <= endToken) {
					// we found a match
					if (optTokenAfterMatch - nextOptMatchingStart > MIN_TOKENS_NORMAL_TEXT_SEARCH) {
						// Only add possible matches if it matched enough tokens
						//TODO: This approximation of the number of tokens matched may include tokens consumed by a variable match. To make this more accurate, we should count the tokens of just the text nodes a children 
						retval.add(nextOptMatchingStart);
					}
					nextMatchingStart = optTokenAfterMatch;
				}
			}
			if (firstNormalTextIndex < 0) {
				// Set to the end
				retval.add(endToken+1);
				return retval;
			}
			
			Map<Integer, LineColumn> normalTextLocations = new HashMap<Integer, LineColumn>();
			String[] textTokens = LicenseCompareHelper.tokenizeLicenseText(LicenseCompareHelper.normalizeText(subInstructions.get(firstNormalTextIndex).getText()), normalTextLocations);
			if (textTokens.length > MAX_NEXT_NORMAL_TEXT_SEARCH_LENGTH) {
				textTokens = Arrays.copyOf(textTokens, MAX_NEXT_NORMAL_TEXT_SEARCH_LENGTH);
			}

			int tokenAfterMatch = compareText(textTokens, matchTokens, nextMatchingStart, endToken, null);
			boolean foundEnoughTokens = false;
			while (!foundEnoughTokens && nextMatchingStart <= endToken && !differences.differenceFound) {
				while (tokenAfterMatch < 0 && -tokenAfterMatch <= endToken) {			
					nextMatchingStart = nextMatchingStart + 1;
					tokenAfterMatch = compareText(textTokens, matchTokens, nextMatchingStart, endToken, null);
				}
				if (tokenAfterMatch < 0) {
					// Can not find the text, report a difference
					String ruleDesc = "variable or optional rule";
					if (afterChild.getRule() != null) {
						if (afterChild.getRule().getType() == RuleType.BEGIN_OPTIONAL) {
							ruleDesc = "optional rule";
						} else if (afterChild.getRule().getType() == RuleType.VARIABLE) {
							ruleDesc = "variable rule '" + afterChild.getRule().getName() + "'";
						}
					}
					differences.addDifference(tokenToLocation.get(nextMatchingStart), "",
							"Unable to find the text '" + subInstructions.get(firstNormalTextIndex).getText() + "' following a "+ruleDesc,
									null, rule, getLastOptionalDifference());
				} else if (textTokens.length >= MIN_TOKENS_NORMAL_TEXT_SEARCH) {
					retval.add(nextMatchingStart);
					foundEnoughTokens = true;
				} else {
					// Not enough text tokens, we need to make sure everything matches beyond this point
					DifferenceDescription tempDiffDescription = new DifferenceDescription();
					int nextCheckToken = subInstructions.get(firstNormalTextIndex).match(matchTokens, nextMatchingStart, endToken, originalText, tempDiffDescription, tokenToLocation, true);
					int nextCheckSubInstruction = firstNormalTextIndex + 1;
					while (nextCheckToken > 0 &&
							nextCheckToken - tokenAfterMatch < MIN_TOKENS_NORMAL_TEXT_SEARCH &&
							nextCheckSubInstruction < subInstructions.size()) {
						nextCheckToken = subInstructions.get(nextCheckSubInstruction++).match(matchTokens, nextCheckToken, endToken, originalText, tempDiffDescription, tokenToLocation, true);
					}
					if (nextCheckToken < 0) {
						// we didn't match enough, move on to the next
						nextMatchingStart = nextMatchingStart + 1;
						tokenAfterMatch = compareText(textTokens, matchTokens, nextMatchingStart, endToken, null);
					} else {
						retval.add(nextMatchingStart);
						foundEnoughTokens = true;
					}
				}
			}
			return retval;
		}
		
		/**
		 * Determine the number of tokens matched from the compare text
		 * @param text
		 * @param end End of matching text
		 * @return
		 */
		private int numTokensMatched(String text, int end) {
			if (text.trim().isEmpty()) {
				return 0;
			}
			if (end == 0) {
				return 0;
			}
			Map<Integer, LineColumn> temp = new HashMap<Integer, LineColumn>();
			String subText = text.substring(0, end);
			String[] tokenizedString = LicenseCompareHelper.tokenizeLicenseText(subText, temp);
			return tokenizedString.length;
		}

		/**
		 * Match to a variable rule
		 * @param matchingStartTokens List of indexes for the start tokens for the next normal text
		 * @param matchTokens Tokens to match against
		 * @param startToken Index of the first token to search for the match
		 * @param endToken Index of the last token to search for the match
		 * @param originalText Original text used go generate the matchTokens
		 * @param differences Any differences found
		 * @param tokenToLocation Map of token index to line/column where the token was found in the original text
		 * @return the index of the token after the find or -1 if the text did not match
		 */
		private int matchVariable(List<Integer> matchingStartTokens, String[] matchTokens, int startToken, int endToken, 
				String originalText, DifferenceDescription differences, Map<Integer, LineColumn> tokenToLocation) {
			
			if (differences.isDifferenceFound()) {
				return -1;
			}
			for (int matchingStartToken:matchingStartTokens) {
				String compareText = LicenseCompareHelper.locateOriginalText(originalText, startToken, matchingStartToken-1, tokenToLocation, matchTokens);
				Pattern matchPattern = Pattern.compile(rule.getMatch(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
				Matcher matcher = matchPattern.matcher(compareText);
				if (!matcher.find() || matcher.start() > 0) {
					continue;
				} else {
					int numMatched = numTokensMatched(compareText, matcher.end());
					return startToken + numMatched;
				}
			}
			// if we got here, there was no match found
			differences.addDifference(tokenToLocation.get(startToken), LicenseCompareHelper.getTokenAt(matchTokens, startToken), "Variable text rule "+rule.getName()+" did not match the compare text",
					null, rule, getLastOptionalDifference());
			return -1;
		}

		/**
		 * @return The difference description for the last optional rule which did not match
		 */
		public DifferenceDescription getLastOptionalDifference() {
			if (this.lastOptionalDifference != null) {
				return this.lastOptionalDifference;
			} else if (this.parent != null) {
				return parent.getLastOptionalDifference();
			} else {
				return null;
			}
		}
		
		public void setLastOptionalDifference(DifferenceDescription optionalDifference) {
			if (optionalDifference != null && optionalDifference.getDifferenceMessage() != null && !optionalDifference.getDifferenceMessage().isEmpty()) {
				this.lastOptionalDifference = optionalDifference;
				if (this.parent != null) {
					this.parent.setLastOptionalDifference(optionalDifference);
				}
			}
		}

		/**
		 * @return true if the instruction following this instruction is a beginOptional rule containing text with a single token
		 */
		public boolean isFollowingInstructionOptionalSingleToken() {
			if (parent == null) {
				return false;
			}
			ParseInstruction nextInstruction = parent.findFollowingInstruction(this);
			if (nextInstruction == null || nextInstruction.getRule() == null) {
				return false;
			} else {
				if (!RuleType.BEGIN_OPTIONAL.equals(nextInstruction.getRule().getType())) {
					return false;
				}
				if (nextInstruction.getSubInstructions().size() != 1) {
					return false;
				}
				String optionalText = nextInstruction.getSubInstructions().get(0).getText();
				return LicenseCompareHelper.isSingleTokenString(optionalText);
			}
		}

		/**
		 * @param parseInstruction subInstruction to find the next parse instruction after
		 * @return the next instruction after parseInstruction in the subInstructions
		 */
		private ParseInstruction findFollowingInstruction(ParseInstruction parseInstruction) {
			if (parseInstruction == null) {
				return null;
			}
			for (int i = 0; i < subInstructions.size(); i++) {
				if (parseInstruction.equals(subInstructions.get(i))) {
					if (subInstructions.size() > i+1) {
						return subInstructions.get(i+1);
					} else if (parent == null) {
						return null;
					} else {
						return parent.findFollowingInstruction(this);
					}
				}
			}
			return null;	// instruction not found
		}

		/**
		 * @return the tokens from the next group of optional
		 */
		public String[] getNextOptionalTextTokens() {
			if (parent == null) {
				return new String[0];
			}
			ParseInstruction nextInstruction = parent.findFollowingInstruction(this);
			if (nextInstruction == null || nextInstruction.getRule() == null) {
				return new String[0];
			} else {
				if (!RuleType.BEGIN_OPTIONAL.equals(nextInstruction.getRule().getType())) {
					return new String[0];
				}
				StringBuilder sb = new StringBuilder();
				for (ParseInstruction inst:nextInstruction.getSubInstructions()) {
					if (inst.getText() != null) {
						sb.append(inst.getText());
					}
				}
				Map<Integer, LineColumn> temp = Maps.newHashMap();
				return LicenseCompareHelper.tokenizeLicenseText(sb.toString(), temp);
			}
		}

		/**
		 * Skip the next instruction
		 */
		public void skipNextInstruction() {
			if (parent == null) {
				return;
			}
			ParseInstruction nextInst = parent.findFollowingInstruction(this);
			nextInst.setSkip(true);
		}
		
		public boolean getSkip() {
			return this.skip ;
		}
		
		public void setSkip(boolean skip) {
			this.skip = skip;
		}

		/**
		 * @return the next sibling parse instruction which is just text (no rules)
		 */
		public ParseInstruction getNextNormalTextInstruction() {
			if (this.parent == null) {
				return null;
			}
			List<ParseInstruction> siblings = parent.getSubInstructions();
			int mySiblingIndex = -1;
			for (int i = 0; i < siblings.size(); i++) {
				if (this.equals(siblings.get(i))) {
					mySiblingIndex = i;
					break;
				}
			}
			if (mySiblingIndex < 0) {
				return null;
			}
			int nextOptionalIndex = -1;
			for (int i = mySiblingIndex + 1; i < siblings.size(); i++) {
				if (siblings.get(i).getRule() != null && RuleType.BEGIN_OPTIONAL.equals(siblings.get(i).getRule().getType())) {
					nextOptionalIndex = i;
					break;
				}
			}
			if (nextOptionalIndex > 0) {
				for (int i = nextOptionalIndex + 1; i < siblings.size(); i++) {
					if (siblings.get(i).getText() != null) {
						return siblings.get(i);
					}
				}
				return null; // Note - we could go up to the parent to look for the next text token, but this is getting messy enough as it is
			} else {
				return parent.getNextNormalTextInstruction();
			}
		}

		/**
		 * @param skipFirstTextToken if true, the first text token will be skipped
		 */
		public void setSkipFirstToken(boolean skipFirstTextToken) {
			this.skipFirstTextToken = skipFirstTextToken;
		}
		
		/**
		 * @return true if the first text token should be skipped
		 */
		public boolean isSkipFirstTextToken() {
			return this.skipFirstTextToken;
		}
	}
	
	public class DifferenceDescription {
		private static final int MAX_DIFF_TEXT_LENGTH = 100;
		private boolean differenceFound;
		private String differenceMessage;
		private List<LineColumn> differences;
		
		public DifferenceDescription(boolean differenceFound, String differenceMessage, List<LineColumn> differences) {
			this.differenceFound = differenceFound;
			this.differenceMessage = differenceMessage;
			this.differences = differences;
		}

		public DifferenceDescription() {
			this.differenceFound = false;
			this.differenceMessage = "No difference found";
			this.differences = Lists.newArrayList();
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
		
		/**
		 * @param location Location in the text of the difference
		 * @param token Token causing the difference
		 * @param msg Message for the difference
		 * @param text Template text being compared to
		 * @param rule Template rule where difference was found
		 * @param lastOptionalDifference The difference for the last optional difference that failed
		 */
		public void addDifference(LineColumn location, String token, String msg, String text, 
				LicenseTemplateRule rule, DifferenceDescription lastOptionalDifference) {
			if (token == null) {
				token = "";
			}
			if (msg == null) {
				msg = "UNKNOWN (null)";
			}
			this.differenceMessage = msg;
			if (location != null) {
				this.differenceMessage = this.differenceMessage + " starting at line #"+
						String.valueOf(location.getLine())+ " column #" +
						String.valueOf(location.getColumn())+" \""+
						token+"\"";
				this.differences.add(location);
			} else {
				this.differenceMessage = this.differenceMessage + " at end of text";
			}
			if (text != null) {
				this.differenceMessage = this.differenceMessage + " when comparing to template text \"";
				if (text.length() > MAX_DIFF_TEXT_LENGTH) {
					this.differenceMessage = this.differenceMessage + 
							text.substring(0, MAX_DIFF_TEXT_LENGTH) + "...\"";
				} else {
					this.differenceMessage = this.differenceMessage + text + "\"";
				}
			}
			if (rule != null) {
				this.differenceMessage = this.differenceMessage + " while processing rule " + rule.toString();
			}
			if (lastOptionalDifference != null) {
				this.differenceMessage = this.differenceMessage + 
						".  Last optional text was not found due to the optional difference: \n\t" + 
						lastOptionalDifference.getDifferenceMessage();
			}
			this.differenceFound = true;
		}
	}

	String[] compareTokens = new String[0];
	String compareText = "";
	Map<Integer, LineColumn> tokenToLocation = new HashMap<Integer, LineColumn>();
	ParseInstruction topLevelInstruction = new ParseInstruction(null, null, null);
	DifferenceDescription differences = new DifferenceDescription();
	ParseInstruction currentOptionalInstruction = null;
	boolean parsingComplete = false;
	
	/**
	 * @param compareText Text to compare the parsed SPDX license template to
	 * @throws IOException This is not to be expected since we are using StringReaders
	 */
	public CompareTemplateOutputHandler(String compareText) throws IOException {
		this.compareText = LicenseCompareHelper.normalizeText(compareText);
		this.compareTokens = LicenseCompareHelper.tokenizeLicenseText(this.compareText, tokenToLocation);
	}
	
	/**
	 * @param textTokens
	 * @param matchTokens
	 * @param startToken
	 * @param endToken
	 * @param instruction
	 * @return positive index of the next match token after the match or negative index of the token which first failed the match
	 */
	private int compareText(String[] textTokens, String[] matchTokens, int startToken, int endToken,
			ParseInstruction instruction) {
		int textTokenCounter = 0;
		String nextTextToken = LicenseCompareHelper.getTokenAt(textTokens, textTokenCounter++);
		int matchTokenCounter = startToken;
		String nextMatchToken = LicenseCompareHelper.getTokenAt(matchTokens, matchTokenCounter++);
		while (nextTextToken != null) {
			if (nextMatchToken == null) {
				// end of compare text stream
				while (nextTextToken != null && LicenseCompareHelper.canSkip(nextTextToken)) {
					nextTextToken = LicenseCompareHelper.getTokenAt(textTokens, textTokenCounter++);
				}
				if (nextTextToken != null) {
					return -matchTokenCounter;	// there is more stuff in the compare license text, so not equiv.
				}
			} else if (LicenseCompareHelper.tokensEquivalent(nextTextToken, nextMatchToken)) { 
				// just move onto the next set of tokens
				nextTextToken = LicenseCompareHelper.getTokenAt(textTokens, textTokenCounter++);
				if (nextTextToken != null) {
					nextMatchToken = LicenseCompareHelper.getTokenAt(matchTokens, matchTokenCounter++);
				}
			} else {
				// see if we can skip through some compare tokens to find a match
				while (nextMatchToken != null && LicenseCompareHelper.canSkip(nextMatchToken)) {
					nextMatchToken = LicenseCompareHelper.getTokenAt(matchTokens, matchTokenCounter++);
				}
				// just to be sure, skip forward on the text
				while (nextTextToken != null && LicenseCompareHelper.canSkip(nextTextToken)) {
					nextTextToken = LicenseCompareHelper.getTokenAt(textTokens, textTokenCounter++);
				}
				if (LicenseCompareHelper.tokensEquivalent(nextMatchToken, nextTextToken)) {
					nextTextToken = LicenseCompareHelper.getTokenAt(textTokens, textTokenCounter++);
					if (nextTextToken != null) {
						nextMatchToken = LicenseCompareHelper.getTokenAt(compareTokens, matchTokenCounter++);
					}	
				} else {
					if (textTokenCounter == textTokens.length &&
							instruction != null &&
							instruction.isFollowingInstructionOptionalSingleToken() &&
							nextMatchToken != null) {
						//This is the special case where there may be optional characters which are
						//less than a token at the end of a compare
						//Yes - this is a bit of a hack
						String compareToken = nextTextToken + instruction.getNextOptionalTextTokens()[0];
						if (LicenseCompareHelper.tokensEquivalent(compareToken, nextMatchToken)) {
							instruction.skipNextInstruction();
							return matchTokenCounter;
						} else {
							ParseInstruction nextNormal = instruction.getNextNormalTextInstruction();
							String nextNormalText = LicenseCompareHelper.getFirstLicenseToken(nextNormal.getText());
							if (nextNormalText != null) {
								compareToken = compareToken + nextNormalText;
								String compareWithoutOptional = nextTextToken + nextNormalText;
								if (LicenseCompareHelper.tokensEquivalent(compareToken, nextMatchToken) ||
										LicenseCompareHelper.tokensEquivalent(compareWithoutOptional, nextMatchToken)) {
									instruction.skipNextInstruction();
									nextNormal.setSkipFirstToken(true);
									return matchTokenCounter;
								}
							}
						}
					}
					return -matchTokenCounter;
				}
			}
		}
		return matchTokenCounter;
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#text(java.lang.String)
	 */
	@Override
	public void text(String text) {
		if (currentOptionalInstruction != null) {
			currentOptionalInstruction.addSubInstruction(new ParseInstruction(null, text, currentOptionalInstruction));
		} else {
			this.topLevelInstruction.addSubInstruction(new ParseInstruction(null, text, null));
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#variableRule(org.spdx.licenseTemplate.LicenseTemplateRule)
	 */
	@Override
	public void variableRule(LicenseTemplateRule rule) {
		if (currentOptionalInstruction != null) {
			currentOptionalInstruction.addSubInstruction(new ParseInstruction(rule, null, currentOptionalInstruction));
		} else {
			this.topLevelInstruction.addSubInstruction(new ParseInstruction(rule, null, null));
		}
	}


	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#beginOptional(org.spdx.licenseTemplate.LicenseTemplateRule)
	 */
	@Override
	public void beginOptional(LicenseTemplateRule rule) {
		ParseInstruction optionalInstruction = new ParseInstruction(rule, null, currentOptionalInstruction);
		if (currentOptionalInstruction != null) {
			currentOptionalInstruction.addSubInstruction(optionalInstruction);
		} else {
			this.topLevelInstruction.addSubInstruction(optionalInstruction);
		}
		this.currentOptionalInstruction = optionalInstruction;
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#endOptional(org.spdx.licenseTemplate.LicenseTemplateRule)
	 */
	@Override
	public void endOptional(LicenseTemplateRule rule) {
		if (currentOptionalInstruction != null) {
			currentOptionalInstruction = currentOptionalInstruction.getParent();
			if (currentOptionalInstruction == null || currentOptionalInstruction.getRule() == null || currentOptionalInstruction.getRule().getType() != RuleType.BEGIN_OPTIONAL) {
				currentOptionalInstruction = null;
			}
		}
	}

	/**
	 * Performs the actual parsing if it has not been completed.  NOTE: This should only be called after all text has been added.
	 * @return true if no differences were found
	 * @throws LicenseParserException 
	 */
	public boolean matches() throws LicenseParserException {
		if (!parsingComplete) {
			throw new LicenseParserException("Matches was called prior to completing the parsing.  The method <code>competeParsing()</code> most be called prior to calling <code>matches()</code>");
		}
		return !this.differences.isDifferenceFound();
	}
	
	/**
	 * @return details on the differences found
	 */
	public DifferenceDescription getDifferences() {
		return this.differences;
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#completeParsing()
	 */
	@Override
	public void completeParsing() throws LicenseParserException {
		this.topLevelInstruction.match(compareTokens, 0, compareTokens.length-1, compareText, differences, tokenToLocation);
		parsingComplete = true;
	}

	/**
	 * Compares the text against the compareText
	 * @param text text to compare
	 * @param startToken token of the compareText to being the comparison
	 * @return next token index (positive) if there is a match, negative first token where this is a miss-match if no match
	 */
	public int textEquivalent(String text, int startToken) {
		Map<Integer, LineColumn> textLocations = new HashMap<Integer, LineColumn>();
		String[] textTokens = LicenseCompareHelper.tokenizeLicenseText(LicenseCompareHelper.normalizeText(text), textLocations);
		return this.compareText(textTokens, this.compareTokens, startToken, this.compareTokens.length-1, null);	
	}

}
