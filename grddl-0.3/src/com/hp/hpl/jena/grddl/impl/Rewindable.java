/*
  (c) Copyright 2006 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Rewindable.java 1170 2007-04-24 13:50:52Z jeremy_carroll $
*/
package com.hp.hpl.jena.grddl.impl;

import java.io.IOException;

import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.impl.AbsIRIFactoryImpl;

/**
 * RewindStreamSource
 * @author Jeremy J. Carroll
 */
abstract class Rewindable {

	private XMLReader saxOrTidyParser; 
	final static private IRIFactory iriFactory =
		IRIFactory.jenaImplementation();
	private IRI retrievalIri;
	private IRI baseIri;
	public Rewindable(String u) {
		retrievalIri = iriFactory.create(u);
	}
	
	void updateRetrivalIRI(String u) {
		retrievalIri = iriFactory.create(u);
	}
	
	String resolve(String u) {
		return baseIri().create(u).toString();
	}
	
	private IRI baseIri() {
		return baseIri==null?retrievalIri:baseIri;
	}

	abstract StreamSource startAfreshRaw(boolean rewindable) throws IOException;
	
	/**
	 * 
	 * @return "" if not known
	 */
	abstract String mimetype();
	
	/**
	 * @return null if using an InputStream or 
	 *        encoding of Reader or "" if not known
	 */
	abstract String encoding();


	abstract void close() throws IOException;

	void useSaxOrTidy(XMLReader reader) {
		saxOrTidyParser = reader;
	}

	final SAXSource startAfresh(boolean needRewind) throws IOException {
//		if (tidyParser==null)
//	 	     return startAfreshRaw(needRewind);
		// convert stream source into SAXSource using
		// tidy-ing parser.
		StreamSource ss = startAfreshRaw(needRewind);
		InputSource saxInput = new InputSource();
		saxInput.setSystemId(ss.getSystemId());
		saxInput.setByteStream(ss.getInputStream());
		saxInput.setCharacterStream(ss.getReader());
		saxInput.setEncoding(encoding());
		
		return new SAXSource(saxOrTidyParser,saxInput);
	}

	public void setBase(String xmlBase) {
		baseIri = baseIri().resolve(xmlBase);
	}

	String retrievalIRI() {
		return resolveAgainstRetrievalIRI("");
	}

	public String resolveAgainstRetrievalIRI(String a) {
		return retrievalIri.create(a).toString();
	}
	

}


/*
    (c) Copyright 2006 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/