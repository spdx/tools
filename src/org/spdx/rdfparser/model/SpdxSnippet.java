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
package org.spdx.rdfparser.model;

import java.util.List;
import java.util.Map;

import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.model.pointer.ByteOffsetPointer;
import org.spdx.rdfparser.model.pointer.LineCharPointer;
import org.spdx.rdfparser.model.pointer.StartEndPointer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * @author Gary O'Neall
 *
 */
public class SpdxSnippet extends SpdxItem implements Comparable<SpdxSnippet> {

	private SpdxFile snippetFromFile;
	private StartEndPointer byteRange;
	private StartEndPointer lineRange;

	/**
	 * @param name Identify a specific snippet in a human convenient manner
	 * @param comment This field provides a place for the SPDX document creator to record any general
comments about the snippet.
	 * @param annotations
	 * @param relationships
	 * @param licenseConcluded This field contains the license the SPDX file creator has concluded as governing
the snippet or alternative values if the governing license cannot be determined
	 * @param licenseInfoInFile
	 * @param copyrightText Identify the copyright holder of the snippet, as well as any dates present.
	 * @param licenseComment This field provides a place for the SPDX document creator to record any relevant
background references or analysis that went in to arriving at the Concluded License for a snippet
	 * @param snippetFromFile Uniquely identify the file in an SPDX document which this snippet is associated
	 * @param byteRange This field defines the byte range in the original host file (in X.2) that the snippet
information applies to.
	 * @param lineRange This optional field defines the line range in the original host file (in X.2) that the
snippet information applies to.
	 */
	public SpdxSnippet(String name, String comment, Annotation[] annotations,
			Relationship[] relationships, AnyLicenseInfo licenseConcluded,
			AnyLicenseInfo[] licenseInfoInFile, String copyrightText,
			String licenseComment, SpdxFile snippetFromFile, StartEndPointer byteRange,
			StartEndPointer lineRange) {
		super(name, comment, annotations, relationships, licenseConcluded,
				licenseInfoInFile, copyrightText, licenseComment);
		this.snippetFromFile = snippetFromFile;
		this.byteRange = byteRange;
		this.lineRange = lineRange;
	}

