/**
 * Copyright (c) 2011 Source Auditor Inc.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.spdx.spdxspreadsheet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.AbstractSheet;
import org.spdx.rdfparser.LicenseDeclaration;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SpreadsheetException;

/**
 * Sheet describing the per file information in an SPDX Document
 * @author Gary O'Neall
 *
 */
public class PerFileSheet extends AbstractSheet {

	static final int NUM_COLS = 8;
	static final int FILE_NAME_COL = 0;
	static final int FILE_TYPE_COL = FILE_NAME_COL + 1;
	static final int SHA1_COL = FILE_TYPE_COL + 1;
	static final int ASSERTED_LIC_COL = SHA1_COL + 1;
	static final int SEEN_LIC_COL = ASSERTED_LIC_COL + 1;
	static final int LIC_COMMENTS_COL = SEEN_LIC_COL + 1;
	static final int SEEN_COPYRIGHT_COL = LIC_COMMENTS_COL + 1;
	static final int ARTIFACT_OF_COL = SEEN_COPYRIGHT_COL + 1;
	
	static final boolean[] REQUIRED = new boolean[] {true, true, false, false, 
		false, false, false, false};
	static final String[] HEADER_TITLES = new String[] {"File Name", "File Type",
		"File Identifier", "Asserted License", "Seen License", "License Comments",
		"Seen Copyright", "Artifact Of"};
	
	/**
	 * @param workbook
	 * @param sheetName
	 */
	public PerFileSheet(Workbook workbook, String sheetName) {
		super(workbook, sheetName);
	}
	
	public void add(SPDXFile fileInfo) {
		Row row = addRow();
		if (fileInfo.getArtifactOf() != null && ! fileInfo.getArtifactOf().isEmpty()) {
			row.createCell(ARTIFACT_OF_COL).setCellValue(fileInfo.getArtifactOf());
		}
		if (fileInfo.getFileLicenses() != null && fileInfo.getFileLicenses().length > 0) {
			row.createCell(ASSERTED_LIC_COL).setCellValue(PackageInfoSheet.licensesToString(fileInfo.getFileLicenses()));
		}
		row.createCell(FILE_NAME_COL).setCellValue(fileInfo.getName());
		if (fileInfo.getSha1() != null && !fileInfo.getSha1().isEmpty()) {
			row.createCell(SHA1_COL).setCellValue(fileInfo.getSha1());
		}
		row.createCell(FILE_TYPE_COL).setCellValue(fileInfo.getType());
		if (fileInfo.getLicenseComments() != null && !fileInfo.getLicenseComments().isEmpty()) {
			row.createCell(LIC_COMMENTS_COL).setCellValue(fileInfo.getLicenseComments());
		}
		if (fileInfo.getCopyright() != null && !fileInfo.getCopyright().isEmpty()) {
			row.createCell(SEEN_COPYRIGHT_COL).setCellValue(fileInfo.getCopyright());
		}
		if (fileInfo.getSeenLicenses() != null && fileInfo.getSeenLicenses().length > 0) {
			row.createCell(SEEN_LIC_COL).setCellValue(PackageInfoSheet.licensesToString(fileInfo.getSeenLicenses()));
		}
	}
	
	public SPDXFile getFileInfo(int rowNum) throws SpreadsheetException {
		Row row = sheet.getRow(rowNum);
		if (row == null) {
			return null;
		}
		String ver = validateRow(row);
		if (ver != null && !ver.isEmpty()) {
			throw(new SpreadsheetException(ver));
		}
		String name = row.getCell(FILE_NAME_COL).getStringCellValue();
		String type = row.getCell(FILE_TYPE_COL).getStringCellValue();
		Cell sha1cell = row.getCell(SHA1_COL);
		String sha1;
		if (sha1cell != null) {
			sha1 = sha1cell.getStringCellValue();
		} else {
			sha1 = "";
		}
		LicenseDeclaration[] fileLicenses;
		Cell assertedLicenseCell = row.getCell(ASSERTED_LIC_COL);
		if (assertedLicenseCell != null && !assertedLicenseCell.getStringCellValue().isEmpty()) {
			fileLicenses = PackageInfoSheet.parseLicenseString(assertedLicenseCell.getStringCellValue());
		} else {
			fileLicenses = new LicenseDeclaration[0];
		}
		LicenseDeclaration[] seenLicenses;
		Cell seenLicenseCell = row.getCell(SEEN_LIC_COL);
		if (seenLicenseCell != null && !seenLicenseCell.getStringCellValue().isEmpty()) {
			seenLicenses = PackageInfoSheet.parseLicenseString(seenLicenseCell.getStringCellValue());
		} else {
			seenLicenses = new LicenseDeclaration[0];
		}
		String licenseComments;
		Cell licCommentCell = row.getCell(LIC_COMMENTS_COL);
		if (licCommentCell != null) {
			licenseComments = licCommentCell.getStringCellValue();
		} else {
			licenseComments = "";
		}
		String copyright;
		Cell copyrightCell = row.getCell(SEEN_COPYRIGHT_COL);
		if (copyrightCell != null) {
			copyright = copyrightCell.getStringCellValue();
		} else {
			copyright = "";
		}
		String artifactOf;
		Cell artifactOfCell = row.getCell(ARTIFACT_OF_COL);
		if (artifactOfCell != null) {
			artifactOf = artifactOfCell.getStringCellValue();
		} else {
			artifactOf = "";
		}
		return new SPDXFile(name, type, sha1, fileLicenses, seenLicenses, 
				licenseComments, copyright, artifactOf);		
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.AbstractSheet#verify()
	 */
	@Override
	public String verify() {
		try {
			if (sheet == null) {
				return "Worksheet for SPDX File does not exist";
			}
			Row firstRow = sheet.getRow(firstRowNum);
			for (int i = 0; i < NUM_COLS; i++) {
				Cell cell = firstRow.getCell(i+firstCellNum);
				if (cell == null || 
						cell.getStringCellValue() == null ||
						!cell.getStringCellValue().equals(HEADER_TITLES[i])) {
					return "Column "+HEADER_TITLES[i]+" missing for SPDX File worksheet";
				}
			}
			// validate rows
			boolean done = false;
			int rowNum = firstRowNum + 1;
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
			return "Error in verifying SPDX File work sheet: "+ex.getMessage();
		}
	}

	private String validateRow(Row row) {
		for (int i = 0; i < NUM_COLS; i++) {
			Cell cell = row.getCell(i);
			if (cell == null) {
				if (REQUIRED[i]) {
					return "Required cell "+HEADER_TITLES[i]+" missing for row "+String.valueOf(row.getRowNum());
				}
			} else {
//				if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
//					return "Invalid cell format for "+HEADER_TITLES[i]+" for forw "+String.valueOf(row.getRowNum());
//				}
				if (i == ASSERTED_LIC_COL || i == SEEN_LIC_COL) {
					try {
						PackageInfoSheet.parseLicenseString(cell.getStringCellValue());
					} catch (SpreadsheetException ex) {
						if (i == ASSERTED_LIC_COL) {
							return "Invalid asserted license string in row "+String.valueOf(row.getRowNum()) +
									" details: "+ex.getMessage();
						} else {
							return "Invalid seen license string in row "+String.valueOf(row.getRowNum()) +
							" details: "+ex.getMessage();
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
		Row row = sheet.createRow(0);
		for (int i = 0; i < HEADER_TITLES.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(HEADER_TITLES[i]);
		}
	}
}
