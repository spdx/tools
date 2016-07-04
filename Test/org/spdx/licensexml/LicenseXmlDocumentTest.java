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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.model.UnitTestHelper;
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
	private static final String TEST_LICENSE_HEADER = "Test header";
	private static final String TEST_LICENSE_TEMPLATE = "Test Copyright\nparagraph 1" +
			"\n   1.\n   List item 1\n   2.\n   List item 2\n" +
			"Last Paragraph <<var;name=\"alttest\";original=\"Alternate Text\";match=\".+\">> Non matching line.<<beginOptional>> Optional text<<endOptional>>";

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
	 * Test method for {@link org.spdx.licensexml.LicenseXmlDocument#isListedLicense()}.
	 */
	@Test
	public void testIsListedLicense() {
		//TODO Implement test
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
		SpdxListedLicense license = doc.getListedLicense();
		assertTrue(license.isOsiApproved());
		assertEquals(TEST_LICENSE_COMMENT, license.getComment());
		assertEquals(TEST_LICENSE_ID, license.getLicenseId());
		assertEquals(TEST_LICENSE_TEXT, license.getLicenseText());
		assertEquals(TEST_LICENSE_NAME, license.getName());
		UnitTestHelper.isArraysEqual(TEST_LICENSE_URLS, license.getSeeAlso());
		assertEquals(TEST_LICENSE_HEADER, license.getStandardLicenseHeader());
		assertEquals(TEST_LICENSE_TEMPLATE, license.getStandardLicenseTemplate());
	}

	/**
	 * Test method for {@link org.spdx.licensexml.LicenseXmlDocument#isLicenseException()}.
	 */
	@Test
	public void testIsLicenseException() {
		//TODO Implement test
	}

	/**
	 * Test method for {@link org.spdx.licensexml.LicenseXmlDocument#getLicenseException()}.
	 */
	@Test
	public void testGetLicenseException() {
		//TODO Implement test
	}

	/**
	 * Test method for {@link org.spdx.licensexml.LicenseXmlDocument#isDeprecated()}.
	 */
	@Test
	public void testIsDeprecated() {
		//TODO Implement test
	}

	/**
	 * Test method for {@link org.spdx.licensexml.LicenseXmlDocument#getDeprecatedLicenseInfo()}.
	 */
	@Test
	public void testGetDeprecatedLicenseInfo() {
		//TODO Implement test
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
}
