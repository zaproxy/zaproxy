/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.session;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.decorator.FontHighlighter;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.api.ApiDynamicActionImplementor;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptType;
import org.zaproxy.zap.extension.script.ScriptWrapper;
import org.zaproxy.zap.extension.sessions.ExtensionSessionManagement;
import org.zaproxy.zap.extension.sessions.SessionManagementAPI;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.utils.ApiUtils;
import org.zaproxy.zap.utils.EncodingUtils;
import org.zaproxy.zap.utils.ZapHtmlLabel;
import org.zaproxy.zap.view.DynamicFieldsPanel;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * The type corresponding to a {@link SessionManagementMethod} for web applications that use Session
 * Management Scripts, which can hopefully model any custom behaviour required
 *
 * @since 2.9.0
 */
public class ScriptBasedSessionManagementMethodType extends SessionManagementMethodType {

    public static final String CONTEXT_CONFIG_SESSION_MGMT_SCRIPT =
            ExtensionSessionManagement.CONTEXT_CONFIG_SESSION + ".script";
    public static final String CONTEXT_CONFIG_SESSION_MGMT_SCRIPT_NAME =
            CONTEXT_CONFIG_SESSION_MGMT_SCRIPT + ".name";
    public static final String CONTEXT_CONFIG_SESSION_MGMT_SCRIPT_PARAMS =
            CONTEXT_CONFIG_SESSION_MGMT_SCRIPT + ".params";

    private static final int METHOD_IDENTIFIER = 2;

    private static final Logger LOG =
            LogManager.getLogger(ScriptBasedSessionManagementMethodType.class);

    private static final String METHOD_NAME =
            Constant.messages.getString("sessionmanagement.method.sc.name");

    private static final String API_METHOD_NAME = "scriptBasedSessionManagement";

    public static final String SCRIPT_TYPE_SESSION = "session";

    private static ExtensionScript extensionScript;

    public class ScriptBasedSessionManagementMethod implements SessionManagementMethod {

        private ScriptWrapper script;
        private SessionWrapper session;
        private Map<String, String> paramValues = new HashMap<>();

        /**
         * Always return a new SessionWrapper, but reuse the session if one exists
         *
         * @param msg
         * @return a new SessionWrapper
         */
        private SessionWrapper getSessionWrapper(HttpMessage msg) {
            if (session != null) {
                return new SessionWrapper(session.getSession(), msg, paramValues);
            }
            session = new SessionWrapper(new ScriptBasedSession(), msg, paramValues);
            return session;
        }

        @Override
        public boolean isConfigured() {
            // Always configured
            return true;
        }

        @Override
        public SessionManagementMethodType getType() {
            return new ScriptBasedSessionManagementMethodType();
        }

        @Override
        public WebSession extractWebSession(HttpMessage msg) {
            SessionScript sessionScript = getScriptInterface(script);
            if (sessionScript != null) {
                SessionWrapper wrapper = getSessionWrapper(msg);
                try {
                    sessionScript.extractWebSession(wrapper);
                } catch (Exception e) {
                    getScriptsExtension().handleScriptException(script, e);
                }
                return wrapper.getSession();
            }
            return new ScriptBasedSession();
        }

        @Override
        public WebSession createEmptyWebSession() {
            return new ScriptBasedSession();
        }

        @Override
        public void clearWebSessionIdentifiers(HttpMessage msg) {
            SessionScript sessionScript = getScriptInterface(script);
            if (sessionScript != null) {
                try {
                    sessionScript.clearWebSessionIdentifiers(getSessionWrapper(msg));
                } catch (Exception e) {
                    getScriptsExtension().handleScriptException(script, e);
                }
            }
        }

        @Override
        public ApiResponse getApiResponseRepresentation() {
            Map<String, String> values = new HashMap<>();
            values.put("methodName", API_METHOD_NAME);
            values.put("scriptName", script.getName());
            values.putAll(paramValues);
            return new SessionMethodApiResponseRepresentation<>(values);
        }

        @Override
        public void processMessageToMatchSession(HttpMessage message, WebSession session)
                throws UnsupportedWebSessionException {
            SessionScript sessionScript = getScriptInterface(script);
            if (sessionScript != null) {
                try {
                    ExtensionScript.recordScriptCalledStats(script);
                    sessionScript.processMessageToMatchSession(getSessionWrapper(message));
                } catch (Exception e) {
                    getScriptsExtension().handleScriptException(script, e);
                }
            }
        }

