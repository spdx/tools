/**
 * 
 */
package org.spdx.tools;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.spdx.licensexml.LicenseXmlException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Convert the license list XML from the schema used prior to June 2017 to the standard terms
 * documented at https://docs.google.com/document/d/1z9n44xLH2MxT576KS_AbTOBtecyl5cw6RsrrQHibQtg/edit
 * @author Gary O'Neall
 *
 */
public class ConvertLicenseListXml implements SpdxRdfConstants {
	
	// Document tags and attribute strings
	public static final String OLD_ROOT_ELEMENT_NAME = "SPDX";
	public static final String OLD_NAME_ATTRIBUTE = "name";
	public static final String OLD_ID_ATTRIBUTE = "identifier";
	public static final String OLD_LICENSE_TAG = "license";
	public static final String OLD_EXCEPTION_TAG = "exception";
	public static final String OLD_BODY_TAG = "body";
	public static final String OLD_DEPRECATED_ATTRIBUTE = "deprecated";
	public static final String OLD_NOTES_TAG = "notes";
	public static final String OLD_URLS_TAG = "urls";
	public static final String OLD_URL_TAG = "url";
	public static final String OLD_HEADER_TAG = "header";
	public static final String OLD_OSI_APPROVED_ATTRIBUTE = "osi-approved";
	public static final String OLD_COPYRIGHT_TAG = "copyright";
	public static final String OLD_TITLE_TAG = "title";
	public static final String OLD_LIST_ITEM_TAG = "li";
	public static final String OLD_LIST_TAG = "list";
	public static final String OLD_ALTERNATIVE_TAG = "alt";
	public static final String OLD_OPTIONAL_TAG = "optional";
	public static final String OLD_BREAK_TAG = "br";
	public static final String OLD_PARAGRAPH_TAG = "p";
	public static final String OLD_VAR_NAME_ATTRIBUTE = "name";
	public static final String OLD_VAR_ORIGINAL_ATTRIBUTE = "original";
	public static final String OLD_VAR_MATCH_ATTRIBUTE = "match";
	public static final String OLD_BULLET_TAG = "b";
	private static final String OLD_LICENSE_LIST_VERSION_ATTRIBUTE = LICENSEXML_ATTRIBUTE_LIST_VERSION_ADDED;
	private static final String NAMESPACE = "http://www.spdx.org/license";
	/**
	 * Map of old tag names to new tag names
	 */
	public static Map<String, String> TAG_MAP = new HashMap<String, String>(); 
	static {
		TAG_MAP.put(OLD_ROOT_ELEMENT_NAME, LICENSEXML_ELEMENT_LICENSE_COLLECTION);
		TAG_MAP.put(OLD_NAME_ATTRIBUTE, LICENSEXML_ATTRIBUTE_NAME);
		TAG_MAP.put(OLD_ID_ATTRIBUTE, LICENSEXML_ATTRIBUTE_ID);
		TAG_MAP.put(OLD_LICENSE_LIST_VERSION_ATTRIBUTE, LICENSEXML_ATTRIBUTE_LIST_VERSION_ADDED);
		TAG_MAP.put(OLD_LICENSE_TAG, LICENSEXML_ELEMENT_LICENSE);
		TAG_MAP.put(OLD_EXCEPTION_TAG, LICENSEXML_ELEMENT_EXCEPTION);
		TAG_MAP.put(OLD_DEPRECATED_ATTRIBUTE, LICENSEXML_ATTRIBUTE_DEPRECATED);
		TAG_MAP.put(OLD_NOTES_TAG, LICENSEXML_ELEMENT_NOTES);
		TAG_MAP.put(OLD_URLS_TAG, LICENSEXML_ELEMENT_CROSS_REFS);
		TAG_MAP.put(OLD_URL_TAG, LICENSEXML_ELEMENT_CROSS_REF);
		TAG_MAP.put(OLD_HEADER_TAG, LICENSEXML_ELEMENT_STANDARD_LICENSE_HEADER);
		TAG_MAP.put(OLD_OSI_APPROVED_ATTRIBUTE, LICENSEXML_ATTRIBUTE_OSI_APPROVED);
		TAG_MAP.put(OLD_COPYRIGHT_TAG, LICENSEXML_ELEMENT_COPYRIGHT_TEXT);
		TAG_MAP.put(OLD_TITLE_TAG, LICENSEXML_ELEMENT_TITLE_TEXT);
		TAG_MAP.put(OLD_LIST_ITEM_TAG, LICENSEXML_ELEMENT_ITEM);
		TAG_MAP.put(OLD_LIST_TAG, LICENSEXML_ELEMENT_LIST);
		TAG_MAP.put(OLD_ALTERNATIVE_TAG, LICENSEXML_ELEMENT_ALT);
		TAG_MAP.put(OLD_OPTIONAL_TAG, LICENSEXML_ELEMENT_OPTIONAL);
		TAG_MAP.put(OLD_BREAK_TAG, LICENSEXML_ELEMENT_BREAK);
		TAG_MAP.put(OLD_PARAGRAPH_TAG, LICENSEXML_ELEMENT_PARAGRAPH);
		TAG_MAP.put(OLD_VAR_NAME_ATTRIBUTE, LICENSEXML_ATTRIBUTE_ALT_NAME);
		TAG_MAP.put(OLD_VAR_MATCH_ATTRIBUTE, LICENSEXML_ATTRIBUTE_ALT_MATCH);
		TAG_MAP.put(OLD_BULLET_TAG, LICENSEXML_ELEMENT_BULLET);		
	};

