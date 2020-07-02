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
package org.spdx.spdxspreadsheet;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.poi.ss.usermodel.Workbook;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.model.SpdxFile;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Abstract class for PerFileSheet.  Specific version implementations are implemented
 * as subclasses.
 * 
 * @author Gary O'Neall
 *
 */
public abstract class PerFileSheet extends AbstractSheet {
	
	static final char CSV_SEPARATOR_CHAR = ',';
	static final char CSV_QUOTING_CHAR = '"';
	
	protected String version;
	
	public PerFileSheet(Workbook workbook, String sheetName, String version) {
		super(workbook, sheetName);
		this.version = version;
	}

	/**
	 * Open a specific version of the PerFileSheet
	 * @param workbook
	 * @param perFileSheetName
	 * @param version spreadsheet version
	 * @return
	 */
	public static PerFileSheet openVersion(Workbook workbook,
			String perFileSheetName, String version) {
		if (version.compareToIgnoreCase(SPDXSpreadsheet.VERSION_0_9_4) <= 0) {
			return new PerFileSheetV09d3(workbook, perFileSheetName, version);
		} else if (version.compareToIgnoreCase(SPDXSpreadsheet.VERSION_1_1_0) <= 0) {
			return new PerFileSheetV1d1(workbook, perFileSheetName, version);
		} else if (version.compareToIgnoreCase(SPDXSpreadsheet.VERSION_1_2_0) <=0) {
			return new PerFileSheetV1d2(workbook, perFileSheetName, version);
		} else if (version.compareTo(SPDXSpreadsheet.VERSION_2_1_0) <= 0) {
			// Note: No changes in version 2.1 for the file
			return new PerFileSheetV2d0(workbook, perFileSheetName, version);
		} else {
			return new PerFileSheetV2d2(workbook, perFileSheetName, version);
		}
	}
	
	/**
	 * converts an array of strings to a comma separated list
	 * @param strings
	 * @return
	 */
	public static String stringsToCsv(String[] strings) {
		StringWriter writer = new StringWriter();
		CSVWriter csvWriter = new CSVWriter(writer, CSV_SEPARATOR_CHAR, CSV_QUOTING_CHAR);
		try {
			csvWriter.writeNext(strings);
			csvWriter.flush();
			String retval = writer.toString().trim();
			return retval;
		} catch (Exception e) {
			return "ERROR PARSING CSV Entries";
		} finally {
			try {
				csvWriter.close();
			} catch (IOException e) {
				// ignore the close errors
			}
		}
	}
	
	/**
	 * Converts a comma separated CSV string to an array of strings
	 * @param csv
	 * @return
	 */
	public static String[] csvToStrings(String csv) {
		StringReader reader = new StringReader(csv);
		CSVReader csvReader = new CSVReader(reader, CSV_SEPARATOR_CHAR, CSV_QUOTING_CHAR);
		try {
			return csvReader.readNext();
		} catch (IOException e) {
			return new String[] {"ERROR PARSING CSV String"};
		} finally {
			try {
				csvReader.close();
			} catch (IOException e) {
				// Ignore
			}
		}
	}
	
	/**
	 * Add the file to the spreadsheet
	 * @param file
	 * @param pkgIds string containing the package ID's which contain this file
	 */
	public abstract void add(SpdxFile file, String pkgIds);
	
	/**
	 * Get the file information for a row in the PerFileSheet
	 * @param rowNum
	 * @return
	 */
	public abstract SpdxFile getFileInfo(int rowNum, SpdxDocumentContainer container) throws SpreadsheetException;

	/**	
	 * Create a blank worksheet NOTE: Replaces / deletes existing sheet by the same name
	 * @param wb
	 * @param perFileSheetName
	 */
	public static void create(Workbook wb, String perFileSheetName) {
		//NOTE: This needs to be updated the the most current version
		PerFileSheetV2d2.create(wb, perFileSheetName);
	}

	/**
	 * @param row
	 * @return
	 */
	public abstract String[] getPackageIds(int row);
}
