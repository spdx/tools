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
package org.spdx.rdfparser.model.pointer;

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Factory for creating pointer classes based on the information in the model.
 * Subclasses are determined by the type or the properties in the model.
 * @author Gary O'Neall
 *
 */
public final class PointerFactory {
	
	/**
	 * Get the pointer from the model determining the subclass from the information in the
	 * model.
	 * @param modelContainer
	 * @param object
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	public static SinglePointer getSinglePointerFromModel(
			IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		if (!node.isURI() && !node.isBlank()) {
			throw(new InvalidSPDXAnalysisException("Can not create a SinglePointer from a literal node"));
		}
		if (node.isURI() && !node.getURI().startsWith(modelContainer.getDocumentNamespace())) {
			throw(new InvalidSPDXAnalysisException("Unable to access SinglePointer snippet information outside of the SPDX document"));
		}
		SinglePointer retval = getElementByType(modelContainer, node);
		if (retval == null) {
			retval = guessElementByProperties(modelContainer, node);
			if (retval == null) {
				throw(new InvalidSPDXAnalysisException("Unable to determine the SinglePointer type from the model"));
			}
		}
		return retval;
	}

	/**
	 * Get the single pointer class based on the element type specified in the model
	 * @param modelContainer
	 * @param node
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static SinglePointer getElementByType(
			IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		Node rdfTypePredicate = modelContainer.getModel().getProperty(SpdxRdfConstants.RDF_NAMESPACE, 
				SpdxRdfConstants.RDF_PROP_TYPE).asNode();
		Triple m = Triple.createMatch(node, rdfTypePredicate, null);
		ExtendedIterator<Triple> tripleIter = modelContainer.getModel().getGraph().find(m);	// find the type(s)
		if (tripleIter.hasNext()) {
			Triple triple = tripleIter.next();
			if (tripleIter.hasNext()) {
				throw(new InvalidSPDXAnalysisException("More than one type associated with a SinglePointer"));
			}
			Node typeNode = triple.getObject();
			if (!typeNode.isURI()) {
				throw(new InvalidSPDXAnalysisException("Invalid type for a SinglePointer - not a URI"));
			}
			// need to parse the URI
			String typeUri = typeNode.getURI();
			if (!typeUri.startsWith(SpdxRdfConstants.RDF_POINTER_NAMESPACE)) {
				throw(new InvalidSPDXAnalysisException("Invalid type for a SinglePointer - not an RDF Pointer type (namespace must begin with "+
							SpdxRdfConstants.RDF_POINTER_NAMESPACE));
			}
			String type = typeUri.substring(SpdxRdfConstants.RDF_POINTER_NAMESPACE.length());
			if (type.equals(SpdxRdfConstants.CLASS_POINTER_BYTE_OFFSET_POINTER)) {
				return new ByteOffsetPointer(modelContainer, node);
			} else if (type.equals(SpdxRdfConstants.CLASS_POINTER_LINE_CHAR_POINTER)) {
				return new LineCharPointer(modelContainer, node);
			} else {
				throw(new InvalidSPDXAnalysisException("Unsupported type for SinglePointer '"+type+"'"));
			}
		} else {
			return null;
		}
	}

	/**
	 * @param modelContainer
	 * @param node
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static SinglePointer guessElementByProperties(
			IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		if (propertyExists(modelContainer, node, SpdxRdfConstants.RDF_POINTER_NAMESPACE,
				SpdxRdfConstants.PROP_POINTER_OFFSET)) {
			return new ByteOffsetPointer(modelContainer, node);
		} else if (propertyExists(modelContainer, node, SpdxRdfConstants.RDF_POINTER_NAMESPACE,
				SpdxRdfConstants.PROP_POINTER_LINE_NUMBER)) {
			return new LineCharPointer(modelContainer, node);
		} else {
			return null;
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

}
