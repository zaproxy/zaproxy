/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP development team
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
package org.zaproxy.zap.extension.script;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple way to handle multiple writers
 * @author simon
 *
 */
public class MultipleWriters extends Writer {
	
	private List<Writer> writers = new ArrayList<>();

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		String str = new String(cbuf, off, len);
		for (Writer writer : writers) {
			writer.append(str);
		}
	}

	@Override
	public void flush() throws IOException {
		for (Writer writer : writers) {
			writer.flush();
		}
	}

	@Override
	public void close() throws IOException {
		for (Writer writer : writers) {
			writer.close();
		}
	}

	public void addWriter(Writer writer) {
		this.writers.add(writer);
	}
	
	public void removeWriter(Writer writer) {
		this.writers.remove(writer);
	}
}
