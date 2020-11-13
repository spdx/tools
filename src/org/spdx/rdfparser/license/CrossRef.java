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
package org.spdx.rdfparser.license;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.IRdfModel;
import org.spdx.rdfparser.model.RdfModelObject;

/**
 * Cross reference details for the a URL reference
 * 
 * @author Gary O'Neall
 *
 */
public class CrossRef extends RdfModelObject {
	
	String match;
	String url;
	Boolean isValid;
	Boolean isLive;
	String timestamp;
	Boolean isWayBackLink;
	Integer order;
	
	/**
	 * @param url SeeAlso URL which relates to a license
	 * @param isValid true if the URL is a valid URL
	 * @param isLive true if the web page is accessible on the public internet
	 * @param isWayBackLink true if the URL is a Wayback machine link
	 * @param match true if the website contains license text which matches the cross reference
	 * @param timestamp datetime when the Cross ref detailed information was generated
	 * @param order the order in which this cross ref should be displayed - 0 being displayed first
	 */
	public CrossRef(String url, @Nullable Boolean isValid, @Nullable Boolean isLive, @Nullable Boolean isWayBackLink, 
			@Nullable String match, @Nullable String timestamp, @Nullable Integer order) {
		this.url = url;
		this.isValid = isValid;
		this.isLive = isLive;
		this.isWayBackLink = isWayBackLink;
		this.match = match;
		this.timestamp = timestamp;
		this.order = order;
	}

	/**
	 * @param modelContainer
	 * @param node
	 * @throws InvalidSPDXAnalysisException
	 */
	public CrossRef(IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		getPropertiesFromModel();
	}

