/**
 * Copyright (c) 2016 Source Auditor Inc.
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

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.DoapProject;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.Checksum.ChecksumAlgorithm;
import org.spdx.rdfparser.model.SpdxFile.FileType;
import org.spdx.rdfparser.model.SpdxSnippet;
import org.spdx.rdfparser.model.pointer.ByteOffsetPointer;
import org.spdx.rdfparser.model.pointer.LineCharPointer;
import org.spdx.rdfparser.model.pointer.StartEndPointer;

import com.google.common.collect.Maps;

/**
 * @author Gary O'Neall
 *
 */
public class SpdxSnippetComparerTest {
	
	static final String SNIPPET_NAME1 = "snippet1";
	static final String SNIPPET_NAME2 = "snippet2";
	static final String COMMENT1 = "comment1";
	static final String COMMENT2 = "comment2";
	static final String CONCLUDED_LICENSE_STRING = "ADSL";
	static final String SEEN_LICENSE_STRING = "APSL-2.0";
	static final String COPYRIGHT_TEXT = "copyrightText";
	static final String LICENSE_COMMENT = "License comment";
	
	AnyLicenseInfo CONCLUDED_LICENSE;
	AnyLicenseInfo[] SEEN_LICENSES;
	SpdxFile FROM_FILE;
	SpdxSnippet SNIPPET1;
	
	Integer OFFSET1_1 = new Integer(2342);
	ByteOffsetPointer BOP_POINTER1_1;
	Integer LINE1_1 = new Integer(113);
	LineCharPointer LCP_POINTER1_1; 
	Integer OFFSET2_1 = new Integer(444);
	ByteOffsetPointer BOP_POINTER2_1;
	Integer LINE2_1 = new Integer(23422);
	LineCharPointer LCP_POINTER2_1; 
	Integer OFFSET1_2 = new Integer(3542);
	ByteOffsetPointer BOP_POINTER1_2;
	Integer LINE1_2 = new Integer(555);
	LineCharPointer LCP_POINTER1_2; 
	Integer OFFSET2_2 = new Integer(2444);
	ByteOffsetPointer BOP_POINTER2_2;
	Integer LINE2_2 = new Integer(23428);
	LineCharPointer LCP_POINTER2_2; 
	StartEndPointer BYTE_RANGE1;
	StartEndPointer BYTE_RANGE2;
	StartEndPointer LINE_RANGE1;
	StartEndPointer LINE_RANGE2;
	private static final Map<String, String> LICENSE_XLATION_MAPAB = Maps.newHashMap();
	static {
		LICENSE_XLATION_MAPAB.put("LicenseRef-1", "LicenseRef-4");
		LICENSE_XLATION_MAPAB.put("LicenseRef-2", "LicenseRef-5");
		LICENSE_XLATION_MAPAB.put("LicenseRef-3", "LicenseRef-6");
	}
	
	private static final Map<String, String> LICENSE_XLATION_MAPBA = Maps.newHashMap();
	private static final String FILE_NAME = "FileName";
	private static final String FILE_COMMENT = "File Comment";
	private static final String FILE_COPYRIGHT = "File Copyright";
	private static final String FILE_LICENSE_COMMENT = "File License Comment";
	private static final FileType[] FILE_TYPES = new FileType[] {FileType.fileType_source};
	private static final String FILE_NOTICE = "File Notice";
	
	static {
		LICENSE_XLATION_MAPBA.put("LicenseRef-4", "LicenseRef-1");
		LICENSE_XLATION_MAPBA.put("LicenseRef-5", "LicenseRef-2");
		LICENSE_XLATION_MAPBA.put("LicenseRef-6", "LicenseRef-3");
	}
	
