package org.zaproxy.zap.view;

import java.awt.CardLayout;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.utils.ZapTextArea;
import org.zaproxy.zap.utils.ZapTextField;

public class ContextGeneralPanel extends AbstractContextPropertiesPanel {

	private static final long serialVersionUID = -8337361808959321380L;

	private JPanel panelContext = null;
	private ZapTextField txtName = null;
	private ZapTextArea txtDescription = null;
	private JCheckBox chkInScope = null;
	
	public static String getPanelName(Context ctx) {
		return getPanelName(ctx.getIndex(), ctx.getName());
	}
	
	public static String getPanelName(int index, String name) {
		return index + ":" + name;
	}

	public ContextGeneralPanel(String name, int index) {
		super(index);
		this.setName(name);
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setLayout(new CardLayout());
		this.add(getPanelSession(), this.getName() + "gen");
	}
	
	@Override
	public void setName(String name) {
		if (name.startsWith(this.getContextIndex() + ":")) {
			name = name.substring(name.indexOf(":")+1);
		}
		super.setName(getPanelName(this.getContextIndex(), name));
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
			panelContext.add(getTxtName(), LayoutHelper.getGBC(0, 1, 1, 1.0D));
			panelContext.add(getChkInScope(), LayoutHelper.getGBC(0, 2, 2, 1.0D));
			panelContext.add(new JLabel(Constant.messages.getString("context.label.desc")),
					LayoutHelper.getGBC(0, 3, 1, 1.0D));
			panelContext.add(getTxtDescription(), LayoutHelper.getGBC(0, 4, 1, 1.0D, 1.0D));
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
			txtDescription.setBorder(javax.swing.BorderFactory
					.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
			txtDescription.setLineWrap(true);
		}
		return txtDescription;
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.contexts";
	}

	@Override
	public void initContextData(Session session, Context uiSharedContext) {
		getTxtName().setText(uiSharedContext.getName());
		getTxtName().discardAllEdits();
		getTxtDescription().setText(uiSharedContext.getDescription());
		getTxtDescription().discardAllEdits();
		getChkInScope().setSelected(uiSharedContext.isInScope());
		
		if (uiSharedContext.getName().equals(Integer.toString(uiSharedContext.getIndex())) && 
				uiSharedContext.getIncludeInContextRegexs().size() == 1) {
			// Default to the host name in the first and only regex
			String firstRegex = uiSharedContext.getIncludeInContextRegexs().get(0);
			int startIndex = firstRegex.indexOf("://");
			if (startIndex > 0) {
				String hostPlus = firstRegex.substring(startIndex + 3);
				int endIndex = hostPlus.indexOf("\\");
				if (endIndex > 0) {
					// By default regexes end in \E
					hostPlus = hostPlus.substring(0, endIndex);
				}
				endIndex = hostPlus.indexOf("/");
				if (endIndex > 0) {
					hostPlus = hostPlus.substring(0, endIndex);
				}
				getTxtName().setText(hostPlus);
			}
		}
	}

	@Override
	public void validateContextData(Session session) {
		// no validation needed

	}

	@Override
	public void saveContextData(Session session) {
		Context context = session.getContext(this.getContextIndex());
		saveDataInContext(context);
		String name = getTxtName().getText();
		if (!this.getName().equals(getPanelName(this.getContextIndex(), name)) && View.isInitialised()) {
			View.getSingleton().renameContext(context);
		}
	}

	@Override
	public void saveTemporaryContextData(Context uiSharedContext) {
		saveDataInContext(uiSharedContext);
	}
	
	private void saveDataInContext(Context context){
		context.setName(getTxtName().getText());
		context.setDescription(getTxtDescription().getText());
		context.setInScope(getChkInScope().isSelected());
	}

}
