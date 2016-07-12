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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.ModelContainerForTest;
import org.spdx.rdfparser.model.SpdxElement;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Gary
 *
 */
public class TestPointerFactory {
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
	 * Test method for {@link org.spdx.rdfparser.model.pointer.PointerFactory#getSinglePointerFromModel(org.spdx.rdfparser.IModelContainer, com.hp.hpl.jena.graph.Node)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetSinglePointerFromModel() throws InvalidSPDXAnalysisException {
		int byteOffset = 14;
		int lineOffset = 1231;
		SinglePointer bop = new ByteOffsetPointer(REFERENCED1, byteOffset);
		Resource bopResource = bop.createResource(modelContainer);
		SinglePointer lcp = new LineCharPointer(REFERENCED2, lineOffset);
		Resource lcpResource = lcp.createResource(modelContainer);

		SinglePointer result = PointerFactory.getSinglePointerFromModel(modelContainer, bopResource.asNode());
		assertTrue(result instanceof ByteOffsetPointer);
		assertTrue(REFERENCED1.equivalent(result.getReference()));
		assertEquals(new Integer(byteOffset), ((ByteOffsetPointer)result).getOffset());
		
		result = PointerFactory.getSinglePointerFromModel(modelContainer, lcpResource.asNode());
		assertTrue(result instanceof LineCharPointer);
		assertTrue(REFERENCED2.equivalent(result.getReference()));
		assertEquals(new Integer(lineOffset), ((LineCharPointer)result).getLineNumber());
	}
	
	/**
	 * Test method for {@link org.spdx.rdfparser.model.pointer.PointerFactory#getSinglePointerFromModel(org.spdx.rdfparser.IModelContainer, com.hp.hpl.jena.graph.Node)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetSinglePointerFromModelByProperties() throws InvalidSPDXAnalysisException {
		int byteOffset = 14;
		int lineOffset = 1231;
		SinglePointer bop = new ByteOffsetPointer(REFERENCED1, byteOffset);
		Resource bopResource = bop.createResource(modelContainer);
		SinglePointer lcp = new LineCharPointer(REFERENCED2, lineOffset);
		Resource lcpResource = lcp.createResource(modelContainer);

		// remove the types
		Property rdfTypeProperty = modelContainer.getModel().getProperty(SpdxRdfConstants.RDF_NAMESPACE, 
				SpdxRdfConstants.RDF_PROP_TYPE);
		bopResource.removeAll(rdfTypeProperty);
		lcpResource.removeAll(rdfTypeProperty);
		
		SinglePointer result = PointerFactory.getSinglePointerFromModel(modelContainer, bopResource.asNode());
		assertTrue(result instanceof ByteOffsetPointer);
		assertTrue(REFERENCED1.equivalent(result.getReference()));
		assertEquals(new Integer(byteOffset), ((ByteOffsetPointer)result).getOffset());
		
		result = PointerFactory.getSinglePointerFromModel(modelContainer, lcpResource.asNode());
		assertTrue(result instanceof LineCharPointer);
		assertTrue(REFERENCED2.equivalent(result.getReference()));
		assertEquals(new Integer(lineOffset), ((LineCharPointer)result).getLineNumber());
	}

}
