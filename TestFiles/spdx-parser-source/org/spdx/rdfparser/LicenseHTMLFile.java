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
	static final boolean USE_SITE = false;	// set to true to use the site name for the link of external web pages
	static final String TITLE = "[TITLE]";
	static final String NAME = "[NAME]";
	static final String ID = "[LICENSEID]";
	static final String WEBURL = "[WEBURL]";
	static final String SITE = "[SITE]";
	static final String NOTES = "[NOTES]";
	static final String OTHERWEBPAGE = "[OTHERWEBPAGE]";
	static final String OTHER_WEB_PAGE_ROW =
		"				<li><a href=\""+WEBURL+"\" rel=\"owl:sameAs\">"+SITE+"</a></li>\n";
	static final String LICENSE_TEXT = "[LICENSE_TEXT]";
	static final Pattern SITE_PATTERN = Pattern.compile("http://(.*)\\.(.*)(\\.org|\\.com|\\.net|\\.info)");

	private String template;

	private SPDXStandardLicense license;
	public LicenseHTMLFile(String template, SPDXStandardLicense license) {
		this.template = template;
		this.license = license;
	}
	public LicenseHTMLFile(String template) {
		this.template = template;
		this.license = null;
	}

	/**
	 * @return the license
	 */
	public SPDXStandardLicense getLicense() {
		return license;
	}

	/**
	 * @param license the license to set
	 */
	public void setLicense(SPDXStandardLicense license) {
		this.license = license;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getTemplate() {
		return this.template;
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
			String htmlText = template.replace(ID, escapeHTML(license.getId()));
			htmlText = htmlText.replace(LICENSE_TEXT, escapeHTML(license.getText()));
			htmlText = htmlText.replace(NAME, escapeHTML(license.getName()));
			String notes;
			if (license.getNotes() != null && !license.getNotes().isEmpty()) {
				notes = escapeHTML(license.getNotes());
			} else {
				notes = "None";
			}
			htmlText = htmlText.replace(NOTES, notes);
			String otherWebPages;
			if (license.getSourceUrl() != null && !license.getSourceUrl().isEmpty()) {
				StringBuilder sb = new StringBuilder();
				String[] sourceUrls = license.getSourceUrl().split(" ");
				for (int i = 0; i < sourceUrls.length; i++) {
					String url = sourceUrls[i].trim();
					if (url.isEmpty()) {
						continue;
					}
					String site = getSiteFromUrl(url);
					String urlRow = OTHER_WEB_PAGE_ROW.replace(WEBURL, url);
					urlRow = urlRow.replace(SITE, site);
					sb.append(urlRow);
					sb.append("\n");
				}
				otherWebPages = sb.toString();
			} else {
				otherWebPages = "None";
			}
			htmlText = htmlText.replace(OTHERWEBPAGE, otherWebPages);
			htmlText = htmlText.replace(TITLE, escapeHTML(license.getName()));
			stream = new FileOutputStream(htmlFile);
			writer = new OutputStreamWriter(stream, "UTF-8");
			writer.write(htmlText);
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
	@SuppressWarnings("unused")
	private String getSiteFromUrl(String url) {
		Matcher matcher = SITE_PATTERN.matcher(url);
		if (matcher.find() && USE_SITE) {
			int numGroups = matcher.groupCount();
			return matcher.group(numGroups-1);
		} else {
			return url;
		}
	}
	private String escapeHTML(String text) {
		String retval = StringEscapeUtils.escapeXml(text);
		return retval.replace("\n", "<br/>\n");
	}
}
