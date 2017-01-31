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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;

import com.google.common.collect.Sets;
import org.apache.jena.iri.IRIFactory;

/**
 * Holds static methods used for verify various property valuse
 * @author Gary O'Neall
 *
 */
public class SpdxVerificationHelper {

	static IRIFactory iriFactory = IRIFactory.semanticWebImplementation();

	static Set<String> VALID_FILE_TYPES = Sets.newHashSet();

	static {
		VALID_FILE_TYPES.add("SOURCE");		VALID_FILE_TYPES.add("BINARY");
		VALID_FILE_TYPES.add("ARCHIVE");	VALID_FILE_TYPES.add("OTHER");
	}

	static final String[] VALID_CREATOR_PREFIXES = new String[] {"Person:", "Organization:", "Tool:"};
	static final String[] VALID_ORIGINATOR_SUPPLIER_PREFIXES = new String[] {SpdxRdfConstants.NOASSERTION_VALUE, "Person:", "Organization:"};

	static String verifyChecksumString(String checksum) {
		if (checksum.length() != 40) {
			return "Invalid number of characters for checksum";
		}

		for (int i = 0; i < checksum.length(); i++) {
			char c = checksum.charAt(i);
			if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
				return "Invalid checksum string character at position "+String.valueOf(i);
			}
		}
		return null;	// if we got here, all OK
	}

	/**
	 * @param fileType
	 * @return
	 */
	public static String verifyFileType(String fileType) {
		if (!VALID_FILE_TYPES.contains(fileType)) {
			return "Unrecognized file type";
		} else {
			return null;
		}
	}

	/**
	 * Verifies a creator string value
	 * @param creator
	 * @return
	 */
	public static String verifyCreator(String creator) {
		boolean ok = false;
		for (int i = 0; i < VALID_CREATOR_PREFIXES.length; i++) {
			if (creator.startsWith(VALID_CREATOR_PREFIXES[i])) {
				ok = true;
				break;
			}
		}
		if (!ok) {
			StringBuilder sb = new StringBuilder("Creator does not start with one of ");
			sb.append(VALID_CREATOR_PREFIXES[0]);
			for (int i = 1; i < VALID_CREATOR_PREFIXES.length; i++) {
				sb.append(", ");
				sb.append(VALID_CREATOR_PREFIXES[i]);
			}
			return sb.toString();
		} else {
			return null;
		}
	}

	/**
	 * Verifies the originator string
	 * @param originator
	 * @return
	 */
	public static String verifyOriginator(String originator) {
		return verifyOriginatorOrSupplier(originator);
	}

	/**
	 * Verifies the supplier String
	 * @param supplier
	 * @return
	 */
	public static String verifySupplier(String supplier) {
		return verifyOriginatorOrSupplier(supplier);
	}

	/**
	 * Verifies a the originator or supplier
	 * @param creator
	 * @return
	 */
	private static String verifyOriginatorOrSupplier(String originatorOrSupplier) {
		boolean ok = false;
		for (int i = 0; i < VALID_ORIGINATOR_SUPPLIER_PREFIXES.length; i++) {
			if (originatorOrSupplier.startsWith(VALID_ORIGINATOR_SUPPLIER_PREFIXES[i])) {
				ok = true;
				break;
			}
		}
		if (!ok) {
			StringBuilder sb = new StringBuilder("Value must not start with one of ");
			sb.append(VALID_ORIGINATOR_SUPPLIER_PREFIXES[0]);
			for (int i = 1; i < VALID_ORIGINATOR_SUPPLIER_PREFIXES.length; i++) {
				sb.append(", ");
				sb.append(VALID_ORIGINATOR_SUPPLIER_PREFIXES[i]);
			}
			return sb.toString();
		} else {
			return null;
		}
	}

	/**
	 * @param creationDate
	 * @return
	 */
	public static String verifyDate(String creationDate) {
		SimpleDateFormat format = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);
		try {
			format.parse(creationDate);
		} catch (ParseException e) {
			return("Invalid date format: "+e.getMessage());
		}
		return null;
	}

	/**
	 * @param reviewer
	 * @return
	 */
	public static String verifyReviewer(String reviewer) {
		if (!reviewer.startsWith("Person:")) {
			return "Reviewer does not start with Person:";
		} else {
			return null;
		}
	}

	/**
	 * Validates a URI is indeed valid
	 * @param uri
	 * @return
	 */
	public static boolean isValidUri(String uri) {
		return !iriFactory.create(uri).hasViolation(false);
	}
}
