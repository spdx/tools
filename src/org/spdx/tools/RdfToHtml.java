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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import org.spdx.html.MustacheMap;
import org.spdx.html.PackageContext;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxItem;
import org.spdx.rdfparser.model.SpdxPackage;
import org.spdx.rdfparser.model.SpdxSnippet;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Takes an input SPDX Document and produces the following HTML files in the specified directory:
 *    [spdxdocumentname]-document.html - Document level information
 *    [spdxpackagename]-package.html - Package level information (one per package found)
 *    [spdxpackagename]-packagefiles.html - File level information for all files found in the given package
 *    [spdxdocumentname]-packagefiles.html - File level information for all files described directly by the document
 *    [spdxdocumentname]-extractedlicenses.html - Extracted license information from the document
 * The HTML pretty printer is based on a set of HTML templates found in the
 * resources directory:
 *   SpdxDocHTMLTemplate.html - Document level HTML file template
 *   PackageHTMLTemplate.html - Package level HTML file template
 *   FilesHTMLTemplate.html - File level information HTML file template
 *   LicensesHTMLTemplate.html - Extracted license information HTML file template
 *   
 * The template uses Mustache - see http://mustache.github.com/mustache.5.html
 * @author Gary O'Neall
 *
 */
public class RdfToHtml {
	static final String TEMPLATE_CLASS_PATH = "resources" + "/" + "htmlTemplate";
	static final String TEMPLATE_ROOT_PATH = "resources" + File.separator + "htmlTemplate";
	static final String SPDX_DOCUMENT_HTML_TEMPLATE = "SpdxDocHTMLTemplate.html";
	static final String SPDX_PACKAGE_HTML_TEMPLATE = "PackageHTMLTemplate.html";
	static final String SPDX_FILE_HTML_TEMPLATE = "FilesHTMLTemplate.html";
	static final String SPDX_SNIPPET_HTML_TEMPLATE = "SnippetHTMLTemplate.html";
	static final String SPDX_LICENSE_HTML_TEMPLATE = "LicensesHTMLTemplate.html";
	
	static final String DOC_HTML_FILE_POSTFIX = "-document.html";
	static final String LICENSE_HTML_FILE_POSTFIX = "-extractedlicenses.html";
	static final String PACKAGE_HTML_FILE_POSTFIX = "-package.html";
	static final String PACKAGE_FILE_HTML_FILE_POSTFIX = "-packagefiles.html";
	static final String DOCUMENT_FILE_HTML_FILE_POSTFIX = "-documentfiles.html";
	static final String SNIPPET_FILE_NAME = "snippets.html";

	private static final Escaper HTML_LINEBREAK_ESCAPER = //A variation on Guava's HTML escaper
			Escapers.builder()
					.addEscape('"', "&quot;")
							// Note: "&apos;" is not defined in HTML 4.01.
					.addEscape('\'', "&#39;")
					.addEscape('&', "&amp;")
					.addEscape('<', "&lt;")
					.addEscape('>', "&gt;")
					.addEscape('\n', "<br />")
					.build();
	
	static final int MIN_ARGS = 2;
	static final int MAX_ARGS = 2;
	static final int ERROR = 1;
	/**
	 * @param args Argument 0 is the file path of RDF XML file; argument 1 is the file path for the output HTML file(s)
	 */
	public static void main(String[] args) {
		if (args.length < MIN_ARGS) {
			usage();
			System.exit(ERROR);
		}
		if (args.length > MAX_ARGS) {
			System.out.println("Warning: Extra arguments will be ignored");
			usage();
		}	
		try {
			onlineFunction(args);
		} catch (OnlineToolException e){
			System.out.println(e.getMessage());
			System.exit(ERROR);
		}
	}
	
