/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Original code contributed by Stephen de Vries
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

package org.zaproxy.zap.extension.beanshell;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.parosproxy.paros.extension.AbstractPanel;
import org.zaproxy.zap.utils.ZapTextArea;

import bsh.util.JConsole;


/**
 *
 * Panel to display HTTP request/response with header and body.
 * 
 * Future: to support different view.
 * 
 */
public class BeanShellPanel extends AbstractPanel {   

	private static final long serialVersionUID = -9069040128478670532L;
	
	private JSplitPane splitVert = null;  
	private ZapTextArea txtEditor = null;
	private JPanel panelView = null;
	private JPanel jPanel = null;
	private JPanel panelOption = null;
	private JScrollPane scrollTxtEditor = null;
	private JConsole jConsole = null;
	private boolean saved = true;
		
	/**
	 * This is the default constructor
	 */
	public BeanShellPanel() {
		super();
		initialize();
	}
	
	/**
	 * This method initializes this
	 */
	private  void initialize() {
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

		this.setLayout(new GridBagLayout());
		this.setPreferredSize(new Dimension(400, 300));
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.weighty = 1.0;
		gridBagConstraints1.fill = GridBagConstraints.BOTH;
		gridBagConstraints1.ipadx = 0;
		gridBagConstraints1.ipady = 0;
		gridBagConstraints4.anchor = GridBagConstraints.SOUTHWEST;
		gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
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
	private JSplitPane getSplitVert() {
		if (splitVert == null) {
			splitVert = new JSplitPane();
			splitVert.setDividerLocation(0.5d);
			splitVert.setDividerSize(3);
			splitVert.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitVert.setPreferredSize(new Dimension(400,400));
			splitVert.setResizeWeight(0.5D);
			splitVert.setTopComponent(getConsole());
			splitVert.setContinuousLayout(false);
			splitVert.setBottomComponent(getPanelView());
		}
		return splitVert;
	}

	public ZapTextArea getTxtEditor() {
		if (txtEditor == null) {
			txtEditor = new ZapTextArea();
			txtEditor.setLineWrap(false);
			txtEditor.setName("");
			txtEditor.setTabSize(4);
			txtEditor.setVisible(true);
		    txtEditor.getDocument().addDocumentListener(new EditorDocumentListener());
		}	
		return txtEditor;
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
			panelView.setPreferredSize(new Dimension(278,10));
			panelView.add(getScrollTxtEditor(), "txt");
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
			
			JLabel jLabel = new JLabel();
			
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.weightx = 0.0D;
			gridBagConstraints5.fill = GridBagConstraints.NONE;
			gridBagConstraints5.ipadx = 0;
			gridBagConstraints5.anchor = GridBagConstraints.WEST;
			gridBagConstraints5.insets = new Insets(2,0,2,0);
			
			gridBagConstraints6.anchor = GridBagConstraints.SOUTHEAST;
			gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints6.gridx = 2;
			gridBagConstraints6.gridy = 0;
			gridBagConstraints6.weightx = 1.0D;
			jLabel.setText("      ");
			
			gridBagConstraints7.gridx = 1;
			gridBagConstraints7.gridy = 0;
			gridBagConstraints7.insets = new Insets(2,2,2,2);
			gridBagConstraints7.anchor = GridBagConstraints.WEST;
			
			jPanel.add(jLabel, gridBagConstraints7);
			jPanel.add(getPanelOption(), gridBagConstraints6);
		}
		return jPanel;
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
	 * This method initializes scrollTxtEditor	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getScrollTxtEditor() {
		if (scrollTxtEditor == null) {
			scrollTxtEditor = new JScrollPane();
			scrollTxtEditor.setViewportView(getTxtEditor());
			scrollTxtEditor.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollTxtEditor.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}
		return scrollTxtEditor;
	}
	
	public JConsole getConsole() {
		if (jConsole == null) {
			jConsole = new JConsole();
		}
		return jConsole;
	}
	
	public boolean isSaved() {
		return saved;
	}
	
	public void setSaved(boolean val) {
		saved = val;
	}
	
	private class EditorDocumentListener implements DocumentListener {
		@Override
		public void insertUpdate(DocumentEvent e) {
			update();
		}
		@Override
		public void removeUpdate(DocumentEvent e) {
			update();
		}
		@Override
		public void changedUpdate(DocumentEvent e) {
			update();
		}
		private void update() {
			saved = false;
		}
	}
	
}  
