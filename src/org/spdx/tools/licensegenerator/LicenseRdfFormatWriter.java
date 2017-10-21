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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.LicenseException;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.tools.LicenseContainer;
import org.spdx.tools.LicenseGeneratorException;

/**
 * Write RDF formats for the licenses
 * 
 * @author Gary O'Neall
 *
 */
public class LicenseRdfFormatWriter implements ILicenseFormatWriter {

	private File rdfXml;
	private File rdfTurtle;
	private File rdfNt;
	private LicenseContainer container;

	/**
	 * @param rdfXml File to store RDF XML formatted license list
	 * @param rdfTurtle File to store RDF Turtle formatted license list
	 * @param rdfNt File to store RDF Nt formatted license list
	 */
	public LicenseRdfFormatWriter(File rdfXml, File rdfTurtle, File rdfNt) {
		this.rdfXml = rdfXml;
		this.rdfTurtle = rdfTurtle;
		this.rdfNt = rdfNt;
		container = new LicenseContainer();// Create model container to hold licenses and exceptions
	}

	/**
	 * @return the rdfXml
	 */
	public File getRdfXml() {
		return rdfXml;
	}

	/**
	 * @param rdfXml the rdfXml to set
	 */
	public void setRdfXml(File rdfXml) {
		this.rdfXml = rdfXml;
	}

	/**
	 * @return the rdfTurtle
	 */
	public File getRdfTurtle() {
		return rdfTurtle;
	}

	/**
	 * @param rdfTurtle the rdfTurtle to set
	 */
	public void setRdfTurtle(File rdfTurtle) {
		this.rdfTurtle = rdfTurtle;
	}

	/**
	 * @return the rdfNt
	 */
	public File getRdfNt() {
		return rdfNt;
	}

	/**
	 * @param rdfNt the rdfNt to set
	 */
	public void setRdfNt(File rdfNt) {
		this.rdfNt = rdfNt;
	}

	@Override
	public void writeLicense(SpdxListedLicense license, boolean deprecated, String deprecatedVersion) throws IOException, LicenseGeneratorException {
		AnyLicenseInfo licenseClone = license.clone();
		LicenseContainer onlyThisLicense = new LicenseContainer();
		try {
			licenseClone.createResource(onlyThisLicense);
		} catch (InvalidSPDXAnalysisException e) {
			throw new LicenseGeneratorException("SPDX Analysis error cloning license: "+e.getMessage(),e);
		}
		String licBaseFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(license.getLicenseId());
		writeRdf(onlyThisLicense, rdfXml, rdfTurtle, rdfNt, licBaseFileName);
		try {
			license.createResource(container);
		} catch (InvalidSPDXAnalysisException e) {
			throw new LicenseGeneratorException("SPDX Analysis error creating license resource: "+e.getMessage(),e);
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
		if (rdfXml != null) {
			writeRdf(container, rdfXml.getPath() + File.separator + name + ".rdf", "RDF/XML-ABBREV");
		}
		if (rdfTurtle != null) {
			writeRdf(container, rdfTurtle.getPath() + File.separator + name + ".turtle", "TURTLE");
		}
		if (rdfNt != null) {
			writeRdf(container, rdfNt.getPath() + File.separator + name + ".nt", "NT");
		}
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

	@Override
	public void writeToC() throws IOException, LicenseGeneratorException {
		writeRdf(container, rdfXml, rdfTurtle, rdfNt, "licenses");
	}

	@Override
	public void writeException(LicenseException exception, boolean deprecated, String deprecatedVersion)
			throws IOException, LicenseGeneratorException {
		String exceptionHtmlFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(exception.getLicenseExceptionId());
		LicenseException exceptionClone = exception.clone();
		LicenseContainer onlyThisException = new LicenseContainer();
		try {
			exceptionClone.createResource(onlyThisException);
		} catch (InvalidSPDXAnalysisException e) {
			throw new LicenseGeneratorException("SPDX Analysis error cloning exception: "+e.getMessage(),e);
		}
		writeRdf(onlyThisException, rdfXml, rdfTurtle, rdfNt, exceptionHtmlFileName);
		try {
			exception.createResource(container);
		} catch (InvalidSPDXAnalysisException e) {
			throw new LicenseGeneratorException("SPDX Analysis error creating exception resource: "+e.getMessage(),e);
		}	
	}
}
