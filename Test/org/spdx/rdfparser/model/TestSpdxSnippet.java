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
package org.spdx.rdfparser.model;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ConjunctiveLicenseSet;
import org.spdx.rdfparser.license.DisjunctiveLicenseSet;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.model.Annotation.AnnotationType;
import org.spdx.rdfparser.model.Checksum.ChecksumAlgorithm;
import org.spdx.rdfparser.model.pointer.ByteOffsetPointer;
import org.spdx.rdfparser.model.pointer.LineCharPointer;
import org.spdx.rdfparser.model.pointer.StartEndPointer;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @author Gary
 *
 */
public class TestSpdxSnippet {

	static final String[] NONSTD_IDS = new String[] {SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"1",
		SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"2", SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"3",
		SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"4"};
	static final String[] NONSTD_TEXTS = new String[] {"text1", "text2", "text3", "text4"};
	static final String[] STD_IDS = new String[] {"AFL-3.0", "CECILL-B", "EUPL-1.0"};
	static final String[] STD_TEXTS = new String[] {"std text1", "std text2", "std text3"};
	
	static DateFormat DATEFORMAT = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);
	static String DATE_NOW = DATEFORMAT.format(new Date());
	static final Annotation ANNOTATION1 = new Annotation("Organization: Annotator1", 
			AnnotationType.annotationType_other, DATE_NOW, "Comment 1");
	static final Annotation ANNOTATION2 = new Annotation("Tool: Annotator2", 
			AnnotationType.annotationType_review, DATE_NOW, "Comment 2");
	static final Annotation ANNOTATION3 = new Annotation("Person: Annotator3", 
			AnnotationType.annotationType_other, DATE_NOW, "Comment 3");
	
	ExtractedLicenseInfo[] NON_STD_LICENSES;
	SpdxListedLicense[] STANDARD_LICENSES;
	DisjunctiveLicenseSet[] DISJUNCTIVE_LICENSES;
	ConjunctiveLicenseSet[] CONJUNCTIVE_LICENSES;
	
	ConjunctiveLicenseSet COMPLEX_LICENSE;
	
	Resource[] NON_STD_LICENSES_RESOURCES;
	Resource[] STANDARD_LICENSES_RESOURCES;
	Resource[] DISJUNCTIVE_LICENSES_RESOURCES;
	Resource[] CONJUNCTIVE_LICENSES_RESOURCES;
	Resource COMPLEX_LICENSE_RESOURCE;
	
	SpdxFile FROM_FILE1;
	SpdxFile FROM_FILE2;
	
	Resource REFERENCED_RESOURCE1;
	Resource REFERENCED_RESOURCE2;
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
	
	
	Model model;
	
	IModelContainer modelContainer;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		NON_STD_LICENSES = new ExtractedLicenseInfo[NONSTD_IDS.length];
		for (int i = 0; i < NONSTD_IDS.length; i++) {
			NON_STD_LICENSES[i] = new ExtractedLicenseInfo(NONSTD_IDS[i], NONSTD_TEXTS[i]);
		}
		
		STANDARD_LICENSES = new SpdxListedLicense[STD_IDS.length];
		for (int i = 0; i < STD_IDS.length; i++) {
			STANDARD_LICENSES[i] = new SpdxListedLicense("Name "+String.valueOf(i), 
					STD_IDS[i], STD_TEXTS[i], new String[] {"URL "+String.valueOf(i)}, "Notes "+String.valueOf(i), 
					"LicHeader "+String.valueOf(i), "Template "+String.valueOf(i), true);
		}
		
		DISJUNCTIVE_LICENSES = new DisjunctiveLicenseSet[3];
		CONJUNCTIVE_LICENSES = new ConjunctiveLicenseSet[2];
		
		DISJUNCTIVE_LICENSES[0] = new DisjunctiveLicenseSet(new AnyLicenseInfo[] {
				NON_STD_LICENSES[0], NON_STD_LICENSES[1], STANDARD_LICENSES[1]
		});
		CONJUNCTIVE_LICENSES[0] = new ConjunctiveLicenseSet(new AnyLicenseInfo[] {
				STANDARD_LICENSES[0], NON_STD_LICENSES[0], STANDARD_LICENSES[1]
		});
		CONJUNCTIVE_LICENSES[1] = new ConjunctiveLicenseSet(new AnyLicenseInfo[] {
				DISJUNCTIVE_LICENSES[0], NON_STD_LICENSES[2]
		});
		DISJUNCTIVE_LICENSES[1] = new DisjunctiveLicenseSet(new AnyLicenseInfo[] {
				CONJUNCTIVE_LICENSES[1], NON_STD_LICENSES[0], STANDARD_LICENSES[0]
		});
		DISJUNCTIVE_LICENSES[2] = new DisjunctiveLicenseSet(new AnyLicenseInfo[] {
				DISJUNCTIVE_LICENSES[1], CONJUNCTIVE_LICENSES[0], STANDARD_LICENSES[2]
		});
		COMPLEX_LICENSE = new ConjunctiveLicenseSet(new AnyLicenseInfo[] {
				DISJUNCTIVE_LICENSES[2], NON_STD_LICENSES[2], CONJUNCTIVE_LICENSES[1]
		});
		model = ModelFactory.createDefaultModel();
		modelContainer = new ModelContainerForTest(model, "http://testnamespace.com");
		NON_STD_LICENSES_RESOURCES = new Resource[NON_STD_LICENSES.length];
		for (int i = 0; i < NON_STD_LICENSES.length; i++) {
			NON_STD_LICENSES_RESOURCES[i] = NON_STD_LICENSES[i].createResource(modelContainer);
		}
		STANDARD_LICENSES_RESOURCES = new Resource[STANDARD_LICENSES.length];
		for (int i = 0; i < STANDARD_LICENSES.length; i++) {
			STANDARD_LICENSES_RESOURCES[i] = STANDARD_LICENSES[i].createResource(modelContainer);
		}
		CONJUNCTIVE_LICENSES_RESOURCES = new Resource[CONJUNCTIVE_LICENSES.length];
		for (int i = 0; i < CONJUNCTIVE_LICENSES.length; i++) {
			CONJUNCTIVE_LICENSES_RESOURCES[i] = CONJUNCTIVE_LICENSES[i].createResource(modelContainer);
		}
		DISJUNCTIVE_LICENSES_RESOURCES = new Resource[DISJUNCTIVE_LICENSES.length];
		for (int i = 0; i < DISJUNCTIVE_LICENSES.length; i++) {
			DISJUNCTIVE_LICENSES_RESOURCES[i] = DISJUNCTIVE_LICENSES[i].createResource(modelContainer);
		}
		COMPLEX_LICENSE_RESOURCE = COMPLEX_LICENSE.createResource(modelContainer);
		
		FROM_FILE1 = new SpdxFile("fromFile1", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE, null,
				null, new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
						"1123456789abcdef0123456789abcdef01234567")}, null, null, null);
		
		FROM_FILE2 = new SpdxFile("fromFile2", null, null, null, 
				STANDARD_LICENSES[0], STANDARD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE, null,
				null, new Checksum[] {new Checksum(ChecksumAlgorithm.checksumAlgorithm_sha1,
						"5555556789abcdef0123456789abcdef01234567")}, null, null, null);
		
		BOP_POINTER1_1 = new ByteOffsetPointer(FROM_FILE1, OFFSET1_1);
		BOP_POINTER1_2 = new ByteOffsetPointer(FROM_FILE1, OFFSET1_2);
		BYTE_RANGE1 = new StartEndPointer(BOP_POINTER1_1, BOP_POINTER1_2);
		LCP_POINTER1_1 = new LineCharPointer(FROM_FILE1, LINE1_1);
		LCP_POINTER1_2 = new LineCharPointer(FROM_FILE1, LINE1_2);
		LINE_RANGE1 = new StartEndPointer(LCP_POINTER1_1, LCP_POINTER1_2);
		BOP_POINTER2_1 = new ByteOffsetPointer(FROM_FILE2, OFFSET2_1);
		BOP_POINTER2_2 = new ByteOffsetPointer(FROM_FILE2, OFFSET2_2);
		BYTE_RANGE2 = new StartEndPointer(BOP_POINTER2_1, BOP_POINTER2_2);
		LCP_POINTER2_1 = new LineCharPointer(FROM_FILE2, LINE2_1);
		LCP_POINTER2_2 = new LineCharPointer(FROM_FILE2, LINE2_2);
		LINE_RANGE2 = new StartEndPointer(LCP_POINTER2_1, LCP_POINTER2_2);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxSnippet#getPropertiesFromModel()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetPropertiesFromModel() throws InvalidSPDXAnalysisException {
		SpdxSnippet snippet = new SpdxSnippet("snippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, FROM_FILE1, BYTE_RANGE1, LINE_RANGE1);
		Resource sr = snippet.createResource(modelContainer);
		SpdxSnippet snippetRef = new SpdxSnippet(modelContainer, sr.asNode());
		assertTrue(FROM_FILE1.equivalent(snippetRef.getSnippetFromFile()));
		assertTrue(BYTE_RANGE1.equivalent(snippetRef.getByteRange()));
		assertTrue(LINE_RANGE1.equivalent(snippetRef.getLineRange()));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxSnippet#findDuplicateResource(org.spdx.rdfparser.IModelContainer, java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testFindDuplicateResource() throws InvalidSPDXAnalysisException {
		SpdxSnippet snippet = new SpdxSnippet("snippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, FROM_FILE1, BYTE_RANGE1, LINE_RANGE1);
		Resource sr = snippet.createResource(modelContainer);
		
		Node byteRangeProperty = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_SNIPPET_RANGE).asNode();
		Triple byteRangeMatch = Triple.createMatch(null, byteRangeProperty, null);
		ExtendedIterator<Triple> byteRangeMatchIter = model.getGraph().find(byteRangeMatch);	
		int numByteRanges = 0;
		while (byteRangeMatchIter.hasNext()) {
			byteRangeMatchIter.next();
			numByteRanges++;
		}
		assertEquals(2, numByteRanges);
		SpdxSnippet clonedSnippet = snippet.clone();
		Resource sr2 = clonedSnippet.createResource(modelContainer);
		assertEquals(sr, sr2);

		byteRangeProperty = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_SNIPPET_RANGE).asNode();
		byteRangeMatch = Triple.createMatch(null, byteRangeProperty, null);
		byteRangeMatchIter = model.getGraph().find(byteRangeMatch);	
		numByteRanges = 0;
		while (byteRangeMatchIter.hasNext()) {
			byteRangeMatchIter.next();
			numByteRanges++;
		}
		assertEquals(2, numByteRanges);
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxSnippet#getType(com.hp.hpl.jena.rdf.model.Model)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetType() throws InvalidSPDXAnalysisException {
		SpdxSnippet snippet = new SpdxSnippet("snippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, FROM_FILE1, BYTE_RANGE1, LINE_RANGE1);
		snippet.createResource(modelContainer);
		String expected = SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_SNIPPET;
		Resource result = snippet.getType(model);
		assertTrue(result.isURIResource());
		assertEquals(expected, result.getURI());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxSnippet#verify()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testVerify() throws InvalidSPDXAnalysisException {
		SpdxSnippet snippet = new SpdxSnippet("snippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, FROM_FILE1, BYTE_RANGE1, LINE_RANGE1);
		snippet.createResource(modelContainer);
		List<String> result = snippet.verify();
		assertEquals(0, result.size());
		// missing file
		SpdxSnippet snippet2 = new SpdxSnippet("snippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, null, BYTE_RANGE1, LINE_RANGE1);
		snippet2.createResource(modelContainer);
		result = snippet2.verify();
		assertEquals(1, result.size());
		// missing byte range
		SpdxSnippet snippet3 = new SpdxSnippet("snippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, FROM_FILE2, null, LINE_RANGE1);
		snippet3.createResource(modelContainer);
		result = snippet3.verify();
		assertEquals(1, result.size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxSnippet#equivalent(org.spdx.rdfparser.model.IRdfModel)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testEquivalentIRdfModel() throws InvalidSPDXAnalysisException {
		SpdxSnippet snippet = new SpdxSnippet("snippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, FROM_FILE1, BYTE_RANGE1, LINE_RANGE1);
		SpdxSnippet snippet2 = new SpdxSnippet("snippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, FROM_FILE1, BYTE_RANGE1, LINE_RANGE1);
		assertTrue(snippet.equivalent(snippet2));
		snippet.createResource(modelContainer);
		snippet2.createResource(modelContainer);
		assertTrue(snippet.equivalent(snippet2));
		// Different File
		SpdxSnippet snippet3 = new SpdxSnippet("snippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, FROM_FILE2, BYTE_RANGE1, LINE_RANGE1);
		assertFalse(snippet3.equivalent(snippet));
		assertFalse(snippet.equivalent(snippet3));
		snippet3.createResource(modelContainer);
		assertFalse(snippet3.equivalent(snippet));
		assertFalse(snippet.equivalent(snippet3));
		// different byte range
		SpdxSnippet snippet4 = new SpdxSnippet("snippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, FROM_FILE1, BYTE_RANGE2, LINE_RANGE1);
		assertFalse(snippet4.equivalent(snippet));
		assertFalse(snippet.equivalent(snippet4));
		snippet4.createResource(modelContainer);
		assertFalse(snippet4.equivalent(snippet));
		assertFalse(snippet.equivalent(snippet4));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxSnippet#setSnippetFromFile(org.spdx.rdfparser.model.SpdxFile)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetSnippetFromFile() throws InvalidSPDXAnalysisException {
		SpdxSnippet snippet = new SpdxSnippet("snippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, FROM_FILE1, BYTE_RANGE1, LINE_RANGE1);
		assertTrue(FROM_FILE1.equivalent(snippet.getSnippetFromFile()));
		Resource sr = snippet.createResource(modelContainer);
		SpdxSnippet snCopy = new SpdxSnippet(modelContainer, sr.asNode());
		assertTrue(FROM_FILE1.equivalent(snCopy.getSnippetFromFile()));
		assertTrue(FROM_FILE1.equivalent(snippet.getSnippetFromFile()));
		snCopy.setSnippetFromFile(FROM_FILE2);
		assertTrue(FROM_FILE2.equivalent(snCopy.getSnippetFromFile()));
		assertTrue(FROM_FILE2.equivalent(snippet.getSnippetFromFile()));
		// setting the from file should also set the reference file in the pointers
		assertTrue(FROM_FILE2.equivalent(snCopy.getByteRange().getStartPointer().getReference()));
		assertTrue(FROM_FILE2.equivalent(snCopy.getByteRange().getEndPointer().getReference()));
		assertTrue(FROM_FILE2.equivalent(snCopy.getLineRange().getStartPointer().getReference()));
		assertTrue(FROM_FILE2.equivalent(snCopy.getLineRange().getEndPointer().getReference()));
		assertTrue(FROM_FILE2.equivalent(snippet.getByteRange().getStartPointer().getReference()));
		assertTrue(FROM_FILE2.equivalent(snippet.getByteRange().getEndPointer().getReference()));
		assertTrue(FROM_FILE2.equivalent(snippet.getLineRange().getStartPointer().getReference()));
		assertTrue(FROM_FILE2.equivalent(snippet.getLineRange().getEndPointer().getReference()));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxSnippet#setByteRange(org.spdx.rdfparser.model.pointer.StartEndPointer)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetByteRange() throws InvalidSPDXAnalysisException {
		SpdxSnippet snippet = new SpdxSnippet("snippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, FROM_FILE1, BYTE_RANGE1, LINE_RANGE1);
		assertTrue(BYTE_RANGE1.equivalent(snippet.getByteRange()));
		Resource sr = snippet.createResource(modelContainer);
		SpdxSnippet snCopy = new SpdxSnippet(modelContainer, sr.asNode());
		assertTrue(BYTE_RANGE1.equivalent(snCopy.getByteRange()));
		assertTrue(BYTE_RANGE1.equivalent(snippet.getByteRange()));
		snippet.setByteRange(BYTE_RANGE2);
		assertTrue(BYTE_RANGE2.equivalent(snCopy.getByteRange()));
		assertTrue(BYTE_RANGE2.equivalent(snippet.getByteRange()));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxSnippet#setLineRange(org.spdx.rdfparser.model.pointer.StartEndPointer)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testSetLineRange() throws InvalidSPDXAnalysisException {
		SpdxSnippet snippet = new SpdxSnippet("snippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, FROM_FILE1, BYTE_RANGE1, LINE_RANGE1);
		assertTrue(LINE_RANGE1.equivalent(snippet.getLineRange()));
		Resource sr = snippet.createResource(modelContainer);
		SpdxSnippet snCopy = new SpdxSnippet(modelContainer, sr.asNode());
		assertTrue(LINE_RANGE1.equivalent(snCopy.getLineRange()));
		assertTrue(LINE_RANGE1.equivalent(snippet.getLineRange()));
		snippet.setLineRange(LINE_RANGE2);
		assertTrue(LINE_RANGE2.equivalent(snCopy.getLineRange()));
		assertTrue(LINE_RANGE2.equivalent(snippet.getLineRange()));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxSnippet#clone()}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testClone() throws InvalidSPDXAnalysisException {
		SpdxSnippet snippet = new SpdxSnippet("snippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, FROM_FILE1, BYTE_RANGE1, LINE_RANGE1);
		snippet.createResource(modelContainer);
		SpdxSnippet clone = snippet.clone();
		assertTrue(snippet.equivalent(clone));
		assertTrue(snippet.getByteRange().equivalent(clone.getByteRange()));
		assertTrue(snippet.getSnippetFromFile().equivalent(clone.getSnippetFromFile()));
		assertTrue(snippet.getLineRange().equivalent(clone.getLineRange()));
		clone.setByteRange(BYTE_RANGE2);
		assertFalse(snippet.equivalent(clone));
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.model.SpdxSnippet#compareTo(org.spdx.rdfparser.model.SpdxSnippet)}.
	 */
	@Test
	public void testCompareTo() {
		SpdxSnippet snippet = new SpdxSnippet("snippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, FROM_FILE1, BYTE_RANGE1, LINE_RANGE1);
		// same
		SpdxSnippet snippet2 = new SpdxSnippet("snippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, FROM_FILE1, BYTE_RANGE1, LINE_RANGE1);
		assertEquals(0, snippet.compareTo(snippet2));
		// different filename
		SpdxSnippet snippet3 = new SpdxSnippet("AsnippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, FROM_FILE1, BYTE_RANGE1, LINE_RANGE1);
		assertTrue(snippet.compareTo(snippet3) > 0);
		// different from file
		SpdxSnippet snippet4 = new SpdxSnippet("snippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, FROM_FILE2, BYTE_RANGE1, LINE_RANGE1);
		assertTrue(snippet.compareTo(snippet4) < 0);
		// different byterange
		SpdxSnippet snippet5 = new SpdxSnippet("snippetName", null, null, null, 
				COMPLEX_LICENSE, NON_STD_LICENSES, SpdxRdfConstants.NOASSERTION_VALUE,
				null, FROM_FILE1, BYTE_RANGE2, LINE_RANGE1);
		assertTrue(snippet.compareTo(snippet5) < 0);
	}

}
