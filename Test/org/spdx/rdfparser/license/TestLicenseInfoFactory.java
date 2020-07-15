/**
 * Copyright (c) 2011 Source Auditor Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.model.IRdfModel;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;

/**
 * @author Source Auditor
 *
 */
public class TestLicenseInfoFactory {
	static final String[] NONSTD_IDS = new String[] {SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"1",
		SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"2", SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"3",
		SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"4"};
	static final String[] NONSTD_TEXTS = new String[] {"text1", "text2", "text3", "text4"};
	static final String[] STD_IDS = new String[] {"AFL-3.0", "CECILL-B", "EUPL-1.0"};
	static final String[] STD_TEXTS = new String[] {"Academic Free License (", "CONTRAT DE LICENCE DE LOGICIEL LIBRE CeCILL-B",
		"European Union Public Licence"};

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

	Model model;

	IModelContainer modelContainer = new IModelContainer() {

		@Override
		public Model getModel() {
			return model;
		}

		@Override
		public String getDocumentNamespace() {
			return "http://testNameSPace#";
		}

		@Override
		public String getNextSpdxElementRef() {
			return null;
		}

		@Override
		public boolean spdxElementRefExists(String elementRef) {
			return false;
		}

		@Override
		public void addSpdxElementRef(String elementRef) {

		}

		@Override
		public String documentNamespaceToId(String externalNamespace) {
			return null;
		}

		@Override
		public String externalDocumentIdToNamespace(String docId) {
			return null;
		}

		@Override
		public Resource createResource(Resource duplicate, String uri,
				Resource type, IRdfModel modelObject) {
			if (duplicate != null) {
				return duplicate;
			} else if (uri == null) {
				return model.createResource(type);
			} else {
				return model.createResource(uri, type);
			}
		}

		@Override
		public boolean addCheckNodeObject(Node node, IRdfModel rdfModelObject) {
			// TODO Auto-generated method stub
			return false;
		}

	};

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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLocalUri() throws IOException {
		String id = "BSD-3-Clause";
		File licenseHtmlFile = new File("TestFiles" + File.separator + id);
		String stdLicUri = "file://" + licenseHtmlFile.getAbsolutePath().replace('\\', '/').replace(" ", "%20");
		byte[] buf = new byte[2048];

		InputStream in = FileManager.get().open(stdLicUri);
		try {
			int readLen = in.read(buf, 0, 2048);
			assertTrue(readLen > 0);
		} finally {
			in.close();
		}
	}

	@Test
	public void testGetLicenseFromStdLicModel() throws InvalidSPDXAnalysisException, IOException {
		String id = "BSD-3-Clause";
		File licenseHtmlFile = new File("TestFiles" + File.separator + id + ".jsonld");

		String stdLicUri = "file://" + licenseHtmlFile.getAbsolutePath().replace('\\', '/').replace(" ", "%20");
		SpdxListedLicense lic = ListedLicenses.getListedLicenses().getLicenseFromUri(stdLicUri);
		if (lic == null) {
			fail("license is null");
		}
		String header = "Test BSD Standard License Header";
		String note = "BSD 3 clause notes";
		String url = "http://www.opensource.org/licenses/BSD-3-Clause";
		assertEquals(id, lic.getLicenseId());
		assertEquals(header, lic.getStandardLicenseHeader());
		String template = readTextFile("TestFiles"+File.separator+"BSD-3-Clause.template.txt");
		String licenseTemplate = lic.getStandardLicenseTemplate();
		String s1 = licenseTemplate.replaceAll(" ", "").replaceAll("\n", "").trim();
		String s2 = template.replaceAll(" ", "").replaceAll("\n", "").trim();
		assertEquals(s1, s2);
		int result = compareStringsIgnoreSpaces(template, licenseTemplate);
		assertEquals(0, result);
		assertEquals(note, lic.getComment());
		assertEquals(1, lic.getSeeAlso().length);
		assertEquals(url, lic.getSeeAlso()[0]);
		assertTrue(lic.isOsiApproved());
	}

