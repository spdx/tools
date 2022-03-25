package org.spdx.rdfparser.license;

import org.apache.jena.graph.Node;
import org.spdx.rdfparser.IModelContainer;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;

public class ExtractedLicenseInfoStrategy implements ILicenseInfoSetStrategy {
	@Override
	public AnyLicenseInfo getLicenseInfoSet(IModelContainer modelContainer, Node node) throws InvalidSPDXAnalysisException {
		return new ExtractedLicenseInfo(modelContainer, node);
	}
}
