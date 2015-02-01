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
package org.spdx.compare;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Annotation.AnnotationType;
import org.spdx.rdfparser.model.Relationship.RelationshipType;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxElement;
import org.spdx.rdfparser.model.SpdxItem;

/**
 * @author Gary
 *
 */
public class SpdxItemComparerTest {
	private static final String COMMENTA = "comment A";
	private static final String COMMENTB = "comment B";
	private static final ExtractedLicenseInfo LICENSEA1 = new ExtractedLicenseInfo("LicenseRef-1", "License1");
	private static final ExtractedLicenseInfo LICENSEA2 = new ExtractedLicenseInfo("LicenseRef-2", "License2");
	private static final ExtractedLicenseInfo LICENSEA3 = new ExtractedLicenseInfo("LicenseRef-3", "License3");
	private static final ExtractedLicenseInfo LICENSEB1 = new ExtractedLicenseInfo("LicenseRef-4", "License1");
	private static final ExtractedLicenseInfo LICENSEB2 = new ExtractedLicenseInfo("LicenseRef-5", "License2");
	private static final ExtractedLicenseInfo LICENSEB3 = new ExtractedLicenseInfo("LicenseRef-6", "License3");
	private static final AnyLicenseInfo[] LICENSE_INFO_FROM_FILESA = new AnyLicenseInfo[] {LICENSEA1, LICENSEA2, LICENSEA3};
	private static final AnyLicenseInfo[] LICENSE_INFO_FROM_FILESB = new AnyLicenseInfo[] {LICENSEB1, LICENSEB2, LICENSEB3};
	private static final String LICENSE_COMMENTA = "License Comment A";
	private static final String LICENSE_COMMENTB = "License Comment B";
	private static final String COPYRIGHTA = "Copyright A";
	private static final String COPYRIGHTB = "Copyright B";
	private static final AnyLicenseInfo LICENSE_CONCLUDEDA = LICENSEA1;
	private static final AnyLicenseInfo LICENSE_CONCLUDEDB = LICENSEB1;
	private static final String NAMEA = "NameA";
	private static final String NAMEB = "NameB";
	private static final HashMap<String, String> LICENSE_XLATION_MAP = new HashMap<String, String>();
	
