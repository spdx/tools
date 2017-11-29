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
import org.spdx.rdfparser.license.LicenseParserException;

/**
 * @author Gary O'Neall
 *
 */
public class TestSpdxLicenseTemplateHelper {

	String optionalTextString;
	String normalTextString;
	LicenseTemplateRule variableRule;
	LicenseTemplateRule optionalRule;
	LicenseTemplateRule endOptionalRule;

	public class TestLicenseTemplateOutputHandler implements ILicenseTemplateOutputHandler {
		
		int optionalNestLevel = 0;

		public TestLicenseTemplateOutputHandler() {
			optionalTextString = null;
			normalTextString = null;
			variableRule = null;
			optionalRule = null;
			endOptionalRule = null;
		}

		/* (non-Javadoc)
		 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#text(java.lang.String)
		 */
		@Override
		public void text(String text) {
			if (optionalNestLevel > 0) {
				optionalTextString = text;
			} else {
				normalTextString = text;
			}
		}

		/* (non-Javadoc)
		 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#variableRule(org.spdx.licenseTemplate.LicenseTemplateRule)
		 */
		@Override
		public void variableRule(LicenseTemplateRule rule) {
			variableRule = rule;
		}

		/* (non-Javadoc)
		 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#beginOptional(org.spdx.licenseTemplate.LicenseTemplateRule)
		 */
		@Override
		public void beginOptional(LicenseTemplateRule rule) {
			optionalRule = rule;
			this.optionalNestLevel++;
		}

		/* (non-Javadoc)
		 * @see org.spdx.licenseTemplate.ILicenseTemplateOutputHandler#endOptional(org.spdx.licenseTemplate.LicenseTemplateRule)
		 */
		@Override
		public void endOptional(LicenseTemplateRule rule) {
			endOptionalRule = rule;
			this.optionalNestLevel--;
		}

		@Override
		public void completeParsing() {
			// // Nothing needs to be done - everything is processed inline
			
		}

	}

	static final String LINE1 = "This is the start of the license.\n";
	static final String REQUIRED_RULE="<<var;original=Copyright (c) <year> <owner>\\nAll rights reserved.;match=Copyright \\(c\\) .+All rights reserved.;name=copyright;example=Copyright (C) 2013 John Doe\\nAll rights reserved.>>";
	static final String LINE2 = "\nRedistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met";
	static final String OPTIONAL_RULE="<<beginOptional;name=optional>>Original Text<<endOptional>>";
	static final String LINE3="\nLast line of the license";
	static final String TEMPLATE_TEXT = LINE1+REQUIRED_RULE+LINE2+OPTIONAL_RULE+LINE3;

	static final String HTML_COPYRIGHT="\n<div id=\"copyright\" class=\"replacable-license-text\"  style=\"display: inline\">Copyright (c) &lt;year&gt; &lt;owner&gt;<br/>\nAll rights reserved.</div>\n";
	static final String HTML_OPTIONAL_RULE="\n<div id=\"optional\" class=\"optional-license-text\"  style=\"display: inline\">\nOriginal Text</div>\n";
	static final String HTML_LICENSE = LINE1.replace("\n", "<br/>\n")+
			HTML_COPYRIGHT+
			LINE2.replace("\n", "<br/>\n")+
			HTML_OPTIONAL_RULE+
			LINE3.replace("\n", "<br/>\n");

	static final String TEXT_COPYRIGHT = "Copyright (c) <year> <owner>\nAll rights reserved.";
	static final String TEXT_OPTIONAL_RULE = "Original Text";
	static final String TEXT_LICENSE = LINE1+TEXT_COPYRIGHT+LINE2+TEXT_OPTIONAL_RULE+LINE3;
	private static final Object PARSE_OPTIONAL_RULE_NAME = "OptionalRuleName";
	private static final Object PARSE_VARIABLE_RULE_NAME = "VariableRuleName";
	private static final Object PARSE_OPTIONAL_TEXT = "Optional Text";
	private static final Object PARSE_NORMAL_TEXT = "Normal Text";
	private static final String PARSE_TEXT_STRING = PARSE_NORMAL_TEXT + "<<var;name="+PARSE_VARIABLE_RULE_NAME+
								";original=original;match=.+this>>"+
								"<<beginOptional;name="+PARSE_OPTIONAL_RULE_NAME+
								">>"+PARSE_OPTIONAL_TEXT+"<<endOptional>>";
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
	 * @throws LicenseParserException 
	 */
	@Test
	public void testTemplateToText() throws LicenseTemplateRuleException, LicenseParserException {
		String ret = SpdxLicenseTemplateHelper.templateToText(TEMPLATE_TEXT);
		assertEquals(TEXT_LICENSE, ret);
	}

