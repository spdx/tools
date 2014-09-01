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
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXConjunctiveLicenseSet;
import org.spdx.rdfparser.SPDXDisjunctiveLicenseSet;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXNonStandardLicense;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

/**
 * @author Gang Ling
 *
 */
public class SpdxLicenseMapperTest {

	static final String TEST_RDF_FILE_PATH = "TestFiles"+File.separator+"SPDXRdfExample-v1.2.rdf";
	static final String TEST_RDF_FILE_PATH2 = "TestFiles"+File.separator+"SPDXSpreadsheetMergeTest1.rdf";
	static final String TEST_RDF_FILE_PATH3 = "TestFiles"+File.separator+"SPDXSpreadsheetMergeTest2.rdf";
	File testFile;
	File testFile2;
	File testFile3;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.testFile = new File(TEST_RDF_FILE_PATH);
		this.testFile2 = new File(TEST_RDF_FILE_PATH2);
		this.testFile3 = new File(TEST_RDF_FILE_PATH3);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * 
	 */
	@Test
	public void testSpdxLicenseMapper(){
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#mappingNonStdLic(org.spdx.rdfparser.SPDXDocument, org.spdx.rdfparser.SPDXNonStandardLicense)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testMappingNewNonStdLic() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc3 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH3);
		SPDXNonStandardLicense[] subNonStdLics = doc3.getExtractedLicenseInfos();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();

		SPDXNonStandardLicense clonedNonStdLic = (SPDXNonStandardLicense) subNonStdLics[0].clone();
		mapper.mappingNewNonStdLic(doc1, doc3, clonedNonStdLic);
		if(clonedNonStdLic.equals(subNonStdLics[0]) ){
			fail();
		}
		assertEquals(clonedNonStdLic.getId(),"LicenseRef-5");
		assertFalse(mapper.isNonStdLicIdMapEmpty());
	}
	

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#replaceNonStdLicInFile(org.spdx.rdfparser.SPDXDocument, org.spdx.rdfparser.SPDXFile)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testReplaceNonStdLicInFile() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc3 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH3);
		SPDXNonStandardLicense[] subNonStdLics = doc3.getExtractedLicenseInfos();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();

		SPDXNonStandardLicense clonedNonStdLic = (SPDXNonStandardLicense) subNonStdLics[0].clone();
		mapper.mappingNewNonStdLic(doc1, doc3, clonedNonStdLic);//new clonedNonStdLic id = 5
		subNonStdLics[0] = clonedNonStdLic;//replace the lics 
		doc3.setExtractedLicenseInfos(subNonStdLics);

		SPDXFile[] subFiles = doc3.getSpdxPackage().getFiles();
		String fileName = "lib-source/mergetest2.jar";
		String sha1 = "5ab4e1e67a2d28fced849ee1bb76e7391b93f125";
		SPDXLicenseInfo[] mappedLicsInFile = null;
		SPDXLicenseInfo subConcludedLicInFile = null;
		SPDXLicenseInfo concludedLicense = null;
		for(int i = 0; i < subFiles.length; i++){
			if(subFiles[i].getName().equalsIgnoreCase(fileName) && subFiles[i].getSha1().equals(sha1)){
				mapper.replaceNonStdLicInFile(doc3, subFiles[i]);
				mappedLicsInFile = subFiles[i].getSeenLicenses();
				subConcludedLicInFile = subFiles[i].getConcludedLicenses();
				concludedLicense = mapper.mapLicenseInfo(doc3,subConcludedLicInFile);
			}			
		}
		boolean licMappered = false;
		for(int j = 0; j < mappedLicsInFile.length; j++){
			if(mappedLicsInFile[j].equals(clonedNonStdLic));
			licMappered = true;
		}
		if(!licMappered){
			fail();
		}
		SPDXLicenseInfo expectConcludedLicnse = setLicense(subConcludedLicInFile,subNonStdLics[0],clonedNonStdLic);
		assertEquals(expectConcludedLicnse, concludedLicense);

	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#mapNonStdLicInMap(org.spdx.rdfparser.SPDXDocument, org.spdx.rdfparser.SPDXLicenseInfo)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testMapNonStdLicInMap() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH2);
		SPDXNonStandardLicense[] subNonStdLics = doc2.getExtractedLicenseInfos();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();

		SPDXNonStandardLicense clonedNonStdLic = (SPDXNonStandardLicense) subNonStdLics[0].clone();//input non-standard lic
		mapper.mappingNewNonStdLic(doc1, doc2, clonedNonStdLic);//new clonedNonStdLic id = 5

		SPDXLicenseInfo license1 = subNonStdLics[1].clone();//license1 doesn't in map
		license1 = mapper.mapNonStdLicInMap(doc2, license1);
		assertEquals(subNonStdLics[1],license1);
		
