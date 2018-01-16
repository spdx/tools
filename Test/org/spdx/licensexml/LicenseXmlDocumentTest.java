/**
 * Copyright (c) 2016 Source Auditor Inc.
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
package org.spdx.licensexml;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.html.InvalidLicenseTemplateException;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.LicenseException;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.model.UnitTestHelper;
import org.spdx.spdxspreadsheet.SPDXLicenseSpreadsheet.DeprecatedLicenseInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


/**
 * @author Gary O'Neall
 *
 */
public class LicenseXmlDocumentTest {
	
	static final String TEST_FILE_PATH = "TestFiles" + File.separator + "test-license.xml";
	private static final String TEST_LICENSE_COMMENT = "Test note";
	private static final String TEST_LICENSE_ID = "test-id";
	private static final String TEST_LICENSE_TEXT = "Test Copyright\nparagraph 1" +
			"\n   1.\n   List item 1\n   2.\n   List item 2\n" +
			"Last Paragraph Alternate Text Non matching line. Optional text";
	private static final String TEST_LICENSE_NAME = "Test License";
	private static final String[] TEST_LICENSE_URLS = new String[] {"http://test/url1","http://test/url2"};
	private static final String TEST_LICENSE_HEADER = "Test header optional var";
	private static final String TEST_LICENSE_HEADER_TEMPLATE = "Test header<<beginOptional>> optional<<endOptional>> <<var;name=\"h1test\";original=\"var\";match=\".+\">>";
	private static final String TEST_LICENSE_TEMPLATE = "Test Copyright\nparagraph 1" +
			"\n   <<var;name=\"bullet\";original=\"1.\";match=\".{0,20}\">>\n   List item 1\n   <<var;name=\"bullet\";original=\"2.\";match=\".{0,20}\">>\n   List item 2\n" +
			"Last Paragraph <<var;name=\"alttest\";original=\"Alternate Text\";match=\".+\">> Non matching line.<<beginOptional>> Optional text<<endOptional>>";

	private static final String TEST_DEP_LICENSE_COMMENT = "Test dep note";
	private static final String TEST_DEP_LICENSE_ID = "test-dep";
	private static final String TEST_DEP_LICENSE_TEXT = "Test Copyright dep\nparagraph 1d" +
			"\n   1.d\n   List item 1d\n   2.d\n   List item 2d\n" +
			"Last Paragraph dep Alternate Text dep Non matching line dep. Optional text dep";
	private static final String TEST_DEP_LICENSE_NAME = "Test Deprecated License";
	private static final String[] TEST_DEP_LICENSE_URLS = new String[] {"http://test/url1d","http://test/url2d"};
	private static final String TEST_DEP_LICENSE_HEADER = "Test header dep";
	private static final String TEST_DEP_LICENSE_TEMPLATE = "Test Copyright dep\nparagraph 1d" +
			"\n   <<var;name=\"bullet\";original=\"1.d\";match=\".{0,20}\">>\n   List item 1d\n   <<var;name=\"bullet\";original=\"2.d\";match=\".{0,20}\">>\n   List item 2d\n" +
			"Last Paragraph dep <<var;name=\"alttestd\";original=\"Alternate Text dep\";match=\".+\">> Non matching line dep.<<beginOptional>> Optional text dep<<endOptional>>";

