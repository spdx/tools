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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Gary O'Neall
 *
 */
public class TestNormalizedFileNameComparator {

	static final String FILE_A = "file.a";
	static final String FILE_A_DIR = "dirA/dirB/"+FILE_A;
	static final String FILE_A_DIR_RELATIVE = "./" + FILE_A_DIR;
	static final String FILE_A_DIR_DOS = FILE_A_DIR.replace('/', '\\');
	static final String FILE_A_DIR_EXTRA_DIR = "extra/"+FILE_A_DIR;
	static final String FILE_A_DIR_EXTRA_DIR_RELATIVE = "./" + FILE_A_DIR_EXTRA_DIR;
	static final String FILE_B = "file.b";
	static final String FILE_B_DIR = "dirA/dirB/"+FILE_B;
	static final String FILE_B_DIR_RELATIVE = "./" + FILE_B_DIR;
	static final String FILE_B_DIR_DOS = FILE_B_DIR.replace('/', '\\');
	static final String FILE_B_DIR_EXTRA_DIR = "extra/"+FILE_B_DIR;
	static final String FILE_B_DIR_EXTRA_DIR_RELATIVE = "./" + FILE_B_DIR_EXTRA_DIR;

	/**
	 * Test method for {@link org.spdx.compare.NormalizedFileNameComparator#compare(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testCompare() {
		NormalizedFileNameComparator comp = new NormalizedFileNameComparator();
		assertEquals(0, comp.compare(FILE_A, FILE_A));
		assertTrue(comp.compare(FILE_A, FILE_B) < 0);
		assertTrue(comp.compare(FILE_B, FILE_A) > 0);
	}
	
	@Test
	public void testCompareDir() {
		NormalizedFileNameComparator comp = new NormalizedFileNameComparator();
		assertEquals(0, comp.compare(FILE_A_DIR, FILE_A_DIR));
		assertTrue(comp.compare(FILE_A_DIR, FILE_B_DIR) < 0);
		assertTrue(comp.compare(FILE_B_DIR, FILE_A_DIR) > 0);
	}
	
	@Test
	public void testCompareRelative() {
		NormalizedFileNameComparator comp = new NormalizedFileNameComparator();
		assertEquals(0, comp.compare(FILE_A_DIR, FILE_A_DIR_RELATIVE));
		assertTrue(comp.compare(FILE_A_DIR_RELATIVE, FILE_B_DIR) < 0);
		assertTrue(comp.compare(FILE_B_DIR, FILE_A_DIR_RELATIVE) > 0);
	}
	
	@Test
	public void testCompareDirDos() {
		NormalizedFileNameComparator comp = new NormalizedFileNameComparator();
		assertEquals(0, comp.compare(FILE_A_DIR, FILE_A_DIR_DOS));
		assertTrue(comp.compare(FILE_A_DIR_DOS, FILE_B_DIR) < 0);
		assertTrue(comp.compare(FILE_B_DIR, FILE_A_DIR_DOS) > 0);
	}

//	@Test
//	public void testCompareLeading() {
//		NormalizedFileNameComparator comp = new NormalizedFileNameComparator();
//		assertEquals(0, comp.compare(FILE_A_DIR_RELATIVE, FILE_A_DIR_EXTRA_DIR_RELATIVE));
//		assertTrue(comp.compare(FILE_A_DIR_EXTRA_DIR_RELATIVE, FILE_B_DIR) > 0);
//		assertTrue(comp.compare(FILE_B_DIR, FILE_A_DIR_EXTRA_DIR_RELATIVE) < 0);
//	}
//	
	/**
	 * Test method for {@link org.spdx.compare.NormalizedFileNameComparator#hasLeadingDir(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testHasLeadingDir() {
		assertTrue(NormalizedFileNameComparator.hasLeadingDir(FILE_A_DIR_EXTRA_DIR, FILE_A_DIR));
		assertTrue(NormalizedFileNameComparator.hasLeadingDir(FILE_A_DIR_EXTRA_DIR_RELATIVE, FILE_A_DIR));
		assertTrue(NormalizedFileNameComparator.hasLeadingDir(FILE_A_DIR_EXTRA_DIR, FILE_A_DIR_RELATIVE));
		assertTrue(NormalizedFileNameComparator.hasLeadingDir(FILE_A_DIR_EXTRA_DIR_RELATIVE, FILE_A_DIR_RELATIVE));
		assertTrue(!NormalizedFileNameComparator.hasLeadingDir(FILE_A_DIR, FILE_A_DIR));
		assertTrue(!NormalizedFileNameComparator.hasLeadingDir(FILE_A, FILE_B));
	}

	/**
	 * Test method for {@link org.spdx.compare.NormalizedFileNameComparator#normalizeFileName(java.lang.String)}.
	 */
	@Test
	public void testNormalizeFileName() {
		assertEquals(FILE_A_DIR_RELATIVE, NormalizedFileNameComparator.normalizeFileName(FILE_A_DIR_DOS));
		assertEquals(FILE_A_DIR_RELATIVE, NormalizedFileNameComparator.normalizeFileName(FILE_A_DIR));
		assertEquals(FILE_B_DIR_RELATIVE, NormalizedFileNameComparator.normalizeFileName(FILE_B_DIR_RELATIVE));
	}

}
