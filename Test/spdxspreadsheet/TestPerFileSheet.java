/**
 * Copyright (c) 2011 Source Auditor Inc.
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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.DOAPProject;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ConjunctiveLicenseSet;
import org.spdx.rdfparser.license.DisjunctiveLicenseSet;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.spdxspreadsheet.PerFileSheet;
import org.spdx.spdxspreadsheet.SPDXSpreadsheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Source Auditor
 *
 */
public class TestPerFileSheet {

	static final String[] NONSTD_IDS = new String[] {"id1", "id2", "id3", "id4"};
	static final String[] NONSTD_TEXTS = new String[] {"text1", "text2", "text3", "text4"};
	static final String[] STD_IDS = new String[] {"AFL-3.0", "CECILL-B", "EUPL-1.0"};
	static final String[] STD_TEXTS = new String[] {"std text1", "std text2", "std text3"};

	ExtractedLicenseInfo[] NON_STD_LICENSES;
	SpdxListedLicense[] STANDARD_LICENSES;
	DisjunctiveLicenseSet[] DISJUNCTIVE_LICENSES;
	ConjunctiveLicenseSet[] CONJUNCTIVE_LICENSES;
	
	ConjunctiveLicenseSet COMPLEX_LICENSE;
	
	Resource[] NON_STD_LICENSES_RESOURCES;
	Resource[] STANDARD_LICENSES_RESOURCES;
	Resource[] DISJUNCTIVE_LICENSES_RESOURCES;
	Resource[] CONJUNCTIVE_LICENSES_RESOURCES;
	Resource COMPLEX_LICENSE_RESOURCE;
	
	Model model;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		NON_STD_LICENSES = new ExtractedLicenseInfo[NONSTD_IDS.length];
		for (int i = 0; i < NONSTD_IDS.length; i++) {
			NON_STD_LICENSES[i] = new ExtractedLicenseInfo(NONSTD_IDS[i], NONSTD_TEXTS[i]);
		}
		
		STANDARD_LICENSES = new SpdxListedLicense[STD_IDS.length];
		for (int i = 0; i < STD_IDS.length; i++) {
			STANDARD_LICENSES[i] = new SpdxListedLicense("Name "+String.valueOf(i), 
					STD_IDS[i], STD_TEXTS[i], new String[] {"URL "+String.valueOf(i), "URL2 "+String.valueOf(i)}, "Notes "+String.valueOf(i), 
					"LicHeader "+String.valueOf(i), "Template "+String.valueOf(i), true);
		}
		
		DISJUNCTIVE_LICENSES = new DisjunctiveLicenseSet[3];
		CONJUNCTIVE_LICENSES = new ConjunctiveLicenseSet[2];
		
		DISJUNCTIVE_LICENSES[0] = new DisjunctiveLicenseSet(new AnyLicenseInfo[] {
				NON_STD_LICENSES[0], NON_STD_LICENSES[1], STANDARD_LICENSES[1]
		});
		CONJUNCTIVE_LICENSES[0] = new ConjunctiveLicenseSet(new AnyLicenseInfo[] {
				STANDARD_LICENSES[0], NON_STD_LICENSES[0], STANDARD_LICENSES[1]
		});
		CONJUNCTIVE_LICENSES[1] = new ConjunctiveLicenseSet(new AnyLicenseInfo[] {
				DISJUNCTIVE_LICENSES[0], NON_STD_LICENSES[2]
		});
		DISJUNCTIVE_LICENSES[1] = new DisjunctiveLicenseSet(new AnyLicenseInfo[] {
				CONJUNCTIVE_LICENSES[1], NON_STD_LICENSES[0], STANDARD_LICENSES[0]
		});
		DISJUNCTIVE_LICENSES[2] = new DisjunctiveLicenseSet(new AnyLicenseInfo[] {
				DISJUNCTIVE_LICENSES[1], CONJUNCTIVE_LICENSES[0], STANDARD_LICENSES[2]
		});
		COMPLEX_LICENSE = new ConjunctiveLicenseSet(new AnyLicenseInfo[] {
				DISJUNCTIVE_LICENSES[2], NON_STD_LICENSES[2], CONJUNCTIVE_LICENSES[1]
		});
		model = ModelFactory.createDefaultModel();
		
