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
// ZAP: 2011/10/15 i18n and removed URLs - these are replaced by the regexs in the session properties
// ZAP: 2012/04/14 Changed the method initParam to discard all edits.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate method.
// ZAP: 2012/06/15 Issue 312 Increase the maximum number of scanning threads allowed

package org.parosproxy.paros.extension.spider;

import java.awt.CardLayout;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.spider.SpiderParam;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.ZapTextArea;
import org.zaproxy.zap.view.LayoutHelper;

public class OptionsSpiderPanel extends AbstractParamPanel {

	private static final long serialVersionUID = -5623691753271231473L;
	private JPanel panelSpider = null;
    public OptionsSpiderPanel() {
        super();
 		initialize();
   }
    
	private JSlider sliderMaxDepth = null;
	private JSlider sliderThreads = null;
	private JLabel labelThreadsValue = null;
	private ZapTextArea txtScope = null;
	private JScrollPane jScrollPane = null;
    private JCheckBox chkPostForm = null;
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("spider.options.title"));
	    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
	    	this.setSize(314, 245);
	    }
        this.add(getPanelSpider(), getPanelSpider().getName());
	}
	/**
	 * This method initializes panelSpider	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getPanelSpider() {
		if (panelSpider == null) {
			panelSpider = new JPanel();
			panelSpider.setLayout(new GridBagLayout());
			panelSpider.setName("");
			
		    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
		    	panelSpider.setSize(114, 132);
		    }

			panelSpider.add(new JLabel(Constant.messages.getString("spider.options.label.depth")), 
					LayoutHelper.getGBC(0, 0, 2, 1.0D, 0, java.awt.GridBagConstraints.HORIZONTAL, new java.awt.Insets(2,2,2,2)));
			panelSpider.add(getSliderMaxDepth(), 
					LayoutHelper.getGBC(0, 1, 2, 1.0D, 0, java.awt.GridBagConstraints.HORIZONTAL, new java.awt.Insets(2,2,2,2)));
			panelSpider.add(new JLabel(Constant.messages.getString("spider.options.label.threads")), 
					LayoutHelper.getGBC(0, 2, 1, 1.0D, 0, java.awt.GridBagConstraints.HORIZONTAL, new java.awt.Insets(2,2,2,2)));
			panelSpider.add(getLabelThreadsValue(), 
					LayoutHelper.getGBC(1, 2, 1, 1.0D, 0, java.awt.GridBagConstraints.HORIZONTAL, new java.awt.Insets(2,2,2,2)));
			panelSpider.add(getSliderThreads(), 
					LayoutHelper.getGBC(0, 3, 2, 1.0D, 0, java.awt.GridBagConstraints.HORIZONTAL, new java.awt.Insets(2,2,2,2)));
			panelSpider.add(new JLabel(Constant.messages.getString("spider.options.label.domains")), 
					LayoutHelper.getGBC(0, 4, 2, 1.0D, 0, java.awt.GridBagConstraints.HORIZONTAL, new java.awt.Insets(2,2,2,2)));
			panelSpider.add(getJScrollPane(), 
					LayoutHelper.getGBC(0, 5, 2, 1.0D, 0.3D, java.awt.GridBagConstraints.BOTH, new java.awt.Insets(2,2,2,2)));
			panelSpider.add(getChkPostForm(), 
					LayoutHelper.getGBC(0, 6, 2, 1.0D, 0, java.awt.GridBagConstraints.HORIZONTAL, java.awt.GridBagConstraints.WEST, new java.awt.Insets(2,2,2,2)));
			panelSpider.add(new JLabel(""), 
					LayoutHelper.getGBC(0, 10, 2, 1.0D, 1.0D, java.awt.GridBagConstraints.BOTH, new java.awt.Insets(2,2,2,2)));
		}
		return panelSpider;
	}
	@Override
	public void initParam(Object obj) {
	    OptionsParam options = (OptionsParam) obj;
	    
	    SpiderParam param = (SpiderParam) options.getParamSet(SpiderParam.class);
	    getSliderMaxDepth().setValue(param.getMaxDepth());
	    getSliderThreads().setValue(param.getThread());
	    getTxtScope().setText(param.getScope());
	    getTxtScope().discardAllEdits();
        getChkPostForm().setSelected(param.isPostForm());
	}
	
	@Override
	public void validateParam(Object obj) {
	    // no validation needed
	}
	
	@Override
	public void saveParam (Object obj) throws Exception {
	    OptionsParam options = (OptionsParam) obj;
	    SpiderParam param = (SpiderParam) options.getParamSet(SpiderParam.class);
	    param.setMaxDepth(getSliderMaxDepth().getValue());
	    param.setThread(getSliderThreads().getValue());
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
			sliderMaxDepth.setMaximum(19);
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
			sliderThreads.setMinimum(0);
			sliderThreads.setValue(1);
			sliderThreads.setPaintTicks(true);
			sliderThreads.setPaintLabels(true);
			sliderThreads.setMinorTickSpacing(1);
			sliderThreads.setMajorTickSpacing(5);
			sliderThreads.setSnapToTicks(true);
			sliderThreads.setPaintTrack(true);
			sliderThreads.addChangeListener(new ChangeListener () {
				@Override
				public void stateChanged(ChangeEvent e) {
					// If the minimum is set to 1 then the ticks are at 6, 11 etc
					// But we dont want to support 0 threads, hence this hack
					if (getSliderThreads().getValue() == 0) {
						getSliderThreads().setValue(1);
					}
					setLabelThreadsValue(getSliderThreads().getValue());
				}});
		}
		return sliderThreads;
	}
	
	public int getThreads() {
		return this.getSliderThreads().getValue();
	}
	
	private void setLabelThreadsValue(int value) {
		if (labelThreadsValue == null) {
			labelThreadsValue = new JLabel();
		}
		labelThreadsValue.setText(""+value);
	}

	private JLabel getLabelThreadsValue() {
		if (labelThreadsValue == null) {
			setLabelThreadsValue(getSliderThreads().getValue());
		}
		return labelThreadsValue;
	}

	/**
	 * This method initializes txtScope	
	 * 	
	 * @return org.zaproxy.zap.utils.ZapTextArea	
	 */    
	private ZapTextArea getTxtScope() {
		if (txtScope == null) {
			txtScope = new ZapTextArea();
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
            chkPostForm.setText(Constant.messages.getString("spider.options.label.post"));
        }
        return chkPostForm;
    }
	@Override
	public String getHelpIndex() {
		// ZAP: added help index 
		return "ui.dialogs.options.spider";
	}
    
}  //  @jve:decl-index=0:visual-constraint="10,10"
