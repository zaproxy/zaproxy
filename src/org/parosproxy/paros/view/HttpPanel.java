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
package org.parosproxy.paros.view;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.InputEvent;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.network.HttpMessage;

/**
 *
 * Panel to display HTTP request/response with header and body.
 * 
 * Future: to support different view.
 * 
 */
public class HttpPanel extends AbstractPanel {
    
    private static final String VIEW_RAW = "Raw View    ";
    private static final String VIEW_TABULAR = "Tabular View";
    private static final String VIEW_IMAGE = "Image View";
    
	private javax.swing.JSplitPane splitVert = null;  //
	private javax.swing.JScrollPane scrollHeader = null;
	private javax.swing.JScrollPane scrollTableBody = null;
	private javax.swing.JTextArea txtHeader = null;
	private javax.swing.JTextArea txtBody = null;
	private JLabel lblIcon = null;
	private JPanel panelView = null;
	private JPanel jPanel = null;
	private JComboBox comboView = null;
	private JPanel panelOption = null;
	private JTable tableBody = null;
	private HttpPanelTabularModel httpPanelTabularModel = null;  //  @jve:decl-index=0:parse,visual-constraint="425,147"
	private JScrollPane scrollTxtBody = null;
	private String currentView = VIEW_RAW;
	
	
	private JScrollPane scrollImage = null;
	/**
	 * This is the default constructor
	 */
	public HttpPanel() {
		super();
		initialize();
	}
	
