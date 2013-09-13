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

/**
 * License template output handler for generating an HTML version of a license from a license template.
 * Used when parsing a license template.
 * @author Gary O'Neall
 *
 */
public class HtmlTemplateOutputHandler implements ILicenseTemplateOutputHandler {
	
	public static final String REPLACEABLE_LICENSE_TEXT_CLASS = "replacable-license-text";
	public static final String OPTIONAL_LICENSE_TEXT_CLASS = "optional-license-text";
	
	private static final String STARTS_WITH_LETTER_REGEX = "[A-Za-z].*";

	
	StringBuilder htmlString = new StringBuilder();

	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#optionalText(java.lang.String)
	 */
	@Override
	public void optionalText(String text) {
		htmlString.append(text);
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#normalText(java.lang.String)
	 */
	@Override
	public void normalText(String text) {
		htmlString.append(SpdxLicenseTemplateHelper.escapeHTML(text));
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#variableRule(org.spdx.licenseTemplate.LicenseTemplateRule)
	 */
	@Override
	public void variableRule(LicenseTemplateRule rule) {
		htmlString.append(formatReplaceabledHTML(rule.getOriginal(), rule.getName()));
	}
	
	/**
	 * Format HTML for a replaceable string
	 * @param text text for the optional license string
	 * @param id ID used for the div 
	 * @return
	 */
	public static String formatReplaceabledHTML(String text, String id) {
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
		sb.append(SpdxLicenseTemplateHelper.escapeHTML(text));
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
	 * @return
	 */
	public String getHtml() {
		return this.htmlString.toString();
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#beginOptional(org.spdx.licenseTemplate.LicenseTemplateRule)
	 */
	@Override
	public void beginOptional(LicenseTemplateRule rule) {
		this.htmlString.append(formatStartOptionalHTML(rule.getName()));
	}
	
	/**
	 * Format HTML for an optional string
	 * @param text text for the optional license string
	 * @param id ID used for the div 
	 * @return
	 */
	public static String formatStartOptionalHTML(String id) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n<div ");
		if (id != null && !id.trim().isEmpty()) {
			sb.append("id=\"");
			sb.append(escapeIdString(id));
			sb.append("\" ");
		}
		sb.append("class=\"");
		sb.append(OPTIONAL_LICENSE_TEXT_CLASS);
		sb.append("\">\n");
		return sb.toString();
	}

	public static String formatEndOptionalHTML() {
		return "</div>\n";
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#endOptional(org.spdx.licenseTemplate.LicenseTemplateRule)
	 */
	@Override
	public void endOptional(LicenseTemplateRule rule) {
		this.htmlString.append(formatEndOptionalHTML());
	}

}
