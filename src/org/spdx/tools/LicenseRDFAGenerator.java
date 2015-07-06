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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.spdx.compare.LicenseCompareHelper;
import org.spdx.html.ExceptionHtml;
import org.spdx.html.ExceptionHtmlToc;
import org.spdx.html.LicenseHTMLFile;
import org.spdx.html.LicenseTOCHTMLFile;
import org.spdx.html.LicenseTOCJSONFile;
import org.spdx.licenseTemplate.LicenseTemplateRuleException;
import org.spdx.licenseTemplate.SpdxLicenseTemplateHelper;
import org.spdx.rdfparser.license.ISpdxListedLicenseProvider;
import org.spdx.rdfparser.license.LicenseException;
import org.spdx.rdfparser.license.LicenseRestrictionException;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.license.SpdxListedLicenseException;
import org.spdx.spdxspreadsheet.SPDXLicenseSpreadsheet;
import org.spdx.spdxspreadsheet.SPDXLicenseSpreadsheet.DeprecatedLicenseInfo;
import org.spdx.spdxspreadsheet.SpreadsheetException;

import com.github.mustachejava.MustacheException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

/**
 * Converts a spreadsheet containing SPDX License information into RDFA 
 * HTML pages describing the licenses (root of output folder),
 * license text files (text folder), license template files (template folder), and files
 * containing only the HTML fragments for the license text (html folder).
 * @author Gary O'Neall
 *
 */
public class LicenseRDFAGenerator {
	static final Set<Character> INVALID_FILENAME_CHARS = Sets.newHashSet();

	static { 	

		INVALID_FILENAME_CHARS.add('\\'); INVALID_FILENAME_CHARS.add('/'); INVALID_FILENAME_CHARS.add('*');
		INVALID_FILENAME_CHARS.add('<'); INVALID_FILENAME_CHARS.add('>'); INVALID_FILENAME_CHARS.add('[');
		INVALID_FILENAME_CHARS.add(']'); INVALID_FILENAME_CHARS.add('='); 
		INVALID_FILENAME_CHARS.add(';'); INVALID_FILENAME_CHARS.add(':');
		INVALID_FILENAME_CHARS.add('\''); INVALID_FILENAME_CHARS.add('"'); INVALID_FILENAME_CHARS.add('|');
		INVALID_FILENAME_CHARS.add('\t'); INVALID_FILENAME_CHARS.add('?'); INVALID_FILENAME_CHARS.add('&');
		INVALID_FILENAME_CHARS.add('³');
	}
	
	static final Set<Character> INVALID_TEXT_CHARS = Sets.newHashSet();
	
	static {
		INVALID_TEXT_CHARS.add('\uFFFD');
	}
	static int MIN_ARGS = 2;
	static int MAX_ARGS = 4;

	static final int ERROR_STATUS = 1;
	static final String CSS_TEMPLATE_FILE = "resources/screen.css";
	static final String CSS_FILE_NAME = "screen.css";
	static final String SORTTABLE_JS_FILE = "resources/sorttable.js";
	static final String SORTTABLE_FILE_NAME = "sorttable.js";
	static final String TEXT_FOLDER_NAME = "text";
	static final String TEMPLATE_FOLDER_NAME = "template";
	static final String HTML_FOLDER_NAME = "html";
	static final String LICENSE_TOC_JSON_FILE_NAME = "licenses.json";
	static final String LICENSE_TOC_HTML_FILE_NAME = "index.html";
	static final String EXCEPTION_TOC_FILE_NAME = "exceptions-index.html";
	
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


