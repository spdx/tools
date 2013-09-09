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

import org.apache.commons.lang3.StringEscapeUtils;
import org.spdx.licenseTemplate.LicenseTemplateRule.RuleType;

/**
 * Implements common conversion methods for processing SPDX license templates
 * @author Gary O'Neall
 *
 */
public class SpdxLicenseTemplateHelper {

	public static final String REPLACEABLE_LICENSE_TEXT_CLASS = "replacable-license-text";
	public static final String OPTIONAL_LICENSE_TEXT_CLASS = "optional-license-text";
	
	static final String START_RULE = "<<";
	static final String END_RULE = ">>";
	public static final Pattern RULE_PATTERN = Pattern.compile(START_RULE+"\\s*(.+)\\s*"+END_RULE);
	private static final String STARTS_WITH_LETTER_REGEX = "[A-Za-z].*";

	/**
	 * Converts a license template string to formatted HTML which highlights any 
	 * rules or tags
	 * @param licenseTemplate
	 * @return
	 * @throws LicenseTemplateRuleException 
	 */
	public static String templateTextToHtml(String licenseTemplate) throws LicenseTemplateRuleException {
		Matcher ruleMatcher = RULE_PATTERN.matcher(licenseTemplate);
		StringBuilder retval = new StringBuilder();
		int end = 0;
		while (ruleMatcher.find()) {
			// copy everything up to the start of the find
			String upToTheFind = licenseTemplate.substring(end, ruleMatcher.start());
			retval.append(escapeHTML(upToTheFind));
			end = ruleMatcher.end();
			String rule = ruleMatcher.group(1);
			retval.append(ruleToHTML(rule));
		}
		// copy the rest of the template to the end
		String restOfTemplate = licenseTemplate.substring(end);
		retval.append(escapeHTML(restOfTemplate));
		return retval.toString();
	}

	/**
	 * @param rule
	 * @return
	 * @throws LicenseTemplateRuleException 
	 */
	private static String ruleToHTML(String ruleString) throws LicenseTemplateRuleException {
		LicenseTemplateRule rule = new LicenseTemplateRule(ruleString);
		if (rule.getType() == RuleType.OPTIONAL) {
			return formatOptionalHTML(rule.getOriginal(), rule.getName());
		} else if (rule.getType() == RuleType.REQUIRED) {
			return formatReplaceabledHTML(rule.getOriginal(), rule.getName());
		} else {
			throw(new LicenseTemplateRuleException("Unsupported rule type: "+rule.getType().toString()));
		}
	}

	/**
	 * Format HTML for a replaceable string
	 * @param text text for the optional license string
	 * @param id ID used for the div 
	 * @return
	 */
	private static String formatReplaceabledHTML(String text, String id) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n<div ");
		if (id != null && !id.trim().isEmpty()) {
			sb.append("id=\"");
			sb.append(escapeIdString(id));
			sb.append("\" ");
		}
		sb.append("class=\"");
		sb.append(REPLACEABLE_LICENSE_TEXT_CLASS);
		sb.append("\">");
		sb.append(escapeHTML(text));
		sb.append("</div>\n");
		return sb.toString();
	}

	/**
	 * Format HTML for an optional string
	 * @param text text for the optional license string
	 * @param id ID used for the div 
	 * @return
	 */
	private static String formatOptionalHTML(String text, String id) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n<div ");
		if (id != null && !id.trim().isEmpty()) {
			sb.append("id=\"");
			sb.append(escapeIdString(id));
			sb.append("\" ");
		}
		sb.append("class=\"");
		sb.append(OPTIONAL_LICENSE_TEXT_CLASS);
		sb.append("\">");
		sb.append(escapeHTML(text));
		sb.append("</div>\n");
		return sb.toString();
	}

	/**
	 * Escape the ID string to conform to the legal characters for an HTML ID string
	 * @param id
	 * @return
	 */
	public static String escapeIdString(String id) {
		String retval = id;
		if (!retval.matches(STARTS_WITH_LETTER_REGEX)) {
			retval = "X" + retval;
		}
		for (int i = 0; i < retval.length(); i++) {
			char c = retval.charAt(i);
			if (!validIdChar(c)) {
				// replace with "_"
				retval = retval.replace(c, '_');
			}
		}
		return retval;
	}

	/**
	 * @param c
	 * @return true if c is a valid character for an ID string
	 */
	private static boolean validIdChar(char c) {
		return ((c >= 'a' && c <= 'z') ||
				(c >= 'A' && c <= 'Z') ||
				(c >= '0' && c <= '9') ||
				(c == '-')||
				(c == '_') ||
				(c == '.'));
	}

	/**
	 * Converts template text to standard default text using any default parameters in the rules
	 * @param template
	 * @return
	 * @throws LicenseTemplateRuleException 
	 */
	public static String templateToText(String template) throws LicenseTemplateRuleException {
		Matcher ruleMatcher = RULE_PATTERN.matcher(template);
		StringBuilder retval = new StringBuilder();
		int end = 0;
		while (ruleMatcher.find()) {
			// copy everything up to the start of the find
			String upToTheFind = template.substring(end, ruleMatcher.start());
			retval.append(upToTheFind);
			end = ruleMatcher.end();
			String ruleString = ruleMatcher.group(1);
			LicenseTemplateRule rule = new LicenseTemplateRule(ruleString);
			retval.append(rule.getOriginal());
		}
		// copy the rest of the template to the end
		String restOfTemplate = template.substring(end);
		retval.append(restOfTemplate);
		return retval.toString();
	}
	
	public static String escapeHTML(String text) {
		String retval = StringEscapeUtils.escapeXml(text);
		return retval.replace("\n", "<br/>\n");
	}

}
