/**
 * Copyright (c) 2010 Source Auditor Inc.
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.tag.BuildDocument;
import org.spdx.tag.CommonCode;
import org.spdx.tag.HandBuiltParser;
import org.spdx.tag.InvalidFileFormatException;
import org.spdx.tag.InvalidSpdxTagFileException;
import org.spdx.tag.NoCommentInputStream;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.common.collect.Sets;

/**
 * Translates a tag-value file to an RDF XML format Usage: TagToRDF
 * spdxfile.spdx rdfxmlfile.rdf where spdxfile.spdx is a valid SPDX tag-value
 * file and rdfxmlfile.rdf is the output SPDX RDF file.
 *
 * @author Rana Rahal, Protecode Inc.
 */
public class TagToRDF {
	static final int MIN_ARGS = 2;
	static final int MAX_ARGS = 3;

	// output types
	static final String OUTPUT_XML_ABBREV = "RDF/XML-ABBREV";
	static final String OUTPUT_XML = "RDF/XML";
	static final String OUTPUT_N_TRIPLET = "N-TRIPLET";
	static final String OUTPUT_TURTLE = "TURTLE";
	static final String DEFAULT_OUTPUT_FORMAT = OUTPUT_XML_ABBREV;
	static final Set<String> AVAILABLE_OUTPUT_TYPES = Sets.newHashSet();
	static {
		AVAILABLE_OUTPUT_TYPES.add(OUTPUT_N_TRIPLET);
		AVAILABLE_OUTPUT_TYPES.add(OUTPUT_TURTLE);
		AVAILABLE_OUTPUT_TYPES.add(OUTPUT_XML_ABBREV);
		AVAILABLE_OUTPUT_TYPES.add(OUTPUT_XML);
	}

	public static void main(String[] args) {
		if (args.length < MIN_ARGS) {
			usage();
			return;
		}
		if (args.length > MAX_ARGS) {
			usage();
			return;
		}
		String outputFormat = DEFAULT_OUTPUT_FORMAT;
		if (args.length > 2) {
			outputFormat = args[2];
			if (!AVAILABLE_OUTPUT_TYPES.contains(outputFormat)) {
				System.out.println("Invalid output format type.");
				usage();
				return;
			}
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
	 * @param args args[0] is the Tag Value file to be converted, args[1] is the result RDF file name
	 * @throws OnlineToolException Exception caught by JPype and displayed to the user
	 * @return Warnings of the conversion, displayed to the user
	 */
	public static List<String> onlineFunction(String[] args) throws OnlineToolException{
		// Arguments length(args length== 2 ) will checked in the Python Code
		String outputFormat = DEFAULT_OUTPUT_FORMAT;
		FileInputStream spdxTagStream;
		try {
			spdxTagStream = new FileInputStream(args[0]);
		} catch (FileNotFoundException ex) {
			throw new OnlineToolException("Tag-Value file "+ args[0] + " does not exists.");
		}
		File spdxRDFFile = new File(args[1]);
		// Output File name will be checked in the Python code for no clash, but if still found
		if (spdxRDFFile.exists()) {
			try {
				spdxTagStream.close();
			} catch (IOException e) {
				throw new OnlineToolException("Warning: Unable to close input file on error.");
			}
			throw new OnlineToolException("Error: File " + args[1] +" already exists - please specify a new file.");
		}

		try {
			if (!spdxRDFFile.createNewFile()) {
				try {
					spdxTagStream.close();
				} catch (IOException e) {
					throw new OnlineToolException("Warning: Unable to close input file on error.");
				}
				throw new OnlineToolException("Could not create the new SPDX RDF file "+ args[1]);
			}
		} catch (IOException e1) {
			try {
				spdxTagStream.close();
			} catch (IOException e) {
				throw new OnlineToolException("Warning: Unable to close input file on error.");
			}
			throw new OnlineToolException("Could not create the new SPDX Tag-Value file "+ args[1] + "due to error " + e1.getMessage());
		}

		FileOutputStream out;
		try {
			out = new FileOutputStream(spdxRDFFile);
		} catch (FileNotFoundException e1) {
			try {
				spdxTagStream.close();
			} catch (IOException e) {
				throw new OnlineToolException("Warning: Unable to close input file on error.");
			}
			throw new OnlineToolException("Could not write to the new SPDX RDF file "+ args[1]+ "due to error " + e1.getMessage());
		}
		List<String> warnings = new ArrayList<String>();
		try {
			convertTagFileToRdf(spdxTagStream, out, outputFormat, warnings);
			if (!warnings.isEmpty()) {
				System.out.println("The following warnings and or verification errors were found:");
				for (String warning:warnings) {
					System.out.println("\t"+warning);
				}
			}
		} catch (Exception e) {
			throw new OnlineToolException("Error creating SPDX Analysis: " + e.getMessage());
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					throw new OnlineToolException("Error closing RDF file: " + e.getMessage());
				}
			}
			if (spdxTagStream != null) {
				try {
					spdxTagStream.close();
				} catch (IOException e) {
					throw new OnlineToolException("Error closing Tag/Value file: " + e.getMessage());
				}
			}
		}
		return warnings;
	}
	/**
	 * Convert a Tag File to an RDF output stream
	 * @param spdxTagFile File containing a tag/value formatted SPDX file
	 * @param out Stream where the RDF/XML data is written
	 * @param outputFormat must be one of RDF/XML-ABBREV (default), RDF/XML, N-TRIPLET, or TURTLE
	 * @throws Exception
	 * @throws TokenStreamException
	 * @throws RecognitionException
	 */
	public static void convertTagFileToRdf(InputStream spdxTagFile,
			OutputStream out, String outputFormat, List<String> warnings) throws RecognitionException, TokenStreamException, Exception {

			convertTagFileToRdf(spdxTagFile, outputFormat, warnings).getModel().write(out, outputFormat);
	}

