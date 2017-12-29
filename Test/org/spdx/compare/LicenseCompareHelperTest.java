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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.compare.CompareTemplateOutputHandler.DifferenceDescription;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ConjunctiveLicenseSet;
import org.spdx.rdfparser.license.DisjunctiveLicenseSet;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.license.SpdxNoAssertionLicense;
import org.spdx.rdfparser.license.SpdxNoneLicense;
import org.spdx.rdfparser.model.UnitTestHelper;

import com.google.common.collect.Maps;

/**
 * @author Gary O'Neall
 *
 */
public class LicenseCompareHelperTest {
	
	static final String GPL_2_TEXT = "TestFiles" + File.separator + "GPL-2.0.txt";

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
	 * Test method for {@link org.spdx.compare.LicenseCompareHelper#isLicenseTextEquivalent(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testLicensesMatch() {
		// equal strings
		String testA = "Now is the time  for all . good. men/to \\come to the aid of their country.";
		boolean result = LicenseCompareHelper.isLicenseTextEquivalent(testA, testA);
		assertTrue(result);
		// b is longer
		String testB = testA + " A bit longer";
		result = LicenseCompareHelper.isLicenseTextEquivalent(testA, testB);
		assertFalse(result);
		// first parameter is longer
		result = LicenseCompareHelper.isLicenseTextEquivalent(testB, testA);
		assertFalse(result);
		// white space doesn't matter
		String testPart1 = "Now is the time ";
		String testPart2 = " for all good men";
		String whiteSpace = " \t\n\r";
		testA = testPart1 + testPart2;
		testB = testPart1 + whiteSpace + testPart2;
		result = LicenseCompareHelper.isLicenseTextEquivalent(testA, testB);
		assertTrue(result);
		// trailing white space
		testB = testA + whiteSpace;
		result = LicenseCompareHelper.isLicenseTextEquivalent(testA, testB);
		assertTrue(result);
		result = LicenseCompareHelper.isLicenseTextEquivalent(testB, testA);
		assertTrue(result);
		// preceeding white space
		testB = whiteSpace + testA;
		result = LicenseCompareHelper.isLicenseTextEquivalent(testA, testB);
		assertTrue(result);
		result = LicenseCompareHelper.isLicenseTextEquivalent(testB, testA);
		assertTrue(result);
		// case shouldnt matter
		result = LicenseCompareHelper.isLicenseTextEquivalent(testA, testA.toUpperCase());
		assertTrue(result);
		// punctuation should matter
		testA = testPart1 + testPart2;
		String punctuation = ",";
		testB = testPart1 + punctuation + testPart2;
		result = LicenseCompareHelper.isLicenseTextEquivalent(testA, testB);
		assertFalse(result);
		// dash variations
		testA = testPart1 + "-" + testPart2;
		testB = testPart1 + "\u2013" + testPart2;
		result = LicenseCompareHelper.isLicenseTextEquivalent(testA, testB);
		assertTrue(result);
		testB = testPart1 + "\u2014" + testPart2;
		result = LicenseCompareHelper.isLicenseTextEquivalent(testA, testB);
		assertTrue(result);
		testB = testPart1 + "\u2015" + testPart2;
		result = LicenseCompareHelper.isLicenseTextEquivalent(testA, testB);
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
		boolean result = LicenseCompareHelper.isLicenseTextEquivalent(testA, testB);
		assertTrue(result);
		result = LicenseCompareHelper.isLicenseTextEquivalent(testB, testA);
		assertTrue(result);
		// c style multi line
		String startCMulti = "/*";
		String endCMulti = "*/";
		testB = startCMulti + part1 + part2 + endCMulti;
		result = LicenseCompareHelper.isLicenseTextEquivalent(testA, testB);
		assertTrue(result);
		result = LicenseCompareHelper.isLicenseTextEquivalent(testB, testA);
		assertTrue(result);
		// javaDocs comments
		String startJavaDocs = "/**";
		testB = startJavaDocs + part1 + part2 + endCMulti;
		result = LicenseCompareHelper.isLicenseTextEquivalent(testA, testB);
		assertTrue(result);
		result = LicenseCompareHelper.isLicenseTextEquivalent(testB, testA);
		assertTrue(result);
		// script line comment
		String scriptLineComment = "#";
		testB = scriptLineComment + part1 + scriptLineComment + part2 + "\n"+ scriptLineComment;
		result = LicenseCompareHelper.isLicenseTextEquivalent(testA, testB);
		assertTrue(result);
		result = LicenseCompareHelper.isLicenseTextEquivalent(testB, testA);
		assertTrue(result);
	}
	
