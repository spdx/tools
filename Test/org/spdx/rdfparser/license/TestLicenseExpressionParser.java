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
package org.spdx.rdfparser.license;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

/**
 * @author Gary
 *
 */
public class TestLicenseExpressionParser {

	static final String[] STD_IDS = new String[] {"AFL-3.0", "CECILL-B", "EUPL-1.0", "Afmparse"};
	static final String[] NONSTD_IDS = new String[] {SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"1",
		SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"2", SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"3",
		SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"4"};
	static final String[] STD_TEXTS = new String[] {"Academic Free License (", "CONTRAT DE LICENCE DE LOGICIEL LIBRE CeCILL-B",
	"European Union Public Licence", "Afmparse License"};
	static final String[] NONSTD_TEXTS = new String[] {"text1", "text2", "text3", "text4"};
	static final String[] EXCEPTION_IDS = new String[] {"exception-1","exception-2", "exception-3", "exception-4"};
	static final String[] EXCEPTION_NAMES = new String[] {"exName-1", "exName-2", "exName-3", "exName-4"};
	static final String[] EXCEPTION_TEXTS = new String[] {"Ex text 1", "Ex text 2", "Ex text 3", "Ex text 4"};
	ExtractedLicenseInfo[] NON_STD_LICENSES;
	SpdxListedLicense[] STANDARD_LICENSES;
	LicenseException[] LICENSE_EXCEPTIONS;
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
		LICENSE_EXCEPTIONS = new LicenseException[EXCEPTION_IDS.length];
		for (int i = 0; i < EXCEPTION_IDS.length; i++) {
			LICENSE_EXCEPTIONS[i] = new LicenseException(EXCEPTION_IDS[i], EXCEPTION_NAMES[i], EXCEPTION_TEXTS[i]);
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.LicenseExpressionParser#parseLicenseExpression(java.lang.String)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testSingleStdLicense() throws InvalidSPDXAnalysisException {
		String parseString = STD_IDS[0];
		AnyLicenseInfo expected = STANDARD_LICENSES[0];
		AnyLicenseInfo result = LicenseExpressionParser.parseLicenseExpression(parseString, null);
		assertTrue(expected.equals(result));
	}

	@Test
	public void testSingleExtractedLicense() throws InvalidSPDXAnalysisException {
		String parseString = NONSTD_IDS[0];
		AnyLicenseInfo expected = NON_STD_LICENSES[0];
		AnyLicenseInfo result = LicenseExpressionParser.parseLicenseExpression(parseString, null);
		assertTrue(expected.equals(result));
	}

	@Test
	public void testOrLater() throws InvalidSPDXAnalysisException {
		String parseString = STD_IDS[0]+"+";
		AnyLicenseInfo expected = new OrLaterOperator(STANDARD_LICENSES[0]);
		AnyLicenseInfo result = LicenseExpressionParser.parseLicenseExpression(parseString, null);
		assertTrue(expected.equals(result));
	}

	@Test
	public void testWithException() throws InvalidSPDXAnalysisException {
		String parseString = STD_IDS[0]+" WITH " + EXCEPTION_IDS[0];
		AnyLicenseInfo expected = new WithExceptionOperator(STANDARD_LICENSES[0], LICENSE_EXCEPTIONS[0]);
		AnyLicenseInfo result = LicenseExpressionParser.parseLicenseExpression(parseString, null);
		assertTrue(expected.equals(result));
	}

	@Test
	public void testSimpleAnd() throws InvalidSPDXAnalysisException {
		String parseString = STD_IDS[0] + " AND " + NONSTD_IDS[0];
		AnyLicenseInfo expected = new ConjunctiveLicenseSet(new AnyLicenseInfo[] {STANDARD_LICENSES[0], NON_STD_LICENSES[0]});
		AnyLicenseInfo result = LicenseExpressionParser.parseLicenseExpression(parseString, null);
		assertTrue(expected.equals(result));
	}

	@Test
	public void testSimpleOr() throws InvalidSPDXAnalysisException {
		String parseString = STD_IDS[0] + " OR " + NONSTD_IDS[0];
		AnyLicenseInfo expected = new DisjunctiveLicenseSet(new AnyLicenseInfo[] {STANDARD_LICENSES[0], NON_STD_LICENSES[0]});
		AnyLicenseInfo result = LicenseExpressionParser.parseLicenseExpression(parseString, null);
		assertTrue(expected.equals(result));
	}

