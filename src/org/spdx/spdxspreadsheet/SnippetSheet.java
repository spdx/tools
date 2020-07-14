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
package org.spdx.spdxspreadsheet;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxElement;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxSnippet;
import org.spdx.rdfparser.model.pointer.ByteOffsetPointer;
import org.spdx.rdfparser.model.pointer.LineCharPointer;
import org.spdx.rdfparser.model.pointer.SinglePointer;
import org.spdx.rdfparser.model.pointer.StartEndPointer;

import com.google.common.collect.Maps;

/**
 * Sheet to hold all snippet information
 * @author Gary O'Neall
 *
 */
public class SnippetSheet extends AbstractSheet {

	static final Logger logger = LoggerFactory.getLogger(SnippetSheet.class);

	static final int ID_COL = 0;
	static final int NAME_COL = ID_COL + 1;
	static final int SNIPPET_FROM_FILE_ID_COL = NAME_COL + 1;
	static final int BYTE_RANGE_COL = SNIPPET_FROM_FILE_ID_COL + 1;
	static final int LINE_RANGE_COL = BYTE_RANGE_COL + 1;
	static final int CONCLUDED_LICENSE_COL = LINE_RANGE_COL + 1;
	static final int LICENSE_INFO_IN_SNIPPET_COL = CONCLUDED_LICENSE_COL + 1;
	static final int LICENSE_COMMENT_COL = LICENSE_INFO_IN_SNIPPET_COL + 1;
	static final int COPYRIGHT_COL = LICENSE_COMMENT_COL + 1;
	static final int COMMENT_COL = COPYRIGHT_COL + 1;
	static final int USER_DEFINED_COLS = COMMENT_COL + 1;
	static final int NUM_COLS = USER_DEFINED_COLS + 1;

	static final boolean[] REQUIRED = new boolean[] {true, false, true, true, false,
		false, false, false, false, false, false};
	static final String[] HEADER_TITLES = new String[] {"ID", "Name", "From File ID",
		"Byte Range", "Line Range", "License Concluded", "License Info in Snippet", "License Comments",
		"Snippet Copyright Text", "Comment", "User Defined Columns..."};

	static final int[] COLUMN_WIDTHS = new int[] {25, 25, 25, 40, 40, 60, 60, 60, 60, 60, 40};
	static final boolean[] LEFT_WRAP = new boolean[] {false, false, false, false, false,
		true, true, true, true, true, true};
	static final boolean[] CENTER_NOWRAP = new boolean[] {true, true, true, true, true,
		false, false, false, false, false, false};

	private static Pattern NUMBER_RANGE_PATTERN = Pattern.compile("(\\d+):(\\d+)");

	/**
	 * Hashmap of the snippet ID to SPDX snipet
	 */
	Map<String, SpdxSnippet> snippetCache = Maps.newHashMap();
	/**
	 * @param workbook
	 * @param snippetSheetName
	 */
	public SnippetSheet(Workbook workbook, String snippetSheetName) {
		super(workbook, snippetSheetName);
	}

	/* (non-Javadoc)
	 * @see org.spdx.spdxspreadsheet.AbstractSheet#verify()
	 */
	@Override
	public String verify() {
		try {
			if (sheet == null) {
				return "Worksheet for SPDX Snippets does not exist";
			}
			Row firstRow = sheet.getRow(firstRowNum);
			for (int i = 0; i < NUM_COLS- 1; i++) { 	// Don't check the last (user defined) column
				Cell cell = firstRow.getCell(i+firstCellNum);
				if (cell == null ||
						cell.getStringCellValue() == null ||
						!cell.getStringCellValue().equals(HEADER_TITLES[i])) {
					return "Column "+HEADER_TITLES[i]+" missing for SPDX Snippet worksheet";
				}
			}
			// validate rows
			boolean done = false;
			int rowNum = getFirstDataRow();
			while (!done) {
				Row row = sheet.getRow(rowNum);
				if (row == null || row.getCell(firstCellNum) == null) {
					done = true;
				} else {
					String error = validateRow(row);
					if (error != null) {
						return error;
					}
					rowNum++;
				}
			}
			return null;
		} catch (Exception ex) {
			return "Error in verifying SPDX Snippet work sheet: "+ex.getMessage();
		}
	}

