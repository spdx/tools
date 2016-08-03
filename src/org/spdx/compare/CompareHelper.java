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

import java.util.Arrays;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.ExternalRef;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxElement;
import org.spdx.rdfparser.model.SpdxFile.FileType;
import org.spdx.rdfparser.referencetype.ListedReferenceTypes;
import org.spdx.tag.BuildDocument;
import org.spdx.tag.InvalidSpdxTagFileException;

/**
 * Helper class for comparisons
 * @author Gary O'Neall
 *
 */
public class CompareHelper {
	
	static final int MAX_CHARACTERS_PER_CELL = 32000;

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
		StringBuilder sb = new StringBuilder(annotation.getAnnotationDate());
		sb.append(" ");
		sb.append(annotation.getAnnotator());
		sb.append(": ");
		sb.append(annotation.getComment());
		sb.append("[");
		sb.append(annotation.getAnnotationType().getTag());
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
		Arrays.sort(checksums);
		StringBuilder sb = new StringBuilder(CompareHelper.checksumToString(checksums[0]));
		for (int i = 1; i < checksums.length; i++) {
			sb.append("\n");
			String checksum = checksumToString(checksums[i]);
			if (sb.length() + checksum.length() > MAX_CHARACTERS_PER_CELL) {
				int numRemaing = checksums.length - i;
				sb.append('[');
				sb.append(numRemaing);
				sb.append(" more...]");
				break;
			}
			sb.append(checksum);
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
		sb.append(' ');
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
			String annotation = annotationToString(annotations[i]);
			if (sb.length() + annotation.length() > MAX_CHARACTERS_PER_CELL) {
				int numRemaing = annotations.length - i;
				sb.append('[');
				sb.append(numRemaing);
				sb.append(" more...]");
				break;
			}
			sb.append(annotation);
		}
		return sb.toString();
	}

	public static String relationshipToString(Relationship relationship) {
		if (relationship == null) {
			return "";
		}
		if (relationship.getRelationshipType() == null) {
			return "Unknown relationship type";
		}
		StringBuilder sb = new StringBuilder(relationship.getRelationshipType().toTag());
		sb.append(":");
		if (relationship.getRelatedSpdxElement() == null) {
			sb.append("?NULL");
		} else {
			if (relationship.getRelatedSpdxElement().getName() != null) {
				sb.append('[');
				sb.append(relationship.getRelatedSpdxElement().getName());
				sb.append(']');
			}
			sb.append(relationship.getRelatedSpdxElement().getId());
		}
		if (relationship.getComment() != null && !relationship.getComment().isEmpty()) {
			sb.append('(');
			sb.append(relationship.getComment());
			sb.append(')');
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
			String nextRelationship = relationshipToString(relationships[i]);
			if (sb.length() + nextRelationship.length() > MAX_CHARACTERS_PER_CELL) {
				int numRemaing = relationships.length - i;
				sb.append('[');
				sb.append(numRemaing);
				sb.append(" more...]");
				break;
			}
			sb.append(nextRelationship);
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
			if (elements[0].getName() != null) {
				sb.append('(');
				sb.append(elements[0].getName());
				sb.append(')');
			}
		}
		for (int i = 1; i < elements.length; i++) {
			sb.append(", ");
			if (elements[i] == null || elements[i].getId() == null || 
					elements[i].getId().isEmpty()) {
				sb.append("[UNKNOWNID]");
			} else {
				sb.append(elements[i].getId());
				if (elements[0].getName() != null) {
					sb.append('(');
					sb.append(elements[0].getName());
					sb.append(')');
				}
			}
		}
		return sb.toString();
	}

	/**
	 * @param fileTypes
	 * @return
	 */
	public static String fileTypesToString(FileType[] fileTypes) {
		if (fileTypes == null || fileTypes.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(fileTypes[0].getTag());
		for (int i = 1;i < fileTypes.length; i++) {
			sb.append(", ");
			String fileType = fileTypes[i].getTag();
			if (sb.length() + fileType.length() > MAX_CHARACTERS_PER_CELL) {
				int numRemaing = fileTypes.length - i;
				sb.append('[');
				sb.append(numRemaing);
				sb.append(" more...]");
				break;
			}
			sb.append(fileType);
		}
		return sb.toString();
	}

	/**
	 * @param typeStr
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	public static FileType[] parseFileTypeString(String typeStr) throws InvalidSPDXAnalysisException {
		if (typeStr == null || typeStr.trim().isEmpty()) {
			return new FileType[0];
		}
		String[] fileTypeStrs = typeStr.split(",");
		FileType[] retval = new FileType[fileTypeStrs.length];
		for (int i = 0; i < fileTypeStrs.length; i++) {
			fileTypeStrs[i] = fileTypeStrs[i].trim();
			if (fileTypeStrs[i].endsWith(",")) {
				fileTypeStrs[i] = fileTypeStrs[i].substring(0, fileTypeStrs[i].length()-1);
				fileTypeStrs[i] = fileTypeStrs[i].trim();
			}
			retval[i] = FileType.fromTag(fileTypeStrs[i]);
			if (retval[i] == null) {
				throw(new InvalidSPDXAnalysisException("Unrecognized file type "+fileTypeStrs[i]));
			}
		}
		return retval;
	}

	/**
	 * @param checksumsString
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	public static Checksum[] strToChecksums(String checksumsString) throws InvalidSPDXAnalysisException {
		if (checksumsString == null || checksumsString.trim().isEmpty()) {
			return new Checksum[0];
		}
		String[] parts = checksumsString.split("\n");
		Checksum[] retval = new Checksum[parts.length];
		for (int i = 0; i < parts.length; i++) {
			try {
				retval[i] = BuildDocument.parseChecksum(parts[i].trim());
			} catch (InvalidSpdxTagFileException e) {
				throw(new InvalidSPDXAnalysisException("Invalid checksum string: "+parts[i]));
			}
		}
		return retval;
	}

	/**
	 * Convert external refs to a friendly string
	 * @param externalRefs
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	public static String externalRefsToString(ExternalRef[] externalRefs, String docNamespace) throws InvalidSPDXAnalysisException {
		if (externalRefs == null || externalRefs.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(externalRefToString(externalRefs[0], docNamespace));
		for (int i = 1; i < externalRefs.length; i++) {
			sb.append("; ");
			sb.append(externalRefToString(externalRefs[i], docNamespace));
		}
		return sb.toString();
	}
	
	/**
	 * Convert a single external ref to a friendly string
	 * @param externalRef
	 * @param docNamespace
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	public static String externalRefToString(ExternalRef externalRef, String docNamespace) throws InvalidSPDXAnalysisException {
		String category = null;
		if (externalRef.getReferenceCategory() == null) {
			category = "OTHER";
		} else {
			category = externalRef.getReferenceCategory().getTag();
		}
		String referenceType = null;
		if (externalRef.getReferenceType() == null || 
				externalRef.getReferenceType().getReferenceTypeUri() == null) {
			referenceType = "[MISSING]";
		} else {
			try {
				referenceType = ListedReferenceTypes.getListedReferenceTypes().getListedReferenceName(externalRef.getReferenceType().getReferenceTypeUri());
			} catch (InvalidSPDXAnalysisException e) {
				referenceType = null;
			}
			if (referenceType == null) {
				referenceType = externalRef.getReferenceType().getReferenceTypeUri().toString();
				if (docNamespace != null && !docNamespace.isEmpty() && referenceType.startsWith(docNamespace)) {
					referenceType = referenceType.substring(docNamespace.length());
				}
			}
		}
		String referenceLocator = externalRef.getReferenceLocator();
		if (referenceLocator == null) {
			referenceLocator = "[MISSING]";
		}
		String retval = category + " " + referenceType + " " + referenceLocator;
		if (externalRef.getComment() != null && !externalRef.getComment().isEmpty()) {
			retval = retval + "(" + externalRef.getComment() + ")";
		}
		return retval;
	}
}
