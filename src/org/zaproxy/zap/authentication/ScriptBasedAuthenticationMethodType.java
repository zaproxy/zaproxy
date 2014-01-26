package org.zaproxy.zap.authentication;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.authentication.GenericAuthenticationCredentials.GenericAuthenticationCredentialsOptionsPanel;
import org.zaproxy.zap.extension.api.ApiDynamicActionImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptType;
import org.zaproxy.zap.extension.script.ScriptWrapper;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.WebSession;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.EncodingUtils;
import org.zaproxy.zap.view.DynamicFieldsPanel;
import org.zaproxy.zap.view.LayoutHelper;

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;

public class ScriptBasedAuthenticationMethodType extends AuthenticationMethodType {

	private static final int METHOD_IDENTIFIER = 4;

	private static final Logger log = Logger.getLogger(ScriptBasedAuthenticationMethodType.class);

	/** The Constant SCRIPT_TYPE_AUTH. */
	private static final String SCRIPT_TYPE_AUTH = "authentication";

	/** The SCRIPT ICON. */
	private static final ImageIcon SCRIPT_ICON_AUTH = new ImageIcon(
			ZAP.class.getResource("/resource/icon/16/script-auth.png"));

	/** The Authentication method's name. */
	private static final String METHOD_NAME = Constant.messages
			.getString("authentication.method.script.name");

	private ExtensionScript extensionScript;

	public class ScriptBasedAuthenticationMethod extends AuthenticationMethod {

		private ScriptWrapper script;

		private String[] credentialsParamNames;

		private Map<String, String> paramValues;

		private HttpSender httpSender;

		protected HttpSender getHttpSender() {
			if (this.httpSender == null) {
				this.httpSender = new HttpSender(Model.getSingleton().getOptionsParam().getConnectionParam(),
						true, HttpSender.AUTHENTICATION_INITIATOR);
			}
			return httpSender;
		}

		@Override
		public boolean isConfigured() {
			return true;
		}

		@Override
		protected AuthenticationMethod duplicate() {
			ScriptBasedAuthenticationMethod method = new ScriptBasedAuthenticationMethod();
			method.script = script;
			method.paramValues = new HashMap<String, String>(this.paramValues);
			method.credentialsParamNames = this.credentialsParamNames;
			return method;
		}

		@Override
		public AuthenticationCredentials createAuthenticationCredentials() {
			return new GenericAuthenticationCredentials(this.credentialsParamNames);
		}

		@Override
		public AuthenticationMethodType getType() {
			return new ScriptBasedAuthenticationMethodType();
		}

		@Override
		public WebSession authenticate(SessionManagementMethod sessionManagementMethod,
				AuthenticationCredentials credentials, User user)
				throws UnsupportedAuthenticationCredentialsException {
			// type check
			if (!(credentials instanceof GenericAuthenticationCredentials)) {
				throw new UnsupportedAuthenticationCredentialsException(
						"Script based Authentication method only supports "
								+ GenericAuthenticationCredentials.class.getSimpleName());
			}
			GenericAuthenticationCredentials cred = (GenericAuthenticationCredentials) credentials;

			// Call the script to get an authenticated message from which we can then extract the
			// session
			AuthenticationScript script;
			HttpMessage msg = null;
			try {
				script = getScriptsExtension().getInterface(this.script, AuthenticationScript.class);
				msg = script.authenticate(new AuthenticationHelper(getHttpSender(), sessionManagementMethod,
						user), this.paramValues, cred);
			} catch (ScriptException | IOException e) {
				log.error("An error occurred while trying to authenticate using the Authentication Script: "
						+ this.script.getName(), e);
				getScriptsExtension().setError(this.script, e);
				return null;
			}

			// Let the user know it worked
			AuthenticationHelper.notifyOutputAuthSuccessful();

			// Add message to history
			AuthenticationHelper.addAuthMessageToHistory(msg);

			// Return the web session as extracted by the session management method
			return sessionManagementMethod.extractWebSession(msg);
		}

