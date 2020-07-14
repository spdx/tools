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
package org.spdx.html;

import org.spdx.rdfparser.model.Annotation;

/**
 * Context for an Annotation
 * @author Gary O'Neall
 *
 */
public class AnnotationContext {

	private String date;
	private String type;
	private String annotator;
	private String comment;

	public AnnotationContext(Annotation annotation) {

		this.date = annotation.getAnnotationDate().toString();
		this.type = annotation.getAnnotationType().getTag();
		this.annotator = annotation.getAnnotator();
		this.comment = annotation.getComment();
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the annotator
	 */
	public String getAnnotator() {
		return annotator;
	}

	/**
	 * @param annotator the annotator to set
	 */
	public void setAnnotator(String annotator) {
		this.annotator = annotator;
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
	}

}
