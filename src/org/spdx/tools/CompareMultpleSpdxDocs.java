/**
 * Copyright (c) 2013 Source Auditor Inc.
 * Copyright (c) 2013 Black Duck Software Inc.
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
import java.util.ArrayList;

import org.spdx.compare.MultiDocumentSpreadsheet;
import org.spdx.compare.SpdxCompareException;
import org.spdx.compare.SpdxComparer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.spdxspreadsheet.SpreadsheetException;

/**
 * Compares multiple SPDX documents and stores the results in a spreadsheet
 * Usage: CompareSpdxDoc output.xls doc1 doc2 doc3 ... docN
 * where output.xls is a file name for the output spreadsheet file
 * and docX are SPDX document files to compare.  Document files can be either in RDF/XML  or tag/value format
 * 
 * @author Gary O'Neall
 *
 */
public class CompareMultpleSpdxDocs {
	static final int MIN_ARGS = 3;
	static final int MAX_ARGS = 14;
	static final int ERROR_STATUS = 1;


	/**
	 * @param args args[0] is the output Excel file name, all other args are SPDX document file names
	 */
	public static void main(String[] args) {
		if (args.length < MIN_ARGS) {
			System.out.println("Insufficient arguments");
			usage();
			System.exit(ERROR_STATUS);
		}
		if (args.length > MAX_ARGS) {
			System.out.println("Too many SPDX documents specified.  Must be less than "+String.valueOf(MAX_ARGS-1)+" document filenames");
			usage();
			System.exit(ERROR_STATUS);
		}
		File outputFile = new File(args[0]);
		if (outputFile.exists()) {
			System.out.println("Output file "+args[0]+" already exists.");
			usage();
			System.exit(ERROR_STATUS);
		}
		SpdxDocument[] compareDocs = new SpdxDocument[args.length-1];
		String[] docNames = new String[args.length-1];
		@SuppressWarnings("unchecked")
		ArrayList<String>[] verificationErrors = new ArrayList[args.length-1];
		for (int i = 1; i < args.length; i++) {
			try {
				compareDocs[i-1] = CompareSpdxDocs.openRdfOrTagDoc(args[i]);
				docNames[i-1]  = CompareSpdxDocs.convertDocName(args[i]);
				verificationErrors[i-1] = compareDocs[i-1].verify();
				if (verificationErrors[i-1] != null && verificationErrors[i-1].size() > 0) {
					System.out.println("Warning: "+docNames[i-1]+" contains verification errors.");
				}
			} catch (SpdxCompareException e) {
				System.out.println("Error opening SPDX document "+args[i]+": "+e.getMessage());
				System.exit(ERROR_STATUS);
			}
		}
		MultiDocumentSpreadsheet outSheet = null;
		try {
			outSheet = new MultiDocumentSpreadsheet(outputFile, true, false);
			outSheet.importVerificationErrors(verificationErrors, docNames);
			SpdxComparer comparer = new SpdxComparer();
			comparer.compare(compareDocs);
			outSheet.importCompareResults(comparer, docNames);
		} catch (SpreadsheetException e) {
			System.out.println("Unable to create output spreadsheet: "+e.getMessage());
			System.exit(ERROR_STATUS);
		} catch (InvalidSPDXAnalysisException e) {
			System.out.println("Invalid SPDX analysis: "+e.getMessage());
			System.exit(ERROR_STATUS);
		} catch (SpdxCompareException e) {
			System.out.println("Error comparing SPDX documents: "+e.getMessage());
			System.exit(ERROR_STATUS);
		} finally {
			if (outSheet != null) {
				try {
					outSheet.close();
				} catch (SpreadsheetException e) {
					System.out.println("Warning - error closing spreadsheet: "+e.getMessage());
				}
			}
		}
	}


	/**
	 * 
	 */
	private static void usage() {
		System.out.println("Usage: CompareMultipleSpdxDoc output.xls doc1 doc2 ... docN");
		System.out.println("where output.xls is a file name for the output spreadsheet file");
		System.out.println("and doc1 through docN are file names of valid SPDX documents ");
		System.out.println("in either tag/value or RDF/XML format");	
	}

}
