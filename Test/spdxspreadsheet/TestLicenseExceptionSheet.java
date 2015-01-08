/**
 * Copyright (c) 2014 Source Auditor Inc.
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.license.LicenseRestrictionException;
import org.spdx.rdfparser.license.LicenseException;
import org.spdx.spdxspreadsheet.SPDXLicenseSpreadsheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;

/**
 * @author Gary O'Neall
 *
 */
public class TestLicenseExceptionSheet {

	String LICENSE_SPREADSHEET_PATH_20 = "TestFiles" + File.separator + "spdx_licenselist_v2.0.xls";
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
	public void testAdd() throws IOException, SpreadsheetException, LicenseRestrictionException {
		File tempFile = File.createTempFile("TestLic", "test");
		String tempDirPath = tempFile.getPath() + "-DIR";
		File tempDir = new File(tempDirPath);
		if (!tempDir.mkdir()) {
			tempFile.delete();
			fail("Could not create temporary directory");
		}
		try {
			// create a copy of the spreadsheet then compare
			ArrayList<LicenseException> exceptions = new ArrayList<LicenseException>();
			File origSpreadsheetFile = new File(LICENSE_SPREADSHEET_PATH_20);
			SPDXLicenseSpreadsheet origSpreadsheet = new SPDXLicenseSpreadsheet(origSpreadsheetFile, false, true);
			Iterator<LicenseException> iter = origSpreadsheet.getExceptionIterator();
			File spreadsheetCopy = new File(tempDir.getPath()+File.separator+"sscopy.xls");
			SPDXLicenseSpreadsheet copy = new SPDXLicenseSpreadsheet(spreadsheetCopy, true, false);
			while (iter.hasNext()) {
				LicenseException nextRestriction = iter.next();
				exceptions.add(nextRestriction);
				copy.getLicenseExceptionSheet().add(nextRestriction);
			}
			copy.close();
			origSpreadsheet.close();
			// compare
			SPDXLicenseSpreadsheet compare = new SPDXLicenseSpreadsheet(spreadsheetCopy, false, true);
			try {
				iter = compare.getExceptionIterator();
				int i = 0;
				while (iter.hasNext()) {
					if (i > exceptions.size()) {
						fail("to many exceptions in copy");
					}
					LicenseException nextException = iter.next();
					assertEquals(exceptions.get(i).getLicenseExceptionId(), nextException.getLicenseExceptionId());
					assertEquals(exceptions.get(i).getName(), nextException.getName());
					assertEquals(exceptions.get(i).getComment(), nextException.getComment());
					assertEquals(exceptions.get(i).getExample(), nextException.getExample());
					if (!TestLicenseSheet.compareText(exceptions.get(i).getLicenseExceptionText(), nextException.getLicenseExceptionText())) {
						fail("license text does not match for "+exceptions.get(i).getLicenseExceptionId());
					}
					assertStringArraysEquals(exceptions.get(i).getSeeAlso(),
							nextException.getSeeAlso());
					i = i + 1;
				}
				assertEquals(exceptions.size(), i);
			} finally {
				compare.close();
			}
		} finally {
			delDir(tempDir);
			tempFile.delete();
		}
	}
	/**
	 * @param tempDir
	 */
	private void delDir(File tempDir) {
		File[] files = tempDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				delDir(files[i]);
			} else {
				files[i].delete();
			}
		}
		tempDir.delete();
	}
	/**
	 * @param s1
	 * @param s2
	 */
	private void assertStringArraysEquals(String[] s1,
			String[] s2) {
		if (s1 == null) {
			if (s2 != null) {
				fail("Second array is not null");
			}
		}
		if (s2 == null) {
			
			fail ("first array is not null");
		}
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
	
	

}
