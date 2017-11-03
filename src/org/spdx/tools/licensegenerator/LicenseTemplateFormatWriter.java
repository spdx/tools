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

import org.spdx.rdfparser.license.LicenseException;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.tools.LicenseGeneratorException;

import com.google.common.io.Files;

/**
 * Write license template format as described in the SPDX spec
 * @author Gary O'Neall
 *
 */
public class LicenseTemplateFormatWriter implements ILicenseFormatWriter {

	private File templateFolder;
	private Charset utf8 = Charset.forName("UTF-8");

	/**
	 * @param templateFolder Folder containing the template files
	 */
	public LicenseTemplateFormatWriter(File templateFolder) {
		this.templateFolder = templateFolder;
	}

	/**
	 * @return the templateFolder
	 */
	public File getTemplateFolder() {
		return templateFolder;
	}

	/**
	 * @param templateFolder the templateFolder to set
	 */
	public void setTemplateFolder(File templateFolder) {
		this.templateFolder = templateFolder;
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
		File templateFile = new File(templateFolder.getPath() + File.separator + licBaseHtmlFileName + ".template.txt");
		if (license.getStandardLicenseTemplate() != null && !license.getStandardLicenseTemplate().trim().isEmpty()) {			
			Files.write(license.getStandardLicenseTemplate(), templateFile, utf8);
		} else {
			Files.write(license.getLicenseText(), templateFile, utf8);
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.tools.licensegenerator.ILicenseFormatWriter#writeToC()
	 */
	@Override
	public void writeToC() throws IOException, LicenseGeneratorException {
		// Nothing to write - no ToC
		
	}

	/* (non-Javadoc)
	 * @see org.spdx.tools.licensegenerator.ILicenseFormatWriter#writeException(org.spdx.rdfparser.license.LicenseException, boolean, java.lang.String)
	 */
	@Override
	public void writeException(LicenseException exception, boolean deprecated, String deprecatedVersion)
			throws IOException, LicenseGeneratorException {
		String licBaseHtmlFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(exception.getLicenseExceptionId());
		if (deprecated) {
			licBaseHtmlFileName = "deprecated_" + licBaseHtmlFileName;
		}
		File templateFile = new File(templateFolder.getPath() + File.separator + licBaseHtmlFileName + ".template.txt");
		Files.write(exception.getLicenseExceptionTemplate(), templateFile, utf8);
	}
}
