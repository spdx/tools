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
package org.spdx.rdfparser;

import java.util.Set;

import org.spdx.rdfparser.model.IRdfModel;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

/**
 * Static class containing helper methods for implementations of IRdfModel
 * @author Gary O'Neall
 *
 */
public final class RdfModelHelper {

	/**
	 * 
	 */
	private RdfModelHelper() {
		// This is a static class, it should not be instantiated
	}
	
	/**
	 * Compares 2 arrays to see if thier content is the same independent of
	 * order and considering nulls
	 * @param array1
	 * @param array2
	 * @return
	 */
	public static boolean arraysEqual(Object[] array1, Object[] array2) {
		if (array1 == null) {
			return array2 == null;
		}
		if (array2 == null) {
			return false;
		}
		if (array1.length != array2.length) {
			return false;
		}
		Set<Integer> foundIndexes = Sets.newHashSet();
		for (int i = 0; i < array1.length; i++) {
			boolean found = false;
			for (int j = 0; j < array2.length; j++) {
                if (!foundIndexes.contains(j) && Objects.equal(array1[i], array2[j])) {
					found = true;
					foundIndexes.add(j);
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
	 * Strings are considered equivalent if they are equal, 
	 * or if they are null and/or empty (null and empty strings are considered equivalent)
	 * linefeeds are also normalized (e.g. \r\n is the same as \r)
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static boolean stringsEquivalent(String s1, String s2) {
		if (Objects.equal(s1, s2)) {
			return true;
		} else if (s1 == null && s2.isEmpty()) {
			return true;
		} else if (s2 == null && s1.isEmpty()) {
			return true;
		} else {
			String s1norm = s1.replace("\r\n", "\n").trim();
			String s2norm = s2.replace("\r\n", "\n").trim();
			boolean retval = s1norm.equals(s2norm);
			if (!retval) {
				int j = 1;
				j = j + 1;
			}
			return retval;
		}
	}
	
	/**
	 * Compares 2 arrays to see if the property values for the element RdfModelObjects are the same independent of
	 * order and considering nulls
	 * @param array1
	 * @param array2
	 * @return
	 */
	public static boolean arraysEquivalent(IRdfModel[] array1, IRdfModel[] array2) {
		if (array1 == null) {
			return array2 == null;
		}
		if (array2 == null) {
			return false;
		}
		if (array1.length != array2.length) {
			return false;
		}
		Set<Integer> foundIndexes = Sets.newHashSet();
		for (int i = 0; i < array1.length; i++) {
			boolean found = false;
			for (int j = 0; j < array2.length; j++) {
				if (!foundIndexes.contains(j) &&
						equivalentConsideringNull(array1[i],array2[j])) {
					found = true;
					foundIndexes.add(j);
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
	 * Compares the properties of two RdfModelObjects considering possible null values
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static boolean equivalentConsideringNull(IRdfModel o1, IRdfModel o2) {
		if (o1 == null) {
			return (o2 == null);
		} else {
			return o1.equivalent(o2);
		}
	}

}
