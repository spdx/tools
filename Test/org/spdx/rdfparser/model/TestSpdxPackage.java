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
package org.spdx.rdfparser.model;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.model.Annotation.AnnotationType;
import org.spdx.rdfparser.model.Checksum.ChecksumAlgorithm;
import org.spdx.rdfparser.model.Relationship.RelationshipType;
import org.spdx.rdfparser.model.SpdxFile.FileType;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Gary
 *
 */
public class TestSpdxPackage {
	static final String DOCUMENT_NAMESPACE = "http://doc/name/space#";
	static final String PKG_NAME1 = "PackageName1";
	static final String PKG_NAME2 = "PackageName2";
	static final String PKG_COMMENT1 = "Comment1";
	static final String PKG_COMMENT2 = "Comment2";
	static final String DESCRIPTION1 = "Description 1";
	static final String DESCRIPTION2 = "Description 2";
	static final String DOWNLOAD_LOCATION1 = "Download location 1";
	static final String DOWNLOAD_LOCATION2 = "Download location 2";
	static final String HOMEPAGE1 = "http://home.page.one/one";
	static final String HOMEPAGE2 = "http://home.page.two/two2";
	static final String ORIGINATOR1 = "Organization: Originator1";
	static final String ORIGINATOR2 = "Organization: Originator2";
	static final String PACKAGEFILENAME1 = "PkgFileName1";
	static final String PACKAGEFILENAME2 = "PkgFileName2";
	static final String SOURCEINFO1 = "SourceInfo1";
	static final String SOURCEINFO2 = "SourceInfo2";
	static final String SUMMARY1 = "Summary 1";
	static final String SUMMARY2 = "Summary 2";
	static final String SUPPLIER1 = "Person: supplier1";
	static final String SUPPLIER2 = "Person: supplier2";
	static final String VERSION1 = "V1";
	static final String VERSION2 = "V2";
	
