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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SPDXNonStandardLicense;

/**
 * @author Gang Ling
 *
 */
public class SpdxLicenseInfoMergerTest {

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
	 * Test method for {@link org.spdx.merge.SpdxLicenseInfoMerger#SpdxLicenseInfoMerger(org.spdx.rdfparser.SPDXDocument)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testSpdxLicenseInfoMerger() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxLicenseInfoMerger licMerger = new SpdxLicenseInfoMerger(doc1, new SpdxLicenseMapper());
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseInfoMerger#mergeNonStdLic(org.spdx.rdfparser.SPDXDocument[])}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testMergeNonStdLic() throws IOException, InvalidSPDXAnalysisException {
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxLicenseInfoMerger licMerger = new SpdxLicenseInfoMerger(doc1, new SpdxLicenseMapper());
		SPDXDocument [] subDocs = new SPDXDocument[]{doc2};
		SPDXNonStandardLicense[] mergedLicense = licMerger.mergeNonStdLic(subDocs);
		SPDXNonStandardLicense[] exceptedResult = doc1.getExtractedLicenseInfos();
		int num = 0;
		for(int i = 0; i < mergedLicense.length; i++){
			for(int j = 0; j < exceptedResult.length;j++){
				if(mergedLicense[i].equals(exceptedResult[j])){
					num++;
					break;
				}
			}
		}
		assertEquals(num,mergedLicense.length);
		
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseInfoMerger#cloneNonStdLic(org.spdx.rdfparser.SPDXNonStandardLicense[])}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	@Test
	public void testCloneNonStdLic() throws IOException, InvalidSPDXAnalysisException{
		SPDXDocument doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SPDXDocument doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxLicenseInfoMerger licMerger = new SpdxLicenseInfoMerger(doc1, new SpdxLicenseMapper());
		SPDXNonStandardLicense[] exceptedResult = doc2.getExtractedLicenseInfos(); 
		SPDXNonStandardLicense[] clonedLicense = licMerger.cloneNonStdLic(exceptedResult);
		int num = 0;
		for(int i = 0; i < clonedLicense.length; i++){
			for(int j = 0; j < exceptedResult.length;j++){
				if(clonedLicense[i].equals(exceptedResult[j])){
					num++;
					break;
				}
			}
		}
		assertEquals(num,clonedLicense.length);
	}

}
