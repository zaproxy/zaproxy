/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ApiElement {

    private String name = null;
    private String descriptionTag = "";
    private List<ApiParameter> parameters = new ArrayList<>();

    /**
     * Flag that indicates whether or not the API element is deprecated.
     *
     * @see #deprecatedDescription
     */
    private boolean deprecated;

    /**
     * The description for the deprecation.
     *
     * @see #deprecated
     */
    private String deprecatedDescription;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApiElement(String name) {
        super();
        this.name = name;
    }

    public ApiElement(String name, List<String> mandatoryParamNames) {
        this(name, mandatoryParamNames, null);
    }

    public ApiElement(
            String name, List<String> mandatoryParamNames, List<String> optionalParamNames) {
        super();
        this.name = name;

        addParameters(mandatoryParamNames, true);
        addParameters(optionalParamNames, false);
    }

    public ApiElement(String name, String[] mandatoryParamNames) {
        this(name, mandatoryParamNames, null);
    }

    public ApiElement(String name, String[] mandatoryParamNames, String[] optionalParamNames) {
        this(name, asList(mandatoryParamNames), asList(optionalParamNames));
    }

    private static List<String> asList(String[] elements) {
        return elements != null ? Arrays.asList(elements) : null;
    }

    public void setMandatoryParamNames(String[] paramNames) {
        setMandatoryParamNames(asList(paramNames));
    }

    public void setMandatoryParamNames(List<String> paramNames) {
        parameters.removeIf(ApiParameter::isRequired);

        if (paramNames != null) {
            List<ApiParameter> optionalParameters = parameters;
            parameters = new ArrayList<>(optionalParameters.size() + paramNames.size());
            addParameters(paramNames, true);
            parameters.addAll(optionalParameters);
        }
    }

    private void addParameters(List<String> names, boolean required) {
        if (names == null) {
            return;
        }
        names.forEach(param -> parameters.add(new ApiParameter(param, "", required)));
    }

    public List<String> getMandatoryParamNames() {
        return getParametersNames(ApiParameter::isRequired);
    }

    private List<String> getParametersNames(Predicate<ApiParameter> predicate) {
        return parameters.stream()
                .filter(predicate)
                .map(ApiParameter::getName)
                .collect(Collectors.toList());
    }

    /**
     * Gets the description's resource key.
     *
     * @return the resource key, never {@code null} (since 2.9.0).
     */
    public String getDescriptionTag() {
        return descriptionTag;
    }

    public void setDescriptionTag(String descriptionTag) {
        this.descriptionTag = descriptionTag == null ? "" : descriptionTag;
    }

    public List<String> getOptionalParamNames() {
        return getParametersNames(e -> !e.isRequired());
    }

    public void setOptionalParamNames(String[] optionalParamNames) {
        setOptionalParamNames(asList(optionalParamNames));
    }

    public void setOptionalParamNames(List<String> optionalParamNames) {
        parameters.removeIf(e -> !e.isRequired());
        addParameters(optionalParamNames, false);
    }

    /**
     * Gets the parameters.
     *
     * @return an unmodifiable list with the parameters, never {@code null}.
     * @since 2.9.0
     */
    public List<ApiParameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    /**
     * Tells whether or not the API element is deprecated.
     *
     * @return {@code true} if the API element is deprecated, {@code false} otherwise.
     * @since 2.6.0
     * @see #getDeprecatedDescription()
     */
    public boolean isDeprecated() {
        return deprecated;
    }

    /**
     * Sets whether or not the API element is deprecated.
     *
     * <p>Deprecated elements are shown in the {@link WebUI} with a note that they are deprecated
     * and their use is discouraged. The API generators might also mark the elements as deprecated.
     *
     * @param deprecated {@code true} if the API element is deprecated, {@code false} otherwise.
     * @since 2.6.0
     * @see #setDeprecatedDescription(String)
     */
    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    /**
     * Gets the description of the deprecation.
     *
     * @return the description of the deprecation, might be {@code null}.
     * @since 2.6.0
     * @see #isDeprecated()
     */
    public String getDeprecatedDescription() {
        return deprecatedDescription;
    }

    /**
     * Sets the (concise) description of the deprecation.
     *
     * <p>The description should explain why it was deprecated and what are the alternative
     * endpoints that should be used, if any.
     *
     * <p><strong>Note:</strong> It should be in plain text (i.e. no HTML tags).
     *
     * @param description the description of the deprecation.
     * @since 2.6.0
     * @see #setDeprecated(boolean)
     */
    public void setDeprecatedDescription(String description) {
        this.deprecatedDescription = description;
    }
}
