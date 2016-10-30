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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.ISpdxListedLicenseProvider;
import org.spdx.rdfparser.license.LicenseException;
import org.spdx.rdfparser.license.LicenseRestrictionException;
import org.spdx.rdfparser.license.SpdxListedLicense;

/**
 * A spreadhseet containing license information
 * @author Source Auditor
 *
 */
public class SPDXLicenseSpreadsheet extends AbstractSpreadsheet implements ISpdxListedLicenseProvider  {
	
	public class LicenseIterator implements Iterator<SpdxListedLicense> {
		private int currentRowNum;
		SpdxListedLicense currentLicense;
		public LicenseIterator() throws SpreadsheetException {
			this.currentRowNum = licenseSheet.getFirstDataRow();	// skip past header row
			try {
                currentLicense = licenseSheet.getLicense(currentRowNum);
            } catch (InvalidSPDXAnalysisException e) {
                throw new SpreadsheetException(e.getMessage());
            }
		}
		@Override
		public boolean hasNext() {
			return currentLicense != null;
		}

		@Override
		public SpdxListedLicense next() {
			SpdxListedLicense retval = currentLicense;
			currentRowNum++;
			try {
                currentLicense = licenseSheet.getLicense(currentRowNum);
            } catch (InvalidSPDXAnalysisException e) {
                throw new RuntimeException(e.getMessage());
            }
			return retval;
		}

		@Override
		public void remove() {
			// not implementd
		}
		
	}
	
	public class LicenseExceptionIterator implements Iterator<LicenseException> {

		private int currentRowNum;
		LicenseException currentException;
		public LicenseExceptionIterator() throws SpreadsheetException {
			this.currentRowNum = exceptionSheet.getFirstDataRow();	// skip past header row
            currentException = exceptionSheet.getException(currentRowNum);
		}
		@Override
		public boolean hasNext() {
			return currentException != null;
		}

		@Override
		public LicenseException next() {
			LicenseException retval = currentException;
			currentRowNum++;
			currentException = exceptionSheet.getException(currentRowNum);
			return retval;
		}

		@Override
		public void remove() {
			// not implementd
		}		
	}
	
	public static class DeprecatedLicenseInfo {
		private SpdxListedLicense license;
		private String deprecatedVersion;
		public DeprecatedLicenseInfo(SpdxListedLicense license, String deprecatedVersion) {
			this.license = license;
			this.deprecatedVersion = deprecatedVersion;
		}
		/**
		 * @return the license
		 */
		public SpdxListedLicense getLicense() {
			return license;
		}
		/**
		 * @param license the license to set
		 */
		public void setLicense(SpdxListedLicense license) {
			this.license = license;
		}
		/**
		 * @return the deprecatedVersion
		 */
		public String getDeprecatedVersion() {
			return deprecatedVersion;
		}
		/**
		 * @param deprecatedVersion the deprecatedVersion to set
		 */
		public void setDeprecatedVersion(String deprecatedVersion) {
			this.deprecatedVersion = deprecatedVersion;
		}
	}
	
	public class DeprecatedLicenseIterator implements Iterator<DeprecatedLicenseInfo> {

		private int currentRowNum;
		DeprecatedLicenseInfo currentDeprecatedLicense;
		public DeprecatedLicenseIterator() throws SpreadsheetException, InvalidSPDXAnalysisException {
			this.currentRowNum = deprecatedLicenseSheet.getFirstDataRow();	// skip past header row
			updateCurrentDeprecatedLicense();
		}
		
		private void updateCurrentDeprecatedLicense() throws InvalidSPDXAnalysisException {
			SpdxListedLicense license = deprecatedLicenseSheet.getLicense(currentRowNum);
			if (license == null) {
				currentDeprecatedLicense = null;
			} else {
				currentDeprecatedLicense = new DeprecatedLicenseInfo(
						license, deprecatedLicenseSheet.getDeprecatedVersion(currentRowNum));
			}	
		}
		@Override
		public boolean hasNext() {
			return currentDeprecatedLicense != null;
		}

		@Override
		public DeprecatedLicenseInfo next() {
			DeprecatedLicenseInfo retval = currentDeprecatedLicense;
			currentRowNum++;
			try {
				updateCurrentDeprecatedLicense();
			} catch (InvalidSPDXAnalysisException e) {
				throw(new RuntimeException(e));
			}
			return retval;
		}

