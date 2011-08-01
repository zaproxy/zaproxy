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

package org.zaproxy.zap.extension.httppanel;

import javax.swing.JComponent;

import org.parosproxy.paros.network.HttpMessage;
/**
 * A Plugable view which can display requests and responses in different ways 
 * @author Psiinon
 * @see org.zaproxy.zap.extension.httppanel.view.hex.HttpPanelHexView for an example of how to implement a new view
 *
 */
public interface HttpPanelView {

	public void load();
	public void save();
	
	/**
	 * The name to be used in the drop down - should be internationalised
	 * @return
	 */
	public String getName();

	/**
	 * If this returns true then this view will appear in the drop down for this message
	 * @param msg
	 * @return
	 */
	public boolean isEnabled(HttpMessage msg);
	
	/**
	 * Sets the content for this view
	 * @param content
	 */
//	public void setModel (String content);

	/**
	 * Gets the content
	 * @return a string representation of the content
	 */
//	public String getModel();

	
	/**
	 * @return true if the content has been changed
	 */
	public boolean hasChanged();
	
	/**
	 * Get the scrolling pane which contains the content
	 * @return
	 */
	public JComponent getPane();
	
	/**
	 * @return true if this view is editable
	 */
	public boolean isEditable();
	
	/**
	 * Set if this view can be changed 
	 * @param editable
	 */
	public void setEditable(boolean editable);
}