	@Test
	public void testLicenseMatchEquivWords() {
		// per cent -> percent
		String part1 = "now is the time for ";
		String testA = part1 + "per cent";
		String testB = part1 + "percent";
		boolean result = LicenseCompareHelper.isLicenseTextEquivalent(testA, testB);
		assertTrue(result);
		// copyright holder-> copyright owner
		testA = "Copyright holder "+part1;
		testB = "copyright Owner "+ part1;
		result = LicenseCompareHelper.isLicenseTextEquivalent(testA, testB);
		assertTrue(result);
		// "license","licence"
		testA = part1 + " license " + part1;
		testB = part1 + " licence " + part1;
		result = LicenseCompareHelper.isLicenseTextEquivalent(testA, testB);
		assertTrue(result);
	}

	@Test
	public void testLicenseEqualsStdLicense() throws InvalidSPDXAnalysisException, SpdxCompareException {
		Map<String, String> xlation = Maps.newHashMap();
		String licName = "name";
		String licId = "ID1";
		String licText = "Text";
		String[] sourceUrls = new String[] {"http://www.sourceauditor.com/licenses"};
		String notes = "Notes";
		String stdLicNotice = "Notice";
		String template = "Template";
		boolean osiApproved = false;
		SpdxListedLicense lic1 = 
			new SpdxListedLicense(licName, licId, licText, 
					sourceUrls, notes, stdLicNotice, template, osiApproved);
		SpdxListedLicense lic2 = 
			new SpdxListedLicense(licName, licId, licText, 
					sourceUrls, notes, stdLicNotice, template, osiApproved);
		assertTrue(LicenseCompareHelper.isLicenseEqual(lic1, lic2, xlation));
		
		// try just changing the text - should still equal since the ID's are equal
		String text2 = "text2";
		SpdxListedLicense lic3 = 
			new SpdxListedLicense(licName, licId, text2, 
					sourceUrls, notes, stdLicNotice, template, osiApproved);
		assertTrue(LicenseCompareHelper.isLicenseEqual(lic1, lic3, xlation));
		// now try a different ID
		String licId2 = "ID2";
		SpdxListedLicense lic4 = 
			new SpdxListedLicense(licName, licId2, licText, 
					sourceUrls, notes, stdLicNotice, template, osiApproved);
		assertFalse(LicenseCompareHelper.isLicenseEqual(lic1, lic4, xlation));
	}
	
