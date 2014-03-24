/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
package org.zaproxy.zap.extension.invoke;

import java.io.File;

import org.zaproxy.zap.utils.Enableable;

public class InvokableApp extends Enableable {

	private String displayName = "";
	private String fullCommand = "";
	private String parameters = "";
	private boolean captureOutput = true;
	private boolean outputNote = false;
	private File workingDirectory = null;

	public InvokableApp() {
	}
	
	public InvokableApp(String displayName, File workingDirectory, String fullCommand, String parameters, 
			boolean captureOutput, boolean outputNote) {
		super();
		this.displayName = displayName;
		this.workingDirectory = workingDirectory;
		this.fullCommand = fullCommand;
		this.parameters = parameters;
		this.captureOutput = captureOutput;
		this.outputNote = outputNote;
	}
	
	public InvokableApp(InvokableApp app) {
		this(app.displayName, app.workingDirectory, app.fullCommand,
				app.parameters, app.captureOutput, app.outputNote);
		
		setEnabled(app.isEnabled());
	}

	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getFullCommand() {
		return fullCommand;
	}
	public void setFullCommand(String fullCommand) {
		this.fullCommand = fullCommand;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public boolean isCaptureOutput() {
		return captureOutput;
	}

	public void setCaptureOutput(boolean captureOutput) {
		this.captureOutput = captureOutput;
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public boolean isOutputNote() {
		return outputNote;
	}

	public void setOutputNote(boolean outputNote) {
		this.outputNote = outputNote;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (captureOutput ? 1231 : 1237);
		result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + ((fullCommand == null) ? 0 : fullCommand.hashCode());
		result = prime * result + (outputNote ? 1231 : 1237);
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((workingDirectory == null) ? 0 : workingDirectory.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		InvokableApp other = (InvokableApp) obj;
		if (captureOutput != other.captureOutput) {
			return false;
		}
		if (displayName == null) {
			if (other.displayName != null) {
				return false;
			}
		} else if (!displayName.equals(other.displayName)) {
			return false;
		}
		if (fullCommand == null) {
			if (other.fullCommand != null) {
				return false;
			}
		} else if (!fullCommand.equals(other.fullCommand)) {
			return false;
		}
		if (outputNote != other.outputNote) {
			return false;
		}
		if (parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!parameters.equals(other.parameters)) {
			return false;
		}
		if (workingDirectory == null) {
			if (other.workingDirectory != null) {
				return false;
			}
		} else if (!workingDirectory.equals(other.workingDirectory)) {
			return false;
		}
		return true;
	}
	
}
