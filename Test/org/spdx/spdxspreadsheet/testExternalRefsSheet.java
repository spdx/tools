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
package org.spdx.spdxspreadsheet;

import static org.junit.Assert.*;

import java.net.URI;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.model.ExternalRef;
import org.spdx.rdfparser.model.ExternalRef.ReferenceCategory;
import org.spdx.rdfparser.model.UnitTestHelper;
import org.spdx.rdfparser.referencetype.ListedReferenceTypes;
import org.spdx.rdfparser.referencetype.ReferenceType;
import org.spdx.spdxspreadsheet.ExternalRefsSheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;

/**
 * @author Gary O'Neall
 *
 */
public class testExternalRefsSheet {
	static final String DOCUMENT_NAMSPACE = "http://local/document/namespace";
	
	static final String LOCAL_REFERENCE_TYPE_NAME = "localType";
	static final String FULL_REFRENCE_TYPE_URI = "http://this/is/not/in/the/document#here";
	
	static final String PKG1_ID = "SPDXRef-pkg1";
	static final String PKG2_ID = "SPDXRef-pkg2";
	
	static final String CPE32_NAME = "cpe23Type";
	static final String MAVEN_NAME = "maven-central";
	
	ReferenceType REFERENCE_TYPE_CPE32;
	ReferenceType REFERENCE_TYPE_MAVEN;
	ReferenceType REFERENCE_TYPE_LOCAL_TO_PACKAGE;
	ReferenceType REFERENCE_TYPE_FULL_URI;
	
	ExternalRef EXTERNAL_PKG1_REF1;
	ExternalRef EXTERNAL_PKG1_REF2;
	ExternalRef EXTERNAL_PKG1_REF3;
	ExternalRef EXTERNAL_PKG2_REF1;
	ExternalRef EXTERNAL_PKG2_REF2;
	
	SpdxDocumentContainer container;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		REFERENCE_TYPE_CPE32 = ListedReferenceTypes.getListedReferenceTypes().getListedReferenceTypeByName(CPE32_NAME);
		REFERENCE_TYPE_MAVEN = ListedReferenceTypes.getListedReferenceTypes().getListedReferenceTypeByName(MAVEN_NAME);
		REFERENCE_TYPE_FULL_URI = new ReferenceType(
				new URI(FULL_REFRENCE_TYPE_URI), null, null, null);
		REFERENCE_TYPE_LOCAL_TO_PACKAGE = new ReferenceType(
				new URI(DOCUMENT_NAMSPACE + "#" + LOCAL_REFERENCE_TYPE_NAME), null, null, null);
		
		EXTERNAL_PKG1_REF1 = new ExternalRef(ReferenceCategory.referenceCategory_security,
				REFERENCE_TYPE_CPE32, "LocatorPkg1Ref1", "CommentPkg1Ref1");
		EXTERNAL_PKG1_REF2 = new ExternalRef(ReferenceCategory.referenceCategory_packageManager,
				REFERENCE_TYPE_MAVEN, "LocatorPkg1Ref2", "CommentPkg1Ref2");
		EXTERNAL_PKG1_REF3 = new ExternalRef(ReferenceCategory.referenceCategory_other,
				REFERENCE_TYPE_LOCAL_TO_PACKAGE, "LocatorPkg1Ref2", "CommentPkg1Ref2");
		EXTERNAL_PKG2_REF1 = new ExternalRef(ReferenceCategory.referenceCategory_security,
				REFERENCE_TYPE_CPE32, "LocatorPkg2Ref1", "CommentPk21Ref1");
		EXTERNAL_PKG2_REF2 = new ExternalRef(ReferenceCategory.referenceCategory_other,
				REFERENCE_TYPE_FULL_URI, "LocatorPkg2Ref2", "CommentPkg2Ref2");
		
