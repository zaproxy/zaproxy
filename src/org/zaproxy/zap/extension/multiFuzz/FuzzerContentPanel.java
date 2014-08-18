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

import java.io.File;

import javax.swing.JComponent;
/**
 * Interface to be implemented by components displaying {@link FuzzResult} or {@link FuzzRecord} for different message types.
 *
 */
public interface FuzzerContentPanel {
	/**
	 * Gets the component itself.
	 * @return The component
	 */
	JComponent getComponent();
	/**
	 * Removes fuzzing results that are currently being displayed.
	 */
	void clear();
	/**
	 * Adds a single entry
	 * @param fuzzResult the {@link FuzzResult} corresponding to the entry.
	 */
	void addFuzzResult(FuzzResult<?, ?> fuzzResult);
	/**
	 * Opens a {@link FuzzResultDialog} containing a graphical representation of the current entries.
	 */
	void showDiagrams();
	/**
	 * Saves the current entries to a specified file.
	 * @param f	the target file
	 */
	void saveRecords(File f);
	/**
	 * Adds entries from a file.
	 * @param f	the target file
	 */
	void loadRecords(File f);
}
