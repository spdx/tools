/**
 * Copyright (c) 2016 Source Auditor Inc.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxSnippet;
import org.spdx.rdfparser.model.pointer.StartEndPointer;
import org.spdx.spdxspreadsheet.AbstractSheet;

/**
 * Comparsion sheet for SPDX Snippets
 * @author Gary O'Neall
 *
 */
public class SnippetSheet extends AbstractSheet {

	static final Logger logger = LoggerFactory.getLogger(SnippetSheet.class);

	private static final int COL_WIDTH = 60;
	protected static final int FIELD_COL = 0;
	protected static final int EQUALS_COL = 1;
	protected static final int FIRST_DOC_COL = 2;
	private static final int FIELD_COL_WIDTH = 20;
	private static final int EQUALS_COL_WIDTH = 7;
	protected static final String FIELD_HEADER_TEXT = "Snippet Property";
	protected static final String EQUALS_HEADER_TEXT = "Equals";
	private static final String NO_SNIPPET = "[No Snippet]";

	protected static final String COPYRIGHT_FIELD_TEXT = "Copyright";
	protected static final String LICENSE_COMMENT_FIELD_TEXT = "License Comment";
	protected static final String DECLARED_LICENSE_FIELD_TEXT = "Declared License";
	protected static final String LICENSE_INFOS_FROM_FILES_FIELD_TEXT = "License From Files";
	protected static final String CONCLUDED_LICENSE_FIELD_TEXT = "Concluded License";
	protected static final String ID_FIELD_TEXT = "SPDX ID";
	protected static final String ANNOTATION_FIELD_TEXT = "Annotations";
	protected static final String RELATIONSHIPS_FIELD_TEXT = "Relationships";
	private static final String SNIPPET_NAME_FIELD_TEXT = "Snippet Name";
	private static final String SNIPPET_FROM_FILE_FIELD_TEXT = "From File";
	protected static final String DIFFERENT_STRING = "Diff";
	protected static final String EQUAL_STRING = "Equal";
	protected static final String MISSING_STRING = "Equal*";
	private static final String NO_VALUE = "[No Value]";
	private static final String BYTE_RANGE_FIELD_TEXT = "Byte Range";
	private static final String LINE_RANGE_FIELD_TEXT = "Line Range";

	public SnippetSheet(Workbook workbook, String sheetName) {
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
		Row headerRow = sheet.createRow(0);
		sheet.setColumnWidth(FIELD_COL, FIELD_COL_WIDTH*256);
		sheet.setDefaultColumnStyle(FIELD_COL, defaultStyle);
		Cell fieldCell = headerRow.createCell(FIELD_COL);
		fieldCell.setCellStyle(headerStyle);
		fieldCell.setCellValue(FIELD_HEADER_TEXT);

		sheet.setColumnWidth(EQUALS_COL, EQUALS_COL_WIDTH * 256);
		sheet.setDefaultColumnStyle(EQUALS_COL, defaultStyle);
		Cell equalsCell = headerRow.createCell(EQUALS_COL);
		equalsCell.setCellStyle(headerStyle);
		equalsCell.setCellValue(EQUALS_HEADER_TEXT);

		for (int i = FIRST_DOC_COL; i < MultiDocumentSpreadsheet.MAX_DOCUMENTS+FIRST_DOC_COL; i++) {
			sheet.setColumnWidth(i, COL_WIDTH*256);
			sheet.setDefaultColumnStyle(i, defaultStyle);
			Cell cell = headerRow.createCell(i);
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
			Cell headerCell = header.getCell(FIRST_DOC_COL+i);
			headerCell.setCellValue(docNames[i]);
		}

		SpdxSnippetComparer[] snippetComparers = comparer.getSnippetComparers();
		Arrays.sort(snippetComparers, new Comparator<SpdxSnippetComparer>() {

			@Override
			public int compare(SpdxSnippetComparer o1, SpdxSnippetComparer o2) {
				return o1.toString().compareTo(o2.toString());
			}

		});
		for (int i = 0; i < snippetComparers.length; i++) {
			addSnippetToSheet(snippetComparers[i], comparer.getSpdxDocuments());
		}
	}