		NON_STD_LICENSES_RESOURCES = new Resource[NON_STD_LICENSES.length];
		for (int i = 0; i < NON_STD_LICENSES.length; i++) {
			NON_STD_LICENSES_RESOURCES[i] = NON_STD_LICENSES[i].createResource(model);
		}
		STANDARD_LICENSES_RESOURCES = new Resource[STANDARD_LICENSES.length];
		for (int i = 0; i < STANDARD_LICENSES.length; i++) {
			STANDARD_LICENSES_RESOURCES[i] = STANDARD_LICENSES[i].createResource(model);
		}
		CONJUNCTIVE_LICENSES_RESOURCES = new Resource[CONJUNCTIVE_LICENSES.length];
		for (int i = 0; i < CONJUNCTIVE_LICENSES.length; i++) {
			CONJUNCTIVE_LICENSES_RESOURCES[i] = CONJUNCTIVE_LICENSES[i].createResource(model);
		}
		DISJUNCTIVE_LICENSES_RESOURCES = new Resource[DISJUNCTIVE_LICENSES.length];
		for (int i = 0; i < DISJUNCTIVE_LICENSES.length; i++) {
			DISJUNCTIVE_LICENSES_RESOURCES[i] = DISJUNCTIVE_LICENSES[i].createResource(model);
		}
		COMPLEX_LICENSE_RESOURCE = COMPLEX_LICENSE.createResource(model);
	}
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}


	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.PerFileSheetV09d3#add(org.spdx.rdfparser.SPDXFile)}.
	 * @throws SpreadsheetException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testAddAndGet() throws SpreadsheetException, InvalidSPDXAnalysisException {
		Workbook wb = new HSSFWorkbook();
		PerFileSheet.create(wb, "File Info");
		PerFileSheet fileInfoSheet = PerFileSheet.openVersion(wb, "File Info", SPDXSpreadsheet.CURRENT_VERSION);
		AnyLicenseInfo[] testLicenses1 = new AnyLicenseInfo[] {COMPLEX_LICENSE};
		AnyLicenseInfo[] testLicenses2 = new AnyLicenseInfo[] {NON_STD_LICENSES[0]};
		DOAPProject[] testProject2 = new DOAPProject[] {new DOAPProject("artifactof 2", "home page2")};
		DOAPProject[] testProject3 = new DOAPProject[] {new DOAPProject("artifactof 3", "home page3"), 
				new DOAPProject("artifactof 4", "home page4")};
		String fileComment1 = "comment 1";
		String[] contributors1 = new String[] {"Contrib1", "Contrib2"};
		String noticeText1 = "notice 1";
		SPDXFile testFile1 = new SPDXFile("FileName1", "fileType1", "sha1", COMPLEX_LICENSE, testLicenses2, 
				"license comments 1", "copyright (c) 1", testProject2, fileComment1,
				new SPDXFile[0], contributors1, noticeText1);
		SPDXFile testFile2 = new SPDXFile("FileName2", "fileType2", "sha12", NON_STD_LICENSES[0], testLicenses1, 
				"license comments2", "copyright (c) 12", testProject3);
		SPDXFile testFile3 = new SPDXFile("FileName3", "fileType3", "sha13", NON_STD_LICENSES[0], testLicenses1, 
				"license comments3", "copyright (c) 123", new DOAPProject[0], "Comment3",
				new SPDXFile[] {testFile1, testFile2}, new String[] {"c1"}, "Notice");
		fileInfoSheet.add(testFile1);
		fileInfoSheet.add(testFile3);
		fileInfoSheet.add(testFile2);
		SPDXFile result1 = fileInfoSheet.getFileInfo(1);
		SPDXFile result3 = fileInfoSheet.getFileInfo(2);
		SPDXFile result2 = fileInfoSheet.getFileInfo(3);
		SPDXFile result4 = fileInfoSheet.getFileInfo(4);
		compareSPDXFile(testFile1, result1);
		compareSPDXFile(testFile2, result2);
		compareSPDXFile(testFile3, result3);
		if (result4 != null) {
			fail("expected null");
		}
	}

	private void compareSPDXFile(SPDXFile testFile, SPDXFile result) throws InvalidSPDXAnalysisException {
		assertEquals(testFile.getConcludedLicenses(), result.getConcludedLicenses());
		compareLicenseDeclarations(testFile.getSeenLicenses(), result.getSeenLicenses());
		compareProjects(testFile.getArtifactOf(), result.getArtifactOf());
		assertEquals(testFile.getCopyright(), result.getCopyright());
		assertEquals(testFile.getLicenseComments(), result.getLicenseComments());
		assertEquals(testFile.getName(), result.getName());
		assertEquals(testFile.getSha1(), result.getSha1());
		assertEquals(testFile.getType(), result.getType());
		assertEquals(testFile.getNoticeText(), result.getNoticeText());
		compareStrings(testFile.getContributors(), result.getContributors());
		compareSPDXFiles(testFile.getFileDependencies(), result.getFileDependencies());
	}
	
	/**
	 * @param files1
	 * @param files2
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void compareSPDXFiles(SPDXFile[] files1,
			SPDXFile[] files2) throws InvalidSPDXAnalysisException {
		assertEquals(files1.length, files2.length);
		for (int i = 0; i < files1.length; i++) {
			boolean found = false;
			for (int j = 0; j < files2.length; j++) {
				if (files1[i].getName().equals(files2[j].getName())) {
					found = true;
					compareSPDXFile(files1[i], files2[j]);
					break;
				}
			}
			assertTrue(found);
		}
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
	 * Compares 2 projects and fails if they don't match
	 * @param artifactOf
	 * @param artifactOf2
	 */
	private void compareProjects(DOAPProject[] projects,
			DOAPProject[] result) {
		assertEquals(projects.length, result.length);
		for (int i = 0; i < projects.length; i++) {
			boolean found = false;
			for (int j = 0; j < result.length; j++) {
				if (projects[i].getName() == null) {
					if (result[j].getName() == null) {
						if (projects[i].getHomePage() == null) {
							if (result[j].getHomePage() == null) {
								found = true;
								break;
							}
						} else if (projects[i].getHomePage().equals(result[j].getHomePage())) {
							found = true;
							break;
						}
					}
				} else if (projects[i].getName().equals(result[j].getName())) {
					if (projects[i].getHomePage() == null) {
						if (result[j].getHomePage() == null) {
							found = true;
							break;
						}
					} else if (projects[i].getHomePage().equals(result[j].getHomePage())) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				fail("Project not found: "+projects[i].getName());
			}
		}
	}
	private void compareLicenseDeclarations(
			AnyLicenseInfo[] testLicenses,
			AnyLicenseInfo[] result) {
		assertEquals(testLicenses.length, result.length);
		for (int i = 0;i < testLicenses.length; i++) {
			boolean found = false;
			for (int j = 0; j < result.length; j++) {
				if (testLicenses[i].equals(result[j])) {
					found = true;
					break;
				}
			}
			if (!found) {
				fail("license not found: "+testLicenses[i].toString());
			}
		}
	}
	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.PerFileSheetV09d3#create(org.apache.poi.ss.usermodel.Workbook, java.lang.String)}.
	 */
	@Test
	public void testCreate() {
		Workbook wb = new HSSFWorkbook();
		PerFileSheet.create(wb, "File Info");
		PerFileSheet fileInfoSheet = PerFileSheet.openVersion(wb, "File Info", SPDXSpreadsheet.CURRENT_VERSION);
		String ver = fileInfoSheet.verify();
		if (ver != null && !ver.isEmpty()){
			fail(ver);
		}
	}
	
	@Test
	public void testCsv() {
		String[] strings = new String[] {"Test1", "\"Quoted test2\"", "", "Test4 with, comma"};
		String csvString = PerFileSheet.stringsToCsv(strings);
		String[] result = PerFileSheet.csvToStrings(csvString);
		assertEquals(strings.length, result.length);
		for (int i = 0; i < strings.length; i++) {
			assertEquals(strings[i], result[i]);
		}
	}

}