	@Test
	public void testLicenseEqualsNonStdLicense() throws InvalidSPDXAnalysisException, SpdxCompareException {
		Map<String, String> xlation = Maps.newHashMap();
		String licId = "ID1";
		String licText = "Text";

		// same license ID's
		ExtractedLicenseInfo lic1 = 
			new ExtractedLicenseInfo(licId, licText);
		ExtractedLicenseInfo lic2 = 
			new ExtractedLicenseInfo(licId, licText);
		xlation.put(licId, licId);
		assertTrue(LicenseCompareHelper.isLicenseEqual(lic1, lic2, xlation));
		// different license ID, same license
		xlation.clear();
		String licId2 = "id2";
		lic2 = 
			new ExtractedLicenseInfo(licId2, licText);
		xlation.put(licId, licId2);
		assertTrue(LicenseCompareHelper.isLicenseEqual(lic1, lic2, xlation));
		// different license ID, different license
		String licId3 = "id3";
		lic2 = 
			new ExtractedLicenseInfo(licId3, licId2);
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
		Map<String, String> xlation = Maps.newHashMap();
		ExtractedLicenseInfo lic1 = new ExtractedLicenseInfo(licId1, licText);
		ExtractedLicenseInfo lic2 = new ExtractedLicenseInfo(licId2, licText);
		ExtractedLicenseInfo lic3 = new ExtractedLicenseInfo(licId3, licText);
		ExtractedLicenseInfo lic4 = new ExtractedLicenseInfo(licId4, licText);
		ExtractedLicenseInfo lic5 = new ExtractedLicenseInfo(licId5, licText);
		ExtractedLicenseInfo lic6 = new ExtractedLicenseInfo(licId6, licText);
		xlation.put(licId1, licId4);
		xlation.put(licId2, licId5);
		xlation.put(licId3, licId6);
		AnyLicenseInfo[] set1 = new AnyLicenseInfo[] {
				lic1, lic2, lic3
		};
		AnyLicenseInfo[] set2 = new AnyLicenseInfo[] {
				lic4, lic5, lic6
		};
		ConjunctiveLicenseSet conj1 = new ConjunctiveLicenseSet(set1);
		ConjunctiveLicenseSet conj2 = new ConjunctiveLicenseSet(set2);
		
		assertTrue(LicenseCompareHelper.isLicenseEqual(conj1, conj2, xlation));
		// different order
		set2 = new AnyLicenseInfo[] {
				lic5, lic6, lic4
		};
		conj2 = new ConjunctiveLicenseSet(set2);
		assertTrue(LicenseCompareHelper.isLicenseEqual(conj1, conj2, xlation));

		String licId7 = "id7";
		ExtractedLicenseInfo lic7 = new ExtractedLicenseInfo(licId7, licText);
		AnyLicenseInfo[] set3 = new AnyLicenseInfo[] {
				lic4, lic5, lic7
		};
		ConjunctiveLicenseSet conj3 = new ConjunctiveLicenseSet(set3);
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
		Map<String, String> xlation = Maps.newHashMap();
		ExtractedLicenseInfo lic1 = new ExtractedLicenseInfo(licId1, licText);
		ExtractedLicenseInfo lic2 = new ExtractedLicenseInfo(licId2, licText);
		ExtractedLicenseInfo lic3 = new ExtractedLicenseInfo(licId3, licText);
		ExtractedLicenseInfo lic4 = new ExtractedLicenseInfo(licId4, licText);
		ExtractedLicenseInfo lic5 = new ExtractedLicenseInfo(licId5, licText);
		ExtractedLicenseInfo lic6 = new ExtractedLicenseInfo(licId6, licText);
		xlation.put(licId1, licId4);
		xlation.put(licId2, licId5);
		xlation.put(licId3, licId6);
		AnyLicenseInfo[] set1 = new AnyLicenseInfo[] {
				lic1, lic2, lic3
		};
		AnyLicenseInfo[] set2 = new AnyLicenseInfo[] {
				lic4, lic6, lic5
		};
		ConjunctiveLicenseSet conj1 = new ConjunctiveLicenseSet(set1);
		ConjunctiveLicenseSet conj2 = new ConjunctiveLicenseSet(set2);
		
		assertTrue(LicenseCompareHelper.isLicenseEqual(conj1, conj2, xlation));
	
		// busybox-1.rdf: (LicenseRef-14 AND LicenseRef-5 AND LicenseRef-6 AND LicenseRef-15 AND LicenseRef-3 AND LicenseRef-12 AND LicenseRef-4 AND LicenseRef-13 AND LicenseRef-10 AND LicenseRef-9 AND LicenseRef-11 AND LicenseRef-7 AND LicenseRef-8 AND LGPL-2.1+ AND LicenseRef-1 AND LicenseRef-2 AND LicenseRef-0 AND GPL-2.0+ AND GPL-2.0 AND LicenseRef-17 AND LicenseRef-16 AND BSD-2-Clause-Clear)
		xlation.clear();
		String licIdRef14 = "LicenseRef-14";
		ExtractedLicenseInfo licref14 = new ExtractedLicenseInfo(licIdRef14, licText);
		xlation.put(licIdRef14, licIdRef14);
		String licIdRef5 = "LicenseRef-5";
		ExtractedLicenseInfo licref5 = new ExtractedLicenseInfo(licIdRef5, licText);
		xlation.put(licIdRef5, licIdRef5);
		String licIdref6 = "LicenseRef-6";
		ExtractedLicenseInfo licref6 = new ExtractedLicenseInfo(licIdref6, licText);
		xlation.put(licIdref6, licIdref6);
		String licIdRef15 = "LicenseRef-15";
		ExtractedLicenseInfo licref15 = new ExtractedLicenseInfo(licIdRef15, licText);
		xlation.put(licIdRef15, licIdRef15);
		String licIdRef3 = "LicenseRef-3";
		ExtractedLicenseInfo licref3 = new ExtractedLicenseInfo(licIdRef3, licText);
		xlation.put(licIdRef3, licIdRef3);
		String licIdRef12 = "LicenseRef-12";
		ExtractedLicenseInfo licref12 = new ExtractedLicenseInfo(licIdRef12, licText);
		xlation.put(licIdRef12, licIdRef12);
		String licIdRef4 = "LicenseRef-4";
		ExtractedLicenseInfo licref4 = new ExtractedLicenseInfo(licIdRef4, licText);
		xlation.put(licIdRef4, licIdRef4);
		String licIdRef13 = "LicenseRef-13";
		ExtractedLicenseInfo licref13 = new ExtractedLicenseInfo(licIdRef13, licText);
		xlation.put(licIdRef13, licIdRef13);
		String licIdref10 = "LicenseRef-10";
		ExtractedLicenseInfo licref10 = new ExtractedLicenseInfo(licIdref10, licText);
		xlation.put(licIdref10, licIdref10);
		String licIdRef9 = "LicenseRef-9";
		ExtractedLicenseInfo licref9 = new ExtractedLicenseInfo(licIdRef9, licText);
		xlation.put(licIdRef9, licIdRef9);
		String licIdRef11 = "LicenseRef-11";
		ExtractedLicenseInfo licref11 = new ExtractedLicenseInfo(licIdRef11, licText);
		xlation.put(licIdRef11, licIdRef11);
		String licIdRef7 = "LicenseRef-7";
		ExtractedLicenseInfo licref7 = new ExtractedLicenseInfo(licIdRef7, licText);
		xlation.put(licIdRef7, licIdRef7);
		String licIdRef8 = "LicenseRef-8";
		ExtractedLicenseInfo licref8 = new ExtractedLicenseInfo(licIdRef8, licText);
		xlation.put(licIdRef8, licIdRef8);
		String licLGPLPlusId = "LGPL-2.1+";
		SpdxListedLicense licLGPLPlus = LicenseInfoFactory.getListedLicenseById(licLGPLPlusId);
		String licRef1 = "LicenseRef-1";
		ExtractedLicenseInfo licref1 = new ExtractedLicenseInfo(licRef1, licText);
		xlation.put(licRef1, licRef1);
		String licRef2 = "LicenseRef-2";
		ExtractedLicenseInfo licref2 = new ExtractedLicenseInfo(licRef2, licText);
		xlation.put(licRef2, licRef2);
		String licRef0 = "LicenseRef-0";
		ExtractedLicenseInfo licref0 = new ExtractedLicenseInfo(licRef0, licText);
		xlation.put(licRef0, licRef0);
		String licGPL20PlusId = "GPL-2.0+";
		SpdxListedLicense licGPL20Plus = LicenseInfoFactory.getListedLicenseById(licGPL20PlusId);
		String licGPL20id = "GPL-2.0";
		SpdxListedLicense licGPL20 = LicenseInfoFactory.getListedLicenseById(licGPL20id);
		String licRef17 = "LicenseRef-17";
		ExtractedLicenseInfo licref17 = new ExtractedLicenseInfo(licRef17, licText);
		xlation.put(licRef17, licRef17);
		String licRef16 = "LicenseRef-16";
		ExtractedLicenseInfo licref16 = new ExtractedLicenseInfo(licRef16, licText);
		xlation.put(licRef16, licRef16);
		String licRefBSD2Clearid = "BSD-2-Clause";
		SpdxListedLicense licRefBSD2Clear = LicenseInfoFactory.getListedLicenseById(licRefBSD2Clearid);
		// busybox-1.rdf: (LicenseRef-14 AND LicenseRef-5 AND LicenseRef-6 AND LicenseRef-15 AND LicenseRef-3 AND 
		//LicenseRef-12 AND LicenseRef-4 AND LicenseRef-13 AND LicenseRef-10 AND LicenseRef-9 AND LicenseRef-11 AND 
		//LicenseRef-7 AND LicenseRef-8 AND LGPL-2.1+ AND LicenseRef-1 AND LicenseRef-2 AND LicenseRef-0 AND 
		//GPL-2.0+ AND GPL-2.0 AND LicenseRef-17 AND LicenseRef-16 AND BSD-2-Clause-Clear)

		AnyLicenseInfo[] bbset1 = new AnyLicenseInfo[] {licref14, licref5, licref6, licref15, licref3, licref12, licref4, 
				licref13,licref10, licref9, licref11, licref7, licref8, licLGPLPlus, licref1, licref2, licref0, licGPL20Plus,
				licGPL20, licref17, licref16, licRefBSD2Clear
		};
		ConjunctiveLicenseSet bbconj1 = new ConjunctiveLicenseSet(bbset1);
		// busybox-2.rdf: (LicenseRef-14 AND LicenseRef-5 AND LicenseRef-6 AND LicenseRef-15 AND LicenseRef-12 AND LicenseRef-3
		//AND LicenseRef-13 AND LicenseRef-4 AND LicenseRef-10 AND LicenseRef-9 AND LicenseRef-11 AND LicenseRef-7 AND 
		//LicenseRef-8 AND LGPL-2.1+ AND LicenseRef-1 AND LicenseRef-2 AND LicenseRef-0 AND GPL-2.0+ AND GPL-2.0 AND 
		//LicenseRef-17 AND BSD-2-Clause-Clear AND LicenseRef-16)

		AnyLicenseInfo[] bbset2 = new AnyLicenseInfo[] {licref14, licref5, licref6, licref15, licref12, licref3, licref13,
				licref4, licref10, licref9, licref11, licref7, licref8, licLGPLPlus, licref1, licref2, licref0, licGPL20Plus,
				licGPL20, licref17, licRefBSD2Clear, licref16
		};
		ConjunctiveLicenseSet bbconj2 = new ConjunctiveLicenseSet(bbset2);
		assertTrue(LicenseCompareHelper.isLicenseEqual(bbconj1, bbconj2, xlation));
		assertTrue(LicenseCompareHelper.isLicenseEqual(bbconj2, bbconj1, xlation));
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
		Map<String, String> xlation = Maps.newHashMap();
		ExtractedLicenseInfo lic1 = new ExtractedLicenseInfo(licId1, licText);
		ExtractedLicenseInfo lic2 = new ExtractedLicenseInfo(licId2, licText);
		ExtractedLicenseInfo lic3 = new ExtractedLicenseInfo(licId3, licText);
		ExtractedLicenseInfo lic4 = new ExtractedLicenseInfo(licId4, licText);
		ExtractedLicenseInfo lic5 = new ExtractedLicenseInfo(licId5, licText);
		ExtractedLicenseInfo lic6 = new ExtractedLicenseInfo(licId6, licText);
		xlation.put(licId1, licId4);
		xlation.put(licId2, licId5);
		xlation.put(licId3, licId6);
		AnyLicenseInfo[] set1 = new AnyLicenseInfo[] {
				lic1, lic2, lic3
		};
		AnyLicenseInfo[] set2 = new AnyLicenseInfo[] {
				lic4, lic5, lic6
		};
		DisjunctiveLicenseSet conj1 = new DisjunctiveLicenseSet(set1);
		DisjunctiveLicenseSet conj2 = new DisjunctiveLicenseSet(set2);
		
		assertTrue(LicenseCompareHelper.isLicenseEqual(conj1, conj2, xlation));
		
		String licId7 = "id7";
		ExtractedLicenseInfo lic7 = new ExtractedLicenseInfo(licId7, licText);
		AnyLicenseInfo[] set3 = new AnyLicenseInfo[] {
				lic4, lic5, lic7
		};
		DisjunctiveLicenseSet conj3 = new DisjunctiveLicenseSet(set3);
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
		Map<String, String> xlation = Maps.newHashMap();
		ExtractedLicenseInfo lic1 = new ExtractedLicenseInfo(licId1, licText);
		ExtractedLicenseInfo lic2 = new ExtractedLicenseInfo(licId2, licText);
		ExtractedLicenseInfo lic3 = new ExtractedLicenseInfo(licId3, licText);
		ExtractedLicenseInfo lic4 = new ExtractedLicenseInfo(licId4, licText);
		ExtractedLicenseInfo lic5 = new ExtractedLicenseInfo(licId5, licText);
		ExtractedLicenseInfo lic6 = new ExtractedLicenseInfo(licId6, licText);
		xlation.put(licId1, licId4);
		xlation.put(licId2, licId5);
		xlation.put(licId3, licId6);
		AnyLicenseInfo[] set1 = new AnyLicenseInfo[] {
				lic1, lic2
		};
		AnyLicenseInfo[] set2 = new AnyLicenseInfo[] {
				lic4, lic5
		};
		DisjunctiveLicenseSet conj1 = new DisjunctiveLicenseSet(set1);
		DisjunctiveLicenseSet conj2 = new DisjunctiveLicenseSet(set2);
		
		AnyLicenseInfo[] set3 = new AnyLicenseInfo[] {
				conj1, lic3
		};
		AnyLicenseInfo[] set4 = new AnyLicenseInfo[] {
				lic6, conj2
		};
		ConjunctiveLicenseSet conj3 = new ConjunctiveLicenseSet(set3);
		ConjunctiveLicenseSet conj4 = new ConjunctiveLicenseSet(set4);
		
		assertTrue(LicenseCompareHelper.isLicenseEqual(conj3, conj4, xlation));
	}	
	
