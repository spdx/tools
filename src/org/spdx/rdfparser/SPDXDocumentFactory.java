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
package org.spdx.rdfparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.spdx.rdfparser.model.SpdxDocument;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;

/**
 * Factory for creating an SPDX Document from a variety of different sources
 * @author Gary O'Neall
 * 
 *
 */
public class SPDXDocumentFactory {
	
	static final Logger logger = Logger.getLogger(SPDXDocumentFactory.class.getName());

	/**
	 * Create a new SPDX Document populating the data from the existing model
	 * @param model
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	public static SpdxDocument createSpdxDocument(Model model) throws InvalidSPDXAnalysisException {
		
		SpdxDocumentContainer docContainer = new SpdxDocumentContainer(model);
		return docContainer.getSpdxDocument();
	}
	
	/**
	 * Create a new Legacy SPDX Document populating the data from the existing model
	 * Legacy SPDX documents only specification version 1.2 features
	 * @param model
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	@SuppressWarnings("deprecation")
	public static SPDXDocument createLegacySpdxDocument(Model model) throws InvalidSPDXAnalysisException {
		return new SPDXDocument(model);
	}
	
	/**
	 * Create an Legacy SPDX Document from a file - Legacy SPDX documents only specification version 1.2 features
	 * @param fileNameOrUrl local file name or Url containing the SPDX data.  Can be in RDF/XML or RDFa format
	 * @return SPDX Document initialized with the exsiting data
	 * @throws IOException
	 * @throws InvalidSPDXAnalysisException
	 */
	@SuppressWarnings("deprecation")
	public static SPDXDocument createLegacySpdxDocument(String fileNameOrUrl) throws IOException, InvalidSPDXAnalysisException {
		try {
			Class.forName("net.rootdev.javardfa.jena.RDFaReader");
		} catch(java.lang.ClassNotFoundException e) {
			logger.warn("Unable to load the RDFaReader Class");
		}  

		InputStream spdxRdfInput = FileManager.get().open(fileNameOrUrl);
		if (spdxRdfInput == null)
			throw new FileNotFoundException("Unable to open \"" + fileNameOrUrl + "\" for reading");

		return createLegacySpdxDocument(spdxRdfInput, figureBaseUri(fileNameOrUrl), fileType(fileNameOrUrl));
	}
	
	/**
	 * Create an SPDX Document from a file
	 * @param fileNameOrUrl local file name or Url containing the SPDX data.  Can be in RDF/XML or RDFa format
	 * @return SPDX Document initialized with the exsiting data
	 * @throws IOException
	 * @throws InvalidSPDXAnalysisException
	 */
	public static SpdxDocument createSpdxDocument(String fileNameOrUrl) throws IOException, InvalidSPDXAnalysisException {
		try {
			Class.forName("net.rootdev.javardfa.jena.RDFaReader");
		} catch(java.lang.ClassNotFoundException e) {
			logger.warn("Unable to load the RDFaReader Class");
		}  

		InputStream spdxRdfInput = FileManager.get().open(fileNameOrUrl);
		if (spdxRdfInput == null)
			throw new FileNotFoundException("Unable to open \"" + fileNameOrUrl + "\" for reading");

		return createSpdxDocument(spdxRdfInput, figureBaseUri(fileNameOrUrl), fileType(fileNameOrUrl));
	}
	
	public static SpdxDocument createSpdxDocument(InputStream input, String baseUri, String fileType) throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		model.read(input, baseUri, fileType);
		SpdxDocumentContainer docContainer = new SpdxDocumentContainer(model);
		return docContainer.getSpdxDocument();
	}
	
	@SuppressWarnings("deprecation")
	public static SPDXDocument createLegacySpdxDocument(InputStream input, String baseUri, String fileType) throws InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		model.read(input, baseUri, fileType);
		return new SPDXDocument(model);
	}
	
	private static String figureBaseUri(String src) {
		
		URI s = null;
		try{
			s = new URI(src);
		} catch(URISyntaxException e) {
			s = null;
		}
			
		if (s == null || s.getScheme() == null) {
			// assume this is a file path
			String filePath = "///" + new File(src).getAbsoluteFile().toString().replace('\\', '/');
			try {
				s = new URI("file", filePath, null);
			} catch (URISyntaxException e1) {
				logger.error("Invalid URI syntax for "+src);
				return null;
			}
		}
		return s.toString();
	}

    private static String fileType(String path) {
		if (Pattern.matches("(?i:.*\\.x?html?$)", path))
			return "HTML";
		else
			return "RDF/XML";
	}
}
