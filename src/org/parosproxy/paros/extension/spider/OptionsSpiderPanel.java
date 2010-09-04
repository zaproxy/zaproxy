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
package org.parosproxy.paros.extension.spider;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.spider.SpiderParam;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import javax.swing.JCheckBox;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OptionsSpiderPanel extends AbstractParamPanel {

	private JPanel panelSpider = null;  //  @jve:decl-index=0:visual-constraint="520,10"
    public OptionsSpiderPanel() {
        super();
 		initialize();
   }


    
	private JSlider sliderMaxDepth = null;
	private JSlider sliderThreads = null;
	private JTextArea txtScope = null;
	private JScrollPane jScrollPane = null;
    private JCheckBox chkPostForm = null;
    private JLabel jLabel5 = null;
    private JScrollPane jScrollPane1 = null;
    private JTextArea txtSkipURL = null;
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setLayout(new CardLayout());
        this.setName("Spider");
        this.setSize(314, 245);
        this.add(getPanelSpider(), getPanelSpider().getName());
	}
	/**
	 * This method initializes panelSpider	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getPanelSpider() {
		if (panelSpider == null) {
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints21.weighty = 0.7D;
			gridBagConstraints21.gridx = 0;
			gridBagConstraints21.gridy = 6;
			gridBagConstraints21.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints21.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints21.weightx = 1.0;
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints12.gridy = 4;
			gridBagConstraints12.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints12.weightx = 1.0D;
			gridBagConstraints12.gridx = 0;
			jLabel5 = new JLabel();
			jLabel5.setText("URLs to be skipped and not read (CR to separate, '*' as wildcard)");
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints.gridy = 10;
			java.awt.GridBagConstraints gridBagConstraints10 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints8 = new GridBagConstraints();

			javax.swing.JLabel jLabel3 = new JLabel();

			java.awt.GridBagConstraints gridBagConstraints6 = new GridBagConstraints();

			javax.swing.JLabel jLabel2 = new JLabel();

			java.awt.GridBagConstraints gridBagConstraints4 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints3 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

			panelSpider = new JPanel();
			javax.swing.JLabel jLabel1 = new JLabel();

			javax.swing.JLabel jLabel = new JLabel();

			panelSpider.setLayout(new GridBagLayout());
			panelSpider.setSize(114, 132);
			panelSpider.setName("");
			jLabel.setText("Maximum depth to crawl:");
			jLabel1.setText("Number of threads used:");
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.ipadx = 0;
			gridBagConstraints1.ipady = 0;
			gridBagConstraints1.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.weightx = 1.0D;
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.ipadx = 0;
			gridBagConstraints2.ipady = 0;
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints2.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 2;
			gridBagConstraints3.ipadx = 0;
			gridBagConstraints3.ipady = 0;
			gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints3.weightx = 1.0D;
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.gridy = 3;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.ipadx = 0;
			gridBagConstraints4.ipady = 0;
			gridBagConstraints4.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints4.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 11;
			gridBagConstraints6.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints6.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints6.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints6.weightx = 1.0D;
			gridBagConstraints6.weighty = 1.0D;
			jLabel2.setText("");
			jLabel3.setText("Domain suffix included in spider (use ';' to separate, '*' as wildcard)");
			gridBagConstraints8.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints8.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.gridy = 7;
			gridBagConstraints8.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints8.weightx = 1.0D;
			gridBagConstraints10.weightx = 1.0;
			gridBagConstraints10.weighty = 0.3D;
			gridBagConstraints10.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints10.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints10.gridx = 0;
			gridBagConstraints10.gridy = 8;
			gridBagConstraints10.insets = new java.awt.Insets(2,2,2,2);
			panelSpider.add(jLabel, gridBagConstraints1);
			panelSpider.add(getSliderMaxDepth(), gridBagConstraints2);
			panelSpider.add(jLabel1, gridBagConstraints3);
			panelSpider.add(getSliderThreads(), gridBagConstraints4);
			panelSpider.add(jLabel5, gridBagConstraints12);
			panelSpider.add(getJScrollPane1(), gridBagConstraints21);
			panelSpider.add(jLabel3, gridBagConstraints8);
			panelSpider.add(getJScrollPane(), gridBagConstraints10);
			panelSpider.add(getChkPostForm(), gridBagConstraints);
			panelSpider.add(jLabel2, gridBagConstraints6);
		}
		return panelSpider;
	}
	public void initParam(Object obj) {
	    OptionsParam options = (OptionsParam) obj;
	    
	    SpiderParam param = (SpiderParam) options.getParamSet(SpiderParam.class);
	    getSliderMaxDepth().setValue(param.getMaxDepth());
	    getSliderThreads().setValue(param.getThread());
        getTxtSkipURL().setText(param.getSkipURL());
	    getTxtScope().setText(param.getScope());
        getChkPostForm().setSelected(param.isPostForm());
	}
	
	public void validateParam(Object obj) {
	    // no validation needed
	}
	
	public void saveParam (Object obj) throws Exception {
	    OptionsParam options = (OptionsParam) obj;
	    SpiderParam param = (SpiderParam) options.getParamSet(SpiderParam.class);
	    param.setMaxDepth(getSliderMaxDepth().getValue());
	    param.setThread(getSliderThreads().getValue());
        param.setSkipURL(getTxtSkipURL().getText());
        param.setScope(getTxtScope().getText());
        param.setPostForm(getChkPostForm().isSelected());
	}
	
	/**
	 * This method initializes sliderMaxDepth	
	 * 	
	 * @return javax.swing.JSlider	
	 */    
	private JSlider getSliderMaxDepth() {
		if (sliderMaxDepth == null) {
			sliderMaxDepth = new JSlider();
			sliderMaxDepth.setMaximum(9);
			sliderMaxDepth.setMinimum(1);
			sliderMaxDepth.setMinorTickSpacing(1);
			sliderMaxDepth.setPaintTicks(true);
			sliderMaxDepth.setPaintLabels(true);
			sliderMaxDepth.setName("");
			sliderMaxDepth.setMajorTickSpacing(1);
			sliderMaxDepth.setSnapToTicks(true);
			sliderMaxDepth.setPaintTrack(true);
		}
		return sliderMaxDepth;
	}
	/**
	 * This method initializes sliderThreads	
	 * 	
	 * @return javax.swing.JSlider	
	 */    
	private JSlider getSliderThreads() {
		if (sliderThreads == null) {
			sliderThreads = new JSlider();
			sliderThreads.setMaximum(Constant.MAX_HOST_CONNECTION);
			sliderThreads.setMinimum(1);
			sliderThreads.setValue(1);
			sliderThreads.setPaintTicks(true);
			sliderThreads.setPaintLabels(true);
			sliderThreads.setMinorTickSpacing(1);
			sliderThreads.setMajorTickSpacing(1);
			sliderThreads.setSnapToTicks(true);
			sliderThreads.setPaintTrack(true);
		}
		return sliderThreads;
	}
	/**
	 * This method initializes txtScope	
	 * 	
	 * @return javax.swing.JTextArea	
	 */    
	private JTextArea getTxtScope() {
		if (txtScope == null) {
			txtScope = new JTextArea();
			txtScope.setLineWrap(true);
			txtScope.setRows(3);
			txtScope.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
		}
		return txtScope;
	}
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setPreferredSize(new java.awt.Dimension(294,30));
			jScrollPane.setViewportView(getTxtScope());
		}
		return jScrollPane;
	}
    /**
     * This method initializes chkFormFill	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getChkPostForm() {
        if (chkPostForm == null) {
            chkPostForm = new JCheckBox();
            chkPostForm.setText("POST forms (recommended but may generate multiple unwanted requests)");
        }
        return chkPostForm;
    }
    /**
     * This method initializes jScrollPane1	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getJScrollPane1() {
        if (jScrollPane1 == null) {
            jScrollPane1 = new JScrollPane();
            jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            jScrollPane1.setViewportView(getTxtSkipURL());
        }
        return jScrollPane1;
    }
    /**
     * This method initializes jTextArea	
     * 	
     * @return javax.swing.JTextArea	
     */
    private JTextArea getTxtSkipURL() {
        if (txtSkipURL == null) {
            txtSkipURL = new JTextArea();
            txtSkipURL.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 11));
            txtSkipURL.setSize(new java.awt.Dimension(290,52));
        }
        return txtSkipURL;
    }
 }  //  @jve:decl-index=0:visual-constraint="10,10"