	@Test
	public void testLargerAnd() throws InvalidSPDXAnalysisException {
		String parseString = STD_IDS[1] + " AND " + NONSTD_IDS[1] + " AND " +
					STD_IDS[2] + " AND " + STD_IDS[3];
		AnyLicenseInfo expected = new ConjunctiveLicenseSet(new AnyLicenseInfo[] {STANDARD_LICENSES[1],
				NON_STD_LICENSES[1], STANDARD_LICENSES[2], STANDARD_LICENSES[3]});
		AnyLicenseInfo result = LicenseExpressionParser.parseLicenseExpression(parseString, null);
		assertTrue(expected.equals(result));
	}

	@Test
	public void testLargerOr() throws InvalidSPDXAnalysisException {
		String parseString = STD_IDS[1] + " OR " + NONSTD_IDS[1] + " OR " +
					STD_IDS[2] + " OR " + STD_IDS[3];
		AnyLicenseInfo expected = new DisjunctiveLicenseSet(new AnyLicenseInfo[] {STANDARD_LICENSES[1],
				NON_STD_LICENSES[1], STANDARD_LICENSES[2], STANDARD_LICENSES[3]});
		AnyLicenseInfo result = LicenseExpressionParser.parseLicenseExpression(parseString, null);
		assertTrue(expected.equals(result));
	}

	@Test
	public void testOuterParens() throws InvalidSPDXAnalysisException {
		String parseString = "(" + STD_IDS[1] + " OR " + NONSTD_IDS[1] + " OR " +
					STD_IDS[2] + " OR " + STD_IDS[3] + ")";
		AnyLicenseInfo expected = new DisjunctiveLicenseSet(new AnyLicenseInfo[] {STANDARD_LICENSES[1],
				NON_STD_LICENSES[1], STANDARD_LICENSES[2], STANDARD_LICENSES[3]});
		AnyLicenseInfo result = LicenseExpressionParser.parseLicenseExpression(parseString, null);
		assertTrue(expected.equals(result));
	}

	@Test
	public void testInnerParens() throws InvalidSPDXAnalysisException {
		String parseString = STD_IDS[1] + " AND " + NONSTD_IDS[1] + " AND " +
				"(" + STD_IDS[2] + " OR " + STD_IDS[3] + ")";
		DisjunctiveLicenseSet dls = new DisjunctiveLicenseSet(new AnyLicenseInfo[] {STANDARD_LICENSES[2], STANDARD_LICENSES[3]});
		AnyLicenseInfo expected = new ConjunctiveLicenseSet(new AnyLicenseInfo[] {STANDARD_LICENSES[1] ,
				NON_STD_LICENSES[1], dls});
		AnyLicenseInfo result = LicenseExpressionParser.parseLicenseExpression(parseString, null);
		assertTrue(expected.equals(result));
	}

	@Test
	public void testAndOrPrecedence() throws InvalidSPDXAnalysisException {
		String parseString = STD_IDS[1] + " OR " + NONSTD_IDS[1] + " AND " +
				STD_IDS[2] + " OR " + STD_IDS[3];
		ConjunctiveLicenseSet cls = new ConjunctiveLicenseSet(new AnyLicenseInfo[] {NON_STD_LICENSES[1], STANDARD_LICENSES[2]});
		AnyLicenseInfo expected = new DisjunctiveLicenseSet(new AnyLicenseInfo[] {STANDARD_LICENSES[1] ,
				cls, STANDARD_LICENSES[3]});
		AnyLicenseInfo result = LicenseExpressionParser.parseLicenseExpression(parseString, null);
		assertTrue(expected.equals(result));
	}

	@Test
	public void testContainer() throws InvalidSPDXAnalysisException {
		String docUri = "http://www.spdx.org/spdxdocs/uniquenameofsomesort";
		String version = "SPDX-1.2";
		SpdxDocumentContainer container = new SpdxDocumentContainer(docUri, version);
		assertTrue(container.getExtractedLicense(NONSTD_IDS[0]) == null);
		LicenseExpressionParser.parseLicenseExpression(
				NONSTD_IDS[0], container);
		assertTrue(container.extractedLicenseExists(NONSTD_IDS[0]));
		LicenseExpressionParser.parseLicenseExpression(
				NONSTD_IDS[1] + " AND " + NONSTD_IDS[2], container);
		assertTrue(container.extractedLicenseExists(NONSTD_IDS[1]));
		assertTrue(container.extractedLicenseExists(NONSTD_IDS[2]));
	}
	
	@Test
	public void regressionMitWith() throws InvalidSPDXAnalysisException, InvalidLicenseStringException {
	    AnyLicenseInfo result = LicenseInfoFactory.parseSPDXLicenseString("MIT WITH Autoconf-exception-2.0");
	    assertEquals("MIT WITH Autoconf-exception-2.0",result.toString());
	}
}
