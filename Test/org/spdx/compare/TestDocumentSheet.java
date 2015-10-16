package org.spdx.compare;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Annotation.AnnotationType;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.Relationship.RelationshipType;
import org.spdx.rdfparser.model.SpdxDocument;

public class TestDocumentSheet {
	
	private static final String DIFFERENT_STRING = "Diff";
	static final String TEST_RDF_FILE_PATH = "TestFiles"+File.separator+"SPDXRdfExample.rdf";
	static final String SHEET_NAME = "Name";
	String[] docNames = new String[] {"doc1", "doc2", "doc3"};
	
	SpdxComparer comparer = new SpdxComparer();
	SpdxDocument doc1;	// for the unit tests, doc1 and doc2 are the same and doc3 is different
	SpdxDocument doc2;
	SpdxDocument doc3;
	Workbook wb;
	DocumentSheet docSheet;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		doc1 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		doc2 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		doc3 = SPDXDocumentFactory.createSpdxDocument(TEST_RDF_FILE_PATH);
		wb = new HSSFWorkbook();
		DocumentSheet.create(wb, SHEET_NAME);
		docSheet = new DocumentSheet(wb, SHEET_NAME);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDocName() throws InvalidSPDXAnalysisException, SpdxCompareException {
		String different = "DifferentName";
		doc3.setName(different);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		docSheet.importCompareResults(comparer, docNames);
		SpdxDocument[] comparedDocs = comparer.getSpdxDocuments();
		for (int i = 0; i < comparedDocs.length; i++) {
			assertEquals(comparedDocs[i].getName(),
					getDataValue(docSheet.DOCUMENT_NAME_COL, i));
		}
	}
	
	private String getEqualsValue(int columnNum) {
		return docSheet.getSheet().getRow(docSheet.getFirstDataRow()).
				getCell(columnNum).getStringCellValue();
	}
	
	private String getDataValue(int columnNum, int docIndex) {
		return docSheet.getSheet().getRow(docSheet.getFirstDataRow()+ docIndex + 1).
				getCell(columnNum).getStringCellValue();
	}
	
	@Test
	public void testSpdxVersion() throws InvalidSPDXAnalysisException, SpdxCompareException {
		doc3.setSpecVersion(SpdxDocumentContainer.ONE_DOT_ONE_SPDX_VERSION);
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		docSheet.importCompareResults(comparer, docNames);
		assertEquals(DIFFERENT_STRING, getEqualsValue(docSheet.SPDX_VERSION_COL));
		SpdxDocument[] comparedDocs = comparer.getSpdxDocuments();
		for (int i = 0; i < comparedDocs.length; i++) {
			assertEquals(comparedDocs[i].getSpecVersion(),
					getDataValue(docSheet.SPDX_VERSION_COL, i));
		}
	}
	
	@Test
	public void testDataLicense() throws InvalidSPDXAnalysisException, SpdxCompareException {
		doc3.setDataLicense(LicenseInfoFactory.getListedLicenseById("Apache-1.0"));
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		docSheet.importCompareResults(comparer, docNames);
		assertEquals(DIFFERENT_STRING, getEqualsValue(docSheet.DATA_LICENSE_COL));
		SpdxDocument[] comparedDocs = comparer.getSpdxDocuments();
		for (int i = 0; i < comparedDocs.length; i++) {
			assertEquals(comparedDocs[i].getDataLicense().toString(),
					getDataValue(docSheet.DATA_LICENSE_COL, i));
		}
	}
	
	@Test
	public void testId() throws InvalidSPDXAnalysisException, SpdxCompareException {
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		docSheet.importCompareResults(comparer, docNames);
		SpdxDocument[] comparedDocs = comparer.getSpdxDocuments();
		for (int i = 0; i < comparedDocs.length; i++) {
			assertEquals(comparedDocs[i].getId(),
					getDataValue(docSheet.SPDX_IDENTIFIER_COL, i));
		}
	}
	
	@Test
	public void testUri() throws InvalidSPDXAnalysisException, SpdxCompareException {
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		docSheet.importCompareResults(comparer, docNames);
		SpdxDocument[] comparedDocs = comparer.getSpdxDocuments();
		for (int i = 0; i < comparedDocs.length; i++) {
			assertEquals(comparedDocs[i].getDocumentNamespace(),
					getDataValue(docSheet.DOCUMENT_NAMESPACE_COL, i));
		}
	}
	
