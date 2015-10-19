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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.LicenseSheet;
import org.spdx.rdfparser.SPDXStandardLicense;

/**
 * A spreadhseet containing license information
 * @author Source Auditor
 *
 */
public class SPDXLicenseSpreadsheet extends AbstractSpreadsheet {

	public class LicenseIterator implements Iterator<SPDXStandardLicense> {

		private int currentRowNum;
		SPDXStandardLicense currentLicense;
		public LicenseIterator() {
			this.currentRowNum = 2;	// skip past header row
			currentLicense = licenseSheet.getLicense(currentRowNum);
		}
		@Override
		public boolean hasNext() {
			if (currentLicense == null) {
				return false;
			} else {
				return true;
			}
		}

		@Override
		public SPDXStandardLicense next() {
			SPDXStandardLicense retval = currentLicense;
			currentRowNum++;
			currentLicense = licenseSheet.getLicense(currentRowNum);
			return retval;
		}

		@Override
		public void remove() {
			// not implementd
		}

	}
	static final String LICENSE_SHEET_NAME = "Licenses";

	private LicenseSheet licenseSheet;

	public SPDXLicenseSpreadsheet(File spreadsheetFile, boolean create, boolean readonly)
			throws SpreadsheetException {
		super(spreadsheetFile, create, readonly);
		this.licenseSheet = new LicenseSheet(this.workbook, LICENSE_SHEET_NAME);
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
			LicenseSheet.create(wb, LICENSE_SHEET_NAME);
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
		this.licenseSheet.clear();
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.AbstractSpreadsheet#verifyWorkbook()
	 */
	@Override
	public String verifyWorkbook() {
		return this.licenseSheet.verify();
	}

	/**
	 * @return the licenseSheet
	 */
	public LicenseSheet getLicenseSheet() {
		return licenseSheet;
	}

	public Iterator<SPDXStandardLicense> getIterator() {
		return new LicenseIterator();
	}


}