		@Override
		public ApiResponse getApiResponseRepresentation() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public class ScriptBasedAuthenticationMethodOptionsPanel extends AbstractAuthenticationMethodOptionsPanel {

		private static final long serialVersionUID = 7812841049435409987L;

		private final String SCRIPT_NAME_LABEL = Constant.messages
				.getString("authentication.method.script.field.label.scriptName");
		private final String LABEL_NOT_LOADED = Constant.messages
				.getString("authentication.method.script.field.label.notLoaded");
		private JComboBox<ScriptWrapper> scriptsComboBox;
		private JButton loadScriptButton;

		private ScriptBasedAuthenticationMethod method;

		private ScriptWrapper loadedScript;

		private JPanel dynamicContentPanel;

		private DynamicFieldsPanel dynamicFieldsPanel;

		private String[] loadedCredentialParams;

		public ScriptBasedAuthenticationMethodOptionsPanel() {
			super();
			initialize();
		}

		@SuppressWarnings("unchecked")
		private void initialize() {
			this.setLayout(new GridBagLayout());

			this.add(new JLabel(SCRIPT_NAME_LABEL), LayoutHelper.getGBC(0, 0, 1, 0.0d, 0.0d));

			this.scriptsComboBox = new JComboBox<>();
			this.scriptsComboBox.setRenderer(new ScriptWrapperRenderer(this));
			this.add(this.scriptsComboBox, LayoutHelper.getGBC(1, 0, 1, 1.0d, 0.0d));

			this.loadScriptButton = new JButton("Load");
			this.add(this.loadScriptButton, LayoutHelper.getGBC(2, 0, 1, 0.0d, 0.0d));
			this.loadScriptButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					loadScript((ScriptWrapper) scriptsComboBox.getSelectedItem(), true);
				}
			});

