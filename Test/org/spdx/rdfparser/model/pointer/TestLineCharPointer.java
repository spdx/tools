/**
 * Copyright (c) 2016 Source Auditor Inc.
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
package org.spdx.rdfparser.model.pointer;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.ModelContainerForTest;
import org.spdx.rdfparser.model.SpdxElement;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Gary
 *
 */
public class TestLineCharPointer {
	
	String REFERENCED_ELEMENT_NAME1 = "Element1";
	String REFERENCED_ELEMENT_NAME2 = "Element2";
	SpdxElement REFERENCED1;
	SpdxElement REFERENCED2;
	Resource REFERENCED_RESOURCE1;
	Resource REFERENCED_RESOURCE2;

	Model model;
	IModelContainer modelContainer;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.model = ModelFactory.createDefaultModel();
		modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		REFERENCED1 = new SpdxElement(REFERENCED_ELEMENT_NAME1, "", null, null);
		REFERENCED_RESOURCE1 = REFERENCED1.createResource(modelContainer);
		REFERENCED2 = new SpdxElement(REFERENCED_ELEMENT_NAME2, "", null, null);
		REFERENCED_RESOURCE2 = REFERENCED2.createResource(modelContainer);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.pointer.LineCharPointer#getType(com.hp.hpl.jena.rdf.model.Model)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetType() throws InvalidSPDXAnalysisException {
		LineCharPointer lcp = new LineCharPointer(REFERENCED1, 15);
		lcp.createResource(modelContainer);
		assertEquals(SpdxRdfConstants.RDF_POINTER_NAMESPACE + SpdxRdfConstants.CLASS_POINTER_LINE_CHAR_POINTER,
				lcp.getType(model).toString());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.pointer.LineCharPointer#verify()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException {
		LineCharPointer lcp = new LineCharPointer(REFERENCED1, 15);
		lcp.createResource(modelContainer);
		List<String> result = lcp.verify();
		assertEquals(0, result.size());
		// Null referenced
		LineCharPointer lcp2 = new LineCharPointer(null, 15);
		lcp2.createResource(modelContainer);
		result = lcp2.verify();
		assertEquals(1, result.size());
		LineCharPointer lcp3 = new LineCharPointer(REFERENCED1, -1);
		lcp3.createResource(modelContainer);
		result = lcp3.verify();
		assertEquals(1, result.size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.pointer.LineCharPointer#equivalent(org.spdx.rdfparser.model.IRdfModel)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testEquivalent() throws InvalidSPDXAnalysisException {
		LineCharPointer lcp = new LineCharPointer(REFERENCED1, 15);
		LineCharPointer lcp2 = new LineCharPointer(REFERENCED1, 15);
		assertTrue(lcp.equivalent(lcp2));
		lcp.createResource(modelContainer);
		assertTrue(lcp.equivalent(lcp2));
		lcp2.createResource(modelContainer);
		assertTrue(lcp.equivalent(lcp2));
		LineCharPointer lcp3 = new LineCharPointer(REFERENCED1, 55);
		lcp3.createResource(modelContainer);
		assertFalse(lcp.equivalent(lcp3));
		assertFalse(lcp3.equivalent(lcp));
		LineCharPointer lcp4 = new LineCharPointer(REFERENCED2, 15);
		lcp4.createResource(modelContainer);
		assertFalse(lcp.equivalent(lcp4));
		assertFalse(lcp4.equivalent(lcp));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.pointer.LineCharPointer#setLineNumber(java.lang.Integer)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetLineNumber() throws InvalidSPDXAnalysisException {
		LineCharPointer bop = new LineCharPointer(REFERENCED1, 15);
		assertEquals(new Integer(15), bop.getLineNumber());
		Resource r = bop.createResource(modelContainer);
		assertEquals(new Integer(15), bop.getLineNumber());
		LineCharPointer bop2 = new LineCharPointer(modelContainer, r.asNode());
		assertEquals(new Integer(15), bop2.getLineNumber());
		bop.setLineNumber(55);
		assertEquals(new Integer(55), bop.getLineNumber());
		assertEquals(new Integer(55), bop2.getLineNumber());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.pointer.SinglePointer#setReference(org.spdx.rdfparser.model.SpdxElement)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetReference() throws InvalidSPDXAnalysisException {
		LineCharPointer lcp = new LineCharPointer(REFERENCED1, 15);
		assertEquals(REFERENCED1.getName(), lcp.getReference().getName());
		Resource r = lcp.createResource(modelContainer);
		assertEquals(REFERENCED1.getName(), lcp.getReference().getName());
		LineCharPointer lcp2 = new LineCharPointer(modelContainer, r.asNode());
		assertEquals(REFERENCED1.getName(), lcp2.getReference().getName());
		lcp.setReference(REFERENCED2);
		assertEquals(REFERENCED2.getName(), lcp.getReference().getName());
		assertEquals(REFERENCED2.getName(), lcp2.getReference().getName());
	}


	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		LineCharPointer lcp = new LineCharPointer(REFERENCED1, 15);
		lcp.createResource(modelContainer);
		LineCharPointer lcp2 = lcp.clone();
		lcp2.createResource(modelContainer);
		assertTrue(lcp2.equivalent(lcp2));
		lcp2.getReference().setName("New Name");
		assertFalse(lcp2.equivalent(lcp));
	}
	
	@Test
	public void testCompareTo() {
		LineCharPointer lcp = new LineCharPointer(REFERENCED1, 15);
		LineCharPointer lcp2 = new LineCharPointer(REFERENCED1, 15);
		LineCharPointer lcp3 = new LineCharPointer(REFERENCED2, 15);
		LineCharPointer lcp4 = new LineCharPointer(REFERENCED1, 18);
		assertEquals(0, lcp.compareTo(lcp2));
		assertTrue(lcp.compareTo(lcp3) < 0);
		assertTrue(lcp4.compareTo(lcp) > 0);
	}
}
