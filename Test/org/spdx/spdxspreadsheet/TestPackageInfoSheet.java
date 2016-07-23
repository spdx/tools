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
package org.spdx.spdxspreadsheet;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.ExternalRef;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxPackage;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ConjunctiveLicenseSet;
import org.spdx.rdfparser.license.DisjunctiveLicenseSet;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.SpdxNoneLicense;
import org.spdx.spdxspreadsheet.PackageInfoSheet;
import org.spdx.spdxspreadsheet.SPDXSpreadsheet;
import org.spdx.spdxspreadsheet.SpreadsheetException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;

public class TestPackageInfoSheet {

	File spreadsheetFile;

	@Before
	public void setUp() throws Exception {
		spreadsheetFile = File.createTempFile("TEST_PKG_INFO", "xls");
	}

	@After
	public void tearDown() throws Exception {
		spreadsheetFile.delete();
	}


	@Test
	public void testCreate() throws IOException, InvalidFormatException {

		Workbook wb = new HSSFWorkbook();
		PackageInfoSheet.create(wb, "Package Info");
		PackageInfoSheet pkgInfo = PackageInfoSheet.openVersion(wb, "Package Info", SPDXSpreadsheet.CURRENT_VERSION);
		String ver = pkgInfo.verify();
		if (ver != null && !ver.isEmpty()){
			fail(ver);
		}
	}

	@Test
	public void testAddAndGet() throws SpreadsheetException, InvalidSPDXAnalysisException {
		AnyLicenseInfo[] testLicenses1 = new AnyLicenseInfo[3];
		testLicenses1[0] = new ExtractedLicenseInfo("License1", "License1Text");
		AnyLicenseInfo[] disjunctiveLicenses = new AnyLicenseInfo[3];
		disjunctiveLicenses[0] = new ExtractedLicenseInfo("disj1", "disj1 Text");
		disjunctiveLicenses[1] = new ExtractedLicenseInfo("disj2", "disj2 Text");
		disjunctiveLicenses[2] = new ExtractedLicenseInfo("disj3", "disj3 Text");
		testLicenses1[1] = new DisjunctiveLicenseSet(disjunctiveLicenses);
		AnyLicenseInfo[] conjunctiveLicenses = new AnyLicenseInfo[] {
				new ExtractedLicenseInfo("conj1", "conj1 Text"),
				new ExtractedLicenseInfo("conj2", "conj2 Text")
		};
		testLicenses1[2] = new ConjunctiveLicenseSet(conjunctiveLicenses);
		AnyLicenseInfo testLicense1 = new DisjunctiveLicenseSet(testLicenses1);

//		String lic1String = PackageInfoSheet.licensesToString(testLicenses1);
		AnyLicenseInfo[] testLicenses2 = new AnyLicenseInfo[2];
		testLicenses2[0] = new ExtractedLicenseInfo("License3", "License 3 text");
		testLicenses2[1] = new ExtractedLicenseInfo("License4", "License 4 text");
		AnyLicenseInfo testLicense2 = new ConjunctiveLicenseSet(testLicenses2);
		AnyLicenseInfo[] testLicenseInfos = new AnyLicenseInfo[] {new SpdxNoneLicense()};
		SpdxPackageVerificationCode testVerification = new SpdxPackageVerificationCode("value",
				new String[] {"skippedfil1", "skippedfile2"});
//		String lic2String = PackageInfoSheet.licensesToString(testLicenses2);

		SpdxPackage pkgInfo1 =  new SpdxPackage("decname1", "Comment1", new Annotation[0],
				new Relationship[0], testLicense1, testLicenseInfos,
				"dec-copyright1", "license comments", testLicense2,
				new Checksum[] {new Checksum(Checksum.ChecksumAlgorithm.checksumAlgorithm_sha1, "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12")},
				"desc1", "http://url1", new SpdxFile[0], "http://www.home.page1", "Organization: originator1",
				"machinename1", testVerification,  "sourceinfo1", "short desc1", "Person: supplier1",
				"Version1", true, new ExternalRef[0]);
		pkgInfo1.setId("SPDXRef-Package1");
		SpdxPackage pkgInfo2 =  new SpdxPackage("decname1", "Comment1", new Annotation[0],
				new Relationship[0], testLicense1, testLicenseInfos,
				"dec-copyright1", "license comments2", testLicense2,
				new Checksum[] {new Checksum(Checksum.ChecksumAlgorithm.checksumAlgorithm_sha1, "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12")},
				"desc1", "http://url1", new SpdxFile[0], "http://www.home.page2", "Organization: originator2",
				"machinename1", testVerification,  "sourceinfo1", "short desc1", "Person: supplier1",
				"Version2", false, new ExternalRef[0]);
		pkgInfo2.setId("SPDXRef-Package2");
		Workbook wb = new HSSFWorkbook();
		PackageInfoSheet.create(wb, "Package Info");
		PackageInfoSheet pkgInfoSheet = PackageInfoSheet.openVersion(wb, "Package Info", SPDXSpreadsheet.CURRENT_VERSION);
		pkgInfoSheet.add(pkgInfo1);
		pkgInfoSheet.add(pkgInfo2);
		SpdxPackage tstPkgInfo1 = pkgInfoSheet.getPackages(null)[0];
		SpdxPackage tstPkgInfo2 = pkgInfoSheet.getPackages(null)[1];
		comparePkgInfo(pkgInfo1, tstPkgInfo1);
		comparePkgInfo(pkgInfo2, tstPkgInfo2);
		assertEquals(2, pkgInfoSheet.getPackages(null).length);
	}

