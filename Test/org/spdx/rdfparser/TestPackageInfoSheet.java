package org.spdx.rdfparser;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.rdfparser.LicenseDeclaration;
import org.spdx.spdxspreadsheet.PackageInfoSheet;
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
	public void testVerify() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreate() throws IOException, InvalidFormatException {
		
		Workbook wb = new HSSFWorkbook();
		PackageInfoSheet.create(wb, "Package Info");
		PackageInfoSheet pkgInfo = new PackageInfoSheet(wb, "Package Info");
		String ver = pkgInfo.verify();
		if (ver != null && !ver.isEmpty()){
			fail(ver);
		}
	}

	@Test
	public void testAddAndGet() throws SpreadsheetException {
		LicenseDeclaration[] testLicenses1 = new LicenseDeclaration[3];
		testLicenses1[0] = new LicenseDeclaration("License1", new String[0]);
		testLicenses1[1] = new LicenseDeclaration("License2", new String[] {
				"disj1", "disj2", "disj3"
		});
		testLicenses1[2] = new LicenseDeclaration("License3", new String[] {"disjj"});
//		String lic1String = PackageInfoSheet.licensesToString(testLicenses1);
		LicenseDeclaration[] testLicenses2 = new LicenseDeclaration[2];
		testLicenses2[0] = new LicenseDeclaration("License3", new String[]{"testdik1", "testdis2"});
		testLicenses2[1] = new LicenseDeclaration("License4", new String[] {
				"disj1", "disj2", "disj3", "disj4"
		});
//		String lic2String = PackageInfoSheet.licensesToString(testLicenses2);
		SPDXPackageInfo pkgInfo1 = new SPDXPackageInfo("decname1", "machinename1", 
				"sha1-1", "sourceinfo1", testLicenses1, testLicenses2, "dec-copyright1",
				"short desc1", "desc1", "http://url1", "filechecksum1");
		SPDXPackageInfo pkgInfo2 = new SPDXPackageInfo("decname1", "machinename1", 
				"sha1-1", "sourceinfo1", testLicenses1, testLicenses2, "dec-copyright1",
				"short desc1", "desc1", "http://url1", "filechecksum1");
		Workbook wb = new HSSFWorkbook();
		PackageInfoSheet.create(wb, "Package Info");
		PackageInfoSheet pkgInfoSheet = new PackageInfoSheet(wb, "Package Info");
		pkgInfoSheet.add(pkgInfo1);
		pkgInfoSheet.add(pkgInfo2);
		SPDXPackageInfo tstPkgInfo1 = pkgInfoSheet.getPackageInfo(1);
		SPDXPackageInfo tstPkgInfo2 = pkgInfoSheet.getPackageInfo(2);
		comparePkgInfo(pkgInfo1, tstPkgInfo1);
		comparePkgInfo(pkgInfo2, tstPkgInfo2);
		SPDXPackageInfo tstPkgInfo3 = pkgInfoSheet.getPackageInfo(3);
		if (tstPkgInfo3 != null) {
			fail("should be null");
		}
	}

	private void comparePkgInfo(SPDXPackageInfo pkgInfo1,
			SPDXPackageInfo pkgInfo2) {
		assertEquals(pkgInfo1.getDeclaredCopyright(), pkgInfo2.getDeclaredCopyright());
		compareLicenseDeclarations(pkgInfo1.getDeclaredLicenses(), pkgInfo2.getDeclaredLicenses());
		compareLicenseDeclarations(pkgInfo1.getDetectedLicenses(), pkgInfo2.getDetectedLicenses());
		assertEquals(pkgInfo1.getDeclaredName(), pkgInfo2.getDeclaredName());
		assertEquals(pkgInfo1.getDescription(), pkgInfo2.getDescription());
		assertEquals(pkgInfo1.getFileChecksum(), pkgInfo2.getFileChecksum());
		assertEquals(pkgInfo1.getFileName(), pkgInfo2.getFileName());
		assertEquals(pkgInfo1.getSha1(), pkgInfo2.getSha1());
		assertEquals(pkgInfo1.getShortDescription(), pkgInfo2.getShortDescription());
		assertEquals(pkgInfo1.getSourceInfo(), pkgInfo2.getSourceInfo());
		assertEquals(pkgInfo1.getUrl(), pkgInfo2.getUrl());
	}

	static public void compareLicenseDeclarations(
			LicenseDeclaration[] testLicenses,
			LicenseDeclaration[] result) {
		assertEquals(testLicenses.length, result.length);
		for (int i = 0;i < testLicenses.length; i++) {
			boolean found = false;
			for (int j = 0; j < result.length; j++) {
				if (testLicenses[i].getName().equals(result[j].getName())) {
					if ((testLicenses[i].getDisjunctiveLicenses() == null ||
							testLicenses[i].getDisjunctiveLicenses().length == 0) &&
							(result[j].getDisjunctiveLicenses() == null ||
							result[j].getDisjunctiveLicenses().length == 0)) {
						found = true;
						break;
					} else if (testLicenses[i].getDisjunctiveLicenses().length == result[j].getDisjunctiveLicenses().length) {
						for (int k = 0; k < testLicenses[i].getDisjunctiveLicenses().length; k++) {
							boolean found2 = false;
							for (int l = 0; l < result[j].getDisjunctiveLicenses().length; l++) {
								if (testLicenses[i].getDisjunctiveLicenses()[k].equals(result[j].getDisjunctiveLicenses()[l])) {
									found2 = true;
									break;
								}
							}
							if (found2) {
								found = true;
							} else {
								found = false;
								break;
							}
						}
					}
				}
			}
			if (!found) {
				fail("License match "+testLicenses[i].getName()+ " was not found.");
			}
		}
	}

	@Test
	public void testParseLicenseString() throws SpreadsheetException {
		LicenseDeclaration[] testLicenses = new LicenseDeclaration[3];
		testLicenses[0] = new LicenseDeclaration("License1", new String[0]);
		testLicenses[1] = new LicenseDeclaration("License2", new String[] {
				"disj1", "disj2", "disj3"
		});
		testLicenses[2] = new LicenseDeclaration("License3", new String[] {"disjj"});
		String licString = PackageInfoSheet.licensesToString(testLicenses);
		LicenseDeclaration[] result = PackageInfoSheet.parseLicenseString(licString);
		compareLicenseDeclarations(testLicenses, result);
	}

}
