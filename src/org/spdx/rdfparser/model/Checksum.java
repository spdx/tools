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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.RdfParserHelper;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.SpdxVerificationHelper;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * A Checksum is value that allows the contents of a file to be authenticated. 
 * Even small changes to the content of the file will change its checksum. 
 * This class allows the results of a variety of checksum and cryptographic 
 * message digest algorithms to be represented.
 * 
 * @author Gary O'Neall
 *
 */
public class Checksum extends RdfModelObject implements Comparable<Checksum> {

	static final Logger logger = LoggerFactory.getLogger(Checksum.class);
	public enum ChecksumAlgorithm {checksumAlgorithm_sha1, checksumAlgorithm_sha256,
		checksumAlgorithm_md5};		
		
	// Mapping tables for Checksum Algorithms
	public static final ImmutableMap<ChecksumAlgorithm, String> CHECKSUM_ALGORITHM_TO_TAG = 
			new ImmutableMap.Builder<ChecksumAlgorithm, String>()
				.put(ChecksumAlgorithm.checksumAlgorithm_md5, "MD5:")
				.put(ChecksumAlgorithm.checksumAlgorithm_sha1, "SHA1:")
				.put(ChecksumAlgorithm.checksumAlgorithm_sha256, "SHA256:")
				.build();
	public static final ImmutableMap<String, ChecksumAlgorithm> CHECKSUM_TAG_TO_ALGORITHM = 
			new ImmutableMap.Builder<String, ChecksumAlgorithm>()
			.put("MD5:", ChecksumAlgorithm.checksumAlgorithm_md5)
			.put("SHA1:", ChecksumAlgorithm.checksumAlgorithm_sha1)
			.put("SHA256:", ChecksumAlgorithm.checksumAlgorithm_sha256)
			.build();

	ChecksumAlgorithm algorithm;
	String checksumValue;
	
