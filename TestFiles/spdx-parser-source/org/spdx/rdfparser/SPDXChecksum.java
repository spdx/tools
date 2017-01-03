/**
 * Copyright (c) 2011 Source Auditor Inc.
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
package org.spdx.rdfparser;

import java.util.ArrayList;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * SPDX Checksum class for packages and files
 * @author Gary O'Neall
 *
 */
public class SPDXChecksum {

	// Supported algorithms

	public static final String ALGORITHM_SHA1 = "SHA1";
	private String algorithm;
	private String value;
	private Model model;
	private Node checksumNode;
	private Resource checksumResource;

	public SPDXChecksum(String algorithm, String value) {
		this.algorithm = algorithm;
		this.value = value;
	}

	public SPDXChecksum(Model spdxModel, Node checksumNode) throws InvalidSPDXAnalysisException {
		this.model = spdxModel;
		this.checksumNode = checksumNode;
		if (checksumNode.isBlank()) {
			checksumResource = model.createResource(checksumNode.getBlankNodeId());
		} else if (checksumNode.isURI()) {
			checksumResource = model.createResource(checksumNode.getURI());
		} else {
			throw(new InvalidSPDXAnalysisException("Checksum node can not be a literal"));
		}
		// Algorithm
		Node p = spdxModel.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CHECKSUM_ALGORITHM).asNode();
		Triple m = Triple.createMatch(checksumNode, p, null);
		ExtendedIterator<Triple> tripleIter = spdxModel.getGraph().find(m);
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.algorithm = t.getObject().toString(false);
		}

		// value
		p = spdxModel.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CHECKSUM_VALUE).asNode();
		m = Triple.createMatch(checksumNode, p, null);
		tripleIter = spdxModel.getGraph().find(m);
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.value = t.getObject().toString(false);
		}
	}

	/**
	 * @return the algorithm
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * @param algorithm the algorithm to set
	 */
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
		if (this.model != null && this.checksumNode != null) {
			// delete any previous algorithm
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CHECKSUM_ALGORITHM);
			model.removeAll(checksumResource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CHECKSUM_ALGORITHM);
			checksumResource.addProperty(p, algorithm);
		}
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
		if (this.model != null && this.checksumNode != null) {
			// delete any previous value
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CHECKSUM_VALUE);
			model.removeAll(checksumResource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CHECKSUM_VALUE);
			checksumResource.addProperty(p, value);
		}
	}

	/**
	 * Creates a resource from this SPDX Checksum
	 * @param model
	 * @return
	 */
	public Resource createResource(Model model) {
		this.model = model;
		Resource type = model.createResource(SpdxRdfConstants.SPDX_NAMESPACE +
				SpdxRdfConstants.CLASS_SPDX_CHECKSUM);
		Resource r = model.createResource(type);
		if (algorithm != null) {
			Property algProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE,
					SpdxRdfConstants.PROP_CHECKSUM_ALGORITHM);
			r.addProperty(algProperty, this.algorithm);
		}
		if (this.value != null) {
			Property valueProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CHECKSUM_VALUE);
			r.addProperty(valueProperty, this.value);
		}
		this.checksumNode = r.asNode();
		this.checksumResource = r;
		return r;
	}

	public ArrayList<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();
		String algorithm = this.getAlgorithm();
		if (algorithm == null || algorithm.isEmpty()) {
			retval.add("Missing required algorithm");
		} else {
			if (!algorithm.equals(ALGORITHM_SHA1)) {
				retval.add("Unsupported checksum algorithm: "+algorithm);
			}
		}
		String value = this.getValue();
		if (value == null || value.isEmpty()) {
			retval.add("Missing required checksum value");
		} else {
			String verify = SpdxVerificationHelper.verifyChecksumString(value);
			if (verify != null) {
				retval.add(verify);
			}
		}
		return retval;
	}

}
