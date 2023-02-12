/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.parosproxy.paros.core.scanner;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.SessionStructure;

/**
 * Variant class used for URL path elements that are defined as Data Driven Nodes. For a URL like:
 * {@literal http://www.example.com/aaa/bbb/ccc?ddd=eee&fff=ggg} parameter position aaa 1 bbb 2 ccc
 * 3 Given: https://www.example.com/en-US/container/item/itemA
 * https://www.example.com/en-US/container/item/itemB Defined as: {@literal
 * https://www.example.com/en-US/container/item/<<DDN1>>} Test/inject {@literal <<DDN1>>}
 */
public class VariantDdnPath implements Variant {

    private static final Logger LOGGER = LogManager.getLogger(VariantDdnPath.class);
    private final List<NameValuePair> stringParam = new ArrayList<>();

    private static final String SHORT_NAME = "datadrivennode";

    @Override
    public String getShortName() {
        return SHORT_NAME;
    }

    @Override
    public void setMessage(HttpMessage msg) {
        try {
            List<String> treePath = SessionStructure.getTreePath(Model.getSingleton(), msg);
            String actualPath = msg.getRequestHeader().getURI().getPath();
            addParamsFromTreePath(treePath, actualPath);
        } catch (URIException e) {
            // Ignore
        }
    }

    void addParamsFromTreePath(List<String> treePath, String actualPath) {
        int position = 0;
        boolean useActualValue = false;
        String[] actualPathParts = null;
        for (String nodeName : treePath) {
            if (nodeName.startsWith(SessionStructure.DATA_DRIVEN_NODE_PREFIX)) {
                if (actualPathParts == null && actualPath != null) {
                    actualPathParts = actualPath.split("/");
                    useActualValue = treePath.size() == actualPathParts.length;
                }
                String value = useActualValue ? actualPathParts[position] : nodeName;
                stringParam.add(
                        new NameValuePair(NameValuePair.TYPE_URL_PATH, nodeName, value, position));
            }
            position++;
        }
    }

    @Override
    public List<NameValuePair> getParamList() {
        return stringParam;
    }

    @Override
    public String setParameter(
            HttpMessage msg, NameValuePair originalPair, String name, String value) {
        return setParameter(msg, originalPair, name, value, false);
    }

    @Override
    public String setEscapedParameter(
            HttpMessage msg, NameValuePair originalPair, String name, String value) {
        return setParameter(msg, originalPair, name, value, true);
    }

    /**
     * Encode the parameter value for a correct URL introduction
     *
     * @param value the value that need to be encoded
     * @return the Encoded value
     */
    private String getEscapedValue(String value) {
        if (value != null) {
            try {
                return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException ex) {
            }
        }
        return "";
    }

    private String setParameter(
            HttpMessage msg,
            NameValuePair originalPair,
            String name,
            String value,
            boolean escaped) {
        URI uri = msg.getRequestHeader().getURI();
        String[] paths = uri.getEscapedPath().split("/");
        if (originalPair.getPosition() < paths.length) {
            String encodedValue = (escaped) ? value : getEscapedValue(value);
            paths[originalPair.getPosition()] = encodedValue;
            String path = String.join("/", paths);
            try {
                uri.setEscapedPath(path);
            } catch (URIException e) {
                // Looks like it wasn't escaped after all
                try {
                    uri.setPath(path);
                } catch (URIException e1) {
                    LOGGER.debug(e1.getMessage(), e1);
                }
                LOGGER.warn(e.getMessage(), e);
            }
        }
        return value;
    }
}
