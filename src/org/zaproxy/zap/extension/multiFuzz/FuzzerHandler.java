/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.zaproxy.zap.extension.multiFuzz;

import java.util.List;
import java.util.regex.Pattern;

import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.search.SearchResult;

/**
 * Handler associated with a certain implementation of the message interface. Responsible for launching the appropriate {@link FuzzDialog}
 * and starting / administrating the {@link FuzzerThread} corresponding to the users choices. 
 *
 * @param <M> Specific message type
 * @param <D> Corresponding {@link FuzzDialog} implementation
 */

public interface FuzzerHandler<M extends Message, D extends FuzzDialog<?, ?, ?, ?>> {
	/**
	 * Gets the {@link ExtensionFuzz} the handler is registered with.
	 * @return parent {@link ExtensionFuzz}
	 */
	ExtensionFuzz getExtension();

	/**
	 * Shows the {@link FuzzDialog} to allow the choice of targets and payloads.
	 * 
	 * @param fc invoking {@link FuzzableComponent}
	 */
	void showFuzzDialog(FuzzableComponent<M> fc);
	
	/**
	 * Gets the {@link FuzzerContentPanel} corresponding to the handlers message type
	 * @return the corresponding {@link FuzzerContentPanel}
	 */
	FuzzerContentPanel getFuzzerContentPanel();

	/**
	 * Given a pattern finds all fuzzed messages corresponding to it.
	 * @param pattern	zhe pattern
	 * @param inverse	return the inverted findings
	 * @return			a list of corresponding messages
	 */
	List<SearchResult> searchResults(Pattern pattern, boolean inverse);
	/**
	 * Resets the {@link FuzzerContentPanel}
	 */
	public void reset();
	/**
	 * Initiates and run {@link FuzzProcess}
	 */
	public void startFuzzers();
	/**
	 * Stops all existing {@link FuzzProcess}
	 */
	public void stopFuzzers();
	/**
	 * Pauses all running {@link FuzzProcess}
	 */
	public void pauseFuzzers();
	/**
	 * Restarts all existing {@link FuzzProcess}
	 */
	public void resumeFuzzers();
}
