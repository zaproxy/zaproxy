/*
 * 
 */
package org.zaproxy.zap.userauth.authentication;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.httpsessions.ExtensionHttpSessions;
import org.zaproxy.zap.extension.httpsessions.HttpSession;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * The implementation for an {@link AuthenticationMethod} where the user manually authenticates and
 * then just selects an already authenticated {@link HttpSession}.
 */
public class ManualAuthenticationMethod implements AuthenticationMethod {

	private HttpSession selectedSession;
	private static final String NAME = Constant.messages.getString("userauth.auth.manual.name");

	@Override
	public String toString() {
		return NAME;
	}

	@Override
	public HttpSession authenticate() {
		return selectedSession;
	}

	/**
	 * A factory for creating ManualAuthenticationMethod objects.
	 */
	public static class ManualAuthenticationMethodFactory extends
			AuthenticationMethodFactory<ManualAuthenticationMethod> {

		private static final String METHOD_NAME = Constant.messages.getString("userauth.auth.manual.name");

		@Override
		public ManualAuthenticationMethod buildAuthenticationMethod() {
			return new ManualAuthenticationMethod();
		}

		@Override
		public String getName() {
			return METHOD_NAME;
		}

		@Override
		public AbstractAuthenticationMethodOptionsPanel<ManualAuthenticationMethod> buildOptionsPanel(
				ManualAuthenticationMethod existingMethod, int contextId) {
			return new ManualAuthenticationMethodOptionsPanel(existingMethod, contextId);
		}

		@Override
		public boolean hasOptionsPanel() {
			return true;
		}

		@Override
		public boolean isFactoryForMethod(Class<? extends AuthenticationMethod> methodClass) {
			return methodClass == ManualAuthenticationMethod.class;
		}
	}

	/**
	 * The option panel for configuring ManualAuthenticationMethod objects.
	 */
	public static class ManualAuthenticationMethodOptionsPanel extends
			AbstractAuthenticationMethodOptionsPanel<ManualAuthenticationMethod> {

		private static final Logger log = Logger.getLogger(ManualAuthenticationMethodOptionsPanel.class);
		private JComboBox<HttpSession> sessionsComboBox;
		private Context context;

		private ManualAuthenticationMethodOptionsPanel(ManualAuthenticationMethod existingMethod,
				int contextId) {
			super(existingMethod);
			context = Model.getSingleton().getSession().getContext(contextId);
			initialize();
		}

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -1468816215967024375L;

		@Override
		public boolean validateFields() {
			if (sessionsComboBox.getSelectedIndex() < 0) {
				JOptionPane.showMessageDialog(this,
						Constant.messages.getString("userauth.auth.manual.dialog.error.nosession.text"),
						Constant.messages.getString("userauth.auth.manual.dialog.error.title"),
						JOptionPane.WARNING_MESSAGE);
				sessionsComboBox.requestFocusInWindow();
				return false;
			}
			return true;
		}

		@Override
		public void saveMethod() {
			log.info("Saving Manual Authentication Method: " + getSessionsComboBox().getSelectedItem());
			getMethod().selectedSession = (HttpSession) getSessionsComboBox().getSelectedItem();
		}

		/**
		 * Initialize the panel.
		 */
		protected void initialize() {
			log.debug("Initializing options panel for context: " + context);

			this.setLayout(new GridBagLayout());
			Insets insets = new Insets(4, 8, 2, 4);

			JLabel sessionsLabel = new JLabel(
					Constant.messages.getString("userauth.auth.manual.field.sessions"));

			this.add(sessionsLabel, LayoutHelper.getGBC(0, 0, 1, 0.5D, insets));
			this.add(getSessionsComboBox(), LayoutHelper.getGBC(1, 0, 1, 0.5D, insets));
			this.getSessionsComboBox().setRenderer(new HttpSessionRenderer());
		}

		/**
		 * A renderer for properly displaying the name of an HttpSession in a ComboBox.
		 */
		class HttpSessionRenderer extends BasicComboBoxRenderer {
			private static final long serialVersionUID = 3654541772447187317L;

			public Component getListCellRendererComponent(JList list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value != null) {
					HttpSession item = (HttpSession) value;
					setText(item.getName());
				}
				return this;
			}
		}

		private JComboBox<HttpSession> getSessionsComboBox() {
			if (sessionsComboBox == null) {
				ExtensionHttpSessions extensionHttpSessions = (ExtensionHttpSessions) Control.getSingleton()
						.getExtensionLoader().getExtension(ExtensionHttpSessions.NAME);
				List<HttpSession> sessions = extensionHttpSessions.getHttpSessionsForContext(context);
				if (log.isDebugEnabled())
					log.debug("Found sessions for Manual Authentication Config: " + sessions);
				sessionsComboBox = new JComboBox<>(sessions.toArray(new HttpSession[sessions.size()]));
			}
			return sessionsComboBox;
		}
	}

	@Override
	public String getStatusDescription() {
		return "Selected HTTP Session: " + selectedSession.getName();
	}

	@Override
	public boolean isConfigured() {
		return selectedSession != null;
	}

}