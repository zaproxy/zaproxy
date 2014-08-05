package org.zaproxy.zap.extension.multiFuzz.impl.http;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;
import org.zaproxy.zap.extension.multiFuzz.SubComponent;

public class AntiCSRFComponent implements SubComponent{
	
	private JCheckBox enableTokens;
	private JCheckBox showTokenRequests;
	private boolean incAcsrfToken = false;
	private HttpFuzzerDialogTokenPane tokenPane;
	
	public AntiCSRFComponent(HttpMessage msg){
		checkAntiCSRF(msg);
	}
	
	@Override
	public int addOptions(JPanel panel, int currentRow) {
		if (incAcsrfToken) { // Options for AcsrfTokens
			panel.add(
					new JLabel(Constant.messages
							.getString("fuzz.label.anticsrf")),
					getGBC(0, currentRow, 6, 2.0D));
			currentRow++;
			panel.add(getEnableTokens(), getGBC(0, currentRow, 6, 0.0D));
			currentRow++;
			panel.add(
					getTokensPane().getPane(),
					getGBC(0, currentRow, 6, 1.0D, 0.0D,
							java.awt.GridBagConstraints.BOTH));
			currentRow++;

			panel.add(
					new JLabel(Constant.messages
							.getString("fuzz.label.showtokens")),
					getGBC(0, currentRow, 2, 1.0D));
			panel.add(getShowTokenRequests(), getGBC(2, currentRow, 4, 0.0D));
			currentRow++;
		}
		return currentRow;
	}

	private void checkAntiCSRF(HttpMessage fuzzableMessage) {
		ExtensionAntiCSRF extAntiCSRF = (ExtensionAntiCSRF) Control
				.getSingleton().getExtensionLoader()
				.getExtension(ExtensionAntiCSRF.NAME);
		List<AntiCsrfToken> tokens = null;
		if (extAntiCSRF != null) {
			tokens = extAntiCSRF.getTokens(fuzzableMessage);
		}
		if (tokens == null || tokens.size() == 0) {
			incAcsrfToken = false;
		} else {
			incAcsrfToken = true;
		}
		if (incAcsrfToken) {
			setAntiCsrfTokens(tokens);
		}
	}
	public boolean getShowTokens(){
		return getShowTokenRequests().isSelected();
	}
	public boolean getTokensEnabled(){
		return getEnableTokens().isSelected();
	}
	private HttpFuzzerDialogTokenPane getTokensPane() {
		if (tokenPane == null) {
			tokenPane = new HttpFuzzerDialogTokenPane();
		}
		return tokenPane;
	}
	private void setAntiCsrfTokens(List<AntiCsrfToken> acsrfTokens) {
		if (acsrfTokens != null && acsrfTokens.size() > 0) {
			getTokensPane().setAll(true, acsrfTokens.get(0),
					acsrfTokens.get(0).getTargetURL());
			this.getEnableTokens().setSelected(true);
			this.getEnableTokens().setEnabled(true);
			this.getTokensPane().getPane().setVisible(true);
		} else {
			getTokensPane().reset();
			this.getEnableTokens().setSelected(false);
			this.getEnableTokens().setEnabled(false);
			this.getTokensPane().getPane().setVisible(false);
		}
	}

	private JCheckBox getEnableTokens() {
		if (enableTokens == null) {
			enableTokens = new JCheckBox();
			enableTokens.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getTokensPane().setEnabled(enableTokens.isSelected());
					getShowTokenRequests()
							.setEnabled(enableTokens.isSelected());
				}
			});
		}
		return enableTokens;
	}

	private JCheckBox getShowTokenRequests() {
		if (showTokenRequests == null) {
			showTokenRequests = new JCheckBox();
		}
		return showTokenRequests;
	}
	protected GridBagConstraints getGBC(int x, int y, int width, double weightx) {
		return getGBC(x, y, width, weightx, 0.0,
				java.awt.GridBagConstraints.NONE);
	}

	protected GridBagConstraints getGBC(int x, int y, int width,
			double weightx, double weighty, int fill) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.insets = new java.awt.Insets(1, 5, 1, 5);
		gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gbc.fill = fill;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.gridwidth = width;
		return gbc;
	}
}
