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
package org.spdx.compare;

import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxElement;

/**
 * @author Gary
 *
 */
public class CompareHelper {

	/**
	 * 
	 */
	private CompareHelper() {
		// Static helper, should not be instantiated
	}

	/**
	 * @param annotation
	 * @return
	 */
	public static String annotationToString(Annotation annotation) {
		if (annotation == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(annotation.getDate());
		sb.append(" ");
		sb.append(annotation.getAnnotator());
		sb.append(": ");
		sb.append(annotation.getComment());
		sb.append("[");
		sb.append(Annotation.ANNOTATION_TYPE_TO_TAG.get(annotation.getAnnotationType()));
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Create a string from an array of checksums
	 * @param checksums
	 * @return
	 */
	public static String checksumsToString(Checksum[] checksums) {
		if (checksums == null || checksums.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(CompareHelper.checksumToString(checksums[0]));
		for (int i = 1; i < checksums.length; i++) {
			sb.append("\n");
			sb.append(CompareHelper.checksumToString(checksums[0]));
		}
		return sb.toString();
	}

	/**
	 * @param checksum
	 * @return
	 */
	public static String checksumToString(Checksum checksum) {
		if (checksum == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(Checksum.CHECKSUM_ALGORITHM_TO_TAG.get(checksum.getAlgorithm()));
		sb.append(checksum.getValue());
		return sb.toString();
	}

	/**
	 * @param licenseInfoFromFiles
	 * @return
	 */
	public static String licenseInfosToString(AnyLicenseInfo[] licenseInfoFromFiles) {
		if (licenseInfoFromFiles == null || licenseInfoFromFiles.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(licenseInfoFromFiles[0].toString());
		for (int i = 1; i < licenseInfoFromFiles.length; i++) {
			sb.append(", ");
			sb.append(licenseInfoFromFiles[i].toString());
		}
		return sb.toString();
	}

	/**
	 * @param annotations
	 * @return
	 */
	public static String annotationsToString(Annotation[] annotations) {
		if (annotations == null || annotations.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(annotationToString(annotations[0]));
		for (int i = 1; i < annotations.length; i++) {
			sb.append("\n");
			sb.append(annotationToString(annotations[i]));
		}
		return sb.toString();
	}

	public static String relationshipToString(Relationship relationship) {
		if (relationship == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(Relationship.RELATIONSHIP_TYPE_TO_TAG.get(
				relationship.getRelationshipType()));
		sb.append(":");
		sb.append(relationship.getRelatedSpdxElement().getId());
		if (relationship.getComment() != null && !relationship.getComment().isEmpty()) {
			sb.append("(");
			sb.append(relationship.getComment());
			sb.append(")");
		}
		return sb.toString();
	}

	/**
	 * @param relationships
	 * @return
	 */
	public static String relationshipsToString(Relationship[] relationships) {
		if (relationships == null || relationships.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(relationshipToString(relationships[0]));
		for (int i = 1; i < relationships.length; i++) {
			sb.append("\n");
			sb.append(relationshipToString(relationships[i]));
		}
		return sb.toString();
	}

	public static String formatSpdxElementList(SpdxElement[] elements) {
		if (elements == null || elements.length == 0) {
			return "";
		}
		StringBuilder sb;
		if (elements[0] == null || elements[0].getId() == null || 
				elements[0].getId().isEmpty()) {
			sb = new StringBuilder("[UNKNOWNID]");
		} else {
			sb = new StringBuilder(elements[0].getId());
		}
		for (int i = 1; i < elements.length; i++) {
			sb.append(", ");
			if (elements[i] == null || elements[i].getId() == null || 
					elements[i].getId().isEmpty()) {
				sb.append("[UNKNOWNID]");
			} else {
				sb.append(elements[i].getId());
			}
		}
		return sb.toString();
	}

}
