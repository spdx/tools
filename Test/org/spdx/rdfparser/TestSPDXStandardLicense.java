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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Gary O'Neall
 *
 */
public class TestSPDXStandardLicense {

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
		Model model = ModelFactory.createDefaultModel();
		String name = "name";
		String id = "id";
		String text = "text";
		String sourceUrl = "source url";
		String notes = "notes";
		String standardLicenseHeader = "Standard license header";
		String template = "template";
		SPDXStandardLicense stdl = new SPDXStandardLicense(name, id, text,
				sourceUrl, notes, standardLicenseHeader, template);
		Resource licResource = stdl.createResource(model);
		SPDXStandardLicense compLic = new SPDXStandardLicense(model, licResource.asNode());
		assertEquals(id, compLic.getId());
		assertEquals(text, compLic.getText());
/*		assertEquals(name, compLic.getName());
		assertEquals(sourceUrl, compLic.getSourceUrl());
		assertEquals(notes, compLic.getNotes());
		assertEquals(standardLicenseHeader, compLic.getStandardLicenseHeader());
		assertEquals(template, compLic.getTemplate());
*/
	}
	
	@Test
	public void testSetIDandText() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		String name = "name";
		String id = "id";
		String text = "text";
		String sourceUrl = "source url";
		String notes = "notes";
		String standardLicenseHeader = "Standard license header";
		String template = "template";
		SPDXStandardLicense stdl = new SPDXStandardLicense(name, id, text,
				sourceUrl, notes, standardLicenseHeader, template);
		Resource licResource = stdl.createResource(model);
		SPDXStandardLicense compLic = new SPDXStandardLicense(model, licResource.asNode());
		assertEquals(id, compLic.getId());
		assertEquals(text, compLic.getText());
		
		String newID = "newID";
		String newText = "new Text";
		compLic.setId(newID);
		compLic.setText(newText);
		assertEquals(newID, compLic.getId());
		assertEquals(newText, compLic.getText());
		SPDXStandardLicense compLic2 = new SPDXStandardLicense(model, licResource.asNode());
		assertEquals(newID, compLic2.getId());
		assertEquals(newText, compLic2.getText());
	}
	
	@Test
	public void testCreateMultile() {
		// test to make sure if we create a node with the same id, we
		// get back the same URI
		Model model = ModelFactory.createDefaultModel();
		String name = "name";
		String id = "id";
		String text = "text";
		String sourceUrl = "source url";
		String notes = "notes";
		String standardLicenseHeader = "Standard license header";
		String template = "template";
		String id2 = "id2";
		String name2 = "name2";
		
		SPDXStandardLicense stdl = new SPDXStandardLicense(name, id, text,
				sourceUrl, notes, standardLicenseHeader, template);
		Resource licResource = stdl.createResource(model);
		
		SPDXStandardLicense stdl3 = new SPDXStandardLicense(name2, id2, text,
				sourceUrl, notes, standardLicenseHeader, template);
		@SuppressWarnings("unused")
		Resource compResource3 = stdl3.createResource(model);
		
		SPDXStandardLicense stdl2 = new SPDXStandardLicense(name2, id, text,
				sourceUrl, notes, standardLicenseHeader, template);
		Resource compResource = stdl2.createResource(model);
		assertTrue(licResource.equals(compResource));
		assertEquals(licResource.getURI(), compResource.getURI());
	}
}
