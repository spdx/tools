/**
 * Copyright (c) 2015 Source Auditor Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Annotation.AnnotationType;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.Checksum.ChecksumAlgorithm;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.Relationship.RelationshipType;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxElement;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxFile.FileType;
import org.spdx.rdfparser.model.SpdxPackage;

import com.google.common.collect.Maps;

/**
 * @author Gary
 *
 */
public class SpdxPackageComparerTest {

	private static final String COMMENTA = "comment A";
	@SuppressWarnings("unused")
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
	private static final AnyLicenseInfo LICENSE_DECLAREDA = LICENSEA2;
	private static final AnyLicenseInfo LICENSE_DECLAREDB = LICENSEB2;
	private static final String ORIGINATORA = "Organization: OrgA";
	private static final String ORIGINATORB = "Organization: OrgB";
	private static final String HOMEPAGEA = "http://home.page/a";
	private static final String HOMEPAGEB = "http://home.page/b";
	private static final String DOWNLOADA = "http://download.page/a";
	private static final String DOWNLOADB = "http://download.page/b";
	private static final String DESCRIPTIONA = "Description A";
	private static final String DESCRIPTIONB = "Description B";
	private static final String PACKAGE_FILENAMEA = "packageFileNameA";
	@SuppressWarnings("unused")
	private static final String PACKAGE_FILENAMEB = "packageFileNameB";
	private static final String SOURCEINFOA = "Sourc info A";
	private static final String SOURCEINFOB = "Sourc info B";
	private static final String SUMMARYA = "Summary A";
	private static final String SUMMARYB = "Summary B";
	private static final String VERSIONINFOA = "Version A";
	private static final String VERSIONINFOB = "Version B";
	private static final String SUPPLIERA = "Person: Supplier A";
	private static final String SUPPLIERB = "Person: Supplier B";
	private static final Map<String, String> LICENSE_XLATION_MAPAB = Maps.newHashMap();
	
	static {
		LICENSE_XLATION_MAPAB.put("LicenseRef-1", "LicenseRef-4");
		LICENSE_XLATION_MAPAB.put("LicenseRef-2", "LicenseRef-5");
		LICENSE_XLATION_MAPAB.put("LicenseRef-3", "LicenseRef-6");
	}
	
	private static final Map<String, String> LICENSE_XLATION_MAPBA = Maps.newHashMap();
	
	static {
		LICENSE_XLATION_MAPBA.put("LicenseRef-4", "LicenseRef-1");
		LICENSE_XLATION_MAPBA.put("LicenseRef-5", "LicenseRef-2");
		LICENSE_XLATION_MAPBA.put("LicenseRef-6", "LicenseRef-3");
	}
	
	private final Map<SpdxDocument, Map<SpdxDocument, Map<String, String>>> LICENSE_XLATION_MAP = Maps.newHashMap();

