/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP development team
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

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javax.script.ScriptException;

public class ScriptWrapper {

	private String name;
	private String description;
	private ScriptEngineWrapper engine;
	private String engineName;
	private ScriptType type;
	private String typeName;
	private String contents = "";
	private String lastOutput = "";
	private boolean changed = false;
	private boolean enabled = false;
	private boolean error = false;
	private boolean loadOnStart = false;
	private File file;
	private String lastErrorDetails = "";
	private Exception lastException = null;
	private Writer writer = null;
	
	public ScriptWrapper() {
	}
	
	public ScriptWrapper(String name, String description, ScriptEngineWrapper engine, ScriptType type) {
		super();
		this.name = name;
		this.description = description;
		this.engine = engine;
		this.type = type;
	}
	
	public ScriptWrapper(String name, String description, String engineName, ScriptType type, boolean enabled, File file) {
		super();
		this.name = name;
		this.description = description;
		this.engineName = engineName;
		this.type = type;
		this.enabled = enabled;
		this.file = file;
	}
	
	public ScriptWrapper(String name, String description,
			ScriptEngineWrapper engine, ScriptType type, boolean enabled, File file) {
		super();
		this.name = name;
		this.description = description;
		this.engine = engine;
		this.type = type;
		this.enabled = enabled;
		this.file = file;
	}

	protected ScriptWrapper(String name, String description, String engineName, String typeName, boolean enabled, File file) {
		super();
		this.name = name;
		this.description = description;
		this.engineName = engineName;
		this.typeName = typeName;
		this.enabled = enabled;
		this.file = file;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public ScriptEngineWrapper getEngine() {
		return engine;
	}
	public void setEngine(ScriptEngineWrapper engine) {
		this.engine = engine;
	}
	public void setEngineName(String engineName) {
		this.engineName = engineName;
	}
	public String getEngineName() {
		if (engine != null) {
			return engine.getEngineName();
		}
		return engineName;
	}
	public ScriptType getType() {
		return type;
	}
	public void setType(ScriptType type) {
		this.type = type;
	}
	
	public String getTypeName() {
		if (type != null) {
			return type.getName();
		}
		return typeName;
	}

	public String getContents() {
		return contents;
	}
	public void setContents(String contents) {
		if (!contents.equals(this.contents)) {
			this.contents = contents;
			this.changed = true;
		}
	}

	public String getLastOutput() {
		return lastOutput;
	}

	public void setLastOutput(String lastOutput) {
		this.lastOutput = lastOutput;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			this.changed = true;
		}
	}

	public String getLastErrorDetails() {
		return lastErrorDetails;
	}

	public void setLastErrorDetails(String lastErrorDetails) {
		this.lastErrorDetails = lastErrorDetails;
	}

	public Exception getLastException() {
		return lastException;
	}

	public void setLastException(Exception lastException) {
		this.lastException = lastException;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public boolean isLoadOnStart() {
		return loadOnStart;
	}

	public void setLoadOnStart(boolean loadOnStart) {
		this.loadOnStart = loadOnStart;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	public <T> T getInterface(Class<T> class1) throws ScriptException, IOException {
		return null;
	}

	public boolean isRunableStandalone() {
		return this.getType() != null && ExtensionScript.TYPE_STANDALONE.equals(this.getType().getName());
	}

	/**
	 * Gets the writer which will be written to every time this script runs (if any)
	 * @return
	 */
	public Writer getWriter() {
		return writer;
	}

	/**
	 * Set a writer which will be written to every time this script runs
	 * @param writer
	 */
	public void setWriter(Writer writer) {
		this.writer = writer;
	}
}
