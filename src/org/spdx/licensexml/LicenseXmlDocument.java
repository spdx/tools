/**
 * Copyright (c) 2016 Source Auditor Inc.
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
package org.spdx.licensexml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.LicenseException;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.spdxspreadsheet.SPDXLicenseSpreadsheet.DeprecatedLicenseInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Parses and provides access to a License XML document
 * @author Gary O'Neall
 *
 */
public class LicenseXmlDocument {
	static final Logger logger = LoggerFactory.getLogger(LicenseXmlDocument.class.getName());
	
	public static final String PROP_SCHEMA_FILENAME = "listedLicenseSchema";
	public static final String LICENSE_XML_SCHEMA_URL = "https://raw.githubusercontent.com/spdx/license-list-XML/master/schema/ListedLicense.xsd";
	public static final String LICENSE_XML_SCHEMA_LOCATION = "org/spdx/licensexml/ListedLicense.xsd";

	private static Schema _schema = null;	// cache of the license XML schema
	private Document xmlDocument;

	/**
	 * @param file XML file for the License
	 */
	public LicenseXmlDocument(File file) throws LicenseXmlException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error("Parser configuration error creating document builder",e);
			throw(new LicenseXmlException("Error creating parser for license XML file"));
		}
		try {
			this.xmlDocument = builder.parse(file);
		} catch (SAXException e) {
			logger.error("Error parsing license XML document",e);
			throw(new LicenseXmlException("Unable to parse license XML file: "+e.getMessage()));
		} catch (IOException e) {
			logger.error("I/O Error reading license XML file",e);
			throw(new LicenseXmlException("I/O Error reading XML file: "+e.getMessage()));
		}
		assertValid(file);
	}
	
	/**
	 * @return listed license XML schema
	 * @throws LicenseXmlException
	 */
	private synchronized Schema getSchema() throws LicenseXmlException {
		if (_schema == null) {
			InputStream schemaIs = null;
			try {
				String schemaFilePath = System.getProperty(PROP_SCHEMA_FILENAME);
				if (schemaFilePath != null) {
					try {
					schemaIs = new FileInputStream(schemaFilePath);
					} catch (IOException e) {
						logger.error("IO Exception opening specified schema file "+schemaFilePath,e);
						throw new LicenseXmlException("Invalid license XML schema file");
					}
				} else {
					try {
						URL schemaUrl = new URL(LICENSE_XML_SCHEMA_URL);
						schemaIs = schemaUrl.openStream();
					} catch (Exception e) {
						logger.warn("Unable to open license XML schema URL, using cached copy",e);
					}
					if (schemaIs == null) {
						schemaIs = LicenseXmlDocument.class.getClassLoader().getResourceAsStream(LICENSE_XML_SCHEMA_LOCATION);			
					}
				}
				Source schemaSource = new StreamSource(schemaIs);
				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				_schema = schemaFactory.newSchema(schemaSource);
			} catch (SAXException e) {
				logger.error("Invalid schema file",e);
				throw new LicenseXmlException("Invalid Listed License Schema",e);
			} finally {
				if (schemaIs != null) {
					try {
						schemaIs.close();
					} catch (IOException e) {
						logger.warn("Unable to close Schema stream",e);
					}
				}
			}		
		}
		return _schema;
	}

	/**
	 * Checks the xmlDocument for a valid file and throws a LicenseXmlException if not valid
	 */
	private void assertValid(File licenseXmlFile) throws LicenseXmlException {		
		try {			
			Source xmlSource = new StreamSource(licenseXmlFile);
			Schema schema = getSchema();
			Validator validator = schema.newValidator();
			validator.validate(xmlSource);
		} catch (MalformedURLException e) {
			logger.error("Unable to open License List XML schema file",e);
			throw new LicenseXmlException("Unable to open License List XML schema file");
		} catch (SAXParseException e) {
			logger.error("Invalid license XML file "+licenseXmlFile.getName(),e);
			throw new LicenseXmlException("Parsing error in XML file "+licenseXmlFile.getName()+ " at line "+e.getLineNumber()+", column "+e.getColumnNumber()+":"+e.getMessage());
		} catch (SAXException e) {
			logger.error("Invalid license XML file "+licenseXmlFile.getName(),e);
			throw new LicenseXmlException("Invalid XML file "+licenseXmlFile.getName()+":"+e.getMessage());
		} catch (IOException e) {
			logger.error("IO Error validating license XML file",e);
			throw new LicenseXmlException("IO Error validating license XML file");
		}
	}

	public LicenseXmlDocument(Document xmlDocument) throws LicenseXmlException {
		this.xmlDocument = xmlDocument;
	}

	/**
	 * Will skip deprecated licenses
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 * @throws LicenseXmlException 
	 */
	public List<SpdxListedLicense> getListedLicenses() throws InvalidSPDXAnalysisException, LicenseXmlException {
		List<SpdxListedLicense> retval = new ArrayList<SpdxListedLicense>();
		Element rootElement = this.xmlDocument.getDocumentElement();
		NodeList licenseElements = rootElement.getElementsByTagName(SpdxRdfConstants.LICENSEXML_ELEMENT_LICENSE);
		for (int i = 0; i < licenseElements.getLength(); i++) {
			Element licenseElement = (Element)(licenseElements.item(i));
			if (!licenseElement.hasAttribute(SpdxRdfConstants.LICENSEXML_ATTRIBUTE_DEPRECATED) ||
					"false".equals(licenseElement.getAttribute(SpdxRdfConstants.LICENSEXML_ATTRIBUTE_DEPRECATED).toLowerCase())) {
				retval.add(getListedLicense(licenseElement));
			}
		}
		return retval;
	}

	private SpdxListedLicense getListedLicense(Element licenseElement) throws InvalidSPDXAnalysisException, LicenseXmlException {
		String name = licenseElement.getAttribute(SpdxRdfConstants.LICENSEXML_ATTRIBUTE_NAME);
		String id = licenseElement.getAttribute(SpdxRdfConstants.LICENSEXML_ATTRIBUTE_ID);
		boolean deprecated = licenseElement.hasAttribute(SpdxRdfConstants.LICENSEXML_ATTRIBUTE_DEPRECATED) &&
				"true".equals(licenseElement.getAttribute(SpdxRdfConstants.LICENSEXML_ATTRIBUTE_DEPRECATED).toLowerCase());
		NodeList textNodes = licenseElement.getElementsByTagName(SpdxRdfConstants.LICENSEXML_ELEMENT_TEXT);
		if (textNodes.getLength() != 1) {
			throw new LicenseXmlException("Invalid number of text elements.  Expected 1 - found "+textNodes.getLength());
		}
		Element textElement = (Element)textNodes.item(0);
		String text = LicenseXmlHelper.getLicenseText(textElement);
		NodeList notes = licenseElement.getElementsByTagName(SpdxRdfConstants.LICENSEXML_ELEMENT_NOTES);
		String comment = null;
		if (notes.getLength() > 0) {
			StringBuilder commentBuilder = new StringBuilder(LicenseXmlHelper.getNoteText((Element)(notes.item(0))));
			for (int i = 1; i < notes.getLength(); i++) {
				commentBuilder.append("; ");
				commentBuilder.append(LicenseXmlHelper.getNoteText((Element)(notes.item(i))));
			}
			comment = commentBuilder.toString();
		}
		NodeList urlNodes = licenseElement.getElementsByTagName(SpdxRdfConstants.LICENSEXML_ELEMENT_CROSS_REF);
		String[] sourceUrls = new String[urlNodes.getLength()];
		for (int i = 0; i < urlNodes.getLength(); i++) {
			sourceUrls[i] = urlNodes.item(i).getTextContent();
		}
		String licenseHeader = null;
		String licenseHeaderTemplate = null;
		String licenseHeaderTemplateHtml = null;
		NodeList headerNodes = licenseElement.getElementsByTagName(SpdxRdfConstants.LICENSEXML_ELEMENT_STANDARD_LICENSE_HEADER);
		if (headerNodes.getLength() > 0) {
			StringBuilder sbText = new StringBuilder();
			StringBuilder sbTemplate = new StringBuilder();
			StringBuilder sbHtml = new StringBuilder();
			sbText.append(LicenseXmlHelper.getHeaderText((Element)headerNodes.item(0)));
			sbTemplate.append(LicenseXmlHelper.getHeaderTemplate((Element)headerNodes.item(0)));
			sbHtml.append(LicenseXmlHelper.getHeaderTextHtml((Element)headerNodes.item(0)));
			for (int i = 1; i < headerNodes.getLength(); i++) {
				sbText.append('\n');
				sbText.append(LicenseXmlHelper.getHeaderText((Element)headerNodes.item(i)));
				sbTemplate.append('\n');
				sbTemplate.append(LicenseXmlHelper.getHeaderTemplate((Element)headerNodes.item(i)));
				sbHtml.append("<br />\n");
				sbHtml.append(LicenseXmlHelper.getHeaderTextHtml((Element)headerNodes.item(i)));
			}
			licenseHeader = sbText.toString();
			licenseHeaderTemplate = sbTemplate.toString();
			licenseHeaderTemplateHtml = sbHtml.toString();
		}
		String template = LicenseXmlHelper.getLicenseTemplate(textElement);
		boolean osiApproved;
		if (licenseElement.hasAttribute(SpdxRdfConstants.LICENSEXML_ATTRIBUTE_OSI_APPROVED)) {
			osiApproved = "true".equals(licenseElement.getAttribute(SpdxRdfConstants.LICENSEXML_ATTRIBUTE_OSI_APPROVED).toLowerCase());
		} else {
			osiApproved = false;
		}
		boolean fsfLibre;
		if (licenseElement.hasAttribute(SpdxRdfConstants.LICENSEXML_ATTRIBUTE_FSF_LIBRE)) {
			fsfLibre = "true".equals(licenseElement.getAttribute(SpdxRdfConstants.LICENSEXML_ATTRIBUTE_FSF_LIBRE).toLowerCase());
		} else {
			fsfLibre = false;
		}
		String licenseHtml = LicenseXmlHelper.getLicenseTextHtml(textElement);
		SpdxListedLicense retval = new SpdxListedLicense(name, id, text, sourceUrls, comment, licenseHeader, 
				template, licenseHeaderTemplate, osiApproved, fsfLibre, licenseHtml, licenseHeaderTemplateHtml);
		retval.setDeprecated(deprecated);
		return retval;
	}

	/**
	 * @return
	 * @throws LicenseXmlException 
	 */
	public List<LicenseException> getLicenseExceptions() throws LicenseXmlException {
		List<LicenseException> retval = new ArrayList<LicenseException>();
		Element rootElement = this.xmlDocument.getDocumentElement();
		NodeList exceptionElements = rootElement.getElementsByTagName(SpdxRdfConstants.LICENSEXML_ELEMENT_EXCEPTION);
		for (int i = 0; i < exceptionElements.getLength(); i++) {
			Element exceptionElement = (Element)(exceptionElements.item(i));
			retval.add(getException(exceptionElement));
		}
		return retval;
	}

	private LicenseException getException(Element exceptionElement) throws LicenseXmlException {
		String name = exceptionElement.getAttribute(SpdxRdfConstants.LICENSEXML_ATTRIBUTE_NAME);
		String id = exceptionElement.getAttribute(SpdxRdfConstants.LICENSEXML_ATTRIBUTE_ID);
		NodeList textNodes = exceptionElement.getElementsByTagName(SpdxRdfConstants.LICENSEXML_ELEMENT_TEXT);
		if (textNodes.getLength() != 1) {
			throw new LicenseXmlException("Invalid number of text elements.  Expected 1 - found "+textNodes.getLength());
		}
		Element textElement = (Element)textNodes.item(0);
		String text = LicenseXmlHelper.getLicenseText(textElement);
		String template = LicenseXmlHelper.getLicenseTemplate(textElement);
		String html = LicenseXmlHelper.getLicenseTextHtml(textElement);
		NodeList notes = exceptionElement.getElementsByTagName(SpdxRdfConstants.LICENSEXML_ELEMENT_NOTES);
		String comment = null;
		if (notes.getLength() > 0) {
			StringBuilder commentBuilder = new StringBuilder(LicenseXmlHelper.getNoteText((Element)(notes.item(0))));
			for (int i = 1; i < notes.getLength(); i++) {
				commentBuilder.append("; ");
				commentBuilder.append(LicenseXmlHelper.getNoteText((Element)(notes.item(i))));
			}
			comment = commentBuilder.toString();
		}
		NodeList urlNodes = exceptionElement.getElementsByTagName(SpdxRdfConstants.LICENSEXML_ELEMENT_CROSS_REF);
		String[] sourceUrls = new String[urlNodes.getLength()];
		for (int i = 0; i < urlNodes.getLength(); i++) {
			sourceUrls[i] = urlNodes.item(i).getTextContent();
		}
		return new LicenseException(id, name, text, template, sourceUrls, comment, html);
	}

	/**
	 * @return
	 * @throws LicenseXmlException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	public List<DeprecatedLicenseInfo> getDeprecatedLicenseInfos() throws InvalidSPDXAnalysisException, LicenseXmlException {
		List<DeprecatedLicenseInfo> retval = new ArrayList<DeprecatedLicenseInfo>();
		Element rootElement = this.xmlDocument.getDocumentElement();
		NodeList licenseElements = rootElement.getElementsByTagName(SpdxRdfConstants.LICENSEXML_ELEMENT_LICENSE);
		for (int i = 0; i < licenseElements.getLength(); i++) {
			Element licenseElement = (Element)(licenseElements.item(i));
			if (licenseElement.hasAttribute(SpdxRdfConstants.LICENSEXML_ATTRIBUTE_DEPRECATED) &&
					"true".equals(licenseElement.getAttribute(SpdxRdfConstants.LICENSEXML_ATTRIBUTE_DEPRECATED).toLowerCase())) {
				String deprecatedVersion = null;
				if (licenseElement.hasAttribute(SpdxRdfConstants.LICENSEXML_ATTRIBUTE_DEPRECATED_VERSION)) {
					deprecatedVersion = licenseElement.getAttribute(SpdxRdfConstants.LICENSEXML_ATTRIBUTE_DEPRECATED_VERSION);
				}
				DeprecatedLicenseInfo dli = new DeprecatedLicenseInfo(getListedLicense(licenseElement), deprecatedVersion);
				retval.add(dli);
			}
		}
		return retval;
	}

}
