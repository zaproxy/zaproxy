/*
 * Created on May 31, 2004
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.
package org.parosproxy.paros.network;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class HttpOutputStream extends BufferedOutputStream {

    private static final int	BUFFER_SIZE = 4096;
	private static final String CODEPAGE = "8859_1";
		
	public HttpOutputStream(OutputStream out) {
		//this.out = BufferedOutputStream(out);
	    super(out, BUFFER_SIZE);
	}
		

	public void write(String data) throws IOException {
		//out.write(data.getBytes(CODEPAGE));
		super.write(data.getBytes(CODEPAGE));
		flush();
	}
	
	public void write(HttpBody body) throws IOException {
		if (body != null && body.length() > 0) {
			write(body.toString().getBytes(CODEPAGE));
		}
	}
	
	public void write(HttpResponseHeader resHeader) throws IOException {
		write(resHeader.toString());
	}


	@Override
	public void write(byte[] buf) throws IOException {
		if (buf == null) return;
		write(buf, 0, buf.length);
	}

	public void write(byte[] buf, int len) throws IOException {
		write(buf, 0, len);
	}
		
	@Override
	public void write(byte[] buf, int off, int len) throws IOException {
		if (buf == null) return;
		//out.write(buf, off, len);
		super.write(buf, off, len);
		flush();
	}

	@Override
	public void close() {
		try {
//			out.close();
//			out = null;
		    super.close();
		} catch (Exception e) {
		}
	}

	@Override
	public void flush() throws IOException {
		super.flush();	//out.flush();
	}
}