	/**
	 * Convert an tag/value format input stream into an SPDX Document
	 * @param spdxTagFile Input stream containing a SPDX tag/value format text
	 * @param outputFormat must be one of RDF/XML-ABBREV (default), RDF/XML, N-TRIPLET, or TURTLE
	 * @param warnings List of any warnings generated during the tag/value parsing
	 * @return SpdxDocumentContainer containing the SPDX document represented by the spdxTagVile
	 * @throws Exception
	 */
	public static SpdxDocumentContainer convertTagFileToRdf(
			InputStream spdxTagFile, String outputFormat, List<String> warnings) throws RecognitionException, TokenStreamException,InvalidSpdxTagFileException,InvalidFileFormatException, Exception  {
		// read the tag-value constants from a file
		Properties constants = CommonCode.getTextFromProperties("org/spdx/tag/SpdxTagValueConstants.properties");
		NoCommentInputStream nci = new NoCommentInputStream(spdxTagFile);
//		TagValueLexer lexer = new TagValueLexer(new DataInputStream(nci));
//		TagValueParser parser = new TagValueParser(lexer);
		try{
			HandBuiltParser parser = new HandBuiltParser(nci);
			SpdxDocumentContainer[] result = new SpdxDocumentContainer[1];
			parser.setBehavior(new BuildDocument(result, constants, warnings));
			parser.data();
			if (result[0] == null) {
				throw(new RuntimeException("Unexpected error parsing SPDX tag document - the result is null."));
			}
			return result[0];
		} catch (RecognitionException e) {
			// error in tag value file
			throw(new InvalidSpdxTagFileException(e.getMessage()));
		} catch (InvalidFileFormatException e) {
			// invalid spdx file format
			throw(new InvalidFileFormatException(e.getMessage()));
		} catch (Exception e){
			// If any other exception - assume this is an RDF/XML file.
			throw(new Exception(e.getMessage()));
		}
	}


	private static void usage() {
		System.out.println("Usage: TagToRDF spdxfile.spdx rdfxmlfile.rdf [outputFormat]\n"
				+ "where spdxfile.spdx is a valid SPDX tag-value file, \n"
				+ "rdfxmlfile.rdf is the output SPDX RDF analysis file and \n"
				+ "[outputFormat] is an optional format for the XML document.\n"
				+ "[outputFormat] must be one of RDF/XML-ABBREV (default), RDF/XML, N-TRIPLET, or TURTLE.");
	}

}
