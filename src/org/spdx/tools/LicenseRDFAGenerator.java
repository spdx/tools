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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.spdx.compare.LicenseCompareHelper;
import org.spdx.compare.SpdxCompareException;
import org.spdx.html.ExceptionHtml;
import org.spdx.html.ExceptionHtmlToc;
import org.spdx.html.ExceptionTOCJSONFile;
import org.spdx.html.InvalidLicenseTemplateException;
import org.spdx.html.LicenseExceptionJSONFile;
import org.spdx.html.LicenseHTMLFile;
import org.spdx.html.LicenseTOCHTMLFile;
import org.spdx.html.LicenseTOCJSONFile;
import org.spdx.html.LicenseJSONFile;
import org.spdx.licenseTemplate.LicenseTemplateRuleException;
import org.spdx.licenseTemplate.SpdxLicenseTemplateHelper;
import org.spdx.licensexml.XmlLicenseProvider;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.AnyLicenseInfo;
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
	static int MAX_ARGS = 5;

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
	static final String EXCEPTION_JSON_TOC_FILE_NAME = "exceptions.json";
	static final String RDFA_FOLDER_NAME = "rdfa";
	static final String JSON_FOLDER_NAME = "json";
	private static final String WEBSITE_FOLDER_NAME = "website";
	private static final String RDFXML_FOLDER_NAME = "rdfxml";
	private static final String RDFTURTLE_FOLDER_NAME = "rdfturtle";
	private static final String RDFNT_FOLDER_NAME = "rdfnt";
	private static final String TABLE_OF_CONTENTS_FILE_NAME = "licenses.md";
	
	/**
	 * @param args Arg 0 is either an input spreadsheet or a directory of licenses in XML format, arg 1 is the directory for the output html files
	 */
	public static void main(String[] args) {
		if (args == null || args.length < MIN_ARGS || args.length > MAX_ARGS) {
			System.out.println("Invalid arguments");
			usage();
			System.exit(ERROR_STATUS);
		}
		File ssFile = new File(args[0]);
		if (!ssFile.exists()) {
			System.out.println("Spreadsheet file "+ssFile.getName()+" does not exist");
			usage();
			System.exit(ERROR_STATUS);
		}
		File dir = new File(args[1]);
		if (!dir.exists()) {
			System.out.println("Output directory "+dir.getName()+" does not exist");
			usage();
			System.exit(ERROR_STATUS);
		}
		if (!dir.isDirectory()) {
			System.out.println("Output directory "+dir.getName()+" is not a directory");
			usage();
			System.exit(ERROR_STATUS);
		}
		String version = null;
		if (args.length > 2) {
			version = args[2];
		}
		String releaseDate = null;
		if (args.length > 3) {
			releaseDate = args[3];
		}
		File testFileDir = null;
		if (args.length > 4) {
			testFileDir = new File(args[4]);
			if (!testFileDir.exists()) {
				System.out.println("License test directory "+testFileDir.getName()+" does not exist");
				usage();
				System.exit(ERROR_STATUS);
			}
			if (!testFileDir.isDirectory()) {
				System.out.println("License test directory "+testFileDir.getName()+" is not a directory");
				usage();
				System.exit(ERROR_STATUS);
			}
		}

		try {
			generateLicenseData(ssFile, dir, version, releaseDate, testFileDir);
		} catch (LicenseGeneratorException e) {
			System.out.println(e.getMessage());
			System.exit(ERROR_STATUS);
		}


	}
	/**
	 * Generate license data
	 * @param ssFile Either a license spreadsheet file or a directory containing license XML files
	 * @param dir Output directory for the generated results
	 * @param version Version for the license lise
	 * @param releaseDate Release data string for the license
	 * @param testFileDir Directory of license text to test the generated licenses against
	 * @return warnings
	 * @throws LicenseGeneratorException 
	 */
	public static List<String> generateLicenseData(File ssFile, File dir,
			String version, String releaseDate, File testFileDir) throws LicenseGeneratorException {
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
			} else if (ssFile.isDirectory()) {
				licenseProvider = new XmlLicenseProvider(ssFile);
			} else {
				throw new LicenseGeneratorException("Unsupported file format.  Must be a .xls file");
			}
			File textFolder = new File(dir.getPath() + File.separator +  TEXT_FOLDER_NAME);
			if (!textFolder.isDirectory() && !textFolder.mkdir()) {
				throw new LicenseGeneratorException("Error: text folder is not a directory");
			}
			File templateFolder = new File(dir.getPath() + File.separator +  TEMPLATE_FOLDER_NAME);
			if (!templateFolder.isDirectory() && !templateFolder.mkdir()) {
				throw new LicenseGeneratorException("Error: template folder is not a directory");
			}
			File htmlFolder = new File(dir.getPath() + File.separator +  HTML_FOLDER_NAME);
			if (!htmlFolder.isDirectory() && !htmlFolder.mkdir()) {
				throw new LicenseGeneratorException("Error: HTML folder is not a directory");
			}
			File rdfaFolder = new File(dir.getPath() + File.separator +  RDFA_FOLDER_NAME);
			if (!rdfaFolder.isDirectory() && !rdfaFolder.mkdir()) {
				throw new LicenseGeneratorException("Error: RDFa folder is not a directory");
			}
			File jsonFolder = new File(dir.getPath() + File.separator +  JSON_FOLDER_NAME);
			if (!jsonFolder.isDirectory() && !jsonFolder.mkdir()) {
				throw new LicenseGeneratorException("Error: JSON folder is not a directory");
			}
			File jsonFolderDetails = new File(dir.getPath() + File.separator +  JSON_FOLDER_NAME+ File.separator + "details");
			if (!jsonFolderDetails.isDirectory() && !jsonFolderDetails.mkdir()) {
				throw new LicenseGeneratorException("Error: JSON folder is not a directory");
			}
			File jsonFolderExceptions = new File(dir.getPath() + File.separator +  JSON_FOLDER_NAME + File.separator + "exceptions");
			if (!jsonFolderExceptions.isDirectory() && !jsonFolderExceptions.mkdir()) {
				throw new LicenseGeneratorException("Error: JSON folder is not a directory");
			}
			File website = new File(dir.getPath() + File.separator +  WEBSITE_FOLDER_NAME);
			if (!website.isDirectory() && !website.mkdir()) {
				throw new LicenseGeneratorException("Error: Website folder is not a directory");
			}
			File rdfXml = new File(dir.getPath() + File.separator +  RDFXML_FOLDER_NAME);
			if (!rdfXml.isDirectory() && !rdfXml.mkdir()) {
				throw new LicenseGeneratorException("Error: RdfXML folder is not a directory");
			}
			File rdfTurtle = new File(dir.getPath() + File.separator +  RDFTURTLE_FOLDER_NAME);
			if (!rdfTurtle.isDirectory() && !rdfTurtle.mkdir()) {
				throw new LicenseGeneratorException("Error: RDF Turtle folder is not a directory");
			}
			File rdfNt = new File(dir.getPath() + File.separator +  RDFNT_FOLDER_NAME);
			if (!rdfNt.isDirectory() && !rdfNt.mkdir()) {
				throw new LicenseGeneratorException("Error: RDF NT folder is not a directory");
			}
			File markdownFile = new File(dir.getPath() + File.separator +  TABLE_OF_CONTENTS_FILE_NAME);
			if (!markdownFile.isFile() && !markdownFile.createNewFile()) {
				throw new LicenseGeneratorException("Error: Unable to create markdown file");
			}
			LicenseTester tester = null;
			if (testFileDir != null) {
				tester = new LicenseTester(testFileDir);
			}
			// Create model container to hold licenses and exceptions
			LicenseContainer container = new LicenseContainer();
			// Create a MarkdownTable to hold the licenses and exceptions
			MarkdownTable markdownTable = new MarkdownTable(version);
			System.out.print("Processing License List");
			writeLicenseList(version, releaseDate, licenseProvider, warnings,
					website, textFolder, htmlFolder, templateFolder, rdfaFolder, jsonFolder,
					container, rdfXml, rdfTurtle, rdfNt, markdownTable, tester);
			System.out.println();
			System.out.print("Processing Exceptions");
			writeExceptionList(version, releaseDate, licenseProvider, warnings,
					website, textFolder, htmlFolder, templateFolder, rdfaFolder, jsonFolder,
					container, rdfXml, rdfTurtle, rdfNt, markdownTable, tester);
			writeRdf(container, rdfXml, rdfTurtle, rdfNt, "licenses");
			markdownTable.writeToFile(markdownFile);
			
			writeCssFile(website);
			writeSortTableFile(website);
			System.out.println();
			warnings.addAll(licenseProvider.getWarnings());
			if (warnings.size() > 0) {
				System.out.println("The following warning(s) were identified:");
				for (String warning : warnings) {
					System.out.println("\t"+warning);
				}
			}
			System.out.println("Completed processing licenses");
			return warnings;
		} catch (SpreadsheetException e) {
			throw new LicenseGeneratorException("\nInvalid spreadsheet: "+e.getMessage(),e);
		} catch (SpdxListedLicenseException e) {
			throw new LicenseGeneratorException("\nError reading standard licenses: "+e.getMessage(),e);
		} catch (InvalidLicenseTemplateException e) {
			throw new LicenseGeneratorException("\nInvalid template found on one of the licenses: "+e.getMessage(),e);
		} catch (LicenseGeneratorException e) {
			throw(e);
		} catch (Exception e) {
			throw new LicenseGeneratorException("\nUnhandled exception generating html: "+e.getMessage(),e);
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
	 * Write the RDF representations of the licenses and exceptions
	 * @param container Container with the licenses and exceptions
	 * @param rdfXml Folder for the RdfXML representation
	 * @param rdfTurtle Folder for the Turtle representation
	 * @param rdfNt Folder for the NT representation
	 * @param name Name of the file
	 * @throws LicenseGeneratorException 
	 */
	private static void writeRdf(IModelContainer container, File rdfXml, File rdfTurtle, File rdfNt, String name) throws LicenseGeneratorException {
		writeRdf(container, rdfXml.getPath() + File.separator + name + ".rdf", "RDF/XML-ABBREV");
		writeRdf(container, rdfTurtle.getPath() + File.separator + name + ".turtle", "TURTLE");
		writeRdf(container, rdfNt.getPath() + File.separator + name + ".nt", "NT");
	}
	
	/**
	 * Write an RDF file for for all elements in the container
	 * @param container Container for the RDF elements
	 * @param fileName File name to write the elements to
	 * @param format Jena RDF format
	 * @throws LicenseGeneratorException 
	 */
	private static void writeRdf(IModelContainer container, String fileName, String format) throws LicenseGeneratorException {
		File outFile = new File(fileName);
		if (!outFile.exists()) {
			try {
				if (!outFile.createNewFile()) {
					throw new LicenseGeneratorException("Can not create RDF output file "+fileName);
				}
			} catch (IOException e) {
				throw new LicenseGeneratorException("Can not create RDF output file "+fileName);
			}
		}
		
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outFile);
			container.getModel().write(out, format);
		} catch (FileNotFoundException e1) {
			throw new LicenseGeneratorException("Can not create RDF output file "+fileName);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					System.out.println("Warning - unable to close RDF output file "+fileName);
				}
			}
		}
	}
	/**
	 * @param version License list version
	 * @param releaseDate release date for the license list
	 * @param licenseProvider Provides the licensing information
	 * @param warnings Populated with any warnings if they occur
	 * @param dir Directory storing the main HTML files
	 * @param textFolder Directory holding the text only representation of the files
	 * @param htmlFolder Directory holding the HTML formated license text
	 * @param templateFolder Directory holding the template representation of the license text
	 * @param jsonFolder Folder containing only the JSON files
	 * @param rdfaFolder Folder containing the RDFa HTML files from the website
	 * @param modelContainer container to store all exceptions processed
	 * @param rdfXmlFolder Folder for the RDF XML files to be written
	 * @param rdfTurtleFolder Folder for the RDF Turtle files to be written
	 * @param rdfNtFolder Folder for the RDF NT files to be written
	 * @param markdown Markdown table to hold a markdown table of contents
	 * @param tester License tester used to test the results of licenses
	 * @throws IOException 
	 * @throws SpreadsheetException 
	 * @throws LicenseRestrictionException 
	 * @throws MustacheException 
	 * @throws InvalidSPDXAnalysisException 
	 * @throws LicenseGeneratorException 
	*/
	private static void writeExceptionList(String version,String releaseDate,
			ISpdxListedLicenseProvider licenseProvider,
			List<String> warnings, File dir, File textFolder,
			File htmlFolder, File templateFolder, File rdfaFolder, File jsonFolder,
			IModelContainer allLicensesContainer, File rdfXmlFolder, File rdfTurtleFolder, 
			File rdfNtFolder, MarkdownTable markdown, LicenseTester tester) throws IOException, LicenseRestrictionException, SpreadsheetException, MustacheException, InvalidSPDXAnalysisException, LicenseGeneratorException {
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
		ExceptionTOCJSONFile jsonToc = new ExceptionTOCJSONFile(version, releaseDate);
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
				String exceptionJsonFileName = exceptionHtmlFileName + ".json";
				String exceptionJSONReference= "./" + exceptionJsonFileName;
				File exceptionHtmlFile = new File(dir.getPath()+File.separator+exceptionHtmlFileName + ".html");
				exceptionHtml.writeToFile(exceptionHtmlFile, exceptionHtmlTocReference);
				File exceptionHtmlFileCopy = new File(rdfaFolder.getPath()+File.separator+exceptionHtmlFileName + ".html");
				exceptionHtml.writeToFile(exceptionHtmlFileCopy, exceptionHtmlTocReference);
				exceptionToc.addException(nextException, exceptionHTMLReference);
				jsonToc.addException(nextException, exceptionHTMLReference, exceptionJSONReference, false);
				
				File textFile = new File(textFolder.getPath() + File.separator + exceptionHtmlFileName + ".txt");
				Files.write(nextException.getLicenseExceptionText(), textFile, utf8);
				File htmlTextFile = new File(htmlFolder.getPath() + File.separator + exceptionHtmlFileName + ".html");
				Files.write(SpdxLicenseTemplateHelper.formatEscapeHTML(nextException.getLicenseExceptionText()), htmlTextFile, utf8);
				LicenseExceptionJSONFile exceptionJson = new LicenseExceptionJSONFile();
				exceptionJson.setException(nextException, false);
				File exceptionJsonFile = new File(dir.getPath() + File.separator + exceptionJsonFileName);
				exceptionJson.writeToFile(exceptionJsonFile);
				File exceptionJsonFileCopy = new File(jsonFolder.getPath() + File.separator + "exceptions" + File.separator +  exceptionJsonFileName);
				exceptionJson.writeToFile(exceptionJsonFileCopy);
				LicenseException exceptionClone = nextException.clone();
				LicenseContainer onlyThisException = new LicenseContainer();
				exceptionClone.createResource(onlyThisException);
				writeRdf(allLicensesContainer, rdfXmlFolder, rdfTurtleFolder, rdfNtFolder, exceptionHtmlFileName);
				markdown.addException(nextException, false);
				if (tester != null) {
					List<String> testResults = tester.testException(nextException);
					if (testResults != null && testResults.size() > 0) {
						for (String testResult:testResults) {
							warnings.add("Test for exception "+nextException.getLicenseExceptionId() + " failed: "+testResult);
						}
					}
				}
				nextException.createResource(allLicensesContainer);	
			}
		}
		File exceptionTocFile = new File(dir.getPath()+File.separator+EXCEPTION_TOC_FILE_NAME);
		exceptionToc.writeToFile(exceptionTocFile, version);
		File exceptionTocFileCopy = new File(rdfaFolder.getPath()+File.separator+EXCEPTION_TOC_FILE_NAME);
		exceptionToc.writeToFile(exceptionTocFileCopy, version);
		File exceptionJsonTocFile = new File(dir.getPath()+File.separator+EXCEPTION_JSON_TOC_FILE_NAME);
		jsonToc.writeToFile(exceptionJsonTocFile);
		File exceptionJsonTocFileCopy = new File(jsonFolder.getPath()+File.separator+EXCEPTION_JSON_TOC_FILE_NAME);
		jsonToc.writeToFile(exceptionJsonTocFileCopy);
	}
	/**
	 * Check text for invalid characters
	 * @param text Text to check
	 * @param textDescription Description of the text being check (this will be used to form warning messages)
	 * @param warnings Array list of warnings to add to if an problem is found with the text
	 */
	private static void checkText(String text, String textDescription,
			List<String> warnings) {
		BufferedReader reader = new BufferedReader(new StringReader(text));
		try {
			int lineNumber = 1;
			String line = reader.readLine();
			while (line != null) {
				for (int i = 0; i < line.length(); i++) {
					if (INVALID_TEXT_CHARS.contains(line.charAt(i))) {
						warnings.add("Invalid character in " + textDescription +
								" at line number " + String.valueOf(lineNumber) + 
								" \"" +line + "\" at character location "+String.valueOf(i));
					}
				}
				lineNumber++;
				line = reader.readLine();
			}
		} catch (IOException e) {
			warnings.add("IO error reading text");
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				warnings.add("IO Error closing string reader");
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
	 * @param jsonFolder Folder with only the JSON output files
	 * @param rdfaFolder Folder containing all the HTML files copied to the website
	 * @param container Container to store the licenses
	 * @param rdfXmlFolder Folder for the RDF XML files to be written
	 * @param rdfTurtleFolder Folder for the RDF Turtle files to be written
	 * @param rdfNtFolder Folder for the RDF NT files to be written
	 * @param markdown Markdown table to hold a markdown table of contents
	 * @param tester license tester to test the results of each license added
	 * @throws SpdxListedLicenseException 
	 * @throws IOException 
	 * @throws LicenseTemplateRuleException 
	 * @throws MustacheException 
	 * @throws InvalidSPDXAnalysisException 
	 * @throws LicenseGeneratorException 
	 * @throws SpdxCompareException 
	 * 
	 */
	private static void writeLicenseList(String version, String releaseDate,
			ISpdxListedLicenseProvider licenseProvider, List<String> warnings,
			File dir, File textFolder, File htmlFolder, File templateFolder, 
			File rdfaFolder, File jsonFolder, IModelContainer container,
			File rdfXmlFolder, File rdfTurtleFolder, File rdfNtFolder,
			MarkdownTable markdown, LicenseTester tester) throws SpdxListedLicenseException, IOException, InvalidLicenseTemplateException, MustacheException, InvalidSPDXAnalysisException, LicenseGeneratorException, SpdxCompareException {
		Charset utf8 = Charset.forName("UTF-8");
		LicenseHTMLFile licHtml = new LicenseHTMLFile();
		LicenseJSONFile licJson = new LicenseJSONFile();
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
				licJson.setLicense(license, false);
				String licBaseHtmlFileName = formLicenseHTMLFileName(license.getLicenseId());
				String licHtmlFileName = licBaseHtmlFileName + ".html";
				String licJsonFileName = licBaseHtmlFileName + ".json";
				String licHTMLReference = "./"+licHtmlFileName;
				String licJSONReference = "./"+licJsonFileName;
				String tocHTMLReference = "./"+LICENSE_TOC_HTML_FILE_NAME;
				// the base file is used for direct references from tools, the html is used for rendering by the website
				File licBaseHtmlFile = new File(dir.getPath()+File.separator+licBaseHtmlFileName);
				File licJsonFile = new File(dir.getPath()+File.separator+licJsonFileName);
				File licJsonFileCopy = new File(jsonFolder.getPath()+File.separator+"details"+File.separator+licJsonFileName);
				licHtml.writeToFile(licBaseHtmlFile, tocHTMLReference);
				File licHtmlFile = new File(dir.getPath()+File.separator+licHtmlFileName);
				File licHtmlFileCopy = new File(rdfaFolder.getPath()+File.separator+licHtmlFileName);
				licHtml.writeToFile(licHtmlFile, tocHTMLReference);
				licHtml.writeToFile(licHtmlFileCopy, tocHTMLReference);
				licJson.writeToFile(licJsonFile);
				licJson.writeToFile(licJsonFileCopy);
				tableOfContentsJSON.addLicense(license, licHTMLReference, licJSONReference, false);
				tableOfContentsHTML.addLicense(license, licHTMLReference);
				File textFile = new File(textFolder.getPath() + File.separator + licBaseHtmlFileName + ".txt");
				Files.write(license.getLicenseText(), textFile, utf8);
				if (license.getStandardLicenseTemplate() != null && !license.getStandardLicenseTemplate().trim().isEmpty()) {
					File templateFile = new File(templateFolder.getPath() + File.separator + licBaseHtmlFileName + ".template.txt");
					Files.write(license.getStandardLicenseTemplate(), templateFile, utf8);
				}
				File htmlTextFile = new File(htmlFolder.getPath() + File.separator + licHtmlFileName);
				Files.write(SpdxLicenseTemplateHelper.formatEscapeHTML(license.getLicenseText()), htmlTextFile, utf8);
				AnyLicenseInfo licenseClone = license.clone();
				LicenseContainer onlyThisLicense = new LicenseContainer();
				licenseClone.createResource(onlyThisLicense);
				writeRdf(onlyThisLicense, rdfXmlFolder, rdfTurtleFolder, rdfNtFolder, licHtmlFileName);
				markdown.addLicense(license, false);
				license.createResource(container);
				if (tester != null) {
					List<String> testResults = tester.testLicense(license);
					if (testResults != null && testResults.size() > 0) {
						for (String testResult:testResults) {
							warnings.add("Test for license "+license.getLicenseId() + " failed: "+testResult);
						}
					}
				}
			}
		}
		Iterator<DeprecatedLicenseInfo> depIter = licenseProvider.getDeprecatedLicenseIterator();
		while (depIter.hasNext()) {
			DeprecatedLicenseInfo deprecatedLicense = depIter.next();
			licHtml.setLicense(deprecatedLicense.getLicense());
			licHtml.setDeprecated(true);
			licHtml.setDeprecatedVersion(deprecatedLicense.getDeprecatedVersion());
			licJson.setLicense(deprecatedLicense.getLicense(), true);
			String licBaseHtmlFileName = formLicenseHTMLFileName(deprecatedLicense.getLicense().getLicenseId());
			String licHtmlFileName = licBaseHtmlFileName + ".html";
			String licHTMLReference = "./"+licHtmlFileName;
			String tocHTMLReference = "./"+LICENSE_TOC_HTML_FILE_NAME;
			// the base file is used for direct references from tools, the html is used for rendering by the website
			File licBaseHtmlFile = new File(dir.getPath()+File.separator+licBaseHtmlFileName);
			File licHtmlFile = new File(dir.getPath()+File.separator+licHtmlFileName);
			File licHtmlFileCopy = new File(rdfaFolder.getPath()+File.separator+licHtmlFileName);
			String licJsonFileName = licBaseHtmlFileName + ".json";
			String licJSONReference = "./"+licJsonFileName;
			licHtml.writeToFile(licBaseHtmlFile, tocHTMLReference);
			licHtml.writeToFile(licHtmlFile, tocHTMLReference);
			licHtml.writeToFile(licHtmlFileCopy, tocHTMLReference);
			File licJsonFile = new File(dir.getPath()+File.separator+licJsonFileName);
			File licJsonFileCopy = new File(jsonFolder.getPath()+File.separator+"details"+File.separator+licJsonFileName);
			licJson.writeToFile(licJsonFile);
			licJson.writeToFile(licJsonFileCopy);
			tableOfContentsHTML.addDeprecatedLicense(deprecatedLicense, licHTMLReference);
			tableOfContentsJSON.addLicense(deprecatedLicense.getLicense(), licHTMLReference, licJSONReference, true);
			File textFile = new File(textFolder.getPath() + File.separator + "depreciate_" + licBaseHtmlFileName + ".txt");
			Files.write(deprecatedLicense.getLicense().getLicenseText(), textFile, utf8);
			if (deprecatedLicense.getLicense().getStandardLicenseTemplate() != null && !deprecatedLicense.getLicense().getStandardLicenseTemplate().trim().isEmpty()) {
				File templateFile = new File(templateFolder.getPath() + File.separator + "depreciate_" + licBaseHtmlFileName + ".template.txt");
				Files.write(deprecatedLicense.getLicense().getStandardLicenseTemplate(), templateFile, utf8);
			}
			File htmlTextFile = new File(htmlFolder.getPath() + File.separator + licHtmlFileName);
			Files.write(SpdxLicenseTemplateHelper.formatEscapeHTML(deprecatedLicense.getLicense().getLicenseText()), htmlTextFile, utf8);
			markdown.addLicense(deprecatedLicense.getLicense(), true);
			if (tester != null) {
				List<String> testResults = tester.testLicense(deprecatedLicense.getLicense());
				if (testResults != null && testResults.size() > 0) {
					for (String testResult:testResults) {
						warnings.add("Test for license "+deprecatedLicense.getLicense().getLicenseId() + " failed: "+testResult);
					}
				}
			}
		}
		File tocJsonFile = new File(dir.getPath()+File.separator+LICENSE_TOC_JSON_FILE_NAME);
		File tocHtmlFile = new File(dir.getPath()+File.separator+LICENSE_TOC_HTML_FILE_NAME);
		tableOfContentsJSON.writeToFile(tocJsonFile);
		tableOfContentsHTML.writeToFile(tocHtmlFile);
		File tocJsonFileCopy = new File(jsonFolder.getPath()+File.separator+LICENSE_TOC_JSON_FILE_NAME);
		File tocHtmlFileCopy = new File(rdfaFolder.getPath()+File.separator+LICENSE_TOC_HTML_FILE_NAME);
		tableOfContentsJSON.writeToFile(tocJsonFileCopy);
		tableOfContentsHTML.writeToFile(tocHtmlFileCopy);
	}
	
	/**
	 * Copy a file from the resources directory to a destination file
	 * @param resourceFileName filename of the file in the resources directory
	 * @param destination target file - warning, this will be overwritten
	 * @throws IOException 
	 */
	private static void copyResourceFile(String resourceFileName, File destination) throws IOException {
		File resourceFile = new File(resourceFileName);
		if (resourceFile.exists()) {
			Files.copy(resourceFile, destination);
		} else {
			InputStream is = LicenseRDFAGenerator.class.getClassLoader().getResourceAsStream(resourceFileName);
			InputStreamReader reader = new InputStreamReader(is);
			FileWriter writer = new FileWriter(destination);
			try {
				char[] buf = new char[2048];
				int len = reader.read(buf);
				while (len > 0) {
					writer.write(buf, 0, len);
					len = reader.read(buf);
				}
			} finally {
				if (writer != null) {
					writer.close();
				}
				reader.close();
			}
		}
	}

	private static void writeCssFile(File dir) throws IOException {
		File cssFile = new File(dir.getPath()+ File.separator + CSS_FILE_NAME);
		if (cssFile.exists()) {
			if (!cssFile.delete()) {
				throw(new IOException("Unable to delete old file"));
			}
		}
		copyResourceFile(CSS_TEMPLATE_FILE, cssFile);
	}
	
	private static void writeSortTableFile(File dir) throws IOException {
		File sortTableFile = new File(dir.getPath()+ File.separator + SORTTABLE_FILE_NAME);
		if (sortTableFile.exists()) {
			return;	// assume we don't need to create it
		}
		copyResourceFile(SORTTABLE_JS_FILE, sortTableFile);
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