	public HttpPanel(boolean isEditable) {
		this();
		getTxtHeader().setEditable(isEditable);
		getTxtBody().setEditable(isEditable);
		getHttpPanelTabularModel().setEditable(isEditable);	
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private  void initialize() {
		java.awt.GridBagConstraints gridBagConstraints4 = new GridBagConstraints();

		java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

		this.setLayout(new GridBagLayout());
		this.setSize(403, 296);
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.weighty = 1.0;
		gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints1.ipadx = 0;
		gridBagConstraints1.ipady = 0;
		gridBagConstraints4.anchor = java.awt.GridBagConstraints.SOUTHWEST;
		gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints4.gridx = 0;
		gridBagConstraints4.gridy = 1;
		gridBagConstraints4.weightx = 1.0D;
		this.add(getSplitVert(), gridBagConstraints1);
		this.add(getJPanel(), gridBagConstraints4);
	}
	/**

	 * This method initializes jSplitPane	

	 * 	

	 * @return javax.swing.JSplitPane	

	 */    
	private javax.swing.JSplitPane getSplitVert() {
		if (splitVert == null) {
			splitVert = new javax.swing.JSplitPane();
			splitVert.setDividerLocation(220);
			splitVert.setDividerSize(3);
			splitVert.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
			splitVert.setPreferredSize(new java.awt.Dimension(400,400));
			splitVert.setResizeWeight(0.5D);
			splitVert.setTopComponent(getScrollHeader());
			splitVert.setContinuousLayout(false);
			splitVert.setBottomComponent(getPanelView());
		}
		return splitVert;
	}

	/**

	 * This method initializes scrollHeader	

	 * 	

	 * @return javax.swing.JScrollPane	

	 */    
	private javax.swing.JScrollPane getScrollHeader() {
		if (scrollHeader == null) {
			scrollHeader = new javax.swing.JScrollPane();
			scrollHeader.setViewportView(getTxtHeader());
		}
		return scrollHeader;
	}

	/**

	 * This method initializes scrollTableBody	

	 * 	

	 * @return javax.swing.JScrollPane	

	 */    
	private javax.swing.JScrollPane getScrollTableBody() {
		if (scrollTableBody == null) {
			scrollTableBody = new javax.swing.JScrollPane();
			scrollTableBody.setName(VIEW_TABULAR);
			scrollTableBody.setViewportView(getTableBody());
		}
		return scrollTableBody;
	}

	/**

	 * This method initializes txtHeader	

	 * 	

	 * @return javax.swing.JTextArea	

	 */    
	public javax.swing.JTextArea getTxtHeader() {
		if (txtHeader == null) {
			txtHeader = new javax.swing.JTextArea();
			txtHeader.setLineWrap(true);
			txtHeader.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
			txtHeader.setName("");
			txtHeader.addMouseListener(new java.awt.event.MouseAdapter() { 
				public void mousePressed(java.awt.event.MouseEvent e) {    
				    if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {  // right mouse button
				        View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
				    }				
				}
			});
		}
		return txtHeader;
	}

	/**

	 * This method initializes txtBody	

	 * 	

	 * @return javax.swing.JTextArea	

	 */    
	public javax.swing.JTextArea getTxtBody() {
		if (txtBody == null) {
			txtBody = new javax.swing.JTextArea();
			txtBody.setLineWrap(true);
			txtBody.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
			txtBody.setName("");
			txtBody.setTabSize(4);
			txtBody.setVisible(true);
		    txtBody.addMouseListener(new java.awt.event.MouseAdapter() { 
		    	public void mousePressed(java.awt.event.MouseEvent e) {    
				    if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {  // right mouse button
				        View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
				    }			    	
				}
		    });
		}
		
	    if (currentView.equals(VIEW_TABULAR)) {
            String s = getHttpPanelTabularModel().getText();
            if (s != null && s.length() > 0) {
                txtBody.setText(s);
            }
		}
		
		return txtBody;
	}

	/**
	 * This method initializes panelView	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getPanelView() {
		if (panelView == null) {
			panelView = new JPanel();
			panelView.setLayout(new CardLayout());
			panelView.setPreferredSize(new java.awt.Dimension(278,10));
			panelView.add(getScrollTxtBody(), getScrollTxtBody().getName());
			panelView.add(getScrollImage(), getScrollImage().getName());
			panelView.add(getScrollTableBody(), getScrollTableBody().getName());
			show(VIEW_RAW);
		}
		return panelView;
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			java.awt.GridBagConstraints gridBagConstraints7 = new GridBagConstraints();

			javax.swing.JLabel jLabel = new JLabel();

			java.awt.GridBagConstraints gridBagConstraints6 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints5 = new GridBagConstraints();

			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.weightx = 0.0D;
			gridBagConstraints5.fill = java.awt.GridBagConstraints.NONE;
			gridBagConstraints5.ipadx = 0;
			gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints5.insets = new java.awt.Insets(2,0,2,0);
			gridBagConstraints6.anchor = java.awt.GridBagConstraints.SOUTHEAST;
			gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints6.gridx = 2;
			gridBagConstraints6.gridy = 0;
			gridBagConstraints6.weightx = 1.0D;
			jLabel.setText("      ");
			gridBagConstraints7.gridx = 1;
			gridBagConstraints7.gridy = 0;
			gridBagConstraints7.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints7.anchor = java.awt.GridBagConstraints.WEST;
			jPanel.add(getComboView(), gridBagConstraints5);
			jPanel.add(jLabel, gridBagConstraints7);
			jPanel.add(getPanelOption(), gridBagConstraints6);
		}
		return jPanel;
	}
	/**
	 * This method initializes comboView	
	 * 	
	 * @return javax.swing.JComboBox	
	 */    
	private JComboBox getComboView() {
		if (comboView == null) {
			comboView = new JComboBox();
			comboView.setSelectedIndex(-1);
			comboView.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    

				    String item = (String) comboView.getSelectedItem();
				    if (item == null || item.equals(currentView)) {
				        // no change
				        return;
				    }
				    
			        if (currentView.equals(VIEW_TABULAR)) {
			            // do not use getTxtBody() here to avoid setting text
                        String s = getHttpPanelTabularModel().getText();
				        if (s != null && s.length() > 0) {
                            // set only if model is not empty because binary data not work for tabularModel
                            txtBody.setText(s);
                        }
				    }
				    
				    if (item.equals(VIEW_TABULAR)) {
				        getHttpPanelTabularModel().setText(getTxtBody().getText());
				    }
				    
				    currentView = item;
				    show(item);
				}
			});

