/*
  (c) Copyright 2007 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: RewindableReader.java 1170 2007-04-24 13:50:52Z jeremy_carroll $
*/
package com.hp.hpl.jena.grddl.impl;

import java.io.IOException;
import java.io.Reader;

import javax.xml.transform.stream.StreamSource;

/**
 * RewindableInputStream
 * @author Jeremy J. Carroll
 */
public class RewindableReader extends AbsRewindableInputStreamOrReader {

	JBufferedReader bis;
	public RewindableReader(Reader r, String base) throws IOException  {
		super(base);
		bis = new JBufferedReader(r){
			public void close() {
				// do nothing; transformer incorrectly closes stream.
			}
		};
		bis.mark(Integer.MAX_VALUE);
		
	}


	@Override
	StreamSource startAfreshRaw(boolean rewindable) throws IOException {
		bis.reset();
		if (!rewindable)
			bis.mark(0);
		return new StreamSource(bis,retrievalIRI());
	}
	
	

}


/*
    (c) Copyright 2007 Hewlett-Packard Development Company, LP
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