package org.parosproxy.paros.core.scanner;

/**
 * {@code AppParamValueType} represents the type of AppParameter's value. This is useful for the
 * use-cases like say there is a parameter which is a URL then caller can itself encode/escape it so
 * that it can be directly used to modify {@code HttpMessage} or {@code Variant} has to escape it.
 *
 * @author preetkaran20@gmail.com KSASAN
 */
public enum AppParamValueType {
    REQUIRES_ESCAPING,
    ALREADY_ESCAPED
}
