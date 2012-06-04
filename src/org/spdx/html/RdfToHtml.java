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
package org.spdx.html;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocumentFactory;

import com.sampullara.mustache.Mustache;
import com.sampullara.mustache.MustacheBuilder;
import com.sampullara.mustache.MustacheException;

/**
 * Produces a directory of HTML files which renders an SPDX Document
 * [insert stylesheet comment]
 * The HTML pretty printer is based on a set of HTML templates found in the
 * resources directory:
 *   SpdxHTMLTemplate.html - single HTML file output for an SPDX document
 * The template uses Mustache - see http://mustache.github.com/mustache.5.html
 * @author Gary O'Neall
 *
 */
public class RdfToHtml {
	static final String TEMPLATE_CLASS_PATH = "resources" + "/" + "htmlTemplate";
	static final String TEMPLATE_ROOT_PATH = "resources" + File.separator + "htmlTemplate";
	static final String SPDX_HTML_TEMPLATE = "SpdxHTMLTemplate.html";
	static final int MIN_ARGS = 2;
	static final int MAX_ARGS = 2;
	/**
	 * @param args Argument 0 is the file path of RDF XML file; argument 1 is the file path for the output HTML file(s)
	 */
	public static void main(String[] args) {
		if (args.length < MIN_ARGS) {
			usage();
			return;
		}
		if (args.length > MAX_ARGS) {
			System.out.println("Warning: Extra arguments will be ignored");
			usage();
		}		
		File spdxFile = new File(args[0]);
		if (!spdxFile.exists()) {
			System.out.println("SPDX File "+args[0]+" does not exist.");
			usage();
			return;
		}
		if (!spdxFile.canRead()) {
			System.out.println("Can not read SPDX File "+args[0]+".  Check permissions on the file.");
			usage();
			return;
		}
		File outputHtmlFile = new File(args[1]);
		if (outputHtmlFile.exists()) {
			System.out.println("Can not create output file "+args[1]+", already exists.");
			usage();
			return;
		}
		Writer writer = null;
		try {
			SPDXDocument doc = SPDXDocumentFactory.creatSpdxDocument(args[0]);
			writer = new FileWriter(outputHtmlFile);
			rdfToHtml(doc, writer);
		} catch (IOException e) {
			System.out.println("IO Error opening SPDX Document");
			usage();
			return;
		} catch (InvalidSPDXAnalysisException e) {
			System.out.println("Invalid SPDX Document: "+e.getMessage());
			usage();
			return;
		} catch (MustacheException e) {
			System.out.println("Unexpected error reading the HTML template: "+e.getMessage());
			usage();
			return;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					System.out.println("Warning: error closing HTML file: "+e.getMessage());
				}
				writer = null;
			}
		}
	}
	
	/**
	 * Display usage information to console
	 */
	private static void usage() {
		System.out
		.println("Usage: RdfToHtml rdfxmlfile.rdf htmlfile.html\n"
				+ "where rdfxmlfile.rdf is a valid SPDX RDF XML file and htmlfile.html is\n"
				+ "the output html file.");
	}
	
    public static void rdfToHtml(SPDXDocument doc, Writer writer, String templateDirName, String templateFileName) throws MustacheException, IOException, InvalidSPDXAnalysisException {
        MustacheBuilder builder = new MustacheBuilder(templateDirName);
        HashMap<String, Object> mustacheMap = MustacheMap.buildMustachMap(doc);
        Mustache mustache = builder.parseFile(templateFileName);
        mustache.execute(writer, mustacheMap);
    }


	public static void rdfToHtml(SPDXDocument doc, Writer writer) throws MustacheException, IOException, InvalidSPDXAnalysisException {
		String templateDir = TEMPLATE_ROOT_PATH;
		File templateDirectoryRoot = new File(templateDir);
		if (!(templateDirectoryRoot.exists() && templateDirectoryRoot.isDirectory())) {
			templateDir = TEMPLATE_CLASS_PATH;
		}
		rdfToHtml(doc, writer, templateDir, SPDX_HTML_TEMPLATE);
	}

}
