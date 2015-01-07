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
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.DOAPProject;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocument.SPDXPackage;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SPDXFile;

/**
 * @author Gang Ling
 *
 */
public class SpdxFileInfoMergerTest {

	static final String TEST_RDF_FILE_PATH = "TestFiles"+File.separator+"SPDXRdfExample-v1.2.rdf";
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
	 * Test method for {@link org.spdx.merge.SpdxFileInfoMerger#SpdxFileInfoMerger(org.spdx.rdfparser.SPDXDocument.SPDXPackage)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testSpdxFileInfoMerger() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXPackage packageInfo = doc1.getSpdxPackage();
		SpdxFileInfoMerger fileMerger = new SpdxFileInfoMerger(packageInfo, new SpdxLicenseMapper());
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxFileInfoMerger#mergeFileInfo(org.spdx.rdfparser.SPDXDocument[])}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testMergeFileInfo() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXPackage packageInfo = doc1.getSpdxPackage();
		ExtractedLicenseInfo[] subNonStdLics = doc2.getExtractedLicenseInfos();
		
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();
		ExtractedLicenseInfo clonedNonStdLic = (ExtractedLicenseInfo) subNonStdLics[0].clone();
		mapper.mappingNewNonStdLic(doc1, doc2, clonedNonStdLic);
		
		SpdxFileInfoMerger fileMerger = new SpdxFileInfoMerger(packageInfo, new SpdxLicenseMapper());
		SPDXDocument [] subDocs = new SPDXDocument[]{doc2};
		SPDXFile[] mergedResult = fileMerger.mergeFileInfo(subDocs);
		
		SPDXFile[] expectedResult = packageInfo.getFiles();
		int num = 0;
		for(int i = 0; i < mergedResult.length; i++){
			for(int j = 0; j < expectedResult.length; j++){
				if(mergedResult[i].equivalent(expectedResult[j])){
					num ++;
					break;
				}
			}
		}	
		assertEquals(3,num);
		assertEquals(expectedResult.length, mergedResult.length);
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxFileInfoMerger#checkDOAPProject(org.spdx.rdfparser.SPDXFile)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testCheckDOAPProject() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXPackage packageInfo = doc1.getSpdxPackage();
		SpdxFileInfoMerger fileMerger = new SpdxFileInfoMerger(packageInfo, new SpdxLicenseMapper());
		SPDXFile[] testFiles = packageInfo.getFiles();
		int num = 0;
		for(int i =0; i < testFiles.length; i++){
			if(fileMerger.checkDOAPProject(testFiles[i])){
				num ++;
			}
		}
		assertEquals(2, num);
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxFileInfoMerger#mergeDOAPInfo(org.spdx.rdfparser.DOAPProject[], org.spdx.rdfparser.DOAPProject[])}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testMergeDOAPInfo() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXPackage packageInfo = doc1.getSpdxPackage();
		SpdxFileInfoMerger fileMerger = new SpdxFileInfoMerger(packageInfo, new SpdxLicenseMapper());
		SPDXFile[] testFiles = packageInfo.getFiles();
		ArrayList<DOAPProject> testProjects = new ArrayList<DOAPProject>();
		for(int i = 0; i < testFiles.length; i++){
			if(fileMerger.checkDOAPProject(testFiles[i])){
				DOAPProject[] retval = testFiles[i].getArtifactOf();
				for(int k = 0; k < retval.length; k++){
					testProjects.add(retval[k]);
				}
			}
		}
		DOAPProject[] testProjects1 = new DOAPProject[testProjects.size()];
		testProjects.toArray(testProjects1);
		DOAPProject[] testProjects2 = new DOAPProject[testProjects.size()];
		testProjects.toArray(testProjects2);
		
		DOAPProject[] result = fileMerger.mergeDOAPInfo(testProjects1, testProjects2);
		assertEquals(testProjects1.length,result.length);
		assertEquals(2, result.length);
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxFileInfoMerger#cloneFiles(org.spdx.rdfparser.SPDXFile[])}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testCloneFiles() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXPackage packageInfo = doc1.getSpdxPackage();
		SpdxFileInfoMerger fileMerger = new SpdxFileInfoMerger(packageInfo, new SpdxLicenseMapper());
		SPDXFile[] testFiles = packageInfo.getFiles();
		SPDXFile[] clonedFiles = fileMerger.cloneFiles(testFiles);
		int num = 0;
		for(int i = 0; i < clonedFiles.length; i++){
			for(int j = 0; j < testFiles.length; j++){
				if(clonedFiles[i].equivalent(testFiles[j])){
					num++;
					break;
				}
			}
		}
		assertEquals(3, num);
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxFileInfoMerger#cloneDOAPProject(org.spdx.rdfparser.DOAPProject[])}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testCloneDOAPProject() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXPackage packageInfo = doc1.getSpdxPackage();
		SpdxFileInfoMerger fileMerger = new SpdxFileInfoMerger(packageInfo, new SpdxLicenseMapper());
		SPDXFile[] testFiles = packageInfo.getFiles();
		ArrayList<DOAPProject> testProjects = new ArrayList<DOAPProject>(); 
		for(int i = 0; i < testFiles.length; i++){
			if(fileMerger.checkDOAPProject(testFiles[i])){
				DOAPProject[] projects = testFiles[i].getArtifactOf();
				DOAPProject[] clonedProjects = fileMerger.cloneDOAPProject(projects);
				for(int j = 0; j < clonedProjects.length; j++){
					testProjects.add(clonedProjects[j]);
				}
			}
		}
		assertEquals(2,testProjects.size());
	}

}
