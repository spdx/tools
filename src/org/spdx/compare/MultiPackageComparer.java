/**
 * Copyright (c) 2015 Source Auditor Inc.
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
package org.spdx.compare;

import org.spdx.rdfparser.model.SpdxPackage;

/**
 * Compares multiple SPDX packages
 * @author Gary O'Neall
 *
 */
public class MultiPackageComparer {
	
	SpdxPackageComparer[][] pkgComparers;

	/**
	 * @param nextPackages
	 */
	public MultiPackageComparer(SpdxPackage[] nextPackages) {
		// need to compare NXN
		// remove nulls first - they are allowed
		int count = 0;
		for (int i = 0; i < nextPackages.length; i++) {
			if (nextPackages[i]!= null) {
				count++;
			}
		}
		if (count < 2) {
			// No need for comparisons
			pkgComparers = new SpdxPackageComparer[0][];
			return;
		}
		pkgComparers = new SpdxPackageComparer[count-1][];
		SpdxPackage[] nonNullPackages = new SpdxPackage[count];
		int nnindex = 0;
		for (int i = 0; i < nextPackages.length; i++) {
			if (nextPackages[i]!= null) {
				nonNullPackages[nnindex++] = nextPackages[i];
			}
		}
		for (int i = 0; i < nonNullPackages.length; i++) {
			pkgComparers[i] = new SpdxPackageComparer[nonNullPackages.length-(1+i)];
			for (int j = 0; j < nonNullPackages.length-(1+i); j++) {
				pkgComparers[i][j] = new SpdxPackageComparer();
//				pkgComparers[i][j].compare(pkg1, pkg2, licenseXlationMap)
				// TODO - Finish implementation
			}
		}
	}

	/**
	 * @return
	 */
	public boolean isPackageNamesEqual() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public boolean isPackageVersionsEqual() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public boolean isPackageFileNamesEqual() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public boolean isPackageSuppliersEqual() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public boolean isPackageOriginatorsEqual() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public boolean ispackageHomePagesEqual() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public boolean isPackageDownloadLocationsEqual() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public boolean isPackageVerificationCodesEqual() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public boolean isPackageChecksumsEqual() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public boolean isSourceInformationEqual() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public boolean isPackageConcludedLicensesEqual() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public boolean isPackageLicenseInfoFromFilesEqual() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public boolean isPackageDeclaredLicensesEqual() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public boolean isLicenseCommentsEqual() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public boolean isCopyrightTextsEqual() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public boolean isPackageSummariesEqual() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public boolean isPackageDescriptionsEqual() {
		// TODO Auto-generated method stub
		return false;
	}

}