		SPDXLicenseInfo license2 = subNonStdLics[0].clone();//license2 does in map
		license2 = mapper.mapNonStdLicInMap(doc2, license2);
		assertEquals(clonedNonStdLic, license2);
		
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#mapLicenseInfo(org.spdx.rdfparser.SPDXDocument, org.spdx.rdfparser.SPDXLicenseInfo)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 * @throws InvalidLicenseStringException 
	 */
	@Test
	public void testMapLicenseInfo() throws IOException, InvalidSPDXAnalysisException, InvalidLicenseStringException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc3 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH2);
		SPDXNonStandardLicense[] subNonStdLics = doc3.getExtractedLicenseInfos();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();

		SPDXNonStandardLicense clonedNonStdLic = (SPDXNonStandardLicense) subNonStdLics[0].clone();//input non-standard lic
		mapper.mappingNewNonStdLic(doc1, doc3, clonedNonStdLic);//new clonedNonStdLic id = 5		
		
		SPDXLicenseInfo mappedLicense = mapper.mapLicenseInfo(doc3, doc3.getSpdxPackage().getDeclaredLicense().clone());
		SPDXLicenseInfo expectedLicense = setLicense(doc3.getSpdxPackage().getDeclaredLicense().clone(),subNonStdLics[0],clonedNonStdLic);
		assertEquals(expectedLicense, mappedLicense);

	}
	
	public SPDXLicenseInfo setLicense(SPDXLicenseInfo license, SPDXNonStandardLicense orignal, SPDXNonStandardLicense mapped){
		if(license instanceof SPDXConjunctiveLicenseSet){
			SPDXLicenseInfo[] members = ((SPDXConjunctiveLicenseSet) license).getSPDXLicenseInfos();
			SPDXLicenseInfo[] mappedMembers = new SPDXLicenseInfo[members.length];
			for(int i = 0; i < members.length; i++){
				mappedMembers[i] = setLicense(members[i],orignal,mapped);
			}
			return new SPDXConjunctiveLicenseSet(mappedMembers);
		}
		else if(license instanceof SPDXDisjunctiveLicenseSet){
			SPDXLicenseInfo[] members = ((SPDXDisjunctiveLicenseSet) license).getSPDXLicenseInfos();
			SPDXLicenseInfo[] mappedMembers = new SPDXLicenseInfo[members.length];
			for(int q = 0; q < members.length; q++ ){
				mappedMembers[q] = setLicense(members[q],orignal,mapped);
			}
			return new SPDXDisjunctiveLicenseSet(mappedMembers);
		}else if(license instanceof SPDXNonStandardLicense){
			if(license.equals(orignal)){
				license = mapped;
			}
			return license;
		}
		return license;	
	}
	
	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#docInNonStdLicIdMap(org.spdx.rdfparser.SPDXDocument)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testDocInNonStdLicIdMap() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc3 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH3);
		SPDXNonStandardLicense[] subNonStdLics = doc3.getExtractedLicenseInfos();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();
		
		SPDXNonStandardLicense clonedNonStdLic = (SPDXNonStandardLicense) subNonStdLics[0].clone();
		mapper.mappingNewNonStdLic(doc1, doc3, clonedNonStdLic);
		
		assertTrue(mapper.docInNonStdLicIdMap(doc3));
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#foundNonStdLicIds(org.spdx.rdfparser.SPDXDocument)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testFoundNonStdLicIds() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc3 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH3);
		SPDXNonStandardLicense[] subNonStdLics = doc3.getExtractedLicenseInfos();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();
		
		SPDXNonStandardLicense clonedNonStdLic = (SPDXNonStandardLicense) subNonStdLics[0].clone();
		mapper.mappingNewNonStdLic(doc1, doc3, clonedNonStdLic);
		
		HashMap<SPDXLicenseInfo, SPDXLicenseInfo> interalMap = mapper.foundInterMap(doc3);
		HashMap<SPDXLicenseInfo,SPDXLicenseInfo> expected = new HashMap<SPDXLicenseInfo, SPDXLicenseInfo>();
		String NewNonStdLicId = doc1.getNextLicenseRef();
		clonedNonStdLic.setId(NewNonStdLicId);
		expected.put(subNonStdLics[0], clonedNonStdLic);
		
		assertEquals(interalMap,expected);	
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#isNonStdLicIdMapEmpty()}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testIsNonStdLicIdMapEmpty() throws IOException, InvalidSPDXAnalysisException {
		@SuppressWarnings("unused")
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();
		assertTrue(mapper.isNonStdLicIdMapEmpty());
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#clearNonStdLicIdMap()}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testClearNonStdLicIdMap() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc3 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH3);
		SPDXNonStandardLicense[] subNonStdLics = doc3.getExtractedLicenseInfos().clone();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();

		@SuppressWarnings("unused")
		SPDXNonStandardLicense resultNonStdLic = mapper.mappingNewNonStdLic(doc1, doc3, subNonStdLics[0]);
		mapper.clearNonStdLicIdMap();
		assertTrue(mapper.isNonStdLicIdMapEmpty());
	}

}