	protected static Resource findSpdxChecksum(Model model, Checksum checksum) throws InvalidSPDXAnalysisException {
		// find any matching checksum values
		if (checksum == null || checksum.algorithm == null || checksum.checksumValue == null) {
			return null;
		}
		Node checksumValueProperty = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CHECKSUM_VALUE).asNode();
		Triple checksumValueMatch = Triple.createMatch(null, checksumValueProperty, NodeFactory.createLiteral(checksum.getValue()));
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
				if (algorithmMatchTriple.getObject().isURI()) {
					algorithm = algorithmMatchTriple.getObject().getURI().substring(SpdxRdfConstants.SPDX_NAMESPACE.length());
					if (algorithm == null) {
						algorithm = "UNKNOWN";
					}
					if (algorithm.equals(checksum.getAlgorithm().toString())) {
						return RdfParserHelper.convertToResource(model, checksumNode);
					}
				}
			}
		}
		// if we get to here, we did not find a match
		return null;
	}
	
	public Checksum(ChecksumAlgorithm algorithm, String checksumValue) {
		this.algorithm = algorithm;
		this.checksumValue = checksumValue;
	}

	/**
	 * @param modelContainer
	 * @param node
	 * @throws InvalidSPDXAnalysisException
	 */
	public Checksum(IModelContainer modelContainer, Node node)
			throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		getPropertiesFromModel();
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	public void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		// Algorithm
		String algorithmUri = findUriPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_CHECKSUM_ALGORITHM);
		if (algorithmUri != null && !algorithmUri.isEmpty()) {
			if (!algorithmUri.startsWith(SpdxRdfConstants.SPDX_NAMESPACE)) {
				throw(new InvalidSPDXAnalysisException("Invalid checksum algorithm: "+algorithmUri));
			}
			String algorithmS = algorithmUri.substring(SpdxRdfConstants.SPDX_NAMESPACE.length());
			try {
				this.algorithm = ChecksumAlgorithm.valueOf(algorithmS);
			} catch (Exception ex) {
				logger.error("Invalid checksum algorithm in the model - "+algorithmS);
				throw(new InvalidSPDXAnalysisException("Invalid checksum algorithm: "+algorithmS));
			}
		}
		// Value
		this.checksumValue = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_CHECKSUM_VALUE);
	}

	/**
	 * @return the algorithm
	 */
	public ChecksumAlgorithm getAlgorithm() {
		if (this.resource != null && this.refreshOnGet) {
			String algorithmUri = findUriPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_CHECKSUM_ALGORITHM);
			if (algorithmUri != null && !algorithmUri.isEmpty()) {
				if (!algorithmUri.startsWith(SpdxRdfConstants.SPDX_NAMESPACE)) {
					logger.error("Invalid checksum algorithm in the model - "+algorithmUri);
				} else {
					String algorithmS = algorithmUri.substring(SpdxRdfConstants.SPDX_NAMESPACE.length());
					try {
						this.algorithm = ChecksumAlgorithm.valueOf(algorithmS);
					} catch (Exception ex) {
						logger.error("Invalid checksum algorithm in the model - "+algorithmS);
						algorithm = null;
					}
				}
			} else {
				algorithm = null;
			}
		}
		return algorithm;
	}

	/**
	 * @param algorithm the algorithm to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setAlgorithm(ChecksumAlgorithm algorithm) throws InvalidSPDXAnalysisException {
		this.algorithm = algorithm;
		if (algorithm == null) {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_CHECKSUM_ALGORITHM);
		} else {
			setPropertyUriValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_CHECKSUM_ALGORITHM, 
					SpdxRdfConstants.SPDX_NAMESPACE + this.algorithm.toString());
		}
	}

	/**
	 * @return the checksumValue
	 */
	public String getValue() {
		if (this.resource != null && this.refreshOnGet) {
			this.checksumValue = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_CHECKSUM_VALUE);
		}
		return checksumValue;
	}

	/**
	 * @param checksumValue the checksumValue to set
	 */
	public void setValue(String checksumValue) {
		this.checksumValue = checksumValue;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_CHECKSUM_VALUE, checksumValue);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#verify()
	 */
	@Override
	public List<String> verify() {
		List<String> retval = Lists.newArrayList();
		if (this.algorithm == null) {
			retval.add("Missing required algorithm");
		}
		if (this.checksumValue == null || this.checksumValue.isEmpty()) {
			retval.add("Missing required checksum value");
		} else {
			String verify = SpdxVerificationHelper.verifyChecksumString(this.checksumValue, this.algorithm);
			if (verify != null) {
				retval.add(verify);
			}
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getUri(org.spdx.rdfparser.IModelContainer)
	 */
	@Override
	public String getUri(IModelContainer modelContainer) {
		// Use anonomous nodes
		return null;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(org.apache.jena.rdf.model.Model)
	 */
	@Override
	public Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_CHECKSUM);
	}

	@Override
	public Resource findDuplicateResource(IModelContainer modelContainer, String uri) throws InvalidSPDXAnalysisException {
		// see if we want to change what is considered a duplicate
		// currently, a file is considered a duplicate if the checksum and filename
		// are the same.
		return findSpdxChecksum(modelContainer.getModel(), this);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#populateModel()
	 */
	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
		// algorithm
		if (algorithm == null) {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_CHECKSUM_ALGORITHM);
		} else {
			setPropertyUriValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_CHECKSUM_ALGORITHM, 
					SpdxRdfConstants.SPDX_NAMESPACE + this.algorithm.toString());
		}
		// value
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_CHECKSUM_VALUE, checksumValue);
	}
	
	@Override
	public Checksum clone() {
		return new Checksum(this.algorithm, this.checksumValue);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#equivalent(org.spdx.rdfparser.model.RdfModelObject)
	 */
	@Override
	public boolean equivalent(IRdfModel compare) {
		if (compare == this) {
			return true;
		}
		if (!(compare instanceof Checksum)) {
			return false;
		}
		Checksum cksum = (Checksum)compare;
        return (Objects.equal(this.getAlgorithm(), cksum.getAlgorithm()) && Objects.equal(this.getValue(), cksum.getValue()));
	}
	
	@Override
	public String toString() {
		if (this.algorithm == null || this.checksumValue == null) {
			return "";
		} else {
			return (CHECKSUM_ALGORITHM_TO_TAG.get(this.algorithm)+" "+this.checksumValue);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Checksum compare) {
		int retval = 0;
		if (this.getAlgorithm() == null) {
			if (compare.getAlgorithm() != null) {
				retval = 1;
			} else {
				retval = 0;
			}
		} else {
			if (compare.getAlgorithm() == null) {
				retval = -1;
			} else {
				retval = Checksum.CHECKSUM_ALGORITHM_TO_TAG.get(this.getAlgorithm()).compareTo(
						Checksum.CHECKSUM_ALGORITHM_TO_TAG.get(compare.getAlgorithm()));
			}
			
		} 
		if (retval == 0) {
			if (this.getValue() == null) {
				if (compare.getValue() != null) {
					retval = 1;
				} else {
					retval = 0;
				}
			} else {
				if (compare.getValue() == null) {
					retval = -1;
				} else {
					retval = this.getValue().compareTo(compare.getValue());
				}
			}
		}
		return retval;
	}
}
