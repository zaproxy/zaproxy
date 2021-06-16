package org.parosproxy.paros.core.scanner;

/**
 * {@code AppParameter} class wraps the parameters which are used to modify the {@code HttpMessage}.
 * It is specifically used for updating multiple parameters of {@code HttpMessage}
 *
 * @author preetkaran20@gmail.com KSASAN
 */
public class AppParameter {
    private NameValuePair nameValuePair;
    private String param;
    private String value;
    private AppParamValueType appParamValueType;

    AppParameter(
            NameValuePair nameValuePair,
            String param,
            String value,
            AppParamValueType appParamValueType) {
        this.nameValuePair = nameValuePair;
        this.param = param;
        this.value = value;
        this.appParamValueType = appParamValueType;
    }

    public NameValuePair getNameValuePair() {
        return nameValuePair;
    }

    public String getParam() {
        return param;
    }

    public String getValue() {
        return value;
    }

    public AppParamValueType getAppParamValueType() {
        return appParamValueType;
    }
}
