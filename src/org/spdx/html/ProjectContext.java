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
package org.spdx.html;

import org.spdx.rdfparser.DOAPProject;

/**
 * Context for a DOAP Project reference
 * @author Gary O'Neall
 *
 */
public class ProjectContext {

	Exception error = null;
	DOAPProject project = null;
	
	/**
	 * @param error
	 */
	public ProjectContext(Exception error) {
		this.error = error;
	}

	/**
	 * @param doapProject
	 */
	public ProjectContext(DOAPProject doapProject) {
		this.project = doapProject;
	}
	
	public String name() {
		if (this.project == null && this.error != null) {
			return "Error getting project information: "+error.getMessage();
		}
		if (this.project != null) {
			return this.project.getName();
		} else {
			return null;
		}
	}
	
	public String homepage() {
		if (this.project == null && this.error != null) {
			return "Error getting project information: "+error.getMessage();
		}
		if (this.project != null) {
			return this.project.getHomePage();
		} else {
			return null;
		}
	}
	
	public String ArtifactOfProjectURI() {
		if (this.project == null && this.error != null) {
			return "Error getting project information: "+error.getMessage();
		}
		if (this.project != null) {
			return this.project.getProjectUri();
		} else {
			return null;
		}
	}

}
