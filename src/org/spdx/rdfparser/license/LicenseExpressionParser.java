/**
 * Copyright (c) 2015 Source Auditor Inc.
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
package org.spdx.rdfparser.license;

import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.SpdxRdfConstants;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A parser for the SPDX License Expressions as documented in the SPDX appendix.
 * 
 * This is a static help class.  The primary method is parseLicenseExpression which 
 * returns an AnyLicenseInfo.
 * @author Gary O'Neall
 *
 */
public class LicenseExpressionParser {
	
	enum Operator {
		OR_LATER, WITH, AND, OR	//NOTE: These must be in precedence order 
	};
	static final String LEFT_PAREN = "(";
	static final String RIGHT_PAREN = ")";
	static final Map<String, Operator> OPERATOR_MAP = Maps.newHashMap();
	
	static {
		OPERATOR_MAP.put("+", Operator.OR_LATER);
		OPERATOR_MAP.put("AND", Operator.AND);
		OPERATOR_MAP.put("OR", Operator.OR);
		OPERATOR_MAP.put("WITH", Operator.WITH);
		OPERATOR_MAP.put("and", Operator.AND);
		OPERATOR_MAP.put("or", Operator.OR);
		OPERATOR_MAP.put("with", Operator.WITH);
	}
	/**
	 * Parses a license expression into an license for use in the RDF Parser
	 * @param expression Expression to be parsed
	 * @param container Container containing any extractedLicenseInfos - if any extractedLicenseInfos by ID already exist, they will be used.  If
	 * none exist for an ID, they will be added.  If null, a simple Java object will be created for the extractedLicenseInfo.
	 * @return 
	 * @throws InvalidSPDXAnalysisException 
	 */
	static AnyLicenseInfo parseLicenseExpression(String expression, SpdxDocumentContainer container) throws InvalidSPDXAnalysisException {
		if (expression == null || expression.trim().isEmpty()) {
			throw(new LicenseParserException("Empty license expression"));
		}
		String[] tokens  = tokenizeExpression(expression);
		if (tokens.length == 1 && tokens[0].equals(SpdxRdfConstants.NOASSERTION_VALUE)) {
			return new SpdxNoAssertionLicense();
		} else if (tokens.length == 1 && tokens[0].equals(SpdxRdfConstants.NONE_VALUE)) {
			return new SpdxNoneLicense();
		} else {
			try {
				return parseLicenseExpression(tokens, container);
			} catch (EmptyStackException ex) {
				throw(new LicenseParserException("Invalid license expression - check that every operator (e.g. AND and OR) has operators and that parenthesis are matched"));
			}
		}
	}

	/**
	 * A custom tokenizer since there is not white space between parents and pluses
	 * @param expression
	 * @return
	 */
	private static String[] tokenizeExpression(String expression) {
		String[] startTokens = expression.split("\\s");
		List<String> endTokens = Lists.newArrayList();
		for (String token : startTokens) {
			processPreToken(token, endTokens);
		}
		return endTokens.toArray(new String[endTokens.size()]);
	}

	/**
	 * @param preToken
	 * @param tokenList
	 */
	private static void processPreToken(String preToken,
			List<String> tokenList) {
		if (preToken.isEmpty()) {
			return;
		} else if (preToken.startsWith("(")) {
			tokenList.add("(");
			processPreToken(preToken.substring(1), tokenList);
		} else if (preToken.endsWith(")")) {
			processPreToken(preToken.substring(0, preToken.length()-1), tokenList);
			tokenList.add(")");
		} else if (preToken.endsWith("+")) {
			processPreToken(preToken.substring(0, preToken.length()-1), tokenList);
			tokenList.add("+");
		} else {
			tokenList.add(preToken);
		}
	}

