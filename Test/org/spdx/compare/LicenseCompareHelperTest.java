/**
 * Copyright (c) 2013 Source Auditor Inc.
 * Copyright (c) 2013 Black Duck Software Inc.
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

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXConjunctiveLicenseSet;
import org.spdx.rdfparser.SPDXDisjunctiveLicenseSet;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXNonStandardLicense;
import org.spdx.rdfparser.SPDXNoneLicense;
import org.spdx.rdfparser.SPDXStandardLicense;
import org.spdx.rdfparser.SpdxNoAssertionLicense;

/**
 * @author Gary O'Neall
 *
 */
public class LicenseCompareHelperTest {

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
	 * Test method for {@link org.spdx.compare.LicenseCompareHelper#licensesMatch(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testLicensesMatch() {
		// equal strings
		String testA = "Now is the time  for all . good. men/to \\come to the aid of their country.";
		boolean result = LicenseCompareHelper.licensesMatch(testA, testA);
		assertTrue(result);
		// b is longer
		String testB = testA + " A bit longer";
		result = LicenseCompareHelper.licensesMatch(testA, testB);
		assertFalse(result);
		// first parameter is longer
		result = LicenseCompareHelper.licensesMatch(testB, testA);
		assertFalse(result);
		// white space doesn't matter
		String testPart1 = "Now is the time ";
		String testPart2 = " for all good men";
		String whiteSpace = " \t\n\r";
		testA = testPart1 + testPart2;
		testB = testPart1 + whiteSpace + testPart2;
		result = LicenseCompareHelper.licensesMatch(testA, testB);
		assertTrue(result);
		// trailing white space
		testB = testA + whiteSpace;
		result = LicenseCompareHelper.licensesMatch(testA, testB);
		assertTrue(result);
		result = LicenseCompareHelper.licensesMatch(testB, testA);
		assertTrue(result);
		// preceeding white space
		testB = whiteSpace + testA;
		result = LicenseCompareHelper.licensesMatch(testA, testB);
		assertTrue(result);
		result = LicenseCompareHelper.licensesMatch(testB, testA);
		assertTrue(result);
		// case shouldnt matter
		result = LicenseCompareHelper.licensesMatch(testA, testA.toUpperCase());
		assertTrue(result);
		// punctuation should matter
		testA = testPart1 + testPart2;
		String punctuation = ",";
		testB = testPart1 + punctuation + testPart2;
		result = LicenseCompareHelper.licensesMatch(testA, testB);
		assertFalse(result);
		// dash variations
		testA = testPart1 + "-" + testPart2;
		testB = testPart1 + "\u2013" + testPart2;
		result = LicenseCompareHelper.licensesMatch(testA, testB);
		assertTrue(result);
		testB = testPart1 + "\u2014" + testPart2;
		result = LicenseCompareHelper.licensesMatch(testA, testB);
		assertTrue(result);
		testB = testPart1 + "\u2015" + testPart2;
		result = LicenseCompareHelper.licensesMatch(testA, testB);
		assertTrue(result);
	}
	
	@Test
	public void testLicenseMatchCodeComments() {
		String part1 = " now is the time for all good men\n";
		String part2 = "\tto come to the aid ";
		// c style line comment
		String cCommentLine = "//";
		String testA = part1 + part2;
		String testB = cCommentLine + part1 + cCommentLine + part2 + "\n"+ cCommentLine;
		boolean result = LicenseCompareHelper.licensesMatch(testA, testB);
		assertTrue(result);
		result = LicenseCompareHelper.licensesMatch(testB, testA);
		assertTrue(result);
		// c style multi line
		String startCMulti = "/*";
		String endCMulti = "*/";
		testB = startCMulti + part1 + part2 + endCMulti;
		result = LicenseCompareHelper.licensesMatch(testA, testB);
		assertTrue(result);
		result = LicenseCompareHelper.licensesMatch(testB, testA);
		assertTrue(result);
		// javaDocs comments
		String startJavaDocs = "/**";
		testB = startJavaDocs + part1 + part2 + endCMulti;
		result = LicenseCompareHelper.licensesMatch(testA, testB);
		assertTrue(result);
		result = LicenseCompareHelper.licensesMatch(testB, testA);
		assertTrue(result);
		// script line comment
		String scriptLineComment = "#";
		testB = scriptLineComment + part1 + scriptLineComment + part2 + "\n"+ scriptLineComment;
		result = LicenseCompareHelper.licensesMatch(testA, testB);
		assertTrue(result);
		result = LicenseCompareHelper.licensesMatch(testB, testA);
		assertTrue(result);
	}
	
