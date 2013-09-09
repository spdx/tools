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

/**
 * @author Gary O'Neall
 *
 */
public class TestSpdxLicenseTemplateHelper {
	
	static final String LINE1 = "This is the start of the license.\n";
	static final String REQUIRED_RULE="<<original=Copyright (c) <year> <owner>\\nAll rights reserved.;match=Copyright \\(c\\) .+All rights reserved.;name=copyright;type=required;example=Copyright (C) 2013 John Doe\\nAll rights reserved.>>";
	static final String LINE2 = "\nRedistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met";
	static final String OPTIONAL_RULE="<<original=Original Text;match=Original Text;type=optional;name=optional>>";
	static final String LINE3="\nLast line of the license";
	static final String TEMPLATE_TEXT = LINE1+REQUIRED_RULE+LINE2+OPTIONAL_RULE+LINE3;
	
	static final String HTML_COPYRIGHT="\n<div id=\"copyright\" class=\"replacable-license-text\">Copyright (c) &lt;year&gt; &lt;owner&gt;<br/>\nAll rights reserved.</div>\n";
	static final String HTML_OPTIONAL_RULE="\n<div id=\"optional\" class=\"optional-license-text\">Original Text</div>\n";
	static final String HTML_LICENSE = LINE1.replace("\n", "<br/>\n")+
			HTML_COPYRIGHT+
			LINE2.replace("\n", "<br/>\n")+
			HTML_OPTIONAL_RULE+
			LINE3.replace("\n", "<br/>\n");
	
	static final String TEXT_COPYRIGHT = "Copyright (c) <year> <owner>\nAll rights reserved.";
	static final String TEXT_OPTIONAL_RULE = "Original Text";
	static final String TEXT_LICENSE = LINE1+TEXT_COPYRIGHT+LINE2+TEXT_OPTIONAL_RULE+LINE3;
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
	 * Test method for {@link org.spdx.licenseTemplate.SpdxLicenseTemplateHelper#templateTextToHtml(java.lang.String)}.
	 * @throws LicenseTemplateRuleException 
	 */
	@Test
	public void testTemplateTextToHtml() throws LicenseTemplateRuleException {
		String ret = SpdxLicenseTemplateHelper.templateTextToHtml(TEMPLATE_TEXT);
		assertEquals(HTML_LICENSE, ret);
	}

	/**
	 * Test method for {@link org.spdx.licenseTemplate.SpdxLicenseTemplateHelper#templateToText(java.lang.String)}.
	 * @throws LicenseTemplateRuleException 
	 */
	@Test
	public void testTemplateToText() throws LicenseTemplateRuleException {
		String ret = SpdxLicenseTemplateHelper.templateToText(TEMPLATE_TEXT);
		assertEquals(TEXT_LICENSE, ret);
	}

	/**
	 * Test method for {@link org.spdx.licenseTemplate.SpdxLicenseTemplateHelper#escapeHTML(java.lang.String)}.
	 */
	@Test
	public void testEscapeHTML() {
		String unEscaped = "<abc\nline2>";
		String escaped = "&lt;abc<br/>\nline2&gt;";
		assertEquals(escaped, SpdxLicenseTemplateHelper.escapeHTML(unEscaped));
	}

	@Test
	public void testEscapeIdString() {
		// not starting with letter
		String testId = "1test";
		assertEquals("X"+testId, SpdxLicenseTemplateHelper.escapeIdString(testId));
		// invalid character
		String invalidChar = "idWith:Invalid";
		String validChar = "idWith_Invalid";
		assertEquals(validChar, SpdxLicenseTemplateHelper.escapeIdString(invalidChar));
		String allValid = "iDwith0-.valid";
		assertEquals(allValid, SpdxLicenseTemplateHelper.escapeIdString(allValid));
	}
}
