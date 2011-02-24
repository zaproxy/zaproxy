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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.history.ManualRequestEditorDialog;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.extension.tab.Tab;
import org.zaproxy.zap.httputils.RequestUtils;
import org.zaproxy.zap.view.HttpPanelManager;
import org.zaproxy.zap.view.HttpPanelView;

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

	private javax.swing.JSplitPane contentSplit = null;  //
	private javax.swing.JScrollPane scrollHeader = null;
	private javax.swing.JScrollPane scrollTableBody = null;
	private javax.swing.JTextArea txtHeader = null;
	private javax.swing.JTextArea txtBody = null;

	private JLabel lblIcon = null;
	private JPanel panelView = null;
	private JComboBox comboView = null;
	private JPanel panelOption = null;
	private JTable tableBody = null;
	private HttpPanelTabularModel httpPanelTabularModel = null;  //  @jve:decl-index=0:parse,visual-constraint="425,147"
	private JScrollPane scrollTxtBody = null;
	private String currentView = VIEW_RAW;

	// ZAP: Support plugable views
	private List <HttpPanelView> views = new ArrayList<HttpPanelView>();
	private boolean editable = false;

	private JScrollPane scrollImage = null;
	private Extension extension = null;

	private JPanel panelHeader;
//	protected ManualRequestEditorDialog requestEditor;

	private HttpSender httpSender = null;

	/*** Constructors ***/
	
	/**
	 * This is the default constructor
	 */
	public HttpPanel() {
		super();
		initialize();

		HttpPanelManager.getInstance().addPanel(this);
	}

	public HttpPanel(boolean isEditable) {
		this();
		this.editable = isEditable;

		getTxtHeader().setEditable(isEditable);
		getTxtBody().setEditable(isEditable);
		getHttpPanelTabularModel().setEditable(isEditable);
	}

	public HttpPanel(boolean isEditable, Extension extension) {
		this(isEditable);
		this.extension = extension;
	}

	/**
	 * This method initializes this Window.
	 * 
	 * @return void
	 */
	private  void initialize() {
		this.setLayout(new BorderLayout());
		this.add(getPanelHeader(), BorderLayout.NORTH);
		this.add(getSplitPane(), BorderLayout.CENTER);
	}

	/*** View Functions ***/
	
	/**
	 * This method initializes the header, aka toolbar
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getPanelHeader() {
		if (panelHeader == null) {
		java.awt.GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
		java.awt.GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
		java.awt.GridBagConstraints gridBagConstraints5 = new GridBagConstraints();

		javax.swing.JLabel jLabel = new JLabel();
		panelHeader = new JPanel();
		panelHeader.setLayout(new GridBagLayout());
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

		panelHeader.add(getComboView(), gridBagConstraints5);
		panelHeader.add(jLabel, gridBagConstraints7);
		panelHeader.add(getPanelOption(), gridBagConstraints6);
		}
		
		return panelHeader;
	}

	/**
	 * This method initializes jSplitPane, the content
	 * 	
	 * @return javax.swing.JSplitPane	
	 */    
	private JSplitPane getSplitPane() {
		if (contentSplit == null) {
		contentSplit = new javax.swing.JSplitPane();
		contentSplit.setDividerLocation(220);
		contentSplit.setDividerSize(3);
		contentSplit.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		//splitVert.setPreferredSize(new java.awt.Dimension(400,400));
		contentSplit.setResizeWeight(0.5D);
		contentSplit.setContinuousLayout(false);
		contentSplit.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		contentSplit.setTopComponent(getScrollHeader());			
		contentSplit.setBottomComponent(getPanelView());
		}
		return contentSplit;
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
	 * This method initializes comboView	
	 * 	
	 * @return javax.swing.JComboBox	
	 */    
	protected JComboBox getComboView() {
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
					} else {
						// ZAP: Support plugable views
						for (HttpPanelView view : views) {
							if (currentView.equals(view.getName())) {
								if (view.hasChanged()) {
									txtBody.setText(view.getContent());
								}
								break;
							}
						}
					}

					if (item.equals(VIEW_TABULAR)) {
						getHttpPanelTabularModel().setText(getTxtBody().getText());
					} else {
						// ZAP: Support plugable views
						for (HttpPanelView view : views) {
							if (item.equals(view.getName())) {
								view.setContent(getTxtBody().getText());
								break;
							}
						}
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

	protected void setCurrentView(String view) {
		currentView = view;
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
	protected HttpPanelTabularModel getHttpPanelTabularModel() {
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

	protected void show(String viewName) {
		CardLayout card = (CardLayout) getPanelView().getLayout();
		card.show(getPanelView(), viewName);

	}

	protected void pluggableView(HttpMessage msg) {
		// ZAP: Support plugable views
		for (HttpPanelView view : views) {
			if (view.isEnabled(msg)) {
				view.setEditable(editable);
				getComboView().addItem(view.getName());
			}
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

	protected JLabel getLblIcon() {
		if (lblIcon == null) {
			lblIcon = new JLabel();
			lblIcon.setText("");

			lblIcon.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			lblIcon.setBackground(java.awt.SystemColor.text);
		}
		return lblIcon;
	}
	
	protected ImageIcon getImageIcon(HttpMessage msg) {
		ImageIcon image = new ImageIcon(msg.getResponseBody().getBytes());
		return image;
	}

	
	/*** Data Functions ***/
	
	protected String getHeaderFromJTextArea(JTextArea txtArea) {
		String msg = txtArea.getText();
		String result = msg.replaceAll("\\n", "\r\n");
		result = result.replaceAll("(\\r\\n)*\\z", "") + "\r\n\r\n";
		return result;
	}

	protected String replaceHeaderForJTextArea(String msg) {
		return msg.replaceAll("\\r\\n", "\n");
	}
	
	abstract public void getMessage(HttpMessage msg, boolean isRequest);
	abstract protected void setDisplay(HttpMessage msg);

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

		//TODO        getBtnSend().setEnabled(true);
	}

	public void setMessage(HttpMessage msg) {
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
			setCurrentView(VIEW_RAW);
			show(VIEW_RAW);
			
			getHttpPanelTabularModel().setText("");
			return;
		}

		setDisplay(msg);
		
		this.validate();

		//TODO        getBtnSend().setEnabled(true);
	}
	
	// ZAP: Support plugable views
	public void addView (HttpPanelView view) {
		view.setEditable(editable);
		this.views.add(view);
		panelView.add(view.getPane(), view.getPane().getName());
	}

	public boolean isEditable() {
		return editable;
	}

	public void setExtension(Extension extension) {
		this.extension = extension;
	}

	public Extension getExtention() {
		return extension;
	}

}
