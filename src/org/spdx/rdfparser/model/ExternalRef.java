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
package org.spdx.rdfparser.model;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.RdfParserHelper;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.referencetype.ReferenceType;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
/**
 * An External Reference allows a Package to reference an external source of
 * additional information, metadata, enumerations, asset identifiers, or downloadable content believed to
 * be relevant to the Package.
 * 
 * @author Gary O'Neall
 *
 */
public class ExternalRef extends RdfModelObject implements Comparable<ExternalRef> {
	
	static final Logger logger = LoggerFactory.getLogger(ExternalRef.class);
	
	public enum ReferenceCategory {referenceCategory_packageManager, referenceCategory_security,
		referenceCategory_other;
		public String getTag() {
			switch(this) {
			case referenceCategory_packageManager: return "PACKAGE-MANAGER";
			case referenceCategory_security: return "SECURITY";
			case referenceCategory_other: return "OTHER";
			default: return "OTHER";
			}
		}
		public static ReferenceCategory fromTag(String tag) {
			String uTag = tag.toUpperCase();
			if ("PACKAGE-MANAGER".equals(uTag)) {
				return referenceCategory_packageManager;
			} else if ("SECURITY".equals(uTag)) {
				return referenceCategory_security;
			} else if ("OTHER".equals(uTag)) {
				return referenceCategory_other;
			} else {
				return null;
			}
		}
	}
	
	private ReferenceCategory referenceCategory;
	private ReferenceType referenceType;
	private String referenceLocator;
	private String comment;
	
	/**
	 * @param referenceCategory Category for the external reference
	 * @param referenceType Reference type as defined in the SPDX appendix
	 * @param referenceLocator unique string with no spaces necessary to access the packagespecific
     * information, metadata, or content within the target location. The format of the locator is subject
     * to constraints defined by the referenceType
	 */
	public ExternalRef(ReferenceCategory referenceCategory, ReferenceType referenceType,
			String referenceLocator, String comment) {
		this.referenceCategory = referenceCategory;
		this.referenceType = referenceType;
		this.referenceLocator = referenceLocator;
		this.comment = comment;
	}
	
