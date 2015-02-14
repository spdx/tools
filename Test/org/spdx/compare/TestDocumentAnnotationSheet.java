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
package org.spdx.compare;

import static org.junit.Assert.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.SpdxDocument;

/**
 * @author Gary
 *
 */
public class TestDocumentAnnotationSheet {

	static final String ANNOTATOR1 = "Person: Annotator1";
	static final String ANNOTATOR2 = "Person: Annotator2";
	static final String ANNOTATOR3 = "Person: Annotator3";
	static final String COMMENT1 = "Comment1";
	static final String COMMENT2 = "Comment2";
	static final String COMMENT3 = "Comment3";
	static final String DATE1 = "2011-01-29T18:30:22Z";
	static final String DATE2 = "2012-01-29T18:30:22Z";
	static final String DATE3 = "2013-01-29T18:30:22Z";
	static final String DATE4 = "2014-01-29T18:30:22Z";
	static final String DATE5 = "2015-01-29T18:30:22Z";
	
	static Annotation.AnnotationType REVIEW_ANNOTATION = Annotation.AnnotationType.annotationType_review;
	static Annotation.AnnotationType OTHER_ANNOTATION = Annotation.AnnotationType.annotationType_other;

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

	@Test
	public void test() throws InvalidSPDXAnalysisException, SpdxCompareException {
		String uri1 = "http://spdx.org/test/uri1";
		SpdxDocumentContainer container1 = new SpdxDocumentContainer(uri1);
		String uri2 = "http://spdx.org/test/uri2";
		SpdxDocumentContainer container2 = new SpdxDocumentContainer(uri2);
		String uri3 = "http://spdx.org/test/uri3";
		SpdxDocumentContainer container3 = new SpdxDocumentContainer(uri3);
		SpdxDocument doc1 = container1.getSpdxDocument();
		SpdxDocument doc2 = container2.getSpdxDocument();
		SpdxDocument doc3 = container3.getSpdxDocument();
		doc1.setName("Name1");
		doc1.setCreationInfo(new SPDXCreatorInformation(
				new String[] {"Person: CreatorB"}, "2012-01-29T18:30:22Z", 
				"Creator CommentB", "1.17"));
		doc2.setName("Name2");
		doc2.setCreationInfo(new SPDXCreatorInformation(
				new String[] {"Person: CreatorB"}, "2012-01-29T18:30:22Z", 
				"Creator CommentB", "1.17"));
		doc3.setName("Name3");
		doc3.setCreationInfo(new SPDXCreatorInformation(
				new String[] {"Person: CreatorB"}, "2012-01-29T18:30:22Z", 
				"Creator CommentB", "1.17"));

		Annotation ann1_1 = new Annotation(ANNOTATOR1, OTHER_ANNOTATION, DATE1, COMMENT1);
		Annotation ann1_2 = new Annotation(ANNOTATOR2, REVIEW_ANNOTATION, DATE2, COMMENT2);		
		Annotation[] anns1 = new Annotation[] {ann1_1, ann1_2};
		doc1.setAnnotations(anns1);
		
		Annotation ann2_1 = new Annotation(ANNOTATOR1, OTHER_ANNOTATION, DATE3, COMMENT1);
		Annotation ann2_3 = new Annotation(ANNOTATOR3, OTHER_ANNOTATION, DATE4, COMMENT3);		
		Annotation[] anns2 = new Annotation[] {ann2_1, ann2_3};
		doc2.setAnnotations(anns2);
		
		Annotation ann3_1 = new Annotation(ANNOTATOR1, OTHER_ANNOTATION, DATE5, COMMENT1);
		Annotation[] anns3 = new Annotation[] {ann3_1};
		doc3.setAnnotations(anns3);
		
		SpdxComparer comparer = new SpdxComparer();
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		
		String sheetName = "Sheet";
		HSSFWorkbook wb = new HSSFWorkbook();
		DocumentAnnotationSheet.create(wb, sheetName);
		DocumentAnnotationSheet sheet = new DocumentAnnotationSheet(wb, sheetName);
		sheet.importCompareResults(comparer, new String[] {"Name1", "Name2", "Name3"});
		Row row = null;
		Cell annotatorCell = null;
		Cell dateCell = null;
		Cell commentCell = null;
		Cell typeCell = null;
		row = sheet.getSheet().getRow(sheet.getFirstDataRow()+0);
		annotatorCell = row.getCell(DocumentAnnotationSheet.ANNOTATOR_COL);
		assertEquals(ANNOTATOR1, annotatorCell.getStringCellValue());
		commentCell = row.getCell(DocumentAnnotationSheet.COMMENT_COL);
		assertEquals(COMMENT1, commentCell.getStringCellValue());
		typeCell = row.getCell(DocumentAnnotationSheet.TYPE_COL);
		assertEquals(Annotation.ANNOTATION_TYPE_TO_TAG.get(OTHER_ANNOTATION), typeCell.getStringCellValue());
		dateCell  = row.getCell(DocumentAnnotationSheet.FIRST_DATE_COL + 0);
		assertEquals(DATE1, dateCell.getStringCellValue());
		dateCell  = row.getCell(DocumentAnnotationSheet.FIRST_DATE_COL + 1);
		assertEquals(DATE3, dateCell.getStringCellValue());
		dateCell  = row.getCell(DocumentAnnotationSheet.FIRST_DATE_COL + 2);
		assertEquals(DATE5, dateCell.getStringCellValue());
		
		row = sheet.getSheet().getRow(sheet.getFirstDataRow()+1);
		annotatorCell = row.getCell(DocumentAnnotationSheet.ANNOTATOR_COL);
		assertEquals(ANNOTATOR2, annotatorCell.getStringCellValue());
		commentCell = row.getCell(DocumentAnnotationSheet.COMMENT_COL);
		assertEquals(COMMENT2, commentCell.getStringCellValue());
		typeCell = row.getCell(DocumentAnnotationSheet.TYPE_COL);
		assertEquals(Annotation.ANNOTATION_TYPE_TO_TAG.get(REVIEW_ANNOTATION), typeCell.getStringCellValue());
		dateCell  = row.getCell(DocumentAnnotationSheet.FIRST_DATE_COL + 0);
		assertEquals(DATE2, dateCell.getStringCellValue());
		dateCell  = row.getCell(DocumentAnnotationSheet.FIRST_DATE_COL + 1);
		assertTrue(cellEmpty(dateCell));
		dateCell  = row.getCell(DocumentAnnotationSheet.FIRST_DATE_COL + 2);
		assertTrue(cellEmpty(dateCell));
		
		row = sheet.getSheet().getRow(sheet.getFirstDataRow()+2);
		annotatorCell = row.getCell(DocumentAnnotationSheet.ANNOTATOR_COL);
		assertEquals(ANNOTATOR3, annotatorCell.getStringCellValue());
		commentCell = row.getCell(DocumentAnnotationSheet.COMMENT_COL);
		assertEquals(COMMENT3, commentCell.getStringCellValue());
		typeCell = row.getCell(DocumentAnnotationSheet.TYPE_COL);
		assertEquals(Annotation.ANNOTATION_TYPE_TO_TAG.get(OTHER_ANNOTATION), typeCell.getStringCellValue());
		assertTrue(cellEmpty(dateCell));
		dateCell  = row.getCell(DocumentAnnotationSheet.FIRST_DATE_COL + 1);
		assertEquals(DATE4, dateCell.getStringCellValue());
		dateCell  = row.getCell(DocumentAnnotationSheet.FIRST_DATE_COL + 2);
		assertTrue(cellEmpty(dateCell));
	}
	
	/**
	 * @param cell
	 * @return
	 */
	private boolean cellEmpty(Cell cell) {
		if (cell == null) {
			return true;
		}
		if (cell.getStringCellValue() == null) {
			return true;
		}
		if (cell.getStringCellValue().trim().isEmpty()) {
			return true;
		}
		return false;
	}


}
