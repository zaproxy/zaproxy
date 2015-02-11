/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2015 The ZAP Development Team
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
package org.zaproxy.zap.control;

import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 * An add-on entry of {@code ZapVersions.xml} file.
 * <p>
 * It also reads:
 * <ul>
 * <li>status;</li>
 * <li>file;</li>
 * <li>size;</li>
 * <li>info;</li>
 * <li>hash.</li>
 * </ul>
 * 
 * @since 2.4.0
 */
public class ZapVersionsAddOnEntry extends BaseZapAddOnXmlData {

    private static final String STATUS = "status";
    private static final String FILE = "file";
    private static final String SIZE = "size";
    private static final String INFO = "info";
    private static final String HASH = "hash";

    private String status;
    private String file;
    private long size;
    private String info;
    private String hash;

    public ZapVersionsAddOnEntry(HierarchicalConfiguration node) {
        super(node);
    }

    @Override
    protected void readAdditionalData(HierarchicalConfiguration zapAddOnData) {
        status = zapAddOnData.getString(STATUS);
        file = zapAddOnData.getString(FILE);
        size = zapAddOnData.getLong(SIZE);
        info = zapAddOnData.getString(INFO);
        hash = zapAddOnData.getString(HASH, null);
    }

    /**
     * Returns the status of the add-on, ("alpha", "beta" or "release").
     *
     * @return the status of the add-on
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the name of the file of the add-on.
     *
     * @return the name of the file
     */
    public String getFile() {
        return file;
    }

    /**
     * Returns the size of the add-on, in bytes.
     *
     * @return the size, in bytes, of the add-on
     */
    public long getSize() {
        return size;
    }

    /**
     * Returns the URL to obtain more information about the add-on.
     *
     * @return the URL to obtain more information about the add-on
     */
    public String getInfo() {
        return info;
    }

    /**
     * Returns the hash of the add-on file.
     *
     * @return the hash of the add-on, {@code null} if not present
     */
    public String getHash() {
        return hash;
    }
}
