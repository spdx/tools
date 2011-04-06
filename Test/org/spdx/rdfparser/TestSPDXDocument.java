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
import org.spdx.rdfparser.SPDXAnalysis.SPDXPackage;

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
	 * Test method for {@link org.spdx.rdfparser.SPDXAnalysis#SPDXAnalysis(com.hp.hpl.jena.rdf.model.Model)}.
	 */
	@Test
	public void testSPDXAnalysis() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXAnalysis#getSpdxVersion()}.
	 */
	@Test
	public void testGetSpdxVersion() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXAnalysis#setSpdxVersion(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testSetSpdxVersion() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXAnalysis doc = new SPDXAnalysis(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxAnalysis(testUri);
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
	 * Test method for {@link org.spdx.rdfparser.SPDXAnalysis#getCreators()}.
	 */
	@Test
	public void testGetCreatedBy() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXAnalysis#setCreator(java.lang.String[])}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testSetCreatedBy() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXAnalysis doc = new SPDXAnalysis(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxAnalysis(testUri);
		String[] noCreatedBy = doc.getCreators();
		assertEquals(0, noCreatedBy.length);
		String[] testCreatedBy = new String[] {"Created By Me"};
		doc.setCreators(testCreatedBy);
		String[] resultCreatedBy = doc.getCreators();
		compareArrays(testCreatedBy, resultCreatedBy);
		writer = new StringWriter();
		doc.getModel().write(writer);
		String afterCreate = writer.toString();
		String[] testCreatedBy2 = new String[] {
				"second created", 
				"another",
				"and another"};
		doc.setCreators(testCreatedBy2);
		String[] resultCreatedBy2 = doc.getCreators();
		compareArrays(testCreatedBy2, resultCreatedBy2);
	}
	
	@Test
	public void testCreatorComment() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXAnalysis doc = new SPDXAnalysis(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxAnalysis(testUri);
		String creatorComment = doc.getCreatorComment();
		if (creatorComment != null) {
			if (!creatorComment.isEmpty()) {
				fail("Comment should be empty");
			}
		}
		String comment = "This is a comment";
		doc.setCreatorComment(comment);
		String result = doc.getCreatorComment();
		assertEquals(comment, result);
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
	 * Test method for {@link org.spdx.rdfparser.SPDXAnalysis#getReviewers()}.
	 */
	@Test
	public void testGetReviewers() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXAnalysis#setReviewers(java.lang.String[])}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testSetReviewers() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXAnalysis doc = new SPDXAnalysis(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxAnalysis(testUri);
		SPDXReview[] noReviewedBy = doc.getReviewers();
		assertEquals(0, noReviewedBy.length);
		SPDXReview[] testreviewedBy = new SPDXReview[] {new SPDXReview("reviewed By Me", "date1", "comment1")};
		doc.setReviewers(testreviewedBy);
		SPDXReview[] resultreviewedBy = doc.getReviewers();
		compareArrays(testreviewedBy, resultreviewedBy);
		SPDXReview[] testreviewedBy2 = new SPDXReview[] {new SPDXReview("review1", "date=1", "comment-1"), 
				new SPDXReview("review2", "date2", "comment2"), 
				new SPDXReview("review3", "date3", "comment3")};
		doc.setReviewers(testreviewedBy2);
		SPDXReview[] resultreviewedBy2 = doc.getReviewers();
		compareArrays(testreviewedBy2, resultreviewedBy2);
		writer = new StringWriter();
		doc.getModel().write(writer);
		String afterCreate = writer.toString();
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXAnalysis#getCreated()}.
	 */
	@Test
	public void testGetCreated() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXAnalysis#setCreated(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testSetCreated() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXAnalysis doc = new SPDXAnalysis(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxAnalysis(testUri);
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
	 * Test method for {@link org.spdx.rdfparser.SPDXAnalysis#getSpdxPackage()}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testGetSpdxPackage() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXAnalysis doc = new SPDXAnalysis(model);
		String testDocUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxAnalysis(testDocUri);
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
	 * Test method for {@link org.spdx.rdfparser.SPDXAnalysis#createSpdxPackage(String)}.
	 */
	@Test
	public void testCreateSpdxPackage() {
		
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXAnalysis#getNonStandardLicenses()}.
	 */
	@Test
	public void testGetNonStandardLicenses() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXAnalysis#setNonStandardLicenses(org.spdx.rdfparser.SPDXStandardLicense[])}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testSetNonStandardLicenses() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXAnalysis doc = new SPDXAnalysis(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxAnalysis(testUri);
		SPDXStandardLicense[] noNonStdLic = doc.getNonStandardLicenses();
		assertEquals(0, noNonStdLic.length);
		SPDXStandardLicense[] testNonStdLic = new SPDXStandardLicense[] {new SPDXStandardLicense(
				"name", "LicID1", "Licnese Text 1", "URL", "LicNotes1", "StdHeader", "Template")};
		doc.setNonStandardLicenses(testNonStdLic);
		SPDXStandardLicense[] resultNonStdLic = doc.getNonStandardLicenses();
		assertEquals(1, resultNonStdLic.length);
		assertEquals(testNonStdLic[0].getId(), resultNonStdLic[0].getId());
		assertEquals(testNonStdLic[0].getText(), resultNonStdLic[0].getText());

		SPDXStandardLicense[] testNonStdLic2 = new SPDXStandardLicense[] {new SPDXStandardLicense(
				"name", "LicID2", "Licnese Text 2", "URL", "LicNotes1", "StdHeader", "Template"),
				new SPDXStandardLicense(
						"name", "LicID3", "Licnese Text 3", "URL", "LicNotes1", "StdHeader", "Template"),
				new SPDXStandardLicense(
						"name", "LicID4", "Licnese Text 4", "URL", "LicNotes1", "StdHeader", "Template")};
		doc.setNonStandardLicenses(testNonStdLic2);
		SPDXStandardLicense[] resultNonStdLic2 = doc.getNonStandardLicenses();
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
	 * Test method for {@link org.spdx.rdfparser.SPDXAnalysis#getName()}.
	 */
	@Test
	public void testGetName() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXAnalysis#createSpdxAnalysis(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testcreateSpdxDocument() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXAnalysis doc = new SPDXAnalysis(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxAnalysis(testUri);
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
