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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.compare.SpdxLicenseDifference;
import org.spdx.compare.SpdxComparer.SPDXReviewDifference;
import org.spdx.rdfparser.DOAPProject;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXConjunctiveLicenseSet;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXDisjunctiveLicenseSet;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicense;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXLicenseInfoFactory;
import org.spdx.rdfparser.SPDXNonStandardLicense;
import org.spdx.rdfparser.SPDXNoneLicense;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SPDXStandardLicense;
import org.spdx.rdfparser.SpdxNoAssertionLicense;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

/**
 * @author Gary O'Neall
 *
 */
public class SpdxComparerTest {
	
	static final String TEST_RDF_FILE_PATH = "TestFiles"+File.separator+"SPDXRdfExample.rdf";
	private static final String STD_LIC_ID_CC0 = "CC-BY-1.0";
	private static final String STD_LIC_ID_MPL11 = "MPL-1.1";
	File testRDFFile;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.testRDFFile = new File(TEST_RDF_FILE_PATH); 
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#compare(org.spdx.rdfparser.SPDXDocument, org.spdx.rdfparser.SPDXDocument)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testCompare() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		try {
			comparer.isCopyrightTextsEqual();	// should fail
			fail("Not checking for comparer being complete");
		} catch (SpdxCompareException ex) {
			// we expect an error
		}
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isDifferenceFound());
		assertEquals(doc1.hashCode(), comparer.getSpdxDoc(0).hashCode());
		assertEquals(doc2.hashCode(), comparer.getSpdxDoc(1).hashCode());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#compareLicense(int, org.spdx.rdfparser.SPDXLicenseInfo, int, org.spdx.rdfparser.SPDXLicenseInfo)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 * @throws SpdxCompareException 
	 * @throws InvalidLicenseStringException 
	 */
	@Test
	public void testCompareLicense() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException, InvalidLicenseStringException {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isDifferenceFound());
		SPDXNonStandardLicense[] extractedInfos1 = doc1.getExtractedLicenseInfos();
		SPDXNonStandardLicense[] extractedInfos2 = doc2.getExtractedLicenseInfos();
		
		HashMap<Integer, Integer> xlateDoc1ToDoc2LicId = createLicIdXlation(extractedInfos1, extractedInfos2);
		
		//Standard License
		SPDXStandardLicense lic1 = SPDXLicenseInfoFactory.getStandardLicenseById(STD_LIC_ID_CC0);
		SPDXStandardLicense lic1_1 = SPDXLicenseInfoFactory.getStandardLicenseById(STD_LIC_ID_CC0);
		SPDXStandardLicense lic2 = SPDXLicenseInfoFactory.getStandardLicenseById(STD_LIC_ID_MPL11);
		assertTrue(comparer.compareLicense(0, lic1, 1, lic1_1));
		assertFalse(comparer.compareLicense(0, lic1, 1, lic2));
		//Extracted License
		assertTrue(comparer.compareLicense(0, extractedInfos1[0], 1, extractedInfos2[xlateDoc1ToDoc2LicId.get(0)]));
		int nonEqual = 0;
		if (xlateDoc1ToDoc2LicId.get(0) == nonEqual) {
			nonEqual = 1;
		}
		assertFalse(comparer.compareLicense(0, extractedInfos1[0], 1, extractedInfos2[nonEqual]));
		try {
			assertFalse(comparer.compareLicense(0, extractedInfos1[0], 1, extractedInfos1[0]));
		} catch(SpdxCompareException ex) {
			// we expect a mappint exception
		}
		//Conjunctive License
		StringBuilder sb = new StringBuilder("(");
		sb.append(STD_LIC_ID_CC0);
		sb.append(" AND ");
		sb.append(extractedInfos1[0].getId());
		sb.append(" AND ");
		sb.append(STD_LIC_ID_MPL11);
		sb.append(" AND ");
		sb.append(extractedInfos1[1].getId());
		sb.append(")");
		SPDXLicenseInfo conj1 = SPDXLicenseInfoFactory.parseSPDXLicenseString(sb.toString());

		sb = new StringBuilder("(");
		sb.append(STD_LIC_ID_MPL11);
		sb.append(" AND ");
		sb.append(extractedInfos2[xlateDoc1ToDoc2LicId.get(1)].getId());
		sb.append(" AND ");
		sb.append(STD_LIC_ID_CC0);
		sb.append(" AND ");
		sb.append(extractedInfos2[xlateDoc1ToDoc2LicId.get(0)].getId());
		sb.append(")");
		SPDXLicenseInfo conj2 = SPDXLicenseInfoFactory.parseSPDXLicenseString(sb.toString());
		
		sb = new StringBuilder("(");
		sb.append(STD_LIC_ID_MPL11);
		sb.append(" AND ");
		sb.append(extractedInfos2[xlateDoc1ToDoc2LicId.get(1)].getId());
		sb.append(" AND ");
		sb.append(STD_LIC_ID_CC0);
		sb.append(")");
		SPDXLicenseInfo conj3 = SPDXLicenseInfoFactory.parseSPDXLicenseString(sb.toString());
		
		assertTrue(comparer.compareLicense(0, conj1, 1, conj2));
		assertFalse(comparer.compareLicense(0, conj1, 1, conj3));
		try {
			assertFalse(comparer.compareLicense(0, conj2, 1, conj2));
		} catch(SpdxCompareException ex) {
			// we expect a mappint exception
		}
		//Disjunctive License
		sb = new StringBuilder("(");
		sb.append(STD_LIC_ID_CC0);
		sb.append(" OR ");
		sb.append(extractedInfos1[0].getId());
		sb.append(" OR ");
		sb.append(STD_LIC_ID_MPL11);
		sb.append(" OR ");
		sb.append(extractedInfos1[1].getId());
		sb.append(")");
		SPDXLicenseInfo dis1 = SPDXLicenseInfoFactory.parseSPDXLicenseString(sb.toString());

		sb = new StringBuilder("(");
		sb.append(STD_LIC_ID_MPL11);
		sb.append(" OR ");
		sb.append(extractedInfos2[xlateDoc1ToDoc2LicId.get(1)].getId());
		sb.append(" OR ");
		sb.append(extractedInfos2[xlateDoc1ToDoc2LicId.get(0)].getId());
		sb.append(" OR ");
		sb.append(STD_LIC_ID_CC0);
		sb.append(")");
		SPDXLicenseInfo dis2 = SPDXLicenseInfoFactory.parseSPDXLicenseString(sb.toString());
		
		sb = new StringBuilder("(");
		sb.append(STD_LIC_ID_MPL11);
		sb.append(" OR ");
		sb.append(extractedInfos2[xlateDoc1ToDoc2LicId.get(1)].getId());
		sb.append(" OR ");
		sb.append(STD_LIC_ID_CC0);
		sb.append(")");
		SPDXLicenseInfo dis3 = SPDXLicenseInfoFactory.parseSPDXLicenseString(sb.toString());
		
		assertTrue(comparer.compareLicense(0, dis1, 1, dis2));
		assertFalse(comparer.compareLicense(0, dis1, 1, dis3));
		try {
			assertFalse(comparer.compareLicense(0, dis2, 1, dis2));
		} catch(SpdxCompareException ex) {
			// we expect a mappint exception
		}
		//Complex License
		SPDXDisjunctiveLicenseSet subcomplex1 = new SPDXDisjunctiveLicenseSet(
					new SPDXLicenseInfo[] {lic1, conj1});
		SPDXConjunctiveLicenseSet complex1 = new SPDXConjunctiveLicenseSet(
				new SPDXLicenseInfo[] {subcomplex1, dis1, extractedInfos1[0]});
		SPDXDisjunctiveLicenseSet subcomplex2 = new SPDXDisjunctiveLicenseSet(
				new SPDXLicenseInfo[] {conj2, lic1_1});
		SPDXConjunctiveLicenseSet complex2 = new SPDXConjunctiveLicenseSet(
			new SPDXLicenseInfo[] {dis2, subcomplex2, extractedInfos2[xlateDoc1ToDoc2LicId.get(0)]});
		
		SPDXDisjunctiveLicenseSet subcomplex3 = new SPDXDisjunctiveLicenseSet(
				new SPDXLicenseInfo[] {conj3, lic1_1});
		SPDXConjunctiveLicenseSet complex3 = new SPDXConjunctiveLicenseSet(
			new SPDXLicenseInfo[] {dis2, subcomplex3, extractedInfos2[xlateDoc1ToDoc2LicId.get(0)]});
		assertTrue(comparer.compareLicense(0, complex1, 1, complex2));
		assertFalse(comparer.compareLicense(0, complex1, 1, complex3));
		//NONE
		SPDXNoneLicense noneLic1 = new SPDXNoneLicense();
		SPDXNoneLicense noneLic2 = new SPDXNoneLicense();
		SpdxNoAssertionLicense noAssertLic1 = new SpdxNoAssertionLicense();
		SpdxNoAssertionLicense noAssertLic2 = new SpdxNoAssertionLicense();
		assertTrue (comparer.compareLicense(0, noneLic1, 1, noneLic2));
		assertFalse (comparer.compareLicense(0, complex1, 1, noneLic2));
		//NOASSERTION
		assertTrue(comparer.compareLicense(0, noAssertLic1, 1, noAssertLic2));
		assertFalse(comparer.compareLicense(0, noAssertLic2, 1, lic1));
		assertFalse(comparer.compareLicense(0, noneLic2, 1, noAssertLic1));
	}

	/**
	 * Create a license ID mapping table between licInfos1 and licInfos2 based on EXACT matches of text
	 * @param licInfos1
	 * @param licInfos2
	 * @return
	 */
	private HashMap<Integer, Integer> createLicIdXlation(
			SPDXNonStandardLicense[] licInfos1,
			SPDXNonStandardLicense[] licInfos2) {
		HashMap<Integer, Integer> retval = new HashMap<Integer, Integer>();
		for (int i = 0;i < licInfos1.length; i++) {
			boolean found = false;
			for (int j = 0; j < licInfos2.length; j++) {
				if (licInfos1[i].getText().equals(licInfos2[j].getText())) {
					if (found) {
						fail("Two licenses found with the same text: "+licInfos1[i].getText());
					}
					retval.put(i, j);
				}
			}
		}
		return retval;
	}

	/**
	 * Modifies the extracted license info license ID's by adding a digit to
	 * each of the ID's
	 * @param doc
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void alterExtractedLicenseInfoIds(SPDXDocument doc, int digit) throws InvalidSPDXAnalysisException {
		SPDXNonStandardLicense[] extracted = doc.getExtractedLicenseInfos();
		for (int i = 0; i < extracted.length; i++) {
			String oldId = extracted[i].getId();
			String newId = oldId + String.valueOf(digit);
			extracted[i].setId(newId);
			assertEquals(0, extracted[i].verify().size());
		}
		
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isDifferenceFound()}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsDifferenceFound() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isDifferenceFound());
		doc2.setDocumentComment("a new doc comment");
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isDifferenceFound());
		// Note - we will test the isDifferenceFound in each of the specific
		// differences unit tests below

	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isSpdxVersionEqual()}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws SpdxCompareException 
	 * @throws IOException 
	 */
	@Test
	public void testIsSpdxVersionEqual() throws InvalidSPDXAnalysisException, SpdxCompareException, IOException {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isDifferenceFound());
		assertTrue(comparer.isSpdxVersionEqual());
		doc2.setSpdxVersion("SPDX-1.0");
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isDifferenceFound());
		assertFalse(comparer.isSpdxVersionEqual());
		doc1.setSpdxVersion("SPDX-1.0");
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#getSpdxDoc(int)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testGetSpdxDoc() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		String DOC_2_COMMENT = "doc2";
		String DOC_1_COMMENT = "doc1";
		doc1.setDocumentComment(DOC_1_COMMENT);
		doc2.setDocumentComment(DOC_2_COMMENT);
		comparer.compare(doc1, doc2);
		assertEquals(DOC_1_COMMENT, comparer.getSpdxDoc(0).getDocumentComment());
		assertEquals(DOC_2_COMMENT, comparer.getSpdxDoc(1).getDocumentComment());

	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isDataLicenseEqual()}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsDataLicenseEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isDifferenceFound());
		assertTrue(comparer.isDataLicenseEqual());
		doc2.setSpdxVersion("SPDX-1.0");
		doc2.setDataLicense(SPDXLicenseInfoFactory.getStandardLicenseById(SpdxRdfConstants.SPDX_DATA_LICENSE_ID_VERSION_1_0));
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isDifferenceFound());
		assertFalse(comparer.isDataLicenseEqual());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isDocumentCommentsEqual()}.
	 */
	@Test
	public void testIsDocumentCommentsEqual()throws IOException, InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isDocumentCommentsEqual());
		assertFalse(comparer.isDifferenceFound());
		doc2.setDocumentComment("a new doc comment");
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isDifferenceFound());
		assertFalse(comparer.isDocumentCommentsEqual());
		doc1.setDocumentComment("a new doc comment");
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isDifferenceFound());
		assertTrue(comparer.isDocumentCommentsEqual());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isReviewersEqual()}.
	 */
	@Test
	public void testIsReviewersEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);

		String reviewDate1 = "2010-02-03T00:00:00Z";
		String reviewerComment1 = "comment1";
		String reviewer1 = "Person: creator1";
		SPDXReview review1 = new SPDXReview(reviewer1, reviewDate1, reviewerComment1);
		String reviewDate2 = "2010-02-03T10:00:00Z";
		String reviewerComment2 = "comment2";
		String reviewer2 = "Person: creator2";
		SPDXReview review2 = new SPDXReview(reviewer2, reviewDate2, reviewerComment2);
		String reviewDate3 = "2011-02-13T00:00:00Z";
		String reviewerComment3 = "comment3";
		String reviewer3 = "Person: creator3";
		SPDXReview review3 = new SPDXReview(reviewer3, reviewDate3, reviewerComment3);

		// empty reviewers
		SPDXReview[] emptyReview = new SPDXReview[0];
		doc1.setReviewers(emptyReview);
		doc2.setReviewers(emptyReview);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isReviewersEqual());
		assertFalse(comparer.isDifferenceFound());
		// one review
		SPDXReview[] oneReview = new SPDXReview[] {review1};
		doc1.setReviewers(oneReview);
		doc2.setReviewers(oneReview);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isReviewersEqual());
		assertFalse(comparer.isDifferenceFound());
		
		// empty reviewers and one more reviewer	
		doc1.setReviewers(emptyReview);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isReviewersEqual());
		assertTrue(comparer.isDifferenceFound());
		doc1.setReviewers(emptyReview);
		doc2.setReviewers(oneReview);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isReviewersEqual());
		assertTrue(comparer.isDifferenceFound());

		// more reviewers
		SPDXReview[] twoReview = new SPDXReview[] {review1, review2};
		SPDXReview[] threeReview = new SPDXReview[] {review1, review2, review3};
		doc1.setReviewers(twoReview);
		doc2.setReviewers(threeReview);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isReviewersEqual());
		assertTrue(comparer.isDifferenceFound());

		// same reviewer names but different comment
		SPDXReview reviewThreeDiffComment = new SPDXReview(reviewer3, reviewDate3, "");
		SPDXReview[] threeReviewDiffComment = new SPDXReview[] {review1, review2, reviewThreeDiffComment};
		doc1.setReviewers(threeReviewDiffComment);
		doc2.setReviewers(threeReview);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isReviewersEqual());
		assertTrue(comparer.isDifferenceFound());

		// same reviewer names but different date
		SPDXReview review2DiffDate = new SPDXReview(reviewer2, "2012-02-03T10:00:00Z", reviewerComment2);
		SPDXReview[] threeReviewDiffDate = new SPDXReview[] {review1, review2, review2DiffDate};
		doc1.setReviewers(threeReview);
		doc2.setReviewers(threeReviewDiffDate);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isReviewersEqual());
		assertTrue(comparer.isDifferenceFound());

		// multiple reviewers different order
		SPDXReview[] threeReviewDifferentOrder = new SPDXReview[] {review2, review1, review3};
		doc1.setReviewers(threeReview);
		doc2.setReviewers(threeReviewDifferentOrder);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isReviewersEqual());
		assertFalse(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isExtractedLicensingInfosEqual()}.
	 */
	@Test
	public void testIsExtractedLicensingInfosEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		SPDXNonStandardLicense[] orig1 = doc1.getExtractedLicenseInfos();
		SPDXNonStandardLicense[] orig2 = doc2.getExtractedLicenseInfos();
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isExtractedLicensingInfosEqual());
		assertFalse(comparer.isDifferenceFound());

		int doc1id = 100;
		int doc2id = 200;
		String id1_1 = SPDXDocument.formNonStandardLicenseID(doc1id++);
		String text1 = "License text 1";
		String name1 = "licname1";
		String[] crossReff1 = new String[] {"http://cross.ref.one"};
		String comment1 = "comment1";
		SPDXNonStandardLicense lic1_1 = new SPDXNonStandardLicense(
				id1_1, text1, name1, crossReff1, comment1);
		String id1_2 = SPDXDocument.formNonStandardLicenseID(doc2id++);
		SPDXNonStandardLicense lic1_2 = new SPDXNonStandardLicense(
				id1_2, text1, name1, crossReff1, comment1);
		
		String id2_1 = SPDXDocument.formNonStandardLicenseID(doc1id++);
		String text2 = "License text 2";
		String name2 = "licname2";
		String[] crossReff2 = new String[] {"http://cross.ref.one", "http://cross.ref.two"};
		String comment2 = "comment2";
		SPDXNonStandardLicense lic2_1 = new SPDXNonStandardLicense(
				id2_1, text2, name2, crossReff2, comment2);
		String id2_2 = SPDXDocument.formNonStandardLicenseID(doc2id++);
		SPDXNonStandardLicense lic2_2 = new SPDXNonStandardLicense(
				id2_2, text2, name2, crossReff2, comment2);
		
		String id3_1 = SPDXDocument.formNonStandardLicenseID(doc1id++);
		String text3 = "License text 3";
		String name3 = "";
		String[] crossReff3 = new String[] {};
		String comment3 = "comment3";
		SPDXNonStandardLicense lic3_1 = new SPDXNonStandardLicense(
				id3_1, text3, name3, crossReff3, comment3);
		String id3_2 = SPDXDocument.formNonStandardLicenseID(doc2id++);
		SPDXNonStandardLicense lic3_2 = new SPDXNonStandardLicense(
				id3_2, text3, name3, crossReff3, comment3);

		String id4_1 = SPDXDocument.formNonStandardLicenseID(doc1id++);
		String text4 = "License text 4";
		String name4 = "";
		String[] crossReff4 = new String[] {};
		String comment4 = "";
		SPDXNonStandardLicense lic4_1 = new SPDXNonStandardLicense(
				id4_1, text4, name4, crossReff4, comment4);
		String id4_2 = SPDXDocument.formNonStandardLicenseID(doc2id++);
		SPDXNonStandardLicense lic4_2 = new SPDXNonStandardLicense(
				id4_2, text4, name4, crossReff4, comment4);

		// same licenses, different order
		SPDXNonStandardLicense[] exLicenses1 = Arrays.copyOf(orig1, orig1.length+4);
		exLicenses1[orig1.length+0] = lic1_1;
		exLicenses1[orig1.length+1] = lic2_1;
		exLicenses1[orig1.length+2] = lic3_1;
		exLicenses1[orig1.length+3] = lic4_1;
		SPDXNonStandardLicense[] exLicenses2 = Arrays.copyOf(orig2, orig2.length+4);
		exLicenses2[orig2.length+0] = lic3_2;
		exLicenses2[orig2.length+1] = lic1_2;
		exLicenses2[orig2.length+2] = lic4_2;
		exLicenses2[orig2.length+3] = lic2_2;
		doc1.setExtractedLicenseInfos(exLicenses1);
		doc2.setExtractedLicenseInfos(exLicenses2);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isExtractedLicensingInfosEqual());
		assertFalse(comparer.isDifferenceFound());

		// More licenses in doc1
		exLicenses1 = Arrays.copyOf(orig1, orig1.length+4);
		exLicenses1[orig1.length+0] = lic1_1;
		exLicenses1[orig1.length+1] = lic2_1;
		exLicenses1[orig1.length+2] = lic3_1;
		exLicenses1[orig1.length+3] = lic4_1;
		exLicenses2 = Arrays.copyOf(orig2, orig2.length+2);
		exLicenses2[orig2.length+0] = lic3_2;
		exLicenses2[orig2.length+1] = lic1_2;

		doc1.setExtractedLicenseInfos(exLicenses1);
		doc2.setExtractedLicenseInfos(exLicenses2);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isExtractedLicensingInfosEqual());
		assertTrue(comparer.isDifferenceFound());
		
		// more licenses in doc2
		exLicenses1 = Arrays.copyOf(orig1, orig1.length);

		exLicenses2 = Arrays.copyOf(orig2, orig2.length+4);
		exLicenses2[orig2.length+0] = lic3_2;
		exLicenses2[orig2.length+1] = lic1_2;
		exLicenses2[orig2.length+2] = lic4_2;
		exLicenses2[orig2.length+3] = lic2_2;
		doc1.setExtractedLicenseInfos(exLicenses1);
		doc2.setExtractedLicenseInfos(exLicenses2);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isExtractedLicensingInfosEqual());
		assertTrue(comparer.isDifferenceFound());

		// license text different
		exLicenses1 = Arrays.copyOf(orig1, orig1.length+4);
		exLicenses1[orig1.length+0] = lic1_1;
		exLicenses1[orig1.length+1] = lic2_1;
		exLicenses1[orig1.length+2] = lic3_1;
		exLicenses1[orig1.length+3] = lic4_1;
		exLicenses2 = Arrays.copyOf(orig2, orig2.length+4);
		
		SPDXNonStandardLicense lic1_2_diff_Text = new SPDXNonStandardLicense(
				id1_2, "Different Text", name1, crossReff1, comment1);
		exLicenses2[orig2.length+0] = lic3_2;
		exLicenses2[orig2.length+1] = lic1_2_diff_Text;
		exLicenses2[orig2.length+2] = lic4_2;
		exLicenses2[orig2.length+3] = lic2_2;

		doc1.setExtractedLicenseInfos(exLicenses1);
		doc2.setExtractedLicenseInfos(exLicenses2);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isExtractedLicensingInfosEqual());
		assertTrue(comparer.isDifferenceFound());
		
		// license comments differ	
		exLicenses1 = Arrays.copyOf(orig1, orig1.length+4);
		exLicenses1[orig1.length+0] = lic1_1;
		exLicenses1[orig1.length+1] = lic2_1;
		exLicenses1[orig1.length+2] = lic3_1;
		exLicenses1[orig1.length+3] = lic4_1;
		exLicenses2 = Arrays.copyOf(orig2, orig2.length+4);
		
		SPDXNonStandardLicense lic1_2_diff_Comment = new SPDXNonStandardLicense(
				id1_2, text1, name1, crossReff1, "different comment");
		exLicenses2[orig2.length+0] = lic3_2;
		exLicenses2[orig2.length+1] = lic1_2_diff_Comment;
		exLicenses2[orig2.length+2] = lic4_2;
		exLicenses2[orig2.length+3] = lic2_2;

		doc1.setExtractedLicenseInfos(exLicenses1);
		doc2.setExtractedLicenseInfos(exLicenses2);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isExtractedLicensingInfosEqual());
		assertTrue(comparer.isDifferenceFound());
		
		// license reference URLs differ
		
		exLicenses1 = Arrays.copyOf(orig1, orig1.length+4);
		exLicenses1[orig1.length+0] = lic1_1;
		exLicenses1[orig1.length+1] = lic2_1;
		exLicenses1[orig1.length+2] = lic3_1;
		exLicenses1[orig1.length+3] = lic4_1;
		exLicenses2 = Arrays.copyOf(orig2, orig2.length+4);
		
		SPDXNonStandardLicense lic1_2_diff_licenref = new SPDXNonStandardLicense(
				id1_2, text1, name1, crossReff2, comment1);
		exLicenses2[orig2.length+0] = lic3_2;
		exLicenses2[orig2.length+1] = lic1_2_diff_licenref;
		exLicenses2[orig2.length+2] = lic4_2;
		exLicenses2[orig2.length+3] = lic2_2;

		doc1.setExtractedLicenseInfos(exLicenses1);
		doc2.setExtractedLicenseInfos(exLicenses2);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isExtractedLicensingInfosEqual());
		assertTrue(comparer.isDifferenceFound());		
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#getUniqueReviewers(int, int)}.
	 */
	@Test
	public void testGetUniqueReviewers() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);

		String reviewDate1 = "2010-02-03T00:00:00Z";
		String reviewerComment1 = "comment1";
		String reviewer1 = "Person: creator1";
		SPDXReview review1 = new SPDXReview(reviewer1, reviewDate1, reviewerComment1);
		String reviewDate2 = "2010-02-03T10:00:00Z";
		String reviewerComment2 = "comment2";
		String reviewer2 = "Person: creator2";
		SPDXReview review2 = new SPDXReview(reviewer2, reviewDate2, reviewerComment2);
		String reviewDate3 = "2011-02-13T00:00:00Z";
		String reviewerComment3 = "comment3";
		String reviewer3 = "Person: creator3";
		SPDXReview review3 = new SPDXReview(reviewer3, reviewDate3, reviewerComment3);
		SPDXReview[] reviewers3 = new SPDXReview[] {review1, review2, review3};
		SPDXReview[] reviewers2 = new SPDXReview[] {review2, review3};
		SPDXReview[] reviewers1 = new SPDXReview[] {review1};
		HashSet<SPDXReview> in3not1 = new HashSet<SPDXReview>();
		in3not1.add(review2);
		in3not1.add(review3);
		HashSet<SPDXReview> in3 = new HashSet<SPDXReview>();
		for (int i = 0; i < reviewers3.length; i++) {
			in3.add(reviewers3[i]);
		}
		SPDXReview[] emptyreviewers = new SPDXReview[0];
		
		// same reviewers
		doc1.setReviewers(reviewers3);
		doc2.setReviewers(reviewers3);
		comparer.compare(doc1, doc2);
		SPDXReview[] result = comparer.getUniqueReviewers(0, 1);
		assertEquals(0, result.length);
		result = comparer.getUniqueReviewers(1, 0);
		assertEquals(0, result.length);
		
		// more reviewers in 1
		doc1.setReviewers(reviewers3);
		doc2.setReviewers(reviewers1);
		comparer.compare(doc1, doc2);
		result = comparer.getUniqueReviewers(0, 1);
		assertEquals(2, result.length);
		assertTrue(in3not1.contains(result[0]));
		assertTrue(in3not1.contains(result[1]));
		result = comparer.getUniqueReviewers(1, 0);
		assertEquals(0, result.length);
		// more reviewers in 2
		doc1.setReviewers(reviewers2);
		doc2.setReviewers(reviewers3);
		comparer.compare(doc1, doc2);
		result = comparer.getUniqueReviewers(0, 1);
		assertEquals(0, result.length);
		result = comparer.getUniqueReviewers(1, 0);
		assertEquals(1, result.length);
		assertEquals(reviewer1, result[0].getReviewer());
		
		// empty reviewers
		doc1.setReviewers(reviewers3);
		doc2.setReviewers(emptyreviewers);
		comparer.compare(doc1, doc2);
		result = comparer.getUniqueReviewers(0, 1);
		assertEquals(3, result.length);
		for (int i = 0; i < result.length; i++) {
			assertTrue(in3.contains(result[i]));
		}
		
		// different reviewer comment
		SPDXReview review1diffComment = new SPDXReview(reviewer1, reviewDate1, "different comment");
		SPDXReview[] review3diffcomment = new SPDXReview[] {review1diffComment, review2, review3};
		doc1.setReviewers(review3diffcomment);
		doc2.setReviewers(reviewers3);
		comparer.compare(doc1, doc2);
		result = comparer.getUniqueReviewers(0, 1);
		assertEquals(0, result.length);
		result = comparer.getUniqueReviewers(1, 0);
		assertEquals(0, result.length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#getReviewerDifferences(int, int)}.
	 */
	@Test
	public void testGetReviewerDifferences() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);

		String reviewDate1 = "2010-02-03T00:00:00Z";
		String reviewerComment1 = "comment1";
		String reviewer1 = "Person: creator1";
		SPDXReview review1 = new SPDXReview(reviewer1, reviewDate1, reviewerComment1);
		String reviewDate2 = "2010-02-03T10:00:00Z";
		String reviewerComment2 = "comment2";
		String reviewer2 = "Person: creator2";
		SPDXReview review2 = new SPDXReview(reviewer2, reviewDate2, reviewerComment2);
		String reviewDate3 = "2011-02-13T00:00:00Z";
		String reviewerComment3 = "comment3";
		String reviewer3 = "Person: creator3";
		SPDXReview review3 = new SPDXReview(reviewer3, reviewDate3, reviewerComment3);
		SPDXReview[] reviewers3 = new SPDXReview[] {review1, review2, review3};

		// no changes
		doc1.setReviewers(reviewers3);
		doc2.setReviewers(reviewers3);
		comparer.compare(doc1, doc2);
		SPDXReviewDifference[] result = comparer.getReviewerDifferences(0, 1);
		assertEquals(0, result.length);
		result = comparer.getReviewerDifferences(0, 1);
		assertEquals(0, result.length);
		
		// different comment and date
		String diffDate1 = "2001-02-13T010:00:00Z";
		String diffComment1 = "diff rev 1 comment";
		SPDXReview review1diffCommentAndDate = new SPDXReview(reviewer1, diffDate1, diffComment1);
		String diffComment2 = "different comment";
		SPDXReview review2diffcomment = new SPDXReview(reviewer2, reviewDate2, diffComment2);
		String diffdate3 = "2001-02-13T00:00:00Z";
		SPDXReview review3diffDate = new SPDXReview(reviewer3, diffdate3, reviewerComment3);
		SPDXReview[] diffReviewers = new SPDXReview[] {review1diffCommentAndDate, review2diffcomment, review3diffDate};
		
		doc1.setReviewers(reviewers3);
		doc2.setReviewers(diffReviewers);
		comparer.compare(doc1, doc2);
		result = comparer.getReviewerDifferences(0, 1);
		assertEquals(3, result.length);
		boolean foundrev1 = false;
		boolean foundrev2 = false;
		boolean foundrev3 = false;
		for (int i = 0; i < result.length; i++) {
			if (result[i].getReviewer().equals(reviewer1)) {
				assertFalse(result[i].isCommentEqual());
				assertEquals(reviewerComment1, result[i].getComment(0));
				assertEquals(diffComment1, result[i].getComment(1));
				assertFalse(result[i].isDateEqual());
				assertEquals(reviewDate1, result[i].getDate(0));
				assertEquals(diffDate1, result[i].getDate(1));
				foundrev1 = true;
			} else if (result[i].getReviewer().equals(reviewer2)) {
				assertFalse(result[i].isCommentEqual());
				assertEquals(reviewerComment2, result[i].getComment(0));
				assertEquals(diffComment2, result[i].getComment(1));
				assertTrue(result[i].isDateEqual());
				assertEquals(reviewDate2, result[i].getDate(0));
				assertEquals(reviewDate2, result[i].getDate(1));
				foundrev2 = true;
			} else if (result[i].getReviewer().equals(reviewer3)) {
				assertTrue(result[i].isCommentEqual());
				assertEquals(reviewerComment3, result[i].getComment(0));
				assertEquals(reviewerComment3, result[i].getComment(1));
				assertFalse(result[i].isDateEqual());
				assertEquals(reviewDate3, result[i].getDate(0));
				assertEquals(diffdate3, result[i].getDate(1));
				foundrev3 = true;
			}
		}
		assertTrue(foundrev1);
		assertTrue(foundrev2);
		assertTrue(foundrev3);
		result = comparer.getReviewerDifferences(1, 0);
		for (int i = 0; i < result.length; i++) {
			if (result[i].getReviewer().equals(reviewer1)) {
				assertFalse(result[i].isCommentEqual());
				assertEquals(diffComment1, result[i].getComment(0));
				assertEquals(reviewerComment1, result[i].getComment(1));
				assertFalse(result[i].isDateEqual());
				assertEquals(diffDate1, result[i].getDate(0));
				assertEquals(reviewDate1, result[i].getDate(1));
				foundrev1 = true;
			} else if (result[i].getReviewer().equals(reviewer2)) {
				assertFalse(result[i].isCommentEqual());
				assertEquals(diffComment2, result[i].getComment(0));
				assertEquals(reviewerComment2, result[i].getComment(1));
				assertTrue(result[i].isDateEqual());
				assertEquals(reviewDate2, result[i].getDate(0));
				assertEquals(reviewDate2, result[i].getDate(1));
				foundrev2 = true;
			} else if (result[i].getReviewer().equals(reviewer3)) {
				assertTrue(result[i].isCommentEqual());
				assertEquals(reviewerComment3, result[i].getComment(0));
				assertEquals(reviewerComment3, result[i].getComment(1));
				assertFalse(result[i].isDateEqual());
				assertEquals(diffdate3, result[i].getDate(0));
				assertEquals(reviewDate3, result[i].getDate(1));
				foundrev3 = true;
			}
		}
		assertTrue(foundrev1);
		assertTrue(foundrev2);
		assertTrue(foundrev3);
		
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#getUniqueExtractedLicenses(int, int)}.
	 */
	@Test
	public void testGetUniqueExtractedLicenses() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		SPDXNonStandardLicense[] orig1 = doc1.getExtractedLicenseInfos();
		SPDXNonStandardLicense[] orig2 = doc2.getExtractedLicenseInfos();
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isExtractedLicensingInfosEqual());
		assertFalse(comparer.isDifferenceFound());

		int doc1id = 100;
		int doc2id = 200;
		String id1_1 = SPDXDocument.formNonStandardLicenseID(doc1id++);
		String text1 = "License text 1";
		String name1 = "licname1";
		String[] crossReff1 = new String[] {"http://cross.ref.one"};
		String comment1 = "comment1";
		SPDXNonStandardLicense lic1_1 = new SPDXNonStandardLicense(
				id1_1, text1, name1, crossReff1, comment1);
		String id1_2 = SPDXDocument.formNonStandardLicenseID(doc2id++);
		SPDXNonStandardLicense lic1_2 = new SPDXNonStandardLicense(
				id1_2, text1, name1, crossReff1, comment1);
		
		String id2_1 = SPDXDocument.formNonStandardLicenseID(doc1id++);
		String text2 = "License text 2";
		String name2 = "licname2";
		String[] crossReff2 = new String[] {"http://cross.ref.one", "http://cross.ref.two"};
		String comment2 = "comment2";
		SPDXNonStandardLicense lic2_1 = new SPDXNonStandardLicense(
				id2_1, text2, name2, crossReff2, comment2);
		String id2_2 = SPDXDocument.formNonStandardLicenseID(doc2id++);
		SPDXNonStandardLicense lic2_2 = new SPDXNonStandardLicense(
				id2_2, text2, name2, crossReff2, comment2);
		
		String id3_1 = SPDXDocument.formNonStandardLicenseID(doc1id++);
		String text3 = "License text 3";
		String name3 = "";
		String[] crossReff3 = new String[] {};
		String comment3 = "comment3";
		SPDXNonStandardLicense lic3_1 = new SPDXNonStandardLicense(
				id3_1, text3, name3, crossReff3, comment3);
		String id3_2 = SPDXDocument.formNonStandardLicenseID(doc2id++);
		SPDXNonStandardLicense lic3_2 = new SPDXNonStandardLicense(
				id3_2, text3, name3, crossReff3, comment3);

		String id4_1 = SPDXDocument.formNonStandardLicenseID(doc1id++);
		String text4 = "License text 4";
		String name4 = "";
		String[] crossReff4 = new String[] {};
		String comment4 = "";
		SPDXNonStandardLicense lic4_1 = new SPDXNonStandardLicense(
				id4_1, text4, name4, crossReff4, comment4);
		String id4_2 = SPDXDocument.formNonStandardLicenseID(doc2id++);
		SPDXNonStandardLicense lic4_2 = new SPDXNonStandardLicense(
				id4_2, text4, name4, crossReff4, comment4);

		// same licenses, different order
		SPDXNonStandardLicense[] exLicenses1 = Arrays.copyOf(orig1, orig1.length+4);
		exLicenses1[orig1.length+0] = lic1_1;
		exLicenses1[orig1.length+1] = lic2_1;
		exLicenses1[orig1.length+2] = lic3_1;
		exLicenses1[orig1.length+3] = lic4_1;
		SPDXNonStandardLicense[] exLicenses2 = Arrays.copyOf(orig2, orig2.length+4);
		exLicenses2[orig2.length+0] = lic3_2;
		exLicenses2[orig2.length+1] = lic1_2;
		exLicenses2[orig2.length+2] = lic4_2;
		exLicenses2[orig2.length+3] = lic2_2;
		doc1.setExtractedLicenseInfos(exLicenses1);
		doc2.setExtractedLicenseInfos(exLicenses2);
		comparer.compare(doc1, doc2);
		SPDXNonStandardLicense[] result = comparer.getUniqueExtractedLicenses(0, 1);
		assertEquals(0, result.length);
		result = comparer.getUniqueExtractedLicenses(1, 0);
		assertEquals(0, result.length);

		// More licenses in doc1
		exLicenses1 = Arrays.copyOf(orig1, orig1.length+4);
		exLicenses1[orig1.length+0] = lic1_1;
		exLicenses1[orig1.length+1] = lic2_1;
		exLicenses1[orig1.length+2] = lic3_1;
		exLicenses1[orig1.length+3] = lic4_1;
		exLicenses2 = Arrays.copyOf(orig2, orig2.length+2);
		exLicenses2[orig2.length+0] = lic3_2;
		exLicenses2[orig2.length+1] = lic1_2;
		HashSet<String> uniqueLicIds = new HashSet<String>();
		uniqueLicIds.add(lic2_1.getId());
		uniqueLicIds.add(lic4_1.getId());

		doc1.setExtractedLicenseInfos(exLicenses1);
		doc2.setExtractedLicenseInfos(exLicenses2);
		comparer.compare(doc1, doc2);
		result = comparer.getUniqueExtractedLicenses(0, 1);
		assertEquals(2, result.length);
		assertTrue(uniqueLicIds.contains(result[0].getId()));
		assertTrue(uniqueLicIds.contains(result[1].getId()));
		result = comparer.getUniqueExtractedLicenses(1, 0);
		assertEquals(0, result.length);
		
		// more licenses in doc2
		exLicenses1 = Arrays.copyOf(orig1, orig1.length);

		exLicenses2 = Arrays.copyOf(orig2, orig2.length+4);
		exLicenses2[orig2.length+0] = lic3_2;
		exLicenses2[orig2.length+1] = lic1_2;
		exLicenses2[orig2.length+2] = lic4_2;
		exLicenses2[orig2.length+3] = lic2_2;
		uniqueLicIds.clear();
		uniqueLicIds.add(lic3_2.getId());
		uniqueLicIds.add(lic1_2.getId());		
		uniqueLicIds.add(lic4_2.getId());
		uniqueLicIds.add(lic2_2.getId());
		
		doc1.setExtractedLicenseInfos(exLicenses1);
		doc2.setExtractedLicenseInfos(exLicenses2);
		comparer.compare(doc1, doc2);
		result = comparer.getUniqueExtractedLicenses(0, 1);
		assertEquals(0, result.length);
		result = comparer.getUniqueExtractedLicenses(1, 0);
		assertEquals(4, result.length);
		assertTrue(uniqueLicIds.contains(result[0].getId()));
		assertTrue(uniqueLicIds.contains(result[1].getId()));
		assertTrue(uniqueLicIds.contains(result[2].getId()));
		assertTrue(uniqueLicIds.contains(result[3].getId()));
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#getExtractedLicenseDifferences(int, int)}.
	 */
	@Test
	public void testGetExtractedLicenseDifferences() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		SPDXNonStandardLicense[] orig1 = doc1.getExtractedLicenseInfos();
		SPDXNonStandardLicense[] orig2 = doc2.getExtractedLicenseInfos();
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isExtractedLicensingInfosEqual());
		assertFalse(comparer.isDifferenceFound());

		int doc1id = 100;
		int doc2id = 200;
		String id1_1 = SPDXDocument.formNonStandardLicenseID(doc1id++);
		String text1 = "License text 1";
		String name1 = "licname1";
		String[] crossReff1 = new String[] {"http://cross.ref.one"};
		String comment1 = "comment1";
		SPDXNonStandardLicense lic1_1 = new SPDXNonStandardLicense(
				id1_1, text1, name1, crossReff1, comment1);
		String id1_2 = SPDXDocument.formNonStandardLicenseID(doc2id++);
		SPDXNonStandardLicense lic1_2 = new SPDXNonStandardLicense(
				id1_2, text1, name1, crossReff1, comment1);
		
		String id2_1 = SPDXDocument.formNonStandardLicenseID(doc1id++);
		String text2 = "License text 2";
		String name2 = "licname2";
		String[] crossReff2 = new String[] {"http://cross.ref.one", "http://cross.ref.two"};
		String comment2 = "comment2";
		SPDXNonStandardLicense lic2_1 = new SPDXNonStandardLicense(
				id2_1, text2, name2, crossReff2, comment2);
		String id2_2 = SPDXDocument.formNonStandardLicenseID(doc2id++);
		SPDXNonStandardLicense lic2_2 = new SPDXNonStandardLicense(
				id2_2, text2, name2, crossReff2, comment2);
		
		String id3_1 = SPDXDocument.formNonStandardLicenseID(doc1id++);
		String text3 = "License text 3";
		String name3 = "name3";
		String[] crossReff3 = new String[] {"http://crossref3.org"};
		String comment3 = "comment3";
		SPDXNonStandardLicense lic3_1 = new SPDXNonStandardLicense(
				id3_1, text3, name3, crossReff3, comment3);
		String id3_2 = SPDXDocument.formNonStandardLicenseID(doc2id++);
		SPDXNonStandardLicense lic3_2 = new SPDXNonStandardLicense(
				id3_2, text3, name3, crossReff3, comment3);

		String id4_1 = SPDXDocument.formNonStandardLicenseID(doc1id++);
		String text4 = "License text 4";
		String name4 = "";
		String[] crossReff4 = new String[] {};
		String comment4 = "";
		SPDXNonStandardLicense lic4_1 = new SPDXNonStandardLicense(
				id4_1, text4, name4, crossReff4, comment4);
		String id4_2 = SPDXDocument.formNonStandardLicenseID(doc2id++);
		SPDXNonStandardLicense lic4_2 = new SPDXNonStandardLicense(
				id4_2, text4, name4, crossReff4, comment4);

		// same licenses, different order
		SPDXNonStandardLicense[] exLicenses1 = Arrays.copyOf(orig1, orig1.length+4);
		exLicenses1[orig1.length+0] = lic1_1;
		exLicenses1[orig1.length+1] = lic2_1;
		exLicenses1[orig1.length+2] = lic3_1;
		exLicenses1[orig1.length+3] = lic4_1;
		SPDXNonStandardLicense[] exLicenses2 = Arrays.copyOf(orig2, orig2.length+4);
		exLicenses2[orig2.length+0] = lic3_2;
		exLicenses2[orig2.length+1] = lic1_2;
		exLicenses2[orig2.length+2] = lic4_2;
		exLicenses2[orig2.length+3] = lic2_2;
		doc1.setExtractedLicenseInfos(exLicenses1);
		doc2.setExtractedLicenseInfos(exLicenses2);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isExtractedLicensingInfosEqual());
		assertFalse(comparer.isDifferenceFound());
		SpdxLicenseDifference[] result = comparer.getExtractedLicenseDifferences(0, 1);
		assertEquals(0, result.length);
		result = comparer.getExtractedLicenseDifferences(1, 0);
		assertEquals(0, result.length);
		
		// differences
		exLicenses1 = Arrays.copyOf(orig1, orig1.length+4);
		exLicenses1[orig1.length+0] = lic1_1;
		exLicenses1[orig1.length+1] = lic2_1;
		exLicenses1[orig1.length+2] = lic3_1;
		exLicenses1[orig1.length+3] = lic4_1;
		exLicenses2 = Arrays.copyOf(orig2, orig2.length+4);
		
		String differentName = "differentLicenseName";
		SPDXNonStandardLicense lic1_2_diff_name = new SPDXNonStandardLicense(
				id1_2, text1, differentName, crossReff1, comment1);
		String differentComment = "different comment";
		SPDXNonStandardLicense lic2_2_diff_Comment = new SPDXNonStandardLicense(
				id2_2, text2, name2, crossReff2, differentComment);
		SPDXNonStandardLicense lic3_2_diff_licenref = new SPDXNonStandardLicense(
				id3_2, text3, name3, crossReff2, comment3);
		exLicenses2[orig2.length+0] = lic3_2_diff_licenref;
		exLicenses2[orig2.length+1] = lic1_2_diff_name;
		exLicenses2[orig2.length+2] = lic4_2;
		exLicenses2[orig2.length+3] = lic2_2_diff_Comment;

		doc1.setExtractedLicenseInfos(exLicenses1);
		doc2.setExtractedLicenseInfos(exLicenses2);
		comparer.compare(doc1, doc2);
		
		result = comparer.getExtractedLicenseDifferences(0,1);
		assertEquals(3, result.length);
		boolean lic1Found = false;
		boolean lic2Found = false;
		boolean lic3Found = false;
		for (int i = 0; i < result.length; i++) {
			if (result[i].getIdA().equals(lic2_1.getId())) {
				lic2Found = true;
				assertEquals(lic2_2.getId(), result[i].getIdB());
				assertEquals(comment2, result[i].getCommentA());
				assertEquals(differentComment, result[i].getCommentB());
				assertEquals(name2, result[i].getLicenseNameA());
				assertEquals(name2, result[i].getLicenseNameB());
				assertEquals(text2, result[i].getLicenseText());
				assertTrue(stringsSame(crossReff2, result[i].getSourceUrlsA()));
				assertTrue(stringsSame(crossReff2, result[i].getSourceUrlsB()));
			} else if (result[i].getIdA().equals(lic1_1.getId())) {
				lic1Found = true;
				assertEquals(lic1_2.getId(), result[i].getIdB());
				assertEquals(comment1, result[i].getCommentA());
				assertEquals(comment1, result[i].getCommentB());
				assertEquals(name1, result[i].getLicenseNameA());
				assertEquals(differentName, result[i].getLicenseNameB());
				assertEquals(text1, result[i].getLicenseText());
				assertTrue(stringsSame(crossReff1, result[i].getSourceUrlsA()));
				assertTrue(stringsSame(crossReff1, result[i].getSourceUrlsB()));
			}else if (result[i].getIdA().equals(lic3_1.getId())) {
				lic3Found = true;
				assertEquals(lic3_2.getId(), result[i].getIdB());
				assertEquals(comment3, result[i].getCommentA());
				assertEquals(comment3, result[i].getCommentB());
				assertEquals(name3, result[i].getLicenseNameA());
				assertEquals(name3, result[i].getLicenseNameB());
				assertEquals(text3, result[i].getLicenseText());
				assertTrue(stringsSame(crossReff3, result[i].getSourceUrlsA()));
				assertTrue(stringsSame(crossReff2, result[i].getSourceUrlsB()));
			}
		}
		assertTrue(lic1Found);
		assertTrue(lic2Found);
		assertTrue(lic3Found);
		
		result = comparer.getExtractedLicenseDifferences(1, 0);
		assertEquals(3, result.length);
		for (int i = 0; i < result.length; i++) {
			if (result[i].getIdB().equals(lic2_2.getId())) {
				lic2Found = true;
				assertEquals(lic2_1.getId(), result[i].getIdB());
				assertEquals(differentComment, result[i].getCommentA());
				assertEquals(comment2, result[i].getCommentB());
				assertEquals(name2, result[i].getLicenseNameA());
				assertEquals(name2, result[i].getLicenseNameB());
				assertEquals(text2, result[i].getLicenseText());
				assertTrue(stringsSame(crossReff2, result[i].getSourceUrlsA()));
				assertTrue(stringsSame(crossReff2, result[i].getSourceUrlsB()));
			} else if (result[i].getIdA().equals(lic1_2.getId())) {
				lic1Found = true;
				assertEquals(lic1_1.getId(), result[i].getIdB());
				assertEquals(comment1, result[i].getCommentA());
				assertEquals(comment1, result[i].getCommentB());
				assertEquals(differentName, result[i].getLicenseNameA());
				assertEquals(name1, result[i].getLicenseNameB());
				assertEquals(text1, result[i].getLicenseText());
				assertTrue(stringsSame(crossReff1, result[i].getSourceUrlsA()));
				assertTrue(stringsSame(crossReff1, result[i].getSourceUrlsB()));
			}else if (result[i].getIdA().equals(lic3_2.getId())) {
				lic3Found = true;
				assertEquals(lic3_1.getId(), result[i].getIdB());
				assertEquals(comment3, result[i].getCommentA());
				assertEquals(comment3, result[i].getCommentB());
				assertEquals(name3, result[i].getLicenseNameA());
				assertEquals(name3, result[i].getLicenseNameB());
				assertEquals(text3, result[i].getLicenseText());
				assertTrue(stringsSame(crossReff2, result[i].getSourceUrlsA()));
				assertTrue(stringsSame(crossReff3, result[i].getSourceUrlsB()));
			}
		}
		assertTrue(lic1Found);
		assertTrue(lic2Found);
		assertTrue(lic3Found);
	}

	/**
	 * Compare two arrays or strings
	 * @param stringsA
	 * @param stringsB
	 * @return true if arrays contain the same strings independant of order
	 */
	private boolean stringsSame(String[] stringsA, String[] stringsB) {
		if (stringsA == null) {
			return (stringsB == null);
		}
		if (stringsB == null) {
			return false;
		}
		if (stringsA.length != stringsB.length) {
			return false;
		}
		HashSet<String> sset = new HashSet<String>();
		for (int i = 0; i < stringsA.length; i++) {
			sset.add(stringsA[i]);
		}
		for (int i = 0; i < stringsB.length; i++) {
			if (!sset.contains(stringsB[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isPackageEqual()}.
	 */
	@Test
	public void testIsPackageEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackageEqual());
		assertFalse(comparer.isDifferenceFound());
		
		doc1.getSpdxPackage().setDescription("Different Description");
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
		// note - other test cases will test to make sure isPackageEquals is set for all changes where it should be false
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isPackageNamesEqual()}.
	 */
	@Test
	public void testIsPackageNamesEqual()  throws IOException, InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackageEqual());
		assertTrue(comparer.isPackageNamesEqual());
		assertFalse(comparer.isDifferenceFound());
		
		doc2.getSpdxPackage().setDeclaredName("New Package Name");
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isPackageNamesEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isPackageVersionsEqual()}.
	 */
	@Test
	public void testIsPackageVersionsEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackageEqual());
		assertTrue(comparer.isPackageVersionsEqual());
		assertFalse(comparer.isDifferenceFound());
		
		doc2.getSpdxPackage().setVersionInfo("2.2.2");
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isPackageVersionsEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isPackageLicenseInfoFromFilesEqual()}.
	 */
	@Test
	public void testIsPackageLicenseInfoFromFilesEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackageEqual());
		assertTrue(comparer.isPackageVersionsEqual());
		assertFalse(comparer.isDifferenceFound());
		
		// one additional license
		SPDXLicenseInfo[] orig = doc1.getSpdxPackage().getLicenseInfoFromFiles();
		SPDXLicenseInfo[] addOne = Arrays.copyOf(orig, orig.length+1);
		addOne[orig.length] = SPDXLicenseInfoFactory.getStandardLicenseById(STD_LIC_ID_CC0);
		doc1.getSpdxPackage().setLicenseInfoFromFiles(addOne);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isPackageLicenseInfoFromFilesEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
		
		if (orig[0] instanceof SPDXStandardLicense &&
				((SPDXStandardLicense)orig[0]).getId() == STD_LIC_ID_CC0) {
			orig[0] = SPDXLicenseInfoFactory.getStandardLicenseById(STD_LIC_ID_MPL11);
		} else {
			orig[0] = SPDXLicenseInfoFactory.getStandardLicenseById(STD_LIC_ID_CC0);
		}
		doc1.getSpdxPackage().setLicenseInfoFromFiles(orig);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isPackageLicenseInfoFromFilesEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isPackageFileNamesEqual()}.
	 */
	@Test
	public void testIsPackageFileNamesEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackageEqual());
		assertTrue(comparer.isPackageFileNamesEqual());
		assertFalse(comparer.isDifferenceFound());
		
		doc2.getSpdxPackage().setFileName("NewFileName.tar.gz");
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isPackageFileNamesEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isPackageSuppliersEqual()}.
	 */
	@Test
	public void testIsPackageSuppliersEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackageEqual());
		assertTrue(comparer.isPackageSuppliersEqual());
		assertFalse(comparer.isDifferenceFound());
		
		doc2.getSpdxPackage().setSupplier("Person: New supplier");
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isPackageSuppliersEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isPackageOriginatorsEqual()}.
	 */
	@Test
	public void testIsPackageOriginatorsEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackageEqual());
		assertTrue(comparer.isPackageOriginatorsEqual());
		assertFalse(comparer.isDifferenceFound());
		
		doc2.getSpdxPackage().setOriginator("Person: New originator");
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isPackageOriginatorsEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isPackageDownloadLocationsEqual()}.
	 */
	@Test
	public void testIsPackageDownloadLocationsEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackageEqual());
		assertTrue(comparer.isPackageDownloadLocationsEqual());
		assertFalse(comparer.isDifferenceFound());
		
		doc2.getSpdxPackage().setDownloadUrl("http://newdownloadurl.org");
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isPackageDownloadLocationsEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
	}
	
	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isPackageHomePagesEqual()}.
	 */
	@Test
	public void testIsPackageHomePagesEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackageEqual());
		assertTrue(comparer.ispackageHomePagesEqual());
		assertFalse(comparer.isDifferenceFound());
		
		doc2.getSpdxPackage().setHomePage("http://different/home/page");
		comparer.compare(doc1, doc2);
		assertFalse(comparer.ispackageHomePagesEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isPackageVerificationCodesEqual()}.
	 */
	@Test
	public void testIsPackageVerificationCodesEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackageEqual());
		assertTrue(comparer.isPackageVerificationCodesEqual());
		assertFalse(comparer.isDifferenceFound());
		SpdxPackageVerificationCode oriVc =doc1.getSpdxPackage().getVerificationCode();
		// different value
		SpdxPackageVerificationCode newVc = new SpdxPackageVerificationCode("e6eec0619e2a58d2f5100e5c8772b56f058cbc58",
				oriVc.getExcludedFileNames());
		doc1.getSpdxPackage().setVerificationCode(newVc);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isPackageVerificationCodesEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
		
		// different excluded files
		String[] excludedFiles = new String[] {"excluded/file/one.c", "exlucded2.c"};
		newVc = new SpdxPackageVerificationCode(oriVc.getValue(),excludedFiles);
		doc1.getSpdxPackage().setVerificationCode(newVc);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isPackageVerificationCodesEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isPackageChecksumsEqual()}.
	 */
	@Test
	public void testIsPackageChecksumsEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackageEqual());
		assertTrue(comparer.isPackageChecksumsEqual());
		assertFalse(comparer.isDifferenceFound());
		
		doc2.getSpdxPackage().setSha1("e6eec0619e2a58d2f5100e5c8772b56f058cbc58");
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isPackageChecksumsEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isSourceInformationEqual()}.
	 */
	@Test
	public void testIsSourceInformationEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackageEqual());
		assertTrue(comparer.isSourceInformationEqual());
		assertFalse(comparer.isDifferenceFound());
		
		doc2.getSpdxPackage().setSourceInfo("New source info");
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isSourceInformationEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isPackageDeclaredLicensesEqual()}.
	 */
	@Test
	public void testIsPackageDeclaredLicensesEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackageEqual());
		assertTrue(comparer.isPackageDeclaredLicensesEqual());
		assertFalse(comparer.isDifferenceFound());
		
		SPDXStandardLicense lic = SPDXLicenseInfoFactory.getStandardLicenseById(STD_LIC_ID_CC0);
		doc2.getSpdxPackage().setDeclaredLicense(lic);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isPackageDeclaredLicensesEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isPackageConcludedLicensesEqual()}.
	 */
	@Test
	public void testIsPackageConcludedLicensesEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackageEqual());
		assertTrue(comparer.isPackageConcludedLicensesEqual());
		assertFalse(comparer.isDifferenceFound());
		
		SPDXStandardLicense lic = SPDXLicenseInfoFactory.getStandardLicenseById(STD_LIC_ID_CC0);
		doc2.getSpdxPackage().setConcludedLicenses(lic);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isPackageConcludedLicensesEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isLicenseCommentsEqual()}.
	 */
	@Test
	public void testIsLicenseCommentsEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackageEqual());
		assertTrue(comparer.isLicenseCommentsEqual());
		assertFalse(comparer.isDifferenceFound());
		
		doc2.getSpdxPackage().setLicenseComment("New license Comment");
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isLicenseCommentsEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isCopyrightTextsEqual()}.
	 */
	@Test
	public void testIsCopyrightTextsEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackageEqual());
		assertTrue(comparer.isCopyrightTextsEqual());
		assertFalse(comparer.isDifferenceFound());
		
		doc2.getSpdxPackage().setDeclaredCopyright("New copyright text");
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isCopyrightTextsEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isPackageSummariesEqual()}.
	 */
	@Test
	public void testIsPackageSummariesEqual()  throws IOException, InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackageEqual());
		assertTrue(comparer.isPackageSummariesEqual());
		assertFalse(comparer.isDifferenceFound());
		
		doc2.getSpdxPackage().setShortDescription("New summary");
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isPackageSummariesEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isPackageDescriptionsEqual()}.
	 */
	@Test
	public void testIsPackageDescriptionsEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackageEqual());
		assertTrue(comparer.isPackageDescriptionsEqual());
		assertFalse(comparer.isDifferenceFound());
		
		doc2.getSpdxPackage().setDescription("New description");
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isPackageDescriptionsEqual());
		assertFalse(comparer.isPackageEqual());
		assertTrue(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isCreatorInformationEqual()}.
	 */
	@Test
	public void testIsCreatorInformationEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isCreatorInformationEqual());
		assertFalse(comparer.isDifferenceFound());

		// one more creator
		SPDXCreatorInformation origCreators = doc1.getCreatorInfo();
		String[] oneMore = Arrays.copyOf(origCreators.getCreators(), origCreators.getCreators().length+1);
		oneMore[oneMore.length-1] = "Person: One More Person";
		SPDXCreatorInformation updatedCreators = new SPDXCreatorInformation(
				oneMore, origCreators.getCreated(), origCreators.getComment(), origCreators.getLicenseListVersion());
		doc1.setCreationInfo(updatedCreators);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isCreatorInformationEqual());
		assertTrue(comparer.isDifferenceFound());
		
		// different creator
		String[] creatorarray = Arrays.copyOf(origCreators.getCreators(), origCreators.getCreators().length);
		creatorarray[0] = "Person: Different Person";
		updatedCreators = new SPDXCreatorInformation(
				creatorarray, origCreators.getCreated(), origCreators.getComment(), origCreators.getLicenseListVersion());
		doc1.setCreationInfo(updatedCreators);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isCreatorInformationEqual());
		assertTrue(comparer.isDifferenceFound());
		
		// Different creation date
		updatedCreators = new SPDXCreatorInformation(
				origCreators.getCreators(), "2013-02-03T00:00:00Z", origCreators.getComment(), origCreators.getLicenseListVersion());
		doc1.setCreationInfo(updatedCreators);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isCreatorInformationEqual());
		assertTrue(comparer.isDifferenceFound());
		
		// different comment
		updatedCreators = new SPDXCreatorInformation(
				origCreators.getCreators(), origCreators.getCreated(), "Different Comment", origCreators.getLicenseListVersion());
		doc1.setCreationInfo(updatedCreators);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isCreatorInformationEqual());
		assertTrue(comparer.isDifferenceFound());
		
		// different license list version
		updatedCreators = new SPDXCreatorInformation(
				origCreators.getCreators(), origCreators.getCreated(), origCreators.getLicenseListVersion(), "1.25");
		doc1.setCreationInfo(updatedCreators);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isCreatorInformationEqual());
		assertTrue(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#getUniqueCreators(int, int)}.
	 */
	@Test
	public void testGetUniqueCreators() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isCreatorInformationEqual());
		assertFalse(comparer.isDifferenceFound());
		String[] result = comparer.getUniqueCreators(0, 1);
		assertEquals(0, result.length);
		result = comparer.getUniqueCreators(1, 0);
		assertEquals(0, result.length);		
		
		// different order of creators
		String creator1 = "Person: Creator1";
		String creator2 = "Organization: Creator2";
		String creator3 = "Tool: Creator3";
		String createdDate = "2013-02-03T00:00:00Z";
		String creatorComment = "Creator comments";
		String licenseListVersion = "1.19";
		String[] creators1 = new String[] {creator1, creator2, creator3};
		String[] creators2 = new String[] {creator3, creator2, creator1};	
		SPDXCreatorInformation creationInfo1 = new SPDXCreatorInformation(
				creators1, createdDate, creatorComment, licenseListVersion);
		SPDXCreatorInformation creationInfo2 = new SPDXCreatorInformation(
				creators2, createdDate, creatorComment, licenseListVersion);
		doc1.setCreationInfo(creationInfo1);
		doc2.setCreationInfo(creationInfo2);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isCreatorInformationEqual());
		assertFalse(comparer.isDifferenceFound());
		result = comparer.getUniqueCreators(0, 1);
		assertEquals(0, result.length);
		result = comparer.getUniqueCreators(1, 0);
		assertEquals(0, result.length);		
	
		// more results in the first
		creators1 = new String[] {creator1, creator2, creator3};
		creators2 = new String[] {creator1};
		creationInfo1 = new SPDXCreatorInformation(
				creators1, createdDate, creatorComment, licenseListVersion);
		creationInfo2 = new SPDXCreatorInformation(
				creators2, createdDate, creatorComment, licenseListVersion);
		doc1.setCreationInfo(creationInfo1);
		doc2.setCreationInfo(creationInfo2);
		HashSet<String> additionalCreators = new HashSet<String>();
		additionalCreators.add(creator2);
		additionalCreators.add(creator3);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isCreatorInformationEqual());
		assertTrue(comparer.isDifferenceFound());
		result = comparer.getUniqueCreators(0, 1);
		assertEquals(additionalCreators.size(), result.length);
		for (int i = 0; i < result.length; i++) {
			assertTrue(additionalCreators.contains(result[i]));
		}
		result = comparer.getUniqueCreators(1, 0);
		assertEquals(0, result.length);	
		
		// more results in the second
		creators1 = new String[] {creator2, creator3};
		creators2 = new String[] {creator1, creator2, creator3};
		creationInfo1 = new SPDXCreatorInformation(
				creators1, createdDate, creatorComment, licenseListVersion);
		creationInfo2 = new SPDXCreatorInformation(
				creators2, createdDate, creatorComment, licenseListVersion);
		doc1.setCreationInfo(creationInfo1);
		doc2.setCreationInfo(creationInfo2);
		additionalCreators.clear();
		additionalCreators.add(creator1);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isCreatorInformationEqual());
		assertTrue(comparer.isDifferenceFound());
		result = comparer.getUniqueCreators(0, 1);
		assertEquals(0, result.length);	
		result = comparer.getUniqueCreators(1, 0);
		assertEquals(additionalCreators.size(), result.length);
		for (int i = 0; i < result.length; i++) {
			assertTrue(additionalCreators.contains(result[i]));
		}
	}

	@Test
	public void testGetFileDifferences() throws InvalidSPDXAnalysisException, IOException, SpdxCompareException, InvalidLicenseStringException {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		SpdxFileDifference[] differences = comparer.getFileDifferences(0, 1);
		assertEquals(0, differences.length);
		SPDXFile[] files = doc1.getSpdxPackage().getFiles();
		String file0Name = files[0].getName();
		files[0] = new SPDXFile(files[0].getName(), files[0].getType(), files[0].getSha1(),files[0].getConcludedLicenses(),
				files[0].getSeenLicenses(), files[0].getLicenseComments(), files[0].getCopyright(), files[0].getArtifactOf());
		files[0].setComment("a new and unique comment");
		String file1Name = files[1].getName();
		files[1] = new SPDXFile(files[1].getName(), files[1].getType(), files[1].getSha1(),files[1].getConcludedLicenses(),
				files[1].getSeenLicenses(), files[1].getLicenseComments(), files[1].getCopyright(), files[1].getArtifactOf());
		files[1].setConcludedLicenses(SPDXLicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0));
		doc2.getSpdxPackage().setFiles(files);
		comparer.compare(doc1, doc2);
		differences = comparer.getFileDifferences(0, 1);
		assertEquals(2, differences.length);
		if (differences[0].getFileName().equals(file0Name)) {
			assertFalse(differences[0].isCommentsEqual());
			assertEquals(differences[0].getCommentB(),files[0].getComment());
		} else if (differences[0].getFileName().equals(file1Name)) {
			assertFalse(differences[0].isCommentsEqual());
			assertEquals(((SPDXLicense)(files[1].getConcludedLicenses())).getId(), 
					(differences[0].getConcludedLicenseB()));
		} else {
			fail("invalid file name");
		}
		if (differences[1].getFileName().equals(file0Name)) {
			assertFalse(differences[1].isCommentsEqual());
			assertEquals(differences[1].getCommentB(),files[0].getComment());
		} else if (differences[1].getFileName().equals(file1Name)) {
			assertFalse(differences[1].isConcludedLicenseEquals());
			assertEquals(((SPDXLicense)(files[1].getConcludedLicenses())).getId(), differences[1].getConcludedLicenseB());
		} else {
			fail ("Invalid file name");
		}
		
		comparer.compare(doc2, doc1);
		differences = comparer.getFileDifferences(0, 1);
		assertEquals(2, differences.length);
		if (differences[0].getFileName().equals(file0Name)) {
			assertFalse(differences[0].isCommentsEqual());
			assertEquals(differences[0].getCommentA(),files[0].getComment());
		} else if (differences[0].getFileName().equals(file1Name)) {
			assertFalse(differences[0].isConcludedLicenseEquals());
			assertEquals(((SPDXLicense)(files[1].getConcludedLicenses())).getId(), differences[0].getConcludedLicenseA());
		} else {
			fail("invalid file name");
		}
		if (differences[1].getFileName().equals(file0Name)) {
			assertFalse(differences[1].isCommentsEqual());
			assertEquals(differences[1].getCommentA(),files[0].getComment());
		} else if (differences[1].getFileName().equals(file1Name)) {
			assertFalse(differences[1].isConcludedLicenseEquals());
			assertEquals(((SPDXLicense)(files[1].getConcludedLicenses())).getId(), differences[1].getConcludedLicenseA());
		} else {
			fail ("Invalid file name");
		}
	}
	
	@Test
	public void testGetUniqueFiles() throws InvalidSPDXAnalysisException, IOException, SpdxCompareException {
		SpdxComparer comparer = new SpdxComparer();
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		SpdxFileDifference[] differences = comparer.getFileDifferences(0, 1);
		assertEquals(0, differences.length);
		SPDXFile[] files = doc1.getSpdxPackage().getFiles();
		SPDXFile[] newFiles = Arrays.copyOf(files, files.length+1);
		newFiles[files.length] = new SPDXFile("NewName", SpdxRdfConstants.FILE_TYPE_ARCHIVE, files[0].getSha1(), 
				files[1].getConcludedLicenses(), files[0].getSeenLicenses(), "", "", 
				new DOAPProject[] {});
		doc2.getSpdxPackage().setFiles(newFiles);
		comparer.compare(doc1, doc2);
		SPDXFile[] unique = comparer.getUniqueFiles(0, 1);
		assertEquals(0, unique.length);
		unique = comparer.getUniqueFiles(1, 0);
		assertEquals(1, unique.length);
		assertEquals(newFiles[files.length].getName(), unique[0].getName());
	}
}
