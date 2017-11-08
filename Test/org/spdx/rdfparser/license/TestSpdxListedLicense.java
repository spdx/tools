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
package org.spdx.rdfparser.license;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.html.InvalidLicenseTemplateException;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.IRdfModel;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

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
		@Override
		public boolean spdxElementRefExists(String elementRef) {
			return false;
		}
		@Override
		public void addSpdxElementRef(String elementRef)
				throws InvalidSPDXAnalysisException {
			
		}
		@Override
		public String documentNamespaceToId(String externalNamespace) {
			return null;
		}
		@Override
		public String externalDocumentIdToNamespace(String docId) {
			return null;
		}
		@Override
		public Resource createResource(Resource duplicate, String uri,
				Resource type, IRdfModel modelObject) {
			if (duplicate != null) {
				return duplicate;
			} else if (uri == null) {			
				return model.createResource(type);
			} else {
				return model.createResource(uri, type);
			}
		}
		@Override
		public boolean addCheckNodeObject(Node node, IRdfModel rdfModelObject) {
			return false;
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
	public void testCreate() throws InvalidSPDXAnalysisException, InvalidLicenseTemplateException {
		model = ModelFactory.createDefaultModel();
		String name = "name";
		String id = "AFL-3.0";
		String text = "text";
		String[] sourceUrls = new String[] {"source url1", "source url2"};
		String notes = "notes";
		String standardLicenseHeader = "Standard license header";
		String template = "template";
		String licenseHtml = "<html>html</html>";
		SpdxListedLicense stdl = new SpdxListedLicense(name, id, text,
				sourceUrls, notes, standardLicenseHeader, template, true, true, licenseHtml);
		Resource licResource = stdl.createResource(modelContainer);
		SpdxListedLicense compLic = new SpdxListedLicense(modelContainer, licResource.asNode());
		assertEquals(id, compLic.getLicenseId());
		assertEquals(text, compLic.getLicenseText());
		List<String> verify = stdl.verify();
		assertEquals(0, verify.size());
		verify = compLic.verify();
		assertEquals(0, verify.size());
		assertEquals(name, compLic.getName());
		assertEquals(sourceUrls.length, compLic.getSeeAlso().length);
		assertEquals(notes, compLic.getComment());
		assertEquals(standardLicenseHeader, compLic.getStandardLicenseHeader());
		assertEquals(template, compLic.getStandardLicenseTemplate());
		assertTrue(compLic.isFsfLibre());
		assertTrue(compLic.isOsiApproved());
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

		List<String> verify = stdl.verify();
		assertEquals(0, verify.size());
		verify = compLic.verify();
		assertEquals(0, verify.size());
	}
	
	@Test
	public void testSetFsfLibre() throws InvalidSPDXAnalysisException {
		model = ModelFactory.createDefaultModel();
		String name = "name";
		String id = "AFL-3.0";
		String text = "text";
		String[] sourceUrls = new String[] {"source url1", "source url2"};
		String notes = "notes";
		String standardLicenseHeader = "Standard license header";
		String template = "template";
		String licenseHtml = "<html>html</html>";
		SpdxListedLicense stdl = new SpdxListedLicense(name, id, text,
				sourceUrls, notes, standardLicenseHeader, template, false, false, licenseHtml);
		assertFalse(stdl.isFsfLibre());
		stdl.setFsfLibre(true);
		assertTrue(stdl.isFsfLibre());
		Resource licResource = stdl.createResource(modelContainer);
		SpdxListedLicense compLic = new SpdxListedLicense(modelContainer, licResource.asNode());
		assertTrue(stdl.isFsfLibre());
		compLic.setFsfLibre(false);
		assertFalse(compLic.isFsfLibre());
		SpdxListedLicense compLic2 = new SpdxListedLicense(modelContainer, licResource.asNode());
		assertFalse(compLic2.isFsfLibre());
		List<String> verify = stdl.verify();
		assertEquals(0, verify.size());
		verify = compLic.verify();
		assertEquals(0, verify.size());
	}
	
	@Test
	public void testSetDeprecated() throws InvalidSPDXAnalysisException {
		model = ModelFactory.createDefaultModel();
		String name = "name";
		String id = "AFL-3.0";
		String text = "text";
		String[] sourceUrls = new String[] {"source url2", "source url3"};
		String comments = "comments1";
		String standardLicenseHeader = "Standard license header";
		String template = "template";
		SpdxListedLicense stdl = new SpdxListedLicense(name, id, text,
				sourceUrls, comments, standardLicenseHeader, template, true);
		stdl.setDeprecated(true);
		Resource licResource = stdl.createResource(modelContainer);
		SpdxListedLicense compLic = new SpdxListedLicense(modelContainer, licResource.asNode());
		assertEquals(true, compLic.isDeprecated());
		
		compLic.setDeprecated(false);
		assertEquals(false, compLic.isDeprecated());
		SpdxListedLicense compLic2 = new SpdxListedLicense(modelContainer, licResource.asNode());
		assertEquals(false, compLic2.isDeprecated());
		List<String> verify = stdl.verify();
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
		List<String> verify = stdl.verify();
		assertEquals(1, verify.size());	// verify will fail since this is not a valid listed license ID
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
		stdl.setDeprecated(true);
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
		assertEquals(true, lic2.isDeprecated());
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
	public void testEquivalent() throws InvalidSPDXAnalysisException {
		model = ModelFactory.createDefaultModel();
		String name = "name";
		String name2 = "name2";
		String id = "AFL-3.0";
		String text = "text";
		String text2 = "text2";
		String[] sourceUrls = new String[] {"source url1", "source url2"};
		String[] sourceUrls2 = new String[] {"source url2"};
		String notes = "notes";
		String notes2 = "notes2";
		String standardLicenseHeader = "Standard license header";
		String standardLicenseHeader2 = "Standard license header2";
		String template = "template";
		String template2 = "template2";
		SpdxListedLicense stdl = new SpdxListedLicense(name, id, text,
				sourceUrls, notes, standardLicenseHeader, template, true);
		assertTrue(stdl.equivalent(stdl));
		SpdxListedLicense stdl2 = new SpdxListedLicense(name2, id, text2,
				sourceUrls2, notes2, standardLicenseHeader2, template2, false);
		assertTrue(stdl2.equivalent(stdl));
		stdl2.setLicenseId("Apache-2.0");
		assertFalse(stdl.equivalent(stdl2));
	}
	
	@Test
	public void testSetHeaderTemplate() throws InvalidSPDXAnalysisException {
		model = ModelFactory.createDefaultModel();
		String name = "name";
		String id = "AFL-3.0";
		String text = "text";
		String[] sourceUrls = new String[] {"source url2", "source url3"};
		String notes = "notes";
		String standardLicenseHeader = "Standard license header";
		String standardLicenseHeaderTemplate = "Standard license<<beginOptional>>optional<<endOptional>> header";
		String template = "template";
		SpdxListedLicense stdl = new SpdxListedLicense(name, id, text,
				sourceUrls, notes, standardLicenseHeader, template, standardLicenseHeaderTemplate, true, true);
		assertEquals(standardLicenseHeaderTemplate, stdl.getStandardLicenseHeaderTemplate());
		Resource licResource = stdl.createResource(modelContainer);
		SpdxListedLicense compLic = new SpdxListedLicense(modelContainer, licResource.asNode());
		assertEquals(standardLicenseHeaderTemplate, compLic.getStandardLicenseHeaderTemplate());
		
		String newHeaderTemplate = "New standard license template";
		compLic.setStandardLicenseHeaderTemplate(newHeaderTemplate);
		assertEquals(newHeaderTemplate, compLic.getStandardLicenseHeaderTemplate());
		SpdxListedLicense compLic2 = new SpdxListedLicense(modelContainer, licResource.asNode());
		assertEquals(newHeaderTemplate, compLic2.getStandardLicenseHeaderTemplate());
		List<String> verify = stdl.verify();
		assertEquals(0, verify.size());
		verify = compLic.verify();
		assertEquals(0, verify.size());
	}
	
	@Test
	public void testSetHeaderTemplateHtml() throws InvalidSPDXAnalysisException, InvalidLicenseTemplateException {
		model = ModelFactory.createDefaultModel();
		String name = "name";
		String id = "AFL-3.0";
		String text = "text";
		String[] sourceUrls = new String[] {"source url2", "source url3"};
		String notes = "notes";
		String standardLicenseHeader = "Standard license header";
		String standardLicenseHeaderTemplate = "Standard license<<beginOptional>>optional<<endOptional>> header";
		String template = "template";
		String standardLicenseHeaderHtml = "<h1>licenseHeader</h1>";
		String textHtml = "<h1>text</h1>";
		SpdxListedLicense stdl = new SpdxListedLicense(name, id, text,
				sourceUrls, notes, standardLicenseHeader, template, standardLicenseHeaderTemplate, 
				true, true, textHtml, standardLicenseHeaderHtml);
		assertEquals(textHtml, stdl.getLicenseTextHtml());
		assertEquals(standardLicenseHeaderHtml, stdl.getLicenseHeaderHtml());
		String newStandardLicenseHeaderHtml = "<h2>licenseHeader2</h2>";
		String newTextHtml = "<h2>text2</h2>";
		stdl.setLicenseTextHtml(newTextHtml);
		stdl.setLicenseHeaderHtml(newStandardLicenseHeaderHtml);
		assertEquals(newTextHtml, stdl.getLicenseTextHtml());
		assertEquals(newStandardLicenseHeaderHtml, stdl.getLicenseHeaderHtml());
	}
}
