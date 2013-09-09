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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Iterator;

import org.spdx.licenseTemplate.SpdxLicenseTemplateHelper;
import org.spdx.spdxspreadsheet.SPDXLicenseSpreadsheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;

/**
 * Converts a spreadsheet containing SPDX License information into RDFA 
 * HTML pages describing the licenses.
 * @author Gary O'Neall
 *
 */
public class LicenseRDFAGenerator {
	static final HashSet<Character> INVALID_FILE_CHARS = new HashSet<Character>();

	static { 	

		INVALID_FILE_CHARS.add('\\'); INVALID_FILE_CHARS.add('/'); INVALID_FILE_CHARS.add('*');
		INVALID_FILE_CHARS.add('<'); INVALID_FILE_CHARS.add('>'); INVALID_FILE_CHARS.add('[');
		INVALID_FILE_CHARS.add(']'); INVALID_FILE_CHARS.add('='); 
		INVALID_FILE_CHARS.add(';'); INVALID_FILE_CHARS.add(':');
		INVALID_FILE_CHARS.add('\''); INVALID_FILE_CHARS.add('"'); INVALID_FILE_CHARS.add('|');
		INVALID_FILE_CHARS.add('\t'); INVALID_FILE_CHARS.add('?'); INVALID_FILE_CHARS.add('&');
		INVALID_FILE_CHARS.add('³');
	}
	static int MIN_ARGS = 2;
	static int MAX_ARGS = 4;
	
