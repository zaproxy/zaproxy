package org.zaproxy.zap.authentication;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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

import net.sf.json.JSONObject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.authentication.GenericAuthenticationCredentials.GenericAuthenticationCredentialsOptionsPanel;
import org.zaproxy.zap.extension.api.ApiDynamicActionImplementor;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.authentication.AuthenticationAPI;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptType;
import org.zaproxy.zap.extension.script.ScriptWrapper;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.WebSession;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.ApiUtils;
import org.zaproxy.zap.utils.EncodingUtils;
import org.zaproxy.zap.view.DynamicFieldsPanel;
import org.zaproxy.zap.view.LayoutHelper;

public class ScriptBasedAuthenticationMethodType extends AuthenticationMethodType {

	public static final String CONTEXT_CONFIG_AUTH_SCRIPT = AuthenticationMethod.CONTEXT_CONFIG_AUTH + ".script";
	public static final String CONTEXT_CONFIG_AUTH_SCRIPT_NAME = CONTEXT_CONFIG_AUTH_SCRIPT + ".name";
	public static final String CONTEXT_CONFIG_AUTH_SCRIPT_PARAMS = CONTEXT_CONFIG_AUTH_SCRIPT + ".params";

	public static final int METHOD_IDENTIFIER = 4;

	private static final Logger log = Logger.getLogger(ScriptBasedAuthenticationMethodType.class);

	/** The Constant SCRIPT_TYPE_AUTH. */
	public static final String SCRIPT_TYPE_AUTH = "authentication";

	private static final String API_METHOD_NAME = "scriptBasedAuthentication";

	/** The SCRIPT ICON. */
	private static final ImageIcon SCRIPT_ICON_AUTH = new ImageIcon(
			ZAP.class.getResource("/resource/icon/16/script-auth.png"));

	/** The Authentication method's name. */
	private static final String METHOD_NAME = Constant.messages.getString("authentication.method.script.name");

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

		/**
		 * Load a script and fills in the method's filled according to the values specified by the
		 * script.
		 * <p>
		 * If the method already had a loaded script and a set of values for the parameters, it
		 * tries to provide new values for the new parameters if they match any previous parameter
		 * names.
		 * 
		 * @param scriptW the script wrapper
		 * @throws IllegalArgumentException if an error occurs while loading the script.
		 */
		public void loadScript(ScriptWrapper scriptW) {
			AuthenticationScript script = getScriptInterfaceV2(scriptW);
			if (script == null) {
				script = getScriptInterface(scriptW);
			}
			if (script == null) {
				log.warn("The script " + scriptW.getName()
						+ " does not properly implement the Authentication Script interface.");
				throw new IllegalArgumentException(Constant.messages.getString(
						"authentication.method.script.dialog.error.text.interface", scriptW.getName()));
			}

			try {
				if (script instanceof AuthenticationScriptV2) {
					AuthenticationScriptV2 scriptV2 = (AuthenticationScriptV2) script;
					setLoggedInIndicatorPattern(scriptV2.getLoggedInIndicator());
					setLoggedOutIndicatorPattern(scriptV2.getLoggedOutIndicator());
				}
				String[] requiredParams = script.getRequiredParamsNames();
				String[] optionalParams = script.getOptionalParamsNames();
				this.credentialsParamNames = script.getCredentialsParamsNames();
				if (log.isDebugEnabled()) {
					log.debug("Loaded authentication script - required parameters: "
							+ Arrays.toString(requiredParams) + " - optional parameters: "
							+ Arrays.toString(optionalParams));
				}

				// If there's an already loaded script, make sure we save its values and _try_
				// to use them
				Map<String, String> oldValues = this.paramValues != null ? this.paramValues : Collections
						.<String, String> emptyMap();
				this.paramValues = new HashMap<>(requiredParams.length + optionalParams.length);
				for (String param : requiredParams)
					this.paramValues.put(param, oldValues.get(param));
				for (String param : optionalParams)
					this.paramValues.put(param, oldValues.get(param));

				this.script = scriptW;
				log.info("Successfully loaded new script for ScriptBasedAuthentication: " + this);
			} catch (Exception e) {
				log.error("Error while loading authentication script", e);
				getScriptsExtension().handleScriptException(this.script, e);
				throw new IllegalArgumentException(Constant.messages.getString(
						"authentication.method.script.dialog.error.text.loading", e.getMessage()));
			}
		}

