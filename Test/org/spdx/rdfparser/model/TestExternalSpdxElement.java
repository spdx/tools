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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Gary
 *
 */
public class TestExternalSpdxElement {

	static final String DOCID1 = SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM + "DOCID1";
	static final String SPDXID1 = SpdxRdfConstants.SPDX_ELEMENT_REF_PRENUM + "SPDXID1";
	static final String DOCURI1 = "http://doc/uri/one";
	static final String ID1 = DOCID1 + ":" + SPDXID1;

	static final String DOCID2 = SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM + "DOCID1";
	static final String SPDXID2 = SpdxRdfConstants.SPDX_ELEMENT_REF_PRENUM + "SPDXID2";
	static final String DOCURI2 = "http://doc/uri/two";
	static final String ID2 = DOCID2 + ":" + SPDXID2;

	static final String DOCID3 = SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM + "DOCID3";
	static final String SPDXID3 = SpdxRdfConstants.SPDX_ELEMENT_REF_PRENUM + "SPDXID3";
	static final String DOCURI3 = "http://doc/uri/three";
	static final String ID3 = DOCID3 + ":" + SPDXID3;

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
	 * Test method for {@link org.spdx.rdfparser.model.ExternalSpdxElement#getUri(org.spdx.rdfparser.IModelContainer)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testGetUri() throws InvalidSPDXAnalysisException {
		ExternalSpdxElement externalElement = new ExternalSpdxElement(ID1);
		Model model = ModelFactory.createDefaultModel();
		ModelContainerForTest container = new ModelContainerForTest(model, "http://container.namespace");
		container.addExternalDocReference(DOCID1, DOCURI1);
		String expected = DOCURI1 + "#" + SPDXID1;
		assertEquals(expected, externalElement.getUri(container));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalSpdxElement#equivalent(org.spdx.rdfparser.model.RdfModelObject)}.
	 */
	@Test
	public void testEquivalentRdfModelObject() throws InvalidSPDXAnalysisException {
		ExternalSpdxElement externalElement = new ExternalSpdxElement(ID1);
		assertTrue(externalElement.equivalent(externalElement));
		ExternalSpdxElement externalElement2 = new ExternalSpdxElement(ID1);
		assertTrue(externalElement.equivalent(externalElement2));
		ExternalSpdxElement externalElement3 = new ExternalSpdxElement(ID2);
		assertFalse(externalElement.equivalent(externalElement3));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalSpdxElement#verify()}.
	 */
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException {
		ExternalSpdxElement externalElement = new ExternalSpdxElement(ID1);
		assertEquals(0, externalElement.verify().size());
		externalElement.setId(null);
		assertEquals(1, externalElement.verify().size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalSpdxElement#setId(java.lang.String)}.
	 */
	@Test
	public void testSetId() throws InvalidSPDXAnalysisException {
		ExternalSpdxElement externalElement = new ExternalSpdxElement(ID1);
		assertEquals(ID1, externalElement.getId());
		externalElement.setId(ID2);
		assertEquals(ID2, externalElement.getId());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalSpdxElement#getExternalDocumentId()}.
	 */
	@Test
	public void testGetExternalDocumentId() throws InvalidSPDXAnalysisException {
		ExternalSpdxElement externalElement = new ExternalSpdxElement(ID1);
		assertEquals(DOCID1, externalElement.getExternalDocumentId());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalSpdxElement#getExternalElementId()}.
	 */
	@Test
	public void testGetExternalElementId() throws InvalidSPDXAnalysisException {
		ExternalSpdxElement externalElement = new ExternalSpdxElement(ID1);
		assertEquals(SPDXID1, externalElement.getExternalElementId());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalSpdxElement#clone()}.
	 */
	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		ExternalSpdxElement externalElement = new ExternalSpdxElement(ID1);
		ExternalSpdxElement cloned = externalElement.clone();
		assertEquals(ID1, cloned.getId());
		assertTrue(cloned.equivalent(externalElement));
	}

}
