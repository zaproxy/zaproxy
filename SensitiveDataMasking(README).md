# Sensitive Data Masking – Summary of Code Changes

This document summarizes the code changes made in this branch/PR compared to the ZAP upstream baseline you started from.

## 1. Added new parameter class for storing masking settings

**File**: `zaproxy/zap/src/main/java/org/zaproxy/zap/extension/sensitive/OptionsParamSensitiveData.java`  
**Intention**: Introduce configurable options for masking sensitive HTTP data (enable/disable, mask value, and sensitive key patterns).  
**Changes**:
- Added a new `AbstractParam` implementation with configuration keys:
  - `sensitiveData.masking.enabled`
  - `sensitiveData.mask.value`
  - `sensitiveData.keys`
- Added defaults for common sensitive keys (for example `password`, `token`, `authorization`, `cookie`).
- Normalized/deduplicated configured keys (preserving order).
- Implemented defensive parsing if configuration is not available.
**Feature added**: ZAP can persist and retrieve sensitive-data masking preferences from configuration.

## 2. Added core masking utilities (values, header lines, and header blocks)

**File**: `zaproxy/zap/src/main/java/org/zaproxy/zap/extension/sensitive/SensitiveDataUtils.java`  
**Intention**: Centralize masking logic so UI/components can reuse it consistently.  
**Changes**:
- Implemented `maskIfSensitive(key, value, options)` (case-insensitive key matching).
- Added `maskHeaderLineIfSensitive("Header: value", options)` to mask only the value portion.
- Added `maskHeaderBlockIfSensitive(block, options)` to mask multi-line header blocks.
- Preserved the original line separator style (`\r\n`, `\n`, or `\r`) when rewriting header blocks.
- Added Log4j2 `INFO` logging when masking is applied.
**Feature added**: A reusable API to mask sensitive values across different UI display contexts.

## 3. Added an extension to register the options param set and the Options panel

**File**: `zaproxy/zap/src/main/java/org/zaproxy/zap/extension/sensitive/ExtensionSensitiveData.java`  
**Intention**: Register the sensitive-data options as a proper ZAP extension and expose a UI panel in Options.  
**Changes**:
- Created `ExtensionSensitiveData` (extends `ExtensionAdaptor`).
- Registered the param set via `extensionHook.addOptionsParamSet(optionsParam)`.
- Added an options panel via `extensionHook.getHookView().addOptionPanel(...)` when `hasView()` is true.
**Feature added**: The feature becomes a first-class “extension-style” options param set and UI panel.

## 4. Added a UI Options panel to configure masking

**File**: `zaproxy/zap/src/main/java/org/zaproxy/zap/extension/sensitive/OptionsSensitiveDataPanel.java`  
**Intention**: Provide GUI controls to enable/disable masking, set a mask value, and edit sensitive keys.  
**Changes**:
- New `AbstractParamPanel` named “Sensitive Data”.
- Checkbox: “Enable masking”.
- Text field: mask value.
- Text area: sensitive keys (one per line).
- Loads/saves values via `OptionsParam.getParamSet(OptionsParamSensitiveData.class)`.
**Feature added**: Users can configure masking from **Tools → Options → Sensitive Data**.

## 5. Registered the new extension in core built-in extensions

**File**: `zaproxy/zap/src/main/java/org/zaproxy/zap/control/CoreFunctionality.java`  
**Intention**: Ensure the new core extension loads automatically in the standard ZAP build.  
**Changes**:
- Added `new org.zaproxy.zap.extension.sensitive.ExtensionSensitiveData()` to the built-in extensions list.
**Feature added**: The Sensitive Data extension loads by default (core build), enabling the Options panel and param set registration.

## 6. Integrated masking into request header display models (UI request panel text)

**Files**:
- `zaproxy/zap/src/main/java/org/zaproxy/zap/extension/httppanel/view/impl/models/http/request/RequestHeaderStringHttpPanelViewModel.java`
- `zaproxy/zap/src/main/java/org/zaproxy/zap/extension/httppanel/view/impl/models/http/request/RequestStringHttpPanelViewModel.java`
- `zaproxy/zap/src/main/java/org/zaproxy/zap/extension/httppanel/view/largerequest/LargeRequestStringHttpPanelViewModel.java`

**Intention**: Ensure masked data appears in the Request panel header text views when masking is enabled.  
**Changes**:
- In `getData()`, retrieves the sensitive options via:
  - `Model.getSingleton().getOptionsParam().getParamSet(OptionsParamSensitiveData.class)`
- Masks the rendered header text via:
  - `SensitiveDataUtils.maskHeaderBlockIfSensitive(header, sensitiveDataOptions)`
**Feature added**: Sensitive header values are masked in request header displays (when masking is enabled).

## 7. Added unit tests for masking utilities

**File**: `zaproxy/zap/src/test/java/org/zaproxy/zap/extension/sensitive/SensitiveDataUtilsUnitTest.java`  
**Intention**: Validate masking behavior and prevent regressions.  
**Changes**:
- Added tests covering:
  - masking enabled/disabled behavior
  - custom mask value behavior
  - masking a single header line
  - masking a multi-line header block
**Feature added**: Automated verification that masking logic behaves as expected.


### PR Updates / New Additions Compared to Previous PR

1.  **Architecture shift**: moved from `OptionsParam`-owned param to proper extension-based param set.

2.  **Extension registration**: `ExtensionSensitiveData` now registers the param set and UI panel via `ExtensionHook`.

3.  **UI integration**: HTTP panels updated to retrieve param via `optionsParam.getParamSet(...)` instead of global OptionsParam.

4.  **Verified functionality**: compiles cleanly and unit tests pass. Masking is disabled by default, but the user can enable it in **Tools -> Options -> Sensitive Data**.



