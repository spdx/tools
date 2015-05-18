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
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.Relationship.RelationshipType;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxElement;

/**
 * @author Gary
 *
 */
public class TestDocumentRelationshipSheet {
	
	static final SpdxElement RELATED_ELEMENT1 = new SpdxElement("relatedElementName1", 
			"related element comment 1", null, null);
	static final SpdxElement RELATED_ELEMENT2 = new SpdxElement("relatedElementName2", 
			"related element comment 2", null, null);
	static final SpdxElement RELATED_ELEMENT3 = new SpdxElement("relatedElementName3", 
			"related element comment 2", null, null);
	static final SpdxElement RELATED_ELEMENT4 = new SpdxElement("relatedElementName4", 
			"related element comment 4", null, null);
	static final RelationshipType TYPE1 = RelationshipType.relationshipType_amends;
	static final RelationshipType TYPE2 = RelationshipType.relationshipType_ancestorOf;
	static final RelationshipType TYPE3 = RelationshipType.relationshipType_descendantOf;
	static final RelationshipType TYPE4 = RelationshipType.relationshipType_generates;

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
		Relationship rel1_1 = new Relationship(RELATED_ELEMENT1, TYPE1, "Comment1");
		Relationship rel1_2 = new Relationship(RELATED_ELEMENT2, TYPE1, "Comment2");
		Relationship rel1_3 = new Relationship(RELATED_ELEMENT3, TYPE2, "Comment3");
		Relationship[] rels1 = new Relationship[] {rel1_1, rel1_2, rel1_3};
		doc1.setRelationships(rels1);
		
		Relationship rel2_1 = new Relationship(RELATED_ELEMENT1, TYPE1, "Comment4");
		// missing 2
		Relationship rel2_3 = new Relationship(RELATED_ELEMENT4, TYPE2, "Comment5");
		// differen related element for type 2
		Relationship[] rels2 = new Relationship[] {rel2_1, rel2_3};
		doc2.setRelationships(rels2);
		
		Relationship rel3_1 = new Relationship(RELATED_ELEMENT1, TYPE1, "Comment6");
		Relationship[] rels3 = new Relationship[] {rel3_1};
		doc3.setRelationships(rels3);
		
		SpdxComparer comparer = new SpdxComparer();
		comparer.compare(new SpdxDocument[] {doc1, doc2, doc3});
		
		String sheetName = "Sheet";
		HSSFWorkbook wb = new HSSFWorkbook();
		DocumentRelationshipSheet.create(wb, sheetName);
		DocumentRelationshipSheet sheet = new DocumentRelationshipSheet(wb, sheetName);
		sheet.importCompareResults(comparer, new String[] {"Name1", "Name2", "Name3"});
		Row row = null;
		Cell typeCell = null;
		Cell relationshipCell = null;

		row = sheet.getSheet().getRow(sheet.getFirstDataRow()+0);
		typeCell = row.getCell(DocumentRelationshipSheet.TYPE_COL);
		assertEquals(Relationship.RELATIONSHIP_TYPE_TO_TAG.get(TYPE1), typeCell.getStringCellValue());
		relationshipCell = row.getCell(DocumentRelationshipSheet.FIRST_RELATIONSHIP_COL + 0);
		assertEquals(CompareHelper.relationshipToString(rel1_1), relationshipCell.getStringCellValue());
		relationshipCell = row.getCell(DocumentRelationshipSheet.FIRST_RELATIONSHIP_COL + 1);
		assertEquals(CompareHelper.relationshipToString(rel2_1), relationshipCell.getStringCellValue());
		relationshipCell = row.getCell(DocumentRelationshipSheet.FIRST_RELATIONSHIP_COL + 2);
		assertEquals(CompareHelper.relationshipToString(rel3_1), relationshipCell.getStringCellValue());
		
		row = sheet.getSheet().getRow(sheet.getFirstDataRow()+1);
		typeCell = row.getCell(DocumentRelationshipSheet.TYPE_COL);
		assertEquals(Relationship.RELATIONSHIP_TYPE_TO_TAG.get(TYPE1), typeCell.getStringCellValue());
		relationshipCell = row.getCell(DocumentRelationshipSheet.FIRST_RELATIONSHIP_COL + 0);
		assertEquals(CompareHelper.relationshipToString(rel1_2), relationshipCell.getStringCellValue());
		relationshipCell = row.getCell(DocumentRelationshipSheet.FIRST_RELATIONSHIP_COL + 1);
		assertTrue(cellEmpty(relationshipCell));
		relationshipCell = row.getCell(DocumentRelationshipSheet.FIRST_RELATIONSHIP_COL + 2);
		assertTrue(cellEmpty(relationshipCell));
		
		row = sheet.getSheet().getRow(sheet.getFirstDataRow()+2);
		typeCell = row.getCell(DocumentRelationshipSheet.TYPE_COL);
		assertEquals(Relationship.RELATIONSHIP_TYPE_TO_TAG.get(TYPE2), typeCell.getStringCellValue());
		relationshipCell = row.getCell(DocumentRelationshipSheet.FIRST_RELATIONSHIP_COL + 0);
		assertEquals(CompareHelper.relationshipToString(rel1_3), relationshipCell.getStringCellValue());
		relationshipCell = row.getCell(DocumentRelationshipSheet.FIRST_RELATIONSHIP_COL + 1);
		assertTrue(cellEmpty(relationshipCell));
		relationshipCell = row.getCell(DocumentRelationshipSheet.FIRST_RELATIONSHIP_COL + 2);
		assertTrue(cellEmpty(relationshipCell));
		
		row = sheet.getSheet().getRow(sheet.getFirstDataRow()+3);
		typeCell = row.getCell(DocumentRelationshipSheet.TYPE_COL);
		assertEquals(Relationship.RELATIONSHIP_TYPE_TO_TAG.get(TYPE2), typeCell.getStringCellValue());
		relationshipCell = row.getCell(DocumentRelationshipSheet.FIRST_RELATIONSHIP_COL + 0);
		assertTrue(cellEmpty(relationshipCell));
		relationshipCell = row.getCell(DocumentRelationshipSheet.FIRST_RELATIONSHIP_COL + 1);
		assertEquals(CompareHelper.relationshipToString(rel2_3), relationshipCell.getStringCellValue());
		relationshipCell = row.getCell(DocumentRelationshipSheet.FIRST_RELATIONSHIP_COL + 2);
		assertTrue(cellEmpty(relationshipCell));
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
