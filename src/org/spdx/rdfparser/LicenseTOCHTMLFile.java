/**
 * Copyright (c) 2011 Source Auditor Inc.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spdx.rdfparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * This class holds a formatted HTML file for a license table of contents
 * @author Source Auditor
 *
 */
public class LicenseTOCHTMLFile {
	
	static final String HTML_FILE_TEMPLATE_FIRST = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+
			"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML+RDFa 1.0//EN\" \"http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd\">\n"+
			"<html xmlns=\"http://www.w3.org/1999/xhtml\"\n"+
			"      xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"+
			"      xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"+
			"      xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"+
			"      xmlns:dc=\"http://purl.org/dc/terms/\"\n"+
			"      xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n"+
			"      xmlns:spdx=\"http://spdx.org/spec#\">\n"+
			"<head>\n"+
			"  <title></title>\n"+
			"  <link rel=\"stylesheet\" href=\"screen.css\" media=\"screen\" type=\"text/css\" />\n"+
			"</head>\n"+
			"\n"+ 
			"<body typeof=\"rdf:Bag\">\n"+
			"<h1>SPDX Open Source License Registry</h1>\n"+
			"\n"+ 
			"<p>This following is a list of all liceneses currently registered with SPDX.</p>\n"+
			"\n"+ 
			"<h2>Licenses</h2>\n"+
			"<table>\n"+
			"  <thead><tr>\n"+
			"    <th>Full name</th><th>Identifier</th><th>Text</th>\n"+
			"  </tr></thead>\n"+
			"\n"+ 
			"  <tbody>\n";
	static final String REFERENCE = "[REFERENCE]";
	static final String LICENSEID = "[LICENSEID]";
	static final String LICENSE_NAME = "[LICENSE_NAME]";
      static final String ROW_TEMPLATE = "    <tr>\n      <td><a href=\""+REFERENCE+"\" rel=\"rdf:_1\">"+LICENSE_NAME+"</a></td>\n"+
      		"      <td><code>"+LICENSEID+"</code></td>\n"+
      		"      <td><a href=\""+REFERENCE+"#licenseText\">License Text</a></td>\n"+
      		"    </tr>\n";
      static final String HTML_FILE_TEMPLATE_LAST = "    </tbody>\n</table>\n</body>\n</html>";

      ArrayList<String> tableRows = new ArrayList<String>();
      
	public void addLicense(SPDXLicense license, String licHTMLReference) {
		String newRow = ROW_TEMPLATE.replace(REFERENCE, licHTMLReference);
		newRow = newRow.replace(LICENSE_NAME, escapeHTML(license.getName()));
		newRow = newRow.replace(LICENSEID, escapeHTML(license.getId()));
		tableRows.add(newRow);
	}
	
	private String escapeHTML(String name) {
		// TODO Auto-generated method stub
		return name;
	}

	public void writeToFile(File htmlFile) throws IOException {
		FileOutputStream stream = null;
		OutputStreamWriter writer = null;
		if (!htmlFile.exists()) {
			if (!htmlFile.createNewFile()) {
				throw(new IOException("Can not create new file "+htmlFile.getName()));
			}
		}
		try {
			stream = new FileOutputStream(htmlFile);
			writer = new OutputStreamWriter(stream);
			writer.write(HTML_FILE_TEMPLATE_FIRST);
			for (int i = 0; i < this.tableRows.size(); i++) {
				writer.write(this.tableRows.get(i));
			}
			writer.write(HTML_FILE_TEMPLATE_LAST);
		} catch (FileNotFoundException e) {
			throw(e);
		} finally {
			if (writer != null) {
				writer.close();
			}
			if (stream != null) {
				stream.close();
			}
		}
	}
	

}
