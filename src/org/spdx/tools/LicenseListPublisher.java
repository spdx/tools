/**
 * Copyright (c) 2016 Source Auditor Inc.
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
package org.spdx.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * Publishes a new version of the license list.
 * 
 * The license list data is taken from the github repository spdx/license-list-XML,
 * https://github.com/spdx/license-list-XML
 * 
 * The first parameter is the release ID for the license list.  The license-list-XML
 * tag MUST contain a tag in the master branch matching the the release ID.
 * 
 * The 2nd and 3rd parameters are the username and passwords for github.
 * 
 * If there are no errors, the license data is published to the github repository
 * spdx/license-list-data, https://github.com/spdx/license-list-data
 * 
 * The output is tagged by release
 * 
 * To publish the license list to spdx.org/licenses, the following steps should be
 * performed on the linux server hosting the spdx.org/license website (currently phpphpweb1.linux-foundation.org):
 * 1.	Create a new subdirectory in the ~/licenseArchive directory with the format mm-dd-yyyy where mm is the month, dd is the day, and yyyy 
 * 		is the year the files were uploaded.
 * 2.	Upload the files from the spdx/license-list-data github repository website folder from the correct tag to a the folder created in step 1
 * 3.	Backup the current files by replacing the files in the ~/backup folder with the files in ~/www/spdx/content/licenses.
 * 		IMPORTANT NOTE: Do NOT do a recursive copy, only copy the files and do NOT copy any subdirectories.
 * 4.	Create a new subdirectory ~/www/spdx/content/licenses/archive/archived_ll_vx.xx where x.xx is the version of the PREVIOUSLY PUBLISHED 
 * 		license list being replaced.
 * 5.	Copy the files from ~/backup to the subdirectory created in step 4
 * 6.	Edit the file ~/www/spdx/content/licenses/archive/archived_ll_vx.xx/index.html.  Add the line
 * 		"<p style="color: #FA0207;"><strong>THIS IS NOT THE CURRENT VERSION OF THE SPDX LICENSE LIST.  
 * 		PLEASE USE THE CURRENT VERSION, LOCATED AT: <a href="http://spdx.org/licenses/">http://spdx.org/licenses/</a>"
 * 		immediately prior to the line "<h1>SPDX License List</h1>"
 * 7.	Copy the files from the subdirectory created in steps 1 and 2 to ~/www/spdx/content/licenses
 * 8.	If there are any problems, copy the files from the backup back to ~/www/spdx/content/licenses
 */
public class LicenseListPublisher {
	