	/**
	 * @param modelContainer
	 * @param node
	 * @throws InvalidSPDXAnalysisException
	 */
	public SpdxSnippet(IModelContainer modelContainer, Node node)
			throws InvalidSPDXAnalysisException {
		super(modelContainer, node);
		getMyPropertiesFromModel();
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getPropertiesFromModel()
	 */
	@Override
	public void getPropertiesFromModel() throws InvalidSPDXAnalysisException {
		super.getPropertiesFromModel();
		getMyPropertiesFromModel();
	}

	private void getSnippetFromFileFromModel() throws InvalidSPDXAnalysisException {
		SpdxElement snippetFromElement = findElementPropertyValue(
				SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_SNIPPET_FROM_FILE);
		if (snippetFromElement == null) {
			this.snippetFromFile = null;
		} else {
			if (!(snippetFromElement instanceof SpdxFile)) {
				logger.error("Model contains an SPDX element which is not of type SpdxFile for the snippetFromFile: "+
						snippetFromElement.getClass().toString());
				throw new InvalidSPDXAnalysisException("Incorect type for the SpdxFile associated with a snippet.");
			}
			this.snippetFromFile = (SpdxFile)snippetFromElement;
		}
	}

	void getMyPropertiesFromModel() throws InvalidSPDXAnalysisException {
		getSnippetFromFileFromModel();
		getRangesFromModel();
	}

	/**
	 * Load the byte and line range properties from the model
	 * @throws InvalidSPDXAnalysisException
	 */
	private void getRangesFromModel() throws InvalidSPDXAnalysisException {
		StartEndPointer[] allPointers = findStartEndPointerPropertyValues(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_SNIPPET_RANGE);
		this.byteRange = null;
		this.lineRange = null;
		for (StartEndPointer sep:allPointers) {
			if (sep.getStartPointer() instanceof ByteOffsetPointer) {
				if (!(sep.getEndPointer() instanceof ByteOffsetPointer)) {
					logger.error("Incompatable start and end pointer types - must both be offset or line types");
					throw new InvalidSPDXAnalysisException("Incompatable start and end snippet specification - mixing byte and line ranges");
				}
				if (this.byteRange != null) {
					logger.error("More than one byte offset snippet range specified");
					throw new InvalidSPDXAnalysisException("More than one byte offset snippet range specified");
				}
				this.byteRange = sep;
			} else if (sep.getStartPointer() instanceof LineCharPointer) {
				if (!(sep.getEndPointer() instanceof LineCharPointer)) {
					logger.error("Incompatable start and end pointer types - must both be offset or line types");
					throw new InvalidSPDXAnalysisException("Incompatable start and end snippet specification - mixing byte and line ranges");
				}
				if (this.lineRange != null) {
					logger.error("More than one byte offset snippet range specified");
					throw new InvalidSPDXAnalysisException("More than one byte offset snippet range specified");
				}
				this.lineRange = sep;
			}
		}
	}

	@Override
	public Resource findDuplicateResource(IModelContainer modelContainer, String uri) throws InvalidSPDXAnalysisException {
		if (this.snippetFromFile == null) {
			return null;
		}
		if (this.byteRange == null) {
			return null;
		}
		Resource snippetFromFileResource = SpdxFile.findFileResource(modelContainer, this.snippetFromFile);
		if (snippetFromFileResource == null) {
			return null;
		}
		Model model = modelContainer.getModel();
		Node snippetFromFileProperty = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_SNIPPET_FROM_FILE).asNode();
		Triple fileMatch = Triple.createMatch(null, snippetFromFileProperty, snippetFromFileResource.asNode());

		ExtendedIterator<Triple> fileMatchIter = model.getGraph().find(fileMatch);
		while (fileMatchIter.hasNext()) {
			Triple fileMatchTriple = fileMatchIter.next();
			SpdxSnippet localSnippet = new SpdxSnippet(modelContainer, fileMatchTriple.getSubject());
			if (this.byteRange.equivalent(localSnippet.getByteRange())) {
				return model.asRDFNode(fileMatchTriple.getSubject()).asResource();
			}
		}
		return null;
	}

	@Override
	public String getLicenseInfoFromFilesPropertyName() {
		return SpdxRdfConstants.PROP_LICENSE_INFO_FROM_SNIPPETS;
	}


	/**
	 * @return the snippetFromFile
	 * @throws InvalidSPDXAnalysisException
	 */
	public SpdxFile getSnippetFromFile() throws InvalidSPDXAnalysisException {
		if (this.resource != null && this.refreshOnGet) {
			getSnippetFromFileFromModel();
		}
		return snippetFromFile;
	}

