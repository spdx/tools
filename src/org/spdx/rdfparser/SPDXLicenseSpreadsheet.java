/**
 * 
 */
package org.spdx.rdfparser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
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

	public SPDXLicenseSpreadsheet(File spreadsheetFile, boolean create)
			throws SpreadsheetException {
		super(spreadsheetFile, create);
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
