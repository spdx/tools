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
package org.spdx.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.spdxspreadsheet.SPDXSpreadsheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;
import org.spdx.tag.CommonCode;


/**
 * Converts a spreadsheet to a tag-value format Usage: SpreadsheetToTag
 * spreadsheetfile.xls spdxfile.spdx where spreadsheetfile.xls is a valid SPDX
 * Spreadsheet and spdxfile.spdx is the output SPDX tag-value file.
 *
 * @author Rana Rahal, Protecode Inc.
 *
 */
public class SpreadsheetToTag {

	static final int MIN_ARGS = 2;
	static final int MAX_ARGS = 2;
	static DateFormat format = new SimpleDateFormat(
			SpdxRdfConstants.SPDX_DATE_FORMAT);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < MIN_ARGS) {
			usage();
			return;
		}
		if (args.length > MAX_ARGS) {
			usage();
			return;
		}
		try {
			onlineFunction(args);
		} catch (OnlineToolException e){
			System.out.println(e.getMessage());
			usage();
			return;
		}
	}

	/**
	 *
	 * @param args args[0] is the Spreadsheet file to be converted, args[1] is the result Tag Value file name
	 * @throws OnlineToolException Exception caught by JPype and displayed to the user
	 * @return Warnings of the conversion, displayed to the user
	 */
	public static List<String> onlineFunction(String[] args) throws OnlineToolException{
		// Arguments length(args length== 2 ) will checked in the Python Code
		File spdxSpreadsheetFile = new File(args[0]);
		if (!spdxSpreadsheetFile.exists()) {
			throw new OnlineToolException("Spreadsheet file " + args[0] + " does not exists.");
		}
		File spdxTagFile = new File(args[1]);
		// Output File name will be checked in the Python code for no clash, but if still found
		if (spdxTagFile.exists()) {
			throw new OnlineToolException("Error: File " + args[1] +" already exists - please specify a new file.");
		}

		try {
			if (!spdxTagFile.createNewFile()) {
				throw new OnlineToolException("Could not create the new SPDX Tag file "
						+ args[1]);
			}
		} catch (IOException e1) {
			throw new OnlineToolException("Could not create the new SPDX Tag file "
					+ args[1] + "due to error " + e1.getMessage());
		}
		PrintWriter out = null;
		List<String> verify = new ArrayList<String>();
		try {
			try {
				out = new PrintWriter(spdxTagFile, "UTF-8");
			} catch (IOException e1) {
				throw new OnlineToolException("Could not write to the new SPDX Tag file "
						+ args[1] + "due to error " + e1.getMessage());
			}

			SPDXSpreadsheet ss = null;
			try {
				ss = new SPDXSpreadsheet(spdxSpreadsheetFile, false, true);
				SpdxDocument analysis = SpreadsheetToRDF.copySpreadsheetToSPDXAnalysis(ss);
				verify = analysis.verify();
				if (verify.size() > 0) {
					System.out
							.println("Warning: The following verification errors were found in the resultant SPDX Document:");
					for (int i = 0; i < verify.size(); i++) {
						System.out.println("\t" + verify.get(i));
					}
				}
				// read the constants from a file
				Properties constants = CommonCode
						.getTextFromProperties("org/spdx/tag/SpdxTagValueConstants.properties");
				CommonCode.printDoc(analysis, out, constants);
			} catch (SpreadsheetException e) {
				throw new OnlineToolException("Error creating or writing to spreadsheet: "
						+ e.getMessage());
			} catch (InvalidSPDXAnalysisException e) {
				throw new OnlineToolException("Error translating the Tag file: "
						+ e.getMessage());
			} catch (Exception e) {
				throw new OnlineToolException("Unexpected error converting SPDX Document: "
						+ e.getMessage());
			} finally {
				if (ss != null) {
					try {
						ss.close();
					} catch (SpreadsheetException e) {
						throw new OnlineToolException("Error closing spreadsheet: "
								+ e.getMessage());
					}
				}
			}
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
		return verify;
	}
	private static void usage() {
		System.out
				.println("Usage: SpreadsheetToTag spreadsheetfile.xls spdxfile.spdx \n"
						+ "where spreadsheetfile.xls is a valid SPDX Spreadsheet and\n"
						+ "spdxfile.spdx is the output SPDX tag-value file.");
	}

}
