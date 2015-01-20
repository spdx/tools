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

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.SpdxVerificationHelper;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * An Annotation is a comment on an SpdxItem by an agent.
 * @author Gary O'Neall
 *
 */
public class Annotation extends RdfModelObject {
	
	static final Logger logger = Logger.getLogger(RdfModelObject.class.getName());

	public enum AnnotationType {annotationType_other, annotationType_review};
	AnnotationType annotationType;
	String annotator;
	String comment;
	String date;
	
	public Annotation(String annotator, AnnotationType annotationType, String date,
			String comment) {
		super();
		this.annotator = annotator;
		this.annotationType = annotationType;
		this.date = date;
		this.comment = comment;
	}
	
	public Annotation(IModelContainer modelContainer, Node annotationNode) throws InvalidSPDXAnalysisException {
		super(modelContainer, annotationNode);
		//annotator
		this.annotator = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_ANNOTATOR);

		//Date
		this.date = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_DATE);
		//Comment
		this.comment = findSinglePropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
		//Annotation type
		String annotationTypeUri = findUriPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_ANNOTATION_TYPE);
		if (annotationTypeUri != null) {
			String sAnnotationType = annotationTypeUri.substring(SpdxRdfConstants.SPDX_NAMESPACE.length());
			try {
				this.annotationType = AnnotationType.valueOf(sAnnotationType);
			} catch (Exception ex) {
				logger.error("Invalid annotation type found in the model: "+sAnnotationType);
				throw(new InvalidSPDXAnalysisException("Invalid annotation type: "+sAnnotationType));
			}
		}
	}
	
	@Override
	public Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_ANNOTATION);
	}

	/**
	 * Populate the model from the properties
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Override
	protected void populateModel() throws InvalidSPDXAnalysisException {
		if (annotationType != null) {
			setPropertyUriValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_ANNOTATION_TYPE, 
					SpdxRdfConstants.SPDX_NAMESPACE + this.annotationType.toString());
		}
		if (annotator != null) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_ANNOTATOR, annotator);
		}
		if (comment != null) {
			setPropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT, comment);
		}
		if (date != null) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_DATE, date);
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#verify()
	 */
	@Override
	public ArrayList<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();
		if (annotationType == null) {
			retval.add("Missing annotationtype for Annotation");
		}
		if (annotator == null) {
			retval.add("Missing annotator for Annotation");
		}
		if (comment == null) {
			retval.add("Missing comment for Annotation");
		}
		if (date == null) {
			retval.add("Missing date for Annotation");
		} else {
			String dateVerify = SpdxVerificationHelper.verifyDate(date);
			if (dateVerify != null && !dateVerify.isEmpty()) {
				retval.add(dateVerify);
			}
		}
		return retval;
	}

	/**
	 * @return the annotationType
	 */
	public AnnotationType getAnnotationType() {
		if (this.resource != null) {
			String annotationTypeUri = findUriPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_ANNOTATION_TYPE);
			if (annotationTypeUri != null) {
				String sAnnotationType = annotationTypeUri.substring(SpdxRdfConstants.SPDX_NAMESPACE.length());
				try {
					this.annotationType = AnnotationType.valueOf(sAnnotationType);
				} catch (Exception ex) {
					logger.error("Invalid annotation type found in the model - "+sAnnotationType);
				}
			}
		}
		return annotationType;
	}

	/**
	 * @param annotationType the annotationType to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setAnnotationType(AnnotationType annotationType) throws InvalidSPDXAnalysisException {
		this.annotationType = annotationType;
		if (annotationType != null) {
			setPropertyUriValue(SpdxRdfConstants.SPDX_NAMESPACE, 
					SpdxRdfConstants.PROP_ANNOTATION_TYPE, 
					SpdxRdfConstants.SPDX_NAMESPACE + annotationType.toString());
		} else {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_ANNOTATION_TYPE);
		}
	}

	/**
	 * @return the annotator
	 */
	public String getAnnotator() {
		if (this.resource != null) {
			this.annotator = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_ANNOTATOR);
		}
		return annotator;
	}

	/**
	 * @param annotator the annotator to set
	 */
	public void setAnnotator(String annotator) {
		this.annotator = annotator;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_ANNOTATOR, annotator);
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		if (this.resource != null) {
			this.comment = findSinglePropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT);
		}
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
		setPropertyValue(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT, comment);
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		if (this.resource != null) {
			date = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_DATE);
		}
		return date;
	}

	/**
	 * @param date the date to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setDate(String date) throws InvalidSPDXAnalysisException {
		this.date = date;
		if (date != null) {
			String dateVerify = SpdxVerificationHelper.verifyDate(date);
			if (dateVerify != null && !dateVerify.isEmpty()) {
				throw(new InvalidSPDXAnalysisException("Invalid date format: "+dateVerify));
			}
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_DATE, date);
		} else {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_DATE);
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getUri(org.spdx.rdfparser.IModelContainer)
	 */
	@Override
	String getUri(IModelContainer modelContainer) {
		// We will just use anonymous nodes for Annotations
		return null;
	}

	public Annotation clone() {
		return new Annotation(this.annotator, this.annotationType, this.date, 
				this.comment);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#equivalent(org.spdx.rdfparser.model.RdfModelObject)
	 */
	@Override
	public boolean equivalent(RdfModelObject o) {
		if (!(o instanceof Annotation)) {
			return false;
		}
		Annotation comp = (Annotation)o;
		return (equalsConsideringNull(annotator, comp.getAnnotator()) &&
				equalsConsideringNull(annotationType, comp.getAnnotationType()) &&
				equalsConsideringNull(comment, comp.getComment()) &&
				equalsConsideringNull(date, comp.getDate()));
	}
	
}
