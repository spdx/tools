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
import java.util.HashMap;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * SPDX Checksum class for packages and files
 * @author Gary O'Neall
 *
 */
public class SPDXChecksum {

	// Supported algorithms
	public static final HashMap<String, String> ALGORITHM_TO_URI = new HashMap<String, String>();
	public static final HashMap<String, String> URI_TO_ALGORITHM = new HashMap<String, String>();
	static {
		ALGORITHM_TO_URI.put(SpdxRdfConstants.ALGORITHM_SHA1, 
				SpdxRdfConstants.SPDX_NAMESPACE+SpdxRdfConstants.PROP_CHECKSUM_ALGORITHM_SHA1);
		URI_TO_ALGORITHM.put(SpdxRdfConstants.SPDX_NAMESPACE+SpdxRdfConstants.PROP_CHECKSUM_ALGORITHM_SHA1,
				SpdxRdfConstants.ALGORITHM_SHA1);
	}
	
	private String algorithm;
	private String value;
	private Model model;
	private Node checksumNode;
	private Resource checksumResource;
	
	protected static Resource findSpdxChecksum(Model model, SPDXChecksum checksum) throws InvalidSPDXAnalysisException {
		// find any matching checksum values
		Node checksumValueProperty = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CHECKSUM_VALUE).asNode();
		Triple checksumValueMatch = Triple.createMatch(null, checksumValueProperty, Node.createLiteral(checksum.getValue()));
		ExtendedIterator<Triple> checksumMatchIter = model.getGraph().find(checksumValueMatch);	
		while (checksumMatchIter.hasNext()) {
			Triple checksumMatchTriple = checksumMatchIter.next();
			Node checksumNode = checksumMatchTriple.getSubject();
			// check the algorithm
			Node algorithmProperty = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CHECKSUM_ALGORITHM).asNode();
			Triple algorithmMatch = Triple.createMatch(checksumNode, algorithmProperty, null);
			ExtendedIterator<Triple> algorithmMatchIterator = model.getGraph().find(algorithmMatch);
			if (algorithmMatchIterator.hasNext()) {
				String algorithm = "UNKNOWN";
				Triple algorithmMatchTriple = algorithmMatchIterator.next();
				if (algorithmMatchTriple.getObject().isLiteral()) {
					// The following is for compatibility with rdf generated with older
					// versions of the tool
					algorithm = algorithmMatchTriple.getObject().toString(false);
				} else if (algorithmMatchTriple.getObject().isURI()) {
					algorithm = URI_TO_ALGORITHM.get(algorithmMatchTriple.getObject().getURI());
					if (algorithm == null) {
						algorithm = "UNKNOWN";
					}
				}
				if (algorithm.equals(checksum.getAlgorithm())) {
					return RdfParserHelper.convertToResource(model, checksumNode);
				}
			}
		}
		// if we get to here, we did not find a match
		return null;
	}
	
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
			if (t.getObject().isLiteral()) {
				// The following is for compatibility with rdf generated with older
				// versions of the tool
				this.algorithm = t.getObject().toString(false);
			} else if (t.getObject().isURI()) {
				this.algorithm = URI_TO_ALGORITHM.get(t.getObject().getURI());
				if (this.algorithm == null) {
					this.algorithm = "UNKNOWN";
				}
			} else {
				throw(new InvalidSPDXAnalysisException("Invalid checksum algorithm - must be one of the defined algorithms supported by SPDX."));
			}
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
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setAlgorithm(String algorithm) throws InvalidSPDXAnalysisException {
		this.algorithm = algorithm;
		if (this.model != null && this.checksumNode != null) {
			Resource algResource = algorithmStringToResource(algorithm, this.model);
			// delete any previous algorithm
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CHECKSUM_ALGORITHM);
			model.removeAll(checksumResource, p, null);
			// add the property
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CHECKSUM_ALGORITHM);
			checksumResource.addProperty(p, algResource);
		}
	}

	/**
	 * Converts a string algorithm to an RDF resource
	 * @param algorithm
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	public static Resource  algorithmStringToResource(String algorithm, Model model) throws InvalidSPDXAnalysisException {
		String resourceUri = ALGORITHM_TO_URI.get(algorithm);
		if (resourceUri == null) {
			throw(new InvalidSPDXAnalysisException("Invalid algorithm: "+algorithm));
		}
		Resource retval = model.createResource(resourceUri);
		return retval;
	}
	
	public static String algorithmResourceToString(Resource algorithmResource) throws InvalidSPDXAnalysisException {
		String uri = algorithmResource.getURI();
		if (!algorithmResource.isURIResource()) {
			throw(new InvalidSPDXAnalysisException("Algorithm resource must be a URI"));
		}
		String retval = URI_TO_ALGORITHM.get(uri);
		if (retval == null) {
			throw(new InvalidSPDXAnalysisException("Invalid algorithm resource."));
		}
		return retval;
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
		
		Resource r;
		try {
			r = findSpdxChecksum(model, this);
		} catch (InvalidSPDXAnalysisException e) {
			// if we run into an error finding the checksum, we'll just create a new one
			r = null;
		}		// prevent duplicate checksum objects
		if (r == null) {
			r = model.createResource(type);
		}
		if (algorithm != null) {
			Property algProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_CHECKSUM_ALGORITHM);
			Resource algResource = model.createResource(ALGORITHM_TO_URI.get(algorithm));
			r.addProperty(algProperty, algResource);
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
			if (!ALGORITHM_TO_URI.containsKey(algorithm)) {
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
	
	@Override 
	public boolean equals(Object o) {
		if (!(o instanceof SPDXChecksum)) {
			return false;
		}
		SPDXChecksum compare = (SPDXChecksum)o;
		if (!compare.getAlgorithm().equals(this.getAlgorithm())) {
			return false;
		}
		return compare.getValue().compareToIgnoreCase(this.getValue()) == 0;
	}

	/**
	 * @return
	 */
	public Resource getResource() {
		return this.checksumResource;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public SPDXChecksum clone() {
		return new SPDXChecksum(this.algorithm, this.value);
	}
}
