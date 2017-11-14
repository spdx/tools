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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.licenseTemplate.LicenseTemplateRule.RuleType;

/**
 * @author Source Auditor
 *
 */
public class TestHtmlTemplateOutputHandler {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.licenseTemplate.HtmlTemplateOutputHandler#normalText(java.lang.String)}.
	 */
	@Test
	public void testNormalText() {
		String normalText = "normal text\t\nwith spec chars>";
		String escapedNormalText = "normal text\t<br/>\nwith spec chars&gt;";
		HtmlTemplateOutputHandler htoh = new HtmlTemplateOutputHandler();
		htoh.text(normalText);
		assertEquals(escapedNormalText,htoh.getHtml());
	}

	/**
	 * Test method for {@link org.spdx.licenseTemplate.HtmlTemplateOutputHandler#variableRule(org.spdx.licenseTemplate.LicenseTemplateRule)}.
	 */
	@Test
	public void testVariableRule() throws LicenseTemplateRuleException {
		String ruleName = "testRule";
		String originalText = "Original \\ntext";
		String compareOriginalText = "Original <br/>\ntext";
		String matchText = "match text";
		String exampleText = "Example \\n text";
		LicenseTemplateRule normalRule = new LicenseTemplateRule(ruleName, RuleType.VARIABLE,
				originalText, matchText, exampleText);
		String expectedResult = "\n<span id=\"" + ruleName +
			"\" class=\"replacable-license-text\">" + compareOriginalText +
			"</span>\n";
		HtmlTemplateOutputHandler htoh = new HtmlTemplateOutputHandler();
		htoh.variableRule(normalRule);
		assertEquals(expectedResult, htoh.getHtml());
	}

	/**
	 * Test method for {@link org.spdx.licenseTemplate.HtmlTemplateOutputHandler#formatReplaceabledHTML(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testFormatReplaceabledHTML() {
		String originalText = "Original \ntext";
		String compareOriginalText = "Original <br/>\ntext";
		String ruleName = "testRule";
		String expectedResult = "\n<span id=\"" + ruleName +
		"\" class=\"replacable-license-text\">" + compareOriginalText +
		"</span>\n";
		String result = HtmlTemplateOutputHandler.formatReplaceabledHTML(originalText, ruleName);
		assertEquals(expectedResult, result);
	}

	/**
	 * Test method for {@link org.spdx.licenseTemplate.HtmlTemplateOutputHandler#escapeIdString(java.lang.String)}.
	 */
	@Test
	public void testEscapeIdString() {
		// not starting with letter
		String testId = "1test";
		assertEquals("X"+testId, HtmlTemplateOutputHandler.escapeIdString(testId));
		// invalid character
		String invalidChar = "idWith:Invalid";
		String validChar = "idWith_Invalid";
		assertEquals(validChar, HtmlTemplateOutputHandler.escapeIdString(invalidChar));
		String allValid = "iDwith0-.valid";
		assertEquals(allValid, HtmlTemplateOutputHandler.escapeIdString(allValid));
	}

