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
package org.spdx.html;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXReview;

/**
 * @author Source Auditor
 *
 */
public class ReviewerContext {

	Exception error = null;
	SPDXReview review = null;
	
	/**
	 * Add a reviewer which will return an error for any of the methods called
	 * This may be considered a bit of a kludge for the error reporting, so
	 * feel free to implement a better approach
	 * @param e
	 */
	public ReviewerContext(InvalidSPDXAnalysisException e) {
		error = e;
	}

	/**
	 * @param spdxReview
	 */
	public ReviewerContext(SPDXReview spdxReview) {
		review = spdxReview;
	}
	
	public String reviewer() {
		if (review != null) {
			return review.getReviewer();
		} else if (error != null) {
			return "Error getting reviewer information: "+error.getMessage();
		} else {
			return null;
		}
	}

	public String reviewDate() {
		if (review != null) {
			return review.getReviewDate();
		} else if (error != null) {
			return "Error getting reviewer information: "+error.getMessage();
		} else {
			return null;
		}
	}
	
	public String comment() {
		if (review != null) {
			return review.getComment();
		} else if (error != null) {
			return "Error getting reviewer information: "+error.getMessage();
		} else {
			return null;
		}
	}
}
