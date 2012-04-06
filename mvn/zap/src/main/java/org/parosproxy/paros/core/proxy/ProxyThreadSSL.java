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
package org.parosproxy.paros.core.proxy;

import java.net.Socket;

//import org.parosproxy.paros.network.HttpUtil;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
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
