package org.zaproxy.zap.extension.sensitive;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

class SensitiveDataUtilsUnitTest {

    private OptionsParamSensitiveData options;

    @BeforeEach
    void setUp() {
        options = new OptionsParamSensitiveData();
        options.load(new ZapXmlConfiguration());
    }

    @Test
    void shouldReturnOriginalValueWhenMaskingIsDisabled() {
        options.setMaskingEnabled(false);

        String result = SensitiveDataUtils.maskIfSensitive("password", "secret123", options);

        assertThat(result, equalTo("secret123"));
    }

    @Test
    void shouldMaskPasswordWhenMaskingIsEnabled() {
        options.setMaskingEnabled(true);

        String result = SensitiveDataUtils.maskIfSensitive("password", "secret123", options);

        assertThat(result, equalTo("****"));
    }

    @Test
    void shouldMaskAuthorizationHeaderWhenMaskingIsEnabled() {
        options.setMaskingEnabled(true);

        String result =
                SensitiveDataUtils.maskIfSensitive("Authorization", "Bearer abc.def.ghi", options);

        assertThat(result, equalTo("****"));
    }

    @Test
    void shouldReturnOriginalValueForNonSensitiveKey() {
        options.setMaskingEnabled(true);

        String result = SensitiveDataUtils.maskIfSensitive("username", "alice", options);

        assertThat(result, equalTo("alice"));
    }

    @Test
    void shouldUseCustomMaskValue() {
        options.setMaskingEnabled(true);
        options.setMaskValue("[REDACTED]");

        String result = SensitiveDataUtils.maskIfSensitive("api_key", "12345", options);

        assertThat(result, equalTo("[REDACTED]"));
    }

    @Test
    void shouldMaskSensitiveHeaderLine() {
        options.setMaskingEnabled(true);

        String result =
                SensitiveDataUtils.maskHeaderLineIfSensitive(
                        "Authorization: Bearer abc.def.ghi", options);

        assertThat(result, equalTo("Authorization: ****"));
    }

    @Test
    void shouldMaskSensitiveHeaderBlock() {
        options.setMaskingEnabled(true);

        String result =
                SensitiveDataUtils.maskHeaderBlockIfSensitive(
                        "GET / HTTP/1.1\nAuthorization: Bearer abc.def.ghi\nHost: example.com",
                        options);

        assertThat(result, equalTo("GET / HTTP/1.1\nAuthorization: ****\nHost: example.com"));
    }
}

