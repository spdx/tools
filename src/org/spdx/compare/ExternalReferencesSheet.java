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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.ExternalDocumentRef;
import org.spdx.spdxspreadsheet.AbstractSheet;

import com.google.common.base.Objects;

/**
 * Sheet that compares the external document references
 * @author Gary O'Neall
 *
 */
public class ExternalReferencesSheet extends AbstractSheet {

	private static class ExternalDocRefComparator implements Comparator<ExternalDocumentRef>, Serializable {

		/**
		 *
		 */
		private static final long serialVersionUID = -4558641025187339674L;

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(ExternalDocumentRef o1, ExternalDocumentRef o2) {
			if (o1 != null) {
				if (o2 != null) {
					ExternalDocumentRef r1 = o1;
					ExternalDocumentRef r2 = o2;
					int retval = r1.getSpdxDocumentNamespace().compareTo((r2.getSpdxDocumentNamespace()));
					if (retval == 0) {
						try {
							return r1.getChecksum().getValue().compareTo(r2.getChecksum().getValue());
						} catch (InvalidSPDXAnalysisException e) {
							return retval;
						}
					} else {
						return retval;
					}
				} else {
					return 1;
				}
			} else {
				return -1;
			}
		}
	}

	ExternalDocRefComparator externalDocRefComparator = new ExternalDocRefComparator();


	static final int NAMESPACE_COL = 0;
	static final String NAMESPACE_TEXT_TITLE = "External Document Namespace";
	static final int NAMESPACE_COL_WIDTH = 80;

	static final int CHECKSUM_COL = 1;
	static final String CHECKSUM_TEXT_TITLE = "External Doc Checksum";
	static final int CHECKSUM_COL_WIDTH = 55;

	static final int FIRST_DOC_ID_COL = 2;
	static final int DOC_ID_COL_WIDTH = 30;

	public ExternalReferencesSheet(Workbook workbook, String sheetName) {
		super(workbook, sheetName);
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.AbstractSheet#verify()
	 */
	@Override
	public String verify() {
		return null;	// nothing to verify
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
		sheet.setColumnWidth(NAMESPACE_COL, NAMESPACE_COL_WIDTH*256);
		sheet.setDefaultColumnStyle(NAMESPACE_COL, defaultStyle);
		Cell extractedHeaderCell = row.createCell(NAMESPACE_COL);
		extractedHeaderCell.setCellStyle(headerStyle);
		extractedHeaderCell.setCellValue(NAMESPACE_TEXT_TITLE);

		sheet.setColumnWidth(CHECKSUM_COL, CHECKSUM_COL_WIDTH*256);
		sheet.setDefaultColumnStyle(CHECKSUM_COL, defaultStyle);
		Cell checksumHeaderCell = row.createCell(CHECKSUM_COL);
		checksumHeaderCell.setCellStyle(headerStyle);
		checksumHeaderCell.setCellValue(CHECKSUM_TEXT_TITLE);

		for (int i = FIRST_DOC_ID_COL; i < MultiDocumentSpreadsheet.MAX_DOCUMENTS; i++) {
			sheet.setColumnWidth(i, DOC_ID_COL_WIDTH*256);
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
		int[] refIndexes = new int[comparer.getNumSpdxDocs()];
		ExternalDocumentRef[][] externalRefs = new ExternalDocumentRef[comparer.getNumSpdxDocs()][];
		for (int i = 0; i < externalRefs.length; i++) {
			Cell headerCell = header.getCell(FIRST_DOC_ID_COL+i);
			headerCell.setCellValue(docNames[i]);
			ExternalDocumentRef[] docExternalRefs = comparer.getSpdxDoc(i).getExternalDocumentRefs();
			Arrays.sort(docExternalRefs, externalDocRefComparator);
			externalRefs[i] = docExternalRefs;
			refIndexes[i] = 0;
		}
		while (!allExternalRefsExhausted(externalRefs, refIndexes)) {
			Row currentRow = this.addRow();
			ExternalDocumentRef nextRef = getNextExternalRef(externalRefs, refIndexes);
			Cell namespaceCell = currentRow.createCell(NAMESPACE_COL);
			namespaceCell.setCellValue(nextRef.getSpdxDocumentNamespace());
			Cell checksumCell = currentRow.createCell(CHECKSUM_COL);
			checksumCell.setCellValue(CompareHelper.checksumToString(nextRef.getChecksum()));
			for (int i = 0; i < externalRefs.length; i++) {
				if (externalRefs[i].length > refIndexes[i]) {
					ExternalDocumentRef compareRef = externalRefs[i][refIndexes[i]];
                    if (Objects.equal(nextRef.getSpdxDocumentNamespace(),
							compareRef.getSpdxDocumentNamespace()) &&
							nextRef.equivalentConsideringNull(nextRef.getChecksum(),
									compareRef.getChecksum())) {
						Cell docIdCell = currentRow.createCell(FIRST_DOC_ID_COL+i);
						docIdCell.setCellValue(externalRefs[i][refIndexes[i]].getExternalDocumentId());
						refIndexes[i]++;
					}
				}
			}
		}
	}

	/**
	 * @param externalRefs
	 * @param refIndexes
	 * @return
	 */
	private ExternalDocumentRef getNextExternalRef(
			ExternalDocumentRef[][] externalRefs, int[] refIndexes) {
		ExternalDocumentRef retval = null;
		for (int i = 0; i < externalRefs.length; i++) {
			if (externalRefs[i].length > refIndexes[i]) {
				ExternalDocumentRef candidate = externalRefs[i][refIndexes[i]];
				if (retval == null || this.externalDocRefComparator.compare(retval, candidate) > 0) {
					retval = candidate;
				}
			}
		}
		return retval;
	}

	/**
	 * @param externalRefs
	 * @param refIndexes
	 * @return
	 */
	private boolean allExternalRefsExhausted(
			ExternalDocumentRef[][] externalRefs, int[] refIndexes) {
		for (int i = 0; i < externalRefs.length; i++) {
			if (refIndexes[i] < externalRefs[i].length) {
				return false;
			}
		}
		return true;
	}

}
