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
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXNonStandardLicense;

/**
 * @author Gang Ling
 *
 */
public class SpdxLicenseMapperTest {

	static final String TEST_RDF_FILE_PATH = "TestFiles"+File.separator+"SPDXRdfExample.rdf";
	File testFile;
	HashMap<SPDXDocument, HashMap<SPDXLicenseInfo, SPDXLicenseInfo>> nonStdLicIdMap = new HashMap<SPDXDocument, HashMap< SPDXLicenseInfo, SPDXLicenseInfo>>();
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.testFile = new File(TEST_RDF_FILE_PATH);
		initializeMap();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	public  void initializeMap() throws IOException, InvalidSPDXAnalysisException{
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXNonStandardLicense[] doc1NonStdLics = doc1.getExtractedLicenseInfos();
		HashMap<SPDXLicenseInfo,SPDXLicenseInfo> interMap = new HashMap<SPDXLicenseInfo,SPDXLicenseInfo>();
		for(int i = 0; i < doc1NonStdLics.length; i++){
			interMap.put(doc1NonStdLics[i], doc1NonStdLics[i]);
		}
		if(!nonStdLicIdMap.isEmpty()){
			nonStdLicIdMap.clear();
		}
		nonStdLicIdMap.put(doc1, interMap);
	}
	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#SpdxLicenseMapper(org.spdx.rdfparser.SPDXDocument)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testSpdxLicenseMapper() throws IOException, InvalidSPDXAnalysisException{
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxLicenseMapper mapper = new SpdxLicenseMapper(doc1);
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
		SpdxLicenseMapper mapper = new SpdxLicenseMapper(doc1);
		SPDXNonStandardLicense resultNonStdLic = mapper.mappingNonStdLic(doc2, subNonStdLics[0]);
		assertEquals(subNonStdLics[0].getId(), resultNonStdLic.getId());
		assertFalse(mapper.isNonStdLicIdMapEmpty());
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#replaceNonStdLicInFile(org.spdx.rdfparser.SPDXDocument, org.spdx.rdfparser.SPDXFile)}.
	 */
	@Test
	public void testReplaceNonStdLicInFile() {
		
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
	 */
	@Test
	public void testDocInNonStdLicIdMap() {
	
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#foundNonStdLicIds(org.spdx.rdfparser.SPDXDocument)}.
	 */
	@Test
	public void testFoundNonStdLicIds() {
		
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#isNonStdLicIdMapEmpty()}.
	 */
	@Test
	public void testIsNonStdLicIdMapEmpty() {

	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#clearNonStdLicIdMap()}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testClearNonStdLicIdMap() throws IOException, InvalidSPDXAnalysisException {
		initializeMap();
		nonStdLicIdMap.clear();
		assertTrue(nonStdLicIdMap.isEmpty());
	}

}
