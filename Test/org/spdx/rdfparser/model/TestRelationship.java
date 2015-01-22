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
public class TestRelationship {
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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Relationship#Relationship(org.spdx.rdfparser.model.SpdxElement, org.spdx.rdfparser.model.Relationship.RelationshipType, java.lang.String)}.
	 */
	@Test
	public void testRelationshipSpdxElementRelationshipTypeString() {
		RelationshipType relationshipType1  = RelationshipType.relationshipType_descendantOf;
		String comment1 = "Comment1";
		Relationship relationship = new Relationship(RELATED_ELEMENT1, relationshipType1, comment1);
		assertEquals(RELATED_ELEMENT1, relationship.getRelatedSpdxElement());
		assertEquals(relationshipType1, relationship.getRelationshipType());
		assertEquals(comment1, relationship.getComment());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Relationship#Relationship(org.spdx.rdfparser.IModelContainer, com.hp.hpl.jena.graph.Node)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testRelationshipIModelContainerNode() throws InvalidSPDXAnalysisException {
		RelationshipType relationshipType1  = RelationshipType.relationshipType_descendantOf;
		String comment1 = "Comment1";
		Relationship relationship = new Relationship(RELATED_ELEMENT1, relationshipType1, comment1);
		assertEquals(RELATED_ELEMENT1, relationship.getRelatedSpdxElement());
		assertEquals(relationshipType1, relationship.getRelationshipType());
		assertEquals(comment1, relationship.getComment());
		Resource r = relationship.createResource(modelContainer);
		Relationship result = new Relationship(modelContainer, r.asNode());
		assertEquals(RELATED_ELEMENT1, result.getRelatedSpdxElement());
		assertEquals(relationshipType1, result.getRelationshipType());
		assertEquals(comment1, result.getComment());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Relationship#verify()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException {
		RelationshipType relationshipType1  = RelationshipType.relationshipType_descendantOf;
		String comment1 = "Comment1";
		Relationship relationship = new Relationship(RELATED_ELEMENT1, relationshipType1, comment1);
		assertEquals(0, relationship.verify().size());
		relationship.setRelatedSpdxElement(null);
		assertEquals(1, relationship.verify().size());
		relationship.setRelationshipType(null);
		assertEquals(2, relationship.verify().size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Relationship#setRelationshipType(org.spdx.rdfparser.model.Relationship.RelationshipType)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetRelationshipType() throws InvalidSPDXAnalysisException {
		RelationshipType relationshipType1  = RelationshipType.relationshipType_descendantOf;
		String comment1 = "Comment1";
		Relationship relationship = new Relationship(RELATED_ELEMENT1, relationshipType1, comment1);
		assertEquals(RELATED_ELEMENT1, relationship.getRelatedSpdxElement());
		assertEquals(relationshipType1, relationship.getRelationshipType());
		assertEquals(comment1, relationship.getComment());
		relationship.createResource(modelContainer);
		RelationshipType relationshipType2  = RelationshipType.relationshipType_copyOf;
		relationship.setRelationshipType(relationshipType2);
		assertEquals(relationshipType2, relationship.getRelationshipType());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Relationship#setComment(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetComment() throws InvalidSPDXAnalysisException {
		RelationshipType relationshipType1  = RelationshipType.relationshipType_descendantOf;
		String comment1 = "Comment1";
		Relationship relationship = new Relationship(RELATED_ELEMENT1, relationshipType1, comment1);
		assertEquals(RELATED_ELEMENT1, relationship.getRelatedSpdxElement());
		assertEquals(relationshipType1, relationship.getRelationshipType());
		assertEquals(comment1, relationship.getComment());
		relationship.createResource(modelContainer);
		String comment2 = "Comment Number 2";
		relationship.setComment(comment2);
		assertEquals(comment2, relationship.getComment());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.Relationship#setRelatedSpdxElement(org.spdx.rdfparser.model.SpdxElement)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetRelatedSpdxElement() throws InvalidSPDXAnalysisException {
		RelationshipType relationshipType1  = RelationshipType.relationshipType_descendantOf;
		String comment1 = "Comment1";
		Relationship relationship = new Relationship(RELATED_ELEMENT1, relationshipType1, comment1);
		assertEquals(RELATED_ELEMENT1, relationship.getRelatedSpdxElement());
		assertEquals(relationshipType1, relationship.getRelationshipType());
		assertEquals(comment1, relationship.getComment());
		relationship.createResource(modelContainer);
		relationship.setRelatedSpdxElement(RELATED_ELEMENT2);
		assertEquals(RELATED_ELEMENT2, relationship.getRelatedSpdxElement());
	}
	
	@Test
	public void testEquivalent() throws InvalidSPDXAnalysisException {
		RelationshipType relationshipType1  = RelationshipType.relationshipType_descendantOf;
		String comment1 = "Comment1";
		Relationship relationship = new Relationship(RELATED_ELEMENT1, relationshipType1, comment1);
		assertEquals(RELATED_ELEMENT1, relationship.getRelatedSpdxElement());
		assertEquals(relationshipType1, relationship.getRelationshipType());
		assertEquals(comment1, relationship.getComment());
		relationship.createResource(modelContainer);
		assertTrue(relationship.equivalent(relationship));
		Relationship relationship2 = new Relationship(RELATED_ELEMENT1, relationshipType1, comment1);
		assertTrue(relationship.equivalent(relationship2));
		relationship2.createResource(modelContainer);
		assertTrue(relationship.equivalent(relationship2));
		// related SPDX element
		relationship2.setRelatedSpdxElement(RELATED_ELEMENT2);
		assertFalse(relationship.equivalent(relationship2));
		relationship2.setRelatedSpdxElement(RELATED_ELEMENT1);
		assertTrue(relationship2.equivalent(relationship));
		// relationship type
		relationship2.setRelationshipType(RelationshipType.relationshipType_dynamicLink);
		assertFalse(relationship.equivalent(relationship2));
		relationship2.setRelationshipType(relationshipType1);
		assertTrue(relationship2.equivalent(relationship));
		// comment
		relationship2.setComment("yet a different comment");
		assertFalse(relationship.equivalent(relationship2));
		relationship2.setComment(comment1);
		assertTrue(relationship2.equivalent(relationship));
	}
	
	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		RelationshipType relationshipType1  = RelationshipType.relationshipType_descendantOf;
		String comment1 = "Comment1";
		Relationship relationship = new Relationship(RELATED_ELEMENT1, relationshipType1, comment1);
		relationship.createResource(modelContainer);
		Relationship relationship2 = relationship.clone();
		assertTrue(relationship.getRelatedSpdxElement().equivalent(relationship2.getRelatedSpdxElement()));
		assertEquals(relationship.getRelationshipType(), relationship2.getRelationshipType());
		assertEquals(relationship.getComment(), relationship2.getComment());
		assertFalse(relationship.node == relationship2.node);
	}

}
