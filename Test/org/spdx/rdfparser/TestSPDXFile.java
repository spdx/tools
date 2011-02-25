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
	 * Test method for {@link org.spdx.rdfparser.SPDXFile#populateModel(com.hp.hpl.jena.rdf.model.Resource, com.hp.hpl.jena.rdf.model.Model)}.
	 * @throws IOException 
	 * @throws InvalidSPDXDocException 
	 */
	@Test
	public void testPopulateModel() throws IOException, InvalidSPDXDocException {
		Model model = ModelFactory.createDefaultModel();
		SPDXDocument doc = new SPDXDocument(model);
		String testDocUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082";
		doc.createSpdxDocument(testDocUri);
		String pkgUri = "https://olex.openlogic.com/spdxdoc/package_versions/download/4832?path=openlogic/zlib/1.2.3/zlib-1.2.3-all-src.zip&amp;package_version_id=1082?pkg";
		doc.createSpdxPackage(pkgUri);
		StringWriter writer = new StringWriter();
		doc.getModel().write(writer);
		String beforeCreate = writer.toString();
		writer.close();
		Resource pkgResource = model.getResource(pkgUri);
		Property p = model.createProperty(SPDXDocument.SPDX_NAMESPACE, SPDXDocument.PROP_PACKAGE_FILE);
		Resource fileResource = model.createResource(p);
		pkgResource.addProperty(p, fileResource);
		
		LicenseDeclaration[] declaredLic = new LicenseDeclaration[] {
				new LicenseDeclaration("lic1name", null),
				new LicenseDeclaration("Lic2Name", new String[] {"dis1", "dis2"})
		};
		LicenseDeclaration[] seenLic = new LicenseDeclaration[] {
				new LicenseDeclaration("lic3name", new String[] {"dis4"}),
				new LicenseDeclaration("Lic4Name", new String[0])
		};
		SPDXFile file = new SPDXFile("fileName", "FileType", "sha1", 
				declaredLic, seenLic, "License comments", 
				"Copyrights", "Artifactof");
		file.populateModel(fileResource, model);
		
		SPDXFile file2 = new SPDXFile(fileResource.asNode(), model);
// - not yet implemented		assertEquals(file.getArtifactOf(), file2.getArtifactOf());
		assertEquals(file.getCopyright(), file2.getCopyright());
		assertEquals(file.getLicenseComments(), file2.getLicenseComments());
		assertEquals(file.getName(), file2.getName());
		assertEquals(file.getSha1(), file2.getSha1());
		assertEquals(file.getType(), file2.getType());
		TestPackageInfoSheet.compareLicenseDeclarations(file.getFileLicenses(), file2.getFileLicenses());
		TestPackageInfoSheet.compareLicenseDeclarations(file.getSeenLicenses(), file2.getSeenLicenses());
	}

}
