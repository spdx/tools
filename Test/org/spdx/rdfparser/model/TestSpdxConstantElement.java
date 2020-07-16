/**
 * Copyright (c) 2020 Source Auditor Inc.
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
 */
package org.spdx.rdfparser.model;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.Annotation.AnnotationType;
import org.spdx.rdfparser.model.Relationship.RelationshipType;
import org.spdx.rdfparser.model.SpdxNoneElement;

/**
 * @author gary
 *
 */
public class TestSpdxConstantElement {

	Model model;

	IModelContainer modelContainer;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		model = ModelFactory.createDefaultModel();
		modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxConstantElement#verify()}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException {
		SpdxNoneElement el = new SpdxNoneElement();
		assertEquals(0, el.verify().size());
		Resource res = el.createResource(modelContainer);
		SpdxNoneElement el2 = new SpdxNoneElement(modelContainer, res.asNode());
		assertEquals(0, el2.verify().size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxConstantElement#setAnnotations(org.spdx.rdfparser.model.Annotation[])}.
	 */
	@Test
	public void testSetAnnotations() throws InvalidSPDXAnalysisException {
		SpdxNoneElement el = new SpdxNoneElement();
		try {
			el.setAnnotations(new Annotation[0]);
			fail("This should have failed");
		} catch(Exception ex) {
			// expected
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxConstantElement#setComment(java.lang.String)}.
	 */
	@Test
	public void testSetComment() throws InvalidSPDXAnalysisException {
		SpdxNoneElement el = new SpdxNoneElement();
		try {
			el.setComment("New comment");
			fail("This should have failed");
		} catch(Exception ex) {
			// expected
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxConstantElement#setName(java.lang.String)}.
	 */
	@Test
	public void testSetName() throws InvalidSPDXAnalysisException {
		SpdxNoneElement el = new SpdxNoneElement();
		try {
			el.setName("NewName");
			fail("This should have failed");
		} catch(Exception ex) {
			// expected
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxConstantElement#setRelationships(org.spdx.rdfparser.model.Relationship[])}.
	 */
	@Test
	public void testSetRelationships() throws InvalidSPDXAnalysisException {
		SpdxNoneElement el = new SpdxNoneElement();
		try {
			el.setRelationships(new Relationship[0]);
			fail("This should have failed");
		} catch(Exception ex) {
			// expected
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxConstantElement#equivalent(org.spdx.rdfparser.model.IRdfModel)}.
	 */
	@Test
	public void testEquivalentIRdfModel() throws InvalidSPDXAnalysisException {
		SpdxNoneElement el = new SpdxNoneElement();
		Resource res = el.createResource(modelContainer);
		SpdxNoneElement el2 = new SpdxNoneElement(modelContainer, res.asNode());
		assertTrue(el2.equivalent(el));
		assertTrue(el2.equivalent(new SpdxNoneElement()));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxConstantElement#addRelationship(org.spdx.rdfparser.model.Relationship)}.
	 */
	@Test
	public void testAddRelationship() throws InvalidSPDXAnalysisException {
		SpdxNoneElement el = new SpdxNoneElement();
		try {
			el.addRelationship(new Relationship(new SpdxNoAssertionElement(), RelationshipType.AMENDS, ""));
			fail("This should have failed");
		} catch(Exception ex) {
			// expected
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxConstantElement#addAnnotation(org.spdx.rdfparser.model.Annotation)}.
	 */
	@Test
	public void testAddAnnotation() throws InvalidSPDXAnalysisException {
		SpdxNoneElement el = new SpdxNoneElement();
		try {
			el.addAnnotation(new Annotation("Person: me", AnnotationType.annotationType_review,
					new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT).format(new Date()), "c"));
			fail("This should have failed");
		} catch(Exception ex) {
			// expected
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElement#getUri(org.spdx.rdfparser.IModelContainer)}.
	 */
	@Test
	public void testGetUri() throws InvalidSPDXAnalysisException {
		SpdxNoneElement el = new SpdxNoneElement();
		assertEquals(SpdxNoneElement.NONE_ELEMENT_URI, el.getUri(modelContainer));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElement#getType(org.apache.jena.rdf.model.Model)}.
	 */
	@Test
	public void testGetType() throws InvalidSPDXAnalysisException {
		SpdxNoneElement el = new SpdxNoneElement();
		Resource expected = model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_NONE_ELEMENT);
		assertEquals(expected, el.getType(model));
		SpdxNoAssertionElement nae = new SpdxNoAssertionElement();
		expected = model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_NOASSERTION_ELEMENT);
		assertEquals(expected, nae.getType(model));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElement#getAnnotations()}.
	 */
	@Test
	public void testGetAnnotations() throws InvalidSPDXAnalysisException {
		SpdxNoneElement el = new SpdxNoneElement();
		assertEquals(0, el.getAnnotations().length);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElement#getComment()}.
	 */
	@Test
	public void testGetComment() throws InvalidSPDXAnalysisException {
		SpdxNoneElement el = new SpdxNoneElement();
		assertTrue(el.getComment().length() > 0);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElement#getName()}.
	 */
	@Test
	public void testGetName() throws InvalidSPDXAnalysisException {
		SpdxNoneElement el = new SpdxNoneElement();
		assertTrue(el.getName().length() > 0);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElement#getRelationships()}.
	 */
	@Test
	public void testGetRelationships() throws InvalidSPDXAnalysisException {
		SpdxNoneElement el = new SpdxNoneElement();
		assertEquals(0, el.getRelationships().length);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElement#getId()}.
	 */
	@Test
	public void testGetId() throws InvalidSPDXAnalysisException {
		SpdxNoneElement el = new SpdxNoneElement();
		assertEquals(SpdxNoneElement.NONE_ELEMENT_ID, el.getId());
		SpdxNoAssertionElement nae = new SpdxNoAssertionElement();
		assertEquals(SpdxNoAssertionElement.NOASSERTION_ELEMENT_ID, nae.getId());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElement#setId(java.lang.String)}.
	 */
	@Test
	public void testSetId() throws InvalidSPDXAnalysisException {
		SpdxNoneElement el = new SpdxNoneElement();
		try {
			el.setId("newId");
			fail("This should have failed");
		} catch(Exception ex) {
			// expected
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxElement#clone()}.
	 */
	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		SpdxNoneElement el = new SpdxNoneElement();
		SpdxNoneElement el2 = el.clone();
		assertEquals(el, el2);
		SpdxNoAssertionElement nae = new SpdxNoAssertionElement();
		SpdxNoAssertionElement nae2 = nae.clone();
		assertEquals(nae, nae2);
	}

}
