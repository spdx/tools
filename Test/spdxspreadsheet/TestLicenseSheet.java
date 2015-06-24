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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.spdxspreadsheet.SPDXLicenseSpreadsheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;

import com.google.common.collect.Lists;

/**
 * @author Gary O'Neall
 *
 */
public class TestLicenseSheet {
	
	String LICENSE_SPREADSHEET_PATH_13 = "TestFiles" + File.separator + "spdx_licenselist_v1.13.xls";
	String LICENSE_SPREADSHEET_PATH_14 = "TestFiles" + File.separator + "spdx_licenselist_v1.14.xls";
	String LICENSE_SPREADSHEET_PATH_19 = "TestFiles" + File.separator + "spdx_licenselist_v1.19.xls";
	String LICENSE_SPREADSHEET_PATH_20 = "TestFiles" + File.separator + "spdx_licenselist_v2.0.xls";

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
	 * Test method for {@link org.spdx.spdxspreadsheet.LicenseSheet#add(org.spdx.rdfparser.license.SpdxListedLicense)}.
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
			List<SpdxListedLicense> licenses = Lists.newArrayList();
			File spreadsheetFile = new File(LICENSE_SPREADSHEET_PATH_20);
			SPDXLicenseSpreadsheet version13ss = new SPDXLicenseSpreadsheet(spreadsheetFile, false, true);
			Iterator<SpdxListedLicense> iter = version13ss.getLicenseIterator();
			File spreadsheetCopy = new File(tempDir.getPath()+File.separator+"sscopy.xls");
			SPDXLicenseSpreadsheet copy = new SPDXLicenseSpreadsheet(spreadsheetCopy, true, false);
			while (iter.hasNext()) {
				SpdxListedLicense nextLic = iter.next();
				licenses.add(nextLic);
				copy.getLicenseSheet().add(nextLic);
			}
			copy.close();
			version13ss.close();
			// compare
			SPDXLicenseSpreadsheet compare = new SPDXLicenseSpreadsheet(spreadsheetCopy, false, true);
			try {
				iter = compare.getLicenseIterator();
				int i = 0;
				while (iter.hasNext()) {
					if (i > licenses.size()) {
						fail("to many licenses in copy");
					}
					SpdxListedLicense nextLic = iter.next();
					if (!nextLic.equals(licenses.get(i))) {
						fail("Licenses "+nextLic.getLicenseId()+" does not equal "+licenses.get(i).getLicenseId());
					}
					assertEquals(licenses.get(i).getLicenseId(), nextLic.getLicenseId());
					assertEquals(licenses.get(i).getName(), nextLic.getName());
					assertEquals(licenses.get(i).getComment(), nextLic.getComment());
					assertEquals(licenses.get(i).getSeeAlso(),nextLic.getSeeAlso());
					assertEquals(licenses.get(i).getStandardLicenseHeader(),
							nextLic.getStandardLicenseHeader());
					assertEquals(licenses.get(i).getStandardLicenseTemplate(), nextLic.getStandardLicenseTemplate());
					if (!compareText(licenses.get(i).getLicenseText(), nextLic.getLicenseText())) {
						fail("license text does not match for "+licenses.get(i).getLicenseId());
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
	public static boolean compareText(String textA,
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
	 * Test method for {@link org.spdx.rdfparser.SPDXLicenseSpreadSheet#getLicenseIterator()}.
	 * @throws SpreadsheetException 
	 */
	@Test
	public void testLicenseIterator() throws SpreadsheetException {
		File spreadsheetFile = new File(LICENSE_SPREADSHEET_PATH_20);
		SPDXLicenseSpreadsheet ss = new SPDXLicenseSpreadsheet(spreadsheetFile, false, true);
		Iterator<SpdxListedLicense> iter = ss.getLicenseIterator();
		// we'll look at the first one in detail
		SpdxListedLicense firstLic = iter.next();
		assertEquals("Glide", firstLic.getLicenseId());
		assertEquals("3dfx Glide License", firstLic.getName());
		assertEquals("", firstLic.getComment());
		assertEquals(1, firstLic.getSeeAlso().length);
		assertEquals("http://www.users.on.net/~triforce/glidexp/COPYING.txt", 
				firstLic.getSeeAlso()[0]);
		assertEquals("",
				firstLic.getStandardLicenseHeader());
		String licText = firstLic.getLicenseText();
		assertTrue(licText.startsWith("3DFX GLIDE Source Code General Public License"));
		assertTrue(!firstLic.isOsiApproved());
		int numLicenses = 1;
		SpdxListedLicense lastLic = null;
		while (iter.hasNext()) {
			numLicenses++;
			lastLic = iter.next();
		}
		assertEquals("Zope Public License 2.1", lastLic.getName());
		ss.close();
	}

}
