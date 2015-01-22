/**
 * Copyright (c) 2012 Source Auditor Inc.
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
package org.spdx.rdfparser.license;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Source Auditor
 *
 */
public class TestExtractedLicenseInfo {
	static final String TEST_RDF_FILE_PATH = "TestFiles"+File.separator+"SPDXRdfExample.rdf";
	static final String ID1 = SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM + "1";
	static final String TEXT1 = "Text1";
	static final String TEXT2 = "Text2";
	static final String COMMENT1 = "Comment1";
	static final String COMMENT2 = "Comment2";
	static final String LICENSENAME1 = "license1";
	static final String LICENSENAME2 = "license2";
	static final String[] SOURCEURLS1 = new String[] {"url1", "url2"};
	static final String[] SOURCEURLS2 = new String[] {"url3", "url4", "url5"};
	Model model;
	IModelContainer modelContainer = new IModelContainer() {

		@Override
		public Model getModel() {
			return model;
		}

		@Override
		public String getDocumentNamespace() {
			return "http://testNameSPace#";
		}

		@Override
		public String getNextSpdxElementRef() {
			return null;
		}
		
	};
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		model = ModelFactory.createDefaultModel();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.ExtractedLicenseInfo#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		ExtractedLicenseInfo lic1 = new ExtractedLicenseInfo(ID1, TEXT1);
		ExtractedLicenseInfo lic2 = new ExtractedLicenseInfo(ID1, TEXT2);
		if (!lic1.equals(lic2)) {
			fail("Should equal when ID's equal");
		}
		if (lic1.hashCode() != lic2.hashCode()) {
			fail("Hashcodes should equal");
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.ExtractedLicenseInfo#SPDXNonStandardLicense(com.hp.hpl.jena.rdf.model.Model, com.hp.hpl.jena.graph.Node)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSPDXNonStandardLicenseModelNode() throws InvalidSPDXAnalysisException {
		ExtractedLicenseInfo lic = new ExtractedLicenseInfo(ID1, TEXT1);
		lic.setComment(COMMENT1);
		Resource licResource = lic.createResource(modelContainer);
		ExtractedLicenseInfo lic2 = new ExtractedLicenseInfo(modelContainer, licResource.asNode());
		assertEquals(ID1, lic2.getLicenseId());
		assertEquals(TEXT1, lic2.getExtractedText());
		assertEquals(COMMENT1, lic2.getComment());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.ExtractedLicenseInfo#SPDXNonStandardLicense(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSPDXNonStandardLicenseStringString() {
		ExtractedLicenseInfo lic = new ExtractedLicenseInfo(ID1, TEXT1);
		assertEquals(ID1, lic.getLicenseId());
		assertEquals(TEXT1, lic.getExtractedText());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.ExtractedLicenseInfo#setExtractedText(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetText() throws InvalidSPDXAnalysisException {
		ExtractedLicenseInfo lic = new ExtractedLicenseInfo(ID1, TEXT1);
		lic.setComment(COMMENT1);
		Resource licResource = lic.createResource(modelContainer);
		ExtractedLicenseInfo lic2 = new ExtractedLicenseInfo(modelContainer, licResource.asNode());
		lic2.setExtractedText(TEXT2);
		assertEquals(ID1, lic2.getLicenseId());
		assertEquals(TEXT2, lic2.getExtractedText());
		assertEquals(COMMENT1, lic2.getComment());
		ExtractedLicenseInfo lic3 = new ExtractedLicenseInfo(modelContainer, licResource.asNode());
		assertEquals(TEXT2, lic3.getExtractedText());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.ExtractedLicenseInfo#setComment(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetComment() throws InvalidSPDXAnalysisException {
		ExtractedLicenseInfo lic = new ExtractedLicenseInfo(ID1, TEXT1);
		lic.setComment(COMMENT1);
		Resource licResource = lic.createResource(modelContainer);
		ExtractedLicenseInfo lic2 = new ExtractedLicenseInfo(modelContainer, licResource.asNode());
		lic2.setComment(COMMENT2);
		assertEquals(ID1, lic2.getLicenseId());
		assertEquals(TEXT1, lic2.getExtractedText());
		assertEquals(COMMENT2, lic2.getComment());
		StringWriter writer = new StringWriter();
		model.write(writer);
		@SuppressWarnings("unused")
		String rdfstring = writer.toString();
		ExtractedLicenseInfo lic3 = new ExtractedLicenseInfo(modelContainer, licResource.asNode());
		assertEquals(COMMENT2, lic3.getComment());	
	}
	@Test
	public void testSetLicenseName() throws InvalidSPDXAnalysisException {
		ExtractedLicenseInfo lic = new ExtractedLicenseInfo(ID1, TEXT1);
		lic.setName(LICENSENAME1);
		Resource licResource = lic.createResource(modelContainer);
		ExtractedLicenseInfo lic2 = new ExtractedLicenseInfo(modelContainer, licResource.asNode());
		lic2.setName(LICENSENAME2);
		assertEquals(LICENSENAME2, lic2.getName());
		ExtractedLicenseInfo lic3 = new ExtractedLicenseInfo(modelContainer, licResource.asNode());
		assertEquals(LICENSENAME2, lic3.getName());
	}
	
	@Test
	public void testSetSourceUrls() throws InvalidSPDXAnalysisException {
		ExtractedLicenseInfo lic = new ExtractedLicenseInfo(ID1, TEXT1);
		lic.setSeeAlso(SOURCEURLS1);
		Resource licResource = lic.createResource(modelContainer);
		ExtractedLicenseInfo lic2 = new ExtractedLicenseInfo(modelContainer, licResource.asNode());
		lic2.setSeeAlso(SOURCEURLS2);
		if (!compareArrayContent(SOURCEURLS2, lic2.getSeeAlso())) {
			fail("Source URLS not the same");
		}
		ExtractedLicenseInfo lic3 = new ExtractedLicenseInfo(modelContainer, licResource.asNode());
		if (!compareArrayContent(SOURCEURLS2, lic3.getSeeAlso())) {
			fail("Source URLS not the same");
		}
	}

	/**
	 * @param strings1
	 * @param strings2
	 * @return true if both arrays contain the same content independent of order
	 */
	private boolean compareArrayContent(String[] strings1,
			String[] strings2) {
		if (strings1.length != strings2.length) {
			return false;
		}
		for (int i = 0; i < strings1.length; i++) {
			boolean found = false;
			for (int j = 0; j < strings2.length; j++) {
				if (strings1[i].equals(strings2[j])) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
	
	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		ExtractedLicenseInfo lic = new ExtractedLicenseInfo(ID1, TEXT1, 
				LICENSENAME1, SOURCEURLS1, COMMENT1);
		@SuppressWarnings("unused")
		Resource licResource = lic.createResource(modelContainer);
		
		ExtractedLicenseInfo lic2 = (ExtractedLicenseInfo)lic.clone();

		assertEquals(ID1, lic2.getLicenseId());
		assertEquals(TEXT1, lic2.getExtractedText());
		assertEquals(COMMENT1, lic2.getComment());
		assertEquals(LICENSENAME1, lic2.getName());
		assertTrue(compareArrayContent(SOURCEURLS1, lic2.getSeeAlso()));
		assertTrue(lic2.getResource() == null);
	}
	
	@Test
	public void testBackwardsCompatibility() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		ExtractedLicenseInfo[] extractedLicenses = doc1.getExtractedLicenseInfos();
		doc1.setExtractedLicenseInfos(extractedLicenses);
		ExtractedLicenseInfo[] result = doc1.getExtractedLicenseInfos();
	}
}