	/**
	 * Test method for {@link org.spdx.licenseTemplate.SpdxLicenseTemplateHelper#formatEscapeHTML(java.lang.String)}.
	 */
	@Test
	public void testEscapeHTML() {
		String unEscaped = "<abc\nline2>";
		String escaped = "&lt;abc<br/>\nline2&gt;";
		assertEquals(escaped, SpdxLicenseTemplateHelper.formatEscapeHTML(unEscaped));
	}

	@Test
	public void testParseTemplate() throws LicenseTemplateRuleException, LicenseParserException {
		String noText = "";
		TestLicenseTemplateOutputHandler handler = new TestLicenseTemplateOutputHandler();
		SpdxLicenseTemplateHelper.parseTemplate(noText, handler);
		assertEquals(null, optionalTextString);
		assertEquals(null, normalTextString);
		assertEquals(null, variableRule);
		assertEquals(null, optionalRule);
		assertEquals(null, endOptionalRule);

		handler = new TestLicenseTemplateOutputHandler();
		SpdxLicenseTemplateHelper.parseTemplate(PARSE_TEXT_STRING, handler);
		assertEquals(PARSE_NORMAL_TEXT, normalTextString);
		assertEquals(PARSE_OPTIONAL_TEXT, optionalTextString);
		assertEquals(PARSE_VARIABLE_RULE_NAME, variableRule.getName());
		assertEquals(RuleType.VARIABLE, variableRule.getType());
		assertEquals(PARSE_OPTIONAL_RULE_NAME, optionalRule.getName());
		assertEquals(RuleType.BEGIN_OPTIONAL, optionalRule.getType());
		assertEquals(RuleType.END_OPTIONAL, endOptionalRule.getType());
	}

	@Test
	public void testAddHtmlFormatting() {
		String noParagraphs = "lines1\nline2\nline3";
		String noParagraphsTagged = "lines1<br/>\nline2<br/>\nline3";
		assertEquals(noParagraphsTagged, SpdxLicenseTemplateHelper.addHtmlFormatting(noParagraphs));
		String empty = "";
		assertEquals(empty, SpdxLicenseTemplateHelper.addHtmlFormatting(empty));
		String oneLine = "one line";
		assertEquals(oneLine, SpdxLicenseTemplateHelper.addHtmlFormatting(oneLine));
		String paragraphs = "paragraph1\n\nparagraph2\n\nparagraph3";
		String paragraphsTagged = "paragraph1\n<p>paragraph2</p>\n<p>paragraph3</p>";
		assertEquals(paragraphsTagged, SpdxLicenseTemplateHelper.addHtmlFormatting(paragraphs));
		String tabbed = "paragraph1\n\n     tabbed paragraph\n\nnormal paragraph";
		String tabbedTagged = "paragraph1\n<p style=\"margin-left: 20px;\">     tabbed paragraph</p>\n<p>normal paragraph</p>";
		assertEquals(tabbedTagged, SpdxLicenseTemplateHelper.addHtmlFormatting(tabbed));
		String quadTabbed = "paragraph1\n\n                    tabbed paragraph\n\nnormal paragraph";
		String quadTabbedTagged = "paragraph1\n<p style=\"margin-left: 70px;\">                    tabbed paragraph</p>\n<p>normal paragraph</p>";
		assertEquals(quadTabbedTagged, SpdxLicenseTemplateHelper.addHtmlFormatting(quadTabbed));
	}
}
