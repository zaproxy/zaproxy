package org.zaproxy.zap.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpHeaderField;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.parosproxy.paros.network.HttpBody;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.zaproxy.zap.extension.sensitive.OptionsParamSensitiveData;



public class SensitiveDataMasker {

    private static final String MASK = "******";

    public static HttpMessage buildMaskedMessage(HttpMessage original) {
        OptionsParam optionsParam = Model.getSingleton().getOptionsParam();
        OptionsParamSensitiveData param = optionsParam.getSensitiveDataParam();

        if (param == null || !param.isEnabled()) {
            // Feature disabled; return original reference (do not clone)
            return original;
        }

        // Clone the message so we don't alter the original (used by scanners, etc.).
        HttpMessage masked = original.cloneRequest(); // clones request/response depending on version
        // If cloneRequest is not suitable, you can use new HttpMessage(original);

        Set<String> headersToMask = toLowerTrimmedSet(param.getHeadersToMask());
        Set<String> bodyFieldsToMask = toLowerTrimmedSet(param.getBodyFieldsToMask());

        // Mask headers
        maskHeaders(masked.getRequestHeader(), headersToMask);
        maskHeaders(masked.getResponseHeader(), headersToMask);

        // Mask bodies (simple JSON/key=value masking)
        maskBody(masked.getRequestBody(), bodyFieldsToMask);
        maskBody(masked.getResponseBody(), bodyFieldsToMask);

        return masked;
    }

    private static Set<String> toLowerTrimmedSet(String csv) {
        if (csv == null || csv.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(
                Arrays.asList(
                        Arrays.stream(csv.split(","))
                                .map(String::trim)
                                .map(s -> s.toLowerCase(Locale.ROOT))
                                .toArray(String[]::new)));
    }

    private static void maskHeaders(HttpHeader header, Set<String> headersToMask) {
        if (header == null || headersToMask.isEmpty()) {
            return;
        }
        for (HttpHeaderField headerField : header.getHeaders()) {
            String name = headerField.getName();
            if (headersToMask.contains(name.toLowerCase(Locale.ROOT))) {
                header.setHeader(name, MASK);
            }
        }
    }

    private static void maskBody(HttpBody body, Set<String> fieldsToMask) {
        if (body == null || fieldsToMask.isEmpty()) {
            return;
        }
        String content = body.toString();
        if (content.isEmpty()) {
            return;
        }

        // Very simple generic masking:
        // For each configured field name, replace `"field":"value"` or `field=value` with masked value.
        String masked = content;
        for (String field : fieldsToMask) {
            if (field.isEmpty()) {
                continue;
            }
            String regexJson = "(\"" + field + "\"\\s*:\\s*\")[^\"]*\"";
            masked = masked.replaceAll(regexJson, "$1" + MASK + "\"");

            String regexForm = "(" + field + "=)[^&\\s]*";
            masked = masked.replaceAll(regexForm, "$1" + MASK);
        }

        body.setBody(masked);
    }

}
