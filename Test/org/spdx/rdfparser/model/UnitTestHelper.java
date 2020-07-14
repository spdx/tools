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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.io.Files;

/**
 * Helper class for unit tests
 * @author Gary
 *
 */
public class UnitTestHelper {

	/**
	 * @param a1
	 * @param a2
	 */
	public static boolean isArraysEqual(Object[] a1,
			Object[] a2) {
		if (a1 == null) {
			return(a2 == null);
		}
		if (a2 == null) {
			return false;
		}
		if (a1.length != a2.length) {
			return false;
		}
		for (int i = 0; i < a1.length; i++) {
			boolean found = false;
			for (int j = 0; j < a2.length; j++) {
				if (a1[i].equals(a2[j])) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param a1
	 * @param a2
	 * @return true if the members of the two array are equivalent independent of order
	 */
	public static boolean isArraysEquivalent(RdfModelObject[] a1,
			RdfModelObject[] a2) {
		if (a1 == null) {
			return(a2 == null);
		}
		if (a2 == null) {
			return false;
		}
		if (a1.length != a2.length) {
			return false;
		}
		for (int i = 0; i < a1.length; i++) {
			boolean found = false;
			for (int j = 0; j < a2.length; j++) {
				if (a1[i].equivalent(a2[j])) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param filePath Path for file
	 * @return Text from the file
	 * @throws IOException
	 */
	public static String fileToText(String filePath) throws IOException {
		return Files.toString(new File(filePath), Charset.forName("UTF-8"));
	}

}