        @Override
        public SessionManagementMethod clone() {
            ScriptBasedSessionManagementMethod method = new ScriptBasedSessionManagementMethod();
            method.script = this.script;
            method.paramValues = new HashMap<>(this.paramValues);
            return method;
        }

        @Override
        public String toString() {
            return "ScriptBasedSessionManagementMethod [script="
                    + script
                    + ", paramValues="
                    + paramValues
                    + "]";
        }
    }

    public static class ScriptBasedSession extends WebSession {

        private static int generatedNameIndex;
        private Map<String, Object> map = new HashMap<>();

        public ScriptBasedSession(String name) {
            super(name, new HttpState());
        }

        public ScriptBasedSession() {
            super("Script Based Session " + generatedNameIndex++, new HttpState());
        }

        public void setValue(String key, Object value) {
            map.put(key, value);
        }

        public Object getValue(String key) {
            return this.map.get(key);
        }
    }

    @Override
    public ScriptBasedSessionManagementMethod createSessionManagementMethod(int contextId) {
        return new ScriptBasedSessionManagementMethod();
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
    public AbstractSessionManagementMethodOptionsPanel buildOptionsPanel(Context uiSharedContext) {
        return new ScriptBasedSessionManagementMethodOptionsPanel();
    }

    @Override
    public boolean hasOptionsPanel() {
        return true;
    }

    @Override
    public boolean isTypeForMethod(SessionManagementMethod method) {
        return method instanceof ScriptBasedSessionManagementMethod;
    }

    @SuppressWarnings("serial")
    public static class ScriptBasedSessionManagementMethodOptionsPanel
            extends AbstractSessionManagementMethodOptionsPanel {

        private static final long serialVersionUID = 7812841049435409987L;

        private final String SCRIPT_NAME_LABEL =
                Constant.messages.getString("session.method.script.field.label.scriptName");
        private final String LABEL_NOT_LOADED =
                Constant.messages.getString("session.method.script.field.label.notLoaded");
        private JXComboBox scriptsComboBox;
        private JButton loadScriptButton;

        private ScriptBasedSessionManagementMethod method;

        private ScriptWrapper loadedScript;

        private JPanel dynamicContentPanel;

        private DynamicFieldsPanel dynamicFieldsPanel;

        public ScriptBasedSessionManagementMethodOptionsPanel() {
            super();
            initialize();
        }

        private void initialize() {
            this.setLayout(new GridBagLayout());

            this.add(new JLabel(SCRIPT_NAME_LABEL), LayoutHelper.getGBC(0, 0, 1, 0.0d, 0.0d));

            scriptsComboBox = new JXComboBox();
            scriptsComboBox.addHighlighter(
                    new FontHighlighter(
                            (renderer, adapter) -> loadedScript == adapter.getValue(),
                            scriptsComboBox.getFont().deriveFont(Font.BOLD)));
            scriptsComboBox.setRenderer(
                    new DefaultListRenderer(
                            sw -> {
                                if (sw == null) {
                                    return null;
                                }

                                String name = ((ScriptWrapper) sw).getName();
                                if (loadedScript == sw) {
                                    return Constant.messages.getString(
                                            "session.method.script.loaded", name);
                                }
                                return name;
                            }));
            this.add(this.scriptsComboBox, LayoutHelper.getGBC(1, 0, 1, 1.0d, 0.0d));

            this.loadScriptButton =
                    new JButton(Constant.messages.getString("session.method.script.load.button"));
            this.add(this.loadScriptButton, LayoutHelper.getGBC(2, 0, 1, 0.0d, 0.0d));
            this.loadScriptButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            loadScript((ScriptWrapper) scriptsComboBox.getSelectedItem(), true);
                        }
                    });

