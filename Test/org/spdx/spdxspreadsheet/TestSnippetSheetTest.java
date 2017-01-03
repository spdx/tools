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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ConjunctiveLicenseSet;
import org.spdx.rdfparser.license.DisjunctiveLicenseSet;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.DoapProject;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxFile.FileType;
import org.spdx.rdfparser.model.SpdxSnippet;
import org.spdx.rdfparser.model.pointer.ByteOffsetPointer;
import org.spdx.rdfparser.model.pointer.LineCharPointer;
import org.spdx.rdfparser.model.pointer.StartEndPointer;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;
import org.spdx.spdxspreadsheet.SnippetSheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;

import org.apache.jena.rdf.model.Resource;

/**
 * @author Gary
 *
 */
public class TestSnippetSheetTest {

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

	SpdxDocumentContainer spdxContainer;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		spdxContainer = new SpdxDocumentContainer("http://spdx.uri/for/me");
		NON_STD_LICENSES = new ExtractedLicenseInfo[NONSTD_IDS.length];
		for (int i = 0; i < NONSTD_IDS.length; i++) {
			NON_STD_LICENSES[i] = new ExtractedLicenseInfo(NONSTD_IDS[i], NONSTD_TEXTS[i]);
			spdxContainer.addNewExtractedLicenseInfo(NON_STD_LICENSES[i]);
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
				DISJUNCTIVE_LICENSES[0], NON_STD_LICENSES[3]
		});
		DISJUNCTIVE_LICENSES[1] = new DisjunctiveLicenseSet(new AnyLicenseInfo[] {
				CONJUNCTIVE_LICENSES[1], NON_STD_LICENSES[0], STANDARD_LICENSES[0]
		});
		DISJUNCTIVE_LICENSES[2] = new DisjunctiveLicenseSet(new AnyLicenseInfo[] {
				DISJUNCTIVE_LICENSES[1], CONJUNCTIVE_LICENSES[0], STANDARD_LICENSES[2]
		});
		COMPLEX_LICENSE = new ConjunctiveLicenseSet(new AnyLicenseInfo[] {
				DISJUNCTIVE_LICENSES[2], DISJUNCTIVE_LICENSES[1]
		});
		
		NON_STD_LICENSES_RESOURCES = new Resource[NON_STD_LICENSES.length];
		for (int i = 0; i < NON_STD_LICENSES.length; i++) {
			NON_STD_LICENSES_RESOURCES[i] = NON_STD_LICENSES[i].createResource(spdxContainer);
		}
		STANDARD_LICENSES_RESOURCES = new Resource[STANDARD_LICENSES.length];
		for (int i = 0; i < STANDARD_LICENSES.length; i++) {
			STANDARD_LICENSES_RESOURCES[i] = STANDARD_LICENSES[i].createResource(spdxContainer);
		}
		CONJUNCTIVE_LICENSES_RESOURCES = new Resource[CONJUNCTIVE_LICENSES.length];
		for (int i = 0; i < CONJUNCTIVE_LICENSES.length; i++) {
			CONJUNCTIVE_LICENSES_RESOURCES[i] = CONJUNCTIVE_LICENSES[i].createResource(spdxContainer);
		}
		DISJUNCTIVE_LICENSES_RESOURCES = new Resource[DISJUNCTIVE_LICENSES.length];
		for (int i = 0; i < DISJUNCTIVE_LICENSES.length; i++) {
			DISJUNCTIVE_LICENSES_RESOURCES[i] = DISJUNCTIVE_LICENSES[i].createResource(spdxContainer);
		}
		COMPLEX_LICENSE_RESOURCE = COMPLEX_LICENSE.createResource(spdxContainer);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testLicenseEquiv() throws InvalidLicenseStringException {
		String complexStr = COMPLEX_LICENSE.toString();
		AnyLicenseInfo compLic = LicenseInfoFactory.parseSPDXLicenseString(complexStr, spdxContainer);
		assertTrue(COMPLEX_LICENSE.equivalent(compLic));
	}

	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.SnippetSheet#create(org.apache.poi.ss.usermodel.Workbook, java.lang.String)}.
	 */
	@Test
	public void testCreate() {
		Workbook wb = new HSSFWorkbook();
		SnippetSheet.create(wb, "Snippets");
		SnippetSheet snippetSheet = new SnippetSheet(wb, "Snippets");
		assertTrue(snippetSheet.verify() == null);
	}

	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.SnippetSheet#getSnippet(int, org.spdx.rdfparser.SpdxDocumentContainer)}.
	 * @throws SpreadsheetException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testAddGet() throws SpreadsheetException, InvalidSPDXAnalysisException {
		Workbook wb = new HSSFWorkbook();
		SnippetSheet.create(wb, "Snippets");
		SnippetSheet snippetSheet = new SnippetSheet(wb, "Snippets");
		AnyLicenseInfo[] testLicenses1 = new AnyLicenseInfo[] {COMPLEX_LICENSE};
		AnyLicenseInfo[] testLicenses2 = new AnyLicenseInfo[] {NON_STD_LICENSES[0]};
		String comment1 = "comment 1";
		String comment2 = "comment 2";
		String fileComment1 = "comment 1";
		String[] contributors1 = new String[] {"Contrib1", "Contrib2"};
		String noticeText1 = "notice 1";
		SpdxFile testFile1 = new SpdxFile("FileName1", fileComment1, new Annotation[0],
				new Relationship[0], COMPLEX_LICENSE,
				testLicenses2, "copyright (c) 1",
				"license comments 1", new FileType[] {FileType.fileType_binary} ,
				new Checksum[] {new Checksum(Checksum.ChecksumAlgorithm.checksumAlgorithm_sha1, "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12")},
				contributors1, noticeText1, new DoapProject[0]);
		testFile1.setId("SPDXRef-File1");
		SpdxFile testFile2 = new SpdxFile("FileName2", fileComment1, new Annotation[0],
				new Relationship[0], NON_STD_LICENSES[0],
				testLicenses1,  "copyright (c) 12",
				"license comments2", new FileType[] {FileType.fileType_source} ,
				new Checksum[] {new Checksum(Checksum.ChecksumAlgorithm.checksumAlgorithm_sha1, "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12")},
				contributors1, noticeText1, new DoapProject[0]);
		testFile2.setId("SPDXRef-File2");
		StartEndPointer byteRange1 = new StartEndPointer(new ByteOffsetPointer(testFile1, 5),
				new ByteOffsetPointer(testFile1, 10));
		StartEndPointer byteRange2 = new StartEndPointer(new ByteOffsetPointer(testFile2, 7),
				new ByteOffsetPointer(testFile2, 8));
		StartEndPointer lineRange1 = new StartEndPointer(new LineCharPointer(testFile1, 5),
				new LineCharPointer(testFile1, 10));
		StartEndPointer lineRange2 = new StartEndPointer(new LineCharPointer(testFile2, 55),
				new LineCharPointer(testFile2, 1213));
		SpdxSnippet snippet1 = new SpdxSnippet("snippet1", comment1, new Annotation[0],
				new Relationship[0], COMPLEX_LICENSE, testLicenses1, "copyright (c) 1", 
				"license comments 1", testFile1, byteRange1, lineRange1);
		snippet1.setId("SPDXRef-snippet1");
		snippet1.createResource(spdxContainer);
		SpdxSnippet snippet2 = new SpdxSnippet("snippet2", comment2, new Annotation[0],
				new Relationship[0], NON_STD_LICENSES[0], testLicenses2, "copyright (c) 2", 
				"license comments 2", testFile2, byteRange2, lineRange2);
		snippet2.setId("SPDXRef-snippet2");
		snippet2.createResource(spdxContainer);
		snippetSheet.add(snippet1);
		snippetSheet.add(snippet2);
		
		SpdxSnippet result1 = snippetSheet.getSnippet(1, spdxContainer);
		SpdxSnippet result2 = snippetSheet.getSnippet(2, spdxContainer);
		SpdxSnippet result3 = snippetSheet.getSnippet(3, spdxContainer);
		assertTrue(snippet1.equivalent(result1));
		assertTrue(snippet2.equivalent(result2));
		assertTrue(result3 == null);
		assertEquals(snippet1.getSnippetFromFile().getId(), snippetSheet.getSnippetFileId(1));
		assertEquals(snippet2.getSnippetFromFile().getId(), snippetSheet.getSnippetFileId(2));
		assertTrue(snippetSheet.verify() == null);
	}
}