		container = new SpdxDocumentContainer(DOCUMENT_NAMSPACE);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.ExternalRefsSheet#create(org.apache.poi.ss.usermodel.Workbook, java.lang.String)}.
	 */
	@Test
	public void testCreate() {
		Workbook wb = new HSSFWorkbook();
		ExternalRefsSheet.create(wb, "External Refs");
		ExternalRefsSheet externalRefsSheet = new ExternalRefsSheet(wb, "External Refs");
		assertTrue(externalRefsSheet.verify() == null);
	}

	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.ExternalRefsSheet#add(java.lang.String, org.spdx.rdfparser.model.ExternalRef, org.spdx.rdfparser.SpdxDocumentContainer)}.
	 * @throws SpreadsheetException 
	 */
	@Test
	public void testAddGet() throws SpreadsheetException {
		Workbook wb = new HSSFWorkbook();
		ExternalRefsSheet.create(wb, "External Refs");
		ExternalRefsSheet externalRefsSheet = new ExternalRefsSheet(wb, "External Refs");
		externalRefsSheet.add(PKG1_ID, EXTERNAL_PKG1_REF1, container);
		externalRefsSheet.add(PKG2_ID, EXTERNAL_PKG2_REF1, container);
		externalRefsSheet.add(PKG1_ID, EXTERNAL_PKG1_REF2, container);
		externalRefsSheet.add(PKG1_ID, EXTERNAL_PKG1_REF3, container);
		externalRefsSheet.add(PKG2_ID, EXTERNAL_PKG2_REF2, container);
		assertTrue(externalRefsSheet.verify() == null);
		
		ExternalRef[] expectedPkg1 = new ExternalRef[] {EXTERNAL_PKG1_REF1,
				EXTERNAL_PKG1_REF2, EXTERNAL_PKG1_REF3};
		ExternalRef[] expectedPkg2 = new ExternalRef[] {EXTERNAL_PKG2_REF1,
				EXTERNAL_PKG2_REF2};
		
		ExternalRef[] result = externalRefsSheet.getExternalRefsForPkgid(PKG1_ID, container);
		assertTrue(UnitTestHelper.isArraysEquivalent(expectedPkg1, result));
		
		result = externalRefsSheet.getExternalRefsForPkgid(PKG2_ID, container);
		assertTrue(UnitTestHelper.isArraysEquivalent(expectedPkg2, result));
	}

	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.ExternalRefsSheet#refTypeToString(org.spdx.rdfparser.referencetype.ReferenceType, org.spdx.rdfparser.SpdxDocumentContainer)}.
	 */
	@Test
	public void testRefTypeToString() {
		assertEquals(CPE32_NAME, ExternalRefsSheet.refTypeToString(REFERENCE_TYPE_CPE32, container));
		assertEquals(MAVEN_NAME, ExternalRefsSheet.refTypeToString(REFERENCE_TYPE_MAVEN, container));
		assertEquals(LOCAL_REFERENCE_TYPE_NAME, ExternalRefsSheet.refTypeToString(REFERENCE_TYPE_LOCAL_TO_PACKAGE, container));
		assertEquals(FULL_REFRENCE_TYPE_URI, ExternalRefsSheet.refTypeToString(REFERENCE_TYPE_FULL_URI, container));
	}
	
	@Test
	public void testStringToReferenceType() {
		assertTrue(REFERENCE_TYPE_CPE32.equivalent(ExternalRefsSheet.stringToRefType(CPE32_NAME, container)));
		assertTrue(REFERENCE_TYPE_MAVEN.equivalent(ExternalRefsSheet.stringToRefType(MAVEN_NAME, container)));
		assertTrue(REFERENCE_TYPE_LOCAL_TO_PACKAGE.equivalent(ExternalRefsSheet.stringToRefType(LOCAL_REFERENCE_TYPE_NAME, container)));
		assertTrue(REFERENCE_TYPE_FULL_URI.equivalent(ExternalRefsSheet.stringToRefType(FULL_REFRENCE_TYPE_URI, container)));
	}

}
