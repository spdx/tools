/**
 * Copyright (c) 2011 Source Auditor Inc.
* Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.spdx.rdfparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * This class contains a formatted HTML file for a given license.  Specific
 * formatting information is contained in this file.
 * @author Gary O'Neall
 *
 */
public class LicenseHTMLFile {
	static final String HTML_BEFORE_TITLE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+
			"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML+RDFa 1.0//EN\" \"http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd\">\n"+
			"<html xmlns=\"http://www.w3.org/1999/xhtml\"\n"+
			"	xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"+
			"	xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"+
			"	xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"+
			"	xmlns:dc=\"http://purl.org/dc/terms/\"\n"+
			"	xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n"+
			"	xmlns:spdx=\"http://spdx.org/spec#\">\n"+
			"<head>\n"+
			"	<title></title>\n"+
			"	<link rel=\"stylesheet\" href=\"screen.css\" media=\"screen\" type=\"text/css\" />\n"+
			"</head>\n"+
			"<body typeof=\"spdx:License\">\n"+
			"	<h1 property=\"dc:title\">";
	static final String AFTER_TITLE = "</h1>\n\n<p>Back to the <a href=\"";
	static final String AFTER_TOC_REFERENCE = "\">License List</a></p>\n\n<h2 id=\"information\">Information</h2>\n<dl>\n"+
			"			<dt>Full name</dt>\n"+
			"			<dd property=\"rdfs:label\">";
	static final String AFTER_LICENSE_NAME = "</dd>\n\n<dt>Short identifier</dt>\n<dd><code>";
	static final String AFTER_SHORT_ID = "</dd>\n";
	static final String WEBURL = "[WEBURL]";
	static final String SITE = "[SITE]";
	static final String OTHER_WEB_PAGE_FIRST = "<dt>Other web pages for this license</dt>\n"+
			"		<dd>\n"+
			"			<ul>\n";
	static final String OTHER_WEB_PAGE_ROW = 
			"				<li><a href=\""+WEBURL+"\" rel=\"owl:sameAs\">"+SITE+"</a></li>\n";
	static final String OTHER_WEB_PAGE_END = "</ul>\n</dd>\n</dl>\n";
	static final String BEFORE_NOTES = "</dl>\n";
	static final String NOTES = "[NOTES]";
	static final String NOTES_HTML = "<h2 id=\"notes\">Notes</h2>\n\n<p>"+NOTES+"</p>";
	static final String LICENSE_TEXT = "[LICENSE_TEXT]";
	static final String TEXT_HTML = "<h2 id=\"licenseText\">Text</h2>\n\n<div property=\"spdx:LicenseText\" class=\"license-text\">\n"+
							LICENSE_TEXT+"\n</div>\n";
	static final String END_HTML = "</body>\n</html>";

	static final Pattern SITE_PATTERN = Pattern.compile("http://(.*)\\.(.*)(\\.org|\\.com|\\.net|\\.info)");
	
	private SPDXLicense license;
	public LicenseHTMLFile(SPDXLicense license) {
		this.license = license;
	}
	public LicenseHTMLFile() {
		this.license = null;
	}
	
	/**
	 * @return the license
	 */
	public SPDXLicense getLicense() {
		return license;
	}

	/**
	 * @param license the license to set
	 */
	public void setLicense(SPDXLicense license) {
		this.license = license;
	}

	public void writeToFile(File htmlFile, String tableOfContentsReference) throws IOException {
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
			writer.write(HTML_BEFORE_TITLE);
			writer.write(escapeHTML(license.getName()));
			writer.write(AFTER_TITLE);
			writer.write(tableOfContentsReference);
			writer.write(AFTER_TOC_REFERENCE);
			writer.write(escapeHTML(license.getName()));
			writer.write(AFTER_LICENSE_NAME);
			writer.write(escapeHTML(license.getId()));
			writer.write(AFTER_SHORT_ID);
			if (license.getSourceUrl() != null) {
				writer.write(OTHER_WEB_PAGE_FIRST);
				String[] sourceUrls = license.getSourceUrl().split("\n");
				for (int i = 0; i < sourceUrls.length; i++) {
					String url = sourceUrls[i].trim();
					String site = getSiteFromUrl(url);
					String urlRow = OTHER_WEB_PAGE_ROW.replace(WEBURL, url);
					urlRow = urlRow.replace(SITE, site);
					writer.write(urlRow);
				}
				writer.write(OTHER_WEB_PAGE_END);
			}
			writer.write(BEFORE_NOTES);
			if (license.getNotes()!= null && !license.getNotes().isEmpty()) {
				writer.write(NOTES_HTML.replace(NOTES, escapeHTML(license.getNotes())));
			}
			writer.write(TEXT_HTML.replace(LICENSE_TEXT, escapeHTML(license.getText())));
			writer.write(END_HTML);
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
	private String getSiteFromUrl(String url) {
		Matcher matcher = SITE_PATTERN.matcher(url);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return url;
		}
	}
	private String escapeHTML(String text) {
		String retval = StringEscapeUtils.escapeHtml(text);
		return retval.replace("\n", "<br/>\n");
	}
}