	static final int ERROR_STATUS = 1;
	private static final String LICENSE_XML_URI = "https://github.com/goneall/license-list-XML.git";
	//TODO: set the real URL once testing is done
//	private static final String LICENSE_XML_URI = "https://github.com/spdx/license-list-XML.git";
	private static final String LICENSE_DATA_URI = "https://github.com/goneall/license-list-data.git";
	//TODO: Set the real URL once testing is done
//	private static final String LICENSE_DATA_URI = "https://github.com/spdx/license-list-data.git";
	/**
	 * @param args Single argument - release name for the license list
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Invalid number of arguments.  Expected 3 arguments");			
			usage();
			System.exit(ERROR_STATUS);
		}
		boolean ignoreWarnings = false;
		String release = null;
		String gitUserName = null;
		String gitPassword = null;
		//TODO put the git username and password in the properties or preferences file
		if ("--ignoreWarnings".equals(args[0])) {
			if (args.length != 4) {
				System.out.println("Invalid number of arguments.  Expected 3 arguments");			
				usage();
				System.exit(ERROR_STATUS);
			}
			ignoreWarnings = true;
			release = args[1];
			gitUserName = args[2];
			gitPassword = args[3];
		} else {
			if (args.length != 3) {
				System.out.println("Invalid number of arguments.  Expected 3 arguments");			
				usage();
				System.exit(ERROR_STATUS);
			}
			release = args[0];
			gitUserName = args[1];
			gitPassword = args[2];
		}

		try {
			publishLicenseList(release, gitUserName, gitPassword, ignoreWarnings);
			System.out.println("Version "+release+" published to spdx/license-list-data");
		} catch (LicensePublisherException e) {
			System.out.println(e.getMessage());
			System.exit(ERROR_STATUS);
		} catch (LicenseGeneratorException e) {
			System.out.println(e.getMessage());
			System.exit(ERROR_STATUS);
		}
	}

	/**
	 * Publish a license list to the license data git repository
	 * @param release license list release name (must be associatd with a tag in the license-list-xml repo)
	 * @param gitUserName github username to be used - must have commit access to the license-xml-data repo
	 * @param gitPassword github password
	 * @throws LicensePublisherException 
	 * @throws LicenseGeneratorException 
	 */
	private static void publishLicenseList(String release, String gitUserName,
			String gitPassword, boolean ignoreWarnings) throws LicensePublisherException, LicenseGeneratorException {
		CredentialsProvider githubCredentials = new UsernamePasswordCredentialsProvider(gitUserName, gitPassword);
		File licenseXmlDir = null;
		File licenseDataDir = null;
		Git licenseXmlGit = null;
		Git licenseDataGit = null;
		try {
			licenseXmlDir = Files.createTempDirectory("LicenseXML").toFile();
			System.out.println("Cloning the license XML repository - this could take a while...");
			licenseXmlGit = Git.cloneRepository()
					.setCredentialsProvider(githubCredentials)
					.setDirectory(licenseXmlDir)
					.setURI(LICENSE_XML_URI)
					.call();
			Ref releaseTag = licenseXmlGit.getRepository().getTags().get(release);
			if (releaseTag == null) {
				throw new LicensePublisherException("Release "+release+" not found as a tag in the License List XML repository");
			}
			licenseXmlGit.checkout().setName(releaseTag.getName()).call();
			licenseDataDir = Files.createTempDirectory("LicenseData").toFile();
			System.out.println("Cloning the license data repository - this could take a while...");
			licenseDataGit = Git.cloneRepository()
					.setCredentialsProvider(githubCredentials)
					.setDirectory(licenseDataDir)
					.setURI(LICENSE_DATA_URI)
					.call();
			Ref dataReleaseTag = licenseDataGit.getRepository().getTags().get(release);
			boolean dataReleaseTagExists = false;
			if (dataReleaseTag != null) {
				dataReleaseTagExists = true;
				licenseDataGit.checkout().setName(releaseTag.getName()).call();
			}
			cleanLicenseDataDir(licenseDataDir);
			String todayDate = new SimpleDateFormat("dd-MMM-yyyy").format(Calendar.getInstance().getTime());
			List<String> warnings = LicenseRDFAGenerator.generateLicenseData(new File(licenseXmlDir.getPath() + File.separator + "src"),
												licenseDataDir, release, todayDate);
			if (warnings.size() > 0 && !ignoreWarnings) {
				throw new LicensePublisherException("There are some skipped or invalid license input data.  Publishing aborted.  To ignore, add the --ignore option as the first parameter");
			}
			licenseDataGit.add().addFilepattern(".").call();
			licenseDataGit.commit()
					.setAll(true)
					.setCommitter("SPDX License List Publisher", "spdx-tech@lists.spdx.org")
					.setMessage("License List Publisher for "+gitUserName+".  License list version "+release)
					.call();
			if (!dataReleaseTagExists) {
				licenseDataGit.tag().setName(release).setMessage("SPDX License List release "+release).call();
			}
			licenseDataGit.push().setCredentialsProvider(githubCredentials).setPushTags().call();
		} catch (IOException e) {
			throw new LicensePublisherException("I/O Error publishing license list",e);
		} catch (InvalidRemoteException e) {
			throw new LicensePublisherException("Invalid remote error trying to access the git repositories",e);
		} catch (TransportException e) {
			throw new LicensePublisherException("Transport error trying to access the git repositories",e);
		} catch (GitAPIException e) {
			throw new LicensePublisherException("GIT API error trying to access the git repositories",e);
		} finally {
			if (licenseXmlGit != null) {
				licenseXmlGit.close();
			}
			if (licenseDataGit != null) {
				licenseDataGit.close();
			}
			if (licenseXmlDir != null) {
				deleteDir(licenseXmlDir);
			}
			if (licenseDataDir != null) {
				deleteDir(licenseDataDir);
			}
		}
	}

	/**
	 * @param licenseDataDir
	 */
	private static void cleanLicenseDataDir(File licenseDataDir) {
		// Leave the files at the top level and delete files underneath leaving the directories
		File[] children = licenseDataDir.listFiles();
		if (children != null) {
			for (File child:children) {
				if (child != null && child.isDirectory()) {
					deleteOnlyFiles(child);
				}
			}
		}
	}

	/**
	 * Delete only the files, not the directories
	 * @param child
	 */
	private static void deleteOnlyFiles(File file) {
		if (file.getName().equals(".git")) {
			return;
		}
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			if (children != null) {
				for (File child:children) {
					if (child != null) {
						if (child.isDirectory()) {
							deleteOnlyFiles(child);
						} else {
							child.delete();
						}
					}
				}
			}
		} else {
			file.delete();
		}
	}

	/**
	 * Delete a directory and the directory contents
	 * @param dir
	 */
	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			if (children != null) {
				for (File child:children) {
					if (child != null && !deleteDir(child)) {
						return false;
					}
				}
			}
		}
		return dir.delete();
	}

	/**
	 * Print usage
	 */
	private static void usage() {
		System.out.println("Usage:");
		System.out.println("LicenseListPublisher [--ignoreWarnings] release gitusername gitpassword");
		System.out.println("where release is the release name of the SPDX License List.  The release must exist as a tag in the spdx/license-list-xml git repository");
		System.out.println("gituser must have read access to the spdx/license-list-xml repo and read/write access to the spdx/license-list-data repo");
		System.out.println("The optional --ignoreWarnings will publish even if there are warnings generated.  This must be the first parameter.");
	}

}
