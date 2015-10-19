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
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.ExternalDocumentRef;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.Checksum.ChecksumAlgorithm;

/**
 * @author Gary
 *
 */
public class TestExternalDocReferencesSheet {
	static final String SHA1_VALUE1 = "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12";
	static final String SHA1_VALUE2 = "2222e1c67a2d28fced849ee1bb76e7391b93eb12";
	static final String SHA1_VALUE3 = "3333e1c67a2d28fced849ee1bb76e7391b93eb12";
	static final String SHA1_VALUE4 = "4444e1c67a2d28fced849ee1bb76e7391b93eb12";
	static final String SHA1_VALUE5 = "5555e1c67a2d28fced849ee1bb76e7391b93eb12";
	static final Checksum CHECKSUM1 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, SHA1_VALUE1);
	static final Checksum CHECKSUM2 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, SHA1_VALUE2);
	static final Checksum CHECKSUM3 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, SHA1_VALUE3);
	static final Checksum CHECKSUM4 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, SHA1_VALUE4);
	static final Checksum CHECKSUM5 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, SHA1_VALUE5);
	static final String DOCUMENT_URI1 = "http://spdx.org/docs/uniquevalue1";
	static final String DOCUMENT_URI2 = "http://spdx.org/docs/uniquevalue2";
	static final String DOCUMENT_URI3 = "http://spdx.org/docs/uniquevalue3";
	static final String DOCUMENT_URI4 = "http://spdx.org/docs/uniquevalue4";
	static final String DOCUMENT_URI5 = "http://spdx.org/docs/uniquevalue5";



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
		ExternalDocumentRef ref1_1 = new ExternalDocumentRef(DOCUMENT_URI1, CHECKSUM1,
				SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"1_1");
		ExternalDocumentRef ref1_2 = new ExternalDocumentRef(DOCUMENT_URI2, CHECKSUM2,
				SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"1_2");
		ExternalDocumentRef ref1_3 = new ExternalDocumentRef(DOCUMENT_URI3, CHECKSUM3,
				SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"1_3");
		ExternalDocumentRef ref1_4 = new ExternalDocumentRef(DOCUMENT_URI4, CHECKSUM4,
				SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"1_4");
		ExternalDocumentRef ref1_5 = new ExternalDocumentRef(DOCUMENT_URI5, CHECKSUM5,
				SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"1_5");
		ExternalDocumentRef[] refs1 = new ExternalDocumentRef[] {
				ref1_4, ref1_2, ref1_1, ref1_5, ref1_3
		};
		ExternalDocumentRef ref2_1 = new ExternalDocumentRef(DOCUMENT_URI1, CHECKSUM1,
				SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"2_1");
		// missing 2
		// missing 3
		ExternalDocumentRef ref2_4 = new ExternalDocumentRef(DOCUMENT_URI4, CHECKSUM4,
				SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"2_4");
		ExternalDocumentRef ref2_5 = new ExternalDocumentRef(DOCUMENT_URI5, CHECKSUM5,
				SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"2_5");
		ExternalDocumentRef[] refs2 = new ExternalDocumentRef[] {
				ref2_1, ref2_4, ref2_5
		};
		ExternalDocumentRef ref3_1 = new ExternalDocumentRef(DOCUMENT_URI1, CHECKSUM1,
				SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"3_1");
		ExternalDocumentRef ref3_2 = new ExternalDocumentRef(DOCUMENT_URI2, CHECKSUM2,
				SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"3_2");
		// missing 3
		// missing 4
		ExternalDocumentRef ref3_5 = new ExternalDocumentRef(DOCUMENT_URI5, CHECKSUM4,
				SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"3_5");
		// different checksum
		ExternalDocumentRef[] refs3 = new ExternalDocumentRef[] {
				ref3_1, ref3_2, ref3_5
		};
		doc1.setExternalDocumentRefs(refs1);
		doc2.setExternalDocumentRefs(refs2);
		doc3.setExternalDocumentRefs(refs3);
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

		SpdxComparer comparer = new SpdxComparer();
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		String sheetName = "Sheet";
		HSSFWorkbook wb = new HSSFWorkbook();
		ExternalReferencesSheet.create(wb, sheetName);
		ExternalReferencesSheet refSheet = new ExternalReferencesSheet(wb, sheetName);
		refSheet.importCompareResults(comparer, new String[] {"Name1", "Name2", "Name3"});
		Row row = null;
		Cell namespaceCell = null;
		Cell checksumCell = null;
		Cell idCell = null;
		row = refSheet.getSheet().getRow(refSheet.getFirstDataRow()+0);
		namespaceCell = row.getCell(ExternalReferencesSheet.NAMESPACE_COL);
		assertEquals(DOCUMENT_URI1, namespaceCell.getStringCellValue());
		checksumCell = row.getCell(ExternalReferencesSheet.CHECKSUM_COL);
		assertEquals(CompareHelper.checksumToString(CHECKSUM1), checksumCell.getStringCellValue());
		idCell = row.getCell(ExternalReferencesSheet.FIRST_DOC_ID_COL + 0);
		assertEquals(SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"1_1", idCell.getStringCellValue());
		idCell = row.getCell(ExternalReferencesSheet.FIRST_DOC_ID_COL + 1);
		assertEquals(SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"2_1", idCell.getStringCellValue());
		idCell = row.getCell(ExternalReferencesSheet.FIRST_DOC_ID_COL + 2);
		assertEquals(SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"3_1", idCell.getStringCellValue());

		row = refSheet.getSheet().getRow(refSheet.getFirstDataRow()+1);
		namespaceCell = row.getCell(ExternalReferencesSheet.NAMESPACE_COL);
		assertEquals(DOCUMENT_URI2, namespaceCell.getStringCellValue());
		checksumCell = row.getCell(ExternalReferencesSheet.CHECKSUM_COL);
		assertEquals(CompareHelper.checksumToString(CHECKSUM2), checksumCell.getStringCellValue());
		idCell = row.getCell(ExternalReferencesSheet.FIRST_DOC_ID_COL + 0);
		assertEquals(SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"1_2", idCell.getStringCellValue());
		idCell = row.getCell(ExternalReferencesSheet.FIRST_DOC_ID_COL + 1);
		assertTrue(cellEmpty(idCell));
		idCell = row.getCell(ExternalReferencesSheet.FIRST_DOC_ID_COL + 2);
		assertEquals(SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"3_2", idCell.getStringCellValue());

		row = refSheet.getSheet().getRow(refSheet.getFirstDataRow()+2);
		namespaceCell = row.getCell(ExternalReferencesSheet.NAMESPACE_COL);
		assertEquals(DOCUMENT_URI3, namespaceCell.getStringCellValue());
		checksumCell = row.getCell(ExternalReferencesSheet.CHECKSUM_COL);
		assertEquals(CompareHelper.checksumToString(CHECKSUM3), checksumCell.getStringCellValue());
		idCell = row.getCell(ExternalReferencesSheet.FIRST_DOC_ID_COL + 0);
		assertEquals(SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"1_3", idCell.getStringCellValue());
		idCell = row.getCell(ExternalReferencesSheet.FIRST_DOC_ID_COL + 1);
		assertTrue(cellEmpty(idCell));
		idCell = row.getCell(ExternalReferencesSheet.FIRST_DOC_ID_COL + 2);
		assertTrue(cellEmpty(idCell));

		row = refSheet.getSheet().getRow(refSheet.getFirstDataRow()+3);
		namespaceCell = row.getCell(ExternalReferencesSheet.NAMESPACE_COL);
		assertEquals(DOCUMENT_URI4, namespaceCell.getStringCellValue());
		checksumCell = row.getCell(ExternalReferencesSheet.CHECKSUM_COL);
		assertEquals(CompareHelper.checksumToString(CHECKSUM4), checksumCell.getStringCellValue());
		idCell = row.getCell(ExternalReferencesSheet.FIRST_DOC_ID_COL + 0);
		assertEquals(SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"1_4", idCell.getStringCellValue());
		idCell = row.getCell(ExternalReferencesSheet.FIRST_DOC_ID_COL + 1);
		assertEquals(SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"2_4", idCell.getStringCellValue());
		idCell = row.getCell(ExternalReferencesSheet.FIRST_DOC_ID_COL + 2);
		assertTrue(cellEmpty(idCell));

		row = refSheet.getSheet().getRow(refSheet.getFirstDataRow()+4);
		namespaceCell = row.getCell(ExternalReferencesSheet.NAMESPACE_COL);
		assertEquals(DOCUMENT_URI5, namespaceCell.getStringCellValue());
		checksumCell = row.getCell(ExternalReferencesSheet.CHECKSUM_COL);
		assertEquals(CompareHelper.checksumToString(CHECKSUM4), checksumCell.getStringCellValue());
		idCell = row.getCell(ExternalReferencesSheet.FIRST_DOC_ID_COL + 0);
		assertTrue(cellEmpty(idCell));
		idCell = row.getCell(ExternalReferencesSheet.FIRST_DOC_ID_COL + 1);
		assertTrue(cellEmpty(idCell));
		idCell = row.getCell(ExternalReferencesSheet.FIRST_DOC_ID_COL + 2);
		assertEquals(SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"3_5", idCell.getStringCellValue());

		row = refSheet.getSheet().getRow(refSheet.getFirstDataRow()+5);
		namespaceCell = row.getCell(ExternalReferencesSheet.NAMESPACE_COL);
		assertEquals(DOCUMENT_URI5, namespaceCell.getStringCellValue());
		checksumCell = row.getCell(ExternalReferencesSheet.CHECKSUM_COL);
		assertEquals(CompareHelper.checksumToString(CHECKSUM5), checksumCell.getStringCellValue());
		idCell = row.getCell(ExternalReferencesSheet.FIRST_DOC_ID_COL + 0);
		assertEquals(SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"1_5", idCell.getStringCellValue());
		idCell = row.getCell(ExternalReferencesSheet.FIRST_DOC_ID_COL + 1);
		assertEquals(SpdxRdfConstants.EXTERNAL_DOC_REF_PRENUM+"2_5", idCell.getStringCellValue());
		idCell = row.getCell(ExternalReferencesSheet.FIRST_DOC_ID_COL + 2);
		assertTrue(cellEmpty(idCell));
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
