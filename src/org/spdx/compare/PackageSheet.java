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

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxPackage;
import org.spdx.spdxspreadsheet.AbstractSheet;

/**
 * Document level fields for comparison spreadsheet
 * Column1 is the document field name, column2 indicates if all docs are equal, 
 * columns3 through columnN are document specific field values
 * @author Gary O'Neall
 *
 */
public class PackageSheet extends AbstractSheet {
	
	static final Logger logger = Logger.getLogger(PackageSheet.class);
	private static final int COL_WIDTH = 60;
	protected static final int FIELD_COL = 0;
	protected static final int EQUALS_COL = 1;
	protected static final int FIRST_DOC_COL = 2;
	private static final int FIELD_COL_WIDTH = 20;
	private static final int EQUALS_COL_WIDTH = 7;
	protected static final String FIELD_HEADER_TEXT = "Package Property";
	protected static final String EQUALS_HEADER_TEXT = "Equals";
	protected static final String DESCRIPTION_FIELD_TEXT = "Description";
	protected static final String SUMMARY_FIELD_TEXT = "Summary";
	protected static final String COPYRIGHT_FIELD_TEXT = "Copyright";
	protected static final String LICENSE_COMMENT_FIELD_TEXT = "License Comment";
	protected static final String DECLARED_LICENSE_FIELD_TEXT = "Declared License";
	protected static final String LICENSE_INFOS_FROM_FILES_FIELD_TEXT = "License From Files";
	protected static final String CONCLUDED_LICENSE_FIELD_TEXT = "Concluded License";
	protected static final String SOURCEINFO_FIELD_TEXT = "Source Info";
	protected static final String CHECKSUM_FIELD_TEXT = "Checksum";
	protected static final String VERIFICATION_EXCLUDED_FIELD_TEXT = "Verification Excluded";
	protected static final String VERIFICATION_FIELD_TEXT = "Verification Value";
	protected static final String DOWNLOAD_FIELD_TEXT = "Dowload Location";
	protected static final String ORIGINATOR_FIELD_TEXT = "Originator";
	protected static final String SUPPLIER_FIELD_TEXT = "Supplier";
	protected static final String FILE_NAME_FIELD_TEXT = "File Name";
	protected static final String VERSION_FIELD_TEXT = "Version";
	protected static final String PACKAGE_NAME_FIELD_TEXT = "Package Name";
	protected static final String DIFFERENT_STRING = "Diff";
	protected static final String EQUAL_STRING = "Equal";
	protected static final String MISSING_STRING = "Equal*";
	protected static final String HOMEPAGE_FIELD_TEXT = "Home Page";
	protected static final String ID_FIELD_TEXT = "SPDX ID";
	protected static final String ANNOTATION_FIELD_TEXT = "Annotations";
	protected static final String RELATIONSHIPS_FIELD_TEXT = "Relationships";
	private static final String NO_PACKAGE = "[No Package]";
	
	/**
	 * @param workbook
	 * @param sheetName
	 */
	public PackageSheet(Workbook workbook, String sheetName) {
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
		
		SpdxPackageComparer[] packagComparers = comparer.getPackageComparers();
		Arrays.sort(packagComparers, new Comparator<SpdxPackageComparer>() {

			@Override
			public int compare(SpdxPackageComparer o1, SpdxPackageComparer o2) {
				try {
					return o1.getPackageName().compareTo(o2.getPackageName());
				} catch (SpdxCompareException e) {
					logger.error("Error getting package names during compare",e);
					return 0;	// can't throw an exception
				}
			}
			
		});
		for (int i = 0; i < packagComparers.length; i++) {
			addPackageToSheet(packagComparers[i], comparer.getSpdxDocuments());
		}
	}
	
