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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.spdx.rdfparser.license.LicenseException;
import org.spdx.rdfparser.license.SpdxListedLicense;

/**
 * Holds license information and generates a file in markdown format which links to the HTML version of the license files
 * @author Gary O'Neall
 *
 */
public class MarkdownTable {

	class ExceptionInfo {
		public boolean isDeprecated() {
			return deprecated;
		}

		public void setDeprecated(boolean deprecated) {
			this.deprecated = deprecated;
		}
		private String id;
		private String name;
		private boolean deprecated;
		
		private ExceptionInfo(String id, String name, boolean deprecated) {
			this.id = id;
			this.name = name;
			this.deprecated = deprecated;
		}
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}
	
	class LicenseInfo extends ExceptionInfo {
		private boolean isOsiApproved;
		
		public LicenseInfo(String id, String name, boolean deprecated, boolean isOsiApproved) {
			super(id, name, deprecated);
			this.isOsiApproved = isOsiApproved;
		}

		public boolean isOsiApproved() {
			return isOsiApproved;
		}

		public void setOsiApproved(boolean isOsiApproved) {
			this.isOsiApproved = isOsiApproved;
		}		
	}
	
	List<ExceptionInfo> exceptions = new ArrayList<ExceptionInfo>();
	List<LicenseInfo> licenses = new ArrayList<LicenseInfo>();

	private String licenseListVersion;
	
	public MarkdownTable(String licenseListVersion) {
		if (licenseListVersion == null) {
			this.licenseListVersion = "UNKNOWN";
		} else {
			this.licenseListVersion = licenseListVersion;
		}
	}
	/**
	 * Add an exception to be added to the markdown table of contents
	 * @param exception
	 * @param deprecated
	 */
	public void addException(LicenseException exception, boolean deprecated) {
		exceptions.add(new ExceptionInfo(exception.getLicenseExceptionId(), exception.getName(), deprecated));
	}
	
	/**
	 * Add a license to be included in the markdown table of contents
	 * @param license
	 * @param deprecated
	 */
	public void addLicense(SpdxListedLicense license, boolean deprecated) {
		licenses.add(new LicenseInfo(license.getLicenseId(), license.getName(), deprecated, license.isOsiApproved()));
	}
	