	private static final String TEST_EXCEPTION_COMMENT = "Test note exception";
	private static final String TEST_EXCEPTION_ID = "test-ex";
	private static final String TEST_EXCEPTION_TEXT = "Test Copyrighte\nparagraph 1e" +
			"\n   1.e\n   List item 1e\n   2.e\n   List item 2e\n" +
			"Last Paragraph exc Alternate Text exc Non matching line. e Optional text exc";
	private static final String TEST_EXCEPTION_NAME = "Test Exception";
	private static final String[] TEST_EXCEPTION_URLS = new String[] {"http://test/url1e","http://test/url2e"};
	@SuppressWarnings("unused")
	private static final String TEST_EXCEPTION_TEMPLATE = "Test Copyrighte\nparagraph 1e" +
			"\n   1.e\n   List item 1e\n   2.e\n   List item 2e\n" +
			"Last Paragraph exc <<var;name=\"altteste\";original=\"Alternate Text exc\";match=\".+\">> Non matching line. e<<beginOptional>> Optional text exc<<endOptional>>";
	private static final String TEST_DEP_LICENSE_VERSION = "2.2";
	private static final String AGPL3ONLY_FILE_PATH = "TestFiles" + File.separator + "AGPL-3.0-only.xml";

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
	 * Test method for {@link org.spdx.licensexml.LicenseXmlDocument#LicenseXmlDocument(java.io.File)}.
	 */
	@Test
	public void testLicenseXmlDocumentFile() throws Exception {
		File licenseFile = new File(TEST_FILE_PATH);
		new LicenseXmlDocument(licenseFile);
		// I guess if we don't get any exceptions, it passed
	}

	/**
	 * Test method for {@link org.spdx.licensexml.LicenseXmlDocument#getListedLicense()}.
	 * @throws LicenseXmlException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetListedLicense() throws LicenseXmlException, InvalidSPDXAnalysisException {
		File licenseFile = new File(TEST_FILE_PATH);
		LicenseXmlDocument doc = new LicenseXmlDocument(licenseFile);
		List<SpdxListedLicense> licenses = doc.getListedLicenses();
		assertEquals(1, licenses.size());
		SpdxListedLicense license = licenses.get(0);
		assertTrue(license.isOsiApproved());
		assertEquals(TEST_LICENSE_COMMENT, license.getComment());
		assertEquals(TEST_LICENSE_ID, license.getLicenseId());
		assertEquals(TEST_LICENSE_TEXT, license.getLicenseText());
		assertEquals(TEST_LICENSE_NAME, license.getName());
		UnitTestHelper.isArraysEqual(TEST_LICENSE_URLS, license.getSeeAlso());
		assertEquals(TEST_LICENSE_HEADER, license.getStandardLicenseHeader());
		assertEquals(TEST_LICENSE_HEADER_TEMPLATE, license.getStandardLicenseHeaderTemplate());
		assertEquals(TEST_LICENSE_TEMPLATE, license.getStandardLicenseTemplate());
	}

	/**
	 * Test method for {@link org.spdx.licensexml.LicenseXmlDocument#getLicenseException()}.
	 * @throws LicenseXmlException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetLicenseException() throws LicenseXmlException, InvalidSPDXAnalysisException {
		File licenseFile = new File(TEST_FILE_PATH);
		LicenseXmlDocument doc = new LicenseXmlDocument(licenseFile);
		List<LicenseException> exceptions = doc.getLicenseExceptions();
		assertEquals(1, exceptions.size());
		LicenseException exception = exceptions.get(0);
		assertEquals(TEST_EXCEPTION_COMMENT, exception.getComment());
		assertEquals(TEST_EXCEPTION_ID, exception.getLicenseExceptionId());
		assertEquals(TEST_EXCEPTION_TEXT, exception.getLicenseExceptionText());
		assertEquals(TEST_EXCEPTION_NAME, exception.getName());
		UnitTestHelper.isArraysEqual(TEST_EXCEPTION_URLS, exception.getSeeAlso());
	}

	/**
	 * Test method for {@link org.spdx.licensexml.LicenseXmlDocument#getDeprecatedLicenseInfo()}.
	 * @throws LicenseXmlException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetDeprecatedLicenseInfo() throws LicenseXmlException, InvalidSPDXAnalysisException {
		File licenseFile = new File(TEST_FILE_PATH);
		LicenseXmlDocument doc = new LicenseXmlDocument(licenseFile);
		List<DeprecatedLicenseInfo> licenses = doc.getDeprecatedLicenseInfos();
		assertEquals(1, licenses.size());
		DeprecatedLicenseInfo deprecatedLicenseInfo = licenses.get(0);
		SpdxListedLicense license = deprecatedLicenseInfo.getLicense();
		assertEquals(TEST_DEP_LICENSE_VERSION,deprecatedLicenseInfo.getDeprecatedVersion());
		assertFalse(license.isOsiApproved());
		assertEquals(TEST_DEP_LICENSE_COMMENT, license.getComment());
		assertEquals(TEST_DEP_LICENSE_ID, license.getLicenseId());
		assertEquals(TEST_DEP_LICENSE_TEXT, license.getLicenseText());
		assertEquals(TEST_DEP_LICENSE_NAME, license.getName());
		UnitTestHelper.isArraysEqual(TEST_DEP_LICENSE_URLS, license.getSeeAlso());
		assertEquals(TEST_DEP_LICENSE_HEADER, license.getStandardLicenseHeader());
		assertEquals(TEST_DEP_LICENSE_TEMPLATE, license.getStandardLicenseTemplate());
	}

	@Test
	public void testParserBehavior() throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		File file = new File(TEST_FILE_PATH);
		Document doc = builder.parse(file);
		String result = LicenseXmlHelper.dumpLicenseDom((Element) doc.getDocumentElement().getElementsByTagName("license").item(0));
		assertTrue(result.length() > 0);
	}
	
	@Test
	public void testRegressionAgpl3Only() throws LicenseXmlException, InvalidSPDXAnalysisException, InvalidLicenseTemplateException {
		File licenseFile = new File(AGPL3ONLY_FILE_PATH);
		LicenseXmlDocument doc = new LicenseXmlDocument(licenseFile);
		List<SpdxListedLicense> licenses = doc.getListedLicenses();
		assertEquals(1, licenses.size());
		SpdxListedLicense license = licenses.get(0);
		String licenseTextHtml = license.getLicenseTextHtml();
		String licenseHeaderHtml = license.getLicenseHeaderHtml();
	}
}
