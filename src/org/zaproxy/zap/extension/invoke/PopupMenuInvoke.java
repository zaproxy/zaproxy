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

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.popup.PopupMenuItemHttpMessageContainer;

public class PopupMenuInvoke extends PopupMenuItemHttpMessageContainer {

	private static final long serialVersionUID = 1L;
    private String command = null;
    private File workingDir = null;
    private String parameters = null;
    private boolean captureOutput = true;
    private boolean outputNote = false;

    private Logger logger = Logger.getLogger(PopupMenuInvoke.class);

    /**
     * @param label
     */
    public PopupMenuInvoke(String label) {
        super(label);
    }

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
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

	public File getWorkingDir() {
		return workingDir;
	}

	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}

	public boolean isOutputNote() {
		return outputNote;
	}

	public void setOutputNote(boolean outputNote) {
		this.outputNote = outputNote;
	}

	@Override
	public void performAction(HttpMessage msg) {
		try {
    		if (command != null) {
    			InvokeAppWorker iaw = 
    					new InvokeAppWorker(command, workingDir, parameters, captureOutput, outputNote, msg);
    			iaw.execute();
    		}
		} catch (Exception e1) {
			View.getSingleton().showWarningDialog(e1.getMessage());
			logger.error(e1.getMessage(), e1);

		}
	}

}