	/**
	 * @param row
	 * @return
	 */
	private String validateRow(Row row) {
		for (int i = 0; i < NUM_COLS; i++) {
			Cell cell = row.getCell(i);
			if (cell == null) {
				if (REQUIRED[i]) {
					return "Required cell "+HEADER_TITLES[i]+" missing for row "+String.valueOf(row.getRowNum());
				}
			} else {
				if (i == CONCLUDED_LICENSE_COL) {
					try {
						LicenseInfoFactory.parseSPDXLicenseString(cell.getStringCellValue(), null);
					} catch (SpreadsheetException ex) {
						return "Invalid asserted license string in row "+String.valueOf(row.getRowNum()) +
								" details: "+ex.getMessage();
					}
				} else if (i == BYTE_RANGE_COL || i == LINE_RANGE_COL) {
					String range = cell.getStringCellValue();
					if (range != null && !range.isEmpty()) {
						Matcher rangeMatcher = NUMBER_RANGE_PATTERN.matcher(cell.getStringCellValue());
						if (!rangeMatcher.matches()) {
							return "Invalid range for "+HEADER_TITLES[i]+": "+cell.getStringCellValue();
						}
						int start = 0;
						int end = 0;
						try {
							start = Integer.parseInt(rangeMatcher.group(1));
							end = Integer.parseInt(rangeMatcher.group(2));
							if (start >= end) {
								return "Invalid range for "+HEADER_TITLES[i]+": "+cell.getStringCellValue() + ".  End is not greater than or equal to the end.";
							}
						} catch(Exception ex) {
							return "Invalid range for "+HEADER_TITLES[i]+": "+cell.getStringCellValue();
						}
					}
				}
			}
		}
		return null;
	}

	public static void create(Workbook wb, String sheetName) {
		int sheetNum = wb.getSheetIndex(sheetName);
		if (sheetNum >= 0) {
			wb.removeSheetAt(sheetNum);
		}
		Sheet sheet = wb.createSheet(sheetName);
		CellStyle headerStyle = AbstractSheet.createHeaderStyle(wb);
		CellStyle centerStyle = AbstractSheet.createCenterStyle(wb);
		CellStyle wrapStyle = AbstractSheet.createLeftWrapStyle(wb);
		Row row = sheet.createRow(0);
		for (int i = 0; i < HEADER_TITLES.length; i++) {
			sheet.setColumnWidth(i, COLUMN_WIDTHS[i]*256);
			if (LEFT_WRAP[i]) {
				sheet.setDefaultColumnStyle(i, wrapStyle);
			} else if (CENTER_NOWRAP[i]) {
				sheet.setDefaultColumnStyle(i, centerStyle);
			}
			Cell cell = row.createCell(i);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(HEADER_TITLES[i]);
		}
	}

	/**
	 * @param snippet
	 * @throws SpreadsheetException
	 */
	public void add(SpdxSnippet snippet) throws SpreadsheetException {
		Row row = addRow();
		if (snippet.getId() != null && !snippet.getId().isEmpty()) {
			row.createCell(ID_COL).setCellValue(snippet.getId());
		}
		if (snippet.getName() != null && !snippet.getName().isEmpty()) {
			row.createCell(NAME_COL).setCellValue(snippet.getName());
		}
		SpdxFile snippetFromFile;
		try {
			snippetFromFile = snippet.getSnippetFromFile();
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Error getting the snippetFromFile",e);
			throw new SpreadsheetException("Unable to get the Snippet from File from the Snippet: "+e.getMessage());
		}
		if (snippetFromFile != null && snippetFromFile.getId() != null) {
			row.createCell(SNIPPET_FROM_FILE_ID_COL).setCellValue(snippetFromFile.getId());
		}
		StartEndPointer byteRange;
		try {
			byteRange = snippet.getByteRange();
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Error getting the byteRange",e);
			throw new SpreadsheetException("Unable to get the byte range from the Snippet: "+e.getMessage());
		}
		if (byteRange != null) {
			try {
				row.createCell(BYTE_RANGE_COL).setCellValue(rangeToStr(byteRange));
			} catch (InvalidSPDXAnalysisException e) {
				logger.error("Invalid byte range",e);
				throw new SpreadsheetException("Invalid byte range: "+e.getMessage());
			}
		}
		StartEndPointer lineRange;
		try {
			lineRange = snippet.getLineRange();
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Error getting the lineRange",e);
			throw new SpreadsheetException("Unable to get the line range from the Snippet: "+e.getMessage());
		}
		if (lineRange != null) {
			try {
				row.createCell(LINE_RANGE_COL).setCellValue(rangeToStr(lineRange));
			} catch (InvalidSPDXAnalysisException e) {
				logger.error("Invalid line range",e);
				throw new SpreadsheetException("Invalid line range: "+e.getMessage());
			}
		}
		if (snippet.getLicenseConcluded() != null) {
			row.createCell(CONCLUDED_LICENSE_COL).setCellValue(snippet.getLicenseConcluded().toString());
		}
		AnyLicenseInfo[] licenseInfoFromSnippet = snippet.getLicenseInfoFromFiles();
		if (licenseInfoFromSnippet != null && licenseInfoFromSnippet.length > 0) {
			row.createCell(LICENSE_INFO_IN_SNIPPET_COL).setCellValue(PackageInfoSheet.licensesToString(licenseInfoFromSnippet));
		}
		if (snippet.getLicenseComments() != null) {
			row.createCell(LICENSE_COMMENT_COL).setCellValue(snippet.getLicenseComments());
		}
		if (snippet.getCopyrightText() != null) {
			row.createCell(COPYRIGHT_COL).setCellValue(snippet.getCopyrightText());
		}
		if (snippet.getComment() != null) {
			row.createCell(COMMENT_COL).setCellValue(snippet.getComment());
		}
		this.snippetCache.put(snippet.getId(), snippet);
	}