	static final Checksum CHECKSUM1 = new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1, 
			"111bf72bf99b7e471f1a27989667a903658652bb");
	static final Checksum[] FILE_CHECKSUMS = new Checksum[] {CHECKSUM1};
	
	private final Map<SpdxDocument, Map<SpdxDocument, Map<String, String>>> LICENSE_XLATION_MAP = Maps.newHashMap();
	private SpdxDocument DOCA;
	private SpdxDocument DOCB;


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		CONCLUDED_LICENSE = LicenseInfoFactory.parseSPDXLicenseString(CONCLUDED_LICENSE_STRING);
		SEEN_LICENSES = new AnyLicenseInfo[] {LicenseInfoFactory.parseSPDXLicenseString(SEEN_LICENSE_STRING)};
		FROM_FILE = new SpdxFile(FILE_NAME, FILE_COMMENT, new Annotation[0], new Relationship[0], 
				CONCLUDED_LICENSE, SEEN_LICENSES, FILE_COPYRIGHT, FILE_LICENSE_COMMENT,
				FILE_TYPES, FILE_CHECKSUMS, new String[0], FILE_NOTICE, new DoapProject[0]);
		BOP_POINTER1_1 = new ByteOffsetPointer(FROM_FILE, OFFSET1_1);
		BOP_POINTER1_2 = new ByteOffsetPointer(FROM_FILE, OFFSET1_2);
		BYTE_RANGE1 = new StartEndPointer(BOP_POINTER1_1, BOP_POINTER1_2);
		LCP_POINTER1_1 = new LineCharPointer(FROM_FILE, LINE1_1);
		LCP_POINTER1_2 = new LineCharPointer(FROM_FILE, LINE1_2);
		LINE_RANGE1 = new StartEndPointer(LCP_POINTER1_1, LCP_POINTER1_2);
		BOP_POINTER2_1 = new ByteOffsetPointer(FROM_FILE, OFFSET2_1);
		BOP_POINTER2_2 = new ByteOffsetPointer(FROM_FILE, OFFSET2_2);
		BYTE_RANGE2 = new StartEndPointer(BOP_POINTER2_1, BOP_POINTER2_2);
		LCP_POINTER2_1 = new LineCharPointer(FROM_FILE, LINE2_1);
		LCP_POINTER2_2 = new LineCharPointer(FROM_FILE, LINE2_2);
		LINE_RANGE2 = new StartEndPointer(LCP_POINTER2_1, LCP_POINTER2_2);
		SNIPPET1 = new SpdxSnippet(SNIPPET_NAME1, COMMENT1, null, null, 
				CONCLUDED_LICENSE, SEEN_LICENSES, COPYRIGHT_TEXT, LICENSE_COMMENT,
				FROM_FILE, BYTE_RANGE1, LINE_RANGE1);
		
		String uri1 = "http://doc/uri1";
		SpdxDocumentContainer containerA = new SpdxDocumentContainer(uri1);
		DOCA = containerA.getSpdxDocument();
		String uri2 = "http://doc/uri2";
		SpdxDocumentContainer containerB = new SpdxDocumentContainer(uri2);
		DOCB = containerB.getSpdxDocument();
		Map<SpdxDocument, Map<String, String>> bmap = Maps.newHashMap();
		bmap.put(DOCB, LICENSE_XLATION_MAPAB);
		LICENSE_XLATION_MAP.put(DOCA, bmap);
		Map<SpdxDocument, Map<String, String>> amap = Maps.newHashMap();
		amap.put(DOCA, LICENSE_XLATION_MAPBA);
		LICENSE_XLATION_MAP.put(DOCB, amap);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testNoDifference() throws InvalidSPDXAnalysisException, SpdxCompareException {

		SpdxSnippetComparer comparer = new SpdxSnippetComparer(LICENSE_XLATION_MAP);
		SpdxSnippet snippetClone = SNIPPET1.clone();
		DOCA.getDocumentContainer().addElement(SNIPPET1);
		DOCB.getDocumentContainer().addElement(snippetClone);
		comparer.addDocumentSnippet(DOCA, SNIPPET1);
		comparer.addDocumentSnippet(DOCB, snippetClone);
		assertFalse(comparer.isDifferenceFound());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxSnippetComparer#getSnippetFromFileDifference(org.spdx.rdfparser.model.SpdxDocument, org.spdx.rdfparser.model.SpdxDocument)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testGetSnippetFromFileDifference() throws InvalidSPDXAnalysisException, SpdxCompareException {

		SpdxSnippetComparer comparer = new SpdxSnippetComparer(LICENSE_XLATION_MAP);
		SpdxSnippet snippetClone = SNIPPET1.clone();
		SpdxFile snippetFromFile = snippetClone.getSnippetFromFile();
		String newCopyright = "New copyright";
		snippetFromFile.setCopyrightText(newCopyright);
		// Note: we need to set the from file so that the start/end pointers get updated to the correct file
		snippetClone.setSnippetFromFile(snippetFromFile);
		DOCA.getDocumentContainer().addElement(SNIPPET1);
		DOCB.getDocumentContainer().addElement(snippetClone);
		comparer.addDocumentSnippet(DOCA, SNIPPET1);
		comparer.addDocumentSnippet(DOCB, snippetClone);
		assertTrue(comparer.isDifferenceFound());
		assertFalse(comparer.isSnippetFromFilesEquals());
		SpdxFileDifference fileDiff = comparer.getSnippetFromFileDifference(DOCA, DOCB);
		assertFalse(fileDiff.isCopyrightsEqual());
		assertEquals(FILE_COPYRIGHT, fileDiff.getCopyrightA());
		assertEquals(newCopyright, fileDiff.getCopyrightB());
	}
	
	@Test
	public void testGetSnippetFromFileDifferentFileName() throws InvalidSPDXAnalysisException, SpdxCompareException {

		SpdxSnippetComparer comparer = new SpdxSnippetComparer(LICENSE_XLATION_MAP);
		SpdxSnippet snippetClone = SNIPPET1.clone();
		SpdxFile snippetFromFile = snippetClone.getSnippetFromFile();
		String newFileName = "NewFIleName.c";
		snippetFromFile.setName(newFileName);
		snippetClone.setSnippetFromFile(snippetFromFile);
		DOCA.getDocumentContainer().addElement(SNIPPET1);
		DOCB.getDocumentContainer().addElement(snippetClone);
		comparer.addDocumentSnippet(DOCA, SNIPPET1);
		comparer.addDocumentSnippet(DOCB, snippetClone);
		assertTrue(comparer.isDifferenceFound());
		assertFalse(comparer.isSnippetFromFilesEquals());
		assertTrue(comparer.getSnippetFromFileDifference(DOCA, DOCB) == null);
		assertEquals(FILE_NAME, comparer.getUniqueSnippetFromFile(DOCA, DOCB).getName());
		assertEquals(newFileName, comparer.getUniqueSnippetFromFile(DOCB, DOCA).getName());
		
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxSnippetComparer#isByteRangeEquals()}.
	 */
	@Test
	public void testIsByteRangeEquals() throws InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxSnippetComparer comparer = new SpdxSnippetComparer(LICENSE_XLATION_MAP);
		SpdxSnippet snippetClone = SNIPPET1.clone();
		snippetClone.setByteRange(BYTE_RANGE2);
		DOCA.getDocumentContainer().addElement(SNIPPET1);
		DOCB.getDocumentContainer().addElement(snippetClone);
		comparer.addDocumentSnippet(DOCA, SNIPPET1);
		comparer.addDocumentSnippet(DOCB, snippetClone);
		assertTrue(comparer.isDifferenceFound());
		assertFalse(comparer.isByteRangeEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxSnippetComparer#isLineRangeEquals()}.
	 */
	@Test
	public void testIsLineRangeEquals() throws InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxSnippetComparer comparer = new SpdxSnippetComparer(LICENSE_XLATION_MAP);
		SpdxSnippet snippetClone = SNIPPET1.clone();
		snippetClone.setLineRange(LINE_RANGE2);
		DOCA.getDocumentContainer().addElement(SNIPPET1);
		DOCB.getDocumentContainer().addElement(snippetClone);
		comparer.addDocumentSnippet(DOCA, SNIPPET1);
		comparer.addDocumentSnippet(DOCB, snippetClone);
		assertTrue(comparer.isDifferenceFound());
		assertFalse(comparer.isLineRangeEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxSnippetComparer#isNameEquals()}.
	 */
	@Test
	public void testIsNameEquals() throws InvalidSPDXAnalysisException, SpdxCompareException {
		SpdxSnippetComparer comparer = new SpdxSnippetComparer(LICENSE_XLATION_MAP);
		SpdxSnippet snippetClone = SNIPPET1.clone();
		String newSnippetName = "NewSnippetName";
		snippetClone.setName(newSnippetName);
		DOCA.getDocumentContainer().addElement(SNIPPET1);
		DOCB.getDocumentContainer().addElement(snippetClone);
		comparer.addDocumentSnippet(DOCA, SNIPPET1);
		comparer.addDocumentSnippet(DOCB, snippetClone);
		assertTrue(comparer.isDifferenceFound());
		assertFalse(comparer.isNameEquals());
	}

}
