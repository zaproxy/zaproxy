/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package ch.csnc.extension.httpclient;

import org.apache.commons.lang.StringUtils;

/**
 * A representation of Sun PKCS#11 provider configuration. Used to create configurations for instances of
 * {@code sun.security.pkcs11.SunPKCS11}.
 * <p>
 * Example usage: <blockquote>
 * 
 * <pre>
 * SunPKCS11Configuration configuration = new SunPKCS11Configuration("Provider X", "/path/to/pkcs11library");
 * configuration.setSlotId(1);
 * 
 * java.security.Provider p = new sun.security.pkcs11.SunPKCS11(new ByteArrayInputStream(configuration.toString().getBytes()));
 * java.security.Security.addProvider(p);
 * </pre>
 * 
 * </blockquote>
 * </p>
 * <p>
 * <strong>Note:</strong> Only the mandatory attributes, <i>name</i> and <i>library</i>, and the optional attributes,
 * <i>slot</i> and <i>slotListIndex</i>, are implemented.
 * </p>
 * 
 * @see <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/security/p11guide.html#Config">Sun PKCS#11 Configuration</a>
 * 
 */
public class SunPKCS11Configuration {

    private String name;

    private String library;

    private boolean useSlotListIndex;

    private int slotId;

    private int slotListIndex;

    public SunPKCS11Configuration(String name, String library) {
        super();

        setSlotListIndex(0);

        setName(name);
        setLibrary(library);
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter name must not be null.");
        }

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Parameter name must not be empty.");
        }

        this.name = name;
    }

    public String getLibrary() {
        return library;
    }

    public final void setLibrary(String library) {
        if (library == null) {
            throw new IllegalArgumentException("Parameter library must not be null.");
        }

        if (library.isEmpty()) {
            throw new IllegalArgumentException("Parameter library must not be empty.");
        }

        this.library = library;
    }

    public int getSlotListIndex() {
        return this.slotListIndex;
    }

    public final void setSlotListIndex(int slotListIndex) {
        if (slotListIndex < 0) {
            throw new IllegalArgumentException("Parameter slotListIndex must be greater or equal to zero.");
        }

        this.useSlotListIndex = true;

        this.slotListIndex = slotListIndex;
        this.slotId = -1;
    }

    public int getSlotId() {
        return this.slotId;
    }

    public void setSlotId(int slotId) {
        if (slotId < 0) {
            throw new IllegalArgumentException("Parameter slotId must be greater or equal to zero.");
        }

        useSlotListIndex = false;

        this.slotId = slotId;
        this.slotListIndex = -1;
    }

    @Override
    public String toString() {
        StringBuilder sbConfiguration = new StringBuilder(150);
        sbConfiguration.append("name = \"").append(escapeBackslashesAndQuotationMarks(name)).append("\"\n");
        sbConfiguration.append("library = ").append(library).append('\n');

        if (useSlotListIndex) {
            sbConfiguration.append("slotListIndex = ").append(slotListIndex).append('\n');
        } else {
            sbConfiguration.append("slot = ").append(slotId).append('\n');
        }

        return sbConfiguration.toString();
    }

    private static String escapeBackslashesAndQuotationMarks(String value) {
        String[] searchValues = new String[] { "\\", "\"" };
        String[] replacementValues = new String[] { "\\\\", "\\\"" };

        return StringUtils.replaceEach(value, searchValues, replacementValues);
    }

}
