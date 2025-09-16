/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2025 The ZAP Development Team
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.parosproxy.paros.core.scanner.Alert;

/**
 * A helper class for creating example alerts in scan rules.
 *
 * <p>This class provides utility methods for creating example alerts that demonstrate
 * the types of vulnerabilities a scanner can detect. Example alerts are used for
 * documentation, testing, and UI purposes but are never persisted to the database.
 *
 * @since 2.15.0
 */
public class ExampleAlertBuilder {

    private static final String EXAMPLE_URI = "https://www.example.com/app/";
    private static final String EXAMPLE_PARAM = "input";
    private static final String EXAMPLE_ATTACK = "<script>alert('XSS')</script>";
    private static final String EXAMPLE_EVIDENCE = "alert('XSS')";

    private ExampleAlertBuilder() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a new example alert builder for the given plugin.
     *
     * @param pluginId the ID of the plugin creating the example alert
     * @param name the name of the alert
     * @return a new Alert.Builder configured for example alerts
     */
    public static Alert.Builder newExampleAlert(int pluginId, String name) {
        return Alert.builder()
                .setPluginId(pluginId)
                .setName(name)
                .setUri(EXAMPLE_URI)
                .setSource(Alert.Source.ACTIVE); // Default to active source
    }

    /**
     * Creates a high-risk example alert.
     *
     * @param pluginId the ID of the plugin creating the example alert
     * @param name the name of the alert
     * @param description the description of the vulnerability
     * @param solution the recommended solution
     * @return a configured Alert.Builder for a high-risk alert
     */
    public static Alert.Builder createHighRiskExample(
            int pluginId, String name, String description, String solution) {
        return newExampleAlert(pluginId, name)
                .setRisk(Alert.RISK_HIGH)
                .setConfidence(Alert.CONFIDENCE_MEDIUM)
                .setDescription(description)
                .setSolution(solution)
                .setParam(EXAMPLE_PARAM)
                .setAttack(EXAMPLE_ATTACK)
                .setEvidence(EXAMPLE_EVIDENCE);
    }

    /**
     * Creates a medium-risk example alert.
     *
     * @param pluginId the ID of the plugin creating the example alert
     * @param name the name of the alert
     * @param description the description of the vulnerability
     * @param solution the recommended solution
     * @return a configured Alert.Builder for a medium-risk alert
     */
    public static Alert.Builder createMediumRiskExample(
            int pluginId, String name, String description, String solution) {
        return newExampleAlert(pluginId, name)
                .setRisk(Alert.RISK_MEDIUM)
                .setConfidence(Alert.CONFIDENCE_MEDIUM)
                .setDescription(description)
                .setSolution(solution)
                .setParam(EXAMPLE_PARAM);
    }

    /**
     * Creates a low-risk example alert.
     *
     * @param pluginId the ID of the plugin creating the example alert
     * @param name the name of the alert
     * @param description the description of the vulnerability
     * @param solution the recommended solution
     * @return a configured Alert.Builder for a low-risk alert
     */
    public static Alert.Builder createLowRiskExample(
            int pluginId, String name, String description, String solution) {
        return newExampleAlert(pluginId, name)
                .setRisk(Alert.RISK_LOW)
                .setConfidence(Alert.CONFIDENCE_MEDIUM)
                .setDescription(description)
                .setSolution(solution);
    }

    /**
     * Creates an informational example alert.
     *
     * @param pluginId the ID of the plugin creating the example alert
     * @param name the name of the alert
     * @param description the description of the issue
     * @param otherInfo additional information about the issue
     * @return a configured Alert.Builder for an informational alert
     */
    public static Alert.Builder createInfoExample(
            int pluginId, String name, String description, String otherInfo) {
        return newExampleAlert(pluginId, name)
                .setRisk(Alert.RISK_INFO)
                .setConfidence(Alert.CONFIDENCE_MEDIUM)
                .setDescription(description)
                .setOtherInfo(otherInfo);
    }

    /**
     * Creates a list of example alerts from the provided builders.
     *
     * @param builders the alert builders to convert to alerts
     * @return a list of built example alerts
     */
    public static List<Alert> buildExampleAlerts(Alert.Builder... builders) {
        List<Alert> alerts = new ArrayList<>();
        for (Alert.Builder builder : builders) {
            alerts.add(builder.build());
        }
        return alerts;
    }

    /**
     * Creates a list of example alerts from the provided builders.
     *
     * @param builders the alert builders to convert to alerts
     * @return a list of built example alerts
     */
    public static List<Alert> buildExampleAlerts(List<Alert.Builder> builders) {
        List<Alert> alerts = new ArrayList<>();
        for (Alert.Builder builder : builders) {
            alerts.add(builder.build());
        }
        return alerts;
    }

    /**
     * Creates an SQL injection example alert.
     *
     * @param pluginId the ID of the plugin creating the example alert
     * @return a configured Alert.Builder for an SQL injection alert
     */
    public static Alert.Builder createSqlInjectionExample(int pluginId) {
        return createHighRiskExample(
                pluginId,
                "SQL Injection",
                "SQL injection may be possible.",
                "Use prepared statements and parameterized queries to prevent SQL injection.")
                .setParam("id")
                .setAttack("1' OR '1'='1")
                .setEvidence("mysql_fetch_array()")
                .setCweId(89)
                .setWascId(19);
    }

    /**
     * Creates a Cross-Site Scripting (XSS) example alert.
     *
     * @param pluginId the ID of the plugin creating the example alert
     * @return a configured Alert.Builder for an XSS alert
     */
    public static Alert.Builder createXssExample(int pluginId) {
        return createHighRiskExample(
                pluginId,
                "Cross Site Scripting (Reflected)",
                "Cross-site Scripting (XSS) is possible.",
                "Validate all input and encode all output.")
                .setParam("search")
                .setAttack("<script>alert('XSS')</script>")
                .setEvidence("alert('XSS')")
                .setCweId(79)
                .setWascId(8);
    }

    /**
     * Creates an information disclosure example alert.
     *
     * @param pluginId the ID of the plugin creating the example alert
     * @return a configured Alert.Builder for an information disclosure alert
     */
    public static Alert.Builder createInfoDisclosureExample(int pluginId) {
        return createMediumRiskExample(
                pluginId,
                "Information Disclosure - Sensitive Information in URL",
                "The request appears to contain sensitive information in the URL.",
                "Do not pass sensitive information in URLs.")
                .setParam("password")
                .setEvidence("password=secret123")
                .setCweId(200)
                .setWascId(13);
    }
}