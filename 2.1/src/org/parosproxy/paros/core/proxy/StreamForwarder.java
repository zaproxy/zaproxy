/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2005 Chinotec Technologies Company
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
// ZAP: 2012/04/25 Added @Override annotation to the appropriate method.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/03/03 Issue 547: Deprecate unused classes and methods
package org.parosproxy.paros.core.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * @deprecated No longer used/needed. It will be removed in a future release.
 */
@Deprecated
public class StreamForwarder extends Thread {

    private Socket inSocket = null;
    private OutputStream out = null;
    private InputStream in = null;
    private boolean isStop = false;
    
    public StreamForwarder(Socket inSocket, InputStream in, OutputStream out) {
        this.inSocket = inSocket;
        this.out = out;
        this.in = in;
        this.setDaemon(true);

        
    }
    
    public void setStop(boolean isStop) {
        this.isStop = isStop;
    }
    
    @Override
    public void run() {
        byte[] buffer = new byte[2048];
        int len = -1;
//        long startTime = System.currentTimeMillis();
        int continuousCount = 0;
        
        try {
            inSocket.setSoTimeout(150);
            
            do {
                try {
                    len = in.read(buffer);
                    
                    if (len > 0) {
                        out.write(buffer, 0, len);
                        out.flush();
	                    continuousCount++;
	                    if (continuousCount % 5 == 4) Thread.yield(); // avoid same thread occupy all CPU time.

                    }
                } catch (SocketTimeoutException ex) {
                    len = 0;
		            continuousCount = 0;
                }
                
            } while (!isStop && len >= 0);
                
        } catch (IOException e) {
            
        }
    }
}