	private SpdxDocument DOCA;
	private SpdxDocument DOCB;
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
	private SpdxFile FILE1A;
	private SpdxFile FILE1B;
	private SpdxFile FILE1B_DIFF_CHECKSUM;
	private SpdxFile FILE2A;
	private SpdxFile FILE3A;
	private SpdxFile FILE2B;
	private SpdxFile FILE3B;
	private SpdxFile[] FILESA;
	private SpdxFile[] FILESB;
	private SpdxFile[] FILESB_SAME;
	private SpdxPackageVerificationCode VERIFICATION_CODEA;
	private SpdxPackageVerificationCode VERIFICATION_CODEB;
	
	
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
		String uri1 = "http://doc/uri1";
		SpdxDocumentContainer containerA = new SpdxDocumentContainer(uri1);
		DOCA = containerA.getSpdxDocument();
		String uri2 = "http://doc/uri2";
		SpdxDocumentContainer containerB = new SpdxDocumentContainer(uri2);
		DOCB = containerB.getSpdxDocument();
		Map<SpdxDocument, Map<String, String>> bmap = Maps.newHashMap();
		bmap.put(DOCB, LICENSE_XLATION_MAPAB);
		LICENSE_XLATION_MAP.put(DOCA, bmap);
		Map<SpdxDocument, Map<String, String>> amap = Maps.newHashMap();
		amap.put(DOCA, LICENSE_XLATION_MAPBA);
		LICENSE_XLATION_MAP.put(DOCB, amap);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#compare(org.spdx.rdfparser.model.SpdxPackage, org.spdx.rdfparser.model.SpdxPackage, java.util.HashMap)}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testCompareSpdxPackageSpdxPackageHashMapOfStringString() throws SpdxCompareException {
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESB_SAME, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertFalse(pc.isDifferenceFound());
		assertTrue(pc.isAnnotationsEquals());
		assertTrue(pc.isCommentsEquals());
		assertTrue(pc.isConcludedLicenseEquals());
		assertTrue(pc.isCopyrightsEquals());
		assertTrue(pc.isDeclaredLicensesEquals());
		assertTrue(pc.isLicenseCommmentsEquals());
		assertTrue(pc.isPackageChecksumsEquals());
		assertTrue(pc.isPackageDescriptionsEquals());
		assertTrue(pc.isPackageDownloadLocationsEquals());
		assertTrue(pc.isPackageFilenamesEquals());
		assertTrue(pc.isPackageFilesEquals());
		assertTrue(pc.isPackageHomePagesEquals());
		assertTrue(pc.isPackageOriginatorsEqual());
		assertTrue(pc.isPackageSourceInfosEquals());
		assertTrue(pc.isPackageSummaryEquals());
		assertTrue(pc.isPackageSuppliersEquals());
		assertTrue(pc.isPackageVerificationCodesEquals());
		assertTrue(pc.isPackageVersionsEquals());
		assertTrue(pc.isRelationshipsEquals());
		assertTrue(pc.isSeenLicenseEquals());	
		assertEquals(0, pc.getUniqueChecksums(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueChecksums(DOCB, DOCA).length);
		assertEquals(0, pc.getUniqueFiles(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueFiles(DOCB, DOCA).length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#isPackageVersionsEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsPackageVersionsEquals() throws SpdxCompareException {
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESB_SAME, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOB);
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertTrue(pc.isDifferenceFound());
		assertTrue(pc.isAnnotationsEquals());
		assertTrue(pc.isCommentsEquals());
		assertTrue(pc.isConcludedLicenseEquals());
		assertTrue(pc.isCopyrightsEquals());
		assertTrue(pc.isDeclaredLicensesEquals());
		assertTrue(pc.isLicenseCommmentsEquals());
		assertTrue(pc.isPackageChecksumsEquals());
		assertTrue(pc.isPackageDescriptionsEquals());
		assertTrue(pc.isPackageDownloadLocationsEquals());
		assertTrue(pc.isPackageFilenamesEquals());
		assertTrue(pc.isPackageFilesEquals());
		assertTrue(pc.isPackageHomePagesEquals());
		assertTrue(pc.isPackageOriginatorsEqual());
		assertTrue(pc.isPackageSourceInfosEquals());
		assertTrue(pc.isPackageSummaryEquals());
		assertTrue(pc.isPackageSuppliersEquals());
		assertTrue(pc.isPackageVerificationCodesEquals());
		assertFalse(pc.isPackageVersionsEquals());
		assertTrue(pc.isRelationshipsEquals());
		assertTrue(pc.isSeenLicenseEquals());	
		assertEquals(0, pc.getUniqueChecksums(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueChecksums(DOCB, DOCA).length);
		assertEquals(0, pc.getUniqueFiles(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueFiles(DOCB, DOCA).length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#isPackageSuppliersEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsPackageSuppliersEquals() throws SpdxCompareException {
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESB_SAME, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERB, VERSIONINFOA);
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertTrue(pc.isDifferenceFound());
		assertTrue(pc.isAnnotationsEquals());
		assertTrue(pc.isCommentsEquals());
		assertTrue(pc.isConcludedLicenseEquals());
		assertTrue(pc.isCopyrightsEquals());
		assertTrue(pc.isDeclaredLicensesEquals());
		assertTrue(pc.isLicenseCommmentsEquals());
		assertTrue(pc.isPackageChecksumsEquals());
		assertTrue(pc.isPackageDescriptionsEquals());
		assertTrue(pc.isPackageDownloadLocationsEquals());
		assertTrue(pc.isPackageFilenamesEquals());
		assertTrue(pc.isPackageFilesEquals());
		assertTrue(pc.isPackageHomePagesEquals());
		assertTrue(pc.isPackageOriginatorsEqual());
		assertTrue(pc.isPackageSourceInfosEquals());
		assertTrue(pc.isPackageSummaryEquals());
		assertFalse(pc.isPackageSuppliersEquals());
		assertTrue(pc.isPackageVerificationCodesEquals());
		assertTrue(pc.isPackageVersionsEquals());
		assertTrue(pc.isRelationshipsEquals());
		assertTrue(pc.isSeenLicenseEquals());	
		assertEquals(0, pc.getUniqueChecksums(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueChecksums(DOCB, DOCA).length);
		assertEquals(0, pc.getUniqueFiles(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueFiles(DOCB, DOCA).length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#isPackageDownloadLocationsEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsPackageDownloadLocationsEquals() throws SpdxCompareException {
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADB, FILESB_SAME, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertTrue(pc.isDifferenceFound());
		assertTrue(pc.isAnnotationsEquals());
		assertTrue(pc.isCommentsEquals());
		assertTrue(pc.isConcludedLicenseEquals());
		assertTrue(pc.isCopyrightsEquals());
		assertTrue(pc.isDeclaredLicensesEquals());
		assertTrue(pc.isLicenseCommmentsEquals());
		assertTrue(pc.isPackageChecksumsEquals());
		assertTrue(pc.isPackageDescriptionsEquals());
		assertFalse(pc.isPackageDownloadLocationsEquals());
		assertTrue(pc.isPackageFilenamesEquals());
		assertTrue(pc.isPackageFilesEquals());
		assertTrue(pc.isPackageHomePagesEquals());
		assertTrue(pc.isPackageOriginatorsEqual());
		assertTrue(pc.isPackageSourceInfosEquals());
		assertTrue(pc.isPackageSummaryEquals());
		assertTrue(pc.isPackageSuppliersEquals());
		assertTrue(pc.isPackageVerificationCodesEquals());
		assertTrue(pc.isPackageVersionsEquals());
		assertTrue(pc.isRelationshipsEquals());
		assertTrue(pc.isSeenLicenseEquals());	
		assertEquals(0, pc.getUniqueChecksums(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueChecksums(DOCB, DOCA).length);
		assertEquals(0, pc.getUniqueFiles(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueFiles(DOCB, DOCA).length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#isPackageVerificationCodesEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsPackageVerificationCodesEquals() throws SpdxCompareException {
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESB_SAME, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEB, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertTrue(pc.isDifferenceFound());
		assertTrue(pc.isAnnotationsEquals());
		assertTrue(pc.isCommentsEquals());
		assertTrue(pc.isConcludedLicenseEquals());
		assertTrue(pc.isCopyrightsEquals());
		assertTrue(pc.isDeclaredLicensesEquals());
		assertTrue(pc.isLicenseCommmentsEquals());
		assertTrue(pc.isPackageChecksumsEquals());
		assertTrue(pc.isPackageDescriptionsEquals());
		assertTrue(pc.isPackageDownloadLocationsEquals());
		assertTrue(pc.isPackageFilenamesEquals());
		assertTrue(pc.isPackageFilesEquals());
		assertTrue(pc.isPackageHomePagesEquals());
		assertTrue(pc.isPackageOriginatorsEqual());
		assertTrue(pc.isPackageSourceInfosEquals());
		assertTrue(pc.isPackageSummaryEquals());
		assertTrue(pc.isPackageSuppliersEquals());
		assertFalse(pc.isPackageVerificationCodesEquals());
		assertTrue(pc.isPackageVersionsEquals());
		assertTrue(pc.isRelationshipsEquals());
		assertTrue(pc.isSeenLicenseEquals());	
		assertEquals(0, pc.getUniqueChecksums(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueChecksums(DOCB, DOCA).length);
		assertEquals(0, pc.getUniqueFiles(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueFiles(DOCB, DOCA).length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#isPackageChecksumsEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsPackageChecksumsEquals() throws SpdxCompareException {
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSB,
				DESCRIPTIONA, DOWNLOADA, FILESB_SAME, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertTrue(pc.isDifferenceFound());
		assertTrue(pc.isAnnotationsEquals());
		assertTrue(pc.isCommentsEquals());
		assertTrue(pc.isConcludedLicenseEquals());
		assertTrue(pc.isCopyrightsEquals());
		assertTrue(pc.isDeclaredLicensesEquals());
		assertTrue(pc.isLicenseCommmentsEquals());
		assertFalse(pc.isPackageChecksumsEquals());
		assertTrue(pc.isPackageDescriptionsEquals());
		assertTrue(pc.isPackageDownloadLocationsEquals());
		assertTrue(pc.isPackageFilenamesEquals());
		assertTrue(pc.isPackageFilesEquals());
		assertTrue(pc.isPackageHomePagesEquals());
		assertTrue(pc.isPackageOriginatorsEqual());
		assertTrue(pc.isPackageSourceInfosEquals());
		assertTrue(pc.isPackageSummaryEquals());
		assertTrue(pc.isPackageSuppliersEquals());
		assertTrue(pc.isPackageVerificationCodesEquals());
		assertTrue(pc.isPackageVersionsEquals());
		assertTrue(pc.isRelationshipsEquals());
		assertTrue(pc.isSeenLicenseEquals());	
		assertEquals(2, pc.getUniqueChecksums(DOCA, DOCB).length);
		assertEquals(2, pc.getUniqueChecksums(DOCB, DOCA).length);
		assertEquals(0, pc.getUniqueFiles(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueFiles(DOCB, DOCA).length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#isPackageSourceInfosEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsPackageSourceInfosEquals() throws SpdxCompareException {
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESB_SAME, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOB,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertTrue(pc.isDifferenceFound());
		assertTrue(pc.isAnnotationsEquals());
		assertTrue(pc.isCommentsEquals());
		assertTrue(pc.isConcludedLicenseEquals());
		assertTrue(pc.isCopyrightsEquals());
		assertTrue(pc.isDeclaredLicensesEquals());
		assertTrue(pc.isLicenseCommmentsEquals());
		assertTrue(pc.isPackageChecksumsEquals());
		assertTrue(pc.isPackageDescriptionsEquals());
		assertTrue(pc.isPackageDownloadLocationsEquals());
		assertTrue(pc.isPackageFilenamesEquals());
		assertTrue(pc.isPackageFilesEquals());
		assertTrue(pc.isPackageHomePagesEquals());
		assertTrue(pc.isPackageOriginatorsEqual());
		assertFalse(pc.isPackageSourceInfosEquals());
		assertTrue(pc.isPackageSummaryEquals());
		assertTrue(pc.isPackageSuppliersEquals());
		assertTrue(pc.isPackageVerificationCodesEquals());
		assertTrue(pc.isPackageVersionsEquals());
		assertTrue(pc.isRelationshipsEquals());
		assertTrue(pc.isSeenLicenseEquals());	
		assertEquals(0, pc.getUniqueChecksums(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueChecksums(DOCB, DOCA).length);
		assertEquals(0, pc.getUniqueFiles(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueFiles(DOCB, DOCA).length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#isDeclaredLicensesEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testisDeclaredLicensesEquals() throws SpdxCompareException {
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSEB1, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESB_SAME, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertTrue(pc.isDifferenceFound());
		assertTrue(pc.isAnnotationsEquals());
		assertTrue(pc.isCommentsEquals());
		assertTrue(pc.isConcludedLicenseEquals());
		assertTrue(pc.isCopyrightsEquals());
		assertFalse(pc.isDeclaredLicensesEquals());
		assertTrue(pc.isLicenseCommmentsEquals());
		assertTrue(pc.isPackageChecksumsEquals());
		assertTrue(pc.isPackageDescriptionsEquals());
		assertTrue(pc.isPackageDownloadLocationsEquals());
		assertTrue(pc.isPackageFilenamesEquals());
		assertTrue(pc.isPackageFilesEquals());
		assertTrue(pc.isPackageHomePagesEquals());
		assertTrue(pc.isPackageOriginatorsEqual());
		assertTrue(pc.isPackageSourceInfosEquals());
		assertTrue(pc.isPackageSummaryEquals());
		assertTrue(pc.isPackageSuppliersEquals());
		assertTrue(pc.isPackageVerificationCodesEquals());
		assertTrue(pc.isPackageVersionsEquals());
		assertTrue(pc.isRelationshipsEquals());
		assertTrue(pc.isSeenLicenseEquals());	
		assertEquals(0, pc.getUniqueChecksums(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueChecksums(DOCB, DOCA).length);
		assertEquals(0, pc.getUniqueFiles(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueFiles(DOCB, DOCA).length);
		
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#isPackageSummaryEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsPackageSummaryEquals() throws SpdxCompareException {
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESB_SAME, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYB, SUPPLIERA, VERSIONINFOA);
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertTrue(pc.isDifferenceFound());
		assertTrue(pc.isAnnotationsEquals());
		assertTrue(pc.isCommentsEquals());
		assertTrue(pc.isConcludedLicenseEquals());
		assertTrue(pc.isCopyrightsEquals());
		assertTrue(pc.isDeclaredLicensesEquals());
		assertTrue(pc.isLicenseCommmentsEquals());
		assertTrue(pc.isPackageChecksumsEquals());
		assertTrue(pc.isPackageDescriptionsEquals());
		assertTrue(pc.isPackageDownloadLocationsEquals());
		assertTrue(pc.isPackageFilenamesEquals());
		assertTrue(pc.isPackageFilesEquals());
		assertTrue(pc.isPackageHomePagesEquals());
		assertTrue(pc.isPackageOriginatorsEqual());
		assertTrue(pc.isPackageSourceInfosEquals());
		assertFalse(pc.isPackageSummaryEquals());
		assertTrue(pc.isPackageSuppliersEquals());
		assertTrue(pc.isPackageVerificationCodesEquals());
		assertTrue(pc.isPackageVersionsEquals());
		assertTrue(pc.isRelationshipsEquals());
		assertTrue(pc.isSeenLicenseEquals());	
		assertEquals(0, pc.getUniqueChecksums(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueChecksums(DOCB, DOCA).length);
		assertEquals(0, pc.getUniqueFiles(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueFiles(DOCB, DOCA).length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#isPackageDescriptionsEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsPackageDescriptionsEquals() throws SpdxCompareException {
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONB, DOWNLOADA, FILESB_SAME, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertTrue(pc.isDifferenceFound());
		assertTrue(pc.isAnnotationsEquals());
		assertTrue(pc.isCommentsEquals());
		assertTrue(pc.isConcludedLicenseEquals());
		assertTrue(pc.isCopyrightsEquals());
		assertTrue(pc.isDeclaredLicensesEquals());
		assertTrue(pc.isLicenseCommmentsEquals());
		assertTrue(pc.isPackageChecksumsEquals());
		assertFalse(pc.isPackageDescriptionsEquals());
		assertTrue(pc.isPackageDownloadLocationsEquals());
		assertTrue(pc.isPackageFilenamesEquals());
		assertTrue(pc.isPackageFilesEquals());
		assertTrue(pc.isPackageHomePagesEquals());
		assertTrue(pc.isPackageOriginatorsEqual());
		assertTrue(pc.isPackageSourceInfosEquals());
		assertTrue(pc.isPackageSummaryEquals());
		assertTrue(pc.isPackageSuppliersEquals());
		assertTrue(pc.isPackageVerificationCodesEquals());
		assertTrue(pc.isPackageVersionsEquals());
		assertTrue(pc.isRelationshipsEquals());
		assertTrue(pc.isSeenLicenseEquals());	
		assertEquals(0, pc.getUniqueChecksums(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueChecksums(DOCB, DOCA).length);
		assertEquals(0, pc.getUniqueFiles(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueFiles(DOCB, DOCA).length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#isPackageOriginatorsEqual()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsPackageOriginatorsEqual() throws SpdxCompareException {
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESB_SAME, HOMEPAGEA, ORIGINATORB, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertTrue(pc.isDifferenceFound());
		assertTrue(pc.isAnnotationsEquals());
		assertTrue(pc.isCommentsEquals());
		assertTrue(pc.isConcludedLicenseEquals());
		assertTrue(pc.isCopyrightsEquals());
		assertTrue(pc.isDeclaredLicensesEquals());
		assertTrue(pc.isLicenseCommmentsEquals());
		assertTrue(pc.isPackageChecksumsEquals());
		assertTrue(pc.isPackageDescriptionsEquals());
		assertTrue(pc.isPackageDownloadLocationsEquals());
		assertTrue(pc.isPackageFilenamesEquals());
		assertTrue(pc.isPackageFilesEquals());
		assertTrue(pc.isPackageHomePagesEquals());
		assertFalse(pc.isPackageOriginatorsEqual());
		assertTrue(pc.isPackageSourceInfosEquals());
		assertTrue(pc.isPackageSummaryEquals());
		assertTrue(pc.isPackageSuppliersEquals());
		assertTrue(pc.isPackageVerificationCodesEquals());
		assertTrue(pc.isPackageVersionsEquals());
		assertTrue(pc.isRelationshipsEquals());
		assertTrue(pc.isSeenLicenseEquals());	
		assertEquals(0, pc.getUniqueChecksums(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueChecksums(DOCB, DOCA).length);
		assertEquals(0, pc.getUniqueFiles(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueFiles(DOCB, DOCA).length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#isPackageHomePagesEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsPackageHomePagesEquals() throws SpdxCompareException {
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESB_SAME, HOMEPAGEB, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertTrue(pc.isDifferenceFound());
		assertTrue(pc.isAnnotationsEquals());
		assertTrue(pc.isCommentsEquals());
		assertTrue(pc.isConcludedLicenseEquals());
		assertTrue(pc.isCopyrightsEquals());
		assertTrue(pc.isDeclaredLicensesEquals());
		assertTrue(pc.isLicenseCommmentsEquals());
		assertTrue(pc.isPackageChecksumsEquals());
		assertTrue(pc.isPackageDescriptionsEquals());
		assertTrue(pc.isPackageDownloadLocationsEquals());
		assertTrue(pc.isPackageFilenamesEquals());
		assertTrue(pc.isPackageFilesEquals());
		assertFalse(pc.isPackageHomePagesEquals());
		assertTrue(pc.isPackageOriginatorsEqual());
		assertTrue(pc.isPackageSourceInfosEquals());
		assertTrue(pc.isPackageSummaryEquals());
		assertTrue(pc.isPackageSuppliersEquals());
		assertTrue(pc.isPackageVerificationCodesEquals());
		assertTrue(pc.isPackageVersionsEquals());
		assertTrue(pc.isRelationshipsEquals());
		assertTrue(pc.isSeenLicenseEquals());	
		assertEquals(0, pc.getUniqueChecksums(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueChecksums(DOCB, DOCA).length);
		assertEquals(0, pc.getUniqueFiles(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueFiles(DOCB, DOCA).length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#getPkgA()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testGetPkg() throws SpdxCompareException {
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, FILESB_SAME, HOMEPAGEB, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertEquals(pkgA, pc.getDocPackage(DOCA));
		assertEquals(pkgB, pc.getDocPackage(DOCB));
	}
	

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#getUniqueChecksumsA()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testGetUniqueChecksumsA() throws SpdxCompareException {
		Checksum[] checksumsA = new Checksum[] {CHECKSUM1, CHECKSUM2};
		Checksum[] checksumsB = new Checksum[] {CHECKSUM2, CHECKSUM3};
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, checksumsA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, checksumsB,
				DESCRIPTIONA, DOWNLOADA, FILESB_SAME, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertTrue(pc.isDifferenceFound());
		assertFalse(pc.isPackageChecksumsEquals());
		assertEquals(1, pc.getUniqueChecksums(DOCB, DOCA).length);
		Checksum[] result = pc.getUniqueChecksums(DOCA, DOCB);
		assertEquals(1, result.length);
		assertEquals(CHECKSUM1, result[0]);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#getUniqueChecksumsB()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testGetUniqueChecksumsB() throws SpdxCompareException {
		Checksum[] checksumsA = new Checksum[] {CHECKSUM1, CHECKSUM2};
		Checksum[] checksumsB = new Checksum[] {CHECKSUM2, CHECKSUM3};
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, checksumsA,
				DESCRIPTIONA, DOWNLOADA, FILESA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, checksumsB,
				DESCRIPTIONA, DOWNLOADA, FILESB_SAME, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertTrue(pc.isDifferenceFound());
		assertFalse(pc.isPackageChecksumsEquals());
		assertEquals(1, pc.getUniqueChecksums(DOCA, DOCB).length);
		Checksum[] result = pc.getUniqueChecksums(DOCB, DOCA);
		assertEquals(1, result.length);
		assertEquals(CHECKSUM3, result[0]);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#isPackageFilesEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsPackageFilesEquals() throws SpdxCompareException {
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
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertTrue(pc.isDifferenceFound());
		assertTrue(pc.isAnnotationsEquals());
		assertTrue(pc.isCommentsEquals());
		assertTrue(pc.isConcludedLicenseEquals());
		assertTrue(pc.isCopyrightsEquals());
		assertTrue(pc.isDeclaredLicensesEquals());
		assertTrue(pc.isLicenseCommmentsEquals());
		assertTrue(pc.isPackageChecksumsEquals());
		assertTrue(pc.isPackageDescriptionsEquals());
		assertTrue(pc.isPackageDownloadLocationsEquals());
		assertTrue(pc.isPackageFilenamesEquals());
		assertFalse(pc.isPackageFilesEquals());
		assertTrue(pc.isPackageHomePagesEquals());
		assertTrue(pc.isPackageOriginatorsEqual());
		assertTrue(pc.isPackageSourceInfosEquals());
		assertTrue(pc.isPackageSummaryEquals());
		assertTrue(pc.isPackageSuppliersEquals());
		assertTrue(pc.isPackageVerificationCodesEquals());
		assertTrue(pc.isPackageVersionsEquals());
		assertTrue(pc.isRelationshipsEquals());
		assertTrue(pc.isSeenLicenseEquals());	
		assertEquals(0, pc.getUniqueChecksums(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueChecksums(DOCB, DOCA).length);
		assertEquals(1, pc.getUniqueFiles(DOCA, DOCB).length);
		assertEquals(1, pc.getUniqueFiles(DOCB, DOCA).length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#getFileDifferences()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testGetFileDifferences() throws SpdxCompareException {
		SpdxFile[] filesA = new SpdxFile[] {FILE1A, FILE2A, FILE3A};
		SpdxFile[] filesB = new SpdxFile[] {FILE1B_DIFF_CHECKSUM, FILE2B, FILE3B};
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, filesA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, filesB, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertTrue(pc.isDifferenceFound());
		assertFalse(pc.isPackageFilesEquals());
		assertEquals(0, pc.getUniqueFiles(DOCA, DOCB).length);
		assertEquals(0, pc.getUniqueFiles(DOCB, DOCA).length);
		SpdxFileDifference[] result = pc.getFileDifferences(DOCA, DOCB);
		assertEquals(1, result.length);
		assertFalse(result[0].isChecksumsEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#getUniqueFilesA()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testGetUniqueFilesA() throws SpdxCompareException {
		SpdxFile[] filesA = new SpdxFile[] {FILE1A, FILE2A};
		SpdxFile[] filesB = new SpdxFile[] {FILE2B, FILE3B};
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, filesA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, filesB, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertTrue(pc.isDifferenceFound());
		assertFalse(pc.isPackageFilesEquals());
		assertEquals(0, pc.getFileDifferences(DOCA, DOCB).length);
		assertEquals(1, pc.getUniqueFiles(DOCB, DOCA).length);
		SpdxFile[] result = pc.getUniqueFiles(DOCA, DOCB);
		assertEquals(1, result.length);
		assertEquals(FILE1A, result[0]);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxPackageComparer#getUniqueFilesB()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testGetUniqueFilesB() throws SpdxCompareException {
		SpdxFile[] filesA = new SpdxFile[] {FILE1A, FILE2A};
		SpdxFile[] filesB = new SpdxFile[] {FILE2B, FILE3B};
		SpdxPackage pkgA = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDA, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, filesA, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackage pkgB = new SpdxPackage(NAMEA, COMMENTA, ANNOTATIONSA, 
				RELATIONSHIPSA, LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB,
				COPYRIGHTA, LICENSE_COMMENTA, LICENSE_DECLAREDB, CHECKSUMSA,
				DESCRIPTIONA, DOWNLOADA, filesB, HOMEPAGEA, ORIGINATORA, 
				PACKAGE_FILENAMEA, VERIFICATION_CODEA, SOURCEINFOA,
				SUMMARYA, SUPPLIERA, VERSIONINFOA);
		SpdxPackageComparer pc = new SpdxPackageComparer(LICENSE_XLATION_MAP);
		pc.addDocumentPackage(DOCA, pkgA);
		pc.addDocumentPackage(DOCB, pkgB);
		assertTrue(pc.isDifferenceFound());
		assertFalse(pc.isPackageFilesEquals());
		assertEquals(0, pc.getFileDifferences(DOCA, DOCB).length);
		assertEquals(1, pc.getUniqueFiles(DOCA, DOCB).length);
		SpdxFile[] result = pc.getUniqueFiles(DOCB, DOCA);
		assertEquals(1, result.length);
		assertEquals(FILE3B, result[0]);
	}

}
