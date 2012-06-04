/**
 * Copyright (c) 2012 Source Auditor Inc.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.SPDXStandardLicense;
import org.spdx.spdxspreadsheet.SPDXLicenseSpreadsheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;

/**
 * @author Gary O'Neall
 *
 */
public class TestLicenseSheet {
	
	String LICENSE_SPREADSHEET_PATH_13 = "TestFiles" + File.separator + "spdx_licenselist_v1.13.xls";
	String LICENSE_SPREADSHEET_PATH_14 = "TestFiles" + File.separator + "spdx_licenselist_v1.14.xls";

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
	 * Test method for {@link org.spdx.spdxspreadsheet.LicenseSheet#add(org.spdx.rdfparser.SPDXStandardLicense)}.
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
			ArrayList<SPDXStandardLicense> licenses = new ArrayList<SPDXStandardLicense>();
			File version14File = new File(LICENSE_SPREADSHEET_PATH_14);
			SPDXLicenseSpreadsheet version13ss = new SPDXLicenseSpreadsheet(version14File, false, true);
			Iterator<SPDXStandardLicense> iter = version13ss.getIterator();
			File spreadsheetCopy = new File(tempDir.getPath()+File.separator+"sscopy.xls");
			SPDXLicenseSpreadsheet copy = new SPDXLicenseSpreadsheet(spreadsheetCopy, true, false);
			while (iter.hasNext()) {
				SPDXStandardLicense nextLic = iter.next();
				licenses.add(nextLic);
				copy.getLicenseSheet().add(nextLic);
			}
			copy.close();
			version13ss.close();
			// compare
			SPDXLicenseSpreadsheet compare = new SPDXLicenseSpreadsheet(spreadsheetCopy, false, true);
			try {
				iter = compare.getIterator();
				int i = 0;
				while (iter.hasNext()) {
					if (i > licenses.size()) {
						fail("to many licenses in copy");
					}
					SPDXStandardLicense nextLic = iter.next();
					if (!nextLic.equals(licenses.get(i))) {
						fail("Licenses "+nextLic.getId()+" does not equal "+licenses.get(i).getId());
					}
					assertEquals(licenses.get(i).getId(), nextLic.getId());
					assertEquals(licenses.get(i).getName(), nextLic.getName());
					assertEquals(licenses.get(i).getNotes(), nextLic.getNotes());
					assertEquals(licenses.get(i).getSourceUrl(),nextLic.getSourceUrl());
					assertEquals(licenses.get(i).getStandardLicenseHeader(),
							nextLic.getStandardLicenseHeader());
					assertEquals(licenses.get(i).getTemplate(), nextLic.getTemplate());
					if (!compareText(licenses.get(i).getText(), nextLic.getText())) {
						fail("license text does not match for "+licenses.get(i).getId());
					}
					assertEquals(licenses.get(i).isOsiApproved(), nextLic.isOsiApproved());
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
	 * Compare text ignoring newline characters
	 * @param textA
	 * @param textB
	 * @return
	 * @throws IOException 
	 */
	private boolean compareText(String textA,
			String textB) throws IOException {
		BufferedReader readerA = new BufferedReader(new StringReader(textA));
		BufferedReader readerB = new BufferedReader(new StringReader(textB));
		try {
			String lineA = readerA.readLine();
			String lineB = readerB.readLine();
			String lastLineA = lineA;	// for debuggin
			String lastLineB = lineB;	// for debugging
			int lineNum = 1;	// debugging
			while (lineA != null) {
				// the following is a bit of a kludge to not fail if the last few empty lines don't match
				if (lineB == null) {
					if (lineA.isEmpty()) {
						lineA = readerA.readLine();
						return (lineA == null);
					}
				}
				if (!lineA.equals(lineB)) {
					return false;
				}
				lastLineA = lineA;
				lastLineB = lineB;
				lineA = readerA.readLine();
				lineB = readerB.readLine();
				lineNum++;
			}
			return (lineB == null);
		} finally {
			readerA.close();
			readerB.close();
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
	 * Test method for {@link org.spdx.rdfparser.SPDXLicenseSpreadSheet#getIterator()}.
	 * @throws SpreadsheetException 
	 */
	@Test
	public void testLicenseIterator() throws SpreadsheetException {
		File version14File = new File(LICENSE_SPREADSHEET_PATH_14);
		SPDXLicenseSpreadsheet version13ss = new SPDXLicenseSpreadsheet(version14File, false, true);
		Iterator<SPDXStandardLicense> iter = version13ss.getIterator();
		// we'll look at the first one in detail
		SPDXStandardLicense firstLic = iter.next();
		assertEquals("AFL-1.1", firstLic.getId());
		assertEquals("Academic Free License v1.1", firstLic.getName());
		assertEquals("This license has been superseded by later versions.\n", firstLic.getNotes());
		assertEquals("http://opensource.linux-mirror.org/licenses/afl-1.1.txt","http://opensource.linux-mirror.org/licenses/afl-1.1.txt", 
				firstLic.getSourceUrl());
		assertEquals("Licensed under the Academic Free License version 1.1.",
				firstLic.getStandardLicenseHeader());
//		assertEquals("", firstLic.getTemplate());
		assertTrue(firstLic.getText().startsWith("Academic Free License"));
//		assertTrue(firstLic.isOsiApproved());
		int numLicenses = 1;
		SPDXStandardLicense lastLic = null;
		while (iter.hasNext()) {
			numLicenses++;
			lastLic = iter.next();
		}
		assertEquals(161, numLicenses);
		assertEquals("Zope Public License 2.1", lastLic.getName());
		version13ss.close();
	}

}
