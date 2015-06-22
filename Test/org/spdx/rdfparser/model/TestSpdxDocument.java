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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.license.SimpleLicensingInfo;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.model.Annotation.AnnotationType;
import org.spdx.rdfparser.model.Checksum.ChecksumAlgorithm;
import org.spdx.rdfparser.model.Relationship.RelationshipType;
import org.spdx.rdfparser.model.SpdxFile.FileType;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Gary
 *
 */
public class TestSpdxDocument {
	
	private static final String CONTAINER_NAMESPACE = "http://spdx.org/spdx/docs/abcd-e";
	private static final String ANNOTATOR1 = "Person: Annotator1";
	private static final String ANNOTATOR2 = "Person: Annotator2";
	private static final AnnotationType ANNOTATION_TYPE1 = AnnotationType.annotationType_review;
	private static final AnnotationType ANNOTATION_TYPE2 = AnnotationType.annotationType_other;
	private static final String ANNOTATION_COMMENT1 = "Comment 1 for annotation";
	private static final String ANNOTATION_COMMENT2 = "Comment 2 for annotation";
	private static final String DATE1 = "2010-01-29T18:30:22Z";
	private static final String DATE2 = "2015-01-29T18:30:22Z";
	private static final String[] CREATORS1 = new String[] {"Tool: SPDX tool", "Person: the person"};
	private static final String[] CREATORS2 = new String[] {"Tool: Teesst"};
	private static final String CREATOR_COMMENT1 = "Creator comment1";
	private static final String CREATOR_COMMENT2 = "Creator comment2";
	private static final String LICENSE_LISTV1 = "1.18";
	private static final String LICENSE_LISTV2 = "1.22";
	static final String SHA1_VALUE1 = "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12";
	static final String SHA1_VALUE2 = "2222e1c67a2d28fced849ee1bb76e7391b93eb12";
	private static final String DOC_COMMENT1 = "Doc Comment1";
	private static final String REFERENCED_DOC_URI1 = "http://referenced.document/uri1";
	private static final String REFERENCED_DOC_URI2 = "http://referenced.document/uri2";
	static final ExtractedLicenseInfo LICENSE1 = new ExtractedLicenseInfo("LicenseRef-1", "License Text 1");
	static final ExtractedLicenseInfo LICENSE2 = new ExtractedLicenseInfo("LicenseRef-2", "License Text 2");
	static final ExtractedLicenseInfo LICENSE3 = new ExtractedLicenseInfo("LicenseRef-3", "License Text 3");
	private static final String DOC_NAME1 = "DocName1";
	private static final String DOCID1 = "DocumentRef-1";
	private static final String DOCID2 = "DocumentRef-2";
	
	AnyLicenseInfo CCO_DATALICENSE;
	private SpdxDocumentContainer container;
	
