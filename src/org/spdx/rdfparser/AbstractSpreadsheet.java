/**
 * Copyright (c) 2011 Source Auditor Inc.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spdx.rdfparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Abstract class for implementing file based spreadsheets.
 * @author Gary OpNeall
 *
 */
public abstract class AbstractSpreadsheet {

	static final Logger logger = Logger.getLogger(AbstractSpreadsheet.class.getName());
	
	protected File saveFile;
	protected Workbook workbook;

	/**
	 * @param spreadsheetFile
	 * @param create
	 * @throws AnalyzeException 
	 */
	public AbstractSpreadsheet(File spreadsheetFile, boolean create) throws SpreadsheetException {
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