	@Test
	public void testLicenseEqualsNoAsserLicense() throws InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxNoAssertionLicense lic1 = new SpdxNoAssertionLicense();
		SpdxNoAssertionLicense lic2 = new SpdxNoAssertionLicense();
		SpdxNoneLicense lic3 = new SpdxNoneLicense();
		Map<String, String> xlationMap = Maps.newHashMap();
		assertTrue(LicenseCompareHelper.isLicenseEqual(lic1, lic2, xlationMap));
		assertFalse(LicenseCompareHelper.isLicenseEqual(lic1, lic3, xlationMap));
	}	
	
	@Test
	public void testLicenseEqualsNoneLicense() throws InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxNoAssertionLicense lic2 = new SpdxNoAssertionLicense();
		SpdxNoneLicense lic3 = new SpdxNoneLicense();
		SpdxNoneLicense lic4 = new SpdxNoneLicense();
		Map<String, String> xlationMap = Maps.newHashMap();
		assertTrue(LicenseCompareHelper.isLicenseEqual(lic3, lic4, xlationMap));
		assertFalse(LicenseCompareHelper.isLicenseEqual(lic4, lic2, xlationMap));
	}	
	
	@Test
	public void testTokenizeLicenseText() {
		String test = "Now is the.time,for? \"all\" good men.";
		Map<Integer, LineColumn> tokenToLocation = new HashMap<Integer, LineColumn>();
		String[] result = LicenseCompareHelper.tokenizeLicenseText(test, tokenToLocation);
		assertEquals(14,result.length);
		assertEquals("now",result[0]);
		assertEquals("is",result[1]);
		assertEquals("the",result[2]);
		assertEquals(".",result[3]);
		assertEquals("time",result[4]);
		assertEquals(",",result[5]);
		assertEquals("for",result[6]);
		assertEquals("?",result[7]);
		assertEquals("\"",result[8]);
		assertEquals("all",result[9]);
		assertEquals("\"",result[10]);
		assertEquals("good",result[11]);
		assertEquals("men",result[12]);
		assertEquals(".",result[13]);
		assertEquals(0,tokenToLocation.get(0).getColumn());
		assertEquals(4,tokenToLocation.get(1).getColumn());
		assertEquals(7,tokenToLocation.get(2).getColumn());
		assertEquals(10,tokenToLocation.get(3).getColumn());
		assertEquals(11,tokenToLocation.get(4).getColumn());
		assertEquals(15,tokenToLocation.get(5).getColumn());
		assertEquals(16,tokenToLocation.get(6).getColumn());
		assertEquals(19,tokenToLocation.get(7).getColumn());
		assertEquals(21,tokenToLocation.get(8).getColumn());
		assertEquals(22,tokenToLocation.get(9).getColumn());
		assertEquals(25,tokenToLocation.get(10).getColumn());
		assertEquals(27,tokenToLocation.get(11).getColumn());
		assertEquals(32,tokenToLocation.get(12).getColumn());
		assertEquals(35,tokenToLocation.get(13).getColumn());
	}
	
	@Test
	public void regressionTokenString() {
		String test = "THIS SOFTWARE IS PROVIDED BY COPYRIGHT HOLDER \"AS IS\" AND";
		Map<Integer, LineColumn> tokenToLocation = new HashMap<Integer, LineColumn>();
		String[] result = LicenseCompareHelper.tokenizeLicenseText(test, tokenToLocation);
		assertEquals(11, result.length);
		assertEquals("this",result[0]);
		assertEquals("software",result[1]);
		assertEquals("is",result[2]);
		assertEquals("provided",result[3]);
		assertEquals("by",result[4]);
		assertEquals("copyright-holder",result[5]);
		assertEquals("\"",result[6]);
		assertEquals("as",result[7]);
		assertEquals("is",result[8]);
		assertEquals("\"",result[9]);
		assertEquals("and",result[10]);
		assertEquals(0,tokenToLocation.get(0).getColumn());
		assertEquals(5,tokenToLocation.get(1).getColumn());
		assertEquals(14,tokenToLocation.get(2).getColumn());
		assertEquals(17,tokenToLocation.get(3).getColumn());
		assertEquals(26,tokenToLocation.get(4).getColumn());
		assertEquals(29,tokenToLocation.get(5).getColumn());
		assertEquals(46,tokenToLocation.get(6).getColumn());
		assertEquals(47,tokenToLocation.get(7).getColumn());
		assertEquals(50,tokenToLocation.get(8).getColumn());
		assertEquals(52,tokenToLocation.get(9).getColumn());
		assertEquals(54,tokenToLocation.get(10).getColumn());
	}
	
	@Test
	public void testOddChars() {
		String test = "COPYRIGHT   I B M   CORPORATION 2002";
		Map<Integer, LineColumn> tokenToLocation = new HashMap<Integer, LineColumn>();
		String[] result = LicenseCompareHelper.tokenizeLicenseText(test, tokenToLocation);
		assertEquals(6,result.length);
		assertEquals("copyright", result[0]);
		assertEquals("i", result[1]);
		assertEquals("b", result[2]);
		assertEquals("m", result[3]);
		assertEquals("corporation", result[4]);
		assertEquals("2002", result[5]);
		test = "Claims      If";
		result = LicenseCompareHelper.tokenizeLicenseText(test, tokenToLocation);
		assertEquals(2, result.length);
		assertEquals("claims",result[0]);
		assertEquals("if", result[1]);	
	}
	
	@Test
	public void testisSingleTokenString() {
		assertTrue(LicenseCompareHelper.isSingleTokenString(" token "));
		assertTrue(LicenseCompareHelper.isSingleTokenString("'"));
		assertTrue(LicenseCompareHelper.isSingleTokenString(" '"));
		assertTrue(LicenseCompareHelper.isSingleTokenString("' "));
		assertFalse(LicenseCompareHelper.isSingleTokenString("a and"));
		assertFalse(LicenseCompareHelper.isSingleTokenString("a\nand"));
	}
	
	@Test
	public void regressionTestMatchingGpl20Only() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException {
		String compareText = UnitTestHelper.fileToText(GPL_2_TEXT);
		DifferenceDescription result = LicenseCompareHelper.isTextStandardLicense(LicenseInfoFactory.getListedLicenseById("GPL-2.0-only"), compareText);
		assertFalse(result.isDifferenceFound());
	}
	
	@Test
	public void testMatchingStandardLicenseIds() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException {
		// This test is too slow!
//		String compareText = UnitTestHelper.fileToText(GPL_2_TEXT);
//		String[] result = LicenseCompareHelper.matchingStandardLicenseIds(compareText);
//		assertEquals(2,result.length);
//		assertTrue(result[0].startsWith("GPL-2"));
//		assertTrue(result[1].startsWith("GPL-2"));
	}
	
	@Test
	public void testFirstLicenseToken() {
		assertEquals("first", LicenseCompareHelper.getFirstLicenseToken("   first,token that is needed\nnext"));
	}
	
	@SuppressWarnings("unused")
	private String stringCharToUnicode(String s, int location) {
		return "\\u" + Integer.toHexString(s.charAt(location) | 0x10000).substring(1);
	}
}
