package org.spdx.rdfparser.license;

import org.junit.Assert;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * @author yevster@gmail.com, Black Duck Software
 *         SPDX-License-Identifier: Apache-2.0
 */
public class TestListedLicenses {

	@Test
	public void testLicenseListVersionFormat() {
		String licenseListversion = ListedLicenses.getListedLicenses().getLicenseListVersion();

		Assert.assertEquals("Expected one point in license list version. ", 1, StringUtils.countMatches(licenseListversion, "."));
		Assert.assertTrue("Number expected before the point in license list version (" + licenseListversion + ")", StringUtils.isNumeric(StringUtils.substringBefore(licenseListversion, ".")));
		Assert.assertTrue("Number expected after the point in license list version (" + licenseListversion + ")", StringUtils.isNumeric(StringUtils.substringAfter(licenseListversion, ".")));
	}
}