	/**
	 * All null details
	 */
	public CrossRef(String url) {
		this(url, null, null, null, null, null, null);
	}


	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#verify()
	 */
	@Override
	public List<String> verify() {
		List<String> retval = new ArrayList<>();
		if (Objects.isNull(url)) {
			retval.add("URL is Null");
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#equivalent(org.spdx.rdfparser.model.IRdfModel)
	 */
	@Override
	public boolean equivalent(IRdfModel compare) {
		if (compare instanceof CrossRef) {
			CrossRef compCrossRef = (CrossRef)compare;
			return Objects.equals(url, compCrossRef.getUrl());
		} else {
			return false;
		}
	}
	
	/**
	 * @param namespace
	 * @param propertyName
	 * @return the property boolean value if found
	 * @throws InvalidSPDXAnalysisException 
	 */
	private @Nullable Boolean findBooleanPropertyValue(String namespace, String propertyName) throws InvalidSPDXAnalysisException {
		String textValue = findSinglePropertyValue(namespace, propertyName);
		if (textValue != null) {
			textValue = textValue.trim();
			if (textValue.equals("true") || textValue.equals("1")) {
				return new Boolean(true);
			} else if (textValue.equals("false") || textValue.equals("0")) {
				return new Boolean(false);
			} else {
				throw(new InvalidSPDXAnalysisException("Invalid value for boolean {true, false, 0, 1}"));
			}
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	public void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		// match
		this.match = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_MATCH);
		// url
		this.url = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_URL);
		// isValid
		this.isValid = findBooleanPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_IS_VALID);
		// isLive
		this.isLive = findBooleanPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_IS_LIVE);
		// timestamp
		this.timestamp = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_TIMESTAMP);
		// isWayBackLink
		this.isWayBackLink = findBooleanPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_WAYBACK_LINK);
		// order
		String orderText = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_ORDER);
		if (Objects.nonNull(orderText)) {
			try {
				this.order = Integer.parseInt(orderText);
			} catch(NumberFormatException ex) {
				throw new InvalidSPDXAnalysisException("Invalid value for order - could not convert to integer: "+orderText, ex);
			}
		} else {
			this.order = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getUri(org.spdx.rdfparser.IModelContainer)
	 */
	@Override
	public String getUri(IModelContainer modelContainer) throws InvalidSPDXAnalysisException {
		// Use anonomous nodes
		return null;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(org.apache.jena.rdf.model.Model)
	 */
	@Override
	public Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_CROSS_REF);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#populateModel()
	 */
	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
		// url
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_URL, this.url);
		// isValid
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_IS_VALID);
		if (Objects.nonNull(this.isValid)) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_IS_VALID, this.isValid.toString());
		}
		// isLive
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_IS_LIVE);
		if (Objects.nonNull(this.isValid)) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_IS_LIVE, this.isLive.toString());
		}
		// isWaybackLink
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_WAYBACK_LINK);
		if (Objects.nonNull(this.isValid)) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_WAYBACK_LINK, this.isWayBackLink.toString());
		}
		// match
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_MATCH, this.match);
		// timestamp
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_TIMESTAMP, this.timestamp);
		// order
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_ORDER);
		if (Objects.nonNull(this.order)) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_ORDER, this.order.toString());
		}
	}

	/**
	 * @return the match
	 */
	public String getMatch() {
		if (this.resource != null && this.refreshOnGet) {
			this.match = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_MATCH);
		}
		return match;
	}

	/**
	 * @param match the match to set
	 */
	public void setMatch(String match) {
		this.match = match;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_MATCH, this.match);
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		if (this.resource != null && this.refreshOnGet) {
			this.url = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_URL);
		}
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_URL, this.url);
	}

	/**
	 * @return the isValid
	 * @throws InvalidSPDXAnalysisException 
	 */
	public Boolean isValid() throws InvalidSPDXAnalysisException {
		if (this.resource != null && this.refreshOnGet) {
			this.isValid = findBooleanPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_IS_VALID);
		}
		return isValid;
	}

	/**
	 * @param isValid the isValid to set
	 */
	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_IS_VALID);
		if (Objects.nonNull(this.isValid)) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_IS_VALID, this.isValid.toString());
		}
	}

	/**
	 * @return the isLive
	 * @throws InvalidSPDXAnalysisException 
	 */
	public Boolean isLive() throws InvalidSPDXAnalysisException {
		if (this.resource != null && this.refreshOnGet) {
			this.isLive = findBooleanPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_IS_LIVE);
		}
		return isLive;
	}

	/**
	 * @param isLive the isLive to set
	 */
	public void setIsLive(Boolean isLive) {
		this.isLive = isLive;
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_IS_LIVE);
		if (Objects.nonNull(this.isLive)) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_IS_LIVE, this.isLive.toString());
		}
	}

	/**
	 * @return the timestamp
	 */
	public String getTimestamp() {
		if (this.resource != null && this.refreshOnGet) {
			this.timestamp = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_TIMESTAMP);
		}
		return timestamp;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_TIMESTAMP, this.timestamp);
	}

	/**
	 * @return the isWayBackLink
	 * @throws InvalidSPDXAnalysisException 
	 */
	public Boolean isWayBackLink() throws InvalidSPDXAnalysisException {
		if (this.resource != null && this.refreshOnGet) {
			this.isWayBackLink = findBooleanPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_WAYBACK_LINK);
		}
		return isWayBackLink;
	}

	/**
	 * @param isWayBackLink the isWayBackLink to set
	 */
	public void setIsWayBackLink(Boolean isWayBackLink) {
		this.isWayBackLink = isWayBackLink;
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_WAYBACK_LINK);
		if (Objects.nonNull(this.isWayBackLink)) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_WAYBACK_LINK, this.isWayBackLink.toString());
		}
	}

	/**
	 * @return the order
	 * @throws InvalidSPDXAnalysisException 
	 */
	public Integer getOrder() throws InvalidSPDXAnalysisException {
		if (this.resource != null && this.refreshOnGet) {
			String orderText = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_ORDER);
			if (Objects.nonNull(orderText)) {
				try {
					this.order = Integer.parseInt(orderText);
				} catch(NumberFormatException ex) {
					throw new InvalidSPDXAnalysisException("Invalid value for order - could not convert to integer: "+orderText, ex);
				}
			} else {
				this.order = null;
			}
		}
		return order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(Integer order) {
		this.order = order;
		removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_ORDER);
		if (Objects.nonNull(this.order)) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_CROSS_REF_ORDER, this.order.toString());
		}
	}
	
	/**
	 * Convenience method for setting details related to the URL checking
	 * @param isValid
	 * @param isLive
	 * @param isWayBackLink
	 * @param match
	 * @param timestamp
	 */
	public void setDetails(Boolean isValid, Boolean isLive, Boolean isWayBackLink, String match, String timestamp) {
		this.setIsValid(isValid);
		this.setIsLive(isLive);
		this.setIsWayBackLink(isWayBackLink);
		this.setMatch(match);
		this.setTimestamp(timestamp);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		String crossRefDetails = String.format("{%s: %s,%s: %b,%s: %b,%s: %b,%s: %s,%s: %s}",
				SpdxRdfConstants.PROP_CROSS_REF_URL, url,
				SpdxRdfConstants.PROP_CROSS_REF_IS_VALID, isValid,
				SpdxRdfConstants.PROP_CROSS_REF_IS_LIVE, isLive,
				SpdxRdfConstants.PROP_CROSS_REF_WAYBACK_LINK, isWayBackLink,
				SpdxRdfConstants.PROP_CROSS_REF_MATCH, match,
				SpdxRdfConstants.PROP_CROSS_REF_TIMESTAMP, timestamp);
		return crossRefDetails;
	}
}