	@Test
	public void testDocumentComment() throws InvalidSPDXAnalysisException, SpdxCompareException {
		doc3.setComment("Different");
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		docSheet.importCompareResults(comparer, docNames);
		assertEquals(DIFFERENT_STRING, getEqualsValue(docSheet.DOCUMENT_COMMENT_COL));
		SpdxDocument[] comparedDocs = comparer.getSpdxDocuments();
		for (int i = 0; i < comparedDocs.length; i++) {
			assertEquals(comparedDocs[i].getComment(),
					getDataValue(docSheet.DOCUMENT_COMMENT_COL, i));
		}
	}
	
	@Test
	public void testCreatorDate() throws InvalidSPDXAnalysisException, SpdxCompareException {
		doc3.getCreationInfo().setCreated("Different");
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		docSheet.importCompareResults(comparer, docNames);
		assertEquals(DIFFERENT_STRING, getEqualsValue(docSheet.CREATION_DATE_COL));
		SpdxDocument[] comparedDocs = comparer.getSpdxDocuments();
		for (int i = 0; i < comparedDocs.length; i++) {
			assertEquals(comparedDocs[i].getCreationInfo().getCreated(),
					getDataValue(docSheet.CREATION_DATE_COL, i));
		}
	}
	
	@Test
	public void testCreatorComment() throws InvalidSPDXAnalysisException, SpdxCompareException {
		doc3.getCreationInfo().setComment("Different");
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		docSheet.importCompareResults(comparer, docNames);
		assertEquals(DIFFERENT_STRING, getEqualsValue(docSheet.CREATOR_COMMENT_COL));
		SpdxDocument[] comparedDocs = comparer.getSpdxDocuments();
		for (int i = 0; i < comparedDocs.length; i++) {
			assertEquals(comparedDocs[i].getCreationInfo().getComment(),
					getDataValue(docSheet.CREATOR_COMMENT_COL, i));
		}
	}
	
	@Test
	public void testLicenseListVersion() throws InvalidSPDXAnalysisException, SpdxCompareException {
		doc3.getCreationInfo().setLicenseListVersion("Different");
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		docSheet.importCompareResults(comparer, docNames);
		assertEquals(DIFFERENT_STRING, getEqualsValue(docSheet.LICENSE_LIST_VERSION_COL));
		SpdxDocument[] comparedDocs = comparer.getSpdxDocuments();
		for (int i = 0; i < comparedDocs.length; i++) {
			assertEquals(comparedDocs[i].getCreationInfo().getLicenseListVersion(),
					getDataValue(docSheet.LICENSE_LIST_VERSION_COL, i));
		}
	}
	
	@Test
	public void testAnnotations() throws InvalidSPDXAnalysisException, SpdxCompareException {
		doc3.setAnnotations(new Annotation[] {
				new Annotation("Person: Different", AnnotationType.annotationType_review,
						"Date", "Different")});
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		docSheet.importCompareResults(comparer, docNames);
		assertEquals(DIFFERENT_STRING, getEqualsValue(docSheet.ANNOTATION_COL));
		SpdxDocument[] comparedDocs = comparer.getSpdxDocuments();
		for (int i = 0; i < comparedDocs.length; i++) {
			assertEquals(CompareHelper.annotationsToString(comparedDocs[i].getAnnotations()),
					getDataValue(docSheet.ANNOTATION_COL, i));
		}
	}
	
	@Test
	public void testRelationships() throws InvalidSPDXAnalysisException, SpdxCompareException {
		doc3.addRelationship(new Relationship(doc3.getDocumentDescribes()[0], 
						RelationshipType.relationshipType_contains, "Different"));
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		docSheet.importCompareResults(comparer, docNames);
		assertEquals(DIFFERENT_STRING, getEqualsValue(docSheet.RELATIONSHIP_COL));
		SpdxDocument[] comparedDocs = comparer.getSpdxDocuments();
		for (int i = 0; i < comparedDocs.length; i++) {
			assertEquals(CompareHelper.relationshipsToString(comparedDocs[i].getRelationships()),
					getDataValue(docSheet.RELATIONSHIP_COL, i));
		}
	}
}
