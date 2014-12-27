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
import org.spdx.spdxspreadsheet.SPDXLicenseSpreadsheet;
import org.spdx.spdxspreadsheet.SPDXLicenseSpreadsheet.DeprecatedLicenseInfo;
import org.spdx.spdxspreadsheet.SpreadsheetException;

/**
 * @author Gary
 *
 */
public class TestDeprecatedLicenseSheet {
	
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

	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.DeprecatedLicenseSheet#add(org.spdx.rdfparser.SPDXStandardLicense)}.
	 * @throws IOException 
	 * @throws SpreadsheetException 
	 */
	@Test
	public void testAdd() throws IOException, SpreadsheetException {
		File tempFile = File.createTempFile("TestLic", "test");
		String tempDirPath = tempFile.getPath() + "-DIR";
		File tempDir = new File(tempDirPath);
		if (!tempDir.mkdir()) {
			tempFile.delete();
			fail("Could not create temporary directory");
		}
		try {
			// create a copy of the spreadsheet then compare
			ArrayList<DeprecatedLicenseInfo> licenses = new ArrayList<DeprecatedLicenseInfo>();
			File spreadsheetFile = new File(LICENSE_SPREADSHEET_PATH_20);
			SPDXLicenseSpreadsheet spreadsheet = new SPDXLicenseSpreadsheet(spreadsheetFile, false, true);
			File spreadsheetCopy = new File(tempDir.getPath()+File.separator+"sscopy.xls");
			SPDXLicenseSpreadsheet copy = new SPDXLicenseSpreadsheet(spreadsheetCopy, true, false);
			Iterator<DeprecatedLicenseInfo> iter = spreadsheet.getDeprecatedLicenseIterator();
			while (iter.hasNext()) {
				DeprecatedLicenseInfo nextLic = iter.next();
				licenses.add(nextLic);
				copy.getDeprecatedLicenseSheet().add(nextLic.getLicense(), nextLic.getDeprecatedVersion());
			}
			copy.close();
			spreadsheet.close();
			// compare
			SPDXLicenseSpreadsheet compare = new SPDXLicenseSpreadsheet(spreadsheetCopy, false, true);
			try {
				iter = compare.getDeprecatedLicenseIterator();
				int i = 0;
				while (iter.hasNext()) {
					if (i > licenses.size()) {
						fail("to many licenses in copy");
					}
					DeprecatedLicenseInfo nextLic = iter.next();
					assertEquals(licenses.get(i).getLicense().getId(),
							nextLic.getLicense().getId());
					assertEquals(licenses.get(i).getLicense().getName(),
							nextLic.getLicense().getName());
					assertEquals(licenses.get(i).getLicense().getComment(),
							nextLic.getLicense().getComment());
					assertEquals(licenses.get(i).getLicense().getSourceUrl(),
							nextLic.getLicense().getSourceUrl());
					assertEquals(licenses.get(i).getLicense().getStandardLicenseHeader(),
							nextLic.getLicense().getStandardLicenseHeader());
					assertEquals(licenses.get(i).getLicense().getTemplate(),
							nextLic.getLicense().getTemplate());
					if (!TestLicenseSheet.compareText(licenses.get(i).getLicense().getText(),
							nextLic.getLicense().getText())) {
						fail("license text does not match for "+licenses.get(i).getLicense().getId());
					}
					assertEquals(licenses.get(i).getLicense().isOsiApproved(), 
							nextLic.getLicense().isOsiApproved());
					assertEquals(licenses.get(i).getDeprecatedVersion(), 
							nextLic.getDeprecatedVersion());
					i = i + 1;
				}
				assertEquals(licenses.size(), i);
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
}
