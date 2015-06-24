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
package org.spdx.tag;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.spdx.rdfparser.DOAPProject;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXCreatorInformation;
import org.spdx.rdfparser.SPDXDocument;
import org.spdx.rdfparser.SPDXDocument.SPDXPackage;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXReview;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.LicenseInfoFactory;
import org.spdx.rdfparser.license.SpdxNoneLicense;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Translates an tag-value file to a an SPDX Document.
 * 
 * This is the legacy tag-value tranlater for versions 1.2 and earlier
 * 
 * This has been replaced by BuildDocument which supports the SPDX 2.0 syntax
 * 
 * @author Rana Rahal, Protecode Inc.
 */
@Deprecated
public class BuildLegacyDocument implements TagValueBehavior, Serializable {
	private static final long serialVersionUID = -5490491489627686708L;

	private static final String DEFAULT_SHA1 = "0000000000000000000000000000000000000000";
	
	private Properties constants;
	private SPDXDocument analysis;
	private DateFormat format = new SimpleDateFormat(SpdxRdfConstants.SPDX_DATE_FORMAT);

	//When we retrieve a list from the SPDXDocument the order changes, therefore keep track of 
	//the last object that we are looking at so that we can fill in all of it's information
	private SPDXReview lastReviewer = null;
	private ExtractedLicenseInfo lastExtractedLicense = null;
	private SPDXFile lastFile = null;
	private DOAPProject lastProject = null;
	// Keep track of all file dependencies since these need to be added after all of the files
	// have been parsed.  Map of file dependency file name to the SPDX files which depends on it
	private Map<String, List<SPDXFile>> fileDependencyMap = Maps.newHashMap();

	public BuildLegacyDocument(Model model, SPDXDocument spdxDocument, Properties constants) {
		this.constants = constants;
		analysis = spdxDocument;
		try {
			analysis.createSpdxAnalysis("http://www.uri.com" + "#SPDXANALYSIS");
			analysis.createSpdxPackage();
		} catch (InvalidSPDXAnalysisException ex) {
			System.out
					.print("Error creating SPDX Analysis: " + ex.getMessage());
			return;
		}
	}

	@Override
    public void enter() throws Exception {
		// do nothing???
	}