	/**
	 * Compares 2 strings and (same as CompareTo()) but ignores any leading or trailing blanks
	 * @param s1
	 * @param s2
	 * @return
	 */
	private int compareStringsIgnoreSpaces(String s1,
			String s2) {
		String[] s1lines = s1.split("\n");
		String[] s2lines = s2.split("\n");
		int i = 0;
		int j = 0;
		while (i < s1lines.length && j < s2lines.length) {
			if (s1lines[i].trim().isEmpty()) {
				i++;
			}
			else if (s2lines[j].trim().isEmpty()) {
				j++;
			} else {
				int result = s1lines[i++].trim().compareTo(s2lines[j++].trim());
				if (result != 0) {
					return result;
				}
			}
		}
		if (i < s1lines.length) {
			for (int ii = i; ii < s1lines.length; ii++) {
				if (!s1lines[ii].trim().isEmpty()) {
					return 1;
				}
			}
		}
		if (j < s2lines.length) {
			for (int jj = j; jj < s2lines.length; jj++) {
				if (!s2lines[jj].trim().isEmpty()) {
					return -1;
				}
			}
		}
		return 0;
	}

	/**
	 * Reads in a text file - assumes UTF-8 encoding
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	private String readTextFile(String filePath) throws IOException {
		File file = new File(filePath);
        List<String> lines = Files.readLines(file, Charsets.UTF_8);
		Iterator<String> iter = lines.iterator();
		StringBuilder sb = new StringBuilder();
		if (iter.hasNext()) {
			sb.append(iter.next());
		}
		while (iter.hasNext()) {
			sb.append("\n");
			sb.append(iter.next());
		}
		return sb.toString();
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.LicenseInfoFactory#getLicenseInfoFromModel(org.apache.jena.rdf.model.Model, org.apache.jena.graph.Node)}.
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testGetLicenseInfoFromModel() throws InvalidSPDXAnalysisException {
		// standard license
		AnyLicenseInfo li = LicenseInfoFactory.getLicenseInfoFromModel(modelContainer, STANDARD_LICENSES_RESOURCES[0].asNode());
		if (!(li instanceof SpdxListedLicense)) {
			fail ("Wrong type for standard license");
		}
		List<String> verify = li.verify();
		assertEquals(0, verify.size());
		SpdxListedLicense sli = (SpdxListedLicense)li;
		assertEquals(STD_IDS[0], sli.getLicenseId());
		String licenseText = sli.getLicenseText().trim();
		if (!licenseText.startsWith(STD_TEXTS[0])) {
			fail("Incorrect license text");
		}
		// non-standard license
		AnyLicenseInfo li2 = LicenseInfoFactory.getLicenseInfoFromModel(modelContainer, NON_STD_LICENSES_RESOURCES[0].asNode());
		if (!(li2 instanceof ExtractedLicenseInfo)) {
			fail ("Wrong type for non-standard license");
		}
		ExtractedLicenseInfo nsli2 = (ExtractedLicenseInfo)li2;
		assertEquals(NONSTD_IDS[0], nsli2.getLicenseId());
		assertEquals(NONSTD_TEXTS[0], nsli2.getExtractedText());
		verify = li2.verify();
		assertEquals(0, verify.size());
		// conjunctive license
		AnyLicenseInfo cli = LicenseInfoFactory.getLicenseInfoFromModel(modelContainer, CONJUNCTIVE_LICENSES_RESOURCES[0].asNode());
		if (!(cli instanceof ConjunctiveLicenseSet)) {
			fail ("Wrong type for conjuctive licenses license");
		}
		assertEquals(CONJUNCTIVE_LICENSES[0], cli);
		verify = cli.verify();
		assertEquals(0, verify.size());
		// disjunctive license
		AnyLicenseInfo dli = LicenseInfoFactory.getLicenseInfoFromModel(modelContainer, DISJUNCTIVE_LICENSES_RESOURCES[0].asNode());
		if (!(dli instanceof DisjunctiveLicenseSet)) {
			fail ("Wrong type for disjuncdtive licenses license");
		}
		assertEquals(DISJUNCTIVE_LICENSES[0], dli);
		verify = dli.verify();
		assertEquals(0, verify.size());
		// complex license
		AnyLicenseInfo complex = LicenseInfoFactory.getLicenseInfoFromModel(modelContainer, COMPLEX_LICENSE_RESOURCE.asNode());
		assertEquals(COMPLEX_LICENSE, complex);
		verify = complex.verify();
		assertEquals(0, verify.size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.license.LicenseInfoFactory#parseSPDXLicenseString(java.lang.String)}.
	 * @throws InvalidLicenseStringException
	 */
	@Test
	public void testParseSPDXLicenseString() throws InvalidLicenseStringException {
		String parseString = COMPLEX_LICENSE.toString();
		AnyLicenseInfo li = LicenseInfoFactory.parseSPDXLicenseString(parseString);
		if (!li.equals(COMPLEX_LICENSE)) {
			fail("Parsed license does not equal");
		}
	}

