/**
 * Copyright (c) 2013 Source Auditor Inc.
 * Copyright (c) 2013 Black Duck Software Inc.
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
import org.spdx.rdfparser.SPDXReview;
import org.spdx.spdxspreadsheet.AbstractSheet;

/**
 * Sheet of Reviewer comparisons between SPDX documents
 * Columns 1 through N are for reviewers in each of the documents
 * Format of the reviewer is ReviewerName [date] (comment)
 
 * @author Gary O'Neall
 *
 */
public class ReviewerSheet extends AbstractSheet {
	
	class ReviewerComparator implements Comparator<SPDXReview> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(SPDXReview o1, SPDXReview o2) {
			int retval = o1.getReviewer().compareTo(o2.getReviewer());
			if (retval == 0) {
				retval = o1.getReviewDate().compareTo(o2.getReviewDate());
			}
			if (retval == 0) {
				retval = o1.getComment().compareTo(o2.getComment());
			}
			return retval;
		}		
	}
	
	private ReviewerComparator reviewerComparator = new ReviewerComparator();
	static final int COL_WIDTH = 40;
	
	/**
	 * @param workbook
	 * @param sheetName
	 */
	public ReviewerSheet(Workbook workbook, String sheetName) {
		super(workbook, sheetName);
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.AbstractSheet#verify()
	 */
	@Override
	public String verify() {
		// Nothing to verify
		return null;
	}

	/**
	 * @param wb
	 * @param reviewerSheetName
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
		for (int i = 0; i < MultiDocumentSpreadsheet.MAX_DOCUMENTS; i++) {
			sheet.setColumnWidth(i, COL_WIDTH*256);
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
		for (int i = 0; i < comparer.getNumSpdxDocs(); i++) {
			Cell headerCell = header.getCell(i);
			headerCell.setCellValue(docNames[i]);
			SPDXReview[] reviewers = comparer.getSpdxDoc(i).getReviewers();
			if (reviewers == null) {
				continue;
			}
			StringBuilder sb = new StringBuilder();
			Arrays.sort(reviewers, reviewerComparator);
			for (int j = 0; j < reviewers.length; j++) {
				Cell reviewerCell = null;
				while (j+1 > this.getNumDataRows()) {
					this.addRow();
				}
				reviewerCell = sheet.getRow(j+1).createCell(i);
				
				sb.setLength(0);
				sb.append(reviewers[j].getReviewer());
				sb.append("[");
				sb.append(reviewers[j].getReviewDate());
				sb.append("]");
				if (reviewers[j].getComment() != null && !reviewers[j].getComment().isEmpty()) {
					sb.append(" (");
					sb.append(reviewers[j].getComment());
					sb.append(")");
				}
				reviewerCell.setCellValue(sb.toString());
			}
		}
	}

}
