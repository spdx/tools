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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Static helper class for License XML parsing
 * @author Gary O'Neall
 *
 */
public class LicenseXmlHelper {
	static final Logger logger = Logger.getLogger(LicenseXmlHelper.class);

	private static final String INDENT_STRING = "   ";
	
	/**
	 * Tags that do not require any processing - the text for the children will be included
	 */
	static HashSet<String> UNPROCESSED_TAGS = new HashSet<String>();
	static {
		UNPROCESSED_TAGS.add(LicenseXmlDocument.BODY_TAG);
		UNPROCESSED_TAGS.add(LicenseXmlDocument.COPYRIGHT_TAG);
		UNPROCESSED_TAGS.add(LicenseXmlDocument.TITLE_TAG);
		UNPROCESSED_TAGS.add(LicenseXmlDocument.LIST_ITEM_TAG);
		UNPROCESSED_TAGS.add(LicenseXmlDocument.LICENSE_TAG);
		UNPROCESSED_TAGS.add(LicenseXmlDocument.BULLET_TAG);
		UNPROCESSED_TAGS.add(LicenseXmlDocument.HEADER_TAG);
	}

	/**
	 * Convert a node to text which contains various markup information and appends it to the sb
	 * @param node node to convert
	 * @param useTemplateFormat If true, convert any optional or variable elements into the template markup language
	 * if false, translate to the equivalent text
	 * @param sb Stringbuilder to append the text to
	 * @param indentCount number of indentations (e.g. number of embedded lists)
	 * @return
	 * @throws LicenseXmlException 
	 */
	private static void appendNodeText(Node node, boolean useTemplateFormat, StringBuilder sb, int indentCount) throws LicenseXmlException {
		if (node.getNodeType() == Node.TEXT_NODE) {
			appendNormalizedWhiteSpaceText(sb, node.getNodeValue());
		} else if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element)node;
			String tagName = element.getTagName();
			if (LicenseXmlDocument.LIST_TAG.equals(tagName)) {
				appendListElements(element, useTemplateFormat, sb, indentCount);
			} else if (LicenseXmlDocument.ALTERNATIVE_TAG.equals(tagName)) {
				appendAltText(element, useTemplateFormat, sb, indentCount);
			} else if (LicenseXmlDocument.OPTIONAL_TAG.equals(tagName)) {
				appendOptionalText(element, useTemplateFormat, sb, indentCount);
			} else {
				if (LicenseXmlDocument.BREAK_TAG.equals(tagName)) {
					addNewline(sb, indentCount);
				} else if (LicenseXmlDocument.PARAGRAPH_TAG.equals(tagName)) {
					if (sb.length() > 1) {
						addNewline(sb, indentCount);
					}
				} else if (!UNPROCESSED_TAGS.contains(tagName)) {
					throw(new LicenseXmlException("Unknown license element tag name: "+tagName));
				}
				appendElementChildrenText(element, useTemplateFormat, sb, indentCount);
			}
		}
	}

	/**
	 * Appends text removing any extra whitespace and linefeed information
	 * @param text
	 */
	private static void appendNormalizedWhiteSpaceText(StringBuilder sb, String text) {
		boolean endsInWhiteSpace = sb.length() == 0 || Character.isWhitespace(sb.charAt(sb.length()-1));
		List<String> tokens = tokenize(text);
		if (tokens.size() > 0) {
			if (!endsInWhiteSpace) {
				sb.append(' ');
			}
			sb.append(tokens.get(0));
			for (int i = 1; i < tokens.size(); i++) {
				sb.append(' ');
				sb.append(tokens.get(i));
			}
		}
	}

	/**
	 * Tokenize a string based on the Character whitespace
	 * @param text
	 * @return
	 */
	private static List<String> tokenize(String text) {
		List<String> result = new ArrayList<String>();
		int loc = 0;
		while (loc < text.length()) {
			while (loc < text.length() && Character.isWhitespace(text.charAt(loc))) {
				loc++;
			}
			if (loc < text.length()) {
				StringBuilder sb = new StringBuilder();
				while (loc < text.length() && !Character.isWhitespace(text.charAt(loc))) {
					sb.append(text.charAt(loc++));
				}
				result.add(sb.toString());
			}
		}
		return result;
	}

	/**
	 * Appends the text for all the child nodes in the element
	 * @param element Element to convert
	 * @param useTemplateFormat If true, convert any optional or variable elements into the template markup language
	 * if false, translate to the equivalent text
	 * @param sb Stringbuilder to append the text to
	 * @param indentCount number of indentations (e.g. number of embedded lists)
	 * @throws LicenseXmlException 
	 */
	private static void appendElementChildrenText(Element element,
			boolean useTemplateFormat, StringBuilder sb, int indentCount) throws LicenseXmlException {
		NodeList licenseChildNodes = element.getChildNodes();
		for (int i = 0; i < licenseChildNodes.getLength(); i++) {
			appendNodeText(licenseChildNodes.item(i),useTemplateFormat, sb, indentCount);
		}	
	}

	/**
	 * Add a newline to the stringbuilder and indent per the indent count
	 * @param sb Stringbuild to append to
	 * @param indentCount
	 */
	private static void addNewline(StringBuilder sb, int indentCount) {
		sb.append('\n');
		for (int i = 0; i < indentCount; i ++) {
			sb.append(INDENT_STRING);
		}
	}

	/**
	 * Append optional text
	 * @param element Element element containing the optional text
	 * @param useTemplateFormat If true, convert any optional or variable elements into the template markup language
	 * if false, translate to the equivalent text
	 * @param sb Stringbuilder to append the text to
	 * @param indentCount number of indentations (e.g. number of embedded lists)
	 * @throws LicenseXmlException 
	 */
	private static void appendOptionalText(Element element,
			boolean useTemplateFormat, StringBuilder sb, int indentCount) throws LicenseXmlException {
		if (!LicenseXmlDocument.OPTIONAL_TAG.equals(element.getTagName())) {
			throw(new LicenseXmlException("Expecting optional tag, found "+element.getTagName()));
		}
		StringBuilder childSb = new StringBuilder();
		if (element.hasChildNodes()) {
			appendElementChildrenText(element, useTemplateFormat, childSb, indentCount);
		} else {
			childSb.append(element.getTextContent());
		}
		if (useTemplateFormat) {
			sb.append("<<beginOptional>>");
			if (childSb.length() > 0 && childSb.charAt(0) == ' ') {
				sb.append(' ');
				childSb.delete(0, 1);
			} else if (sb.length() > 0 && !Character.isWhitespace(sb.charAt(sb.length()-1))) {
				sb.append(' ');
			}
			sb.append(childSb);
			sb.append("<<endOptional>>");
		} else {
			if (sb.length() > 0 && !Character.isWhitespace(sb.charAt(sb.length()-1))) {
				sb.append(' ');
			}
			sb.append(childSb);
		}
	}

	/**
	 * Add text for an alternative expression
	 * @param element Element containing the alternative expression
	 * @param useTemplateFormat If true, convert any optional or variable elements into the template markup language
	 * if false, translate to the equivalent text
	 * @param sb Stringbuilder to append the text to
	 * @param indentCount number of indentations (e.g. number of embedded lists)
	 * @throws LicenseXmlException 
	 */
	private static void appendAltText(Element element,
			boolean useTemplateFormat, StringBuilder sb, int indentCount) throws LicenseXmlException {
		if (!LicenseXmlDocument.ALTERNATIVE_TAG.equals(element.getTagName())) {
			throw(new LicenseXmlException("Expected alt tag.  Found '"+element.getTagName()+"'"));
		}
		StringBuilder originalSb = new StringBuilder();
		if (element.hasChildNodes()) {
			appendElementChildrenText(element, useTemplateFormat, originalSb, indentCount);
		} else {
			originalSb.append(element.getTextContent());
		}
		if (useTemplateFormat) {
			if (originalSb.length() > 0 && originalSb.charAt(0) == ' ') {
				sb.append(' ');
				originalSb.delete(0, 1);
			} else if (sb.length() > 0 && !Character.isWhitespace(sb.charAt(sb.length()-1))) {
				sb.append(' ');
			}
			sb.append("<<var;name=\"");
			if (!element.hasAttribute(LicenseXmlDocument.VAR_NAME_ATTRIBUTE)) {
				throw(new LicenseXmlException("Missing name attribute for variable text"));
			}
			sb.append(element.getAttribute(LicenseXmlDocument.VAR_NAME_ATTRIBUTE));
			sb.append("\";original=\"");
			sb.append(originalSb);
			sb.append("\";match=\"");
			if (!element.hasAttribute(LicenseXmlDocument.VAR_MATCH_ATTRIBUTE)) {
				throw(new LicenseXmlException("Missing match attribute for variable text"));
			}
			sb.append(element.getAttribute(LicenseXmlDocument.VAR_MATCH_ATTRIBUTE));
			sb.append("\">>");
		} else {
			if (sb.length() > 0 && !Character.isWhitespace(sb.charAt(sb.length()-1))) {
				sb.append(' ');
			}
			sb.append(originalSb);
		}
	}

	/**
	 * Appends a list element to the stringbuilder sb
	 * @param element
	 * @param useTemplateFormat
	 * @param sb
	 * @param indentCount Number of indentations for the text
	 * @throws LicenseXmlException 
	 */
	private static void appendListElements(Element element,
			boolean useTemplateFormat, StringBuilder sb, int indentCount) throws LicenseXmlException {
		if (!LicenseXmlDocument.LIST_TAG.equals(element.getTagName())) {
			throw(new LicenseXmlException("Invalid list element tag - expected 'list', found '"+element.getTagName()+"'"));
		}
		NodeList listItemNodes = element.getChildNodes();
		for (int i = 0; i < listItemNodes.getLength(); i++) {
			if (listItemNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element listItem = (Element)listItemNodes.item(i);
				if (!LicenseXmlDocument.LIST_ITEM_TAG.equals(listItem.getTagName())) {
					throw(new LicenseXmlException("Expected only list item tags ('li') in a list, found "+listItem.getTagName()));
				}
				addNewline(sb, indentCount+1);
				appendNodeText(listItem, useTemplateFormat, sb, indentCount + 1);
			} else if (listItemNodes.item(i).getNodeType() != Node.TEXT_NODE) {
				throw(new LicenseXmlException("Expected only element children for a list element"));	
			}
		}
	}

	/**
	 * Gets the license template text from the license element
	 * @param licenseElement
	 * @return
	 * @throws LicenseXmlException 
	 */
	public static String getLicenseTemplate(Element licenseElement) throws LicenseXmlException {
		StringBuilder sb = new StringBuilder();
		appendNodeText(licenseElement, true, sb, 0);
		return sb.toString();
	}
	
	/**
	 * Format note text taking into account line breaks, paragraphs etc.
	 * @param licenseElement
	 * @return
	 * @throws LicenseXmlException
	 */
	public static String getNoteText(Element licenseElement) throws LicenseXmlException {
		StringBuilder sb = new StringBuilder();
		appendNodeText(licenseElement, false, sb, 0);
		return sb.toString();
	}

	/**
	 * Gets license text from the license element
	 * @param licenseElement
	 * @return
	 * @throws LicenseXmlException 
	 */
	public static String getLicenseText(Element licenseElement) throws LicenseXmlException {
		StringBuilder sb = new StringBuilder();
		appendNodeText(licenseElement, false, sb, 0);
		return sb.toString();
	}
	
	public static String dumpLicenseDom(Element licenseElement) {
		StringBuilder sb = new StringBuilder();
		appendNode(licenseElement, sb, 0);
		return sb.toString();
	}

	/**
	 * @param licenseElement
	 * @param sb
	 */
	private static void appendNode(Node node,
			StringBuilder sb, int indent) {
		for (int i = 0; i  < indent; i++) {
			sb.append(INDENT_STRING);
		}
		sb.append("Node Type: ");
		sb.append(node.getNodeType());
		sb.append(", Node Name: ");
		sb.append(node.getNodeName());
		sb.append(", Node Value: '");
		sb.append(node.getNodeValue());
		sb.append('\'');
		sb.append(", Node Text: '");
		sb.append(node.getTextContent());
		sb.append("'\n");
		if (node.hasChildNodes()) {
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				appendNode(children.item(i), sb, indent+1);
			}
		}
	}

	/**
	 * @param headerNode
	 * @return
	 * @throws LicenseXmlException 
	 */
	public static Object getHeaderText(Node headerNode) throws LicenseXmlException {
		StringBuilder sb = new StringBuilder();
		appendNodeText(headerNode, false, sb, 0);
		return sb.toString();
	}

}
