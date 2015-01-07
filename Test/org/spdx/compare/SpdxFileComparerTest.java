/**
 * Copyright (c) 2013 Source Auditor Inc.
 * Copyright (c) 2013 Black Duck Software Inc.
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

import java.io.File;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.DOAPProject;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.License;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

/**
 * @author Gary O'Neall
 *
 */
public class SpdxFileComparerTest {

	static final String TEST_RDF_FILE_PATH = "TestFiles"+File.separator+"SPDXRdfExample.rdf";
	private static final String STD_LIC_ID_CC0 = "CC-BY-1.0";
	private static final String STD_LIC_ID_MPL11 = "MPL-1.1";
	File testRDFFile;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.testRDFFile = new File(TEST_RDF_FILE_PATH); 
	}
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#SpdxFileComparer()}.
	 * @throws InvalidLicenseStringException 
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testSpdxFileComparer() throws InvalidLicenseStringException, SpdxCompareException {
		@SuppressWarnings("unused")
		SpdxFileComparer fc = new SpdxFileComparer();
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#compare(org.spdx.rdfparser.SPDXFile, org.spdx.rdfparser.SPDXFile, java.util.HashMap)}.
	 * @throws SpdxCompareException 
	 * @throws InvalidLicenseStringException 
	 */
	@Test
	public void testCompare() throws SpdxCompareException, InvalidLicenseStringException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		String fileTypeA = SpdxRdfConstants.FILE_TYPE_SOURCE;
		String fileTypeB = fileTypeA;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = sha1A;
		AnyLicenseInfo concludedLicenseA = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0);
		AnyLicenseInfo concludedLicenseB = concludedLicenseA;
		AnyLicenseInfo[] seenLicenseA = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11)
				};
		AnyLicenseInfo[] seenLicenseB = seenLicenseA;
		String licenseCommentsA = "License Comments";
		String licenseCommentsB = licenseCommentsA;
		String copyrightA = "Copyright";
		String copyrightB = copyrightA;
		String proj1HomePage = "http://home.page";
		String proj1Name = "project1";
		DOAPProject proj1 = new DOAPProject(proj1Name, proj1HomePage);
		DOAPProject[] artifactOfA = new DOAPProject[] {proj1};
		DOAPProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SPDXFile fileA = new SPDXFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SPDXFile fileB = new SPDXFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer();
		HashMap<String, String> licenseXlationMap = new HashMap<String, String>();
		fc.compare(fileA, fileB, licenseXlationMap);
		assertFalse(fc.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isConcludedLicenseEquals()}.
	 * @throws SpdxCompareException 
	 * @throws InvalidLicenseStringException 
	 */
	@Test
	public void testIsConcludedLicenseEquals() throws SpdxCompareException, InvalidLicenseStringException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		String fileTypeA = SpdxRdfConstants.FILE_TYPE_SOURCE;
		String fileTypeB = fileTypeA;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = sha1A;
		AnyLicenseInfo concludedLicenseA = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0);
		AnyLicenseInfo concludedLicenseB = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11);
		AnyLicenseInfo[] seenLicenseA = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11)
				};
		AnyLicenseInfo[] seenLicenseB = seenLicenseA;
		String licenseCommentsA = "License Comments";
		String licenseCommentsB = licenseCommentsA;
		String copyrightA = "Copyright";
		String copyrightB = copyrightA;
		String proj1HomePage = "http://home.page";
		String proj1Name = "project1";
		DOAPProject proj1 = new DOAPProject(proj1Name, proj1HomePage);
		DOAPProject[] artifactOfA = new DOAPProject[] {proj1};
		DOAPProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SPDXFile fileA = new SPDXFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SPDXFile fileB = new SPDXFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer();
		HashMap<String, String> licenseXlationMap = new HashMap<String, String>();
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isConcludedLicenseEquals());
		SpdxFileDifference diff = fc.getFileDifference();
		assertFalse(diff.isConcludedLicenseEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isSeenLicenseEquals()}.
	 */
	@Test
	public void testIsSeenLicenseEquals() throws SpdxCompareException, InvalidLicenseStringException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		String fileTypeA = SpdxRdfConstants.FILE_TYPE_SOURCE;
		String fileTypeB = fileTypeA;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = sha1A;
		AnyLicenseInfo concludedLicenseA = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0);
		AnyLicenseInfo concludedLicenseB = concludedLicenseA;
		AnyLicenseInfo[] seenLicenseA = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11)
				};
		AnyLicenseInfo[] seenLicenseB = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11),
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0)
		};
		String licenseCommentsA = "License Comments";
		String licenseCommentsB = licenseCommentsA;
		String copyrightA = "Copyright";
		String copyrightB = copyrightA;
		String proj1HomePage = "http://home.page";
		String proj1Name = "project1";
		DOAPProject proj1 = new DOAPProject(proj1Name, proj1HomePage);
		DOAPProject[] artifactOfA = new DOAPProject[] {proj1};
		DOAPProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SPDXFile fileA = new SPDXFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SPDXFile fileB = new SPDXFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer();
		HashMap<String, String> licenseXlationMap = new HashMap<String, String>();
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isSeenLicenseEquals());
		SpdxFileDifference diff = fc.getFileDifference();
		assertFalse(diff.isSeenLicensesEqual());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isArtifactOfEquals()}.
	 */
	@Test
	public void testIsArtifactOfEquals() throws SpdxCompareException, InvalidLicenseStringException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		String fileTypeA = SpdxRdfConstants.FILE_TYPE_SOURCE;
		String fileTypeB = fileTypeA;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = sha1A;
		AnyLicenseInfo concludedLicenseA = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0);
		AnyLicenseInfo concludedLicenseB = concludedLicenseA;
		AnyLicenseInfo[] seenLicenseA = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11)
				};
		AnyLicenseInfo[] seenLicenseB = seenLicenseA;
		String licenseCommentsA = "License Comments";
		String licenseCommentsB = licenseCommentsA;
		String copyrightA = "Copyright";
		String copyrightB = copyrightA;
		String proj1HomePage = "http://home.page";
		String proj1Name = "project1";
		DOAPProject proj1 = new DOAPProject(proj1Name, proj1HomePage);
		String proj2HomePage = "http://proj2.page";
		String proj2Name = proj1Name;
		DOAPProject proj2 = new DOAPProject(proj2Name, proj2HomePage);
		String proj3Name = "project3";
		String proj3HomePage = proj1HomePage;
		DOAPProject proj3 = new DOAPProject(proj3Name, proj3HomePage);
		DOAPProject[] artifactOfA = new DOAPProject[] {proj1};
		DOAPProject[] artifactOfB = new DOAPProject[] {proj2};
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SPDXFile fileA = new SPDXFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SPDXFile fileB = new SPDXFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer();
		HashMap<String, String> licenseXlationMap = new HashMap<String, String>();
		// Different homepage
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isArtifactOfEquals());
		SpdxFileDifference diff = fc.getFileDifference();
		assertFalse(diff.isArtifactOfsEquals());
		
		// different name
		fileB.setArtifactOf(new DOAPProject[] {proj3});
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isArtifactOfEquals());
		
		// more A
		fileA.setArtifactOf(new DOAPProject[] {proj1, proj3});
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isArtifactOfEquals());

		// more B
		fileB.setArtifactOf(new DOAPProject[] {proj2, proj3, proj1});
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isArtifactOfEquals());
		
		// back to equals (different order)
		fileA.setArtifactOf(new DOAPProject[] {proj1, proj2, proj3});
		fc.compare(fileA, fileB, licenseXlationMap);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isArtifactOfEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#getUniqueSeenLicensesB()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetUniqueSeenLicensesB() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		String fileTypeA = SpdxRdfConstants.FILE_TYPE_SOURCE;
		String fileTypeB = fileTypeA;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = sha1A;
		AnyLicenseInfo concludedLicenseA = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0);
		AnyLicenseInfo concludedLicenseB = concludedLicenseA;
		AnyLicenseInfo[] seenLicenseA = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11)
				};
		AnyLicenseInfo[] seenLicenseB = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11),
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0)
		};
		String licenseCommentsA = "License Comments";
		String licenseCommentsB = licenseCommentsA;
		String copyrightA = "Copyright";
		String copyrightB = copyrightA;
		String proj1HomePage = "http://home.page";
		String proj1Name = "project1";
		DOAPProject proj1 = new DOAPProject(proj1Name, proj1HomePage);
		DOAPProject[] artifactOfA = new DOAPProject[] {proj1};
		DOAPProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SPDXFile fileA = new SPDXFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SPDXFile fileB = new SPDXFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer();
		HashMap<String, String> licenseXlationMap = new HashMap<String, String>();
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isSeenLicenseEquals());
		AnyLicenseInfo[] unique = fc.getUniqueSeenLicensesB();
		assertEquals(1, unique.length);
		assertEquals(STD_LIC_ID_CC0, ((License)unique[0]).getLicenseId());
		SpdxFileDifference diff = fc.getFileDifference();
		assertFalse(diff.isSeenLicensesEqual());
		AnyLicenseInfo[] diffUnique = diff.getUniqueSeenLicensesB();
		assertEquals(1, diffUnique.length);
		assertEquals(STD_LIC_ID_CC0, ((License)diffUnique[0]).getLicenseId());

		
		fileA.setSeenLicenses(seenLicenseB);
		fileB.setSeenLicenses(seenLicenseA);
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isSeenLicenseEquals());
		unique = fc.getUniqueSeenLicensesB();
		assertEquals(0, unique.length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#getUniqueSeenLicensesA()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetUniqueSeenLicensesA() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		String fileTypeA = SpdxRdfConstants.FILE_TYPE_SOURCE;
		String fileTypeB = fileTypeA;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = sha1A;
		AnyLicenseInfo concludedLicenseA = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0);
		AnyLicenseInfo concludedLicenseB = concludedLicenseA;
		AnyLicenseInfo[] seenLicenseA = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11),
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0)
				};
		AnyLicenseInfo[] seenLicenseB = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11)
		};
		String licenseCommentsA = "License Comments";
		String licenseCommentsB = licenseCommentsA;
		String copyrightA = "Copyright";
		String copyrightB = copyrightA;
		String proj1HomePage = "http://home.page";
		String proj1Name = "project1";
		DOAPProject proj1 = new DOAPProject(proj1Name, proj1HomePage);
		DOAPProject[] artifactOfA = new DOAPProject[] {proj1};
		DOAPProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SPDXFile fileA = new SPDXFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SPDXFile fileB = new SPDXFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer();
		HashMap<String, String> licenseXlationMap = new HashMap<String, String>();
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isSeenLicenseEquals());
		AnyLicenseInfo[] unique = fc.getUniqueSeenLicensesA();
		assertEquals(1, unique.length);
		assertEquals(STD_LIC_ID_CC0, ((License)unique[0]).getLicenseId());
		SpdxFileDifference diff = fc.getFileDifference();
		assertFalse(diff.isSeenLicensesEqual());
		AnyLicenseInfo[] diffUnique = diff.getUniqueSeenLicensesA();
		assertEquals(1, diffUnique.length);
		assertEquals(STD_LIC_ID_CC0, ((License)diffUnique[0]).getLicenseId());
		
		fileA.setSeenLicenses(seenLicenseB);
		fileB.setSeenLicenses(seenLicenseA);
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isSeenLicenseEquals());
		unique = fc.getUniqueSeenLicensesA();
		assertEquals(0, unique.length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#getUniqueArtifactOfA()}.
	 */
	@Test
	public void testGetUniqueArtifactOfAB() throws SpdxCompareException, InvalidLicenseStringException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		String fileTypeA = SpdxRdfConstants.FILE_TYPE_SOURCE;
		String fileTypeB = fileTypeA;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = sha1A;
		AnyLicenseInfo concludedLicenseA = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0);
		AnyLicenseInfo concludedLicenseB = concludedLicenseA;
		AnyLicenseInfo[] seenLicenseA = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11)
				};
		AnyLicenseInfo[] seenLicenseB = seenLicenseA;
		String licenseCommentsA = "License Comments";
		String licenseCommentsB = licenseCommentsA;
		String copyrightA = "Copyright";
		String copyrightB = copyrightA;
		String proj1HomePage = "http://home.page";
		String proj1Name = "project1";
		DOAPProject proj1 = new DOAPProject(proj1Name, proj1HomePage);
		String proj2HomePage = "http://proj2.page";
		String proj2Name = proj1Name;
		DOAPProject proj2 = new DOAPProject(proj2Name, proj2HomePage);
		String proj3Name = "project3";
		String proj3HomePage = proj1HomePage;
		DOAPProject proj3 = new DOAPProject(proj3Name, proj3HomePage);
		DOAPProject[] artifactOfA = new DOAPProject[] {proj1};
		DOAPProject[] artifactOfB = new DOAPProject[] {proj2};
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SPDXFile fileA = new SPDXFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SPDXFile fileB = new SPDXFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer();
		HashMap<String, String> licenseXlationMap = new HashMap<String, String>();
		
		// different name
		fileB.setArtifactOf(new DOAPProject[] {proj3});
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isArtifactOfEquals());
		DOAPProject[] uniqueA = fc.getUniqueArtifactOfA();
		DOAPProject[] uniqueB = fc.getUniqueArtifactOfB();
		assertEquals(1, uniqueA.length);
		assertEquals(proj1.getName(), uniqueA[0].getName());
		assertEquals(1, uniqueB.length);
		assertEquals(proj3.getName(), uniqueB[0].getName());
		SpdxFileDifference diff = fc.getFileDifference();
		assertFalse(diff.isArtifactOfsEquals());
		DOAPProject[] diffUnique = diff.getArtifactsOfA();
		assertEquals(1, diffUnique.length);
		assertEquals(proj1.getName(), diffUnique[0].getName());
		diffUnique = diff.getArtifactsOfB();
		assertEquals(1, diffUnique.length);
		assertEquals(proj3.getName(), diffUnique[0].getName());
		
		
		// more A
		fileA.setArtifactOf(new DOAPProject[] {proj1, proj3});
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isArtifactOfEquals());
		uniqueA = fc.getUniqueArtifactOfA();
		uniqueB = fc.getUniqueArtifactOfB();
		assertEquals(1, uniqueA.length);
		assertEquals(proj1.getName(), uniqueA[0].getName());
		assertEquals(0, uniqueB.length);

		// more B
		fileB.setArtifactOf(new DOAPProject[] {proj2, proj3, proj1});
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isArtifactOfEquals());
		uniqueA = fc.getUniqueArtifactOfA();
		uniqueB = fc.getUniqueArtifactOfB();
		assertEquals(0, uniqueA.length);
		assertEquals(1, uniqueB.length);
		assertEquals(proj2.getName(), uniqueB[0].getName());
		
		// back to equals (different order)
		fileA.setArtifactOf(new DOAPProject[] {proj1, proj2, proj3});
		fc.compare(fileA, fileB, licenseXlationMap);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isArtifactOfEquals());
		uniqueA = fc.getUniqueArtifactOfA();
		uniqueB = fc.getUniqueArtifactOfB();
		assertEquals(0, uniqueA.length);
		assertEquals(0, uniqueB.length);
	}



	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isCommentsEquals()}.
	 */
	@Test
	public void testIsLicenseCommentsEquals() throws SpdxCompareException, InvalidLicenseStringException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		String fileTypeA = SpdxRdfConstants.FILE_TYPE_SOURCE;
		String fileTypeB = fileTypeA;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = sha1A;
		AnyLicenseInfo concludedLicenseA = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0);
		AnyLicenseInfo concludedLicenseB = concludedLicenseA;
		AnyLicenseInfo[] seenLicenseA = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11)
				};
		AnyLicenseInfo[] seenLicenseB = seenLicenseA;
		String licenseCommentsA = "License Comments";
		String licenseCommentsB = "B license comments";
		String copyrightA = "Copyright";
		String copyrightB = copyrightA;
		String proj1HomePage = "http://home.page";
		String proj1Name = "project1";
		DOAPProject proj1 = new DOAPProject(proj1Name, proj1HomePage);
		DOAPProject[] artifactOfA = new DOAPProject[] {proj1};
		DOAPProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SPDXFile fileA = new SPDXFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SPDXFile fileB = new SPDXFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer();
		HashMap<String, String> licenseXlationMap = new HashMap<String, String>();
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isLicenseCommmentsEquals());
		SpdxFileDifference diff = fc.getFileDifference();
		assertFalse(diff.isLicenseCommentsEqual());
		
		fileB.setLicenseComments(licenseCommentsA);
		fc.compare(fileA, fileB, licenseXlationMap);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isLicenseCommmentsEquals());		
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isCopyrightsEquals()}.
	 */
	@Test
	public void testIsCopyrightsEquals() throws SpdxCompareException, InvalidLicenseStringException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		String fileTypeA = SpdxRdfConstants.FILE_TYPE_SOURCE;
		String fileTypeB = fileTypeA;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = sha1A;
		AnyLicenseInfo concludedLicenseA = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0);
		AnyLicenseInfo concludedLicenseB = concludedLicenseA;
		AnyLicenseInfo[] seenLicenseA = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11)
				};
		AnyLicenseInfo[] seenLicenseB = seenLicenseA;
		String licenseCommentsA = "License Comments";
		String licenseCommentsB = licenseCommentsA;
		String copyrightA = "Copyright";
		String copyrightB = "B Copyright";
		String proj1HomePage = "http://home.page";
		String proj1Name = "project1";
		DOAPProject proj1 = new DOAPProject(proj1Name, proj1HomePage);
		DOAPProject[] artifactOfA = new DOAPProject[] {proj1};
		DOAPProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SPDXFile fileA = new SPDXFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SPDXFile fileB = new SPDXFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer();
		HashMap<String, String> licenseXlationMap = new HashMap<String, String>();
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isCopyrightsEquals());
		SpdxFileDifference diff = fc.getFileDifference();
		assertFalse(diff.isCopyrightsEqual());
		
		fileB.setCopyright(copyrightA);
		fc.compare(fileA, fileB, licenseXlationMap);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isCopyrightsEquals());	
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isLicenseCommmentsEquals()}.
	 */
	@Test
	public void testIsCommmentsEquals() throws SpdxCompareException, InvalidLicenseStringException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		String fileTypeA = SpdxRdfConstants.FILE_TYPE_SOURCE;
		String fileTypeB = fileTypeA;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = sha1A;
		AnyLicenseInfo concludedLicenseA = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0);
		AnyLicenseInfo concludedLicenseB = concludedLicenseA;
		AnyLicenseInfo[] seenLicenseA = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11)
				};
		AnyLicenseInfo[] seenLicenseB = seenLicenseA;
		String licenseCommentsA = "License Comments";
		String licenseCommentsB = licenseCommentsA;
		String copyrightA = "Copyright";
		String copyrightB = copyrightA;
		String proj1HomePage = "http://home.page";
		String proj1Name = "project1";
		DOAPProject proj1 = new DOAPProject(proj1Name, proj1HomePage);
		DOAPProject[] artifactOfA = new DOAPProject[] {proj1};
		DOAPProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = "file B comment";
		SPDXFile fileA = new SPDXFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SPDXFile fileB = new SPDXFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer();
		HashMap<String, String> licenseXlationMap = new HashMap<String, String>();
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isCommentsEquals());
		SpdxFileDifference diff = fc.getFileDifference();
		assertFalse(diff.isCommentsEqual());

		fileB.setComment(fileCommentA);
		fc.compare(fileA, fileB, licenseXlationMap);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isCommentsEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isNamesEquals()}.
	 */
	@Test
	public void testIsNamesEquals() throws SpdxCompareException, InvalidLicenseStringException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = "fileb/dir/name.txt";
		String fileTypeA = SpdxRdfConstants.FILE_TYPE_SOURCE;
		String fileTypeB = fileTypeA;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = sha1A;
		AnyLicenseInfo concludedLicenseA = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0);
		AnyLicenseInfo concludedLicenseB = concludedLicenseA;
		AnyLicenseInfo[] seenLicenseA = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11)
				};
		AnyLicenseInfo[] seenLicenseB = seenLicenseA;
		String licenseCommentsA = "License Comments";
		String licenseCommentsB = licenseCommentsA;
		String copyrightA = "Copyright";
		String copyrightB = copyrightA;
		String proj1HomePage = "http://home.page";
		String proj1Name = "project1";
		DOAPProject proj1 = new DOAPProject(proj1Name, proj1HomePage);
		DOAPProject[] artifactOfA = new DOAPProject[] {proj1};
		DOAPProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SPDXFile fileA = new SPDXFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SPDXFile fileB = new SPDXFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer();
		HashMap<String, String> licenseXlationMap = new HashMap<String, String>();
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isNamesEquals());
		
		fileB.setName(fileNameA);
		fc.compare(fileA, fileB, licenseXlationMap);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isNamesEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isChecksumsEquals()}.
	 */
	@Test
	public void testIsChecksumsEquals() throws SpdxCompareException, InvalidLicenseStringException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		String fileTypeA = SpdxRdfConstants.FILE_TYPE_SOURCE;
		String fileTypeB = fileTypeA;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = "cccbf72bf99b7e471f1a27989667a903658652bb";
		AnyLicenseInfo concludedLicenseA = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0);
		AnyLicenseInfo concludedLicenseB = concludedLicenseA;
		AnyLicenseInfo[] seenLicenseA = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11)
				};
		AnyLicenseInfo[] seenLicenseB = seenLicenseA;
		String licenseCommentsA = "License Comments";
		String licenseCommentsB = licenseCommentsA;
		String copyrightA = "Copyright";
		String copyrightB = copyrightA;
		String proj1HomePage = "http://home.page";
		String proj1Name = "project1";
		DOAPProject proj1 = new DOAPProject(proj1Name, proj1HomePage);
		DOAPProject[] artifactOfA = new DOAPProject[] {proj1};
		DOAPProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SPDXFile fileA = new SPDXFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SPDXFile fileB = new SPDXFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer();
		HashMap<String, String> licenseXlationMap = new HashMap<String, String>();
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isChecksumsEquals());
		SpdxFileDifference diff = fc.getFileDifference();
		assertFalse(diff.isChecksumsEqual());

		fileA.setSha1(sha1B);
		fc.compare(fileA, fileB, licenseXlationMap);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isChecksumsEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isTypesEquals()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testIsTypesEquals() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		String fileTypeA = SpdxRdfConstants.FILE_TYPE_SOURCE;
		String fileTypeB = SpdxRdfConstants.FILE_TYPE_BINARY;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = sha1A;
		AnyLicenseInfo concludedLicenseA = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0);
		AnyLicenseInfo concludedLicenseB = concludedLicenseA;
		AnyLicenseInfo[] seenLicenseA = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11)
				};
		AnyLicenseInfo[] seenLicenseB = seenLicenseA;
		String licenseCommentsA = "License Comments";
		String licenseCommentsB = licenseCommentsA;
		String copyrightA = "Copyright";
		String copyrightB = copyrightA;
		String proj1HomePage = "http://home.page";
		String proj1Name = "project1";
		DOAPProject proj1 = new DOAPProject(proj1Name, proj1HomePage);
		DOAPProject[] artifactOfA = new DOAPProject[] {proj1};
		DOAPProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SPDXFile fileA = new SPDXFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SPDXFile fileB = new SPDXFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer();
		HashMap<String, String> licenseXlationMap = new HashMap<String, String>();
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isTypesEquals());
		SpdxFileDifference diff = fc.getFileDifference();
		assertFalse(diff.isTypeEqual());
		fileA.setType(fileTypeB);
		fc.compare(fileA, fileB, licenseXlationMap);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isTypesEquals());
	}
	
	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isContributorsEquals()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testIsContributorsEquals() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		String fileTypeA = SpdxRdfConstants.FILE_TYPE_SOURCE;
		String fileTypeB = fileTypeA;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = sha1A;
		AnyLicenseInfo concludedLicenseA = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0);
		AnyLicenseInfo concludedLicenseB = concludedLicenseA;
		AnyLicenseInfo[] seenLicenseA = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11)
				};
		AnyLicenseInfo[] seenLicenseB = seenLicenseA;
		String licenseCommentsA = "License Comments";
		String licenseCommentsB = licenseCommentsA;
		String copyrightA = "Copyright";
		String copyrightB = copyrightA;
		String proj1HomePage = "http://home.page";
		String proj1Name = "project1";
		DOAPProject proj1 = new DOAPProject(proj1Name, proj1HomePage);
		DOAPProject[] artifactOfA = new DOAPProject[] {proj1};
		DOAPProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SPDXFile[] fileADependencies = new SPDXFile[0];
		String[] fileAContributors = new String[] {"ContributorA", "ContributorB"};
		String fileANotice = "File A Notice";
		SPDXFile[] fileBDependencies = fileADependencies;
		String[] fileBContributors = new String[] {"Different", "Contributors", "Entirely"};
		String fileBNotice = fileANotice;
		SPDXFile fileA = new SPDXFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA, fileADependencies, fileAContributors, fileANotice);
		
		SPDXFile fileB = new SPDXFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB, fileBDependencies, fileBContributors, fileBNotice);
		
		SpdxFileComparer fc = new SpdxFileComparer();
		HashMap<String, String> licenseXlationMap = new HashMap<String, String>();
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isContributorsEquals());
		SpdxFileDifference diff = fc.getFileDifference();
		assertFalse(diff.isContributorsEqual());
		fileA.setContributors(fileBContributors);
		fc.compare(fileA, fileB, licenseXlationMap);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isContributorsEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isFileDependenciesEquals()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testIsFileDependenciesEquals() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		String fileTypeA = SpdxRdfConstants.FILE_TYPE_SOURCE;
		String fileTypeB = fileTypeA;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = sha1A;
		AnyLicenseInfo concludedLicenseA = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0);
		AnyLicenseInfo concludedLicenseB = concludedLicenseA;
		AnyLicenseInfo[] seenLicenseA = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11)
				};
		AnyLicenseInfo[] seenLicenseB = seenLicenseA;
		String licenseCommentsA = "License Comments";
		String licenseCommentsB = licenseCommentsA;
		String copyrightA = "Copyright";
		String copyrightB = copyrightA;
		String proj1HomePage = "http://home.page";
		String proj1Name = "project1";
		DOAPProject proj1 = new DOAPProject(proj1Name, proj1HomePage);
		DOAPProject[] artifactOfA = new DOAPProject[] {proj1};
		DOAPProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SPDXFile[] fileADependencies = new SPDXFile[0];
		String[] fileAContributors = new String[] {"ContributorA", "ContributorB"};
		String fileANotice = "File A Notice";

		String[] fileBContributors = fileAContributors;
		String fileBNotice = fileANotice;
		SPDXFile fileA = new SPDXFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA, fileADependencies, fileAContributors, fileANotice);
		SPDXFile[] fileBDependencies = new SPDXFile[] {fileA};
		SPDXFile fileB = new SPDXFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB, fileBDependencies, fileBContributors, fileBNotice);
		
		SpdxFileComparer fc = new SpdxFileComparer();
		HashMap<String, String> licenseXlationMap = new HashMap<String, String>();
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isFileDependenciesEquals());
		SpdxFileDifference diff = fc.getFileDifference();
		assertFalse(diff.isFileDependenciesEqual());
		fileA.setFileDependencies(fileBDependencies, null);
		fc.compare(fileA, fileB, licenseXlationMap);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isFileDependenciesEquals());
	}
	
	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isNoticeTextEqual()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testIsNoticeTextEquals() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		String fileTypeA = SpdxRdfConstants.FILE_TYPE_SOURCE;
		String fileTypeB = fileTypeA;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = sha1A;
		AnyLicenseInfo concludedLicenseA = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0);
		AnyLicenseInfo concludedLicenseB = concludedLicenseA;
		AnyLicenseInfo[] seenLicenseA = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11)
				};
		AnyLicenseInfo[] seenLicenseB = seenLicenseA;
		String licenseCommentsA = "License Comments";
		String licenseCommentsB = licenseCommentsA;
		String copyrightA = "Copyright";
		String copyrightB = copyrightA;
		String proj1HomePage = "http://home.page";
		String proj1Name = "project1";
		DOAPProject proj1 = new DOAPProject(proj1Name, proj1HomePage);
		DOAPProject[] artifactOfA = new DOAPProject[] {proj1};
		DOAPProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SPDXFile[] fileADependencies = new SPDXFile[0];
		String[] fileAContributors = new String[] {"ContributorA", "ContributorB"};
		String fileANotice = "File A Notice";
		SPDXFile[] fileBDependencies = fileADependencies;
		
		String[] fileBContributors = fileAContributors;
		String fileBNotice = "file B Notice which is different";
		SPDXFile fileA = new SPDXFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA, fileADependencies, fileAContributors, fileANotice);

		SPDXFile fileB = new SPDXFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB, fileBDependencies, fileBContributors, fileBNotice);
		
		SpdxFileComparer fc = new SpdxFileComparer();
		HashMap<String, String> licenseXlationMap = new HashMap<String, String>();
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isNoticeTextEquals());
		SpdxFileDifference diff = fc.getFileDifference();
		assertFalse(diff.isNoticeTextsEqual());
		fileA.setNoticeText(fileBNotice);
		fc.compare(fileA, fileB, licenseXlationMap);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isNoticeTextEquals());
	}
	
	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isDifferenceFound()}.
	 */
	@Test
	public void testIsDifferenceFound() throws SpdxCompareException, InvalidLicenseStringException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		String fileTypeA = SpdxRdfConstants.FILE_TYPE_SOURCE;
		String fileTypeB = fileTypeA;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = sha1A;
		AnyLicenseInfo concludedLicenseA = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0);
		AnyLicenseInfo concludedLicenseB = concludedLicenseA;
		AnyLicenseInfo[] seenLicenseA = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11)
				};
		AnyLicenseInfo[] seenLicenseB = seenLicenseA;
		String licenseCommentsA = "License Comments";
		String licenseCommentsB = licenseCommentsA;
		String copyrightA = "Copyright";
		String copyrightB = copyrightA;
		String proj1HomePage = "http://home.page";
		String proj1Name = "project1";
		DOAPProject proj1 = new DOAPProject(proj1Name, proj1HomePage);
		DOAPProject[] artifactOfA = new DOAPProject[] {proj1};
		DOAPProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SPDXFile fileA = new SPDXFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SPDXFile fileB = new SPDXFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer();
		HashMap<String, String> licenseXlationMap = new HashMap<String, String>();
		fc.compare(fileA, fileB, licenseXlationMap);
		assertFalse(fc.isDifferenceFound());
		fileA.setComment("Different");
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		// Note - all of the other fields are tested in the individual test cases
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#getFileDifference()}.
	 */
	@Test
	public void testGetFileDifference() throws SpdxCompareException, InvalidLicenseStringException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		String fileTypeA = SpdxRdfConstants.FILE_TYPE_SOURCE;
		String fileTypeB = SpdxRdfConstants.FILE_TYPE_BINARY;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = sha1A;
		AnyLicenseInfo concludedLicenseA = LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_CC0);
		AnyLicenseInfo concludedLicenseB = concludedLicenseA;
		AnyLicenseInfo[] seenLicenseA = new AnyLicenseInfo[] {
				LicenseInfoFactory.parseSPDXLicenseString(STD_LIC_ID_MPL11)
				};
		AnyLicenseInfo[] seenLicenseB = seenLicenseA;
		String licenseCommentsA = "License Comments";
		String licenseCommentsB = licenseCommentsA;
		String copyrightA = "Copyright";
		String copyrightB = copyrightA;
		String proj1HomePage = "http://home.page";
		String proj1Name = "project1";
		DOAPProject proj1 = new DOAPProject(proj1Name, proj1HomePage);
		DOAPProject[] artifactOfA = new DOAPProject[] {proj1};
		DOAPProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SPDXFile fileA = new SPDXFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SPDXFile fileB = new SPDXFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer();
		HashMap<String, String> licenseXlationMap = new HashMap<String, String>();
		fc.compare(fileA, fileB, licenseXlationMap);
		assertTrue(fc.isDifferenceFound());
		SpdxFileDifference diff = fc.getFileDifference();
		assertFalse(diff.isTypeEqual());
		//Note - each of the individual fields is tested in their respecive unit tests
	}

}
