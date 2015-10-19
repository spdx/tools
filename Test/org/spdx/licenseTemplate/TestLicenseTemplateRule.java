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
public class TestLicenseTemplateRule {

	static final String PARSEABLE_RULE = "var;original=Copyright (c) <year> <owner>\\nAll rights reserved.;match=Copyright \\(c\\) .+All rights reserved.;name=copyright;example=Copyright (C) 2013 John Doe\\nAll rights reserved.\n";
	static final String RULE_NAME = "copyright";
	static final RuleType RULE_TYPE = RuleType.VARIABLE;
	static final String RULE_ORIGINAL = "Copyright (c) <year> <owner>\nAll rights reserved.";
	static final String RULE_MATCH = "Copyright \\(c\\) .+All rights reserved.";
	static final String RULE_EXAMPLE = "Copyright (C) 2013 John Doe\nAll rights reserved.";
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

	@Test
	public void testparseLicenseTemplateRule() throws LicenseTemplateRuleException {
		LicenseTemplateRule rule = new LicenseTemplateRule("Name", RuleType.BEGIN_OPTIONAL, "original", "match", "example");
		rule.parseLicenseTemplateRule(PARSEABLE_RULE);
		assertEquals(rule.getExample(), RULE_EXAMPLE);
		assertEquals(rule.getName(), RULE_NAME);
		assertEquals(rule.getOriginal(), RULE_ORIGINAL);
		assertEquals(rule.getType(), RULE_TYPE);
		assertEquals(rule.getMatch(), RULE_MATCH);
	}

	@Test
	public void testBeginOptionalRule() throws LicenseTemplateRuleException {
		String ruleName = "optionalName";
		String ruleStr = "beginOptional;name="+ruleName;
		LicenseTemplateRule rule = new LicenseTemplateRule(ruleStr);
		assertEquals(RuleType.BEGIN_OPTIONAL, rule.getType());
		assertEquals(ruleName, rule.getName());
	}

	@Test
	public void testEndOptionalRule() throws LicenseTemplateRuleException {
		String ruleStr = "endOptional";
		LicenseTemplateRule rule = new LicenseTemplateRule(ruleStr);
		assertEquals(RuleType.END_OPTIONAL, rule.getType());
	}
}