            // Make sure the 'Load' button is disabled when nothing is selected
            this.loadScriptButton.setEnabled(false);
            this.scriptsComboBox.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            loadScriptButton.setEnabled(scriptsComboBox.getSelectedIndex() >= 0);
                        }
                    });

            this.dynamicContentPanel = new JPanel(new BorderLayout());
            this.add(this.dynamicContentPanel, LayoutHelper.getGBC(0, 1, 3, 1.0d, 0.0d));
            this.dynamicContentPanel.add(new ZapHtmlLabel(LABEL_NOT_LOADED));
        }

        @Override
        public void validateFields() throws IllegalStateException {
            if (this.loadedScript == null) {
                this.scriptsComboBox.requestFocusInWindow();
                throw new IllegalStateException(
                        Constant.messages.getString(
                                "session.method.script.dialog.error.text.notLoadedNorConfigured"));
            }
            this.dynamicFieldsPanel.validateFields();
        }

        @Override
        public void saveMethod() {
            this.method.script = (ScriptWrapper) this.scriptsComboBox.getSelectedItem();
            // This method will also be called when switching panels to save a temporary state so
            // the state of the session management method might not be valid
            if (this.dynamicFieldsPanel != null) {
                this.method.paramValues = this.dynamicFieldsPanel.getFieldValues();
            } else {
                this.method.paramValues = Collections.emptyMap();
            }
        }

        private void loadScript(ScriptWrapper scriptW, boolean adaptOldValues) {
            SessionScript script = getScriptInterface(scriptW);

            if (script == null) {
                LOG.warn(
                        "The script {} does not properly implement the Session Script interface.",
                        scriptW.getName());
                warnAndResetPanel(
                        Constant.messages.getString(
                                "session.method.script.dialog.error.text.interface",
                                scriptW.getName()));
                return;
            }

            try {
                String[] requiredParams = script.getRequiredParamsNames();
                String[] optionalParams = script.getOptionalParamsNames();

                // If there's an already loaded script, make sure we save its values and _try_
                // to place them in the new panel

                Map<String, String> oldValues = null;
                if (adaptOldValues && dynamicFieldsPanel != null) {
                    oldValues = dynamicFieldsPanel.getFieldValues();
                    LOG.debug("Trying to adapt old values: {}", oldValues);
                }

                this.dynamicFieldsPanel = new DynamicFieldsPanel(requiredParams, optionalParams);
                this.loadedScript = scriptW;
                if (adaptOldValues && oldValues != null) {
                    this.dynamicFieldsPanel.bindFieldValues(oldValues);
                }

                this.dynamicContentPanel.removeAll();
                this.dynamicContentPanel.add(dynamicFieldsPanel, BorderLayout.CENTER);
                this.dynamicContentPanel.revalidate();

            } catch (Exception e) {
                getScriptsExtension().handleScriptException(scriptW, e);
                LOG.error("Error while calling session management script", e);
                warnAndResetPanel(
                        Constant.messages.getString(
                                "session.method.script.dialog.error.text.loading",
                                ExceptionUtils.getRootCauseMessage(e)));
            }
        }

        private void warnAndResetPanel(String errorMessage) {
            JOptionPane.showMessageDialog(
                    this,
                    errorMessage,
                    Constant.messages.getString("session.method.script.dialog.error.title"),
                    JOptionPane.ERROR_MESSAGE);
            this.loadedScript = null;
            this.scriptsComboBox.setSelectedItem(null);
            this.dynamicFieldsPanel = null;
            this.dynamicContentPanel.removeAll();
            this.dynamicContentPanel.add(new JLabel(LABEL_NOT_LOADED), BorderLayout.CENTER);
            this.dynamicContentPanel.revalidate();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void bindMethod(SessionManagementMethod method)
                throws UnsupportedSessionManagementMethodException {
            this.method = (ScriptBasedSessionManagementMethod) method;

            // Make sure the list of scripts is refreshed
            List<ScriptWrapper> scripts = getScriptsExtension().getScripts(SCRIPT_TYPE_SESSION);
            DefaultComboBoxModel<ScriptWrapper> model =
                    new DefaultComboBoxModel<>(scripts.toArray(new ScriptWrapper[scripts.size()]));
            this.scriptsComboBox.setModel(model);
            this.scriptsComboBox.setSelectedItem(this.method.script);
            this.loadScriptButton.setEnabled(this.method.script != null);

            // Load the selected script, if any
            if (this.method.script != null) {
                loadScript(this.method.script, false);
                if (this.dynamicFieldsPanel != null) {
                    this.dynamicFieldsPanel.bindFieldValues(this.method.paramValues);
                }
            }
        }

        @Override
        public SessionManagementMethod getMethod() {
            return this.method;
        }
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        // Hook up the Script Type
        if (getScriptsExtension() != null) {
            LOG.debug("Registering Script...");
            getScriptsExtension()
                    .registerScriptType(
                            new ScriptType(
                                    SCRIPT_TYPE_SESSION,
                                    "session.method.script.type",
                                    getScriptsExtension().getView() != null
                                            ? new ImageIcon(
                                                    getClass()
                                                            .getResource(
                                                                    "/resource/icon/16/script-session.png"))
                                            : null,
                                    false,
                                    new String[] {ScriptType.CAPABILITY_APPEND}));
        }
    }

    @Override
    public SessionManagementMethod loadMethodFromSession(Session session, int contextId)
            throws DatabaseException {
        ScriptBasedSessionManagementMethod method = new ScriptBasedSessionManagementMethod();

        List<String> scripts =
                session.getContextDataStrings(
                        contextId, RecordContext.TYPE_SESSION_MANAGEMENT_FIELD_1);
        if (scripts.size() > 0) {
            // Load the script and make sure it still exists and still follows the required
            // interface
            this.loadMethod(
                    method,
                    scripts.get(0),
                    session.getContextDataStrings(
                            contextId, RecordContext.TYPE_SESSION_MANAGEMENT_FIELD_2));
        }
        return method;
    }

    @Override
    public void exportData(Configuration config, SessionManagementMethod sessionMethod) {
        if (!(sessionMethod instanceof ScriptBasedSessionManagementMethod)) {
            throw new UnsupportedSessionManagementMethodException(
                    "Script based session management type only supports: "
                            + ScriptBasedSessionManagementMethod.class.getName());
        }
        ScriptBasedSessionManagementMethod method =
                (ScriptBasedSessionManagementMethod) sessionMethod;
        if (method.script != null) {
            config.setProperty(CONTEXT_CONFIG_SESSION_MGMT_SCRIPT_NAME, method.script.getName());
        }
        config.setProperty(
                CONTEXT_CONFIG_SESSION_MGMT_SCRIPT_PARAMS,
                EncodingUtils.mapToString(method.paramValues));
    }

    @Override
    public void persistMethodToSession(
            Session session, int contextId, SessionManagementMethod method)
            throws DatabaseException {

        if (!(method instanceof ScriptBasedSessionManagementMethod)) {
            throw new UnsupportedSessionManagementMethodException(
                    "Script based session management type only supports: "
                            + ScriptBasedSessionManagementMethod.class.getName());
        }

        ScriptBasedSessionManagementMethod scriptMethod =
                (ScriptBasedSessionManagementMethod) method;
        if (scriptMethod.script != null) {
            session.setContextData(
                    contextId,
                    RecordContext.TYPE_SESSION_MANAGEMENT_FIELD_1,
                    scriptMethod.script.getName());
        }
        session.setContextData(
                contextId,
                RecordContext.TYPE_SESSION_MANAGEMENT_FIELD_2,
                EncodingUtils.mapToString(scriptMethod.paramValues));
    }

    private void loadMethod(
            ScriptBasedSessionManagementMethod method,
            String scriptName,
            List<String> paramValuesS) {

        // Load the script and make sure it still exists and still follows the required interface
        if (scriptName != null) {
            ScriptWrapper script = getScriptsExtension().getScript(scriptName);
            if (script == null) {
                LOG.error(
                        "Unable to find script while loading Script Based Session Management Method for name: {}",
                        scriptName);
                if (View.isInitialised()) {
                    View.getSingleton()
                            .showMessageDialog(
                                    Constant.messages.getString(
                                            "session.method.script.load.errorScriptNotFound",
                                            scriptName));
                }
                return;
            }
            LOG.info("Loaded script:{}", script.getName());
            method.script = script;

            // Check script interface
            SessionScript s = getScriptInterface(script);
            if (s == null) {
                LOG.error(
                        "Unable to load Script Based Session Management method. The script {} does not properly implement the Session Management Script interface.",
                        scriptName);
                return;
            }
        }

        // Load the parameter values
        Map<String, String> paramValues = null;
        if (paramValuesS != null && paramValuesS.size() > 0) {
            paramValues = EncodingUtils.stringToMap(paramValuesS.get(0));
            method.paramValues = paramValues;
        } else {
            method.paramValues = new HashMap<>();
            LOG.error(
                    "Unable to load script parameter values loading Script Based Session Management Method for name: {}",
                    scriptName);
        }
    }

    @Override
    public void importData(Configuration config, SessionManagementMethod sessionMethod)
            throws ConfigurationException {
        if (!(sessionMethod instanceof ScriptBasedSessionManagementMethod)) {
            throw new UnsupportedSessionManagementMethodException(
                    "Script based session management type only supports: "
                            + ScriptBasedSessionManagementMethod.class.getName());
        }
        ScriptBasedSessionManagementMethod method =
                (ScriptBasedSessionManagementMethod) sessionMethod;
        this.loadMethod(
                method,
                config.getString(CONTEXT_CONFIG_SESSION_MGMT_SCRIPT_NAME),
                objListToStrList(config.getList(CONTEXT_CONFIG_SESSION_MGMT_SCRIPT_PARAMS)));
    }

    private static List<String> objListToStrList(List<Object> oList) {
        List<String> sList = new ArrayList<>(oList.size());
        for (Object o : oList) {
            sList.add(o.toString());
        }
        return sList;
    }

    /* API related constants and methods. */
    private static final String PARAM_SCRIPT_NAME = "scriptName";
    private static final String PARAM_SCRIPT_CONFIG_PARAMS = "scriptConfigParams";

    @Override
    public ApiDynamicActionImplementor getSetMethodForContextApiAction() {
        return new ApiDynamicActionImplementor(
                API_METHOD_NAME,
                new String[] {PARAM_SCRIPT_NAME},
                new String[] {PARAM_SCRIPT_CONFIG_PARAMS}) {

            @Override
            public void handleAction(JSONObject params) throws ApiException {
                Context context =
                        ApiUtils.getContextByParamId(params, SessionManagementAPI.PARAM_CONTEXT_ID);
                String scriptName = ApiUtils.getNonEmptyStringParam(params, PARAM_SCRIPT_NAME);

                // Prepare the method
                ScriptBasedSessionManagementMethod method =
                        createSessionManagementMethod(context.getId());

                // Load the script and make sure it exists and follows the required interface
                ScriptWrapper script = getScriptsExtension().getScript(scriptName);
                if (script == null) {
                    LOG.error(
                            "Unable to find script while loading Script Based Session Management Method for name: {}",
                            scriptName);
                    throw new ApiException(ApiException.Type.SCRIPT_NOT_FOUND, scriptName);
                } else {
                    LOG.info("Loaded script for API:{}", script.getName());
                }
                method.script = script;

                SessionScript sessionScript = getScriptInterface(script);
                String[] requiredParams = sessionScript.getRequiredParamsNames();
                String[] optionalParams = sessionScript.getOptionalParamsNames();
                LOG.debug(
                        "Loaded session management script - required parameters: {} - optial parameters: {}",
                        Arrays.toString(requiredParams),
                        Arrays.toString(optionalParams));

                Map<String, String> paramValues = new HashMap<>();
                for (String rp : requiredParams) {
                    // If one of the required parameters is not present, it will throw
                    // an exception
                    String val = ApiUtils.getNonEmptyStringParam(params, rp);
                    paramValues.put(rp, val);
                }

                for (String op : optionalParams)
                    paramValues.put(op, ApiUtils.getOptionalStringParam(params, op));
                method.paramValues = paramValues;
                LOG.debug("Loaded session management script parameters:{}", paramValues);

                context.setSessionManagementMethod(method);
            }
        };
    }

    private static ExtensionScript getScriptsExtension() {
        if (extensionScript == null)
            extensionScript =
                    Control.getSingleton().getExtensionLoader().getExtension(ExtensionScript.class);
        return extensionScript;
    }

    /**
     * Sets the {@code ExtensionScript}.
     *
     * <p><strong>Note:</strong> Not part of the public API.
     *
     * @param extension the script extension.
     */
    public static void setExtensionScript(ExtensionScript extension) {
        extensionScript = extension;
    }

    private static SessionScript getScriptInterface(ScriptWrapper script) {
        try {
            return getScriptsExtension().getInterface(script, SessionScript.class);
        } catch (Exception e) {
            getScriptsExtension()
                    .handleFailedScriptInterface(
                            script,
                            Constant.messages.getString(
                                    "session.method.script.dialog.error.text.interface",
                                    script.getName()));
        }
        return null;
    }

    public static class SessionWrapper {

        private ScriptBasedSession session;
        private HttpMessage httpMessage;
        private Map<String, String> paramValues;

        public SessionWrapper(
                ScriptBasedSession session, HttpMessage msg, Map<String, String> paramValues) {
            this.session = session;
            this.httpMessage = msg;
            this.paramValues = paramValues;
        }

        public ScriptBasedSession getSession() {
            return session;
        }

        public HttpMessage getHttpMessage() {
            return httpMessage;
        }

        public String getParam(String key) {
            return paramValues.get(key);
        }
    }

    /** The Interface that needs to be implemented by a Session Script. */
    public interface SessionScript {

        public void extractWebSession(SessionWrapper sessionWrapper);

        public void clearWebSessionIdentifiers(SessionWrapper sessionWrapper);

        public void processMessageToMatchSession(SessionWrapper sessionWrapper);

        public String[] getRequiredParamsNames();

        public String[] getOptionalParamsNames();
    }

    static class SessionMethodApiResponseRepresentation<T> extends ApiResponseSet<T> {

        public SessionMethodApiResponseRepresentation(Map<String, T> values) {
            super("method", values);
        }

        @Override
        public JSON toJSON() {
            JSONObject response = new JSONObject();
            response.put(getName(), super.toJSON());
            return response;
        }
    }
}
