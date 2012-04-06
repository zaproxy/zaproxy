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

import org.apache.log4j.Level;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.ScanStatus;

public class ZapOutputWriter extends WriterAppender {

	private ScanStatus scanStatus = null;
	
	public ZapOutputWriter () {
		System.out.println("ZapOutputWriter constructor");
		
	}

	public ZapOutputWriter(ScanStatus scanStatus) {
		this.scanStatus = scanStatus;
	}

	public void append(LoggingEvent event) {

		if (event.getLevel().equals(Level.ERROR)) {
			if (scanStatus != null) {
				scanStatus.incScanCount();
			}
			
			View.getSingleton().getOutputPanel().appendDirty(event.getRenderedMessage());
			View.getSingleton().getOutputPanel().appendDirty("\n");

			String [] tsr = event.getThrowableStrRep();
			if (tsr != null) {
				for (String str : tsr) {
					View.getSingleton().getOutputPanel().appendDirty(str);
					View.getSingleton().getOutputPanel().appendDirty("\n");
				}
			}
		}
	}
}
