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
package spdxspreadsheet;

import static org.junit.Assert.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Annotation.AnnotationType;
import org.spdx.spdxspreadsheet.AnnotationsSheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;

/**
 * @author Gary
 *
 */
public class TestAnnotationSheet {

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

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.AnnotationsSheet#create(org.apache.poi.ss.usermodel.Workbook, java.lang.String)}.
	 */
	@Test
	public void testCreate() {
		Workbook wb = new HSSFWorkbook();
		AnnotationsSheet.create(wb, "Annotation Info");
		AnnotationsSheet annotations = new AnnotationsSheet(wb, "Annotation Info");
		String ver = annotations.verify();
		if (ver != null && !ver.isEmpty()){
			fail(ver);
		}

	}

	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.AnnotationsSheet#add(org.spdx.rdfparser.model.Annotation, java.lang.String)}.
	 * @throws SpreadsheetException 
	 */
	@Test
	public void testAdd() throws SpreadsheetException {
		Workbook wb = new HSSFWorkbook();
		AnnotationsSheet.create(wb, "Annotation Info");
		AnnotationsSheet annotations = new AnnotationsSheet(wb, "Annotation Info");
		Annotation an1 = new Annotation("Person: Annotator1", AnnotationType.annotationType_other,
				"2010-01-29T18:30:22Z", "Comment1");
		Annotation an2 = new Annotation("Person: Annotator2", AnnotationType.annotationType_review,
				"2015-01-29T18:30:22Z", "Comment2");
		String id1 = "SPDXRef-1";
		String id2 = "SPDXRef-2";
		annotations.add(an1, id1);
		annotations.add(an2, id2);
		Annotation result = annotations.getAnnotation(1);
		assertTrue(an1.equivalent(result));
		assertEquals(id1, annotations.getElmementId(1));
		result = annotations.getAnnotation(2);
		assertTrue(an2.equivalent(result));
		assertEquals(id2, annotations.getElmementId(2));
		String ver = annotations.verify();
		if (ver != null && !ver.isEmpty()){
			fail(ver);
		}
	}

}
