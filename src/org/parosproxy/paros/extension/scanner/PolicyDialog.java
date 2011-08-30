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
// ZAP: 2011/08/30 Support for scanner levels

package org.parosproxy.paros.extension.scanner;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.core.scanner.PluginFactory;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.AbstractParamDialog;
import org.parosproxy.paros.view.AbstractParamPanel;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PolicyDialog extends AbstractParamDialog {

    private static final String[] ROOT = {};
	private PolicyCategoryPanel policyCategoryPanel = null;  //  @jve:decl-index=0:visual-constraint="431,185"
	private PolicyAllCategoryPanel policyAllCategoryPanel = null;

	private List<AbstractParamPanel> additionalPanels = new ArrayList<AbstractParamPanel>();

	public PolicyDialog() {
        super();
        initialize();
        
    }
    
    public PolicyDialog(Frame parent) throws HeadlessException {
        super(parent, true, "Policy", Constant.messages.getString("ascan.policy.title"));
        initialize();
    }

    private void initialize() {
        this.setTitle("Policy");
	    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
                this.setSize(550, 400);
	    }
        addParamPanel(null, getPolicyAllCategoryPanel(), false);
        for (int i=0; i<Category.getAllNames().length; i++) {
            addParamPanel(ROOT, Category.getName(i), getPolicyCategoryPanel(), true);
        }
        getBtnCancel().setEnabled(false);
    }
    
    public void addPolicyPanel (AbstractParamPanel panel) {
    	this.additionalPanels.add(panel);
        addParamPanel(ROOT, panel.getName(), panel, true);
    }
    
	/**
	 * This method initializes policyCategoryPanel	
	 * 	
	 * @return com.proofsecure.paros.plugin.scanner.PolicyCategoryPanel	
	 */    
	private PolicyCategoryPanel getPolicyCategoryPanel() {
		if (policyCategoryPanel == null) {
			policyCategoryPanel = new PolicyCategoryPanel();
		}
		return policyCategoryPanel;
	}
	
	private void categorySelected(String name) {
	    int category = Category.getCategory(name);
	    getPolicyCategoryPanel().getCategoryTableModel().setTable(category, PluginFactory.getAllPlugin());
	}
	
@Override
	public void showParamPanel(String name) {
	    super.showParamPanel(name);
	    if (!name.equals(this.getRootNode().toString())) {
	    	// Check addn ones first
	    	boolean found = false;
	    	for (AbstractParamPanel panel : this.additionalPanels) {
	    		if (name.equals(panel.getName())) {
	    			found = true;
	    		}
	    	}
	    	if (! found) {
	    		categorySelected(name);
	    	}
	    } else {
	    }
	}
	/**
	 * This method initializes policyAllCategoryPanel	
	 * 	
	 * @return com.proofsecure.paros.extension.scanner.PolicyAllCategoryPanel	
	 */    
	private PolicyAllCategoryPanel getPolicyAllCategoryPanel() {
		if (policyAllCategoryPanel == null) {
			policyAllCategoryPanel = new PolicyAllCategoryPanel();
			policyAllCategoryPanel.setName(Constant.messages.getString("ascan.policy.title"));
		}
		return policyAllCategoryPanel;
	}
  }
