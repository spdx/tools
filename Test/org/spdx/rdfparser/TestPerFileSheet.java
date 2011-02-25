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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.spdxspreadsheet.PerFileSheet;

/**
 * @author Source Auditor
 *
 */
public class TestPerFileSheet {

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
	 * Test method for {@link org.spdx.spdxspreadsheet.PerFileSheet#verify()}.
	 */
	@Test
	public void testVerify() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.PerFileSheet#add(org.spdx.rdfparser.SPDXFile)}.
	 * @throws SpreadsheetException 
	 */
	@Test
	public void testAddAndGet() throws SpreadsheetException {
		Workbook wb = new HSSFWorkbook();
		PerFileSheet.create(wb, "File Info");
		PerFileSheet fileInfoSheet = new PerFileSheet(wb, "File Info");
		LicenseDeclaration[] testLicenses1 = new LicenseDeclaration[3];
		testLicenses1[0] = new LicenseDeclaration("License1", new String[0]);
		testLicenses1[1] = new LicenseDeclaration("License2", new String[] {
				"disj1", "disj2", "disj3"
		});
		testLicenses1[2] = new LicenseDeclaration("License3", new String[] {"disjj"});
//		String lic1String = PackageInfoSheet.licensesToString(testLicenses1);
		LicenseDeclaration[] testLicenses2 = new LicenseDeclaration[2];
		testLicenses2[0] = new LicenseDeclaration("License3", new String[]{"testdik1", "testdis2"});
		testLicenses2[1] = new LicenseDeclaration("License4", new String[] {
				"disj1", "disj2", "disj3", "disj4"
		});
//		String lic2String = PackageInfoSheet.licensesToString(testLicenses2);

		SPDXFile testFile1 = new SPDXFile("FileName1", "fileType1", "sha1", testLicenses1, testLicenses2, 
				"license comments 1", "copyright (c) 1", "artifactof 2");
		SPDXFile testFile2 = new SPDXFile("FileName2", "fileType2", "sha12", testLicenses2, testLicenses1, 
				"license comments2", "copyright (c) 12", "artifactof 3");
		fileInfoSheet.add(testFile1);
		fileInfoSheet.add(testFile2);
		SPDXFile result1 = fileInfoSheet.getFileInfo(1);
		SPDXFile result2 = fileInfoSheet.getFileInfo(2);
		SPDXFile result3 = fileInfoSheet.getFileInfo(3);
		compareSPDXFile(testFile1, result1);
		compareSPDXFile(testFile2, result2);
		if (result3 != null) {
			fail("expected null");
		}
	}

	private void compareSPDXFile(SPDXFile testFile, SPDXFile result) {
		compareLicenseDeclarations(testFile.getFileLicenses(), result.getFileLicenses());
		compareLicenseDeclarations(testFile.getSeenLicenses(), result.getSeenLicenses());
		assertEquals(testFile.getArtifactOf(), result.getArtifactOf());
		assertEquals(testFile.getCopyright(), result.getCopyright());
		assertEquals(testFile.getLicenseComments(), result.getLicenseComments());
		assertEquals(testFile.getName(), result.getName());
		assertEquals(testFile.getSha1(), result.getSha1());
		assertEquals(testFile.getType(), result.getType());
	}
	
	private void compareLicenseDeclarations(
			LicenseDeclaration[] testLicenses,
			LicenseDeclaration[] result) {
		assertEquals(testLicenses.length, result.length);
		for (int i = 0;i < testLicenses.length; i++) {
			assertEquals(testLicenses[i].getName(), result[i].getName());
			assertEquals(testLicenses[i].getDisjunctiveLicenses().length, 
					result[i].getDisjunctiveLicenses().length);
			for (int j = 0; j < result[i].getDisjunctiveLicenses().length; j++) {
				assertEquals(testLicenses[i].getDisjunctiveLicenses()[j],
						result[i].getDisjunctiveLicenses()[j]);
			}
		}
	}
	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.PerFileSheet#create(org.apache.poi.ss.usermodel.Workbook, java.lang.String)}.
	 */
	@Test
	public void testCreate() {
		Workbook wb = new HSSFWorkbook();
		PerFileSheet.create(wb, "File Info");
		PerFileSheet fileInfoSheet = new PerFileSheet(wb, "File Info");
		String ver = fileInfoSheet.verify();
		if (ver != null && !ver.isEmpty()){
			fail(ver);
		}

	}

}
