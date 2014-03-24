/*
 * Created on May 29, 2004
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
// ZAP: 2012/03/15 Changed to use the classes HttpRequestBody and HttpResponseBody
//      Added @Override annotation where appropriate.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments

package org.parosproxy.paros.network;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLSocket;

import org.apache.log4j.Logger;
import org.zaproxy.zap.network.HttpRequestBody;
import org.zaproxy.zap.network.HttpResponseBody;

public class HttpInputStream extends BufferedInputStream {
	private static Logger log = Logger.getLogger(HttpInputStream.class);

	private static final int	BUFFER_SIZE = 4096;
	private static final String CRLF = "\r\n";
	private static final String CRLF2 = CRLF + CRLF;
	private static final String LF = "\n";
	private static final String LF2 = LF + LF;

//	private BufferedInputStream in = null;
	private byte[] mBuffer = new byte[BUFFER_SIZE];
	private Socket mSocket = null;
	
	public HttpInputStream(Socket socket) throws IOException {
	    super(socket.getInputStream(), BUFFER_SIZE);
		setSocket(socket);
		//this.in = new BufferedInputStream(mSocket.getInputStream());
	}
	
	public HttpRequestHeader readRequestHeader(boolean isSecure) throws HttpMalformedHeaderException, IOException {
		String msg = "";
		HttpRequestHeader httpRequestHeader = null;
		
		msg = readHeader();
		if (msg.length() == 0) {
    		log.debug("Read 0 bytes from upstream. Could not read header!");
			throw new IOException();
		}
		
		httpRequestHeader = new HttpRequestHeader(msg, isSecure); 
		
		return httpRequestHeader;
	}

    
	public synchronized String readHeader() throws IOException {
		String msg = "";
        int		oneByte = -1;
        boolean eoh = false;
        StringBuilder sb = new StringBuilder(200);
        
        do {
            oneByte = super.read();
        	
        	if (oneByte == -1) {
        		eoh = true;
				break;
        	}
            sb.append((char) oneByte);

            if (((char) oneByte) == '\n' && isHeaderEnd(sb)) {
                eoh = true;
                msg = sb.toString();
            }
		} while (!eoh);
        
        return msg;

	}

	/**
	 * Check if the current StringBuilder trailing characters is an HTTP header end (empty CRLF).
	 * @param sb
	 * @return true - if end of HTTP header.
	 */
	private static final boolean isHeaderEnd(StringBuilder sb) {
		int len = sb.length();
		if (len > 2) {
			if (LF2.equals(sb.substring(len-2))) {
				return true;
			}
		}
	
		if (len > 4) {
			if (CRLF2.equals(sb.substring(len-4))) {
				return true;
			}
		}
	
		return false;
	}

	/**
	 * Read Http body from input stream as a string basing on the content length on the method.
	 * @param httpHeader
	 * @return Http body
	 */
	public synchronized HttpRequestBody readRequestBody(HttpHeader httpHeader) {

		int contentLength = httpHeader.getContentLength();	// -1 = default to unlimited length until connection close
		
		HttpRequestBody body = (contentLength > 0) ? new HttpRequestBody(contentLength) : new HttpRequestBody();
		
		readBody(contentLength, body);
		
		return body;
	}

	/**
	 * Read Http body from input stream as a string basing on the content length on the method.
	 * @param httpHeader
	 * @return Http body
	 */
	public synchronized HttpResponseBody readResponseBody(HttpHeader httpHeader) {

		int contentLength = httpHeader.getContentLength();	// -1 = default to unlimited length until connection close
		
		HttpResponseBody body = (contentLength > 0) ? new HttpResponseBody(contentLength) : new HttpResponseBody();
		
		readBody(contentLength, body);
		
		return body;
	}

	private void readBody(int contentLength, HttpBody body) {
		
		int readBodyLength = 0;
		int len = 0;
		
		try {
			while (contentLength == -1 || readBodyLength < contentLength) {
				len = readBody(contentLength, readBodyLength, mBuffer);	// use mBuffer to avoid locally create too many data buffer
                if (len > 0) {
					readBodyLength += len;
				} else if (len < 0) {
					// ZAP: FindBugs fix
					break;
				}
				body.append(mBuffer, len);
			}
		} catch (IOException e) {
			// read until IO error occur - eg connection close
		}
	}
	
	/**
	 * 
	 * @param contentLength		Content length read to be read.  -1 = unlimited until connection close.
	 * @param readBodyLength 	Body length read so far
	 * @param data				Buffer storing the read bytes.
	 * @return					Number of bytes read in buffer
	 * @throws IOException
	 */
	private int readBody(int contentLength, int readBodyLength, byte[] buffer) throws IOException {

		int len = 0;
		int remainingLen = 0;

		if (contentLength == -1) {
//			len = in.read(buffer);
			len = super.read(buffer);

		} else {
			remainingLen = contentLength - readBodyLength;
			if (remainingLen < buffer.length && remainingLen > 0) {
//				len = in.read(buffer,0,remainingLen);
				len = super.read(buffer,0,remainingLen);

			} else if (remainingLen > buffer.length) {
//				len = in.read(buffer);
				len = super.read(buffer);

			}

		}

		return len;
	}
	
	public void setSocket(Socket socket) {
		mSocket = socket;
	}

	@Override
	public int available() throws IOException {
		int avail = 0;
//		int oneByte = -1;
		int timeout = 0;

		avail = super.available();

		if (avail == 0 && mSocket != null && mSocket instanceof SSLSocket) {
			try {
				timeout = mSocket.getSoTimeout();
				mSocket.setSoTimeout(1);
				super.mark(256);
				super.read();
				super.reset();
				avail = super.available();
			} catch (SocketTimeoutException e) {
				avail = 0;
			} finally {
				mSocket.setSoTimeout(timeout);
			}
		}
		
		return avail;
	}
	
	@Override
	public int read() throws IOException {
		//return in.read();
		return super.read();

	}
	
	@Override
	public int read(byte[] b) throws IOException {

		return super.read(b);

	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {

		return super.read(b, off, len);

	}

	@Override
	public void close() {
		try {

		    super.close();
		} catch (Exception e) {
			
		}
	}
}
