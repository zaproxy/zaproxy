/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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
package ch.csnc.extension.util;

import org.parosproxy.paros.common.AbstractParam;

/** @deprecated (2.12.0) No longer in use. */
@Deprecated
public class OptionsParamExperimentalSliSupport extends AbstractParam {

    public static final String EXPERIMENTAL_SLOT_LIST_INDEXES =
            "certificate.experimentalSlotListIndex";
    private boolean expSliSupportEnabled = false;

    public OptionsParamExperimentalSliSupport() {}

    @Override
    protected void parse() {
        expSliSupportEnabled = getBoolean(EXPERIMENTAL_SLOT_LIST_INDEXES, false);
    }

    /** @deprecated (2.11.0) Use {@link #isExperimentalSliSupportEnabled()} */
    @Deprecated
    public boolean isExerimentalSliSupportEnabled() {
        return isExperimentalSliSupportEnabled();
    }

    public boolean isExperimentalSliSupportEnabled() {
        return expSliSupportEnabled;
    }

    public void setSlotListIndexSupport(boolean expSliSupportEnabled) {
        this.expSliSupportEnabled = expSliSupportEnabled;
        getConfig().setProperty(EXPERIMENTAL_SLOT_LIST_INDEXES, expSliSupportEnabled);
    }
}
