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
import org.spdx.rdfparser.model.DoapProject;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.License;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.Checksum.ChecksumAlgorithm;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxFile.FileType;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

/**
 * @author Gary O'Neall
 *
 */
public class SpdxFileComparerTest {

	static final String TEST_RDF_FILE_PATH = "TestFiles"+File.separator+"SPDXRdfExample.rdf";
	private static final String STD_LIC_ID_CC0 = "CC-BY-1.0";
	private static final String STD_LIC_ID_MPL11 = "MPL-1.1";
	HashMap<SpdxDocument, HashMap<SpdxDocument, HashMap<String, String>>> LICENSE_XLATION = 
			new HashMap<SpdxDocument, HashMap<SpdxDocument, HashMap<String, String>>>();
	File testRDFFile;
	SpdxDocument DOCA;
	SpdxDocument DOCB;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.testRDFFile = new File(TEST_RDF_FILE_PATH); 
		String uri1 = "http://doc/uri1";
		SpdxDocumentContainer containerA = new SpdxDocumentContainer(uri1);
		DOCA = containerA.getSpdxDocument();
		String uri2 = "http://doc/uri2";
		SpdxDocumentContainer containerB = new SpdxDocumentContainer(uri2);
		DOCB = containerB.getSpdxDocument();
		HashMap<SpdxDocument, HashMap<String, String>> bmap = 
				new HashMap<SpdxDocument, HashMap<String, String>>();
		bmap.put(DOCB, new HashMap<String, String>());
		LICENSE_XLATION.put(DOCA, bmap);
		HashMap<SpdxDocument, HashMap<String, String>> amap = 
				new HashMap<SpdxDocument, HashMap<String, String>>();
		amap.put(DOCA, new HashMap<String, String>());
		LICENSE_XLATION.put(DOCB, amap);
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
		SpdxFileComparer fc = new SpdxFileComparer(LICENSE_XLATION);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#compare(org.spdx.rdfparser.SpdxFile, org.spdx.rdfparser.SpdxFile, java.util.HashMap)}.
	 * @throws SpdxCompareException 
	 * @throws InvalidLicenseStringException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testCompare() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		FileType[] fileTypeA = new FileType[] {FileType.fileType_source};
		FileType[] fileTypeB = fileTypeA;
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
		DoapProject proj1 = new DoapProject(proj1Name, proj1HomePage);
		DoapProject[] artifactOfA = new DoapProject[] {proj1};
		DoapProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SpdxFile fileA = new SpdxFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SpdxFile fileB = new SpdxFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertFalse(fc.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isConcludedLicenseEquals()}.
	 * @throws SpdxCompareException 
	 * @throws InvalidLicenseStringException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testIsConcludedLicenseEquals() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		FileType[] fileTypeA = new FileType[]{FileType.fileType_source};
		FileType[] fileTypeB = fileTypeA;
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
		DoapProject proj1 = new DoapProject(proj1Name, proj1HomePage);
		DoapProject[] artifactOfA = new DoapProject[] {proj1};
		DoapProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SpdxFile fileA = new SpdxFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SpdxFile fileB = new SpdxFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isConcludedLicenseEquals());
		SpdxFileDifference diff = fc.getFileDifference(DOCA, DOCB);
		assertFalse(diff.isConcludedLicenseEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isSeenLicenseEquals()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testIsSeenLicenseEquals() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		FileType[] fileTypeA = new FileType[]{FileType.fileType_source};
		FileType[] fileTypeB = fileTypeA;
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
		DoapProject proj1 = new DoapProject(proj1Name, proj1HomePage);
		DoapProject[] artifactOfA = new DoapProject[] {proj1};
		DoapProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SpdxFile fileA = new SpdxFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SpdxFile fileB = new SpdxFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isSeenLicenseEquals());
		SpdxFileDifference diff = fc.getFileDifference(DOCA, DOCB);
		assertFalse(diff.isSeenLicensesEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isArtifactOfEquals()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testIsArtifactOfEquals() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		FileType[] fileTypeA = new FileType[]{FileType.fileType_source};
		FileType[] fileTypeB = fileTypeA;
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
		DoapProject proj1 = new DoapProject(proj1Name, proj1HomePage);
		String proj2HomePage = "http://proj2.page";
		String proj2Name = proj1Name;
		DoapProject proj2 = new DoapProject(proj2Name, proj2HomePage);
		String proj3Name = "project3";
		String proj3HomePage = proj1HomePage;
		DoapProject proj3 = new DoapProject(proj3Name, proj3HomePage);
		DoapProject[] artifactOfA = new DoapProject[] {proj1};
		DoapProject[] artifactOfB = new DoapProject[] {proj2};
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SpdxFile fileA = new SpdxFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SpdxFile fileB = new SpdxFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isArtifactOfEquals());
		SpdxFileDifference diff = fc.getFileDifference(DOCA, DOCB);
		assertFalse(diff.isArtifactOfsEquals());
		
		// different name
		fileB.setArtifactOf(new DoapProject[] {proj3});
		fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isArtifactOfEquals());
		
		// more A
		fileA.setArtifactOf(new DoapProject[] {proj1, proj3});
		fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isArtifactOfEquals());

		// more B
		fileB.setArtifactOf(new DoapProject[] {proj2, proj3, proj1});
		fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isArtifactOfEquals());
		
		// back to equals (different order)
		fileA.setArtifactOf(new DoapProject[] {proj1, proj2, proj3});
		fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
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
		FileType[] fileTypeA = new FileType[]{FileType.fileType_source};
		FileType[] fileTypeB = fileTypeA;
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
		DoapProject proj1 = new DoapProject(proj1Name, proj1HomePage);
		DoapProject[] artifactOfA = new DoapProject[] {proj1};
		DoapProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SpdxFile fileA = new SpdxFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SpdxFile fileB = new SpdxFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isSeenLicenseEquals());
		AnyLicenseInfo[] unique = fc.getUniqueSeenLicenses(DOCB, DOCA);
		assertEquals(1, unique.length);
		assertEquals(STD_LIC_ID_CC0, ((License)unique[0]).getLicenseId());
		SpdxFileDifference diff = fc.getFileDifference(DOCA, DOCB);
		assertFalse(diff.isSeenLicensesEquals());
		AnyLicenseInfo[] diffUnique = diff.getUniqueSeenLicensesB();
		assertEquals(1, diffUnique.length);
		assertEquals(STD_LIC_ID_CC0, ((License)diffUnique[0]).getLicenseId());

		
		fileA.setLicenseInfosFromFiles(seenLicenseB);
		fileB.setLicenseInfosFromFiles(seenLicenseA);
		fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isSeenLicenseEquals());
		unique = fc.getUniqueSeenLicenses(DOCB, DOCA);
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
		FileType[] fileTypeA = new FileType[]{FileType.fileType_source};
		FileType[] fileTypeB = fileTypeA;
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
		DoapProject proj1 = new DoapProject(proj1Name, proj1HomePage);
		DoapProject[] artifactOfA = new DoapProject[] {proj1};
		DoapProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SpdxFile fileA = new SpdxFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SpdxFile fileB = new SpdxFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isSeenLicenseEquals());
		AnyLicenseInfo[] unique = fc.getUniqueSeenLicenses(DOCA, DOCB);
		assertEquals(1, unique.length);
		assertEquals(STD_LIC_ID_CC0, ((License)unique[0]).getLicenseId());
		SpdxFileDifference diff = fc.getFileDifference(DOCA, DOCB);
		assertFalse(diff.isSeenLicensesEquals());
		AnyLicenseInfo[] diffUnique = diff.getUniqueSeenLicensesA();
		assertEquals(1, diffUnique.length);
		assertEquals(STD_LIC_ID_CC0, ((License)diffUnique[0]).getLicenseId());
		
		fileA.setLicenseInfosFromFiles(seenLicenseB);
		fileB.setLicenseInfosFromFiles(seenLicenseA);
		fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isSeenLicenseEquals());
		unique = fc.getUniqueSeenLicenses(DOCA, DOCB);
		assertEquals(0, unique.length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#getUniqueArtifactOfA()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetUniqueArtifactOfAB() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		FileType[] fileTypeA = new FileType[]{FileType.fileType_source};
		FileType[] fileTypeB = fileTypeA;
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
		DoapProject proj1 = new DoapProject(proj1Name, proj1HomePage);
		String proj2HomePage = "http://proj2.page";
		String proj2Name = proj1Name;
		DoapProject proj2 = new DoapProject(proj2Name, proj2HomePage);
		String proj3Name = "project3";
		String proj3HomePage = proj1HomePage;
		DoapProject proj3 = new DoapProject(proj3Name, proj3HomePage);
		DoapProject[] artifactOfA = new DoapProject[] {proj1};
		DoapProject[] artifactOfB = new DoapProject[] {proj2};
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SpdxFile fileA = new SpdxFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SpdxFile fileB = new SpdxFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		// different name
		fileB.setArtifactOf(new DoapProject[] {proj3});
		SpdxFileComparer fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isArtifactOfEquals());
		DoapProject[] uniqueA = fc.getUniqueArtifactOf(DOCA, DOCB);
		DoapProject[] uniqueB = fc.getUniqueArtifactOf(DOCB, DOCA);
		assertEquals(1, uniqueA.length);
		assertEquals(proj1.getName(), uniqueA[0].getName());
		assertEquals(1, uniqueB.length);
		assertEquals(proj3.getName(), uniqueB[0].getName());
		SpdxFileDifference diff = fc.getFileDifference(DOCA, DOCB);
		assertFalse(diff.isArtifactOfsEquals());
		DoapProject[] diffUnique = diff.getArtifactsOfA();
		assertEquals(1, diffUnique.length);
		assertEquals(proj1.getName(), diffUnique[0].getName());
		diffUnique = diff.getArtifactsOfB();
		assertEquals(1, diffUnique.length);
		assertEquals(proj3.getName(), diffUnique[0].getName());
		
		
		// more A
		fileA.setArtifactOf(new DoapProject[] {proj1, proj3});
		fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isArtifactOfEquals());
		uniqueA = fc.getUniqueArtifactOf(DOCA, DOCB);
		uniqueB = fc.getUniqueArtifactOf(DOCB, DOCA);
		assertEquals(1, uniqueA.length);
		assertEquals(proj1.getName(), uniqueA[0].getName());
		assertEquals(0, uniqueB.length);

		// more B
		fileB.setArtifactOf(new DoapProject[] {proj2, proj3, proj1});
		fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isArtifactOfEquals());
		uniqueA = fc.getUniqueArtifactOf(DOCA, DOCB);
		uniqueB = fc.getUniqueArtifactOf(DOCB, DOCA);
		assertEquals(0, uniqueA.length);
		assertEquals(1, uniqueB.length);
		assertEquals(proj2.getName(), uniqueB[0].getName());
		
		// back to equals (different order)
		fileA.setArtifactOf(new DoapProject[] {proj1, proj2, proj3});
		fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isArtifactOfEquals());
		uniqueA = fc.getUniqueArtifactOf(DOCA, DOCB);
		uniqueB = fc.getUniqueArtifactOf(DOCB, DOCA);
		assertEquals(0, uniqueA.length);
		assertEquals(0, uniqueB.length);
	}



	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isCommentsEquals()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testIsLicenseCommentsEquals() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		FileType[] fileTypeA = new FileType[]{FileType.fileType_source};
		FileType[] fileTypeB = fileTypeA;
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
		DoapProject proj1 = new DoapProject(proj1Name, proj1HomePage);
		DoapProject[] artifactOfA = new DoapProject[] {proj1};
		DoapProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SpdxFile fileA = new SpdxFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SpdxFile fileB = new SpdxFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isLicenseCommmentsEquals());
		SpdxFileDifference diff = fc.getFileDifference(DOCA, DOCB);
		assertFalse(diff.isLicenseCommentsEqual());
		
		fileB.setLicenseComment(licenseCommentsA);
		fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isLicenseCommmentsEquals());		
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isCopyrightsEquals()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testIsCopyrightsEquals() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		FileType[] fileTypeA = new FileType[]{FileType.fileType_source};
		FileType[] fileTypeB = fileTypeA;
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
		DoapProject proj1 = new DoapProject(proj1Name, proj1HomePage);
		DoapProject[] artifactOfA = new DoapProject[] {proj1};
		DoapProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SpdxFile fileA = new SpdxFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SpdxFile fileB = new SpdxFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isCopyrightsEquals());
		SpdxFileDifference diff = fc.getFileDifference(DOCA, DOCB);
		assertFalse(diff.isCopyrightsEqual());
		
		fileB.setCopyrightText(copyrightA);
		fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isCopyrightsEquals());	
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isLicenseCommmentsEquals()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testIsCommmentsEquals() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		FileType[] fileTypeA = new FileType[]{FileType.fileType_source};
		FileType[] fileTypeB = fileTypeA;
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
		DoapProject proj1 = new DoapProject(proj1Name, proj1HomePage);
		DoapProject[] artifactOfA = new DoapProject[] {proj1};
		DoapProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = "file B comment";
		SpdxFile fileA = new SpdxFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SpdxFile fileB = new SpdxFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isCommentsEquals());
		SpdxFileDifference diff = fc.getFileDifference(DOCA, DOCB);
		assertFalse(diff.isCommentsEquals());

		fileB.setComment(fileCommentA);
	    fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isCommentsEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isChecksumsEquals()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testIsChecksumsEquals() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		FileType[] fileTypeA = new FileType[]{FileType.fileType_source};
		FileType[] fileTypeB = fileTypeA;
		String sha1A = "027bf72bf99b7e471f1a27989667a903658652bb";
		String sha1B = "cccbf72bf99b7e471f1a27989667a903658652bb";
		String sha1C = "dddbf72bf99b7e471f1a27989667a903658652bb";
		Checksum sumA = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, sha1A);
		Checksum sumB = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, sha1B);
		Checksum sumC = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, sha1C);
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
		DoapProject proj1 = new DoapProject(proj1Name, proj1HomePage);
		DoapProject[] artifactOfA = new DoapProject[] {proj1};
		DoapProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		String[] fileAContributors = new String[] {"contrib A", "contrib A2"};
		String[] fileBContributors = fileAContributors;
		String fileANotice = "noticeA";
		String fileBNotice = fileANotice;
		
		SpdxFile fileA = new SpdxFile(fileNameA, fileCommentA, new Annotation[0],
				new Relationship[0], concludedLicenseA, seenLicenseA, 
				copyrightA, licenseCommentsA, fileTypeA,
				new Checksum[] {sumA, sumB},
				fileAContributors, fileANotice, artifactOfA);
		SpdxFile fileB = new SpdxFile(fileNameB, fileCommentB, new Annotation[0],
				new Relationship[0], concludedLicenseB, seenLicenseB, 
				copyrightB, licenseCommentsB, fileTypeB,
				new Checksum[] {sumB, sumC},
				fileBContributors, fileBNotice, artifactOfB);
		
		SpdxFileComparer fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isChecksumsEquals());
		SpdxFileDifference diff = fc.getFileDifference(DOCA, DOCB);
		assertFalse(diff.isChecksumsEquals());
		Checksum[] result = diff.getUniqueChecksumsA();
		assertEquals(1, result.length);
		assertEquals(sumA, result[0]);
		result = diff.getUniqueChecksumsB();
		assertEquals(1, result.length);
		assertEquals(sumC, result[0]);

		fileA.setChecksums(new Checksum[] {sumC, sumB});
		fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
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
		FileType[] fileTypeA = new FileType[]{FileType.fileType_source};
		FileType[] fileTypeB = new FileType[]{FileType.fileType_binary, FileType.fileType_source};
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
		DoapProject proj1 = new DoapProject(proj1Name, proj1HomePage);
		DoapProject[] artifactOfA = new DoapProject[] {proj1};
		DoapProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SpdxFile fileA = new SpdxFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SpdxFile fileB = new SpdxFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isTypesEquals());
		SpdxFileDifference diff = fc.getFileDifference(DOCA, DOCB);
		assertFalse(diff.isTypeEqual());
		fileA.setFileTypes(fileTypeB);
		fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isTypesEquals());
	}
	
	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isContributorsEquals()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testIsContributorsEquals() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		FileType[] fileTypeA = new FileType[]{FileType.fileType_source};
		FileType[] fileTypeB = fileTypeA;
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
		DoapProject proj1 = new DoapProject(proj1Name, proj1HomePage);
		DoapProject[] artifactOfA = new DoapProject[] {proj1};
		DoapProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SpdxFile[] fileADependencies = new SpdxFile[0];
		String[] fileAContributors = new String[] {"ContributorA", "ContributorB"};
		String fileANotice = "File A Notice";
		SpdxFile[] fileBDependencies = fileADependencies;
		String[] fileBContributors = new String[] {"Different", "Contributors", "Entirely"};
		String fileBNotice = fileANotice;
		SpdxFile fileA = new SpdxFile(fileNameA, fileCommentA, new Annotation[0],
				new Relationship[0], concludedLicenseA, seenLicenseA, 
				copyrightA, licenseCommentsA, fileTypeA,
				new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, sha1A)},
				fileAContributors, fileANotice, artifactOfA);
		fileA.setFileDependencies(fileADependencies);
		
		SpdxFile fileB = new SpdxFile(fileNameB, fileCommentB, new Annotation[0],
				new Relationship[0], concludedLicenseB, seenLicenseB, 
				copyrightB, licenseCommentsB, fileTypeB,
				new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, sha1B)},
				fileBContributors, fileBNotice, artifactOfB);
		fileB.setFileDependencies(fileBDependencies);
		
		SpdxFileComparer fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isContributorsEquals());
		SpdxFileDifference diff = fc.getFileDifference(DOCA, DOCB);
		assertFalse(diff.isContributorsEqual());
		fileA.setFileContributors(fileBContributors);
		fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isContributorsEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isFileDependenciesEquals()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testIsFileDependenciesEquals() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		FileType[] fileTypeA = new FileType[]{FileType.fileType_source};
		FileType[] fileTypeB = fileTypeA;
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
		DoapProject proj1 = new DoapProject(proj1Name, proj1HomePage);
		DoapProject[] artifactOfA = new DoapProject[] {proj1};
		DoapProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SpdxFile[] fileADependencies = new SpdxFile[0];
		String[] fileAContributors = new String[] {"ContributorA", "ContributorB"};
		String fileANotice = "File A Notice";

		String[] fileBContributors = fileAContributors;
		String fileBNotice = fileANotice;
		SpdxFile fileA = new SpdxFile(fileNameA, fileCommentA, new Annotation[0],
				new Relationship[0], concludedLicenseA, seenLicenseA, 
				copyrightA, licenseCommentsA, fileTypeA,
				new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, sha1A)},
				fileAContributors, fileANotice, artifactOfA);
		fileA.setFileDependencies(fileADependencies);
		
		SpdxFile[] fileBDependencies = new SpdxFile[] {fileA};
		SpdxFile fileB = new SpdxFile(fileNameB, fileCommentB, new Annotation[0],
				new Relationship[0], concludedLicenseB, seenLicenseB, 
				copyrightB, licenseCommentsB, fileTypeB,
				new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, sha1B)},
				fileBContributors, fileBNotice, artifactOfB);
		fileB.setFileDependencies(fileBDependencies);
		
		SpdxFileComparer fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isFileDependenciesEquals());
		SpdxFileDifference diff = fc.getFileDifference(DOCA, DOCB);
		assertFalse(diff.isFileDependenciesEqual());
		fileA.setFileDependencies(fileBDependencies);
		fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isFileDependenciesEquals());
	}
	
	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isNoticeTextEqual()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testIsNoticeTextEquals() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		FileType[] fileTypeA = new FileType[]{FileType.fileType_source};
		FileType[] fileTypeB = fileTypeA;
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
		DoapProject proj1 = new DoapProject(proj1Name, proj1HomePage);
		DoapProject[] artifactOfA = new DoapProject[] {proj1};
		DoapProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SpdxFile[] fileADependencies = new SpdxFile[0];
		String[] fileAContributors = new String[] {"ContributorA", "ContributorB"};
		String fileANotice = "File A Notice";
		SpdxFile[] fileBDependencies = fileADependencies;
		
		String[] fileBContributors = fileAContributors;
		String fileBNotice = "file B Notice which is different";
		SpdxFile fileA = new SpdxFile(fileNameA, fileCommentA, new Annotation[0],
				new Relationship[0], concludedLicenseA, seenLicenseA, 
				copyrightA, licenseCommentsA, fileTypeA,
				new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, sha1A)},
				fileAContributors, fileANotice, artifactOfA);
		fileA.setFileDependencies(fileADependencies);
		
		SpdxFile fileB = new SpdxFile(fileNameB, fileCommentB, new Annotation[0],
				new Relationship[0], concludedLicenseB, seenLicenseB, 
				copyrightB, licenseCommentsB, fileTypeB,
				new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, sha1B)},
				fileBContributors, fileBNotice, artifactOfB);
		fileB.setFileDependencies(fileBDependencies);
		
		SpdxFileComparer fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		assertFalse(fc.isNoticeTextEquals());
		SpdxFileDifference diff = fc.getFileDifference(DOCA, DOCB);
		assertFalse(diff.isNoticeTextsEqual());
		fileA.setNoticeText(fileBNotice);
		fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertFalse(fc.isDifferenceFound());
		assertTrue(fc.isNoticeTextEquals());
	}
	
	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#isDifferenceFound()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testIsDifferenceFound() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		FileType[] fileTypeA = new FileType[]{FileType.fileType_source};
		FileType[] fileTypeB = fileTypeA;
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
		DoapProject proj1 = new DoapProject(proj1Name, proj1HomePage);
		DoapProject[] artifactOfA = new DoapProject[] {proj1};
		DoapProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SpdxFile fileA = new SpdxFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SpdxFile fileB = new SpdxFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertFalse(fc.isDifferenceFound());
		fileA.setComment("Different");
		fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		// Note - all of the other fields are tested in the individual test cases
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxFileComparer#getFileDifference()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetFileDifference() throws SpdxCompareException, InvalidLicenseStringException, InvalidSPDXAnalysisException {
		String fileNameA = "a/b/c/name.txt";
		String fileNameB = fileNameA;
		FileType[] fileTypeA = new FileType[]{FileType.fileType_source};
		FileType[] fileTypeB = new FileType[]{FileType.fileType_binary};
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
		DoapProject proj1 = new DoapProject(proj1Name, proj1HomePage);
		DoapProject[] artifactOfA = new DoapProject[] {proj1};
		DoapProject[] artifactOfB = artifactOfA;
		String fileCommentA = "file comment";
		String fileCommentB = fileCommentA;
		SpdxFile fileA = new SpdxFile(fileNameA,
				fileTypeA, sha1A, concludedLicenseA,
				seenLicenseA, licenseCommentsA, copyrightA,
				artifactOfA, fileCommentA);
		
		SpdxFile fileB = new SpdxFile(fileNameB,
				fileTypeB, sha1B, concludedLicenseB,
				seenLicenseB, licenseCommentsB, copyrightB,
				artifactOfB, fileCommentB);
		
		SpdxFileComparer fc = new SpdxFileComparer(LICENSE_XLATION);
		fc.addDocumentFile(DOCA, fileA);
		fc.addDocumentFile(DOCB, fileB);
		assertTrue(fc.isDifferenceFound());
		SpdxFileDifference diff = fc.getFileDifference(DOCA, DOCB);
		assertFalse(diff.isTypeEqual());
		//Note - each of the individual fields is tested in their respecive unit tests
	}

}
