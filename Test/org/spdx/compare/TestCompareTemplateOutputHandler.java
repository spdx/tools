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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.licenseTemplate.LicenseTemplateRule;
import org.spdx.licenseTemplate.LicenseTemplateRuleException;
import org.spdx.licenseTemplate.LicenseTemplateRule.RuleType;

/**
 * @author Source Auditor
 *
 */
public class TestCompareTemplateOutputHandler {

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
	 */
	@Test
	public void testCompareTemplateOutputHandler() {
		CompareTemplateOutputHandler ctoh = new CompareTemplateOutputHandler("test");
		assertTrue(ctoh.matches());
	}

	/**
	 * Test method for {@link org.spdx.compare.CompareTemplateOutputHandler#optionalText(java.lang.String)}.
	 * @throws LicenseTemplateRuleException
	 */
	@Test
	public void testOptionalText() throws LicenseTemplateRuleException {
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
		assertTrue(ctoh.matches());
	}

	/**
	 * Test method for {@link org.spdx.compare.CompareTemplateOutputHandler#textEquivalent(java.lang.String)}.
	 */
	@Test
	public void testTextEquivalent() {
		String l1 = "Line 1 with // skippable ## /** stuff\n";
		String l1S = "Line 1 with skippable stuff\n";
		String l2 = "## Line 2 with replaceable analogue cancelled stuff\n";
		String l2S = "Line 2 with replaceable analogue cancelled stuff\n";
		String l2R = "## Line 2 with replaceable analog canceled stuff\n";
		String l3 = "Line\n";
		String l4 = "Line 4";
		String compareText = l1+l2+l3+l4;
		CompareTemplateOutputHandler ctoh = new CompareTemplateOutputHandler(compareText);
		assertTrue(ctoh.matches());
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
		assertTrue(ctoh.matches());
		assertTrue(ctoh.textEquivalent(l1S));
		assertTrue(ctoh.textEquivalent(l2S));
		assertTrue(ctoh.textEquivalent(l3));
		assertTrue(ctoh.textEquivalent(l4));

		// equivalent tokens
		ctoh = new CompareTemplateOutputHandler(compareText);
		assertTrue(ctoh.matches());
		assertTrue(ctoh.textEquivalent(l1));
		assertTrue(ctoh.textEquivalent(l2R));
		assertTrue(ctoh.textEquivalent(l3));
		assertTrue(ctoh.textEquivalent(l4));
	}

	/**
	 * Test method for {@link org.spdx.compare.CompareTemplateOutputHandler#normalText(java.lang.String)}.
	 */
	@Test
	public void testNormalText() {
		String line1 = "this is line one\n";
		String line2 = "this line 2 is another line\n";
		String line3 = "yet another third line\n";
		String compareText = line1 + line2 + line3;
		// success scenario
		CompareTemplateOutputHandler ctoh = new CompareTemplateOutputHandler(compareText);
		assertTrue(ctoh.matches());
		ctoh.normalText(line1);
		assertTrue(ctoh.matches());
		ctoh.normalText(line2);
		assertTrue(ctoh.matches());
		ctoh.normalText(line3);
		assertTrue(ctoh.matches());

		// missmatched line
		ctoh = new CompareTemplateOutputHandler(compareText);
		assertTrue(ctoh.matches());
		ctoh.normalText(line1);
		assertTrue(ctoh.matches());
		ctoh.normalText("wait - this doesn't match");
		assertTrue(!ctoh.matches());
		ctoh.normalText(line3);
		assertTrue(!ctoh.matches());

		// off the end of the compare text
		ctoh = new CompareTemplateOutputHandler(compareText);
		assertTrue(ctoh.matches());
		ctoh.normalText(line1);
		assertTrue(ctoh.matches());
		ctoh.normalText(line2);
		assertTrue(ctoh.matches());
		ctoh.normalText(line3);
		assertTrue(ctoh.matches());
		ctoh.normalText("more off the end");
		assertTrue(!ctoh.matches());
	}

	/**
	 * Test method for {@link org.spdx.compare.CompareTemplateOutputHandler#variableRule(org.spdx.licenseTemplate.LicenseTemplateRule)}.
	 * @throws LicenseTemplateRuleException
	 */
	@Test
	public void testVariableRule() throws LicenseTemplateRuleException {
		String line1 = "this is line one\n";
		String line2 = "this line 2 is another line\n";
		String line2Match = "this\\sline\\s.+another\\sline";
		String line2MissMatch = "this\\sline\\s.+oops\\sline";
		String line2PartialMatch = "this\\sline\\s.+another";
		String line3 = "yet another third line\n";
		String compareText = line1 + line2 + line3;
		// success scenario
		CompareTemplateOutputHandler ctoh = new CompareTemplateOutputHandler(compareText);
		assertTrue(ctoh.matches());
		ctoh.normalText(line1);
		assertTrue(ctoh.matches());
		LicenseTemplateRule variableRule = new LicenseTemplateRule("Variable Rule",
								RuleType.VARIABLE, line2, line2Match, "Example: "+line2);
		ctoh.variableRule(variableRule);
		assertTrue(ctoh.matches());
		ctoh.normalText(line3);
		assertTrue(ctoh.matches());

		// success scenario - match at end
		String partialCompareText = line1 + line2;
		ctoh = new CompareTemplateOutputHandler(partialCompareText);
		assertTrue(ctoh.matches());
		ctoh.normalText(line1);
		assertTrue(ctoh.matches());
		variableRule = new LicenseTemplateRule("Variable Rule",
								RuleType.VARIABLE, line2, line2Match, "Example: "+line2);
		ctoh.variableRule(variableRule);
		assertTrue(ctoh.matches());

		// non-matching
		ctoh = new CompareTemplateOutputHandler(compareText);
		assertTrue(ctoh.matches());
		ctoh.normalText(line1);
		assertTrue(ctoh.matches());
		variableRule = new LicenseTemplateRule("Variable Rule",
				RuleType.VARIABLE, line2, line2MissMatch, "Example: "+line2);
		ctoh.variableRule(variableRule);
		assertTrue(!ctoh.matches());
		ctoh.normalText(line3);
		assertTrue(!ctoh.matches());

		// Extra word
		ctoh = new CompareTemplateOutputHandler(compareText);
		assertTrue(ctoh.matches());
		ctoh.normalText(line1);
		assertTrue(ctoh.matches());
		variableRule = new LicenseTemplateRule("Variable Rule",
				RuleType.VARIABLE, line2, line2PartialMatch, "Example: "+line2);
		ctoh.variableRule(variableRule);
		assertTrue(ctoh.matches());
		ctoh.normalText(line3);
		assertTrue(!ctoh.matches());
	}
}
