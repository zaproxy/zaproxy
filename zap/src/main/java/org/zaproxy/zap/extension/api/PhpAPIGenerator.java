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
package org.zaproxy.zap.extension.api;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PhpAPIGenerator extends AbstractAPIGenerator {

    private static final String DEFAULT_OUTPUT_DIR = "../zap-api-php/src/Zap/";

    private final String HEADER =
            "<?php\n"
                    + "/**\n"
                    + " * Zed Attack Proxy (ZAP) and its related class files.\n"
                    + " *\n"
                    + " * ZAP is an HTTP/HTTPS proxy for assessing web application security.\n"
                    + " *\n"
                    + " * Copyright "
                    + Year.now()
                    + " the ZAP development team\n"
                    + " *\n"
                    + " * Licensed under the Apache License, Version 2.0 (the \"License\");\n"
                    + " * you may not use this file except in compliance with the License.\n"
                    + " * You may obtain a copy of the License at\n"
                    + " *\n"
                    + " *   http://www.apache.org/licenses/LICENSE-2.0\n"
                    + " *\n"
                    + " * Unless required by applicable law or agreed to in writing, software\n"
                    + " * distributed under the License is distributed on an \"AS IS\" BASIS,\n"
                    + " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
                    + " * See the License for the specific language governing permissions and\n"
                    + " * limitations under the License.\n"
                    + " */\n"
                    + "\n\n";

    /** Map any names which are reserved in java to something legal */
    private static final Map<String, String> nameMap;

    static {
        Map<String, String> initMap = new HashMap<>();
        initMap.put("Break", "Brk");
        initMap.put("break", "brk");
        initMap.put("continue", "cont");
        nameMap = Collections.unmodifiableMap(initMap);
    }

    public PhpAPIGenerator() {
        super(DEFAULT_OUTPUT_DIR);
    }

    public PhpAPIGenerator(String path, boolean optional) {
        super(path, optional);
    }

    public PhpAPIGenerator(String path, boolean optional, ResourceBundle resourceBundle) {
        super(path, optional, resourceBundle);
    }

    private void generatePhpElement(ApiElement element, String component, String type, Writer out)
            throws IOException {

        boolean hasParams = !element.getParameters().isEmpty();

        // Add description if defined
        String descTag = element.getDescriptionTag();

        try {
            String desc = getMessages().getString(descTag);
            out.write("\t/**\n");
            out.write("\t * " + desc + "\n");
            if (isOptional()) {
                out.write("\t * " + OPTIONAL_MESSAGE + "\n");
            }
            out.write("\t */\n");
        } catch (Exception e) {
            // Might not be set, so just print out the ones that are missing
            System.out.println("No i18n for: " + descTag);
            if (isOptional()) {
                out.write("\t/**\n");
                out.write("\t * " + OPTIONAL_MESSAGE + "\n");
                out.write("\t */\n");
            }
        }

        out.write("\tpublic function " + createMethodName(element.getName()) + "(");

        out.write(
                element.getParameters().stream()
                        .map(
                                parameter -> {
                                    String varName =
                                            "$" + parameter.getName().toLowerCase(Locale.ROOT);
                                    if (parameter.isRequired()) {
                                        return varName;
                                    }
                                    return varName + "=NULL";
                                })
                        .collect(Collectors.joining(", ")));

        if (hasParams) {
            out.write(", ");
        }
        // Always add the API key - we've no way of knowing if it will be required or not
        out.write("$" + API.API_KEY_PARAM + "='') {\n");

        StringBuilder reqParams = new StringBuilder();
        reqParams.append("array(");
        String params =
                element.getParameters().stream()
                        .filter(ApiParameter::isRequired)
                        .map(
                                parameter -> {
                                    String name = parameter.getName();
                                    return "'" + name + "' => $" + name.toLowerCase(Locale.ROOT);
                                })
                        .collect(Collectors.joining(", "));
        reqParams.append(params);
        boolean first = params.isEmpty();

        // Always add the API key - we've no way of knowing if it will be required or not
        if (!first) {
            reqParams.append(", ");
        }
        reqParams
                .append("'")
                .append(API.API_KEY_PARAM)
                .append("' => $")
                .append(API.API_KEY_PARAM)
                .append(")");

        List<ApiParameter> optionalParameters =
                element.getParameters().stream()
                        .filter(e -> !e.isRequired())
                        .collect(Collectors.toList());
        if (!optionalParameters.isEmpty()) {
            out.write("\t\t$params = ");
            out.write(reqParams.toString());
            out.write(";\n");
            reqParams.replace(0, reqParams.length(), "$params");

            for (ApiParameter parameter : optionalParameters) {
                String name = parameter.getName();
                String varName = name.toLowerCase(Locale.ROOT);
                out.write("\t\tif ($" + varName + " !== NULL) {\n");
                out.write("\t\t\t$params['" + name + "'] = $" + varName + ";\n");
                out.write("\t\t}\n");
            }
        }

        String method = "request";
        String baseUrl = "base";
        if (type.equals(OTHER_ENDPOINT)) {
            method += "Other";
            baseUrl += "_other";
        }

        out.write(
                "\t\treturn $this->zap->"
                        + method
                        + "($this->zap->"
                        + baseUrl
                        + " . '"
                        + component
                        + "/"
                        + type
                        + "/"
                        + element.getName()
                        + "/'");

        out.write(", ");
        out.write(reqParams.toString());
        out.write(")");
        if (type.equals(VIEW_ENDPOINT)) {
            if (element.getName().startsWith("option")) {
                out.write("->{'" + element.getName().substring(6) + "'};\n");
            } else {
                out.write("->{'" + element.getName() + "'};\n");
            }
        } else {
            out.write(";\n");
        }
        out.write("\t}\n\n");
    }

    private static String createMethodName(String name) {
        if (nameMap.containsKey(name)) {
            name = nameMap.get(name);
        }
        return removeAllFullStopCharacters(name);
    }

    private static String removeAllFullStopCharacters(String string) {
        return string.replaceAll("\\.", "");
    }

    @Override
    protected void generateAPIFiles(ApiImplementor imp) throws IOException {
        String className =
                safeName(
                        imp.getPrefix().substring(0, 1).toUpperCase()
                                + imp.getPrefix().substring(1));

        Path file = getDirectory().resolve(className + ".php");
        System.out.println("Generating " + file.toAbsolutePath());
        try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            out.write(HEADER);
            out.write("namespace Zap;\n\n");

            out.write("\n");
            out.write("/**\n");
            out.write(" * This file was automatically generated.\n");
            out.write(" * @property Zap $zap\n");
            out.write(" */\n");
            out.write("class " + className + " {\n\n");

            out.write("\tpublic function __construct ($zap) {\n");
            out.write("\t\t$this->zap = $zap;\n");
            out.write("\t}\n\n");

            for (ApiElement view : imp.getApiViews()) {
                this.generatePhpElement(view, imp.getPrefix(), VIEW_ENDPOINT, out);
            }
            for (ApiElement action : imp.getApiActions()) {
                this.generatePhpElement(action, imp.getPrefix(), ACTION_ENDPOINT, out);
            }
            for (ApiElement other : imp.getApiOthers()) {
                this.generatePhpElement(other, imp.getPrefix(), OTHER_ENDPOINT, out);
            }
            out.write("}\n");
        }
    }

    private static String safeName(String name) {
        if (nameMap.containsKey(name)) {
            return nameMap.get(name);
        }
        return name;
    }

    public static void main(String[] args) throws Exception {
        PhpAPIGenerator wapi = new PhpAPIGenerator();
        wapi.generateCoreAPIFiles();
    }
}
