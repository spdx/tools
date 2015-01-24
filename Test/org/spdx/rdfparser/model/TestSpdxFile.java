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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.DOAPProject;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocument;
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

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

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
		modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
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
	
	@Test
	public void testGetType() throws InvalidSPDXAnalysisException {
		SpdxFile file = new SpdxFile("SpdxRef-1", "filename", null, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE, null,
				null, new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
						"1123456789abcdef0123456789abcdef01234567")}, null, null, null);
		String expected = SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_FILE;
		Resource result = file.getType(model);
		assertTrue(result.isURIResource());
		assertEquals(expected, result.getURI());
	}

	@Test
	public void testSetFiletypeType() throws InvalidSPDXAnalysisException {
		FileType[] fileTypes1 = new FileType[] {FileType.fileType_archive, 
				FileType.fileType_spdx, FileType.fileType_other, FileType.fileType_text};
		FileType[] fileTypes2 = new FileType[] {FileType.fileType_image, 
				FileType.fileType_binary, FileType.fileType_documentation};
		FileType[] fileTypeSingle = new FileType[] {FileType.fileType_source};

		model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://somethingunique.com/something");
		SpdxFile file = new SpdxFile("SpdxRef-1", "filename", null, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE, null,
				fileTypes1, new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
						"1123456789abcdef0123456789abcdef01234567")}, null, null, null);
		FileType[] result = file.getFileTypes();
		assertTrue(UnitTestHelper.isArraysEqual(fileTypes1, result));
		Resource r = file.createResource(modelContainer);
		SpdxFile file2 = new SpdxFile(modelContainer, r.asNode());
		result = file2.getFileTypes();
		assertTrue(UnitTestHelper.isArraysEqual(fileTypes1, result));
		file.setFileTypes(fileTypes2);
		result = file.getFileTypes();
		assertTrue(UnitTestHelper.isArraysEqual(fileTypes2, result));
		result = file2.getFileTypes();
		assertTrue(UnitTestHelper.isArraysEqual(fileTypes2, result));
		file2.setFileTypes(fileTypeSingle);
		result = file2.getFileTypes();
		assertTrue(UnitTestHelper.isArraysEqual(fileTypeSingle, result));
		result = file.getFileTypes();
		assertTrue(UnitTestHelper.isArraysEqual(fileTypeSingle, result));
		file.setFileTypes(null);
		assertEquals(0, file.getFileTypes().length);
		assertEquals(0, file2.getFileTypes().length);
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
	
	@SuppressWarnings("deprecation")
	@Test
	public void testCloneModelSimple() throws InvalidSPDXAnalysisException, IOException {
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
		ArrayList<String> verify = file.verify();
		assertEquals(0, verify.size());
		// assign a model to the original, then clone
		final Model fromModel = ModelFactory.createDefaultModel();
		final String fromDocNamespace = "https://my/test/doc2#";
		String fromFileUri = fromDocNamespace + id;
		
		IModelContainer fromModelContainer = new IModelContainer() {
			@Override
			public String getNextSpdxElementRef() {
				return null;
			}
			@Override
			public Model getModel() {
				return fromModel;
			}

			@Override
			public String getDocumentNamespace() {
				return fromDocNamespace;
			}
			
		};
		Resource fileResource = file.createResource(fromModelContainer);
		assertTrue(fileResource.isURIResource());
		assertEquals(fromFileUri, fileResource.getURI());
		final Model toModel2 = ModelFactory.createDefaultModel();
		final String testDocNamespace = "https://my/test/doc3#";
		String toFileUri2 = testDocNamespace + id;
		IModelContainer toModelContainer = new IModelContainer() {
			int nextRef = 1;
			@Override
			public String getNextSpdxElementRef() {
				return "SpdxRef-"+String.valueOf(nextRef++);
			}
			@Override
			public Model getModel() {
				return toModel2;
			}

			@Override
			public String getDocumentNamespace() {
				return testDocNamespace;
			}
			
		};
		SpdxFile toFile = file.clone();
		toFile.setId(id);
		Resource toFileResource = toFile.createResource(toModelContainer);
		assertTrue(toFileResource.isURIResource());
		assertEquals(toFileUri2, toFileResource.getURI());
		assertEquals(file.getArtifactOf()[0].getName(), toFile.getArtifactOf()[0].getName());
		assertEquals(file.getArtifactOf()[0].getHomePage(), toFile.getArtifactOf()[0].getHomePage());
		assertEquals(file.getCopyrightText(), toFile.getCopyrightText());
		assertEquals(file.getLicenseComment(), toFile.getLicenseComment());
		assertEquals(file.getComment(), toFile.getComment());
		assertEquals(file.getName(), toFile.getName());
		assertEquals(file.getSha1(), toFile.getSha1());
		assertTrue(UnitTestHelper.isArraysEqual(file.getFileTypes(), toFile.getFileTypes()));
		assertEquals(file.getLicenseConcluded(), toFile.getLicenseConcluded());
		assertEquals(file.getNoticeText(), toFile.getNoticeText());
		assertTrue(UnitTestHelper.isArraysEqual(contributors, toFile.getFileContributors()));
		TestPackageInfoSheet.compareLicenseDeclarations(file.getLicenseInfoFromFiles(), toFile.getLicenseInfoFromFiles());
		assertTrue(UnitTestHelper.isArraysEquivalent(file.getFileDependencies(), toFile.getFileDependencies()));
		verify = toFile.verify();
		assertEquals(0, verify.size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#equivalent(org.spdx.rdfparser.model.RdfModelObject)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@SuppressWarnings("deprecation")
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
	 * @throws InvalidSPDXAnalysisException 
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException {
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
		assertEquals(0, file.verify().size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#findFileResource(org.spdx.rdfparser.IModelContainer, org.spdx.rdfparser.model.SpdxFile)}.
	 */
	@Test
	public void testFindFileResource() throws InvalidSPDXAnalysisException {
		model = ModelFactory.createDefaultModel();
		String namespace = "http://test.name/space";
		IModelContainer modelContainer = new ModelContainerForTest(model, namespace);
		
		String FILE1_NAME = "./file/name/name1";
		String FILE1_ID = "SpdxRef-1";
		Checksum FILE1_SHA1 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
				"1123456789abcdef0123456789abcdef01234567");
		String FILE1_COPYRIGHT = "Copyright 1";
		
		String FILE2_NAME = "./file2/name/name2";
		String FILE2_ID = "SpdxRef-2";
		Checksum FILE2_SHA1 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
				"2222456789abcdef0123456789abcdef01234567");
		String FILE2_COPYRIGHT = "Copyright 2";
		String FILE3_NAME = "./a/different/name";
		String FILE3_ID = "SpdxRef-3";
		String FILE3_COPYRIGHT = "Copyright 3";
		Checksum FILE4_SHA1 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
				"4444456789abcdef0123456789abcdef01234567");

		SpdxFile file1 = new SpdxFile(FILE1_ID, FILE1_NAME, null, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, FILE1_COPYRIGHT, null,
				null, new Checksum[] {FILE1_SHA1}, null, null, null);
		Resource file1Resource = file1.createResource(modelContainer);
		SpdxFile file2 = new SpdxFile(FILE2_ID, FILE2_NAME, null, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, FILE2_COPYRIGHT, null,
				null, new Checksum[] {FILE2_SHA1}, null, null, null);		
		Resource file2Resource = file2.createResource(modelContainer);
		
		SpdxFile testFile1 = new SpdxFile(FILE1_ID, FILE1_NAME, null, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, FILE1_COPYRIGHT, null,
				null, new Checksum[] {FILE1_SHA1}, null, null, null);
		SpdxFile testFile2 = new SpdxFile(FILE2_ID, FILE2_NAME, null, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, FILE2_COPYRIGHT, null,
				null, new Checksum[] {FILE2_SHA1}, null, null, null);	
		SpdxFile testFile3 = new SpdxFile(FILE3_ID, FILE3_NAME, null, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, FILE3_COPYRIGHT, null,
				null, new Checksum[] {FILE1_SHA1}, null, null, null);	
		SpdxFile testFile4 = new SpdxFile(FILE3_ID, FILE3_NAME, null, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, FILE3_COPYRIGHT, null,
				null, new Checksum[] {FILE4_SHA1}, null, null, null);
		Resource retval = SpdxFile.findFileResource(modelContainer, testFile1);
		assertEquals(file1Resource, retval);
		retval = SpdxFile.findFileResource(modelContainer, testFile2);
		assertEquals(file2Resource, retval);
		retval = SpdxFile.findFileResource(modelContainer, testFile3);
		if (retval != null) {
			fail("Should be null due to different file names");
		}
		retval = SpdxFile.findFileResource(modelContainer, testFile4);
		if (retval != null) {
			fail("Should be null due to different checksums");
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#setChecksums(org.spdx.rdfparser.model.Checksum)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetChecksum() throws InvalidSPDXAnalysisException {
		String SHA1_VALUE1 = "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12";
		String SHA1_VALUE2 = "2222e1c67a2d28fced849ee1bb76e7391b93eb12";
		String SHA256_VALUE1 = "CA978112CA1BBDCAFAC231B39A23DC4DA786EFF8147C4E72B9807785AFEE48BB";
		String SHA256_VALUE2 = "F7846F55CF23E14EEBEAB5B4E1550CAD5B509E3348FBC4EFA3A1413D393CB650";
		String MD5_VALUE1 = "9e107d9d372bb6826bd81d3542a419d6";
		String MD5_VALUE2 = "d41d8cd98f00b204e9800998ecf8427e";
		Checksum checksum1 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, SHA1_VALUE1);
		Checksum checksum2 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, SHA1_VALUE2);
		Checksum checksum3 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha256, SHA256_VALUE1);
		Checksum checksum4 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha256, SHA256_VALUE2);
		Checksum checksum5 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_md5, MD5_VALUE1);
		Checksum checksum6 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_md5, MD5_VALUE2);
		
		Checksum[] checksums1 = new Checksum[] {checksum1, checksum3, checksum5};
		Checksum[] checksums2 = new Checksum[] {checksum2, checksum4, checksum6};
		Checksum[] checksumSingle = new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
				"1123456789abcdef0123456789abcdef01234567")};

		
		model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://somethingunique.com/something");
		SpdxFile file = new SpdxFile("SpdxRef-1", "filename", null, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE, null,
				null, checksums1, null, null, null);
		Checksum[] result = file.getChecksums();
		assertTrue(UnitTestHelper.isArraysEqual(checksums1, result));
		Resource r = file.createResource(modelContainer);
		result = file.getChecksums();
		assertTrue(UnitTestHelper.isArraysEqual(checksums1, result));
		SpdxFile file2 = new SpdxFile(modelContainer, r.asNode());
		result = file2.getChecksums();
		assertTrue(UnitTestHelper.isArraysEqual(checksums1, result));
		file.setChecksums(checksums2);
		result = file.getChecksums();
		assertTrue(UnitTestHelper.isArraysEqual(checksums2, result));
		result = file2.getChecksums();
		assertTrue(UnitTestHelper.isArraysEqual(checksums2, result));
		file2.setChecksums(checksumSingle);
		result = file2.getChecksums();
		assertTrue(UnitTestHelper.isArraysEqual(checksumSingle, result));
		result = file.getChecksums();
		assertTrue(UnitTestHelper.isArraysEqual(checksumSingle, result));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#setFileContributors(java.lang.String[])}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetFileContributors() throws InvalidSPDXAnalysisException {
		String CONTRIBUTOR1 = "Contributor 1";
		String CONTRIBUTOR2 = "Contributor 2";
		String CONTRIBUTOR3 = "Contributor 3";
		String[] contributors = new String[] {CONTRIBUTOR1, CONTRIBUTOR2, CONTRIBUTOR3};
		
		String CONTRIBUTOR4 = "Contributor 4";
		String[] oneContributor = new String[] {CONTRIBUTOR4};
		model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://somethingunique.com/something");
		SpdxFile file = new SpdxFile("SpdxRef-1", "filename", null, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE, null,
				null, new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
						"1123456789abcdef0123456789abcdef01234567")}, oneContributor, null, null);

		assertEquals(1, file.getFileContributors().length);
		String[] result = file.getFileContributors();
		Resource fileResource = file.createResource(modelContainer);
		file.setFileContributors(contributors);
		result = file.getFileContributors();
		assertTrue(UnitTestHelper.isArraysEqual(contributors, result));
		SpdxFile file2 = new SpdxFile(modelContainer, fileResource.asNode());
		result = file2.getFileContributors();
		assertTrue(UnitTestHelper.isArraysEqual(contributors, result));
		file2.setFileContributors(new String[0]);
		assertEquals(0, file2.getFileContributors().length);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#setNoticeText(java.lang.String)}.
	 */
	@Test
	public void testSetNoticeText() throws InvalidSPDXAnalysisException {
		String fileNotice = "This is a file notice";

		model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://somethingunique.com/something");
		SpdxFile file = new SpdxFile("SpdxRef-1", "filename", null, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE, null,
				null, new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
						"1123456789abcdef0123456789abcdef01234567")}, null, null, null);
		if (file.getNoticeText() != null && !file.getNoticeText().isEmpty()) {
			fail("nto null notice text");
		}
		Resource fileResource = file.createResource(modelContainer);
		file.setNoticeText(fileNotice);
		String result  = file.getNoticeText();
		assertEquals(fileNotice, result);
		SpdxFile file2 = new SpdxFile(modelContainer, fileResource.asNode());
		result = file2.getNoticeText();
		assertEquals(fileNotice, file2.getNoticeText());
		file2.setNoticeText(null);
		if (file2.getNoticeText() != null && !file2.getNoticeText().isEmpty()) {
			fail("nto null notice text");
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#setId(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetId() throws InvalidSPDXAnalysisException {
		model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://somethingunique.com/something");
		String id1 = "SpdxRef-1";
		String id2 = "SpdxRef-2";
		SpdxFile file = new SpdxFile(id1, "filename", null, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE, null,
				null, new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
						"1123456789abcdef0123456789abcdef01234567")}, null, null, null);
		assertEquals(id1, file.getId());
		file.setId(id2);
		assertEquals(id2, file.getId());
		file.setId(null);
		assertTrue(file.getId() == null);
		file.setId(id2);
		assertEquals(id2, file.getId());
		Resource r = file.createResource(modelContainer);
		SpdxFile file2 = new SpdxFile(modelContainer, r.asNode());
		assertEquals(id2, file2.getId());
		assertTrue(r.isURIResource());
		assertTrue(r.getURI().endsWith(id2));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#setArtifactOf(org.spdx.rdfparser.model.DoapProject[])}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetArtifactOf() throws InvalidSPDXAnalysisException {
		DoapProject project1 = new DoapProject("Artifactof Project", "http://project.home.page/this");
		DoapProject project2 = new DoapProject("Artifactof Project2", "http://another.home.page/this");
		DoapProject project3 = new DoapProject("Artifactof Project3", "http://yea.home.page/this");
		DoapProject project4 = new DoapProject("Artifactof Project4", "http://ok.home.page/this");
		DoapProject[] artifactOfs1 = new DoapProject[] {project1, project2, project3};
		DoapProject[] artifactOfs2 = new DoapProject[] {project2, project4};
		DoapProject[] artifactOfSingle = new DoapProject[] {project4};
		
		model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://somethingunique.com/something");
		SpdxFile file = new SpdxFile("SpdxRef-1", "filename", null, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE, null,
				null, new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
						"1123456789abcdef0123456789abcdef01234567")}, null, null, artifactOfs1);
		DoapProject[] result = file.getArtifactOf();
		assertTrue(UnitTestHelper.isArraysEqual(artifactOfs1, result));
		file.setArtifactOf(artifactOfs2);
		result = file.getArtifactOf();
		assertTrue(UnitTestHelper.isArraysEqual(artifactOfs2, result));
		Resource r = file.createResource(modelContainer);
		SpdxFile file2 = new SpdxFile(modelContainer, r.asNode());
		result = file2.getArtifactOf();
		assertTrue(UnitTestHelper.isArraysEqual(artifactOfs2, result));
		file2.setArtifactOf(artifactOfSingle);
		result = file2.getArtifactOf();
		assertTrue(UnitTestHelper.isArraysEqual(artifactOfSingle, result));
		result = file.getArtifactOf();
		assertTrue(UnitTestHelper.isArraysEqual(artifactOfSingle, result));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#setFileDependencies(org.spdx.rdfparser.model.SpdxFile[])}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testSetFileDependencies() throws InvalidSPDXAnalysisException {
		String FileDependencyName1  = "Dependency1";
		String FileDependencyName2 = "dependencies/Dependency2";
		String FileDependencyName3 = "Depenedency3";
		String COMMENT1 = "comment";
		model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://somethingunique.com/something");
		SpdxFile file = new SpdxFile("SpdxRef-1", "filename", COMMENT1, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE, null,
				null, new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
						"1123456789abcdef0123456789abcdef01234567")}, null, null, null);

		SpdxFile fileDependency1 = new SpdxFile("SpdxRef-2", FileDependencyName1, COMMENT1, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE, null,
				null, new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
						"1123456789abcdef0123456789abcdef01234567")}, null, null, null);
		SpdxFile fileDependency2 = new SpdxFile("SpdxRef-3", FileDependencyName2, COMMENT1, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE, null,
				null, new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
						"1123456789abcdef0123456789abcdef01234567")}, null, null, null);
		SpdxFile fileDependency3 = new SpdxFile("SpdxRef-4", FileDependencyName3, COMMENT1, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE, null,
				null, new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
						"1123456789abcdef0123456789abcdef01234567")}, null, null, null);
		Resource fileResource = file.createResource(modelContainer);
		fileDependency1.createResource(modelContainer);
		fileDependency2.createResource(modelContainer);
		fileDependency3.createResource(modelContainer);
		SpdxFile[] fileDependencies = new SpdxFile[] {fileDependency1, fileDependency2, fileDependency3};
		SpdxFile[] noDependencies = file.getFileDependencies();
		assertEquals(0, noDependencies.length);
		file.setFileDependencies(fileDependencies);
		SpdxFile[] result = file.getFileDependencies();
		assertTrue(UnitTestHelper.isArraysEqual(fileDependencies, result));
		SpdxFile file2 = new SpdxFile(modelContainer, fileResource.asNode());
		result = file2.getFileDependencies();
		assertTrue(UnitTestHelper.isArraysEqual(fileDependencies, result));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxFile#compareTo(org.spdx.rdfparser.model.SpdxFile)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testCompareTo() throws InvalidSPDXAnalysisException {
		
		String fileName1 = "afile";
		String fileName2 = "bfile";
		
		SpdxFile file1 = new SpdxFile("SpdxRef-1", fileName1, null, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE, null,
				null, new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
						"1123456789abcdef0123456789abcdef01234567")}, null, null, null);
		SpdxFile file2 = new SpdxFile("SpdxRef-1", fileName2, null, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE, null,
				null, new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
						"1123456789abcdef0123456789abcdef01234567")}, null, null, null);
		SpdxFile file3 = new SpdxFile("SpdxRef-1", fileName1, null, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE, null,
				null, new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
						"1123456789abcdef0123456789abcdef01234567")}, null, null, null);
		
		assertEquals(-1, file1.compareTo(file2));
		assertEquals(1, file2.compareTo(file1));
		assertEquals(0, file1.compareTo(file3));
	}
	
	@Test
	public void testNoassertionCopyright() throws InvalidSPDXAnalysisException {
		model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://somethingunique.com/something");
		SpdxFile file = new SpdxFile("SpdxRef-1", "filename", null, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE, null,
				null, new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
						"1123456789abcdef0123456789abcdef01234567")}, null, null, null);
		Resource fileResource = file.createResource(modelContainer);
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_FILE_COPYRIGHT).asNode();
		Triple m = Triple.createMatch(null, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			assertTrue(t.getObject().isURI());
			assertEquals(SpdxRdfConstants.URI_VALUE_NOASSERTION, t.getObject().getURI());
		}
		SpdxFile file2 = new SpdxFile(modelContainer, fileResource.asNode());
		assertEquals(SpdxRdfConstants.NOASSERTION_VALUE, file2.getCopyrightText());
	}
	
	@Test
	public void testSetComment() throws InvalidSPDXAnalysisException {
		model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://somethingunique.com/something");
		String COMMENT1 = "comment1";
		String COMMENT2 = "comment2";
		String COMMENT3 = "comment3";
		SpdxFile file = new SpdxFile("SpdxRef-1", "filename", COMMENT1, null, null, 
				COMPLEX_LICENSE, CONJUNCTIVE_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE, null,
				null, new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
						"1123456789abcdef0123456789abcdef01234567")}, null, null, null);
		assertEquals(file.getComment(), COMMENT1);
		Resource fileResource = file.createResource(modelContainer);
		file.setLicenseComment("see if this works");
		file.setComment(COMMENT2);
		SpdxFile file2 = new SpdxFile(modelContainer, fileResource.asNode());
		assertEquals(file2.getComment(), COMMENT2);
		file2.setComment(COMMENT3);
		assertEquals(file2.getComment(), COMMENT3);
	}

}
