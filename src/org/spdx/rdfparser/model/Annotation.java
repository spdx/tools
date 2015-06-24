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
import java.util.Map;

import org.apache.log4j.Logger;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.SpdxVerificationHelper;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * An Annotation is a comment on an SpdxItem by an agent.
 * @author Gary O'Neall
 *
 */
public class Annotation extends RdfModelObject implements Comparable<Annotation> {
	
	static final Logger logger = Logger.getLogger(RdfModelObject.class.getName());

	public enum AnnotationType {annotationType_other, annotationType_review};
	
	public static final Map<AnnotationType, String> ANNOTATION_TYPE_TO_TAG = Maps.newHashMap();
	public static final Map<String, AnnotationType> TAG_TO_ANNOTATION_TYPE = Maps.newHashMap();
	static {
		ANNOTATION_TYPE_TO_TAG.put(AnnotationType.annotationType_other, "OTHER");
		TAG_TO_ANNOTATION_TYPE.put("OTHER", AnnotationType.annotationType_other);
		ANNOTATION_TYPE_TO_TAG.put(AnnotationType.annotationType_review, "REVIEW");
		TAG_TO_ANNOTATION_TYPE.put("REVIEW", AnnotationType.annotationType_review);
	}
	AnnotationType annotationType;
	String annotator;
	String comment;
	String annotationDate;
	
	public Annotation(String annotator, AnnotationType annotationType, String date,
			String comment) {
		super();
		this.annotator = annotator;
		this.annotationType = annotationType;
		this.annotationDate = date;
		this.comment = comment;
	}
	
	public Annotation(IModelContainer modelContainer, Node annotationNode) throws InvalidSPDXAnalysisException {
		super(modelContainer, annotationNode);
		getPropertiesFromModel();
	}
	

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		//annotator
		this.annotator = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_ANNOTATOR);

		//Date
		this.annotationDate = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_ANNOTATION_DATE);
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
		if (annotationDate != null) {
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_ANNOTATION_DATE, annotationDate);
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.IRdfModel#verify()
	 */
	@Override
	public List<String> verify() {
		List<String> retval = Lists.newArrayList();
		if (annotationType == null) {
			retval.add("Missing annotationtype for Annotation");
		}
		if (annotator == null) {
			retval.add("Missing annotator for Annotation");
		} else {
			String v = SpdxVerificationHelper.verifyAnnotator(this.annotator);
			if (v != null && !v.isEmpty()) {
				retval.add(v);
			}
		}
		if (comment == null) {
			retval.add("Missing comment for Annotation");
		}
		if (annotationDate == null) {
			retval.add("Missing date for Annotation");
		} else {
			String dateVerify = SpdxVerificationHelper.verifyDate(annotationDate);
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
		if (this.resource != null && this.refreshOnGet) {
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
		if (this.resource != null && this.refreshOnGet) {
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
		if (this.resource != null && this.refreshOnGet) {
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
	public String getAnnotationDate() {
		if (this.resource != null && this.refreshOnGet) {
			annotationDate = findSinglePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_ANNOTATION_DATE);
		}
		return annotationDate;
	}

	/**
	 * @param date the date to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setAnnotationDate(String date) throws InvalidSPDXAnalysisException {
		this.annotationDate = date;
		if (date != null) {
			String dateVerify = SpdxVerificationHelper.verifyDate(date);
			if (dateVerify != null && !dateVerify.isEmpty()) {
				throw(new InvalidSPDXAnalysisException("Invalid date format: "+dateVerify));
			}
			setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_ANNOTATION_DATE, date);
		} else {
			removePropertyValue(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_ANNOTATION_DATE);
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

	@Override
    public Annotation clone() {
		return new Annotation(this.annotator, this.annotationType, this.annotationDate, 
				this.comment);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#equivalent(org.spdx.rdfparser.model.RdfModelObject)
	 */
	@Override
	public boolean equivalent(IRdfModel o) {
		if (!(o instanceof Annotation)) {
			return false;
		}
		Annotation comp = (Annotation)o;
        return (Objects.equal(annotator, comp.getAnnotator()) &&
                Objects.equal(annotationType, comp.getAnnotationType()) &&
                Objects.equal(comment, comp.getComment()) && Objects.equal(annotationDate, comp.getAnnotationDate()));
	}
	
	/**
	 * @return The tag value of the annotation type
	 */
	public String getAnnotationTypeTag() {
		return ANNOTATION_TYPE_TO_TAG.get(this.annotationType);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Annotation o) {
		if (o == null) {
			return 1;
		}
		if (o.getAnnotationDate() == null) {
			if (this.annotationDate != null) {
				return 1;
			}
		}
		if (this.annotationDate == null) {
			return -1;
		}
		int retval = this.annotationDate.compareTo(o.getAnnotationDate());
		if (retval != 0) {
			return retval;
		}
		if (o.getAnnotator() == null) {
			if (this.annotator != null) {
				return 1;
			}
		}
		if (this.annotator == null) {
			return -1;
		}
		retval = this.annotator.compareToIgnoreCase(o.getAnnotator());
		if (retval != 0) {
			return retval;
		}
		if (o.getAnnotationType() == null) {
			if (this.annotationType != null) {
				return 1;
			}
		}
		if (this.annotationType == null) {
			return -1;
		}
		return this.annotationType.compareTo(o.getAnnotationType());
	}
}
