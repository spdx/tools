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

	protected static final Logger logger = Logger.getLogger(AbstractSpreadsheet.class.getName());
	
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