	/**
	 * @param nextPackages
	 * @throws InvalidSPDXAnalysisException 
	 * @throws SpdxCompareException 
	 */
	private void addPackageToSheet(SpdxPackageComparer comparer, 
			SpdxDocument[] docs) throws InvalidSPDXAnalysisException, SpdxCompareException {
		Row packageNameRow = this.addRow();
		boolean allDocsPresent = comparer.getNumPackages() == docs.length;
		packageNameRow.createCell(FIELD_COL).setCellValue(PACKAGE_NAME_FIELD_TEXT);
		setCellEqualValue(packageNameRow.createCell(EQUALS_COL), allDocsPresent);
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
		Row versionRow = this.addRow();
		versionRow.createCell(FIELD_COL).setCellValue(VERSION_FIELD_TEXT);
		if (comparer.isPackageVersionsEquals()) {
			setCellEqualValue(versionRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(versionRow.createCell(EQUALS_COL));
		}
		Row fileNameRow = this.addRow();
		fileNameRow.createCell(FIELD_COL).setCellValue(FILE_NAME_FIELD_TEXT);
		if (comparer.isPackageFilenamesEquals()) {
			setCellEqualValue(fileNameRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(fileNameRow.createCell(EQUALS_COL));
		}
		Row supplierRow = this.addRow();
		supplierRow.createCell(FIELD_COL).setCellValue(SUPPLIER_FIELD_TEXT);
		if (comparer.isPackageSuppliersEquals()) {
			setCellEqualValue(supplierRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(supplierRow.createCell(EQUALS_COL));
		}
		Row originatorRow = this.addRow();
		originatorRow.createCell(FIELD_COL).setCellValue(ORIGINATOR_FIELD_TEXT);
		if (comparer.isPackageOriginatorsEqual()) {
			setCellEqualValue(originatorRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(originatorRow.createCell(EQUALS_COL));
		}
		Row downloadRow = this.addRow();
		downloadRow.createCell(FIELD_COL).setCellValue(DOWNLOAD_FIELD_TEXT);
		if (comparer.isPackageDownloadLocationsEquals()) {
			setCellEqualValue(downloadRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(downloadRow.createCell(EQUALS_COL));
		}
		Row verificationRow = this.addRow();
		verificationRow.createCell(FIELD_COL).setCellValue(VERIFICATION_FIELD_TEXT);
		if (comparer.isPackageVerificationCodesEquals()) {
			setCellEqualValue(verificationRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(verificationRow.createCell(EQUALS_COL));
		}
		Row verificationExcludedRow = this.addRow();
		verificationExcludedRow.createCell(FIELD_COL).setCellValue(VERIFICATION_EXCLUDED_FIELD_TEXT);
		if (comparer.isPackageVerificationCodesEquals()) {
			setCellEqualValue(verificationExcludedRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(verificationExcludedRow.createCell(EQUALS_COL));
		}
		Row checksumRow = this.addRow();
		checksumRow.createCell(FIELD_COL).setCellValue(CHECKSUM_FIELD_TEXT);
		if (comparer.isPackageChecksumsEquals()) {
			setCellEqualValue(checksumRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(checksumRow.createCell(EQUALS_COL));
		}
		Row homePageRow = this.addRow();
		homePageRow.createCell(FIELD_COL).setCellValue(HOMEPAGE_FIELD_TEXT);
		if (comparer.isPackageHomePagesEquals()) {
			setCellEqualValue(homePageRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(homePageRow.createCell(EQUALS_COL));
		}
		Row sourceInfoRow = this.addRow();
		sourceInfoRow.createCell(FIELD_COL).setCellValue(SOURCEINFO_FIELD_TEXT);
		if (comparer.isPackageSourceInfosEquals()) {
			setCellEqualValue(sourceInfoRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(sourceInfoRow.createCell(EQUALS_COL));
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
		Row declaredLicenseRow = this.addRow();
		declaredLicenseRow.createCell(FIELD_COL).setCellValue(DECLARED_LICENSE_FIELD_TEXT);
		if (comparer.isDeclaredLicensesEquals()) {
			setCellEqualValue(declaredLicenseRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(declaredLicenseRow.createCell(EQUALS_COL));
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
		Row summaryRow = this.addRow();
		summaryRow.createCell(FIELD_COL).setCellValue(SUMMARY_FIELD_TEXT);
		if (comparer.isPackageSummaryEquals()) {
			setCellEqualValue(summaryRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(summaryRow.createCell(EQUALS_COL));
		}
		Row descriptionRow = this.addRow();
		descriptionRow.createCell(FIELD_COL).setCellValue(DESCRIPTION_FIELD_TEXT);
		if (comparer.isPackageDescriptionsEquals()) {
			setCellEqualValue(descriptionRow.createCell(EQUALS_COL), allDocsPresent);
		} else {
			setCellDifferentValue(descriptionRow.createCell(EQUALS_COL));
		}
		
		for (int i = 0; i < docs.length; i++) {
			SpdxPackage pkg = comparer.getDocPackage(docs[i]);
			if (pkg != null) {
				packageNameRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getName());
				idRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getId());
				annotationsRow.createCell(FIRST_DOC_COL+i).setCellValue(CompareHelper.annotationsToString(pkg.getAnnotations()));
				relationshipsRow.createCell(FIRST_DOC_COL+i).setCellValue(CompareHelper.relationshipsToString(pkg.getRelationships()));
				versionRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getVersionInfo());
				fileNameRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getPackageFileName());
				supplierRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getSupplier());
				originatorRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getOriginator());
				downloadRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getDownloadLocation());
				verificationRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getPackageVerificationCode().getValue());
				verificationExcludedRow.createCell(FIRST_DOC_COL+i).setCellValue(exludeFilesToString(pkg.getPackageVerificationCode().getExcludedFileNames()));
				checksumRow.createCell(FIRST_DOC_COL+i).setCellValue(CompareHelper.checksumsToString(pkg.getChecksums()));
				homePageRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getHomepage());
				sourceInfoRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getSourceInfo());
				concludedLicenseRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getLicenseConcluded().toString());
				licenseInfosFromFilesRow.createCell(FIRST_DOC_COL+i).setCellValue(CompareHelper.licenseInfosToString(pkg.getLicenseInfoFromFiles()));
				declaredLicenseRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getLicenseDeclared().toString());
				licenseCommentRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getLicenseComments());
				copyrightRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getCopyrightText());
				summaryRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getSummary());
				descriptionRow.createCell(FIRST_DOC_COL+i).setCellValue(pkg.getDescription());
			} else {
				packageNameRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				idRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				annotationsRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				relationshipsRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				versionRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				fileNameRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				supplierRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				originatorRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				downloadRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				verificationRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				verificationExcludedRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				checksumRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				homePageRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				sourceInfoRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				concludedLicenseRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				licenseInfosFromFilesRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				declaredLicenseRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				licenseCommentRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				copyrightRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				summaryRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
				descriptionRow.createCell(FIRST_DOC_COL+i).setCellValue(NO_PACKAGE);
			}
		}
	}

	/**
	 * @param excludedFileNames
	 * @return
	 */
	protected String exludeFilesToString(String[] excludedFileNames) {
		if (excludedFileNames == null || excludedFileNames.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(excludedFileNames[0]);
		for (int i = 1; i < excludedFileNames.length; i++) {
			sb.append(", ");
			sb.append(excludedFileNames[i]);
		}
		return sb.toString();
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
