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
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * This class holds a formatted HTML file for a license table of contents
 * @author Source Auditor
 *
 */
public class LicenseTOCHTMLFile {

	static final String TABLE_ROW = "[TABLEROW]";
	static final String REFERENCE = "[REFERENCE]";
	static final String REFNUMBER = "[REFERENCE_NUMBER]";
	static final String LICENSEID = "[LICENSEID]";
	static final String LICENSE_NAME = "[LICENSE_NAME]";
	static final String OSI_APPROVED = "[OSI_APPROVED]";
      static final String ROW_TEMPLATE = "    <tr>\n      <td><a href=\""+REFERENCE+
		"\" rel=\"rdf:_"+REFNUMBER+"\">"+LICENSE_NAME+"</a></td>\n"+
		"      <td about=\""+REFERENCE+"\" typeof=\"spdx:License\">\n"+
		"      <code property=\"spdx:licenseId\">"+LICENSEID+"</code></td>\n"+
		"      <td align=\"center\">"+OSI_APPROVED+"</td>\n"+
		"      <td><a href=\""+REFERENCE+"#licenseText\">License Text</a></td>\n"+
		"    </tr>\n";
      ArrayList<String> tableRows = new ArrayList<String>();

      private int currentRefNumber = 1;

      String template;

      public LicenseTOCHTMLFile(String template) {
	  this.template = template;
      }

	public void addLicense(SPDXStandardLicense license, String licHTMLReference) {
		String newRow = ROW_TEMPLATE.replace(REFERENCE, licHTMLReference);
		currentRefNumber++;
		newRow = newRow.replace(REFNUMBER, String.valueOf(this.currentRefNumber));
		newRow = newRow.replace(LICENSE_NAME, escapeHTML(license.getName()));
		newRow = newRow.replace(LICENSEID, escapeHTML(license.getId()));
		if (license.isOsiApproved()) {
			newRow = newRow.replace(OSI_APPROVED, "Y");
		} else {
			newRow = newRow.replace(OSI_APPROVED, "");
		}
		tableRows.add(newRow);
	}

	private String escapeHTML(String s) {
		return StringEscapeUtils.escapeHtml(s);
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
			writer = new OutputStreamWriter(stream, "UTF-8");
			int rowLocation = template.indexOf(TABLE_ROW);
			String firstPart = template.substring(0, rowLocation);
			String lastPart = template.substring(rowLocation + TABLE_ROW.length());
			writer.write(firstPart);
			for (int i = 0; i < this.tableRows.size(); i++) {
				writer.write(this.tableRows.get(i));
			}
			writer.write(lastPart);
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
