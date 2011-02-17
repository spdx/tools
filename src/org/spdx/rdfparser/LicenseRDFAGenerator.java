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
import java.util.HashSet;
import java.util.Iterator;

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
		INVALID_FILE_CHARS.add(']'); INVALID_FILE_CHARS.add('='); INVALID_FILE_CHARS.add('+');
		INVALID_FILE_CHARS.add(';'); INVALID_FILE_CHARS.add(':');
		INVALID_FILE_CHARS.add('\''); INVALID_FILE_CHARS.add('"'); INVALID_FILE_CHARS.add('|');
		INVALID_FILE_CHARS.add('\t'); INVALID_FILE_CHARS.add('?'); INVALID_FILE_CHARS.add('&');
		INVALID_FILE_CHARS.add('³');
	}
	static int MIN_ARGS = 2;
	static int MAX_ARGS = 2;
	
	static final String CSS_FILE_TEXT = "body { font-family: Tahoma, Verdana, sans-serif; }\n\n.license-text {\n"+
		"background-color: WhiteSmoke;\nborder: 1px dashed Black;\npadding: 1ex;\n}\n\nh2 {\n"+
		"border-bottom: 1px solid Gray;\n}\ndt {\nfont-weight: bold;\n}\n\nul {\n"+
		"padding-left: 1em;\n}\n\ntable {\nborder-collapse: collapse;\n}\n"+
		"td,th {\nmargin: 0;\nborder: 1px solid black;\n}\n";
	static final String CSS_FILE_NAME = "screen.css";
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
		SPDXLicenseSpreadsheet ss = null;
		try {
			ss = new SPDXLicenseSpreadsheet(ssFile, false, true);
			LicenseHTMLFile licHtml = new LicenseHTMLFile();
			LicenseTOCHTMLFile tableOfContents = new LicenseTOCHTMLFile();
			Iterator<SPDXLicense> iter = ss.getIterator();
			String tocFileName = "index.html";
			while (iter.hasNext()) {
				SPDXLicense license = iter.next();
				System.out.println("Processing "+license.toString());
				licHtml.setLicense(license);
				String licHtmlFileName = formLicenseHTMLFileName(license);
				String licHTMLReference = "./"+licHtmlFileName;
				String tocHTMLReference = "./"+tocFileName;
				File licHtmlFile = new File(dir.getPath()+File.separator+licHtmlFileName);
				licHtml.writeToFile(licHtmlFile, tocHTMLReference);
				tableOfContents.addLicense(license, licHTMLReference);
			}
			File tocHtmlFile = new File(dir.getPath()+File.separator+tocFileName);
			tableOfContents.writeToFile(tocHtmlFile);
			writeCssFile(dir);
		} catch (SpreadsheetException e) {
			System.out.println("Invalid spreadsheet: "+e.getMessage());
		} catch (Exception e) {
			System.out.println("Unhandled exception generating html:");
			e.printStackTrace();
		} finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (SpreadsheetException e) {
					System.out.println("Error closing spreadsheet file: "+e.getMessage());
				}
			}
		}
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
	private static String formLicenseHTMLFileName(SPDXLicense license) {
		StringBuilder sb = new StringBuilder();
		String licId = license.getId();
		for (int i = 0; i < licId.length(); i++) {
			if (INVALID_FILE_CHARS.contains(licId.charAt(i))) {
				sb.append("_");
			} else {
				sb.append(licId.charAt(i));
			}
		}
		sb.append(".html");
		return sb.toString();
	}
	private static void usage() {
		System.out.println("Usage:");
		System.out.println("LicenseRDFAGenerator licenseSpreadsheet.xls, outputDirectory");
	}

}
