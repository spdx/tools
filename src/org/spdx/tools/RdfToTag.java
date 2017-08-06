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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.tag.CommonCode;

/**
 * Translates an RDF XML file to a tag-value format Usage: RdfToTag
 * rdfxmlfile.rdf spdxfile.spdx where rdfxmlfile.rdf is a valid SPDX RDF XML
 * file and spdxfile.spdx is the output SPDX tag-value file.
 * 
 * @author Rana Rahal, Protecode Inc.
 */
public class RdfToTag {
	static final int MIN_ARGS = 2;
	static final int MAX_ARGS = 2;
	static final Logger logger = Logger.getLogger(RdfToTag.class.getName());

	/**
	 * @param args
	 *            Argument 0 is a the file path name of the SPDX RDF/XML file
	 */
	public static void main(String[] args) {
		if (args.length < MIN_ARGS) {
			usage();
			return;
		}
		if (args.length > MAX_ARGS) {
			System.out.printf("Warning: Extra arguments will be ignored%n");
			usage();
		}
		File spdxRdfFile = new File(args[0]);
		if (!spdxRdfFile.exists()) {
			System.out.printf("RDF file %1$s does not exists.%n", args[0]);
			return;
		}
		File spdxTagFile = new File(args[1]);
		if (spdxTagFile.exists()) {
			System.out
					.printf("Error: File %1$s already exists - please specify a new file.%n",
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
			SpdxDocument doc = null;
			try {
				doc = SPDXDocumentFactory.createSpdxDocument(args[0]);
			} catch (Exception ex) {
				System.out.print("Error creating SPDX Document: "
						+ ex.getMessage());
				return;
			}
			try {
				List<String> verify = new LinkedList<String>(); // doc.verify();
				if (verify.size() > 0) {
					System.out
							.println("This SPDX Document is not valid due to:");
					for (int i = 0; i < verify.size(); i++) {
						System.out.println("\t" + verify.get(i));
					}
				}
				// read the tag-value constants from a file
				Properties constants = CommonCode
						.getTextFromProperties("org/spdx/tag/SpdxTagValueConstants.properties");
				// print document to a file using tag-value format
				CommonCode.printDoc(doc, out, constants);
			} catch (InvalidSPDXAnalysisException e) {
				System.out
						.print("Error transalting SPDX Document to tag-value format: "
								+ e.getMessage());
				return;
			} catch (Exception e) {
				System.out.print("Unexpected error displaying SPDX Document: "
						+ e.getMessage());
			}
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}

	/**
	 * 
	 * @param args args[0] is the RDF file to be converted, args[1] is the result Tag file name
	 * @throws OnlineToolException Exception caught by JPype and displayed to the user
	 * @return Warnings of the conversion, displayed to the user
	 */
	public static List<String> onlineFunction(String[] args) throws OnlineToolException{
		// Arguments length(args length== 2 ) will checked in the Python Code
		File spdxRdfFile = new File(args[0]);
		// Output File name will be checked in the Python code for no clash, but if still found
		if (!spdxRdfFile.exists()) {
			System.out.printf("RDF file %1$s does not exists.%n", args[0]);
			throw new OnlineToolException("RDF file " + args[0] +" does not exists.");
		}
		File spdxTagFile = new File(args[1]);
		if (spdxTagFile.exists()) {
			System.out.printf("Error: File %1$s already exists - please specify a new file.%n",
							args[1]);
			throw new OnlineToolException("Error: File " +args[1] +" already exists - please specify a new file.");
		}
		try {
			if (!spdxTagFile.createNewFile()) {
				System.out.println("Could not create the new SPDX Tag file "
						+ args[1]);
				throw new OnlineToolException("Could not create the new SPDX Tag file "
						+ args[1]);
			}
		} catch (IOException e1) {
			System.out.println("Could not create the new SPDX Tag file "
					+ args[1]);
			System.out.println("due to error " + e1.getMessage());
			throw new OnlineToolException("Could not create the new SPDX Tag file " + args[1] + "due to error " + e1.getMessage());
		}
		PrintWriter out = null;
		List<String> verify = new LinkedList<String>();
		try {
			try {
				out = new PrintWriter(spdxTagFile, "UTF-8");
			} catch (IOException e1) {
				System.out.println("Could not write to the new SPDX Tag file "
						+ args[1]);
				System.out.println("due to error " + e1.getMessage());
				throw new OnlineToolException("Could not write to the new SPDX Tag file "
						+ args[1] +  "due to error " + e1.getMessage());
				
			}
			SpdxDocument doc = null;
			try {
				doc = SPDXDocumentFactory.createSpdxDocument(args[0]);
			} catch (InvalidSPDXAnalysisException ex) {
				System.out.print("Error creating SPDX Document: "+ex.getMessage());
				throw new OnlineToolException("Error creating SPDX Document: "+ex.getMessage());
			} catch (IOException e) {
				System.out.print("Error creating SPDX Document:"+args[0]+", "+e.getMessage());
				throw new OnlineToolException("Unable to open file :"+args[0]+", "+e.getMessage());
			} catch (Exception e) {
				System.out.println("Error creating SPDX Document: "+e.getMessage());
				throw new OnlineToolException("Error creating SPDX Document: "+e.getMessage(),e);
			}

			try {
				verify = doc.verify();
				if (verify.size() > 0) {
					System.out
							.println("This SPDX Document is not valid due to:");
					for (int i = 0; i < verify.size(); i++) {
						System.out.println("\t" + verify.get(i));
					}
				}
				// read the tag-value constants from a file
				Properties constants = CommonCode
						.getTextFromProperties("org/spdx/tag/SpdxTagValueConstants.properties");
				// print document to a file using tag-value format
				CommonCode.printDoc(doc, out, constants);
			} catch (InvalidSPDXAnalysisException e) {
				System.out
						.print("Error transalting SPDX Document to tag-value format: "
								+ e.getMessage());
				throw new OnlineToolException("Error transalting SPDX Document to tag-value format: "
						+ e.getMessage());
			} catch (Exception e) {
				System.out.print("Unexpected error displaying SPDX Document: "
						+ e.getMessage());
				throw new OnlineToolException("Unexpected error displaying SPDX Document: "
						+ e.getMessage());
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
				.println("Usage: RdfToTag rdfxmlfile.rdf spdxfile.spdx\n"
						+ "where rdfxmlfile.rdf is a valid SPDX RDF XML file and spdxfile.spdx is\n"
						+ "the output SPDX tag-value file.");
	}

}