	/**
	 * Parses a tokenized license expression into a license for use in the RDF Parser
	 * @param tokens
	 * @param container
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static AnyLicenseInfo parseLicenseExpression(String[] tokens, SpdxDocumentContainer container) throws InvalidSPDXAnalysisException {
		if (tokens == null || tokens.length == 0) {
			throw(new LicenseParserException("Expected license expression"));
		}
		Stack<AnyLicenseInfo> operandStack = new Stack<AnyLicenseInfo>();
		Stack<Operator> operatorStack = new Stack<Operator>(); 
		int tokenIndex = 0;
		String token;
		while (tokenIndex < tokens.length) {
			token = tokens[tokenIndex++];
			// left operand
			if (LEFT_PAREN.equals(token)) {
				int rightParenIndex = findMatchingParen(tokens, tokenIndex);
				if (rightParenIndex < 0) {
					throw(new LicenseParserException("Missing right parenthesis"));
				}
				String[] nestedTokens = Arrays.copyOfRange(tokens, tokenIndex, rightParenIndex);
				operandStack.push(parseLicenseExpression(nestedTokens, container));
				tokenIndex = rightParenIndex + 1;		
			} else if (OPERATOR_MAP.get(token) == null) {	// assumed to be a simple licensing type
				operandStack.push(parseSimpleLicenseToken(token, container));
			} else {
				Operator operator = OPERATOR_MAP.get(token);
				if (operator == Operator.WITH) {
					// special processing here since With must be with an exception, not a licenseInfo
					if (!operatorStack.isEmpty() && Operator.OR_LATER.equals(operatorStack.peek())) {
						Operator tosOperator = operatorStack.pop();
						evaluateExpression(tosOperator, operandStack);
					}
					if (tokenIndex >= tokens.length) {
						throw(new LicenseParserException("Missing exception clause"));
					}
					token = tokens[tokenIndex++];
					LicenseException licenseException = null;
					if (LicenseInfoFactory.isSPdxListedExceptionID(token)) {
						licenseException = LicenseInfoFactory.getListedExceptionById(token);
					} else {
						licenseException = new LicenseException(token);
					}
					AnyLicenseInfo operand = operandStack.pop();
					if (operand == null) {
						throw(new LicenseParserException("Missing license for with clause"));
					}
					if (!((operand instanceof SimpleLicensingInfo) || (operand instanceof OrLaterOperator))) {
						throw(new LicenseParserException("License with exception is not of type SimpleLicensingInfo or OrLaterOperator"));
					}
					operandStack.push(new WithExceptionOperator(operand, licenseException));			
				} else {
					// process in order of prcedence using the shunting yard algorithm
					while (!operatorStack.isEmpty() && 
							operatorStack.peek().ordinal() <= operator.ordinal()) {
						Operator tosOperator = operatorStack.pop();
						evaluateExpression(tosOperator, operandStack);
					}
					operatorStack.push(operator);
				}
			}
		}
		// go through the rest of the stack
		while (!operatorStack.isEmpty()) {
			Operator tosOperator = operatorStack.pop();
			evaluateExpression(tosOperator, operandStack);
		}
		AnyLicenseInfo retval = operandStack.pop();
		if (!operandStack.isEmpty()) {
			throw(new LicenseParserException("Invalid license expression.  Expecting more operands."));
		}
		return retval;
	}

	/**
	 * Returns the index of the rightmost parenthesis or -1 if not found
	 * @param tokens
	 * @return
	 */
	private static int findMatchingParen(String[] tokens, int startToken) {
		if (tokens == null) {
			return -1;
		}
		int nestCount = 0;
		for (int i = startToken; i < tokens.length; i++) {
			if (LEFT_PAREN.equals(tokens[i])) {
				nestCount++;
			} else if (RIGHT_PAREN.equals(tokens[i])) {
				if (nestCount == 0) {
					return i;
				} else {
					nestCount--;
				}
			}
		}
		return -1;
	}

	/**
	 * Converts a string token into its equivalent license
	 * checking for a listed license
	 * @param token
	 * @param container 
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static AnyLicenseInfo parseSimpleLicenseToken(String token, SpdxDocumentContainer container) throws InvalidSPDXAnalysisException {
		if (LicenseInfoFactory.isSpdxListedLicenseID(token)) {
			return LicenseInfoFactory.getListedLicenseById(token);
		} else {
			ExtractedLicenseInfo retval = null;
			if (container != null) {
				if (container.extractedLicenseExists(token)) {
					retval = container.getExtractedLicense(token);
				} else {
					retval = new ExtractedLicenseInfo(token, null);
					container.addNewExtractedLicenseInfo(retval);
				}
			} else {
				retval = new ExtractedLicenseInfo(token, null);
			}
			return retval;
		}
	}

	/**
	 * Evaluate the given operator using paramaeters in the parameter stack
	 * @param operator
	 * @param operandStack
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static void evaluateExpression(Operator operator,
			Stack<AnyLicenseInfo> operandStack) throws InvalidSPDXAnalysisException {
		if (operator == Operator.OR_LATER) {
			// unary operator
			AnyLicenseInfo license = operandStack.pop();
			if (!(license instanceof SimpleLicensingInfo)) {
				throw(new LicenseParserException("Missing license for the '+' or later operator"));
			}
			operandStack.push(new OrLaterOperator((SimpleLicensingInfo)license));
		} else {
			// binary operator
			AnyLicenseInfo operand2 = operandStack.pop();
			AnyLicenseInfo operand1 = operandStack.pop();
			if (operand1 == null || operand2 == null) {
				throw(new LicenseParserException("Missing operands for the "+operator.toString()+" operator"));
			}
			operandStack.push(evaluateBinary(operator, operand1, operand2));
		}		
	}

	/**
	 * Evaluates a binary expression and merges conjuctive and disjunctive licenses
	 * @param tosOperator
	 * @param operand1
	 * @param operand2
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static AnyLicenseInfo evaluateBinary(Operator tosOperator,
			AnyLicenseInfo operand1, AnyLicenseInfo operand2) throws InvalidSPDXAnalysisException {
		if (tosOperator == Operator.AND) {
			if (operand1 instanceof ConjunctiveLicenseSet) {
				// just merge into operand1
				AnyLicenseInfo[] origMembers = ((ConjunctiveLicenseSet) operand1).getMembers();
				AnyLicenseInfo[] newMembers = Arrays.copyOf(origMembers, origMembers.length+1);
				newMembers[origMembers.length] = operand2;
				((ConjunctiveLicenseSet) operand1).setMembers(newMembers);
				return operand1;
			} else {
				AnyLicenseInfo[] members = new AnyLicenseInfo[] {operand1, operand2};
				return new ConjunctiveLicenseSet(members);
			}
		} else if (tosOperator == Operator.OR) {
			if (operand1 instanceof DisjunctiveLicenseSet) {
				// just merge into operand1
				AnyLicenseInfo[] origMembers = ((DisjunctiveLicenseSet) operand1).getMembers();
				AnyLicenseInfo[] newMembers = Arrays.copyOf(origMembers, origMembers.length+1);
				newMembers[origMembers.length] = operand2;
				((DisjunctiveLicenseSet) operand1).setMembers(newMembers);
				return operand1;
			} else {
				AnyLicenseInfo[] members = new AnyLicenseInfo[] {operand1, operand2};
				return new DisjunctiveLicenseSet(members);
			}
		} else {
			throw(new LicenseParserException("Unknown operator "+tosOperator.toString()));
		}
	}
}