		List<String> warnings = Lists.newArrayList();
		ISpdxListedLicenseProvider licenseProvider = null;
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
					dir, textFolder, htmlFolder, templateFolder);
			writeCssFile(dir);
			writeSortTableFile(dir);
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
		} catch (SpdxListedLicenseException e) {
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
	 * @throws LicenseRestrictionException 
	 * @throws MustacheException 
	*/
	private static void writeExceptionList(String version,
			ISpdxListedLicenseProvider licenseProvider,
			List<String> warnings, File dir, File textFolder,
			File htmlFolder, File templateFolder) throws IOException, LicenseRestrictionException, SpreadsheetException, MustacheException {
		Charset utf8 = Charset.forName("UTF-8");
		// Collect license ID's to check for any duplicate ID's being used (e.g. license ID == exception ID)
		Set<String> licenseIds = Sets.newHashSet();
		try {
			Iterator<SpdxListedLicense> licIter = licenseProvider.getLicenseIterator();
			while (licIter.hasNext()) {
				licenseIds.add(licIter.next().getLicenseId());
			}	
		} catch (SpdxListedLicenseException e) {
			System.out.println("Warning - Not able to check for duplicate license and exception ID's");
		}
		String exceptionHtmlTocReference = "./" + EXCEPTION_TOC_FILE_NAME;
		ExceptionHtmlToc exceptionToc = new ExceptionHtmlToc();
		Iterator<LicenseException> exceptionIter = licenseProvider.getExceptionIterator();
		Map<String, String> addedExceptionsMap = Maps.newHashMap();
		while (exceptionIter.hasNext()) {
			System.out.print(".");
			LicenseException nextException = exceptionIter.next();
			if (nextException.getLicenseExceptionId() != null && !nextException.getLicenseExceptionId().isEmpty()) {
				// check for duplicate exceptions
				Iterator<Entry<String, String>> addedExceptionIter = addedExceptionsMap.entrySet().iterator();
				while (addedExceptionIter.hasNext()) {
					Entry<String, String> entry = addedExceptionIter.next();
					if (entry.getValue().trim().equals(nextException.getLicenseExceptionText().trim())) {
						warnings.add("Duplicates exceptions: "+nextException.getLicenseExceptionId()+", "+entry.getKey());
					}
				}
				// check for a license ID with the same ID as the exception
				if (licenseIds.contains(nextException.getLicenseExceptionId())) {
					warnings.add("A license ID exists with the same ID as an exception ID: "+nextException.getLicenseExceptionId());
				}
				checkText(nextException.getLicenseExceptionText(), 
						"License Exception Text for "+nextException.getLicenseExceptionId(), warnings);
				addedExceptionsMap.put(nextException.getLicenseExceptionId(), nextException.getLicenseExceptionText());
				ExceptionHtml exceptionHtml = new ExceptionHtml(nextException);
				String exceptionHtmlFileName = formLicenseHTMLFileName(nextException.getLicenseExceptionId());
				String exceptionHTMLReference = "./"+exceptionHtmlFileName + ".html";
				File exceptionHtmlFile = new File(dir.getPath()+File.separator+exceptionHtmlFileName + ".html");
				exceptionHtml.writeToFile(exceptionHtmlFile, exceptionHtmlTocReference);
				exceptionToc.addException(nextException, exceptionHTMLReference);
				File textFile = new File(textFolder.getPath() + File.separator + exceptionHtmlFileName + ".txt");
				Files.write(nextException.getLicenseExceptionText(), textFile, utf8);
				File htmlTextFile = new File(htmlFolder.getPath() + File.separator + exceptionHtmlFileName + ".html");
				Files.write(SpdxLicenseTemplateHelper.escapeHTML(nextException.getLicenseExceptionText()), htmlTextFile, utf8);
			}
		}
		File exceptionTocFile = new File(dir.getPath()+File.separator+EXCEPTION_TOC_FILE_NAME);
		exceptionToc.writeToFile(exceptionTocFile, version);
	}
	/**
	 * Check text for invalid characters
	 * @param text Text to check
	 * @param textDescription Description of the text being check (this will be used to form warning messages)
	 * @param warnings Array list of warnings to add to if an problem is found with the text
	 */
	private static void checkText(String text, String textDescription,
			List<String> warnings) {
		for (int i = 0; i < text.length(); i++) {
			if (INVALID_TEXT_CHARS.contains(text.charAt(i))) {
				warnings.add("Invalid character in " + textDescription +
						" at character location "+String.valueOf(i));
			}
		}
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
	 * @throws SpdxListedLicenseException 
	 * @throws IOException 
	 * @throws LicenseTemplateRuleException 
	 * @throws MustacheException 
	 * 
	 */
	private static void writeLicenseList(String version, String releaseDate,
			ISpdxListedLicenseProvider licenseProvider, List<String> warnings,
			File dir, File textFolder, File htmlFolder, File templateFolder) throws SpdxListedLicenseException, IOException, LicenseTemplateRuleException, MustacheException {
		Charset utf8 = Charset.forName("UTF-8");
		LicenseHTMLFile licHtml = new LicenseHTMLFile();
		LicenseTOCJSONFile tableOfContentsJSON = new LicenseTOCJSONFile(version, releaseDate);
		LicenseTOCHTMLFile tableOfContentsHTML = new LicenseTOCHTMLFile(version, releaseDate);
		// Main page - License list
		Iterator<SpdxListedLicense> licenseIter = licenseProvider.getLicenseIterator();
		Map<String, String> addedLicIdTextMap = Maps.newHashMap();
		while (licenseIter.hasNext()) {
			System.out.print(".");
			SpdxListedLicense license = licenseIter.next();
			if (license.getLicenseId() != null && !license.getLicenseId().isEmpty()) {
				// Check for duplicate licenses
				Iterator<Entry<String, String>> addedLicenseTextIter = addedLicIdTextMap.entrySet().iterator();
				while (addedLicenseTextIter.hasNext()) {
					Entry<String, String> entry = addedLicenseTextIter.next();
					if (LicenseCompareHelper.isLicenseTextEquivalent(entry.getValue(), license.getLicenseText())) {
						warnings.add("Duplicates licenses: "+license.getLicenseId()+", "+entry.getKey());
					}
				}
				addedLicIdTextMap.put(license.getLicenseId(), license.getLicenseText());
				checkText(license.getLicenseText(), "License text for "+license.getLicenseId(), warnings);
				licHtml.setLicense(license);
				licHtml.setDeprecated(false);
				String licBaseHtmlFileName = formLicenseHTMLFileName(license.getLicenseId());
				String licHtmlFileName = licBaseHtmlFileName + ".html";
				String licHTMLReference = "./"+licHtmlFileName;
				String tocHTMLReference = "./"+LICENSE_TOC_HTML_FILE_NAME;
				File licBaseHtmlFile = new File(dir.getPath()+File.separator+licBaseHtmlFileName);
				// the base file is used for direct references from tools, the html is used for rendering by the website
				licHtml.writeToFile(licBaseHtmlFile, tocHTMLReference);
				File licHtmlFile = new File(dir.getPath()+File.separator+licHtmlFileName);
				licHtml.writeToFile(licHtmlFile, tocHTMLReference);
				tableOfContentsJSON.addLicense(license, licHTMLReference);
				tableOfContentsHTML.addLicense(license, licHTMLReference);
				File textFile = new File(textFolder.getPath() + File.separator + licHtmlFileName + ".txt");
				Files.write(license.getLicenseText(), textFile, utf8);
				if (license.getStandardLicenseTemplate() != null && !license.getStandardLicenseTemplate().trim().isEmpty()) {
					File templateFile = new File(templateFolder.getPath() + File.separator + licHtmlFileName + "-template.txt");
					Files.write(license.getStandardLicenseTemplate(), templateFile, utf8);
				}
				File htmlTextFile = new File(htmlFolder.getPath() + File.separator + licHtmlFileName + ".html");
				Files.write(SpdxLicenseTemplateHelper.escapeHTML(license.getLicenseText()), htmlTextFile, utf8);
			}
		}
		Iterator<DeprecatedLicenseInfo> depIter = licenseProvider.getDeprecatedLicenseIterator();
		while (depIter.hasNext()) {
			DeprecatedLicenseInfo deprecatedLicense = depIter.next();
			licHtml.setLicense(deprecatedLicense.getLicense());
			licHtml.setDeprecated(true);
			licHtml.setDeprecatedVersion(deprecatedLicense.getDeprecatedVersion());
			String licHtmlFileName = formLicenseHTMLFileName(deprecatedLicense.getLicense().getLicenseId());
			String licHTMLReference = "./"+licHtmlFileName;
			String tocHTMLReference = "./"+LICENSE_TOC_HTML_FILE_NAME;
			File licHtmlFile = new File(dir.getPath()+File.separator+licHtmlFileName);
			licHtml.writeToFile(licHtmlFile, tocHTMLReference);
			tableOfContentsHTML.addDeprecatedLicense(deprecatedLicense, licHTMLReference);
			File textFile = new File(textFolder.getPath() + File.separator + licHtmlFileName + ".txt");
			Files.write(deprecatedLicense.getLicense().getLicenseText(), textFile, utf8);
			if (deprecatedLicense.getLicense().getStandardLicenseTemplate() != null && !deprecatedLicense.getLicense().getStandardLicenseTemplate().trim().isEmpty()) {
				File templateFile = new File(templateFolder.getPath() + File.separator + licHtmlFileName + "-template.txt");
				Files.write(deprecatedLicense.getLicense().getStandardLicenseTemplate(), templateFile, utf8);
			}
			File htmlTextFile = new File(htmlFolder.getPath() + File.separator + licHtmlFileName + ".html");
			Files.write(SpdxLicenseTemplateHelper.escapeHTML(deprecatedLicense.getLicense().getLicenseText()), htmlTextFile, utf8);

		}
		File tocJsonFile = new File(dir.getPath()+File.separator+LICENSE_TOC_JSON_FILE_NAME);
		File tocHtmlFile = new File(dir.getPath()+File.separator+LICENSE_TOC_HTML_FILE_NAME);
		tableOfContentsJSON.writeToFile(tocJsonFile);
		tableOfContentsHTML.writeToFile(tocHtmlFile);
	}

	private static void writeCssFile(File dir) throws IOException {
		File cssFile = new File(dir.getPath()+ File.separator + CSS_FILE_NAME);
		if (cssFile.exists()) {
			cssFile.delete();
		}
		File cssTemplateFile = new File(CSS_TEMPLATE_FILE); 
		Files.copy(cssTemplateFile, cssFile);		
	}
	
	private static void writeSortTableFile(File dir) throws IOException {
		File sortTableFile = new File(dir.getPath()+ File.separator + SORTTABLE_FILE_NAME);
		if (sortTableFile.exists()) {
			return;	// assume we don't need to create it
		}
		File sortTableJsFile = new File(SORTTABLE_JS_FILE); 
		Files.copy(sortTableJsFile, sortTableFile);		
	}
	
	private static String formLicenseHTMLFileName(String id) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < id.length(); i++) {
			if (INVALID_FILENAME_CHARS.contains(id.charAt(i))) {
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
