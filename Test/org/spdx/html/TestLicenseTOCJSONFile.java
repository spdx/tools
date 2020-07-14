package org.spdx.html;

import static org.junit.Assert.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

public class TestLicenseTOCJSONFile {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetJsonObject() throws InvalidLicenseStringException {
		String version = "12.0";
		String releaseDate = "12/12/2018";
		LicenseTOCJSONFile tocJson = new LicenseTOCJSONFile(version, releaseDate);

		SpdxListedLicense zeroBSD = (SpdxListedLicense)LicenseInfoFactory.parseSPDXLicenseString("0BSD");
		String zeroBSDLicHtmlRef = "license/0BSD";
		String zeroBSDLicJsonRef = "json/0BSD";
		boolean zeroBSDDeprecated = false;
		tocJson.addLicense(zeroBSD, zeroBSDLicHtmlRef, zeroBSDLicJsonRef, zeroBSDDeprecated);

		SpdxListedLicense zlib = (SpdxListedLicense)LicenseInfoFactory.parseSPDXLicenseString("Zlib");
		String zlibLicHtmlRef = "license/Zlib";
		String zlibLicJsonRef = "json/Zlib";
		boolean zlibDeprecated = true;
		tocJson.addLicense(zlib, zlibLicHtmlRef, zlibLicJsonRef, zlibDeprecated);

		SpdxListedLicense giftware = (SpdxListedLicense)LicenseInfoFactory.parseSPDXLicenseString("Giftware");
		String giftwareLicHtmlRef = "license/giftware";
		String giftwareLicJsonRef = "json/giftware";
		boolean giftwareDeprecated = false;
		tocJson.addLicense(giftware, giftwareLicHtmlRef, giftwareLicJsonRef, giftwareDeprecated);


		JSONObject result = tocJson.getJsonObject();
		assertEquals(version, result.get(SpdxRdfConstants.PROP_LICENSE_LIST_VERSION));
		assertEquals(releaseDate, result.get("releaseDate"));
		JSONArray licenses = (JSONArray)result.get("licenses");
		assertEquals(3, licenses.size());
		JSONObject firstLicense = (JSONObject)licenses.get(0);
		assertEquals(zeroBSD.getLicenseId(), firstLicense.get(SpdxRdfConstants.PROP_LICENSE_ID));
		assertEquals(zeroBSDLicHtmlRef, firstLicense.get("reference"));
		assertEquals("http://spdx.org/licenses/" + zeroBSDLicJsonRef, firstLicense.get(LicenseTOCJSONFile.JSON_REFERENCE_FIELD));
		assertEquals(zeroBSDDeprecated, firstLicense.get(SpdxRdfConstants.PROP_LIC_ID_DEPRECATED));

		JSONObject secondLicense = (JSONObject)licenses.get(1); //Note - sorted list this license should be next
		assertEquals(giftware.getLicenseId(), secondLicense.get(SpdxRdfConstants.PROP_LICENSE_ID));
		assertEquals(giftwareLicHtmlRef, secondLicense.get("reference"));
		assertEquals("http://spdx.org/licenses/" + giftwareLicJsonRef, secondLicense.get(LicenseTOCJSONFile.JSON_REFERENCE_FIELD));
		assertEquals(giftwareDeprecated, secondLicense.get(SpdxRdfConstants.PROP_LIC_ID_DEPRECATED));

		JSONObject thirdLicense = (JSONObject)licenses.get(2);
		assertEquals(zlib.getLicenseId(), thirdLicense.get(SpdxRdfConstants.PROP_LICENSE_ID));
		assertEquals(zlibLicHtmlRef, thirdLicense.get("reference"));
		assertEquals("http://spdx.org/licenses/" + zlibLicJsonRef, thirdLicense.get(LicenseTOCJSONFile.JSON_REFERENCE_FIELD));
		assertEquals(zlibDeprecated, thirdLicense.get(SpdxRdfConstants.PROP_LIC_ID_DEPRECATED));
	}

}
