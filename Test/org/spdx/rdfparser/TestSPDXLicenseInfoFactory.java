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
package org.spdx.rdfparser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

/**
 * @author Source Auditor
 *
 */
public class TestSPDXLicenseInfoFactory {
	static final String[] NONSTD_IDS = new String[] {SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"1",
		SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"2", SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"3",
		SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM+"4"};
	static final String[] NONSTD_TEXTS = new String[] {"text1", "text2", "text3", "text4"};
	static final String[] STD_IDS = new String[] {"AFL-3.0", "CECILL-B", "EUPL-1.0"};
	static final String[] STD_TEXTS = new String[] {"Academic Free License (", "CONTRAT DE LICENCE DE LOGICIEL LIBRE CeCILL-B",
		"European Union Public Licence"};

	SPDXNonStandardLicense[] NON_STD_LICENSES;
	SPDXStandardLicense[] STANDARD_LICENSES;
	SPDXDisjunctiveLicenseSet[] DISJUNCTIVE_LICENSES;
	SPDXConjunctiveLicenseSet[] CONJUNCTIVE_LICENSES;
	
	SPDXConjunctiveLicenseSet COMPLEX_LICENSE;
	
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
		NON_STD_LICENSES = new SPDXNonStandardLicense[NONSTD_IDS.length];
		for (int i = 0; i < NONSTD_IDS.length; i++) {
			NON_STD_LICENSES[i] = new SPDXNonStandardLicense(NONSTD_IDS[i], NONSTD_TEXTS[i]);
		}
		
		STANDARD_LICENSES = new SPDXStandardLicense[STD_IDS.length];
		for (int i = 0; i < STD_IDS.length; i++) {
			STANDARD_LICENSES[i] = new SPDXStandardLicense("Name "+String.valueOf(i), 
					STD_IDS[i], STD_TEXTS[i], new String[] {"URL "+String.valueOf(i)}, "Notes "+String.valueOf(i), 
					"LicHeader "+String.valueOf(i), "Template "+String.valueOf(i), true);
		}
		
		DISJUNCTIVE_LICENSES = new SPDXDisjunctiveLicenseSet[3];
		CONJUNCTIVE_LICENSES = new SPDXConjunctiveLicenseSet[2];
		
