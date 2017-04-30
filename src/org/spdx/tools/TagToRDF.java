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
		FileInputStream spdxTagStream;
		try {
			spdxTagStream = new FileInputStream(args[0]);
		} catch (FileNotFoundException ex) {
			System.out
					.printf("Tag-Value file %1$s does not exists.%n", args[0]);
			return;
		}

		File spdxRDFFile = new File(args[1]);
		if (spdxRDFFile.exists()) {
			System.out
					.printf("Error: File %1$s already exists - please specify a new file.%n",
							args[1]);
			try {
				spdxTagStream.close();
			} catch (IOException e) {
				System.out.println("Warning: Unable to close input file on error.");
			}
			return;
		}

		try {
			if (!spdxRDFFile.createNewFile()) {
				System.out.println("Could not create the new SPDX RDF file "
						+ args[1]);
				usage();
				try {
					spdxTagStream.close();
				} catch (IOException e) {
					System.out.println("Warning: Unable to close input file on error.");
				}
				return;
			}
		} catch (IOException e1) {
			System.out.println("Could not create the new SPDX Tag-Value file "
					+ args[1]);
			System.out.println("due to error " + e1.getMessage());
			usage();
			try {
				spdxTagStream.close();
			} catch (IOException e) {
				System.out.println("Warning: Unable to close input file on error.");
			}
			return;
		}

		FileOutputStream out;
		try {
			out = new FileOutputStream(spdxRDFFile);
		} catch (FileNotFoundException e1) {
			System.out.println("Could not write to the new SPDX RDF file "
					+ args[1]);
			System.out.println("due to error " + e1.getMessage());
			usage();
			try {
				spdxTagStream.close();
			} catch (IOException e) {
				System.out.println("Warning: Unable to close input file on error.");
			}
			return;
		}

		try {
			List<String> warnings = new ArrayList<String>();
			convertTagFileToRdf(spdxTagStream, out, outputFormat, warnings);
			if (!warnings.isEmpty()) {
				System.out.println("The following warnings and or verification errors were found:");
				for (String warning:warnings) {
					System.out.println("\t"+warning);
				}
			}
		} catch (Exception e) {
			System.err.println("Error creating SPDX Analysis: " + e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					System.out.println("Error closing RDF file: " + e.getMessage());
				}
			}
			if (spdxTagStream != null) {
				try {
					spdxTagStream.close();
				} catch (IOException e) {
					System.out.println("Error closing Tag/Value file: " + e.getMessage());
				}
			}
		}
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
			InputStream spdxTagFile, String outputFormat, List<String> warnings) throws RecognitionException, TokenStreamException, Exception  {
		// read the tag-value constants from a file
		Properties constants = CommonCode.getTextFromProperties("org/spdx/tag/SpdxTagValueConstants.properties");
		NoCommentInputStream nci = new NoCommentInputStream(spdxTagFile);
//		TagValueLexer lexer = new TagValueLexer(new DataInputStream(nci));
//		TagValueParser parser = new TagValueParser(lexer);
		HandBuiltParser parser = new HandBuiltParser(new DataInputStream(nci));
		SpdxDocumentContainer[] result = new SpdxDocumentContainer[1];
		parser.setBehavior(new BuildDocument(result, constants, warnings));
		parser.data();
		if (result[0] == null) {
			throw(new RuntimeException("Unexpected error parsing SPDX tag document - the result is null."));
		}
		return result[0];
	}


	private static void usage() {
		System.out.println("Usage: TagToRDF spdxfile.spdx rdfxmlfile.rdf [outputFormat]\n"
				+ "where spdxfile.spdx is a valid SPDX tag-value file, \n"
				+ "rdfxmlfile.rdf is the output SPDX RDF analysis file and \n"
				+ "[outputFormat] is an optional format for the XML document.\n"
				+ "[outputFormat] must be one of RDF/XML-ABBREV (default), RDF/XML, N-TRIPLET, or TURTLE.");
	}

}
