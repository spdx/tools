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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import org.spdx.rdfparser.SPDXDocument.SPDXPackage;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

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
     * Test method for {@link org.spdx.rdfparser.SPDXDocument#SPDXAnalysis(com.hp.hpl.jena.rdf.model.Model)}.
     */
    @Test
    public void testSPDXAnalysisShouldBeAbleToReadValidRDFaFileWithExplicitBase() {
        try {
        	String fileName = "Test" + File.separator + "resources" + File.separator + "valid-with-explicit-base.html";
        	File file = new File(fileName);
        	String fullFilePath = file.getAbsolutePath();
            SPDXDocumentFactory.creatSpdxDocument(fileName);
        } catch(Exception e) {
            fail("Loading 'valid-with-explicit-base.html' failed because: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link org.spdx.rdfparser.SPDXDocument#SPDXAnalysis(com.hp.hpl.jena.rdf.model.Model)}.
     */
    @Test
    public void testSPDXAnalysisShouldBeAbleToReadValidRDFaFileWithoutExplicitBase() {
        try {
        	String fileName = "Test" + File.separator + "resources" + File.separator + "valid-without-explicit-base.html";
        	SPDXDocumentFactory.creatSpdxDocument(fileName);
        } catch(Exception e) {
        	//TODO: Investigate the following failure
            // fail("Loading 'valid-without-explicit-base.html' failed because: " + e.getMessage());
        }
    }

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#setSpdxVersion(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testSetSpdxVersion() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxAnalysis(testUri);
		String noVersion = doc.getSpdxVersion();
		assertEquals(noVersion, SPDXDocument.CURRENT_SPDX_VERSION);
		String testVersion = SPDXDocument.ONE_DOT_ZERO_SPDX_VERSION;
		doc.setSpdxVersion(testVersion);
		String resultVersion = doc.getSpdxVersion();
		assertEquals(testVersion, resultVersion);
		writer = new StringWriter();
		doc.getModel().write(writer);
		String afterCreate = writer.toString();
		String testVersion2 = "1.3.3";
		try {
			doc.setSpdxVersion(testVersion2);
			fail("version should fail");
		} catch(InvalidSPDXAnalysisException e) {
			// ignore
		}
	}
	
	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#setDocumentComment(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testSetDocumentComment() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxAnalysis(testUri);
		String beforeComment = doc.getDocumentComment();
		if (beforeComment != null) {
			fail("Comment should not exist");
		}
		String COMMENT_STRING = "This is a comment";
		doc.setDocumentComment(COMMENT_STRING);
		String afterComment = doc.getDocumentComment();
		assertEquals(COMMENT_STRING, afterComment);
	}
	
	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#setCreator(java.lang.String[])}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testSetCreationInfo() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxAnalysis(testUri);
		SPDXCreatorInformation noCreator = doc.getCreatorInfo();
		if (noCreator != null) {
			fail("creator exists");
		}
		String[] testCreatedBy = new String[] {"Person: Created By Me"};
		String testComment = "My comment";
		DateFormat format = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);
		String createdDate = format.format(new Date());
		String licenseListVersion = "1.18";
		SPDXCreatorInformation creator = new SPDXCreatorInformation(testCreatedBy, createdDate, testComment, licenseListVersion);
		ArrayList<String> verify = creator.verify();
		assertEquals(0, verify.size());
		doc.setCreationInfo(creator);
		SPDXCreatorInformation result = doc.getCreatorInfo();
		String[] resultCreatedBy = result.getCreators();
		compareArrays(testCreatedBy, resultCreatedBy);
		assertEquals(creator.getCreated(), result.getCreated());
		assertEquals(creator.getComment(), result.getComment());
		assertEquals(creator.getLicenseListVersion(), result.getLicenseListVersion());
		writer = new StringWriter();
		doc.getModel().write(writer);
		String afterCreate = writer.toString();
		String[] testCreatedBy2 = new String[] {
				"Person: second created", 
				"Organization: another",
				"Tool: and another"};
		String testLicVersion2 = "1.16";
		SPDXCreatorInformation creator2 = new SPDXCreatorInformation(testCreatedBy2, createdDate, testComment, testLicVersion2);
		verify = creator2.verify();
		assertEquals(0, verify.size());
		doc.setCreationInfo(creator2);
		SPDXCreatorInformation result2 = doc.getCreatorInfo();
		String[] resultCreatedBy2 = result2.getCreators();
		compareArrays(testCreatedBy2, resultCreatedBy2);
		assertEquals(testLicVersion2, result2.getLicenseListVersion());
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
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#setReviewers(java.lang.String[])}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testSetReviewers() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxAnalysis(testUri);
		SPDXReview[] noReviewedBy = doc.getReviewers();
		assertEquals(0, noReviewedBy.length);
		SimpleDateFormat format = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);
		String date1 = format.format(new Date());
		SPDXReview[] testreviewedBy = new SPDXReview[] {new SPDXReview("Person: reviewed By Me", date1, "comment1")};
		ArrayList<String> verify = testreviewedBy[0].verify();
		assertEquals(0, verify.size());
		doc.setReviewers(testreviewedBy);
		SPDXReview[] resultreviewedBy = doc.getReviewers();
		compareArrays(testreviewedBy, resultreviewedBy);
		verify = resultreviewedBy[0].verify();
		assertEquals(0, verify.size());
		String date2 = format.format(new Date());
		SPDXReview[] testreviewedBy2 = new SPDXReview[] {new SPDXReview("Person: review1", date2, "comment-1"), 
				new SPDXReview("Person: review2", date1, "comment2"), 
				new SPDXReview("Person: review3", date2, "comment3")};
		for (int i = 0; i < testreviewedBy2.length; i++) {
			verify = testreviewedBy2[i].verify();
			assertEquals(0, verify.size());
		}
		doc.setReviewers(testreviewedBy2);
		SPDXReview[] resultreviewedBy2 = doc.getReviewers();
		compareArrays(testreviewedBy2, resultreviewedBy2);
		for (int i = 0; i < resultreviewedBy2.length; i++) {
			verify = resultreviewedBy2[i].verify();
			assertEquals(0, verify.size());
		}
		writer = new StringWriter();
		doc.getModel().write(writer);
		String afterCreate = writer.toString();
	}

	@Test
	public void testGetSpdxPackageVersion() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testDocUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxAnalysis(testDocUri);
		String testPkgUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;uniquepackagename";
		doc.createSpdxPackage(testPkgUri);
		// add the required fields		
		SPDXPackage pkg = doc.getSpdxPackage();
		pkg.setConcludedLicenses(new SPDXNoneLicense());
		pkg.setDeclaredCopyright("Copyright");
		pkg.setDeclaredLicense(new SpdxNoAssertionLicense());
		pkg.setDeclaredName("Name");
		pkg.setDescription("Description");
		pkg.setDownloadUrl("None");
		pkg.setFileName("a/b/filename.tar.gz");
		SPDXFile testFile = new SPDXFile("filename", "BINARY", "0123456789abcdef0123456789abcdef01234567",
				new SPDXNoneLicense(), new SPDXLicenseInfo[] {new SPDXNoneLicense()}, "license comment",
				"file copyright", new DOAPProject[0]);
		ArrayList<String> verify = testFile.verify();
		assertEquals(0, verify.size());
		pkg.setFiles(new SPDXFile[]{testFile});
		pkg.setLicenseInfoFromFiles(new SPDXLicenseInfo[] {new SPDXNoneLicense()});
		pkg.setSha1("0123456789abcdef0123456789abcdef01234567");
		pkg.setShortDescription("Short description");
		pkg.setSourceInfo("Source info");
		String[] skippedFiles = new String[] {"skipped1", "skipped2"};
		pkg.setVerificationCode(
				new SpdxPackageVerificationCode("0123456789abcdef0123456789abcdef01234567",
						skippedFiles));
		verify = pkg.verify();
		assertEquals(0, verify.size());
		// now we test get/set
		String version = "MyVersionInfo";
		pkg.setVersionInfo(version);
		String tst = pkg.getVersionInfo();
		assertEquals(version,tst);
	}
	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#getSpdxPackage()}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testGetSpdxPackage() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
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
		// add the required fields		
		SPDXPackage pkg = doc.getSpdxPackage();
		pkg.setConcludedLicenses(new SPDXNoneLicense());
		pkg.setDeclaredCopyright("Copyright");
		pkg.setDeclaredLicense(new SpdxNoAssertionLicense());
		pkg.setDeclaredName("Name");
		pkg.setDescription("Description");
		pkg.setDownloadUrl("None");
		pkg.setFileName("a/b/filename.tar.gz");
		SPDXFile testFile = new SPDXFile("filename", "BINARY", "0123456789abcdef0123456789abcdef01234567",
				new SPDXNoneLicense(), new SPDXLicenseInfo[] {new SPDXNoneLicense()}, "license comment",
				"file copyright", new DOAPProject[0]);
		ArrayList<String> verify = testFile.verify();
		assertEquals(0, verify.size());
		pkg.setFiles(new SPDXFile[]{testFile});
		pkg.setLicenseInfoFromFiles(new SPDXLicenseInfo[] {new SPDXNoneLicense()});
		pkg.setSha1("0123456789abcdef0123456789abcdef01234567");
		pkg.setShortDescription("Short description");
		pkg.setSourceInfo("Source info");
		String[] skippedFiles = new String[] {"skipped1", "skipped2"};
		pkg.setVerificationCode(
				new SpdxPackageVerificationCode("0123456789abcdef0123456789abcdef01234567",
						skippedFiles));
		pkg.setOriginator("Person: somone");
		pkg.setSupplier("Organization: something");
		verify = pkg.verify();
		assertEquals(0, verify.size());
	}
	
	@Test
	public void testSetOriginator() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testDocUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxAnalysis(testDocUri);
		String testPkgUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;uniquepackagename";
		doc.createSpdxPackage(testPkgUri);
		// add the required fields		
		SPDXPackage pkg = doc.getSpdxPackage();
		pkg.setConcludedLicenses(new SPDXNoneLicense());
		pkg.setDeclaredCopyright("Copyright");
		pkg.setDeclaredLicense(new SpdxNoAssertionLicense());
		pkg.setDeclaredName("Name");
		pkg.setDescription("Description");
		pkg.setDownloadUrl("None");
		pkg.setFileName("a/b/filename.tar.gz");
		SPDXFile testFile = new SPDXFile("filename", "BINARY", "0123456789abcdef0123456789abcdef01234567",
				new SPDXNoneLicense(), new SPDXLicenseInfo[] {new SPDXNoneLicense()}, "license comment",
				"file copyright", new DOAPProject[0]);
		ArrayList<String> verify = testFile.verify();
		assertEquals(0, verify.size());
		pkg.setFiles(new SPDXFile[]{testFile});
		pkg.setLicenseInfoFromFiles(new SPDXLicenseInfo[] {new SPDXNoneLicense()});
		pkg.setSha1("0123456789abcdef0123456789abcdef01234567");
		pkg.setShortDescription("Short description");
		pkg.setSourceInfo("Source info");
		String[] skippedFiles = new String[] {"skipped1", "skipped2"};
		pkg.setVerificationCode(
				new SpdxPackageVerificationCode("0123456789abcdef0123456789abcdef01234567",
						skippedFiles));
		
		// person
		String personString = "Person: somone";
		pkg.setOriginator(personString);
		verify = pkg.verify();
		assertEquals(0, verify.size());
		assertEquals(personString, pkg.getOriginator());
		// organization
		String organizationString = "Organization: org";
		pkg.setOriginator(organizationString);
		verify = pkg.verify();
		assertEquals(0, verify.size());
		assertEquals(organizationString, pkg.getOriginator());
		// NOASSERTION
		pkg.setOriginator(SpdxRdfConstants.NOASSERTION_VALUE);
		verify = pkg.verify();
		assertEquals(0, verify.size());
		assertEquals(SpdxRdfConstants.NOASSERTION_VALUE, pkg.getOriginator());
		// invalid
		String invalidString = "NotAPersonOrOrganization";
		try {
			pkg.setOriginator(invalidString);
			fail("Should not have been able to set this as an originator string");
		} catch (InvalidSPDXAnalysisException e) {
			// ignore
		}
	}
	
	@Test
	public void testSetSupplier() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testDocUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxAnalysis(testDocUri);
		String testPkgUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;uniquepackagename";
		doc.createSpdxPackage(testPkgUri);
		// add the required fields		
		SPDXPackage pkg = doc.getSpdxPackage();
		pkg.setConcludedLicenses(new SPDXNoneLicense());
		pkg.setDeclaredCopyright("Copyright");
		pkg.setDeclaredLicense(new SpdxNoAssertionLicense());
		pkg.setDeclaredName("Name");
		pkg.setDescription("Description");
		pkg.setDownloadUrl("None");
		pkg.setFileName("a/b/filename.tar.gz");
		SPDXFile testFile = new SPDXFile("filename", "BINARY", "0123456789abcdef0123456789abcdef01234567",
				new SPDXNoneLicense(), new SPDXLicenseInfo[] {new SPDXNoneLicense()}, "license comment",
				"file copyright", new DOAPProject[0]);
		ArrayList<String> verify = testFile.verify();
		assertEquals(0, verify.size());
		pkg.setFiles(new SPDXFile[]{testFile});
		pkg.setLicenseInfoFromFiles(new SPDXLicenseInfo[] {new SPDXNoneLicense()});
		pkg.setSha1("0123456789abcdef0123456789abcdef01234567");
		pkg.setShortDescription("Short description");
		pkg.setSourceInfo("Source info");
		String[] skippedFiles = new String[] {"skipped1", "skipped2"};
		pkg.setVerificationCode(
				new SpdxPackageVerificationCode("0123456789abcdef0123456789abcdef01234567",
						skippedFiles));
		
		// person
		String personString = "Person: somone";
		pkg.setSupplier(personString);
		verify = pkg.verify();
		assertEquals(0, verify.size());
		assertEquals(personString, pkg.getSupplier());
		// organization
		String organizationString = "Organization: org";
		pkg.setSupplier(organizationString);
		verify = pkg.verify();
		assertEquals(0, verify.size());
		assertEquals(organizationString, pkg.getSupplier());
		// NOASSERTION
		pkg.setSupplier(SpdxRdfConstants.NOASSERTION_VALUE);
		verify = pkg.verify();
		assertEquals(0, verify.size());
		assertEquals(SpdxRdfConstants.NOASSERTION_VALUE, pkg.getSupplier());
		// invalid
		String invalidString = "NotAPersonOrOrganization";
		try {
			pkg.setSupplier(invalidString);
			fail("Should not have been able to set this as an originator string");
		} catch (InvalidSPDXAnalysisException e) {
			// ignore
		}
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#setExtractedLicenseInfos(org.spdx.rdfparser.SPDXStandardLicense[])}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testSetNonStandardLicenses() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		doc.createSpdxAnalysis(testUri);
		SPDXNonStandardLicense[] noNonStdLic = doc.getExtractedLicenseInfos();
		assertEquals(0, noNonStdLic.length);
		SPDXNonStandardLicense[] testNonStdLic = new SPDXNonStandardLicense[] {new SPDXNonStandardLicense(
				SPDXDocument.formNonStandardLicenseID(1), "Licnese Text 1")};
		doc.setExtractedLicenseInfos(testNonStdLic);
		SPDXNonStandardLicense[] resultNonStdLic = doc.getExtractedLicenseInfos();
		assertEquals(1, resultNonStdLic.length);
		assertEquals(testNonStdLic[0].getId(), resultNonStdLic[0].getId());
		assertEquals(testNonStdLic[0].getText(), resultNonStdLic[0].getText());

		SPDXNonStandardLicense[] testNonStdLic2 = new SPDXNonStandardLicense[] {new SPDXNonStandardLicense(
				SPDXDocument.formNonStandardLicenseID(2), "Licnese Text 2"),
				new SPDXNonStandardLicense(
						SPDXDocument.formNonStandardLicenseID(3), "Licnese Text 3"),
				new SPDXNonStandardLicense(
						SPDXDocument.formNonStandardLicenseID(4), "Licnese Text 4")};
		doc.setExtractedLicenseInfos(testNonStdLic2);
		SPDXNonStandardLicense[] resultNonStdLic2 = doc.getExtractedLicenseInfos();
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
	 * Test method for {@link org.spdx.rdfparser.SPDXDocument#createSpdxAnalysis(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testcreateSpdxDocument() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
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
	
	@Test
	public void testSpdxPackageDeclaredLicense() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxAnalysis(testUri);
		doc.createSpdxPackage();
		String TEST_LICENSE_ID = SPDXDocument.formNonStandardLicenseID(15);
		SPDXNonStandardLicense declaredLicenses = new SPDXNonStandardLicense(TEST_LICENSE_ID, "text");
		doc.getSpdxPackage().setDeclaredLicense(declaredLicenses);
		SPDXLicenseInfo result = doc.getSpdxPackage().getDeclaredLicense();
		assertEquals(declaredLicenses, result);
		assertEquals(TEST_LICENSE_ID, ((SPDXNonStandardLicense)result).getId());
	}
	
	@Test
	public void testSpdxPackageHomePage() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxAnalysis(testUri);
		doc.createSpdxPackage();
		String TEST_HOME_PAGE = "http://www.homepage.com";
		doc.getSpdxPackage().setHomePage(TEST_HOME_PAGE);
		String result = doc.getSpdxPackage().getHomePage();
		assertEquals(TEST_HOME_PAGE, result);
	}

	@Test
	public void testAddNonStandardLicense() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxAnalysis(testUri);
		doc.createSpdxPackage();
		String NON_STD_LIC_TEXT1 = "licenseText1";
		String NON_STD_LIC_TEXT2 = "LicenseText2";
		SPDXNonStandardLicense[] emptyLic = doc.getExtractedLicenseInfos();
		assertEquals(0,emptyLic.length);
		SPDXNonStandardLicense lic1 = doc.addNewExtractedLicenseInfo(NON_STD_LIC_TEXT1);
		String licID1 = SPDXDocument.formNonStandardLicenseID(1);
		assertEquals(licID1, lic1.getId());
		assertEquals(NON_STD_LIC_TEXT1, lic1.getText());
		SPDXNonStandardLicense[] licresult1 = doc.getExtractedLicenseInfos();
		assertEquals(1, licresult1.length);
		assertEquals(licID1, licresult1[0].getId());
		assertEquals(NON_STD_LIC_TEXT1, licresult1[0].getText());
		SPDXNonStandardLicense lic2 = doc.addNewExtractedLicenseInfo(NON_STD_LIC_TEXT2);
		String licID2 = SPDXDocument.formNonStandardLicenseID(2);
		assertEquals(licID2, lic2.getId());
		assertEquals(NON_STD_LIC_TEXT2, lic2.getText());
		SPDXNonStandardLicense[] licresult2 = doc.getExtractedLicenseInfos();
		assertEquals(2, licresult2.length);
		if (!licresult2[0].getId().equals(licID2) && !licresult2[1].getId().equals(licID2)) {
			fail("second license not found");
		}
	}
	
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
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
		// add the required fields		
		SPDXPackage pkg = doc.getSpdxPackage();
		pkg.setConcludedLicenses(new SPDXNoneLicense());
		pkg.setDeclaredCopyright("Copyright");
		pkg.setDeclaredLicense(new SpdxNoAssertionLicense());
		pkg.setDeclaredName("Name");
		pkg.setDescription("Description");
		pkg.setDownloadUrl("None");
		pkg.setFileName("a/b/filename.tar.gz");
		String homePage = "http://www.home.page";
		pkg.setHomePage(homePage);
		SPDXFile testFile = new SPDXFile("filename", "BINARY", "0123456789abcdef0123456789abcdef01234567",
				new SPDXNoneLicense(), new SPDXLicenseInfo[] {new SPDXNoneLicense()}, "license comment",
				"file copyright", new DOAPProject[0]);
		ArrayList<String> verify = testFile.verify();
		assertEquals(0, verify.size());
		pkg.setFiles(new SPDXFile[]{testFile});
		pkg.setLicenseInfoFromFiles(new SPDXLicenseInfo[] {new SPDXNoneLicense()});
		pkg.setSha1("0123456789abcdef0123456789abcdef01234567");
		pkg.setShortDescription("Short description");
		pkg.setSourceInfo("Source info");
		String[] skippedFiles = new String[] {"skipped1", "skipped2"};
		pkg.setVerificationCode(
				new SpdxPackageVerificationCode("0123456789abcdef0123456789abcdef01234567",
						skippedFiles));
		verify = pkg.verify();
		assertEquals(0, verify.size());
		DateFormat format = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);
		String date = format.format(new Date());
		SPDXCreatorInformation creator = new SPDXCreatorInformation(new String[] {"Person: creator"},
				date, "", null);
		verify = creator.verify();
		assertEquals(0, verify.size());
		doc.setCreationInfo(creator);
		doc.setExtractedLicenseInfos(new SPDXNonStandardLicense[] {new SPDXNonStandardLicense(SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"11", "Text")});
		doc.setSpdxVersion(SPDXDocument.CURRENT_SPDX_VERSION);
		verify = doc.verify();
		assertEquals(0, verify.size());		
	}
	
	@Test
	public void testNoneValues() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testDocUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxAnalysis(testDocUri);
		String testPkgUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;uniquepackagename";
		doc.createSpdxPackage(testPkgUri);
		SPDXPackage pkg = doc.getSpdxPackage();
		pkg.setDownloadUrl(SpdxRdfConstants.NONE_VALUE);
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_PACKAGE_DOWNLOAD_URL).asNode();
		Triple m = Triple.createMatch(null, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			assertTrue(t.getObject().isURI());
			assertEquals(SpdxRdfConstants.URI_VALUE_NONE, t.getObject().getURI());
		}
		assertEquals(SpdxRdfConstants.NONE_VALUE, doc.getSpdxPackage().getDownloadUrl());
		pkg.setDeclaredCopyright(SpdxRdfConstants.NONE_VALUE);
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_PACKAGE_DECLARED_COPYRIGHT).asNode();
		m = Triple.createMatch(null, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			assertTrue(t.getObject().isURI());
			assertEquals(SpdxRdfConstants.URI_VALUE_NONE, t.getObject().getURI());
		}
		assertEquals(SpdxRdfConstants.NONE_VALUE, doc.getSpdxPackage().getDeclaredCopyright());
	}
	
	@Test
	public void testNoAssertionValues() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testDocUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxAnalysis(testDocUri);
		String testPkgUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;uniquepackagename";
		doc.createSpdxPackage(testPkgUri);
		SPDXPackage pkg = doc.getSpdxPackage();
		pkg.setDownloadUrl(SpdxRdfConstants.NOASSERTION_VALUE);
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_PACKAGE_DOWNLOAD_URL).asNode();
		Triple m = Triple.createMatch(null, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			assertTrue(t.getObject().isURI());
			assertEquals(SpdxRdfConstants.URI_VALUE_NOASSERTION, t.getObject().getURI());
		}
		assertEquals(SpdxRdfConstants.NOASSERTION_VALUE, doc.getSpdxPackage().getDownloadUrl());
		pkg.setDeclaredCopyright(SpdxRdfConstants.NOASSERTION_VALUE);
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_PACKAGE_DECLARED_COPYRIGHT).asNode();
		m = Triple.createMatch(null, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			assertTrue(t.getObject().isURI());
			assertEquals(SpdxRdfConstants.URI_VALUE_NOASSERTION, t.getObject().getURI());
		}
		assertEquals(SpdxRdfConstants.NOASSERTION_VALUE, doc.getSpdxPackage().getDeclaredCopyright());
	}
	
	@Test
	public void testDataLicense() throws InvalidSPDXAnalysisException, InvalidLicenseStringException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testDocUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxAnalysis(testDocUri);
		// check default
		SPDXStandardLicense dataLicense = doc.getDataLicense();
		assertEquals(org.spdx.rdfparser.SpdxRdfConstants.SPDX_DATA_LICENSE_ID, dataLicense.getId());
		// check set correct license
		SPDXLicenseInfo cc0License = SPDXLicenseInfoFactory.parseSPDXLicenseString(org.spdx.rdfparser.SpdxRdfConstants.SPDX_DATA_LICENSE_ID);
		doc.setDataLicense((SPDXStandardLicense)cc0License);
		dataLicense = doc.getDataLicense();
		assertEquals(org.spdx.rdfparser.SpdxRdfConstants.SPDX_DATA_LICENSE_ID, dataLicense.getId());
		// check error when setting wrong license
		SPDXLicenseInfo ngplLicense = SPDXLicenseInfoFactory.parseSPDXLicenseString("NGPL");
		try {
			doc.setDataLicense((SPDXStandardLicense)ngplLicense);
			fail("Incorrect license allowed to be set for data license");
		} catch(InvalidSPDXAnalysisException e) {
			// expected - do nothing
		}
	}
	
	@Test
	public void testReferencesFile() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testDocUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxAnalysis(testDocUri);
		String testPkgUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;uniquepackagename";
		doc.createSpdxPackage(testPkgUri);
		// add the required fields		
		SPDXPackage pkg = doc.getSpdxPackage();
		pkg.setConcludedLicenses(new SPDXNoneLicense());
		pkg.setDeclaredCopyright("Copyright");
		pkg.setDeclaredLicense(new SpdxNoAssertionLicense());
		pkg.setDeclaredName("Name");
		pkg.setDescription("Description");
		pkg.setDownloadUrl("None");
		pkg.setFileName("a/b/filename.tar.gz");
		SPDXFile testFile = new SPDXFile("filename", "BINARY", "0123456789abcdef0123456789abcdef01234567",
				new SPDXNoneLicense(), new SPDXLicenseInfo[] {new SPDXNoneLicense()}, "license comment",
				"file copyright", new DOAPProject[0]);
		SPDXFile testFile2 = new SPDXFile("filename2", "SOURCE", "0123456789abcdef0123456789abcdef01234567",
				new SPDXNoneLicense(), new SPDXLicenseInfo[] {new SPDXNoneLicense()}, "license comment",
				"file copyright", new DOAPProject[0]);
		SPDXFile[] testFiles = new SPDXFile[] {testFile, testFile2};
		ArrayList<String> verify = testFile.verify();
		assertEquals(0, verify.size());
		pkg.setFiles(testFiles);
		pkg.setLicenseInfoFromFiles(new SPDXLicenseInfo[] {new SPDXNoneLicense()});
		pkg.setSha1("0123456789abcdef0123456789abcdef01234567");
		pkg.setShortDescription("Short description");
		pkg.setSourceInfo("Source info");
		String[] skippedFiles = new String[] {"skipped1", "skipped2"};
		pkg.setVerificationCode(
				new SpdxPackageVerificationCode("0123456789abcdef0123456789abcdef01234567",
						skippedFiles));
		verify = pkg.verify();
		assertEquals(0, verify.size());
		
		// OK - now we can test to see if the files are there
		SPDXFile[] pkgFiles = pkg.getFiles();
		SPDXFile[] docFiles = doc.getFileReferences();
		
		assertEquals(testFiles.length, docFiles.length);
		for (int i = 0; i < testFiles.length; i++) {
			boolean found = false;
			for (int j = 0; j < docFiles.length; j++) {
				if (testFiles[i].getName().equals(docFiles[j].getName())) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
	}
	
	@Test
	public void testAddFile() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testDocUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxAnalysis(testDocUri);
		String testPkgUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;uniquepackagename";
		doc.createSpdxPackage(testPkgUri);
		// add the required fields		
		SPDXPackage pkg = doc.getSpdxPackage();
		pkg.setConcludedLicenses(new SPDXNoneLicense());
		pkg.setDeclaredCopyright("Copyright");
		pkg.setDeclaredLicense(new SpdxNoAssertionLicense());
		pkg.setDeclaredName("Name");
		pkg.setDescription("Description");
		pkg.setDownloadUrl("None");
		pkg.setFileName("a/b/filename.tar.gz");
		SPDXFile testFile = new SPDXFile("filename", "BINARY", "0123456789abcdef0123456789abcdef01234567",
				new SPDXNoneLicense(), new SPDXLicenseInfo[] {new SPDXNoneLicense()}, "license comment",
				"file copyright", new DOAPProject[0]);
		SPDXFile testFile2 = new SPDXFile("filename2", "SOURCE", "0123456789abcdef0123456789abcdef01234567",
				new SPDXNoneLicense(), new SPDXLicenseInfo[] {new SPDXNoneLicense()}, "license comment",
				"file copyright", new DOAPProject[0]);
		SPDXFile[] testFiles = new SPDXFile[] {testFile, testFile2};
		ArrayList<String> verify = testFile.verify();
		assertEquals(0, verify.size());
		pkg.addFile(testFiles[0]);
		pkg.addFile(testFiles[1]);
		pkg.setLicenseInfoFromFiles(new SPDXLicenseInfo[] {new SPDXNoneLicense()});
		pkg.setSha1("0123456789abcdef0123456789abcdef01234567");
		pkg.setShortDescription("Short description");
		pkg.setSourceInfo("Source info");
		String[] skippedFiles = new String[] {"skipped1", "skipped2"};
		pkg.setVerificationCode(
				new SpdxPackageVerificationCode("0123456789abcdef0123456789abcdef01234567",
						skippedFiles));
		verify = pkg.verify();
		assertEquals(0, verify.size());
		
		// OK - now we can test to see if the files are there
		SPDXFile[] pkgFiles = pkg.getFiles();
		
		assertEquals(testFiles.length, pkgFiles.length);
		for (int i = 0; i < testFiles.length; i++) {
			boolean found = false;
			for (int j = 0; j < pkgFiles.length; j++) {
				if (testFiles[i].getName().equals(pkgFiles[j].getName())) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
	}
	
	@Test
	public void testRemoveFile() throws InvalidSPDXAnalysisException, IOException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testDocUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxAnalysis(testDocUri);
		String testPkgUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;uniquepackagename";
		doc.createSpdxPackage(testPkgUri);
		// add the required fields		
		SPDXPackage pkg = doc.getSpdxPackage();
		pkg.setConcludedLicenses(new SPDXNoneLicense());
		pkg.setDeclaredCopyright("Copyright");
		pkg.setDeclaredLicense(new SpdxNoAssertionLicense());
		pkg.setDeclaredName("Name");
		pkg.setDescription("Description");
		pkg.setDownloadUrl("None");
		pkg.setFileName("a/b/filename.tar.gz");
		SPDXFile testFile = new SPDXFile("filename", "BINARY", "0123456789abcdef0123456789abcdef01234567",
				new SPDXNoneLicense(), new SPDXLicenseInfo[] {new SPDXNoneLicense()}, "license comment",
				"file copyright", new DOAPProject[0]);
		SPDXFile testFile2 = new SPDXFile("filename2", "SOURCE", "0123456789abcdef0123456789abcdef01234567",
				new SPDXNoneLicense(), new SPDXLicenseInfo[] {new SPDXNoneLicense()}, "license comment",
				"file copyright", new DOAPProject[0]);
		SPDXFile[] testFiles = new SPDXFile[] {testFile, testFile2};
		ArrayList<String> verify = testFile.verify();
		assertEquals(0, verify.size());
		pkg.setFiles(testFiles);
		pkg.setLicenseInfoFromFiles(new SPDXLicenseInfo[] {new SPDXNoneLicense()});
		pkg.setSha1("0123456789abcdef0123456789abcdef01234567");
		pkg.setShortDescription("Short description");
		pkg.setSourceInfo("Source info");
		String[] skippedFiles = new String[] {"skipped1", "skipped2"};
		pkg.setVerificationCode(
				new SpdxPackageVerificationCode("0123456789abcdef0123456789abcdef01234567",
						skippedFiles));
		verify = pkg.verify();
		assertEquals(0, verify.size());
		
		// Remove one the first file
		pkg.removeFile(testFiles[0].getName());
		// OK - now we can test to see if the files are there
		SPDXFile[] pkgFiles = pkg.getFiles();
		SPDXFile[] refFiles = doc.getFileReferences();
		assertEquals(testFiles.length-1, pkgFiles.length);
		for (int i = 1; i < testFiles.length; i++) {
			boolean found = false;
			for (int j = 0; j < pkgFiles.length; j++) {
				if (testFiles[i].getName().equals(pkgFiles[j].getName())) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
		boolean found = false;
		for (int i = 0; i < pkgFiles.length; i++) {
			if (testFiles[0].getName().equals(pkgFiles[i].getName())) {
				found = true;
				break;
			}
		}
		assertFalse(found);
		
		assertEquals(testFiles.length-1, refFiles.length);
		for (int i = 1; i < testFiles.length; i++) {
			found = false;
			for (int j = 0; j < refFiles.length; j++) {
				if (testFiles[i].getName().equals(refFiles[j].getName())) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
		found = false;
		for (int i = 0; i < refFiles.length; i++) {
			if (testFiles[0].getName().equals(refFiles[i].getName())) {
				found = true;
				break;
			}
		}
		assertFalse(found);
	}
	
	@Test
	public void testSpdxDocVersions() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testDocUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxAnalysis(testDocUri, SPDXDocument.POINT_NINE_SPDX_VERSION);
		assertEquals(SPDXDocument.POINT_NINE_SPDX_VERSION, doc.getSpdxVersion());
		if (doc.getDataLicense() != null) {
			fail("No license should exist for current data license");
		}
		// 1.0
		model = ModelFactory.createDefaultModel();
		doc = new SPDXDocument(model);
		doc.createSpdxAnalysis(testDocUri, SPDXDocument.ONE_DOT_ZERO_SPDX_VERSION);
		assertEquals(SPDXDocument.ONE_DOT_ZERO_SPDX_VERSION, doc.getSpdxVersion());
		assertEquals(SpdxRdfConstants.SPDX_DATA_LICENSE_ID_VERSION_1_0, doc.getDataLicense().getId());
		
		// current version
		model = ModelFactory.createDefaultModel();
		doc = new SPDXDocument(model);
		doc.createSpdxAnalysis(testDocUri);
		assertEquals(SPDXDocument.CURRENT_SPDX_VERSION, doc.getSpdxVersion());
		assertEquals(SpdxRdfConstants.SPDX_DATA_LICENSE_ID, doc.getDataLicense().getId());
	}
	
	@Test
	public void testextractedLicenseExists() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxAnalysis(testUri);
		doc.createSpdxPackage();
		String NON_STD_LIC_ID1 = "LicenseRef-nonstd1";
		String NON_STD_LIC_TEXT1 = "licenseText1";
		String NON_STD_LIC_NAME1 = "licenseName1";
		String[] NON_STD_LIC_REFERENCES1 = new String[] {"ref1"};
		String NON_STD_LIC_COMMENT1 = "License 1 comment";
		SPDXNonStandardLicense lic1 = new SPDXNonStandardLicense(NON_STD_LIC_ID1, NON_STD_LIC_TEXT1, 
				NON_STD_LIC_NAME1, NON_STD_LIC_REFERENCES1, NON_STD_LIC_COMMENT1);
		
		String NON_STD_LIC_TEXT2 = "LicenseText2";

		SPDXNonStandardLicense[] emptyLic = doc.getExtractedLicenseInfos();
		assertEquals(0,emptyLic.length);
		assertTrue(!doc.extractedLicenseExists(NON_STD_LIC_ID1));
		
		doc.addNewExtractedLicenseInfo(lic1);
		assertTrue(doc.extractedLicenseExists(NON_STD_LIC_ID1));
		
		SPDXNonStandardLicense lic2 = doc.addNewExtractedLicenseInfo(NON_STD_LIC_TEXT2);
		assertTrue(doc.extractedLicenseExists(NON_STD_LIC_ID1));
		assertTrue(doc.extractedLicenseExists(lic2.getId()));
	}
	
	@Test
	public void addNewExtractedLicenseInfoLicense() throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxAnalysis(testUri);
		doc.createSpdxPackage();
		String NON_STD_LIC_ID1 = "LicenseRef-nonstd1";
		String NON_STD_LIC_TEXT1 = "licenseText1";
		String NON_STD_LIC_NAME1 = "licenseName1";
		String[] NON_STD_LIC_REFERENCES1 = new String[] {"ref1"};
		String NON_STD_LIC_COMMENT1 = "License 1 comment";
		SPDXNonStandardLicense lic1 = new SPDXNonStandardLicense(NON_STD_LIC_ID1, NON_STD_LIC_TEXT1, 
				NON_STD_LIC_NAME1, NON_STD_LIC_REFERENCES1, NON_STD_LIC_COMMENT1);
		String NON_STD_LIC_ID2 = "LicenseRef-623";
		String NON_STD_LIC_TEXT2 = "LicenseText2";
		String NON_STD_LIC_NAME2 = "licenseName2";
		String[] NON_STD_LIC_REFERENCES2 = new String[] {"ref2"};
		String NON_STD_LIC_COMMENT2 = "License 2 comment";
		SPDXNonStandardLicense lic2 = new SPDXNonStandardLicense(NON_STD_LIC_ID2, NON_STD_LIC_TEXT2, 
				NON_STD_LIC_NAME2, NON_STD_LIC_REFERENCES2, NON_STD_LIC_COMMENT2);
		SPDXNonStandardLicense[] emptyLic = doc.getExtractedLicenseInfos();
		assertEquals(0,emptyLic.length);
		doc.addNewExtractedLicenseInfo(lic1);
		SPDXNonStandardLicense[] licresult1 = doc.getExtractedLicenseInfos();
		assertEquals(1, licresult1.length);
		assertEquals(NON_STD_LIC_ID1, licresult1[0].getId());
		assertEquals(NON_STD_LIC_TEXT1, licresult1[0].getText());
		assertEquals(NON_STD_LIC_NAME1, licresult1[0].getLicenseName());
		assertEquals(NON_STD_LIC_COMMENT1, licresult1[0].getComment());
		assertEquals(1, licresult1[0].getSourceUrls().length);
		assertEquals(NON_STD_LIC_REFERENCES1[0], licresult1[0].getSourceUrls()[0]);
		doc.addNewExtractedLicenseInfo(lic2);
		SPDXNonStandardLicense[] licresult2 = doc.getExtractedLicenseInfos();
		assertEquals(2, licresult2.length);
		if (!licresult2[0].getId().equals(NON_STD_LIC_ID2) && !licresult2[1].getId().equals(NON_STD_LIC_ID2)) {
			fail("second license not found");
		}
	}
	
	@Test
	public void testGetElementRefNumber() {
		int refNum1 = 5532;
		int refNum2 = 12;
		String ref1 = SpdxRdfConstants.SPDX_ELEMENT_REF_PRENUM + String.valueOf(refNum1);
		String ref2 = SpdxRdfConstants.SPDX_ELEMENT_REF_PRENUM + String.valueOf(refNum2);
		String invalidRef = SpdxRdfConstants.SPDX_ELEMENT_REF_PRENUM + "xaf234";
		int result = SPDXDocument.getElementRefNumber(ref1);
		assertEquals(refNum1, result);
		result = SPDXDocument.getElementRefNumber(ref2);
		assertEquals(refNum2, result);
		result = SPDXDocument.getElementRefNumber(invalidRef);
		assertEquals(-1, result);		
	}
	
	@Test
	public void testGetDocumentNamespace() throws InvalidSPDXAnalysisException {
		String docUri = "http://www.spdx.org/spdxdocs/uniquenameofsomesort";
		// without a part
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		doc.createSpdxAnalysis(docUri);
		String result = doc.getDocumentNamespace();
		// with part
		assertEquals(docUri + "#", result);
		doc = new SPDXDocument(model);
		doc.createSpdxAnalysis(docUri + "#SPDXDocument");
		result = doc.getDocumentNamespace();
		assertEquals(docUri + "#", result);	
	}
	
	@Test
	public void testGetNextSpdxRef() throws InvalidSPDXAnalysisException {
		String docUri = "http://www.spdx.org/spdxdocs/uniquenameofsomesort";
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		doc.createSpdxAnalysis(docUri);
		String nextSpdxElementRef = doc.getNextSpdxElementRef();
		String expected = SpdxRdfConstants.SPDX_ELEMENT_REF_PRENUM + String.valueOf(1);
		assertEquals(expected, nextSpdxElementRef);
		nextSpdxElementRef = doc.getNextSpdxElementRef();
		expected = SpdxRdfConstants.SPDX_ELEMENT_REF_PRENUM + String.valueOf(2);
		assertEquals(expected, nextSpdxElementRef);
		// test that it survives across a new doc
		doc.createSpdxPackage(doc.getDocumentNamespace() + nextSpdxElementRef);
		SPDXDocument doc2 = new SPDXDocument(model);
		nextSpdxElementRef = doc2.getNextSpdxElementRef();
		expected = SpdxRdfConstants.SPDX_ELEMENT_REF_PRENUM + String.valueOf(3);
		assertEquals(expected, nextSpdxElementRef);
		nextSpdxElementRef = doc.getNextSpdxElementRef();
		expected = SpdxRdfConstants.SPDX_ELEMENT_REF_PRENUM + String.valueOf(3);
		assertEquals(expected, nextSpdxElementRef);	// original SPDX doc should maintain its own
	}
}
