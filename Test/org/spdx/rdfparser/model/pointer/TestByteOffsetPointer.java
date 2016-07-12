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
public class TestByteOffsetPointer {
	
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
	 * Test method for {@link org.spdx.rdfparser.model.pointer.ByteOffsetPointer#getType(com.hp.hpl.jena.rdf.model.Model)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetType() throws InvalidSPDXAnalysisException {
		ByteOffsetPointer bop = new ByteOffsetPointer(REFERENCED1, 15);
		bop.createResource(modelContainer);
		assertEquals(SpdxRdfConstants.RDF_POINTER_NAMESPACE + SpdxRdfConstants.CLASS_POINTER_BYTE_OFFSET_POINTER,
				bop.getType(model).toString());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.pointer.ByteOffsetPointer#verify()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException {
		ByteOffsetPointer bop = new ByteOffsetPointer(REFERENCED1, 15);
		bop.createResource(modelContainer);
		List<String> result = bop.verify();
		assertEquals(0, result.size());
		// Null referenced
		ByteOffsetPointer bop2 = new ByteOffsetPointer(null, 15);
		bop2.createResource(modelContainer);
		result = bop2.verify();
		assertEquals(1, result.size());
		ByteOffsetPointer bop3 = new ByteOffsetPointer(REFERENCED1, -1);
		bop3.createResource(modelContainer);
		result = bop3.verify();
		assertEquals(1, result.size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.pointer.ByteOffsetPointer#equivalent(org.spdx.rdfparser.model.IRdfModel)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testEquivalent() throws InvalidSPDXAnalysisException {
		ByteOffsetPointer bop = new ByteOffsetPointer(REFERENCED1, 15);
		ByteOffsetPointer bop2 = new ByteOffsetPointer(REFERENCED1, 15);
		assertTrue(bop.equivalent(bop2));
		bop.createResource(modelContainer);
		assertTrue(bop.equivalent(bop2));
		bop2.createResource(modelContainer);
		assertTrue(bop.equivalent(bop2));
		ByteOffsetPointer bop3 = new ByteOffsetPointer(REFERENCED1, 55);
		bop3.createResource(modelContainer);
		assertFalse(bop.equivalent(bop3));
		assertFalse(bop3.equivalent(bop));
		ByteOffsetPointer bop4 = new ByteOffsetPointer(REFERENCED2, 15);
		bop4.createResource(modelContainer);
		assertFalse(bop.equivalent(bop4));
		assertFalse(bop4.equivalent(bop));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.pointer.ByteOffsetPointer#setOffset(java.lang.Integer)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetOffset() throws InvalidSPDXAnalysisException {
		ByteOffsetPointer bop = new ByteOffsetPointer(REFERENCED1, 15);
		assertEquals(new Integer(15), bop.getOffset());
		Resource r = bop.createResource(modelContainer);
		assertEquals(new Integer(15), bop.getOffset());
		ByteOffsetPointer bop2 = new ByteOffsetPointer(modelContainer, r.asNode());
		assertEquals(new Integer(15), bop2.getOffset());
		bop.setOffset(55);
		assertEquals(new Integer(55), bop.getOffset());
		assertEquals(new Integer(55), bop2.getOffset());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.pointer.SinglePointer#setReference(org.spdx.rdfparser.model.SpdxElement)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetReference() throws InvalidSPDXAnalysisException {
		ByteOffsetPointer bop = new ByteOffsetPointer(REFERENCED1, 15);
		assertEquals(REFERENCED1.getName(), bop.getReference().getName());
		Resource r = bop.createResource(modelContainer);
		assertEquals(REFERENCED1.getName(), bop.getReference().getName());
		ByteOffsetPointer bop2 = new ByteOffsetPointer(modelContainer, r.asNode());
		assertEquals(REFERENCED1.getName(), bop2.getReference().getName());
		bop.setReference(REFERENCED2);
		assertEquals(REFERENCED2.getName(), bop.getReference().getName());
		assertEquals(REFERENCED2.getName(), bop2.getReference().getName());
	}

	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		ByteOffsetPointer bop = new ByteOffsetPointer(REFERENCED1, 15);
		bop.createResource(modelContainer);
		ByteOffsetPointer bop2 = bop.clone();
		bop2.createResource(modelContainer);
		assertTrue(bop.equivalent(bop2));
		bop2.getReference().setName("New Name");
		assertFalse(bop.equivalent(bop2));
	}
	
	@Test
	public void testCompareTo() {
		ByteOffsetPointer bop = new ByteOffsetPointer(REFERENCED1, 15);
		ByteOffsetPointer bop2 = new ByteOffsetPointer(REFERENCED1, 15);
		ByteOffsetPointer bop3 = new ByteOffsetPointer(REFERENCED2, 15);
		ByteOffsetPointer bop4 = new ByteOffsetPointer(REFERENCED1, 18);
		assertEquals(0, bop.compareTo(bop2));
		assertTrue(bop.compareTo(bop3) < 0);
		assertTrue(bop4.compareTo(bop) > 0);
	}
}
