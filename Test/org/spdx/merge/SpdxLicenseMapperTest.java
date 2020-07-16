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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ConjunctiveLicenseSet;
import org.spdx.rdfparser.license.DisjunctiveLicenseSet;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxPackage;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Gang Ling
 *
 */
public class SpdxLicenseMapperTest {

	static final String TEST_RDF_FILE_PATH = "TestFiles"+File.separator+"SPDXRdfExample-v2.0.rdf";
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
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#mappingNonStdLic(org.spdx.rdfparser.SpdxDocument, org.spdx.rdfparser.license.ExtractedLicenseInfo)}.
	 * @throws InvalidSPDXAnalysisException
	 * @throws IOException
	 */
	@Test
	public void testMappingNewNonStdLic() throws IOException, InvalidSPDXAnalysisException {
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc3 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH3);
		ExtractedLicenseInfo[] subNonStdLics = doc3.getExtractedLicenseInfos();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();

		ExtractedLicenseInfo clonedNonStdLic = (ExtractedLicenseInfo) subNonStdLics[0].clone();
		mapper.mappingNewNonStdLic(doc1, doc3, clonedNonStdLic);
		if(clonedNonStdLic.equals(subNonStdLics[0]) ){
			fail();
		}
		assertEquals(clonedNonStdLic.getLicenseId(),"LicenseRef-5");
		assertFalse(mapper.isNonStdLicIdMapEmpty());
	}


	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#replaceNonStdLicInFile(org.spdx.rdfparser.SpdxDocument, org.spdx.rdfparser.SpdxFile)}.
	 * @throws InvalidSPDXAnalysisException
	 * @throws IOException
	 */
	@Test
	public void testReplaceNonStdLicInFile() throws IOException, InvalidSPDXAnalysisException {
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc3 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH3);
		ExtractedLicenseInfo[] subNonStdLics = doc3.getExtractedLicenseInfos();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();

		ExtractedLicenseInfo clonedNonStdLic = (ExtractedLicenseInfo) subNonStdLics[0].clone();
		mapper.mappingNewNonStdLic(doc1, doc3, clonedNonStdLic);//new clonedNonStdLic id = 5
		subNonStdLics[0] = clonedNonStdLic;//replace the lics
		doc3.setExtractedLicenseInfos(subNonStdLics);

