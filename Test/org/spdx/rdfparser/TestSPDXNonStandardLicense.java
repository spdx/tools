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
package org.spdx.rdfparser;

import static org.junit.Assert.*;

import java.io.StringWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Source Auditor
 *
 */
public class TestSPDXNonStandardLicense {
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
	 * Test method for {@link org.spdx.rdfparser.SPDXNonStandardLicense#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		SPDXNonStandardLicense lic1 = new SPDXNonStandardLicense(ID1, TEXT1);
		SPDXNonStandardLicense lic2 = new SPDXNonStandardLicense(ID1, TEXT2);
		if (!lic1.equals(lic2)) {
			fail("Should equal when ID's equal");
		}
		if (lic1.hashCode() != lic2.hashCode()) {
			fail("Hashcodes should equal");
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXNonStandardLicense#SPDXNonStandardLicense(com.hp.hpl.jena.rdf.model.Model, com.hp.hpl.jena.graph.Node)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSPDXNonStandardLicenseModelNode() throws InvalidSPDXAnalysisException {
		SPDXNonStandardLicense lic = new SPDXNonStandardLicense(ID1, TEXT1);
		lic.setComment(COMMENT1);
		Resource licResource = lic.createResource(model);
		SPDXNonStandardLicense lic2 = new SPDXNonStandardLicense(model, licResource.asNode());
		assertEquals(ID1, lic2.getId());
		assertEquals(TEXT1, lic2.getText());
		assertEquals(COMMENT1, lic2.getComment());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXNonStandardLicense#SPDXNonStandardLicense(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSPDXNonStandardLicenseStringString() {
		SPDXNonStandardLicense lic = new SPDXNonStandardLicense(ID1, TEXT1);
		assertEquals(ID1, lic.getId());
		assertEquals(TEXT1, lic.getText());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXNonStandardLicense#setText(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetText() throws InvalidSPDXAnalysisException {
		SPDXNonStandardLicense lic = new SPDXNonStandardLicense(ID1, TEXT1);
		lic.setComment(COMMENT1);
		Resource licResource = lic.createResource(model);
		SPDXNonStandardLicense lic2 = new SPDXNonStandardLicense(model, licResource.asNode());
		lic2.setText(TEXT2);
		assertEquals(ID1, lic2.getId());
		assertEquals(TEXT2, lic2.getText());
		assertEquals(COMMENT1, lic2.getComment());
		SPDXNonStandardLicense lic3 = new SPDXNonStandardLicense(model, licResource.asNode());
		assertEquals(TEXT2, lic3.getText());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXNonStandardLicense#setComment(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetComment() throws InvalidSPDXAnalysisException {
		SPDXNonStandardLicense lic = new SPDXNonStandardLicense(ID1, TEXT1);
		lic.setComment(COMMENT1);
		Resource licResource = lic.createResource(model);
		SPDXNonStandardLicense lic2 = new SPDXNonStandardLicense(model, licResource.asNode());
		lic2.setComment(COMMENT2);
		assertEquals(ID1, lic2.getId());
		assertEquals(TEXT1, lic2.getText());
		assertEquals(COMMENT2, lic2.getComment());
		StringWriter writer = new StringWriter();
		model.write(writer);
		@SuppressWarnings("unused")
		String rdfstring = writer.toString();
		SPDXNonStandardLicense lic3 = new SPDXNonStandardLicense(model, licResource.asNode());
		assertEquals(COMMENT2, lic3.getComment());	
	}
	@Test
	public void testSetLicenseName() throws InvalidSPDXAnalysisException {
		SPDXNonStandardLicense lic = new SPDXNonStandardLicense(ID1, TEXT1);
		lic.setLicenseName(LICENSENAME1);
		Resource licResource = lic.createResource(model);
		SPDXNonStandardLicense lic2 = new SPDXNonStandardLicense(model, licResource.asNode());
		lic2.setLicenseName(LICENSENAME2);
		assertEquals(LICENSENAME2, lic2.getLicenseName());
		SPDXNonStandardLicense lic3 = new SPDXNonStandardLicense(model, licResource.asNode());
		assertEquals(LICENSENAME2, lic3.getLicenseName());
	}
	
	@Test
	public void testSetSourceUrls() throws InvalidSPDXAnalysisException {
		SPDXNonStandardLicense lic = new SPDXNonStandardLicense(ID1, TEXT1);
		lic.setSourceUrls(SOURCEURLS1);
		Resource licResource = lic.createResource(model);
		SPDXNonStandardLicense lic2 = new SPDXNonStandardLicense(model, licResource.asNode());
		lic2.setSourceUrls(SOURCEURLS2);
		if (!compareArrayContent(SOURCEURLS2, lic2.getSourceUrls())) {
			fail("Source URLS not the same");
		}
		SPDXNonStandardLicense lic3 = new SPDXNonStandardLicense(model, licResource.asNode());
		if (!compareArrayContent(SOURCEURLS2, lic3.getSourceUrls())) {
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
		SPDXNonStandardLicense lic = new SPDXNonStandardLicense(ID1, TEXT1, 
				LICENSENAME1, SOURCEURLS1, COMMENT1);
		@SuppressWarnings("unused")
		Resource licResource = lic.createResource(model);
		
		SPDXNonStandardLicense lic2 = (SPDXNonStandardLicense)lic.clone();

		assertEquals(ID1, lic2.getId());
		assertEquals(TEXT1, lic2.getText());
		assertEquals(COMMENT1, lic2.getComment());
		assertEquals(LICENSENAME1, lic2.getLicenseName());
		assertTrue(compareArrayContent(SOURCEURLS1, lic2.getSourceUrls()));
		assertTrue(lic2.getResource() == null);
	}
}