	/**
	 * Converts a directory of license XML files.
	 * args[0] is the path to the directory for the input original xml files
	 * args[1] is the path to an empty output directory where the new XML files will be written
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			error("Invalid number of parameters");
		}
		Path inputDir = Paths.get(args[0].trim());
		if (!Files.exists(inputDir)) {
			error("Input directory "+args[0]+" does not exist");
		}
		if (!Files.isDirectory(inputDir)) {
			error(args[0]+" is not a directory");
		}
		Path outputDir = Paths.get(args[1].trim());
		if (!Files.exists(outputDir)) {
			error("Output directory "+args[1]+" does not exist");
		}
		if (!Files.isDirectory(outputDir)) {
			error(args[1]+" is not a directory");
		}
		if (outputDir.toFile().listFiles().length > 0) {
			error("Output directory must be empty");
		}
		
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(inputDir, "*.xml")) {
	         for (Path inputFile:dirStream) {
	        	 Path outputFile = outputDir.resolve(inputFile.getFileName());
	        	 try {
					convertLicenseXmlFile(inputFile, outputFile);
				} catch (LicenseXmlException e) {
					System.out.println("Error convering "+inputFile.getFileName()+".  Skipping. : "+e.getMessage());
				}
	         }
	     } catch (DirectoryIteratorException ex) {
	    	 error("Error listing input directory: "+ ex.getMessage());
	     } catch (IOException ex) {
	    	 error("IO error: "+ ex.getMessage());
		} catch (LicenseXmlConverterException e) {
			error("Conversion error: " + e.getMessage());
		}
	}

	/**
	 * Convert a license list XML file from the schema used prior to June 2017 to the standard terms
	 * @param inputFile old format file
	 * @param outputFile file to be written with the new format
	 * @throws LicenseXmlConverterException 
	 * @throws LicenseXmlException 
	 */
	public static void convertLicenseXmlFile(Path inputFile, Path outputFile) throws LicenseXmlConverterException, LicenseXmlException {
		if (!Files.exists(inputFile)) {
			throw(new LicenseXmlConverterException("Input file "+inputFile.toString()+ " does not exist."));
		}
		if (!Files.exists(outputFile)) {
			try {
				Files.createFile(outputFile);
			} catch (IOException e) {
				throw new LicenseXmlConverterException("Error creating output file: "+e.getMessage(),e);
			}
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw(new LicenseXmlConverterException("Error creating parser for license XML file"));
		}
		Document inputXmlDocument;
		try {
			inputXmlDocument = builder.parse(inputFile.toFile());
		} catch (SAXException e) {
			throw(new LicenseXmlConverterException("Unable to parse license XML file "+inputFile.getFileName()+": "+e.getMessage()));
		} catch (IOException e) {
			throw(new LicenseXmlConverterException("I/O Error reading XML file"+inputFile.getFileName()+": "+e.getMessage()));
		}
		try {
			assertValid(inputXmlDocument);
		} catch(LicenseXmlConverterException ex) {
			throw new LicenseXmlConverterException("File "+inputFile.getFileName()+" is invalid: "+ex.getMessage());
		}
		
		Document outputXmlDocument = builder.newDocument();
		convertLicenseXmlDocument(inputXmlDocument, outputXmlDocument);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			throw(new LicenseXmlConverterException("Unable to create XML transformer: "+e.getMessage()));
		}
		DOMSource source = new DOMSource(outputXmlDocument);
		StreamResult result = new StreamResult(outputFile.toFile());
		try {
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
		} catch (TransformerException e) {
			throw(new LicenseXmlConverterException("Error saving the converted XML file"+outputFile.getFileName()+": "+e.getMessage()));
		}
	}

	/**
	 * Convert the input document in the old format to the new format
	 * @param inputXmlDocument
	 * @param outputXmlDocument
	 * @throws LicenseXmlConverterException 
	 */
	private static void convertLicenseXmlDocument(Document inputXmlDocument, Document outputXmlDocument) throws LicenseXmlConverterException {
		// Parse the entire document and convert at each step
		// Start at the root
		Element inputRootElement = inputXmlDocument.getDocumentElement();
		Element outputRootElement = outputXmlDocument.createElementNS(NAMESPACE, LICENSEXML_ELEMENT_LICENSE_COLLECTION);
		outputRootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", NAMESPACE);
		outputXmlDocument.appendChild(outputRootElement);
		
		// Create the license nodes
		NodeList licenseNodes = inputRootElement.getElementsByTagName(OLD_LICENSE_TAG);
		for (int i = 0; i < licenseNodes.getLength(); i++) {
			Element outputLicenseElement = outputXmlDocument.createElementNS(NAMESPACE, LICENSEXML_ELEMENT_LICENSE);
			convertLicense(outputXmlDocument, inputRootElement, (Element)licenseNodes.item(i), outputLicenseElement);
			outputRootElement.appendChild(outputLicenseElement);
		}
			
		// Create the exception nodes
		NodeList exceptioneNodes = inputRootElement.getElementsByTagName(OLD_EXCEPTION_TAG);
		for (int i = 0; i < exceptioneNodes.getLength(); i++) {
			Element outputExceptionElement = outputXmlDocument.createElementNS(NAMESPACE, LICENSEXML_ELEMENT_EXCEPTION);
			convertLicense(outputXmlDocument, inputRootElement, (Element)exceptioneNodes.item(i), outputExceptionElement);
			outputRootElement.appendChild(outputExceptionElement);
		}
	}

	/**
	 * Convert a license or exception from the old element structure to the new
	 * @param outputXmlDocument output XML document
	 * @param inputRootElement Old root element containing the license attributes
	 * @param licenseOrException The old license or exception node
	 * @param outputElement The element to append information from the inputRootElement and licenseOrException
	 * @throws LicenseXmlConverterException 
	 */
	private static void convertLicense(Document outputXmlDocument, Element inputRootElement, Element licenseOrException, Element outputElement) throws LicenseXmlConverterException {
		// Add the attributes
		if (inputRootElement.hasAttribute(OLD_ID_ATTRIBUTE)) {
			outputElement.setAttribute(LICENSEXML_ATTRIBUTE_ID, inputRootElement.getAttribute(OLD_ID_ATTRIBUTE));
		}
		if (inputRootElement.hasAttribute(OLD_DEPRECATED_ATTRIBUTE)) {
			outputElement.setAttribute(LICENSEXML_ATTRIBUTE_DEPRECATED, inputRootElement.getAttribute(OLD_DEPRECATED_ATTRIBUTE));
		}
		if (inputRootElement.hasAttribute(OLD_NAME_ATTRIBUTE)) {
			outputElement.setAttribute(LICENSEXML_ATTRIBUTE_NAME, inputRootElement.getAttribute(OLD_NAME_ATTRIBUTE));
		}
		if (inputRootElement.hasAttribute(OLD_OSI_APPROVED_ATTRIBUTE)) {
			outputElement.setAttribute(LICENSEXML_ATTRIBUTE_OSI_APPROVED, inputRootElement.getAttribute(OLD_OSI_APPROVED_ATTRIBUTE));
		}
		if (inputRootElement.hasAttribute(OLD_LICENSE_LIST_VERSION_ATTRIBUTE)) {
			outputElement.setAttribute(LICENSEXML_ATTRIBUTE_LIST_VERSION_ADDED, inputRootElement.getAttribute(OLD_LICENSE_LIST_VERSION_ATTRIBUTE));
		}
		// Cross reference URLS
		NodeList urls = inputRootElement.getElementsByTagName(OLD_URLS_TAG);
		if (urls.getLength() > 0) {
			Element outputCrossRefs = outputXmlDocument.createElementNS(NAMESPACE, LICENSEXML_ELEMENT_CROSS_REFS);
			outputElement.appendChild(outputCrossRefs);
			for (int i = 0; i < urls.getLength(); i++) {
				NodeList urlsUrls = ((Element)(urls.item(i))).getElementsByTagName(OLD_URL_TAG);
				for (int j = 0; j < urlsUrls.getLength(); j++) {
					Element outputCrossRef = outputXmlDocument.createElementNS(NAMESPACE, LICENSEXML_ELEMENT_CROSS_REF);
					outputCrossRef.setTextContent(urlsUrls.item(j).getTextContent());
					outputCrossRefs.appendChild(outputCrossRef);
				}
			}
		}
		// Standard header
		NodeList headers = inputRootElement.getElementsByTagName(OLD_HEADER_TAG);
		for (int i = 0; i < headers.getLength(); i++) {
			Element outputHeader = outputXmlDocument.createElementNS(NAMESPACE, LICENSEXML_ELEMENT_STANDARD_LICENSE_HEADER);
			convertMixedBody((Element)headers.item(i), outputHeader, outputXmlDocument);
			outputElement.appendChild(outputHeader);
		}
		// Notes
		NodeList notes = inputRootElement.getElementsByTagName(OLD_NOTES_TAG);
		for (int i = 0; i < notes.getLength(); i++) {
			Element outputNotes = outputXmlDocument.createElementNS(NAMESPACE, LICENSEXML_ELEMENT_NOTES);
			outputNotes.setTextContent(notes.item(i).getTextContent());
			outputElement.appendChild(outputNotes);
		}
		// Copy the information in the license itself
		convertMixedBody(licenseOrException, outputElement, outputXmlDocument);
	}
	
	/**
	 * Convert a source node to a similar destination node
	 * @param source
	 * @param outputXmlDocument
	 * @return converted node
	 * @throws LicenseXmlConverterException 
	 */
	private static Node convertNode(Node source, Document outputXmlDocument) throws LicenseXmlConverterException {
		if (source.getNodeType() == Node.ELEMENT_NODE) {
			Element sourceElement = (Element)source;
			String tagName = TAG_MAP.get(sourceElement.getTagName());
			if (tagName == null) {
				throw new LicenseXmlConverterException("Unrecognized source element tag: "+sourceElement.getTagName());
			}
			Element destinationElement = outputXmlDocument.createElementNS(NAMESPACE, tagName);
			if (hasBulletFollowedByParagraph(sourceElement.getChildNodes())) {
				convertRemovingPragraphAfterBullet(sourceElement, destinationElement, outputXmlDocument);
			} else {
				convertMixedBody(sourceElement, destinationElement, outputXmlDocument);
			}
			return destinationElement;
		} else if (source.getNodeType() == Node.ATTRIBUTE_NODE) {
			throw new LicenseXmlConverterException("Unexpected attribute: "+source.getNodeName());
		} else if (source.getNodeType() == Node.TEXT_NODE) {
			if (source.hasChildNodes()) {
				throw new LicenseXmlConverterException("Unexpected CDATA children");
			}
			
			return outputXmlDocument.createTextNode(((Text)source).getWholeText());
		} else {
			throw new LicenseXmlConverterException("Unexpected node type: "+source.getNodeType());
		}
	}

	/**
	 * Similar to convertMixed, but removes the paragraph tag after the bullet tag
	 * @param sourceElement
	 * @param destinationElement
	 * @param outputXmlDocument
	 * @throws LicenseXmlConverterException 
	 */
	private static void convertRemovingPragraphAfterBullet(Element sourceElement, Element destinationElement,
			Document outputXmlDocument) throws LicenseXmlConverterException {
		if (!sourceElement.getTagName().equals(OLD_LIST_ITEM_TAG)) {
			throw new LicenseXmlConverterException("Can not convert removing paragraph after bullet for anything other than a list item");
		}
		NodeList children = sourceElement.getChildNodes();
		Element firstElement = null;
		Element secondElement = null;
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (firstElement == null) {
					firstElement = (Element)child;
					if (!firstElement.getTagName().equals(OLD_BULLET_TAG)) {
						throw new LicenseXmlConverterException("Missing bullet tag");
					}
					Node convertedNode = convertNode(child, outputXmlDocument);
					destinationElement.appendChild(convertedNode);
				} else if (secondElement == null) {
					secondElement = (Element)child;
					if (!secondElement.getTagName().equals(OLD_PARAGRAPH_TAG)) {
						throw new LicenseXmlConverterException("Missing paragraph tag");
					}
					NodeList grandChildren = secondElement.getChildNodes();
					for (int j = 0; j < grandChildren.getLength(); j++) {
						// this should skip the paragraph tag
						Node convertedNode = convertNode(grandChildren.item(j), outputXmlDocument);
						destinationElement.appendChild(convertedNode);
					}
				} else {
					Node convertedNode = convertNode(child, outputXmlDocument);
					destinationElement.appendChild(convertedNode);
				}
			} else {
				Node convertedNode = convertNode(child, outputXmlDocument);
				destinationElement.appendChild(convertedNode);
			}
		}
	}

	/**
	 * Check to see if there is a <b> tag followed by a <p> tag
	 * @param childNodes
	 * @return
	 */
	private static boolean hasBulletFollowedByParagraph(NodeList childNodes) {
		Element firstElement = null;
		Element secondElement = null;
		for (int i = 0; i < childNodes.getLength(); i++) {
			if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element)childNodes.item(i);
				if (firstElement == null) {
					firstElement = childElement;
					if (!firstElement.getTagName().equals(OLD_BULLET_TAG)) {
						return false;
					}
				} else if (secondElement == null) {
					secondElement = childElement;
					return secondElement.getTagName().equals(OLD_PARAGRAPH_TAG);
				}
			}
		}
		return false;
	}

	/**
	 * Copy and convert the entire tree from source to destination
	 * @param sourceElement
	 * @param destinationElement
	 * @param destinationDocument
	 * @throws LicenseXmlConverterException 
	 */
	private static void convertMixedBody(Element sourceElement, Element destinationElement, Document destinationDocument) throws LicenseXmlConverterException {
		String tag = sourceElement.getTagName();
		// Check for any tags that have attributes
		if (OLD_ALTERNATIVE_TAG.equals(tag)) {
			// add alternative attributes
			if (sourceElement.hasAttribute(OLD_VAR_NAME_ATTRIBUTE)) {
				destinationElement.setAttribute(LICENSEXML_ATTRIBUTE_ALT_NAME, sourceElement.getAttribute(OLD_VAR_NAME_ATTRIBUTE));
			}
			if (sourceElement.hasAttribute(OLD_VAR_MATCH_ATTRIBUTE)) {
				destinationElement.setAttribute(LICENSEXML_ATTRIBUTE_ALT_MATCH, sourceElement.getAttribute(OLD_VAR_MATCH_ATTRIBUTE));
			}
		}
		NodeList children = sourceElement.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			// remove the body tag
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && OLD_BODY_TAG.equals(((Element)child).getTagName())) {
				convertMixedBody((Element)child, destinationElement, destinationDocument);
			} else {
				Node convertedNode = convertNode(child, destinationDocument);
				destinationElement.appendChild(convertedNode);
			}
		}
	}

	/**
	 * Check he validitity of the old format document and throw an exception if not valid
	 * @param xmlDocument
	 * @throws LicenseXmlConverterException
	 */
	private static void assertValid(Document xmlDocument) throws LicenseXmlConverterException {
		// Check that the root element is SPDX
		Element rootElement = xmlDocument.getDocumentElement();
		if (!OLD_ROOT_ELEMENT_NAME.equals(rootElement.getTagName().toUpperCase())) {
			throw(new LicenseXmlConverterException("Incorrect document element name - expected '"+
					OLD_ROOT_ELEMENT_NAME+"', found '"+rootElement.getTagName() + "'"));
		}
		// Required name
		if (!rootElement.hasAttribute(OLD_NAME_ATTRIBUTE)) {
			throw(new LicenseXmlConverterException("Missing required license name"));
		}
		// Required ID
		if (!rootElement.hasAttribute(OLD_ID_ATTRIBUTE)) {
			throw(new LicenseXmlConverterException("Missing required license ID"));
		}
		// Check to make sure there are no unknown attributes
		NamedNodeMap rootAttributes = rootElement.getAttributes();
		for (int i = 0; i < rootAttributes.getLength(); i++) {
			if (TAG_MAP.get(rootAttributes.item(i).getNodeName()) == null) {
				throw new LicenseXmlConverterException("Unknown attribute "+rootAttributes.item(i).getNodeName());
			}
		}
		// Check to make sure there are no unknown elements
		NodeList rootChildren = rootElement.getChildNodes();
		for (int i = 0; i < rootChildren.getLength(); i++) {
			if (rootChildren.item(i).getNodeType() == Node.ELEMENT_NODE) {
				if (TAG_MAP.get(rootChildren.item(i).getNodeName()) == null) {
					throw new LicenseXmlConverterException("Unknown element "+rootChildren.item(i).getNodeName());
				}
			}
		}
		// Check for the license or exception, should only be one
		NodeList licenseNodes = rootElement.getElementsByTagName(OLD_LICENSE_TAG);
		NodeList exceptionNodes = rootElement.getElementsByTagName(OLD_EXCEPTION_TAG);
		if (licenseNodes.getLength() < 1 && exceptionNodes.getLength() < 1) {
			throw(new LicenseXmlConverterException("Missing required license or exception element"));
		}
		if (licenseNodes.getLength() +  exceptionNodes.getLength() > 1) {
			throw(new LicenseXmlConverterException("More than one license and exception elements"));
		}
		Node licenseNode;
		if (licenseNodes.getLength() > 0) {
			licenseNode = licenseNodes.item(0);
		} else {
			licenseNode = exceptionNodes.item(0);
		}
		if (licenseNode.getNodeType() != Node.ELEMENT_NODE) {
			throw(new LicenseXmlConverterException("Invalid node type for license"));
		}
		NodeList headerNodes = rootElement.getElementsByTagName(OLD_HEADER_TAG);
		if (headerNodes.getLength() > 1) {
			throw(new LicenseXmlConverterException("More than one standard license header elements"));
		}
		//TODO: There are other validations we can do on the text of some of the elements
	}

	/**
	 * Handle errors and exist the application
	 * @param msg
	 */
	private static void error(String msg) {
		System.out.println(msg);
		usage();
		System.exit(1);
	}

	/**
	 * Print the usage
	 */
	private static void usage() {
		System.out.println("Usage: ConvertLicenseListXml inputDir outputDir");
		System.out.println("\tinputDir contains license list XML files in pre-Jun 2017 format");
		System.out.println("\toutputDir is an empty output directory to store the converted xml files");
	}

}
