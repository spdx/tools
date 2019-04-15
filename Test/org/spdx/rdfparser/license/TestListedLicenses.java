package org.spdx.rdfparser.license;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;

/**
 * @author yevster@gmail.com, Black Duck Software
 *         SPDX-License-Identifier: Apache-2.0
 */
public class TestListedLicenses {

	@Test
	public void testLicenseListVersionFormat() {
		String licenseListversion = ListedLicenses.getListedLicenses().getLicenseListVersion();

		assertEquals("Expected one point in license list version. ", 1, StringUtils.countMatches(licenseListversion, "."));
		assertTrue("Number expected before the point in license list version (" + licenseListversion + ")", StringUtils.isNumeric(StringUtils.substringBefore(licenseListversion, ".")));
		assertTrue("Number expected after the point in license list version (" + licenseListversion + ")", StringUtils.isNumeric(StringUtils.substringAfter(licenseListversion, ".")));
	}
	
	@Test
	public void testGetListedLicenseById() throws InvalidSPDXAnalysisException {
		String id = "Apache-2.0";
		SpdxListedLicense result = ListedLicenses.getListedLicenses().getListedLicenseById(id);
		assertEquals(id, result.getLicenseId());
	}
	
	@Test
	public void testGetLicenseIbyIdLocal() throws InvalidSPDXAnalysisException {
		System.setProperty("SPDXParser.OnlyUseLocalLicenses", "true");
		ListedLicenses.resetListedLicenses();
		try {
			String id = "Apache-2.0";
			SpdxListedLicense result = ListedLicenses.getListedLicenses().getListedLicenseById(id);
			assertEquals(id, result.getLicenseId());
		} finally {
			System.setProperty("SPDXParser.OnlyUseLocalLicenses", "false");
			ListedLicenses.resetListedLicenses();
		}
	}
}
