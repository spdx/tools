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
package org.spdx.rdfparser;

import static org.junit.Assert.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.spdxspreadsheet.OriginsSheet;
import org.spdx.spdxspreadsheet.PerFileSheet;
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

	SPDXNonStandardLicense[] NON_STD_LICENSES;
	SPDXStandardLicense[] STANDARD_LICENSES;
	SPDXDisjunctiveLicenseSet[] DISJUNCTIVE_LICENSES;
	SPDXConjunctiveLicenseSet[] CONJUNCTIVE_LICENSES;
	
	SPDXConjunctiveLicenseSet COMPLEX_LICENSE;
	
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
		NON_STD_LICENSES = new SPDXNonStandardLicense[NONSTD_IDS.length];
		for (int i = 0; i < NONSTD_IDS.length; i++) {
			NON_STD_LICENSES[i] = new SPDXNonStandardLicense(NONSTD_IDS[i], NONSTD_TEXTS[i]);
		}
		
		STANDARD_LICENSES = new SPDXStandardLicense[STD_IDS.length];
		for (int i = 0; i < STD_IDS.length; i++) {
			STANDARD_LICENSES[i] = new SPDXStandardLicense("Name "+String.valueOf(i), 
					STD_IDS[i], STD_TEXTS[i], "URL "+String.valueOf(i), "Notes "+String.valueOf(i), 
					"LicHeader "+String.valueOf(i), "Template "+String.valueOf(i), true);
		}
		
		DISJUNCTIVE_LICENSES = new SPDXDisjunctiveLicenseSet[3];
		CONJUNCTIVE_LICENSES = new SPDXConjunctiveLicenseSet[2];
		
		DISJUNCTIVE_LICENSES[0] = new SPDXDisjunctiveLicenseSet(new SPDXLicenseInfo[] {
				NON_STD_LICENSES[0], NON_STD_LICENSES[1], STANDARD_LICENSES[1]
		});
		CONJUNCTIVE_LICENSES[0] = new SPDXConjunctiveLicenseSet(new SPDXLicenseInfo[] {
				STANDARD_LICENSES[0], NON_STD_LICENSES[0], STANDARD_LICENSES[1]
		});
		CONJUNCTIVE_LICENSES[1] = new SPDXConjunctiveLicenseSet(new SPDXLicenseInfo[] {
				DISJUNCTIVE_LICENSES[0], NON_STD_LICENSES[2]
		});
		DISJUNCTIVE_LICENSES[1] = new SPDXDisjunctiveLicenseSet(new SPDXLicenseInfo[] {
				CONJUNCTIVE_LICENSES[1], NON_STD_LICENSES[0], STANDARD_LICENSES[0]
		});
		DISJUNCTIVE_LICENSES[2] = new SPDXDisjunctiveLicenseSet(new SPDXLicenseInfo[] {
				DISJUNCTIVE_LICENSES[1], CONJUNCTIVE_LICENSES[0], STANDARD_LICENSES[2]
		});
		COMPLEX_LICENSE = new SPDXConjunctiveLicenseSet(new SPDXLicenseInfo[] {
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
	 * Test method for {@link org.spdx.spdxspreadsheet.PerFileSheet#add(org.spdx.rdfparser.SPDXFile)}.
	 * @throws SpreadsheetException 
	 */
	@Test
	public void testAddAndGet() throws SpreadsheetException {
		Workbook wb = new HSSFWorkbook();
		PerFileSheet.create(wb, "File Info");
		PerFileSheet fileInfoSheet = new PerFileSheet(wb, "File Info", OriginsSheet.CURRENT_VERSION);
		SPDXLicenseInfo[] testLicenses1 = new SPDXLicenseInfo[] {COMPLEX_LICENSE};
		SPDXLicenseInfo[] testLicenses2 = new SPDXLicenseInfo[] {NON_STD_LICENSES[0]};
		DOAPProject[] testProject2 = new DOAPProject[] {new DOAPProject("artifactof 2", "home page2")};
		DOAPProject[] testProject3 = new DOAPProject[] {new DOAPProject("artifactof 3", "home page3")};
		SPDXFile testFile1 = new SPDXFile("FileName1", "fileType1", "sha1", COMPLEX_LICENSE, testLicenses2, 
				"license comments 1", "copyright (c) 1", testProject2);
		SPDXFile testFile2 = new SPDXFile("FileName2", "fileType2", "sha12", NON_STD_LICENSES[0], testLicenses1, 
				"license comments2", "copyright (c) 12", testProject3);
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
		assertEquals(testFile.getConcludedLicenses(), result.getConcludedLicenses());
		compareLicenseDeclarations(testFile.getSeenLicenses(), result.getSeenLicenses());
		compareProjects(testFile.getArtifactOf(), result.getArtifactOf());
		assertEquals(testFile.getCopyright(), result.getCopyright());
		assertEquals(testFile.getLicenseComments(), result.getLicenseComments());
		assertEquals(testFile.getName(), result.getName());
		assertEquals(testFile.getSha1(), result.getSha1());
		assertEquals(testFile.getType(), result.getType());
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
				if (!found) {
					fail("Project not found: "+projects[i].getName());
				}
			}
		}
	}
	private void compareLicenseDeclarations(
			SPDXLicenseInfo[] testLicenses,
			SPDXLicenseInfo[] result) {
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
	 * Test method for {@link org.spdx.spdxspreadsheet.PerFileSheet#create(org.apache.poi.ss.usermodel.Workbook, java.lang.String)}.
	 */
	@Test
	public void testCreate() {
		Workbook wb = new HSSFWorkbook();
		PerFileSheet.create(wb, "File Info");
		PerFileSheet fileInfoSheet = new PerFileSheet(wb, "File Info", OriginsSheet.CURRENT_VERSION);
		String ver = fileInfoSheet.verify();
		if (ver != null && !ver.isEmpty()){
			fail(ver);
		}

	}

}
