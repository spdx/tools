/**
 * Copyright (c) 2015 Source Auditor Inc.
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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Gary
 *
 */
public class TestOrLaterOperator {
	static final String LICENSE_ID1 = "LicenseRef-1";
	static final String LICENSE_TEXT1 = "licenseText";
	static final String LICENSE_ID2 = "LicenseRef-2";
	static final String LICENSE_TEXT2 = "Second licenseText";
	private SimpleLicensingInfo license1;
	private SimpleLicensingInfo license2;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		license1 = new ExtractedLicenseInfo(LICENSE_ID1, LICENSE_TEXT1);
		license2 = new ExtractedLicenseInfo(LICENSE_ID2, LICENSE_TEXT2);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.OrLaterOperator#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		SimpleLicensingInfo sameLicId = new ExtractedLicenseInfo(LICENSE_ID1, "different text");
		OrLaterOperator olo1 = new OrLaterOperator(license1);
		OrLaterOperator olo2 = new OrLaterOperator(license2);
		OrLaterOperator olo3 = new OrLaterOperator(sameLicId);
		assertFalse(olo1.hashCode() == olo2.hashCode());
		assertTrue(olo1.hashCode() == olo3.hashCode());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.OrLaterOperator#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		SimpleLicensingInfo sameLicId = new ExtractedLicenseInfo(LICENSE_ID1, "different text");
		OrLaterOperator olo1 = new OrLaterOperator(license1);
		OrLaterOperator olo2 = new OrLaterOperator(license2);
		OrLaterOperator olo3 = new OrLaterOperator(sameLicId);
		assertFalse(olo1.equals(olo2));
		assertTrue(olo1.equals(olo3));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.OrLaterOperator#verify()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException {
		OrLaterOperator olo1 = new OrLaterOperator(license1);
		assertEquals(0, olo1.verify().size());
		olo1.setLicense(null);
		assertEquals(1, olo1.verify().size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.OrLaterOperator#clone()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		OrLaterOperator olo1 = new OrLaterOperator(license1);
		Model model = ModelFactory.createDefaultModel();
		olo1.createResource(model);
		OrLaterOperator clone = (OrLaterOperator)olo1.clone();
		ExtractedLicenseInfo lic1 = (ExtractedLicenseInfo)olo1.getLicense();
		ExtractedLicenseInfo lic1FromClone = (ExtractedLicenseInfo)clone.getLicense();
		assertEquals(lic1.getLicenseId(), lic1FromClone.getLicenseId());
		assertEquals(lic1.getExtractedText(), lic1FromClone.getExtractedText());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.OrLaterOperator#setLicense(org.spdx.rdfparser.license.SimpleLicensingInfo)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetLicense() throws InvalidSPDXAnalysisException {
		OrLaterOperator olo1 = new OrLaterOperator(license1);
		Model model = ModelFactory.createDefaultModel();
		olo1.createResource(model);
		ExtractedLicenseInfo lic1 = (ExtractedLicenseInfo)olo1.getLicense();
		assertEquals(LICENSE_ID1, lic1.getLicenseId());
		assertEquals(LICENSE_TEXT1, lic1.getExtractedText());
		olo1.setLicense(license2);
		lic1 = (ExtractedLicenseInfo)olo1.getLicense();
		assertEquals(LICENSE_ID2, lic1.getLicenseId());
		assertEquals(LICENSE_TEXT2, lic1.getExtractedText());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.AnyLicenseInfo#createResource(com.hp.hpl.jena.rdf.model.Model)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testCreateResource() throws InvalidSPDXAnalysisException {
		OrLaterOperator olo1 = new OrLaterOperator(license1);
		Model model = ModelFactory.createDefaultModel();
		Resource r = olo1.createResource(model);
		OrLaterOperator comp = new OrLaterOperator(model, r.asNode());
		ExtractedLicenseInfo lic1 = (ExtractedLicenseInfo)olo1.getLicense();
		assertEquals(LICENSE_ID1, lic1.getLicenseId());
		assertEquals(LICENSE_TEXT1, lic1.getExtractedText());

		ExtractedLicenseInfo compLic = (ExtractedLicenseInfo)comp.getLicense();
		assertEquals(LICENSE_ID1, compLic.getLicenseId());
		assertEquals(LICENSE_TEXT1, compLic.getExtractedText());
	}

}
