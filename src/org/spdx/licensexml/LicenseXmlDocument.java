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
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.LicenseException;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.spdxspreadsheet.SPDXLicenseSpreadsheet.DeprecatedLicenseInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parses and provides access to a License XML document
 * @author Gary O'Neall
 *
 */
public class LicenseXmlDocument {
	static final Logger logger = Logger.getLogger(LicenseXmlDocument.class.getName());
	static final String ROOT_ELEMENT_NAME = "SPDX";
	private static final String NAME_ATTRIBUTE = "name";
	private static final String ID_ATTRIBUTE = "identifier";
	private static final String LICENSE_TAG = "license";
	private static final String BODY_TAG = "body";
	private static final String DEPRECATED_ATTRIBUTE = "deprecated";
	private static final String NOTES_TAG = "notes";
	private static final String URLS_TAG = "urls";
	private static final String HEADER_TAG = "header";
	private static final String OSI_APPROVED_ATTRIBUTE = "osi-approved";
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
		assertValid();
	}

	/**
	 * Checks the xmlDocument for a valid file and throws a LicenseXmlException if not valid
	 */
	private void assertValid() throws LicenseXmlException {
		// Check that the root element is SPDX
		Element rootElement = this.xmlDocument.getDocumentElement();
		if (!ROOT_ELEMENT_NAME.equals(rootElement.getTagName())) {
			throw(new LicenseXmlException("Incorrect document element name - expected '"+
					ROOT_ELEMENT_NAME+"', found '"+rootElement.getTagName() + "'"));
		}
		// Required name
		if (!rootElement.hasAttribute(NAME_ATTRIBUTE)) {
			throw(new LicenseXmlException("Missing required license name"));
		}
		// Required ID
		if (!rootElement.hasAttribute(ID_ATTRIBUTE)) {
			throw(new LicenseXmlException("Missing required license ID"));
		}
		// Check for the license, should only be one
		NodeList licenseNodes = rootElement.getElementsByTagName(LICENSE_TAG);
		if (licenseNodes.getLength() < 1) {
			throw(new LicenseXmlException("Missing required license element"));
		}
		if (licenseNodes.getLength() > 1) {
			throw(new LicenseXmlException("More than one license elements"));
		}
		Node licenseNode = licenseNodes.item(0);
		if (licenseNode.getNodeType() != Node.ELEMENT_NODE) {
			throw(new LicenseXmlException("Invalid node type for license"));
		}
		Element licenseElement = (Element)licenseNode;
		// Check license body - should only be one
		NodeList bodyNodes = licenseElement.getElementsByTagName(BODY_TAG);
		if (bodyNodes.getLength() < 1) {
			throw(new LicenseXmlException("Missing required license body element"));
		}
		if (bodyNodes.getLength() > 1) {
			throw(new LicenseXmlException("More than one license body elements"));
		}
		// zero or one license header
		NodeList headerNodes = rootElement.getElementsByTagName(HEADER_TAG);
		if (headerNodes.getLength() > 1) {
			throw(new LicenseXmlException("More than one standard license header elements"));
		}
		//TODO: There are other validations we can do on the text of some of the elements
	}

	public LicenseXmlDocument(Document xmlDocument) throws LicenseXmlException {
		this.xmlDocument = xmlDocument;
	}
	/**
	 * @return
	 */
	public boolean isListedLicense() {
		// TODO update once we know the tag
		return true;
	}

	/**
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	public SpdxListedLicense getListedLicense() throws InvalidSPDXAnalysisException {
		Element rootElement = this.xmlDocument.getDocumentElement();
		String name = rootElement.getAttribute(NAME_ATTRIBUTE);
		String id = rootElement.getAttribute(ID_ATTRIBUTE);
		Element licenseElement = (Element)(rootElement.getElementsByTagName(LICENSE_TAG).item(0));
		String text = getLicenseText(licenseElement);
		NodeList notes = rootElement.getElementsByTagName(NOTES_TAG);
		String comment = null;
		if (notes.getLength() > 0) {
			StringBuilder commentBuilder = new StringBuilder(notes.item(0).getTextContent());
			for (int i = 1; i < notes.getLength(); i++) {
				commentBuilder.append("; ");
				commentBuilder.append(notes.item(i).getTextContent());
			}
			comment = commentBuilder.toString();
		}
		NodeList urlNodes = rootElement.getElementsByTagName(URLS_TAG);
		String[] sourceUrls = new String[urlNodes.getLength()];
		for (int i = 0; i < urlNodes.getLength(); i++) {
			sourceUrls[i] = urlNodes.item(i).getTextContent();
		}
		String licenseHeader = null;
		NodeList headerNodes = rootElement.getElementsByTagName(HEADER_TAG);
		if (headerNodes.getLength() > 0) {
			licenseHeader = headerNodes.item(0).getTextContent();
		}
		String template = getLicenseTemplate(licenseElement);
		boolean osiApproved;
		if (rootElement.hasAttribute(OSI_APPROVED_ATTRIBUTE)) {
			osiApproved = "true".equals(rootElement.getAttribute(OSI_APPROVED_ATTRIBUTE).toLowerCase());
		} else {
			osiApproved = false;
		}
		return new SpdxListedLicense(name, id, text, sourceUrls, comment, licenseHeader, 
				template, osiApproved);
	}

	/**
	 * @param licenseElement
	 * @return
	 */
	private String getLicenseTemplate(Element licenseElement) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param licenseElement
	 * @return
	 */
	private String getLicenseText(Element licenseElement) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 */
	public boolean isLicenseException() {
		// TODO update once we know the tag
		return false;
	}

	/**
	 * @return
	 */
	public LicenseException getLicenseException() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 */
	public boolean isDeprecated() {
		Element rootElement = this.xmlDocument.getDocumentElement();
		if (rootElement.hasAttribute(DEPRECATED_ATTRIBUTE)) {
			return "true".equals(rootElement.getAttribute(DEPRECATED_ATTRIBUTE).toLowerCase());
		} else {
			return false;
		}
	}

	/**
	 * @return
	 */
	public DeprecatedLicenseInfo getDeprecatedLicenseInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}
