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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ConjunctiveLicenseSet;
import org.spdx.rdfparser.license.DisjunctiveLicenseSet;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;

import sun.nio.cs.StandardCharsets;

import com.google.common.io.Files;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

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
			NON_STD_LICENSES_RESOURCES[i] = NON_STD_LICENSES[i].createResource(model);
		}
		STANDARD_LICENSES_RESOURCES = new Resource[STANDARD_LICENSES.length];
		for (int i = 0; i < STANDARD_LICENSES.length; i++) {
			STANDARD_LICENSES_RESOURCES[i] = STANDARD_LICENSES[i].createResource(model);
		}
		CONJUNCTIVE_LICENSES_RESOURCES = new Resource[CONJUNCTIVE_LICENSES.length];
		for (int i = 0; i < CONJUNCTIVE_LICENSES.length; i++) {
			CONJUNCTIVE_LICENSES_RESOURCES[i] = CONJUNCTIVE_LICENSES[i].createResource(model);
		}
		DISJUNCTIVE_LICENSES_RESOURCES = new Resource[DISJUNCTIVE_LICENSES.length];
		for (int i = 0; i < DISJUNCTIVE_LICENSES.length; i++) {
			DISJUNCTIVE_LICENSES_RESOURCES[i] = DISJUNCTIVE_LICENSES[i].createResource(model);
		}
		COMPLEX_LICENSE_RESOURCE = COMPLEX_LICENSE.createResource(model);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testReadingStandardLicense() throws IOException {
		try {
			Class.forName("net.rootdev.javardfa.jena.RDFaReader");
		} catch(java.lang.ClassNotFoundException e) {
			// do nothing
		}  
		String id = "AFL-1.2";
		String stdLicUri = LicenseInfoFactory.LISTED_LICENSE_URI_PREFIX + id;
		Model model = ModelFactory.createDefaultModel();
		InputStream in = null;
		try {
			in = FileManager.get().open(stdLicUri);
			if ( in != null ) {
				model.read(in, stdLicUri, "HTML");
			}
//			String testOutFilename = "C:\\Users\\Source Auditor\\Documents\\SPDX\\testout.test";
//			File outfile = new File(testOutFilename);
//			outfile.createNewFile();
//			OutputStream os = new FileOutputStream(outfile);
//			OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(os));
//			model.write(writer);
//			writer.close();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					
				}
			}
		}


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
	public void testParseLocalUri() throws IOException, ClassNotFoundException {
		Class.forName("net.rootdev.javardfa.jena.RDFaReader");
		String id = "BSD-3-Clause";
		File licenseHtmlFile = new File("TestFiles" + File.separator + id);
		String stdLicUri = "file://" + licenseHtmlFile.getAbsolutePath().replace('\\', '/').replace(" ", "%20");
		String prefix = "http://spdx.org/licenses/BSD-3-Clause";
		InputStream in = FileManager.get().open(stdLicUri);
		try {
			Model retval = ModelFactory.createDefaultModel();
			retval.read(in, prefix, "HTML");
			StringWriter writer = new StringWriter();
			retval.write(writer);
			Property p = retval.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID);
			assertTrue(retval.contains(null, p));
			
		} finally {
			in.close();
		}
	}
	@Test
	public void testGetLicenseFromStdLicModel() throws InvalidSPDXAnalysisException, IOException {
		String id = "BSD-3-Clause";
		File licenseHtmlFile = new File("TestFiles" + File.separator + id);
		
		String stdLicUri = "file://" + licenseHtmlFile.getAbsolutePath().replace('\\', '/').replace(" ", "%20");
		SpdxListedLicense lic = LicenseInfoFactory.getLicenseFromStdLicModel(stdLicUri);
		if (lic == null) {
			fail("license is null");
		}
		String header = "Test BSD Standard License Header";
		String note = "BSD 3 clause notes";
		String url = "http://www.opensource.org/licenses/BSD-3-Clause";
		assertEquals(id, lic.getLicenseId());
		assertEquals(header, lic.getStandardLicenseHeader());
		String template = readTextFile("TestFiles"+File.separator+"BSD-3-Clause-Template.txt");
		String licenseTemplate = lic.getStandardLicenseTemplate();
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
		List<String> lines = Files.readLines(file, new StandardCharsets().charsetForName("UTF-8"));
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
	 * Test method for {@link org.spdx.rdfparser.license.LicenseInfoFactory#getLicenseInfoFromModel(com.hp.hpl.jena.rdf.model.Model, com.hp.hpl.jena.graph.Node)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetLicenseInfoFromModel() throws InvalidSPDXAnalysisException {
		// standard license
		AnyLicenseInfo li = LicenseInfoFactory.getLicenseInfoFromModel(model, STANDARD_LICENSES_RESOURCES[0].asNode());
		if (!(li instanceof SpdxListedLicense)) {
			fail ("Wrong type for standard license");
		}
		ArrayList<String> verify = li.verify();
		assertEquals(0, verify.size());
		SpdxListedLicense sli = (SpdxListedLicense)li;
		assertEquals(STD_IDS[0], sli.getLicenseId());
		String licenseText = sli.getLicenseText().trim();
		if (!licenseText.startsWith(STD_TEXTS[0])) {
			fail("Incorrect license text");
		}
		// non-standard license
		AnyLicenseInfo li2 = LicenseInfoFactory.getLicenseInfoFromModel(model, NON_STD_LICENSES_RESOURCES[0].asNode());
		if (!(li2 instanceof ExtractedLicenseInfo)) {
			fail ("Wrong type for non-standard license");
		}
		ExtractedLicenseInfo nsli2 = (ExtractedLicenseInfo)li2;
		assertEquals(NONSTD_IDS[0], nsli2.getLicenseId());
		assertEquals(NONSTD_TEXTS[0], nsli2.getExtractedText());
		verify = li2.verify();
		assertEquals(0, verify.size());
		// conjunctive license
		AnyLicenseInfo cli = LicenseInfoFactory.getLicenseInfoFromModel(model, CONJUNCTIVE_LICENSES_RESOURCES[0].asNode());
		if (!(cli instanceof ConjunctiveLicenseSet)) {
			fail ("Wrong type for conjuctive licenses license");
		}
		assertEquals(CONJUNCTIVE_LICENSES[0], cli);
		verify = cli.verify();
		assertEquals(0, verify.size());
		// disjunctive license
		AnyLicenseInfo dli = LicenseInfoFactory.getLicenseInfoFromModel(model, DISJUNCTIVE_LICENSES_RESOURCES[0].asNode());
		if (!(dli instanceof DisjunctiveLicenseSet)) {
			fail ("Wrong type for disjuncdtive licenses license");
		}
		assertEquals(DISJUNCTIVE_LICENSES[0], dli);
		verify = dli.verify();
		assertEquals(0, verify.size());
		// complex license
		AnyLicenseInfo complex = LicenseInfoFactory.getLicenseInfoFromModel(model, COMPLEX_LICENSE_RESOURCE.asNode());
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
	public void testSpecialLicenses() throws InvalidLicenseStringException, InvalidSPDXAnalysisException {
		// NONE
		AnyLicenseInfo none = LicenseInfoFactory.parseSPDXLicenseString(LicenseInfoFactory.NONE_LICENSE_NAME);
		Resource r = none.createResource(model);
		AnyLicenseInfo comp = LicenseInfoFactory.getLicenseInfoFromModel(model, r.asNode());
		assertEquals(none, comp);
		ArrayList<String> verify = comp.verify();
		assertEquals(0, verify.size());
		// NOASSERTION_NAME
		AnyLicenseInfo noAssertion = LicenseInfoFactory.parseSPDXLicenseString(LicenseInfoFactory.NOASSERTION_LICENSE_NAME);
		r = noAssertion.createResource(model);
		comp = LicenseInfoFactory.getLicenseInfoFromModel(model, r.asNode());
		assertEquals(noAssertion, comp);
		verify = comp.verify();
		assertEquals(0, verify.size());
	}

}
