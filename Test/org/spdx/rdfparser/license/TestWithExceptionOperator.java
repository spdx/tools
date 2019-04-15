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
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.IRdfModel;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

/**
 * @author Gary
 *
 */
public class TestWithExceptionOperator {
	
	static final String LICENSE_ID1 = "LicenseRef-1";
	static final String LICENSE_TEXT1 = "licenseText";
	static final String EXCEPTION_ID1 = "Exception-1";
	static final String EXCEPTION_NAME1 = "ExceptionName";
	static final String EXCEPTION_TEXT1 = "ExceptionText";
	static final String LICENSE_ID2 = "LicenseRef-2";
	static final String LICENSE_TEXT2 = "Second licenseText";
	static final String EXCEPTION_ID2 = "Exception-2";
	static final String EXCEPTION_NAME2 = "Second ExceptionName";
	static final String EXCEPTION_TEXT2 = "Second ExceptionText";

	private SimpleLicensingInfo license1;
	private SimpleLicensingInfo license2;
	private LicenseException exception1;
	private LicenseException exception2;
	
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
		public void addSpdxElementRef(String elementRef) {
			
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
			// TODO Auto-generated method stub
			return false;
		}
		
	};
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
		exception1 = new LicenseException(EXCEPTION_ID1, EXCEPTION_NAME1,
				EXCEPTION_TEXT1);
		exception2 = new LicenseException(EXCEPTION_ID2, EXCEPTION_NAME2,
				EXCEPTION_TEXT2);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.WithExceptionOperator#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		SimpleLicensingInfo sameLicId = new ExtractedLicenseInfo(LICENSE_ID1, "different text");
		LicenseException sameExceptionId = new LicenseException(EXCEPTION_ID1, "different Name",
				"different exception text"); 
		WithExceptionOperator weo1 = new WithExceptionOperator(license1, exception1);
		WithExceptionOperator weo2 = new WithExceptionOperator(license2, exception2);
		WithExceptionOperator weoSameIdAs1 = new WithExceptionOperator(sameLicId, sameExceptionId);
		assertFalse(weo1.hashCode() == weo2.hashCode());
		assertTrue(weo1.hashCode() == weoSameIdAs1.hashCode());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.WithExceptionOperator#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		SimpleLicensingInfo sameLicId = new ExtractedLicenseInfo(LICENSE_ID1, "different text");
		LicenseException sameExceptionId = new LicenseException(EXCEPTION_ID1, "different Name",
				"different exception text"); 
		WithExceptionOperator weo1 = new WithExceptionOperator(license1, exception1);
		WithExceptionOperator weo2 = new WithExceptionOperator(license2, exception2);
		WithExceptionOperator weoSameIdAs1 = new WithExceptionOperator(sameLicId, sameExceptionId);
		assertFalse(weo1.equals(weo2));
		assertTrue(weo1.equals(weoSameIdAs1));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.WithExceptionOperator#verify()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException {
		WithExceptionOperator weo1 = new WithExceptionOperator(license1, exception1);
		assertEquals(0, weo1.verify().size());
		weo1.setException(null);
		assertEquals(1, weo1.verify().size());
		weo1.setLicense(null);
		assertEquals(2, weo1.verify().size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.WithExceptionOperator#clone()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		WithExceptionOperator weo1 = new WithExceptionOperator(license1, exception1);
		model = ModelFactory.createDefaultModel();
		weo1.createResource(modelContainer);
		WithExceptionOperator clone = (WithExceptionOperator) weo1.clone();
		ExtractedLicenseInfo lic1 = (ExtractedLicenseInfo)weo1.getLicense();
		ExtractedLicenseInfo lic1FromClone = (ExtractedLicenseInfo)clone.getLicense();
		assertEquals(lic1.getLicenseId(), lic1FromClone.getLicenseId());
		assertEquals(lic1.getExtractedText(), lic1FromClone.getExtractedText());
		LicenseException le1 = weo1.getException();
		LicenseException le1FromClone = clone.getException();
		assertEquals(le1.getLicenseExceptionId(), le1FromClone.getLicenseExceptionId());
		assertEquals(le1.getLicenseExceptionText(), le1FromClone.getLicenseExceptionText());
		assertEquals(le1.getName(), le1FromClone.getName());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.WithExceptionOperator#setLicense(org.spdx.rdfparser.license.SimpleLicensingInfo)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetLicense() throws InvalidSPDXAnalysisException {
		WithExceptionOperator weo1 = new WithExceptionOperator(license1, exception1);
		model = ModelFactory.createDefaultModel();
		weo1.createResource(modelContainer);
		ExtractedLicenseInfo lic1 = (ExtractedLicenseInfo)weo1.getLicense();
		LicenseException le1 = weo1.getException();
		assertEquals(LICENSE_ID1, lic1.getLicenseId());
		assertEquals(LICENSE_TEXT1, lic1.getExtractedText());
		assertEquals(EXCEPTION_ID1, le1.getLicenseExceptionId());
		assertEquals(EXCEPTION_TEXT1, le1.getLicenseExceptionText());
		assertEquals(EXCEPTION_NAME1, le1.getName());
		weo1.setLicense(license2);
		lic1 = (ExtractedLicenseInfo)weo1.getLicense();
		le1 = weo1.getException();
		assertEquals(LICENSE_ID2, lic1.getLicenseId());
		assertEquals(LICENSE_TEXT2, lic1.getExtractedText());
		assertEquals(EXCEPTION_ID1, le1.getLicenseExceptionId());
		assertEquals(EXCEPTION_TEXT1, le1.getLicenseExceptionText());
		assertEquals(EXCEPTION_NAME1, le1.getName());
	}


	/**
	 * Test method for {@link org.spdx.rdfparser.license.WithExceptionOperator#setException(org.spdx.rdfparser.license.LicenseException)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetException() throws InvalidSPDXAnalysisException {
		WithExceptionOperator weo1 = new WithExceptionOperator(license1, exception1);
		model = ModelFactory.createDefaultModel();
		weo1.createResource(modelContainer);
		ExtractedLicenseInfo lic1 = (ExtractedLicenseInfo)weo1.getLicense();
		LicenseException le1 = weo1.getException();
		assertEquals(LICENSE_ID1, lic1.getLicenseId());
		assertEquals(LICENSE_TEXT1, lic1.getExtractedText());
		assertEquals(EXCEPTION_ID1, le1.getLicenseExceptionId());
		assertEquals(EXCEPTION_TEXT1, le1.getLicenseExceptionText());
		assertEquals(EXCEPTION_NAME1, le1.getName());
		weo1.setException(exception2);
		lic1 = (ExtractedLicenseInfo)weo1.getLicense();
		le1 = weo1.getException();
		assertEquals(LICENSE_ID1, lic1.getLicenseId());
		assertEquals(LICENSE_TEXT1, lic1.getExtractedText());
		assertEquals(EXCEPTION_ID2, le1.getLicenseExceptionId());
		assertEquals(EXCEPTION_TEXT2, le1.getLicenseExceptionText());
		assertEquals(EXCEPTION_NAME2, le1.getName());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.AnyLicenseInfo#createResource(org.apache.jena.rdf.model.Model)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testCreateResource() throws InvalidSPDXAnalysisException {
		WithExceptionOperator weo1 = new WithExceptionOperator(license1, exception1);
		model = ModelFactory.createDefaultModel();
		Resource weo1Resource = weo1.createResource(modelContainer);
		WithExceptionOperator weo1FromResource = new WithExceptionOperator(modelContainer, weo1Resource.asNode());
		ExtractedLicenseInfo lic1 = (ExtractedLicenseInfo)weo1.getLicense();
		ExtractedLicenseInfo lic1FromResource = (ExtractedLicenseInfo)weo1FromResource.getLicense();
		assertEquals(lic1.getLicenseId(), lic1FromResource.getLicenseId());
		assertEquals(lic1.getExtractedText(), lic1FromResource.getExtractedText());
		LicenseException le1 = weo1.getException();
		LicenseException le1FromResource = weo1FromResource.getException();
		assertEquals(le1.getLicenseExceptionId(), le1FromResource.getLicenseExceptionId());
		assertEquals(le1.getLicenseExceptionText(), le1FromResource.getLicenseExceptionText());
		assertEquals(le1.getName(), le1FromResource.getName());
	}
	
	@Test
	public void testClassPathException() throws InvalidSPDXAnalysisException, InvalidLicenseStringException {
		assertTrue(LicenseInfoFactory.parseSPDXLicenseString("GPL-2.0-only WITH Classpath-exception-2.0").verify().isEmpty());
	}
}
