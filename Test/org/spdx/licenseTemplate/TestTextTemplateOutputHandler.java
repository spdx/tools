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
public class TestTextTemplateOutputHandler {

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
	 * Test method for {@link org.spdx.licenseTemplate.TextTemplateOutputHandler#normalText(java.lang.String)}.
	 */
	@Test
	public void testNormalText() {
		String test = "test normal\n";
		TextTemplateOutputHandler oh = new TextTemplateOutputHandler();
		oh.text(test);
		assertEquals(test, oh.getText());
	}

	/**
	 * Test method for {@link org.spdx.licenseTemplate.TextTemplateOutputHandler#variableRule(org.spdx.licenseTemplate.LicenseTemplateRule)}.
	 * @throws LicenseTemplateRuleException
	 */
	@Test
	public void testVariableRule() throws LicenseTemplateRuleException {
		String ruleName = "testRule";
		String originalText = "Original \\ntext";
		String compareOriginalText = "Original \ntext";
		String matchText = "match text";
		String exampleText = "Example \\n text";
		LicenseTemplateRule normalRule = new LicenseTemplateRule(ruleName, RuleType.VARIABLE,
				originalText, matchText, exampleText);
		TextTemplateOutputHandler oh = new TextTemplateOutputHandler();
		oh.variableRule(normalRule);
		assertEquals(compareOriginalText, oh.getText());
	}

	/**
	 * Test method for {@link org.spdx.licenseTemplate.TextTemplateOutputHandler#beginOptional(org.spdx.licenseTemplate.LicenseTemplateRule)}.
	 * @throws LicenseTemplateRuleException
	 */
	@Test
	public void testBeginOptional() throws LicenseTemplateRuleException {
		String ruleName = "testRule";
		String originalText = "Original \\ntext";
		String matchText = "match text";
		String exampleText = "Example \\n text";
		LicenseTemplateRule normalRule = new LicenseTemplateRule(ruleName, RuleType.BEGIN_OPTIONAL,
				originalText, matchText, exampleText);
		TextTemplateOutputHandler oh = new TextTemplateOutputHandler();
		oh.beginOptional(normalRule);
		assertEquals("", oh.getText());
	}

	/**
	 * Test method for {@link org.spdx.licenseTemplate.TextTemplateOutputHandler#endOptional(org.spdx.licenseTemplate.LicenseTemplateRule)}.
	 * @throws LicenseTemplateRuleException
	 */
	@Test
	public void testEndOptional() throws LicenseTemplateRuleException {
		String ruleName = "testRule";
		String originalText = "Original \\ntext";
		String matchText = "match text";
		String exampleText = "Example \\n text";
		LicenseTemplateRule normalRule = new LicenseTemplateRule(ruleName, RuleType.END_OPTIONAL,
				originalText, matchText, exampleText);
		TextTemplateOutputHandler oh = new TextTemplateOutputHandler();
		oh.endOptional(normalRule);
		assertEquals("", oh.getText());
	}

	/**
	 * Test method for {@link org.spdx.licenseTemplate.TextTemplateOutputHandler#getText()}.
	 * @throws LicenseTemplateRuleException
	 */
	@Test
	public void testGetText() throws LicenseTemplateRuleException {
		String normalText = "Normal text";
		TextTemplateOutputHandler oh = new TextTemplateOutputHandler();
		assertEquals("", oh.getText());
		String ruleName = "testRule";
		String originalText = "Original \\ntext";
		String matchText = "match text";
		String exampleText = "Example \\n text";
		LicenseTemplateRule beginRule = new LicenseTemplateRule(ruleName, RuleType.BEGIN_OPTIONAL,
				originalText, matchText, exampleText);
		LicenseTemplateRule endRule = new LicenseTemplateRule(ruleName, RuleType.END_OPTIONAL,
				originalText, matchText, exampleText);
		oh.beginOptional(beginRule);
		oh.text(normalText);
		oh.endOptional(endRule);
		assertEquals(normalText, oh.getText());
	}

}