	static final String DATE_NOW = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT).format(new Date());
	static final Annotation ANNOTATION1 = new Annotation("Person: Annotator1", 
			AnnotationType.annotationType_other, DATE_NOW, "Comment1");
	static final Annotation ANNOTATION2 = new Annotation("Tool: Annotator2", 
			AnnotationType.annotationType_review, DATE_NOW, "Comment2");
	SpdxElement RELATED_ELEMENT1;
	SpdxElement RELATED_ELEMENT2;
	Relationship RELATIONSHIP1;
	Relationship RELATIONSHIP2;
	static final ExtractedLicenseInfo LICENSE1 = new ExtractedLicenseInfo("LicenseRef-1", "License Text 1");
	static final ExtractedLicenseInfo LICENSE2 = new ExtractedLicenseInfo("LicenseRef-2", "License Text 2");
	static final ExtractedLicenseInfo LICENSE3 = new ExtractedLicenseInfo("LicenseRef-3", "License Text 3");
	static final String COPYRIGHT_TEXT1 = "copyright text 1";
	static final String COPYRIGHT_TEXT2 = "copyright text 2";
	static final String LICENSE_COMMENT1 = "License Comment 1";
	static final String LICENSE_COMMENT2 = "License comment 2";
	Checksum CHECKSUM1 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, 
			"2fd4e1c67a2d28fced849ee1bb76e7391b93eb12");
	Checksum CHECKSUM2 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, 
			"0000e1c67a2d28fced849ee1bb76e7391b93eb12");
	
	SpdxFile FILE1;
	SpdxFile FILE2;
	
	FileType FILE_TYPE1 = FileType.fileType_image;
	FileType FILE_TYPE2 = FileType.fileType_audio;
	
	DoapProject DOAP_PROJECT1 = new DoapProject("Project1Name", "http://com.projct1");
	DoapProject DOAP_PROJECT2 = new DoapProject("Second project name", "http://yet.another.project/hi");

	SpdxPackageVerificationCode VERIFICATION_CODE1 = new SpdxPackageVerificationCode(
			"2222e1c67a2d28fced849ee1bb76e7391b93eb12", 
			new String[] {"Excluded1", "Excluded2"});
	SpdxPackageVerificationCode VERIFICATION_CODE2 = new SpdxPackageVerificationCode(
			"3333e1c67a2d28fced849ee1bb76e7391b93eb12", 
			new String[] {"Excluded3"});
	Model model;
	IModelContainer modelContainer;

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
		model = ModelFactory.createDefaultModel();
		modelContainer = new ModelContainerForTest(model, DOCUMENT_NAMESPACE);

		FILE1 = new SpdxFile("FileName1", "File COmment1", 
				null, null,LICENSE1, new AnyLicenseInfo[] {LICENSE2}, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, new FileType[] {FILE_TYPE1}, new Checksum[] {CHECKSUM1},
				new String[] {"Contrib 1", "Contrib2"}, "NoticeTExt1", 
				new DoapProject[] {DOAP_PROJECT1});
		
		FILE2 = new SpdxFile("FileName2", "File COmment2", 
				null, null,LICENSE2, new AnyLicenseInfo[] {LICENSE1, LICENSE2}, 
				COPYRIGHT_TEXT2, LICENSE_COMMENT2, new FileType[] {FILE_TYPE2},
				new Checksum[] {CHECKSUM2},
				new String[] {"Contrib 3"}, "NoticeTExt2", 
				new DoapProject[] {DOAP_PROJECT2});
		
		RELATED_ELEMENT1 = new SpdxElement("relatedElementName1", 
				"related element comment 1", null, null);
		RELATED_ELEMENT2 = new SpdxElement("relatedElementName2", 
				"related element comment 2", null, null);
		RELATIONSHIP1 = new Relationship(RELATED_ELEMENT1, 
				RelationshipType.CONTAINS, "Relationship Comment1");
		RELATIONSHIP2 = new Relationship(RELATED_ELEMENT2, 
				RelationshipType.DYNAMIC_LINK, "Relationship Comment2");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxPackage#getType(com.hp.hpl.jena.rdf.model.Model)}.
	 */
	@Test
	public void testGetType() {
		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				new Annotation[] {ANNOTATION1}, new Relationship[] {RELATIONSHIP1},
				LICENSE1, new AnyLicenseInfo[] {LICENSE2}, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, 
				new Checksum[] {CHECKSUM1, CHECKSUM2},
				DESCRIPTION1, DOWNLOAD_LOCATION1, new SpdxFile[] {FILE1, FILE2},
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		Resource result = pkg.getType(model);
		assertTrue(result.isURIResource());
		assertEquals(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_PACKAGE, result.getURI());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxPackage#populateModel()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testPopulateModel() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};
		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		assertEquals(PKG_NAME1, pkg.getName());
		assertEquals(PKG_COMMENT1, pkg.getComment());
		assertTrue(UnitTestHelper.isArraysEqual(annotations, pkg.getAnnotations()));
		assertTrue(UnitTestHelper.isArraysEqual(relationships, pkg.getRelationships()));
		assertEquals(LICENSE1, pkg.getLicenseConcluded());
		assertTrue(UnitTestHelper.isArraysEqual(licenseFromFiles, pkg.getLicenseInfoFromFiles()));
		assertEquals(COPYRIGHT_TEXT1, pkg.getCopyrightText());
		assertEquals(LICENSE_COMMENT1, pkg.getLicenseComments());
		assertEquals(LICENSE3, pkg.getLicenseDeclared());
		assertTrue(UnitTestHelper.isArraysEqual(checksums, pkg.getChecksums()));
		assertEquals(DESCRIPTION1, pkg.getDescription());
		assertEquals(DOWNLOAD_LOCATION1, pkg.getDownloadLocation());
		assertTrue(UnitTestHelper.isArraysEqual(files, pkg.getFiles()));
		assertEquals(HOMEPAGE1, pkg.getHomepage());
		assertEquals(ORIGINATOR1, pkg.getOriginator());
		assertEquals(PACKAGEFILENAME1, pkg.getPackageFileName());
		assertEquals(VERIFICATION_CODE1, pkg.getPackageVerificationCode());
		assertEquals(SOURCEINFO1, pkg.getSourceInfo());
		assertEquals(SUMMARY1, pkg.getSummary());
		assertEquals(SUPPLIER1, pkg.getSupplier());
		assertEquals(VERSION1, pkg.getVersionInfo());
		
		Resource r = pkg.createResource(modelContainer);
		assertEquals(PKG_NAME1, pkg.getName());
		assertEquals(PKG_COMMENT1, pkg.getComment());
		assertTrue(UnitTestHelper.isArraysEqual(annotations, pkg.getAnnotations()));
		assertTrue(UnitTestHelper.isArraysEqual(relationships, pkg.getRelationships()));
		assertEquals(LICENSE1, pkg.getLicenseConcluded());
		assertTrue(UnitTestHelper.isArraysEqual(licenseFromFiles, pkg.getLicenseInfoFromFiles()));
		assertEquals(COPYRIGHT_TEXT1, pkg.getCopyrightText());
		assertEquals(LICENSE_COMMENT1, pkg.getLicenseComments());
		assertEquals(LICENSE3, pkg.getLicenseDeclared());
		assertTrue(UnitTestHelper.isArraysEqual(checksums, pkg.getChecksums()));
		assertEquals(DESCRIPTION1, pkg.getDescription());
		assertEquals(DOWNLOAD_LOCATION1, pkg.getDownloadLocation());
		assertTrue(UnitTestHelper.isArraysEqual(files, pkg.getFiles()));
		assertEquals(HOMEPAGE1, pkg.getHomepage());
		assertEquals(ORIGINATOR1, pkg.getOriginator());
		assertEquals(PACKAGEFILENAME1, pkg.getPackageFileName());
		assertEquals(VERIFICATION_CODE1, pkg.getPackageVerificationCode());
		assertEquals(SOURCEINFO1, pkg.getSourceInfo());
		assertEquals(SUMMARY1, pkg.getSummary());
		assertEquals(SUPPLIER1, pkg.getSupplier());
		assertEquals(VERSION1, pkg.getVersionInfo());
		SpdxPackage pkg2 = new SpdxPackage(modelContainer, r.asNode());
		assertEquals(PKG_NAME1, pkg2.getName());
		assertEquals(PKG_COMMENT1, pkg2.getComment());
		assertTrue(UnitTestHelper.isArraysEquivalent(annotations, pkg2.getAnnotations()));
		assertTrue(UnitTestHelper.isArraysEquivalent(relationships, pkg2.getRelationships()));
		assertEquals(LICENSE1, pkg2.getLicenseConcluded());
		assertTrue(UnitTestHelper.isArraysEqual(licenseFromFiles, pkg2.getLicenseInfoFromFiles()));
		assertEquals(COPYRIGHT_TEXT1, pkg2.getCopyrightText());
		assertEquals(LICENSE_COMMENT1, pkg2.getLicenseComments());
		assertEquals(LICENSE3, pkg2.getLicenseDeclared());
		assertTrue(UnitTestHelper.isArraysEquivalent(checksums, pkg2.getChecksums()));
		assertEquals(DESCRIPTION1, pkg2.getDescription());
		assertEquals(DOWNLOAD_LOCATION1, pkg2.getDownloadLocation());
		assertTrue(UnitTestHelper.isArraysEquivalent(files, pkg2.getFiles()));
		assertEquals(HOMEPAGE1, pkg2.getHomepage());
		assertEquals(ORIGINATOR1, pkg2.getOriginator());
		assertEquals(PACKAGEFILENAME1, pkg2.getPackageFileName());
		assertTrue(VERIFICATION_CODE1.equivalent(pkg2.getPackageVerificationCode()));
		assertEquals(SOURCEINFO1, pkg2.getSourceInfo());
		assertEquals(SUMMARY1, pkg2.getSummary());
		assertEquals(SUPPLIER1, pkg2.getSupplier());
		assertEquals(VERSION1, pkg2.getVersionInfo());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxPackage#equivalent(org.spdx.rdfparser.model.RdfModelObject)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testEquivalent() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		
		assertTrue(pkg.equivalent(pkg));
		SpdxPackage pkg2 = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		assertTrue(pkg.equivalent(pkg2));
		pkg.createResource(modelContainer);
		assertTrue(pkg.equivalent(pkg2));
		// Checksums
		pkg2.setChecksums(new Checksum[] {CHECKSUM2});
		assertFalse(pkg.equivalent(pkg2));
		pkg2.setChecksums(checksums);
		assertTrue(pkg.equivalent(pkg2));
		// Description
		pkg2.setDescription(DESCRIPTION2);
		assertFalse(pkg.equivalent(pkg2));
		pkg2.setDescription(DESCRIPTION1);
		assertTrue(pkg.equivalent(pkg2));
		// download location
		pkg2.setDownloadLocation(DOWNLOAD_LOCATION2);
		assertFalse(pkg.equivalent(pkg2));
		pkg2.setDownloadLocation(DOWNLOAD_LOCATION1);
		assertTrue(pkg.equivalent(pkg2));
		// files
		pkg2.setFiles(new SpdxFile[] {FILE1});
		assertFalse(pkg.equivalent(pkg2));
		pkg2.setFiles(files);
		assertTrue(pkg.equivalent(pkg2));
		// homepage
		pkg2.setHomepage(HOMEPAGE2);
		assertFalse(pkg.equivalent(pkg2));
		pkg2.setHomepage(HOMEPAGE1);
		assertTrue(pkg.equivalent(pkg2));
		// originator
		pkg2.setOriginator(ORIGINATOR2);
		assertFalse(pkg.equivalent(pkg2));
		pkg2.setOriginator(ORIGINATOR1);
		assertTrue(pkg.equivalent(pkg2));
		// packagefilename
		pkg2.setPackageFileName(PACKAGEFILENAME2);
		assertFalse(pkg.equivalent(pkg2));
		pkg2.setPackageFileName(PACKAGEFILENAME1);
		assertTrue(pkg.equivalent(pkg2));
		// verification code
		pkg2.setPackageVerificationCode(VERIFICATION_CODE2);
		assertFalse(pkg.equivalent(pkg2));
		pkg2.setPackageVerificationCode(VERIFICATION_CODE1);
		assertTrue(pkg.equivalent(pkg2));
		// soruceinfo
		pkg2.setSourceInfo(SOURCEINFO2);
		assertFalse(pkg.equivalent(pkg2));
		pkg2.setSourceInfo(SOURCEINFO1);
		assertTrue(pkg.equivalent(pkg2));
		// summary
		pkg2.setSummary(SUMMARY2);
		assertFalse(pkg.equivalent(pkg2));
		pkg2.setSummary(SUMMARY1);
		assertTrue(pkg.equivalent(pkg2));
		// supplier
		pkg2.setSupplier(SUPPLIER2);
		assertFalse(pkg.equivalent(pkg2));
		pkg2.setSupplier(SUPPLIER1);
		assertTrue(pkg.equivalent(pkg2));
		// version
		pkg2.setVersionInfo(VERSION2);
		assertFalse(pkg.equivalent(pkg2));
		pkg2.setVersionInfo(VERSION1);
		assertTrue(pkg.equivalent(pkg2));
	}

	@Test
	public void testFindDuplicate() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		Resource pkgResoure = pkg.createResource(modelContainer);
		SpdxPackage pkg2 = new SpdxPackage(PKG_NAME1, PKG_COMMENT2, 
				null, null,	LICENSE2, licenseFromFiles, 
				COPYRIGHT_TEXT2, LICENSE_COMMENT2, LICENSE2, checksums,
				DESCRIPTION2, DOWNLOAD_LOCATION2, files,
				HOMEPAGE2, ORIGINATOR2, PACKAGEFILENAME2, 
				VERIFICATION_CODE1, SOURCEINFO2, SUMMARY2, SUPPLIER2, VERSION2);
		
		Resource result = pkg2.findDuplicateResource(modelContainer, null);
		assertEquals(pkgResoure, result);
	}
	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxPackage#setLicenseDeclared(org.spdx.rdfparser.license.AnyLicenseInfo)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetLicenseDeclared() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		
		assertEquals(LICENSE3, pkg.getLicenseDeclared());
		Resource r = pkg.createResource(modelContainer);
		assertEquals(LICENSE3, pkg.getLicenseDeclared());
		SpdxPackage pkg2 = new SpdxPackage(modelContainer, r.asNode());
		assertEquals(LICENSE3, pkg2.getLicenseDeclared());
		pkg.setLicenseDeclared(LICENSE1);
		assertEquals(LICENSE1, pkg.getLicenseDeclared());
		assertEquals(LICENSE1, pkg2.getLicenseDeclared());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxPackage#setChecksums(org.spdx.rdfparser.model.Checksum[])}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetChecksums() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		
		Checksum[] checksums2 = new Checksum[] {CHECKSUM1};
		assertTrue(UnitTestHelper.isArraysEquivalent(checksums, pkg.getChecksums()));
		Resource r = pkg.createResource(modelContainer);
		assertTrue(UnitTestHelper.isArraysEquivalent(checksums, pkg.getChecksums()));
		SpdxPackage pkg2 = new SpdxPackage(modelContainer, r.asNode());
		assertTrue(UnitTestHelper.isArraysEquivalent(checksums, pkg2.getChecksums()));
		pkg.setChecksums(checksums2);
		assertTrue(UnitTestHelper.isArraysEquivalent(checksums2, pkg.getChecksums()));
		assertTrue(UnitTestHelper.isArraysEquivalent(checksums2, pkg2.getChecksums()));

	}
	
	@Test
	public void testAddChecksums() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, new Checksum[] {CHECKSUM1},
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		
		assertEquals(1, pkg.getChecksums().length);
		pkg.createResource(modelContainer);
		pkg.addChecksum(CHECKSUM2);
		assertTrue(UnitTestHelper.isArraysEquivalent(checksums, pkg.getChecksums()));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxPackage#setDescription(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetDescription() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		
		assertEquals(DESCRIPTION1, pkg.getDescription());
		Resource r = pkg.createResource(modelContainer);
		assertEquals(DESCRIPTION1, pkg.getDescription());
		SpdxPackage pkg2 = new SpdxPackage(modelContainer, r.asNode());
		assertEquals(DESCRIPTION1, pkg2.getDescription());
		pkg.setDescription(DESCRIPTION2);
		assertEquals(DESCRIPTION2, pkg2.getDescription());
		assertEquals(DESCRIPTION2, pkg2.getDescription());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxPackage#setDownloadLocation(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetDownloadLocation() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		
		assertEquals(DOWNLOAD_LOCATION1, pkg.getDownloadLocation());
		Resource r = pkg.createResource(modelContainer);
		assertEquals(DOWNLOAD_LOCATION1, pkg.getDownloadLocation());
		SpdxPackage pkg2 = new SpdxPackage(modelContainer, r.asNode());
		assertEquals(DOWNLOAD_LOCATION1, pkg2.getDownloadLocation());
		pkg.setDownloadLocation(DOWNLOAD_LOCATION2);
		assertEquals(DOWNLOAD_LOCATION2, pkg.getDownloadLocation());
		assertEquals(DOWNLOAD_LOCATION2, pkg2.getDownloadLocation());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxPackage#setHomepage(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetHomepage() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		
		assertEquals(HOMEPAGE1, pkg.getHomepage());
		Resource r = pkg.createResource(modelContainer);
		assertEquals(HOMEPAGE1, pkg.getHomepage());
		SpdxPackage pkg2 = new SpdxPackage(modelContainer, r.asNode());
		assertEquals(HOMEPAGE1, pkg2.getHomepage());
		pkg.setHomepage(HOMEPAGE2);
		assertEquals(HOMEPAGE2, pkg2.getHomepage());
		assertEquals(HOMEPAGE2, pkg.getHomepage());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxPackage#setOriginator(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetOriginator() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		
		assertEquals(ORIGINATOR1, pkg.getOriginator());
		Resource r = pkg.createResource(modelContainer);
		assertEquals(ORIGINATOR1, pkg.getOriginator());
		SpdxPackage pkg2 = new SpdxPackage(modelContainer, r.asNode());
		assertEquals(ORIGINATOR1, pkg2.getOriginator());
		pkg.setOriginator(ORIGINATOR2);
		assertEquals(ORIGINATOR2, pkg2.getOriginator());
		assertEquals(ORIGINATOR2, pkg.getOriginator());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxPackage#setPackageFileName(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetPackageFileName() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		
		assertEquals(PACKAGEFILENAME1, pkg.getPackageFileName());
		Resource r = pkg.createResource(modelContainer);
		assertEquals(PACKAGEFILENAME1, pkg.getPackageFileName());
		SpdxPackage pkg2 = new SpdxPackage(modelContainer, r.asNode());
		assertEquals(PACKAGEFILENAME1, pkg2.getPackageFileName());
		pkg.setPackageFileName(PACKAGEFILENAME2);
		assertEquals(PACKAGEFILENAME2, pkg.getPackageFileName());
		assertEquals(PACKAGEFILENAME2, pkg2.getPackageFileName());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxPackage#setPackageVerificationCode(org.spdx.rdfparser.SpdxPackageVerificationCode)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetPackageVerificationCode() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		
		assertTrue(VERIFICATION_CODE1.equivalent(pkg.getPackageVerificationCode()));
		Resource r = pkg.createResource(modelContainer);
		assertTrue(VERIFICATION_CODE1.equivalent(pkg.getPackageVerificationCode()));
		SpdxPackage pkg2 = new SpdxPackage(modelContainer, r.asNode());
		assertTrue(VERIFICATION_CODE1.equivalent(pkg2.getPackageVerificationCode()));
		pkg.setPackageVerificationCode(VERIFICATION_CODE2);
		assertTrue(VERIFICATION_CODE2.equivalent(pkg2.getPackageVerificationCode()));
		assertTrue(VERIFICATION_CODE2.equivalent(pkg.getPackageVerificationCode()));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxPackage#setSourceInfo(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetSourceInfo() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		
		assertEquals(SOURCEINFO1, pkg.getSourceInfo());
		Resource r = pkg.createResource(modelContainer);
		assertEquals(SOURCEINFO1, pkg.getSourceInfo());
		SpdxPackage pkg2 = new SpdxPackage(modelContainer, r.asNode());
		assertEquals(SOURCEINFO1, pkg2.getSourceInfo());
		pkg.setSourceInfo(SOURCEINFO2);
		assertEquals(SOURCEINFO2, pkg.getSourceInfo());
		assertEquals(SOURCEINFO2, pkg2.getSourceInfo());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxPackage#setSourceInfo(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetSourceFilesAnalyzed() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1, false);
		
		assertTrue(!pkg.isFilesAnalyzed());
		Resource r = pkg.createResource(modelContainer);
		assertTrue(!pkg.isFilesAnalyzed());
		SpdxPackage pkg2 = new SpdxPackage(modelContainer, r.asNode());
		assertTrue(!pkg2.isFilesAnalyzed());
		pkg.setFilesAnalyzed(true);
		assertTrue(pkg.isFilesAnalyzed());
		assertTrue(pkg2.isFilesAnalyzed());
	}
	
	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxPackage#setSummary(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetSummary() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		
		assertEquals(SUMMARY1, pkg.getSummary());
		Resource r = pkg.createResource(modelContainer);
		assertEquals(SUMMARY1, pkg.getSummary());
		SpdxPackage pkg2 = new SpdxPackage(modelContainer, r.asNode());
		assertEquals(SUMMARY1, pkg2.getSummary());
		pkg.setSummary(SUMMARY2);
		assertEquals(SUMMARY2, pkg.getSummary());
		assertEquals(SUMMARY2, pkg2.getSummary());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxPackage#setSupplier(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetSupplier() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		
		assertEquals(SUPPLIER1, pkg.getSupplier());
		Resource r = pkg.createResource(modelContainer);
		assertEquals(SUPPLIER1, pkg.getSupplier());
		SpdxPackage pkg2 = new SpdxPackage(modelContainer, r.asNode());
		assertEquals(SUPPLIER1, pkg2.getSupplier());
		pkg.setSupplier(SUPPLIER2);
		assertEquals(SUPPLIER2, pkg.getSupplier());
		assertEquals(SUPPLIER2, pkg2.getSupplier());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxPackage#setVersionInfo(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetVersionInfo() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		
		assertEquals(VERSION1, pkg.getVersionInfo());
		Resource r = pkg.createResource(modelContainer);
		assertEquals(VERSION1, pkg.getVersionInfo());
		SpdxPackage pkg2 = new SpdxPackage(modelContainer, r.asNode());
		assertEquals(VERSION1, pkg2.getVersionInfo());
		pkg.setVersionInfo(VERSION2);
		assertEquals(VERSION2, pkg2.getVersionInfo());
		assertEquals(VERSION2, pkg.getVersionInfo());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxPackage#setFiles(org.spdx.rdfparser.model.SpdxFile[])}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetFiles() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		
		SpdxFile[] files2 = new SpdxFile[] {FILE2};
		assertTrue(UnitTestHelper.isArraysEquivalent(files, pkg.getFiles()));
		Resource r = pkg.createResource(modelContainer);
		assertTrue(UnitTestHelper.isArraysEquivalent(files, pkg.getFiles()));
		SpdxPackage pkg2 = new SpdxPackage(modelContainer, r.asNode());
		assertTrue(UnitTestHelper.isArraysEquivalent(files, pkg2.getFiles()));
		pkg.setFiles(files2);
		assertTrue(UnitTestHelper.isArraysEquivalent(files2, pkg.getFiles()));
		assertTrue(UnitTestHelper.isArraysEquivalent(files2, pkg2.getFiles()));
	}
	
	@Test
	public void testAddFiles() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, new SpdxFile[] {FILE1},
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		
		SpdxFile[] result = pkg.getFiles();
		assertEquals(1, result.length);
		assertEquals(FILE1, result[0]);
		pkg.createResource(modelContainer);
		result = pkg.getFiles();
		assertEquals(1, result.length);
		assertEquals(FILE1, result[0]);
		pkg.addFile(FILE2);
		assertTrue(UnitTestHelper.isArraysEquivalent(new SpdxFile[] {FILE1, FILE2}, pkg.getFiles()));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxPackage#clone()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		pkg.createResource(modelContainer);
		SpdxPackage pkg2 = pkg.clone();
		assertTrue(pkg.equivalent(pkg2));
		assertTrue(pkg2.resource == null);
		
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxItem#verify()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1};
		Checksum[] checksums = new Checksum[] {CHECKSUM1, CHECKSUM2};
		SpdxFile[] files = new SpdxFile[] {FILE1, FILE2};
		AnyLicenseInfo[] licenseFromFiles = new AnyLicenseInfo[] {LICENSE2};

		SpdxPackage pkg = new SpdxPackage(PKG_NAME1, PKG_COMMENT1, 
				annotations, relationships,	LICENSE1, licenseFromFiles, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, LICENSE3, checksums,
				DESCRIPTION1, DOWNLOAD_LOCATION1, files,
				HOMEPAGE1, ORIGINATOR1, PACKAGEFILENAME1, 
				VERIFICATION_CODE1, SOURCEINFO1, SUMMARY1, SUPPLIER1, VERSION1);
		
		assertEquals(0, pkg.verify().size());
		// verification code
		pkg.setPackageVerificationCode(null);
		assertEquals(1, pkg.verify().size());
	}

}
