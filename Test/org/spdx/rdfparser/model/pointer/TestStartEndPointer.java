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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

/**
 * @author Gary O'Neall
 *
 */
public class TestStartEndPointer {

	String REFERENCED_ELEMENT_NAME1 = "Element1";
	String REFERENCED_ELEMENT_NAME2 = "Element2";
	SpdxElement REFERENCED1;
	SpdxElement REFERENCED2;
	Resource REFERENCED_RESOURCE1;
	Resource REFERENCED_RESOURCE2;
	Integer OFFSET1 = new Integer(342);
	ByteOffsetPointer BOP_POINTER1;
	Integer LINE1 = new Integer(113);
	LineCharPointer LCP_POINTER1;
	Integer OFFSET2 = new Integer(444);
	ByteOffsetPointer BOP_POINTER2;
	Integer LINE2 = new Integer(23422);
	LineCharPointer LCP_POINTER2;

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
		BOP_POINTER1 = new ByteOffsetPointer(REFERENCED1, OFFSET1);
		LCP_POINTER1 = new LineCharPointer(REFERENCED1, LINE1);
		BOP_POINTER2 = new ByteOffsetPointer(REFERENCED1, OFFSET2);
		LCP_POINTER2 = new LineCharPointer(REFERENCED2, LINE2);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.pointer.StartEndPointer#getPropertiesFromModel()}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testGetPropertiesFromModel() throws InvalidSPDXAnalysisException {
		StartEndPointer sop = new StartEndPointer(BOP_POINTER1, LCP_POINTER1);
		Resource r = sop.createResource(modelContainer);
		StartEndPointer sop2 = new StartEndPointer(modelContainer, r.asNode());
		assertTrue(BOP_POINTER1.equivalent(sop2.getStartPointer()));
		assertTrue(LCP_POINTER1.equivalent(sop2.getEndPointer()));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.pointer.StartEndPointer#getType(org.apache.jena.rdf.model.Model)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testGetType() throws InvalidSPDXAnalysisException {
		StartEndPointer sop = new StartEndPointer(BOP_POINTER1, LCP_POINTER1);
		sop.createResource(modelContainer);
		assertEquals(SpdxRdfConstants.RDF_POINTER_NAMESPACE + SpdxRdfConstants.CLASS_POINTER_START_END_POINTER,
				sop.getType(model).toString());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.pointer.StartEndPointer#verify()}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException {
		StartEndPointer sop = new StartEndPointer(BOP_POINTER1, BOP_POINTER2);
		sop.createResource(modelContainer);
		List<String> result = sop.verify();
		assertEquals(0, result.size());
		StartEndPointer sop2 = new StartEndPointer(null, LCP_POINTER1);
		sop.createResource(modelContainer);
		result = sop2.verify();
		assertEquals(1, result.size());
		StartEndPointer sop3 = new StartEndPointer(BOP_POINTER1, null);
		sop.createResource(modelContainer);
		result = sop3.verify();
		assertEquals(1, result.size());
		ByteOffsetPointer invalidByteOffset = new ByteOffsetPointer(REFERENCED1, -15);
		StartEndPointer sop4 = new StartEndPointer(BOP_POINTER1, invalidByteOffset);
		result = sop4.verify();
		assertEquals(2, result.size());
		// different types
		StartEndPointer sop5 = new StartEndPointer(BOP_POINTER1, LCP_POINTER2);
		sop.createResource(modelContainer);
		result = sop5.verify();
		assertEquals(1, result.size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.pointer.StartEndPointer#setStartPointer(org.spdx.rdfparser.model.pointer.SinglePointer)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testSetStartPointer() throws InvalidSPDXAnalysisException {
		StartEndPointer sop = new StartEndPointer(BOP_POINTER1, LCP_POINTER1);
		SinglePointer result = sop.getStartPointer();
		assertTrue(BOP_POINTER1.equivalent(result));
		Resource r = sop.createResource(modelContainer);
		result = sop.getStartPointer();
		assertTrue(BOP_POINTER1.equivalent(result));
		StartEndPointer sop2 = new StartEndPointer(modelContainer, r.asNode());
		result = sop2.getStartPointer();
		assertTrue(BOP_POINTER1.equivalent(result));
		sop2.setStartPointer(BOP_POINTER2);
		result = sop2.getStartPointer();
		assertTrue(BOP_POINTER2.equivalent(result));
		result = sop.getStartPointer();
		assertTrue(BOP_POINTER2.equivalent(result));
	}

	@Test
	public void testSetEndPointer() throws InvalidSPDXAnalysisException {
		StartEndPointer sop = new StartEndPointer(BOP_POINTER1, LCP_POINTER1);
		SinglePointer result = sop.getEndPointer();
		assertTrue(LCP_POINTER1.equivalent(result));
		Resource r = sop.createResource(modelContainer);
		result = sop.getEndPointer();
		assertTrue(LCP_POINTER1.equivalent(result));
		StartEndPointer sop2 = new StartEndPointer(modelContainer, r.asNode());
		result = sop2.getEndPointer();
		assertTrue(LCP_POINTER1.equivalent(result));
		sop2.setEndPointer(LCP_POINTER2);
		result = sop2.getEndPointer();
		assertTrue(LCP_POINTER2.equivalent(result));
		result = sop.getEndPointer();
		assertTrue(LCP_POINTER2.equivalent(result));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.pointer.StartEndPointer#equivalent(org.spdx.rdfparser.model.IRdfModel)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testEquivalent() throws InvalidSPDXAnalysisException {
		StartEndPointer sop = new StartEndPointer(BOP_POINTER1, LCP_POINTER1);
		sop.createResource(modelContainer);
		StartEndPointer sop2 = new StartEndPointer(BOP_POINTER1, LCP_POINTER1);
		sop2.createResource(modelContainer);
		assertTrue(sop.equivalent(sop2));
		assertTrue(sop2.equivalent(sop));
		StartEndPointer sop3 = new StartEndPointer(BOP_POINTER2, LCP_POINTER1);
		sop3.createResource(modelContainer);
		assertFalse(sop.equivalent(sop3));
		assertFalse(sop3.equivalent(sop));
		StartEndPointer sop4 = new StartEndPointer(BOP_POINTER1, LCP_POINTER2);
		sop4.createResource(modelContainer);
		assertFalse(sop.equivalent(sop4));
		assertFalse(sop4.equivalent(sop));

		ByteOffsetPointer pointerClone = BOP_POINTER1.clone();
		StartEndPointer sop5 = new StartEndPointer(pointerClone, LCP_POINTER1);
		sop5.createResource(modelContainer);
		assertTrue(sop5.equivalent(sop));
		pointerClone.setOffset(1131);
		assertFalse(sop5.equivalent(sop));
	}

	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		StartEndPointer sop = new StartEndPointer(BOP_POINTER1, LCP_POINTER1);
		sop.createResource(modelContainer);
		StartEndPointer sop2 = sop.clone();
		sop2.createResource(modelContainer);
		assertTrue(sop.equivalent(sop2));
		// Test to make sure deep clone
		BOP_POINTER1.setOffset(111);
		assertFalse(sop.equivalent(sop2));
	}

}