	@Test
	public void testLicenseMatchEquivWords() {
		// per cent -> percent
		String part1 = "now is the time for ";
		String testA = part1 + "per cent";
		String testB = part1 + "percent";
		boolean result = LicenseCompareHelper.licensesMatch(testA, testB);
		assertTrue(result);
		// copyright holder-> copyright owner
		testA = "Copyright holder "+part1;
		testB = "copyright Owner "+ part1;
		result = LicenseCompareHelper.licensesMatch(testA, testB);
		assertTrue(result);
		// "license","licence"
		testA = part1 + " license " + part1;
		testB = part1 + " licence " + part1;
		result = LicenseCompareHelper.licensesMatch(testA, testB);
		assertTrue(result);
	}

	@Test
	public void testLicenseEqualsStdLicense() throws InvalidSPDXAnalysisException, SpdxCompareException {
		HashMap<String, String> xlation = new HashMap<String, String>();
		String licName = "name";
		String licId = "ID1";
		String licText = "Text";
		String[] sourceUrls = new String[] {"http://www.sourceauditor.com/licenses"};
		String notes = "Notes";
		String stdLicNotice = "Notice";
		String template = "Template";
		boolean osiApproved = false;
		SPDXStandardLicense lic1 = 
			new SPDXStandardLicense(licName, licId, licText, 
					sourceUrls, notes, stdLicNotice, template, osiApproved);
		SPDXStandardLicense lic2 = 
			new SPDXStandardLicense(licName, licId, licText, 
					sourceUrls, notes, stdLicNotice, template, osiApproved);
		assertTrue(LicenseCompareHelper.isLicenseEqual(lic1, lic2, xlation));
		
		// try just changing the text - should still equal since the ID's are equal
		String text2 = "text2";
		SPDXStandardLicense lic3 = 
			new SPDXStandardLicense(licName, licId, text2, 
					sourceUrls, notes, stdLicNotice, template, osiApproved);
		assertTrue(LicenseCompareHelper.isLicenseEqual(lic1, lic3, xlation));
		// now try a different ID
		String licId2 = "ID2";
		SPDXStandardLicense lic4 = 
			new SPDXStandardLicense(licName, licId2, licText, 
					sourceUrls, notes, stdLicNotice, template, osiApproved);
		assertFalse(LicenseCompareHelper.isLicenseEqual(lic1, lic4, xlation));
	}
	
	
	@Test
	public void testLicenseEqualsNonStdLicense() throws InvalidSPDXAnalysisException, SpdxCompareException {
		HashMap<String, String> xlation = new HashMap<String, String>();
		String licId = "ID1";
		String licText = "Text";

		// same license ID's
		SPDXNonStandardLicense lic1 = 
			new SPDXNonStandardLicense(licId, licText);
		SPDXNonStandardLicense lic2 = 
			new SPDXNonStandardLicense(licId, licText);
		xlation.put(licId, licId);
		assertTrue(LicenseCompareHelper.isLicenseEqual(lic1, lic2, xlation));
		// different license ID, same license
		xlation.clear();
		String licId2 = "id2";
		lic2 = 
			new SPDXNonStandardLicense(licId2, licText);
		xlation.put(licId, licId2);
		assertTrue(LicenseCompareHelper.isLicenseEqual(lic1, lic2, xlation));
		// different license ID, different license
		String licId3 = "id3";
		lic2 = 
			new SPDXNonStandardLicense(licId3, licId2);
		assertFalse(LicenseCompareHelper.isLicenseEqual(lic1, lic2, xlation));
	}
	
