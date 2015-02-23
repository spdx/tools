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

import java.io.File;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.Relationship.RelationshipType;
import org.spdx.rdfparser.model.SpdxElement;
import org.spdx.spdxspreadsheet.RelationshipsSheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;

/**
 * @author Gary O'Neall
 *
 */
public class TestRelationshipSheet {
	File spreadsheetFile;
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

	@Before
	public void setUp() throws Exception {
		spreadsheetFile = File.createTempFile("TEST_PKG_INFO", "xls");
	}

	@After
	public void tearDown() throws Exception {
		spreadsheetFile.delete();
	}
	
	@Test
	public void testCreate() throws IOException, InvalidFormatException {
		
		Workbook wb = new HSSFWorkbook();
		RelationshipsSheet.create(wb, "Relationship Info");
		RelationshipsSheet relationships = new RelationshipsSheet(wb, "Relationship Info");
		String ver = relationships.verify();
		if (ver != null && !ver.isEmpty()){
			fail(ver);
		}
	}

	/**
	 * Test method for {@link org.spdx.spdxspreadsheet.RelationshipsSheet#add(org.spdx.rdfparser.model.Relationship, java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException 
	 * @throws SpreadsheetException 
	 */
	@Test
	public void testAddandGet() throws InvalidSPDXAnalysisException, SpreadsheetException {
		String testUri = "https://olex.openlogic.com/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		SpdxDocumentContainer doc = new SpdxDocumentContainer(testUri,"SPDX-2.0");
		SpdxElement element1 = new SpdxElement("Element1", "", new Annotation[0], 
				new Relationship[0]);
		element1.setId("SPDXRef-1");
		doc.addElement(element1);
		Relationship rel1 = new Relationship(element1, RelationshipType.relationshipType_amendment, "Comment1");
		SpdxElement element2 = new SpdxElement("Element2", "", new Annotation[0], 
				new Relationship[0]);
		element2.setId("SPDXRef-2");
		doc.addElement(element2);
		Relationship rel2 = new Relationship(element2, RelationshipType.relationshipType_contains, null);
		SpdxElement element3 = new SpdxElement("Element3", "", new Annotation[0], 
				new Relationship[0]);
		element3.setId("SPDXRef-3");
		doc.addElement(element3);
		Relationship rel3 = new Relationship(element3, RelationshipType.relationshipType_copyOf, "Comment2");
		Workbook wb = new HSSFWorkbook();
		RelationshipsSheet.create(wb, "Relationship Info");
		RelationshipsSheet relationships = new RelationshipsSheet(wb, "Relationship Info");
		String id1 = "SPDXRef-first";
		String id2 = "SPDXRef-second";
		String id3 = "SPDXRef-third";
		relationships.add(rel1, id1);
		relationships.add(rel2, id2);
		relationships.add(rel3, id3);
		Relationship result = relationships.getRelationship(1, doc);
		assertTrue(result.equivalent(rel1));
		assertEquals(id1, relationships.getElmementId(1));
		result = relationships.getRelationship(2, doc);
		assertTrue(result.equivalent(rel2));
		assertEquals(id2, relationships.getElmementId(2));
		result = relationships.getRelationship(3, doc);
		assertTrue(result.equivalent(rel3));
		assertEquals(id3, relationships.getElmementId(3));
		String ver = relationships.verify();
		if (ver != null && !ver.isEmpty()){
			fail(ver);
		}
	}
}
