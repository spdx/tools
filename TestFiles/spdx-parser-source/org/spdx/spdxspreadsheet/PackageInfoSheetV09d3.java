/**
 * Copyright (c) 2011 Source Auditor Inc.
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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXLicenseInfoFactory;
import org.spdx.rdfparser.SPDXNoneLicense;
import org.spdx.rdfparser.SPDXPackageInfo;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.SpdxVerificationHelper;

/**
 * @author Source Auditor
 *
 */
public class PackageInfoSheetV09d3 extends PackageInfoSheet {

	int NUM_COLS = 17;
	int NAME_COL = 0;
	int VERSION_COL = NAME_COL+1;
	int MACHINE_NAME_COL = VERSION_COL+1;
	int SUPPLIER_COL = MACHINE_NAME_COL + 1;
	int ORIGINATOR_COL = SUPPLIER_COL + 1;
	int URL_COL = ORIGINATOR_COL + 1;
	int PACKAGE_SHA_COL = URL_COL + 1;
	int FILE_VERIFICATION_VALUE_COL = PACKAGE_SHA_COL + 1;
	int VERIFICATION_EXCLUDED_FILES_COL = FILE_VERIFICATION_VALUE_COL + 1;
	int SOURCE_INFO_COL = VERIFICATION_EXCLUDED_FILES_COL + 1;
	int DECLARED_LICENSE_COL = SOURCE_INFO_COL + 1;
	int CONCLUDED_LICENSE_COL = DECLARED_LICENSE_COL + 1;
	int LICENSE_INFO_IN_FILES_COL = CONCLUDED_LICENSE_COL + 1;
	int LICENSE_COMMENT_COL = LICENSE_INFO_IN_FILES_COL + 1;
	int DECLARED_COPYRIGHT_COL = LICENSE_COMMENT_COL + 1;
	int SHORT_DESC_COL = DECLARED_COPYRIGHT_COL + 1;
	int FULL_DESC_COL = SHORT_DESC_COL + 1;


	static final boolean[] REQUIRED = new boolean[] {true, false, true, false, false, true,
		true, true, true, false, true, true, true, false, true, false, false};
	static final String[] HEADER_TITLES = new String[] {"Package Name", "Package Version",
		"Package FileName", "Package Supplier", "Package Originator", "Package Download Location", "Package Checksum", "Package Verification Code",
		"Verification Code Excluded Files", "Source Info", "License Declared", "License Concluded", "License Info From Files",
		"License Comments", "Package Copyright Text", "Summary", "Description"};

	static final int[] COLUMN_WIDTHS = new int[] {30, 17, 30, 30, 30, 50, 25, 25, 40, 30,
		40, 40, 90, 50, 50, 50, 80};

