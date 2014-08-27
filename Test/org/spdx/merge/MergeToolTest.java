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
package org.spdx.merge;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.compare.LicenseCompareHelper;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SPDXLicense;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXNonStandardLicense;
import org.spdx.tools.MergeSpdxDocs;

import com.google.common.io.Files;

/**
 * @author Gary O'Neall
 *
 */
public class MergeToolTest {
	
	static final String TEST_DIR = "TestFiles";
	static File TEMP_DIR;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TEMP_DIR = Files.createTempDir();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		File[] files = TEMP_DIR.listFiles();
		for (File file : files) {
			file.delete();
		}
		TEMP_DIR.delete();
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
	public void testMergeMultipleDocs() throws IOException, InvalidSPDXAnalysisException {
		String[] args = new String[4];
		args[0] = TEST_DIR + File.separator + "SPDXRdfExample-v1.2.rdf";
		args[1] = TEST_DIR + File.separator + "SPDXSpreadsheetMergeTest1.rdf";
		args[2] = TEST_DIR + File.separator + "SPDXSpreadsheetMergeTest2.rdf";
		String outputFileName = TEMP_DIR + File.separator + "outputFile.rdf";
		args[3] = outputFileName;
		MergeSpdxDocs.main(args);
		SPDXDocument outputDoc = SPDXDocumentFactory.creatSpdxDocument(outputFileName);
		SPDXDocument masterDoc = SPDXDocumentFactory.creatSpdxDocument(args[0]);
		SPDXDocument firstMergeDoc = SPDXDocumentFactory.creatSpdxDocument(args[1]);
		SPDXDocument secondMergeDoc = SPDXDocumentFactory.creatSpdxDocument(args[2]);
		assertEquals(outputDoc.getSpdxPackage().getDeclaredName(), masterDoc.getSpdxPackage().getDeclaredName());
		// check for licenses and build a license map

		SPDXLicenseInfo[] masterExtractedLicenses = masterDoc.getExtractedLicenseInfos();
		SPDXLicenseInfo[] outputDocExtratedLicenses = outputDoc.getExtractedLicenseInfos();
		HashMap<String, String> masterDocLicMap = mapLicenseIds(masterExtractedLicenses, outputDocExtratedLicenses);
		HashMap<String, String> firstMergeDoccLicMap = mapLicenseIds(firstMergeDoc.getExtractedLicenseInfos(), outputDocExtratedLicenses);
		HashMap<String, String> secondMergeDocLicMap = mapLicenseIds(secondMergeDoc.getExtractedLicenseInfos(), outputDocExtratedLicenses);

	}

	/**
	 * Creates a map of license ID's from the fromLicenses to the toLicenses
	 * @param fromLicenses
	 * @param fromLicenses
	 * @return
	 */
	private HashMap<String, String> mapLicenseIds(
			SPDXLicenseInfo[] fromLicenses,
			SPDXLicenseInfo[] toLicenses) {
		HashMap<String, String> retval = new HashMap<String, String>();
		for (SPDXLicenseInfo fromLicense : fromLicenses) {
			if (fromLicense instanceof SPDXNonStandardLicense) {
				SPDXNonStandardLicense fromNonStdLicense = (SPDXNonStandardLicense)fromLicense;
				for (SPDXLicenseInfo toLicense : toLicenses) {
					if (toLicense instanceof SPDXNonStandardLicense && 
							LicenseCompareHelper.isLicenseTextEquivalent(fromNonStdLicense.getText(), 
							((SPDXNonStandardLicense)toLicense).getText())) {
						if (retval.containsKey(fromNonStdLicense.getId())) {
							fail("Duplicate license text values for "+fromNonStdLicense.getText());
						} else {
							retval.put(fromNonStdLicense.getId(), ((SPDXNonStandardLicense)toLicense).getId());
						}
					}
				}
				if (!retval.containsKey(fromNonStdLicense.getId())) {
					fail("No matching license found for "+fromNonStdLicense.getText());
				}
			}
		}
		return retval;
	}

}
