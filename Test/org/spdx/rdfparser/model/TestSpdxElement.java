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
import org.spdx.rdfparser.model.Annotation.AnnotationType;
import org.spdx.rdfparser.model.Relationship.RelationshipType;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Gary
 *
 */
public class TestSpdxElement {
	
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
	Model model;
	IModelContainer modelContainer = new ModelContainerForTest(model, DOCUMENT_NAMESPACE);

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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElement#getType(com.hp.hpl.jena.rdf.model.Model)}.
	 */
	@Test
	public void testGetType() {
		SpdxElement element1 = new SpdxElement(ELEMENT_NAME1, ELEMENT_COMMENT1, 
				null, null);
		assertTrue(element1.getType(model).getURI().endsWith(SpdxRdfConstants.CLASS_SPDX_ELEMENT));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElement#populateModel()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testPopulateModel() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		SpdxElement element1 = new SpdxElement(ELEMENT_NAME1, ELEMENT_COMMENT1, 
				annotations, relationships);
		Resource r = element1.createResource(modelContainer);
		SpdxElement element2 = new SpdxElement(modelContainer, r.asNode());
		assertEquals(element1.getName(), element2.getName());
		assertEquals(element1.getComment(), element2.getComment());
		assertTrue(UnitTestHelper.isArraysEqual(element1.getAnnotations(), element2.getAnnotations()));
		assertTrue(UnitTestHelper.isArraysEqual(element1.getRelationships(),  element2.getRelationships()));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElement#equals(java.lang.Object)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testEquivalentObject() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		SpdxElement element1 = new SpdxElement(ELEMENT_NAME1, ELEMENT_COMMENT1, 
				annotations, relationships);
		assertTrue(element1.equivalent(element1));
		SpdxElement element2 = new SpdxElement(ELEMENT_NAME1, ELEMENT_COMMENT1, 
				annotations, relationships);
		assertTrue(element1.equivalent(element2));
		element2.createResource(modelContainer);
		assertTrue(element1.equivalent(element2));
		// name
		element2.setName(ELEMENT_NAME2);
		assertFalse(element1.equivalent(element2));
		element2.setName(ELEMENT_NAME1);
		assertTrue(element2.equivalent(element1));
		// comment
		element2.setComment(ELEMENT_COMMENT2);
		assertFalse(element1.equivalent(element2));
		element2.setComment(ELEMENT_COMMENT1);
		assertTrue(element2.equivalent(element1));
		// annotations order
		Annotation[] annotations2 = new Annotation[] {ANNOTATION2, ANNOTATION1};
		element2.setAnnotations(annotations2);
		assertTrue(element2.equivalent(element1));
		// annotation different
		Annotation[] annotations3 = new Annotation[] {ANNOTATION1};
		element2.setAnnotations(annotations3);
		assertFalse(element1.equivalent(element2));
		element2.setAnnotations(annotations);
		assertTrue(element2.equivalent(element1));
		// annotation null
		element2.setAnnotations(null);
		assertFalse(element1.equivalent(element2));
		element2.setAnnotations(annotations);
		assertTrue(element2.equivalent(element1));
		// relationships order
		Relationship[] relationships2 = new Relationship[] {RELATIONSHIP2, RELATIONSHIP1};
		element2.setRelationships(relationships2);
		assertTrue(element2.equivalent(element1));
		// relationships different
		Relationship[] relationships3 = new Relationship[] {RELATIONSHIP2};
		element2.setRelationships(relationships3);
		assertFalse(element1.equivalent(element2));
		element2.setRelationships(relationships);
		assertTrue(element2.equivalent(element1));
		// relationship null	
		element2.setRelationships(null);
		assertFalse(element1.equivalent(element2));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElement#SpdxElement(java.lang.String, java.lang.String, org.spdx.rdfparser.model.Annotation[], org.spdx.rdfparser.model.Relationship[])}.
	 */
	@Test
	public void testSpdxElementStringStringAnnotationArrayRelationshipArray() {
		SpdxElement element1 = new SpdxElement(ELEMENT_NAME1, ELEMENT_COMMENT1, 
				null, null);
		assertEquals(ELEMENT_NAME1, element1.getName());
		assertEquals(ELEMENT_COMMENT1, element1.getComment());
		assertEquals(0, element1.getAnnotations().length);
		assertEquals(0, element1.getRelationships().length);
		Annotation[] annotations = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		SpdxElement element2 = new SpdxElement(ELEMENT_NAME2, ELEMENT_COMMENT2, 
				annotations, relationships);
		assertEquals(ELEMENT_NAME2, element2.getName());
		assertEquals(ELEMENT_COMMENT2, element2.getComment());
		assertTrue(UnitTestHelper.isArraysEqual(annotations, element2.getAnnotations()));
		assertTrue(UnitTestHelper.isArraysEqual(relationships, element2.getRelationships()));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElement#verify()}.
	 */
	@Test
	public void testVerify() {
		SpdxElement element1 = new SpdxElement(ELEMENT_NAME1, null, null, null);
		assertEquals(0, element1.verify().size());
		element1.setName(null);
		assertEquals(1, element1.verify().size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElement#setAnnotations(org.spdx.rdfparser.model.Annotation[])}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetAnnotations() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		SpdxElement element1 = new SpdxElement(ELEMENT_NAME1, ELEMENT_COMMENT1, 
				annotations, relationships);
		assertTrue(UnitTestHelper.isArraysEqual(annotations, element1.getAnnotations()));
		element1.createResource(modelContainer);
		assertTrue(UnitTestHelper.isArraysEqual(annotations, element1.getAnnotations()));
		Annotation[] annotations2 = new Annotation[] {ANNOTATION1};
		element1.setAnnotations(annotations2);
		assertTrue(UnitTestHelper.isArraysEqual(annotations2, element1.getAnnotations()));
		Annotation[] annotations3 = new Annotation[] {ANNOTATION2};
		element1.setAnnotations(annotations3);
		assertTrue(UnitTestHelper.isArraysEqual(annotations3, element1.getAnnotations()));
		Annotation[] annotations4 = new Annotation[] {};
		element1.setAnnotations(annotations4);
		assertTrue(UnitTestHelper.isArraysEqual(annotations4, element1.getAnnotations()));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElement#setComment(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetComment() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		SpdxElement element1 = new SpdxElement(ELEMENT_NAME1, ELEMENT_COMMENT1, 
				annotations, relationships);
		assertEquals(ELEMENT_COMMENT1, element1.getComment());
		element1.createResource(modelContainer);
		assertEquals(ELEMENT_COMMENT1, element1.getComment());
		element1.setComment(ELEMENT_COMMENT2);
		assertEquals(ELEMENT_COMMENT2, element1.getComment());
		element1.setComment(null);
		assertTrue(element1.getComment() == null);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElement#setName(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetName() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		SpdxElement element1 = new SpdxElement(ELEMENT_NAME1, ELEMENT_COMMENT1, 
				annotations, relationships);
		assertEquals(ELEMENT_NAME1, element1.getName());
		element1.createResource(modelContainer);
		assertEquals(ELEMENT_NAME1, element1.getName());
		element1.setName(ELEMENT_NAME2);
		assertEquals(ELEMENT_NAME2, element1.getName());
		element1.setName(null);
		assertTrue(element1.getName() == null);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElement#setRelationships(org.spdx.rdfparser.model.Relationship[])}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetRelationships() throws InvalidSPDXAnalysisException {
		Annotation[] annotations = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Relationship[] relationships = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		SpdxElement element1 = new SpdxElement(ELEMENT_NAME1, ELEMENT_COMMENT1, 
				annotations, relationships);
		assertTrue(UnitTestHelper.isArraysEqual(relationships, element1.getRelationships()));
		element1.createResource(modelContainer);
		assertTrue(UnitTestHelper.isArraysEqual(relationships, element1.getRelationships()));
		Relationship[] relationships2 = new Relationship[] {RELATIONSHIP1};
		element1.setRelationships(relationships2);
		assertTrue(UnitTestHelper.isArraysEqual(relationships2, element1.getRelationships()));
		Relationship[] relationships3 = new Relationship[] {RELATIONSHIP2};
		element1.setRelationships(relationships3);
		assertTrue(UnitTestHelper.isArraysEqual(relationships3, element1.getRelationships()));
		Relationship[] relationships4 = new Relationship[] {};
		element1.setRelationships(relationships4);
		assertTrue(UnitTestHelper.isArraysEqual(relationships4, element1.getRelationships()));

	}

}
