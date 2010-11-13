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
import java.awt.Color;
import java.awt.GridBagConstraints;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.HttpPanel;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SearchPanel extends AbstractPanel {
	
	private static final long serialVersionUID = 1L;

	public static final String PANEL_NAME = "search";

	private ExtensionSearch extension;
	
	private javax.swing.JPanel panelCommand = null;
	private javax.swing.JToolBar panelToolbar = null;
	private JScrollPane jScrollPane = null;

	private JTextField regEx = null;
	private JButton btnSearch = null;
	private JComboBox searchType = null;
	private JButton btnNext = null;
	private JButton btnPrev = null;
	
	private JList resultsList = new JList();
	private DefaultListModel resultsModel;

	private HttpPanel requestPanel = null;
	private HttpPanel responsePanel = null;

    private SearchPanelCellRenderer searchPanelCellRenderer = null;
    private static Log log = LogFactory.getLog(SearchPanel.class);

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
        this.setSize(474, 251);
        this.setName(Constant.messages.getString("search.panel.title"));
		this.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/049.png")));	// 'magnifying glass' icon
        this.add(getPanelCommand(), getPanelCommand().getName());

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

	protected JTextField getRegExField () {
		if (regEx == null) {
			regEx = new JTextField();
			regEx.setHorizontalAlignment(javax.swing.JTextField.LEFT);
			regEx.setAlignmentX(0.0F);
			regEx.setPreferredSize(new java.awt.Dimension(250,25));
			regEx.setText("");
			regEx.setMinimumSize(new java.awt.Dimension(250,25));
			//regEx.addActionListener(this);
			
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
	}

    public void setDisplayPanel(HttpPanel requestPanel, HttpPanel responsePanel) {
        this.requestPanel = requestPanel;
        this.responsePanel = responsePanel;

    }
    
    private void doSearch() {
		extension.search(regEx.getText(), 
				(ExtensionSearch.Type)searchType.getSelectedItem());
		
		// Select first result
		if (resultsList.getModel().getSize() > 0) {
			resultsList.setSelectedIndex(0);
			resultsList.requestFocus();
		}
    }
    
    protected void setSearchType(ExtensionSearch.Type type) {
    	this.getSearchType().setSelectedItem(type);
    }
    
    private void displayMessage(SearchResult sr) {
        HttpMessage msg = sr.getMessage();
        if (msg.getRequestHeader().isEmpty()) {
            requestPanel.setMessage(null, true);
        } else {
            requestPanel.setMessage(msg, true);
        }
        
        if (msg.getResponseHeader().isEmpty()) {
            responsePanel.setMessage(null, false);
        } else {
            responsePanel.setMessage(msg, false);
        }
        highlightFirstResult(sr);
    }
    
    private void highlightMatch (SearchMatch sm) {
    	JTextArea txtArea = null;
    	if (sm == null) {
    		return;
    	}
    	
    	switch (sm.getLocation()) {
    	case REQUEST_HEAD:	
    		txtArea = requestPanel.getTxtHeader();
    		requestPanel.setTabFocus();
    		requestPanel.requestFocus(); 
    		break;
    	case REQUEST_BODY:	
    		txtArea = requestPanel.getTxtBody();
    		requestPanel.setTabFocus();
    		requestPanel.requestFocus(); 
    		break;
    	case RESPONSE_HEAD:	
    		txtArea = responsePanel.getTxtHeader();
    		responsePanel.setTabFocus();
    		responsePanel.requestFocus(); 
    		break;
    	case RESPONSE_BODY:	
    		txtArea = responsePanel.getTxtBody();
    		responsePanel.setTabFocus();
    		responsePanel.requestFocus(); 
    		break;
    	}
        
        Highlighter hilite = txtArea.getHighlighter();
        HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.LIGHT_GRAY);
        try {
			hilite.removeAllHighlights();
			hilite.addHighlight(sm.getStart(), sm.getEnd(), painter);
			txtArea.setCaretPosition(sm.getStart());
		} catch (BadLocationException e) {
			log.error(e.getMessage(), e);
		}
    }
    
    private void highlightFirstResult (SearchResult sr) {
    	highlightMatch(sr.getFirstMatch(requestPanel, responsePanel));
    }

    protected void highlightNextResult () {
    	if (resultsList.getSelectedValue() == null) {
        	searchFocus();
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
        	searchFocus();
    	}
    }

    private JComboBox getSearchType () {
    	if (searchType == null) {
	    	searchType = new JComboBox();
	    	searchType.addItem(ExtensionSearch.Type.All);
	    	searchType.addItem(ExtensionSearch.Type.URL);
	    	searchType.addItem(ExtensionSearch.Type.Request);
	    	searchType.addItem(ExtensionSearch.Type.Response);
	    	searchType.addItem(ExtensionSearch.Type.Header);
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
            searchPanelCellRenderer.setSize(new java.awt.Dimension(328,21));
            searchPanelCellRenderer.setBackground(java.awt.Color.white);
            searchPanelCellRenderer.setFont(new java.awt.Font("MS Sans Serif", java.awt.Font.PLAIN, 12));
        }
        return searchPanelCellRenderer;
    }
}
