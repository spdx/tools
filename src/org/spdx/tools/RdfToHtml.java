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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.spdx.html.MustacheMap;
import org.spdx.html.PackageContext;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxItem;
import org.spdx.rdfparser.model.SpdxPackage;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheException;

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
	static final String SPDX_LICENSE_HTML_TEMPLATE = "LicensesHTMLTemplate.html";
	
	static final String DOC_HTML_FILE_POSTFIX = "-document.html";
	static final String LICENSE_HTML_FILE_POSTFIX = "-extractedlicenses.html";
	static final String PACKAGE_HTML_FILE_POSTFIX = "-package.html";
	static final String PACKAGE_FILE_HTML_FILE_POSTFIX = "-packagefiles.html";
	static final String DOCUMENT_FILE_HTML_FILE_POSTFIX = "-documentfiles.html";
	
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
		File spdxFile = new File(args[0]);
		if (!spdxFile.exists()) {
			System.out.println("SPDX File "+args[0]+" does not exist.");
			usage();
			System.exit(ERROR);
		}
		if (!spdxFile.canRead()) {
			System.out.println("Can not read SPDX File "+args[0]+".  Check permissions on the file.");
			usage();
			System.exit(ERROR);
		}
		File outputDirectory = new File(args[1]);
		if (!outputDirectory.exists()) {
			if (!outputDirectory.mkdirs()) {
				System.out.println("Unable to create output directory");
				System.exit(ERROR);
			}
		}
		SpdxDocument doc = null;
		try {
			doc = SPDXDocumentFactory.createSpdxDocument(args[0]);
		} catch (IOException e2) {
			System.out.println("IO Error creating the SPDX document");
			System.exit(ERROR);
		} catch (InvalidSPDXAnalysisException e2) {
			System.out.println("Invalid SPDX Document: "+e2.getMessage());
			System.exit(ERROR);
		}
		String documentName = doc.getName();
		ArrayList<File> filesToCreate = new ArrayList<File>();
		String docHtmlFilePath = outputDirectory.getPath() + File.separator + documentName + DOC_HTML_FILE_POSTFIX;
		File docHtmlFile = new File(docHtmlFilePath);
		filesToCreate.add(docHtmlFile);
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
			System.out.println("Error getting packages from the SPDX document: "+e1.getMessage());
			System.exit(ERROR);
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
				System.out.println("File "+file.getName()+" already exists.");
				System.exit(ERROR);
			}
		}
		Writer writer = null;
		try {
			rdfToHtml(doc, docHtmlFile, licenseHtmlFile, docFilesHtmlFile);
		} catch (IOException e) {
			System.out.println("IO Error opening SPDX Document");
			usage();
			System.exit(ERROR);
		} catch (InvalidSPDXAnalysisException e) {
			System.out.println("Invalid SPDX Document: "+e.getMessage());
			usage();
			System.exit(ERROR);
		} catch (MustacheException e) {
			System.out.println("Unexpected error reading the HTML template: "+e.getMessage());
			usage();
			System.exit(ERROR);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					System.out.println("Warning: error closing HTML file: "+e.getMessage());
				}
				writer = null;
			}
		}
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
	
    public static void rdfToHtml(SpdxDocument doc, String templateDirName,
    		File docHtmlFile, File licenseHtmlFile, File docFilesHtmlFile) throws MustacheException, IOException, InvalidSPDXAnalysisException {
        String dirPath = docHtmlFile.getParent();
    	DefaultMustacheFactory builder = new DefaultMustacheFactory(templateDirName);
        HashMap<String, String> spdxIdToUrl = buildIdMap(doc, dirPath);
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
        	HashMap<String, Object> pkgFileMap = MustacheMap.buildPkgFileMap(pkg, spdxIdToUrl);
        	Mustache mustache = builder.compile(SPDX_PACKAGE_HTML_TEMPLATE);
        	FileWriter packageHtmlFileWriter = new FileWriter(packageHtmlFile);
        	try {
        		mustache.execute(packageHtmlFileWriter, pkgContext);
        	} finally {
        		packageHtmlFileWriter.close();
        	}
        	mustache = builder.compile(SPDX_FILE_HTML_TEMPLATE);
        	FileWriter filesHtmlFileWriter = new FileWriter(packageFilesHtmlFile);
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
        	HashMap<String, Object> docFileMap = MustacheMap.buildDocFileMustacheMap(
        			doc, files, spdxIdToUrl);
        	Mustache mustache = builder.compile(SPDX_FILE_HTML_TEMPLATE);
        	FileWriter docFilesHtmlFileWriter = new FileWriter(docFilesHtmlFile);
        	try {
        		mustache.execute(docFilesHtmlFileWriter, docFileMap);
        	} finally {
        		docFilesHtmlFileWriter.close();
        	}
        }
        HashMap<String, Object> extracteLicMustacheMap = MustacheMap.buildExtractedLicMustachMap(doc, spdxIdToUrl);
        Mustache mustache = builder.compile(SPDX_LICENSE_HTML_TEMPLATE);
        FileWriter licenseHtmlFileWriter = new FileWriter(licenseHtmlFile);
        try {
        	mustache.execute(licenseHtmlFileWriter, extracteLicMustacheMap);
        } finally {
        	licenseHtmlFileWriter.close();
        }
        HashMap<String, Object> docMustacheMap = MustacheMap.buildDocMustachMap(doc, spdxIdToUrl);
        mustache = builder.compile(SPDX_DOCUMENT_HTML_TEMPLATE);
        FileWriter docHtmlFileWriter = new FileWriter(docHtmlFile);
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
	private static HashMap<String, String> buildIdMap(SpdxDocument doc,
			String dirPath) throws InvalidSPDXAnalysisException {
		// URLs are all relative and use the ID as the part
		HashMap<String, String> retval = new HashMap<String, String>();
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
			File licenseHtmlFile, File docFilesHtmlFile) throws MustacheException, IOException, InvalidSPDXAnalysisException {
		String templateDir = TEMPLATE_ROOT_PATH;
		File templateDirectoryRoot = new File(templateDir);
		if (!(templateDirectoryRoot.exists() && templateDirectoryRoot.isDirectory())) {
			templateDir = TEMPLATE_CLASS_PATH;
		}
		rdfToHtml(doc, templateDir, docHtmlFile, licenseHtmlFile, docFilesHtmlFile);
	}

}