	@Test
	public void testParseSPDXLicenseStringMixedCase() throws InvalidLicenseStringException {
		String parseString = COMPLEX_LICENSE.toString();
		StringBuilder mixedCase = new StringBuilder();
		for (int i = 0; i < parseString.length(); i++) {
			if (i % 2 == 0) {
				mixedCase.append(parseString.substring(i, i+1).toUpperCase());
			} else {
				mixedCase.append(parseString.substring(i, i+1).toLowerCase());
			}
		}
		AnyLicenseInfo li = LicenseInfoFactory.parseSPDXLicenseString(mixedCase.toString());
		if (!li.equals(COMPLEX_LICENSE)) {
			fail("Parsed license does not equal");
		}
	}

	@Test
	public void testSpecialLicenses() throws InvalidLicenseStringException, InvalidSPDXAnalysisException {
		// NONE
		AnyLicenseInfo none = LicenseInfoFactory.parseSPDXLicenseString(LicenseInfoFactory.NONE_LICENSE_NAME);
		Resource r = none.createResource(modelContainer);
		AnyLicenseInfo comp = LicenseInfoFactory.getLicenseInfoFromModel(modelContainer, r.asNode());
		assertEquals(none, comp);
		List<String> verify = comp.verify();
		assertEquals(0, verify.size());
		// NOASSERTION_NAME
		AnyLicenseInfo noAssertion = LicenseInfoFactory.parseSPDXLicenseString(LicenseInfoFactory.NOASSERTION_LICENSE_NAME);
		r = noAssertion.createResource(modelContainer);
		comp = LicenseInfoFactory.getLicenseInfoFromModel(modelContainer, r.asNode());
		assertEquals(noAssertion, comp);
		verify = comp.verify();
		assertEquals(0, verify.size());
	}

	@Test
	public void testDifferentLicenseOrder() throws InvalidLicenseStringException {
		AnyLicenseInfo order1 = LicenseInfoFactory.parseSPDXLicenseString("(LicenseRef-14 AND LicenseRef-5 AND LicenseRef-6 AND LicenseRef-15 AND LicenseRef-3 AND LicenseRef-12 AND LicenseRef-4 AND LicenseRef-13 AND LicenseRef-10 AND LicenseRef-9 AND LicenseRef-11 AND LicenseRef-7 AND LicenseRef-8 AND LGPL-2.1+ AND LicenseRef-1 AND LicenseRef-2 AND LicenseRef-0 AND GPL-2.0+ AND GPL-2.0 AND LicenseRef-17 AND LicenseRef-16 AND BSD-2-Clause-Clear)");
		AnyLicenseInfo order2 = LicenseInfoFactory.parseSPDXLicenseString("(LicenseRef-14 AND LicenseRef-5 AND LicenseRef-6 AND LicenseRef-15 AND LicenseRef-12 AND LicenseRef-3 AND LicenseRef-13 AND LicenseRef-4 AND LicenseRef-10 AND LicenseRef-9 AND LicenseRef-11 AND LicenseRef-7 AND LicenseRef-8 AND LGPL-2.1+ AND LicenseRef-1 AND LicenseRef-2 AND LicenseRef-0 AND GPL-2.0+ AND GPL-2.0 AND LicenseRef-17 AND BSD-2-Clause-Clear AND LicenseRef-16)");
		assertTrue(order1.equals(order2));
		assertTrue(order1.equivalent(order2));
	}

}