	/**
	 * @param workbook
	 * @param sheetName
	 * @param version
	 */
	public PackageInfoSheetV09d3(Workbook workbook, String sheetName, String version) {
		super(workbook, sheetName, version);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.AbstractSheet#verify()
	 */
	@Override
	public String verify() {
		try {
			if (sheet == null) {
				return "Worksheet for SPDX Package Info does not exist";
			}
			if (!OriginsSheet.verifyVersion(version)) {
				return "Unsupported version "+version;
			}
			Row firstRow = sheet.getRow(firstRowNum);
			for (int i = 0; i < NUM_COLS; i++) {
				Cell cell = firstRow.getCell(i+firstCellNum);
				if (cell == null ||
						cell.getStringCellValue() == null ||
						!cell.getStringCellValue().equals(HEADER_TITLES[i])) {
					return "Column "+HEADER_TITLES[i]+" missing for SPDX Package Info worksheet";
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
			return "Error in verifying SPDX Package Info work sheet: "+ex.getMessage();
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
				if (i == DECLARED_LICENSE_COL || i == CONCLUDED_LICENSE_COL) {
					try {
						SPDXLicenseInfoFactory.parseSPDXLicenseString(cell.getStringCellValue());
					} catch(SpreadsheetException ex) {
						if (i == DECLARED_LICENSE_COL) {
							return "Invalid declared license in row "+String.valueOf(row.getRowNum())+" detail: "+ex.getMessage();
						} else {
							return "Invalid seen license in row "+String.valueOf(row.getRowNum())+" detail: "+ex.getMessage();
						}
					}
				} else if (i == LICENSE_INFO_IN_FILES_COL) {
					String[] licenses = row.getCell(LICENSE_INFO_IN_FILES_COL).getStringCellValue().split(",");
					if (licenses.length < 1) {
						return "Missing licenss infos in files";
					}
					for (int j = 0; j < licenses.length; j++) {
						try {
							SPDXLicenseInfoFactory.parseSPDXLicenseString(cell.getStringCellValue().trim());
						} catch(SpreadsheetException ex) {
							return "Invalid license infos in row "+String.valueOf(row.getRowNum())+" detail: "+ex.getMessage();
						}
					}
				} else if (i == ORIGINATOR_COL) {
					Cell origCell = row.getCell(ORIGINATOR_COL);
					if (origCell != null) {
						String originator = origCell.getStringCellValue();
						if (originator != null && !originator.isEmpty()) {
							String error = SpdxVerificationHelper.verifyOriginator(originator);
							if (error != null && !error.isEmpty()) {
								return "Invalid originator in row "+String.valueOf(row.getRowNum()) + ": "+error;
							}
						}
					}
				} else if (i == SUPPLIER_COL) {
					Cell supplierCell = row.getCell(SUPPLIER_COL);
					if (supplierCell != null) {
						String supplier = supplierCell.getStringCellValue();
						if (supplier != null && !supplier.isEmpty()) {
							String error = SpdxVerificationHelper.verifySupplier(supplier);
							if (error != null && !error.isEmpty()) {
								return "Invalid supplier in row "+String.valueOf(row.getRowNum()) + ": "+error;
							}
						}
					}
				}
//				if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
//					return "Invalid cell format for "+HEADER_TITLES[i]+" for forw "+String.valueOf(row.getRowNum());
//				}
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
		CellStyle defaultStyle = AbstractSheet.createLeftWrapStyle(wb);
		Row row = sheet.createRow(0);
		for (int i = 0; i < HEADER_TITLES.length; i++) {
			sheet.setColumnWidth(i, COLUMN_WIDTHS[i]*256);
			sheet.setDefaultColumnStyle(i, defaultStyle);
			Cell cell = row.createCell(i);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(HEADER_TITLES[i]);
		}
	}

	public void add(SPDXPackageInfo pkgInfo) {
		Row row = addRow();
		Cell nameCell = row.createCell(NAME_COL);
		nameCell.setCellValue(pkgInfo.getDeclaredName());
		Cell copyrightCell = row.createCell(DECLARED_COPYRIGHT_COL);
		copyrightCell.setCellValue(pkgInfo.getDeclaredCopyright());
		Cell DeclaredLicenseCol = row.createCell(DECLARED_LICENSE_COL);
		DeclaredLicenseCol.setCellValue(pkgInfo.getDeclaredLicenses().toString());
		Cell concludedLicenseCol = row.createCell(CONCLUDED_LICENSE_COL);
		concludedLicenseCol.setCellValue(pkgInfo.getConcludedLicense().toString());
		Cell fileChecksumCell = row.createCell(FILE_VERIFICATION_VALUE_COL);
		if (pkgInfo.getPackageVerification() != null) {
			fileChecksumCell.setCellValue(pkgInfo.getPackageVerification().getValue());
			Cell verificationExcludedFilesCell = row.createCell(VERIFICATION_EXCLUDED_FILES_COL);
			StringBuilder excFilesStr = new StringBuilder();
			String[] excludedFiles = pkgInfo.getPackageVerification().getExcludedFileNames();
			if (excludedFiles.length > 0) {
				excFilesStr.append(excludedFiles[0]);
				for (int i = 1;i < excludedFiles.length; i++) {
					excFilesStr.append(", ");
					excFilesStr.append(excludedFiles[i]);
				}
			}
			verificationExcludedFilesCell.setCellValue(excFilesStr.toString());
		}

		if (pkgInfo.getDescription() != null) {
			Cell descCell = row.createCell(FULL_DESC_COL);
			descCell.setCellValue(pkgInfo.getDescription());
		}
		Cell fileNameCell = row.createCell(MACHINE_NAME_COL);
		fileNameCell.setCellValue(pkgInfo.getFileName());
		Cell pkgSha1 = row.createCell(PACKAGE_SHA_COL);
		if (pkgInfo.getSha1() != null) {
			pkgSha1.setCellValue(pkgInfo.getSha1());
		}
		// add the license infos in files in multiple rows
		SPDXLicenseInfo[] licenseInfosInFiles = pkgInfo.getLicensesFromFiles();
		if (licenseInfosInFiles != null && licenseInfosInFiles.length > 0) {
			StringBuilder sb = new StringBuilder(licenseInfosInFiles[0].toString());
			for (int i = 1; i < licenseInfosInFiles.length; i++) {
				sb.append(",");
				sb.append(licenseInfosInFiles[i].toString());
			}
			row.createCell(LICENSE_INFO_IN_FILES_COL).setCellValue(sb.toString());
		}
		if (pkgInfo.getLicenseComments() != null) {
			row.createCell(LICENSE_COMMENT_COL).setCellValue(pkgInfo.getLicenseComments());
		}
		if (pkgInfo.getShortDescription() != null) {
			Cell shortDescCell = row.createCell(SHORT_DESC_COL);
			shortDescCell.setCellValue(pkgInfo.getShortDescription());
		}
		if (pkgInfo.getSourceInfo() != null) {
			Cell sourceInfoCell = row.createCell(SOURCE_INFO_COL);
			sourceInfoCell.setCellValue(pkgInfo.getSourceInfo());
		}
		Cell urlCell = row.createCell(URL_COL);
		urlCell.setCellValue(pkgInfo.getUrl());
		if (pkgInfo.getVersionInfo() != null) {
			Cell versionInfoCell = row.createCell(VERSION_COL);
			versionInfoCell.setCellValue(pkgInfo.getVersionInfo());
		}
		if (pkgInfo.getOriginator() != null) {
			Cell originatorCell = row.createCell(ORIGINATOR_COL);
			originatorCell.setCellValue(pkgInfo.getOriginator());
		}
		if (pkgInfo.getSupplier() != null) {
			Cell supplierCell = row.createCell(SUPPLIER_COL);
			supplierCell.setCellValue(pkgInfo.getSupplier());
		}
	}

	public SPDXPackageInfo getPackageInfo(int rowNum) throws SpreadsheetException {
		Row row = sheet.getRow(rowNum);
		if (row == null) {
			return null;
		}
		Cell nameCell = row.getCell(NAME_COL);
		if (nameCell == null || nameCell.getStringCellValue().isEmpty()) {
			return null;
		}
		String error = validateRow(row);
		if (error != null && !error.isEmpty()) {
			throw(new SpreadsheetException(error));
		}
		String declaredName = nameCell.getStringCellValue();
		String machineName = row.getCell(MACHINE_NAME_COL).getStringCellValue();
		String sha1 = row.getCell(PACKAGE_SHA_COL).getStringCellValue();
		String sourceInfo;
		Cell sourceInfocol = row.getCell(SOURCE_INFO_COL);
		if (sourceInfocol != null) {
			sourceInfo = sourceInfocol.getStringCellValue();
		} else {
			sourceInfo = "";
		}
		SPDXLicenseInfo declaredLicenses =
				SPDXLicenseInfoFactory.parseSPDXLicenseString(row.getCell(DECLARED_LICENSE_COL).getStringCellValue());
		SPDXLicenseInfo concludedLicense;
		Cell concludedLicensesCell = row.getCell(CONCLUDED_LICENSE_COL);
		if (concludedLicensesCell != null && !concludedLicensesCell.getStringCellValue().isEmpty()) {
			concludedLicense = SPDXLicenseInfoFactory.parseSPDXLicenseString(concludedLicensesCell.getStringCellValue());
		} else {
			concludedLicense = new SPDXNoneLicense();
		}
		String[] licenseStrings = row.getCell(LICENSE_INFO_IN_FILES_COL).getStringCellValue().split(",");
		SPDXLicenseInfo[] licenseInfosFromFiles = new SPDXLicenseInfo[licenseStrings.length];
		for (int i = 0; i < licenseStrings.length; i++) {
			licenseInfosFromFiles[i] = SPDXLicenseInfoFactory.parseSPDXLicenseString(licenseStrings[i].trim());
		}
		Cell licenseCommentCell = row.getCell(LICENSE_COMMENT_COL);
		String licenseComment;
		if (licenseCommentCell != null && !licenseCommentCell.getStringCellValue().isEmpty()) {
			licenseComment = licenseCommentCell.getStringCellValue();
		} else {
			licenseComment = "";
		}
		String declaredCopyright = row.getCell(DECLARED_COPYRIGHT_COL).getStringCellValue();
		Cell shortDescCell = row.getCell(SHORT_DESC_COL);
		String shortDesc;
		if (shortDescCell != null && !shortDescCell.getStringCellValue().isEmpty()) {
			shortDesc = shortDescCell.getStringCellValue();
		} else {
			shortDesc = "";
		}
		Cell descCell = row.getCell(FULL_DESC_COL);
		String description;
		if (descCell != null && !descCell.getStringCellValue().isEmpty()) {
			description = descCell.getStringCellValue();
		} else {
			description = "";
		}
		String url = row.getCell(URL_COL).getStringCellValue();
		String packageVerificationValue = row.getCell(FILE_VERIFICATION_VALUE_COL).getStringCellValue();
		String[] excludedFiles;
		String excludedFilesStr = row.getCell(VERIFICATION_EXCLUDED_FILES_COL).getStringCellValue();
		if (excludedFilesStr != null && !excludedFilesStr.isEmpty()) {
			excludedFiles = excludedFilesStr.split(",");
			for (int i = 0;i < excludedFiles.length; i++) {
				excludedFiles[i] = excludedFiles[i].trim();
			}
		} else {
			excludedFiles = new String[0];
		}
		Cell versionInfoCell = row.getCell(VERSION_COL);
		String versionInfo;
		if (versionInfoCell != null && !versionInfoCell.getStringCellValue().isEmpty()) {
			versionInfo = versionInfoCell.getStringCellValue();
		} else {
			versionInfo = "";
		}
		String supplier;
		Cell supplierCell = row.getCell(SUPPLIER_COL);
		if (supplierCell != null && !supplierCell.getStringCellValue().isEmpty()) {
			supplier = supplierCell.getStringCellValue();
		} else {
			supplier = "";
		}
		String originator;
		Cell originatorCell = row.getCell(ORIGINATOR_COL);
		if (originatorCell != null && !originatorCell.getStringCellValue().isEmpty()) {
			originator = originatorCell.getStringCellValue();
		} else {
			originator = "";
		}
		SpdxPackageVerificationCode verificationCode = new SpdxPackageVerificationCode(packageVerificationValue, excludedFiles);
		return new SPDXPackageInfo(declaredName, versionInfo, machineName, sha1, sourceInfo,
				declaredLicenses, concludedLicense, licenseInfosFromFiles,
				licenseComment, declaredCopyright, shortDesc,
				description, url, verificationCode, supplier, originator);
	}

	public static String licensesToString(SPDXLicenseInfo[] licenses) {
		if (licenses == null || licenses.length == 0) {
			return "";
		} else if (licenses.length == 1) {
			return licenses[0].toString();
		} else {
			StringBuilder sb = new StringBuilder(licenses[0].toString());
			for (int i = 1; i < licenses.length; i++) {
				sb.append(", ");
				sb.append(licenses[i].toString());
			}
			return sb.toString();
		}
	}

}
