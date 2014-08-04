/**
 * Copyright (c) 2014 Gang Ling.
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
package org.spdx.merge;

import java.security.NoSuchAlgorithmException;

import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.JavaSha1ChecksumGenerator;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocument.SPDXPackage;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXPackageInfo;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.VerificationCodeGenerator;

/**
 * @author Gang Ling
 *
 */
public class SpdxPackageInfoMerger {

		private SPDXDocument master = null;
		public SpdxPackageInfoMerger(SPDXDocument masterDoc){
			this.master = masterDoc;
		}
		
		public SPDXPackage mergePackageInfo(SPDXFile[] fileMergeResult) throws InvalidSPDXAnalysisException, NoSuchAlgorithmException{
			SPDXPackage pkgMergeResult = SPDXPackageInfo.clone(master, master.getSpdxPackage().getHomePage());
			
			String[] skippedFiles = null;
			VerificationCodeGenerator vg = new VerificationCodeGenerator(new JavaSha1ChecksumGenerator());
			SpdxPackageVerificationCode result = vg.generatePackageVerificationCode(fileMergeResult, skippedFiles);
			pkgMergeResult.setVerificationCode(result);
			return pkgMergeResult;			
		}
		
		
		
}