	@Override
    public void buildDocument(String tag, String value) throws Exception {
		tag = tag.trim()+" ";
		value = trim(value.trim());
		// document
		if (tag.equals(constants.getProperty("PROP_SPDX_VERSION"))) {
			analysis.setSpdxVersion(value);
		} else if (tag.equals(constants.getProperty("PROP_SPDX_DATA_LICENSE"))) {
			analysis.getDataLicense().setName(value);
		} else if (tag.equals(constants.getProperty("PROP_CREATION_CREATOR"))) {
			if (analysis.getCreatorInfo() == null) {
				SPDXCreatorInformation creator = new SPDXCreatorInformation(new String[] { value }, "", "", "");
				analysis.setCreationInfo(creator);
			} else {
				List<String> creators = Lists.newArrayList(analysis.getCreatorInfo().getCreators());
				creators.add(value);
				analysis.getCreatorInfo().setCreators(creators.toArray(new String[0]));
			}
		} else if (tag.equals(constants.getProperty("PROP_CREATION_CREATED"))) {
			if (analysis.getCreatorInfo() == null) {
				SPDXCreatorInformation creator = new SPDXCreatorInformation(new String[] {  }, "", "", "");
				analysis.setCreationInfo(creator);
			}
			analysis.getCreatorInfo().setCreated(value);
		} else if (tag.equals(constants.getProperty("PROP_CREATION_COMMENT"))) {
			if (analysis.getCreatorInfo() == null) {
				SPDXCreatorInformation creator = new SPDXCreatorInformation(new String[] { value }, "", "", "");
				analysis.setCreationInfo(creator);
			}
			analysis.getCreatorInfo().setComment(value);
		} else if (tag.equals(constants.getProperty("PROP_LICENSE_LIST_VERSION"))) {
			if (analysis.getCreatorInfo() == null) {
				SPDXCreatorInformation creator = new SPDXCreatorInformation(new String[] { value }, "", "", "");
				analysis.setCreationInfo(creator);
			}
			analysis.getCreatorInfo().setLicenseListVersion(value);
		} else if (tag.equals(constants.getProperty("PROP_SPDX_COMMENT"))) {
			analysis.setDocumentComment(value);
		} else if (tag.equals(constants.getProperty("PROP_REVIEW_REVIEWER"))) {
			lastReviewer = new SPDXReview(value, format.format(new Date()), ""); // update date later
			List<SPDXReview> reviewers = Lists.newArrayList(analysis.getReviewers());
			reviewers.add(lastReviewer);
			analysis.setReviewers(reviewers.toArray(new SPDXReview[0]));
		} else if (tag.equals(constants.getProperty("PROP_REVIEW_DATE"))) {
			if (lastReviewer == null) {
				throw(new InvalidSpdxTagFileException("Missing Reviewer - A reviewer must be provided before a review date"));
			}
			lastReviewer.setReviewDate(value);
		} else if (tag.equals(constants.getProperty("PROP_REVIEW_COMMENT"))) {
			if (lastReviewer == null) {
				throw(new InvalidSpdxTagFileException("Missing Reviewer - A reviewer must be provided before a review comment"));
			}
			lastReviewer.setComment(value);
		} else if (tag.equals(constants.getProperty("PROP_LICENSE_ID"))) {
			lastExtractedLicense = new ExtractedLicenseInfo(value, "WARNING: TEXT IS REQUIRED", null, null, null); //change text later
			ExtractedLicenseInfo[] currentNonStdLicenses = analysis.getExtractedLicenseInfos();
			List<ExtractedLicenseInfo> licenses = Lists.newArrayList(currentNonStdLicenses);
			licenses.add(lastExtractedLicense);
			analysis.setExtractedLicenseInfos(licenses.toArray(new ExtractedLicenseInfo[0]));
		} else if (tag.equals(constants.getProperty("PROP_EXTRACTED_TEXT"))) {
			if (lastExtractedLicense == null) {
				throw(new InvalidSpdxTagFileException("Missing Extracted License - An  extracted license ID must be provided before the license text"));
			}
			lastExtractedLicense.setExtractedText(value);
		} else if (tag.equals(constants.getProperty("PROP_LICENSE_NAME"))) {
			if (lastExtractedLicense == null) {
				throw(new InvalidSpdxTagFileException("Missing Extracted License - An  extracted license ID must be provided before the license name"));
			}
			lastExtractedLicense.setName(value);
		} else if (tag.equals(constants.getProperty("PROP_SOURCE_URLS"))) {
			if (lastExtractedLicense == null) {
				throw(new InvalidSpdxTagFileException("Missing Extracted License - An  extracted license ID must be provided before the license URL"));
			}
			String[] values = value.split(",");
			for (int i = 0; i < values.length; i++) {
				values[i] = values[i].trim();
			}
			lastExtractedLicense.setSeeAlso(values);
		} else if (tag.equals(constants.getProperty("PROP_LICENSE_COMMENT"))) {
			if (lastExtractedLicense == null) {
				throw(new InvalidSpdxTagFileException("Missing Extracted License - An  extracted license ID must be provided before the license comment"));
			}
			lastExtractedLicense.setComment(value);
		} else {
			SPDXPackage spdxPackage = analysis.getSpdxPackage();
			buildPackage(spdxPackage, tag, value);
		}
	}

