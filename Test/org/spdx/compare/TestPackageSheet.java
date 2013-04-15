/**
 * Copyright (c) 2013 Source Auditor Inc.
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
package org.spdx.compare;


import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.io.File;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SpdxNoAssertionLicense;

/**
 * @author Source Auditor
 *
 */
public class TestPackageSheet {
	
	static final String TEST_RDF_FILE_PATH = "TestFiles"+File.separator+"SPDXRdfExample.rdf";
	static final String SHEET_NAME = "Name";
	String[] docNames = new String[] {"doc1", "doc2", "doc3"};
	
	SpdxComparer comparer = new SpdxComparer();
	SPDXDocument doc1;	// for the unit tests, doc1 and doc2 are the same and doc3 is different
	SPDXDocument doc2;
	SPDXDocument doc3;
	Workbook wb;
	PackageSheet pkgSheet;

	
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
		doc1 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		doc2 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		doc3 = SPDXDocumentFactory.creatSpdxDocument(TEST_RDF_FILE_PATH);
		wb = new HSSFWorkbook();
		PackageSheet.create(wb, SHEET_NAME);
		pkgSheet = new PackageSheet(wb, SHEET_NAME);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testPackageNameCol() throws Exception {
		String differentName = "DIFFERENT_NAME";
		doc3.getSpdxPackage().setDeclaredName(differentName);
		comparer.compare(new SPDXDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.PACKAGE_NAME_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(doc1.getSpdxPackage().getDeclaredName(), getDocCellValue(0, row));
		assertEquals(doc2.getSpdxPackage().getDeclaredName(), getDocCellValue(1, row));
		assertEquals(differentName, getDocCellValue(2, row));
		comparer.compare(new SPDXDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.PACKAGE_NAME_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}
	
	@Test
	public void testDescriptionCol() throws Exception {
		String different = "DIFFERENT";
		doc3.getSpdxPackage().setDescription(different);
		comparer.compare(new SPDXDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.DESCRIPTION_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(doc1.getSpdxPackage().getDescription(), getDocCellValue(0, row));
		assertEquals(doc2.getSpdxPackage().getDescription(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SPDXDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.DESCRIPTION_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}
	
	@Test
	public void testSummaryCol() throws Exception {
		String different = "DIFFERENT";
		doc3.getSpdxPackage().setShortDescription(different);
		comparer.compare(new SPDXDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.SUMMARY_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(doc1.getSpdxPackage().getShortDescription(), getDocCellValue(0, row));
		assertEquals(doc2.getSpdxPackage().getShortDescription(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SPDXDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.SUMMARY_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}
	
	@Test
	public void testCopyrightCol() throws Exception {
		String different = "DIFFERENT";
		doc3.getSpdxPackage().setDeclaredCopyright(different);
		comparer.compare(new SPDXDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.COPYRIGHT_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(doc1.getSpdxPackage().getDeclaredCopyright(), getDocCellValue(0, row));
		assertEquals(doc2.getSpdxPackage().getDeclaredCopyright(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SPDXDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.COPYRIGHT_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}
	
	@Test
	public void testLicenseCommentCol() throws Exception {
		String different = "DIFFERENT";
		doc3.getSpdxPackage().setLicenseComment(different);
		comparer.compare(new SPDXDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.LICENSE_COMMENT_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(doc1.getSpdxPackage().getLicenseComment(), getDocCellValue(0, row));
		assertEquals(doc2.getSpdxPackage().getLicenseComment(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SPDXDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.LICENSE_COMMENT_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}
	
	@Test
	public void testDeclaredLicenseCol() throws Exception {
		SPDXLicenseInfo diffLicense = new SpdxNoAssertionLicense();
		String different = diffLicense.toString();
		doc3.getSpdxPackage().setDeclaredLicense(diffLicense);
		comparer.compare(new SPDXDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.DECLARED_LICENSE_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(doc1.getSpdxPackage().getDeclaredLicense().toString(), getDocCellValue(0, row));
		assertEquals(doc2.getSpdxPackage().getDeclaredLicense().toString(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SPDXDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.DECLARED_LICENSE_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}
	
	@Test
	public void testSeenLicensesCol() throws Exception {
		SPDXLicenseInfo[] diffLicense = new SPDXLicenseInfo[] {new SpdxNoAssertionLicense()};
		String different = pkgSheet.licenseInfosToString(diffLicense);
		doc3.getSpdxPackage().setLicenseInfoFromFiles(diffLicense);
		comparer.compare(new SPDXDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.LICENSE_INFOS_FROM_FILES_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(pkgSheet.licenseInfosToString(doc1.getSpdxPackage().getLicenseInfoFromFiles()), getDocCellValue(0, row));
		assertEquals(pkgSheet.licenseInfosToString(doc2.getSpdxPackage().getLicenseInfoFromFiles()), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SPDXDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.LICENSE_INFOS_FROM_FILES_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}
	
	@Test
	public void tesConcludedLicensetCol() throws Exception {
		SPDXLicenseInfo diffLicense = new SpdxNoAssertionLicense();
		String different = diffLicense.toString();
		doc3.getSpdxPackage().setConcludedLicenses(diffLicense);
		comparer.compare(new SPDXDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.CONCLUDED_LICENSE_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(doc1.getSpdxPackage().getConcludedLicenses().toString(), getDocCellValue(0, row));
		assertEquals(doc2.getSpdxPackage().getConcludedLicenses().toString(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SPDXDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.CONCLUDED_LICENSE_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}
	
	@Test
	public void testSourceInfotCol() throws Exception {
		String different = "DIFFERENT";
		doc3.getSpdxPackage().setSourceInfo(different);
		comparer.compare(new SPDXDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.SOURCEINFO_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(doc1.getSpdxPackage().getSourceInfo(), getDocCellValue(0, row));
		assertEquals(doc2.getSpdxPackage().getSourceInfo(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SPDXDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.SOURCEINFO_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}
	
	@Test
	public void testChecksumCol() throws Exception {
		String different = "DIFFERENT";
		doc3.getSpdxPackage().setSha1(different);
		comparer.compare(new SPDXDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.CHECKSUM_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(doc1.getSpdxPackage().getSha1(), getDocCellValue(0, row));
		assertEquals(doc2.getSpdxPackage().getSha1(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SPDXDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.CHECKSUM_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}
	
	@Test
	public void testExcludedCol() throws Exception {
		String different = "DIFFERENT";
		doc3.getSpdxPackage().getVerificationCode().setExcludedFileNames(new String[] {different});
		comparer.compare(new SPDXDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.VERIFICATION_EXCLUDED_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(pkgSheet.exludeFilesToString(doc1.getSpdxPackage().getVerificationCode().getExcludedFileNames()), getDocCellValue(0, row));
		assertEquals(pkgSheet.exludeFilesToString(doc2.getSpdxPackage().getVerificationCode().getExcludedFileNames()), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SPDXDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.VERIFICATION_EXCLUDED_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}
	
	@Test
	public void testVerificationCol() throws Exception {
		String different = "DIFFERENT";
		doc3.getSpdxPackage().getVerificationCode().setValue(different);
		comparer.compare(new SPDXDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.VERIFICATION_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(doc1.getSpdxPackage().getVerificationCode().getValue(), getDocCellValue(0, row));
		assertEquals(doc2.getSpdxPackage().getVerificationCode().getValue(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SPDXDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.VERIFICATION_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}

	@Test
	public void testDownloadCol() throws Exception {
		String different = "DIFFERENT";
		doc3.getSpdxPackage().setDownloadUrl(different);
		comparer.compare(new SPDXDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.DOWNLOAD_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(doc1.getSpdxPackage().getDownloadUrl(), getDocCellValue(0, row));
		assertEquals(doc2.getSpdxPackage().getDownloadUrl(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SPDXDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.DOWNLOAD_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}
	
	@Test
	public void testOriginatorCol() throws Exception {
		String different = "Person: DIFFERENT";
		doc3.getSpdxPackage().setOriginator(different);
		comparer.compare(new SPDXDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.ORIGINATOR_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(doc1.getSpdxPackage().getOriginator(), getDocCellValue(0, row));
		assertEquals(doc2.getSpdxPackage().getOriginator(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SPDXDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.ORIGINATOR_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}
	
	@Test
	public void testSupplierCol() throws Exception {
		String different = "Person: DIFFERENT";
		doc3.getSpdxPackage().setSupplier(different);
		comparer.compare(new SPDXDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.SUPPLIER_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(doc1.getSpdxPackage().getSupplier(), getDocCellValue(0, row));
		assertEquals(doc2.getSpdxPackage().getSupplier(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SPDXDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.SUPPLIER_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}
	
	@Test
	public void testFileNameCol() throws Exception {
		String different = "DIFFERENT";
		doc3.getSpdxPackage().setFileName(different);
		comparer.compare(new SPDXDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.FILE_NAME_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(doc1.getSpdxPackage().getFileName(), getDocCellValue(0, row));
		assertEquals(doc2.getSpdxPackage().getFileName(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SPDXDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.FILE_NAME_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}
	
	@Test
	public void testVersionNameCol() throws Exception {
		String different = "DIFFERENT";
		doc3.getSpdxPackage().setVersionInfo(different);
		comparer.compare(new SPDXDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.VERSION_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(doc1.getSpdxPackage().getVersionInfo(), getDocCellValue(0, row));
		assertEquals(doc2.getSpdxPackage().getVersionInfo(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SPDXDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.VERSION_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}
	
	/**
	 * @param docIndex
	 * @return
	 */
	private Object getDocCellValue(int docIndex, Row row) {
		return row.getCell(docIndex + PackageSheet.FIRST_DOC_COL).getStringCellValue();
	}

	/**
	 * @return
	 */
	private Object getEqualCellValue(Row row) {
		return row.getCell(PackageSheet.EQUALS_COL).getStringCellValue();
	}

	/**
	 * @param fieldNameText
	 * @return
	 */
	private Row findRow(String fieldNameText) {
		for (int i = pkgSheet.getFirstDataRow(); i <= pkgSheet.getNumDataRows()+pkgSheet.getFirstDataRow(); i++) {
			Row row = pkgSheet.getSheet().getRow(i);
			if (row.getCell(PackageSheet.FIELD_COL).getStringCellValue().equals(fieldNameText)) {
				return row;
			}
		}
		return null;
	}

}