			comboView.addItem(VIEW_RAW);
			comboView.addItem(VIEW_TABULAR);

			

		}
		return comboView;
	}
	/**
	 * This method initializes panelOption	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	public JPanel getPanelOption() {
		if (panelOption == null) {
			panelOption = new JPanel();
			panelOption.setLayout(new CardLayout());
		}
		return panelOption;
	}
	/**
	 * This method initializes tableBody	
	 * 	
	 * @return javax.swing.JTable	
	 */    
	private JTable getTableBody() {
		if (tableBody == null) {
			tableBody = new JTable();
			tableBody.setName("");
			tableBody.setModel(getHttpPanelTabularModel());

			tableBody.setGridColor(java.awt.Color.gray);
			tableBody.setIntercellSpacing(new java.awt.Dimension(1,1));
			tableBody.setRowHeight(18);
		}
		return tableBody;
	}
	/**
	 * This method initializes httpPanelTabularModel	
	 * 	
	 * @return com.proofsecure.paros.view.HttpPanelTabularModel	
	 */    
	private HttpPanelTabularModel getHttpPanelTabularModel() {
		if (httpPanelTabularModel == null) {
			httpPanelTabularModel = new HttpPanelTabularModel();
		}
		return httpPanelTabularModel;
	}
	/**
	 * This method initializes scrollTxtBody	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getScrollTxtBody() {
		if (scrollTxtBody == null) {
			scrollTxtBody = new JScrollPane();
			scrollTxtBody.setName(VIEW_RAW);
			scrollTxtBody.setViewportView(getTxtBody());
			scrollTxtBody.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		}
		return scrollTxtBody;
	}
	
	private void show(String viewName) {
		CardLayout card = (CardLayout) getPanelView().getLayout();
		card.show(getPanelView(), viewName);
	    
	}
	
	public void setMessage(String header, String body, boolean enableViewSelect) {
	    getComboView().setEnabled(enableViewSelect);

        javax.swing.JTextArea txtBody = getTxtBody();
        
        this.validate();
        if (enableViewSelect) {
	        getHttpPanelTabularModel().setText(body);
	    } else {
		    getComboView().setSelectedItem(VIEW_RAW);
		    currentView = VIEW_RAW;

	        show(VIEW_RAW);
	        getHttpPanelTabularModel().setText("");
	    }

	    getTxtHeader().setText(header);
        getTxtHeader().setCaretPosition(0);

        txtBody.setText(body);
        txtBody.setCaretPosition(0);

	}
	
	public void setMessage(HttpMessage msg, boolean isRequest) {

	    javax.swing.JTextArea txtBody = getTxtBody();

	    getComboView().removeAllItems();
	    getComboView().setEnabled(false);
	    getComboView().addItem(VIEW_RAW);
	    
	    if (msg == null) {
	        // perform clear display
		    getTxtHeader().setText("");
		    getTxtHeader().setCaretPosition(0);

	        txtBody.setText("");
	        txtBody.setCaretPosition(0);

	        getComboView().setSelectedItem(VIEW_RAW);
		    currentView = VIEW_RAW;

	        show(VIEW_RAW);
	        getHttpPanelTabularModel().setText("");
	        return;
	    }
	    
	    if (isRequest) {
	        setDisplayRequest(msg);
	        
	    } else {
	        setDisplayResponse(msg);
	    }
        this.validate();
	    
	}

	private void setDisplayRequest(HttpMessage msg) {

	    String header = replaceHeaderForJTextArea(msg.getRequestHeader().toString());
	    String body = msg.getRequestBody().toString();
	    
	    getHttpPanelTabularModel().setText(msg.getRequestBody().toString());

	    getTxtHeader().setText(header);
        getTxtHeader().setCaretPosition(0);

        txtBody.setText(body);
        txtBody.setCaretPosition(0);

        getComboView().addItem(VIEW_TABULAR);
	    getComboView().setEnabled(true);

	}
	
	private void setDisplayResponse(HttpMessage msg) {
	    
	    
	    if (msg.getResponseHeader().isEmpty()) {
		    getTxtHeader().setText("");
		    getTxtHeader().setCaretPosition(0);

	        txtBody.setText("");
	        txtBody.setCaretPosition(0);
	        
	        getLblIcon().setIcon(null);
	        return;
	    }
	    
	    String header = replaceHeaderForJTextArea(msg.getResponseHeader().toString());
	    String body = msg.getResponseBody().toString();

	    getTxtHeader().setText(header);
	    getTxtHeader().setCaretPosition(0);
        
        txtBody.setText(body);
        txtBody.setCaretPosition(0);

        getComboView().removeAllItems();
        getComboView().addItem(VIEW_RAW);

	    getComboView().setEnabled(true);

	    if (msg.getResponseHeader().isImage()) {
	        getComboView().addItem(VIEW_IMAGE);
	        getLblIcon().setIcon(getImageIcon(msg));

	    }
	    
	    if (msg.getResponseHeader().isImage()) {
		    getComboView().setSelectedItem(VIEW_IMAGE);	        
	    } else {
		    getComboView().setSelectedItem(VIEW_RAW);	        	        
	    }

	    
	}
	
	private String getHeaderFromJTextArea(JTextArea txtArea) {
		
		String msg = txtArea.getText();
		String result = msg.replaceAll("\\n", "\r\n");
		result = result.replaceAll("(\\r\\n)*\\z", "") + "\r\n\r\n";
		return result;
	}
	
	private String replaceHeaderForJTextArea(String msg) {
		return msg.replaceAll("\\r\\n", "\n");
	}
	
	public void getMessage(HttpMessage msg, boolean isRequest) {
	    try {
	        if (isRequest) {
	            if (getTxtHeader().getText().length() == 0) {
	                msg.getRequestHeader().clear();
	                msg.getRequestBody().setBody("");
	            } else {
	                msg.getRequestHeader().setMessage(getHeaderFromJTextArea(getTxtHeader()));
	                msg.getRequestBody().setBody(getTxtBody().getText());
	                msg.getRequestHeader().setContentLength(msg.getRequestBody().length());
	            }
	        } else {
	            if (getTxtHeader().getText().length() == 0) {
	                msg.getResponseHeader().clear();
	                msg.getResponseBody().setBody("");
	            } else {
	                msg.getResponseHeader().setMessage(getHeaderFromJTextArea(getTxtHeader()));
	                String txt = getTxtBody().getText();
	                msg.getResponseBody().setBody(txt);
	                msg.getResponseHeader().setContentLength(msg.getResponseBody().length());
	            }
	        }
	    } catch (Exception e) {
	    }

	}
	
	
	/**
	 * This method initializes scrollImage	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getScrollImage() {
		if (scrollImage == null) {
			
			scrollImage = new JScrollPane();
			scrollImage.setName(VIEW_IMAGE);
			scrollImage.setViewportView(getLblIcon());
		}
		return scrollImage;
	}
	
	private JLabel getLblIcon() {
	    if (lblIcon == null) {
			lblIcon = new JLabel();
			lblIcon.setText("");

			lblIcon.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			lblIcon.setBackground(java.awt.SystemColor.text);
	    }
	    return lblIcon;
	}
	private ImageIcon getImageIcon(HttpMessage msg) {
	    ImageIcon image = new ImageIcon(msg.getResponseBody().getBytes());
	    return image;
	}
	
  }  //  @jve:decl-index=0:visual-constraint="10,10"
