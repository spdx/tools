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
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.SimpleLicensingInfo;
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
public class TestSpdxElementFactory {

	static final String DOCUMENT_NAMESPACE = "http://doc/name/space#";
	static final String ELEMENT_NAME1 = "element1";
	static final String ELEMENT_NAME2 = "element2";
	static final String ELEMENT_COMMENT1 = "comment1";
	static final String ELEMENT_COMMENT2 = "comment2";
	
	static final String DATE_NOW = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT).format(new Date());
	static final Annotation ANNOTATION1 = new Annotation("Annotator1", 
			AnnotationType.annotationType_other, DATE_NOW, "Comment1");
	static final Annotation ANNOTATION2 = new Annotation("Annotator2", 
			AnnotationType.annotationType_review, DATE_NOW, "Comment2");
	static final SpdxElement RELATED_ELEMENT1 = new SpdxElement("relatedElementName1", 
			"related element comment 1", null, null);
	static final SpdxElement RELATED_ELEMENT2 = new SpdxElement("relatedElementName2", 
			"related element comment 2", null, null);
	static final Relationship RELATIONSHIP1 = new Relationship(RELATED_ELEMENT1, 
			RelationshipType.relationshipType_contains, "Relationship Comment1");
	static final Relationship RELATIONSHIP2 = new Relationship(RELATED_ELEMENT2, 
			RelationshipType.relationshipType_dynamicLink, "Relationship Comment2");
	static final ExtractedLicenseInfo LICENSE1 = new ExtractedLicenseInfo("LicenseRef-1", "License Text 1");
	static final ExtractedLicenseInfo LICENSE2 = new ExtractedLicenseInfo("LicenseRef-2", "License Text 2");
	static final String COPYRIGHT_TEXT1 = "copyright text 1";
	static final String COPYRIGHT_TEXT2 = "copyright text 2";
	static final String LICENSE_COMMENT1 = "License Comment 1";
	static final String LICENSE_COMMENT2 = "License comment 2";
	
	String SPDX_ID1 = "SPDXRef-1";
	String SPDX_ID2 = "SPDXRef-2";
	
	String FILE_CONTRIBUTOR1 = "File Contributor 1";
	String FILE_CONTRIBUTOR2 = "File Contributor 2";
	
	String NOTICE_TEXT1 = "Notice1";
	String NOTICE_TEXT2 = "Notice2";
	
	Checksum CHECKSUM1 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, 
				"2fd4e1c67a2d28fced849ee1bb76e7391b93eb12");
	Checksum CHECKSUM2 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, 
			"0000e1c67a2d28fced849ee1bb76e7391b93eb12");
	
	FileType FILE_TYPE1 = FileType.fileType_image;
	FileType FILE_TYPE2 = FileType.fileType_audio;
	
	DoapProject DOAP_PROJECT1 = new DoapProject("Project1Name", "http://com.projct1");
	DoapProject DOAP_PROJECT2 = new DoapProject("Second project name", "http://yet.another.project/hi");
	
	String documentNamespace;
	Model model;
	IModelContainer modelContainer = new IModelContainer() {

		@Override
		public Model getModel() {
			return model;
		}

		@Override
		public String getDocumentNamespace() {
			return documentNamespace;
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
		model = ModelFactory.createDefaultModel();
		documentNamespace = DOCUMENT_NAMESPACE;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElementFactory#createElementFromModel(org.spdx.rdfparser.IModelContainer, com.hp.hpl.jena.graph.Node)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testCreateElementFromModel() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		String[] fileContributors = new String[] {FILE_CONTRIBUTOR1, FILE_CONTRIBUTOR2};
		
		// SpdxFile
		SimpleLicensingInfo[] extractedLicenses = new ExtractedLicenseInfo[] {LICENSE2};
		DoapProject[] artifactOfs = new DoapProject[] {DOAP_PROJECT1, DOAP_PROJECT2};
		SpdxFile file = new SpdxFile(SPDX_ID1, ELEMENT_NAME1, ELEMENT_COMMENT1, 
				annotations, relationships,LICENSE1, extractedLicenses, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1, new FileType[] {FILE_TYPE1}, new Checksum[] {CHECKSUM1},
				fileContributors, NOTICE_TEXT1, artifactOfs);
		Resource r = file.createResource(modelContainer);
		SpdxElement result = SpdxElementFactory.createElementFromModel(modelContainer, r.asNode());
		assertTrue(result instanceof SpdxFile);
		SpdxFile fileResult = (SpdxFile)result;
		assertEquals(ELEMENT_NAME1, fileResult.getName());
		assertEquals(ELEMENT_COMMENT1, fileResult.getComment());
		assertTrue(UnitTestHelper.isArraysEqual(annotations, fileResult.getAnnotations()));
		assertTrue(UnitTestHelper.isArraysEqual(relationships, fileResult.getRelationships()));
		assertEquals(LICENSE1, fileResult.getLicenseConcluded());
		assertEquals(COPYRIGHT_TEXT1, fileResult.getCopyrightText());
		assertEquals(LICENSE_COMMENT1, fileResult.getLicenseComment());
		
		// SpdxPackage
		SpdxPackage sPackage = new SpdxPackage(ELEMENT_NAME1, ELEMENT_COMMENT1, 
				annotations, relationships,LICENSE1, new SimpleLicensingInfo[] { LICENSE2}, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1);
		r = sPackage.createResource(modelContainer);
		result = SpdxElementFactory.createElementFromModel(modelContainer, r.asNode());
		assertTrue(result instanceof SpdxPackage);
		SpdxPackage packageResult = (SpdxPackage)result;
		assertEquals(ELEMENT_NAME1, packageResult.getName());
		assertEquals(ELEMENT_COMMENT1, packageResult.getComment());
		assertTrue(UnitTestHelper.isArraysEqual(annotations, packageResult.getAnnotations()));
		assertTrue(UnitTestHelper.isArraysEqual(relationships, packageResult.getRelationships()));
// UNCOMMENT THE LINES BELOW ONCE SPDXFILE is IMPLEMENTED
//		assertEquals(LICENSE1, packageResult.getLicenseConcluded());
//		assertEquals(LICENSE2, packageResult.getLicenseDeclared());
//		assertEquals(COPYRIGHT_TEXT1, packageResult.getCopyrightText());
//		assertEquals(LICENSE_COMMENT1, packageResult.getLicenseComment());
		
		// SpdxItem
		SpdxItem item = new SpdxItem(ELEMENT_NAME1, ELEMENT_COMMENT1, 
				annotations, relationships,LICENSE1, new SimpleLicensingInfo[] {LICENSE2}, 
				COPYRIGHT_TEXT1, LICENSE_COMMENT1);
		r = item.createResource(modelContainer);
		result = SpdxElementFactory.createElementFromModel(modelContainer, r.asNode());
		assertTrue(result instanceof SpdxItem);
		SpdxItem itemResult = (SpdxItem)result;
		assertEquals(ELEMENT_NAME1, itemResult.getName());
		assertEquals(ELEMENT_COMMENT1, itemResult.getComment());
		assertTrue(UnitTestHelper.isArraysEqual(annotations, itemResult.getAnnotations()));
		assertTrue(UnitTestHelper.isArraysEqual(relationships, itemResult.getRelationships()));
// UNCOMMENT THE LINES BELOW ONCE SPDXFILE is IMPLEMENTED
//		assertEquals(LICENSE1, itemResult.getLicenseConcluded());
//		assertEquals(LICENSE2, itemResult.getLicenseDeclared());
//		assertEquals(COPYRIGHT_TEXT1, itemResult.getCopyrightText());
//		assertEquals(LICENSE_COMMENT1, itemResult.getLicenseComment());
		
		// SpdxElement
		SpdxElement element = new SpdxElement(ELEMENT_NAME1, ELEMENT_COMMENT1, 
				annotations, relationships);
		r = element.createResource(modelContainer);
		result = SpdxElementFactory.createElementFromModel(modelContainer, r.asNode());
		assertTrue(result instanceof SpdxElement);
		SpdxElement elementResult = (SpdxElement)result;
		assertEquals(ELEMENT_NAME1, elementResult.getName());
		assertEquals(ELEMENT_COMMENT1, elementResult.getComment());
		assertTrue(UnitTestHelper.isArraysEqual(annotations, elementResult.getAnnotations()));
		assertTrue(UnitTestHelper.isArraysEqual(relationships, elementResult.getRelationships()));
	}

}
