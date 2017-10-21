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

import org.spdx.html.ExceptionHtml;
import org.spdx.html.ExceptionHtmlToc;
import org.spdx.html.InvalidLicenseTemplateException;
import org.spdx.html.LicenseHTMLFile;
import org.spdx.html.LicenseTOCHTMLFile;
import org.spdx.rdfparser.license.LicenseException;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.spdxspreadsheet.SPDXLicenseSpreadsheet.DeprecatedLicenseInfo;
import org.spdx.tools.LicenseGeneratorException;

import com.github.mustachejava.MustacheException;

/**
 * @author gary
 *
 */
public class LicenseRdfaFormatWriter implements ILicenseFormatWriter {
	
	static final String LICENSE_TOC_HTML_FILE_NAME = "index.html";
	static final String EXCEPTION_TOC_FILE_NAME = "exceptions-index.html";
	
	private File rdfaFolder;
	private String version;
	private String releaseDate;
	private LicenseHTMLFile licHtml;
	private LicenseTOCHTMLFile tableOfContentsHTML;
	private ExceptionHtmlToc htmlExceptionToc;
	private String exceptionHtmlTocReference = "./" + EXCEPTION_TOC_FILE_NAME;

	/**
	 * @param version License list version
	 * @param releaseDate release date for the license list
	 * @param rdfaFolder Folder to store all of the RDFa files
	 */
	public LicenseRdfaFormatWriter(String version, String releaseDate, File rdfaFolder) {
		this.rdfaFolder = rdfaFolder;
		this.version = version;
		this.releaseDate = releaseDate;
		this.tableOfContentsHTML = new LicenseTOCHTMLFile(version, releaseDate);
		this.licHtml = new LicenseHTMLFile();
		htmlExceptionToc = new ExceptionHtmlToc();
	}

	/**
	 * @return the rdfaFolder
	 */
	public File getRdfaFolder() {
		return rdfaFolder;
	}



	/**
	 * @param rdfaFolder the rdfaFolder to set
	 */
	public void setRdfaFolder(File rdfaFolder) {
		this.rdfaFolder = rdfaFolder;
	}



	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}



	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}



	/**
	 * @return the releaseDate
	 */
	public String getReleaseDate() {
		return releaseDate;
	}



	/**
	 * @param releaseDate the releaseDate to set
	 */
	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}



	/**
	 * @return the licHtml
	 */
	public LicenseHTMLFile getLicHtml() {
		return licHtml;
	}



	/**
	 * @param licHtml the licHtml to set
	 */
	public void setLicHtml(LicenseHTMLFile licHtml) {
		this.licHtml = licHtml;
	}



	/**
	 * @return the tableOfContentsHTML
	 */
	public LicenseTOCHTMLFile getTableOfContentsHTML() {
		return tableOfContentsHTML;
	}



	/**
	 * @param tableOfContentsHTML the tableOfContentsHTML to set
	 */
	public void setTableOfContentsHTML(LicenseTOCHTMLFile tableOfContentsHTML) {
		this.tableOfContentsHTML = tableOfContentsHTML;
	}

	/* (non-Javadoc)
	 * @see org.spdx.tools.licensegenerator.ILicenseFormatWriter#writeLicense(org.spdx.rdfparser.license.SpdxListedLicense, boolean, java.lang.String)
	 */
	@Override
	public void writeLicense(SpdxListedLicense license, boolean deprecated, String deprecatedVersion)
			throws IOException, LicenseGeneratorException {
		this.licHtml.setLicense(license);
		this.licHtml.setDeprecated(deprecated);
		if (deprecatedVersion != null) {
			this.licHtml.setDeprecatedVersion(deprecatedVersion);
		}
		String licBaseHtmlFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(license.getLicenseId());
		String licHtmlFileName = licBaseHtmlFileName + ".html";
		String licHTMLReference = "./"+licHtmlFileName;
		String tocHTMLReference = "./"+LICENSE_TOC_HTML_FILE_NAME;

		File licHtmlFile = new File(rdfaFolder.getPath()+File.separator+licHtmlFileName);
		try {
			licHtml.writeToFile(licHtmlFile, tocHTMLReference);
		} catch (MustacheException e) {
			throw new LicenseGeneratorException("Template error for license HTML file: "+e.getMessage(),e);
		} catch (InvalidLicenseTemplateException e) {
			throw new LicenseGeneratorException("License template error for license HTML file: "+e.getMessage(),e);
		}
		if (deprecated) {
			tableOfContentsHTML.addDeprecatedLicense(new DeprecatedLicenseInfo(license, deprecatedVersion), licHTMLReference);
		} else {
			tableOfContentsHTML.addLicense(license, licHTMLReference);
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.tools.licensegenerator.ILicenseFormatWriter#writeToC()
	 */
	@Override
	public void writeToC() throws IOException {
		File tocHtmlFile = new File(rdfaFolder.getPath()+File.separator+LICENSE_TOC_HTML_FILE_NAME);
		tableOfContentsHTML.writeToFile(tocHtmlFile);
		File exceptionTocFile = new File(rdfaFolder.getPath()+File.separator+EXCEPTION_TOC_FILE_NAME);
		htmlExceptionToc.writeToFile(exceptionTocFile, version);
	}

	@Override
	public void writeException(LicenseException exception, boolean deprecated, String deprecatedVersion)
			throws IOException {
		ExceptionHtml exceptionHtml = new ExceptionHtml(exception);
		String exceptionHtmlFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(exception.getLicenseExceptionId());
		String exceptionHTMLReference = "./"+exceptionHtmlFileName + ".html";
		File exceptionHtmlFile = new File(rdfaFolder.getPath()+File.separator+exceptionHtmlFileName + ".html");
		exceptionHtml.writeToFile(exceptionHtmlFile, exceptionHtmlTocReference);
		htmlExceptionToc.addException(exception, exceptionHTMLReference);
	}

}
