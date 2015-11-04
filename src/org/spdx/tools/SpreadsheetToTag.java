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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
		File spdxSpreadsheetFile = new File(args[0]);
		if (!spdxSpreadsheetFile.exists()) {
			System.out.printf("Spreadsheet file %1$s does not exists.\n",
					args[0]);
			return;
		}
		File spdxTagFile = new File(args[1]);
		if (spdxTagFile.exists()) {
			System.out
					.printf("Error: File %1$s already exists - please specify a new file.\n",
							args[1]);
			return;
		}

		try {
			if (!spdxTagFile.createNewFile()) {
				System.out.println("Could not create the new SPDX Tag file "
						+ args[1]);
				usage();
				return;
			}
		} catch (IOException e1) {
			System.out.println("Could not create the new SPDX Tag file "
					+ args[1]);
			System.out.println("due to error " + e1.getMessage());
			usage();
			return;
		}
		PrintWriter out = null;
		try {
			try {
				out = new PrintWriter(spdxTagFile, "UTF-8");
			} catch (IOException e1) {
				System.out.println("Could not write to the new SPDX Tag file "
						+ args[1]);
				System.out.println("due to error " + e1.getMessage());
				usage();
				return;
			}

			SPDXSpreadsheet ss = null;
			try {
				ss = new SPDXSpreadsheet(spdxSpreadsheetFile, false, true);
				SpdxDocument analysis = SpreadsheetToRDF.copySpreadsheetToSPDXAnalysis(ss);
				List<String> verify = analysis.verify();
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
				System.out.println("Error creating or writing to spreadsheet: "
						+ e.getMessage());
			} catch (InvalidSPDXAnalysisException e) {
				System.out.println("Error translating the Tag file: "
						+ e.getMessage());
			} catch (Exception e) {
				System.out.print("Unexpected error displaying SPDX Document: "
						+ e.getMessage());
			} finally {
				if (ss != null) {
					try {
						ss.close();
					} catch (SpreadsheetException e) {
						System.out.println("Error closing spreadsheet: "
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
	}

	private static void usage() {
		System.out
				.println("Usage: SpreadsheetToTag spreadsheetfile.xls spdxfile.spdx \n"
						+ "where spreadsheetfile.xls is a valid SPDX Spreadsheet and\n"
						+ "spdxfile.spdx is the output SPDX tag-value file.");
	}

}