	/**
	 * @param modelContainer Container for the RDF model
	 * @param node Node for this external references
	 * @throws InvalidSPDXAnalysisException
	 */
	public ExternalRef(IModelContainer modelContainer, Node node)
			throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		getPropertiesFromModel();
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#verify()
	 */
	@Override
	public List<String> verify() {
		List<String> retval = Lists.newArrayList();
		if (this.referenceCategory == null) {
			retval.add("Missing or invalid reference category");
		}
		if (this.referenceType == null) {
			retval.add("Missing or invalid reference type");
		} else {
			retval.addAll(this.referenceType.verify());
		}
		if (this.referenceLocator == null || this.referenceLocator.isEmpty()) {
			retval.add("Missing or invalid reference locator");
		} else if (this.referenceLocator.contains(" ")) {
			retval.add("Reference locator contains spaces");
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#equivalent(org.spdx.rdfparser.model.IRdfModel)
	 */
	@Override
	public boolean equivalent(IRdfModel compare) {
		if (compare == this) {
			return true;
		}
		if (!(compare instanceof ExternalRef)) {
			return false;
		}
		// Note - we consider references equiv. even if the comments differ
		ExternalRef erCompare = (ExternalRef)compare;
		try {
			return Objects.equal(this.getReferenceCategory(), erCompare.getReferenceCategory()) &&
					Objects.equal(this.getReferenceLocator(), erCompare.getReferenceLocator()) &&
					this.equivalentConsideringNull(this.getReferenceType(), erCompare.getReferenceType());
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Error getting equiv. data",e);
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	public void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		// referenceCategory
		String categoryUri = findUriPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_REFERENCE_CATEGORY);
		if (categoryUri != null && !categoryUri.isEmpty()) {
			if (!categoryUri.startsWith(SpdxRdfConstants.SPDX_NAMESPACE)) {
				throw(new InvalidSPDXAnalysisException("Invalid reference category: "+categoryUri));
			}
			String categoryS = categoryUri.substring(SpdxRdfConstants.SPDX_NAMESPACE.length());
			try {
				this.referenceCategory = ReferenceCategory.valueOf(categoryS);
			} catch (Exception ex) {
				logger.error("Invalid reference category in the model - "+categoryS);
				throw(new InvalidSPDXAnalysisException("Invalid referenceCategory: "+categoryS));
			}
		}
		// referenceType
		this.referenceType = findReferenceTypePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_REFERENCE_TYPE);
		// referenceLocator
		this.referenceLocator = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_REFERENCE_LOCATOR);
		// comment
		this.comment = findSinglePropertyValue(SpdxRdfConstants.RDFS_NAMESPACE,
				SpdxRdfConstants.RDFS_PROP_COMMENT);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getUri(org.spdx.rdfparser.IModelContainer)
	 */
	@Override
	public String getUri(IModelContainer modelContainer)
			throws InvalidSPDXAnalysisException {
		// Use anon. nodes
		return null;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(org.apache.jena.rdf.model.Model)
	 */
	@Override
	public Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_EXTERNAL_REFERENCE);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#populateModel()
	 */
	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
		// referenceCategory
		if (this.referenceCategory == null) {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_REFERENCE_CATEGORY);
		} else {
			setPropertyUriValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_REFERENCE_CATEGORY, 
					SpdxRdfConstants.SPDX_NAMESPACE + this.referenceCategory.toString());
		}
		// referenceType
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_REFERENCE_TYPE, this.referenceType);
		// referenceLocator
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_REFERENCE_LOCATOR, this.referenceLocator);
		// comment
		setPropertyValue(SpdxRdfConstants.RDFS_NAMESPACE,
				SpdxRdfConstants.RDFS_PROP_COMMENT, this.comment);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ExternalRef o) {
		
		ReferenceType myReferenceType = null;
		int retval = 0;
		try {
			myReferenceType = this.getReferenceType();
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Invalid reference type during compare",e);
		}
		ReferenceType compRefType = null;
		try {
			compRefType = o.getReferenceType();
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Invalid reference type during compare",e);
		}
		if (myReferenceType == null) {
			if (compRefType != null) {
				retval = 1;
			}
		} else if (compRefType == null) { 
			retval = -1;
		} else {
			retval = myReferenceType.compareTo(compRefType);
		}
		if (retval == 0) {
			String myReferenceLocator = this.getReferenceLocator();
			if (myReferenceLocator == null) {
				if (o.getReferenceLocator() != null) {
					retval = 1;
				}
			} else if (o.getReferenceLocator() == null) { 
				retval = -1;
			} else {
				retval = myReferenceLocator.compareTo(o.getReferenceLocator());
			}
		}
		if (retval == 0) {
			if (this.getReferenceCategory() == null) {
				if (o.getReferenceCategory() != null) {
					return 1;
				} else {
					return 0;
				}
			} else {
				retval = this.referenceCategory.toString().compareTo(o.getReferenceCategory().toString());
			}
		}
		if (retval == 0) {
			String myComment = this.getComment();
			if (myComment == null) {
				if (o.getComment() != null) {
					retval = 1;
				}
			} else if (o.getComment() == null) {
				retval = -1;
			} else {
				retval = myComment.compareTo(o.getComment());
			}
		}
		return retval;
	}
	
	@Override
	public ExternalRef clone() {
		return new ExternalRef(this.referenceCategory, this.referenceType,
				this.referenceLocator, this.comment);
	}
	
	/**
	 * @return the comment
	 */
	public String getComment() {
		if (this.resource != null && this.refreshOnGet) {
			this.comment = findSinglePropertyValue(SpdxRdfConstants.RDFS_NAMESPACE,
				SpdxRdfConstants.RDFS_PROP_COMMENT);
		}
		return this.comment;
	}
	
	public void setComment(String comment) {
		if (this.comment == null) {
			removePropertyValue(SpdxRdfConstants.RDFS_NAMESPACE,
				SpdxRdfConstants.RDFS_PROP_COMMENT);
		} else {
			setPropertyValue(SpdxRdfConstants.RDFS_NAMESPACE,
				SpdxRdfConstants.RDFS_PROP_COMMENT, comment);
		}
		this.comment = comment;
	}

	/**
	 * @return the referenceCategory
	 */
	public ReferenceCategory getReferenceCategory() {
		if (this.resource != null && this.refreshOnGet) {
			String categoryUri = findUriPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_REFERENCE_CATEGORY);
			if (categoryUri != null && !categoryUri.isEmpty()) {
				if (!categoryUri.startsWith(SpdxRdfConstants.SPDX_NAMESPACE)) {
					logger.error("Invalid reference category in the model - "+categoryUri);
				} else {
					String categoryS = categoryUri.substring(SpdxRdfConstants.SPDX_NAMESPACE.length());
					try {
						this.referenceCategory = ReferenceCategory.valueOf(categoryS);
					} catch (Exception ex) {
						logger.error("Invalid reference category in the model - "+categoryS);
						this.referenceCategory = null;
					}
				}
			} else {
				this.referenceCategory = null;
			}
		}
		return referenceCategory;
	}

	/**
	 * @param referenceCategory the referenceCategory to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setReferenceCategory(ReferenceCategory referenceCategory) throws InvalidSPDXAnalysisException {
		this.referenceCategory = referenceCategory;
		if (referenceCategory == null) {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_REFERENCE_CATEGORY);
		} else {
			setPropertyUriValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_REFERENCE_CATEGORY, 
					SpdxRdfConstants.SPDX_NAMESPACE + this.referenceCategory.toString());
		}
	}

	/**
	 * @return the referenceType
	 * @throws InvalidSPDXAnalysisException 
	 */
	public ReferenceType getReferenceType() throws InvalidSPDXAnalysisException {
		if (this.resource != null && this.refreshOnGet) {
			referenceType = findReferenceTypePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_REFERENCE_TYPE);
		}
		return referenceType;
	}

	/**
	 * @param referenceType the referenceType to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setReferenceType(ReferenceType referenceType) throws InvalidSPDXAnalysisException {
		this.referenceType = referenceType;
		if (referenceType == null) {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_REFERENCE_TYPE);
		} else {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_REFERENCE_TYPE, 
					referenceType);
		}
	}

	/**
	 * @return the referenceLocator
	 */
	public String getReferenceLocator() {
		if (this.resource != null && this.refreshOnGet) {
			referenceLocator = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_REFERENCE_LOCATOR);
		}
		return referenceLocator;
	}

	/**
	 * @param referenceLocator the referenceLocator to set
	 */
	public void setReferenceLocator(String referenceLocator) {
		this.referenceLocator = referenceLocator;
		if (referenceLocator == null) {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_REFERENCE_LOCATOR);
		} else {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_REFERENCE_LOCATOR, 
					referenceLocator);
		}
	}
	
	@Override
	public Resource findDuplicateResource(IModelContainer modelContainer, String uri) throws InvalidSPDXAnalysisException {
		if (referenceCategory == null || referenceType == null || referenceLocator == null) {
			return null;
		}
		Node referenceLocatorProperty = modelContainer.getModel().getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_REFERENCE_LOCATOR).asNode();
		Triple referenceLocatorMatch = Triple.createMatch(null, referenceLocatorProperty, NodeFactory.createLiteral(this.referenceLocator));
		ExtendedIterator<Triple> referenceMatchIter = modelContainer.getModel().getGraph().find(referenceLocatorMatch);	
		while (referenceMatchIter.hasNext()) {
			Triple referenceMatchTriple = referenceMatchIter.next();
			Node referenceNode = referenceMatchTriple.getSubject();
			// Check the type and category
			Node typeProperty = modelContainer.getModel().getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_REFERENCE_TYPE).asNode();
			Node categoryProperty = modelContainer.getModel().getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_REFERENCE_CATEGORY).asNode();
			Triple typeMatch = Triple.createMatch(referenceNode, typeProperty, null);
			Triple categoryMatch = Triple.createMatch(referenceNode, categoryProperty, null);
			ExtendedIterator<Triple> typeMatchIterator = modelContainer.getModel().getGraph().find(typeMatch);
			ExtendedIterator<Triple> categoryMatchIterator = modelContainer.getModel().getGraph().find(categoryMatch);
			if (typeMatchIterator.hasNext()) {
				Triple typeMatchTriple = typeMatchIterator.next();
				if (typeMatchTriple.getObject() != null && typeMatchTriple.getObject().isURI()) {
					if (this.referenceType.getReferenceTypeUri().toString().equals(typeMatchTriple.getObject().getURI())) {
						// check category
						if (categoryMatchIterator.hasNext()) {
							Triple categoryMatchTriple = categoryMatchIterator.next();
							if (categoryMatchTriple.getObject() != null && categoryMatchTriple.getObject().isURI()) {
								String categoryUri = categoryMatchTriple.getObject().getURI();
								if (categoryUri != null && categoryUri.length() > SpdxRdfConstants.SPDX_NAMESPACE.length()) {
									if (this.referenceCategory.toString().equals(categoryUri.substring(SpdxRdfConstants.SPDX_NAMESPACE.length()))) {
										return RdfParserHelper.convertToResource(modelContainer.getModel(), referenceNode);
									}
								}
							}
						}
					}
				}
			}
		}
		// if we get to here, we did not find a match
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this.referenceCategory == null) {
			sb.append("[NONE] ");
		} else {
			sb.append(this.referenceCategory.getTag());
			sb.append(' ');
		}
		if (this.referenceType == null) {
			sb.append("[NONE] ");
		} else {
			sb.append(this.referenceType.toString());
			sb.append(' ');
		}
		if (this.referenceLocator == null) {
			sb.append("[NONE]");
		} else {
			sb.append(this.referenceLocator);
		}
		if (this.comment != null) {
			sb.append(" (");
			sb.append(this.comment);
			sb.append(")");
		}
		return sb.toString();
	}
}
