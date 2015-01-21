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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ConjunctiveLicenseSet;
import org.spdx.rdfparser.license.DisjunctiveLicenseSet;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.model.Annotation.AnnotationType;
import org.spdx.rdfparser.model.Checksum.ChecksumAlgorithm;
import org.spdx.rdfparser.model.Relationship.RelationshipType;
import org.spdx.rdfparser.model.SpdxFile.FileType;

import spdxspreadsheet.TestPackageInfoSheet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Gary
 *
 */
public class TestSpdxFile {
	
	static final String[] NONSTD_IDS = new String[] {SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"1",
		SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"2", SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"3",
		SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"4"};
	static final String[] NONSTD_TEXTS = new String[] {"text1", "text2", "text3", "text4"};
	static final String[] STD_IDS = new String[] {"AFL-3.0", "CECILL-B", "EUPL-1.0"};
	static final String[] STD_TEXTS = new String[] {"std text1", "std text2", "std text3"};
	
	static DateFormat DATEFORMAT = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);
	static String DATE_NOW = DATEFORMAT.format(new Date());
	static final Annotation ANNOTATION1 = new Annotation("Annotator1", 
			AnnotationType.annotationType_other, DATE_NOW, "Comment 1");
	static final Annotation ANNOTATION2 = new Annotation("Annotator2", 
			AnnotationType.annotationType_review, DATE_NOW, "Comment 2");
	static final Annotation ANNOTATION3 = new Annotation("Annotator3", 
			AnnotationType.annotationType_other, DATE_NOW, "Comment 3");
	
	ExtractedLicenseInfo[] NON_STD_LICENSES;
	SpdxListedLicense[] STANDARD_LICENSES;
	DisjunctiveLicenseSet[] DISJUNCTIVE_LICENSES;
	ConjunctiveLicenseSet[] CONJUNCTIVE_LICENSES;
	
	ConjunctiveLicenseSet COMPLEX_LICENSE;
	
	Resource[] NON_STD_LICENSES_RESOURCES;
	Resource[] STANDARD_LICENSES_RESOURCES;
	Resource[] DISJUNCTIVE_LICENSES_RESOURCES;
	Resource[] CONJUNCTIVE_LICENSES_RESOURCES;
	Resource COMPLEX_LICENSE_RESOURCE;
	
	Model model;
	
	IModelContainer modelContainer = new IModelContainer() {

		@Override
		public Model getModel() {
			return model;
		}

		@Override
		public String getDocumentNamespace() {
			return "http://testNameSPace#";
		}
		
	};

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
		NON_STD_LICENSES = new ExtractedLicenseInfo[NONSTD_IDS.length];
		for (int i = 0; i < NONSTD_IDS.length; i++) {
			NON_STD_LICENSES[i] = new ExtractedLicenseInfo(NONSTD_IDS[i], NONSTD_TEXTS[i]);
		}
		
		STANDARD_LICENSES = new SpdxListedLicense[STD_IDS.length];
		for (int i = 0; i < STD_IDS.length; i++) {
			STANDARD_LICENSES[i] = new SpdxListedLicense("Name "+String.valueOf(i), 
					STD_IDS[i], STD_TEXTS[i], new String[] {"URL "+String.valueOf(i)}, "Notes "+String.valueOf(i), 
					"LicHeader "+String.valueOf(i), "Template "+String.valueOf(i), true);
		}
		
		DISJUNCTIVE_LICENSES = new DisjunctiveLicenseSet[3];
		CONJUNCTIVE_LICENSES = new ConjunctiveLicenseSet[2];
		
		DISJUNCTIVE_LICENSES[0] = new DisjunctiveLicenseSet(new AnyLicenseInfo[] {
				NON_STD_LICENSES[0], NON_STD_LICENSES[1], STANDARD_LICENSES[1]
		});
		CONJUNCTIVE_LICENSES[0] = new ConjunctiveLicenseSet(new AnyLicenseInfo[] {
				STANDARD_LICENSES[0], NON_STD_LICENSES[0], STANDARD_LICENSES[1]
		});
		CONJUNCTIVE_LICENSES[1] = new ConjunctiveLicenseSet(new AnyLicenseInfo[] {
				DISJUNCTIVE_LICENSES[0], NON_STD_LICENSES[2]
		});
		DISJUNCTIVE_LICENSES[1] = new DisjunctiveLicenseSet(new AnyLicenseInfo[] {
				CONJUNCTIVE_LICENSES[1], NON_STD_LICENSES[0], STANDARD_LICENSES[0]
		});
		DISJUNCTIVE_LICENSES[2] = new DisjunctiveLicenseSet(new AnyLicenseInfo[] {
				DISJUNCTIVE_LICENSES[1], CONJUNCTIVE_LICENSES[0], STANDARD_LICENSES[2]
		});
		COMPLEX_LICENSE = new ConjunctiveLicenseSet(new AnyLicenseInfo[] {
				DISJUNCTIVE_LICENSES[2], NON_STD_LICENSES[2], CONJUNCTIVE_LICENSES[1]
		});
		model = ModelFactory.createDefaultModel();
		
		NON_STD_LICENSES_RESOURCES = new Resource[NON_STD_LICENSES.length];
		for (int i = 0; i < NON_STD_LICENSES.length; i++) {
			NON_STD_LICENSES_RESOURCES[i] = NON_STD_LICENSES[i].createResource(modelContainer);
		}
		STANDARD_LICENSES_RESOURCES = new Resource[STANDARD_LICENSES.length];
		for (int i = 0; i < STANDARD_LICENSES.length; i++) {
			STANDARD_LICENSES_RESOURCES[i] = STANDARD_LICENSES[i].createResource(modelContainer);
		}
		CONJUNCTIVE_LICENSES_RESOURCES = new Resource[CONJUNCTIVE_LICENSES.length];
		for (int i = 0; i < CONJUNCTIVE_LICENSES.length; i++) {
			CONJUNCTIVE_LICENSES_RESOURCES[i] = CONJUNCTIVE_LICENSES[i].createResource(modelContainer);
		}
		DISJUNCTIVE_LICENSES_RESOURCES = new Resource[DISJUNCTIVE_LICENSES.length];
		for (int i = 0; i < DISJUNCTIVE_LICENSES.length; i++) {
			DISJUNCTIVE_LICENSES_RESOURCES[i] = DISJUNCTIVE_LICENSES[i].createResource(modelContainer);
		}
		COMPLEX_LICENSE_RESOURCE = COMPLEX_LICENSE.createResource(modelContainer);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#findDuplicateResource(org.spdx.rdfparser.IModelContainer, java.lang.String)}.
	 */
	@Test
	public void testFindDuplicateResource() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#getType(com.hp.hpl.jena.rdf.model.Model)}.
	 */
	@Test
	public void testGetType() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#populateModel()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testPopulateModel() throws InvalidSPDXAnalysisException {
		AnyLicenseInfo[] seenLic = new AnyLicenseInfo[] {STANDARD_LICENSES[0]};
		String[] contributors = new String[] {"Contrib1", "Contrib2"};
		DoapProject[] artifactOfs = new DoapProject[] {new DoapProject("Artifactof Project", "http://project.home.page/this")};
		
		SpdxFile fileDep1 = new SpdxFile("SpdxRef-1", "fileDep1", 
				"Comment1", new Annotation[] {ANNOTATION1, ANNOTATION2}, null,
				COMPLEX_LICENSE, seenLic, "Copyright1", "License Comments1",
				new FileType[] {FileType.fileType_source}, 
				new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, "1123456789abcdef0123456789abcdef01234567")}, 
				contributors, "Notice Text", artifactOfs);
		SpdxFile fileDep2 = new SpdxFile("SpdxRef-2", "fileDep2", 
				"Comment2", new Annotation[] {ANNOTATION3}, null,
				COMPLEX_LICENSE, seenLic, "Copyright1", "License Comments2",
				new FileType[] {FileType.fileType_binary}, 
				new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, "2123456789abcdef0123456789abcdef01234567")}, 
				contributors, "Notice Text", artifactOfs);
		SpdxFile[] fileDependencies = new SpdxFile[] {fileDep1, fileDep2};
		String fileNotice = "File Notice";
		Relationship rel1 = new Relationship(fileDep1, RelationshipType.relationshipType_contains, "Relationship 1 comment");
		Relationship rel2 = new Relationship(fileDep2, RelationshipType.relationshipType_documentation, "Relationship 2 comment");
		Relationship[] relationships = new Relationship[] {rel1, rel2};
		SpdxFile file = new SpdxFile("SpdxRef-3", "fileName", 
				"file comments", new Annotation[] {ANNOTATION3}, relationships,
				COMPLEX_LICENSE, seenLic, "Copyrights", "License comments",
				new FileType[] {FileType.fileType_source}, 
				new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, "0123456789abcdef0123456789abcdef01234567")}, 
				contributors, fileNotice, artifactOfs);
		file.setFileDependencies(fileDependencies);
		ArrayList<String> verify = file.verify();
		assertEquals(0, verify.size());
		Resource fileResource = file.createResource(modelContainer);
		SpdxFile file2 = new SpdxFile(modelContainer, fileResource.asNode());
		assertEquals(file.getArtifactOf()[0].getName(), file2.getArtifactOf()[0].getName());
		assertEquals(file.getArtifactOf()[0].getHomePage(), file2.getArtifactOf()[0].getHomePage());
		assertEquals(file.getCopyrightText(), file2.getCopyrightText());
		assertEquals(file.getLicenseComment(), file2.getLicenseComment());
		assertEquals(file.getComment(), file2.getComment());
		assertEquals(file.getName(), file2.getName());
		assertTrue(UnitTestHelper.isArraysEquivalent(file.getChecksums(), file2.getChecksums()));
		assertEquals(file.getFileTypes()[0], file2.getFileTypes()[0]);
		assertEquals(file.getLicenseConcluded(), file2.getLicenseConcluded());
		assertEquals(file.getNoticeText(), file2.getNoticeText());
		assertTrue(UnitTestHelper.isArraysEqual(contributors, file2.getFileContributors()));
		TestPackageInfoSheet.compareLicenseDeclarations(file.getLicenseInfoFromFiles(), file2.getLicenseInfoFromFiles());
		assertTrue(UnitTestHelper.isArraysEquivalent(file.getFileDependencies(), file2.getFileDependencies()));
		assertTrue(UnitTestHelper.isArraysEquivalent(relationships, file2.getRelationships()));
		assertEquals(file.getId(), file2.getId());
		verify = file2.verify();
		assertEquals(0, verify.size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#equivalent(org.spdx.rdfparser.model.RdfModelObject)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testEquivalent() throws InvalidSPDXAnalysisException {
		AnyLicenseInfo[] seenLic = new AnyLicenseInfo[] {STANDARD_LICENSES[0]};
		String[] contributors = new String[] {"Contrib1", "Contrib2"};
		DoapProject[] artifactOfs = new DoapProject[] {new DoapProject("Artifactof Project", "http://project.home.page/this")};
		
		SpdxFile fileDep1 = new SpdxFile("SpdxRef-1", "fileDep1", 
				"Comment1", new Annotation[] {ANNOTATION1, ANNOTATION2}, null,
				COMPLEX_LICENSE, seenLic, "Copyright1", "License Comments1",
				new FileType[] {FileType.fileType_source}, 
				new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, "1123456789abcdef0123456789abcdef01234567")}, 
				contributors, "Notice Text", artifactOfs);
		SpdxFile fileDep2 = new SpdxFile("SpdxRef-2", "fileDep2", 
				"Comment2", new Annotation[] {ANNOTATION3}, null,
				COMPLEX_LICENSE, seenLic, "Copyright1", "License Comments2",
				new FileType[] {FileType.fileType_binary}, 
				new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, "2123456789abcdef0123456789abcdef01234567")}, 
				contributors, "Notice Text", artifactOfs);
		SpdxFile[] fileDependencies = new SpdxFile[] {fileDep1, fileDep2};
		String fileNotice = "File Notice";
		Relationship rel1 = new Relationship(fileDep1, RelationshipType.relationshipType_contains, "Relationship 1 comment");
		Relationship rel2 = new Relationship(fileDep2, RelationshipType.relationshipType_documentation, "Relationship 2 comment");
		Relationship[] relationships = new Relationship[] {rel1, rel2};
		String id = "SpdxRef-3";
		String name = "fileName";
		String comment = "file comments";
		Annotation[] annotations = new Annotation[] {ANNOTATION3};
		String copyright = "Copyrights";
		String licenseComment = "License comments";
		FileType[] filetypes = new FileType[] {FileType.fileType_source};
		Checksum[] checksum = new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, "0123456789abcdef0123456789abcdef01234567")};
		
		SpdxFile file = new SpdxFile(id, name, 
				comment, annotations, relationships,
				COMPLEX_LICENSE, seenLic, copyright, licenseComment,
				filetypes, checksum, contributors, fileNotice, artifactOfs);
		file.setFileDependencies(fileDependencies);
		SpdxFile file2 = new SpdxFile(id, name, 
				comment, annotations, relationships,
				COMPLEX_LICENSE, seenLic, copyright, licenseComment,
				filetypes, checksum, contributors, fileNotice, artifactOfs);
		file2.setFileDependencies(fileDependencies);
		assertTrue(file.equivalent(file2));
		file2.createResource(modelContainer);
		assertTrue(file2.equivalent(file2));
		// name
		file2.setName("NewName");
		assertFalse(file.equivalent(file2));
		file2.setName(name);
		assertTrue(file.equivalent(file2));
		// comment
		file2.setComment("New Comment");
		assertFalse(file.equivalent(file2));
		file2.setComment(comment);
		assertTrue(file.equivalent(file2));
		// annotations
		file2.setAnnotations(new Annotation[] {ANNOTATION1, ANNOTATION2});
		assertFalse(file.equivalent(file2));
		file2.setAnnotations(annotations);
		assertTrue(file.equivalent(file2));
		// relationships
		file2.setRelationships(new Relationship[] {rel1});
		assertFalse(file.equivalent(file2));
		file2.setRelationships(relationships);
		assertTrue(file.equivalent(file2));
		// licenseConcluded
		file2.setLicenseConcluded(NON_STD_LICENSES[0]);
		assertFalse(file.equivalent(file2));
		file2.setLicenseConcluded(COMPLEX_LICENSE);
		assertTrue(file.equivalent(file2));
		// seen licenses
		file2.setLicenseInfosFromFiles(NON_STD_LICENSES);
		assertFalse(file.equivalent(file2));
		file2.setLicenseInfosFromFiles(seenLic);
		assertTrue(file.equivalent(file2));
		// copyrights
		file2.setCopyrightText("new copyright");
		assertFalse(file.equivalent(file2));
		file2.setCopyrightText(copyright);
		assertTrue(file.equivalent(file2));
		// license comments
		file2.setLicenseComment("New license comment");
		assertFalse(file.equivalent(file2));
		file2.setLicenseComment(licenseComment);
		assertTrue(file.equivalent(file2));
		// file types
		file2.setFileTypes(new FileType[] {FileType.fileType_text, FileType.fileType_archive});
		assertFalse(file.equivalent(file2));
		file2.setFileTypes(filetypes);
		assertTrue(file.equivalent(file2));
		// checksum
		file2.setChecksums(new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha256, "1123456789abcdef0123456789abcdef01234567")});
		assertFalse(file.equivalent(file2));
		file2.setChecksums(checksum);
		assertTrue(file.equivalent(file2));
		// contributors
		file2.setFileContributors(new String[] {"new 1", "new2"});
		assertFalse(file.equivalent(file2));
		file2.setFileContributors(contributors);
		assertTrue(file.equivalent(file2));
		// file notice
		file2.setNoticeText("New file notice");
		assertFalse(file.equivalent(file2));
		file2.setNoticeText(fileNotice);
		assertTrue(file.equivalent(file2));
		// artifactOfs
		file2.setArtifactOf(new DoapProject[] {new DoapProject("NewProject", "http://new.home.page/this")});
		assertFalse(file.equivalent(file2));
		file2.setArtifactOf(artifactOfs);
		assertTrue(file.equivalent(file2));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#verify()}.
	 */
	@Test
	public void testVerify() {
		AnyLicenseInfo[] seenLic = new AnyLicenseInfo[] {STANDARD_LICENSES[0]};
		String[] contributors = new String[] {"Contrib1", "Contrib2"};
		DoapProject[] artifactOfs = new DoapProject[] {new DoapProject("Artifactof Project", "http://project.home.page/this")};
		
		SpdxFile fileDep1 = new SpdxFile("SpdxRef-1", "fileDep1", 
				"Comment1", new Annotation[] {ANNOTATION1, ANNOTATION2}, null,
				COMPLEX_LICENSE, seenLic, "Copyright1", "License Comments1",
				new FileType[] {FileType.fileType_source}, 
				new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, "1123456789abcdef0123456789abcdef01234567")}, 
				contributors, "Notice Text", artifactOfs);
		SpdxFile fileDep2 = new SpdxFile("SpdxRef-2", "fileDep2", 
				"Comment2", new Annotation[] {ANNOTATION3}, null,
				COMPLEX_LICENSE, seenLic, "Copyright1", "License Comments2",
				new FileType[] {FileType.fileType_binary}, 
				new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, "2123456789abcdef0123456789abcdef01234567")}, 
				contributors, "Notice Text", artifactOfs);
		SpdxFile[] fileDependencies = new SpdxFile[] {fileDep1, fileDep2};
		String fileNotice = "File Notice";
		Relationship rel1 = new Relationship(fileDep1, RelationshipType.relationshipType_contains, "Relationship 1 comment");
		Relationship rel2 = new Relationship(fileDep2, RelationshipType.relationshipType_documentation, "Relationship 2 comment");
		Relationship[] relationships = new Relationship[] {rel1, rel2};
		String id = "SpdxRef-3";
		String name = "fileName";
		String comment = "file comments";
		Annotation[] annotations = new Annotation[] {ANNOTATION3};
		String copyright = "Copyrights";
		String licenseComment = "License comments";
		FileType[] filetypes = new FileType[] {FileType.fileType_source};
		Checksum[] checksum = new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, "0123456789abcdef0123456789abcdef01234567")};
		
		SpdxFile file = new SpdxFile(id, name, 
				comment, annotations, relationships,
				COMPLEX_LICENSE, seenLic, copyright, licenseComment,
				filetypes, checksum, contributors, fileNotice, artifactOfs);
		assertEquals(0, file.verify());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#SpdxFile(java.lang.String, java.lang.String, java.lang.String, org.spdx.rdfparser.model.Annotation[], org.spdx.rdfparser.model.Relationship[], org.spdx.rdfparser.license.AnyLicenseInfo, org.spdx.rdfparser.license.SimpleLicensingInfo[], java.lang.String, java.lang.String, org.spdx.rdfparser.model.SpdxFile.FileType, org.spdx.rdfparser.model.Checksum, java.lang.String[], java.lang.String, org.spdx.rdfparser.model.DoapProject[])}.
	 */
	@Test
	public void testSpdxFileStringStringStringAnnotationArrayRelationshipArrayAnyLicenseInfoSimpleLicensingInfoArrayStringStringFileTypeChecksumStringArrayStringDoapProjectArray() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#convertDeclaredLicense(org.spdx.rdfparser.license.SimpleLicensingInfo[])}.
	 */
	@Test
	public void testConvertDeclaredLicense() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#convertLicenseInfoFromFile(org.spdx.rdfparser.license.AnyLicenseInfo)}.
	 */
	@Test
	public void testConvertLicenseInfoFromFile() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#findFileResource(org.spdx.rdfparser.IModelContainer, org.spdx.rdfparser.model.SpdxFile)}.
	 */
	@Test
	public void testFindFileResource() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#setFileType(org.spdx.rdfparser.model.SpdxFile.FileType)}.
	 */
	@Test
	public void testSetFileType() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#setChecksums(org.spdx.rdfparser.model.Checksum)}.
	 */
	@Test
	public void testSetChecksum() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#setFileContributors(java.lang.String[])}.
	 */
	@Test
	public void testSetFileContributors() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#setNoticeText(java.lang.String)}.
	 */
	@Test
	public void testSetNoticeText() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#setId(java.lang.String)}.
	 */
	@Test
	public void testSetId() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#setArtifactOf(org.spdx.rdfparser.model.DoapProject[])}.
	 */
	@Test
	public void testSetArtifactOf() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#setFileDependencies(org.spdx.rdfparser.model.SpdxFile[])}.
	 */
	@Test
	public void testSetFileDependencies() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#clone()}.
	 */
	@Test
	public void testClone() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#compareTo(org.spdx.rdfparser.model.SpdxFile)}.
	 */
	@Test
	public void testCompareTo() {
		fail("Not yet implemented");
	}

}
