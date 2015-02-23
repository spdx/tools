/**
 * Copyright (c) 2013 Source Auditor Inc.
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
package spdxspreadsheet;

import static org.junit.Assert.*;

import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.spdxspreadsheet.DocumentInfoSheet;
import org.spdx.spdxspreadsheet.SPDXSpreadsheet;

/**
 * @author Gary O'Neall
 *
 */
public class TestOriginsSheet {

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

	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.DocumentInfoSheet#create(org.apache.poi.ss.usermodel.Workbook, java.lang.String)}.
	 */
	@Test
	public void testCreate() {
		Workbook wb = new HSSFWorkbook();
		DocumentInfoSheet.create(wb, "Origins");
		DocumentInfoSheet originsSheet = DocumentInfoSheet.openVersion(wb, "Origins", SPDXSpreadsheet.CURRENT_VERSION);
		String result = originsSheet.verify();
		if (result != null && !result.isEmpty()) {
			fail(result);
		}
	}

	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.DocumentInfoSheet#setSPDXVersion(java.lang.String)}.
	 */
	@Test
	public void testSetSPDXVersion() {
		Workbook wb = new HSSFWorkbook();
		DocumentInfoSheet.create(wb, "Origins");
		DocumentInfoSheet originsSheet = DocumentInfoSheet.openVersion(wb, "Origins", SPDXSpreadsheet.CURRENT_VERSION);
		String spdxVersion = "1.1";
		originsSheet.setSPDXVersion(spdxVersion);
		assertEquals(spdxVersion, originsSheet.getSPDXVersion());
		spdxVersion = "1.2";
		originsSheet.setSPDXVersion(spdxVersion);
		assertEquals(spdxVersion, originsSheet.getSPDXVersion());		
	}

	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.DocumentInfoSheet#setCreatedBy(java.lang.String[])}.
	 */
	@Test
	public void testSetCreatedBy() {
		Workbook wb = new HSSFWorkbook();
		DocumentInfoSheet.create(wb, "Origins");
		DocumentInfoSheet originsSheet = DocumentInfoSheet.openVersion(wb, "Origins", SPDXSpreadsheet.CURRENT_VERSION);
		String[] createdBys = new String[] {"Person: Gary O'Neall", "Tool: Source Auditor Scanner"};
		originsSheet.setCreatedBy(createdBys);
		compareStrings(createdBys, originsSheet.getCreatedBy());
		createdBys = new String[] {"Tool: FOSSOlogy"};
		originsSheet.setCreatedBy(createdBys);
		compareStrings(createdBys, originsSheet.getCreatedBy());
	}
	
	/**
	 * @param s1
	 * @param s2
	 */
	private void compareStrings(String[] s1, String[] s2) {
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
	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.DocumentInfoSheet#setDataLicense(java.lang.String)}.
	 */
	@Test
	public void testSetDataLicense() {
		Workbook wb = new HSSFWorkbook();
		DocumentInfoSheet.create(wb, "Origins");
		DocumentInfoSheet originsSheet = DocumentInfoSheet.openVersion(wb, "Origins", SPDXSpreadsheet.CURRENT_VERSION);
		String licenseId = "CC0";
		originsSheet.setDataLicense(licenseId);
		assertEquals(licenseId, originsSheet.getDataLicense());
		licenseId = "GPL-2.0+";
		originsSheet.setDataLicense(licenseId);
		assertEquals(licenseId, originsSheet.getDataLicense());	
	}

	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.DocumentInfoSheet#setAuthorComments(java.lang.String)}.
	 */
	@Test
	public void testSetAuthorComments() {
		Workbook wb = new HSSFWorkbook();
		DocumentInfoSheet.create(wb, "Origins");
		DocumentInfoSheet originsSheet = DocumentInfoSheet.openVersion(wb, "Origins", SPDXSpreadsheet.CURRENT_VERSION);
		String comment = "comment1";
		originsSheet.setAuthorComments(comment);
		assertEquals(comment, originsSheet.getAuthorComments());
		comment = "comment which is different";
		originsSheet.setAuthorComments(comment);
		assertEquals(comment, originsSheet.getAuthorComments());
	}

	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.DocumentInfoSheet#setCreated(java.util.Date)}.
	 */
	@Test
	public void testSetCreated() {
		Workbook wb = new HSSFWorkbook();
		DocumentInfoSheet.create(wb, "Origins");
		DocumentInfoSheet originsSheet = DocumentInfoSheet.openVersion(wb, "Origins", SPDXSpreadsheet.CURRENT_VERSION);
		Date created = new Date();
		originsSheet.setCreated(created);
		assertEquals(created.toString(), originsSheet.getCreated().toString());
	}

	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.DocumentInfoSheet#getDocumentComment()}.
	 */
	@Test
	public void testGetDocumentomment() {
		Workbook wb = new HSSFWorkbook();
		DocumentInfoSheet.create(wb, "Origins");
		DocumentInfoSheet originsSheet = DocumentInfoSheet.openVersion(wb, "Origins", SPDXSpreadsheet.CURRENT_VERSION);
		String comment = "comment1";
		originsSheet.setDocumentComment(comment);
		assertEquals(comment, originsSheet.getDocumentComment());
		comment = "comment which is different";
		originsSheet.setDocumentComment(comment);
		assertEquals(comment, originsSheet.getDocumentComment());
	}

	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.DocumentInfoSheet#setLicenseListVersion(java.lang.String)}.
	 */
	@Test
	public void testSetLicenseListVersion() {
		Workbook wb = new HSSFWorkbook();
		DocumentInfoSheet.create(wb, "Origins");
		DocumentInfoSheet originsSheet = DocumentInfoSheet.openVersion(wb, "Origins", SPDXSpreadsheet.CURRENT_VERSION);
		String ver = "1.19";
		originsSheet.setLicenseListVersion(ver);
		assertEquals(ver, originsSheet.getLicenseListVersion());
		ver = "1.20";
		originsSheet.setLicenseListVersion(ver);
		assertEquals(ver, originsSheet.getLicenseListVersion());
	}

}
