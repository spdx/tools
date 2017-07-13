package org.spdx.tools;

import static org.junit.Assert.*;

import java.io.StringWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.license.LicenseException;
import org.spdx.rdfparser.license.SpdxListedLicense;

public class TestMarkdownTable {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		String version = "v1.1";
		MarkdownTable md = new MarkdownTable(version);
		String name1 = "name1";
		String id1 = "id1";
		boolean osiApproved1 = true;
		SpdxListedLicense lic1 = new SpdxListedLicense(name1, id1, "text", new String[0],
				"", "", "", osiApproved1);
		md.addLicense(lic1, false);
		String name2 = "name2";
		String id2 = "id2";
		boolean osiApproved2 = false;
		SpdxListedLicense lic2 = new SpdxListedLicense(name2, id2, "text", new String[0],
				"", "", "", osiApproved2);
		md.addLicense(lic2, false);
		String name3 = "name3";
		String id3 = "id3";
		boolean osiApproved3 = false;
		SpdxListedLicense depLicense = new SpdxListedLicense(name3, id3, "text", new String[0],
				"", "", "", osiApproved3);
		md.addLicense(depLicense, false);
		String name4 = "name4";
		String id4 = "id4";
		LicenseException ex1 = new LicenseException();
		ex1.setLicenseExceptionId(id4);
		ex1.setName(name4);
		md.addException(ex1, false);
		String name5 = "name5";
		String id5 = "id5";
		LicenseException ex2 = new LicenseException();
		ex2.setLicenseExceptionId(id5);
		ex2.setName(name5);
		md.addException(ex2, false);
		StringWriter result = new StringWriter();
		md.writeTOC(result);
		String sresult = result.toString();
		assertTrue(sresult.contains(name1));
		assertTrue(sresult.contains(name2));
		//TODO: add remaining checks after implementation
	}

}
