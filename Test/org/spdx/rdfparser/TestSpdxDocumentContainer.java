/**
 * Copyright (c) 2015 Source Auditor Inc.
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
package org.spdx.rdfparser;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.model.ExternalDocumentRef;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxElement;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxPackage;
import org.spdx.rdfparser.model.UnitTestHelper;
import org.spdx.rdfparser.model.Relationship.RelationshipType;

import com.hp.hpl.jena.rdf.model.Model;


/**
 * @author Gary
 *
 */
public class TestSpdxDocumentContainer {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
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

	/**
	 * Test method for {@link org.spdx.rdfparser.SpdxDocumentContainer#SpdxDocumentContainer(com.hp.hpl.jena.rdf.model.Model)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSpdxDocumentContainerModel() throws InvalidSPDXAnalysisException {
		String docUri = "http://www.spdx.org/spdxdocs/uniquenameofsomesort";
		String version = "SPDX-1.2";
		SpdxDocumentContainer container = new SpdxDocumentContainer(docUri, version);
		assertEquals(docUri + "#", container.getDocumentNamespace());
		assertEquals(version, container.getSpdxDocument().getSpecVersion());
		
		Model model = container.getModel();
		SpdxDocumentContainer container2 = new SpdxDocumentContainer(model);
		assertEquals(version, container2.getSpdxDocument().getSpecVersion());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SpdxDocumentContainer#SpdxDocumentContainer(java.lang.String, java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSpdxDocumentContainerStringString() throws InvalidSPDXAnalysisException {
		String docUri = "http://www.spdx.org/spdxdocs/uniquenameofsomesort";
		String version = "SPDX-1.2";
		SpdxDocumentContainer container = new SpdxDocumentContainer(docUri, version);
		assertEquals(docUri + "#", container.getDocumentNamespace());
		assertEquals(version, container.getSpdxDocument().getSpecVersion());
	}

	@Test
	public void testGetDocumentNamespace() throws InvalidSPDXAnalysisException {
		String docUri = "http://www.spdx.org/spdxdocs/uniquenameofsomesort";
		// without a part
		SpdxDocumentContainer doc = new SpdxDocumentContainer(docUri);
		String result = doc.getDocumentNamespace();
		// with part
		assertEquals(docUri + "#", result);
		doc = new SpdxDocumentContainer(docUri + "#SPDXDocument");
		result = doc.getDocumentNamespace();
		assertEquals(docUri + "#", result);	
	}

	@Test
	public void testGetElementRefNumber() {
		int refNum1 = 5532;
		int refNum2 = 12;
		String ref1 = SpdxRdfConstants.SPDX_ELEMENT_REF_PRENUM + String.valueOf(refNum1);
		String ref2 = SpdxRdfConstants.SPDX_ELEMENT_REF_PRENUM + String.valueOf(refNum2);
		String invalidRef = SpdxRdfConstants.SPDX_ELEMENT_REF_PRENUM + "xaf234";
		int result = SpdxDocumentContainer.getElementRefNumber(ref1);
		assertEquals(refNum1, result);
		result = SpdxDocumentContainer.getElementRefNumber(ref2);
		assertEquals(refNum2, result);
		result = SpdxDocumentContainer.getElementRefNumber(invalidRef);
		assertEquals(-1, result);		
	}

	@Test
	public void testGetNextSpdxRef() throws InvalidSPDXAnalysisException {
		String docUri = "http://www.spdx.org/spdxdocs/uniquenameofsomesort";
		SpdxDocumentContainer doc = new SpdxDocumentContainer(docUri);
		String nextSpdxElementRef = doc.getNextSpdxElementRef();
		String expected = SpdxRdfConstants.SPDX_ELEMENT_REF_PRENUM + String.valueOf(1);
		assertEquals(expected, nextSpdxElementRef);
		nextSpdxElementRef = doc.getNextSpdxElementRef();
		expected = SpdxRdfConstants.SPDX_ELEMENT_REF_PRENUM + String.valueOf(2);
		assertEquals(expected, nextSpdxElementRef);
		SpdxElement element = new SpdxElement("Name", "Comment", null, null);
		String elementRef5 = SpdxRdfConstants.SPDX_ELEMENT_REF_PRENUM + String.valueOf(5);
		element.setId(elementRef5);
		element.createResource(doc);
		nextSpdxElementRef = doc.getNextSpdxElementRef();
		expected = SpdxRdfConstants.SPDX_ELEMENT_REF_PRENUM + String.valueOf(6);
		// see if it survives in the model
		// the highest spdx ref will be the element
		Model model = doc.getModel();
		SpdxDocumentContainer doc2 = new SpdxDocumentContainer(model);
		nextSpdxElementRef = doc2.getNextSpdxElementRef();
		assertEquals(expected, nextSpdxElementRef);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SpdxDocumentContainer#formNonStandardLicenseID(int)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testFormNonStandardLicenseID() throws InvalidSPDXAnalysisException {
		String expected = SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM + "44";
		String result = SpdxDocumentContainer.formNonStandardLicenseID(44);
		assertEquals(expected, result);
	}


	/**
	 * Test method for {@link org.spdx.rdfparser.SpdxDocumentContainer#getNextLicenseRef()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetNextLicenseRef() throws InvalidSPDXAnalysisException {
		String docUri = "http://www.spdx.org/spdxdocs/uniquenameofsomesort";
		SpdxDocumentContainer doc = new SpdxDocumentContainer(docUri);
		String expected = SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM + String.valueOf(1);
		assertEquals(expected, doc.getNextLicenseRef());
		expected = SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM + String.valueOf(2);
		assertEquals(expected, doc.getNextLicenseRef());
		int licenseRefNum = 545;
		ExtractedLicenseInfo lic = new ExtractedLicenseInfo(SpdxDocumentContainer.formNonStandardLicenseID(licenseRefNum), "License Text");
		doc.setExtractedLicenseInfos(new ExtractedLicenseInfo[] {lic});
		expected = SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM + String.valueOf(licenseRefNum + 1);
		assertEquals(expected, doc.getNextLicenseRef());		
	}


	/**
	 * Test method for {@link org.spdx.rdfparser.SpdxDocumentContainer#formSpdxElementRef(int)}.
	 */
	@Test
	public void testFormSpdxElementRef() {
		String expected = SpdxRdfConstants.SPDX_ELEMENT_REF_PRENUM + "55";
		String result = SpdxDocumentContainer.formSpdxElementRef(55);
		assertEquals(expected, result);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SpdxDocumentContainer#addNewExtractedLicenseInfo(java.lang.String)}.
	 */
	@Test
	public void testAddNonStandardLicense() throws InvalidSPDXAnalysisException {
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		SpdxDocumentContainer doc = new SpdxDocumentContainer(testUri,"SPDX-2.0");
		String NON_STD_LIC_TEXT1 = "licenseText1";
		String NON_STD_LIC_TEXT2 = "LicenseText2";
		ExtractedLicenseInfo[] emptyLic = doc.getSpdxDocument().getExtractedLicenseInfos();
		assertEquals(0,emptyLic.length);
		ExtractedLicenseInfo lic1 = doc.addNewExtractedLicenseInfo(NON_STD_LIC_TEXT1);
		String licID1 = SpdxDocumentContainer.formNonStandardLicenseID(1);
		assertEquals(licID1, lic1.getLicenseId());
		assertEquals(NON_STD_LIC_TEXT1, lic1.getExtractedText());
		ExtractedLicenseInfo[] licresult1 = doc.getSpdxDocument().getExtractedLicenseInfos();
		assertEquals(1, licresult1.length);
		assertEquals(licID1, licresult1[0].getLicenseId());
		assertEquals(NON_STD_LIC_TEXT1, licresult1[0].getExtractedText());
		ExtractedLicenseInfo lic2 = doc.addNewExtractedLicenseInfo(NON_STD_LIC_TEXT2);
		String licID2 = SpdxDocumentContainer.formNonStandardLicenseID(2);
		assertEquals(licID2, lic2.getLicenseId());
		assertEquals(NON_STD_LIC_TEXT2, lic2.getExtractedText());
		ExtractedLicenseInfo[] licresult2 = doc.getSpdxDocument().getExtractedLicenseInfos();
		assertEquals(2, licresult2.length);
		if (!licresult2[0].getLicenseId().equals(licID2) && !licresult2[1].getLicenseId().equals(licID2)) {
			fail("second license not found");
		}
	}

	@Test
	public void testextractedLicenseExists() throws InvalidSPDXAnalysisException {
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		SpdxDocumentContainer doc = new SpdxDocumentContainer(testUri,"SPDX-2.0");

		String NON_STD_LIC_ID1 = "LicenseRef-nonstd1";
		String NON_STD_LIC_TEXT1 = "licenseText1";
		String NON_STD_LIC_NAME1 = "licenseName1";
		String[] NON_STD_LIC_REFERENCES1 = new String[] {"ref1"};
		String NON_STD_LIC_COMMENT1 = "License 1 comment";
		ExtractedLicenseInfo lic1 = new ExtractedLicenseInfo(NON_STD_LIC_ID1, NON_STD_LIC_TEXT1, 
				NON_STD_LIC_NAME1, NON_STD_LIC_REFERENCES1, NON_STD_LIC_COMMENT1);
		
		String NON_STD_LIC_TEXT2 = "LicenseText2";

		ExtractedLicenseInfo[] emptyLic = doc.getSpdxDocument().getExtractedLicenseInfos();
		assertEquals(0,emptyLic.length);
		assertTrue(!doc.extractedLicenseExists(NON_STD_LIC_ID1));
		
		doc.addNewExtractedLicenseInfo(lic1);
		assertTrue(doc.extractedLicenseExists(NON_STD_LIC_ID1));
		
		ExtractedLicenseInfo lic2 = doc.addNewExtractedLicenseInfo(NON_STD_LIC_TEXT2);
		assertTrue(doc.extractedLicenseExists(NON_STD_LIC_ID1));
		assertTrue(doc.extractedLicenseExists(lic2.getLicenseId()));
	}
	
	@Test
	public void testGetExtractedLicenseInfo() throws InvalidSPDXAnalysisException {
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		SpdxDocumentContainer doc = new SpdxDocumentContainer(testUri,"SPDX-2.0");

		String NON_STD_LIC_ID1 = "LicenseRef-nonstd1";
		String NON_STD_LIC_TEXT1 = "licenseText1";
		String NON_STD_LIC_NAME1 = "licenseName1";
		String[] NON_STD_LIC_REFERENCES1 = new String[] {"ref1"};
		String NON_STD_LIC_COMMENT1 = "License 1 comment";
		ExtractedLicenseInfo lic1 = new ExtractedLicenseInfo(NON_STD_LIC_ID1, NON_STD_LIC_TEXT1, 
				NON_STD_LIC_NAME1, NON_STD_LIC_REFERENCES1, NON_STD_LIC_COMMENT1);
		
		String NON_STD_LIC_TEXT2 = "LicenseText2";

		ExtractedLicenseInfo[] emptyLic = doc.getSpdxDocument().getExtractedLicenseInfos();
		assertEquals(0,emptyLic.length);
		assertTrue(doc.getExtractedLicense(NON_STD_LIC_ID1) == null);
		
		doc.addNewExtractedLicenseInfo(lic1);
		assertEquals(lic1, doc.getExtractedLicense(NON_STD_LIC_ID1));
		
		ExtractedLicenseInfo lic2 = doc.addNewExtractedLicenseInfo(NON_STD_LIC_TEXT2);
		assertEquals(lic2, doc.getExtractedLicense(lic2.getLicenseId()));
	}

	@Test
	public void testSpdxDocVersions() throws InvalidSPDXAnalysisException {
		String testDocUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		SpdxDocumentContainer doc = new SpdxDocumentContainer(testDocUri, SpdxDocumentContainer.POINT_NINE_SPDX_VERSION);
		assertEquals(SpdxDocumentContainer.POINT_NINE_SPDX_VERSION, doc.getSpdxDocument().getSpecVersion());
		if (doc.getSpdxDocument().getDataLicense() != null) {
			fail("No license should exist for current data license");
		}
		// 1.0
		doc = new SpdxDocumentContainer(testDocUri, SpdxDocumentContainer.ONE_DOT_ZERO_SPDX_VERSION);
		assertEquals(SpdxDocumentContainer.ONE_DOT_ZERO_SPDX_VERSION, doc.getSpdxDocument().getSpecVersion());
		assertEquals(SpdxRdfConstants.SPDX_DATA_LICENSE_ID_VERSION_1_0, ((SpdxListedLicense)(doc.getSpdxDocument().getDataLicense())).getLicenseId());
		
		// current version
		doc = new SpdxDocumentContainer(testDocUri);
		assertEquals(SpdxDocumentContainer.CURRENT_SPDX_VERSION, doc.getSpdxDocument().getSpecVersion());
		assertEquals(SpdxRdfConstants.SPDX_DATA_LICENSE_ID, ((SpdxListedLicense)(doc.getSpdxDocument().getDataLicense())).getLicenseId());
	}
	
	@Test
	public void testFindElementById() throws InvalidSPDXAnalysisException {
		String PACKAGE_ID = "SpdxRef-PackageID";
		String FILE_ID1 = "SpdxRef-File1";
		String FILE_ID2 = "SpdxRef-File2";
		String FILE_ID3 = "SpdxRef-File3";
		SpdxPackage pkg1 = new SpdxPackage("PkgName", null, null, null, null, null, null, null);
		pkg1.setId(PACKAGE_ID);
		SpdxFile file1 = new SpdxFile("File1", null, null, null, null, null, null, null, null);
		file1.setId(FILE_ID1);
		SpdxFile file2 = new SpdxFile("File2", null, null, null, null, null, null, null, null);
		file2.setId(FILE_ID2);
		SpdxFile file3 = new SpdxFile("File3", null, null, null, null, null, null, null, null);
		file3.setId(FILE_ID3);
		pkg1.addFile(file1);
		pkg1.addFile(file2);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		SpdxDocumentContainer doc = new SpdxDocumentContainer(testUri,"SPDX-2.0");
		doc.addElement(pkg1);
		doc.addElement(file3);
		SpdxElement result = doc.findElementById(PACKAGE_ID);
		assertTrue(pkg1.equivalent(result));
		result = doc.findElementById(FILE_ID1);
		assertTrue(file1.equivalent(result));
		result = doc.findElementById(FILE_ID2);
		assertTrue(file2.equivalent(result));
		result = doc.findElementById(FILE_ID3);
		assertTrue(file3.equivalent(result));		
	}
	
	@Test
	public void testGetFileReferences() throws InvalidSPDXAnalysisException {
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		SpdxDocumentContainer doc = new SpdxDocumentContainer(testUri,"SPDX-2.0");
		SpdxPackage pkg1 = new SpdxPackage("PkgName", null, null, null, null, null, null, null);
		SpdxFile file1 = new SpdxFile("File1", null, null, null, null, null, null, null, null);
		SpdxFile file2 = new SpdxFile("File2", null, null, null, null, null, null, null, null);
		SpdxFile file3 = new SpdxFile("File3", null, null, null, null, null, null, null, null);
		pkg1.addFile(file1);
		pkg1.addFile(file2);
		doc.addElement(pkg1);
		doc.addElement(file3);
		SpdxFile[] expected = new SpdxFile[] {file1, file2, file3};
		assertTrue(UnitTestHelper.isArraysEquivalent(expected, doc.getFileReferences()));
	}
	
	@Test
	public void testDocumentNamespaceToId() throws InvalidSPDXAnalysisException {
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		SpdxDocumentContainer doc = new SpdxDocumentContainer(testUri,"SPDX-2.0");
		String uri1 = "http://www.uri.one";
		String id1 = SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM + "one";
		String uri2 = "http://www.uri.two";
		String id2 = SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM + "two";
		String uri3 = "http://www.uri.three";
		String id3 = SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM + "three";
		String uri4 = "http://www.uri.four";
		String id4 = SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM + "four";
		assertTrue(doc.externalDocumentIdToNamespace(id1) == null);
		assertTrue(doc.documentNamespaceToId(uri1) == null);
		ExternalDocumentRef[] externalDocumentRefs = new ExternalDocumentRef[] {
				new ExternalDocumentRef(uri1, null, id1),
				new ExternalDocumentRef(uri2, null, id2)
		};
		doc.setExternalDocumentRefs(externalDocumentRefs);
		assertEquals(uri1, doc.externalDocumentIdToNamespace(id1));
		assertEquals(id1, doc.documentNamespaceToId(uri1));
		assertEquals(uri2, doc.externalDocumentIdToNamespace(id2));
		assertEquals(id2, doc.documentNamespaceToId(uri2));
		
		ExternalDocumentRef[] newRefs = new ExternalDocumentRef[] {
				new ExternalDocumentRef(uri3, null, id3),
				new ExternalDocumentRef(uri4, null, id4)
		};
		doc.setExternalDocumentRefs(newRefs);
		assertTrue(doc.externalDocumentIdToNamespace(id1) == null);
		assertTrue(doc.documentNamespaceToId(uri1) == null);
		assertEquals(uri3, doc.externalDocumentIdToNamespace(id3));
		assertEquals(id3, doc.documentNamespaceToId(uri3));
		assertEquals(uri4, doc.externalDocumentIdToNamespace(id4));
		assertEquals(id4, doc.documentNamespaceToId(uri4));
	}
	
	@Test
	public void testFindAllElements() throws InvalidSPDXAnalysisException {
		String FILE_ID1 = "SpdxRef-File1";
		String FILE_ID2 = "SpdxRef-File2";
		String FILE_ID3 = "SpdxRef-File3";
		SpdxFile file1 = new SpdxFile("File1", null, null, null, null, null, null, null, null);
		file1.setId(FILE_ID1);
		SpdxFile file2 = new SpdxFile("File2", null, null, null, null, null, null, null, null);
		file2.setId(FILE_ID2);
		SpdxFile file3 = new SpdxFile("File3", null, null, null, null, null, null, null, null);
		file3.setId(FILE_ID3);
		String PACKAGE_ID1 = "SpdxRef-PackageID";
		String PACKAGE_ID2 = "SpdxRef-PackageID2";
		String PACKAGE_ID3 = "SpdxRef-PackageID3";
		SpdxPackage pkg1 = new SpdxPackage("PkgName1", null, null, null, null, null, null, null);
		pkg1.setId(PACKAGE_ID1);
		SpdxPackage pkg2 = new SpdxPackage("PkgName1", null, null, null, null, null, null, null);
		pkg1.setId(PACKAGE_ID2);
		SpdxPackage pkg3 = new SpdxPackage("PkgName1", null, null, null, null, null, null, null);
		pkg1.setId(PACKAGE_ID3);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		SpdxDocumentContainer doc = new SpdxDocumentContainer(testUri,"SPDX-2.0");
		List<SpdxElement> result = doc.findAllElements();
		assertEquals(1, result.size());
		assertEquals(doc.getSpdxDocument(), result.get(0));
		doc.addElement(file1);
		doc.addElement(file2);
		doc.addElement(file3);
		doc.addElement(pkg3);
		doc.addElement(pkg2);
		doc.addElement(pkg1);
		SpdxElement[] expected = new SpdxElement[] {doc.getSpdxDocument(), file1, file2, file3, pkg1, pkg2, pkg3};
		UnitTestHelper.isArraysEquivalent(expected, doc.findAllElements().toArray(new SpdxElement[doc.findAllElements().size()]));
	}
	
	@Test
	public void testFindAllPackage() throws InvalidSPDXAnalysisException {
		String PACKAGE_ID1 = "SpdxRef-PackageID";
		String PACKAGE_ID2 = "SpdxRef-PackageID2";
		String PACKAGE_ID3 = "SpdxRef-PackageID3";
		SpdxPackage pkg1 = new SpdxPackage("PkgName1", null, null, null, null, null, null, null);
		pkg1.setId(PACKAGE_ID1);
		SpdxPackage pkg2 = new SpdxPackage("PkgName1", null, null, null, null, null, null, null);
		pkg1.setId(PACKAGE_ID2);
		SpdxPackage pkg3 = new SpdxPackage("PkgName1", null, null, null, null, null, null, null);
		pkg1.setId(PACKAGE_ID3);
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		SpdxDocumentContainer doc = new SpdxDocumentContainer(testUri,"SPDX-2.0");
		assertTrue(doc.findAllPackages().isEmpty());
		doc.addElement(pkg1);
		List<SpdxPackage> result = doc.findAllPackages();
		assertEquals(1, result.size());
		assertTrue(result.contains(pkg1));
		Relationship rel = new Relationship(pkg3, RelationshipType.relationshipType_ancestorOf, "");
		pkg2.setRelationships(new Relationship[] {rel});
		doc.addElement(pkg2);
		result = doc.findAllPackages();
		assertEquals(3, result.size());
		SpdxPackage[] resultArray = result.toArray(new SpdxPackage[result.size()]);
		assertTrue(UnitTestHelper.isArraysEquivalent(new SpdxPackage[] {pkg1,  pkg2, pkg3}, resultArray));
	}
	
	@Test
	public void testFindAllFiles() throws InvalidSPDXAnalysisException {
		String FILE_ID1 = "SpdxRef-File1";
		String FILE_ID2 = "SpdxRef-File2";
		String FILE_ID3 = "SpdxRef-File3";
		SpdxFile file1 = new SpdxFile("File1", null, null, null, null, null, null, null, null);
		file1.setId(FILE_ID1);
		SpdxFile file2 = new SpdxFile("File2", null, null, null, null, null, null, null, null);
		file2.setId(FILE_ID2);
		SpdxFile file3 = new SpdxFile("File3", null, null, null, null, null, null, null, null);
		file3.setId(FILE_ID3);
		
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		SpdxDocumentContainer doc = new SpdxDocumentContainer(testUri,"SPDX-2.0");
		assertTrue(doc.findAllFiles().isEmpty());
		doc.addElement(file1);
		List<SpdxFile> result = doc.findAllFiles();
		assertEquals(1, result.size());
		assertTrue(result.contains(file1));
		Relationship rel = new Relationship(file3, RelationshipType.relationshipType_ancestorOf, "");
		file2.setRelationships(new Relationship[] {rel});
		doc.addElement(file2);
		result = doc.findAllFiles();
		assertEquals(3, result.size());
		SpdxFile[] resultArray = result.toArray(new SpdxFile[result.size()]);
		assertTrue(UnitTestHelper.isArraysEquivalent(new SpdxFile[] {file1,  file2, file3}, resultArray));
	}
	
	@Test
	public void testMultipleObjectsForTheSameNode() throws InvalidSPDXAnalysisException {
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		SpdxDocumentContainer doc = new SpdxDocumentContainer(testUri,"SPDX-2.0");
		String elementName = "element1";
		String elementComment1 = "element comment 1";
		SpdxElement element1 = new SpdxElement(elementName, elementComment1, null, null);
		doc.addElement(element1);
		assertFalse(element1.isRefreshOnGet());
		SpdxElement element2 = new SpdxElement(doc, element1.getNode());
		assertTrue(element1.isRefreshOnGet());
		assertTrue(element2.isRefreshOnGet());
		String elementComment2 = "Different comment";
		element2.setComment(elementComment2);
		assertEquals(elementComment2, element1.getComment());
		assertEquals(elementComment2, element2.getComment());
	}
}
