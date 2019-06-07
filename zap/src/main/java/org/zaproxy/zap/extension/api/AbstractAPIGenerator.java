/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
package org.zaproxy.zap.extension.api;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.I18N;

/**
 * The base class for ZAP API client generators.
 *
 * @since 2.6.0
 */
public abstract class AbstractAPIGenerator {

    protected static final String OPTIONAL_MESSAGE =
            "This component is optional and therefore the API will only work if it is installed";

    protected static final String VIEW_ENDPOINT = "view";
    protected static final String ACTION_ENDPOINT = "action";
    protected static final String OTHER_ENDPOINT = "other";

    private final Path directory;
    private final boolean optional;

    private final ResourceBundle messages;

    /**
     * Constructs an {@code AbstractAPIGenerator} with the given directory.
     *
     * @param directory the directory where the API client files should be generated
     */
    protected AbstractAPIGenerator(String directory) {
        this(directory, false);
    }

    /**
     * Constructs an {@code AbstractAPIGenerator} with the given directory and optional state.
     *
     * @param directory the directory where the API client files should be generated
     * @param optional {@code true} if the API client files are optional, {@code false} otherwise
     */
    protected AbstractAPIGenerator(String directory, boolean optional) {
        this(directory, optional, null);
    }

    /**
     * Constructs an {@code AbstractAPIGenerator} with the given directory, optional state, and
     * {@code ResourceBundle}.
     *
     * @param directory the directory where the API client files should be generated
     * @param optional {@code true} if the API client files are optional, {@code false} otherwise
     * @param resourceBundle the {@code ResourceBundle} used for doc of the generated classes.
     * @since 2.8.0
     */
    protected AbstractAPIGenerator(
            String directory, boolean optional, ResourceBundle resourceBundle) {
        this.directory = Paths.get(directory);
        this.optional = optional;

        Constant.messages = new I18N(Locale.ENGLISH);
        messages =
                resourceBundle != null ? resourceBundle : Constant.messages.getCoreResourceBundle();
    }

    /**
     * Gets the directory where the API client files should be generated.
     *
     * @return the directory where the API client files should be generated
     */
    protected Path getDirectory() {
        return directory;
    }

    /**
     * If the API client files being generated are optional, that is, belong to an add-on.
     *
     * @return {@code true} if the API client files are optional, {@code false} otherwise
     */
    protected boolean isOptional() {
        return optional;
    }

    /**
     * Gets the messages for doc of the generated classes.
     *
     * @return the {@code ResourceBundle} with the messages for doc
     */
    protected ResourceBundle getMessages() {
        return messages;
    }

    /**
     * Generates the API client files for the core API implementors.
     *
     * @throws IOException if an error occurred while writing the API files
     */
    public void generateCoreAPIFiles() throws IOException {
        generateAPIFiles(ApiGeneratorUtils.getAllImplementors());
    }

    /**
     * Generates the API client files of the given API implementors.
     *
     * @param implementors the API implementors, must not be {@code null}
     * @throws IOException if an error occurred while writing the API files
     */
    public void generateAPIFiles(List<ApiImplementor> implementors) throws IOException {
        for (ApiImplementor implementor : implementors) {
            this.generateAPIFiles(implementor);
        }
    }

    /**
     * Generates the API client files of the given API implementor.
     *
     * @param implementor the API implementor, must not be {@code null}
     * @throws IOException if an error occurred while writing the API files
     */
    protected abstract void generateAPIFiles(ApiImplementor implementor) throws IOException;
}
