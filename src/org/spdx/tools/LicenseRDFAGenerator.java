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
package org.spdx.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.spdx.compare.LicenseCompareHelper;
import org.spdx.html.ExceptionHtml;
import org.spdx.html.ExceptionHtmlToc;
import org.spdx.html.LicenseHTMLFile;
import org.spdx.html.LicenseTOCHTMLFile;
import org.spdx.licenseTemplate.LicenseTemplateRuleException;
import org.spdx.licenseTemplate.SpdxLicenseTemplateHelper;
import org.spdx.rdfparser.IStandardLicenseProvider;
import org.spdx.rdfparser.SPDXLicenseRestrictionException;
import org.spdx.rdfparser.SPDXStandardLicense;
import org.spdx.rdfparser.SpdxLicenseRestriction;
import org.spdx.rdfparser.SpdxStdLicenseException;
import org.spdx.spdxspreadsheet.SPDXLicenseSpreadsheet;
import org.spdx.spdxspreadsheet.SPDXLicenseSpreadsheet.DeprecatedLicenseInfo;
import org.spdx.spdxspreadsheet.SpreadsheetException;

import com.google.common.io.Files;
import com.sampullara.mustache.MustacheException;

/**
 * Converts a spreadsheet containing SPDX License information into RDFA 
 * HTML pages describing the licenses (root of output folder),
 * license text files (text folder), license template files (template folder), and files
 * containing only the HTML fragments for the license text (html folder).
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

	static final int ERROR_STATUS = 1;
	static final String CSS_TEMPLATE_FILE = "resources/screen.css";
	static final String CSS_FILE_NAME = "screen.css";
	static final String TEXT_FOLDER_NAME = "text";
	static final String TEMPLATE_FOLDER_NAME = "template";
	static final String HTML_FOLDER_NAME = "html";
	static final String LICENSE_TOC_FILE_NAME = "index.html";
	static final String EXCEPTION_FOLDER_NAME = "exceptions";
	static final String EXCEPTION_TOC_FILE_NAME = "index.html";
	
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


		ArrayList<String> warnings = new ArrayList<String>();
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
				System.out.println("Unsupported file format.  Must be a .xls file");
				System.exit(ERROR_STATUS);
			}
			File exceptionsFolder = new File(dir.getPath() + File.separator + EXCEPTION_FOLDER_NAME);
			if (!exceptionsFolder.exists()) {
				exceptionsFolder.mkdir();
			}
			File textFolder = new File(dir.getPath() + File.separator +  TEXT_FOLDER_NAME);
			if (!textFolder.exists()) {
				textFolder.mkdir();
			}
			if (!textFolder.isDirectory()) {
				System.out.println("Error: text folder is not a directory");
				return;
			}
			File templateFolder = new File(dir.getPath() + File.separator +  TEMPLATE_FOLDER_NAME);
			if (!templateFolder.exists()) {
				templateFolder.mkdir();
			}
			if (!templateFolder.isDirectory()) {
				System.out.println("Error: template folder is not a directory");
				return;
			}
			File htmlFolder = new File(dir.getPath() + File.separator +  HTML_FOLDER_NAME);
			if (!htmlFolder.exists()) {
				htmlFolder.mkdir();
			}
			if (!htmlFolder.isDirectory()) {
				System.out.println("Error: HTML folder is not a directory");
				return;
			}
			System.out.print("Processing License List");
			writeLicenseList(version, releaseDate, licenseProvider, warnings,
					dir, textFolder, htmlFolder, templateFolder);
			System.out.println();
			System.out.print("Processing Exceptions");
			writeExceptionList(version, licenseProvider, warnings,
					exceptionsFolder, textFolder, htmlFolder, templateFolder);
			writeCssFile(dir);
			System.out.println();
			if (warnings.size() > 0) {
				System.out.println("The following warning(s) were identified:");
				for (String warning : warnings) {
					System.out.println("\t"+warning);
				}
			}
			System.out.println("Completed processing licenses");
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
			}
		}
	}
	/**
	 * @param version License list version
	 * @param licenseProvider Provides the licensing information
	 * @param warnings Populated with any warnings if they occur
	 * @param dir Directory storing the main HTML files
	 * @param textFolder Directory holding the text only representation of the files
	 * @param htmlFolder Directory holding the HTML formated license text
	 * @param templateFolder Directory holding the template representation of the license text
	 * @throws IOException 
	 * @throws SpreadsheetException 
	 * @throws SPDXLicenseRestrictionException 
	 * @throws MustacheException 
	*/
	private static void writeExceptionList(String version,
			IStandardLicenseProvider licenseProvider,
			ArrayList<String> warnings, File dir, File textFolder,
			File htmlFolder, File templateFolder) throws IOException, SPDXLicenseRestrictionException, SpreadsheetException, MustacheException {
		Charset utf8 = Charset.forName("UTF-8");
		String exceptionHtmlTocReference = "./" + EXCEPTION_TOC_FILE_NAME;
		ExceptionHtmlToc exceptionToc = new ExceptionHtmlToc();
		Iterator<SpdxLicenseRestriction> exceptionIter = licenseProvider.getExceptionIterator();
		HashMap<String, String> addedExceptionsMap = new HashMap<String, String>();
		while (exceptionIter.hasNext()) {
			System.out.print(".");
			SpdxLicenseRestriction nextException = exceptionIter.next();
			if (nextException.getId() != null && !nextException.getId().isEmpty()) {
				// check for duplicate exceptions
				Iterator<Entry<String, String>> addedExceptionIter = addedExceptionsMap.entrySet().iterator();
				while (addedExceptionIter.hasNext()) {
					Entry<String, String> entry = addedExceptionIter.next();
					if (entry.getValue().trim().equals(nextException.getText().trim())) {
						warnings.add("Duplicates exceptions: "+nextException.getId()+", "+entry.getKey());
					}
				}
				addedExceptionsMap.put(nextException.getId(), nextException.getText());
				ExceptionHtml exceptionHtml = new ExceptionHtml(nextException);
				String exceptionHtmlFileName = formLicenseHTMLFileName(nextException.getId());
				String exceptionHTMLReference = "./"+exceptionHtmlFileName;
				File exceptionHtmlFile = new File(dir.getPath()+File.separator+exceptionHtmlFileName);
				exceptionHtml.writeToFile(exceptionHtmlFile, exceptionHtmlTocReference);
				exceptionToc.addException(nextException, exceptionHTMLReference);
				File textFile = new File(textFolder.getPath() + File.separator + exceptionHtmlFileName + ".txt");
				Files.write(nextException.getText(), textFile, utf8);
				File htmlTextFile = new File(htmlFolder.getPath() + File.separator + exceptionHtmlFileName + ".html");
				Files.write(SpdxLicenseTemplateHelper.escapeHTML(nextException.getText()), htmlTextFile, utf8);
			}
		}
		File exceptionTocFile = new File(dir.getPath()+File.separator+EXCEPTION_TOC_FILE_NAME);
		exceptionToc.writeToFile(exceptionTocFile, version);
	}
	/**
	 * Writes the index.html file and the individual license list HTML files
	 * @param version License list version
	 * @param releaseDate License list release date
	 * @param licenseProvider Provides the licensing information
	 * @param warnings Populated with any warnings if they occur
	 * @param dir Directory storing the main HTML files
	 * @param textFolder Directory holding the text only representation of the files
	 * @param htmlFolder Directory holding the HTML formated license text
	 * @param templateFolder Directory holding the template representation of the license text
	 * @throws SpdxStdLicenseException 
	 * @throws IOException 
	 * @throws LicenseTemplateRuleException 
	 * @throws MustacheException 
	 * 
	 */
	private static void writeLicenseList(String version, String releaseDate,
			IStandardLicenseProvider licenseProvider, ArrayList<String> warnings,
			File dir, File textFolder, File htmlFolder, File templateFolder) throws SpdxStdLicenseException, IOException, LicenseTemplateRuleException, MustacheException {
		Charset utf8 = Charset.forName("UTF-8");
		LicenseHTMLFile licHtml = new LicenseHTMLFile();
		LicenseTOCHTMLFile tableOfContents = new LicenseTOCHTMLFile(version, releaseDate);
		// Main page - License list
		Iterator<SPDXStandardLicense> licenseIter = licenseProvider.getLicenseIterator();
		HashMap<String, String> addedLicIdTextMap = new HashMap<String, String>();
		while (licenseIter.hasNext()) {
			System.out.print(".");
			SPDXStandardLicense license = licenseIter.next();
			if (license.getId() != null && !license.getId().isEmpty()) {
				// Check for duplicate licenses
				Iterator<Entry<String, String>> addedLicenseTextIter = addedLicIdTextMap.entrySet().iterator();
				while (addedLicenseTextIter.hasNext()) {
					Entry<String, String> entry = addedLicenseTextIter.next();
					if (LicenseCompareHelper.isLicenseTextEquivalent(entry.getValue(), license.getText())) {
						warnings.add("Duplicates licenses: "+license.getId()+", "+entry.getKey());
					}
				}
				addedLicIdTextMap.put(license.getId(), license.getText());
				licHtml.setLicense(license);
				licHtml.setDeprecated(false);
				String licHtmlFileName = formLicenseHTMLFileName(license.getId());
				String licHTMLReference = "./"+licHtmlFileName;
				String tocHTMLReference = "./"+LICENSE_TOC_FILE_NAME;
				File licHtmlFile = new File(dir.getPath()+File.separator+licHtmlFileName);
				licHtml.writeToFile(licHtmlFile, tocHTMLReference);
				tableOfContents.addLicense(license, licHTMLReference);
				File textFile = new File(textFolder.getPath() + File.separator + licHtmlFileName + ".txt");
				Files.write(license.getText(), textFile, utf8);
				if (license.getTemplate() != null && !license.getTemplate().trim().isEmpty()) {
					File templateFile = new File(templateFolder.getPath() + File.separator + licHtmlFileName + "-template.txt");
					Files.write(license.getTemplate(), templateFile, utf8);
				}
				File htmlTextFile = new File(htmlFolder.getPath() + File.separator + licHtmlFileName + ".html");
				Files.write(SpdxLicenseTemplateHelper.escapeHTML(license.getText()), htmlTextFile, utf8);
			}
		}
		Iterator<DeprecatedLicenseInfo> depIter = licenseProvider.getDeprecatedLicenseIterator();
		while (depIter.hasNext()) {
			DeprecatedLicenseInfo deprecatedLicense = depIter.next();
			licHtml.setLicense(deprecatedLicense.getLicense());
			licHtml.setDeprecated(true);
			licHtml.setDeprecatedVersion(deprecatedLicense.getDeprecatedVersion());
			String licHtmlFileName = formLicenseHTMLFileName(deprecatedLicense.getLicense().getId());
			String licHTMLReference = "./"+licHtmlFileName;
			String tocHTMLReference = "./"+LICENSE_TOC_FILE_NAME;
			File licHtmlFile = new File(dir.getPath()+File.separator+licHtmlFileName);
			licHtml.writeToFile(licHtmlFile, tocHTMLReference);
			tableOfContents.addDeprecatedLicense(deprecatedLicense, licHTMLReference);
			File textFile = new File(textFolder.getPath() + File.separator + licHtmlFileName + ".txt");
			Files.write(deprecatedLicense.getLicense().getText(), textFile, utf8);
			if (deprecatedLicense.getLicense().getTemplate() != null && !deprecatedLicense.getLicense().getTemplate().trim().isEmpty()) {
				File templateFile = new File(templateFolder.getPath() + File.separator + licHtmlFileName + "-template.txt");
				Files.write(deprecatedLicense.getLicense().getTemplate(), templateFile, utf8);
			}
			File htmlTextFile = new File(htmlFolder.getPath() + File.separator + licHtmlFileName + ".html");
			Files.write(SpdxLicenseTemplateHelper.escapeHTML(deprecatedLicense.getLicense().getText()), htmlTextFile, utf8);

		}
		File tocHtmlFile = new File(dir.getPath()+File.separator+LICENSE_TOC_FILE_NAME);
		tableOfContents.writeToFile(tocHtmlFile);
	}

	private static void writeCssFile(File dir) throws IOException {
		File cssFile = new File(dir.getPath()+ File.separator + CSS_FILE_NAME);
		if (cssFile.exists()) {
			return;	// assume we don't need to create it
		}
		File cssTemplateFile = new File(CSS_TEMPLATE_FILE); 
		Files.copy(cssTemplateFile, cssFile);		
	}
	
	private static String formLicenseHTMLFileName(String id) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < id.length(); i++) {
			if (INVALID_FILE_CHARS.contains(id.charAt(i))) {
				sb.append('_');
			} else {
				sb.append(id.charAt(i));
			}
		}
		return sb.toString();
	}
	private static void usage() {
		System.out.println("Usage:");
		System.out.println("LicenseRDFAGenerator licenseSpreadsheet.xls outputDirectory [version] [releasedate]");
		System.out.println("   Note - if version or release date is not specified, the information will be taken from the spreadsheet.");
	}

}
