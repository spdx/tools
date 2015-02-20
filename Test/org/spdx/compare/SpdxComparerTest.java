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
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ConjunctiveLicenseSet;
import org.spdx.rdfparser.license.DisjunctiveLicenseSet;
import org.spdx.rdfparser.license.DuplicateExtractedLicenseIdException;
import org.spdx.rdfparser.license.License;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.SpdxNoneLicense;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.license.SpdxNoAssertionLicense;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.ExternalDocumentRef;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxElement;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxItem;
import org.spdx.rdfparser.model.SpdxPackage;
import org.spdx.rdfparser.model.Annotation.AnnotationType;
import org.spdx.rdfparser.model.Checksum.ChecksumAlgorithm;
import org.spdx.rdfparser.model.Relationship.RelationshipType;
import org.spdx.rdfparser.model.SpdxFile.FileType;
import org.spdx.rdfparser.model.UnitTestHelper;
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
	
	private static final String COMMENTA = "comment A";
	private static final String COMMENTB = "comment B";
	private static final ExtractedLicenseInfo LICENSEA1 = new ExtractedLicenseInfo("LicenseRef-1", "License1");
	private static final ExtractedLicenseInfo LICENSEA2 = new ExtractedLicenseInfo("LicenseRef-2", "License2");
	private static final ExtractedLicenseInfo LICENSEA3 = new ExtractedLicenseInfo("LicenseRef-3", "License3");
	private static final ExtractedLicenseInfo LICENSEB1 = new ExtractedLicenseInfo("LicenseRef-4", "License1");
	private static final ExtractedLicenseInfo LICENSEB2 = new ExtractedLicenseInfo("LicenseRef-5", "License2");
	private static final ExtractedLicenseInfo LICENSEB3 = new ExtractedLicenseInfo("LicenseRef-6", "License3");
	private static final AnyLicenseInfo[] LICENSE_INFO_FROM_FILESA = new AnyLicenseInfo[] {LICENSEA1, LICENSEA2, LICENSEA3};
	private static final AnyLicenseInfo[] LICENSE_INFO_FROM_FILESB = new AnyLicenseInfo[] {LICENSEB1, LICENSEB2, LICENSEB3};
	private static final String LICENSE_COMMENTA = "License Comment A";
	@SuppressWarnings("unused")
	private static final String LICENSE_COMMENTB = "License Comment B";
	private static final String COPYRIGHTA = "Copyright A";
	@SuppressWarnings("unused")
	private static final String COPYRIGHTB = "Copyright B";
	private static final AnyLicenseInfo LICENSE_CONCLUDEDA = LICENSEA1;
	private static final AnyLicenseInfo LICENSE_CONCLUDEDB = LICENSEB1;
	private static final String NAMEA = "NameA";
	private static final String NAMEB = "NameB";
	private static final String NAMEC = "NameC";
	private static final HashMap<String, String> LICENSE_XLATION_MAP = new HashMap<String, String>();
	private static final AnyLicenseInfo LICENSE_DECLAREDA = LICENSEA2;
	private static final AnyLicenseInfo LICENSE_DECLAREDB = LICENSEB2;
	private static final String ORIGINATORA = "Organization: OrgA";
	@SuppressWarnings("unused")
	private static final String ORIGINATORB = "Organization: OrgB";
	private static final String HOMEPAGEA = "http://home.page/a";
	@SuppressWarnings("unused")
	private static final String HOMEPAGEB = "http://home.page/b";
	private static final String DOWNLOADA = "http://download.page/a";
	@SuppressWarnings("unused")
	private static final String DOWNLOADB = "http://download.page/b";
	private static final String DESCRIPTIONA = "Description A";
	@SuppressWarnings("unused")
	private static final String DESCRIPTIONB = "Description B";
	private static final String PACKAGE_FILENAMEA = "packageFileNameA";
	@SuppressWarnings("unused")
	private static final String PACKAGE_FILENAMEB = "packageFileNameB";
	private static final String SOURCEINFOA = "Sourc info A";
	@SuppressWarnings("unused")
	private static final String SOURCEINFOB = "Sourc info B";
	private static final String SUMMARYA = "Summary A";
	@SuppressWarnings("unused")
	private static final String SUMMARYB = "Summary B";
	private static final String VERSIONINFOA = "Version A";
	@SuppressWarnings("unused")
	private static final String VERSIONINFOB = "Version B";
	private static final String SUPPLIERA = "Person: Supplier A";
	@SuppressWarnings("unused")
	private static final String SUPPLIERB = "Person: Supplier B";
	private static final String DOC_URIA = "http://spdx.org/documents/uriA";
	private static final String DOC_URIB = "http://spdx.org/documents/uriB";
	private static final String DOC_URIC = "http://spdx.org/documents/uriC";
	private static final ExtractedLicenseInfo[] EXTRACTED_LICENSESA = new ExtractedLicenseInfo[]{
		LICENSEA1, LICENSEA2, LICENSEA3
	};
	private static final ExtractedLicenseInfo[] EXTRACTED_LICENSESB = new ExtractedLicenseInfo[]{
		LICENSEB1, LICENSEB2, LICENSEB3
	};
	private static final String DOC_NAMEA = "DocumentA";
	private static final String DOC_NAMEB = "DocumentB";
	private static final SPDXCreatorInformation CREATION_INFOA = new SPDXCreatorInformation(
			new String[] {"Person: CreatorA"}, "2010-01-29T18:30:22Z", "Creator CommentA", "1.15");
	private static final SPDXCreatorInformation CREATION_INFOB = new SPDXCreatorInformation(
			new String[] {"Person: CreatorB"}, "2012-01-29T18:30:22Z", "Creator CommentB", "1.17");
	static {
		LICENSE_XLATION_MAP.put("LicenseRef-1", "LicenseRef-4");
		LICENSE_XLATION_MAP.put("LicenseRef-2", "LicenseRef-5");
		LICENSE_XLATION_MAP.put("LicenseRef-3", "LicenseRef-6");
	}
	private Annotation ANNOTATION1;
	private Annotation ANNOTATION2;
	private Annotation ANNOTATION3;
	private Annotation ANNOTATION4;
	private Annotation[] ANNOTATIONSA;
	@SuppressWarnings("unused")
	private Annotation[] ANNOTATIONSB;
	
	private Relationship[] RELATIONSHIPSA;
	@SuppressWarnings("unused")
	private Relationship[] RELATIONSHIPSB;
	private SpdxElement RELATED_ELEMENT1;
	private SpdxElement RELATED_ELEMENT2;
	private SpdxElement RELATED_ELEMENT3;
	private SpdxElement RELATED_ELEMENT4;
	private Relationship RELATIONSHIP1;
	private Relationship RELATIONSHIP2;
	private Relationship RELATIONSHIP3;
	private Relationship RELATIONSHIP4;
	private Checksum CHECKSUM1;
	private Checksum CHECKSUM2;
	private Checksum CHECKSUM3;
	private Checksum CHECKSUM4;
	private Checksum[] CHECKSUMSA;
	private Checksum[] CHECKSUMSB;
	private String FILE1_NAME = "file1Name";
	private String FILE2_NAME = "file2Name";
	private String FILE3_NAME = "file3Name";
	private String FILE4_NAME = "file4Name";
	private SpdxFile FILE1A;
	private SpdxFile FILE1B;
	private SpdxFile FILE1B_DIFF_CHECKSUM;
	private SpdxFile FILE2A;
	private SpdxFile FILE3A;
	private SpdxFile FILE2B;
	private SpdxFile FILE3B;
	private SpdxFile FILE4A;
	private SpdxFile[] FILESA;
	private SpdxFile[] FILESB;
	private SpdxFile[] FILESB_SAME;
	private SpdxPackageVerificationCode VERIFICATION_CODEA;
	@SuppressWarnings("unused")
	private SpdxPackageVerificationCode VERIFICATION_CODEB;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.testRDFFile = new File(TEST_RDF_FILE_PATH); 
		ANNOTATION1 = new Annotation("Annotator1", AnnotationType.annotationType_other, 
				"2010-01-29T18:30:22Z", "AnnotationComment1");
		ANNOTATION2 = new Annotation("Annotator2", AnnotationType.annotationType_review, 
				"2011-01-29T18:30:22Z", "AnnotationComment2");
		ANNOTATION3 = new Annotation("Annotator3", AnnotationType.annotationType_other, 
				"2012-01-29T18:30:22Z", "AnnotationComment3");
		ANNOTATION4 = new Annotation("Annotator4", AnnotationType.annotationType_review, 
				"2013-01-29T18:30:22Z", "AnnotationComment4");
		ANNOTATIONSA = new Annotation[] {ANNOTATION1, ANNOTATION2};
		ANNOTATIONSB = new Annotation[] {ANNOTATION3, ANNOTATION4};
		RELATED_ELEMENT1 = new SpdxElement("relatedElementName1", 
				"related element comment 1", null, null);
		RELATED_ELEMENT2 = new SpdxElement("relatedElementName2", 
				"related element comment 2", null, null);
		RELATED_ELEMENT3 = new SpdxElement("relatedElementName3", 
				"related element comment 3", null, null);
		RELATED_ELEMENT4 = new SpdxElement("relatedElementName4", 
				"related element comment 4", null, null);
		RELATIONSHIP1 = new Relationship(RELATED_ELEMENT1, 
				RelationshipType.relationshipType_contains, "Relationship Comment1");
		RELATIONSHIP2 = new Relationship(RELATED_ELEMENT2, 
				RelationshipType.relationshipType_dynamicLink, "Relationship Comment2");
		RELATIONSHIP3 = new Relationship(RELATED_ELEMENT3, 
				RelationshipType.relationshipType_dataFile, "Relationship Comment3");
		RELATIONSHIP4 = new Relationship(RELATED_ELEMENT4, 
				RelationshipType.relationshipType_distributionArtifact, "Relationship Comment4");
		RELATIONSHIPSA = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		RELATIONSHIPSB = new Relationship[] {RELATIONSHIP3, RELATIONSHIP4};
		CHECKSUM1 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, 
				"111bf72bf99b7e471f1a27989667a903658652bb");
		CHECKSUM2 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, 
				"222bf72bf99b7e471f1a27989667a903658652bb");
		CHECKSUM3 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, 
				"333bf72bf99b7e471f1a27989667a903658652bb");
		CHECKSUM4 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, 
				"444bf72bf99b7e471f1a27989667a903658652bb");
		CHECKSUMSA = new Checksum[] {CHECKSUM1, CHECKSUM2};
		CHECKSUMSB = new Checksum[] {CHECKSUM3, CHECKSUM4};
		
		FILE1A = new SpdxFile(FILE1_NAME, null, null, null, 
				LICENSE_CONCLUDEDA, new AnyLicenseInfo[] {LICENSEA1, LICENSEA2},
				null, null, new FileType[] {FileType.fileType_documentation, FileType.fileType_text},
				CHECKSUMSA, null, null, null);
		FILE1B = new SpdxFile(FILE1_NAME, null, null, null, 
				LICENSE_CONCLUDEDB, new AnyLicenseInfo[] {LICENSEB1, LICENSEB2},
				null, null, new FileType[] {FileType.fileType_documentation, FileType.fileType_text},
				CHECKSUMSA, null, null, null);
		FILE1B_DIFF_CHECKSUM = new SpdxFile(FILE1_NAME, null, null, null, 
				LICENSE_CONCLUDEDB, new AnyLicenseInfo[] {LICENSEB1, LICENSEB2},
				null, null, new FileType[] {FileType.fileType_documentation, FileType.fileType_text},
				CHECKSUMSB, null, null, null);
		FILE2A = new SpdxFile(FILE2_NAME, null, null, null, 
				LICENSE_CONCLUDEDA, new AnyLicenseInfo[] {LICENSEA1, LICENSEA2},
				null, null, new FileType[] {FileType.fileType_documentation, FileType.fileType_text},
				CHECKSUMSA, null, null, null);
		FILE3A = new SpdxFile(FILE3_NAME, null, null, null, 
				LICENSE_CONCLUDEDA, new AnyLicenseInfo[] {LICENSEA1, LICENSEA2},
				null, null, new FileType[] {FileType.fileType_documentation, FileType.fileType_text},
				CHECKSUMSA, null, null, null);
		FILE4A = new SpdxFile(FILE4_NAME, null, null, null, 
				LICENSE_CONCLUDEDA, new AnyLicenseInfo[] {LICENSEA1, LICENSEA2},
				null, null, new FileType[] {FileType.fileType_documentation, FileType.fileType_text},
				CHECKSUMSA, null, null, null);
		FILE2B = new SpdxFile(FILE2_NAME, null, null, null, 
				LICENSE_CONCLUDEDB, new AnyLicenseInfo[] {LICENSEB1, LICENSEB2},
				null, null, new FileType[] {FileType.fileType_documentation, FileType.fileType_text},
				CHECKSUMSA, null, null, null);
		FILE3B = new SpdxFile(FILE3_NAME, null, null, null, 
				LICENSE_CONCLUDEDB, new AnyLicenseInfo[] {LICENSEB1, LICENSEB2},
				null, null, new FileType[] {FileType.fileType_documentation, FileType.fileType_text},
				CHECKSUMSA, null, null, null);

		FILESA = new SpdxFile[] {FILE1A, FILE2A};
		FILESB_SAME = new SpdxFile[] {FILE1B, FILE2B};
		FILESB = new SpdxFile[] {FILE1B_DIFF_CHECKSUM, FILE3B};
		VERIFICATION_CODEA = new SpdxPackageVerificationCode("aaabf72bf99b7e471f1a27989667a903658652bb",
				new String[] {"file2"});
		VERIFICATION_CODEB = new SpdxPackageVerificationCode("bbbbf72bf99b7e471f1a27989667a903658652bb",
				new String[] {"file3"});
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#compare(org.spdx.rdfparser.SpdxDocument, org.spdx.rdfparser.SpdxDocument)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testCompare() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxComparer comparer = new SpdxComparer();
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		try {
			comparer.isCreatorInformationEqual();	// should fail
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
	 * Test method for {@link org.spdx.compare.SpdxComparer#compareLicense(int, org.spdx.rdfparser.license.AnyLicenseInfo, int, org.spdx.rdfparser.license.AnyLicenseInfo)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 * @throws SpdxCompareException 
	 * @throws InvalidLicenseStringException 
	 */
	@Test
	public void testCompareLicense() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException, InvalidLicenseStringException {
		SpdxComparer comparer = new SpdxComparer();
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isDifferenceFound());
		ExtractedLicenseInfo[] extractedInfos1 = doc1.getExtractedLicenseInfos();
		ExtractedLicenseInfo[] extractedInfos2 = doc2.getExtractedLicenseInfos();
		
		HashMap<Integer, Integer> xlateDoc1ToDoc2LicId = createLicIdXlation(extractedInfos1, extractedInfos2);
		
		//Standard License
		SpdxListedLicense lic1 = LicenseInfoFactory.getListedLicenseById(STD_LIC_ID_CC0);
		SpdxListedLicense lic1_1 = LicenseInfoFactory.getListedLicenseById(STD_LIC_ID_CC0);
		SpdxListedLicense lic2 = LicenseInfoFactory.getListedLicenseById(STD_LIC_ID_MPL11);
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
		sb.append(extractedInfos1[0].getLicenseId());
		sb.append(" AND ");
		sb.append(STD_LIC_ID_MPL11);
		sb.append(" AND ");
		sb.append(extractedInfos1[1].getLicenseId());
		sb.append(")");
		AnyLicenseInfo conj1 = LicenseInfoFactory.parseSPDXLicenseString(sb.toString());

		sb = new StringBuilder("(");
		sb.append(STD_LIC_ID_MPL11);
		sb.append(" AND ");
		sb.append(extractedInfos2[xlateDoc1ToDoc2LicId.get(1)].getLicenseId());
		sb.append(" AND ");
		sb.append(STD_LIC_ID_CC0);
		sb.append(" AND ");
		sb.append(extractedInfos2[xlateDoc1ToDoc2LicId.get(0)].getLicenseId());
		sb.append(")");
		AnyLicenseInfo conj2 = LicenseInfoFactory.parseSPDXLicenseString(sb.toString());
		
		sb = new StringBuilder("(");
		sb.append(STD_LIC_ID_MPL11);
		sb.append(" AND ");
		sb.append(extractedInfos2[xlateDoc1ToDoc2LicId.get(1)].getLicenseId());
		sb.append(" AND ");
		sb.append(STD_LIC_ID_CC0);
		sb.append(")");
		AnyLicenseInfo conj3 = LicenseInfoFactory.parseSPDXLicenseString(sb.toString());
		
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
		sb.append(extractedInfos1[0].getLicenseId());
		sb.append(" OR ");
		sb.append(STD_LIC_ID_MPL11);
		sb.append(" OR ");
		sb.append(extractedInfos1[1].getLicenseId());
		sb.append(")");
		AnyLicenseInfo dis1 = LicenseInfoFactory.parseSPDXLicenseString(sb.toString());

		sb = new StringBuilder("(");
		sb.append(STD_LIC_ID_MPL11);
		sb.append(" OR ");
		sb.append(extractedInfos2[xlateDoc1ToDoc2LicId.get(1)].getLicenseId());
		sb.append(" OR ");
		sb.append(extractedInfos2[xlateDoc1ToDoc2LicId.get(0)].getLicenseId());
		sb.append(" OR ");
		sb.append(STD_LIC_ID_CC0);
		sb.append(")");
		AnyLicenseInfo dis2 = LicenseInfoFactory.parseSPDXLicenseString(sb.toString());
		
		sb = new StringBuilder("(");
		sb.append(STD_LIC_ID_MPL11);
		sb.append(" OR ");
		sb.append(extractedInfos2[xlateDoc1ToDoc2LicId.get(1)].getLicenseId());
		sb.append(" OR ");
		sb.append(STD_LIC_ID_CC0);
		sb.append(")");
		AnyLicenseInfo dis3 = LicenseInfoFactory.parseSPDXLicenseString(sb.toString());
		
		assertTrue(comparer.compareLicense(0, dis1, 1, dis2));
		assertFalse(comparer.compareLicense(0, dis1, 1, dis3));
		try {
			assertFalse(comparer.compareLicense(0, dis2, 1, dis2));
		} catch(SpdxCompareException ex) {
			// we expect a mappint exception
		}
		//Complex License
		DisjunctiveLicenseSet subcomplex1 = new DisjunctiveLicenseSet(
					new AnyLicenseInfo[] {lic1, conj1});
		ConjunctiveLicenseSet complex1 = new ConjunctiveLicenseSet(
				new AnyLicenseInfo[] {subcomplex1, dis1, extractedInfos1[0]});
		DisjunctiveLicenseSet subcomplex2 = new DisjunctiveLicenseSet(
				new AnyLicenseInfo[] {conj2, lic1_1});
		ConjunctiveLicenseSet complex2 = new ConjunctiveLicenseSet(
			new AnyLicenseInfo[] {dis2, subcomplex2, extractedInfos2[xlateDoc1ToDoc2LicId.get(0)]});
		
		DisjunctiveLicenseSet subcomplex3 = new DisjunctiveLicenseSet(
				new AnyLicenseInfo[] {conj3, lic1_1});
		ConjunctiveLicenseSet complex3 = new ConjunctiveLicenseSet(
			new AnyLicenseInfo[] {dis2, subcomplex3, extractedInfos2[xlateDoc1ToDoc2LicId.get(0)]});
		assertTrue(comparer.compareLicense(0, complex1, 1, complex2));
		assertFalse(comparer.compareLicense(0, complex1, 1, complex3));
		//NONE
		SpdxNoneLicense noneLic1 = new SpdxNoneLicense();
		SpdxNoneLicense noneLic2 = new SpdxNoneLicense();
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
			ExtractedLicenseInfo[] licInfos1,
			ExtractedLicenseInfo[] licInfos2) {
		HashMap<Integer, Integer> retval = new HashMap<Integer, Integer>();
		for (int i = 0;i < licInfos1.length; i++) {
			boolean found = false;
			for (int j = 0; j < licInfos2.length; j++) {
				if (licInfos1[i].getExtractedText().equals(licInfos2[j].getExtractedText())) {
					if (found) {
						fail("Two licenses found with the same text: "+licInfos1[i].getExtractedText());
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
	private void alterExtractedLicenseInfoIds(SpdxDocument doc, int digit) throws InvalidSPDXAnalysisException {
		ExtractedLicenseInfo[] extracted = doc.getExtractedLicenseInfos();
		for (int i = 0; i < extracted.length; i++) {
			String oldId = extracted[i].getLicenseId();
			String newId = oldId + String.valueOf(digit);
			extracted[i].setLicenseId(newId);
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
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isDifferenceFound());
		doc2.setComment("a new doc comment");
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
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isDifferenceFound());
		assertTrue(comparer.isSpdxVersionEqual());
		doc2.setSpecVersion("SPDX-1.0");
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isDifferenceFound());
		assertFalse(comparer.isSpdxVersionEqual());
		doc1.setSpecVersion("SPDX-1.0");
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
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		String DOC_2_COMMENT = "doc2";
		String DOC_1_COMMENT = "doc1";
		doc1.setComment(DOC_1_COMMENT);
		doc2.setComment(DOC_2_COMMENT);
		comparer.compare(doc1, doc2);
		assertEquals(DOC_1_COMMENT, comparer.getSpdxDoc(0).getComment());
		assertEquals(DOC_2_COMMENT, comparer.getSpdxDoc(1).getComment());

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
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isDifferenceFound());
		assertTrue(comparer.isDataLicenseEqual());
		doc2.setSpecVersion("SPDX-1.0");
		doc2.setDataLicense(LicenseInfoFactory.getListedLicenseById(SpdxRdfConstants.SPDX_DATA_LICENSE_ID_VERSION_1_0));
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
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isDocumentCommentsEqual());
		assertFalse(comparer.isDifferenceFound());
		doc2.setComment("a new doc comment");
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isDifferenceFound());
		assertFalse(comparer.isDocumentCommentsEqual());
		doc1.setComment("a new doc comment");
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isDifferenceFound());
		assertTrue(comparer.isDocumentCommentsEqual());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isReviewersEqual()}.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testIsReviewersEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
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
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		ExtractedLicenseInfo[] orig1 = doc1.getExtractedLicenseInfos();
		ExtractedLicenseInfo[] orig2 = doc2.getExtractedLicenseInfos();
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isExtractedLicensingInfosEqual());
		assertFalse(comparer.isDifferenceFound());

		int doc1id = 100;
		int doc2id = 200;
		String id1_1 = SpdxDocumentContainer.formNonStandardLicenseID(doc1id++);
		String text1 = "License text 1";
		String name1 = "licname1";
		String[] crossReff1 = new String[] {"http://cross.ref.one"};
		String comment1 = "comment1";
		ExtractedLicenseInfo lic1_1 = new ExtractedLicenseInfo(
				id1_1, text1, name1, crossReff1, comment1);
		String id1_2 = SpdxDocumentContainer.formNonStandardLicenseID(doc2id++);
		ExtractedLicenseInfo lic1_2 = new ExtractedLicenseInfo(
				id1_2, text1, name1, crossReff1, comment1);
		
		String id2_1 = SpdxDocumentContainer.formNonStandardLicenseID(doc1id++);
		String text2 = "License text 2";
		String name2 = "licname2";
		String[] crossReff2 = new String[] {"http://cross.ref.one", "http://cross.ref.two"};
		String comment2 = "comment2";
		ExtractedLicenseInfo lic2_1 = new ExtractedLicenseInfo(
				id2_1, text2, name2, crossReff2, comment2);
		String id2_2 = SpdxDocumentContainer.formNonStandardLicenseID(doc2id++);
		ExtractedLicenseInfo lic2_2 = new ExtractedLicenseInfo(
				id2_2, text2, name2, crossReff2, comment2);
		
		String id3_1 = SpdxDocumentContainer.formNonStandardLicenseID(doc1id++);
		String text3 = "License text 3";
		String name3 = "";
		String[] crossReff3 = new String[] {};
		String comment3 = "comment3";
		ExtractedLicenseInfo lic3_1 = new ExtractedLicenseInfo(
				id3_1, text3, name3, crossReff3, comment3);
		String id3_2 = SpdxDocumentContainer.formNonStandardLicenseID(doc2id++);
		ExtractedLicenseInfo lic3_2 = new ExtractedLicenseInfo(
				id3_2, text3, name3, crossReff3, comment3);

		String id4_1 = SpdxDocumentContainer.formNonStandardLicenseID(doc1id++);
		String text4 = "License text 4";
		String name4 = "";
		String[] crossReff4 = new String[] {};
		String comment4 = "";
		ExtractedLicenseInfo lic4_1 = new ExtractedLicenseInfo(
				id4_1, text4, name4, crossReff4, comment4);
		String id4_2 = SpdxDocumentContainer.formNonStandardLicenseID(doc2id++);
		ExtractedLicenseInfo lic4_2 = new ExtractedLicenseInfo(
				id4_2, text4, name4, crossReff4, comment4);

		// same licenses, different order
		ExtractedLicenseInfo[] exLicenses1 = Arrays.copyOf(orig1, orig1.length+4);
		exLicenses1[orig1.length+0] = lic1_1;
		exLicenses1[orig1.length+1] = lic2_1;
		exLicenses1[orig1.length+2] = lic3_1;
		exLicenses1[orig1.length+3] = lic4_1;
		ExtractedLicenseInfo[] exLicenses2 = Arrays.copyOf(orig2, orig2.length+4);
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

		doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		doc1.setExtractedLicenseInfos(exLicenses1);
		doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
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
		doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
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
		
		ExtractedLicenseInfo lic1_2_diff_Text = new ExtractedLicenseInfo(
				id1_2, "Different Text", name1, crossReff1, comment1);
		exLicenses2[orig2.length+0] = lic3_2;
		exLicenses2[orig2.length+1] = lic1_2_diff_Text;
		exLicenses2[orig2.length+2] = lic4_2;
		exLicenses2[orig2.length+3] = lic2_2;

		doc1.setExtractedLicenseInfos(exLicenses1);
		boolean caughtDupException = false;
		try {
			doc2.setExtractedLicenseInfos(exLicenses2);
		} catch (DuplicateExtractedLicenseIdException e) {
			caughtDupException = true;
		}
		assertTrue(caughtDupException);		
		
		// license comments differ	
		exLicenses1 = Arrays.copyOf(orig1, orig1.length+4);
		exLicenses1[orig1.length+0] = lic1_1;
		exLicenses1[orig1.length+1] = lic2_1;
		exLicenses1[orig1.length+2] = lic3_1;
		exLicenses1[orig1.length+3] = lic4_1;
		exLicenses2 = Arrays.copyOf(orig2, orig2.length+4);
		
		ExtractedLicenseInfo lic1_2_diff_Comment = new ExtractedLicenseInfo(
				id1_2, text1, name1, crossReff1, "different comment");
		exLicenses2[orig2.length+0] = lic3_2;
		exLicenses2[orig2.length+1] = lic1_2_diff_Comment;
		exLicenses2[orig2.length+2] = lic4_2;
		exLicenses2[orig2.length+3] = lic2_2;

		doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
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
		
		ExtractedLicenseInfo lic1_2_diff_licenref = new ExtractedLicenseInfo(
				id1_2, text1, name1, crossReff2, comment1);
		exLicenses2[orig2.length+0] = lic3_2;
		exLicenses2[orig2.length+1] = lic1_2_diff_licenref;
		exLicenses2[orig2.length+2] = lic4_2;
		exLicenses2[orig2.length+3] = lic2_2;
		doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		doc1.setExtractedLicenseInfos(exLicenses1);
		doc2.setExtractedLicenseInfos(exLicenses2);
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isExtractedLicensingInfosEqual());
		assertTrue(comparer.isDifferenceFound());		
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#getUniqueReviewers(int, int)}.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testGetUniqueReviewers() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
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
	@SuppressWarnings("deprecation")
	@Test
	public void testGetReviewerDifferences() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
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
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		ExtractedLicenseInfo[] orig1 = doc1.getExtractedLicenseInfos();
		ExtractedLicenseInfo[] orig2 = doc2.getExtractedLicenseInfos();
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isExtractedLicensingInfosEqual());
		assertFalse(comparer.isDifferenceFound());

		int doc1id = 100;
		int doc2id = 200;
		String id1_1 = SpdxDocumentContainer.formNonStandardLicenseID(doc1id++);
		String text1 = "License text 1";
		String name1 = "licname1";
		String[] crossReff1 = new String[] {"http://cross.ref.one"};
		String comment1 = "comment1";
		ExtractedLicenseInfo lic1_1 = new ExtractedLicenseInfo(
				id1_1, text1, name1, crossReff1, comment1);
		String id1_2 = SpdxDocumentContainer.formNonStandardLicenseID(doc2id++);
		ExtractedLicenseInfo lic1_2 = new ExtractedLicenseInfo(
				id1_2, text1, name1, crossReff1, comment1);
		
		String id2_1 = SpdxDocumentContainer.formNonStandardLicenseID(doc1id++);
		String text2 = "License text 2";
		String name2 = "licname2";
		String[] crossReff2 = new String[] {"http://cross.ref.one", "http://cross.ref.two"};
		String comment2 = "comment2";
		ExtractedLicenseInfo lic2_1 = new ExtractedLicenseInfo(
				id2_1, text2, name2, crossReff2, comment2);
		String id2_2 = SpdxDocumentContainer.formNonStandardLicenseID(doc2id++);
		ExtractedLicenseInfo lic2_2 = new ExtractedLicenseInfo(
				id2_2, text2, name2, crossReff2, comment2);
		
		String id3_1 = SpdxDocumentContainer.formNonStandardLicenseID(doc1id++);
		String text3 = "License text 3";
		String name3 = "";
		String[] crossReff3 = new String[] {};
		String comment3 = "comment3";
		ExtractedLicenseInfo lic3_1 = new ExtractedLicenseInfo(
				id3_1, text3, name3, crossReff3, comment3);
		String id3_2 = SpdxDocumentContainer.formNonStandardLicenseID(doc2id++);
		ExtractedLicenseInfo lic3_2 = new ExtractedLicenseInfo(
				id3_2, text3, name3, crossReff3, comment3);

		String id4_1 = SpdxDocumentContainer.formNonStandardLicenseID(doc1id++);
		String text4 = "License text 4";
		String name4 = "";
		String[] crossReff4 = new String[] {};
		String comment4 = "";
		ExtractedLicenseInfo lic4_1 = new ExtractedLicenseInfo(
				id4_1, text4, name4, crossReff4, comment4);
		String id4_2 = SpdxDocumentContainer.formNonStandardLicenseID(doc2id++);
		ExtractedLicenseInfo lic4_2 = new ExtractedLicenseInfo(
				id4_2, text4, name4, crossReff4, comment4);

		// same licenses, different order
		ExtractedLicenseInfo[] exLicenses1 = Arrays.copyOf(orig1, orig1.length+4);
		exLicenses1[orig1.length+0] = lic1_1;
		exLicenses1[orig1.length+1] = lic2_1;
		exLicenses1[orig1.length+2] = lic3_1;
		exLicenses1[orig1.length+3] = lic4_1;
		ExtractedLicenseInfo[] exLicenses2 = Arrays.copyOf(orig2, orig2.length+4);
		exLicenses2[orig2.length+0] = lic3_2;
		exLicenses2[orig2.length+1] = lic1_2;
		exLicenses2[orig2.length+2] = lic4_2;
		exLicenses2[orig2.length+3] = lic2_2;
		doc1.setExtractedLicenseInfos(exLicenses1);
		doc2.setExtractedLicenseInfos(exLicenses2);
		comparer.compare(doc1, doc2);
		ExtractedLicenseInfo[] result = comparer.getUniqueExtractedLicenses(0, 1);
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
		uniqueLicIds.add(lic2_1.getLicenseId());
		uniqueLicIds.add(lic4_1.getLicenseId());

		doc1.setExtractedLicenseInfos(exLicenses1);
		doc2.setExtractedLicenseInfos(exLicenses2);
		comparer.compare(doc1, doc2);
		result = comparer.getUniqueExtractedLicenses(0, 1);
		assertEquals(2, result.length);
		assertTrue(uniqueLicIds.contains(result[0].getLicenseId()));
		assertTrue(uniqueLicIds.contains(result[1].getLicenseId()));
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
		uniqueLicIds.add(lic3_2.getLicenseId());
		uniqueLicIds.add(lic1_2.getLicenseId());		
		uniqueLicIds.add(lic4_2.getLicenseId());
		uniqueLicIds.add(lic2_2.getLicenseId());
		
		doc1.setExtractedLicenseInfos(exLicenses1);
		doc2.setExtractedLicenseInfos(exLicenses2);
		comparer.compare(doc1, doc2);
		result = comparer.getUniqueExtractedLicenses(0, 1);
		assertEquals(0, result.length);
		result = comparer.getUniqueExtractedLicenses(1, 0);
		assertEquals(4, result.length);
		assertTrue(uniqueLicIds.contains(result[0].getLicenseId()));
		assertTrue(uniqueLicIds.contains(result[1].getLicenseId()));
		assertTrue(uniqueLicIds.contains(result[2].getLicenseId()));
		assertTrue(uniqueLicIds.contains(result[3].getLicenseId()));
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#getExtractedLicenseDifferences(int, int)}.
	 */
	@Test
	public void testGetExtractedLicenseDifferences() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		ExtractedLicenseInfo[] orig1 = doc1.getExtractedLicenseInfos();
		ExtractedLicenseInfo[] orig2 = doc2.getExtractedLicenseInfos();
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isExtractedLicensingInfosEqual());
		assertFalse(comparer.isDifferenceFound());

		int doc1id = 100;
		int doc2id = 200;
		String id1_1 = SpdxDocumentContainer.formNonStandardLicenseID(doc1id++);
		String text1 = "License text 1";
		String name1 = "licname1";
		String[] crossReff1 = new String[] {"http://cross.ref.one"};
		String comment1 = "comment1";
		ExtractedLicenseInfo lic1_1 = new ExtractedLicenseInfo(
				id1_1, text1, name1, crossReff1, comment1);
		String id1_2 = SpdxDocumentContainer.formNonStandardLicenseID(doc2id++);
		ExtractedLicenseInfo lic1_2 = new ExtractedLicenseInfo(
				id1_2, text1, name1, crossReff1, comment1);
		
		String id2_1 = SpdxDocumentContainer.formNonStandardLicenseID(doc1id++);
		String text2 = "License text 2";
		String name2 = "licname2";
		String[] crossReff2 = new String[] {"http://cross.ref.one", "http://cross.ref.two"};
		String comment2 = "comment2";
		ExtractedLicenseInfo lic2_1 = new ExtractedLicenseInfo(
				id2_1, text2, name2, crossReff2, comment2);
		String id2_2 = SpdxDocumentContainer.formNonStandardLicenseID(doc2id++);
		ExtractedLicenseInfo lic2_2 = new ExtractedLicenseInfo(
				id2_2, text2, name2, crossReff2, comment2);
		
		String id3_1 = SpdxDocumentContainer.formNonStandardLicenseID(doc1id++);
		String text3 = "License text 3";
		String name3 = "name3";
		String[] crossReff3 = new String[] {"http://crossref3.org"};
		String comment3 = "comment3";
		ExtractedLicenseInfo lic3_1 = new ExtractedLicenseInfo(
				id3_1, text3, name3, crossReff3, comment3);
		String id3_2 = SpdxDocumentContainer.formNonStandardLicenseID(doc2id++);
		ExtractedLicenseInfo lic3_2 = new ExtractedLicenseInfo(
				id3_2, text3, name3, crossReff3, comment3);

		String id4_1 = SpdxDocumentContainer.formNonStandardLicenseID(doc1id++);
		String text4 = "License text 4";
		String name4 = "";
		String[] crossReff4 = new String[] {};
		String comment4 = "";
		ExtractedLicenseInfo lic4_1 = new ExtractedLicenseInfo(
				id4_1, text4, name4, crossReff4, comment4);
		String id4_2 = SpdxDocumentContainer.formNonStandardLicenseID(doc2id++);
		ExtractedLicenseInfo lic4_2 = new ExtractedLicenseInfo(
				id4_2, text4, name4, crossReff4, comment4);

		// same licenses, different order
		ExtractedLicenseInfo[] exLicenses1 = Arrays.copyOf(orig1, orig1.length+4);
		exLicenses1[orig1.length+0] = lic1_1;
		exLicenses1[orig1.length+1] = lic2_1;
		exLicenses1[orig1.length+2] = lic3_1;
		exLicenses1[orig1.length+3] = lic4_1;
		ExtractedLicenseInfo[] exLicenses2 = Arrays.copyOf(orig2, orig2.length+4);
		exLicenses2[orig2.length+0] = lic3_2;
		exLicenses2[orig2.length+1] = lic1_2;
		exLicenses2[orig2.length+2] = lic4_2;
		exLicenses2[orig2.length+3] = lic2_2;
		
		doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		doc1.setExtractedLicenseInfos(exLicenses1);
		doc2.setExtractedLicenseInfos(exLicenses2);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isExtractedLicensingInfosEqual());
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
		ExtractedLicenseInfo lic1_2_diff_name = new ExtractedLicenseInfo(
				id1_2, text1, differentName, crossReff1, comment1);
		String differentComment = "different comment";
		ExtractedLicenseInfo lic2_2_diff_Comment = new ExtractedLicenseInfo(
				id2_2, text2, name2, crossReff2, differentComment);
		ExtractedLicenseInfo lic3_2_diff_licenref = new ExtractedLicenseInfo(
				id3_2, text3, name3, crossReff2, comment3);
		exLicenses2[orig2.length+0] = lic3_2_diff_licenref;
		exLicenses2[orig2.length+1] = lic1_2_diff_name;
		exLicenses2[orig2.length+2] = lic4_2;
		exLicenses2[orig2.length+3] = lic2_2_diff_Comment;

		doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		doc1.setExtractedLicenseInfos(exLicenses1);
		doc2.setExtractedLicenseInfos(exLicenses2);
		comparer.compare(doc1, doc2);
		
		result = comparer.getExtractedLicenseDifferences(0,1);
		assertEquals(3, result.length);
		boolean lic1Found = false;
		boolean lic2Found = false;
		boolean lic3Found = false;
		for (int i = 0; i < result.length; i++) {
			if (result[i].getIdA().equals(lic2_1.getLicenseId())) {
				lic2Found = true;
				assertEquals(lic2_2.getLicenseId(), result[i].getIdB());
				assertEquals(comment2, result[i].getCommentA());
				assertEquals(differentComment, result[i].getCommentB());
				assertEquals(name2, result[i].getLicenseNameA());
				assertEquals(name2, result[i].getLicenseNameB());
				assertEquals(text2, result[i].getLicenseText());
				assertTrue(stringsSame(crossReff2, result[i].getSourceUrlsA()));
				assertTrue(stringsSame(crossReff2, result[i].getSourceUrlsB()));
			} else if (result[i].getIdA().equals(lic1_1.getLicenseId())) {
				lic1Found = true;
				assertEquals(lic1_2.getLicenseId(), result[i].getIdB());
				assertEquals(comment1, result[i].getCommentA());
				assertEquals(comment1, result[i].getCommentB());
				assertEquals(name1, result[i].getLicenseNameA());
				assertEquals(differentName, result[i].getLicenseNameB());
				assertEquals(text1, result[i].getLicenseText());
				assertTrue(stringsSame(crossReff1, result[i].getSourceUrlsA()));
				assertTrue(stringsSame(crossReff1, result[i].getSourceUrlsB()));
			}else if (result[i].getIdA().equals(lic3_1.getLicenseId())) {
				lic3Found = true;
				assertEquals(lic3_2.getLicenseId(), result[i].getIdB());
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
			if (result[i].getIdB().equals(lic2_2.getLicenseId())) {
				lic2Found = true;
				assertEquals(lic2_1.getLicenseId(), result[i].getIdB());
				assertEquals(differentComment, result[i].getCommentA());
				assertEquals(comment2, result[i].getCommentB());
				assertEquals(name2, result[i].getLicenseNameA());
				assertEquals(name2, result[i].getLicenseNameB());
				assertEquals(text2, result[i].getLicenseText());
				assertTrue(stringsSame(crossReff2, result[i].getSourceUrlsA()));
				assertTrue(stringsSame(crossReff2, result[i].getSourceUrlsB()));
			} else if (result[i].getIdA().equals(lic1_2.getLicenseId())) {
				lic1Found = true;
				assertEquals(lic1_1.getLicenseId(), result[i].getIdB());
				assertEquals(comment1, result[i].getCommentA());
				assertEquals(comment1, result[i].getCommentB());
				assertEquals(differentName, result[i].getLicenseNameA());
				assertEquals(name1, result[i].getLicenseNameB());
				assertEquals(text1, result[i].getLicenseText());
				assertTrue(stringsSame(crossReff1, result[i].getSourceUrlsA()));
				assertTrue(stringsSame(crossReff1, result[i].getSourceUrlsB()));
			}else if (result[i].getIdA().equals(lic3_2.getLicenseId())) {
				lic3Found = true;
				assertEquals(lic3_1.getLicenseId(), result[i].getIdB());
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
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isPackagesEquals());
		assertFalse(comparer.isDifferenceFound());
		((SpdxPackage)(doc1.getDocumentDescribes()[0])).setDescription("Different Description");
		comparer.compare(doc1, doc2);
		assertFalse(comparer.isPackagesEquals());
		assertTrue(comparer.isDifferenceFound());
		// note - other test cases will test to make sure isPackageEquals is set for all changes where it should be false
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxComparer#isCreatorInformationEqual()}.
	 */
	@Test
	public void testIsCreatorInformationEqual() throws IOException, InvalidSPDXAnalysisException, SpdxCompareException  {
		SpdxComparer comparer = new SpdxComparer();
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		assertTrue(comparer.isCreatorInformationEqual());
		assertFalse(comparer.isDifferenceFound());

		// one more creator
		SPDXCreatorInformation origCreators = doc1.getCreationInfo();
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
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
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
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		// need to remove the file dependencies to prevent values from getting over-written
		
		alterExtractedLicenseInfoIds(doc2, 1);
		comparer.compare(doc1, doc2);
		SpdxFileDifference[] differences = comparer.getFileDifferences(0, 1);
		assertEquals(0, differences.length);
		SpdxFile[] doc1files = ((SpdxPackage)doc1.getDocumentDescribes()[0]).getFiles();
		SpdxFile[] files = new SpdxFile[doc1files.length];
		for (int i = 0; i < doc1files.length; i++) {
			files[i] = doc1files[i].clone();
		}
		String file0Name = files[0].getName();
		files[0] = new SpdxFile(files[0].getName(), files[0].getFileTypes(), files[0].getSha1(),files[0].getLicenseConcluded(),
				files[0].getLicenseInfoFromFiles(), files[0].getLicenseComments(), files[0].getCopyrightText(), files[0].getArtifactOf(), files[0].getComment());
		files[0].setComment("a new and unique comment");
		String file1Name = files[1].getName();
		files[1] = new SpdxFile(files[1].getName(), files[1].getFileTypes(), files[1].getSha1(),files[1].getLicenseConcluded(),
				files[1].getLicenseInfoFromFiles(), files[1].getLicenseComments(), files[1].getCopyrightText(), files[1].getArtifactOf(), files[1].getComment());
		files[1].setLicenseConcluded(LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0));
		((SpdxPackage)(doc2.getDocumentDescribes()[0])).setFiles(files);
		comparer.compare(doc1, doc2);
		differences = comparer.getFileDifferences(0, 1);
		assertEquals(2, differences.length);
		if (differences[0].getFileName().equals(file0Name)) {
			assertFalse(differences[0].isCommentsEquals());
			assertEquals(differences[0].getCommentB(),files[0].getComment());
		} else if (differences[0].getFileName().equals(file1Name)) {
			assertFalse(differences[0].isCommentsEquals());
			assertEquals(((License)(files[1].getLicenseConcluded())).getLicenseId(), 
					(differences[0].getConcludedLicenseB()));
		} else {
			fail("invalid file name");
		}
		if (differences[1].getFileName().equals(file0Name)) {
			assertFalse(differences[1].isCommentsEquals());
			assertEquals(differences[1].getCommentB(),files[0].getComment());
		} else if (differences[1].getFileName().equals(file1Name)) {
			assertFalse(differences[1].isConcludedLicenseEquals());
			assertEquals(((License)(files[1].getLicenseConcluded())).getLicenseId(), differences[1].getConcludedLicenseB());
		} else {
			fail ("Invalid file name");
		}
		
		comparer.compare(doc2, doc1);
		differences = comparer.getFileDifferences(0, 1);
		assertEquals(2, differences.length);
		if (differences[0].getFileName().equals(file0Name)) {
			assertFalse(differences[0].isCommentsEquals());
			assertEquals(differences[0].getCommentA(),files[0].getComment());
		} else if (differences[0].getFileName().equals(file1Name)) {
			assertFalse(differences[0].isConcludedLicenseEquals());
			assertEquals(((License)(files[1].getLicenseConcluded())).getLicenseId(), differences[0].getConcludedLicenseA());
		} else {
			fail("invalid file name");
		}
		if (differences[1].getFileName().equals(file0Name)) {
			assertFalse(differences[1].isCommentsEquals());
			assertEquals(differences[1].getCommentA(),files[0].getComment());
		} else if (differences[1].getFileName().equals(file1Name)) {
			assertFalse(differences[1].isConcludedLicenseEquals());
			assertEquals(((License)(files[1].getLicenseConcluded())).getLicenseId(), differences[1].getConcludedLicenseA());
		} else {
			fail ("Invalid file name");
		}
	}
	
	@Test
	public void testGetUniqueFiles() throws InvalidSPDXAnalysisException, IOException, SpdxCompareException {
		SpdxFile[] pkgAFiles = new SpdxFile[] {FILE1A};
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, pkgAFiles, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxFile[] pkgBFiles = new SpdxFile[] {FILE3B};
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, pkgBFiles, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxItem[] itemsA = new SpdxItem[] {FILE2A, pkgA};
		SpdxItem[] itemsB = new SpdxItem[] {FILE2B, pkgB};
		SpdxDocumentContainer containerA = new SpdxDocumentContainer(DOC_URIA);
		SpdxDocumentContainer containerB = new SpdxDocumentContainer(DOC_URIB);
		SpdxDocument docA = containerA.getSpdxDocument();
		SpdxDocument docB = containerB.getSpdxDocument();
		docA.setExtractedLicenseInfos(EXTRACTED_LICENSESA);
		docB.setExtractedLicenseInfos(EXTRACTED_LICENSESB);
		docA.setName(DOC_NAMEA);
		docB.setName(DOC_NAMEB);
		docA.setCreationInfo(CREATION_INFOA);
		docB.setCreationInfo(CREATION_INFOB);
		docA.setDocumentDescribes(itemsA);
		docB.setDocumentDescribes(itemsB);

		SpdxComparer comparer = new SpdxComparer();
		comparer.compare(docA, docB);
		SpdxFile[] result = comparer.getUniqueFiles(0, 1);
		assertEquals(1, result.length);
		assertEquals(FILE1A, result[0]);
		result = comparer.getUniqueFiles(1, 0);
		assertEquals(1, result.length);
		assertEquals(FILE3B, result[0]);
	}
	
	@Test
	public void testGetFileDifferences2()throws InvalidSPDXAnalysisException, IOException, SpdxCompareException {
		SpdxFile[] pkgAFiles = new SpdxFile[] {FILE1A, FILE2A, FILE3A};
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, pkgAFiles, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxFile[] pkgBFiles = new SpdxFile[] {FILE1B_DIFF_CHECKSUM, FILE2B, FILE3B};
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, pkgBFiles, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxItem[] itemsA = new SpdxItem[] {pkgA};
		SpdxItem[] itemsB = new SpdxItem[] {pkgB};
		SpdxDocumentContainer containerA = new SpdxDocumentContainer(DOC_URIA);
		SpdxDocumentContainer containerB = new SpdxDocumentContainer(DOC_URIB);
		SpdxDocument docA = containerA.getSpdxDocument();
		SpdxDocument docB = containerB.getSpdxDocument();
		docA.setExtractedLicenseInfos(EXTRACTED_LICENSESA);
		docB.setExtractedLicenseInfos(EXTRACTED_LICENSESB);
		docA.setName(DOC_NAMEA);
		docB.setName(DOC_NAMEB);
		docA.setCreationInfo(CREATION_INFOA);
		docB.setCreationInfo(CREATION_INFOB);
		docA.setDocumentDescribes(itemsA);
		docB.setDocumentDescribes(itemsB);

		SpdxComparer comparer = new SpdxComparer();
		comparer.compare(docA, docB);
		SpdxFileDifference[] result = comparer.getFileDifferences(0, 1);
		assertEquals(1, result.length);
		assertEquals(FILE1A.getName(), result[0].getName());
		assertFalse(result[0].isChecksumsEquals());
		result = comparer.getFileDifferences(1, 0);
		assertEquals(1, result.length);
		assertEquals(FILE1A.getName(), result[0].getName());
		assertFalse(result[0].isChecksumsEquals());
	}
	
	@Test
	public void testGetPackageDifferences()throws InvalidSPDXAnalysisException, IOException, SpdxCompareException {
		SpdxPackage pkgA1 = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgA2 = new SpdxPackage(NAMEB, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB1 = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESB_SAME, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB2 = new SpdxPackage(NAMEB, COMMENTB, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESB_SAME, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		
		SpdxItem[] itemsA = new SpdxItem[] {pkgA1, pkgA2};
		SpdxItem[] itemsB = new SpdxItem[] {pkgB1, pkgB2};
		SpdxDocumentContainer containerA = new SpdxDocumentContainer(DOC_URIA);
		SpdxDocumentContainer containerB = new SpdxDocumentContainer(DOC_URIB);
		SpdxDocument docA = containerA.getSpdxDocument();
		SpdxDocument docB = containerB.getSpdxDocument();
		docA.setExtractedLicenseInfos(EXTRACTED_LICENSESA);
		docB.setExtractedLicenseInfos(EXTRACTED_LICENSESB);
		docA.setName(DOC_NAMEA);
		docB.setName(DOC_NAMEB);
		docA.setCreationInfo(CREATION_INFOA);
		docB.setCreationInfo(CREATION_INFOB);
		docA.setDocumentDescribes(itemsA);
		docB.setDocumentDescribes(itemsB);

		SpdxComparer comparer = new SpdxComparer();
		comparer.compare(docA, docB);
		SpdxPackageComparer[] result = comparer.getPackageDifferences();
		assertEquals(1, result.length);
		assertEquals(NAMEB, result[0].getDocPackage(docA).getName());
		assertFalse(result[0].isCommentsEquals());
		result = comparer.getPackageDifferences();
		assertEquals(1, result.length);
		assertEquals(NAMEB, result[0].getDocPackage(docA).getName());
		assertFalse(result[0].isCommentsEquals());
	}
	
	@Test
	public void testGetUniquePackages()throws InvalidSPDXAnalysisException, IOException, SpdxCompareException {
		SpdxPackage pkgA1 = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgA2 = new SpdxPackage(NAMEB, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB1 = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESB_SAME, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB2 = new SpdxPackage(NAMEC, COMMENTB, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESB_SAME, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		
		SpdxItem[] itemsA = new SpdxItem[] {pkgA1, pkgA2};
		SpdxItem[] itemsB = new SpdxItem[] {pkgB1, pkgB2};
		SpdxDocumentContainer containerA = new SpdxDocumentContainer(DOC_URIA);
		SpdxDocumentContainer containerB = new SpdxDocumentContainer(DOC_URIB);
		SpdxDocument docA = containerA.getSpdxDocument();
		SpdxDocument docB = containerB.getSpdxDocument();
		docA.setExtractedLicenseInfos(EXTRACTED_LICENSESA);
		docB.setExtractedLicenseInfos(EXTRACTED_LICENSESB);
		docA.setName(DOC_NAMEA);
		docB.setName(DOC_NAMEB);
		docA.setCreationInfo(CREATION_INFOA);
		docB.setCreationInfo(CREATION_INFOB);
		docA.setDocumentDescribes(itemsA);
		docB.setDocumentDescribes(itemsB);

		SpdxComparer comparer = new SpdxComparer();
		comparer.compare(docA, docB);
		SpdxPackage[] result = comparer.getUniquePackages(0, 1);
		assertEquals(1, result.length);
		assertEquals(pkgA2, result[0]);
		result = comparer.getUniquePackages(1, 0);
		assertEquals(1, result.length);
		assertEquals(pkgB2, result[0]);
		
	}
	
	@Test
	public void testElementEquivalent() {
		String nameA = "A";
		String nameB = "B";
		SpdxElement element1A = new SpdxElement(nameA, null, null, null);
		SpdxElement element2A = new SpdxElement(nameA, null, null, null);
		SpdxElement element3A = new SpdxElement(nameA, null, null, null);
		SpdxElement element4B = new SpdxElement(nameB, null, null, null);
		
		assertTrue(SpdxComparer.elementsEquivalent(new SpdxElement[] {
				element1A, element2A}, new SpdxElement[] {
						element2A, element3A}));
		assertFalse(SpdxComparer.elementsEquivalent(new SpdxElement[] {
				element1A, element2A}, new SpdxElement[] {
						element2A, element4B}));
	}

	@Test
	public void testFindUniqueChecksums() {
		Checksum[] checksumsA = new Checksum[] {CHECKSUM1, CHECKSUM2, CHECKSUM3};
		Checksum[] checksumsB = new Checksum[] {CHECKSUM2, CHECKSUM3, CHECKSUM4};
		Checksum[] result = SpdxComparer.findUniqueChecksums(checksumsA, checksumsB);
		assertEquals(1, result.length);
		assertEquals(CHECKSUM1, result[0]);
	}
	@Test
	public void testCollectAllFiles() throws InvalidSPDXAnalysisException {
		SpdxFile[] pkgAFiles = new SpdxFile[] {FILE1A, FILE2A};
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, pkgAFiles, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxFile[] pkgBFiles = new SpdxFile[] {FILE3B};
		SpdxPackage pkgB = new SpdxPackage(NAMEB, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, pkgBFiles, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxItem[] itemsA = new SpdxItem[] {pkgA, pkgB, FILE4A};
		SpdxFile[] expected = new SpdxFile[] {FILE1A, FILE2A, FILE3B, FILE4A};
		SpdxDocumentContainer containerA = new SpdxDocumentContainer(DOC_URIA);
		SpdxDocument docA = containerA.getSpdxDocument();
		docA.setExtractedLicenseInfos(EXTRACTED_LICENSESA);
		docA.setName(DOC_NAMEA);
		docA.setCreationInfo(CREATION_INFOA);
		docA.setDocumentDescribes(itemsA);

		SpdxComparer comparer = new SpdxComparer();
		SpdxFile[] result = comparer.collectAllFiles(docA);
		assertTrue(UnitTestHelper.isArraysEqual(expected, result));
	}
	
	@Test
	public void testExternalDocumentRefsEqual() throws InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESB, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxItem[] itemsA = new SpdxItem[] {pkgA};
		SpdxItem[] itemsB = new SpdxItem[] {pkgB};
		SpdxDocumentContainer containerA = new SpdxDocumentContainer(DOC_URIA);
		SpdxDocumentContainer containerB = new SpdxDocumentContainer(DOC_URIB);
		SpdxDocument docA = containerA.getSpdxDocument();
		SpdxDocument docB = containerB.getSpdxDocument();
		docA.setExtractedLicenseInfos(EXTRACTED_LICENSESA);
		docB.setExtractedLicenseInfos(EXTRACTED_LICENSESB);
		docA.setName(DOC_NAMEA);
		docB.setName(DOC_NAMEB);
		docA.setCreationInfo(CREATION_INFOA);
		docB.setCreationInfo(CREATION_INFOB);
		docA.setDocumentDescribes(itemsA);
		docB.setDocumentDescribes(itemsB);

		ExternalDocumentRef ref1 = new ExternalDocumentRef("http://namespace/one", CHECKSUM1, "SPDXDocumentRef-1");
		ExternalDocumentRef ref2 = new ExternalDocumentRef("http://namespace/two", CHECKSUM2, "SPDXDocumentRef-2");
		ExternalDocumentRef ref3 = new ExternalDocumentRef("http://namespace/three", CHECKSUM3, "SPDXDocumentRef-3");
		docA.setExternalDocumentRefs(new ExternalDocumentRef[] {ref1, ref2} );
		docB.setExternalDocumentRefs(new ExternalDocumentRef[] { ref2, ref3 });
		SpdxComparer comparer = new SpdxComparer();
		comparer.compare(docA, docB);
		assertTrue(comparer.isDifferenceFound());
		assertFalse(comparer.isExternalDcoumentRefsEquals());
		ExternalDocumentRef[] result = comparer.getUniqueExternalDocumentRefs(0, 1);
		assertEquals(1, result.length);
		assertEquals(ref1, result[0]);
		result = comparer.getUniqueExternalDocumentRefs(1, 0);
		assertEquals(1, result.length);
		assertEquals(ref3, result[0]);
	}
	
	@Test
	public void testDocumentAnnotationsEquals() throws InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESB, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxItem[] itemsA = new SpdxItem[] {pkgA};
		SpdxItem[] itemsB = new SpdxItem[] {pkgB};
		SpdxDocumentContainer containerA = new SpdxDocumentContainer(DOC_URIA);
		SpdxDocumentContainer containerB = new SpdxDocumentContainer(DOC_URIB);
		SpdxDocument docA = containerA.getSpdxDocument();
		SpdxDocument docB = containerB.getSpdxDocument();
		docA.setExtractedLicenseInfos(EXTRACTED_LICENSESA);
		docB.setExtractedLicenseInfos(EXTRACTED_LICENSESB);
		docA.setName(DOC_NAMEA);
		docB.setName(DOC_NAMEB);
		docA.setCreationInfo(CREATION_INFOA);
		docB.setCreationInfo(CREATION_INFOB);
		docA.setDocumentDescribes(itemsA);
		docB.setDocumentDescribes(itemsB);

		docA.setAnnotations(new Annotation[] {ANNOTATION1, ANNOTATION2});
		docB.setAnnotations(new Annotation[] {ANNOTATION2, ANNOTATION3});
		SpdxComparer comparer = new SpdxComparer();
		comparer.compare(docA, docB);
		assertTrue(comparer.isDifferenceFound());
		assertFalse(comparer.isDocumentAnnotationsEquals());
		Annotation[] result = comparer.getUniqueDocumentAnnotations(0, 1);
		assertEquals(1, result.length);
		assertEquals(ANNOTATION1, result[0]);
		result = comparer.getUniqueDocumentAnnotations(1, 0);
		assertEquals(1, result.length);
		assertEquals(ANNOTATION3, result[0]);
	}
	@Test
	public void testDocumentRelationshipsEquals() throws InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESB, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxItem[] itemsA = new SpdxItem[] {pkgA};
		SpdxItem[] itemsB = new SpdxItem[] {pkgB};
		SpdxDocumentContainer containerA = new SpdxDocumentContainer(DOC_URIA);
		SpdxDocumentContainer containerB = new SpdxDocumentContainer(DOC_URIB);
		SpdxDocument docA = containerA.getSpdxDocument();
		SpdxDocument docB = containerB.getSpdxDocument();
		docA.setExtractedLicenseInfos(EXTRACTED_LICENSESA);
		docB.setExtractedLicenseInfos(EXTRACTED_LICENSESB);
		docA.setName(DOC_NAMEA);
		docB.setName(DOC_NAMEB);
		docA.setCreationInfo(CREATION_INFOA);
		docB.setCreationInfo(CREATION_INFOB);
		docA.setDocumentDescribes(itemsA);
		docB.setDocumentDescribes(itemsB);
		docA.setRelationships(new Relationship[] {RELATIONSHIP1, RELATIONSHIP2});
		docB.setRelationships(new Relationship[] {RELATIONSHIP2, RELATIONSHIP3});
		SpdxComparer comparer = new SpdxComparer();
		comparer.compare(docA, docB);
		assertTrue(comparer.isDifferenceFound());
		assertFalse(comparer.isDocumentRelationshipsEquals());
		Relationship[] result= comparer.getUniqueDocumentRelationship(0, 1);
		assertEquals(1, result.length);
		assertEquals(RELATIONSHIP1, result[0]);
		result = comparer.getUniqueDocumentRelationship(1, 0);
		assertEquals(1, result.length);
		assertEquals(RELATIONSHIP3, result[0]);
	}
	
	@Test
	public void testSpdxDocumentContentsEquals() throws InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxItem[] itemsA = new SpdxItem[] {pkgA};
		SpdxItem[] itemsB = new SpdxItem[] {pkgB};
		SpdxDocumentContainer containerA = new SpdxDocumentContainer(DOC_URIA);
		SpdxDocumentContainer containerB = new SpdxDocumentContainer(DOC_URIB);
		SpdxDocument docA = containerA.getSpdxDocument();
		SpdxDocument docB = containerB.getSpdxDocument();
		docA.setName(DOC_NAMEA);
		docB.setName(DOC_NAMEB);
		docA.setExtractedLicenseInfos(EXTRACTED_LICENSESA);
		docB.setExtractedLicenseInfos(EXTRACTED_LICENSESA);
		docA.setCreationInfo(CREATION_INFOA);
		docB.setCreationInfo(CREATION_INFOA);
		docA.setDocumentDescribes(itemsA);
		docB.setDocumentDescribes(itemsB);
		assertTrue(pkgA.equivalent(pkgB));
		SpdxComparer comparer = new SpdxComparer();
		comparer.compare(docA, docB);
		assertFalse(comparer.isDifferenceFound());
		assertTrue(comparer.isDocumentContentsEquals());
		SpdxPackage pkgC = new SpdxPackage(NAMEB, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESB, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		assertFalse(pkgA.equivalent(pkgC));
		SpdxDocumentContainer containerC = new SpdxDocumentContainer(DOC_URIC);
		SpdxDocument docC = containerC.getSpdxDocument();
		docC.setExtractedLicenseInfos(EXTRACTED_LICENSESB);
		docC.setName(DOC_NAMEA);
		docC.setCreationInfo(CREATION_INFOA);
		docC.setDocumentDescribes(new SpdxItem[] {pkgC});
		comparer = new SpdxComparer();
		comparer.compare(new SpdxDocument[] {docA, docB, docC});
		assertTrue(comparer.isDifferenceFound());
		assertFalse(comparer.isDocumentContentsEquals());
	}
}
