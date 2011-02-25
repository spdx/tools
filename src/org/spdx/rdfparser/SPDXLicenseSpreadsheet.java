/**
 * Copyright (c) 2011 Source Auditor Inc.
 * * Redistribution and use in source and binary forms, with or without
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
package org.spdx.rdfparser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * A spreadhseet containing license information
 * @author Source Auditor
 *
 */
public class SPDXLicenseSpreadsheet extends AbstractSpreadsheet {
	
	public class LicenseIterator implements Iterator<SPDXLicense> {

		private int currentRowNum;
		SPDXLicense currentLicense;
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
		public SPDXLicense next() {
			SPDXLicense retval = currentLicense;
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

	public Iterator<SPDXLicense> getIterator() {
		return new LicenseIterator();
	}
	
	
}
