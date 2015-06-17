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

import java.util.HashSet;

import org.spdx.rdfparser.model.IRdfModel;

import com.google.common.base.Objects;

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
		HashSet<Integer> foundIndexes = new HashSet<Integer>();
		for (int i = 0; i < array1.length; i++) {
			boolean found = false;
			for (int j = 0; j < array2.length; j++) {
				if (!foundIndexes.contains(j) &&
						RdfModelHelper.equivalentConsideringNull(array1[i],array2[j])) {
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

	        /**
     * Compares to objects considering possible null values
     * 
     * @param o1
     *            The first object to compare
     * @param o2
     *            The second object to compare
     * @return True if the objects are equal, false otherwise
     * @deprecated Use {@link com.google.common.base.Objects#equal(Object, Object)} instead
     */
	@Deprecated
    public static boolean equalsConsideringNull(Object o1, Object o2) {
        return Objects.equal(o1, o2);
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
		HashSet<Integer> foundIndexes = new HashSet<Integer>();
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
	
	

}