	private void comparePkgInfo(SpdxPackage pkgInfo1,
			SpdxPackage pkgInfo2) throws InvalidSPDXAnalysisException {
		assertEquals(pkgInfo1.getId(), pkgInfo2.getId());
		assertEquals(pkgInfo1.getCopyrightText(), pkgInfo2.getCopyrightText());
		assertEquals(pkgInfo1.getVersionInfo(), pkgInfo2.getVersionInfo());
		assertEquals(pkgInfo1.getLicenseDeclared(), pkgInfo2.getLicenseDeclared());
		assertEquals(pkgInfo1.getLicenseConcluded(), pkgInfo2.getLicenseConcluded());
		assertEquals(pkgInfo1.getName(), pkgInfo2.getName());
		assertEquals(pkgInfo1.getDescription(), pkgInfo2.getDescription());
		assertEquals(pkgInfo1.getPackageVerificationCode().getValue(), pkgInfo2.getPackageVerificationCode().getValue());
		assertEquals(pkgInfo1.getPackageFileName(), pkgInfo2.getPackageFileName());
		assertEquals(pkgInfo1.getSha1(), pkgInfo2.getSha1());
		assertEquals(pkgInfo1.getSummary(), pkgInfo2.getSummary());
		assertEquals(pkgInfo1.getSourceInfo(), pkgInfo2.getSourceInfo());
		assertEquals(pkgInfo1.getDownloadLocation(), pkgInfo2.getDownloadLocation());
		if (!compareLicenses(pkgInfo1.getLicenseInfoFromFiles(), pkgInfo2.getLicenseInfoFromFiles())) {
			fail("license information in files not equal");
		}
		assertEquals(pkgInfo1.getPackageVerificationCode().getValue(),
				pkgInfo2.getPackageVerificationCode().getValue());
		assertEquals(pkgInfo1.getPackageVerificationCode().getExcludedFileNames().length,
				pkgInfo2.getPackageVerificationCode().getExcludedFileNames().length);
		for (int i = 0; i < pkgInfo1.getPackageVerificationCode().getExcludedFileNames().length; i++) {
			assertEquals(pkgInfo1.getPackageVerificationCode().getExcludedFileNames()[i],
					pkgInfo2.getPackageVerificationCode().getExcludedFileNames()[i]);
		}
		assertEquals(pkgInfo1.getSupplier(), pkgInfo2.getSupplier());
		assertEquals(pkgInfo1.getOriginator(), pkgInfo2.getOriginator());
		assertEquals(pkgInfo1.getHomepage(), pkgInfo2.getHomepage());
		assertEquals(pkgInfo1.isFilesAnalyzed(), pkgInfo2.isFilesAnalyzed());
	}

	/**
	 * @param license1
	 * @param licenses2
	 */
	private boolean compareLicenses(AnyLicenseInfo[] licenses1,
			AnyLicenseInfo[] licenses2) {
		if (licenses1.length != licenses2.length) {
			return false;
		}
		for (int i = 0; i < licenses1.length; i++) {
			boolean found = false;
			for (int j = 0; j < licenses2.length; j++) {
				if (licenses1[i].equals(licenses2[j])) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	static public void compareLicenseDeclarations(
			AnyLicenseInfo[] testLicenses,
			AnyLicenseInfo[] result) {
		assertEquals(testLicenses.length, result.length);
		for (int i = 0;i < testLicenses.length; i++) {
			boolean found = false;
			for (int j = 0; j < result.length; j++) {
				if (testLicenses[i].equals(result[j])) {
					found = true;
				}
			}
			if (!found) {
				fail("License match "+testLicenses[i].toString()+ " was not found.");
			}
		}
	}
}
