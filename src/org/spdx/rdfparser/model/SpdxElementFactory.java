/**
 * Copyright (c) 2015 Source Auditor Inc.
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
package org.spdx.rdfparser.model;

import java.util.Map;

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.SpdxRdfConstants;

import com.google.common.collect.Maps;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Factory for creating SpdxElements from a Jena Model
 * @author Gary O'Neall
 *
 */
public class SpdxElementFactory {
	
	/**
	 * Add to a cache of created elements
	 * @param modelContainer
	 * @param node
	 * @param element
	 */
	static synchronized void addToCreatedElements(IModelContainer modelContainer,
			Node node, SpdxElement element) {
		Map<Node, SpdxElement> containerNodes = createdElements.get(modelContainer);
		if (containerNodes == null) {
			containerNodes = Maps.newHashMap();
			createdElements.put(modelContainer, containerNodes);
		}
		containerNodes.put(node, element);
	}
	/**
	 * Keep track of all nodes created both for performance and to prevent
	 * an infinite recursion from continually creating the same objects.
	 */
	private static Map<IModelContainer, Map<Node, SpdxElement>> createdElements = Maps.newHashMap();

	public static synchronized SpdxElement createElementFromModel(IModelContainer modelContainer,
			Node node) throws InvalidSPDXAnalysisException {
		Map<Node, SpdxElement> containerNodes = createdElements.get(modelContainer);
		if (containerNodes == null) {
			containerNodes = Maps.newHashMap();
			createdElements.put(modelContainer, containerNodes);
		}
		SpdxElement retval = containerNodes.get(node);
		if (retval != null) {
			return retval;
		}
		if (node.isBlank() ||
				(node.isURI() && node.getURI().startsWith(modelContainer.getDocumentNamespace()))) {
			// SPDX element local to this document
			retval = getElementByType(modelContainer, node);
			if (retval == null) {
				retval = guessElementByProperties(modelContainer, node);
				if (retval == null) {
					throw(new InvalidSPDXAnalysisException("Unable to determine the SPDX element type from the model"));
				}
			}
			containerNodes.put(node, retval);
			return retval;
		} else if (node.isURI()) {
			if (SpdxNoneElement.NONE_ELEMENT_URI.equals(node.getURI())) {
				return new SpdxNoneElement();
			} else if (SpdxNoAssertionElement.NOASSERTION_ELEMENT_URI.equals(node.getURI())) {
				return new SpdxNoAssertionElement();
			} else {
				// assume this is an external document reference
				String[] uriParts = node.getURI().split("#");
				if (uriParts.length != 2) {
					throw(new InvalidSPDXAnalysisException("Invalid element URI: "+node.getURI()));
				}
				String docId = modelContainer.documentNamespaceToId(uriParts[0]);
				if (docId == null) {
					throw(new InvalidSPDXAnalysisException("No external document reference was found for URI "+node.getURI()));
				}
				String externalId = docId + ":" + uriParts[1];
				return new ExternalSpdxElement(externalId);
			}
		} else {
			throw(new InvalidSPDXAnalysisException("Can not create an SPDX Element from a literal node"));
		}
	}

