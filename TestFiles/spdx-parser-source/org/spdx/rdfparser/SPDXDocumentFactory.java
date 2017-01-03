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

	/**
	 * Create a new SPDX Document populating the date from the existing model
	 * @param model
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	static SpdxRdfConstants createSpdxDocument(Model model) throws InvalidSPDXAnalysisException {
		return new SPDXDocument(model);
	}

	/**
	 * Create an SPDX Document from a file
	 * @param fileNameOrUrl local file name or Url containing the SPDX data.  Can be in RDF/XML or RDFa format
	 * @return SPDX Document initialized with the exsiting data
	 * @throws IOException
	 * @throws InvalidSPDXAnalysisException
	 */
	public static SPDXDocument creatSpdxDocument(String fileNameOrUrl) throws IOException, InvalidSPDXAnalysisException {
		try {
			Class.forName("net.rootdev.javardfa.jena.RDFaReader");
		} catch(java.lang.ClassNotFoundException e) {}  // do nothing

		Model model = ModelFactory.createDefaultModel();

		InputStream spdxRdfInput = FileManager.get().open(fileNameOrUrl);
		if (spdxRdfInput == null)
			throw new FileNotFoundException("Unable to open \"" + fileNameOrUrl + "\" for reading");

		model.read(spdxRdfInput, figureBaseUri(fileNameOrUrl), fileType(fileNameOrUrl));

		return new SPDXDocument(model);
	}

	private static String figureBaseUri(String src) {
		try{
			URI s = new URI(src);

			if (null == s.getScheme())
				return "file://" + new File(src).getAbsoluteFile().toString().replace("\\", "/");
			else
				return s.toString();

		} catch(URISyntaxException e){
			return null;
		}
	}

    private static String fileType(String path) {
		if (Pattern.matches("(?i:.*\\.x?html?$)", path))
			return "HTML";
		else
			return "RDF/XML";
	}
}
