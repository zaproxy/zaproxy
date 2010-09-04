/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2005 Chinotec Technologies Company
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
package org.parosproxy.paros.extension.trap;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;

import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OptionsTrapPanel extends AbstractParamPanel {

	private TrapFilterPanel trapFilterPanel = null;
	private JLabel jLabel = null;
    /**
     * 
     */
    public OptionsTrapPanel() {
        super();
		initialize();
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        jLabel = new JLabel();
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        this.setLayout(new GridBagLayout());
        this.setName("Trap");
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.insets = new java.awt.Insets(2,2,2,2);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.weightx = 1.0D;
        jLabel.setText(" ");
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.insets = new java.awt.Insets(2,2,2,2);
        gridBagConstraints2.weightx = 1.0D;
        gridBagConstraints2.weighty = 1.0D;
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
        this.add(getTrapFilterPanel(), gridBagConstraints1);
        this.add(jLabel, gridBagConstraints2);
			
	}
    /* (non-Javadoc)
     * @see org.parosproxy.paros.view.AbstractParamPanel#initParam(java.lang.Object)
     */
    public void initParam(Object obj) {
        
	    OptionsParam optionsParam = (OptionsParam) obj;
	    TrapParam trapParam = (TrapParam) optionsParam.getParamSet(TrapParam.class);
	    
	    if (trapParam.getInclusiveFilter().equals("")) {
	        getTrapFilterPanel().getChkEnableInclusiveFilter().setSelected(false);
	        getTrapFilterPanel().setInclusiveFilter(false);
	    } else {
	        getTrapFilterPanel().getChkEnableInclusiveFilter().setSelected(true);
	        getTrapFilterPanel().setInclusiveFilter(true);
	    }
        getTrapFilterPanel().getTxtInclusiveFilter().setText(trapParam.getInclusiveFilter());

	    if (trapParam.getExclusiveFilter().equals("")) {
	        getTrapFilterPanel().getChkEnableExclusiveFilter().setSelected(false);
	        getTrapFilterPanel().setExclusiveFilter(false);
	    } else {
	        getTrapFilterPanel().getChkEnableExclusiveFilter().setSelected(true);
	        getTrapFilterPanel().setExclusiveFilter(true);
	    }
        getTrapFilterPanel().getTxtExclusiveFilter().setText(trapParam.getExclusiveFilter());		    


    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.view.AbstractParamPanel#validateParam(java.lang.Object)
     */
    public void validateParam(Object obj) throws Exception {

    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.view.AbstractParamPanel#saveParam(java.lang.Object)
     */
    public void saveParam(Object obj) throws Exception {
        
        OptionsParam optionsParam = (OptionsParam) obj;
        TrapParam trapParam = (TrapParam) optionsParam.getParamSet(TrapParam.class);
	    
//        if (getTrapFilterPanel().getChkEnableInclusiveFilter().isSelected()) {
            try {
                String s = getTrapFilterPanel().getTxtInclusiveFilter().getText();
                trapParam.setInclusiveFilter(s); 
            } catch (Exception e) {
                getTrapFilterPanel().getTxtInclusiveFilter().requestFocus();
                e.printStackTrace();
                throw new Exception("Invalid pattern.");
                
            }
//        }
        
//        if (getTrapFilterPanel().getChkEnableExclusiveFilter().isSelected()) {
            try {
                String s = getTrapFilterPanel().getTxtExclusiveFilter().getText();
                trapParam.setExclusiveFilter(s);
            } catch (Exception e) {
                getTrapFilterPanel().getTxtExclusiveFilter().requestFocus();
                throw new Exception("Invalid pattern.");
                
            }
//        }

    }

	/**
	 * This method initializes trapFilterPanel	
	 * 	
	 * @return org.parosproxy.paros.extension.trap.TrapFilterPanel	
	 */    
	private TrapFilterPanel getTrapFilterPanel() {
		if (trapFilterPanel == null) {
			trapFilterPanel = new TrapFilterPanel();
		}
		return trapFilterPanel;
	}
 }
