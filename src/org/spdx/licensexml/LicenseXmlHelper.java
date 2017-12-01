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

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.spdx.licenseTemplate.HtmlTemplateOutputHandler;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Static helper class for License XML parsing
 * @author Gary O'Neall
 *
 */
public class LicenseXmlHelper implements SpdxRdfConstants {
	static final Logger logger = Logger.getLogger(LicenseXmlHelper.class);

	private static final String INDENT_STRING = "   ";

	private static final String BULLET_ALT_MATCH = ".{0,20}";

	private static final String BULLET_ALT_NAME = "bullet";

	/**
	 * Tags that do not require any processing - the text for the children will be included
	 */
	static HashSet<String> LICENSE_AND_EXCEPTION_SKIPPED_TAGS = new HashSet<String>();
	static {
		LICENSE_AND_EXCEPTION_SKIPPED_TAGS.add(SpdxRdfConstants.LICENSEXML_ELEMENT_CROSS_REF);
		LICENSE_AND_EXCEPTION_SKIPPED_TAGS.add(SpdxRdfConstants.LICENSEXML_ELEMENT_CROSS_REFS);
		LICENSE_AND_EXCEPTION_SKIPPED_TAGS.add(SpdxRdfConstants.LICENSEXML_ELEMENT_NOTES);
		LICENSE_AND_EXCEPTION_SKIPPED_TAGS.add(SpdxRdfConstants.LICENSEXML_ELEMENT_STANDARD_LICENSE_HEADER);
	}
	
