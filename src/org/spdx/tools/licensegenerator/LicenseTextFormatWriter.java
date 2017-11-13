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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.spdx.rdfparser.license.LicenseException;
import org.spdx.rdfparser.license.SpdxListedLicense;


/**
 * Writes licenses in a simple text format
 * 
 * @author Gary O'Neall
 *
 */
public class LicenseTextFormatWriter implements ILicenseFormatWriter {

	private File textFolder;
	private Charset utf8 = Charset.forName("UTF-8");

	/**
	 * @param textFolder Folder to write the text files
	 */
	public LicenseTextFormatWriter(File textFolder) {
		this.textFolder = textFolder;
	}

	/**
	 * @return Folder to write the text files
	 */
	public File getTextFolder() {
		return textFolder;
	}

	/**
	 * @param textFolder Folder to write the text files
	 */
	public void setTextFolder(File textFolder) {
		this.textFolder = textFolder;
	}

	/* (non-Javadoc)
	 * @see org.spdx.tools.licensegenerator.ILicenseFormatWriter#writeLicense(org.spdx.rdfparser.license.SpdxListedLicense, boolean)
	 */
	@Override
	public void writeLicense(SpdxListedLicense license, boolean deprecated, String deprecatedVersion) throws IOException {
		String licBaseHtmlFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(license.getLicenseId());
		if (deprecated) {
			licBaseHtmlFileName = "deprecated_" + licBaseHtmlFileName;
		}
		Path textFilePath = Paths.get(textFolder.getPath(), licBaseHtmlFileName + ".txt");
		Files.write(textFilePath, Arrays.asList(license.getLicenseText().split("\\n")), utf8);
	}

	@Override
	public void writeToC() throws IOException {
		// Don't need to do anything - no TOC
		
	}

	@Override
	public void writeException(LicenseException exception, boolean deprecated, String deprecatedVersion)
			throws IOException {
		
		String exceptionHtmlFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(exception.getLicenseExceptionId());
		Path textFilePath = Paths.get(textFolder.getPath(), exceptionHtmlFileName + ".txt");
		Files.write(textFilePath, Arrays.asList(exception.getLicenseExceptionText().split("\\n")), utf8);
	}
}
