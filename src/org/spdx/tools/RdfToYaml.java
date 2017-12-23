/**
 * Copyright (c) 2017 Source Auditor Inc.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Converts SPDX RDF Format to YAML
 * Usage: RdfToYaml rdfxmlfile.rdf spdxfile.yaml where rdfxmlfile.rdf is a valid SPDX RDF XML
 * file and spdxfile.yaml is the output SPDX YAML file.
 * @author Gary O'Neall
 *
 */
public class RdfToYaml {
	
	static final int MIN_ARGS = 2;
	static final int MAX_ARGS = 2;
	static final Logger logger = LoggerFactory.getLogger(RdfToYaml.class);
	/**
	 * @param args args[0] is the file path to a valid RDF XML SPDX file, args[1] is the file path to the output YAML format SPDX file
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
	 * @param args args[0] is the file path to a valid RDF XML SPDX file, args[1] is the file path to the output YAML format SPDX file
	 * @throws OnlineToolException Exception caught by JPype and displayed to the user
	 * @return Warnings of the conversion, displayed to the user
	 */
	public static List<String> onlineFunction(String[] args) throws OnlineToolException{
		File spdxRdfFile = new File(args[0]);
		if (!spdxRdfFile.exists()) {
			throw new OnlineToolException("RDF file " + args[0] +" does not exists.");
		}
		File spdxYamlFile = new File(args[1]);
		if (spdxYamlFile.exists()) {
			throw new OnlineToolException("Error: File " +args[1] +" already exists - please specify a new file.");
		}
		try {
			if (!spdxYamlFile.createNewFile()) {
				throw new OnlineToolException("Could not create the new SPDX YAML file "
						+ args[1]);
			}
		} catch (IOException e1) {
			throw new OnlineToolException("Could not create the new SPDX YAML file " + args[1] + "due to error " + e1.getMessage());
		}
		FileOutputStream yamlout = null;
		SequenceWriter writer = null;
		try {
			yamlout = new FileOutputStream(spdxYamlFile);
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
			YAMLFactory yamlFactory = new YAMLFactory();
			ObjectMapper mapper = new ObjectMapper(yamlFactory);
			writer = mapper.writerWithDefaultPrettyPrinter().writeValues(yamlout);
			writer.write(doc);
			return doc.verify();
		} catch (FileNotFoundException e1) {
			logger.error("File not found exception creating output stream for YAML output file",e1);
			throw new OnlineToolException("Error opening the YAML output file.  File not found.");
		} catch (IOException e) {
			logger.error("IO Error writing to YAML output file",e);
			throw new OnlineToolException("I/O error writing to the YAML output file.: "+e.getMessage());
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					logger.error("IO Error closing YAML output file",e);
				}
			} else if (yamlout != null) {
				try {
					yamlout.close();
				} catch (IOException e) {
					logger.error("IO Error closing YAML output file",e);
				}
			}
		}
	}
	
	private static void usage() {
		System.out
				.println("Usage: RdfToYaml rdfxmlfile.rdf spdxfile.yaml\n"
						+ "where rdfxmlfile.rdf is a valid SPDX RDF XML file and spdxfile.yaml is\n"
						+ "the output SPDX YAML format file.");
	}

}