	@Test
	public void testLicenseEqualsConjunctiveLicense() throws InvalidSPDXAnalysisException, SpdxCompareException {
		String licText = "Text";
		String licId1 = "id1";
		String licId2 = "id2";
		String licId3 = "id3";
		String licId4 = "id4";
		String licId5 = "id5";
		String licId6 = "id6";
		HashMap<String, String> xlation = new HashMap<String, String>();
		SPDXNonStandardLicense lic1 = new SPDXNonStandardLicense(licId1, licText);
		SPDXNonStandardLicense lic2 = new SPDXNonStandardLicense(licId2, licText);
		SPDXNonStandardLicense lic3 = new SPDXNonStandardLicense(licId3, licText);
		SPDXNonStandardLicense lic4 = new SPDXNonStandardLicense(licId4, licText);
		SPDXNonStandardLicense lic5 = new SPDXNonStandardLicense(licId5, licText);
		SPDXNonStandardLicense lic6 = new SPDXNonStandardLicense(licId6, licText);
		xlation.put(licId1, licId4);
		xlation.put(licId2, licId5);
		xlation.put(licId3, licId6);
		SPDXLicenseInfo[] set1 = new SPDXLicenseInfo[] {
				lic1, lic2, lic3
		};
		SPDXLicenseInfo[] set2 = new SPDXLicenseInfo[] {
				lic4, lic5, lic6
		};
		SPDXConjunctiveLicenseSet conj1 = new SPDXConjunctiveLicenseSet(set1);
		SPDXConjunctiveLicenseSet conj2 = new SPDXConjunctiveLicenseSet(set2);
		
		assertTrue(LicenseCompareHelper.isLicenseEqual(conj1, conj2, xlation));
		
		String licId7 = "id7";
		SPDXNonStandardLicense lic7 = new SPDXNonStandardLicense(licId7, licText);
		SPDXLicenseInfo[] set3 = new SPDXLicenseInfo[] {
				lic4, lic5, lic7
		};
		SPDXConjunctiveLicenseSet conj3 = new SPDXConjunctiveLicenseSet(set3);
		assertFalse(LicenseCompareHelper.isLicenseEqual(conj1, conj3, xlation));		
	}		
	
	@Test
	public void testLicenseEqualsConjunctiveLicenseDifferentOrder() throws InvalidSPDXAnalysisException, SpdxCompareException {
		String licText = "Text";
		String licId1 = "id1";
		String licId2 = "id2";
		String licId3 = "id3";
		String licId4 = "id4";
		String licId5 = "id5";
		String licId6 = "id6";
		HashMap<String, String> xlation = new HashMap<String, String>();
		SPDXNonStandardLicense lic1 = new SPDXNonStandardLicense(licId1, licText);
		SPDXNonStandardLicense lic2 = new SPDXNonStandardLicense(licId2, licText);
		SPDXNonStandardLicense lic3 = new SPDXNonStandardLicense(licId3, licText);
		SPDXNonStandardLicense lic4 = new SPDXNonStandardLicense(licId4, licText);
		SPDXNonStandardLicense lic5 = new SPDXNonStandardLicense(licId5, licText);
		SPDXNonStandardLicense lic6 = new SPDXNonStandardLicense(licId6, licText);
		xlation.put(licId1, licId4);
		xlation.put(licId2, licId5);
		xlation.put(licId3, licId6);
		SPDXLicenseInfo[] set1 = new SPDXLicenseInfo[] {
				lic1, lic2, lic3
		};
		SPDXLicenseInfo[] set2 = new SPDXLicenseInfo[] {
				lic4, lic6, lic5
		};
		SPDXConjunctiveLicenseSet conj1 = new SPDXConjunctiveLicenseSet(set1);
		SPDXConjunctiveLicenseSet conj2 = new SPDXConjunctiveLicenseSet(set2);
		
		assertTrue(LicenseCompareHelper.isLicenseEqual(conj1, conj2, xlation));
	
	}	
	
	@Test
	public void testLicenseEqualsDisjunctiveLicense() throws InvalidSPDXAnalysisException, SpdxCompareException {
		String licText = "Text";
		String licId1 = "id1";
		String licId2 = "id2";
		String licId3 = "id3";
		String licId4 = "id4";
		String licId5 = "id5";
		String licId6 = "id6";
		HashMap<String, String> xlation = new HashMap<String, String>();
		SPDXNonStandardLicense lic1 = new SPDXNonStandardLicense(licId1, licText);
		SPDXNonStandardLicense lic2 = new SPDXNonStandardLicense(licId2, licText);
		SPDXNonStandardLicense lic3 = new SPDXNonStandardLicense(licId3, licText);
		SPDXNonStandardLicense lic4 = new SPDXNonStandardLicense(licId4, licText);
		SPDXNonStandardLicense lic5 = new SPDXNonStandardLicense(licId5, licText);
		SPDXNonStandardLicense lic6 = new SPDXNonStandardLicense(licId6, licText);
		xlation.put(licId1, licId4);
		xlation.put(licId2, licId5);
		xlation.put(licId3, licId6);
		SPDXLicenseInfo[] set1 = new SPDXLicenseInfo[] {
				lic1, lic2, lic3
		};
		SPDXLicenseInfo[] set2 = new SPDXLicenseInfo[] {
				lic4, lic5, lic6
		};
		SPDXDisjunctiveLicenseSet conj1 = new SPDXDisjunctiveLicenseSet(set1);
		SPDXDisjunctiveLicenseSet conj2 = new SPDXDisjunctiveLicenseSet(set2);
		
		assertTrue(LicenseCompareHelper.isLicenseEqual(conj1, conj2, xlation));
		
		String licId7 = "id7";
		SPDXNonStandardLicense lic7 = new SPDXNonStandardLicense(licId7, licText);
		SPDXLicenseInfo[] set3 = new SPDXLicenseInfo[] {
				lic4, lic5, lic7
		};
		SPDXDisjunctiveLicenseSet conj3 = new SPDXDisjunctiveLicenseSet(set3);
		assertFalse(LicenseCompareHelper.isLicenseEqual(conj1, conj3, xlation));
	}	
	
