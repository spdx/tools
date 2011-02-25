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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.AbstractSpreadsheet;
import org.spdx.rdfparser.SpreadsheetException;

/**
 * A spreadsheet containing information on an SPDX Document.
 * 
 * The spreadsheet contains 4 sheets:
 *   - Origins - Information about the origin of the SPDX Document (Version, createdby, ...)
 *   - Package Info - Information about the package itself
 *   - Non-standard licenses - text from any non-standard licenses found
 *   - Per File Info - Information about each file in the document
 *   - Reviewers - Information on any organizations who have reviewed the documents
 * @author Gary O'Neall
 *
 */
public class SPDXSpreadsheet extends AbstractSpreadsheet {

	private OriginsSheet originsSheet;
	static final String ORIGIN_SHEET_NAME = "Origins";
	private PackageInfoSheet packageInfoSheet;
	static final String PACKAGE_INFO_SHEET_NAME = "Package Info";
	private NonStandardLicensesSheet nonStandardLicensesSheet;
	static final String NON_STANDARD_LICENSE_SHEET_NAME = "Non Standard Licenses";
	private PerFileSheet perFileSheet;
	static final String PER_FILE_SHEET_NAME = "Per File Info";
	private ReviewersSheet reviewersSheet;
	static final String REVIEWERS_SHEET_NAME = "Reviewers";
	
	/**
	 * @param spreadsheetFile
	 * @param create
	 * @param readonly
	 * @throws SpreadsheetException
	 */
	public SPDXSpreadsheet(File spreadsheetFile, boolean create,
			boolean readonly) throws SpreadsheetException {
		super(spreadsheetFile, create, readonly);
		this.originsSheet = new OriginsSheet(this.workbook, ORIGIN_SHEET_NAME);
		this.packageInfoSheet = new PackageInfoSheet(this.workbook, PACKAGE_INFO_SHEET_NAME);
		this.nonStandardLicensesSheet = new NonStandardLicensesSheet(this.workbook, NON_STANDARD_LICENSE_SHEET_NAME);
		this.perFileSheet = new PerFileSheet(this.workbook, PER_FILE_SHEET_NAME);
		this.reviewersSheet = new ReviewersSheet(this.workbook, REVIEWERS_SHEET_NAME);
		String verifyMsg = verifyWorkbook();
		if (verifyMsg != null) {
			logger.error(verifyMsg);
			throw(new SpreadsheetException(verifyMsg));
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.AbstractSpreadsheet#create(java.io.File)
	 */
	@Override
	public void create(File spreadsheetFile) throws IOException,
			SpreadsheetException {
		if (!spreadsheetFile.createNewFile()) {
			logger.error("Unable to create "+spreadsheetFile.getName());
			throw(new SpreadsheetException("Unable to create "+spreadsheetFile.getName()));
		}
		FileOutputStream excelOut = null;
		try {
			excelOut = new FileOutputStream(spreadsheetFile);
			Workbook wb = new HSSFWorkbook();
			OriginsSheet.create(wb, ORIGIN_SHEET_NAME);
			PackageInfoSheet.create(wb, PACKAGE_INFO_SHEET_NAME);
			NonStandardLicensesSheet.create(wb, NON_STANDARD_LICENSE_SHEET_NAME);
			PerFileSheet.create(wb, PER_FILE_SHEET_NAME);
			ReviewersSheet.create(wb, REVIEWERS_SHEET_NAME);
			wb.write(excelOut);
		} finally {
			excelOut.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.AbstractSpreadsheet#clear()
	 */
	@Override
	public void clear() {
		this.originsSheet.clear();
		this.packageInfoSheet.clear();
		this.nonStandardLicensesSheet.clear();
		this.perFileSheet.clear();
		this.reviewersSheet.clear();
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.AbstractSpreadsheet#verifyWorkbook()
	 */
	@Override
	public String verifyWorkbook() {
		String retval = this.originsSheet.verify();
		if (retval == null || retval.isEmpty()) {
			retval = this.packageInfoSheet.verify();
		}
		if (retval == null || retval.isEmpty()) {
			retval = this.nonStandardLicensesSheet.verify();
		}
		if (retval == null || retval.isEmpty()) {
			retval = this.perFileSheet.verify();
		}
		if (retval == null || retval.isEmpty()) {
			retval = this.reviewersSheet.verify();
		}
		return retval;
	}

	/**
	 * @return the originsSheet
	 */
	public OriginsSheet getOriginsSheet() {
		return originsSheet;
	}

	/**
	 * @param originsSheet the originsSheet to set
	 */
	public void setOriginsSheet(OriginsSheet originsSheet) {
		this.originsSheet = originsSheet;
	}

	/**
	 * @return the packageInfoSheet
	 */
	public PackageInfoSheet getPackageInfoSheet() {
		return packageInfoSheet;
	}

	/**
	 * @param packageInfoSheet the packageInfoSheet to set
	 */
	public void setPackageInfoSheet(PackageInfoSheet packageInfoSheet) {
		this.packageInfoSheet = packageInfoSheet;
	}

	/**
	 * @return the nonStandardLicensesSheet
	 */
	public NonStandardLicensesSheet getNonStandardLicensesSheet() {
		return nonStandardLicensesSheet;
	}

	/**
	 * @param nonStandardLicensesSheet the nonStandardLicensesSheet to set
	 */
	public void setNonStandardLicensesSheet(
			NonStandardLicensesSheet nonStandardLicensesSheet) {
		this.nonStandardLicensesSheet = nonStandardLicensesSheet;
	}

	/**
	 * @return the perFileSheet
	 */
	public PerFileSheet getPerFileSheet() {
		return perFileSheet;
	}

	/**
	 * @param perFileSheet the perFileSheet to set
	 */
	public void setPerFileSheet(PerFileSheet perFileSheet) {
		this.perFileSheet = perFileSheet;
	}

	/**
	 * @return the reviewersSheet
	 */
	public ReviewersSheet getReviewersSheet() {
		return reviewersSheet;
	}

	/**
	 * @param reviewersSheet the reviewersSheet to set
	 */
	public void setReviewersSheet(ReviewersSheet reviewersSheet) {
		this.reviewersSheet = reviewersSheet;
	}

}
