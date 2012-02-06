/**
 * Copyright (c) 2012 Source Auditor Inc.
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
package org.spdx.rdfparser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

/**
 * A CSV file reader for SPDX Standard Licenses.
 * 
 * The CSV file must be tab delimited, use " for text delimiters, contain a header row with the following text (exactly):
 * "Full name of License"	"License Identifier"	"Source/url"	"Notes"	"OSI Approved"	"Standard License Header"	"Text"	"Template"
 * @author Gary O'Neall
 *
 */
public class SpdxLicenseCsv implements IStandardLicenseProvider {
	
	static final Logger logger = Logger.getLogger(SpdxLicenseCsv.class.getName());
	public static final int LICENSE_NAME_COL = 0;
	public static final int LICENSE_ID_COL = 1;
	public static final int LICENSE_NOTES_COL = 3;
	public static final int LICENSE_TEXT_COL = 6;
	public static final int LICENSE_URL_COL = 2;
	public static final int LICENSE_TEMPLATE_COL = 7;
	public static final int LICENSE_HEADER_COL = 5;
	public static final int IS_OSI_APPROVED_COL = 4;
	static final char DELIM = ',';
	static final char STRING_DELIM = '"';
	static final String[] HEADER_ROW = new String[] {
		"Full name of License","License Identifier","Source/url","Notes","OSI Approved","Standard License Header","Text","Template"
	};
	private static final int NUM_COLS = HEADER_ROW.length;

	
	class CsvLicenseIterator implements Iterator<SPDXStandardLicense> {
		
		private CSVReader iterReader = null;
		SPDXStandardLicense nextStandardLicense = null;
		
		/**
		 * @param csvFile
		 * @throws IOException 
		 * @throws LicenseCsvException 
		 */
		public CsvLicenseIterator(File csvFile) throws IOException, LicenseCsvException {
			iterReader = new CSVReader(new FileReader(csvFile), DELIM, STRING_DELIM);
			@SuppressWarnings("unused")
			String[] tempHeaderRecord = iterReader.readNext();
			readNextStandardLicense();
		}

		/**
		 * reads the next standard license in the csv file int nextStandardLicense, or null if no more rows exist
		 * @throws IOException 
		 * @throws LicenseCsvException 
		 */
		private void readNextStandardLicense() throws IOException, LicenseCsvException {
			String[] nextRow = iterReader.readNext();
			if (nextRow == null) {
				nextStandardLicense = null;
				return;
			}
			if (nextRow.length != HEADER_ROW.length) {
				nextStandardLicense = null;
				throw (new LicenseCsvException("Invalid number of columns.  Expected "+String.valueOf(HEADER_ROW.length)
						+", found "+String.valueOf(nextRow.length)));
			}
			try {
				nextStandardLicense = new SPDXStandardLicense(nextRow[LICENSE_NAME_COL], 
						nextRow[LICENSE_ID_COL], nextRow[LICENSE_TEXT_COL],
						nextRow[LICENSE_URL_COL], nextRow[LICENSE_NOTES_COL],
						nextRow[LICENSE_HEADER_COL], nextRow[LICENSE_TEMPLATE_COL],
						isTrue(nextRow[IS_OSI_APPROVED_COL]));
			} catch (InvalidSPDXAnalysisException e) {
				nextStandardLicense = null;
				throw(new LicenseCsvException("Error creating standard license: "+e.getMessage(), e));
			}
		}

		/**
		 * @param string
		 * @return
		 */
		private boolean isTrue(String string) {
			if (string == null || string.isEmpty()) {
				return false;
			}
			if (string.toUpperCase().equals("Y") || (string.toUpperCase().equals("YES"))) {
				return true;
			} else {
				return false;
			}
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			if (this.nextStandardLicense == null) {
				return false;
			} else {
				return true;
			}
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public SPDXStandardLicense next() {
			SPDXStandardLicense retval = this.nextStandardLicense;
			if (retval != null) {
				try {
					readNextStandardLicense();
				} catch (IOException e) {
					logger.error("IO Exception getting next record: "+e.getMessage());
				} catch (LicenseCsvException e) {
					logger.error("License csv file format error getting next record: "+e.getMessage());
				}
			}
			return retval;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			
		}

		/**
		 * 
		 */
		public void close() {
			if (this.iterReader != null) {
				try {
					this.iterReader.close();
					this.iterReader = null;
				} catch (IOException e) {
					logger.error("IO error closing CSV reader: "+e.getMessage());
				}
			}
		}
		
	}

	private File csvFile = null;
	private CSVReader reader = null;
	private ArrayList<CsvLicenseIterator> openIterators = new ArrayList<CsvLicenseIterator>();
	/**
	 * @param csvFile
	 * @throws IOException 
	 * @throws LicenseCsvException 
	 */
	public SpdxLicenseCsv(File csvFile) throws IOException, LicenseCsvException {
		this.csvFile = csvFile;
		reader = new CSVReader(new FileReader(csvFile), DELIM, STRING_DELIM);
		String[] header = reader.readNext();
		if (header.length != NUM_COLS) {
			throw(new LicenseCsvException("Incorrect number of columns for License CSV file.  Expected "+
					String.valueOf(NUM_COLS)+", found "+String.valueOf(header.length)));
		}
		for (int i = 0; i < header.length; i++) {
			if (!header[i].equals(HEADER_ROW[i])) {
				throw(new LicenseCsvException("Invalid row header at column "+
						String.valueOf(i)+
						", expected "+HEADER_ROW[i]+", found "+header[i]));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.IStandardLicenseProvider#getIterator()
	 */
	@Override
	public Iterator<SPDXStandardLicense> getIterator() throws LicenseCsvException  {
		CsvLicenseIterator retval;
		try {
			retval = new CsvLicenseIterator(this.csvFile);
		} catch (IOException e) {
			logger.error("IO Exception getting license iterator: "+e.getMessage());
			throw(new LicenseCsvException("IO Exception getting license iterator: "+e.getMessage()));
		} catch (LicenseCsvException e) {
			logger.error("License csv file format error: "+e.getMessage());
			throw(e);
		}
		openIterators.add(retval);
		return retval;
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void close() throws IOException {
		// close all iterators
		for (int i = 0; i < openIterators.size(); i++) {
			openIterators.get(i).close();
		}
		if (reader != null) {
			reader.close();
			reader = null;
		}
	}

}