		@Override
		public void remove() {
			// not implementd
		}		
	}
	
	static final String LICENSE_SHEET_NAME = "Licenses";
	static final String EXCEPTION_SHEET_NAME = "exceptions";
	static final String DEPRECATED_SHEET_NAME = "deprecated";
	private LicenseSheet licenseSheet;
	private LicenseExceptionSheet exceptionSheet;
	private DeprecatedLicenseSheet deprecatedLicenseSheet;
	private List<String> warnings = new ArrayList<String>();
	
	public SPDXLicenseSpreadsheet(File spreadsheetFile, boolean create, boolean readonly)
			throws SpreadsheetException {
		super(spreadsheetFile, create, readonly);
		this.licenseSheet = new LicenseSheet(this.workbook, LICENSE_SHEET_NAME, spreadsheetFile);
		this.deprecatedLicenseSheet = new DeprecatedLicenseSheet(this.workbook, DEPRECATED_SHEET_NAME, spreadsheetFile);
		this.exceptionSheet = new LicenseExceptionSheet(this.workbook, EXCEPTION_SHEET_NAME);
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
		create(spreadsheetFile, "Unknown", "Unknown");
	}
	
	public void create(File spreadsheetFile, String version, String releaseDate) throws IOException,
	SpreadsheetException {
		if (!spreadsheetFile.createNewFile()) {
			logger.error("Unable to create "+spreadsheetFile.getName());
			throw(new SpreadsheetException("Unable to create "+spreadsheetFile.getName()));
		}
		FileOutputStream excelOut = null;
		try {
			excelOut = new FileOutputStream(spreadsheetFile);
			Workbook wb = new HSSFWorkbook();
			LicenseSheet.create(wb, LICENSE_SHEET_NAME, version, releaseDate);
			LicenseExceptionSheet.create(wb, EXCEPTION_SHEET_NAME);
			DeprecatedLicenseSheet.create(wb, DEPRECATED_SHEET_NAME);
			wb.write(excelOut);
		} finally {
		    if(excelOut != null){
		        excelOut.close();
		    }
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.AbstractSpreadsheet#clear()
	 */
	@Override
	public void clear() {
		this.licenseSheet.clear();
		this.exceptionSheet.clear();
		this.deprecatedLicenseSheet.clear();
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.AbstractSpreadsheet#verifyWorkbook()
	 */
	@Override
	public String verifyWorkbook() {
		String retval = this.exceptionSheet.verify();
		if (retval != null && !retval.isEmpty()) {
			return retval;
		}
		retval = this.deprecatedLicenseSheet.verify();
		if (retval != null && !retval.isEmpty()) {
			return retval;
		}
		return this.licenseSheet.verify();
	}

	/**
	 * @return the licenseSheet
	 */
	public LicenseSheet getLicenseSheet() {
		return licenseSheet;
	}

	@Override
    public Iterator<SpdxListedLicense> getLicenseIterator() {
		try {
            return new LicenseIterator();
        } catch (SpreadsheetException e) {
            throw new RuntimeException(e);
        }
	}
	
	@Override
    public Iterator<DeprecatedLicenseInfo> getDeprecatedLicenseIterator() {
		try {
            return new DeprecatedLicenseIterator();
        } catch (SpreadsheetException e) {
            throw new RuntimeException(e);
        } catch (InvalidSPDXAnalysisException e) {
            throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IStandardLicenseProvider#getExceptionIterator()
	 */
	@Override
	public Iterator<LicenseException> getExceptionIterator()
			throws LicenseRestrictionException, SpreadsheetException {
		return new LicenseExceptionIterator();
	}
	
	public LicenseExceptionSheet getLicenseExceptionSheet() {
		return this.exceptionSheet;
	}

	/**
	 * 
	 */
	public DeprecatedLicenseSheet getDeprecatedLicenseSheet() {
		return this.deprecatedLicenseSheet;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.ISpdxListedLicenseProvider#getWarnings()
	 */
	@Override
	public List<String> getWarnings() {
		return this.warnings;
	}
}