	/**
	 * Guesses the element type based on the properties associated with that element.
	 * @param modelContainer
	 * @param node
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static SpdxElement guessElementByProperties(
			IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		// Look for required SPDX Item properties
		if (propertyExists(modelContainer, node, SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_LICENSE_CONCLUDED) && 
				propertyExists(modelContainer, node, SpdxRdfConstants.SPDX_NAMESPACE,
						SpdxRdfConstants.PROP_COPYRIGHT_TEXT)) {
			return guessSpdxItemByProperties(modelContainer, node);
		} else {
			return new SpdxElement(modelContainer, node);
		}
	}

	/**
	 * Guess an SPDX Item based on the properties
	 * @param modelContainer
	 * @param node
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static SpdxElement guessSpdxItemByProperties(
			IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		// Look for required file properties
		if (propertyExists(modelContainer, node, SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_FILE_CHECKSUM) && 
				propertyExists(modelContainer, node, SpdxRdfConstants.SPDX_NAMESPACE, 
						SpdxRdfConstants.PROP_FILE_TYPE))  {
			return new SpdxFile(modelContainer, node);
		} else if (propertyExists(modelContainer, node, SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_FILE_CHECKSUM) && 
				propertyExists(modelContainer, node, SpdxRdfConstants.SPDX_NAMESPACE, 
						SpdxRdfConstants.PROP_PACKAGE_DOWNLOAD_URL) && 
						propertyExists(modelContainer, node, SpdxRdfConstants.SPDX_NAMESPACE, 
								SpdxRdfConstants.PROP_PACKAGE_LICENSE_INFO_FROM_FILES) && 
								propertyExists(modelContainer, node, SpdxRdfConstants.SPDX_NAMESPACE, 
										SpdxRdfConstants.PROP_PACKAGE_VERIFICATION_CODE))  {
			return new SpdxPackage(modelContainer, node);
		} else if (propertyExists(modelContainer, node, SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_SNIPPET_FROM_FILE)) {
			return new SpdxSnippet(modelContainer, node);
		} else {
			return new SpdxItem(modelContainer, node);
		}
	}

	/**
	 * Returns true if a value for a property exists for the subject node
	 * @param modelContainer
	 * @param node
	 * @param namespace
	 * @param propertyName
	 * @return
	 */
	private static boolean propertyExists(IModelContainer modelContainer,
			Node node, String namespace, String propertyName) {
		Node p = modelContainer.getModel().getProperty(namespace, propertyName).asNode();
		Triple m = Triple.createMatch(node, p, null);
		ExtendedIterator<Triple> tripleIter = modelContainer.getModel().getGraph().find(m);	
		return tripleIter.hasNext();
	}

	/**
	 * @param modelContainer
	 * @param node
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static SpdxElement getElementByType(IModelContainer modelContainer,
			Node node) throws InvalidSPDXAnalysisException {
		Node rdfTypePredicate = modelContainer.getModel().getProperty(SpdxRdfConstants.RDF_NAMESPACE, 
				SpdxRdfConstants.RDF_PROP_TYPE).asNode();
		Triple m = Triple.createMatch(node, rdfTypePredicate, null);
		ExtendedIterator<Triple> tripleIter = modelContainer.getModel().getGraph().find(m);	// find the type(s)
		if (tripleIter.hasNext()) {
			Triple triple = tripleIter.next();
			if (tripleIter.hasNext()) {
				throw(new InvalidSPDXAnalysisException("More than one type associated with an SPDX Element"));
			}
			Node typeNode = triple.getObject();
			if (!typeNode.isURI()) {
				throw(new InvalidSPDXAnalysisException("Invalid type for an SPDX Element - not a URI"));
			}
			// need to parse the URI
			String typeUri = typeNode.getURI();
			if (!typeUri.startsWith(SpdxRdfConstants.SPDX_NAMESPACE)) {
				throw(new InvalidSPDXAnalysisException("Invalid type for an SPDX Element - not an SPDX type"));
			}
			String type = typeUri.substring(SpdxRdfConstants.SPDX_NAMESPACE.length());
			if (type.equals(SpdxRdfConstants.CLASS_SPDX_FILE)) {
				return new SpdxFile(modelContainer, node);
			} else if (type.equals(SpdxRdfConstants.CLASS_SPDX_PACKAGE)) {
				return new SpdxPackage(modelContainer, node);
			} else if (type.equals(SpdxRdfConstants.CLASS_SPDX_SNIPPET)) {
				return new SpdxSnippet(modelContainer, node);
			} else if (type.equals(SpdxRdfConstants.CLASS_SPDX_ITEM)) {
				return new SpdxItem(modelContainer, node);
			} else if (type.equals(SpdxRdfConstants.CLASS_SPDX_ELEMENT)) {
				return new SpdxElement(modelContainer, node);
			} else if (type.equals(SpdxRdfConstants.CLASS_SPDX_DOCUMENT)) {
				return new SpdxDocumentContainer(modelContainer.getModel()).getSpdxDocument();
			} else {
				throw(new InvalidSPDXAnalysisException("Invalid type for element '"+type+"'"));
			}
		} else {
			return null;
		}
	}
}