	/**
	 * @param byteRange
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	private String rangeToStr(StartEndPointer rangePointer) throws InvalidSPDXAnalysisException {
		SinglePointer startPointer = rangePointer.getStartPointer();
		if (startPointer == null) {
			throw new InvalidSPDXAnalysisException("Missing start pointer");
		}
		SinglePointer endPointer = rangePointer.getEndPointer();
		if (endPointer == null) {
			throw new InvalidSPDXAnalysisException("Missing end pointer");
		}
		String start = null;
		if (startPointer instanceof ByteOffsetPointer) {
			start = String.valueOf(((ByteOffsetPointer)startPointer).getOffset());
		} else if (startPointer instanceof LineCharPointer) {
			start = String.valueOf(((LineCharPointer)startPointer).getLineNumber());
		} else {
			logger.error("Unknown pointer type for start pointer "+startPointer.toString());
			throw new InvalidSPDXAnalysisException("Unknown pointer type for start pointer");
		}
		String end = null;
		if (endPointer instanceof ByteOffsetPointer) {
			end = String.valueOf(((ByteOffsetPointer)endPointer).getOffset());
		} else if (endPointer instanceof LineCharPointer) {
			end = String.valueOf(((LineCharPointer)endPointer).getLineNumber());
		} else {
			logger.error("Unknown pointer type for start pointer "+startPointer.toString());
			throw new InvalidSPDXAnalysisException("Unknown pointer type for start pointer");
		}
		return start + ":" + end;
	}

	/**
	 * Get the SPDX snippet represented in the row rownum.
	 * IMPORTANT: The Snippet From File will only be filled in if the associated file is found in the container.
	 * This property can be set after calling getSnippet if the model is not already populated with the file
	 * The ID from the Snippet From File can be obtained through the <code> getSnippetFileId(int rowNum)</code> method
	 * @param rowNum
	 * @return Snippet at the row rowNum or null if the row does not exist
	 * @throws SpreadsheetException
	 */
	public SpdxSnippet getSnippet(int rowNum, SpdxDocumentContainer container) throws SpreadsheetException {
		if (sheet == null) {
			return null;
		}
		Row row = sheet.getRow(rowNum);
		if (row == null) {
			return null;
		}
		String ver = validateRow(row);
		if (ver != null && !ver.isEmpty()) {
			throw(new SpreadsheetException(ver));
		}
		String id = null;
		if (row.getCell(ID_COL) != null) {
			id = row.getCell(ID_COL).getStringCellValue();
		}
		if (this.snippetCache.containsKey(id)) {
			return this.snippetCache.get(id);
		}
		String name = null;
		if (row.getCell(NAME_COL) != null) {
			name = row.getCell(NAME_COL).getStringCellValue();
		}
		StartEndPointer byteRange = null;
		if (row.getCell(BYTE_RANGE_COL) != null) {
			String range = row.getCell(BYTE_RANGE_COL).getStringCellValue();
			if (range != null && !range.isEmpty()) {
				int start = 0;
				int end = 0;
				Matcher rangeMatcher = NUMBER_RANGE_PATTERN.matcher(range);
				if (!rangeMatcher.matches()) {
					throw new SpreadsheetException("Invalid byte range: "+range);
				}
				try {
					start = Integer.parseInt(rangeMatcher.group(1));
					end = Integer.parseInt(rangeMatcher.group(2));
				} catch(Exception ex) {
					throw new SpreadsheetException("Invalid byte range: "+range);
				}
				// Note: the reference should be filled in when the snippetFromFile is set for the Spdx Snippet
				ByteOffsetPointer startPointer = new ByteOffsetPointer(null, start);
				ByteOffsetPointer endPointer = new ByteOffsetPointer(null, end);
				byteRange = new StartEndPointer(startPointer, endPointer);
			}
		}
		StartEndPointer lineRange = null;
		if (row.getCell(LINE_RANGE_COL) != null) {
			String range = row.getCell(LINE_RANGE_COL).getStringCellValue();
			if (range != null && !range.isEmpty()) {
				int start = 0;
				int end = 0;
				Matcher rangeMatcher = NUMBER_RANGE_PATTERN.matcher(range);
				if (!rangeMatcher.matches()) {
					throw new SpreadsheetException("Invalid line range: "+range);
				}
				try {
					start = Integer.valueOf(rangeMatcher.group(1));
					end = Integer.valueOf(rangeMatcher.group(2));
				} catch(Exception ex) {
					throw new SpreadsheetException("Invalid line range: "+range);
				}
				// Note: the reference should be filled in when the snippetFromFile is set for the Spdx Snippet
				LineCharPointer startPointer = new LineCharPointer(null, start);
				LineCharPointer endPointer = new LineCharPointer(null, end);
				lineRange = new StartEndPointer(startPointer, endPointer);
			}
		}
		AnyLicenseInfo concludedLicense = null;
		Cell concludedLicenseCell = row.getCell(CONCLUDED_LICENSE_COL);
		if (concludedLicenseCell != null && !concludedLicenseCell.getStringCellValue().isEmpty()) {
			concludedLicense = LicenseInfoFactory.parseSPDXLicenseString(concludedLicenseCell.getStringCellValue(), container);
		} else {
			concludedLicense = null;
		}
		AnyLicenseInfo[] seenLicenses = new AnyLicenseInfo[0];
		Cell seenLicenseCell = row.getCell(LICENSE_INFO_IN_SNIPPET_COL);
		if (seenLicenseCell != null && !seenLicenseCell.getStringCellValue().isEmpty()) {
			String[] licenseStrings = seenLicenseCell.getStringCellValue().split(",");
			seenLicenses = new AnyLicenseInfo[licenseStrings.length];
			for (int i = 0; i < licenseStrings.length; i++) {
				seenLicenses[i] = LicenseInfoFactory.parseSPDXLicenseString(licenseStrings[i].trim(), container);
			}
		} else {
			seenLicenses = null;
		}
		String licenseComments = null;
		Cell licCommentCell = row.getCell(LICENSE_COMMENT_COL);
		if (licCommentCell != null) {
			licenseComments = licCommentCell.getStringCellValue();
		} else {
			licenseComments = "";
		}
		String copyright;
		Cell copyrightCell = row.getCell(COPYRIGHT_COL);
		if (copyrightCell != null) {
			copyright = copyrightCell.getStringCellValue();
		} else {
			copyright = "";
		}
		String comment = null;
		Cell commentCell = row.getCell(COMMENT_COL);
		if (commentCell != null) {
			comment = commentCell.getStringCellValue();
		}
		String snippetFromFileId = getSnippetFileId(rowNum);
		SpdxFile snippetFromFile = null;
		if (snippetFromFileId != null && !snippetFromFileId.isEmpty()) {
			SpdxElement fromFileElement;
			try {
				fromFileElement = container.findElementById(snippetFromFileId);
				if (fromFileElement instanceof SpdxFile) {
					snippetFromFile = (SpdxFile)fromFileElement;
					// Fix up the ranges to reference this snippetFromFile
					if (byteRange != null) {
						byteRange.getStartPointer().setReference(snippetFromFile);
						byteRange.getEndPointer().setReference(snippetFromFile);
					}
					if (lineRange != null) {
						lineRange.getStartPointer().setReference(snippetFromFile);
						lineRange.getEndPointer().setReference(snippetFromFile);
					}
				} else {
					logger.warn("Element associated with the snippetFromFile is not of type SPDX File.  Null will be used");
				}
			} catch (InvalidSPDXAnalysisException e) {
				logger.warn("Error getting snipet from file by ID.  snippetFromFile will be set to null");
			}
		}
		SpdxSnippet retval = new SpdxSnippet(name, comment, new Annotation[0], new Relationship[0],
				concludedLicense, seenLicenses, copyright, licenseComments, snippetFromFile,
				byteRange, lineRange);
		try {
			retval.setId(id);
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Error setting ID for SpdxSnippet", e);
			throw new SpreadsheetException("Error setting ID for SpdxSnippet: "+e.getMessage());
		}
		this.snippetCache.put(id, retval);
		return retval;
	}

	/**
	 * Get the SpdxFromFileSNippet for the given row
	 * @param rowNum
	 * @return
	 * @throws SpreadsheetException
	 */
	public String getSnippetFileId(int rowNum) throws SpreadsheetException {
		Row row = sheet.getRow(rowNum);
		if (row == null) {
			return null;
		}
		String ver = validateRow(row);
		if (ver != null && !ver.isEmpty()) {
			throw(new SpreadsheetException(ver));
		}
		String id = null;
		if (row.getCell(SNIPPET_FROM_FILE_ID_COL) != null) {
			id = row.getCell(SNIPPET_FROM_FILE_ID_COL).getStringCellValue();
		}
		return id;
	}

}
