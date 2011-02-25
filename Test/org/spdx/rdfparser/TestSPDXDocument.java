/**
 * Copyright (c) 2011 Source Auditor Inc.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.spdx.rdfparser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.SPDXDocument.SPDXPackage;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Source Auditor
 *
 */
public class TestSPDXDocument {

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
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#SPDXDocument(com.hp.hpl.jena.rdf.model.Model)}.
	 */
	@Test
	public void testSPDXDocument() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#getSpdxVersion()}.
	 */
	@Test
	public void testGetSpdxVersion() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#setSpdxVersion(java.lang.String)}.
	 * @throws InvalidSPDXDocException 
	 * @throws IOException 
	 */
	@Test
	public void testSetSpdxVersion() throws InvalidSPDXDocException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxDocument(testUri);
		String noVersion = doc.getSpdxVersion();
		assertNull(noVersion);
		String testVersion = "0.7.2";
		doc.setSpdxVersion(testVersion);
		String resultVersion = doc.getSpdxVersion();
		assertEquals(testVersion, resultVersion);
		writer = new StringWriter();
		doc.getModel().write(writer);
		String afterCreate = writer.toString();
		String testVersion2 = "1.3.3";
		doc.setSpdxVersion(testVersion2);
		String resultVersion2 = doc.getSpdxVersion();
		assertEquals(testVersion2, resultVersion2);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#getCreatedBy()}.
	 */
	@Test
	public void testGetCreatedBy() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#setCreatedBy(java.lang.String[])}.
	 * @throws InvalidSPDXDocException 
	 * @throws IOException 
	 */
	@Test
	public void testSetCreatedBy() throws InvalidSPDXDocException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxDocument(testUri);
		String[] noCreatedBy = doc.getCreatedBy();
		assertEquals(0, noCreatedBy.length);
		String[] testCreatedBy = new String[] {"Created By Me"};
		doc.setCreatedBy(testCreatedBy);
		String[] resultCreatedBy = doc.getCreatedBy();
		compareArrays(testCreatedBy, resultCreatedBy);
		writer = new StringWriter();
		doc.getModel().write(writer);
		String afterCreate = writer.toString();
		String[] testCreatedBy2 = new String[] {"second created", "another", "and another"};
		doc.setCreatedBy(testCreatedBy2);
		String[] resultCreatedBy2 = doc.getCreatedBy();
		compareArrays(testCreatedBy2, resultCreatedBy2);
	}

	private void compareArrays(Object[] a1,
			Object[] a2) {
		assertEquals(a1.length, a2.length);
		for (int i = 0; i < a1.length; i++) {
			boolean found = false;
			for (int j = 0; j < a2.length; j++) {
				if (a1[i].equals(a2[j])) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#getReviewers()}.
	 */
	@Test
	public void testGetReviewers() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#setReviewers(java.lang.String[])}.
	 * @throws InvalidSPDXDocException 
	 * @throws IOException 
	 */
	@Test
	public void testSetReviewers() throws InvalidSPDXDocException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxDocument(testUri);
		String[] noReviewedBy = doc.getReviewers();
		assertEquals(0, noReviewedBy.length);
		String[] testreviewedBy = new String[] {"reviewed By Me"};
		doc.setReviewers(testreviewedBy);
		String[] resultreviewedBy = doc.getReviewers();
		compareArrays(testreviewedBy, resultreviewedBy);
		String[] testreviewedBy2 = new String[] {"second reviewed", "another", "and another"};
		doc.setReviewers(testreviewedBy2);
		String[] resultreviewedBy2 = doc.getReviewers();
		compareArrays(testreviewedBy2, resultreviewedBy2);
		writer = new StringWriter();
		doc.getModel().write(writer);
		String afterCreate = writer.toString();
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#getCreated()}.
	 */
	@Test
	public void testGetCreated() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#setCreated(java.lang.String)}.
	 * @throws InvalidSPDXDocException 
	 * @throws IOException 
	 */
	@Test
	public void testSetCreated() throws InvalidSPDXDocException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxDocument(testUri);
		String noCreated = doc.getCreated();
		assertNull(noCreated);
		String testCreated = "Created By Me";
		doc.setCreated(testCreated);
		String resultCreated = doc.getCreated();
		assertEquals(testCreated, resultCreated);
		writer = new StringWriter();
		doc.getModel().write(writer);
		String afterCreate = writer.toString();
		String testCreated2 = "second created";
		doc.setCreated(testCreated2);
		String resultCreated2 = doc.getCreated();
		assertEquals(testCreated2, resultCreated2);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#getSpdxPackage()}.
	 * @throws InvalidSPDXDocException 
	 * @throws IOException 
	 */
	@Test
	public void testGetSpdxPackage() throws InvalidSPDXDocException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testDocUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxDocument(testDocUri);
		String testPkgUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;uniquepackagename";
		doc.createSpdxPackage(testPkgUri);
		writer = new StringWriter();
		doc.getModel().write(writer);
		String afterCreate = writer.toString();
		if (!afterCreate.contains("uniquepackagename")) {
			fail("missing uri in RDF document");
		}
		SPDXPackage pkg = doc.getSpdxPackage();
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#createSpdxPackage(String)}.
	 */
	@Test
	public void testCreateSpdxPackage() {
		
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#getNonStandardLicenses()}.
	 */
	@Test
	public void testGetNonStandardLicenses() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#setNonStandardLicenses(org.spdx.rdfparser.SPDXLicense[])}.
	 * @throws InvalidSPDXDocException 
	 * @throws IOException 
	 */
	@Test
	public void testSetNonStandardLicenses() throws InvalidSPDXDocException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxDocument(testUri);
		SPDXLicense[] noNonStdLic = doc.getNonStandardLicenses();
		assertEquals(0, noNonStdLic.length);
		SPDXLicense[] testNonStdLic = new SPDXLicense[] {new SPDXLicense(
				"name", "LicID1", "Licnese Text 1", "URL", "LicNotes1", "StdHeader", "Template")};
		doc.setNonStandardLicenses(testNonStdLic);
		SPDXLicense[] resultNonStdLic = doc.getNonStandardLicenses();
		assertEquals(1, resultNonStdLic.length);
		assertEquals(testNonStdLic[0].getId(), resultNonStdLic[0].getId());
		assertEquals(testNonStdLic[0].getText(), resultNonStdLic[0].getText());

		SPDXLicense[] testNonStdLic2 = new SPDXLicense[] {new SPDXLicense(
				"name", "LicID2", "Licnese Text 2", "URL", "LicNotes1", "StdHeader", "Template"),
				new SPDXLicense(
						"name", "LicID3", "Licnese Text 3", "URL", "LicNotes1", "StdHeader", "Template"),
				new SPDXLicense(
						"name", "LicID4", "Licnese Text 4", "URL", "LicNotes1", "StdHeader", "Template")};
		doc.setNonStandardLicenses(testNonStdLic2);
		SPDXLicense[] resultNonStdLic2 = doc.getNonStandardLicenses();
		assertEquals(testNonStdLic2.length, resultNonStdLic2.length);
		String[] testLicIds = new String[testNonStdLic2.length];
		String[] testLicTexts = new String[testNonStdLic2.length];
		String[] resultLicIds = new String[testNonStdLic2.length];
		String[] resultLicTexts = new String[testNonStdLic2.length];
		for (int i = 0; i < testLicIds.length; i++) {
			testLicIds[i] = testNonStdLic2[i].getId();
			testLicTexts[i] = testNonStdLic2[i].getText();
			resultLicIds[i] = resultNonStdLic2[i].getId();
			resultLicTexts[i] = resultNonStdLic2[i].getText();
		}
		compareArrays(testLicIds, resultLicIds);
		compareArrays(testLicTexts, resultLicTexts);
		writer = new StringWriter();
		doc.getModel().write(writer);
		String afterCreate = writer.toString();
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#getName()}.
	 */
	@Test
	public void testGetName() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#createSpdxDocument(java.lang.String)}.
	 * @throws InvalidSPDXDocException 
	 * @throws IOException 
	 */
	@Test
	public void testCreateSpdxDocument() throws InvalidSPDXDocException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxDocument(testUri);
		writer = new StringWriter();
		doc.getModel().write(writer);
		String afterCreate = writer.toString();
		if (!afterCreate.contains(testUri)) {
//			fail("Uri string not present after spdx document create");	
			// these don't actually match becuase there is some extra escaping going on in the URL string
		}
		String uriResult = doc.getSpdxDocUri();
		assertEquals(testUri, uriResult);
	}

}
