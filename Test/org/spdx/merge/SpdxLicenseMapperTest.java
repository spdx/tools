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
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXNonStandardLicense;

/**
 * @author Gang Ling
 *
 */
public class SpdxLicenseMapperTest {

	static final String TEST_RDF_FILE_PATH = "TestFiles"+File.separator+"SPDXRdfExample.rdf";
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
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#SpdxLicenseMapper(org.spdx.rdfparser.SPDXDocument)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testSpdxLicenseMapper() throws IOException, InvalidSPDXAnalysisException{
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();
		assertTrue(mapper.isNonStdLicIdMapEmpty());
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#mappingNonStdLic(org.spdx.rdfparser.SPDXDocument, org.spdx.rdfparser.SPDXNonStandardLicense)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testMappingNonStdLic() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXNonStandardLicense[] subNonStdLics = doc2.getExtractedLicenseInfos();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();

		SPDXNonStandardLicense clonedNonStdLic = (SPDXNonStandardLicense) subNonStdLics[0].clone();
		mapper.mappingNonStdLic(doc1, doc2, clonedNonStdLic);
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
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXNonStandardLicense[] subNonStdLics = doc2.getExtractedLicenseInfos();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();

		SPDXNonStandardLicense clonedNonStdLic = (SPDXNonStandardLicense) subNonStdLics[1].clone();//input non-standard lic id = 1
		mapper.mappingNonStdLic(doc1, doc2, clonedNonStdLic);//new clonedNonStdLic id = 5
		subNonStdLics[1] = clonedNonStdLic;//replace the lics 
		doc2.setExtractedLicenseInfos(subNonStdLics);

		SPDXFile[] subFiles = doc2.getSpdxPackage().getFiles();
		String fileName = "Jenna-2.6.3/jena-2.6.3-sources.jar";
		String sha1 = "3ab4e1c67a2d28fced849ee1bb76e7391b93f125";
		SPDXLicenseInfo[] mappedLicsInFile = null;
		for(int i = 0; i < subFiles.length; i++){
			if(subFiles[i].getName().equalsIgnoreCase(fileName) && subFiles[i].getSha1().equals(sha1)){
				mapper.replaceNonStdLicInFile(doc2, subFiles[i]);
				mappedLicsInFile = subFiles[i].getSeenLicenses();
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
		
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#mapNonStdLicInMap(org.spdx.rdfparser.SPDXDocument, org.spdx.rdfparser.SPDXLicenseInfo)}.
	 */
	@Test
	public void testMapNonStdLicInMap() {
	
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#mapLicenseInfo(org.spdx.rdfparser.SPDXDocument, org.spdx.rdfparser.SPDXLicenseInfo)}.
	 */
	@Test
	public void testMapLicenseInfo() {
		
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#docInNonStdLicIdMap(org.spdx.rdfparser.SPDXDocument)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testDocInNonStdLicIdMap() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXNonStandardLicense[] subNonStdLics = doc2.getExtractedLicenseInfos();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();
		
		SPDXNonStandardLicense clonedNonStdLic = (SPDXNonStandardLicense) subNonStdLics[0].clone();
		mapper.mappingNonStdLic(doc1, doc2, clonedNonStdLic);
		
		assertTrue(mapper.docInNonStdLicIdMap(doc2));
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#foundNonStdLicIds(org.spdx.rdfparser.SPDXDocument)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testFoundNonStdLicIds() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXNonStandardLicense[] subNonStdLics = doc2.getExtractedLicenseInfos();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();
		
		SPDXNonStandardLicense clonedNonStdLic = (SPDXNonStandardLicense) subNonStdLics[0].clone();
		mapper.mappingNonStdLic(doc1, doc2, clonedNonStdLic);
		
		HashMap<SPDXLicenseInfo, SPDXLicenseInfo> interalMap = mapper.foundInterMap(doc2);
		HashMap<SPDXLicenseInfo,SPDXLicenseInfo> retval = new HashMap<SPDXLicenseInfo, SPDXLicenseInfo>();
		String NewNonStdLicId = doc1.getNextLicenseRef();
		clonedNonStdLic.setId(NewNonStdLicId);
		retval.put(subNonStdLics[0], clonedNonStdLic);
		
		assertEquals(interalMap,retval);	
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#isNonStdLicIdMapEmpty()}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testIsNonStdLicIdMapEmpty() throws IOException, InvalidSPDXAnalysisException {
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
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXNonStandardLicense[] subNonStdLics = doc2.getExtractedLicenseInfos().clone();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();

		SPDXNonStandardLicense resultNonStdLic = mapper.mappingNonStdLic(doc1, doc2, subNonStdLics[0]);
		mapper.clearNonStdLicIdMap();
		assertTrue(mapper.isNonStdLicIdMapEmpty());
	}

}