	/**
	 * Test method for {@link org.spdx.licenseTemplate.HtmlTemplateOutputHandler#getHtml()}.
	 * @throws LicenseTemplateRuleException
	 */
	@Test
	public void testGetHtml() throws LicenseTemplateRuleException {
		HtmlTemplateOutputHandler htoh = new HtmlTemplateOutputHandler();
		String beginText = "Begin text\n";
		String escapedBeginText = "Begin text<br/>\n";
		htoh.text(beginText);

		String optRuleName = "optionalRule";

		LicenseTemplateRule beginRule = new LicenseTemplateRule(optRuleName, RuleType.BEGIN_OPTIONAL);
		htoh.beginOptional(beginRule);
		String optionalText = "Optional Text";
		htoh.text(optionalText);
		String escapedBeginRuleText = "\n<div id=\"" + optRuleName + "\" class=\"optional-license-text\">\n";
		String escapedOptionalText = optionalText;

		String varRuleName = "testRule";
		String originalText = "Original \\ntext";
		String compareOriginalText = "Original <br/>\ntext";
		String matchText = "match text";
		String exampleText = "Example \\n text";
		LicenseTemplateRule normalRule = new LicenseTemplateRule(varRuleName, RuleType.VARIABLE,
				originalText, matchText, exampleText);
		String escapedVariableRuleText = "\n<span id=\"" + varRuleName +
			"\" class=\"replacable-license-text\">" + compareOriginalText +
			"</span>\n";
		htoh.variableRule(normalRule);
		LicenseTemplateRule endRule = new LicenseTemplateRule(optRuleName, RuleType.END_OPTIONAL);
		htoh.endOptional(endRule);
		String escapedEndRuleText = "</div>\n";

		String lastLine = "\nLast Line.&";
		htoh.text(lastLine);
		String escapedLastLine = "<br/>\nLast Line.&amp;";

		String expectedValue = escapedBeginText +escapedBeginRuleText+escapedOptionalText+
		escapedVariableRuleText+escapedEndRuleText+escapedLastLine;

		assertEquals(expectedValue, htoh.getHtml());
	}

	/**
	 * Test method for {@link org.spdx.licenseTemplate.HtmlTemplateOutputHandler#beginOptional(org.spdx.licenseTemplate.LicenseTemplateRule)}.
	 * @throws LicenseTemplateRuleException
	 */
	@Test
	public void testBeginOptional() throws LicenseTemplateRuleException {
		HtmlTemplateOutputHandler htoh = new HtmlTemplateOutputHandler();
		String optRuleName = "optionalRule";

		LicenseTemplateRule beginRule = new LicenseTemplateRule(optRuleName, RuleType.BEGIN_OPTIONAL);
		htoh.beginOptional(beginRule);
		String escapedBeginRuleText = "\n<div id=\"" + optRuleName + "\" class=\"optional-license-text\">\n";
		assertEquals(escapedBeginRuleText, htoh.getHtml());
	}

	/**
	 * Test method for {@link org.spdx.licenseTemplate.HtmlTemplateOutputHandler#formatStartOptionalHTML(java.lang.String)}.
	 * @throws LicenseTemplateRuleException
	 */
	@Test
	public void testFormatStartOptionalHTML() throws LicenseTemplateRuleException {
		String optRuleName = "optionalRule";
		String escapedBeginRuleText = "\n<div id=\"" + optRuleName + "\" class=\"optional-license-text\">\n";
		assertEquals(escapedBeginRuleText, HtmlTemplateOutputHandler.formatStartOptionalHTML(optRuleName));
	}

	/**
	 * Test method for {@link org.spdx.licenseTemplate.HtmlTemplateOutputHandler#formatEndOptionalHTML()}.
	 */
	@Test
	public void testFormatEndOptionalHTML() {
		String escapedEndRuleText = "</div>\n";
		assertEquals(escapedEndRuleText, HtmlTemplateOutputHandler.formatEndOptionalHTML(false));

	}

	/**
	 * Test method for {@link org.spdx.licenseTemplate.HtmlTemplateOutputHandler#endOptional(org.spdx.licenseTemplate.LicenseTemplateRule)}.
	 * @throws LicenseTemplateRuleException
	 */
	@Test
	public void testEndOptional() throws LicenseTemplateRuleException {
		HtmlTemplateOutputHandler htoh = new HtmlTemplateOutputHandler();
		String optRuleName = "optionalRule";

		LicenseTemplateRule beginRule = new LicenseTemplateRule(optRuleName, RuleType.BEGIN_OPTIONAL);
		htoh.beginOptional(beginRule);

		LicenseTemplateRule endRule = new LicenseTemplateRule(optRuleName, RuleType.END_OPTIONAL);
		htoh.endOptional(endRule);
		String escapedEndRuleText = "</div>\n";
		assertTrue(htoh.getHtml().endsWith(escapedEndRuleText));

	}

}
