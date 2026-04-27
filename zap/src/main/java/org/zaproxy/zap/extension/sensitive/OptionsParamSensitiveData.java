package org.zaproxy.zap.extension.sensitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.configuration.Configuration;
import org.parosproxy.paros.common.AbstractParam;

public class OptionsParamSensitiveData extends AbstractParam {

    private static final String SENSITIVE_DATA_BASE_KEY = "sensitiveData";
    private static final String MASKING_ENABLED_KEY = SENSITIVE_DATA_BASE_KEY + ".masking.enabled";
    private static final String MASK_VALUE_KEY = SENSITIVE_DATA_BASE_KEY + ".mask.value";
    private static final String SENSITIVE_KEYS_KEY = SENSITIVE_DATA_BASE_KEY + ".keys";

    private static final String DEFAULT_MASK_VALUE = "****";

    private static final List<String> DEFAULT_SENSITIVE_KEYS =
            Collections.unmodifiableList(
                    Arrays.asList(
                            "password",
                            "passwd",
                            "pwd",
                            "token",
                            "access_token",
                            "refresh_token",
                            "authorization",
                            "api_key",
                            "apikey",
                            "secret",
                            "session",
                            "cookie"));

    private boolean maskingEnabled;
    private String maskValue;
    private List<String> sensitiveKeys = new ArrayList<>(DEFAULT_SENSITIVE_KEYS);

    @Override
    protected void parse() {
        Configuration config = getConfig();
        if (config == null) {
            maskingEnabled = false;
            maskValue = DEFAULT_MASK_VALUE;
            sensitiveKeys = new ArrayList<>(DEFAULT_SENSITIVE_KEYS);
            return;
        }

        maskingEnabled = config.getBoolean(MASKING_ENABLED_KEY, false);
        maskValue = config.getString(MASK_VALUE_KEY, DEFAULT_MASK_VALUE);
        sensitiveKeys = readSensitiveKeys(config);
    }

    public boolean isMaskingEnabled() {
        return maskingEnabled;
    }

    public void setMaskingEnabled(boolean maskingEnabled) {
        this.maskingEnabled = maskingEnabled;
        setConfigProperty(MASKING_ENABLED_KEY, maskingEnabled);
    }

    public String getMaskValue() {
        return maskValue;
    }

    public void setMaskValue(String maskValue) {
        this.maskValue = (maskValue == null || maskValue.isEmpty()) ? DEFAULT_MASK_VALUE : maskValue;

        setConfigProperty(MASK_VALUE_KEY, this.maskValue);
    }

    public List<String> getSensitiveKeys() {
        return Collections.unmodifiableList(sensitiveKeys);
    }

    public void setSensitiveKeys(List<String> sensitiveKeys) {
        if (sensitiveKeys == null || sensitiveKeys.isEmpty()) {
            this.sensitiveKeys = new ArrayList<>(DEFAULT_SENSITIVE_KEYS);
        } else {
            this.sensitiveKeys = normalizeKeys(sensitiveKeys);
        }

        setConfigProperty(SENSITIVE_KEYS_KEY, this.sensitiveKeys);
    }

    private static List<String> readSensitiveKeys(Configuration config) {
        Object property = config.getProperty(SENSITIVE_KEYS_KEY);

        if (property == null) {
            return new ArrayList<>(DEFAULT_SENSITIVE_KEYS);
        }

        List<Object> configuredValues = config.getList(SENSITIVE_KEYS_KEY);
        List<String> keys = new ArrayList<>();

        for (Object value : configuredValues) {
            if (value != null) {
                String key = value.toString().trim();
                if (!key.isEmpty()) {
                    keys.add(key);
                }
            }
        }

        if (keys.isEmpty()) {
            return new ArrayList<>(DEFAULT_SENSITIVE_KEYS);
        }

        return normalizeKeys(keys);
    }

    private static List<String> normalizeKeys(List<String> keys) {
        Set<String> normalized = new LinkedHashSet<>();

        for (String key : keys) {
            if (key != null) {
                String trimmedKey = key.trim();
                if (!trimmedKey.isEmpty()) {
                    normalized.add(trimmedKey);
                }
            }
        }

        return new ArrayList<>(normalized);
    }

    private void setConfigProperty(String key, Object value) {
        Configuration config = getConfig();

        if (config != null) {
            config.setProperty(key, value);
        }
    }
}