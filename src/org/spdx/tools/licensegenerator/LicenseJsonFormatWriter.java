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
package org.spdx.tools.licensegenerator;

import java.io.File;
import java.io.IOException;

import org.spdx.html.ExceptionTOCJSONFile;
import org.spdx.html.LicenseExceptionJSONFile;
import org.spdx.html.LicenseJSONFile;
import org.spdx.html.LicenseTOCJSONFile;
import org.spdx.rdfparser.license.LicenseException;
import org.spdx.rdfparser.license.SpdxListedLicense;

/**
 * Writes JSON format license information
 * @author Gary O'Neall
 *
 */
public class LicenseJsonFormatWriter implements ILicenseFormatWriter {
	
	static final String LICENSE_TOC_JSON_FILE_NAME = "licenses.json";
	static final String EXCEPTION_JSON_TOC_FILE_NAME = "exceptions.json";

	private File jsonFolder;
	private File jsonFolderExceptions;
	private File jsonFolderDetails;
	LicenseJSONFile licJson;
	LicenseTOCJSONFile tableOfContentsJSON;
	ExceptionTOCJSONFile jsonExceptionToc;

	/**
	 * @param version License list version
	 * @param releaseDate release date for the license list
	 * @param jsonFolder Folder to output the main JSON file
	 * @param jsonFolderDetails Folder to output a detailed JSON file per license 
	 * @param jsonFolderExceptions Folder to output a detailed JSON file per exception
	 */
	public LicenseJsonFormatWriter(String version, String releaseDate,
			File jsonFolder, File jsonFolderDetails, File jsonFolderExceptions) {
		this.jsonFolder = jsonFolder;
		this.jsonFolderDetails = jsonFolderDetails;
		this.jsonFolderExceptions = jsonFolderExceptions;
		licJson = new LicenseJSONFile();
		tableOfContentsJSON = new LicenseTOCJSONFile(version, releaseDate);
		jsonExceptionToc = new ExceptionTOCJSONFile(version, releaseDate);
	}

	/**
	 * @return the jsonFolder
	 */
	public File getJsonFolder() {
		return jsonFolder;
	}

	/**
	 * @param jsonFolder the jsonFolder to set
	 */
	public void setJsonFolder(File jsonFolder) {
		this.jsonFolder = jsonFolder;
	}

	/**
	 * @return the jsonFolderExceptions
	 */
	public File getJsonFolderExceptions() {
		return jsonFolderExceptions;
	}

	/**
	 * @param jsonFolderExceptions the jsonFolderExceptions to set
	 */
	public void setJsonFolderExceptions(File jsonFolderExceptions) {
		this.jsonFolderExceptions = jsonFolderExceptions;
	}

	/**
	 * @return the jsonFolderDetails
	 */
	public File getJsonFolderDetails() {
		return jsonFolderDetails;
	}

	/**
	 * @param jsonFolderDetails the jsonFolderDetails to set
	 */
	public void setJsonFolderDetails(File jsonFolderDetails) {
		this.jsonFolderDetails = jsonFolderDetails;
	}

	@Override
	public void writeLicense(SpdxListedLicense license, boolean deprecated, String deprecatedVersion) throws IOException {
		licJson.setLicense(license, deprecated);
		String licBaseHtmlFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(license.getLicenseId());
		String licHtmlFileName = licBaseHtmlFileName + ".html";
		String licJsonFileName = licBaseHtmlFileName + ".json";
		String licHTMLReference = "./"+licHtmlFileName;
		String licJSONReference = "./"+licJsonFileName;
		File licJsonFile = new File(jsonFolder.getPath()+File.separator+"details"+File.separator+licJsonFileName);
		licJson.writeToFile(licJsonFile);
		tableOfContentsJSON.addLicense(license, licHTMLReference, licJSONReference, deprecated);
	}

	/* (non-Javadoc)
	 * @see org.spdx.tools.licensegenerator.ILicenseFormatWriter#writeToC()
	 */
	@Override
	public void writeToC() throws IOException {
		File tocJsonFile = new File(jsonFolder.getPath()+File.separator+LICENSE_TOC_JSON_FILE_NAME);
		tableOfContentsJSON.writeToFile(tocJsonFile);
		File exceptionJsonTocFile = new File(jsonFolder.getPath()+File.separator+EXCEPTION_JSON_TOC_FILE_NAME);
		jsonExceptionToc.writeToFile(exceptionJsonTocFile);
	}

	@Override
	public void writeException(LicenseException exception, boolean deprecated, String deprecatedVersion)
			throws IOException {
		String exceptionHtmlFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(exception.getLicenseExceptionId());
		String exceptionJsonFileName = exceptionHtmlFileName + ".json";
		String exceptionJSONReference= "./" + exceptionJsonFileName;
		String exceptionHTMLReference = "./"+exceptionHtmlFileName + ".html";
		LicenseExceptionJSONFile exceptionJson = new LicenseExceptionJSONFile();
		jsonExceptionToc.addException(exception, exceptionHTMLReference, exceptionJSONReference, deprecated);
		exceptionJson.setException(exception, deprecated);
		File exceptionJsonFile = new File(jsonFolder.getPath() + File.separator + "exceptions" + File.separator +  exceptionJsonFileName);
		exceptionJson.writeToFile(exceptionJsonFile);
	}
	
	

}