		@Override
		public String toString() {
			return "ScriptBasedAuthenticationMethod [script=" + script + ", paramValues=" + paramValues
					+ ", credentialsParamNames=" + Arrays.toString(credentialsParamNames) + "]";
		}

		@Override
		public boolean isConfigured() {
			return true;
		}

		@Override
		protected AuthenticationMethod duplicate() {
			ScriptBasedAuthenticationMethod method = new ScriptBasedAuthenticationMethod();
			method.script = script;
			method.paramValues = this.paramValues != null ? new HashMap<String, String>(this.paramValues) : null;
			method.credentialsParamNames = this.credentialsParamNames;
			return method;
		}

		@Override
		public boolean validateCreationOfAuthenticationCredentials() {
			if (credentialsParamNames != null) {
				return true;
			}

			if (View.isInitialised()) {
				View.getSingleton().showMessageDialog(
						Constant.messages.getString("authentication.method.script.dialog.error.text.notLoaded"));
			}

			return false;
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
								+ GenericAuthenticationCredentials.class.getSimpleName() + ". Received: "
								+ credentials.getClass());
			}
			GenericAuthenticationCredentials cred = (GenericAuthenticationCredentials) credentials;

			// Call the script to get an authenticated message from which we can then extract the
			// session
			AuthenticationScript script = getScriptInterfaceV2(this.script);
			if (script == null) {
				script = getScriptInterface(this.script);
			}

			if (script == null) {
				return null;
			}

			HttpMessage msg = null;
			try {
				if (script instanceof AuthenticationScriptV2) {
					AuthenticationScriptV2 scriptV2 = (AuthenticationScriptV2) script;
					setLoggedInIndicatorPattern(scriptV2.getLoggedInIndicator());
					setLoggedOutIndicatorPattern(scriptV2.getLoggedOutIndicator());
				}
				msg = script.authenticate(new AuthenticationHelper(getHttpSender(), sessionManagementMethod,
						user), this.paramValues, cred);
			} catch (Exception e) {
				// Catch Exception instead of ScriptException and IOException because script engine implementations
				// might throw other exceptions on script errors (e.g. jdk.nashorn.internal.runtime.ECMAException)
				log.error("An error occurred while trying to authenticate using the Authentication Script: "
						+ this.script.getName(), e);
				getScriptsExtension().handleScriptException(this.script, e);
				return null;
			}

			if (this.isAuthenticated(msg)) {
				// Let the user know it worked
				AuthenticationHelper.notifyOutputAuthSuccessful(msg);
			} else {
				// Let the user know it failed
				AuthenticationHelper.notifyOutputAuthFailure(msg);
			}

			// Add message to history
			AuthenticationHelper.addAuthMessageToHistory(msg);

			// Return the web session as extracted by the session management method
			return sessionManagementMethod.extractWebSession(msg);
		}

		@Override
		public ApiResponse getApiResponseRepresentation() {
			Map<String, String> values = new HashMap<>();
			values.put("methodName", API_METHOD_NAME);
			values.put("scriptName", script.getName());
			values.putAll(paramValues);
			return new ApiResponseSet("method", values);
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
		private AuthenticationIndicatorsPanel indicatorsPanel;

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

			// Make sure the 'Load' button is disabled when nothing is selected
			this.loadScriptButton.setEnabled(false);
			this.scriptsComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					loadScriptButton.setEnabled(scriptsComboBox.getSelectedIndex() >= 0);
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
								.getString("authentication.method.script.dialog.error.text.notLoadedNorConfigured"));
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
			this.loadScriptButton.setEnabled(this.method.script != null);

			// Load the selected script, if any
			if (this.method.script != null) {
				loadScript(this.method.script, false);
				if (this.dynamicFieldsPanel != null)
					this.dynamicFieldsPanel.bindFieldValues(this.method.paramValues);
			}
		}

		@Override
		public void bindMethod(AuthenticationMethod method, AuthenticationIndicatorsPanel indicatorsPanel)
				throws UnsupportedAuthenticationMethodException {
			this.indicatorsPanel = indicatorsPanel;
			bindMethod(method);
		}

		@Override
		public AuthenticationMethod getMethod() {
			return this.method;
		}

		private void loadScript(ScriptWrapper scriptW, boolean adaptOldValues) {
			AuthenticationScript script = getScriptInterfaceV2(scriptW);
			if (script == null) {
				script = getScriptInterface(scriptW);
			}

			if (script == null) {
				log.warn("The script " + scriptW.getName()
						+ " does not properly implement the Authentication Script interface.");
				warnAndResetPanel(Constant.messages.getString(
						"authentication.method.script.dialog.error.text.interface", scriptW.getName()));
				return;
			}

			try {
				if (script instanceof AuthenticationScriptV2) {
					AuthenticationScriptV2 scriptV2 = (AuthenticationScriptV2) script;
					String toolTip = Constant.messages
							.getString("authentication.method.script.dialog.loggedInOutIndicatorsInScript.toolTip");
					String loggedInIndicator = scriptV2.getLoggedInIndicator();
					this.method.setLoggedInIndicatorPattern(loggedInIndicator);
					this.indicatorsPanel.setLoggedInIndicatorPattern(loggedInIndicator);
					this.indicatorsPanel.setLoggedInIndicatorEnabled(false);
					this.indicatorsPanel.setLoggedInIndicatorToolTip(toolTip);

					String loggedOutIndicator = scriptV2.getLoggedOutIndicator();
					this.method.setLoggedOutIndicatorPattern(loggedOutIndicator);
					this.indicatorsPanel.setLoggedOutIndicatorPattern(loggedOutIndicator);
					this.indicatorsPanel.setLoggedOutIndicatorEnabled(false);
					this.indicatorsPanel.setLoggedOutIndicatorToolTip(toolTip);
				} else {
					this.indicatorsPanel.setLoggedInIndicatorEnabled(true);
					this.indicatorsPanel.setLoggedInIndicatorToolTip(null);
					this.indicatorsPanel.setLoggedOutIndicatorEnabled(true);
					this.indicatorsPanel.setLoggedOutIndicatorToolTip(null);
				}
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

			} catch (Exception e) {
				getScriptsExtension().handleScriptException(scriptW, e);
				log.error("Error while calling authentication script", e);
				warnAndResetPanel(Constant.messages.getString(
						"authentication.method.script.dialog.error.text.loading", e.getMessage()));
			}
		}

		private void warnAndResetPanel(String errorMessage) {
			JOptionPane.showMessageDialog(this, errorMessage,
					Constant.messages.getString("authentication.method.script.dialog.error.title"),
					JOptionPane.ERROR_MESSAGE);
			this.loadedScript = null;
			this.scriptsComboBox.setSelectedItem(null);
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
							false, new String[] {ScriptType.CAPABILITY_APPEND}));
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
	public ScriptBasedAuthenticationMethod loadMethodFromSession(Session session, int contextId)
			throws DatabaseException {
		ScriptBasedAuthenticationMethod method = createAuthenticationMethod(contextId);

		// Load the script and make sure it still exists and still follows the required interface
		this.loadMethod(method, 
				session.getContextDataStrings(contextId,RecordContext.TYPE_AUTH_METHOD_FIELD_1), 
				session.getContextDataStrings(contextId,RecordContext.TYPE_AUTH_METHOD_FIELD_2));

		return method;
	}

	public void loadMethod(
			ScriptBasedAuthenticationMethod method, List<String> scripts, List<String> paramValuesS) {

		// Load the script and make sure it still exists and still follows the required interface
		String scriptName = "";
		if (scripts != null && scripts.size() > 0) {
			scriptName = scripts.get(0);
			ScriptWrapper script = getScriptsExtension().getScript(scriptName);
			if (script == null) {
				log.error("Unable to find script while loading Script Based Authentication Method for name: "
						+ scriptName);
				if (View.isInitialised()) {
					View.getSingleton().showMessageDialog(
							Constant.messages.getString("authentication.method.script.load.errorScriptNotFound", scriptName));
				}
				return;
			}
			log.info("Loaded script:" + script.getName());
			method.script = script;

			// Check script interface and make sure we load the credentials parameter names
			AuthenticationScript s = getScriptInterfaceV2(script);
			if (s == null) {
				s = getScriptInterface(script);
			}
			if (s == null) {
				log.error("Unable to load Script Based Authentication method. The script "
						+ scriptName
						+ " does not properly implement the Authentication Script interface.");
				return;
			}

			try {
				if (s instanceof AuthenticationScriptV2) {
					AuthenticationScriptV2 sV2 = (AuthenticationScriptV2) s;
					method.setLoggedInIndicatorPattern(sV2.getLoggedInIndicator());
					method.setLoggedOutIndicatorPattern(sV2.getLoggedOutIndicator());
				}
				method.credentialsParamNames = s.getCredentialsParamsNames();
			} catch (Exception e) {
				getScriptsExtension().handleScriptException(script, e);
			}

		}

		// Load the parameter values
		Map<String, String> paramValues = null;
		if (paramValuesS != null && paramValuesS.size() > 0) {
			paramValues = EncodingUtils.stringToMap(paramValuesS.get(0));
			method.paramValues = paramValues;
		} else {
			method.paramValues = new HashMap<String, String>();
			log.error("Unable to load script parameter values loading Script Based Authentication Method for name: "
					+ scriptName);
		}
	}

	@Override
	public void persistMethodToSession(Session session, int contextId, AuthenticationMethod authMethod)
			throws UnsupportedAuthenticationMethodException, DatabaseException {
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

	private ExtensionScript getScriptsExtension() {
		if (extensionScript == null)
			extensionScript = (ExtensionScript) Control.getSingleton().getExtensionLoader()
					.getExtension(ExtensionScript.NAME);
		return extensionScript;
	}

	private AuthenticationScript getScriptInterface(ScriptWrapper script) {
		try {
			return getScriptsExtension().getInterface(script, AuthenticationScript.class);
		} catch (Exception e) {
			getScriptsExtension().handleFailedScriptInterface(
					script,
					Constant.messages.getString("authentication.method.script.dialog.error.text.interface", script.getName()));
		}
		return null;
	}

	private AuthenticationScriptV2 getScriptInterfaceV2(ScriptWrapper script) {
		try {
			return getScriptsExtension().getInterface(script, AuthenticationScriptV2.class);
		} catch (Exception ignore) {
			// The interface is optional, the AuthenticationScript will be checked after this one.
		}
		return null;
	}

	/**
	 * The Interface that needs to be implemented by an Authentication Script.
	 */
	public interface AuthenticationScript {

		public String[] getRequiredParamsNames();

		public String[] getOptionalParamsNames();

		public String[] getCredentialsParamsNames();

		public HttpMessage authenticate(AuthenticationHelper helper, Map<String, String> paramsValues,
				GenericAuthenticationCredentials credentials) throws ScriptException;
	}

	/**
	 * An {@code AuthenticationScript} that allows to specify the logged in/out indicators.
	 * 
	 * @since 2.5.0
	 */
	public interface AuthenticationScriptV2 extends AuthenticationScript {

		/**
		 * Gets the logged in indicator pattern.
		 * 
		 * @return the logged in indicator pattern
		 */
		String getLoggedInIndicator();

		/**
		 * Gets the logged out indicator pattern.
		 * 
		 * @return the logged out indicator pattern
		 */
		String getLoggedOutIndicator();
	}

	/* API related constants and methods. */
	private static final String PARAM_SCRIPT_NAME = "scriptName";
	private static final String PARAM_SCRIPT_CONFIG_PARAMS = "scriptConfigParams";

	@Override
	public ApiDynamicActionImplementor getSetMethodForContextApiAction() {
		return new ApiDynamicActionImplementor(API_METHOD_NAME, new String[] { PARAM_SCRIPT_NAME },
				new String[] { PARAM_SCRIPT_CONFIG_PARAMS }) {
			@Override
			public void handleAction(JSONObject params) throws ApiException {
				Context context = ApiUtils.getContextByParamId(params, AuthenticationAPI.PARAM_CONTEXT_ID);
				String scriptName = ApiUtils.getNonEmptyStringParam(params, PARAM_SCRIPT_NAME);

				// Prepare the method
				ScriptBasedAuthenticationMethod method = createAuthenticationMethod(context.getIndex());

				// Load the script and make sure it exists and follows the required interface
				ScriptWrapper script = getScriptsExtension().getScript(scriptName);
				if (script == null) {
					log.error("Unable to find script while loading Script Based Authentication Method for name: "
							+ scriptName);
					throw new ApiException(ApiException.Type.SCRIPT_NOT_FOUND, scriptName);
				} else
					log.info("Loaded script for API:" + script.getName());
				method.script = script;

				// Check script interface and make sure we load the credentials parameter names
				AuthenticationScript s = getScriptInterfaceV2(script);
				if (s == null) {
					s = getScriptInterface(script);
				}
				if (s == null) {
					log.error("Unable to load Script Based Authentication method. The script "
							+ script.getName()
							+ " does not properly implement the Authentication Script interface.");
					throw new ApiException(ApiException.Type.BAD_SCRIPT_FORMAT,
							"Does not follow Authentication script interface");
				}
				try {
					if (s instanceof AuthenticationScriptV2) {
						AuthenticationScriptV2 sV2 = (AuthenticationScriptV2) s;
						method.setLoggedInIndicatorPattern(sV2.getLoggedInIndicator());
						method.setLoggedOutIndicatorPattern(sV2.getLoggedOutIndicator());
					}
					method.credentialsParamNames = s.getCredentialsParamsNames();

					// Load config param names + values and make sure all of the required ones
					// are there
					String[] requiredParams = s.getRequiredParamsNames();
					String[] optionalParams = s.getOptionalParamsNames();
					if (log.isDebugEnabled()) {
						log.debug("Loaded authentication script - required parameters: "
								+ Arrays.toString(requiredParams) + " - optional parameters: "
								+ Arrays.toString(optionalParams));
					}

					Map<String, String> paramValues = new HashMap<String, String>();
					for (String rp : requiredParams) {
						// If one of the required parameters is not present, it will throw
						// an exception
						String val = ApiUtils.getNonEmptyStringParam(params, rp);
						paramValues.put(rp, val);
					}

					for (String op : optionalParams)
						paramValues.put(op, ApiUtils.getOptionalStringParam(params, op));
					method.paramValues = paramValues;
					if (log.isDebugEnabled())
						log.debug("Loaded authentication script parameters:" + paramValues);

				} catch (ApiException e) {
					throw e;
				} catch (Exception e) {
					getScriptsExtension().handleScriptException(script, e);
					log.error("Unable to load Script Based Authentication method. The script "
							+ script.getName() + " contains errors.");
					throw new ApiException(ApiException.Type.BAD_SCRIPT_FORMAT, e.getMessage());
				}

				// Set the method, making sure that, if the type is different, things are changed
				// accordingly
				if (!context.getAuthenticationMethod().isSameType(method))
					apiChangedAuthenticationMethodForContext(context.getIndex());
				context.setAuthenticationMethod(method);
			}
		};
	}

	@Override
	public ApiDynamicActionImplementor getSetCredentialsForUserApiAction() {
		return GenericAuthenticationCredentials.getSetCredentialsForUserApiAction(this);
	}

	@Override
	public void exportData(Configuration config, AuthenticationMethod authMethod) {
		if (!(authMethod instanceof ScriptBasedAuthenticationMethod)) {
			throw new UnsupportedAuthenticationMethodException(
					"Script based authentication type only supports: " + ScriptBasedAuthenticationMethod.class.getName());
		}
		ScriptBasedAuthenticationMethod method = (ScriptBasedAuthenticationMethod) authMethod;
		config.setProperty(CONTEXT_CONFIG_AUTH_SCRIPT_NAME, method.script.getName());
		config.setProperty(CONTEXT_CONFIG_AUTH_SCRIPT_PARAMS, EncodingUtils.mapToString(method.paramValues));
	}

	@Override
	public void importData(Configuration config, AuthenticationMethod authMethod) throws ConfigurationException {
		if (!(authMethod instanceof ScriptBasedAuthenticationMethod)) {
			throw new UnsupportedAuthenticationMethodException(
					"Script based authentication type only supports: " + ScriptBasedAuthenticationMethod.class.getName());
		}
		ScriptBasedAuthenticationMethod method = (ScriptBasedAuthenticationMethod) authMethod;
		this.loadMethod(method, 
				objListToStrList(config.getList(CONTEXT_CONFIG_AUTH_SCRIPT_NAME)), 
				objListToStrList(config.getList(CONTEXT_CONFIG_AUTH_SCRIPT_PARAMS)));
	}
	
	private List<String> objListToStrList(List<Object> oList) {
		List<String> sList = new ArrayList<String>(oList.size());
		for (Object o : oList) {
			sList.add(o.toString());
		}
		return sList;
	}
}
