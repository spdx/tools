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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.licenseTemplate.LicenseTemplateRule;
import org.spdx.licenseTemplate.LicenseTemplateRuleException;
import org.spdx.licenseTemplate.SpdxLicenseTemplateHelper;
import org.spdx.rdfparser.license.LicenseParserException;
import org.spdx.rdfparser.model.UnitTestHelper;
import org.spdx.licenseTemplate.LicenseTemplateRule.RuleType;

/**
 * @author Source Auditor
 *
 */
public class TestCompareTemplateOutputHandler {
	
	static final String ADOBE_GLYPH_TEXT = "TestFiles" + File.separator + "Adobe-Glyph.txt";
	static final String ADOBE_GLYPH_TEMPLATE = "TestFiles" + File.separator + "Adobe-Glyph.template.txt";
	static final String AFL_3_TEXT = "TestFiles" + File.separator + "AFL-3.0.txt";
	static final String AFL_3_TEMPLATE = "TestFiles" + File.separator + "AFL-3.0.template.txt";
	static final String BSDNETBSD_TEXT = "TestFiles" + File.separator + "BSD-2-Clause-NetBSD.txt";
	static final String BSDNETBSD_TEMPLATE = "TestFiles" + File.separator + "BSD-2-Clause-NetBSD.template.txt";
	static final String APACHE_1_0_TEXT = "TestFiles" + File.separator + "Apache-1.0.txt";
	static final String APACHE_1_0_TEMPLATE = "TestFiles" + File.separator + "Apache-1.0.template.txt";
	static final String BSD_2_CLAUSE_TEXT = "TestFiles" + File.separator + "BSD-2-Clause.txt";
	static final String BSD_2_CLAUSE_TEMPLATE = "TestFiles" + File.separator + "BSD-2-Clause.template.txt";
	static final String BSD_4_CLAUSE_UC_TEXT  = "TestFiles" + File.separator + "BSD-4-Clause-UC.txt";
	static final String BSD_4_CLAUSE_UC_TEMPLATE  = "TestFiles" + File.separator + "BSD-4-Clause-UC.template.txt";
	static final String BSD_4_CLAUSE_TEXT  = "TestFiles" + File.separator + "BSD-4-Clause.txt";
	static final String BSD_4_CLAUSE_TEMPLATE  = "TestFiles" + File.separator + "BSD-4-Clause.template.txt";
	static final String CROSSWORD_TEXT  = "TestFiles" + File.separator + "Crossword.txt";
	static final String CROSSWORD_TEMPLATE  = "TestFiles" + File.separator + "Crossword.template.txt";
	static final String DFSL_TEXT = "TestFiles" + File.separator + "D-FSL-1.0.txt";
	static final String DFSL_TEMPLATE = "TestFiles" + File.separator + "D-FSL-1.0.template.txt";
	static final String CONDOR_1_1_TEXT = "TestFiles" + File.separator + "Condor-1.1.txt";
	static final String CONDOR_1_1_TEMPLATE = "TestFiles" + File.separator + "Condor-1.1.template.txt";
	static final String ISC_TEXT = "TestFiles" + File.separator + "ISC.txt";
	static final String ISC_TEMPLATE = "TestFiles" + File.separator + "ISC.template.txt";
	static final String MPL_1_TEXT = "TestFiles" + File.separator + "MPL-1.0.txt";
	static final String MPL_1_TEMPLATE = "TestFiles" + File.separator + "MPL-1.0.template.txt";
	static final String RPSL_1_TEXT = "TestFiles" + File.separator + "RPSL-1.0.txt";
	static final String RPSL_1_TEMPLATE = "TestFiles" + File.separator + "RPSL-1.0.template.txt";
	static final String RSCPL_TEXT = "TestFiles" + File.separator + "RSCPL.txt";
	static final String RSCPL_TEMPLATE = "TestFiles" + File.separator + "RSCPL.template.txt";	
	
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
	 * Test method for {@link org.spdx.compare.CompareTemplateOutputHandler#CompareTemplateOutputHandler(java.lang.String)}.
	 * @throws Exception 
	 */
	@Test
	public void testCompareTemplateOutputHandler() throws Exception {
		CompareTemplateOutputHandler ctoh = new CompareTemplateOutputHandler("test");
		ctoh.completeParsing();
		assertTrue(ctoh.matches());
	}

	/**
	 * Test method for {@link org.spdx.compare.CompareTemplateOutputHandler#optionalText(java.lang.String)}.
	 * @throws LicenseTemplateRuleException
	 */
	@Test
	public void testOptionalText()  throws Exception {
		String l1 = "Line 1\n";
		String l2 = "Line 2\n";
		String l3 = "Line 3\n";
		String l4 = "Line 4";
		// optional in middle
		String compareText = l1+l4;
		CompareTemplateOutputHandler ctoh = new CompareTemplateOutputHandler(compareText);
		ctoh.normalText(l1);
		ctoh.beginOptional(new LicenseTemplateRule("OptionalRule", RuleType.BEGIN_OPTIONAL));
		ctoh.optionalText(l2);
		ctoh.optionalText(l3);
		ctoh.endOptional(new LicenseTemplateRule("EndOptional",RuleType.END_OPTIONAL));
		ctoh.normalText(l4);
		ctoh.completeParsing();
		assertTrue(ctoh.matches());

		// optional at beginning
		compareText = l3+l4;
		ctoh = new CompareTemplateOutputHandler(compareText);
		ctoh.beginOptional(new LicenseTemplateRule("OptionalRule", RuleType.BEGIN_OPTIONAL));
		ctoh.optionalText(l1);
		ctoh.optionalText(l2);
		ctoh.endOptional(new LicenseTemplateRule("EndOptional",RuleType.END_OPTIONAL));
		ctoh.normalText(l3);
		ctoh.normalText(l4);
		ctoh.completeParsing();
		assertTrue(ctoh.matches());
		// optional at end
		compareText = l1+l2+l3;
		ctoh = new CompareTemplateOutputHandler(compareText);
		ctoh.normalText(l1);
		ctoh.normalText(l2);
		ctoh.normalText(l3);
		ctoh.beginOptional(new LicenseTemplateRule("OptionalRule", RuleType.BEGIN_OPTIONAL));
		ctoh.optionalText(l4);
		ctoh.endOptional(new LicenseTemplateRule("EndOptional",RuleType.END_OPTIONAL));
		ctoh.completeParsing();
		assertTrue(ctoh.matches());
		// optional code present
		compareText = l1+l2+l3+l4;
		ctoh = new CompareTemplateOutputHandler(compareText);
		ctoh.normalText(l1);
		ctoh.beginOptional(new LicenseTemplateRule("OptionalRule", RuleType.BEGIN_OPTIONAL));
		ctoh.optionalText(l2);
		ctoh.optionalText(l3);
		ctoh.endOptional(new LicenseTemplateRule("EndOptional",RuleType.END_OPTIONAL));
		ctoh.normalText(l4);
		ctoh.completeParsing();
		assertTrue(ctoh.matches());
	}

	/**
	 * Test method for {@link org.spdx.compare.CompareTemplateOutputHandler#textEquivalent(java.lang.String)}.
	 */
	@Test
	public void testTextEquivalent() throws Exception {
		String l1 = "Line 1 with // skippable ## /** stuff\n";
		String l1S = "Line 1 with skippable stuff\n";
		String l2 = "## Line 2 with replaceable analogue cancelled stuff\n";
		String l2S = "Line 2 with replaceable analogue cancelled stuff\n";
		String l2R = "## Line 2 with replaceable analog canceled stuff\n";
		String l3 = "Line\n";
		String l4 = "Line 4";
		String compareText = l1+l2+l3+l4;
		CompareTemplateOutputHandler ctoh = new CompareTemplateOutputHandler(compareText);
		assertTrue(ctoh.textEquivalent(l1));
		assertTrue(ctoh.textEquivalent(l2));
		assertTrue(ctoh.textEquivalent(l3));
		assertTrue(ctoh.textEquivalent(l4));
		// after end of compare string
		assertTrue(!ctoh.textEquivalent(l4));

		// difference in string
		ctoh = new CompareTemplateOutputHandler(compareText);
		assertTrue(!ctoh.textEquivalent(l4));

		// skippable tokens
		ctoh = new CompareTemplateOutputHandler(compareText);
		assertTrue(ctoh.textEquivalent(l1S));
		assertTrue(ctoh.textEquivalent(l2S));
		assertTrue(ctoh.textEquivalent(l3));
		assertTrue(ctoh.textEquivalent(l4));

		// equivalent tokens
		ctoh = new CompareTemplateOutputHandler(compareText);
		assertTrue(ctoh.textEquivalent(l1));
		assertTrue(ctoh.textEquivalent(l2R));
		assertTrue(ctoh.textEquivalent(l3));
		assertTrue(ctoh.textEquivalent(l4));
	}

	/**
	 * Test method for {@link org.spdx.compare.CompareTemplateOutputHandler#normalText(java.lang.String)}.
	 */
	@Test
	public void testNormalText() throws Exception {
		String line1 = "this is line one\n";
		String line2 = "this line 2 is another line\n";
		String line3 = "yet another third line\n";
		String compareText = line1 + line2 + line3;
		// success scenario
		CompareTemplateOutputHandler ctoh = new CompareTemplateOutputHandler(compareText);
		ctoh.normalText(line1);
		ctoh.normalText(line2);
		ctoh.normalText(line3);
		ctoh.completeParsing();
		ctoh.completeParsing();
		assertTrue(ctoh.matches());

		// missmatched line
		ctoh = new CompareTemplateOutputHandler(compareText);
		ctoh.normalText(line1);
		ctoh.normalText("wait - this doesn't match");
		ctoh.normalText(line3);
		ctoh.completeParsing();
		assertTrue(!ctoh.matches());

		// off the end of the compare text
		ctoh = new CompareTemplateOutputHandler(compareText);
		ctoh.normalText(line1);
		ctoh.normalText(line2);
		ctoh.normalText(line3);
		ctoh.normalText("more off the end");
		ctoh.completeParsing();
		assertTrue(!ctoh.matches());
	}

	/**
	 * Test method for {@link org.spdx.compare.CompareTemplateOutputHandler#variableRule(org.spdx.licenseTemplate.LicenseTemplateRule)}.
	 * @throws LicenseTemplateRuleException
	 */
	@Test
	public void testVariableRule()  throws Exception {
		String line1 = "this is line one\n";
		String line2 = "this line 2 is another line\n";
		String line2Match = "this\\sline\\s.+another\\sline";
		String line2MissMatch = "this\\sline\\s.+oops\\sline";
		String line2PartialMatch = "this\\sline\\s.+another";
		String line3 = "yet another third line\n";
		String compareText = line1 + line2 + line3;
		// success scenario
		CompareTemplateOutputHandler ctoh = new CompareTemplateOutputHandler(compareText);
		ctoh.normalText(line1);
		LicenseTemplateRule variableRule = new LicenseTemplateRule("Variable Rule",
								RuleType.VARIABLE, line2, line2Match, "Example: "+line2);
		ctoh.variableRule(variableRule);
		ctoh.normalText(line3);
		ctoh.completeParsing();
		assertTrue(ctoh.matches());

		// success scenario - match at end
		String partialCompareText = line1 + line2;
		ctoh = new CompareTemplateOutputHandler(partialCompareText);
		ctoh.normalText(line1);
		variableRule = new LicenseTemplateRule("Variable Rule",
								RuleType.VARIABLE, line2, line2Match, "Example: "+line2);
		ctoh.variableRule(variableRule);
		ctoh.completeParsing();
		assertTrue(ctoh.matches());

		// non-matching
		ctoh = new CompareTemplateOutputHandler(compareText);
		ctoh.normalText(line1);
		variableRule = new LicenseTemplateRule("Variable Rule",
				RuleType.VARIABLE, line2, line2MissMatch, "Example: "+line2);
		ctoh.variableRule(variableRule);
		ctoh.normalText(line3);
		ctoh.completeParsing();
		assertTrue(!ctoh.matches());

		// Extra word
		ctoh = new CompareTemplateOutputHandler(compareText);
		variableRule = new LicenseTemplateRule("Variable Rule",
				RuleType.VARIABLE, line2, line2PartialMatch, "Example: "+line2);
		ctoh.variableRule(variableRule);
		ctoh.normalText(line3);
		ctoh.completeParsing();
		assertTrue(!ctoh.matches());
	}
	
	@Test
	public void testRegressionAdobeGlyph() throws IOException, LicenseTemplateRuleException, LicenseParserException {
		String compareText = UnitTestHelper.fileToText(ADOBE_GLYPH_TEXT);
		String templateText = UnitTestHelper.fileToText(ADOBE_GLYPH_TEMPLATE);
		CompareTemplateOutputHandler templateOutputHandler = new CompareTemplateOutputHandler(compareText);
		SpdxLicenseTemplateHelper.parseTemplate(templateText, templateOutputHandler);
		assertTrue(templateOutputHandler.matches());
	}
	
	@Test
	public void testRegressionAFL3() throws IOException, LicenseTemplateRuleException, LicenseParserException {
		String compareText = UnitTestHelper.fileToText(AFL_3_TEXT);
		String templateText = UnitTestHelper.fileToText(AFL_3_TEMPLATE);
		CompareTemplateOutputHandler templateOutputHandler = new CompareTemplateOutputHandler(compareText);
		SpdxLicenseTemplateHelper.parseTemplate(templateText, templateOutputHandler);
		assertTrue(templateOutputHandler.matches());
	}
	
	@Test
	public void testRegressionBsdNetBsd() throws IOException, LicenseTemplateRuleException, LicenseParserException {
		String compareText = UnitTestHelper.fileToText(BSDNETBSD_TEXT);
		String templateText = UnitTestHelper.fileToText(BSDNETBSD_TEMPLATE);
		CompareTemplateOutputHandler templateOutputHandler = new CompareTemplateOutputHandler(compareText);
		SpdxLicenseTemplateHelper.parseTemplate(templateText, templateOutputHandler);
		if (!templateOutputHandler.matches()) {
			fail(templateOutputHandler.differenceExplanation);
		}
	}
	
	@Test
	public void testRegressionApache1() throws IOException, LicenseTemplateRuleException, LicenseParserException {
		String compareText = UnitTestHelper.fileToText(APACHE_1_0_TEXT);
		String templateText = UnitTestHelper.fileToText(APACHE_1_0_TEMPLATE);
		CompareTemplateOutputHandler templateOutputHandler = new CompareTemplateOutputHandler(compareText);
		SpdxLicenseTemplateHelper.parseTemplate(templateText, templateOutputHandler);
		if (!templateOutputHandler.matches()) {
			fail(templateOutputHandler.differenceExplanation);
		}
	}
	
	@Test
	public void testRegressionBsd2Clause() throws IOException, LicenseTemplateRuleException, LicenseParserException {
		String compareText = UnitTestHelper.fileToText(BSD_2_CLAUSE_TEXT);
		String templateText = UnitTestHelper.fileToText(BSD_2_CLAUSE_TEMPLATE);
		CompareTemplateOutputHandler templateOutputHandler = new CompareTemplateOutputHandler(compareText);
		SpdxLicenseTemplateHelper.parseTemplate(templateText, templateOutputHandler);
		if (!templateOutputHandler.matches()) {
			fail(templateOutputHandler.differenceExplanation);
		}
	}

	@Test
	public void testRegressionBsd4ClauseUC() throws IOException, LicenseTemplateRuleException, LicenseParserException {
		String compareText = UnitTestHelper.fileToText(BSD_4_CLAUSE_UC_TEXT);
		String templateText = UnitTestHelper.fileToText(BSD_4_CLAUSE_UC_TEMPLATE);
		CompareTemplateOutputHandler templateOutputHandler = new CompareTemplateOutputHandler(compareText);
		SpdxLicenseTemplateHelper.parseTemplate(templateText, templateOutputHandler);
		if (!templateOutputHandler.matches()) {
			fail(templateOutputHandler.differenceExplanation);
		}
	}
	
	@Test
	public void testRegressionBsd4Clause() throws IOException, LicenseTemplateRuleException, LicenseParserException {
		String compareText = UnitTestHelper.fileToText(BSD_4_CLAUSE_TEXT);
		String templateText = UnitTestHelper.fileToText(BSD_4_CLAUSE_TEMPLATE);
		CompareTemplateOutputHandler templateOutputHandler = new CompareTemplateOutputHandler(compareText);
		SpdxLicenseTemplateHelper.parseTemplate(templateText, templateOutputHandler);
		if (!templateOutputHandler.matches()) {
			fail(templateOutputHandler.differenceExplanation);
		}
	}
	
	@Test
	public void testRegressionCrossword() throws IOException, LicenseTemplateRuleException, LicenseParserException {
		String compareText = UnitTestHelper.fileToText(CROSSWORD_TEXT);
		String templateText = UnitTestHelper.fileToText(CROSSWORD_TEMPLATE);
		CompareTemplateOutputHandler templateOutputHandler = new CompareTemplateOutputHandler(compareText);
		SpdxLicenseTemplateHelper.parseTemplate(templateText, templateOutputHandler);
		if (!templateOutputHandler.matches()) {
			fail(templateOutputHandler.differenceExplanation);
		}
	}
	
	@Test
	public void testRegressionDFSL() throws IOException, LicenseTemplateRuleException, LicenseParserException {
		String compareText = UnitTestHelper.fileToText(DFSL_TEXT);
		String templateText = UnitTestHelper.fileToText(DFSL_TEMPLATE);
		CompareTemplateOutputHandler templateOutputHandler = new CompareTemplateOutputHandler(compareText);
		SpdxLicenseTemplateHelper.parseTemplate(templateText, templateOutputHandler);
		if (!templateOutputHandler.matches()) {
			fail(templateOutputHandler.differenceExplanation);
		}
	}
	
	@Test
	public void testRegressionCondor11() throws IOException, LicenseTemplateRuleException, LicenseParserException {
		String compareText = UnitTestHelper.fileToText(CONDOR_1_1_TEXT);
		String templateText = UnitTestHelper.fileToText(CONDOR_1_1_TEMPLATE);
		CompareTemplateOutputHandler templateOutputHandler = new CompareTemplateOutputHandler(compareText);
		SpdxLicenseTemplateHelper.parseTemplate(templateText, templateOutputHandler);
		if (!templateOutputHandler.matches()) {
			fail(templateOutputHandler.differenceExplanation);
		}
	}
	
	@Test
	public void testRegressionIsc() throws IOException, LicenseTemplateRuleException, LicenseParserException {
		String compareText = UnitTestHelper.fileToText(ISC_TEXT);
		String templateText = UnitTestHelper.fileToText(ISC_TEMPLATE);
		CompareTemplateOutputHandler templateOutputHandler = new CompareTemplateOutputHandler(compareText);
		SpdxLicenseTemplateHelper.parseTemplate(templateText, templateOutputHandler);
		if (!templateOutputHandler.matches()) {
			fail(templateOutputHandler.differenceExplanation);
		}
	}
	
	@Test
	public void testRegressionMPL11() throws IOException, LicenseTemplateRuleException, LicenseParserException {
		String compareText = UnitTestHelper.fileToText(MPL_1_TEXT);
		String templateText = UnitTestHelper.fileToText(MPL_1_TEMPLATE);
		CompareTemplateOutputHandler templateOutputHandler = new CompareTemplateOutputHandler(compareText);
		SpdxLicenseTemplateHelper.parseTemplate(templateText, templateOutputHandler);
		if (!templateOutputHandler.matches()) {
			fail(templateOutputHandler.differenceExplanation);
		}
	}
	
	@Test
	public void testRegressionRPSL1() throws IOException, LicenseTemplateRuleException, LicenseParserException {
		String compareText = UnitTestHelper.fileToText(RPSL_1_TEXT);
		String templateText = UnitTestHelper.fileToText(RPSL_1_TEMPLATE);
		CompareTemplateOutputHandler templateOutputHandler = new CompareTemplateOutputHandler(compareText);
		SpdxLicenseTemplateHelper.parseTemplate(templateText, templateOutputHandler);
		if (!templateOutputHandler.matches()) {
			fail(templateOutputHandler.differenceExplanation);
		}
	}
	
	@Test
	public void testRegressionRSCPL() throws IOException, LicenseTemplateRuleException, LicenseParserException {
		String compareText = UnitTestHelper.fileToText(RSCPL_TEXT);
		String templateText = UnitTestHelper.fileToText(RSCPL_TEMPLATE);
		CompareTemplateOutputHandler templateOutputHandler = new CompareTemplateOutputHandler(compareText);
		SpdxLicenseTemplateHelper.parseTemplate(templateText, templateOutputHandler);
		if (!templateOutputHandler.matches()) {
			fail(templateOutputHandler.differenceExplanation);
		}
	}
}
