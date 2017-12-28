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
import java.io.StringReader;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import au.com.bytecode.opencsv.CSVReader;
/**
 * Publishes a new version of the license list.
 * 
 * See the createOptions method for a description of the options used, or execute the command without parameters to get a list of the parameters from the command line
 * 
 * The license list data is taken from the github repository spdx/license-list-XML,
 * https://github.com/spdx/license-list-XML
 * 
 * The first parameter is the release ID for the license list.  The license-list-XML
 * tag MUST contain a tag in the master branch matching the the release ID.
 * 
 * The 2nd and 3rd parameters are the username and passwords for github.
 * 
 * If there are no errors or warnings, the license data is published to the github repository
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
	
	static final Comparator<String> versionComparer = new Comparator<String>() {

		Pattern versionPattern = Pattern.compile("(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)$");
		@Override
		public int compare(String arg0, String arg1) {
			Matcher matcher0 = versionPattern.matcher(arg0);
			Matcher matcher1 = versionPattern.matcher(arg1);
			if (!matcher0.find()) {
				if (!matcher1.find()) {
					return 0;
				} else {
					return -1;
				}
			}
			if (!matcher1.find()) {
				return 1;
			}
			String version0 = matcher0.group(0);
			String version1 = matcher1.group(0);
			return version0.compareTo(version1);
		}
		
	};
	
	static final Logger logger = LoggerFactory.getLogger(LicenseListPublisher.class);
	
	static final int ERROR_STATUS = 1;
	//TODO: set the real URL once testing is done
//	private static final String LICENSE_XML_URI = "https://github.com/spdx/license-list-XML.git";
	private static final String LICENSE_XML_URI = "https://github.com/goneall/license-list-XML.git";
	//TODO: set the real URL once testing is done
//	private static final String LICENSE_XML_URI = "https://github.com/spdx/license-list-XML.git";
	private static final String LICENSE_DATA_URI = "https://github.com/goneall/license-list-data.git";

	private static final String TEST_DIRECTORY_PATH = "test/original";
	//TODO: Set the real URL once testing is done
//	private static final String LICENSE_DATA_URI = "https://github.com/spdx/license-list-data.git";
	/**
	 * @param args Single argument - release name for the license list
	 */
	public static void main(String[] args) {
		Options options = createOptions();
		if (args.length == 1 && "-h".equals(args[0])) {
			usage(options);
			System.exit(0);
		}
		CommandLineParser parser = new DefaultParser();
		CommandLine cmdLine = null;
		try {
			cmdLine = parser.parse(options, args);
		} catch (ParseException e1) {
			System.out.println(e1.getMessage());			
			usage(options);
			System.exit(ERROR_STATUS);
		}
		if (cmdLine.hasOption("h")) {
			usage(options);
			System.exit(0);
		}
		boolean testOnly = cmdLine.hasOption("t");
		String outputRepository = cmdLine.getOptionValue("O", LICENSE_DATA_URI);
		boolean ignoreWarnings = cmdLine.hasOption("I");
		String[] ignoredWarnings = new String[0];
		if (cmdLine.hasOption("w")) {
			CSVReader reader = null;
			try {
				reader = new CSVReader(new StringReader(cmdLine.getOptionValue("w").trim()));
				ignoredWarnings = reader.readNext();
			} catch (IOException e) {
				System.out.println("IO Error reading ignored warnings: "+e.getMessage());
				System.exit(ERROR_STATUS);
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					System.out.println("IO Error closing ignored warnings string: "+e.getMessage());
					System.exit(ERROR_STATUS);
				}
			}
		}
		
		String release = null;
		if (cmdLine.hasOption("r")) {
			release = cmdLine.getOptionValue("r");
		}
		String gitUserName = cmdLine.getOptionValue("u");
		String gitPassword = cmdLine.getOptionValue("p");
		String licenseXmlGitUri = cmdLine.getOptionValue("x",LICENSE_XML_URI);
		try {
			CredentialsProvider githubCredentials = new UsernamePasswordCredentialsProvider(gitUserName, gitPassword);
			if (cmdLine.hasOption("d")) {
				File licenseXmlDir = new File(cmdLine.getOptionValue("d").trim());
				if (!licenseXmlDir.exists()) {
					System.out.println("License XML directory "+cmdLine.getOptionValue("d")+ " does not exist.");
					usage(options);
				}
				if (!licenseXmlDir.isDirectory()) {
					System.out.println("License XML directory "+cmdLine.getOptionValue("d")+ " is not a directory.");
					usage(options);
				}
				publishLicenseList(licenseXmlDir, release, githubCredentials, ignoreWarnings, 
						ignoredWarnings, outputRepository, testOnly);
			} else {
				publishLicenseList(licenseXmlGitUri, release, githubCredentials, ignoreWarnings, ignoredWarnings, 
						outputRepository, testOnly);
			}			
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
	 * @return Options for the LicenseListPublish command
	 */
	private static Options createOptions() {
		Options retval = new Options();
		retval.addOption(Option.builder("O")
				.longOpt("outputrepo")
				.desc("Git repository to output the license list data to.  The git user must have update access to this repository")
				.hasArg(true)
				.required(false)
				.build()
				);
		retval.addOption(Option.builder("I")
				.longOpt("ignoreAllWarnings")
				.desc("Ignore all warnings")
				.required(false)
				.build()
				);
		retval.addOption(Option.builder("w")
				.longOpt("ignoreWarnings")
				.desc("Ignore specific warning messages")
				.hasArg(true)
				.required(false)
				.build());
		retval.addOption(Option.builder("u")
				.longOpt("user")
				.desc("Github Username")
				.hasArg(true)
				.required(true)
				.build());
		retval.addOption(Option.builder("p")
				.longOpt("password")
				.desc("Github password")
				.hasArg(true)
				.required(true)
				.build());
		retval.addOption(Option.builder("d")
				.longOpt("directory")
				.desc("Input XML directory")
				.hasArg(true)
				.required(false)
				.build());
		retval.addOption(Option.builder("x")
				.longOpt("xmlrepo")
				.desc("Input license XML repository")
				.hasArg(true)
				.required(false)
				.build());
		retval.addOption(Option.builder("r")
				.longOpt("release")
				.desc("License list release tag or version")
				.required(false)
				.hasArg(true)
				.build());
		retval.addOption(Option.builder("h")
				.longOpt("help")
				.desc("Prints out this message")
				.required(false)
				.hasArg(false)
				.build());
		retval.addOption(Option.builder("t")
				.longOpt("testOnly")
				.desc("Only tests the license XML files - does not update or publish the results")
				.required(false)
				.hasArg(false)
				.build());
		return retval;
	}

	/**
	 * Publish a license list to the license data git repository
	 * @param release license list release name (must be associatd with a tag in the license-list-xml repo)
	 * @param githubCredentials Credential for the license XML git repository
	 * @param ignoredWarnings 
	 * @param outputRepository GIT Repository to output the files to
	 * @param testOnly If true, only test the license XML and do not update the files in the output repository
	 * @throws LicensePublisherException 
	 * @throws LicenseGeneratorException 
	 */
	private static void publishLicenseList(String licenseXmlGithubUri, String release, CredentialsProvider githubCredentials,
			boolean ignoreWarnings, String[] ignoredWarnings, String outputRepository, boolean testOnly) throws LicensePublisherException, LicenseGeneratorException {
		File licenseXmlDir = null;
		Git licenseXmlGit = null;
		try {
			licenseXmlDir = Files.createTempDirectory("LicenseXML").toFile();
			System.out.println("Cloning the license XML repository - this could take a while...");
			licenseXmlGit = Git.cloneRepository()
					.setCredentialsProvider(githubCredentials)
					.setDirectory(licenseXmlDir)
					.setURI(licenseXmlGithubUri)
					.call();
			if (release != null) {
				Ref releaseTag = licenseXmlGit.getRepository().getTags().get(release);
				if (releaseTag == null) {
					throw new LicensePublisherException("Release "+release+" not found as a tag in the License List XML repository");
				}
				licenseXmlGit.checkout().setName(releaseTag.getName()).call();
			}			
			publishLicenseList(licenseXmlDir, release, githubCredentials, ignoreWarnings, ignoredWarnings, 
					outputRepository, testOnly);
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
			if (licenseXmlDir != null) {
				deleteDir(licenseXmlDir);
			}
		}
	}
	/**
	 * Publish a license list to the license data git repository
	 * @param release license list release name (must be associatd with a tag in the license-list-xml repo)
	 * @param sourceDirectory Directory containing the source XML files
	 * @param githubCredentials Credential for the output git repository
	 * @param ignoredWarnings 
	 * @param outputRepository URL to the GIT Repository to output the files to
	 * @param testOnly If true, only test the license XML and do not update the files in the output repository
	 * @throws LicensePublisherException 
	 * @throws LicenseGeneratorException 
	 */
	private static void publishLicenseList(File sourceDirectory, String release, CredentialsProvider githubCredentials,
			boolean ignoreWarnings, String[] ignoredWarnings, String outputRepository, boolean testOnly) throws LicensePublisherException, LicenseGeneratorException {		
		File licenseTestDir = new File(sourceDirectory.getAbsolutePath() + File.separator + TEST_DIRECTORY_PATH);
		File licenseDataDir = null;
		Git licenseDataGit = null;
		boolean dataReleaseTagExists = false;
		try {
			licenseDataDir = Files.createTempDirectory("LicenseData").toFile();
			if (!testOnly) {
				System.out.println("Cloning the license data repository - this could take a while...");
				licenseDataGit = Git.cloneRepository()
						.setCredentialsProvider(githubCredentials)
						.setDirectory(licenseDataDir)
						.setURI(outputRepository)
						.call();
				if (release != null) {
					Ref dataReleaseTag = licenseDataGit.getRepository().getTags().get(release);				
					if (dataReleaseTag != null) {
						dataReleaseTagExists = true;
						licenseDataGit.checkout().setName(dataReleaseTag.getName()).call();
					}
				}
			}
			cleanLicenseDataDir(licenseDataDir);
			String todayDate = new SimpleDateFormat("dd-MMM-yyyy").format(Calendar.getInstance().getTime());
			String version = null;
			if (release != null) {
				version = release;
			} else {
				version = getVersionFromGitTag(sourceDirectory);
			}
			List<String> warnings = LicenseRDFAGenerator.generateLicenseData(new File(sourceDirectory.getPath() + File.separator + "src"),
												licenseDataDir, version, todayDate, licenseTestDir);
			if (warnings.size() > 0 && !ignoreWarnings) {
				List<String> nonIgnoredWarnings = Lists.newArrayList();
				for (String warning:warnings) {
					boolean ignore = false;
					for (String ignoredWarning:ignoredWarnings) {
						if (warning.equalsIgnoreCase(ignoredWarning)) {
							ignore = true;
							break;
						}
					}
					if (!ignore) {
						nonIgnoredWarnings.add(warning);
					}
				}
				if (nonIgnoredWarnings.size() > 0) {
					StringBuilder errorMsg = new StringBuilder("The following errors or warnings occured while processing the license input data:\n");
					for (String warning:nonIgnoredWarnings) {
						errorMsg.append(warning);
						errorMsg.append("\n");
					}
					throw new LicensePublisherException(errorMsg.toString());
				}
			}
			if (!testOnly) {
				licenseDataGit.add().addFilepattern(".").call();
				String commitMsg = "Auotomated License List Publisher.";
				if (release != null) {
					commitMsg += "  License List Version "+release;
				} else if (version != null) {
					commitMsg += " for license list tag/commit "+version;
				}
				licenseDataGit.commit()
						.setAll(true)
						.setCommitter("SPDX License List Publisher", "spdx-tech@lists.spdx.org")
						.setMessage(commitMsg)
						.call();
				if (!dataReleaseTagExists && release != null) {
					licenseDataGit.tag().setName(release).setMessage("SPDX License List release "+release).call();
				}
				licenseDataGit.push().setCredentialsProvider(githubCredentials).setPushTags().call();
			}
		} catch (IOException e) {
			throw new LicensePublisherException("I/O Error publishing license list",e);
		} catch (InvalidRemoteException e) {
			throw new LicensePublisherException("Invalid remote error trying to access the git repositories",e);
		} catch (TransportException e) {
			throw new LicensePublisherException("Transport error trying to access the git repositories",e);
		} catch (GitAPIException e) {
			throw new LicensePublisherException("GIT API error trying to access the git repositories",e);
		} finally {
			if (licenseDataGit != null) {
				licenseDataGit.close();
			}
			if (licenseDataDir != null) {
				deleteDir(licenseDataDir);
			}
			if (licenseTestDir != null) {
				deleteDir(licenseTestDir);
			}
		}
	}

	private static String getVersionFromGitTag(File sourceDirectory) throws IOException, GitAPIException {
		// Search all the commits for all the tags to find the one that matches head
		// Modeled after jgit cookbook https://github.com/centic9/jgit-cookbook/blob/master/src/main/java/org/dstadler/jgit/porcelain/ListTags.java
		FileRepositoryBuilder builder = new FileRepositoryBuilder().readEnvironment().findGitDir(sourceDirectory);
		if (builder == null) {
			// no git repository was found
			return "UnknownVersion";
		}
		try (Repository repository = builder.build()) {
			try (Git git = new Git(repository)) {
				Ref head = repository.findRef("head");
				ObjectId headObjectId = head.getObjectId();
				List<Ref> tagRefs = git.tagList().call();
				String latestRelease = "";
				for (Ref tagRef:tagRefs) {
					if (versionComparer.compare(tagRef.getName(),latestRelease) > 0) {
						latestRelease = tagRef.getName();
					}
					LogCommand log = git.log();
					Ref peeledTagRef = repository.peel(tagRef);
					if(peeledTagRef.getPeeledObjectId() != null) {
                    	log.add(peeledTagRef.getPeeledObjectId());
                    } else {
                    	log.add(tagRef.getObjectId());
                    }
					Iterable<RevCommit> logs = log.call();
        			for (RevCommit rev : logs) {
        				ObjectId revObjectId = rev.getId();
        				if (revObjectId.equals(headObjectId)) {
        					String releaseName;
        					if (tagRef.getName().startsWith("refs/tags/")) {
        						releaseName = tagRef.getName().substring("refs/tags/".length());
        					} else {
        						releaseName = tagRef.getName();
        					}
        					return releaseName;
        				}
        			}
				}
				// Did find a matching tag if we got here, assume that we are on the latest most recent release
				if (latestRelease.isEmpty()) {
					return "Unknown";	// We could not find a tag associated with the head branch
				} else {
					String releaseName;
					if (latestRelease.startsWith("refs/tags/")) {
						releaseName = latestRelease.substring("refs/tags/".length());
					} else {
						releaseName = latestRelease;
					}
					String qualifier = headObjectId.getName().substring(headObjectId.getName().length()-7);
					return releaseName + "-" + qualifier;
				}
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
							if (!child.delete()) {
								logger.warn("Unable to delete file "+child.getName());
							}
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
	private static void usage(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("LicenseListPublisher", options);
	}
}