	/**
	 * @param comparer
	 * @param docs
	 * @throws SpdxCompareException
	 * @throws InvalidSPDXAnalysisException
	 */
	private void addSnippetToSheet(SpdxSnippetComparer comparer,
			SpdxDocument[] docs) throws SpdxCompareException, InvalidSPDXAnalysisException {
		Row snippetNameRow = this.addRow();
		boolean allDocsPresent = comparer.getNumSnippets() == docs.length;
		snippetNameRow.createCell(FIELD_COL).setCellValue(SNIPPET_NAME_FIELD_TEXT);
		setCellEqualValue(snippetNameRow.createCell(EQUALS_COL), allDocsPresent);
		Row idRow = this.addRow();
		idRow.createCell(FIELD_COL).setCellValue(ID_FIELD_TEXT);
		setCellEqualValue(idRow.createCell(EQUALS_COL), allDocsPresent);
		Row annotationsRow = this.addRow();
		annotationsRow.createCell(FIELD_COL).setCellValue(ANNOTATION_FIELD_TEXT);
		if (comparer.isAnnotationsEquals()) {
			setCellEqualValue(annotationsRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(annotationsRow.createCell(EQUALS_COL));
		}
		Row relationshipsRow = this.addRow();
		relationshipsRow.createCell(FIELD_COL).setCellValue(RELATIONSHIPS_FIELD_TEXT);
		if (comparer.isRelationshipsEquals()) {
			setCellEqualValue(relationshipsRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(relationshipsRow.createCell(EQUALS_COL));
		}
		Row concludedLicenseRow = this.addRow();
		concludedLicenseRow.createCell(FIELD_COL).setCellValue(CONCLUDED_LICENSE_FIELD_TEXT);
		if (comparer.isConcludedLicenseEquals()) {
			setCellEqualValue(concludedLicenseRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(concludedLicenseRow.createCell(EQUALS_COL));
		}
		Row licenseInfosFromFilesRow = this.addRow();
		licenseInfosFromFilesRow.createCell(FIELD_COL).setCellValue(LICENSE_INFOS_FROM_FILES_FIELD_TEXT);
		if (comparer.isSeenLicenseEquals()) {
			setCellEqualValue(licenseInfosFromFilesRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(licenseInfosFromFilesRow.createCell(EQUALS_COL));
		}
		Row licenseCommentRow = this.addRow();
		licenseCommentRow.createCell(FIELD_COL).setCellValue(LICENSE_COMMENT_FIELD_TEXT);
		if (comparer.isLicenseCommmentsEquals()) {
			setCellEqualValue(licenseCommentRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(licenseCommentRow.createCell(EQUALS_COL));
		}
		Row copyrightRow = this.addRow();
		copyrightRow.createCell(FIELD_COL).setCellValue(COPYRIGHT_FIELD_TEXT);
		if (comparer.isCopyrightsEquals()) {
			setCellEqualValue(copyrightRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(copyrightRow.createCell(EQUALS_COL));
		}
		Row snippetFromFileRow = this.addRow();
		snippetFromFileRow.createCell(FIELD_COL).setCellValue(SNIPPET_FROM_FILE_FIELD_TEXT);
		if (comparer.isSnippetFromFilesEquals()) {
			setCellEqualValue(snippetFromFileRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(snippetFromFileRow.createCell(EQUALS_COL));
		}
		Row byteRangeRow = this.addRow();
		byteRangeRow.createCell(FIELD_COL).setCellValue(BYTE_RANGE_FIELD_TEXT);
		if (comparer.isByteRangeEquals()) {
			setCellEqualValue(byteRangeRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(byteRangeRow.createCell(EQUALS_COL));
		}
		Row lineRangeRow = this.addRow();
		lineRangeRow.createCell(FIELD_COL).setCellValue(LINE_RANGE_FIELD_TEXT);
		if (comparer.isLineRangeEquals()) {
			setCellEqualValue(lineRangeRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(lineRangeRow.createCell(EQUALS_COL));
		}
		for (int i = 0; i < docs.length; i++) {
			SpdxSnippet snippet = comparer.getDocSnippet(docs[i]);
			if (snippet != null) {
				snippetNameRow.createCell(FIRST_DOC_COL+i).setCellValue(snippet.getName());
				idRow.createCell(FIRST_DOC_COL+i).setCellValue(snippet.getId());
				annotationsRow.createCell(FIRST_DOC_COL+i).setCellValue(CompareHelper.annotationsToString(snippet.getAnnotations()));
				relationshipsRow.createCell(FIRST_DOC_COL+i).setCellValue(CompareHelper.relationshipsToString(snippet.getRelationships()));
				concludedLicenseRow.createCell(FIRST_DOC_COL+i).setCellValue(snippet.getLicenseConcluded().toString());
				licenseInfosFromFilesRow.createCell(FIRST_DOC_COL+i).setCellValue(CompareHelper.licenseInfosToString(snippet.getLicenseInfoFromFiles()));
				licenseCommentRow.createCell(FIRST_DOC_COL+i).setCellValue(snippet.getLicenseComments());
				copyrightRow.createCell(FIRST_DOC_COL+i).setCellValue(snippet.getCopyrightText());
				SpdxFile snippetFromFile = snippet.getSnippetFromFile();
				if (snippetFromFile != null) {
					snippetFromFileRow.createCell(FIRST_DOC_COL+i).setCellValue(snippetFromFile.toString());
				} else {
					snippetFromFileRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_VALUE);
				}
				StartEndPointer byteRange = snippet.getByteRange();
				if (byteRange != null) {
					byteRangeRow.createCell(FIRST_DOC_COL+i).setCellValue(byteRange.toString());
				} else {
					byteRangeRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_VALUE);
				}
				StartEndPointer lineRange = snippet.getLineRange();
				if (lineRange != null) {
					lineRangeRow.createCell(FIRST_DOC_COL+i).setCellValue(lineRange.toString());
				} else {
					lineRangeRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_VALUE);
				}
			} else {
				snippetNameRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_SNIPPET);
				idRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_SNIPPET);
				annotationsRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_SNIPPET);
				relationshipsRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_SNIPPET);
				concludedLicenseRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_SNIPPET);
				licenseInfosFromFilesRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_SNIPPET);
				licenseCommentRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_SNIPPET);
				copyrightRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_SNIPPET);
				snippetFromFileRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_SNIPPET);
				byteRangeRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_SNIPPET);
				lineRangeRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_SNIPPET);
			}
		}
	}

	/**
	 * @param cell
	 */
	private void setCellDifferentValue(Cell cell) {
		cell.setCellValue(DIFFERENT_STRING);
		cell.setCellStyle(yellowWrapped);
	}

	/**
	 * @param cell
	 */
	private void setCellEqualValue(Cell cell, boolean allPkgsPresent) {
		if (allPkgsPresent) {
			cell.setCellValue(EQUAL_STRING);
		} else {
			cell.setCellValue(MISSING_STRING);
		}
		cell.setCellStyle(greenWrapped);
	}
}