	/**
	 * @param spdxPackage
	 * @throws InvalidSPDXAnalysisException
	 */
	private void buildPackage(SPDXPackage pkg, String tag, String value)
			throws Exception {
		if (tag.equals(constants.getProperty("PROP_PACKAGE_DECLARED_NAME"))) {
			pkg.setDeclaredName(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_VERSION_INFO"))) {
			pkg.setVersionInfo(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_DOWNLOAD_URL"))) {
			// TODO can we set analysis.getModel() uri?
			pkg.setDownloadUrl(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_HOMEPAGE_URL"))) {
			pkg.setHomePage(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_SHORT_DESC"))) {
			pkg.setShortDescription(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_SOURCE_INFO"))) {
			pkg.setSourceInfo(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_FILE_NAME"))) {
			pkg.setFileName(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_SUPPLIER"))) {
			pkg.setSupplier(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_ORIGINATOR"))) {
			pkg.setOriginator(value);
		} else if (constants.getProperty("PROP_PACKAGE_CHECKSUM").startsWith(tag)) { // property contains SHA1:
			pkg.setSha1(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_VERIFICATION_CODE"))) {
			if (value.contains("(")) {
				String[] verification = value.split("\\(");
				String[] excludedFiles = verification[1].replace(")", "").split(",");
				for (int i = 0; i < excludedFiles.length; i++) {
					excludedFiles[i] = excludedFiles[i].trim();
				}
				pkg.setVerificationCode(new SpdxPackageVerificationCode(verification[0].trim(), excludedFiles));
			}
			else {
				pkg.setVerificationCode(new SpdxPackageVerificationCode(value, new String[0]));
			}
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_DESCRIPTION"))) {
			pkg.setDescription(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_DECLARED_COPYRIGHT"))) {
			pkg.setDeclaredCopyright(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_DECLARED_LICENSE"))) {
			AnyLicenseInfo licenseSet = LicenseInfoFactory.parseSPDXLicenseString(value);
			//TODO in the case of all licenses do we need to worry about the text? I'm only setting text in the package non-standard licenses
			pkg.setDeclaredLicense(licenseSet);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_CONCLUDED_LICENSE"))) {
			AnyLicenseInfo licenseSet = LicenseInfoFactory.parseSPDXLicenseString(value);
			pkg.setConcludedLicenses(licenseSet);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_LICENSE_COMMENT"))) {
			pkg.setLicenseComment(value);
		} else if (tag.equals(constants.getProperty("PROP_PACKAGE_LICENSE_INFO_FROM_FILES"))) {
			AnyLicenseInfo license = LicenseInfoFactory.parseSPDXLicenseString(value);
			List<AnyLicenseInfo> licenses = Lists.newArrayList(pkg.getLicenseInfoFromFiles());
			licenses.add(license);
			pkg.setLicenseInfoFromFiles(licenses.toArray(new AnyLicenseInfo[0]));
		} else {
			buildFile(pkg, tag, value);
		}
	}

	/**
	 * @param file
	 */
	private void buildFile(SPDXPackage pkg, String tag, String value)
			throws Exception {
		if (tag.equals(constants.getProperty("PROP_FILE_NAME"))) {
			lastFile = new SPDXFile(value, SpdxRdfConstants.FILE_TYPE_OTHER, DEFAULT_SHA1, new SpdxNoneLicense(),
					new AnyLicenseInfo[0], "", "", new DOAPProject[0]);
			pkg.addFile(lastFile);
		} else {
			if (lastFile == null) {
				if (tag.equals(constants.getProperty("PROP_FILE_TYPE")) || constants.getProperty("PROP_FILE_CHECKSUM").startsWith(tag) ||
						tag.equals(constants.getProperty("PROP_FILE_LICENSE")) || tag.equals(constants.getProperty("PROP_FILE_LIC_COMMENTS")) ||
						tag.equals(constants.getProperty("PROP_FILE_COPYRIGHT")) || tag.equals(constants.getProperty("PROP_FILE_COMMENT"))) {
					throw(new InvalidSpdxTagFileException("Missing File Name - A file name must be specified before the file properties"));
				} else {
					throw(new InvalidSpdxTagFileException("Unrecognized SPDX Tag: "+tag));
				}
			}
			if (tag.equals(constants.getProperty("PROP_FILE_TYPE"))) {
				lastFile.setType(value);
			} else if (constants.getProperty("PROP_FILE_CHECKSUM").startsWith(tag)) {
				lastFile.setSha1(value);
			} else if (tag.equals(constants.getProperty("PROP_FILE_LICENSE"))) {
				AnyLicenseInfo licenseSet = LicenseInfoFactory.parseSPDXLicenseString(value);
				lastFile.setConcludedLicenses(licenseSet);
			} else if (tag.equals(constants.getProperty("PROP_FILE_SEEN_LICENSE"))) {
				AnyLicenseInfo fileLicense = (LicenseInfoFactory.parseSPDXLicenseString(value));
				List<AnyLicenseInfo> seenLicenses = Lists.newArrayList(lastFile.getSeenLicenses());
				seenLicenses.add(fileLicense);
				lastFile.setSeenLicenses(seenLicenses.toArray(new AnyLicenseInfo[0]));
			} else if (tag.equals(constants.getProperty("PROP_FILE_LIC_COMMENTS"))) {
				lastFile.setLicenseComments(value);
			} else if (tag.equals(constants.getProperty("PROP_FILE_COPYRIGHT"))) {
				lastFile.setCopyright(value);
			} else if (tag.equals(constants.getProperty("PROP_FILE_COMMENT"))) {
				lastFile.setComment(value);
			} else if (tag.equals(constants.getProperty("PROP_FILE_DEPENDENCY"))) {
				addFileDependency(lastFile, value);
			} else if (tag.equals(constants.getProperty("PROP_FILE_CONTRIBUTOR"))) {
				addFileContributor(lastFile, value);
			} else if (tag.equals(constants.getProperty("PROP_FILE_NOTICE_TEXT"))) {
				lastFile.setNoticeText(value);
			} else {
				buildProject(lastFile, tag, value);
			}
		}
	}

	/**
	 * Adds a file contributor to the list of contributors for this file
	 * @param file
	 * @param contributor
	 */
	private void addFileContributor(SPDXFile file, String contributor) {
		String[] contributors = file.getContributors();
		if (contributors == null) {
			contributors = new String[] {contributor};
			
		} else {
			contributors = Arrays.copyOf(contributors, contributors.length + 1);
			contributors[contributors.length-1] = contributor;
		}
		file.setContributors(contributors);
	}

	/**
	 * Adds a file dependency to a file
	 * @param file
	 * @param dependentFileName
	 */
	private void addFileDependency(SPDXFile file, String dependentFileName) {
		// Since the files have not all been parsed, we just keep track of the
		// dependencies in a hashmap until we finish all processing and are building the package
		List<SPDXFile> filesWithThisAsADependency = this.fileDependencyMap.get(dependentFileName);
		if (filesWithThisAsADependency == null) {
			filesWithThisAsADependency = Lists.newArrayList();
			this.fileDependencyMap.put(dependentFileName, filesWithThisAsADependency);
		}
		filesWithThisAsADependency.add(file);
	}

	/**
	 * @param doapProject
	 */
	private void buildProject(SPDXFile file, String tag, String value)
			throws Exception {
		if (tag.equals(constants.getProperty("PROP_PROJECT_NAME"))) {
			lastProject = new DOAPProject(value, null);
			List<DOAPProject> projects = Lists.newArrayList(file.getArtifactOf());
			projects.add(lastProject);
			file.setArtifactOf(projects.toArray(new DOAPProject[0]));			
		} else {
			if (tag.equals(constants.getProperty("PROP_PROJECT_HOMEPAGE"))) {
				if (lastProject == null) {
					throw(new InvalidSpdxTagFileException("Missing Project Name - A project name must be provided before the project properties"));
				}
				lastProject.setHomePage(value);
			} else if (tag.equals(constants.getProperty("PROP_PROJECT_URI"))) {
				if (lastProject == null) {
					throw(new InvalidSpdxTagFileException("Missing Project Name - A project name must be provided before the project properties"));
				}
				// can not set the URI since it is already created, we need to replace DOAP project
				DOAPProject[] existingProjects = file.getArtifactOf();
				int i = 0;
				while (i < existingProjects.length && !existingProjects[i].equals(lastProject)) {
					i++;
				}
				if (i >= existingProjects.length) {
					existingProjects = Arrays.copyOf(existingProjects, existingProjects.length+1);
				}
				existingProjects[i] = new DOAPProject(lastProject.getName(), lastProject.getHomePage());
				existingProjects[i].setUri(value);
				file.setArtifactOf(existingProjects);
				lastProject = existingProjects[i];
			} else {
				throw(new InvalidSpdxTagFileException("Unrecognized tag: "+tag));
			}
		}
	}

	private static String trim(String value) {
		value.trim();
		value = value.replaceAll("<text>", "").replaceAll("</text>", "")
				.replaceAll("SHA1: ", "");
		return value;
	}

	@Override
    public void exit() throws Exception {
		fixFileDependencies();
		List<String> warningMessages = analysis.verify();
		assertEquals("SPDXDocument", 0, warningMessages);
	}
	
	/**
	 * Go through all of the file dependencies and add them to the file
	 * @throws InvalidSPDXAnalysisException 
	 */
	private void fixFileDependencies() throws InvalidSPDXAnalysisException {
		// be prepared - it is complicate to make this efficient
		// the HashMap fileDependencyMap contains a map from a file name to all SPDX files which
		// reference that file name as a dependency
		// This method goes through all of the files in the analysis in a single pass and creates
		// a new HashMap of files (as the key) and the dependency files (arraylist) as the values
		// Once that hashmap is built, the actual dependencies are then added.
		// the key contains an SPDX file with one or more dependencies.  The value is the array list of file dependencies
		Map<SPDXFile, List<SPDXFile>> filesWithDependencies = Maps.newHashMap();
		SPDXFile[] allFiles = analysis.getFileReferences();
		// fill in the filesWithDependencies map
		for (int i = 0;i < allFiles.length; i++) {
			List<SPDXFile> alFilesHavingThisDependency = this.fileDependencyMap.get(allFiles[i].getName());
			if (alFilesHavingThisDependency != null) {
				for (int j = 0; j < alFilesHavingThisDependency.size(); j++) {
					SPDXFile fileWithDependency = alFilesHavingThisDependency.get(j);
					List<SPDXFile> alDepdenciesForThisFile = filesWithDependencies.get(fileWithDependency);
					if (alDepdenciesForThisFile == null) {
						alDepdenciesForThisFile = Lists.newArrayList();
						filesWithDependencies.put(fileWithDependency, alDepdenciesForThisFile);
					}
					alDepdenciesForThisFile.add(allFiles[i]);
				}
				// remove from the file dependency map so we can keep track of any files which did
				// not match at the end
				this.fileDependencyMap.remove(allFiles[i].getName());
			}
		}
		// Go through the hashmap we just created and add the dependent files
		Iterator<Entry<SPDXFile, List<SPDXFile>>> iter = filesWithDependencies.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<SPDXFile, List<SPDXFile>> entry = iter.next();
			List<SPDXFile> alDependencies = entry.getValue();
			if (alDependencies != null && alDependencies.size() > 0) {
				entry.getKey().setFileDependencies(alDependencies.toArray(new SPDXFile[alDependencies.size()]), this.analysis);
			}
		}
		// Check to see if there are any left over and and throw an error if the dependent files were
		// not found
		Set<String> missingDependencies = this.fileDependencyMap.keySet();
		if (missingDependencies != null && missingDependencies.size() > 0) {
			System.out.println("The following file names were listed as file dependencies but were not found in the list of files:");
			Iterator<String> missingIter = missingDependencies.iterator();
			while(missingIter.hasNext()) {
				System.out.println("\t"+missingIter.next());
			}
		}
		
	}

	
	private static void assertEquals(String name, int expected,
			List<String> verify) {
		if (verify.size() > expected) {
			System.out.println("The following verifications failed for the " + name + ":");
			for (int x = 0; x < verify.size(); x++) {
				System.out.println("\t" + verify.get(x));
			}
		}
	}
}