			this.dynamicContentPanel = new JPanel(new BorderLayout());
			this.add(this.dynamicContentPanel, LayoutHelper.getGBC(0, 1, 3, 1.0d, 0.0d));
			this.dynamicContentPanel.add(new JLabel(LABEL_NOT_LOADED));
		}

		@Override
		public void validateFields() throws IllegalStateException {
			if (this.loadedScript == null) {
				this.scriptsComboBox.requestFocusInWindow();
				throw new IllegalStateException(
						Constant.messages
								.getString("authentication.method.script.dialog.error.text.notLoaded"));
			}
			this.dynamicFieldsPanel.validateFields();
		}

		@Override
		public void saveMethod() {
			this.method.script = (ScriptWrapper) this.scriptsComboBox.getSelectedItem();
			// This method will also be called when switching panels to save a temporary state so
			// the state of the authentication method might not be valid
			if (this.dynamicFieldsPanel != null)
				this.method.paramValues = this.dynamicFieldsPanel.getFieldValues();
			else
				this.method.paramValues = Collections.emptyMap();
			if (this.loadedScript != null)
				this.method.credentialsParamNames = this.loadedCredentialParams;
		}

		@Override
		public void bindMethod(AuthenticationMethod method) throws UnsupportedAuthenticationMethodException {
			this.method = (ScriptBasedAuthenticationMethod) method;

			// Make sure the list of scripts is refreshed
			List<ScriptWrapper> scripts = getScriptsExtension().getScripts(SCRIPT_TYPE_AUTH);
			DefaultComboBoxModel<ScriptWrapper> model = new DefaultComboBoxModel<>(
					scripts.toArray(new ScriptWrapper[scripts.size()]));
			this.scriptsComboBox.setModel(model);
			this.scriptsComboBox.setSelectedItem(this.method.script);

			// Load the selected script, if any
			if (this.method.script != null) {
				loadScript(this.method.script, false);
				this.dynamicFieldsPanel.bindFieldValues(this.method.paramValues);
			}
		}

		@Override
		public AuthenticationMethod getMethod() {
			return this.method;
		}

		private void loadScript(ScriptWrapper scriptW, boolean adaptOldValues) {
			String errorMessage;
			try {
				AuthenticationScript script = getScriptsExtension().getInterface(scriptW,
						AuthenticationScript.class);

				if (script != null) {

					String[] requiredParams = script.getRequiredParamsNames();
					String[] optionalParams = script.getOptionalParamsNames();
					this.loadedCredentialParams = script.getCredentialsParamsNames();
					if (log.isDebugEnabled()) {
						log.debug("Loaded authentication script - required parameters: "
								+ Arrays.toString(requiredParams) + " - optional parameters: "
								+ Arrays.toString(optionalParams));
					}
					// If there's an already loaded script, make sure we save its values and _try_
					// to place them in the new panel
					Map<String, String> oldValues = null;
					if (adaptOldValues && dynamicFieldsPanel != null) {
						oldValues = dynamicFieldsPanel.getFieldValues();
						if (log.isDebugEnabled())
							log.debug("Trying to adapt old values: " + oldValues);
					}

					this.dynamicFieldsPanel = new DynamicFieldsPanel(requiredParams, optionalParams);
					this.loadedScript = scriptW;
					if (adaptOldValues && oldValues != null)
						this.dynamicFieldsPanel.bindFieldValues(oldValues);

					this.dynamicContentPanel.removeAll();
					this.dynamicContentPanel.add(dynamicFieldsPanel, BorderLayout.CENTER);
					this.dynamicContentPanel.revalidate();

					return;
				} else {
					log.warn("The script " + scriptW.getName()
							+ " does not properly implement the Authentication Script interface.");
					errorMessage = Constant.messages
							.getString("authentication.method.script.dialog.error.text.interface");
				}
			} catch (Exception e) {
				log.error("Error while loading authentication script", e);
				errorMessage = Constant.messages.getString(
						"authentication.method.script.dialog.error.text.loading", e.getMessage());
			}
			// Ooops! If this point is reached, an error has occurred while loading
			getScriptsExtension().setError(scriptW, errorMessage);
			JOptionPane.showMessageDialog(this, errorMessage,
					Constant.messages.getString("authentication.method.script.dialog.error.title"),
					JOptionPane.ERROR_MESSAGE);
			this.loadedScript = null;
			this.dynamicFieldsPanel = null;
			this.dynamicContentPanel.removeAll();
			this.dynamicContentPanel.add(new JLabel(LABEL_NOT_LOADED), BorderLayout.CENTER);
			this.dynamicContentPanel.revalidate();
		}
	}

	/**
	 * A renderer for properly displaying the name of a {@link ScriptWrapper} in a ComboBox and
	 * putting emphasis on loaded script.
	 */
	private static class ScriptWrapperRenderer extends BasicComboBoxRenderer {
		private static final long serialVersionUID = 3654541772447187317L;
		private static final Border BORDER = new EmptyBorder(2, 3, 3, 3);
		private ScriptBasedAuthenticationMethodOptionsPanel panel;

		public ScriptWrapperRenderer(ScriptBasedAuthenticationMethodOptionsPanel panel) {
			super();
			this.panel = panel;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value != null) {
				setBorder(BORDER);
				ScriptWrapper item = (ScriptWrapper) value;
				if (panel.loadedScript == item)
					setText("<html><b>" + item.getName() + " (loaded)</b></html>");
				else
					setText(item.getName());
			}
			return this;
		}
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		// Hook up the Script Type
		if (getScriptsExtension() != null) {
			log.debug("Registering Script...");
			getScriptsExtension().registerScriptType(
					new ScriptType(SCRIPT_TYPE_AUTH, "authentication.method.script.type", SCRIPT_ICON_AUTH,
							false));
		}
	}

	@Override
	public ScriptBasedAuthenticationMethod createAuthenticationMethod(int contextId) {
		return new ScriptBasedAuthenticationMethod();
	}

	@Override
	public String getName() {
		return METHOD_NAME;
	}

	@Override
	public int getUniqueIdentifier() {
		return METHOD_IDENTIFIER;
	}

	@Override
	public AbstractAuthenticationMethodOptionsPanel buildOptionsPanel(Context uiSharedContext) {
		return new ScriptBasedAuthenticationMethodOptionsPanel();
	}

	@Override
	public boolean hasOptionsPanel() {
		return true;
	}

	@Override
	public AbstractCredentialsOptionsPanel<? extends AuthenticationCredentials> buildCredentialsOptionsPanel(
			AuthenticationCredentials credentials, Context uiSharedContext) {
		return new GenericAuthenticationCredentialsOptionsPanel(
				(GenericAuthenticationCredentials) credentials);
	}

	@Override
	public boolean hasCredentialsOptionsPanel() {
		return true;
	}

	@Override
	public boolean isTypeForMethod(AuthenticationMethod method) {
		return (method instanceof ScriptBasedAuthenticationMethod);
	}

	@Override
	public AuthenticationMethod loadMethodFromSession(Session session, int contextId) throws SQLException {
		ScriptBasedAuthenticationMethod method = createAuthenticationMethod(contextId);

		// Load the script and make sure it still exists and still follows the required interface
		List<String> scripts = session.getContextDataStrings(contextId,
				RecordContext.TYPE_AUTH_METHOD_FIELD_1);
		String scriptName = "";
		if (scripts != null && scripts.size() > 0) {
			scriptName = scripts.get(0);
			ScriptWrapper script = getScriptsExtension().getScript(scriptName);
			if (script == null) {
				log.error("Unable to find script while loading Script Based Authentication Method for name: "
						+ scriptName);
			} else
				log.info("Loaded script:" + script.getName());
			method.script = script;

			// Check script interface and make sure we load the credentials parameter names
			AuthenticationScript s;
			try {
				s = getScriptsExtension().getInterface(script, AuthenticationScript.class);
				if (s != null) {
					method.credentialsParamNames = s.getCredentialsParamsNames();
				} else {
					log.error("Unable to load Script Based Authentication method. The script "
							+ script.getName()
							+ " does not properly implement the Authentication Script interface.");
				}
			} catch (ScriptException | IOException e) {
				log.error("Unable to load Script Based Authentication method. The script " + script.getName()
						+ " does not properly implement the Authentication Script interface.");
			}

		}

		// Load the parameter values
		List<String> paramValuesS = session.getContextDataStrings(contextId,
				RecordContext.TYPE_AUTH_METHOD_FIELD_2);
		Map<String, String> paramValues = null;
		if (paramValuesS != null && paramValuesS.size() > 0) {
			paramValues = EncodingUtils.stringToMap(paramValuesS.get(0));
			method.paramValues = paramValues;
		} else {
			method.paramValues = new HashMap<String, String>();
			log.error("Unable to load script parameter values loading Script Based Authentication Method for name: "
					+ scriptName);
		}

		return method;
	}

	@Override
	public void persistMethodToSession(Session session, int contextId, AuthenticationMethod authMethod)
			throws UnsupportedAuthenticationMethodException, SQLException {
		if (!(authMethod instanceof ScriptBasedAuthenticationMethod))
			throw new UnsupportedAuthenticationMethodException(
					"Script based authentication type only supports: "
							+ ScriptBasedAuthenticationMethod.class);

		ScriptBasedAuthenticationMethod method = (ScriptBasedAuthenticationMethod) authMethod;
		session.setContextData(contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_1, method.script.getName());
		session.setContextData(contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_2,
				EncodingUtils.mapToString(method.paramValues));
	}

	@Override
	public AuthenticationCredentials createAuthenticationCredentials() {
		// NOTE: This method will initialize a set of Credentials without any required parameters
		// and, thus, should be later modified explicitly (e.g. through calls to decode())
		return new GenericAuthenticationCredentials(new String[0]);
	}

	@Override
	public ApiDynamicActionImplementor getSetMethodForContextApiAction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApiDynamicActionImplementor getSetCredentialsForUserApiAction() {
		// TODO Auto-generated method stub
		return null;
	}

	private ExtensionScript getScriptsExtension() {
		if (extensionScript == null)
			extensionScript = (ExtensionScript) Control.getSingleton().getExtensionLoader()
					.getExtension(ExtensionScript.NAME);
		return extensionScript;
	}

	/**
	 * The Interface that needs to be implemented by an Authentication Script.
	 */
	public interface AuthenticationScript {

		public String[] getRequiredParamsNames();

		public String[] getOptionalParamsNames();

		public String[] getCredentialsParamsNames();

		public HttpMessage authenticate(AuthenticationHelper helper, Map<String, String> paramsValues,
				GenericAuthenticationCredentials credentials);
	}

}
