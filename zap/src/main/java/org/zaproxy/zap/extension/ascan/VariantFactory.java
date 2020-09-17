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
package org.zaproxy.zap.extension.ascan;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.core.scanner.Variant;
import org.parosproxy.paros.core.scanner.VariantCookie;
import org.parosproxy.paros.core.scanner.VariantCustom;
import org.parosproxy.paros.core.scanner.VariantDdnPath;
import org.parosproxy.paros.core.scanner.VariantDirectWebRemotingQuery;
import org.parosproxy.paros.core.scanner.VariantFormQuery;
import org.parosproxy.paros.core.scanner.VariantGWTQuery;
import org.parosproxy.paros.core.scanner.VariantHeader;
import org.parosproxy.paros.core.scanner.VariantJSONQuery;
import org.parosproxy.paros.core.scanner.VariantMultipartFormParameters;
import org.parosproxy.paros.core.scanner.VariantODataFilterQuery;
import org.parosproxy.paros.core.scanner.VariantODataIdQuery;
import org.parosproxy.paros.core.scanner.VariantURLPath;
import org.parosproxy.paros.core.scanner.VariantURLQuery;
import org.parosproxy.paros.core.scanner.VariantUserDefined;
import org.parosproxy.paros.core.scanner.VariantXMLQuery;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;

public class VariantFactory {
    private static final Logger LOG = Logger.getLogger(VariantFactory.class);

    private static ExtensionScript extension;
    private final List<Class<? extends Variant>> customVariants = new ArrayList<>();

    public void addVariant(Class<? extends Variant> variantClass) {
        customVariants.add(variantClass);
    }

    public void removeVariant(Class<? extends Variant> variantClass) {
        customVariants.remove(variantClass);
    }

    public List<Variant> createVariants(ScannerParam scanOptions, HttpMessage message) {
        List<Variant> listVariant = new ArrayList<Variant>();

        int targets = scanOptions.getTargetParamsInjectable();
        int enabledRPC = scanOptions.getTargetParamsEnabledRPC();

        // First check URL query-string target configuration
        if ((targets & ScannerParam.TARGET_QUERYSTRING) != 0) {
            VariantURLQuery vuq = new VariantURLQuery();
            vuq.setAddQueryParam(scanOptions.isAddQueryParam());
            listVariant.add(vuq);

            if ((enabledRPC & ScannerParam.RPC_ODATA) != 0) {
                listVariant.add(new VariantODataIdQuery());
                listVariant.add(new VariantODataFilterQuery());
            }

            if ((targets & ScannerParam.TARGET_URLPATH) == 0) {
                // If we're not already doing URLPath we should do DDN when doing QueryString
                listVariant.add(new VariantDdnPath());
            }
        }

        // Then check POST data target configuration and RPC enabled methods
        if ((targets & ScannerParam.TARGET_POSTDATA) != 0) {
            listVariant.add(new VariantFormQuery());

            if ((enabledRPC & ScannerParam.RPC_MULTIPART) != 0) {
                listVariant.add(new VariantMultipartFormParameters());
            }

            if ((enabledRPC & ScannerParam.RPC_XML) != 0) {
                listVariant.add(new VariantXMLQuery());
            }

            if ((enabledRPC & ScannerParam.RPC_JSON) != 0) {
                listVariant.add(new VariantJSONQuery());
            }

            if ((enabledRPC & ScannerParam.RPC_GWT) != 0) {
                listVariant.add(new VariantGWTQuery());
            }

            if ((enabledRPC & ScannerParam.RPC_DWR) != 0) {
                listVariant.add(new VariantDirectWebRemotingQuery());
            }
        }

        if ((targets & ScannerParam.TARGET_HTTPHEADERS) != 0) {
            boolean addVariant = scanOptions.isScanHeadersAllRequests();
            if (!addVariant) {
                // If not scanning all requests check if it looks like a dynamic or static page
                // (based on query/post parameters)
                char[] query = message.getRequestHeader().getURI().getRawQuery();
                addVariant =
                        (query != null && query.length != 0)
                                || message.getRequestBody().length() != 0;
            }

            if (addVariant) {
                listVariant.add(new VariantHeader());
            }
        }

        if ((targets & ScannerParam.TARGET_URLPATH) != 0) {
            listVariant.add(new VariantURLPath());
        }

        if ((targets & ScannerParam.TARGET_COOKIE) != 0) {
            listVariant.add(new VariantCookie());
        }

        // Now is time to initialize all the custom Variants
        if ((enabledRPC & ScannerParam.RPC_CUSTOM) != 0) {
            addScriptVariants(listVariant);
        }

        if ((enabledRPC & ScannerParam.RPC_USERDEF) != 0) {
            listVariant.add(new VariantUserDefined());
        }

        addCustomVariants(listVariant);

        return listVariant;
    }

    public List<Variant> createSiteModifyingVariants() {
        List<Variant> listVariant = new ArrayList<Variant>();

        addScriptVariants(listVariant);
        addCustomVariants(listVariant);

        // Note that none of the built-in variants implement this method.
        // If any are changed to do so in the future then then need to be called here.

        return listVariant;
    }

    private static void addScriptVariants(List<Variant> list) {
        if (getExtension() != null) {
            List<ScriptWrapper> scripts =
                    getExtension().getScripts(ExtensionActiveScan.SCRIPT_TYPE_VARIANT);

            for (ScriptWrapper script : scripts) {
                if (script.isEnabled()) {
                    list.add(new VariantCustom(script, getExtension()));
                }
            }
        }
    }

    private void addCustomVariants(List<Variant> list) {
        for (Class<? extends Variant> variant : customVariants) {
            try {
                list.add(variant.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private static ExtensionScript getExtension() {
        if (extension == null) {
            extension =
                    Control.getSingleton().getExtensionLoader().getExtension(ExtensionScript.class);
        }
        return extension;
    }
}
