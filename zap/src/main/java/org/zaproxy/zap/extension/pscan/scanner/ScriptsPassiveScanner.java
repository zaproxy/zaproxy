/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.extension.pscan.scanner;

import java.lang.reflect.UndeclaredThrowableException;
import net.htmlparser.jericho.Source;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.ExtensionPassiveScan;
import org.zaproxy.zap.extension.pscan.PassiveScript;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;
import org.zaproxy.zap.extension.script.ScriptsCache;
import org.zaproxy.zap.extension.script.ScriptsCache.Configuration;

public class ScriptsPassiveScanner extends PluginPassiveScanner {

    private static final Logger logger = LogManager.getLogger(ScriptsPassiveScanner.class);

    private final ScriptsCache<PassiveScript> scripts;

    private int currentHistoryType;

    public ScriptsPassiveScanner() {
        ExtensionScript extension =
                Control.getSingleton().getExtensionLoader().getExtension(ExtensionScript.class);
        scripts =
                extension != null
                        ? extension.createScriptsCache(
                                Configuration.<PassiveScript>builder()
                                        .setScriptType(ExtensionPassiveScan.SCRIPT_TYPE_PASSIVE)
                                        .setTargetInterface(PassiveScript.class)
                                        .setInterfaceErrorMessageProvider(
                                                sw ->
                                                        Constant.messages.getString(
                                                                "pscan.scripts.interface.passive.error",
                                                                sw.getName()))
                                        .build())
                        : null;
    }

    @Override
    public String getName() {
        return Constant.messages.getString("pscan.scripts.passivescanner.title");
    }

    @Override
    public int getPluginId() {
        return 50001;
    }

    @Override
    public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
        if (scripts == null) {
            return;
        }

        scripts.refreshAndExecute(
                (sw, script) -> {
                    if (appliesToCurrentHistoryType(sw, script)) {
                        script.scan(this, msg, source);
                    }
                });
    }

    @Override
    public ScriptsPassiveScanner copy() {
        ScriptsPassiveScanner copy = new ScriptsPassiveScanner();
        copy.currentHistoryType = currentHistoryType;
        return copy;
    }

    private boolean appliesToCurrentHistoryType(ScriptWrapper wrapper, PassiveScript ps) {
        try {
            return ps.appliesToHistoryType(currentHistoryType);
        } catch (UndeclaredThrowableException e) {
            // Python script implementation throws an exception if this optional/default method is
            // not
            // actually implemented by the script (other script implementations, Zest/ECMAScript,
            // just
            // use the default method).
            if (e.getCause() instanceof NoSuchMethodException
                    && "appliesToHistoryType".equals(e.getCause().getMessage())) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Script [Name={}, Engine={}]  does not implement the optional method appliesToHistoryType: ",
                            wrapper.getName(),
                            wrapper.getEngineName(),
                            e);
                }
                return super.appliesToHistoryType(currentHistoryType);
            }
            throw e;
        }
    }

    /** @since 2.9.0 */
    @Override
    public AlertBuilder newAlert() {
        return super.newAlert();
    }

    /**
     * @deprecated (2.9.0) Use {@link #newAlert()} to build and {@link AlertBuilder#raise() raise}
     *     alerts.
     */
    @Deprecated
    public void raiseAlert(
            int risk,
            int confidence,
            String name,
            String description,
            String uri,
            String param,
            String attack,
            String otherInfo,
            String solution,
            String evidence,
            int cweId,
            int wascId,
            HttpMessage msg) {

        raiseAlert(
                risk,
                confidence,
                name,
                description,
                uri,
                param,
                attack,
                otherInfo,
                solution,
                evidence,
                null,
                cweId,
                wascId,
                msg);
    }

    /**
     * @deprecated (2.9.0) Use {@link #newAlert()} to build and {@link AlertBuilder#raise() raise}
     *     alerts.
     */
    @Deprecated
    public void raiseAlert(
            int risk,
            int confidence,
            String name,
            String description,
            String uri,
            String param,
            String attack,
            String otherInfo,
            String solution,
            String evidence,
            String reference,
            int cweId,
            int wascId,
            HttpMessage msg) {

        newAlert()
                .setRisk(risk)
                .setConfidence(confidence)
                .setName(name)
                .setDescription(description)
                .setParam(param)
                .setOtherInfo(otherInfo)
                .setSolution(solution)
                .setReference(reference)
                .setEvidence(evidence)
                .setCweId(cweId)
                .setWascId(wascId)
                .setMessage(msg)
                .raise();
    }

    /** @deprecated 2.12.0 Replaced by {@link #addHistoryTag(String)} */
    @Override
    @Deprecated
    public void addTag(String tag) {
        super.addHistoryTag(tag);
    }

    @Override
    public void addHistoryTag(String tag) {
        super.addHistoryTag(tag);
    }

    @Override
    public boolean appliesToHistoryType(int historyType) {
        this.currentHistoryType = historyType;
        return true;
    }
}
