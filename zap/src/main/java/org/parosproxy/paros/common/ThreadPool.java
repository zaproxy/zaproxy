/*
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
package org.parosproxy.paros.common;


public class ThreadPool {

	private Thread[]	pool		= null;
	
	public ThreadPool(int maxThreadCount) {
		pool = new Thread[maxThreadCount];
	}

	/**
	Get a free thread from thread pool.  If there is no free thread, return null
	*/
	public synchronized Thread getFreeThreadAndRun(Runnable runnable) {
	    
		for (int i=0; i<pool.length; i++) {
			if (pool[i] == null || !pool[i].isAlive()) {
				pool[i] = new Thread(runnable);
				pool[i].setDaemon(true);
				pool[i].start();
				return pool[i];
			}
		}
		return null;
	}

	/**
	Wait until all thread completed tasks (at most some time for each).
	If not completed yet, do not wait.  Each thread should kill itself.
	*/
	public void waitAllThreadComplete(int waitInMillis) {
		for (int i=0; i<pool.length; i++) {
			if (pool[i] != null && pool[i].isAlive()) {
				try {
					pool[i].join(waitInMillis);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	public boolean isAllThreadComplete() {
		for (int i=0; i<pool.length; i++) {
			if (pool[i] != null && pool[i].isAlive()) {
			    return false;
			}
		}
		return true;
	}
}
