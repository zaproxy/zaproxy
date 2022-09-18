/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
package org.zaproxy.zap.extension.alert;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.network.HttpMalformedHeaderException;

/**
 * A {@link PopupMenuItemAlert} that sets the Confidence of one or more {@link Alert alerts} to
 * False Positive.
 *
 * @since 2.10.0
 */
public class PopupMenuAlertSetFalsePositive extends PopupMenuItemAlert {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(PopupMenuAlertSetFalsePositive.class);

    public PopupMenuAlertSetFalsePositive() {
        super(Constant.messages.getString("scanner.false.positive.popup"), true);
    }

    @Override
    protected void performAction(Alert alert) {
        Alert newAlert = alert.newInstance();
        newAlert.setAlertId(alert.getAlertId());
        newAlert.setConfidence(Alert.CONFIDENCE_FALSE_POSITIVE);
        try {
            getExtensionAlert().updateAlert(newAlert);
        } catch (HttpMalformedHeaderException | DatabaseException e) {
            LOGGER.error("Unable to update confidence for alert: {}", alert.getAlertId(), e);
        }
    }

    @Override
    public boolean isSafe() {
        return true;
    }
}
