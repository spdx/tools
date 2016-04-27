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

import java.io.File;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Annotation.AnnotationType;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.Checksum.ChecksumAlgorithm;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.Relationship.RelationshipType;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxElement;
import org.spdx.rdfparser.model.SpdxItem;
import org.spdx.rdfparser.model.SpdxPackage;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.SpdxNoAssertionLicense;

/**
 * @author Source Auditor
 *
 */
public class TestPackageSheet {

	static final String TEST_RDF_FILE_PATH = "TestFiles"+File.separator+"SPDXRdfExample.rdf";
	static final String SHEET_NAME = "Name";
	String[] docNames = new String[] {"doc1", "doc2", "doc3"};

	SpdxComparer comparer = new SpdxComparer();
	SpdxDocument doc1;	// for the unit tests, doc1 and doc2 are the same and doc3 is different
	SpdxDocument doc2;
	SpdxDocument doc3;
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
		doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		doc3 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
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
	public void testDescriptionCol() throws Exception {
		String different = "DIFFERENT";
		getDescribedPackage(doc3).setDescription(different);
		getDescribedPackage(doc3).setDescription(different);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.DESCRIPTION_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(getDescribedPackage(doc1).getDescription(), getDocCellValue(0, row));
		assertEquals(getDescribedPackage(doc2).getDescription(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SpdxDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.DESCRIPTION_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}

	/**
	 * @param doc
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	private SpdxPackage getDescribedPackage(SpdxDocument doc) throws InvalidSPDXAnalysisException {
		SpdxItem[] items = doc.getDocumentDescribes();
		for (int i = 0; i < items.length; i++) {
			if (items[i] instanceof SpdxPackage) {
				return (SpdxPackage)items[i];
			}
		}
		return null;
	}

	@Test
	public void testSummaryCol() throws Exception {
		String different = "DIFFERENT";
		getDescribedPackage(doc3).setSummary(different);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.SUMMARY_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(getDescribedPackage(doc1).getSummary(), getDocCellValue(0, row));
		assertEquals(getDescribedPackage(doc2).getSummary(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SpdxDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.SUMMARY_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}

	@Test
	public void testCopyrightCol() throws Exception {
		String different = "DIFFERENT";
		getDescribedPackage(doc3).setCopyrightText(different);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.COPYRIGHT_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(getDescribedPackage(doc1).getCopyrightText(), getDocCellValue(0, row));
		assertEquals(getDescribedPackage(doc2).getCopyrightText(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SpdxDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.COPYRIGHT_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}

	@Test
	public void testLicenseCommentCol() throws Exception {
		String different = "DIFFERENT";
		getDescribedPackage(doc3).setLicenseComments(different);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.LICENSE_COMMENT_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(getDescribedPackage(doc1).getLicenseComments(), getDocCellValue(0, row));
		assertEquals(getDescribedPackage(doc2).getLicenseComments(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SpdxDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.LICENSE_COMMENT_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}

	@Test
	public void testDeclaredLicenseCol() throws Exception {
		AnyLicenseInfo diffLicense = new SpdxNoAssertionLicense();
		String different = diffLicense.toString();
		getDescribedPackage(doc3).setLicenseDeclared(diffLicense);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.DECLARED_LICENSE_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(getDescribedPackage(doc1).getLicenseDeclared().toString(), getDocCellValue(0, row));
		assertEquals(getDescribedPackage(doc2).getLicenseDeclared().toString(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SpdxDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.DECLARED_LICENSE_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}

	@Test
	public void testSeenLicensesCol() throws Exception {
		AnyLicenseInfo[] diffLicense = new AnyLicenseInfo[] {new SpdxNoAssertionLicense()};
		String different = CompareHelper.licenseInfosToString(diffLicense);
		getDescribedPackage(doc3).setLicenseInfosFromFiles(diffLicense);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.LICENSE_INFOS_FROM_FILES_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(CompareHelper.licenseInfosToString(getDescribedPackage(doc1).getLicenseInfoFromFiles()), getDocCellValue(0, row));
		assertEquals(CompareHelper.licenseInfosToString(getDescribedPackage(doc2).getLicenseInfoFromFiles()), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SpdxDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.LICENSE_INFOS_FROM_FILES_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}

	@Test
	public void tesConcludedLicensetCol() throws Exception {
		AnyLicenseInfo diffLicense = new SpdxNoAssertionLicense();
		String different = diffLicense.toString();
		getDescribedPackage(doc3).setLicenseConcluded(diffLicense);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.CONCLUDED_LICENSE_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(getDescribedPackage(doc1).getLicenseConcluded().toString(), getDocCellValue(0, row));
		assertEquals(getDescribedPackage(doc2).getLicenseConcluded().toString(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SpdxDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.CONCLUDED_LICENSE_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}

	@Test
	public void testSourceInfotCol() throws Exception {
		String different = "DIFFERENT";
		getDescribedPackage(doc3).setSourceInfo(different);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.SOURCEINFO_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(getDescribedPackage(doc1).getSourceInfo(), getDocCellValue(0, row));
		assertEquals(getDescribedPackage(doc2).getSourceInfo(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SpdxDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.SOURCEINFO_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}

	@Test
	public void testChecksumCol() throws Exception {
		Checksum[] different = new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, "DIFFERENT")};
		getDescribedPackage(doc3).setChecksums(different);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.CHECKSUM_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		String expected = CompareHelper.checksumsToString(getDescribedPackage(doc1).getChecksums());
		assertEquals(expected, getDocCellValue(0, row));
		expected = CompareHelper.checksumsToString(getDescribedPackage(doc2).getChecksums());
		assertEquals(expected, getDocCellValue(1, row));
		assertEquals(CompareHelper.checksumToString(different[0]), getDocCellValue(2, row));
		comparer.compare(new SpdxDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.CHECKSUM_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}

	@Test
	public void testExcludedCol() throws Exception {
		String different = "DIFFERENT";
		getDescribedPackage(doc3).getPackageVerificationCode().setExcludedFileNames(new String[] {different});
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.VERIFICATION_EXCLUDED_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(pkgSheet.exludeFilesToString(getDescribedPackage(doc1).getPackageVerificationCode().getExcludedFileNames()), getDocCellValue(0, row));
		assertEquals(pkgSheet.exludeFilesToString(getDescribedPackage(doc2).getPackageVerificationCode().getExcludedFileNames()), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SpdxDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.VERIFICATION_EXCLUDED_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}

	@Test
	public void testVerificationCol() throws Exception {
		String different = "DIFFERENT";
		getDescribedPackage(doc3).getPackageVerificationCode().setValue(different);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.VERIFICATION_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(getDescribedPackage(doc1).getPackageVerificationCode().getValue(), getDocCellValue(0, row));
		assertEquals(getDescribedPackage(doc2).getPackageVerificationCode().getValue(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SpdxDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.VERIFICATION_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}

	@Test
	public void testDownloadCol() throws Exception {
		String different = "DIFFERENT";
		getDescribedPackage(doc3).setDownloadLocation(different);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.DOWNLOAD_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(getDescribedPackage(doc1).getDownloadLocation(), getDocCellValue(0, row));
		assertEquals(getDescribedPackage(doc2).getDownloadLocation(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SpdxDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.DOWNLOAD_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}

	@Test
	public void testOriginatorCol() throws Exception {
		String different = "Person: DIFFERENT";
		getDescribedPackage(doc3).setOriginator(different);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.ORIGINATOR_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(getDescribedPackage(doc1).getOriginator(), getDocCellValue(0, row));
		assertEquals(getDescribedPackage(doc2).getOriginator(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SpdxDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.ORIGINATOR_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}

	@Test
	public void testSupplierCol() throws Exception {
		String different = "Person: DIFFERENT";
		getDescribedPackage(doc3).setSupplier(different);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.SUPPLIER_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(getDescribedPackage(doc1).getSupplier(), getDocCellValue(0, row));
		assertEquals(getDescribedPackage(doc2).getSupplier(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SpdxDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.SUPPLIER_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}

	@Test
	public void testFileNameCol() throws Exception {
		String different = "DIFFERENT";
		getDescribedPackage(doc3).setPackageFileName(different);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.FILE_NAME_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(getDescribedPackage(doc1).getPackageFileName(), getDocCellValue(0, row));
		assertEquals(getDescribedPackage(doc2).getPackageFileName(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SpdxDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.FILE_NAME_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}

	@Test
	public void testVersionNameCol() throws Exception {
		String different = "DIFFERENT";
		getDescribedPackage(doc3).setVersionInfo(different);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.VERSION_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		assertEquals(getDescribedPackage(doc1).getVersionInfo(), getDocCellValue(0, row));
		assertEquals(getDescribedPackage(doc2).getVersionInfo(), getDocCellValue(1, row));
		assertEquals(different, getDocCellValue(2, row));
		comparer.compare(new SpdxDocument[] {doc1, doc2});
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

	@Test
	public void testIdCol() throws InvalidSPDXAnalysisException, SpdxCompareException {
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.ID_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
		assertEquals(getDescribedPackage(doc1).getId(), getDocCellValue(0, row));
		assertEquals(getDescribedPackage(doc2).getId(), getDocCellValue(1, row));
		assertEquals(getDescribedPackage(doc3).getId(), getDocCellValue(2, row));
	}

	@Test
	public void testAnnotationsCol() throws InvalidSPDXAnalysisException, SpdxCompareException {
		Annotation[] different = new Annotation[] {
				new Annotation("Person: Me", AnnotationType.annotationType_other,
						"2010-01-29T18:30:22Z", "Comment")};
		getDescribedPackage(doc3).setAnnotations(different);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.ANNOTATION_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		String expected = CompareHelper.annotationToString(getDescribedPackage(doc1).getAnnotations()[0]);
		assertEquals(expected, getDocCellValue(0, row));
		expected = CompareHelper.annotationToString(getDescribedPackage(doc2).getAnnotations()[0]);
		assertEquals(expected, getDocCellValue(1, row));
		assertEquals(CompareHelper.annotationToString(different[0]), getDocCellValue(2, row));
		comparer.compare(new SpdxDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.ANNOTATION_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}

	@Test
	public void testRelationshipsCol() throws InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxElement relatedElement = getDescribedPackage(doc3).getFiles()[0];
		Relationship[] different = new Relationship[] {
				new Relationship(relatedElement, RelationshipType.CONTAINED_BY, "Comment")};
		getDescribedPackage(doc3).setRelationships(different);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		pkgSheet.importCompareResults(comparer, docNames);
		Row row = findRow(PackageSheet.RELATIONSHIPS_FIELD_TEXT);
		assertEquals(PackageSheet.DIFFERENT_STRING, getEqualCellValue(row));
		String expected = CompareHelper.relationshipToString(getDescribedPackage(doc1).getRelationships()[0]);
		assertEquals(expected, getDocCellValue(0, row));
		expected = CompareHelper.relationshipToString(getDescribedPackage(doc2).getRelationships()[0]);
		assertEquals(expected, getDocCellValue(1, row));
		assertEquals(CompareHelper.relationshipToString(different[0]), getDocCellValue(2, row));
		comparer.compare(new SpdxDocument[] {doc1, doc2});
		pkgSheet.importCompareResults(comparer, new String[] {docNames[0], docNames[1]});
		row = findRow(PackageSheet.RELATIONSHIPS_FIELD_TEXT);
		assertEquals(PackageSheet.EQUAL_STRING, getEqualCellValue(row));
	}
}
