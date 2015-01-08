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
 * @author Gary O'Neall
 *
 */
public class TestLicenseException {

	static final String EXCEPTION_ID1 = "id1";
	static final String EXCEPTION_NAME1 = "name1";
	static final String EXCEPTION_TEXT1 = "exception text one";
	static final String[] EXCEPTION_SEEALSO1 = new String[] {"http://url1", "http://url2"};
	static final String EXCEPTION_COMMENT1 = "comment1";
	static final String EXCEPTION_EXAMPLE1 = "example1";
	static final String EXCEPTION_ID2 = "id2";
	static final String EXCEPTION_NAME2 = "name2";
	static final String EXCEPTION_TEXT2 = "exception text two";
	static final String[] EXCEPTION_SEEALSO2 = new String[] {"http://url3"};
	static final String EXCEPTION_COMMENT2 = "comment2";
	static final String EXCEPTION_EXAMPLE2 = "example2";
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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testLicenseException() {
		LicenseException le = new LicenseException(EXCEPTION_ID1,
				EXCEPTION_NAME1, EXCEPTION_TEXT1, EXCEPTION_SEEALSO1,
				EXCEPTION_COMMENT1, EXCEPTION_EXAMPLE1);
		assertEquals(EXCEPTION_ID1, le.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le.getName());
		assertEquals(EXCEPTION_TEXT1, le.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le.getExample());
	}
	
