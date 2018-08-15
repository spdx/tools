/**
 * Copyright (c) 2018 Source Auditor Inc.
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
 */
package org.spdx.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.model.SpdxDocument;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Converts SPDX RDF Format to XML
 * Usage: RdfToXml rdfxmlfile.rdf spdxfile.xml where rdfxmlfile.rdf is a valid SPDX RDF XML
 * file and spdxfile.xml is the output SPDX XML file.
 * @author Gary O'Neall
 *
 */
public class RdfToXml {

	static final int MIN_ARGS = 2;
	static final int MAX_ARGS = 2;
	static final Logger logger = LoggerFactory.getLogger(RdfToXml.class);
	/**
	 * @param args args[0] is the file path to a valid RDF XML SPDX file, args[1] is the file path to the output XML format SPDX file
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
		try {
			List<String> warnings = onlineFunction(args);
			if (warnings != null && warnings.size() > 0) {
				System.out.println("The following warnings were generated during the conversion: ");
				for (String warning:warnings) {
					System.out.println("\t"+warning);
				}
			}
		} catch (OnlineToolException e){
			System.out.println(e.getMessage());
			usage();
			return;
		}
	}
	
	/**
	 * 
	 * @param args args[0] is the file path to a valid RDF XML SPDX file, args[1] is the file path to the output XML format SPDX file
	 * @throws OnlineToolException Exception caught by JPype and displayed to the user
	 * @return Warnings of the conversion, displayed to the user
	 */
	public static List<String> onlineFunction(String[] args) throws OnlineToolException{
		File spdxRdfFile = new File(args[0]);
		if (!spdxRdfFile.exists()) {
			throw new OnlineToolException("RDF file " + args[0] +" does not exists.");
		}
		File spdxXmlFile = new File(args[1]);
		if (spdxXmlFile.exists()) {
			throw new OnlineToolException("Error: File " +args[1] +" already exists - please specify a new file.");
		}
		try {
			if (!spdxXmlFile.createNewFile()) {
				throw new OnlineToolException("Could not create the new SPDX XML file "
						+ args[1]);
			}
		} catch (IOException e1) {
			throw new OnlineToolException("Could not create the new SPDX XML file " + args[1] + "due to error " + e1.getMessage());
		}
		FileOutputStream xmlout = null;
		try {
			xmlout = new FileOutputStream(spdxXmlFile);
			SpdxDocument doc = null;
			try {
				doc = SPDXDocumentFactory.createSpdxDocument(args[0]);
			} catch (InvalidSPDXAnalysisException ex) {
				throw new OnlineToolException("Error creating SPDX Document: "+ex.getMessage());
			} catch (IOException e) {
				throw new OnlineToolException("Unable to open file :"+args[0]+", "+e.getMessage());
			} catch (Exception e) {
				throw new OnlineToolException("Error creating SPDX Document: "+e.getMessage(),e);
			}
			XmlMapper mapper = new XmlMapper();
			mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
			mapper.setDefaultUseWrapper(false);
			//ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
			//writer.writeValue(xmlout, doc);
			mapper.writeValue(xmlout, doc);
			return doc.verify();
		} catch (FileNotFoundException e1) {
			logger.error("File not found exception creating output stream for XML output file",e1);
			throw new OnlineToolException("Error opening the XML output file.  File not found.");
		} catch (IOException e) {
			logger.error("IO Error writing to XML output file",e);
			throw new OnlineToolException("I/O error writing to the XML output file.: "+e.getMessage());
		} finally {
			if (xmlout != null) {
				try {
					xmlout.close();
				} catch (IOException e) {
					logger.error("IO Error closing XML output file",e);
				}
			}
		}
	}
	
	private static void usage() {
		System.out
				.println("Usage: RdfToXml rdfxmlfile.rdf spdxfile.xml\n"
						+ "where rdfxmlfile.rdf is a valid SPDX RDF XML file and spdxfile.xml is\n"
						+ "the output SPDX XML format file.");
	}

}
