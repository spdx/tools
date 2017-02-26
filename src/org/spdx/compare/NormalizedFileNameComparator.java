/**
 * Copyright (c) 2013 Source Auditor Inc.
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

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares to file name strings normalizing them to a common format using the following rules:
 *  - File separator character is "/"
 *  - Must begin with "./"
 * @author Gary O'Neall
 *
 */
public class NormalizedFileNameComparator implements Comparator<String>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static final char DOS_SEPARATOR = '\\';
	static final char UNIX_SEPARATOR = '/';
	static final String RELATIVE_DIR = "./";
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(String fileName1, String fileName2) {
		String normalizedFileName1 = normalizeFileName(fileName1);
		String normalizedFileName2 = normalizeFileName(fileName2);
		return normalizedFileName1.compareTo(normalizedFileName2);
	}
	
	/**
	 * Returns true if fileName2 matches fileName1 except for leading file name directories
	 * @param fileName1
	 * @param fileName2
	 * @return true if fileName2 matches fileName1 except for leading file name directories
	 */
	public static boolean hasLeadingDir(String fileName1, String fileName2) {
		String compareName1 = fileName1;
		String compareName2 = fileName2;
		if (compareName1.startsWith(RELATIVE_DIR)) {
			compareName1 = compareName1.substring(RELATIVE_DIR.length());
		}
		if (compareName2.startsWith(RELATIVE_DIR)) {
			compareName2 = compareName2.substring(RELATIVE_DIR.length());
		}
		if (compareName1.length() <= compareName2.length()) {
			return false;
		}
		if (!compareName1.endsWith(compareName2)) {
			return false;
		}
		char schar = compareName1.charAt(compareName1.length()-compareName2.length()-1);
		return (schar == UNIX_SEPARATOR);
	}
	
	public static String normalizeFileName(String fileName) {
		String retval = fileName.replace(DOS_SEPARATOR, UNIX_SEPARATOR);
		if (!retval.startsWith(RELATIVE_DIR)) {
			retval = RELATIVE_DIR + retval;
		}
		return retval;
	}

}
