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

import java.util.Arrays;
import java.util.Comparator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.spdxspreadsheet.AbstractSheet;

/**
 * Sheet for document level relationships
 * @author Gary O'Neall
 *
 */
public class DocumentRelationshipSheet extends AbstractSheet {

	class RelationshipComparator implements Comparator<Relationship> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Relationship o1, Relationship o2) {
			if (o1 instanceof Relationship) {
				if (o2 instanceof Relationship) {
					Relationship r1 = (Relationship)o1;
					Relationship r2 = (Relationship)o2;
					int retval = r1.getRelationshipType().toString().compareTo(r2.getRelationshipType().toString());
					if (retval != 0) {
						return retval;
					}
					if (r1.getRelatedSpdxElement().equivalent(r2.getRelatedSpdxElement())) {
						return 0;
					}
					if (r1.getRelatedSpdxElement().getName() != null && 
							r2.getRelatedSpdxElement().getName() != null) {
						return r1.getRelatedSpdxElement().getName().compareTo(r2.getRelatedSpdxElement().getName());
					} else {
						return r1.getRelatedSpdxElement().getId().compareTo(r2.getRelatedSpdxElement().getId());
					}
				} else {
					return 1;
				}
			} else {
				return -1;
			}
		}	
	}
	
	RelationshipComparator relationshipComparator = new RelationshipComparator();
	
	static final int TYPE_COL = 0;
	static final int TYPE_COL_WIDTH = 25;
	static final String TYPE_COL_TEXT_TITLE = "Type";
	static final int FIRST_RELATIONSHIP_COL = 1;
	static final int FIRST_RELATIONSHIP_COL_WIDTH = 60;
	
	public DocumentRelationshipSheet(Workbook workbook, String sheetName) {
		super(workbook, sheetName);
	}
	
	/**
	 * @param wb
	 * @param sheetName
	 */
	public static void create(Workbook wb, String sheetName) {
		int sheetNum = wb.getSheetIndex(sheetName);
		if (sheetNum >= 0) {
			wb.removeSheetAt(sheetNum);
		}
		Sheet sheet = wb.createSheet(sheetName);
		CellStyle headerStyle = AbstractSheet.createHeaderStyle(wb);
		CellStyle defaultStyle = AbstractSheet.createLeftWrapStyle(wb);
		Row row = sheet.createRow(0);

		sheet.setColumnWidth(TYPE_COL, TYPE_COL_WIDTH*256);
		sheet.setDefaultColumnStyle(TYPE_COL, defaultStyle);
		Cell typeHeaderCell = row.createCell(TYPE_COL);
		typeHeaderCell.setCellStyle(headerStyle);
		typeHeaderCell.setCellValue(TYPE_COL_TEXT_TITLE);

		for (int i = FIRST_RELATIONSHIP_COL; i < MultiDocumentSpreadsheet.MAX_DOCUMENTS; i++) {
			sheet.setColumnWidth(i, FIRST_RELATIONSHIP_COL_WIDTH*256);
			sheet.setDefaultColumnStyle(i, defaultStyle);
			Cell cell = row.createCell(i);
			cell.setCellStyle(headerStyle);
		}
	}
	
	/**
	 * @param comparer
	 * @param docNames 
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void importCompareResults(SpdxComparer comparer, String[] docNames) throws SpdxCompareException, InvalidSPDXAnalysisException {
		if (comparer.getNumSpdxDocs() != docNames.length) {
			throw(new SpdxCompareException("Number of document names does not match the number of SPDX documents"));
		}
		this.clear();
		Row header = sheet.getRow(0);
		int[] relationshipsIndexes = new int[comparer.getNumSpdxDocs()];
		Relationship[][] relationships = new Relationship[comparer.getNumSpdxDocs()][];
		for (int i = 0; i < relationships.length; i++) {
			Cell headerCell = header.getCell(FIRST_RELATIONSHIP_COL+i);
			headerCell.setCellValue(docNames[i]);
			Relationship[] docRelationships = comparer.getSpdxDoc(i).getRelationships();
			Arrays.sort(docRelationships, relationshipComparator);
			relationships[i] = docRelationships;
			relationshipsIndexes[i] = 0;
		}
		while (!allRelationshipsExhausted(relationships, relationshipsIndexes)) {
			Row currentRow = this.addRow();
			Relationship nextRelationship = getNexRelationship(relationships, relationshipsIndexes);
			Cell typeCell = currentRow.createCell(TYPE_COL);
			typeCell.setCellValue(Relationship.RELATIONSHIP_TYPE_TO_TAG.get(nextRelationship.getRelationshipType()));
			for (int i = 0; i < relationships.length; i++) {
				if (relationships[i].length > relationshipsIndexes[i]) {
					Relationship compareRelationship = relationships[i][relationshipsIndexes[i]];
					if (relationshipComparator.compare(nextRelationship, compareRelationship) == 0) {
						Cell relationshipCell = currentRow.createCell(FIRST_RELATIONSHIP_COL+i);
						relationshipCell.setCellValue(CompareHelper.relationshipToString(relationships[i][relationshipsIndexes[i]]));
						relationshipsIndexes[i]++;
					}
				}
			}
		}
	}
	/**
	 * @param relationships
	 * @param relationshipsIndexes
	 * @return
	 */
	private Relationship getNexRelationship(Relationship[][] relationships,
			int[] relationshipsIndexes) {
		Relationship retval = null;
		for (int i = 0; i < relationships.length; i++) {
			if (relationships[i].length > relationshipsIndexes[i]) {
				Relationship candidate = relationships[i][relationshipsIndexes[i]];
				if (retval == null || this.relationshipComparator.compare(retval, candidate) > 0) {
					retval = candidate;
				}
			}
		}
		return retval;
	}

	/**
	 * @param relationships
	 * @param relationshipsIndexes
	 * @return
	 */
	private boolean allRelationshipsExhausted(Relationship[][] relationships,
			int[] relationshipsIndexes) {
		for (int i = 0; i < relationships.length; i++) {
			if (relationshipsIndexes[i] < relationships[i].length) {
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.AbstractSheet#verify()
	 */
	@Override
	public String verify() {
		return null;	// Nothing to verify
	}

}