	@Test
	public void testLicenseEqualsComplexLicense() throws InvalidSPDXAnalysisException, SpdxCompareException {
		String licText = "Text";
		String licId1 = "id1";
		String licId2 = "id2";
		String licId3 = "id3";
		String licId4 = "id4";
		String licId5 = "id5";
		String licId6 = "id6";
		HashMap<String, String> xlation = new HashMap<String, String>();
		SPDXNonStandardLicense lic1 = new SPDXNonStandardLicense(licId1, licText);
		SPDXNonStandardLicense lic2 = new SPDXNonStandardLicense(licId2, licText);
		SPDXNonStandardLicense lic3 = new SPDXNonStandardLicense(licId3, licText);
		SPDXNonStandardLicense lic4 = new SPDXNonStandardLicense(licId4, licText);
		SPDXNonStandardLicense lic5 = new SPDXNonStandardLicense(licId5, licText);
		SPDXNonStandardLicense lic6 = new SPDXNonStandardLicense(licId6, licText);
		xlation.put(licId1, licId4);
		xlation.put(licId2, licId5);
		xlation.put(licId3, licId6);
		SPDXLicenseInfo[] set1 = new SPDXLicenseInfo[] {
				lic1, lic2
		};
		SPDXLicenseInfo[] set2 = new SPDXLicenseInfo[] {
				lic4, lic5
		};
		SPDXDisjunctiveLicenseSet conj1 = new SPDXDisjunctiveLicenseSet(set1);
		SPDXDisjunctiveLicenseSet conj2 = new SPDXDisjunctiveLicenseSet(set2);
		
		SPDXLicenseInfo[] set3 = new SPDXLicenseInfo[] {
				conj1, lic3
		};
		SPDXLicenseInfo[] set4 = new SPDXLicenseInfo[] {
				lic6, conj2
		};
		SPDXConjunctiveLicenseSet conj3 = new SPDXConjunctiveLicenseSet(set3);
		SPDXConjunctiveLicenseSet conj4 = new SPDXConjunctiveLicenseSet(set4);
		
		assertTrue(LicenseCompareHelper.isLicenseEqual(conj3, conj4, xlation));
	}	
	
	@Test
	public void testLicenseEqualsNoAsserLicense() throws InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxNoAssertionLicense lic1 = new SpdxNoAssertionLicense();
		SpdxNoAssertionLicense lic2 = new SpdxNoAssertionLicense();
		SPDXNoneLicense lic3 = new SPDXNoneLicense();
		SPDXNoneLicense lic4 = new SPDXNoneLicense();
		HashMap<String, String> xlationMap = new HashMap<String, String>();
		assertTrue(LicenseCompareHelper.isLicenseEqual(lic1, lic2, xlationMap));
		assertFalse(LicenseCompareHelper.isLicenseEqual(lic1, lic3, xlationMap));
	}	
	
	@Test
	public void testLicenseEqualsNoneLicense() throws InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxNoAssertionLicense lic1 = new SpdxNoAssertionLicense();
		SpdxNoAssertionLicense lic2 = new SpdxNoAssertionLicense();
		SPDXNoneLicense lic3 = new SPDXNoneLicense();
		SPDXNoneLicense lic4 = new SPDXNoneLicense();
		HashMap<String, String> xlationMap = new HashMap<String, String>();
		assertTrue(LicenseCompareHelper.isLicenseEqual(lic3, lic4, xlationMap));
		assertFalse(LicenseCompareHelper.isLicenseEqual(lic4, lic2, xlationMap));
	}	
}
