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
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.Checksum.ChecksumAlgorithm;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Gary
 *
 */
public class TestExternalDocumentRef {

	static final String SHA1_VALUE1 = "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12";
	static final String SHA1_VALUE2 = "2222e1c67a2d28fced849ee1bb76e7391b93eb12";
	static final Checksum CHECKSUM1 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, SHA1_VALUE1);
	static final Checksum CHECKSUM2 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, SHA1_VALUE2);
	static final String DOCUMENT_URI1 = "http://spdx.org/docs/uniquevalue1";
	static final String DOCUMENT_URI2 = "http://spdx.org/docs/uniquevalue2";
	static final String DOCUMENT_ID1 = "DocumentRef-1";
	static final String DOCUMENT_ID2 = "DocumentRef-2";

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
	 * Test method for {@link org.spdx.rdfparser.model.ExternalDocumentRef#getType(com.hp.hpl.jena.rdf.model.Model)}.
	 */
	@Test
	public void testGetType() {
		ExternalDocumentRef edf = new ExternalDocumentRef(DOCUMENT_URI1, CHECKSUM1,
				DOCUMENT_ID1);
		Model model = ModelFactory.createDefaultModel();
		Resource result = edf.getType(model);
		assertTrue(result.isURIResource());
		assertEquals(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_EXTERNAL_DOC_REF, result.getURI());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalDocumentRef#populateModel()}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testPopulateModel() throws InvalidSPDXAnalysisException {
		ExternalDocumentRef edf = new ExternalDocumentRef(DOCUMENT_URI1, CHECKSUM1,
				DOCUMENT_ID1);
		Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		assertEquals(DOCUMENT_URI1, edf.getSpdxDocumentNamespace());
		assertTrue(CHECKSUM1.equivalent(edf.getChecksum()));
		Resource r = edf.createResource(modelContainer);
		ExternalDocumentRef copy = new ExternalDocumentRef(modelContainer, r.asNode());
		assertEquals(DOCUMENT_URI1, copy.getSpdxDocumentNamespace());
		assertTrue(CHECKSUM1.equivalent(copy.getChecksum()));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalDocumentRef#equivalent(org.spdx.rdfparser.model.RdfModelObject)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testEquivalent() throws InvalidSPDXAnalysisException {
		ExternalDocumentRef edf = new ExternalDocumentRef(DOCUMENT_URI1, CHECKSUM1,
				DOCUMENT_ID1);
		ExternalDocumentRef edf2 = new ExternalDocumentRef(DOCUMENT_URI1, CHECKSUM1.clone(), DOCUMENT_ID1);
		assertTrue(edf.equivalent(edf2));
		Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		edf.createResource(modelContainer);
		assertTrue(edf.equivalent(edf2));
		// URI
		edf2.setSpdxDocumentNamespace(DOCUMENT_URI2);
		assertFalse(edf.equivalent(edf2));
		edf2.setSpdxDocumentNamespace(DOCUMENT_URI1);
		assertTrue(edf.equivalent(edf2));
		// Checksum
		edf2.setChecksum(CHECKSUM2);
		assertFalse(edf.equivalent(edf2));
		edf2.setChecksum(CHECKSUM1);
		assertTrue(edf.equivalent(edf2));
		// ID
		edf2.setExternalDocumentId(DOCUMENT_ID2);
		assertFalse(edf.equivalent(edf2));
		edf2.setExternalDocumentId(DOCUMENT_ID1);
		assertTrue(edf.equivalent(edf2));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalDocumentRef#verify()}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException {
		ExternalDocumentRef edf = new ExternalDocumentRef(DOCUMENT_URI1, CHECKSUM1,
				DOCUMENT_ID1);
		assertEquals(0, edf.verify().size());
		Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		edf.createResource(modelContainer);
		assertEquals(0, edf.verify().size());
		edf.setChecksum(null);
		assertEquals(1, edf.verify().size());
		edf.setSpdxDocumentNamespace(null);
		assertEquals(2, edf.verify().size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalDocumentRef#setChecksum(org.spdx.rdfparser.model.Checksum)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testSetChecksum() throws InvalidSPDXAnalysisException {
		ExternalDocumentRef edf = new ExternalDocumentRef(DOCUMENT_URI1, CHECKSUM1,
				DOCUMENT_ID1);
		assertTrue(CHECKSUM1.equivalent(edf.getChecksum()));
		Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = edf.createResource(modelContainer);
		assertTrue(CHECKSUM1.equivalent(edf.getChecksum()));
		edf.setChecksum(CHECKSUM2);
		assertTrue(CHECKSUM2.equivalent(edf.getChecksum()));
		ExternalDocumentRef edf2 = new ExternalDocumentRef(modelContainer, r.asNode());
		assertTrue(CHECKSUM2.equivalent(edf2.getChecksum()));
	}

	@Test
	public void testSetExternalDocumentId() throws InvalidSPDXAnalysisException {
		ExternalDocumentRef edf = new ExternalDocumentRef(DOCUMENT_URI1, CHECKSUM1,
				DOCUMENT_ID1);
		assertEquals(DOCUMENT_ID1, edf.getExternalDocumentId());
		Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = edf.createResource(modelContainer);
		assertEquals(DOCUMENT_ID1, edf.getExternalDocumentId());
		edf.setExternalDocumentId(DOCUMENT_ID2);
		assertEquals(DOCUMENT_ID2, edf.getExternalDocumentId());
		ExternalDocumentRef edf2 = new ExternalDocumentRef(modelContainer, r.asNode());
		assertEquals(DOCUMENT_ID2, edf2.getExternalDocumentId());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalDocumentRef#setSpdxDocumentNamespace(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testSetSpdxDocumentUri() throws InvalidSPDXAnalysisException {
		ExternalDocumentRef edf = new ExternalDocumentRef(DOCUMENT_URI1, CHECKSUM1,
				DOCUMENT_ID1);
		assertEquals(DOCUMENT_URI1, edf.getSpdxDocumentNamespace());
		Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = edf.createResource(modelContainer);
		assertEquals(DOCUMENT_URI1, edf.getSpdxDocumentNamespace());
		edf.setSpdxDocumentNamespace(DOCUMENT_URI2);
		assertEquals(DOCUMENT_URI2, edf.getSpdxDocumentNamespace());
		ExternalDocumentRef edf2 = new ExternalDocumentRef(modelContainer, r.asNode());
		assertEquals(DOCUMENT_URI2, edf2.getSpdxDocumentNamespace());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalDocumentRef#setSpdxDocument(org.spdx.rdfparser.model.SpdxDocument)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testSetSpdxDocument() throws InvalidSPDXAnalysisException {
		SpdxDocumentContainer container1 = new SpdxDocumentContainer(DOCUMENT_URI1);
		SpdxDocument doc1 = container1.getSpdxDocument();
		doc1.setName("DocumentName");
		ExternalDocumentRef edf = new ExternalDocumentRef(DOCUMENT_URI2, CHECKSUM1,
				DOCUMENT_ID1);
		Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		Resource r = edf.createResource(modelContainer);
		edf.setSpdxDocument(doc1);
		assertEquals(DOCUMENT_URI1, edf.getSpdxDocumentNamespace());
		assertEquals(doc1.getName(), edf.getSpdxDocument().getName());
		ExternalDocumentRef edf2 = new ExternalDocumentRef(modelContainer, r.asNode());
		assertEquals(DOCUMENT_URI1, edf2.getSpdxDocumentNamespace());
		SpdxDocumentContainer container2 = new SpdxDocumentContainer(DOCUMENT_URI2);
		SpdxDocument doc2 = container2.getSpdxDocument();
		doc2.setName("name2");
		edf2.setSpdxDocument(doc2);
		assertEquals(DOCUMENT_URI2, edf2.getSpdxDocumentNamespace());
		assertEquals(doc2.getName(), edf2.getSpdxDocument().getName());
		assertEquals(DOCUMENT_URI2, edf.getSpdxDocumentNamespace());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.ExternalDocumentRef#clone()}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		ExternalDocumentRef edf = new ExternalDocumentRef(DOCUMENT_URI1, CHECKSUM1,
				DOCUMENT_ID1);
		assertEquals(DOCUMENT_URI1, edf.getSpdxDocumentNamespace());
		Model model = ModelFactory.createDefaultModel();
		IModelContainer modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		edf.createResource(modelContainer);
		ExternalDocumentRef clone = edf.clone();
		assertTrue(edf.equivalent(clone));
		clone.setSpdxDocumentNamespace(DOCUMENT_URI2);
		assertEquals(DOCUMENT_URI2, clone.getSpdxDocumentNamespace());
		assertEquals(DOCUMENT_URI1, edf.getSpdxDocumentNamespace());
	}

}
