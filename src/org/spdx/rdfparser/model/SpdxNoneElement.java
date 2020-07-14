/**
 * Copyright (c) 2020 Source Auditor Inc.
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
package org.spdx.rdfparser.model;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;

/**
 * This SPDX element represents no SPDX element at all.
 *
 * This element should only be used on the right hand side of relationships to represent no SPDX element
 * is related to the subject.
 *
 * This element has no properties and a fixed ID of "NONE".
 *
 * @author Gary O'Neall
 *
 */
public class SpdxNoneElement extends SpdxConstantElement {

	public static final String NONE_ELEMENT_ID = "NONE";
	public static final int NONE_ELEMENT_HASHCODE = 433;
	public static final String NONE_ELEMENT_URI = SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.TERM_ELEMENT_NONE;

	public SpdxNoneElement(IModelContainer container, Node elementNode)
			throws InvalidSPDXAnalysisException {
		super(container, elementNode);
	}

	public SpdxNoneElement() {
		super("NONE",
				"This is a NONE element which represents that NO element is related");
	}

	@Override
	public String getId() {
		return NONE_ELEMENT_ID;
	}

	@Override
	public String toString() {
		return SpdxRdfConstants.NONE_VALUE;
	}

	@Override
	public int hashCode() {
		return NONE_ELEMENT_HASHCODE;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof SpdxNoneElement;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(org.apache.jena.rdf.model.Model)
	 */
	@Override
	public Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_NONE_ELEMENT);
	}

	@Override
	public SpdxNoneElement clone() {
		return new SpdxNoneElement();
	}

	@Override
	public String getUri(IModelContainer modelContainer)
			throws InvalidSPDXAnalysisException {
		return NONE_ELEMENT_URI;
	}

}
