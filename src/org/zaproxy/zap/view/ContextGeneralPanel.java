package org.zaproxy.zap.view;

import java.awt.CardLayout;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.utils.ZapTextArea;
import org.zaproxy.zap.utils.ZapTextField;

public class ContextGeneralPanel extends AbstractParamPanel {

	private static final long serialVersionUID = -8337361808959321380L;

	private int index;
	private JPanel panelContext = null;
	private ZapTextField txtName = null;
	private ZapTextArea txtDescription = null;
	private JCheckBox chkInScope = null;
	
    public ContextGeneralPanel(String name, int index) {
        super();
        this.setName(name);
        this.index = index;
 		initialize();
   }

    
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setLayout(new CardLayout());
        this.add(getPanelSession(), this.getName() + "gen");
	}
	/**
	 * This method initializes panelSession	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getPanelSession() {
		if (panelContext == null) {
			panelContext = new JPanel();
			panelContext.setLayout(new GridBagLayout());
			
		    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
		    	panelContext.setSize(180, 101);
		    }
			
			panelContext.add(new JLabel(Constant.messages.getString("context.label.name")), 
					LayoutHelper.getGBC(0, 0, 1, 1.0D));
			panelContext.add(getTxtName(), 
					LayoutHelper.getGBC(0, 1, 1, 1.0D));
			panelContext.add(getChkInScope(), 
					LayoutHelper.getGBC(0, 2, 2, 1.0D));
			panelContext.add(new JLabel(Constant.messages.getString("context.label.desc")), 
					LayoutHelper.getGBC(0, 3, 1, 1.0D));
			panelContext.add(getTxtDescription(), 
					LayoutHelper.getGBC(0, 4, 1, 1.0D, 1.0D));
		}
		return panelContext;
	}
	/**
	 * This method initializes txtSessionName	
	 * 	
	 * @return org.zaproxy.zap.utils.ZapTextField	
	 */    
	private ZapTextField getTxtName() {
		if (txtName == null) {
			txtName = new ZapTextField();
		}
		return txtName;
	}
	
	private JCheckBox getChkInScope() {
		if (chkInScope == null) {
			chkInScope = new JCheckBox();
			chkInScope.setText(Constant.messages.getString("context.inscope.label"));
		}
		return chkInScope;
	}
	
	/**
	 * This method initializes txtDescription	
	 * 	
	 * @return org.zaproxy.zap.utils.ZapTextArea	
	 */    
	private ZapTextArea getTxtDescription() {
		if (txtDescription == null) {
			txtDescription = new ZapTextArea();
			txtDescription.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
			txtDescription.setLineWrap(true);
			txtDescription.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
		}
		return txtDescription;
	}
	
	@Override
	public void initParam(Object obj) {
	    Session session = (Session) obj;
	    Context context = session.getContext(this.index);
    	this.setName(context.getName());
	    getTxtName().setText(context.getName());
	    getTxtName().discardAllEdits();
	    getTxtDescription().setText(context.getDescription());
	    getTxtDescription().discardAllEdits();
	    getChkInScope().setSelected(context.isInScope());
	}
	
	@Override
	public void validateParam(Object obj) {
	    // no validation needed
	}
	
	@Override
	public void saveParam (Object obj) throws Exception {
	    Session session = (Session) obj;
	    Context context = session.getContext(this.index);

    	String name = getTxtName().getText();
    	if (! this.getName().equals(name) && View.isInitialised()) {
    		View.getSingleton().renameContext(context);
    		this.setName(name);
    	}
	    context.setName(name);
	    context.setDescription(getTxtDescription().getText());
	    context.setInScope(getChkInScope().isSelected());
	    context.save();
	    
	}


	@Override
	public String getHelpIndex() {
		// ZAP: added help index support
		return "ui.dialogs.sessprop";
	}
	
	public int getContextIndex() {
		return this.index;
	}
	
}
