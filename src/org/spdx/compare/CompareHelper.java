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
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxElement;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxFile.FileType;
import org.spdx.tag.BuildDocument;
import org.spdx.tag.InvalidSpdxTagFileException;

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
			sb.append(CompareHelper.checksumToString(checksums[i]));
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
			sb.append(annotationToString(annotations[i]));
		}
		return sb.toString();
	}

	public static String relationshipToString(Relationship relationship) {
		if (relationship == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(relationship.getRelationshipType().getTag());
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

	/**
	 * @param fileTypes
	 * @return
	 */
	public static String fileTypesToString(FileType[] fileTypes) {
		if (fileTypes == null || fileTypes.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(SpdxFile.FILE_TYPE_TO_TAG.get(fileTypes[0]));
		for (int i = 1;i < fileTypes.length; i++) {
			sb.append(", ");
			sb.append(SpdxFile.FILE_TYPE_TO_TAG.get(fileTypes[i]));
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
			retval[i] = SpdxFile.TAG_TO_FILE_TYPE.get(fileTypeStrs[i]);
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
}