	/**
	 * Write the markdown table of contents to an existing file.  Overwrites all content
	 * @param file
	 * @throws IOException
	 */
	public void writeToFile(File file) throws IOException {
		FileWriter writer = null;
		
		try {
			writer = new FileWriter(file);
			writeTOC(writer);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
	/**
	 * Write the text for all of the added licenses and exceptions in the form of a MarkDown formatted table
	 * @param writer
	 * @throws IOException
	 */
	public void writeTOC(Writer writer) throws IOException {
		exceptions.sort(new Comparator<ExceptionInfo>() {

			@Override
			public int compare(ExceptionInfo arg0, ExceptionInfo arg1) {
				return arg0.getId().compareToIgnoreCase(arg1.getId());
			}
			
		});
		licenses.sort(new Comparator<LicenseInfo>() {
			
			@Override
			public int compare(LicenseInfo arg0, LicenseInfo arg1) {
				return arg0.getId().compareToIgnoreCase(arg1.getId());
			}
			
		});
		int maxLicenseName = "Full Name of License".length();
		int maxExceptionName = "Full Name of Exception".length();
		int maxDeprecatedLicenseName = "Full Name of License".length();
		int maxShortIdLength = "Deprecated SPDX License Identifier".length();
		
		for (LicenseInfo li:licenses) {
			if (li.getId().length()+4 > maxShortIdLength) {
				maxShortIdLength = li.getId().length()+4;
			}
			if (li.isDeprecated()) {
				if (li.getName().length() > maxDeprecatedLicenseName) {
					maxDeprecatedLicenseName = li.getName().length();
				}
			} else {
				if (li.getName().length() > maxLicenseName) {
					maxLicenseName = li.getName().length();
				}
			}			
		}
		
		for (ExceptionInfo ei:exceptions) {
			if (ei.getId().length()+4 > maxShortIdLength) {
				maxShortIdLength = ei.getId().length()+4;
			}
			if (ei.getName().length() > maxExceptionName) {
				maxExceptionName = ei.getName().length();
			}
		}

		// license list
		writer.write("# License List\n");
		writer.write("The following liceses have been generated from the license list version ");
		writer.write(licenseListVersion);
		writer.write("\n\n");
		writer.write("## Licenses with Short Idenifiers\n\n");
		String licenseTableHeaderFormat = "| %-"+maxLicenseName+"s | %-"+maxShortIdLength+"s | %-4s |\n";
		writer.write(String.format(licenseTableHeaderFormat, "Full Name of License", "Short Identifier","OSI?" ));
		writer.write("|-");
		addFill(writer, '-', maxLicenseName);
		writer.write("-|-");
		addFill(writer, '-', maxShortIdLength);
		writer.write("-|-");
		addFill(writer, '-', 4);
		writer.write("-|\n");
		String licenseTableRowFormat = "| %-"+maxLicenseName+"s | %-"+maxShortIdLength+"s | %-4s |\n"; 
		for (LicenseInfo li:licenses) {
			if (!li.isDeprecated()) {
				String idStr = formatIdString(li.getId(),maxShortIdLength);
				writer.write(String.format(licenseTableRowFormat, li.getName(), idStr, (li.isOsiApproved)?"Y":""));
			}
		}
		writer.write("\n");
		
		// Exception list
		writer.write("## Exception List\n\n");
		String exceptionTableHeaderFormat = "| %-"+maxExceptionName+"s | %-"+maxShortIdLength+"s\n";
		writer.write(String.format(exceptionTableHeaderFormat, "Full Name of Exception", "SPDX LicenseException" ));
		writer.write("|-");
		addFill(writer, '-', maxExceptionName);
		writer.write("-|-");
		addFill(writer, '-', maxShortIdLength);
		writer.write("-|\n");
		String exceptionTableRowFormat = "| %-"+maxExceptionName+"s | %-"+maxShortIdLength+"s |\n"; 
		for (ExceptionInfo ei:exceptions) {
			if (!ei.isDeprecated()) {
				String idStr = formatIdString(ei.getId(),maxShortIdLength);
				writer.write(String.format(exceptionTableRowFormat, ei.getName(), idStr));
			}
		}
		writer.write("\n");
		
		// deprecated license list
				writer.write("## Deprecated Licenses\n\n");
				String deprecatedLicenseTableHeaderFormat = "| %-"+maxLicenseName+"s | %-"+maxShortIdLength+"s |\n";
				writer.write(String.format(deprecatedLicenseTableHeaderFormat, "Full Name of License", "Deprecated SPDX License Identifier" ));
				writer.write("|-");
				addFill(writer, '-', maxLicenseName);
				writer.write("-|-");
				addFill(writer, '-', maxShortIdLength);
				writer.write("-|\n");
				String deprecatedLicenseTableRowFormat = "| %-"+maxLicenseName+"s | %-"+maxShortIdLength+"s \n"; 
				for (LicenseInfo li:licenses) {
					if (li.isDeprecated()) {
						String idStr = formatIdString(li.getId(),maxShortIdLength);
						writer.write(String.format(deprecatedLicenseTableRowFormat, li.getName(), idStr));
					}
				}
		// print the links
		writer.write("\n");
		String linkFormat = "[%s]: text/%s.txt\n";
		for (LicenseInfo li:licenses) {
			writer.write(String.format(linkFormat, li.getId(), li.getId()));
		}
		for (ExceptionInfo ei:exceptions) {
			writer.write(String.format(linkFormat, ei.getId(), ei.getId()));
		}

	}
	
	private String formatIdString(String id, int maxShortIdLength) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(id);
		sb.append("][]");
		return sb.toString();
	}
	private void addFill(Writer writer, char c, int fillLen) throws IOException {
		for (int i = 0; i < fillLen; i++) {
			writer.write(c);
		}
	}
}
