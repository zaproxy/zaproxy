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
// ZAP: 2012/04/25 Added @Override annotation to the appropriate method.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
package org.parosproxy.paros.core.proxy;

import java.net.Socket;

//import org.parosproxy.paros.network.HttpUtil;


public class ProxyThreadSSL extends ProxyThread {
	ProxyThreadSSL(ProxyServerSSL server, Socket socket) {
		super(server, socket);
//		originProcess = ProxyThread.getOriginatingProcess(inSocket.getPort());
//		if (originProcess != null) {
//		    originProcess.setForwardThread(thread);
//		}
//        
//		thread.setPriority(Thread.NORM_PRIORITY-1);
		
	}

	@Override
	protected void disconnect() {
	    
//	    long startTime = System.currentTimeMillis();
//		while (originProcess!=null && !originProcess.isForwardInputBufferEmpty() && System.currentTimeMillis() - startTime < 3000) {
//			HttpUtil.sleep(300);
//		}
//
//		if (originProcess != null) {
//		    originProcess.setDisconnect(true);
//			try {
//				originProcess.getThread().join(2000);
//			} catch (InterruptedException e) {
//			}
//
//		}


		super.disconnect();
	}
}
