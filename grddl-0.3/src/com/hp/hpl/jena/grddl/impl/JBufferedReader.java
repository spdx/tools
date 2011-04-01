/*
 (c) Copyright 2007 Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: JBufferedReader.java 1121 2007-04-11 15:02:58Z jeremy_carroll $
 */
package com.hp.hpl.jena.grddl.impl;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * JBufferedReader
 * 
 * @author Jeremy J. Carroll
 */
public class JBufferedReader extends Reader {
	final Reader base;

	private List<char[]> buffers = null;

	private int bufSz;

	static private final int MAXBUF = 1024 * 1024;

	private int whichBuf = 0;

	private int where = 0;

	private int lastBuf;

	private int lastWhere;

	private int markedWhere;

	private boolean remember = false;

	private boolean replaying = false;

	private int markLimit;

	private int oldMarkLimit;

	/**
	 * This constructor is intended for testing only.
	 * 
	 * @param base
	 * @param bSz
	 *            Initial value of bufSz - for testing try 1 or 2.
	 */
	public JBufferedReader(Reader base, int bSz) {
		this.base = base;
		bufSz = bSz;
	}

	public JBufferedReader(Reader base) {
		this(base, 256);
	}

	@Override
	public void close() throws IOException {
		forget();
		base.close();
		remember = false;
		replaying = false;
		where = 0;
		whichBuf = 0;
	}

	@Override
	public int read(char[] buf, int off, int len) throws IOException {
		int nc = read1(buf, off, len);
		if (replaying || remember) {
			where += nc;
			markLimit -= nc;
			if (markLimit < 0) {
				if (!replaying)
				  forget();
				remember = false;
			}
		}
		return nc;
	}

	public int read1(char[] buf, int off, int len) throws IOException {
		char currentBuf[];
		if (replaying) {
			int endOfCopiable;
			currentBuf = get();
			if (whichBuf < lastBuf) {
				endOfCopiable = currentBuf.length;
			} else {
				endOfCopiable = lastWhere;
			}
			if (where < endOfCopiable) {
				if (len + where < endOfCopiable) {
					System.arraycopy(currentBuf, where, buf, off, len);
					return len;
				}
				int rslt = endOfCopiable - where;
				System.arraycopy(currentBuf, where, buf, off, rslt);
				if (whichBuf == lastBuf) {
					replaying = false;
					if (!remember) {
						forget();
					}
				} else {
					whichBuf++;
					where = -rslt;
				}
				return rslt;
			} else {
				// if (whichBuf != lastBuf)
				throw new AssertionError("Replay logic");

			}
			// replaying = false;
		}
		if (!remember)
			return base.read(buf, off, len);

		currentBuf = get();
		if (where == currentBuf.length) {
			whichBuf++;
			currentBuf = get();
			where = 0;
		}
		int space = currentBuf.length - where;
		if (space > len)
			space = len;
		int cRead = base.read(currentBuf, where, space);
		if (cRead > 0) {
			System.arraycopy(currentBuf, where, buf, off, cRead);

		}
		return cRead;
	}

	private void forget() {
		buffers = null;
		whichBuf = 0;
		where = 0;
		remember = false;
		lastWhere = 0;
		lastBuf = 0;
	}

	private char[] get() {
		if (buffers == null)
			buffers = new ArrayList<char[]>();
		if (buffers.size() > whichBuf)
			return buffers.get(whichBuf);
		char[] rslt = new char[bufSz];
		if (buffers.size() != whichBuf)
			throw new AssertionError("buffer logic");
		bufSz = bufSz + bufSz;
		if (bufSz > MAXBUF)
			bufSz = MAXBUF;
		buffers.add(whichBuf, rslt);
		return rslt;
	}

	public boolean markSupported() {
		return true;
	}

	public void mark(int x) {
		markedWhere = where;
		while (whichBuf > 0) {
			buffers.remove(0);
			whichBuf--;
			lastBuf--;
		}
		// if (where != 0 || whichBuf != 0)
		// throw new UnsupportedOperationException(
		// "mark only supported at beginning of input");
		if (x <= 0) {
			remember = false;
		} else {
			remember = true;
		}
		markLimit = x;
		oldMarkLimit = markLimit;
	}

	public void reset() throws IOException {
		if (!remember) {
			throw new IOException("No valid mark currently set.");
		}
		if (!replaying) {
			lastWhere = where;
			lastBuf = whichBuf;
			replaying = whichBuf > 0 || where > markedWhere;
		}
		where = markedWhere;
		whichBuf = 0;
		markLimit = oldMarkLimit;
	}

	public boolean ready() throws IOException {
		if (!replaying)
			return base.ready();
		if (whichBuf == lastBuf && where == lastWhere) {
			replaying = false;
			return base.ready();
		}
		return true;
	}

	public long skip(long l) throws IOException {
		return super.skip(l); // TODO somewhat inefficient
	}

}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */