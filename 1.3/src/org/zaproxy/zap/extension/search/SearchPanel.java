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
package org.zaproxy.zap.extension.search;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.utils.ZapTextField;


public class SearchPanel extends AbstractPanel {
	
	private static final long serialVersionUID = 1L;

	public static final String PANEL_NAME = "search";

	private ExtensionSearch extension;
	
	private javax.swing.JPanel panelCommand = null;
	private javax.swing.JToolBar panelToolbar = null;
	private JScrollPane jScrollPane = null;

	private ZapTextField regEx = null;
	private JButton btnSearch = null;
	private JComboBox searchType = null;
	private JButton btnNext = null;
	private JButton btnPrev = null;
	
	private JList resultsList = new JList();
	private DefaultListModel resultsModel;

	private HttpPanelRequest requestPanel = null;
	private HttpPanelResponse responsePanel = null;

    private SearchPanelCellRenderer searchPanelCellRenderer = null;
    //private static Log log = LogFactory.getLog(SearchPanel.class);

    /**
     * 
     */
    public SearchPanel() {
        super();
 		initialize();
    }

	public ExtensionSearch getExtension() {
		return extension;
	}

	public void setExtension(ExtensionSearch extension) {
		this.extension = extension;
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private  void initialize() {
        this.setLayout(new CardLayout());
        //this.setSize(474, 251);
        this.setName(Constant.messages.getString("search.panel.title"));
		this.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/049.png")));	// 'magnifying glass' icon
        this.add(getPanelCommand(), getPanelCommand().getName());
        
        resultsList.setName("listSearch");
		resultsList.setFixedCellHeight(16);	// Significantly speeds up rendering

        resultsList.addMouseListener(new java.awt.event.MouseAdapter() { 
			public void mousePressed(java.awt.event.MouseEvent e) {    
			    if (SwingUtilities.isRightMouseButton(e)) { 
			        View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
			    }	
			}
		});


	}
	
	/**
	 * This method initializes panelCommand	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	/**/
	private javax.swing.JPanel getPanelCommand() {
		if (panelCommand == null) {

			panelCommand = new javax.swing.JPanel();
			panelCommand.setLayout(new java.awt.GridBagLayout());
			panelCommand.setName("Search Panel");
			
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.weightx = 1.0D;
			gridBagConstraints1.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.weighty = 1.0;
			gridBagConstraints2.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
			
			panelCommand.add(this.getPanelToolbar(), gridBagConstraints1);
			panelCommand.add(getJScrollPane(), gridBagConstraints2);

		}
		return panelCommand;
	}
	/**/

	private javax.swing.JToolBar getPanelToolbar() {
		if (panelToolbar == null) {
			
			panelToolbar = new javax.swing.JToolBar();
			panelToolbar.setLayout(new java.awt.GridBagLayout());
			panelToolbar.setEnabled(true);
			panelToolbar.setFloatable(false);
			panelToolbar.setRollover(true);
			panelToolbar.setPreferredSize(new java.awt.Dimension(800,30));
			panelToolbar.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			panelToolbar.setName("Search Toolbar");
			
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			GridBagConstraints gridBagConstraintsX = new GridBagConstraints();

			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
			
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraints3.gridx = 2;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraints4.gridx = 3;
			gridBagConstraints4.gridy = 0;
			gridBagConstraints4.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraints5.gridx = 4;
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraintsX.gridx = 5;
			gridBagConstraintsX.gridy = 0;
			gridBagConstraintsX.weightx = 1.0;
			gridBagConstraintsX.weighty = 1.0;
			gridBagConstraintsX.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraintsX.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraintsX.fill = java.awt.GridBagConstraints.HORIZONTAL;

			JLabel t1 = new JLabel();

			panelToolbar.add(getRegExField(), gridBagConstraints1);
			panelToolbar.add(getSearchType(), gridBagConstraints2);
			panelToolbar.add(getBtnSearch(), gridBagConstraints3);
			panelToolbar.add(getBtnNext(), gridBagConstraints4);
			panelToolbar.add(getBtnPrev(), gridBagConstraints5);
			panelToolbar.add(t1, gridBagConstraintsX);
		}
		return panelToolbar;
	}
	
	private JButton getBtnSearch() {
		if (btnSearch == null) {
			btnSearch = new JButton();
			btnSearch.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/049.png")));	// 'magnifying glass' icon
			btnSearch.setToolTipText("Search");

			btnSearch.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {
					doSearch();
				}
			});
		}
		return btnSearch;
	}

	private JButton getBtnNext() {
		if (btnNext == null) {
			btnNext = new JButton();
			btnNext.setText(Constant.messages.getString("search.toolbar.label.next"));
			btnNext.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/107.png")));	// 'arrow down' icon
			btnNext.setToolTipText(Constant.messages.getString("search.toolbar.label.next"));

			btnNext.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {
					highlightNextResult();
				}
			});
		}
		return btnNext;
	}

	private JButton getBtnPrev() {
		if (btnPrev == null) {
			btnPrev = new JButton();
			btnPrev.setText(Constant.messages.getString("search.toolbar.label.previous"));
			btnPrev.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/108.png")));	// 'arrow up' icon
			btnPrev.setToolTipText(Constant.messages.getString("search.toolbar.label.previous"));

			btnPrev.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {
					highlightPrevResult();
				}
			});
		}
		return btnPrev;
	}

	protected ZapTextField getRegExField () {
		if (regEx == null) {
			regEx = new ZapTextField();
			regEx.setHorizontalAlignment(ZapTextField.LEFT);
			regEx.setAlignmentX(0.0F);
			regEx.setPreferredSize(new java.awt.Dimension(250,25));
			regEx.setText("");
			regEx.setMinimumSize(new java.awt.Dimension(250,25));
			
			regEx.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {
					doSearch();
				}
			});

		}
		return regEx;
	}
	
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			
			resetSearchResults();
			jScrollPane.setViewportView(resultsList);
			
		}
		return jScrollPane;
	}

	public void resetSearchResults() {
		resultsModel = new DefaultListModel();
		resultsList.setModel(resultsModel);
        resultsList.setCellRenderer(getSearchPanelCellRenderer());

		resultsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() { 

			public void valueChanged(javax.swing.event.ListSelectionEvent e) {
			    if (resultsList.getSelectedValue() == null) {
			        return;
			    }
			    
			    displayMessage(((SearchResult)resultsList.getSelectedValue()));
			}
		});
	}

	public void addSearchResult(SearchResult str) {
		resultsModel.addElement(str);
		if (resultsModel.size() == 1) {
			this.highlightFirstResult();
		}
	}

    public void setDisplayPanel(HttpPanelRequest requestPanel, HttpPanelResponse responsePanel) {
        this.requestPanel = requestPanel;
        this.responsePanel = responsePanel;
    }
    
    private void doSearch() {
    	ExtensionSearch.Type type = ExtensionSearch.Type.All;
    	
    	if (Constant.messages.getString("search.toolbar.label.type.url").equals(searchType.getSelectedItem())) {
    		type = ExtensionSearch.Type.URL;
    	} else if (Constant.messages.getString("search.toolbar.label.type.request").equals(searchType.getSelectedItem())) {
    		type = ExtensionSearch.Type.Request;
    	} else if (Constant.messages.getString("search.toolbar.label.type.response").equals(searchType.getSelectedItem())) {
    		type = ExtensionSearch.Type.Response;
    	} else if (Constant.messages.getString("search.toolbar.label.type.header").equals(searchType.getSelectedItem())) {
    		type = ExtensionSearch.Type.Header;
    	} else if (Constant.messages.getString("search.toolbar.label.type.fuzz").equals(searchType.getSelectedItem())) {
    		type = ExtensionSearch.Type.Fuzz;
    	}

    	extension.search(regEx.getText(), type);
		
		// Select first result
		if (resultsList.getModel().getSize() > 0) {
			resultsList.setSelectedIndex(0);
			resultsList.requestFocus();
		}
    }
    
    protected void setSearchType(ExtensionSearch.Type type) {
    	switch (type) {
    	case All:  	this.getSearchType().setSelectedItem(Constant.messages.getString("search.toolbar.label.type.all")); break;
    	case URL:	this.getSearchType().setSelectedItem(Constant.messages.getString("search.toolbar.label.type.url")); break;
    	case Request:	this.getSearchType().setSelectedItem(Constant.messages.getString("search.toolbar.label.type.request")); break;
    	case Response:	this.getSearchType().setSelectedItem(Constant.messages.getString("search.toolbar.label.type.response")); break;
    	case Header:	this.getSearchType().setSelectedItem(Constant.messages.getString("search.toolbar.label.type.header")); break;
    	case Fuzz:	this.getSearchType().setSelectedItem(Constant.messages.getString("search.toolbar.label.type.fuzz")); break;
    	}
    }
    
    private void displayMessage(SearchResult sr) {
        HttpMessage msg = sr.getMessage();
        // TODO fix?
        if (msg.getRequestHeader().isEmpty()) {
            requestPanel.setMessage(null);
        } else {
            requestPanel.setMessage(msg);
        }
        
        if (msg.getResponseHeader().isEmpty()) {
            responsePanel.setMessage(null);
        } else {
            responsePanel.setMessage(msg);
        }
        highlightFirstResult(sr);
    }
    
    private void highlightMatch (SearchMatch sm) {
    	if (sm == null) {
    		return;
    	}
    	
    	switch (sm.getLocation()) {
    	case REQUEST_HEAD:
    		requestPanel.highlightHeader(sm);
    		requestPanel.setTabFocus();
    		requestPanel.requestFocus(); 
    		break;
    	case REQUEST_BODY:	
    		requestPanel.highlightBody(sm);
    		requestPanel.setTabFocus();
    		requestPanel.requestFocus(); 
    		break;
    	case RESPONSE_HEAD:	
    		responsePanel.highlightHeader(sm);
    		responsePanel.setTabFocus();
    		responsePanel.requestFocus(); 
    		break;
    	case RESPONSE_BODY:
    		responsePanel.highlightBody(sm);
    		responsePanel.setTabFocus();
    		responsePanel.requestFocus(); 
    		break;
    	}

    }
    
    private void highlightFirstResult (SearchResult sr) {
    	highlightMatch(sr.getFirstMatch(requestPanel, responsePanel));
    }
    
    protected void highlightFirstResult() {
		if (resultsList.getModel().getSize() > 0) {
			resultsList.setSelectedIndex(0);
			resultsList.requestFocus();
		}
    }

    protected void highlightNextResult () {
    	if (resultsList.getSelectedValue() == null) {
    		this.highlightFirstResult();
    		return;
    	}
    	
    	SearchResult sr = (SearchResult)resultsList.getSelectedValue();
    	SearchMatch sm = sr.getNextMatch();
    	
    	if (sm != null) {
    		highlightMatch(sm);
    	} else {
    		// Next record
        	if (resultsList.getSelectedIndex() < resultsList.getModel().getSize()) {
        		resultsList.setSelectedIndex(resultsList.getSelectedIndex() + 1);
        	}
    	}
    }

    private void highlightLastResult (SearchResult sr) {
    	
    	highlightMatch(sr.getLastMatch(requestPanel, responsePanel));
    }

    protected void highlightPrevResult () {

    	SearchResult sr = (SearchResult)resultsList.getSelectedValue();
    	if (sr != null) {
	    	SearchMatch sm = sr.getPrevMatch();
	    	
	    	if (sm != null) {
	    		highlightMatch(sm);
	    	} else {
	    		// Previous record
	        	if (resultsList.getSelectedIndex() > 0) {
	        		resultsList.setSelectedIndex(resultsList.getSelectedIndex() - 1);
	        		highlightLastResult((SearchResult)resultsList.getSelectedValue());
	        	}
	    	}
    	} else {
    		this.highlightFirstResult();
    	}
    }

    private JComboBox getSearchType () {
    	if (searchType == null) {
	    	searchType = new JComboBox();
	    	searchType.addItem(Constant.messages.getString("search.toolbar.label.type.all"));
	    	searchType.addItem(Constant.messages.getString("search.toolbar.label.type.url"));
	    	searchType.addItem(Constant.messages.getString("search.toolbar.label.type.request"));
	    	searchType.addItem(Constant.messages.getString("search.toolbar.label.type.response"));
	    	searchType.addItem(Constant.messages.getString("search.toolbar.label.type.header"));
	    	searchType.addItem(Constant.messages.getString("search.toolbar.label.type.fuzz"));
    	}
    	return searchType;
    }
    
    public void searchFocus() {
    	this.setTabFocus();
        getRegExField().requestFocus();

    }

    private SearchPanelCellRenderer getSearchPanelCellRenderer() {
        if (searchPanelCellRenderer == null) {
        	searchPanelCellRenderer = new SearchPanelCellRenderer();
            //searchPanelCellRenderer.setSize(new java.awt.Dimension(328,21));
            searchPanelCellRenderer.setBackground(java.awt.Color.white);
            searchPanelCellRenderer.setFont(new java.awt.Font("MS Sans Serif", java.awt.Font.PLAIN, 12));
        }
        return searchPanelCellRenderer;
    }
}