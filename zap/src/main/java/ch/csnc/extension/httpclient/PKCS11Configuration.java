/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package ch.csnc.extension.httpclient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang.StringUtils;

/**
 * A representation of PKCS#11 provider configuration. Used to create configurations for instances
 * of {@code sun.security.pkcs11.SunPKCS11} and {@code
 * com.ibm.crypto.pkcs11impl.provider.IBMPKCS11Impl}.
 *
 * <p>Example usage:
 *
 * <blockquote>
 *
 * <pre>
 * PKCS11Configuration configuration = PKCS11Configuration.builder()
 *         .setName(&quot;Provider X&quot;)
 *         .setLibrary(&quot;/path/to/pkcs11library&quot;)
 *         .setSlotId(1)
 *         .build();
 *
 * java.security.Provider p = new sun.security.pkcs11.SunPKCS11(new ByteArrayInputStream(configuration.toString().getBytes()));
 * java.security.Security.addProvider(p);
 * </pre>
 *
 * </blockquote>
 *
 * <p><strong>Note:</strong> Only the mandatory attributes, <i>name</i> and <i>library</i>, and the
 * optional attributes, <i>description</i>, <i>slot</i> and <i>slotListIndex</i> are implemented.
 *
 * @see <a
 *     href="http://docs.oracle.com/javase/7/docs/technotes/guides/security/p11guide.html#Config">Sun
 *     PKCS#11 Configuration</a>
 * @see <a
 *     href="http://pic.dhe.ibm.com/infocenter/java7sdk/v7r0/index.jsp?topic=%2Fcom.ibm.java.security.component.71.doc%2Fsecurity-component%2Fpkcs11implDocs%2Fconfigfile.html">IBM
 *     PKCS#11 Configuration</a>
 * @deprecated (2.12.0) No longer in use.
 */
@Deprecated
public class PKCS11Configuration {

    private final String name;

    private final String library;

    private final String description;

    private final int slotId;

    private final int slotListIndex;

    private PKCS11Configuration(
            String name, String library, String description, int slotId, int slotListIndex) {
        super();

        this.name = name;
        this.library = library;
        this.description = description;
        this.slotId = slotId;
        this.slotListIndex = slotListIndex;
    }

    public String getName() {
        return name;
    }

    public String getLibrary() {
        return library;
    }

    public String getDescription() {
        return description;
    }

    public int getSlotListIndex() {
        return slotListIndex;
    }

    public int getSlotId() {
        return slotId;
    }

    @Override
    public String toString() {
        StringBuilder sbConfiguration = new StringBuilder(150);
        sbConfiguration
                .append("name = \"")
                .append(escapeBackslashesAndQuotationMarks(name))
                .append("\"\n");
        sbConfiguration.append("library = ").append(library).append('\n');

        if (description != null && !description.isEmpty()) {
            sbConfiguration.append("description = ").append(description).append('\n');
        }

        if (slotListIndex != -1) {
            sbConfiguration.append("slotListIndex = ").append(slotListIndex);
        } else {
            sbConfiguration.append("slot = ").append(slotId);
        }
        sbConfiguration.append('\n');

        return sbConfiguration.toString();
    }

    private static String escapeBackslashesAndQuotationMarks(String value) {
        String[] searchValues = new String[] {"\\", "\""};
        String[] replacementValues = new String[] {"\\\\", "\\\""};

        return StringUtils.replaceEach(value, searchValues, replacementValues);
    }

    public InputStream toInpuStream() {
        return new ByteArrayInputStream(toString().getBytes(StandardCharsets.UTF_8));
    }

    public static PCKS11ConfigurationBuilder builder() {
        return new PCKS11ConfigurationBuilder();
    }

    public static final class PCKS11ConfigurationBuilder {

        private String name;

        private String library;

        private String description;

        private int slotId;

        private int slotListIndex;

        private PCKS11ConfigurationBuilder() {
            slotId = -1;
            slotListIndex = 0;
        }

        public PCKS11ConfigurationBuilder setName(String name) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Parameter name must not be null or empty.");
            }
            this.name = name;
            return this;
        }

        public PCKS11ConfigurationBuilder setLibrary(String library) {
            if (library == null || library.isEmpty()) {
                throw new IllegalArgumentException("Parameter library must not be null or empty.");
            }
            this.library = library;
            return this;
        }

        public PCKS11ConfigurationBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public PCKS11ConfigurationBuilder setSlotListIndex(int slotListIndex) {
            if (slotListIndex < 0) {
                throw new IllegalArgumentException(
                        "Parameter slotListIndex must be greater or equal to zero.");
            }
            this.slotListIndex = slotListIndex;
            this.slotId = -1;
            return this;
        }

        public final PCKS11ConfigurationBuilder setSlotId(int slotId) {
            if (slotId < 0) {
                throw new IllegalArgumentException(
                        "Parameter slotId must be greater or equal to zero.");
            }
            this.slotId = slotId;
            this.slotListIndex = -1;
            return this;
        }

        public PKCS11Configuration build() {
            validateBuilderState();
            return new PKCS11Configuration(name, library, description, slotId, slotListIndex);
        }

        private void validateBuilderState() {
            if (name == null) {
                throw new IllegalStateException("A name must be set.");
            }
            if (library == null) {
                throw new IllegalStateException("A library must be set.");
            }
        }
    }
}
