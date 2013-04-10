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
package org.spdx.tag;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.spdxspreadsheet.RdfToSpreadsheet;
import org.spdx.spdxspreadsheet.SPDXSpreadsheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Translates an tag-value file to a SPDX Spreadsheet format Usage:
 * TagToSpreadsheet spdxfile.spdx spreadsheetfile.xls where spdxfile.spdx is a
 * valid SPDX tag-value file and spreadsheetfile.xls is the output SPDX
 * spreadsheeet file.
 * 
 * @author Rana Rahal, Protecode Inc.
 */
public class TagToSpreadsheet {

	static final int MIN_ARGS = 2;
	static final int MAX_ARGS = 2;

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
		FileInputStream spdxTagFile;
		try {
			spdxTagFile = new FileInputStream(args[0]);
		} catch (FileNotFoundException ex) {
			System.out
					.printf("Tag-Value file %1$s does not exists.\n", args[0]);
			return;
		}
		File spdxSpreadsheetFile = new File(args[1]);
		if (spdxSpreadsheetFile.exists()) {
			System.out
					.println("Spreadsheet file already exists - please specify a new file.");
			return;
		}
		SPDXDocument doc = null;
		try {
			// read the tag-value constants from a file
			Properties constants = CommonCode.getTextFromProperties("org/spdx/tag/SpdxTagValueConstants.properties");
			TagValueLexer lexer = new TagValueLexer(new NoCommentInputStream(spdxTagFile));
			TagValueParser parser = new TagValueParser(lexer);
			Model model = ModelFactory.createDefaultModel();
			doc = new SPDXDocument(model);
			parser.setBehavior(new BuildDocument(model, doc, constants));
			parser.data();
		} catch (Exception e) {
			System.err.println("Error creating SPDX Analysis: " + e);
		}
		SPDXSpreadsheet ss = null;
		try {
			ss = new SPDXSpreadsheet(spdxSpreadsheetFile, true, false);
			RdfToSpreadsheet.copyRdfXmlToSpreadsheet(doc, ss);
		} catch (SpreadsheetException e) {
			System.out.println("Error opening or writing to spreadsheet: "
					+ e.getMessage());
		} catch (InvalidSPDXAnalysisException e) {
			System.out.println("Error translating the Tag file: "
					+ e.getMessage());
		} catch (Exception ex) {
			System.out
					.println("Unexpected error translating the tag-value to spreadsheet: "
							+ ex.getMessage());
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
	}

	private static void usage() {
		System.out
				.println("Usage: TagToSpreadsheet spdxfile.spdx spreadsheetfile.xls \n"
						+ "where spdxfile.spdx is a valid SPDX tag-value file and spreadsheetfile.xls is \n"
						+ "the output SPDX spreadsheeet file.");
	}
}