	static {
		LICENSE_XLATION_MAP.put("LicenseRef-1", "LicenseRef-4");
		LICENSE_XLATION_MAP.put("LicenseRef-2", "LicenseRef-5");
		LICENSE_XLATION_MAP.put("LicenseRef-3", "LicenseRef-6");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	
	private Annotation ANNOTATION1;
	private Annotation ANNOTATION2;
	private Annotation ANNOTATION3;
	private Annotation ANNOTATION4;
	private Annotation[] ANNOTATIONSA;
	@SuppressWarnings("unused")
	private Annotation[] ANNOTATIONSB;
	
	private Relationship[] RELATIONSHIPSA;
	@SuppressWarnings("unused")
	private Relationship[] RELATIONSHIPSB;
	private SpdxElement RELATED_ELEMENT1;
	private SpdxElement RELATED_ELEMENT2;
	private SpdxElement RELATED_ELEMENT3;
	private SpdxElement RELATED_ELEMENT4;
	private Relationship RELATIONSHIP1;
	private Relationship RELATIONSHIP2;
	private Relationship RELATIONSHIP3;
	private Relationship RELATIONSHIP4;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		ANNOTATION1 = new Annotation("Annotator1", AnnotationType.annotationType_other, 
				"2010-01-29T18:30:22Z", "AnnotationComment1");
		ANNOTATION2 = new Annotation("Annotator2", AnnotationType.annotationType_review, 
				"2011-01-29T18:30:22Z", "AnnotationComment2");
		ANNOTATION3 = new Annotation("Annotator3", AnnotationType.annotationType_other, 
				"2012-01-29T18:30:22Z", "AnnotationComment3");
		ANNOTATION4 = new Annotation("Annotator4", AnnotationType.annotationType_review, 
				"2013-01-29T18:30:22Z", "AnnotationComment4");
		ANNOTATIONSA = new Annotation[] {ANNOTATION1, ANNOTATION2};
		ANNOTATIONSB = new Annotation[] {ANNOTATION3, ANNOTATION4};
		RELATED_ELEMENT1 = new SpdxElement("relatedElementName1", 
				"related element comment 1", null, null);
		RELATED_ELEMENT2 = new SpdxElement("relatedElementName2", 
				"related element comment 2", null, null);
		RELATED_ELEMENT3 = new SpdxElement("relatedElementName3", 
				"related element comment 3", null, null);
		RELATED_ELEMENT4 = new SpdxElement("relatedElementName4", 
				"related element comment 4", null, null);
		RELATIONSHIP1 = new Relationship(RELATED_ELEMENT1, 
				RelationshipType.relationshipType_contains, "Relationship Comment1");
		RELATIONSHIP2 = new Relationship(RELATED_ELEMENT2, 
				RelationshipType.relationshipType_dynamicLink, "Relationship Comment2");
		RELATIONSHIP3 = new Relationship(RELATED_ELEMENT3, 
				RelationshipType.relationshipType_dataFile, "Relationship Comment3");
		RELATIONSHIP4 = new Relationship(RELATED_ELEMENT4, 
				RelationshipType.relationshipType_distributionArtifact, "Relationship Comment4");
		RELATIONSHIPSA = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		RELATIONSHIPSB = new Relationship[] {RELATIONSHIP3, RELATIONSHIP4};
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxItemComparer#compare(org.spdx.rdfparser.model.SpdxItem, org.spdx.rdfparser.model.SpdxItem, java.util.HashMap)}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testCompare() throws SpdxCompareException {
		SpdxItem itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItem itemB = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItemComparer comparer = new SpdxItemComparer();
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertFalse(comparer.isDifferenceFound());
		assertTrue(comparer.isAnnotationsEquals());
		assertTrue(comparer.isCommentsEquals());
		assertTrue(comparer.isConcludedLicenseEquals());
		assertTrue(comparer.isCopyrightsEquals());
		assertTrue(comparer.isLicenseCommmentsEquals());
		assertTrue(comparer.isNamesEquals());
		assertTrue(comparer.isRelationshipsEquals());
		assertTrue(comparer.isSeenLicenseEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxItemComparer#isConcludedLicenseEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsConcludedLicenseEquals() throws SpdxCompareException {
		SpdxItem itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItem itemB = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSEB2, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItemComparer comparer = new SpdxItemComparer();
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertTrue(comparer.isDifferenceFound());
		assertTrue(comparer.isAnnotationsEquals());
		assertTrue(comparer.isCommentsEquals());
		assertFalse(comparer.isConcludedLicenseEquals());
		assertTrue(comparer.isCopyrightsEquals());
		assertTrue(comparer.isLicenseCommmentsEquals());
		assertTrue(comparer.isNamesEquals());
		assertTrue(comparer.isRelationshipsEquals());
		assertTrue(comparer.isSeenLicenseEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxItemComparer#isSeenLicenseEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsSeenLicenseEquals() throws SpdxCompareException {
		SpdxItem itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItem itemB = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, new AnyLicenseInfo[] {LICENSEB1}, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItemComparer comparer = new SpdxItemComparer();
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertTrue(comparer.isDifferenceFound());
		assertTrue(comparer.isAnnotationsEquals());
		assertTrue(comparer.isCommentsEquals());
		assertTrue(comparer.isConcludedLicenseEquals());
		assertTrue(comparer.isCopyrightsEquals());
		assertTrue(comparer.isLicenseCommmentsEquals());
		assertTrue(comparer.isNamesEquals());
		assertTrue(comparer.isRelationshipsEquals());
		assertFalse(comparer.isSeenLicenseEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxItemComparer#getUniqueSeenLicensesB()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testGetUniqueSeenLicensesB() throws SpdxCompareException {
		AnyLicenseInfo[] s1 = new AnyLicenseInfo[] {LICENSEA1};
		AnyLicenseInfo[] s2 = new AnyLicenseInfo[] {LICENSEB1, LICENSEB2};
		SpdxItem itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, s1, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItem itemB = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, s2, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItemComparer comparer = new SpdxItemComparer();
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertTrue(comparer.isDifferenceFound());
		assertTrue(comparer.isAnnotationsEquals());
		assertTrue(comparer.isCommentsEquals());
		assertTrue(comparer.isConcludedLicenseEquals());
		assertTrue(comparer.isCopyrightsEquals());
		assertTrue(comparer.isLicenseCommmentsEquals());
		assertTrue(comparer.isNamesEquals());
		assertTrue(comparer.isRelationshipsEquals());
		assertFalse(comparer.isSeenLicenseEquals());
		AnyLicenseInfo[] result = comparer.getUniqueSeenLicensesB();
		assertEquals(1, result.length);
		assertEquals(LICENSEB2, result[0]);
		
		itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		itemB = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertEquals(0, comparer.getUniqueSeenLicensesB().length);

	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxItemComparer#getUniqueSeenLicensesA()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testGetUniqueSeenLicensesA() throws SpdxCompareException {
		AnyLicenseInfo[] s1 = new AnyLicenseInfo[] {LICENSEA1, LICENSEA2};
		AnyLicenseInfo[] s2 = new AnyLicenseInfo[] {LICENSEB1};
		SpdxItem itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, s1, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItem itemB = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, s2, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItemComparer comparer = new SpdxItemComparer();
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertTrue(comparer.isDifferenceFound());
		assertTrue(comparer.isAnnotationsEquals());
		assertTrue(comparer.isCommentsEquals());
		assertTrue(comparer.isConcludedLicenseEquals());
		assertTrue(comparer.isCopyrightsEquals());
		assertTrue(comparer.isLicenseCommmentsEquals());
		assertTrue(comparer.isNamesEquals());
		assertTrue(comparer.isRelationshipsEquals());
		assertFalse(comparer.isSeenLicenseEquals());
		AnyLicenseInfo[] result = comparer.getUniqueSeenLicensesA();
		assertEquals(1, result.length);
		assertEquals(LICENSEA2, result[0]);
		
		itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		itemB = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertEquals(0, comparer.getUniqueSeenLicensesA().length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxItemComparer#isCommentsEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsCommentsEquals() throws SpdxCompareException {
		SpdxItem itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItem itemB = new SpdxItem(NAMEA, COMMENTB, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItemComparer comparer = new SpdxItemComparer();
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertTrue(comparer.isDifferenceFound());
		assertTrue(comparer.isAnnotationsEquals());
		assertFalse(comparer.isCommentsEquals());
		assertTrue(comparer.isConcludedLicenseEquals());
		assertTrue(comparer.isCopyrightsEquals());
		assertTrue(comparer.isLicenseCommmentsEquals());
		assertTrue(comparer.isNamesEquals());
		assertTrue(comparer.isRelationshipsEquals());
		assertTrue(comparer.isSeenLicenseEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxItemComparer#isCopyrightsEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsCopyrightsEquals() throws SpdxCompareException {
		SpdxItem itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItem itemB = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTB, LICENSE_COMMENTA);
		SpdxItemComparer comparer = new SpdxItemComparer();
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertTrue(comparer.isDifferenceFound());
		assertTrue(comparer.isAnnotationsEquals());
		assertTrue(comparer.isCommentsEquals());
		assertTrue(comparer.isConcludedLicenseEquals());
		assertFalse(comparer.isCopyrightsEquals());
		assertTrue(comparer.isLicenseCommmentsEquals());
		assertTrue(comparer.isNamesEquals());
		assertTrue(comparer.isRelationshipsEquals());
		assertTrue(comparer.isSeenLicenseEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxItemComparer#isLicenseCommmentsEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsLicenseCommmentsEquals() throws SpdxCompareException {
		SpdxItem itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItem itemB = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTB);
		SpdxItemComparer comparer = new SpdxItemComparer();
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertTrue(comparer.isDifferenceFound());
		assertTrue(comparer.isAnnotationsEquals());
		assertTrue(comparer.isCommentsEquals());
		assertTrue(comparer.isConcludedLicenseEquals());
		assertTrue(comparer.isCopyrightsEquals());
		assertFalse(comparer.isLicenseCommmentsEquals());
		assertTrue(comparer.isNamesEquals());
		assertTrue(comparer.isRelationshipsEquals());
		assertTrue(comparer.isSeenLicenseEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxItemComparer#isNamesEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsNamesEquals() throws SpdxCompareException {
		SpdxItem itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItem itemB = new SpdxItem(NAMEB, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItemComparer comparer = new SpdxItemComparer();
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertTrue(comparer.isDifferenceFound());
		assertTrue(comparer.isAnnotationsEquals());
		assertTrue(comparer.isCommentsEquals());
		assertTrue(comparer.isConcludedLicenseEquals());
		assertTrue(comparer.isCopyrightsEquals());
		assertTrue(comparer.isLicenseCommmentsEquals());
		assertFalse(comparer.isNamesEquals());
		assertTrue(comparer.isRelationshipsEquals());
		assertTrue(comparer.isSeenLicenseEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxItemComparer#getItemA()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testGetItemA() throws SpdxCompareException {
		SpdxItem itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItem itemB = new SpdxItem(NAMEB, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItemComparer comparer = new SpdxItemComparer();
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertEquals(itemA, comparer.getItemA());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxItemComparer#getItemB()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testGetItemB() throws SpdxCompareException {
		SpdxItem itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItem itemB = new SpdxItem(NAMEB, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItemComparer comparer = new SpdxItemComparer();
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertEquals(itemB, comparer.getItemB());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxItemComparer#isRelationshipsEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsRelationshipsEquals() throws SpdxCompareException {
		Relationship[] r1 = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		Relationship[] r2 = new Relationship[] {RELATIONSHIP2, RELATIONSHIP3};
		SpdxItem itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, r1,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItem itemB = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, r2,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItemComparer comparer = new SpdxItemComparer();
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertTrue(comparer.isDifferenceFound());
		assertTrue(comparer.isAnnotationsEquals());
		assertTrue(comparer.isCommentsEquals());
		assertTrue(comparer.isConcludedLicenseEquals());
		assertTrue(comparer.isCopyrightsEquals());
		assertTrue(comparer.isLicenseCommmentsEquals());
		assertTrue(comparer.isNamesEquals());
		assertFalse(comparer.isRelationshipsEquals());
		assertTrue(comparer.isSeenLicenseEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxItemComparer#getUniqueRelationshipA()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testGetUniqueRelationshipA() throws SpdxCompareException {
		Relationship[] r1 = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		Relationship[] r2 = new Relationship[] {RELATIONSHIP2, RELATIONSHIP3};
		SpdxItem itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, r1,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItem itemB = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, r2,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItemComparer comparer = new SpdxItemComparer();
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertTrue(comparer.isDifferenceFound());
		assertTrue(comparer.isAnnotationsEquals());
		assertTrue(comparer.isCommentsEquals());
		assertTrue(comparer.isConcludedLicenseEquals());
		assertTrue(comparer.isCopyrightsEquals());
		assertTrue(comparer.isLicenseCommmentsEquals());
		assertTrue(comparer.isNamesEquals());
		assertFalse(comparer.isRelationshipsEquals());
		assertTrue(comparer.isSeenLicenseEquals());
		Relationship[] result = comparer.getUniqueRelationshipA();
		assertEquals(1, result.length);
		assertEquals(RELATIONSHIP1, result[0]);
		itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		itemB = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertEquals(0, comparer.getUniqueRelationshipA().length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxItemComparer#getUniqueRelationshipB()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testGetUniqueRelationshipB() throws SpdxCompareException {
		Relationship[] r1 = new Relationship[] {RELATIONSHIP1, RELATIONSHIP2};
		Relationship[] r2 = new Relationship[] {RELATIONSHIP2, RELATIONSHIP3};
		SpdxItem itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, r1,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItem itemB = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, r2,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItemComparer comparer = new SpdxItemComparer();
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertTrue(comparer.isDifferenceFound());
		assertTrue(comparer.isAnnotationsEquals());
		assertTrue(comparer.isCommentsEquals());
		assertTrue(comparer.isConcludedLicenseEquals());
		assertTrue(comparer.isCopyrightsEquals());
		assertTrue(comparer.isLicenseCommmentsEquals());
		assertTrue(comparer.isNamesEquals());
		assertFalse(comparer.isRelationshipsEquals());
		assertTrue(comparer.isSeenLicenseEquals());
		Relationship[] result = comparer.getUniqueRelationshipB();
		assertEquals(1, result.length);
		assertEquals(RELATIONSHIP3, result[0]);
		itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		itemB = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertEquals(0, comparer.getUniqueRelationshipB().length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxItemComparer#isAnnotationsEquals()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testIsAnnotationsEquals() throws SpdxCompareException {
		Annotation[] a1 = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Annotation[] a2 = new Annotation[] {ANNOTATION2, ANNOTATION3};
		SpdxItem itemA = new SpdxItem(NAMEA, COMMENTA, a1, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItem itemB = new SpdxItem(NAMEA, COMMENTA, a2, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItemComparer comparer = new SpdxItemComparer();
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertTrue(comparer.isDifferenceFound());
		assertFalse(comparer.isAnnotationsEquals());
		assertTrue(comparer.isCommentsEquals());
		assertTrue(comparer.isConcludedLicenseEquals());
		assertTrue(comparer.isCopyrightsEquals());
		assertTrue(comparer.isLicenseCommmentsEquals());
		assertTrue(comparer.isNamesEquals());
		assertTrue(comparer.isRelationshipsEquals());
		assertTrue(comparer.isSeenLicenseEquals());
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxItemComparer#getUniqueAnnotationsA()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testGetUniqueAnnotationsA() throws SpdxCompareException {
		Annotation[] a1 = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Annotation[] a2 = new Annotation[] {ANNOTATION2, ANNOTATION3};
		SpdxItem itemA = new SpdxItem(NAMEA, COMMENTA, a1, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItem itemB = new SpdxItem(NAMEA, COMMENTA, a2, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItemComparer comparer = new SpdxItemComparer();
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertTrue(comparer.isDifferenceFound());
		assertFalse(comparer.isAnnotationsEquals());
		assertTrue(comparer.isCommentsEquals());
		assertTrue(comparer.isConcludedLicenseEquals());
		assertTrue(comparer.isCopyrightsEquals());
		assertTrue(comparer.isLicenseCommmentsEquals());
		assertTrue(comparer.isNamesEquals());
		assertTrue(comparer.isRelationshipsEquals());
		assertTrue(comparer.isSeenLicenseEquals());
		Annotation[] result = comparer.getUniqueAnnotationsA();
		assertEquals(1, result.length);
		assertEquals(ANNOTATION1, result[0]);
		itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		itemB = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertEquals(0, comparer.getUniqueAnnotationsA().length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxItemComparer#getUniqueAnnotationsB()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testGetUniqueAnnotationsB() throws SpdxCompareException {
		Annotation[] a1 = new Annotation[] {ANNOTATION1, ANNOTATION2};
		Annotation[] a2 = new Annotation[] {ANNOTATION2, ANNOTATION3};
		SpdxItem itemA = new SpdxItem(NAMEA, COMMENTA, a1, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItem itemB = new SpdxItem(NAMEA, COMMENTA, a2, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItemComparer comparer = new SpdxItemComparer();
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertTrue(comparer.isDifferenceFound());
		assertFalse(comparer.isAnnotationsEquals());
		assertTrue(comparer.isCommentsEquals());
		assertTrue(comparer.isConcludedLicenseEquals());
		assertTrue(comparer.isCopyrightsEquals());
		assertTrue(comparer.isLicenseCommmentsEquals());
		assertTrue(comparer.isNamesEquals());
		assertTrue(comparer.isRelationshipsEquals());
		assertTrue(comparer.isSeenLicenseEquals());
		Annotation[] result = comparer.getUniqueAnnotationsB();
		assertEquals(1, result.length);
		assertEquals(ANNOTATION3, result[0]);
		itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		itemB = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertEquals(0, comparer.getUniqueAnnotationsB().length);
	}

	/**
	 * Test method for {@link org.spdx.compare.SpdxItemComparer#getFileDifference()}.
	 * @throws SpdxCompareException 
	 */
	@Test
	public void testGetItemDifference() throws SpdxCompareException {
		SpdxItem itemA = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDA, LICENSE_INFO_FROM_FILESA, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItem itemB = new SpdxItem(NAMEA, COMMENTA, ANNOTATIONSA, RELATIONSHIPSA,
				LICENSE_CONCLUDEDB, LICENSE_INFO_FROM_FILESB, COPYRIGHTA, LICENSE_COMMENTA);
		SpdxItemComparer comparer = new SpdxItemComparer();
		comparer.compare(itemA, itemB, LICENSE_XLATION_MAP);
		assertFalse(comparer.isDifferenceFound());
		assertTrue(comparer.isAnnotationsEquals());
		assertTrue(comparer.isCommentsEquals());
		assertTrue(comparer.isConcludedLicenseEquals());
		assertTrue(comparer.isCopyrightsEquals());
		assertTrue(comparer.isLicenseCommmentsEquals());
		assertTrue(comparer.isNamesEquals());
		assertTrue(comparer.isRelationshipsEquals());
		assertTrue(comparer.isSeenLicenseEquals());
		SpdxItemDifference diff = comparer.getItemDifference();
		assertTrue(diff.isAnnotationsEquals());
		assertTrue(diff.isCommentsEquals());
		assertTrue(diff.isConcludedLicenseEquals());
		assertTrue(diff.isCopyrightsEqual());
		assertTrue(diff.isLicenseCommentsEqual());
		assertEquals(NAMEA, diff.getName());
		assertTrue(diff.isRelationshipsEquals());
		assertTrue(diff.isSeenLicensesEquals());
		assertEquals(COMMENTA ,diff.getCommentA());
		assertEquals(COMMENTA ,diff.getCommentB());
		assertEquals(LICENSE_CONCLUDEDA.toString() ,diff.getConcludedLicenseA());
		assertEquals(LICENSE_CONCLUDEDB.toString() ,diff.getConcludedLicenseB());
		assertEquals(COPYRIGHTA ,diff.getCopyrightA());
		assertEquals(COPYRIGHTA ,diff.getCopyrightB());
		assertEquals(LICENSE_COMMENTA ,diff.getLicenseCommentsA());
		assertEquals(LICENSE_COMMENTA ,diff.getLicenseCommentsB());
		assertEquals(0 ,diff.getUniqueAnnotationsA().length);
		assertEquals(0 ,diff.getUniqueAnnotationsB().length);
		assertEquals(0 ,diff.getUniqueRelationshipA().length);
		assertEquals(0 ,diff.getUniqueRelationshipB().length);
		assertEquals(0 ,diff.getUniqueSeenLicensesA().length);
		assertEquals(0 ,diff.getUniqueSeenLicensesB().length);
		
	}

}
