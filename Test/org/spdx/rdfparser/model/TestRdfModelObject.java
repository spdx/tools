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

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @author Gary
 *
 */
public class TestRdfModelObject {
	static final String TEST_VERIFY = "test verify";
	static final String TEST_TYPE = "TestTypeClass";
	static final String TEST_NAMESPACE = "http://TestNamespace/rdf#";
	static final String TEST_PROPNAME1 = "property1";
	static final String TEST_PROPNAME2 = "property2";
	static final String TEST_PROPVALUE1 = "value1";
	static final String TEST_PROPVALUE2 = "value2";
	static final String TEST_PROPVALUE3 = "value3";
	static final String TEST_PROPVALUE4 = "value4";
	class EmptyRdfModelObject extends RdfModelObject {

		public EmptyRdfModelObject(Model model, Node node) throws InvalidSPDXAnalysisException {
			super(model, node);
		}
		/**
		 * 
		 */
		public EmptyRdfModelObject() {
			super();
		}
		/* (non-Javadoc)
		 * @see org.spdx.rdfparser.model.IRdfModel#verify()
		 */
		@Override
		public ArrayList<String> verify() {
			ArrayList<String> retval = new ArrayList<String>();
			retval.add(TEST_VERIFY);
			return retval;
		}

		/* (non-Javadoc)
		 * @see org.spdx.rdfparser.model.RdfModelObject#getType()
		 */
		@Override
		Resource getType(Model model) {
			return model.createResource(TEST_NAMESPACE + TEST_TYPE);
		}

		/* (non-Javadoc)
		 * @see org.spdx.rdfparser.model.RdfModelObject#populateModel()
		 */
		@Override
		void populateModel() {
			// Just populate one of the properties
			this.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, TEST_PROPVALUE1);
		}
		
		public String findSinglePropertyValue(String namespace, String propertyName) {
			return super.findSinglePropertyValue(namespace, propertyName);
		}
		
		public String[] findMultiplePropertyValues(String namespace,String propertyName) {
			return super.findMultiplePropertyValues(namespace, propertyName);
		}
		
		public void setPropertyValue(String nameSpace, String propertyName,
				String[] values) {
			super.setPropertyValue(nameSpace, propertyName, values);
		}
		
		public void setPropertyValue(String nameSpace, String propertyName,
				String value) {
			super.setPropertyValue(nameSpace, propertyName, value);
		}
		
	}

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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.RdfModelObject#RdfModelObject(com.hp.hpl.jena.rdf.model.Model, com.hp.hpl.jena.graph.Node)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testRdfModelObjectModelNode() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		Property p = model.createProperty(TEST_NAMESPACE, TEST_PROPNAME2);
		Resource r = model.createResource();
		r.addProperty(p, TEST_PROPVALUE1);
		EmptyRdfModelObject empty = new EmptyRdfModelObject(model, r.asNode());
		String result = empty.findSinglePropertyValue(TEST_NAMESPACE, TEST_PROPNAME2);
		assertEquals(TEST_PROPVALUE1, result);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.RdfModelObject#createResource(com.hp.hpl.jena.rdf.model.Model, java.lang.String)}.
	 */
	@Test
	public void testCreateResource() {
		Model model = ModelFactory.createDefaultModel();
		EmptyRdfModelObject empty = new EmptyRdfModelObject();
		// Anon.
		String URI = "http://a/uri#r";
		Resource r = empty.createResource(model, URI);
		assertTrue(r.isURIResource());
		Node p = model.getProperty(TEST_NAMESPACE, TEST_PROPNAME1).asNode();
		Triple m = Triple.createMatch(r.asNode(), p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		assertTrue(tripleIter.hasNext());
		Triple t = tripleIter.next();
		assertEquals(TEST_PROPVALUE1,t.getObject().toString(false));
		assertFalse(tripleIter.hasNext());
		// Anon
		Resource anon = empty.createResource(model, null);
		assertFalse(anon.isURIResource());
		p = model.getProperty(TEST_NAMESPACE, TEST_PROPNAME1).asNode();
		m = Triple.createMatch(anon.asNode(), p, null);
		tripleIter = model.getGraph().find(m);	
		assertTrue(tripleIter.hasNext());
		t = tripleIter.next();
		assertEquals(TEST_PROPVALUE1,t.getObject().toString(false));
		assertFalse(tripleIter.hasNext());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.RdfModelObject#findSinglePropertyValue(java.lang.String, java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetFindSinglePropertyValue() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(model, r.asNode());
		String result = empty.findSinglePropertyValue(TEST_NAMESPACE, TEST_PROPNAME1);
		assertTrue(result == null);
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, TEST_PROPVALUE1);
		result = empty.findSinglePropertyValue(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(result, TEST_PROPVALUE1);
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, TEST_PROPVALUE2);
		result = empty.findSinglePropertyValue(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(result, TEST_PROPVALUE2);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.RdfModelObject#findMultiplePropertyValues(java.lang.String, java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetFindMultipePropertyValues() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		Resource r = model.createResource();
		EmptyRdfModelObject empty = new EmptyRdfModelObject(model, r.asNode());
		String[] result = empty.findMultiplePropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(0, result.length);
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, new String[] {TEST_PROPVALUE1, TEST_PROPVALUE2});
		result = empty.findMultiplePropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(2, result.length);
		if (result[0].equals(TEST_PROPVALUE1)) {
			assertEquals(TEST_PROPVALUE2, result[1]);
		} else if (result[0].equals(TEST_PROPVALUE2)) {
			assertEquals(TEST_PROPVALUE1, result[1]);
		} else {
			fail("Wrong values");
		}
		empty.setPropertyValue(TEST_NAMESPACE, TEST_PROPNAME1, new String[] {TEST_PROPVALUE3, TEST_PROPVALUE4});
		result = empty.findMultiplePropertyValues(TEST_NAMESPACE, TEST_PROPNAME1);
		assertEquals(2, result.length);
		if (result[0].equals(TEST_PROPVALUE3)) {
			assertEquals(TEST_PROPVALUE4, result[1]);
		} else if (result[0].equals(TEST_PROPVALUE4)) {
			assertEquals(TEST_PROPVALUE3, result[1]);
		} else {
			fail("Wrong values");
		}

	}

}