	/**
	 * 
	 * @param args args[0] is the RDF file to be converted, args[1] is the result HTML file name
	 * @throws OnlineToolException Exception caught by JPype and displayed to the user
	 * 
	 */
	public static List<String> onlineFunction(String[] args) throws OnlineToolException{
		// Arguments length(args length== 2 ) will checked in the Python Code
		File spdxFile = new File(args[0]);
		// Output File name will be checked in the Python code for no clash, but if still found
		if (!spdxFile.exists()) {
			throw new OnlineToolException("SPDX file " + args[0] +" does not exists.");
		}
		if (!spdxFile.canRead()) {
			throw new OnlineToolException("Can not read SPDX File "+args[0]+".  Check permissions on the file.");
		}
		File outputDirectory = new File(args[1]);
		if (!outputDirectory.exists()) {
			if (!outputDirectory.mkdirs()) {
				throw new OnlineToolException("Unable to create output directory");
			}
		}
		SpdxDocument doc = null;
		try {
			doc = SPDXDocumentFactory.createSpdxDocument(args[0]);
		} catch (IOException e2) {
			throw new OnlineToolException("IO Error creating the SPDX document");
		} catch (InvalidSPDXAnalysisException e2) {
			throw new OnlineToolException("Invalid SPDX Document: "+e2.getMessage());
		} catch (Exception e) {
			throw new OnlineToolException("Error creating SPDX Document: "+e.getMessage(),e);
		}
		List<String> verify = new ArrayList<String>();
		verify = doc.verify();
	    if (verify != null && verify.size() > 0) {
	         System.out.println("Warning: The following verifications failed for the resultant SPDX RDF file:");
	         for (int i = 0; i < verify.size(); i++) {
	             System.out.println("\t" + verify.get(i));
	         }
	    }
		String documentName = doc.getName();
		List<File> filesToCreate = Lists.newArrayList();
		String docHtmlFilePath = outputDirectory.getPath() + File.separator + documentName + DOC_HTML_FILE_POSTFIX;
		File docHtmlFile = new File(docHtmlFilePath);
		filesToCreate.add(docHtmlFile);
		String snippetHtmlFilePath = outputDirectory.getPath() + File.separator + SNIPPET_FILE_NAME;
		File snippetHtmlFile = new File(snippetHtmlFilePath);
		filesToCreate.add(snippetHtmlFile);
		String licenseHtmlFilePath = outputDirectory.getPath() + File.separator + documentName + LICENSE_HTML_FILE_POSTFIX;
		File licenseHtmlFile = new File(licenseHtmlFilePath);
		filesToCreate.add(licenseHtmlFile);
		String docFilesHtmlFilePath = outputDirectory.getPath() + File.separator + documentName + DOCUMENT_FILE_HTML_FILE_POSTFIX;
		File docFilesHtmlFile = new File(docFilesHtmlFilePath);
		filesToCreate.add(docFilesHtmlFile);
		List<SpdxPackage> pkgs = null;
		try {
			pkgs = doc.getDocumentContainer().findAllPackages();
		} catch (InvalidSPDXAnalysisException e1) {
			throw new OnlineToolException("Error getting packages from the SPDX document: "+e1.getMessage());
		}
		Iterator<SpdxPackage> iter = pkgs.iterator();
		while (iter.hasNext()) {
			String packageName = iter.next().getName();
			String packageHtmlFilePath = outputDirectory.getPath() + File.separator + packageName + 
					PACKAGE_HTML_FILE_POSTFIX;
			File packageHtmlFile = new File(packageHtmlFilePath);
			filesToCreate.add(packageHtmlFile);
			String packageFilesHtmlFilePath = outputDirectory.getPath() + File.separator + packageName + 
					PACKAGE_FILE_HTML_FILE_POSTFIX;
			File packageFilesHtmlFile = new File(packageFilesHtmlFilePath);
			filesToCreate.add(packageFilesHtmlFile);
		}
		Iterator<File> fileIter = filesToCreate.iterator();
		while (fileIter.hasNext()) {
			File file = fileIter.next();
			if (file.exists()) {
				throw new OnlineToolException("File "+file.getName()+" already exists.");
			}
		}
		Writer writer = null;
		try {
			rdfToHtml(doc, docHtmlFile, licenseHtmlFile, snippetHtmlFile, docFilesHtmlFile);
		} catch (IOException e) {
			throw new OnlineToolException("IO Error opening SPDX Document");
		} catch (InvalidSPDXAnalysisException e) {
			throw new OnlineToolException("Invalid SPDX Document: "+e.getMessage());
		} catch (MustacheException e) {
			throw new OnlineToolException("Unexpected error reading the HTML template: "+e.getMessage());
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					throw new OnlineToolException("Warning: error closing HTML file: "+e.getMessage());
				}
				writer = null;
			}
		}
		return verify;
	}
	
	/**
	 * Display usage information to console
	 */
	private static void usage() {
		System.out
		.println("Usage: RdfToHtml rdfxmlfile.rdf htmlfile.html\n"
				+ "where rdfxmlfile.rdf is a valid SPDX RDF XML file and htmlfile.html is\n"
				+ "the output html file.");
	}

    public static void rdfToHtml(SpdxDocument doc, File templateDir,
    		File docHtmlFile, File licenseHtmlFile, File snippetHtmlFile,
    		File docFilesHtmlFile) throws MustacheException, IOException, InvalidSPDXAnalysisException {
    	String dirPath = docHtmlFile.getParent();
		DefaultMustacheFactory lineBreakEscapingBuilder = new DefaultMustacheFactory(templateDir){
			@Override
			public void encode(String value, Writer writer) {
				try{
					String escapedValue = HTML_LINEBREAK_ESCAPER.escape(value);
					writer.append(escapedValue);
				} catch (IOException ioe) { //Mimic the super's behavior
					throw new MustacheException("Failed to encode value: " + value);
				}

			}
		};
        Map<String, String> spdxIdToUrl = buildIdMap(doc, dirPath);
        Map<String, List<SpdxSnippet>> fileIdToSnippets = Maps.newHashMap();
        List<SpdxSnippet> snippets = doc.getDocumentContainer().findAllSnippets();
        for (SpdxSnippet snippet:snippets) {
        	if (snippet.getId() != null && snippet.getSnippetFromFile() != null && snippet.getSnippetFromFile().getId() != null) {
        		List<SpdxSnippet> fileSnippets = fileIdToSnippets.get(snippet.getId());
        		if (fileSnippets == null) {
        			fileSnippets = Lists.newArrayList();
        			fileIdToSnippets.put(snippet.getSnippetFromFile().getId(), fileSnippets);
        		}
        		fileSnippets.add(snippet);
        	}
        }
        List<SpdxPackage> allPackages = doc.getDocumentContainer().findAllPackages();
        Iterator<SpdxPackage> pkgIter = allPackages.iterator();
        while (pkgIter.hasNext()) {
        	SpdxPackage pkg = pkgIter.next();
        	String packageName = pkg.getName();
        	String packageHtmlFilePath = dirPath + File.separator + packageName + 
					PACKAGE_HTML_FILE_POSTFIX;
			File packageHtmlFile = new File(packageHtmlFilePath);
			String packageFilesHtmlFilePath = dirPath + File.separator + packageName + 
					PACKAGE_FILE_HTML_FILE_POSTFIX;
			File packageFilesHtmlFile = new File(packageFilesHtmlFilePath);
        	PackageContext pkgContext = new PackageContext(pkg, spdxIdToUrl);
        	Map<String, Object> pkgFileMap = MustacheMap.buildPkgFileMap(pkg, spdxIdToUrl,
        			fileIdToSnippets);
        	Mustache mustache = lineBreakEscapingBuilder.compile(SPDX_PACKAGE_HTML_TEMPLATE);
			OutputStreamWriter packageHtmlFileWriter = new OutputStreamWriter(new FileOutputStream(packageHtmlFile), "UTF-8");
        	try {
        		mustache.execute(packageHtmlFileWriter, pkgContext);
        	} finally {
        		packageHtmlFileWriter.close();
        	}
        	mustache = lineBreakEscapingBuilder.compile(SPDX_FILE_HTML_TEMPLATE);
			OutputStreamWriter filesHtmlFileWriter = new OutputStreamWriter(new FileOutputStream(packageFilesHtmlFile), "UTF-8");
        	try {
        		mustache.execute(filesHtmlFileWriter, pkgFileMap);
        	} finally {
        		filesHtmlFileWriter.close();
        	}
        }
        SpdxItem[] describedItems = doc.getDocumentDescribes();
        int numFiles = 0;
        // collect just the files
        for (int i = 0; i < describedItems.length; i++) {
        	if (describedItems[i] instanceof SpdxFile) {
        		numFiles++;
        	}
        }
        if (numFiles > 0) {
        	SpdxFile[] files = new SpdxFile[numFiles];
        	int fi = 0;
        	for (int i = 0; i < describedItems.length; i++) {
        		if (describedItems[i] instanceof SpdxFile) {
        			files[fi++] = (SpdxFile)describedItems[i];
        		}
        	}
        	Map<String, Object> docFileMap = MustacheMap.buildDocFileMustacheMap(
        			doc, files, spdxIdToUrl, fileIdToSnippets);
        	Mustache mustache = lineBreakEscapingBuilder.compile(SPDX_FILE_HTML_TEMPLATE);
        	OutputStreamWriter docFilesHtmlFileWriter = new OutputStreamWriter(new FileOutputStream(docFilesHtmlFile), "UTF-8");
        	try {
        		mustache.execute(docFilesHtmlFileWriter, docFileMap);
        	} finally {
        		docFilesHtmlFileWriter.close();
        	}
        }
        Map<String, Object> extracteLicMustacheMap = MustacheMap.buildExtractedLicMustachMap(doc, spdxIdToUrl);
        Mustache mustache = lineBreakEscapingBuilder.compile(SPDX_LICENSE_HTML_TEMPLATE);
        OutputStreamWriter licenseHtmlFileWriter = new OutputStreamWriter(new FileOutputStream(licenseHtmlFile), "UTF-8");
        try {
        	mustache.execute(licenseHtmlFileWriter, extracteLicMustacheMap);
        } finally {
        	licenseHtmlFileWriter.close();
        }
        Map<String, Object> snippetMustacheMap = MustacheMap.buildSnippetMustachMap(doc, spdxIdToUrl);
        mustache = lineBreakEscapingBuilder.compile(SPDX_SNIPPET_HTML_TEMPLATE);
        OutputStreamWriter snippetHtmlFileWriter = new OutputStreamWriter(new FileOutputStream(snippetHtmlFile), "UTF-8");
        try {
        	mustache.execute(snippetHtmlFileWriter, snippetMustacheMap);
        } finally {
        	snippetHtmlFileWriter.close();
        }
        Map<String, Object> docMustacheMap = MustacheMap.buildDocMustachMap(doc, spdxIdToUrl);
        mustache = lineBreakEscapingBuilder.compile(SPDX_DOCUMENT_HTML_TEMPLATE);
        OutputStreamWriter docHtmlFileWriter = new OutputStreamWriter(new FileOutputStream(docHtmlFile), "UTF-8");
        try {
        	mustache.execute(docHtmlFileWriter, docMustacheMap);
        } finally {
        	docHtmlFileWriter.close();
        }
    }


	/**
	 * Builds a map of SPDX Ids and License IDs to URL's used in the document
	 * @param doc
	 * @param dirPath
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static Map<String, String> buildIdMap(SpdxDocument doc, String dirPath) throws InvalidSPDXAnalysisException {
		// URLs are all relative and use the ID as the part
		Map<String, String> retval = Maps.newHashMap();
		// extracted license IDs
		String extractedLicenseFileName = doc.getName() + LICENSE_HTML_FILE_POSTFIX;
		ExtractedLicenseInfo[] extractedLicenses = doc.getDocumentContainer().getExtractedLicenseInfos();
		for (int i = 0; i < extractedLicenses.length; i++) {
			String id = extractedLicenses[i].getLicenseId();
			retval.put(id, convertToUrl(extractedLicenseFileName, id));
		}
		// package files
		List<SpdxPackage> allPkgs = doc.getDocumentContainer().findAllPackages();
		Iterator<SpdxPackage> pkgIter = allPkgs.iterator();
		while (pkgIter.hasNext()) {
			SpdxPackage pkg = pkgIter.next();
			String pkgFileName = pkg.getName() + PACKAGE_HTML_FILE_POSTFIX;
			String pkgFilesFileName = pkg.getName() + PACKAGE_FILE_HTML_FILE_POSTFIX;
			String pkgId = pkg.getId();
			if (pkgId != null) {
				retval.put(pkgId, convertToUrl(pkgFileName, pkgId));
			}
			SpdxFile[] pkgFiles = pkg.getFiles();
			for (int i = 0; i < pkgFiles.length; i++) {
				String fileId = pkgFiles[i].getId();
				if (fileId != null) {
					retval.put(fileId, convertToUrl(pkgFilesFileName, fileId));
				}
			}
		}
		// Snippets
		List<SpdxSnippet> snippets = doc.getDocumentContainer().findAllSnippets();
		for (SpdxSnippet snippet:snippets) {
			String snippetId = snippet.getId();
			if (snippetId != null) {
				retval.put(snippetId, SNIPPET_FILE_NAME);
			}
		}
		// finally, add the document files
		String docFilesName = doc.getName() + DOCUMENT_FILE_HTML_FILE_POSTFIX;
		SpdxItem[] describedItems = doc.getDocumentDescribes();
		for (int i = 0; i < describedItems.length; i++) {
			if (describedItems[i] instanceof SpdxFile) {
				String fileId = describedItems[i].getId();
				if (fileId != null) {
					retval.put(fileId, convertToUrl(docFilesName, fileId));
				}
			}
		}
		return retval;
	}
	
	private static String convertToUrl(String fileName, String id) {
//		int htmlPos = fileName.lastIndexOf(".html");
//		String retval;
//		if (htmlPos > 0) {
//			retval = "./" + fileName.substring(0, htmlPos);
//		} else {
//			retval = "./" + fileName;
//		}
		String retval = "./" + fileName + "#" + id;
		return retval;
	}

	public static void rdfToHtml(SpdxDocument doc, File docHtmlFile, 
			File licenseHtmlFile, File snippetHtmlFile, File docFilesHtmlFile) throws MustacheException, IOException, InvalidSPDXAnalysisException {
		String templateDir = TEMPLATE_ROOT_PATH;
		File templateDirectoryRoot = new File(templateDir);
		if (!(templateDirectoryRoot.exists() && templateDirectoryRoot.isDirectory())) {
			templateDir = TEMPLATE_CLASS_PATH;
			templateDirectoryRoot = new File(templateDir);
		}
		rdfToHtml(doc, templateDirectoryRoot, docHtmlFile, licenseHtmlFile, snippetHtmlFile, docFilesHtmlFile);
	}

}
