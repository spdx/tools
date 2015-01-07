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
import java.util.ArrayList;
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
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.License;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.tools.MergeSpdxDocs;

import com.google.common.io.Files;

/**
 * @author Gary O'Neall
 * @author Gang Ling
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

		AnyLicenseInfo[] masterExtractedLicenses = masterDoc.getExtractedLicenseInfos();
		AnyLicenseInfo[] outputDocExtratedLicenses = outputDoc.getExtractedLicenseInfos();		

		HashMap<String, String> masterDocLicMap = mapLicenseIds(masterExtractedLicenses, outputDocExtratedLicenses);
		HashMap<String, String> firstMergeDoccLicMap = mapLicenseIds(firstMergeDoc.getExtractedLicenseInfos(), outputDocExtratedLicenses);
		HashMap<String, String> secondMergeDocLicMap = mapLicenseIds(secondMergeDoc.getExtractedLicenseInfos(), outputDocExtratedLicenses);
		
		SPDXFile[] expectedFiles = createExpectedFiles(masterDoc,firstMergeDoc,secondMergeDoc);
		SPDXFile[] outputDocFiles = outputDoc.getSpdxPackage().getFiles();
		
		int num = 0;
		for(SPDXFile outputFile:outputDocFiles){
			for(SPDXFile expectedFile: expectedFiles){
				if(outputFile.equivalent(expectedFile)){
					num ++;
					break;
				}
			}
		}
		assertEquals(5,num);

	}

	/**
	 * Creates a map of license ID's from the fromLicenses to the toLicenses
	 * @param fromLicenses
	 * @param toLicenses
	 * @return
	 */
	private HashMap<String, String> mapLicenseIds(
			AnyLicenseInfo[] fromLicenses,
			AnyLicenseInfo[] toLicenses) {
		HashMap<String, String> retval = new HashMap<String, String>();
		for (AnyLicenseInfo fromLicense : fromLicenses) {
			if (fromLicense instanceof ExtractedLicenseInfo) {
				ExtractedLicenseInfo fromNonStdLicense = (ExtractedLicenseInfo)fromLicense;
				for (AnyLicenseInfo toLicense : toLicenses) {
					if (toLicense instanceof ExtractedLicenseInfo && 
							LicenseCompareHelper.isLicenseTextEquivalent(fromNonStdLicense.getExtractedText(), 
							((ExtractedLicenseInfo)toLicense).getExtractedText())) {
						if (retval.containsKey(fromNonStdLicense.getLicenseId())) {
							fail("Duplicate license text values for "+fromNonStdLicense.getExtractedText());
						} else {
							retval.put(fromNonStdLicense.getLicenseId(), ((ExtractedLicenseInfo)toLicense).getLicenseId());
						}
					}
				}
				if (!retval.containsKey(fromNonStdLicense.getLicenseId())) {
					fail("No matching license found for "+fromNonStdLicense.getExtractedText());
				}
			}
		}
		return retval;
	}
	
	private SPDXFile[] createExpectedFiles(
			SPDXDocument masterDoc, SPDXDocument firstMergeDoc, SPDXDocument secondMergeDoc) throws InvalidSPDXAnalysisException{
		ArrayList<SPDXFile> retval = new ArrayList<SPDXFile>();
		SPDXFile[] masterFiles = masterDoc.getSpdxPackage().getFiles();
		SPDXFile[] firstDocFiles = firstMergeDoc.getSpdxPackage().getFiles();
		SPDXFile[] secondDocFiles = secondMergeDoc.getSpdxPackage().getFiles();
		//add master doc's files into list
		for(SPDXFile masterFile: masterFiles){
			retval.add(masterFile);
		}
		//add first doc's file into list
		for(SPDXFile firstMergeFile: firstDocFiles){
			String fileName = "lib-source/commons-somedepdendency-sources.jar";
			String sha1 = "e2b4e1c67a2d28fced849ee1bb76e7391b93f125";
			if(firstMergeFile.getName().equalsIgnoreCase(fileName) && firstMergeFile.getSha1().equals(sha1)){
				retval.add(firstMergeFile);//only the above file is different from files in the master doc; file license is Apache 2.0
			}
		}
		//prepare second doc's file into list; need to change concluded license and license info in file here
		ExtractedLicenseInfo[] secondMergeDocLics = secondMergeDoc.getExtractedLicenseInfos();
		ExtractedLicenseInfo clonedLic = (ExtractedLicenseInfo) secondMergeDocLics[0].clone();//only one extracted license in the second doc
		String newId = masterDoc.getNextLicenseRef();//master doc and first doc have the same extracted licenses
		clonedLic.setLicenseId(newId);
		secondMergeDocLics[0] = clonedLic;
		secondDocFiles[0].setSeenLicenses(secondMergeDocLics);
		secondDocFiles[0].setConcludedLicenses(clonedLic);
		retval.add(secondDocFiles[0]);
		
		SPDXFile[] result = new SPDXFile[retval.size()];
		retval.toArray(result);
		
		return result;
		
	}

}
