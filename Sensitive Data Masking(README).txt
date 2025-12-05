Sensitive Data Masking – Summary of Code Changes
1. Added new parameter class for storing masking settings

File: zaproxy/zap/src/main/java/org/zaproxy/zap/extension/sensitive/OptionsParamSensitiveData.java
Intention: Introduce configurable options for masking HTTP headers and body fields.
Changes: Added a new parameter class extending AbstractParam, including fields for “enabled”, “headers to mask”, and “body fields to mask”, along with parsing and setter/getter methods.
Feature added: Allows users to store masking preferences that ZAP can access anywhere.

2. Registered the new parameter class inside the global OptionsParam registry

File: zaproxy/zap/src/main/java/org/parosproxy/paros/model/OptionsParam.java
Intention: Make the new sensitive-data settings available to all components.
Changes:

Added a field for OptionsParamSensitiveData.

Loaded it in parse().

Added a getter method.
Feature added: ZAP components can retrieve masking settings through Model.getSingleton().getOptionsParam().getSensitiveDataParam().

3. Added the core masking engine used across the application

File: zaproxy/zap/src/main/java/org/zaproxy/zap/utils/SensitiveDataMasker.java
Intention: Provide a single utility to mask sensitive headers and body fields.
Changes:

Added header masking logic.

Added body masking logic for JSON and form data.

Added toggle logic that only masks when the user has enabled it.
Feature added: All masked views use one consistent implementation.

4. Integrated masking into the proxy history and UI panels

File: zaproxy/zap/src/main/java/org/parosproxy/paros/core/proxy/ProxyListenerLog.java
Intention: Ensure that masked data appears in the History tab, Request/Response panels, and Sites tree.
Changes:

Called SensitiveDataMasker.buildMaskedMessage(msg) at the start of onHttpResponseReceive().

Used the masked message when creating HistoryReference.
Feature added: Sensitive values do not appear in UI panels once masking is enabled.

5. Integrated masking into the passive scan pipeline

File: zaproxy/zap/src/main/java/org/zaproxy/zap/extension/pscan/PassiveScanTask.java
Intention: Prevent sensitive data from leaking into passive-scan logging or follow-up processing.
Changes:

Added masking after all scanners finish analyzing the original message.
Feature added: Passive-scan processes do not record or display sensitive raw data.

6. Ensured masking is applied in generated reports (HTML, XML, JSON)

File: zap-extensions/addOns/reports/src/main/java/org/zaproxy/addon/reports/ReportHelper.java
Intention: Prevent sensitive information from appearing in exported reports.
Changes:

Masking added inside getHttpMessage(int id) before returning the message to the report generator.
Feature added: Reports are generated using masked HTTP messages.

7. Added a new Options panel to configure masking settings

File: zaproxy/zap/src/main/java/org/parosproxy/paros/extension/option/OptionsSensitiveDataPanel.java
Intention: Provide users with a UI to control masking behavior.
Changes:

Created panel with checkbox to enable masking.

Textboxes for header list and body-field list.

Implemented initParam() and saveParam() to bind UI to config.
Feature added: Masking settings appear in Tools → Options → “Sensitive Data”.

8. Registered the new Options panel with ZAP’s extension loader

File: zaproxy/zap/src/main/java/org/parosproxy/paros/extension/option/ExtensionOption.java
Intention: Make the new panel visible in the Options dialog.
Changes:

Added one line to register: addOptionPanel(new OptionsSensitiveDataPanel()).
Feature added: Users can access the masking options panel through the GUI.