//		SpdxFile[] subFiles = doc3.getSpdxPackage().getFiles();
		List<SpdxFile> subFilesList = doc3.getDocumentContainer().findAllFiles();
		String fileName = "lib-source/mergetest2.jar";
		String sha1 = "5ab4e1e67a2d28fced849ee1bb76e7391b93f125";
		AnyLicenseInfo[] mappedLicsInFile = null;
		AnyLicenseInfo subConcludedLicInFile = null;
		AnyLicenseInfo concludedLicense = null;
		for(int i = 0; i < subFilesList.size(); i++){
			if(subFilesList.get(i).getName().equalsIgnoreCase(fileName) && subFilesList.get(i).getSha1().equals(sha1)){
				mapper.replaceNonStdLicInFile(doc3, subFilesList.get(i));
				mappedLicsInFile = subFilesList.get(i).getLicenseInfoFromFiles();
				subConcludedLicInFile = subFilesList.get(i).getLicenseConcluded();
				concludedLicense = mapper.mapLicenseInfo(doc3,subConcludedLicInFile);
			}
		}
		boolean licMappered = false;
		for(int j = 0; j < mappedLicsInFile.length; j++){
			if(mappedLicsInFile[j].equals(clonedNonStdLic)) {
                ;
            }
			licMappered = true;
		}
		if(!licMappered){
			fail();
		}
		AnyLicenseInfo expectConcludedLicnse = setLicense(subConcludedLicInFile,subNonStdLics[0],clonedNonStdLic);
		assertEquals(expectConcludedLicnse, concludedLicense);

	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#mapNonStdLicInMap(org.spdx.rdfparser.SpdxDocument, org.spdx.rdfparser.license.AnyLicenseInfo)}.
	 * @throws InvalidSPDXAnalysisException
	 * @throws IOException
	 */
	@Test
	public void testMapNonStdLicInMap() throws IOException, InvalidSPDXAnalysisException {
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH2);
		ExtractedLicenseInfo[] subNonStdLics = doc2.getExtractedLicenseInfos();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();

		ExtractedLicenseInfo clonedNonStdLic = (ExtractedLicenseInfo) subNonStdLics[0].clone();//input non-standard lic
		mapper.mappingNewNonStdLic(doc1, doc2, clonedNonStdLic);//new clonedNonStdLic id = 5

		AnyLicenseInfo license1 = subNonStdLics[1].clone();//license1 doesn't in map
		license1 = mapper.mapNonStdLicInMap(doc2, license1);
		assertEquals(subNonStdLics[1],license1);

		AnyLicenseInfo license2 = subNonStdLics[0].clone();//license2 does in map
		license2 = mapper.mapNonStdLicInMap(doc2, license2);
		assertEquals(clonedNonStdLic, license2);

	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#mapLicenseInfo(org.spdx.rdfparser.SpdxDocument, org.spdx.rdfparser.license.AnyLicenseInfo)}.
	 * @throws InvalidSPDXAnalysisException
	 * @throws IOException
	 * @throws InvalidLicenseStringException
	 */
	@Test
	public void testMapLicenseInfo() throws IOException, InvalidSPDXAnalysisException, InvalidLicenseStringException {
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc3 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH2);
		ExtractedLicenseInfo[] subNonStdLics = doc3.getExtractedLicenseInfos();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();

		ExtractedLicenseInfo clonedNonStdLic = (ExtractedLicenseInfo) subNonStdLics[0].clone();//input non-standard lic
		mapper.mappingNewNonStdLic(doc1, doc3, clonedNonStdLic);//new clonedNonStdLic id = 5

		List<SpdxPackage> packageList = doc3.getDocumentContainer().findAllPackages();
		List<AnyLicenseInfo> mappedLicenseList = Lists.newArrayList();
		List<AnyLicenseInfo> expectedLicenseList = Lists.newArrayList();

		for(int i = 0; i < packageList.size(); i++){
			mappedLicenseList.add(mapper.mapLicenseInfo(doc3, packageList.get(i).getLicenseDeclared().clone()));
			expectedLicenseList.add(setLicense(packageList.get(i).getLicenseDeclared().clone(),subNonStdLics[0],clonedNonStdLic));
		}

		assertEquals(expectedLicenseList, mappedLicenseList);

	}

	public AnyLicenseInfo setLicense(AnyLicenseInfo license, ExtractedLicenseInfo orignal, ExtractedLicenseInfo mapped){
		if(license instanceof ConjunctiveLicenseSet){
			AnyLicenseInfo[] members = ((ConjunctiveLicenseSet) license).getMembers();
			AnyLicenseInfo[] mappedMembers = new AnyLicenseInfo[members.length];
			for(int i = 0; i < members.length; i++){
				mappedMembers[i] = setLicense(members[i],orignal,mapped);
			}
			return new ConjunctiveLicenseSet(mappedMembers);
		}
		else if(license instanceof DisjunctiveLicenseSet){
			AnyLicenseInfo[] members = ((DisjunctiveLicenseSet) license).getMembers();
			AnyLicenseInfo[] mappedMembers = new AnyLicenseInfo[members.length];
			for(int q = 0; q < members.length; q++ ){
				mappedMembers[q] = setLicense(members[q],orignal,mapped);
			}
			return new DisjunctiveLicenseSet(mappedMembers);
		}else if(license instanceof ExtractedLicenseInfo){
			if(license.equals(orignal)){
				license = mapped;
			}
			return license;
		}
		return license;
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#docInNonStdLicIdMap(org.spdx.rdfparser.SpdxDocument)}.
	 * @throws InvalidSPDXAnalysisException
	 * @throws IOException
	 */
	@Test
	public void testDocInNonStdLicIdMap() throws IOException, InvalidSPDXAnalysisException {
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc3 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH3);
		ExtractedLicenseInfo[] subNonStdLics = doc3.getExtractedLicenseInfos();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();

		ExtractedLicenseInfo clonedNonStdLic = (ExtractedLicenseInfo) subNonStdLics[0].clone();
		mapper.mappingNewNonStdLic(doc1, doc3, clonedNonStdLic);

		assertTrue(mapper.docInNonStdLicIdMap(doc3));
	}

	/**
	 * Test method for {@link org.spdx.merge.SpdxLicenseMapper#foundNonStdLicIds(org.spdx.rdfparser.SpdxDocument)}.
	 * @throws InvalidSPDXAnalysisException
	 * @throws IOException
	 */
	@Test
	public void testFoundNonStdLicIds() throws IOException, InvalidSPDXAnalysisException {
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc3 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH3);
		ExtractedLicenseInfo[] subNonStdLics = doc3.getExtractedLicenseInfos();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();

		ExtractedLicenseInfo clonedNonStdLic = (ExtractedLicenseInfo) subNonStdLics[0].clone();
		mapper.mappingNewNonStdLic(doc1, doc3, clonedNonStdLic);

		Map<AnyLicenseInfo, AnyLicenseInfo> interalMap = mapper.foundInterMap(doc3);
		Map<AnyLicenseInfo,AnyLicenseInfo> expected = Maps.newHashMap();
		String NewNonStdLicId = doc1.getDocumentContainer().getNextLicenseRef();
		clonedNonStdLic.setLicenseId(NewNonStdLicId);
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
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
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
		SpdxDocument doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		SpdxDocument doc3 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH3);
		ExtractedLicenseInfo[] subNonStdLics = doc3.getExtractedLicenseInfos().clone();
		SpdxLicenseMapper mapper = new SpdxLicenseMapper();

		@SuppressWarnings("unused")
		ExtractedLicenseInfo resultNonStdLic = mapper.mappingNewNonStdLic(doc1, doc3, subNonStdLics[0]);
		mapper.clearNonStdLicIdMap();
		assertTrue(mapper.isNonStdLicIdMapEmpty());
	}

}
