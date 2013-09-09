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
package org.spdx.licenseTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements a license rule
 * @author Gary O'Neall
 *
 */
public class LicenseTemplateRule {
	
	public enum RuleType {REQUIRED, OPTIONAL};
	
	RuleType type;
	String original;
	String name;
	String example;
	String match;
	
	static final Pattern SPLIT_REGEX = Pattern.compile("[^\\\\];");
	private static final String EXAMPLE_KEYWORD = "example";
	private static final String NAME_KEYWORD = "name";
	private static final String TYPE_KEYWORD = "type";
	private static final String ORIGINAL_KEYWORD = "original";
	private static final String MATCH_KEYWORD = "match";
	
	/**
	 * Create a new LicenseTemplateRule
	 * @param name Name of the rule - must not be null
	 * @param type - type of rule
	 * @param original - Original text - must not be null
	 * @param example - Example text - may be null
	 * @throws LicenseTemplateRuleException 
	 */
	public LicenseTemplateRule(String name, RuleType type, String original, String match, String example) throws LicenseTemplateRuleException {
		this.type = type;
		this.original = original;
		this.name = name;
		this.example = example;
		this.match = match;
		validate();
	}
	
	/**
	 * Validates that the LicenseTemplateRule is properly initialized
	 * @throws LicenseTemplateRuleException 
	 */
	public void validate() throws LicenseTemplateRuleException {
		if (this.name == null) {
			throw(new LicenseTemplateRuleException("Rule name can not be null."));
		}
		if (this.type == null) {
			throw(new LicenseTemplateRuleException("Rule type can not be null."));
		}
		if (this.original == null) {
			throw(new LicenseTemplateRuleException("Rule original text can not be null."));
		}
		if (this.match == null) {
			throw(new LicenseTemplateRuleException("Rule match regular expression can not be null."));
		}
	}

	/**
	 * Create a new License Template Rule by parsing a rule string compliant with the SPDX
	 * License Template text
	 * @param parseableLicenseTemplateRule
	 * @throws LicenseTemplateRuleException 
	 */
	public LicenseTemplateRule(String parseableLicenseTemplateRule) throws LicenseTemplateRuleException {
		parseLicenseTemplateRule(parseableLicenseTemplateRule);
		validate();
	}

	/**
	 * Parse a license template rule string compliant with the SPDX license template text and
	 * replace all properties with the parsed values
	 * @param parseableLicenseTemplateRule
	 * @throws LicenseTemplateRuleException 
	 */
	public void parseLicenseTemplateRule(String parseableLicenseTemplateRule) throws LicenseTemplateRuleException {
		//TODO: Check for repeated keywords
		this.example = null;
		this.name = null;
		this.original = null;
		this.type = null;
		this.match = null;
		Matcher rulePartMatcher = SPLIT_REGEX.matcher(parseableLicenseTemplateRule);
		int start = 0;
		while (rulePartMatcher.find()) {
			String rulePart = parseableLicenseTemplateRule.substring(start, rulePartMatcher.start()+1);
			parseRulePart(rulePart.trim());
			start = rulePartMatcher.end();
		}
		String remainingRuleString = parseableLicenseTemplateRule.substring(start).trim();
		if (!remainingRuleString.isEmpty()) {
			parseRulePart(remainingRuleString);
		}
		validate();
	}

	/**
	 * @return the type
	 */
	public RuleType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(RuleType type) {
		this.type = type;
	}

	/**
	 * @return the original
	 */
	public String getOriginal() {
		return original;
	}

	/**
	 * @param original the original to set
	 */
	public void setOriginal(String original) {
		this.original = original;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the example
	 */
	public String getExample() {
		return example;
	}

	/**
	 * @param example the example to set
	 */
	public void setExample(String example) {
		this.example = example;
	}

	/**
	 * @return the match
	 */
	public String getMatch() {
		return match;
	}

	/**
	 * @param match the match to set
	 */
	public void setMatch(String match) {
		this.match = match;
	}

	/**
	 * Parse the part of a rule and stores the result as a property
	 * @param rulePart
	 * @throws LicenseTemplateRuleException 
	 */
	private void parseRulePart(String rulePart) throws LicenseTemplateRuleException {
		if (rulePart.startsWith(EXAMPLE_KEYWORD)) {
			String value = getValue(rulePart, EXAMPLE_KEYWORD);
			this.example = formatValue(value);
		} else if (rulePart.startsWith(NAME_KEYWORD)) {
			this.name = getValue(rulePart, NAME_KEYWORD);
		} else if (rulePart.startsWith(TYPE_KEYWORD)) {
			String value = getValue(rulePart, TYPE_KEYWORD);
			try {				
				this.type = RuleType.valueOf(value.toUpperCase());
			} catch(Exception ex) {
				throw(new LicenseTemplateRuleException("Invalid rule type: "+value));
			}
		} else if (rulePart.startsWith(ORIGINAL_KEYWORD)) {
			String value = getValue(rulePart, ORIGINAL_KEYWORD);
			this.original = formatValue(value);
		} else if (rulePart.startsWith(MATCH_KEYWORD)) {
			this.match = getValue(rulePart, MATCH_KEYWORD);
		} else {
			throw(new LicenseTemplateRuleException("Unknown rule keyword: "+rulePart));
		}
	}

	/**
	 * Formats the string interpreting escape characters
	 * @param value
	 * @return
	 */
	private String formatValue(String value) {
		String retval = value.replace("\\n", "\n");
		retval = retval.replace("\\t", "\t");
		return retval;
	}

	/**
	 * Retrieve the value portion of a rule part
	 * @param rulePart
	 * @param keyword
	 * @return
	 * @throws LicenseTemplateRuleException 
	 */
	private String getValue(String rulePart, String keyword) throws LicenseTemplateRuleException {
		String retval = rulePart.substring(keyword.length());
		retval = retval.trim();
		if (!retval.startsWith("=")) {
			throw(new LicenseTemplateRuleException("Missing '=' for "+keyword));
		}
		retval = retval.substring(1).trim();
		return retval;
	}

}
