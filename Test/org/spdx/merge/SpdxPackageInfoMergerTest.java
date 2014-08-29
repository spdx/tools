/**
 * Copyright (c) 2014 Gang Ling.
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
import java.security.NoSuchAlgorithmException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.JavaSha1ChecksumGenerator;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXLicenseInfoFactory;
import org.spdx.rdfparser.SPDXNonStandardLicense;
import org.spdx.rdfparser.SPDXStandardLicense;
import org.spdx.rdfparser.VerificationCodeGenerator;
import org.spdx.rdfparser.SPDXDocument.SPDXPackage;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

/**
 * @author Gang Ling
 *
 */
public class SpdxPackageInfoMergerTest {
	
	static final String TEST_RDF_FILE_PATH = "TestFiles"+File.separator+"SPDXRdfExample-v1.2.rdf";
	private static final String STD_LIC_ID_Apache = "Apache-2.0";
	private static final String NonSTD_LIC_ID = "LicenseRef-1";
	File testFile;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.testFile = new File(TEST_RDF_FILE_PATH);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxPackageInfoMerger#SpdxPackageInfoMerger(org.spdx.rdfparser.SPDXDocument.SPDXPackage, org.spdx.rdfparser.SPDXDocument[])}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testSpdxPackageInfoMerger() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXPackage packageInfo = doc1.getSpdxPackage();
		SPDXDocument[] allDocs = new SPDXDocument[]{doc1,doc2};
		SpdxPackageInfoMerger packageMerger = new SpdxPackageInfoMerger(packageInfo, allDocs);
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxPackageInfoMerger#mergePackageInfo(org.spdx.rdfparser.SPDXDocument[], org.spdx.rdfparser.SPDXFile[])}.
	 */
	@Test
	public void testMergePackageInfo() {
		//the majority functions will be tested by test cases bellow. 
		//only set new declared license doesn't be tested, but it should work.
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxPackageInfoMerger#collectSkippedFiles()}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
	@Test
	public void testCollectSkippedFiles() throws IOException, InvalidSPDXAnalysisException, NoSuchAlgorithmException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXPackage packageInfo = doc1.getSpdxPackage();
		SPDXDocument[] allDocs = new SPDXDocument[]{doc1,doc2};
		SpdxPackageInfoMerger packageMerger = new SpdxPackageInfoMerger(packageInfo, allDocs);
		
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();
		SPDXNonStandardLicense[] subNonStdLics = doc2.getExtractedLicenseInfos();
		SPDXNonStandardLicense clonedNonStdLic = (SPDXNonStandardLicense) subNonStdLics[0].clone();
		mapper.mappingNewNonStdLic(doc1, doc2, clonedNonStdLic);
		
		SpdxFileInfoMerger fileMerger = new SpdxFileInfoMerger(packageInfo, mapper);
		SPDXDocument [] subDocs = new SPDXDocument[]{doc2};
		SPDXFile[] mergedResult = fileMerger.mergeFileInfo(subDocs);
		String[] skippedFiles = packageMerger.collectSkippedFiles();
		
		SpdxPackageVerificationCode expectedResult = packageInfo.getVerificationCode();
		
		VerificationCodeGenerator vg = new VerificationCodeGenerator(new JavaSha1ChecksumGenerator());
		SpdxPackageVerificationCode result = vg.generatePackageVerificationCode(mergedResult, skippedFiles);
		
		assertEquals(expectedResult.getValue(), result.getValue());

	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxPackageInfoMerger#collectLicsInFiles(org.spdx.rdfparser.SPDXFile[])}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 * @throws InvalidLicenseStringException 
	 */
	@Test
	public void testCollectLicsInFiles() throws IOException, InvalidSPDXAnalysisException, InvalidLicenseStringException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXPackage packageInfo = doc1.getSpdxPackage();
		SPDXDocument[] allDocs = new SPDXDocument[]{doc1,doc2};
		SpdxPackageInfoMerger packageMerger = new SpdxPackageInfoMerger(packageInfo, allDocs);
		
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();
		SPDXNonStandardLicense[] subNonStdLics = doc2.getExtractedLicenseInfos();
		SPDXNonStandardLicense clonedNonStdLic = (SPDXNonStandardLicense) subNonStdLics[0].clone();
		mapper.mappingNewNonStdLic(doc1, doc2, clonedNonStdLic);
		
		SpdxFileInfoMerger fileMerger = new SpdxFileInfoMerger(packageInfo, mapper);
		SPDXDocument [] subDocs = new SPDXDocument[]{doc2};
		SPDXFile[] mergedResult = fileMerger.mergeFileInfo(subDocs);
		
		SPDXLicenseInfo[] result = packageMerger.collectLicsInFiles(mergedResult);
		
		SPDXStandardLicense lic1 = SPDXLicenseInfoFactory.getStandardLicenseById(STD_LIC_ID_Apache);
		SPDXLicenseInfo lic2 = SPDXLicenseInfoFactory.parseSPDXLicenseString(NonSTD_LIC_ID);
		SPDXLicenseInfo[] expectedResult = new SPDXLicenseInfo[]{lic1,lic2};
		
		int num = 0;
		for(int i = 0; i < result.length; i++){
			for(int j = 0; j < expectedResult.length; j++){
				if(result[i].equals(expectedResult[j])){
					num++;
					break;
				}
			}
		}
		
		assertEquals(2,num);

	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxPackageInfoMerger#translateSubDelcaredLicsIntoComments(org.spdx.rdfparser.SPDXDocument[])}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testTranslateSubDelcaredLicsIntoComments() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXPackage packageInfo = doc1.getSpdxPackage();
		SPDXDocument[] allDocs = new SPDXDocument[]{doc1,doc2};
		SpdxPackageInfoMerger packageMerger = new SpdxPackageInfoMerger(packageInfo, allDocs);
		
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();
		SPDXNonStandardLicense[] subNonStdLics = doc2.getExtractedLicenseInfos();
		SPDXNonStandardLicense clonedNonStdLic = (SPDXNonStandardLicense) subNonStdLics[0].clone();
		mapper.mappingNewNonStdLic(doc1, doc2, clonedNonStdLic);
		
		SPDXDocument [] subDocs = new SPDXDocument[]{doc2};
		
		String result = packageMerger.translateSubDelcaredLicsIntoComments(subDocs);
		String exceptResult = packageInfo.getLicenseComment();
		
		assertEquals(exceptResult,result);

	}

}