	static final String CSS_FILE_TEXT = "body { font-family: Tahoma, Verdana, sans-serif; }\n\n.license-text {\n"+
		"background-color: WhiteSmoke;\nborder: 1px dashed Black;\npadding: 1ex;\n}\n\n" +
		"."+SpdxLicenseTemplateHelper.REPLACEABLE_LICENSE_TEXT_CLASS+" {\n{color:rgb(0,0,255);\n}\n"+
		"."+SpdxLicenseTemplateHelper.OPTIONAL_LICENSE_TEXT_CLASS+" {\n{color:rgb(0,255,0);\n}\n"+
		"h2 {\n"+
		"border-bottom: 1px solid Gray;\n}\ndt {\nfont-weight: bold;\n}\n\nul {\n"+
		"padding-left: 1em;\n}\n\ntable {\nborder-collapse: collapse;\n}\n"+
		"td,th {\nmargin: 0;\nborder: 1px solid black;\n}\n";
	static final String CSS_FILE_NAME = "screen.css";
	static final String LICENSE_HTML_TEMPLATE_FILENAME = "resources/LicenseHTMLTemplate.txt";
	static final String TOC_HTML_TEMPLATE_FILENAME = "resources/TocHTMLTemplate.txt";
	/**
	 * @param args Arg 0 is the input spreadsheet, arg 1 is the directory for the output html files
	 */
	public static void main(String[] args) {
		if (args == null || args.length < MIN_ARGS || args.length > MAX_ARGS) {
			System.out.println("Invalid arguments");
			usage();
			return;
		}
		File ssFile = new File(args[0]);
		if (!ssFile.exists()) {
			System.out.println("Spreadsheet file "+ssFile.getName()+" does not exist");
			usage();
			return;
		}
		File dir = new File(args[1]);
		if (!dir.exists()) {
			System.out.println("Output directory "+dir.getName()+" does not exist");
			usage();
			return;
		}
		if (!dir.isDirectory()) {
			System.out.println("Output directory "+dir.getName()+" is not a directory");
			usage();
			return;
		}
		String version = null;
		if (args.length > 2) {
			version = args[2];
		}
		String releaseDate = null;
		if (args.length > 3) {
			releaseDate = args[3];
		}
		File htmlTemplateFile = new File(LICENSE_HTML_TEMPLATE_FILENAME);
		if (!htmlTemplateFile.exists()) {
			System.out.println("Missing HTML template file "+htmlTemplateFile.getPath()+".  Check installation");
			return;
		}
		if (!htmlTemplateFile.canRead()) {
			System.out.println("Can not read HTML template file "+htmlTemplateFile.getPath()+".  Make sure program is installed in a directory with read permissions.");
			return;
		}
		String htmlTemplate = textFileToString(htmlTemplateFile);
		if (htmlTemplate == null) {
			System.out.println("Error: empty HTML template");
			return;
		}
		File tocTemplateFile = new File(TOC_HTML_TEMPLATE_FILENAME);
		if (!tocTemplateFile.exists()) {
			System.out.println("Missing Table of Contents template file "+tocTemplateFile.getPath()+".  Check installation");
			return;
		}
		if (!tocTemplateFile.canRead()) {
			System.out.println("Can not read Table of Contents template file "+tocTemplateFile.getPath()+".  Make sure program is installed in a directory with read permissions.");
			return;
		}
		String tocTemplate = textFileToString(tocTemplateFile);
		if (tocTemplate == null) {
			System.out.println("Error: empty Table of Contents template");
			return;
		}
		IStandardLicenseProvider licenseProvider = null;
		try {
			if (ssFile.getName().toLowerCase().endsWith(".xls")) {
				SPDXLicenseSpreadsheet licenseSpreadsheet = new SPDXLicenseSpreadsheet(ssFile, false, true);
				licenseProvider = licenseSpreadsheet;
				if (version == null || version.trim().isEmpty()) {
					version = licenseSpreadsheet.getLicenseSheet().getVersion();
				}
				if (releaseDate == null || releaseDate.trim().isEmpty()) {
					releaseDate = licenseSpreadsheet.getLicenseSheet().getReleaseDate();
				}
			} else {
				// we assume it is a csv file
				licenseProvider = new SpdxLicenseCsv(ssFile);
			}
			LicenseHTMLFile licHtml = new LicenseHTMLFile(htmlTemplate);
			LicenseTOCHTMLFile tableOfContents = new LicenseTOCHTMLFile(tocTemplate, version, releaseDate);
			Iterator<SPDXStandardLicense> iter = licenseProvider.getIterator();
			String tocFileName = "index.html";
			while (iter.hasNext()) {
				SPDXStandardLicense license = iter.next();
				if (license.getId() != null && !license.getId().isEmpty()) {
					System.out.println("Processing "+license.toString());
					licHtml.setLicense(license);
					String licHtmlFileName = formLicenseHTMLFileName(license);
					String licHTMLReference = "./"+licHtmlFileName;
					String tocHTMLReference = "./"+tocFileName;
					File licHtmlFile = new File(dir.getPath()+File.separator+licHtmlFileName);
					licHtml.writeToFile(licHtmlFile, tocHTMLReference);
					tableOfContents.addLicense(license, licHTMLReference);
				}
			}
			File tocHtmlFile = new File(dir.getPath()+File.separator+tocFileName);
			tableOfContents.writeToFile(tocHtmlFile);
			writeCssFile(dir);
		} catch (SpreadsheetException e) {
			System.out.println("Invalid spreadsheet: "+e.getMessage());
		} catch (SpdxStdLicenseException e) {
			System.out.println("Error reading standard licenses: "+e.getMessage());
		} catch (Exception e) {
			System.out.println("Unhandled exception generating html:");
			e.printStackTrace();
		} finally {
			if (licenseProvider != null && (licenseProvider instanceof SPDXLicenseSpreadsheet)) {
				try {
					SPDXLicenseSpreadsheet spreadsheet = (SPDXLicenseSpreadsheet)licenseProvider;
					spreadsheet.close();
				} catch (SpreadsheetException e) {
					System.out.println("Error closing spreadsheet file: "+e.getMessage());
				}
			} else if (licenseProvider != null && (licenseProvider instanceof SpdxLicenseCsv)) {
				SpdxLicenseCsv licenseCsv = (SpdxLicenseCsv)licenseProvider;
				try {
					licenseCsv.close();
				} catch (IOException e) {
					System.out.println("Error closing CSV file: "+e.getMessage());
				}
			}
		}
	}
	/**
	 * @param htmlTemplateFile
	 * @return
	 */
	private static String textFileToString(File htmlTemplateFile) {
		FileInputStream fis = null;
		InputStreamReader reader = null;
		BufferedReader in = null;
		String retval = null;
		try {
			fis = new FileInputStream(htmlTemplateFile);
			reader = new InputStreamReader(fis, "UTF-8");
			in = new BufferedReader(reader);
			StringBuilder sb = new StringBuilder();
			String line = in.readLine();
			while (line != null) {
				sb.append(line);
				sb.append('\n');
				line = in.readLine();
			}
			retval = sb.toString();
		} catch (IOException e) {
			System.out.println("IO Error copying HTML template files: "+e.getMessage());
			return null;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					System.out.println("Warning - error closing HTML template file.  Processing will continue.  Error: "+e.getMessage());
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					System.out.println("Warning - error closing HTML template file.  Processing will continue.  Error: "+e.getMessage());
				}
			} 
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					System.out.println("Warning - error closing HTML template file.  Processing will continue.  Error: "+e.getMessage());
				}
			}
		}
		return retval;
	}
	private static void writeCssFile(File dir) throws IOException {
		File cssFile = new File(dir.getPath()+ File.separator + CSS_FILE_NAME);
		if (cssFile.exists()) {
			return;	// assume we don't need to create it
		}
		FileOutputStream stream = null;
		OutputStreamWriter writer = null;
		try {
			stream = new FileOutputStream(cssFile);
			writer = new OutputStreamWriter(stream);
			writer.write(CSS_FILE_TEXT);
		} finally {
			if (writer != null) {
				writer.close();
			}
			if (stream != null) {
				stream.close();
			}
		}
		
	}
	private static String formLicenseHTMLFileName(SPDXStandardLicense license) {
		StringBuilder sb = new StringBuilder();
		String licId = license.getId();
		for (int i = 0; i < licId.length(); i++) {
			if (INVALID_FILE_CHARS.contains(licId.charAt(i))) {
				sb.append('_');
			} else {
				sb.append(licId.charAt(i));
			}
		}
//		sb.append(".html");
		return sb.toString();
	}
	private static void usage() {
		System.out.println("Usage:");
		System.out.println("LicenseRDFAGenerator licenseSpreadsheet.xls outputDirectory [version] [releasedate]");
		System.out.println("   Note - if version or release date is not specified, the information will be taken from the spreadsheet.");
	}

}