		DISJUNCTIVE_LICENSES[0] = new SPDXDisjunctiveLicenseSet(new SPDXLicenseInfo[] {
				NON_STD_LICENSES[0], NON_STD_LICENSES[1], STANDARD_LICENSES[1]
		});
		CONJUNCTIVE_LICENSES[0] = new SPDXConjunctiveLicenseSet(new SPDXLicenseInfo[] {
				STANDARD_LICENSES[0], NON_STD_LICENSES[0], STANDARD_LICENSES[1]
		});
		CONJUNCTIVE_LICENSES[1] = new SPDXConjunctiveLicenseSet(new SPDXLicenseInfo[] {
				DISJUNCTIVE_LICENSES[0], NON_STD_LICENSES[2]
		});
		DISJUNCTIVE_LICENSES[1] = new SPDXDisjunctiveLicenseSet(new SPDXLicenseInfo[] {
				CONJUNCTIVE_LICENSES[1], NON_STD_LICENSES[0], STANDARD_LICENSES[0]
		});
		DISJUNCTIVE_LICENSES[2] = new SPDXDisjunctiveLicenseSet(new SPDXLicenseInfo[] {
				DISJUNCTIVE_LICENSES[1], CONJUNCTIVE_LICENSES[0], STANDARD_LICENSES[2]
		});
		COMPLEX_LICENSE = new SPDXConjunctiveLicenseSet(new SPDXLicenseInfo[] {
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
		String stdLicUri = SPDXLicenseInfoFactory.STANDARD_LICENSE_URI_PREFIX + id;
		Model model = ModelFactory.createDefaultModel();
		InputStream in = null;
		try {
			in = FileManager.get().open(stdLicUri);
			model.read(in, stdLicUri, "HTML");
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
	public void testGetLicenseFromStdLicModel() throws InvalidSPDXAnalysisException {
		String id = "AFL-1.2";
		String stdLicUri = SPDXLicenseInfoFactory.STANDARD_LICENSE_URI_PREFIX + id;
		SPDXStandardLicense lic = SPDXLicenseInfoFactory.getLicenseFromStdLicModel(stdLicUri);
		if (lic == null) {
			fail("license is null");
		}
		assertEquals(id, lic.getId());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXLicenseInfoFactory#getLicenseInfoFromModel(com.hp.hpl.jena.rdf.model.Model, com.hp.hpl.jena.graph.Node)}.
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testGetLicenseInfoFromModel() throws InvalidSPDXAnalysisException {
		// standard license
		SPDXLicenseInfo li = SPDXLicenseInfoFactory.getLicenseInfoFromModel(model, STANDARD_LICENSES_RESOURCES[0].asNode());
		if (!(li instanceof SPDXStandardLicense)) {
			fail ("Wrong type for standard license");
		}
		ArrayList<String> verify = li.verify();
		assertEquals(0, verify.size());
		SPDXStandardLicense sli = (SPDXStandardLicense)li;
		assertEquals(STD_IDS[0], sli.getId());
		String licenseText = sli.getText().trim();
		if (!licenseText.startsWith(STD_TEXTS[0])) {
			fail("Incorrect license text");
		}
		// non-standard license
		SPDXLicenseInfo li2 = SPDXLicenseInfoFactory.getLicenseInfoFromModel(model, NON_STD_LICENSES_RESOURCES[0].asNode());
		if (!(li2 instanceof SPDXNonStandardLicense)) {
			fail ("Wrong type for non-standard license");
		}
		SPDXNonStandardLicense nsli2 = (SPDXNonStandardLicense)li2;
		assertEquals(NONSTD_IDS[0], nsli2.getId());
		assertEquals(NONSTD_TEXTS[0], nsli2.getText());
		verify = li2.verify();
		assertEquals(0, verify.size());
		// conjunctive license
		SPDXLicenseInfo cli = SPDXLicenseInfoFactory.getLicenseInfoFromModel(model, CONJUNCTIVE_LICENSES_RESOURCES[0].asNode());
		if (!(cli instanceof SPDXConjunctiveLicenseSet)) {
			fail ("Wrong type for conjuctive licenses license");
		}
		assertEquals(CONJUNCTIVE_LICENSES[0], cli);
		verify = cli.verify();
		assertEquals(0, verify.size());
		// disjunctive license
		SPDXLicenseInfo dli = SPDXLicenseInfoFactory.getLicenseInfoFromModel(model, DISJUNCTIVE_LICENSES_RESOURCES[0].asNode());
		if (!(dli instanceof SPDXDisjunctiveLicenseSet)) {
			fail ("Wrong type for disjuncdtive licenses license");
		}
		assertEquals(DISJUNCTIVE_LICENSES[0], dli);
		verify = dli.verify();
		assertEquals(0, verify.size());
		// complex license
		SPDXLicenseInfo complex = SPDXLicenseInfoFactory.getLicenseInfoFromModel(model, COMPLEX_LICENSE_RESOURCE.asNode());
		assertEquals(COMPLEX_LICENSE, complex);
		verify = complex.verify();
		assertEquals(0, verify.size());
	}

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXLicenseInfoFactory#parseSPDXLicenseString(java.lang.String)}.
	 * @throws InvalidLicenseStringException 
	 */
	@Test
	public void testParseSPDXLicenseString() throws InvalidLicenseStringException {
		String parseString = COMPLEX_LICENSE.toString();
		SPDXLicenseInfo li = SPDXLicenseInfoFactory.parseSPDXLicenseString(parseString);
		if (!li.equals(COMPLEX_LICENSE)) {
			fail("Parsed license does not equal");
		}
	}
	
	@Test
	public void testSpecialLicenses() throws InvalidLicenseStringException, InvalidSPDXAnalysisException {
		// NONE
		SPDXLicenseInfo none = SPDXLicenseInfoFactory.parseSPDXLicenseString(SPDXLicenseInfoFactory.NONE_LICENSE_NAME);
		Resource r = none.createResource(model);
		SPDXLicenseInfo comp = SPDXLicenseInfoFactory.getLicenseInfoFromModel(model, r.asNode());
		assertEquals(none, comp);
		ArrayList<String> verify = comp.verify();
		assertEquals(0, verify.size());
		// NOASSERTION_NAME
		SPDXLicenseInfo noAssertion = SPDXLicenseInfoFactory.parseSPDXLicenseString(SPDXLicenseInfoFactory.NOASSERTION_LICENSE_NAME);
		r = noAssertion.createResource(model);
		comp = SPDXLicenseInfoFactory.getLicenseInfoFromModel(model, r.asNode());
		assertEquals(noAssertion, comp);
		verify = comp.verify();
		assertEquals(0, verify.size());
	}

}
