/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.log4j;

import javax.swing.SwingUtilities;

import org.apache.log4j.Level;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.ScanStatus;

public class ZapOutputWriter extends WriterAppender {

	private final static char NEWLINE = '\n';
	private ScanStatus scanStatus = null;
	
	public ZapOutputWriter () {
		System.out.println("ZapOutputWriter constructor");
		
	}

	public ZapOutputWriter(ScanStatus scanStatus) {
		this.scanStatus = scanStatus;
	}

	@Override
	public void append(final LoggingEvent event) {
		if (! View.isInitialised()) {
			// Running in daemon mode
			return;
		}

		if (event.getLevel().equals(Level.ERROR)) {
			if (! SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(new Runnable(){
					@Override
					public void run() {
						append(event);
					}});
				return;
			}

			if (scanStatus != null) {
				scanStatus.incScanCount();
			}
			
			String renderedmessage=event.getRenderedMessage();
			if (renderedmessage!=null) {
				View.getSingleton().getOutputPanel().append(new StringBuilder(renderedmessage).append(NEWLINE).toString());
			}

			String [] tsr = event.getThrowableStrRep();			
			if (tsr != null) {
				StringBuilder eventThrowableStrRep = new StringBuilder(tsr.length*75);//Capacity is guessed, but more than 16 for sure
				for (String str : tsr) {
					eventThrowableStrRep.append(str).append(NEWLINE);
				}
				//Send it as a single string
				View.getSingleton().getOutputPanel().append(eventThrowableStrRep.toString());
			}
		}
	}
}