	private Annotation ANNOTATION1;
	private Annotation ANNOTATION2;
	private SPDXCreatorInformation CREATIONINFO1;
	private SPDXCreatorInformation CREATIONINFO2;
	private Checksum CHECKSUM1;
	private Checksum CHECKSUM2;
	private ExternalDocumentRef EXTERNAL_REF1;
	private ExternalDocumentRef EXTERNAL_REF2;
	SpdxElement RELATED_ELEMENT1;
	SpdxElement RELATED_ELEMENT2;
	Relationship RELATIONSHIP1;
	Relationship RELATIONSHIP2;
	SPDXReview REVIEWER1;
	SPDXReview REVIEWER2;
	SpdxFile FILE1;
	SpdxFile FILE2;
	SpdxFile FILE3;
	SpdxPackage PACKAGE1;
	SpdxPackage PACKAGE2;
	SpdxPackage PACKAGE3;
	
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
		CCO_DATALICENSE = LicenseInfoFactory.getListedLicenseById("CC0-1.0");
		this.container = new SpdxDocumentContainer(CONTAINER_NAMESPACE);
		ANNOTATION1 = new Annotation(ANNOTATOR1, ANNOTATION_TYPE1, DATE1, ANNOTATION_COMMENT1);
		ANNOTATION2 = new Annotation(ANNOTATOR2, ANNOTATION_TYPE2, DATE2, ANNOTATION_COMMENT2);
		CREATIONINFO1 = new SPDXCreatorInformation(CREATORS1, DATE1, CREATOR_COMMENT1, LICENSE_LISTV1);
		CREATIONINFO2 = new SPDXCreatorInformation(CREATORS2, DATE2, CREATOR_COMMENT2, LICENSE_LISTV2);
		CHECKSUM1 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, SHA1_VALUE1);
		CHECKSUM2 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, SHA1_VALUE2);
		EXTERNAL_REF1 = new ExternalDocumentRef(REFERENCED_DOC_URI1, CHECKSUM1, DOCID1);
		EXTERNAL_REF2 = new ExternalDocumentRef(REFERENCED_DOC_URI2, CHECKSUM2, DOCID2);
		RELATED_ELEMENT1 = new SpdxElement("relatedElementName1", 
				"related element comment 1", null, null);
		RELATED_ELEMENT2 = new SpdxElement("relatedElementName2", 
				"related element comment 2", null, null);
		RELATIONSHIP1 = new Relationship(RELATED_ELEMENT1, 
				RelationshipType.relationshipType_contains, "Relationship Comment1");
		RELATIONSHIP2 = new Relationship(RELATED_ELEMENT2, 
				RelationshipType.relationshipType_dynamicLink, "Relationship Comment2");
		REVIEWER1 = new SPDXReview("Reviewer1", DATE1, "Reviewer Comment 1");
		REVIEWER2 = new SPDXReview("Reviewer2", DATE2, "Reviewer Comment 2");
		FILE1 = new SpdxFile("FileName1", "FileComment 1", 
				null, null,LICENSE1, new ExtractedLicenseInfo[] {LICENSE2}, 
				"File Copyright1", "License Comment1", new FileType[] {FileType.fileType_archive}, 
				new Checksum[] {CHECKSUM1},
				new String[] {"File Contrib1"}, "File Notice1", 
				new DoapProject[] {new DoapProject("Project1", "http://project.home.page/one")});
		FILE2 = new SpdxFile("FileName2", "FileComment 2", 
				null, null,LICENSE2, new ExtractedLicenseInfo[] {LICENSE3}, 
				"File Copyright2", "License Comment2", new FileType[] {FileType.fileType_source}, 
				new Checksum[] {CHECKSUM2},
				new String[] {"File Contrib2"}, "File Notice2", 
				new DoapProject[] {new DoapProject("Project2", "http://project.home.page/two")});
		FILE3 = new SpdxFile("FileName3", "FileComment 3", 
				null, null,LICENSE3, new ExtractedLicenseInfo[] {LICENSE1}, 
				"File Copyright3", "License Comment3", new FileType[] {FileType.fileType_text}, 
				new Checksum[] {CHECKSUM1},
				new String[] {"File Contrib3"}, "File Notice3", 
				new DoapProject[] {new DoapProject("Project3", "http://project.home.page/three")});
		PACKAGE1 = new SpdxPackage("Package 1", "Package Comments1", 
				null, null,LICENSE1, new SimpleLicensingInfo[] { LICENSE2}, 
				"Pkg Copyright1", "Pkg License Comment 1", LICENSE2, new Checksum[] {CHECKSUM1},
				"Pkg Description 1", "Downlodlocation1", new SpdxFile[] {FILE1}, 
				"http://home.page/one", "Person: originator1", "packagename1", 
				new SpdxPackageVerificationCode("0000e1c67a2d28fced849ee1bb76e7391b93eb12", new String[] {"excludedfile1", "excluedfiles2"}),
				"sourceinfo1", "summary1", "Person: supplier1", "version1");
		PACKAGE2 = new SpdxPackage("Package 2", "Package Comments2", 
				null, null,LICENSE2, new SimpleLicensingInfo[] { LICENSE3}, 
				"Pkg Copyright2", "Pkg License Comment 2", LICENSE3, new Checksum[] {CHECKSUM2},
				"Pkg Description 2", "Downlodlocation2", new SpdxFile[] {FILE2, FILE3}, 
				"http://home.page/two", "Person: originator2", "packagename2", 
				new SpdxPackageVerificationCode("2222e1c67a2d28fced849ee1bb76e7391b93eb12", new String[] {"excludedfile3", "excluedfiles4"}),
				"sourceinfo2", "summary2", "Person: supplier2", "version2");
		PACKAGE3 = new SpdxPackage("Package 3", "Package Comments3", 
				null, null,LICENSE1, new SimpleLicensingInfo[] { LICENSE2}, 
				"Pkg Copyright3", "Pkg License Comment 3", LICENSE3, new Checksum[] {CHECKSUM1},
				"Pkg Description 3", "Downlodlocation3", new SpdxFile[] {FILE3}, 
				"http://home.page/three", "Person: originator3", "packagename3", 
				new SpdxPackageVerificationCode("3333e1c67a2d28fced849ee1bb76e7391b93eb12", new String[] {"excludedfile4", "excluedfiles5"}),
				"sourceinfo3", "summary3", "Person: supplier3", "version3");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxDocument#getType(com.hp.hpl.jena.rdf.model.Model)}.
	 */
	@Test
	public void testGetType() {
		SpdxDocument doc = container.getSpdxDocument();
		Resource result = doc.getType(container.getModel());
		assertTrue(result.isURIResource());
		assertEquals(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_DOCUMENT, result.getURI());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxDocument#populateModel()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testPopulateModel() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {
			ANNOTATION1, ANNOTATION2	
		};
		ExternalDocumentRef[] externalDocumentRefs = new ExternalDocumentRef[] {
				EXTERNAL_REF1, EXTERNAL_REF2
		};
		ExtractedLicenseInfo[] extractedLicenseInfos = new ExtractedLicenseInfo[] {
				LICENSE1, LICENSE2
		};
		SPDXReview[] reviewers = new SPDXReview[] {
				REVIEWER1, REVIEWER2
		};
		SpdxItem[] items = new SpdxItem[] {
				FILE1, FILE2, PACKAGE1, PACKAGE2
		};
		SpdxDocument doc = container.getSpdxDocument();
		doc.setAnnotations(annotations);
		doc.setComment(DOC_COMMENT1);
		doc.setCreationInfo(CREATIONINFO1);
		doc.setDataLicense(CCO_DATALICENSE);
		doc.setExternalDocumentRefs(externalDocumentRefs);
		doc.setExtractedLicenseInfos(extractedLicenseInfos);
		doc.setName(DOC_NAME1);
		List<Relationship> relationships = Lists.newArrayList();
		relationships.add(RELATIONSHIP1);
		relationships.add(RELATIONSHIP2);
		doc.setRelationships(relationships.toArray(new Relationship[relationships.size()]));
		for (int i = 0; i < items.length; i++) {
			Relationship rel = new Relationship(items[i], 
					Relationship.RelationshipType.relationshipType_describes, "");
			relationships.add(rel);
			doc.addRelationship(rel);
		}
		doc.setReviewers(reviewers);
		assertTrue(UnitTestHelper.isArraysEquivalent(annotations, doc.getAnnotations()));
		assertEquals(DOC_COMMENT1, doc.getComment());
		assertEquals(CREATIONINFO1, doc.getCreationInfo());
		assertEquals(CCO_DATALICENSE, doc.getDataLicense());
		assertTrue(UnitTestHelper.isArraysEquivalent(externalDocumentRefs, doc.getExternalDocumentRefs()));
		assertTrue(UnitTestHelper.isArraysEqual(extractedLicenseInfos, doc.getExtractedLicenseInfos()));
		assertEquals(DOC_NAME1, doc.getName());
		assertTrue(UnitTestHelper.isArraysEquivalent(relationships.toArray(new Relationship[relationships.size()]), doc.getRelationships()));
		assertTrue(UnitTestHelper.isArraysEqual(reviewers, doc.getReviewers()));
		assertTrue(UnitTestHelper.isArraysEquivalent(items, doc.getDocumentDescribes()));
		
		Model model = container.getModel();
		SpdxDocumentContainer container2 = new SpdxDocumentContainer(model);
		SpdxDocument doc2 = container2.getSpdxDocument();
		assertTrue(UnitTestHelper.isArraysEquivalent(annotations, doc2.getAnnotations()));
		assertEquals(DOC_COMMENT1, doc2.getComment());
		assertEquals(CREATIONINFO1, doc2.getCreationInfo());
		assertEquals(CCO_DATALICENSE, doc2.getDataLicense());
		assertTrue(UnitTestHelper.isArraysEquivalent(externalDocumentRefs, doc2.getExternalDocumentRefs()));
		assertTrue(UnitTestHelper.isArraysEqual(extractedLicenseInfos, doc2.getExtractedLicenseInfos()));
		assertEquals(DOC_NAME1, doc2.getName());
		assertTrue(UnitTestHelper.isArraysEquivalent(relationships.toArray(new Relationship[relationships.size()]), doc2.getRelationships()));
		assertTrue(UnitTestHelper.isArraysEqual(reviewers, doc2.getReviewers()));
		assertTrue(UnitTestHelper.isArraysEquivalent(items, doc2.getDocumentDescribes()));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxDocument#equivalent(org.spdx.rdfparser.model.RdfModelObject)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testEquivalent() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {
				ANNOTATION1, ANNOTATION2	
			};
			ExternalDocumentRef[] externalDocumentRefs = new ExternalDocumentRef[] {
					EXTERNAL_REF1, EXTERNAL_REF2
			};
			ExtractedLicenseInfo[] extractedLicenseInfos = new ExtractedLicenseInfo[] {
					LICENSE1, LICENSE2
			};
			SPDXReview[] reviewers = new SPDXReview[] {
					REVIEWER1, REVIEWER2
			};
			SpdxItem[] items = new SpdxItem[] {
					FILE1, FILE2, PACKAGE1, PACKAGE2
			};
			SpdxDocument doc = container.getSpdxDocument();
			doc.setAnnotations(annotations);
			doc.setComment(DOC_COMMENT1);
			doc.setCreationInfo(CREATIONINFO1);

			doc.setDataLicense(CCO_DATALICENSE);
			doc.setExternalDocumentRefs(externalDocumentRefs);
			doc.setExtractedLicenseInfos(extractedLicenseInfos);
			doc.setName(DOC_NAME1);

			List<Relationship> relationships = Lists.newArrayList();
			relationships.add(RELATIONSHIP1);
			relationships.add(RELATIONSHIP2);
			doc.setRelationships(relationships.toArray(new Relationship[relationships.size()]));
			for (int i = 0; i < items.length; i++) {
				Relationship rel = new Relationship(items[i], 
						Relationship.RelationshipType.relationshipType_describes, "");
				relationships.add(rel);
				doc.addRelationship(rel);
			}
			doc.setReviewers(reviewers);
			assertTrue(UnitTestHelper.isArraysEquivalent(annotations, doc.getAnnotations()));
			assertEquals(DOC_COMMENT1, doc.getComment());
			assertEquals(CREATIONINFO1, doc.getCreationInfo());
			assertEquals(CCO_DATALICENSE, doc.getDataLicense());
			assertTrue(UnitTestHelper.isArraysEquivalent(externalDocumentRefs, doc.getExternalDocumentRefs()));
			assertTrue(UnitTestHelper.isArraysEqual(extractedLicenseInfos, doc.getExtractedLicenseInfos()));
			assertEquals(DOC_NAME1, doc.getName());
			assertTrue(UnitTestHelper.isArraysEquivalent(relationships.toArray(new Relationship[relationships.size()]), doc.getRelationships()));
			assertTrue(UnitTestHelper.isArraysEqual(reviewers, doc.getReviewers()));
			assertTrue(UnitTestHelper.isArraysEquivalent(items, doc.getDocumentDescribes()));
			
			assertTrue(doc.equivalent(doc));
			
			String container2Uri = "http://spdx.org/spdx/2ndoc/2342";
			SpdxDocumentContainer container2 = new SpdxDocumentContainer(container2Uri);
			SpdxDocument doc2 = container2.getSpdxDocument();
			doc2.setAnnotations(annotations);
			doc2.setComment(DOC_COMMENT1);
			doc2.setCreationInfo(CREATIONINFO1);
			doc2.setDataLicense(CCO_DATALICENSE);
			doc2.setExternalDocumentRefs(externalDocumentRefs);
			doc2.setExtractedLicenseInfos(extractedLicenseInfos);
			doc2.setName(DOC_NAME1);
			doc2.setRelationships(relationships.toArray(new Relationship[relationships.size()]));
			doc2.setReviewers(reviewers);
			assertTrue(doc.equivalent(doc2));
			// CreationInfo
			doc2.setCreationInfo(CREATIONINFO2);
			assertFalse(doc.equivalent(doc2));
			doc2.setCreationInfo(CREATIONINFO1);
			assertTrue(doc.equivalent(doc2));
			// DataLicense
			doc2.setDataLicense(LicenseInfoFactory.getListedLicenseById("APAFML"));
			assertFalse(doc.equivalent(doc2));
			doc2.setDataLicense(CCO_DATALICENSE);
			assertTrue(doc.equivalent(doc2));
			// ExternalDocumentRefs
			doc2.setExternalDocumentRefs(new ExternalDocumentRef[] {EXTERNAL_REF1});
			assertFalse(doc.equivalent(doc2));
			doc2.setExternalDocumentRefs(externalDocumentRefs);
			assertTrue(doc.equivalent(doc2));
			// ExtracteLicenseInfos
			doc2.setExtractedLicenseInfos(new ExtractedLicenseInfo[] {LICENSE2});
			assertFalse(doc.equivalent(doc2));
			doc2.setExtractedLicenseInfos(extractedLicenseInfos);
			assertTrue(doc.equivalent(doc2));
			// Reviewers
			doc2.setReviewers(new SPDXReview[] {REVIEWER2});
			assertFalse(doc.equivalent(doc2));
			doc2.setReviewers(reviewers);
			assertTrue(doc.equivalent(doc2));
			// Items
			doc2.addRelationship(new Relationship(FILE3, 
					Relationship.RelationshipType.relationshipType_describes, ""));
			doc2.addRelationship(new Relationship(PACKAGE3, 
					Relationship.RelationshipType.relationshipType_describes, ""));
			assertFalse(doc.equivalent(doc2));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxDocument#verify()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {
				ANNOTATION1, ANNOTATION2	
			};
			ExternalDocumentRef[] externalDocumentRefs = new ExternalDocumentRef[] {
					EXTERNAL_REF1, EXTERNAL_REF2
			};
			ExtractedLicenseInfo[] extractedLicenseInfos = new ExtractedLicenseInfo[] {
					LICENSE1, LICENSE2
			};
			Relationship[] relationships = new Relationship[] {
					RELATIONSHIP1, RELATIONSHIP2
			};
			SpdxItem[] items = new SpdxItem[] {
					FILE1, FILE2, PACKAGE1, PACKAGE2
			};
			SpdxDocument doc = container.getSpdxDocument();
			doc.setAnnotations(annotations);
			doc.setComment(DOC_COMMENT1);
			doc.setCreationInfo(CREATIONINFO1);
			doc.setDataLicense(CCO_DATALICENSE);
			doc.setExternalDocumentRefs(externalDocumentRefs);
			doc.setExtractedLicenseInfos(extractedLicenseInfos);
			doc.setName(DOC_NAME1);
			doc.setRelationships(relationships);
			for (int i = 0; i < items.length; i++) {
				doc.addRelationship(new Relationship(items[i], 
						Relationship.RelationshipType.relationshipType_describes, ""));
			}
			List<String> result = doc.verify();
			assertEquals(0, result.size());
			// data license
			doc.setDataLicense(LicenseInfoFactory.getListedLicenseById("AFL-3.0"));
			result = doc.verify();
			assertEquals(1, result.size());
			// Name
			doc.setName(null);
			result = doc.verify();
			assertEquals(2, result.size());
			// SpecVersion
			doc.setSpecVersion(null);
			result = doc.verify();
			assertEquals(3, result.size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxDocument#getPackagesFromItems(org.spdx.rdfparser.model.SpdxItem[])}.
	 */
	@Test
	public void testGetPackagesFromItems() {
		SpdxItem[] allItems = new SpdxItem[] {FILE1, FILE2, FILE3, PACKAGE1, PACKAGE2, PACKAGE3};
		SpdxItem[] expected = new SpdxItem[] {PACKAGE1, PACKAGE2, PACKAGE3};
		SpdxItem[] result = container.getSpdxDocument().getPackagesFromItems(allItems);
		assertTrue(UnitTestHelper.isArraysEqual(expected, result));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxDocument#getFilesFromItems(org.spdx.rdfparser.model.SpdxItem[])}.
	 */
	@Test
	public void testGetFilesFromItems() {
		SpdxItem[] allItems = new SpdxItem[] {FILE1, FILE2, FILE3, PACKAGE1, PACKAGE2, PACKAGE3};
		SpdxItem[] expected = new SpdxItem[] {FILE1, FILE2, FILE3};
		SpdxItem[] result = container.getSpdxDocument().getFilesFromItems(allItems);
		assertTrue(UnitTestHelper.isArraysEqual(expected, result));
	}


	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxDocument#getDocumentContainer()}.
	 */
	@Test
	public void testGetDocumentContainer() {
		assertEquals(container, container.getSpdxDocument().getDocumentContainer());
		
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxDocument#setCreationInfo(org.spdx.rdfparser.SPDXCreatorInformation)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetCreationInfo() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {
				ANNOTATION1, ANNOTATION2	
			};
			ExternalDocumentRef[] externalDocumentRefs = new ExternalDocumentRef[] {
					EXTERNAL_REF1, EXTERNAL_REF2
			};
			ExtractedLicenseInfo[] extractedLicenseInfos = new ExtractedLicenseInfo[] {
					LICENSE1, LICENSE2
			};
			Relationship[] relationships = new Relationship[] {
					RELATIONSHIP1, RELATIONSHIP2
			};
			SpdxItem[] items = new SpdxItem[] {
					FILE1, FILE2, PACKAGE1, PACKAGE2
			};
			SpdxDocument doc = container.getSpdxDocument();
			doc.setAnnotations(annotations);
			doc.setComment(DOC_COMMENT1);
			doc.setCreationInfo(CREATIONINFO1);
			doc.setDataLicense(CCO_DATALICENSE);
			doc.setExternalDocumentRefs(externalDocumentRefs);
			doc.setExtractedLicenseInfos(extractedLicenseInfos);
			doc.setName(DOC_NAME1);
			doc.setRelationships(relationships);
			for (int i = 0; i < items.length; i++) {
				doc.addRelationship(new Relationship(items[i], 
						Relationship.RelationshipType.relationshipType_describes, ""));
			}
			assertEquals(CREATIONINFO1, doc.getCreationInfo());
			doc.setCreationInfo(CREATIONINFO2);
			assertEquals(CREATIONINFO2, doc.getCreationInfo());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxDocument#setDataLicense(org.spdx.rdfparser.license.AnyLicenseInfo)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetDataLicense() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {
				ANNOTATION1, ANNOTATION2	
			};
			ExternalDocumentRef[] externalDocumentRefs = new ExternalDocumentRef[] {
					EXTERNAL_REF1, EXTERNAL_REF2
			};
			ExtractedLicenseInfo[] extractedLicenseInfos = new ExtractedLicenseInfo[] {
					LICENSE1, LICENSE2
			};
			Relationship[] relationships = new Relationship[] {
					RELATIONSHIP1, RELATIONSHIP2
			};
			SpdxItem[] items = new SpdxItem[] {
					FILE1, FILE2, PACKAGE1, PACKAGE2
			};
			SpdxDocument doc = container.getSpdxDocument();
			doc.setAnnotations(annotations);
			doc.setComment(DOC_COMMENT1);
			doc.setCreationInfo(CREATIONINFO1);
			doc.setDataLicense(CCO_DATALICENSE);
			doc.setExternalDocumentRefs(externalDocumentRefs);
			doc.setExtractedLicenseInfos(extractedLicenseInfos);
			doc.setName(DOC_NAME1);
			doc.setRelationships(relationships);
			for (int i = 0; i < items.length; i++) {
				doc.addRelationship(new Relationship(items[i], 
						Relationship.RelationshipType.relationshipType_describes, ""));
			}
			
			assertEquals(CCO_DATALICENSE, doc.getDataLicense());
			SpdxListedLicense lic = LicenseInfoFactory.getListedLicenseById("Apache-2.0");
			doc.setDataLicense(lic);
			assertEquals(lic, doc.getDataLicense());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxDocument#setExternalDocumentRefs(org.spdx.rdfparser.model.ExternalDocumentRef[])}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetExternalDocumentRefs() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {
				ANNOTATION1, ANNOTATION2	
			};
			ExternalDocumentRef[] externalDocumentRefs = new ExternalDocumentRef[] {
					EXTERNAL_REF1, EXTERNAL_REF2
			};
			ExtractedLicenseInfo[] extractedLicenseInfos = new ExtractedLicenseInfo[] {
					LICENSE1, LICENSE2
			};
			Relationship[] relationships = new Relationship[] {
					RELATIONSHIP1, RELATIONSHIP2
			};
			SpdxItem[] items = new SpdxItem[] {
					FILE1, FILE2, PACKAGE1, PACKAGE2
			};
			SpdxDocument doc = container.getSpdxDocument();
			doc.setAnnotations(annotations);
			doc.setComment(DOC_COMMENT1);
			doc.setCreationInfo(CREATIONINFO1);
			doc.setDataLicense(CCO_DATALICENSE);
			doc.setExternalDocumentRefs(externalDocumentRefs);
			doc.setExtractedLicenseInfos(extractedLicenseInfos);
			doc.setName(DOC_NAME1);
			doc.setRelationships(relationships);
			for (int i = 0; i < items.length; i++) {
				doc.addRelationship(new Relationship(items[i], 
						Relationship.RelationshipType.relationshipType_describes, ""));
			}
			assertTrue(UnitTestHelper.isArraysEquivalent(externalDocumentRefs, doc.getExternalDocumentRefs()));
			ExternalDocumentRef[] ref2 = new ExternalDocumentRef[] {
					EXTERNAL_REF2
			};
			doc.setExternalDocumentRefs(ref2);
			assertTrue(UnitTestHelper.isArraysEquivalent(ref2, doc.getExternalDocumentRefs()));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxDocument#setExtractedLicenseInfos(org.spdx.rdfparser.license.ExtractedLicenseInfo[])}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetExtractedLicenseInfos() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {
				ANNOTATION1, ANNOTATION2	
			};
			ExternalDocumentRef[] externalDocumentRefs = new ExternalDocumentRef[] {
					EXTERNAL_REF1, EXTERNAL_REF2
			};
			ExtractedLicenseInfo[] extractedLicenseInfos = new ExtractedLicenseInfo[] {
					LICENSE1, LICENSE2
			};
			Relationship[] relationships = new Relationship[] {
					RELATIONSHIP1, RELATIONSHIP2
			};
			SpdxItem[] items = new SpdxItem[] {
					FILE1, FILE2, PACKAGE1, PACKAGE2
			};
			SpdxDocument doc = container.getSpdxDocument();
			doc.setAnnotations(annotations);
			doc.setComment(DOC_COMMENT1);
			doc.setCreationInfo(CREATIONINFO1);
			doc.setDataLicense(CCO_DATALICENSE);
			doc.setExternalDocumentRefs(externalDocumentRefs);
			doc.setExtractedLicenseInfos(extractedLicenseInfos);
			doc.setName(DOC_NAME1);
			doc.setRelationships(relationships);
			for (int i = 0; i < items.length; i++) {
				doc.addRelationship(new Relationship(items[i], 
						Relationship.RelationshipType.relationshipType_describes, ""));
			}
			assertTrue(UnitTestHelper.isArraysEqual(extractedLicenseInfos, doc.getExtractedLicenseInfos()));
			ExtractedLicenseInfo[] infos2 = new ExtractedLicenseInfo[] {
					LICENSE2, LICENSE3
			};
			doc.setExtractedLicenseInfos(infos2);
			assertTrue(UnitTestHelper.isArraysEqual(infos2, doc.getExtractedLicenseInfos()));
	}

	@Test
	public void testAddExtractedLicenseInfos() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {
				ANNOTATION1, ANNOTATION2	
			};
			ExternalDocumentRef[] externalDocumentRefs = new ExternalDocumentRef[] {
					EXTERNAL_REF1, EXTERNAL_REF2
			};
			ExtractedLicenseInfo[] extractedLicenseInfos = new ExtractedLicenseInfo[] {
					LICENSE1
			};
			Relationship[] relationships = new Relationship[] {
					RELATIONSHIP1, RELATIONSHIP2
			};
			SpdxItem[] items = new SpdxItem[] {
					FILE1, FILE2, PACKAGE1, PACKAGE2
			};
			SpdxDocument doc = container.getSpdxDocument();
			doc.setAnnotations(annotations);
			doc.setComment(DOC_COMMENT1);
			doc.setCreationInfo(CREATIONINFO1);
			doc.setDataLicense(CCO_DATALICENSE);
			doc.setExternalDocumentRefs(externalDocumentRefs);
			doc.setExtractedLicenseInfos(extractedLicenseInfos);
			doc.setName(DOC_NAME1);
			doc.setRelationships(relationships);
			for (int i = 0; i < items.length; i++) {
				doc.addRelationship(new Relationship(items[i], 
						Relationship.RelationshipType.relationshipType_describes, ""));
			}
			assertTrue(UnitTestHelper.isArraysEqual(extractedLicenseInfos, doc.getExtractedLicenseInfos()));

			doc.addExtractedLicenseInfos(LICENSE2);
			assertEquals(2, doc.getExtractedLicenseInfos().length);
			doc.addExtractedLicenseInfos(LICENSE3);
			assertEquals(3, doc.getExtractedLicenseInfos().length);
			ExtractedLicenseInfo[] expected = new ExtractedLicenseInfo[] {
					LICENSE1, LICENSE2, LICENSE3
			};
			assertTrue(UnitTestHelper.isArraysEqual(expected, doc.getExtractedLicenseInfos()));
	}
	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxDocument#setSpecVersion(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetSpecVersion() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {
				ANNOTATION1, ANNOTATION2	
			};
			ExternalDocumentRef[] externalDocumentRefs = new ExternalDocumentRef[] {
					EXTERNAL_REF1, EXTERNAL_REF2
			};
			ExtractedLicenseInfo[] extractedLicenseInfos = new ExtractedLicenseInfo[] {
					LICENSE1, LICENSE2
			};
			Relationship[] relationships = new Relationship[] {
					RELATIONSHIP1, RELATIONSHIP2
			};
			SpdxItem[] items = new SpdxItem[] {
					FILE1, FILE2, PACKAGE1, PACKAGE2
			};
			SpdxDocument doc = container.getSpdxDocument();
			doc.setAnnotations(annotations);
			doc.setComment(DOC_COMMENT1);
			doc.setCreationInfo(CREATIONINFO1);
			doc.setDataLicense(CCO_DATALICENSE);
			doc.setExternalDocumentRefs(externalDocumentRefs);
			doc.setExtractedLicenseInfos(extractedLicenseInfos);
			doc.setName(DOC_NAME1);
			doc.setRelationships(relationships);
			for (int i = 0; i < items.length; i++) {
				doc.addRelationship(new Relationship(items[i], 
						Relationship.RelationshipType.relationshipType_describes, ""));
			}
			
			assertEquals(SpdxDocumentContainer.CURRENT_SPDX_VERSION, doc.getSpecVersion());
			String ver = "2.1";
			doc.setSpecVersion(ver);
			assertEquals(ver, doc.getSpecVersion());
	}

}
