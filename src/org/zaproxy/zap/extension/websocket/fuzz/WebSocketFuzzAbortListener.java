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
package org.zaproxy.zap.extension.websocket.fuzz;

import org.parosproxy.paros.control.Control;
import org.zaproxy.zap.extension.fuzz.ExtensionFuzz;
import org.zaproxy.zap.extension.fuzz.FuzzProcess;
import org.zaproxy.zap.extension.fuzz.FuzzResult;
import org.zaproxy.zap.extension.fuzz.FuzzerListener;

/**
 * Checks if {@link WebSocketFuzzResult#isAbort()} is true and subsequently
 * stops further execution.
 * <p>
 * This is the case when a fuzzer is started on a closed channel or the current
 * channel is closed unexpectedly.
 * </p>
 */
public class WebSocketFuzzAbortListener implements FuzzerListener {

	private ExtensionFuzz extFuzz;

	@Override
	public void notifyFuzzerStarted(int total) {
		// not interested in started fuzzers
	}

	@Override
	public void notifyFuzzProcessStarted(FuzzProcess fuzzProcess) {
		// not interested in processes started
	}

	@Override
	public void notifyFuzzProcessComplete(FuzzResult fuzzResult) {
		if (fuzzResult instanceof WebSocketFuzzResult) {
			if (((WebSocketFuzzResult) fuzzResult).isAbort()) {
				ExtensionFuzz extFuzz = getFuzzExtension();
				if (extFuzz != null) {
					extFuzz.stopFuzzers();
				}
			}
		}
	}

	private ExtensionFuzz getFuzzExtension() {
		if (extFuzz == null) {
			extFuzz = (ExtensionFuzz) Control.getSingleton().getExtensionLoader().getExtension(ExtensionFuzz.NAME);
		}
		return extFuzz;
	}

	@Override
	public void notifyFuzzerComplete() {
		// not interested in fuzzers completed
	}
}