/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2011/08/04 Changed to support new HttpPanel interface 

package org.parosproxy.paros.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.extension.tab.Tab;
import org.zaproxy.zap.view.HttpPanelManager;
import org.zaproxy.zap.view.HttpPanelView;

// ZAP: 2011/08/04 Changed to support new interface

/**
 *
 * Panel to display HTTP request/response with header and body.
 * 
 * Future: to support different view.
 * 
 * This creates:
 * +---------------------+
 * | panelHeader         |
 * +---------------------+
 * | contentSplit        |
 * | ------------------- |
 * |                     |
 * +---------------------+
 * 
 * 
 */
abstract public class HttpPanel extends AbstractPanel implements Tab {
	private static final long serialVersionUID = 1L;
	protected static final String VIEW_RAW = Constant.messages.getString("http.panel.rawView");	// ZAP: i18n
	protected static final String VIEW_TABULAR = Constant.messages.getString("http.panel.tabularView");	// ZAP: i18n
	protected static final String VIEW_IMAGE = Constant.messages.getString("http.panel.imageView");	// ZAP: i18n

	protected boolean editable = false;

	private Extension extension = null;

	private JPanel panelHeader;
	private JPanel panelContent;
	
	private JPanel panelSpecial = null;
	
	protected HttpMessage httpMessage;
	protected List<HttpPanelView> views = new ArrayList<HttpPanelView>();
	
	/*** Constructors ***/

	public HttpPanel() {
		super();
		initialize();

		HttpPanelManager.getInstance().addPanel(this);
	}

	public HttpPanel(boolean isEditable, HttpMessage httpMessage) {
		this();
		this.editable = isEditable;
		this.httpMessage = httpMessage;
	}

	public HttpPanel(boolean isEditable, Extension extension, HttpMessage httpMessage) {
		this(isEditable, httpMessage);
		this.extension = extension;
	}

	private  void initialize() {
		this.setLayout(new BorderLayout());
		
		if (Model.getSingleton().getOptionsParam().getViewParam().getAdvancedViewOption() > 0) {
			this.add(getPanelHeader(), BorderLayout.NORTH);
		}
		this.add(getPanelContent(), BorderLayout.CENTER);
	}

	
	/**
	 * This method initializes the content panel
	 */    
	protected JPanel getPanelContent() {
		if (panelContent == null) {
			panelContent = new JPanel();
			panelContent.setLayout(new CardLayout());
		}
		
		return panelContent;
	}
	

	/**
	 * This method initializes the header, aka toolbar
	 */    
	protected JPanel getPanelHeader() {
		if (panelHeader == null) {
			panelHeader = new JPanel();
			panelSpecial = new JPanel();
			
			panelHeader.add(panelSpecial);
		}

		return panelHeader;
	}
	
	/* Update content of the window.
	 * Called if this.httpMessage was modified external.
	 */
	abstract public void updateContent();
	
	/* Set new HttpMessage
	 * Update UI accordingly.
	 */
	public void setMessage(HttpMessage msg) {
		if (msg == null) {
			   return;
		}
		
		this.httpMessage = msg;
		updateContent();
	}
	
	public void setMessage(HttpMessage msg, boolean enableViewSelect) {
		setMessage(msg);
		
		// TODO
	}
	
	abstract public void clearView(boolean enableViewSelect);

	/* Get Special Panel
	 * Return panel where one can add functionality to this panel
	 */
	public JPanel getPanelSpecial() {
		return panelSpecial;
	}
	
	/* Save data
	 * Save current data in view into HttpMessage
	 */
	abstract public void saveData();
	
	/* Get HttpMessage
	 * External code needs to modify or view saved HttpMessage
	 * save data first so it's current
	 */
	public HttpMessage getHttpMessage() {
//		saveData();
		return httpMessage;
	}

	// ZAP: Support plugable views
	abstract public void addView (HttpPanelView view);
	
	public boolean isEditable() {
		return editable;
	}

	public void setExtension(Extension extension) {
		this.extension = extension;
	}

	public Extension getExtension() {
		return extension;
	}
	
	// Search functions - for SearchPanel and SearchResult
	abstract public void highlightHeader(SearchMatch sm);
	abstract public void highlightBody(SearchMatch sm);
	abstract public void headerSearch(Pattern p, List<SearchMatch> matches);
	abstract public void bodySearch(Pattern p, List<SearchMatch> matches);
}
