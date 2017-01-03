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
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.model.Annotation.AnnotationType;
import org.spdx.rdfparser.model.Relationship.RelationshipType;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

/**
 * @author Gary
 *
 */
public class TestSpdxItem {

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
			RelationshipType.CONTAINS, "Relationship Comment1");
	static final Relationship RELATIONSHIP2 = new Relationship(RELATED_ELEMENT2,
			RelationshipType.DYNAMIC_LINK, "Relationship Comment2");
	static final ExtractedLicenseInfo LICENSE1 = new ExtractedLicenseInfo("LicenseRef-1", "License Text 1");
	static final ExtractedLicenseInfo LICENSE2 = new ExtractedLicenseInfo("LicenseRef-2", "License Text 2");
	static final ExtractedLicenseInfo LICENSE3 = new ExtractedLicenseInfo("LicenseRef-3", "License Text 3");
	static final ExtractedLicenseInfo[] LICENSES = new  ExtractedLicenseInfo[] {LICENSE2, LICENSE3};
	static final String COPYRIGHT_TEXT1 = "copyright text 1";
	static final String COPYRIGHT_TEXT2 = "copyright text 2";
	static final String LICENSE_COMMENT1 = "License Comment 1";
	static final String LICENSE_COMMENT2 = "License comment 2";


	String documentNamespace;
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
		documentNamespace = DOCUMENT_NAMESPACE;
		modelContainer = new ModelContainerForTest(model, documentNamespace);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}


	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxItem#getType(org.apache.jena.rdf.model.Model)}.
	 */
	@Test
	public void testGetType() {
		Annotation[] annotations = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		SpdxItem item = new SpdxItem(ELEMENT_NAME1, ELEMENT_COMMENT1,
				annotations, relationships, LICENSE1, LICENSES,
				COPYRIGHT_TEXT1, LICENSE_COMMENT1);
		assertTrue(item.getType(model).getURI().endsWith(SpdxRdfConstants.CLASS_SPDX_ITEM));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxItem#equals(java.lang.Object)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testEqualsEquivalent() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		SpdxItem item = new SpdxItem(ELEMENT_NAME1, ELEMENT_COMMENT1,
				annotations, relationships, LICENSE1, LICENSES,
				COPYRIGHT_TEXT1, LICENSE_COMMENT1);
		assertTrue(item.equivalent(item));
		item.createResource(modelContainer);
		assertTrue(item.equivalent(item));
		SpdxItem item2 = new SpdxItem(ELEMENT_NAME1, ELEMENT_COMMENT1,
				annotations, relationships, LICENSE1, LICENSES,
				COPYRIGHT_TEXT1, LICENSE_COMMENT1);
		assertTrue(item.equivalent(item2));
		// Name
		item2.setName(ELEMENT_NAME2);
		assertFalse(item.equivalent(item2));
		item2.setName(ELEMENT_NAME1);
		assertTrue(item.equivalent(item2));
		// Comment
		item2.setComment(ELEMENT_COMMENT2);
		assertFalse(item.equivalent(item2));
		item2.setComment(ELEMENT_COMMENT1);
		assertTrue(item.equivalent(item2));
		// Annotations
		// different order
		Annotation[] annotations2 = new Annotation[] {ANNOTATION2, ANNOTATION1};
		item2.setAnnotations(annotations2);
		assertTrue(item.equivalent(item2));
		item2.setAnnotations(new Annotation[] {ANNOTATION1});
		assertFalse(item.equivalent(item2));
		item2.setAnnotations(new Annotation[] {});
		assertFalse(item.equivalent(item2));
		item2.setAnnotations(null);
		assertFalse(item.equivalent(item2));
		item2.setAnnotations(annotations);
		assertTrue(item.equivalent(item2));
		// Relationships
		// different order
		Relationship[] relationships2 = new Relationship[] {RELATIONSHIP2, RELATIONSHIP1};
		item2.setRelationships(relationships2);
		assertTrue(item.equivalent(item2));
		item2.setRelationships(new Relationship[] {RELATIONSHIP1});
		assertFalse(item.equivalent(item2));
		item2.setRelationships(new Relationship[] {});
		assertFalse(item.equivalent(item2));
		item2.setRelationships(null);
		assertFalse(item.equivalent(item2));
		item2.setRelationships(relationships);
		assertTrue(item.equivalent(item2));
		// License concluded
		item2.setLicenseConcluded(LICENSE2);
		assertFalse(item.equivalent(item2));
		item2.setLicenseConcluded(null);
		assertFalse(item.equivalent(item2));
		item2.setLicenseConcluded(LICENSE1);
		assertTrue(item.equivalent(item2));
		// License info in files
		ExtractedLicenseInfo[] licenses2 = new ExtractedLicenseInfo[] {LICENSE1};
		item2.setLicenseInfosFromFiles(licenses2);
		assertFalse(item.equivalent(item2));
		item2.setLicenseInfosFromFiles(null);
		assertFalse(item.equivalent(item2));
		item2.setLicenseInfosFromFiles(LICENSES);
		assertTrue(item.equivalent(item2));
		// Copyright text
		item2.setCopyrightText(COPYRIGHT_TEXT2);
		assertFalse(item.equivalent(item2));
		item2.setCopyrightText(null);
		assertFalse(item.equivalent(item2));
		item2.setCopyrightText(COPYRIGHT_TEXT1);
		assertTrue(item.equivalent(item2));
		// License comment
		item2.setLicenseComments(LICENSE_COMMENT2);
		assertFalse(item.equivalent(item2));
		item2.setLicenseComments(null);
		assertFalse(item.equivalent(item2));
		item2.setLicenseComments(LICENSE_COMMENT1);
		assertTrue(item.equivalent(item2));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxItem#SpdxItem(org.spdx.rdfparser.IModelContainer, org.apache.jena.graph.Node)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testSpdxItemIModelContainerNode() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		SpdxItem item = new SpdxItem(ELEMENT_NAME1, ELEMENT_COMMENT1,
				annotations, relationships, LICENSE1, LICENSES,
				COPYRIGHT_TEXT1, LICENSE_COMMENT1);
		assertEquals(ELEMENT_NAME1, item.getName());
		assertEquals(ELEMENT_COMMENT1, item.getComment());
		assertTrue(UnitTestHelper.isArraysEqual(annotations, item.getAnnotations()));
		assertTrue(UnitTestHelper.isArraysEqual(relationships, item.getRelationships()));
		assertEquals(LICENSE1, item.getLicenseConcluded());
		AnyLicenseInfo[] licenses = item.getLicenseInfoFromFiles();
		assertTrue(UnitTestHelper.isArraysEqual(LICENSES, licenses));
		assertEquals(COPYRIGHT_TEXT1, item.getCopyrightText());
		assertEquals(LICENSE_COMMENT1, item.getLicenseComments());
		Resource r = item.createResource(modelContainer);
		SpdxItem item2 = new SpdxItem(modelContainer, r.asNode());
		assertEquals(item.getName(), item2.getName());
		assertEquals(item.getComment(), item2.getComment());
		assertTrue(UnitTestHelper.isArraysEqual(item.getAnnotations(), item2.getAnnotations()));
		assertTrue(UnitTestHelper.isArraysEqual(item.getRelationships(), item2.getRelationships()));
		assertEquals(item.getLicenseConcluded(), item2.getLicenseConcluded());
		assertTrue(UnitTestHelper.isArraysEqual(item.getLicenseInfoFromFiles(), item2.getLicenseInfoFromFiles()));
		assertEquals(item.getCopyrightText(), item2.getCopyrightText());
		assertEquals(item.getLicenseComments(), item2.getLicenseComments());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxItem#SpdxItem(java.lang.String, java.lang.String, org.spdx.rdfparser.model.Annotation[], org.spdx.rdfparser.model.Relationship[], org.spdx.rdfparser.license.AnyLicenseInfo, org.spdx.rdfparser.license.AnyLicenseInfo, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSpdxItemStringStringAnnotationArrayRelationshipArrayAnyLicenseInfoAnyLicenseInfoStringString() {
		Annotation[] annotations = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		SpdxItem item = new SpdxItem(ELEMENT_NAME1, ELEMENT_COMMENT1,
				annotations, relationships, LICENSE1, LICENSES,
				COPYRIGHT_TEXT1, LICENSE_COMMENT1);
		assertEquals(ELEMENT_NAME1, item.getName());
		assertEquals(ELEMENT_COMMENT1, item.getComment());
		assertTrue(UnitTestHelper.isArraysEqual(annotations, item.getAnnotations()));
		assertTrue(UnitTestHelper.isArraysEqual(relationships, item.getRelationships()));
		assertEquals(LICENSE1, item.getLicenseConcluded());
		assertTrue(UnitTestHelper.isArraysEqual(LICENSES, item.getLicenseInfoFromFiles()));
		assertEquals(COPYRIGHT_TEXT1, item.getCopyrightText());
		assertEquals(LICENSE_COMMENT1, item.getLicenseComments());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxItem#setLicenseConcluded(org.spdx.rdfparser.license.AnyLicenseInfo)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testSetLicenseConcluded() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		SpdxItem item = new SpdxItem(ELEMENT_NAME1, ELEMENT_COMMENT1,
				annotations, relationships, LICENSE1, LICENSES,
				COPYRIGHT_TEXT1, LICENSE_COMMENT1);
		assertEquals(ELEMENT_NAME1, item.getName());
		assertEquals(ELEMENT_COMMENT1, item.getComment());
		assertTrue(UnitTestHelper.isArraysEqual(annotations, item.getAnnotations()));
		assertTrue(UnitTestHelper.isArraysEqual(relationships, item.getRelationships()));
		assertEquals(LICENSE1, item.getLicenseConcluded());
		assertTrue(UnitTestHelper.isArraysEqual(LICENSES, item.getLicenseInfoFromFiles()));
		assertEquals(COPYRIGHT_TEXT1, item.getCopyrightText());
		assertEquals(LICENSE_COMMENT1, item.getLicenseComments());
		Resource r = item.createResource(modelContainer);
		item.setLicenseConcluded(LICENSE2);
		assertEquals(LICENSE2, item.getLicenseConcluded());
		SpdxItem item2= new SpdxItem(modelContainer, r.asNode());
		assertEquals(LICENSE2, item2.getLicenseConcluded());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxItem#setLicenseDeclared(org.spdx.rdfparser.license.AnyLicenseInfo)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testSetLicenseInfosFromFiles() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		SpdxItem item = new SpdxItem(ELEMENT_NAME1, ELEMENT_COMMENT1,
				annotations, relationships, LICENSE1, LICENSES,
				COPYRIGHT_TEXT1, LICENSE_COMMENT1);
		assertEquals(ELEMENT_NAME1, item.getName());
		assertEquals(ELEMENT_COMMENT1, item.getComment());
		assertTrue(UnitTestHelper.isArraysEqual(annotations, item.getAnnotations()));
		assertTrue(UnitTestHelper.isArraysEqual(relationships, item.getRelationships()));
		assertEquals(LICENSE1, item.getLicenseConcluded());
		assertTrue(UnitTestHelper.isArraysEqual(LICENSES, item.getLicenseInfoFromFiles()));
		assertEquals(COPYRIGHT_TEXT1, item.getCopyrightText());
		assertEquals(LICENSE_COMMENT1, item.getLicenseComments());
		Resource r = item.createResource(modelContainer);
		AnyLicenseInfo[] newlicenses = new AnyLicenseInfo[] {LICENSE1};
		item.setLicenseInfosFromFiles(newlicenses);
		assertTrue(UnitTestHelper.isArraysEqual(newlicenses, item.getLicenseInfoFromFiles()));
		SpdxItem item2= new SpdxItem(modelContainer, r.asNode());
		assertTrue(UnitTestHelper.isArraysEqual(newlicenses, item2.getLicenseInfoFromFiles()));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxItem#setCopyrightText(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testSetCopyrightText() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		SpdxItem item = new SpdxItem(ELEMENT_NAME1, ELEMENT_COMMENT1,
				annotations, relationships, LICENSE1, LICENSES,
				COPYRIGHT_TEXT1, LICENSE_COMMENT1);
		assertEquals(ELEMENT_NAME1, item.getName());
		assertEquals(ELEMENT_COMMENT1, item.getComment());
		assertTrue(UnitTestHelper.isArraysEqual(annotations, item.getAnnotations()));
		assertTrue(UnitTestHelper.isArraysEqual(relationships, item.getRelationships()));
		assertEquals(LICENSE1, item.getLicenseConcluded());
		assertTrue(UnitTestHelper.isArraysEqual(LICENSES, item.getLicenseInfoFromFiles()));
		assertEquals(COPYRIGHT_TEXT1, item.getCopyrightText());
		assertEquals(LICENSE_COMMENT1, item.getLicenseComments());
		Resource r = item.createResource(modelContainer);
		item.setCopyrightText(COPYRIGHT_TEXT2);
		assertEquals(COPYRIGHT_TEXT2, item.getCopyrightText());
		SpdxItem item2= new SpdxItem(modelContainer, r.asNode());
		assertEquals(COPYRIGHT_TEXT2, item2.getCopyrightText());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxItem#setLicenseComments(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testSetLicenseComment() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		SpdxItem item = new SpdxItem(ELEMENT_NAME1, ELEMENT_COMMENT1,
				annotations, relationships, LICENSE1, LICENSES,
				COPYRIGHT_TEXT1, LICENSE_COMMENT1);
		assertEquals(ELEMENT_NAME1, item.getName());
		assertEquals(ELEMENT_COMMENT1, item.getComment());
		assertTrue(UnitTestHelper.isArraysEqual(annotations, item.getAnnotations()));
		assertTrue(UnitTestHelper.isArraysEqual(relationships, item.getRelationships()));
		assertEquals(LICENSE1, item.getLicenseConcluded());
		assertTrue(UnitTestHelper.isArraysEqual(LICENSES, item.getLicenseInfoFromFiles()));
		assertEquals(COPYRIGHT_TEXT1, item.getCopyrightText());
		assertEquals(LICENSE_COMMENT1, item.getLicenseComments());
		Resource r = item.createResource(modelContainer);
		item.setLicenseComments(LICENSE_COMMENT2);
		assertEquals(LICENSE_COMMENT2, item.getLicenseComments());
		SpdxItem item2= new SpdxItem(modelContainer, r.asNode());
		assertEquals(LICENSE_COMMENT2, item2.getLicenseComments());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxItem#clone()}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		SpdxItem item = new SpdxItem(ELEMENT_NAME1, ELEMENT_COMMENT1,
				annotations, relationships, LICENSE1, LICENSES,
				COPYRIGHT_TEXT1, LICENSE_COMMENT1);
		assertEquals(ELEMENT_NAME1, item.getName());
		assertEquals(ELEMENT_COMMENT1, item.getComment());
		assertTrue(UnitTestHelper.isArraysEqual(annotations, item.getAnnotations()));
		assertTrue(UnitTestHelper.isArraysEqual(relationships, item.getRelationships()));
		assertEquals(LICENSE1, item.getLicenseConcluded());
		assertTrue(UnitTestHelper.isArraysEqual(LICENSES, item.getLicenseInfoFromFiles()));
		assertEquals(COPYRIGHT_TEXT1, item.getCopyrightText());
		assertEquals(LICENSE_COMMENT1, item.getLicenseComments());
		item.createResource(modelContainer);
		SpdxItem item2 = item.clone();
		assertEquals(item.getName(), item2.getName());
		assertEquals(item.getComment(), item2.getComment());
		assertTrue(UnitTestHelper.isArraysEquivalent(item.getAnnotations(), item2.getAnnotations()));
		assertTrue(UnitTestHelper.isArraysEquivalent(item.getRelationships(), item2.getRelationships()));
		assertEquals(item.getLicenseConcluded(), item2.getLicenseConcluded());
		assertTrue(UnitTestHelper.isArraysEqual(item.getLicenseInfoFromFiles(), item2.getLicenseInfoFromFiles()));
		assertEquals(item.getCopyrightText(), item2.getCopyrightText());
		assertEquals(item.getLicenseComments(), item2.getLicenseComments());
		assertFalse(item.resource == item2.resource);
	}

}
