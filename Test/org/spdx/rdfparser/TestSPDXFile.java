/**
 * Copyright (c) 2011 Source Auditor Inc.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.spdx.rdfparser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Source Auditor
 *
 */
public class TestSPDXFile {

	static final String[] NONSTD_IDS = new String[] {"id1", "id2", "id3", "id4"};
	static final String[] NONSTD_TEXTS = new String[] {"text1", "text2", "text3", "text4"};
	static final String[] STD_IDS = new String[] {"AFL-3", "CECILL-B", "EUPL-1"};
	static final String[] STD_TEXTS = new String[] {"std text1", "std text2", "std text3"};

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
					STD_IDS[i], STD_TEXTS[i], "URL "+String.valueOf(i), "Notes "+String.valueOf(i), 
					"LicHeader "+String.valueOf(i), "Template "+String.valueOf(i));
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

	/**
	 * Test method for {@link org.spdx.rdfparser.SPDXFile#populateModel(com.hp.hpl.jena.rdf.model.Resource, com.hp.hpl.jena.rdf.model.Model)}.
	 * @throws IOException 
	 * @throws InvalidSPDXAnalysisException 
	 */
	@Test
	public void testPopulateModel() throws IOException, InvalidSPDXAnalysisException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testDocUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxAnalysis(testDocUri);
		String pkgUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082?pkg";
		doc.createSpdxPackage(pkgUri);
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		Resource pkgResource = model.getResource(pkgUri);
		Property p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_PACKAGE_FILE);
		
		SPDXLicenseInfo[] declaredLic = new SPDXLicenseInfo[] {COMPLEX_LICENSE};
		SPDXLicenseInfo[] seenLic = new SPDXLicenseInfo[] {STANDARD_LICENSES[0]};
		
		DOAPProject[] artifactOfs = new DOAPProject[] {new DOAPProject("Artifactof Project", "ArtifactOf homepage")};
		SPDXFile file = new SPDXFile("fileName", "FileType", "sha1", 
				declaredLic, seenLic, "License comments", 
				"Copyrights", artifactOfs);
		Resource fileResource = file.createResource(model);
		pkgResource.addProperty(p, fileResource);

		SPDXFile file2 = new SPDXFile(model, fileResource.asNode());
		assertEquals(file.getArtifactOf()[0].getName(), file2.getArtifactOf()[0].getName());
		assertEquals(file.getArtifactOf()[0].getHomePage(), file2.getArtifactOf()[0].getHomePage());
		assertEquals(file.getCopyright(), file2.getCopyright());
		assertEquals(file.getLicenseComments(), file2.getLicenseComments());
		assertEquals(file.getName(), file2.getName());
		assertEquals(file.getSha1(), file2.getSha1());
		assertEquals(file.getType(), file2.getType());
		TestPackageInfoSheet.compareLicenseDeclarations(file.getFileLicenses(), file2.getFileLicenses());
		TestPackageInfoSheet.compareLicenseDeclarations(file.getSeenLicenses(), file2.getSeenLicenses());
	}

}
