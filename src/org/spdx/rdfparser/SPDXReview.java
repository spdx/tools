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

import java.util.List;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Reviewer class for SPDX Analysis
 * @author Gary O'Neall
 *
 */
public class SPDXReview {
	private String reviewer = null;
	private String reviewDate = null;
	private String comment = null;
	private Model model = null;
	private Node reviewerNode = null;
	private Resource reviewerResource = null;
	
	@SuppressWarnings("deprecation")
	public SPDXReview(Model model, Node reviewerNode) throws InvalidSPDXAnalysisException {
		this.model = model;
		this.reviewerNode = reviewerNode;
		if (reviewerNode.isBlank()) {
			reviewerResource = model.createResource(reviewerNode.getBlankNodeId());
		} else if (reviewerNode.isURI()) {
			reviewerResource = model.createResource(reviewerNode.getURI());
		} else {
			throw(new InvalidSPDXAnalysisException("Can not have a Review node as a literal"));
		}
		
		//reviewer
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_REVIEW_REVIEWER).asNode();
		Triple m = Triple.createMatch(reviewerNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.reviewer = t.getObject().toString(false);
		}
		//Date
		p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_REVIEW_DATE).asNode();
		m = Triple.createMatch(reviewerNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.reviewDate = t.getObject().toString(false);
		}
		//Comment
		p = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT).asNode();
		m = Triple.createMatch(reviewerNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.comment = t.getObject().toString(false);
		}
	}
	
	/**
	 * @param reviewer
	 * @param date
	 * @param comment
	 */
	public SPDXReview(String reviewer, String date, String comment) {
		this.reviewer = reviewer;
		this.reviewDate = date;
		this.comment = comment;
	}

	public Resource createResource(Model model) {
		Resource type = model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_REVIEW);
		Resource retval = model.createResource(type);
		populateModel(model, retval);
		return retval;
	}

	/**
	 * @param model Jena model to populate
	 * @param projectResource Project resource to populate
	 */
	@SuppressWarnings("deprecation")
	private void populateModel(Model model, Resource reviewResource) {
		this.model = model;
		this.reviewerNode = reviewResource.asNode();
		this.reviewerResource = reviewResource;
		
		// Reviewer		
		if (reviewer != null) {
			Property p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_REVIEW_REVIEWER);
			reviewResource.addProperty(p, reviewer);
		}

		// Date
		if (reviewDate != null) {
			Property p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_REVIEW_DATE);
			reviewResource.addProperty(p, reviewDate);
		}	
		
		// Comment
		if (comment != null) {
			Property p = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
			reviewResource.addProperty(p, comment);
		}
	}

	/**
	 * @return the reviewer
	 */
	public String getReviewer() {
		return reviewer;
	}

	/**
	 * @param reviewer the reviewer to set
	 */
	@SuppressWarnings("deprecation")
	public void setReviewer(String reviewer) {
		this.reviewer = reviewer;
		if (this.reviewerNode != null && this.model != null) {
			Property p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_REVIEW_REVIEWER);
			model.removeAll(this.reviewerResource, p, null);
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_REVIEW_REVIEWER);
			this.reviewerResource.addProperty(p, reviewer);
		}
	}

	/**
	 * @return the reviewDate
	 */
	public String getReviewDate() {
		return reviewDate;
	}

	/**
	 * @param reviewDate the reviewDate to set
	 */
	@SuppressWarnings("deprecation")
	public void setReviewDate(String reviewDate) {
		this.reviewDate = reviewDate;
		if (this.reviewerNode != null && this.model != null) {
			Property p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_REVIEW_DATE);
			model.removeAll(this.reviewerResource, p, null);
			p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_REVIEW_DATE);
			this.reviewerResource.addProperty(p, reviewDate);
		}
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
		if (this.reviewerNode != null && this.model != null) {
			Property p = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
			model.removeAll(this.reviewerResource, p, null);
			if (comment != null) {
				p = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
				this.reviewerResource.addProperty(p, comment);
			}
		}
	}
	
	@Override
	public int hashCode() {
		int retval = 41;	// prime number
		if (this.getReviewer() != null) {
			retval = retval ^ this.getReviewer().hashCode();
		}
		if (this.getComment() != null) {
			retval = retval ^ this.getComment().hashCode();
		}
		if (this.getReviewDate() != null) {
			retval = retval ^ this.getReviewDate().hashCode();
		}
		return retval;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof SPDXReview)) {
			return false;
		}
		SPDXReview comp = (SPDXReview)o;
		if (this.getReviewer() == null) {
			if (comp.getReviewer() != null) {			
				return false;
			}
		} else {
			if (comp.getReviewer() == null || !this.getReviewer().equals(comp.getReviewer())) {
				return false;
			}
		}
		if (this.getComment() == null) {
			if (comp.getComment() != null) {
				return false;
			}
		} else {
			if (comp.getComment() == null || !this.getComment().equals(comp.getComment())) {
				return false;
			}
		}
		if (this.getReviewDate() == null) {
			if (comp.getReviewDate() != null) {
				return false;
			}
		} else {
			if (comp.getReviewDate() == null || !this.getReviewDate().equals(comp.getReviewDate())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return
	 */
	public List<String> verify() {
		List<String> retval = Lists.newArrayList();
		String reviewer = this.getReviewer();
		if (reviewer == null || reviewer.isEmpty()) {
			retval.add("Missing required reviewer");
		} else {
			String verify = SpdxVerificationHelper.verifyReviewer(reviewer);
			if (verify != null) {
				retval.add(verify);
			}
		}
		String reviewDate = this.getReviewDate();
		if (reviewDate == null || reviewDate.isEmpty()) {
			retval.add("Missing required review date");
		} else {
			String verify = SpdxVerificationHelper.verifyDate(reviewDate);
			if (verify != null) {
				retval.add(verify);
			}
		}

		return retval;
	}
}
