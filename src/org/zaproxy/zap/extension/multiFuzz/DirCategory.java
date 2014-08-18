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
import java.util.ArrayList;
import java.util.List;

/**
 * Class administrating the fuzzers and files of a particular fuzzing category.
 * 
 */
public class DirCategory {
	private String name;
	private List<File> fuzzers = new ArrayList<>();
	/**
	 * Gets a list of fuzzing files in this category. To access these a {@link FileFuzzer} needs to be created for each individual file.
	 * @return the list of fuzzing file
	 */
	public List<File> getFuzzers() {
		return fuzzers;
	}
	/**
	 * The standard constructor
	 * @param name the category name
	 */
	public DirCategory(String name) {
		super();
		this.name = name;
	}
	/**
	 * Adds a file to this category
	 * @param fileFuzzer the file
	 */
	public void addFuzzer(File fileFuzzer) {
		this.fuzzers.add(fileFuzzer);
	}
	/**
	 * Returns the categories name as displayed in a {@link PayloadDialog}.
	 * @return the category name
	 */
	public String getName() {
		return this.name;
	}
	/**
	 * Checks if a fuzzing file of the specified name is part of this category. If so it returns the associated file, otherwise null.
	 * @param name	the name to search for
	 * @return	a file of that name or null
	 */
	public File getFuzzerFile(String name) {
		for (File ff : fuzzers) {
			if (ff.getName().equals(name)) {
				return ff;
			}
		}
		return null;
	}
}
