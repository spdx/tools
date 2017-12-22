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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Abstract class for implementing file based spreadsheets.
 * @author Gary OpNeall
 *
 */
public abstract class AbstractSpreadsheet {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractSpreadsheet.class.getName());

	protected File saveFile;
	protected Workbook workbook;

	private boolean readonly;

	/**
	 * @param spreadsheetFile
	 * @param create
	 * @throws AnalyzeException
	 */
	public AbstractSpreadsheet(File spreadsheetFile, boolean create, boolean readonly) throws SpreadsheetException {
		this.readonly = readonly;
		if (readonly && create) {
			throw(new SpreadsheetException("Can not create a readonly spreadsheet"));
		}
		if (!spreadsheetFile.exists()) {
			if (!create) {
				throw(new SpreadsheetException("File "+spreadsheetFile.getName()+" does not exist"));
			}
			try {
				create(spreadsheetFile);
			} catch (IOException ex) {
				logger.error("IO error creating spreadsheet: "+ex.getMessage());
				throw(new SpreadsheetException("I/O error creating spreadsheet"));
			}
		}
		this.saveFile = spreadsheetFile;
		InputStream input = null;
		try {
			input = new FileInputStream(spreadsheetFile);
			workbook = WorkbookFactory.create(input);
		} catch (FileNotFoundException ex) {
			logger.error("Can not open Excel file.  File "+
					spreadsheetFile.getName()+" does not exist");
			throw(new SpreadsheetException("Can not open Excel file.  File "+
					spreadsheetFile.getName()+" does not exist"));
		} catch (InvalidFormatException ex) {
			logger.error("Unable to open workbook.  Invalid format: "+ex.getMessage());
			throw(new SpreadsheetException("Unable to open workbook.  Invalid format"));
		} catch (IOException ex) {
			logger.error("IO Exception opening excel workbook: "+ex.getMessage());
			throw(new SpreadsheetException("IO Exception opening excel workbook.  See log for more detail."));
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException ex) {
					logger.warn("IO Error closing excel file: "+ex.getMessage());
				}
			}
		}
	}

	public abstract void create(File spreadsheetFile) throws IOException, SpreadsheetException;
	public abstract void clear();
	public abstract String verifyWorkbook();

	/**
	 * Writes the spreadsheet to a file
	 * @throws IOException
	 */
	public void writeToFile(File file) throws IOException {
		if (readonly) {
			return;
		}
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			this.workbook.write(out);
		} finally {
			if (out != null) {
				out.close();
			}
		}

	}

	/**
	 * @throws AnalyzeException
	 *
	 */
	public void close() throws SpreadsheetException {
		try {
			writeToFile(this.saveFile);
		} catch (IOException ex) {
			logger.error("Error writing excel sheet to file: "+ex.getMessage());
			throw(new SpreadsheetException("Error writing excel workbook to file, see log for details."));
		}
	}
}