	@Test
	public void testCreateResource() throws InvalidSPDXAnalysisException {
		LicenseException le = new LicenseException(EXCEPTION_ID1,
				EXCEPTION_NAME1, EXCEPTION_TEXT1, EXCEPTION_SEEALSO1,
				EXCEPTION_COMMENT1, EXCEPTION_EXAMPLE1);
		Model model = ModelFactory.createDefaultModel();
		Resource leResource = le.createResource(model, null);
		LicenseException le2 = new LicenseException(model, leResource.asNode());
		assertEquals(EXCEPTION_ID1, le2.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le2.getName());
		assertEquals(EXCEPTION_TEXT1, le2.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le2.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le2.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le2.getExample());
		
		model = ModelFactory.createDefaultModel();
		String licenseUri = "http://spdx.org/licenses/exceptions#ex1";
		LicenseException le3 = new LicenseException(EXCEPTION_ID1,
				EXCEPTION_NAME1, EXCEPTION_TEXT1, EXCEPTION_SEEALSO1,
				EXCEPTION_COMMENT1, EXCEPTION_EXAMPLE1);
		Resource leResourceUri = le3.createResource(model, licenseUri);
		assertEquals(licenseUri, leResourceUri.getURI());
		LicenseException le4 = new LicenseException(model, leResourceUri.asNode());
		assertEquals(EXCEPTION_ID1, le4.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le4.getName());
		assertEquals(EXCEPTION_TEXT1, le4.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le4.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le4.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le4.getExample());
	}
	
	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		LicenseException le = new LicenseException(EXCEPTION_ID1,
				EXCEPTION_NAME1, EXCEPTION_TEXT1, EXCEPTION_SEEALSO1,
				EXCEPTION_COMMENT1, EXCEPTION_EXAMPLE1);
		LicenseException le2 = le.clone();
		assertEquals(EXCEPTION_ID1, le2.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le2.getName());
		assertEquals(EXCEPTION_TEXT1, le2.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le2.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le2.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le2.getExample());
		Model model = ModelFactory.createDefaultModel();
		le.createResource(model, null);
		LicenseException le3 = le.clone();
		assertEquals(EXCEPTION_ID1, le3.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le3.getName());
		assertEquals(EXCEPTION_TEXT1, le3.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le3.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le3.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le3.getExample());	
	}
	
	@Test
	public void testEquals() {
		LicenseException le = new LicenseException(EXCEPTION_ID1,
				EXCEPTION_NAME1, EXCEPTION_TEXT1, EXCEPTION_SEEALSO1,
				EXCEPTION_COMMENT1, EXCEPTION_EXAMPLE1);
		LicenseException le2 = new LicenseException(EXCEPTION_ID1,
				EXCEPTION_NAME2, EXCEPTION_TEXT2, EXCEPTION_SEEALSO2,
				EXCEPTION_COMMENT2, EXCEPTION_EXAMPLE2);
		LicenseException le3 = new LicenseException(EXCEPTION_ID2,
				EXCEPTION_NAME1, EXCEPTION_TEXT1, EXCEPTION_SEEALSO1,
				EXCEPTION_COMMENT1, EXCEPTION_EXAMPLE1);
		assertTrue(le.equals(le2));
		assertFalse(le.equals(le3));
	}
	
	@Test
	public void testSetComment() throws InvalidSPDXAnalysisException {
		LicenseException le = new LicenseException(EXCEPTION_ID1,
				EXCEPTION_NAME1, EXCEPTION_TEXT1, EXCEPTION_SEEALSO1,
				EXCEPTION_COMMENT1, EXCEPTION_EXAMPLE1);
		assertEquals(EXCEPTION_ID1, le.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le.getName());
		assertEquals(EXCEPTION_TEXT1, le.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le.getExample());
		le.setComment(EXCEPTION_COMMENT2);
		assertEquals(EXCEPTION_ID1, le.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le.getName());
		assertEquals(EXCEPTION_TEXT1, le.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT2, le.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le.getExample());
		Model model = ModelFactory.createDefaultModel();
		Resource leResource = le.createResource(model, null);
		LicenseException le2 = new LicenseException(model, leResource.asNode());
		assertEquals(EXCEPTION_ID1, le2.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le2.getName());
		assertEquals(EXCEPTION_TEXT1, le2.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le2.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT2, le2.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le2.getExample());
		le2.setComment(EXCEPTION_COMMENT1);
		assertEquals(EXCEPTION_ID1, le2.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le2.getName());
		assertEquals(EXCEPTION_TEXT1, le2.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le2.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le2.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le2.getExample());
	}
	
	@Test
	public void testSetExample() throws InvalidSPDXAnalysisException {
		LicenseException le = new LicenseException(EXCEPTION_ID1,
				EXCEPTION_NAME1, EXCEPTION_TEXT1, EXCEPTION_SEEALSO1,
				EXCEPTION_COMMENT1, EXCEPTION_EXAMPLE1);
		assertEquals(EXCEPTION_ID1, le.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le.getName());
		assertEquals(EXCEPTION_TEXT1, le.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le.getExample());
		le.setExample(EXCEPTION_EXAMPLE2);
		assertEquals(EXCEPTION_ID1, le.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le.getName());
		assertEquals(EXCEPTION_TEXT1, le.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le.getComment());
		assertEquals(EXCEPTION_EXAMPLE2, le.getExample());
		Model model = ModelFactory.createDefaultModel();
		Resource leResource = le.createResource(model, null);
		LicenseException le2 = new LicenseException(model, leResource.asNode());
		assertEquals(EXCEPTION_ID1, le2.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le2.getName());
		assertEquals(EXCEPTION_TEXT1, le2.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le2.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le2.getComment());
		assertEquals(EXCEPTION_EXAMPLE2, le2.getExample());
		le2.setExample(EXCEPTION_EXAMPLE1);
		assertEquals(EXCEPTION_ID1, le2.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le2.getName());
		assertEquals(EXCEPTION_TEXT1, le2.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le2.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le2.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le2.getExample());
	}
	
	@Test
	public void testSetLicenseExceptionId() throws InvalidSPDXAnalysisException {
		LicenseException le = new LicenseException(EXCEPTION_ID1,
				EXCEPTION_NAME1, EXCEPTION_TEXT1, EXCEPTION_SEEALSO1,
				EXCEPTION_COMMENT1, EXCEPTION_EXAMPLE1);
		assertEquals(EXCEPTION_ID1, le.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le.getName());
		assertEquals(EXCEPTION_TEXT1, le.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le.getExample());
		le.setLicenseExceptionId(EXCEPTION_ID2);
		assertEquals(EXCEPTION_ID2, le.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le.getName());
		assertEquals(EXCEPTION_TEXT1, le.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le.getExample());
		Model model = ModelFactory.createDefaultModel();
		Resource leResource = le.createResource(model, null);
		LicenseException le2 = new LicenseException(model, leResource.asNode());
		assertEquals(EXCEPTION_ID2, le2.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le2.getName());
		assertEquals(EXCEPTION_TEXT1, le2.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le2.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le2.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le2.getExample());
		le2.setLicenseExceptionId(EXCEPTION_ID1);
		assertEquals(EXCEPTION_ID1, le2.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le2.getName());
		assertEquals(EXCEPTION_TEXT1, le2.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le2.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le2.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le2.getExample());
	}
	
	@Test
	public void testSetLicenseExceptionText() throws InvalidSPDXAnalysisException {
		LicenseException le = new LicenseException(EXCEPTION_ID1,
				EXCEPTION_NAME1, EXCEPTION_TEXT1, EXCEPTION_SEEALSO1,
				EXCEPTION_COMMENT1, EXCEPTION_EXAMPLE1);
		assertEquals(EXCEPTION_ID1, le.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le.getName());
		assertEquals(EXCEPTION_TEXT1, le.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le.getExample());
		le.setLicenseExceptionText(EXCEPTION_TEXT2);
		assertEquals(EXCEPTION_ID1, le.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le.getName());
		assertEquals(EXCEPTION_TEXT2, le.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le.getExample());
		Model model = ModelFactory.createDefaultModel();
		Resource leResource = le.createResource(model, null);
		LicenseException le2 = new LicenseException(model, leResource.asNode());
		assertEquals(EXCEPTION_ID1, le2.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le2.getName());
		assertEquals(EXCEPTION_TEXT2, le2.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le2.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le2.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le2.getExample());
		le2.setLicenseExceptionText(EXCEPTION_TEXT1);
		assertEquals(EXCEPTION_ID1, le2.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le2.getName());
		assertEquals(EXCEPTION_TEXT1, le2.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le2.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le2.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le2.getExample());
	}
	
	@Test
	public void testSetSeeAlso() throws InvalidSPDXAnalysisException {
		LicenseException le = new LicenseException(EXCEPTION_ID1,
				EXCEPTION_NAME1, EXCEPTION_TEXT1, EXCEPTION_SEEALSO1,
				EXCEPTION_COMMENT1, EXCEPTION_EXAMPLE1);
		assertEquals(EXCEPTION_ID1, le.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le.getName());
		assertEquals(EXCEPTION_TEXT1, le.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le.getExample());
		le.setSeeAlso(EXCEPTION_SEEALSO2);
		assertEquals(EXCEPTION_ID1, le.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le.getName());
		assertEquals(EXCEPTION_TEXT1, le.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO2, le.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le.getExample());
		Model model = ModelFactory.createDefaultModel();
		Resource leResource = le.createResource(model, null);
		LicenseException le2 = new LicenseException(model, leResource.asNode());
		assertEquals(EXCEPTION_ID1, le2.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le2.getName());
		assertEquals(EXCEPTION_TEXT1, le2.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO2, le2.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le2.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le2.getExample());
		le2.setSeeAlso(EXCEPTION_SEEALSO1);
		assertEquals(EXCEPTION_ID1, le2.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le2.getName());
		assertEquals(EXCEPTION_TEXT1, le2.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le2.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le2.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le2.getExample());
	}
	
	@Test
	public void testSetName() throws InvalidSPDXAnalysisException {
		LicenseException le = new LicenseException(EXCEPTION_ID1,
				EXCEPTION_NAME1, EXCEPTION_TEXT1, EXCEPTION_SEEALSO1,
				EXCEPTION_COMMENT1, EXCEPTION_EXAMPLE1);
		assertEquals(EXCEPTION_ID1, le.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le.getName());
		assertEquals(EXCEPTION_TEXT1, le.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le.getExample());
		le.setName(EXCEPTION_NAME2);
		assertEquals(EXCEPTION_ID1, le.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME2, le.getName());
		assertEquals(EXCEPTION_TEXT1, le.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le.getExample());
		Model model = ModelFactory.createDefaultModel();
		Resource leResource = le.createResource(model, null);
		LicenseException le2 = new LicenseException(model, leResource.asNode());
		assertEquals(EXCEPTION_ID1, le2.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME2, le2.getName());
		assertEquals(EXCEPTION_TEXT1, le2.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le2.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le2.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le2.getExample());
		le2.setName(EXCEPTION_NAME1);
		assertEquals(EXCEPTION_ID1, le2.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le2.getName());
		assertEquals(EXCEPTION_TEXT1, le2.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le2.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le2.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le2.getExample());
	}
	
	@Test
	public void testHashCode() {
		LicenseException le = new LicenseException(EXCEPTION_ID1,
				EXCEPTION_NAME1, EXCEPTION_TEXT1, EXCEPTION_SEEALSO1,
				EXCEPTION_COMMENT1, EXCEPTION_EXAMPLE1);
		LicenseException le2 = new LicenseException(EXCEPTION_ID1,
				EXCEPTION_NAME2, EXCEPTION_TEXT2, EXCEPTION_SEEALSO2,
				EXCEPTION_COMMENT2, EXCEPTION_EXAMPLE2);
		LicenseException le3 = new LicenseException(EXCEPTION_ID2,
				EXCEPTION_NAME1, EXCEPTION_TEXT1, EXCEPTION_SEEALSO1,
				EXCEPTION_COMMENT1, EXCEPTION_EXAMPLE1);
		assertEquals(le.hashCode(), le2.hashCode());
		assertFalse(le.hashCode() == le3.hashCode());
	}

	@Test
	public void testVerify() throws InvalidSPDXAnalysisException {
		LicenseException le = new LicenseException(EXCEPTION_ID1,
				EXCEPTION_NAME1, EXCEPTION_TEXT1, EXCEPTION_SEEALSO1,
				EXCEPTION_COMMENT1, EXCEPTION_EXAMPLE1);
		assertEquals(0,le.verify().size());
		Model model = ModelFactory.createDefaultModel();
		Resource leResource = le.createResource(model, null);
		LicenseException le2 = new LicenseException(model, leResource.asNode());
		assertEquals(EXCEPTION_ID1, le2.getLicenseExceptionId());
		assertEquals(EXCEPTION_NAME1, le2.getName());
		assertEquals(EXCEPTION_TEXT1, le2.getLicenseExceptionText());
		assertStringsEquals(EXCEPTION_SEEALSO1, le2.getSeeAlso());
		assertEquals(EXCEPTION_COMMENT1, le2.getComment());
		assertEquals(EXCEPTION_EXAMPLE1, le2.getExample());
		assertEquals(0,le2.verify().size());
		le2.setLicenseExceptionId(null);
		assertEquals(1,le2.verify().size());
		le2.setLicenseExceptionText("  ");
		assertEquals(2, le2.verify().size());
	}
	
	/**
	 * @param s1
	 * @param s2
	 */
	private void assertStringsEquals(String[] s1,
			String[] s2) {
		if (s1 == null) {
			assertTrue(s2 == null);
		}
		assertTrue(s2 != null);
		assertEquals(s1.length, s2.length);
		for (int i = 0; i < s1.length; i++) {
			boolean found = false;
			for (int j = 0; j < s2.length; j++) {
				if (s1[i].equals(s2[j])) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
	}

	@Test
	public void testDuplicateRestrictionId() throws InvalidSPDXAnalysisException {
		LicenseException le = new LicenseException(EXCEPTION_ID1,
				EXCEPTION_NAME1, EXCEPTION_TEXT1, EXCEPTION_SEEALSO1,
				EXCEPTION_COMMENT1, EXCEPTION_EXAMPLE1);
		assertEquals(0,le.verify().size());
		Model model = ModelFactory.createDefaultModel();
		le.createResource(model, null);
		LicenseException le2 = new LicenseException(EXCEPTION_ID1,
				EXCEPTION_NAME2, EXCEPTION_TEXT2, EXCEPTION_SEEALSO2,
				EXCEPTION_COMMENT2, EXCEPTION_EXAMPLE2);
		boolean error = false;
		try {
			le2.createResource(model, null);
		} catch (InvalidSPDXAnalysisException e) {
			error = true;
		}
		assertTrue(error);
	}
	
	@Test
	public void testDuplicateRestrictionIdSetId() throws InvalidSPDXAnalysisException {
		LicenseException le = new LicenseException(EXCEPTION_ID1,
				EXCEPTION_NAME1, EXCEPTION_TEXT1, EXCEPTION_SEEALSO1,
				EXCEPTION_COMMENT1, EXCEPTION_EXAMPLE1);
		assertEquals(0,le.verify().size());
		Model model = ModelFactory.createDefaultModel();
		le.createResource(model, null);
		LicenseException le2 = new LicenseException(EXCEPTION_ID2,
				EXCEPTION_NAME2, EXCEPTION_TEXT2, EXCEPTION_SEEALSO2,
				EXCEPTION_COMMENT2, EXCEPTION_EXAMPLE2);
		le2.createResource(model, null);
		boolean error = false;
		try {
			le2.setLicenseExceptionId(EXCEPTION_ID1);
		} catch (InvalidSPDXAnalysisException e) {
			error = true;
		}
		assertTrue(error);
	}
}
