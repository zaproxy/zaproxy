package org.zaproxy.zap.userauth.authentication;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.httpsessions.ExtensionHttpSessions;
import org.zaproxy.zap.extension.httpsessions.HttpSession;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.LayoutHelper;

public class ManualAuthenticationMethod implements AuthenticationMethod {

	private HttpSession selectedSession;

	@Override
	public String toString() {
		return "Manual-Authentication-Method";
	}

	public HttpSession authenticate() {
		return selectedSession;
	}

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
				int contextId) {
			return new ManualAuthenticationMethodOptionsPanel(this, contextId);
		}
	}

	public static class ManualAuthenticationMethodOptionsPanel extends
			AbstractAuthenticationMethodOptionsPanel<ManualAuthenticationMethod> {

		private static final Logger log = Logger.getLogger(ManualAuthenticationMethodOptionsPanel.class);
		private JComboBox<HttpSession> sessionsComboBox;
		private Context context;

		public ManualAuthenticationMethodOptionsPanel(ManualAuthenticationMethod existingMethod, int contextId) {
			super(existingMethod);
			context = Model.getSingleton().getSession().getContext(contextId);
			initialize();
		}

		public ManualAuthenticationMethodOptionsPanel(
				AuthenticationMethodFactory<ManualAuthenticationMethod> factory, int contextId) {
			super(factory);
			context = Model.getSingleton().getSession().getContext(contextId);
			initialize();
		}

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -1468816215967024375L;

		@Override
		public boolean validateFields() {
			return sessionsComboBox.getSelectedIndex() != -1;
		}

		@Override
		public void saveMethod() {
			log.info("Saving Manual Authentication Method: " + getSessionsComboBox().getSelectedItem());
			getMethod().selectedSession = (HttpSession) getSessionsComboBox().getSelectedItem();
		}

		protected void initialize() {
			log.debug("Initializing options panel for context: " + context);

			this.setLayout(new GridBagLayout());
			Insets insets = new Insets(4, 8, 2, 4);

			JLabel sessionsLabel = new JLabel(
					Constant.messages.getString("userauth.auth.manual.field.sessions"));

			this.add(sessionsLabel, LayoutHelper.getGBC(0, 0, 1, 0.5D, insets));
			this.add(getSessionsComboBox(), LayoutHelper.getGBC(1, 0, 1, 0.5D, insets));
			getSessionsComboBox().setRenderer(new HttpSessionRenderer());
		}

		class HttpSessionRenderer extends BasicComboBoxRenderer {

			/** The Constant serialVersionUID. */
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
					log.debug("Found sessions for Manual Authentication: " + sessions);
				sessionsComboBox = new JComboBox<>(sessions.toArray(new HttpSession[sessions.size()]));
			}
			return sessionsComboBox;
		}
	}

	@Override
	public String getStatusDescription() {
		return "Selected HTTP Session: " + selectedSession.getName();
	}
}
