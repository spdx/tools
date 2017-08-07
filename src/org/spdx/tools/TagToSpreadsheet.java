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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.spdxspreadsheet.SPDXSpreadsheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;
import org.spdx.tag.BuildDocument;
import org.spdx.tag.CommonCode;
import org.spdx.tag.HandBuiltParser;
import org.spdx.tag.InvalidFileFormatException;
import org.spdx.tag.InvalidSpdxTagFileException;
import org.spdx.tag.NoCommentInputStream;

import antlr.RecognitionException;
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
	 * @param args args[0] is the Tag Value file to be converted, args[1] is the result spreadsheet file name
	 * @throws OnlineToolException Exception caught by JPype and displayed to the user
	 * @return Warnings of the conversion, display to the user
	 */
	public static List<String> onlineFunction(String[] args) throws OnlineToolException{
		// Arguments length(args length== 2 ) will checked in the Python Code
		FileInputStream spdxTagFile;
		try {
			spdxTagFile = new FileInputStream(args[0]);
		} catch (FileNotFoundException ex) {
			throw new OnlineToolException("Tag-Value file "+ args[0]+" does not exists.");
		}
		File spdxSpreadsheetFile = new File(args[1]);
		// Output File name will be checked in the Python code for no clash, but if still found
		if (spdxSpreadsheetFile.exists()) {
			try {
				spdxTagFile.close();
			} catch (IOException e) {
				throw new OnlineToolException("Warning: Unable to close output SPDX tag file on error: "+e.getMessage());
			}
			throw new OnlineToolException("Spreadsheet file already exists - please specify a new file name.");
		}
		SpdxDocumentContainer[] result = new SpdxDocumentContainer[1];
		List<String> warnings = new ArrayList<String>();
		try {
			// read the tag-value constants from a file
			Properties constants = CommonCode.getTextFromProperties("org/spdx/tag/SpdxTagValueConstants.properties");
			NoCommentInputStream nci = new NoCommentInputStream(spdxTagFile);
			HandBuiltParser parser = new HandBuiltParser(nci);
			parser.setBehavior(new BuildDocument(result, constants, warnings));
			parser.data();
			if (result[0] == null) {
				throw(new OnlineToolException("Unexpected error parsing SPDX tag document - the result is null."));
			}
			if (!warnings.isEmpty()) {
				System.out.println("The following warnings and or verification errors were found:");
				for (String warning:warnings) {
					System.out.println("\t"+warning);
				}
			}
		} catch (RecognitionException e) {
			// error in tag value file
			throw(new OnlineToolException(e.getMessage()));
		} catch (InvalidFileFormatException e) {
			// invalid spdx file format
			throw(new OnlineToolException(e.getMessage()));
		} catch (Exception e) {
			// If any other exception - assume this is an RDF/XML file.
			throw new OnlineToolException("Error creating SPDX Analysis: " + e);
		}
		SPDXSpreadsheet ss = null;
		try {
			ss = new SPDXSpreadsheet(spdxSpreadsheetFile, true, false);
			RdfToSpreadsheet.copyRdfXmlToSpreadsheet(result[0].getSpdxDocument(), ss);
		} catch (SpreadsheetException e) {
			throw new OnlineToolException("Error opening or writing to spreadsheet: "+ e.getMessage());
		} catch (InvalidSPDXAnalysisException e) {
			throw new OnlineToolException("Error translating the Tag file: "+ e.getMessage());
		} catch (Exception ex) {
			throw new OnlineToolException("Unexpected error translating the tag-value to spreadsheet: "+ ex.getMessage());
		} finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (SpreadsheetException e) {
					throw new OnlineToolException("Error closing spreadsheet: "+ e.getMessage());
				}
			}
		}
		return warnings;
	}
	
	
	private static void usage() {
		System.out
				.println("Usage: TagToSpreadsheet spdxfile.spdx spreadsheetfile.xls \n"
						+ "where spdxfile.spdx is a valid SPDX tag-value file and spreadsheetfile.xls is \n"
						+ "the output SPDX spreadsheeet file.");
	}
}
