/**
 * Copyright (c) 2011 Source Auditor Inc.
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
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.license.SpdxListedLicense;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Gary O'Neall
 *
 */
public class TestSpdxListedLicense {

	Model model;
	IModelContainer modelContainer = new IModelContainer() {
		@Override
		public String getNextSpdxElementRef() {
			return null;
		}
		@Override
		public Model getModel() {
			return model;
		}

		@Override
		public String getDocumentNamespace() {
			return "http://testNameSPace#";
		}
		
	};
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
	
	@Test
	public void testCreate() throws InvalidSPDXAnalysisException {
		model = ModelFactory.createDefaultModel();
		String name = "name";
		String id = "AFL-3.0";
		String text = "text";
		String[] sourceUrls = new String[] {"source url1", "source url2"};
		String notes = "notes";
		String standardLicenseHeader = "Standard license header";
		String template = "template";
		SpdxListedLicense stdl = new SpdxListedLicense(name, id, text,
				sourceUrls, notes, standardLicenseHeader, template, true);
		Resource licResource = stdl.createResource(modelContainer);
		SpdxListedLicense compLic = new SpdxListedLicense(modelContainer, licResource.asNode());
		assertEquals(id, compLic.getLicenseId());
		assertEquals(text, compLic.getLicenseText());
		ArrayList<String> verify = stdl.verify();
		assertEquals(0, verify.size());
		verify = compLic.verify();
		assertEquals(0, verify.size());
/*		assertEquals(name, compLic.getName());
		assertEquals(sourceUrl, compLic.getSourceUrl());
		assertEquals(notes, compLic.getNotes());
		assertEquals(standardLicenseHeader, compLic.getStandardLicenseHeader());
		assertEquals(template, compLic.getTemplate());
*/
	}
	
	@Test
	public void testSetComment() throws InvalidSPDXAnalysisException {
		model = ModelFactory.createDefaultModel();
		String name = "name";
		String id = "AFL-3.0";
		String text = "text";
		String[] sourceUrls = new String[] {"source url2", "source url3"};
		String comments = "comments1";
		String comments2 = "comments2";
		String standardLicenseHeader = "Standard license header";
		String template = "template";
		SpdxListedLicense stdl = new SpdxListedLicense(name, id, text,
				sourceUrls, comments, standardLicenseHeader, template, true);
		Resource licResource = stdl.createResource(modelContainer);
		SpdxListedLicense compLic = new SpdxListedLicense(modelContainer, licResource.asNode());
		assertEquals(comments, compLic.getComment());
		
		compLic.setComment(comments2);
		assertEquals(comments2, compLic.getComment());
		SpdxListedLicense compLic2 = new SpdxListedLicense(modelContainer, licResource.asNode());
		assertEquals(comments2, compLic2.getComment());
		StringWriter writer = new StringWriter();
		model.write(writer);
		@SuppressWarnings("unused")
		String rdfstring = writer.toString();

		ArrayList<String> verify = stdl.verify();
		assertEquals(0, verify.size());
		verify = compLic.verify();
		assertEquals(0, verify.size());
	}
	
	@Test
	public void testSetIDandText() throws InvalidSPDXAnalysisException {
		model = ModelFactory.createDefaultModel();
		String name = "name";
		String id = "AFL-3.0";
		String text = "text";
		String[] sourceUrls = new String[] {"source url2", "source url3"};
		String notes = "notes";
		String standardLicenseHeader = "Standard license header";
		String template = "template";
		SpdxListedLicense stdl = new SpdxListedLicense(name, id, text,
				sourceUrls, notes, standardLicenseHeader, template, true);
		Resource licResource = stdl.createResource(modelContainer);
		SpdxListedLicense compLic = new SpdxListedLicense(modelContainer, licResource.asNode());
		assertEquals(id, compLic.getLicenseId());
		assertEquals(text, compLic.getLicenseText());
		
		String newID = "newID";
		String newText = "new Text";
		compLic.setLicenseId(newID);
		compLic.setLicenseText(newText);
		assertEquals(newID, compLic.getLicenseId());
		assertEquals(newText, compLic.getLicenseText());
		SpdxListedLicense compLic2 = new SpdxListedLicense(modelContainer, licResource.asNode());
		assertEquals(newID, compLic2.getLicenseId());
		assertEquals(newText, compLic2.getLicenseText());
		ArrayList<String> verify = stdl.verify();
		assertEquals(0, verify.size());
		verify = compLic.verify();
		assertEquals(1, verify.size());	// verify will fail since this is not a valid listed license ID
	}
	
	@Test
	public void testCreateMultile() {
		// test to make sure if we create a node with the same id, we
		// get back the same URI
		model = ModelFactory.createDefaultModel();
		String name = "name";
		String id = "AFL-3.0";
		String text = "text";
		String[] sourceUrls1 = new String[] {"source url1", "source url2"};
		String[] sourceUrls2 = new String[] {"source url3", "source url4"};
		String notes = "notes";
		String standardLicenseHeader = "Standard license header";
		String template = "template";
		String id2 = "Apache-1.0";
		String name2 = "name2";
		
		try {
    		SpdxListedLicense stdl = new SpdxListedLicense(name, id, text,
    				sourceUrls1, notes, standardLicenseHeader, template, true);
    		Resource licResource = stdl.createResource(modelContainer);
    		
    		SpdxListedLicense stdl3 = new SpdxListedLicense(name2, id2, text,
    				sourceUrls2, notes, standardLicenseHeader, template, true);
    		@SuppressWarnings("unused")
    		Resource compResource3 = stdl3.createResource(modelContainer);
    		
    		SpdxListedLicense stdl2 = new SpdxListedLicense(name2, id, text,
    				sourceUrls2, notes, standardLicenseHeader, template, true);
            
    		Resource compResource = stdl2.createResource(modelContainer);
            assertTrue(licResource.equals(compResource));
            assertEquals(licResource.getURI(), compResource.getURI());		
		} catch (InvalidSPDXAnalysisException e) {
		    throw new RuntimeException(e);
		}
	}
	
	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		model = ModelFactory.createDefaultModel();
		String name = "name";
		String id = "AFL-3.0";
		String text = "text";
		String[] sourceUrls = new String[] {"source url1", "source url2"};
		String notes = "notes";
		String standardLicenseHeader = "Standard license header";
		String template = "template";
		SpdxListedLicense stdl = new SpdxListedLicense(name, id, text,
				sourceUrls, notes, standardLicenseHeader, template, true);
		Resource licResource = stdl.createResource(modelContainer);
		SpdxListedLicense compLic = new SpdxListedLicense(modelContainer, licResource.asNode());

		
		SpdxListedLicense lic2 = (SpdxListedLicense)compLic.clone();

		assertEquals(id, lic2.getLicenseId());
		assertEquals(text, lic2.getLicenseText());
		assertEquals(notes, lic2.getComment());
		assertEquals(name, lic2.getName());
		assertTrue(compareArrayContent(sourceUrls, lic2.getSeeAlso()));
		assertEquals(standardLicenseHeader, lic2.getStandardLicenseHeader());
		assertEquals(template, lic2.getStandardLicenseTemplate());
		assertTrue(lic2.getResource() == null);
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
}