	/**
	 * @param snippetFromFile the snippetFromFile to set
	 * @throws InvalidSPDXAnalysisException
	 */
	public void setSnippetFromFile(SpdxFile snippetFromFile) throws InvalidSPDXAnalysisException {
		this.snippetFromFile = snippetFromFile;
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_SNIPPET_FROM_FILE, snippetFromFile);
		// Update the references in the ranges
		if (this.byteRange != null) {
			if (this.byteRange.getStartPointer() != null) {
				this.byteRange.getStartPointer().setReference(snippetFromFile);
			}
			if (this.byteRange.getEndPointer() != null) {
				this.byteRange.getEndPointer().setReference(snippetFromFile);
			}
		}
		if (this.lineRange != null) {
			if (this.lineRange.getStartPointer() != null) {
				this.lineRange.getStartPointer().setReference(snippetFromFile);
			}
			if (this.lineRange.getEndPointer() != null) {
				this.lineRange.getEndPointer().setReference(snippetFromFile);
			}
		}
	}

	/**
	 * @return the byteRange
	 * @throws InvalidSPDXAnalysisException
	 */
	public StartEndPointer getByteRange() throws InvalidSPDXAnalysisException {
		if (this.resource != null && this.refreshOnGet) {
			getRangesFromModel();
		}
		return byteRange;
	}

	/**
	 * @param byteRange the byteRange to set
	 * @throws InvalidSPDXAnalysisException
	 */
	public void setByteRange(StartEndPointer byteRange) throws InvalidSPDXAnalysisException {
		if (!(byteRange.getStartPointer() instanceof ByteOffsetPointer)) {
			logger.error("Invalid start pointer type for byte offset range.  Must be ByteOffsetPointer");
			throw new InvalidSPDXAnalysisException("Invalid start pointer type for byte offset range.  Must be ByteOffsetPointer");
		}
		if (!(byteRange.getEndPointer() instanceof ByteOffsetPointer)) {
			logger.error("Invalid end pointer type for byte offset range.  Must be ByteOffsetPointer");
			throw new InvalidSPDXAnalysisException("Invalid end pointer type for byte offset range.  Must be ByteOffsetPointer");
		}
		this.byteRange = byteRange;
		setRangesInModel();
	}

	/**
	 * Sets all of the line ranges in the model
	 * @throws InvalidSPDXAnalysisException
	 */
	private void setRangesInModel() throws InvalidSPDXAnalysisException {
		List<StartEndPointer> allRanges = Lists.newArrayList();
		if (this.byteRange != null) {
			allRanges.add(byteRange);
		}
		if (this.lineRange != null) {
			allRanges.add(lineRange);
		}
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_SNIPPET_RANGE, allRanges.toArray(new StartEndPointer[allRanges.size()]));
	}

	/**
	 * @return the lineRange
	 * @throws InvalidSPDXAnalysisException
	 */
	public StartEndPointer getLineRange() throws InvalidSPDXAnalysisException {
		if (this.resource != null && this.refreshOnGet) {
			getRangesFromModel();
		}
		return lineRange;
	}

	/**
	 * @param lineRange the lineRange to set
	 * @throws InvalidSPDXAnalysisException
	 */
	public void setLineRange(StartEndPointer lineRange) throws InvalidSPDXAnalysisException {
		if (!(lineRange.getStartPointer() instanceof LineCharPointer)) {
			logger.error("Invalid start pointer type for line range.  Must be LineCharPointer");
			throw new InvalidSPDXAnalysisException("Invalid start pointer type for line range.  Must be LineCharPointer");
		}
		if (!(lineRange.getEndPointer() instanceof LineCharPointer)) {
			logger.error("Invalid end pointer type for line range.  Must be LineCharPointer");
			throw new InvalidSPDXAnalysisException("Invalid end pointer type for line range.  Must be LineCharPointer");
		}
		this.lineRange = lineRange;
		setRangesInModel();
	}

	@Override
	public void populateModel() throws InvalidSPDXAnalysisException {
		super.populateModel();
		setPropertyValue(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.PROP_SNIPPET_FROM_FILE, snippetFromFile);
		setRangesInModel();
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.RdfModelObject#getType(org.apache.jena.rdf.model.Model)
	 */
	@Override
	public Resource getType(Model model) {
		return model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_SNIPPET);
	}

	@Override
	public boolean equivalent(IRdfModel o) {
		return this.equivalent(o, true);
	}
	@Override
	public boolean equivalent(IRdfModel o, boolean testRelationships) {
		if (!super.equivalent(o, testRelationships)) {
			return false;
		}
		if (!(o instanceof SpdxSnippet)) {
			return false;
		}
		SpdxSnippet comp = (SpdxSnippet)o;
		SpdxFile compSnippetFromFile = null;
		try {
			compSnippetFromFile = comp.getSnippetFromFile();
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Error getting comparison snippet from file",e);
			return false;
		}
		StartEndPointer compByteRange = null;
		try {
			compByteRange = comp.getByteRange();
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Error getting comparison byte range",e);
			return false;
		}
		SpdxFile mySnippetFromFile;
		try {
			mySnippetFromFile = this.getSnippetFromFile();
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Error getting my snippet from file",e);
			return false;
		}
		StartEndPointer myByteRange;
		try {
			myByteRange = this.getByteRange();
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Error getting my byte range",e);
			return false;
		}
		return (equivalentConsideringNull(mySnippetFromFile, compSnippetFromFile)) &&
				(equivalentConsideringNull(myByteRange, compByteRange));
		//Note: We don't compare the line range since the byte range is more precise
	}

	@Override
	public SpdxSnippet clone(Map<String, SpdxElement> clonedElementIds) {
		if (clonedElementIds.containsKey(this.getId())) {
			return (SpdxSnippet)clonedElementIds.get(this.getId());
		}
		SpdxSnippet retval = null;
		retval = new SpdxSnippet(name, comment, cloneAnnotations(),
				null, cloneLicenseConcluded(),
				cloneLicenseInfosFromFiles(), copyrightText,
				licenseComments, cloneSnippetFromFile(clonedElementIds), cloneByteRange(),
				cloneLineRange());
		clonedElementIds.put(this.getId(), retval);
		if(retval != null){
    		try {
    			retval.setRelationships(cloneRelationships(clonedElementIds));
    		} catch (InvalidSPDXAnalysisException e) {
    			logger.error("Unexected error setting relationships during clone",e);
    		}
		}
		return retval;
	}

	/**
	 * @return
	 */
	private StartEndPointer cloneLineRange() {
		if (this.lineRange == null) {
			return null;
		}
		return this.lineRange.clone();
	}

	/**
	 * @param clonedElementIds
	 * @return
	 */
	private SpdxFile cloneSnippetFromFile(Map<String, SpdxElement> clonedElementIds) {
		if (this.snippetFromFile == null) {
			return null;
		}
		return this.snippetFromFile.clone(clonedElementIds);
	}

	/**
	 * @return
	 */
	private StartEndPointer cloneByteRange() {
		if (this.byteRange == null) {
			return null;
		}
		return this.byteRange.clone();
	}

	@Override
	public SpdxSnippet clone() {
		return clone(Maps.<String, SpdxElement>newHashMap());
	}
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.model.SpdxItem#verify()
	 */
	@Override
	public List<String> verify() {
		List<String> retval = super.verify();
		String snippetName = this.name;
		if (snippetName == null) {
			snippetName = "[Unnamed Snippet]";
		}
		if (this.snippetFromFile == null) {
			retval.add("Missing snippet from file in Snippet "+snippetName);
		} else {
			retval.addAll(this.snippetFromFile.verify());
		}
		if (this.byteRange == null) {
			retval.add("Missing snippet byte range from Snippet "+snippetName);
		} else {
			retval.addAll(this.byteRange.verify());
		}
		if (this.lineRange != null) {
			retval.addAll(this.lineRange.verify());
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SpdxSnippet o) {
		try {
			if (o == null) {
				return 1;
			}
			int retval = 0;
			if (this.name != null) {
				retval = this.name.compareTo(o.getName());
			}
			if (retval == 0 && this.snippetFromFile != null) {
					retval = this.snippetFromFile.compareTo(o.getSnippetFromFile());
			}
			if (retval == 0) {
				if (this.byteRange != null) {
					return this.byteRange.compareTo(o.getByteRange());
				} else {
					if (o.getByteRange() == null) {
						return 0;
					} else {
						return 1;
					}
				}
			}
			return retval;
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("Error getting compare for snippet",e);
			return -1;
		}
	}

	@Override
	public String toString() {
		if (name != null && !name.isEmpty()) {
			return name;
		}
		StringBuilder sb = new StringBuilder();
		if (this.snippetFromFile != null) {
			String fileName = this.snippetFromFile.getName();
			if (fileName != null && !fileName.isEmpty()) {
				sb.append(fileName);
			} else {
				if (this.snippetFromFile.getId() != null && !this.snippetFromFile.getId().isEmpty()) {
					sb.append("FileID ");
					sb.append(this.snippetFromFile.getId());
				} else {
					sb.append("[Unnamed File]");
				}
			}
			sb.append(": ");
		}
		if (this.byteRange != null) {
			sb.append(this.byteRange.toString());
		} else {
			sb.append("[No byte range set]");
		}
		return sb.toString();
	}
}