	static HashSet<String> LICENSE_AND_EXCEPTION_UNPROCESSED_TAGS = new HashSet<String>();
	static {
		LICENSE_AND_EXCEPTION_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_COPYRIGHT_TEXT);
		//LICENSE_AND_EXCEPTION_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_TITLE_TEXT);
		LICENSE_AND_EXCEPTION_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_ITEM);
		LICENSE_AND_EXCEPTION_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_LICENSE);
		LICENSE_AND_EXCEPTION_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_EXCEPTION);
		//LICENSE_AND_EXCEPTION_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_BULLET);
	}
	
	static HashSet<String> NOTES_SKIPPED_TAGS = new HashSet<String>();
	static {
		NOTES_SKIPPED_TAGS.add(SpdxRdfConstants.LICENSEXML_ELEMENT_CROSS_REF);
		NOTES_SKIPPED_TAGS.add(SpdxRdfConstants.LICENSEXML_ELEMENT_CROSS_REFS);
		NOTES_SKIPPED_TAGS.add(SpdxRdfConstants.LICENSEXML_ELEMENT_STANDARD_LICENSE_HEADER);
	}
	
	static HashSet<String> NOTES_UNPROCESSED_TAGS = new HashSet<String>();
	static {
		NOTES_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_COPYRIGHT_TEXT);
		NOTES_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_TITLE_TEXT);
		NOTES_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_ITEM);
		NOTES_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_LICENSE);
		NOTES_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_EXCEPTION);
		NOTES_UNPROCESSED_TAGS.add(SpdxRdfConstants.LICENSEXML_ELEMENT_NOTES);
		NOTES_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_BULLET);
	}
	
	static HashSet<String> HEADER_SKIPPED_TAGS = new HashSet<String>();
	static {
		HEADER_SKIPPED_TAGS.add(SpdxRdfConstants.LICENSEXML_ELEMENT_CROSS_REF);
		HEADER_SKIPPED_TAGS.add(SpdxRdfConstants.LICENSEXML_ELEMENT_CROSS_REFS);
		HEADER_SKIPPED_TAGS.add(SpdxRdfConstants.LICENSEXML_ELEMENT_NOTES);
	}
	
	static HashSet<String> HEADER_UNPROCESSED_TAGS = new HashSet<String>();
	static {
		HEADER_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_COPYRIGHT_TEXT);
		HEADER_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_TITLE_TEXT);
		HEADER_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_ITEM);
		HEADER_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_LICENSE);
		HEADER_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_EXCEPTION);
		HEADER_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_BULLET);
		HEADER_UNPROCESSED_TAGS.add(SpdxRdfConstants.LICENSEXML_ELEMENT_STANDARD_LICENSE_HEADER);
	}
	
	static HashSet<String> EXAMPLE_SKIPPED_TAGS = new HashSet<String>();
	static {
		EXAMPLE_SKIPPED_TAGS.add(SpdxRdfConstants.LICENSEXML_ELEMENT_CROSS_REF);
		EXAMPLE_SKIPPED_TAGS.add(SpdxRdfConstants.LICENSEXML_ELEMENT_CROSS_REFS);
		EXAMPLE_SKIPPED_TAGS.add(SpdxRdfConstants.LICENSEXML_ELEMENT_NOTES);
		EXAMPLE_SKIPPED_TAGS.add(SpdxRdfConstants.LICENSEXML_ELEMENT_STANDARD_LICENSE_HEADER);
	}
	
	static HashSet<String> EXAMPLE_UNPROCESSED_TAGS = new HashSet<String>();
	static {
		EXAMPLE_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_COPYRIGHT_TEXT);
		EXAMPLE_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_TITLE_TEXT);
		EXAMPLE_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_ITEM);
		EXAMPLE_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_LICENSE);
		EXAMPLE_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_EXCEPTION);
		EXAMPLE_UNPROCESSED_TAGS.add(LICENSEXML_ELEMENT_BULLET);
	}
	
	static HashSet<String> FLOW_CONTROL_ELEMENTS = new HashSet<String>();
	static {
		FLOW_CONTROL_ELEMENTS.add(LICENSEXML_ELEMENT_LIST);
		FLOW_CONTROL_ELEMENTS.add(LICENSEXML_ELEMENT_PARAGRAPH);
	}
	
	static String DOUBLE_QUOTES_REGEX = "(\\u201C|\\u201D)";
	static String SINGLE_QUOTES_REGEX = "(\\u2018|\\u2019)";
	
	/**
	 * Convert a node to text which contains various markup information and appends it to the sb
	 * @param node node to convert
	 * @param useTemplateFormat If true, convert any optional or variable elements into the template markup language
	 * if false, translate to the equivalent text
	 * @param sb Stringbuilder to append the text to
	 * @param indentCount number of indentations (e.g. number of embedded lists)
	 * @param unprocessedTags Tags that do not require any process - text of the children of that tag should just be appended.
	 * @param skippedTags Tags that should not be included
	 * @param includeHtmlTags if true, include HTML tags for creating an HTML fragment including the formatting from the original XML element
	 * @return
	 * @throws LicenseXmlException 
	 */
	private static void appendNodeText(Node node, boolean useTemplateFormat, StringBuilder sb, int indentCount, HashSet<String> unprocessedTags,
			HashSet<String> skippedTags, boolean includeHtmlTags) throws LicenseXmlException {
		if (node.getNodeType() == Node.TEXT_NODE) {
			if (includeHtmlTags) {
				sb.append(StringEscapeUtils.escapeHtml4(fixUpText(node.getNodeValue())));
			} else {
				appendNormalizedWhiteSpaceText(sb, node.getNodeValue());
			}
		} else if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element)node;
			String tagName = element.getTagName();
			if (LICENSEXML_ELEMENT_LIST.equals(tagName)) {
				appendListElements(element, useTemplateFormat, sb, indentCount, unprocessedTags, skippedTags, includeHtmlTags);
			} else if (LICENSEXML_ELEMENT_ALT.equals(tagName)) {
				if (!element.hasAttribute(LICENSEXML_ATTRIBUTE_ALT_NAME)) {
					throw(new LicenseXmlException("Missing name attribute for variable text"));
				}
				String altName = element.getAttribute(LICENSEXML_ATTRIBUTE_ALT_NAME);
				if (!element.hasAttribute(LICENSEXML_ATTRIBUTE_ALT_MATCH)) {
					throw(new LicenseXmlException("Missing match attribute for variable text"));
				}
				String match = element.getAttribute(LICENSEXML_ATTRIBUTE_ALT_MATCH);
				appendAltText(element, altName, match, useTemplateFormat, sb, indentCount, unprocessedTags, skippedTags, includeHtmlTags);
			} else if (LICENSEXML_ELEMENT_OPTIONAL.equals(tagName)) {
				appendOptionalText(element, useTemplateFormat, sb, indentCount, unprocessedTags, skippedTags, includeHtmlTags);
			} else if (LICENSEXML_ELEMENT_BREAK.equals(tagName)) {
				if (includeHtmlTags) {
					sb.append("<br>");
				}
				addNewline(sb, indentCount);
				appendElementChildrenText(element, useTemplateFormat, sb, indentCount, unprocessedTags, skippedTags, includeHtmlTags);
			} else if (LICENSEXML_ELEMENT_PARAGRAPH.equals(tagName)) {
				if (includeHtmlTags) {
					appendParagraphTag(sb, indentCount);
				} else if (sb.length() > 1) {
					addNewline(sb, indentCount);
				}
				appendElementChildrenText(element, useTemplateFormat, sb, indentCount, unprocessedTags, skippedTags, includeHtmlTags);
				if (includeHtmlTags) {
					sb.append("</p>\n");
				}
			} else if (LICENSEXML_ELEMENT_TITLE_TEXT.equals(tagName)) {
				
				//TODO: Don't append optional text if inside an ALT block
				appendOptionalText(element, useTemplateFormat, sb, indentCount, unprocessedTags, skippedTags, includeHtmlTags);
				//appendElementChildrenText(element, useTemplateFormat, sb, indentCount, unprocessedTags, skippedTags, includeHtmlTags);
			} else if (LICENSEXML_ELEMENT_BULLET.equals(tagName)) {
				//TODO: Don't append alt text if inside another ALT block
				appendAltText(element, BULLET_ALT_NAME, BULLET_ALT_MATCH, useTemplateFormat, sb, indentCount, unprocessedTags, skippedTags, includeHtmlTags);
				//appendElementChildrenText(element, useTemplateFormat, sb, indentCount, unprocessedTags, skippedTags, includeHtmlTags);
			} else if (unprocessedTags.contains(tagName)) {
				appendElementChildrenText(element, useTemplateFormat, sb, indentCount, unprocessedTags, skippedTags, includeHtmlTags);
			} else if (!skippedTags.contains(tagName)) {
				throw(new LicenseXmlException("Unknown license element tag name: "+tagName));
			}
		}
	}

	/**
	 * Create a paragraph tag with the appropriate indentation
	 * @param sb
	 * @param indentCount
	 */
	private static void appendParagraphTag(StringBuilder sb, int indentCount) {
		sb.append("<p>");
	}

	/**<
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
	 * @param unprocessedTags Tags that do not require any process - text of the children of that tag should just be appended.
	 * @param skippedTags Tags that should not be included
	 * @param includeHtmlTags if true, include HTML tags for creating an HTML fragment including the formatting from the original XML element
	 * @throws LicenseXmlException 
	 */
	private static void appendElementChildrenText(Element element,
			boolean useTemplateFormat, StringBuilder sb, int indentCount, HashSet<String> unprocessedTags,
			HashSet<String> skippedTags, boolean includeHtmlTags) throws LicenseXmlException {
		NodeList licenseChildNodes = element.getChildNodes();
		for (int i = 0; i < licenseChildNodes.getLength(); i++) {
			appendNodeText(licenseChildNodes.item(i),useTemplateFormat, sb, indentCount, 
					unprocessedTags, skippedTags, includeHtmlTags);
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
	 * @param unprocessedTags Tags that do not require any process - text of the children of that tag should just be appended.
	 * @param skippedTags Tags that should not be included
	 * @param includeHtmlTags if true, include HTML tags for creating an HTML fragment including the formatting from the original XML element
	 * @throws LicenseXmlException 
	 */
	private static void appendOptionalText(Element element,
			boolean useTemplateFormat, StringBuilder sb, int indentCount, HashSet<String> unprocessedTags,
			HashSet<String> skippedTags, boolean includeHtmlTags) throws LicenseXmlException {
		StringBuilder childSb = new StringBuilder();
		if (element.hasChildNodes()) {
			appendElementChildrenText(element, useTemplateFormat, childSb, indentCount, unprocessedTags, skippedTags, includeHtmlTags);
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
		} else if (includeHtmlTags) {
			if (includesFlowControl(element)) {
				sb.append("\n<div class=\"");
			} else {
				sb.append("\n<var class=\"");
			}
			
			sb.append(HtmlTemplateOutputHandler.OPTIONAL_LICENSE_TEXT_CLASS);
			sb.append("\">");
			sb.append(childSb.toString());
			if (includesFlowControl(element)) {
				sb.append("</div>");
			} else {
				sb.append("</var>");
			}
		} else {
			if (sb.length() > 0 && !Character.isWhitespace(sb.charAt(sb.length()-1))) {
				sb.append(' ');
			}
			sb.append(childSb);
		}
	}

	/**
	 * @param element parent element
	 * @return true if the element includes any flow control content per https://www.w3.org/TR/2014/REC-html5-20141028/dom.html#phrasing-content-1
	 */
	private static boolean includesFlowControl(Element element) {
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element eChild = (Element)child;
				if (FLOW_CONTROL_ELEMENTS.contains(eChild.getTagName())) {
					return true;
				} else {
					if (includesFlowControl(eChild)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Add text for an alternative expression
	 * @param element Element containing the alternative expression
	 * @param altName Name for the alt / var text
	 * @param match Regex pattern match string for the alternate text
	 * @param useTemplateFormat If true, convert any optional or variable elements into the template markup language
	 * if false, translate to the equivalent text
	 * @param sb Stringbuilder to append the text to
	 * @param indentCount number of indentations (e.g. number of embedded lists)
	 * @param unprocessedTags Tags that do not require any process - text of the children of that tag should just be appended.
	 * @param skippedTags Tags that should not be included
	 * @param includeHtmlTags if true, include HTML tags for creating an HTML fragment including the formatting from the original XML element
	 * @throws LicenseXmlException 
	 */
	private static void appendAltText(Element element, String altName, String match,
			boolean useTemplateFormat, StringBuilder sb, int indentCount, HashSet<String> unprocessedTags,
			HashSet<String> skippedTags, boolean includeHtmlTags) throws LicenseXmlException {
		StringBuilder originalSb = new StringBuilder();
		if (element.hasChildNodes()) {
			appendElementChildrenText(element, useTemplateFormat, originalSb, indentCount, 
					unprocessedTags, skippedTags, includeHtmlTags);
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
			sb.append(altName);
			sb.append("\";original=\"");
			sb.append(originalSb);
			sb.append("\";match=\"");
			sb.append(match);
			sb.append("\">>");
		} else if (includeHtmlTags) {
			if (includesFlowControl(element)) {
				sb.append("\n<div class=\"");
			} else {
				sb.append("\n<var class=\"");
			}
			sb.append(HtmlTemplateOutputHandler.REPLACEABLE_LICENSE_TEXT_CLASS);
			sb.append("\">");
			sb.append(originalSb);
			if (includesFlowControl(element)) {
				sb.append("</div>");
			} else {
				sb.append("</var>");
			}
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
	 * @param unprocessedTags Tags that do not require any process - text of the children of that tag should just be appended.
	 * @param skippedTags Tags that should not be included
	 * @param includeHtmlTags if true, include HTML tags for creating an HTML fragment including the formatting from the original XML element
	 * @throws LicenseXmlException 
	 */
	private static void appendListElements(Element element,
			boolean useTemplateFormat, StringBuilder sb, int indentCount, HashSet<String> unprocessedTags,
			HashSet<String> skippedTags, boolean includeHtmlTags) throws LicenseXmlException {
		if (!LICENSEXML_ELEMENT_LIST.equals(element.getTagName())) {
			throw(new LicenseXmlException("Invalid list element tag - expected 'list', found '"+element.getTagName()+"'"));
		}
		if (includeHtmlTags) {
			sb.append("\n<ul style=\"list-style:none\">");
		}
		NodeList listItemNodes = element.getChildNodes();
		for (int i = 0; i < listItemNodes.getLength(); i++) {
			if (listItemNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element listItem = (Element)listItemNodes.item(i);
				if (LICENSEXML_ELEMENT_ITEM.equals(listItem.getTagName())) {
					if (includeHtmlTags) {
						sb.append("\n<li>");
						appendNodeText(listItem, useTemplateFormat, sb, indentCount + 1, unprocessedTags, skippedTags, includeHtmlTags);
						sb.append("</li>");
					} else {
						addNewline(sb, indentCount+1);
						appendNodeText(listItem, useTemplateFormat, sb, indentCount + 1, unprocessedTags, skippedTags, includeHtmlTags);
					}
					
				} else if (LICENSEXML_ELEMENT_LIST.equals(listItem.getTagName())) {
					appendListElements(listItem, useTemplateFormat, sb, indentCount+1,
							unprocessedTags, skippedTags, includeHtmlTags);
				} else {
					throw(new LicenseXmlException("Expected only list item tags ('item') or lists ('list') in a list, found "+listItem.getTagName()));
				}
			} else if (listItemNodes.item(i).getNodeType() != Node.TEXT_NODE) {
				throw(new LicenseXmlException("Expected only element children for a list element"));	
			}
		}
		if (includeHtmlTags) {
			sb.append("\n</ul>");
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
		appendNodeText(licenseElement, true, sb, 0, LICENSE_AND_EXCEPTION_UNPROCESSED_TAGS, 
				LICENSE_AND_EXCEPTION_SKIPPED_TAGS, false);
		return fixUpText(sb.toString());
	}
	
	/**
	 * Format note text taking into account line breaks, paragraphs etc.
	 * @param licenseElement
	 * @return
	 * @throws LicenseXmlException
	 */
	public static String getNoteText(Element licenseElement) throws LicenseXmlException {
		StringBuilder sb = new StringBuilder();
		appendNodeText(licenseElement, false, sb, 0, NOTES_UNPROCESSED_TAGS, NOTES_SKIPPED_TAGS, false);
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
		appendNodeText(licenseElement, false, sb, 0, LICENSE_AND_EXCEPTION_UNPROCESSED_TAGS, 
				LICENSE_AND_EXCEPTION_SKIPPED_TAGS, false);
		return fixUpText(sb.toString());
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
	 * @return header text where headerNode is the root element
	 * @throws LicenseXmlException 
	 */
	public static Object getHeaderText(Node headerNode) throws LicenseXmlException {
		StringBuilder sb = new StringBuilder();
		appendNodeText(headerNode, false, sb, 0, HEADER_UNPROCESSED_TAGS, HEADER_SKIPPED_TAGS, false);
		return fixUpText(sb.toString());
	}
	
	/**
	 * @param headerNode
	 * @return header template where headerNode is the root element
	 * @throws LicenseXmlException 
	 */
	public static Object getHeaderTemplate(Node headerNode) throws LicenseXmlException {
		StringBuilder sb = new StringBuilder();
		appendNodeText(headerNode, true, sb, 0, HEADER_UNPROCESSED_TAGS, HEADER_SKIPPED_TAGS, false);
		return fixUpText(sb.toString());
	}
	
	/**
	 * @param headerNode
	 * @return header html fragment where headerNode is the root element
	 * @throws LicenseXmlException 
	 */
	public static Object getHeaderTextHtml(Node headerNode) throws LicenseXmlException {
		StringBuilder sb = new StringBuilder();
		appendNodeText(headerNode, false, sb, 0, HEADER_UNPROCESSED_TAGS, HEADER_SKIPPED_TAGS, true);
		return fixUpText(sb.toString());
	}

	/**
	 * @param exampleElement
	 * @return Example text where exampleElement is the root element
	 * @throws LicenseXmlException 
	 */
	public static String getExampleText(Element exampleElement) throws LicenseXmlException {
		StringBuilder sb = new StringBuilder();
		appendNodeText(exampleElement, false, sb, 0, EXAMPLE_UNPROCESSED_TAGS, EXAMPLE_SKIPPED_TAGS, false);
		return fixUpText(sb.toString());
	}

	/**
	 * @param string
	 * @return Text normalized for different character variations
	 */
	private static String fixUpText(String string) {
		return string.replaceAll(DOUBLE_QUOTES_REGEX, "\"").replaceAll(SINGLE_QUOTES_REGEX, "'");
	}

	/**
	 * Get the HTML fragment representing the license text from the license body
	 * @param licenseElement root element containing the license text
	 * @return
	 * @throws LicenseXmlException 
	 */
	public static String getLicenseTextHtml(Element licenseElement) throws LicenseXmlException {
		StringBuilder sb = new StringBuilder();
		appendNodeText(licenseElement, false, sb, 0, LICENSE_AND_EXCEPTION_UNPROCESSED_TAGS, LICENSE_AND_EXCEPTION_SKIPPED_TAGS, true);
		return fixUpText(sb.toString());
	}

}
