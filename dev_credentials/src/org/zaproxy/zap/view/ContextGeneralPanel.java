package org.zaproxy.zap.view;

import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.utils.ZapTextArea;
import org.zaproxy.zap.utils.ZapTextField;

public class ContextGeneralPanel extends AbstractContextPropertiesPanel {

	private static final long serialVersionUID = -8337361808959321380L;

	private int index;
	private JPanel panelContext = null;
	private ZapTextField txtName = null;
	private ZapTextArea txtDescription = null;
	private JCheckBox chkInScope = null;
	private Context uiClonedContext;
	
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
			// Store the change in the ui cloned context
			txtName.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent arg0) {
					uiClonedContext.setName(txtName.getText());
				}
				
				@Override
				public void focusGained(FocusEvent arg0) {
				}
			});
		}
		return txtName;
	}
	
	private JCheckBox getChkInScope() {
		if (chkInScope == null) {
			chkInScope = new JCheckBox();
			chkInScope.setText(Constant.messages.getString("context.inscope.label"));
			// Store the change in the ui cloned context
			chkInScope.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent arg0) {
					uiClonedContext.setInScope(chkInScope.isSelected());
				}
			});
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
			// Store the change in the ui common context 
			txtDescription.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent arg0) {
					uiClonedContext.setDescription(txtDescription.getText());
				}
				@Override
				public void focusGained(FocusEvent arg0) {
				}
			});
		}
		return txtDescription;
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.contexts";
	}
	
	public int getContextIndex() {
		return this.index;
	}


	@Override
	public void initContextData(Session session, Context uiContext) {
		this.uiClonedContext=uiContext;
    	this.setName(uiContext.getName());
	    getTxtName().setText(uiContext.getName());
	    getTxtName().discardAllEdits();
	    getTxtDescription().setText(uiContext.getDescription());
	    getTxtDescription().discardAllEdits();
	    getChkInScope().setSelected(uiContext.isInScope());
	}


	@Override
	public void validateContextData(Session session) {
	    // no validation needed
		
	}

	@Override
	public void saveContextData(Session session) {
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
	
}
