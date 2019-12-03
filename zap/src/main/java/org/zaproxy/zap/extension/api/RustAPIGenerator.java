/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
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
import java.nio.file.Paths;
import java.time.Year;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class RustAPIGenerator extends AbstractAPIGenerator {

    private static final String DEFAULT_OUTPUT_DIR = "../zap-api-rust/src";

    private static final String HEADER =
            "/* Zed Attack Proxy (ZAP) and its related class files.\n"
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
        initMap.put("break", "brk");
        initMap.put("continue", "cont");
        initMap.put("match", "mtch");
        initMap.put("type", "typ");
        nameMap = Collections.unmodifiableMap(initMap);
    }

    public RustAPIGenerator() {
        super(DEFAULT_OUTPUT_DIR);
    }

    public RustAPIGenerator(String path, boolean optional) {
        super(path, optional);
    }

    public RustAPIGenerator(String path, boolean optional, ResourceBundle resourceBundle) {
        super(path, optional, resourceBundle);
    }

    private void generateRustElement(ApiElement element, String component, String type, Writer out)
            throws IOException {
        // Add description if defined
        String descTag = element.getDescriptionTag();
        try {
            String desc = getMessages().getString(descTag);
            out.write("/**\n");
            out.write(" * " + desc + "\n");
            if (isOptional()) {
                out.write(" * <p>\n");
                out.write(" * " + OPTIONAL_MESSAGE + "\n");
            }
            out.write("*/\n");
            if (element.isDeprecated()) {
                out.write("#[deprecated");
                String deprecationDesc = element.getDeprecatedDescription();
                if (deprecationDesc != null && !deprecationDesc.isEmpty()) {
                    out.write("(note=\"" + deprecationDesc + "\")");
                }
                out.write("]\n");
            }
        } catch (Exception e) {
            // Might not be set, so just print out the ones that are missing
            System.out.println("No i18n for: " + descTag);
            if (isOptional()) {
                out.write("/**\n");
                out.write(" * " + OPTIONAL_MESSAGE + "\n");
                out.write(" */\n");
            }
        }
        int paramCount = element.getParameters().size();
        if (paramCount > 6) {
            // Clippy defaults to 6, but we also have the service parameter
            out.write("#[allow(clippy::too_many_arguments)]\n");
        }

        out.write("pub fn " + getSafeName(element.getName()) + "(service: &ZapService");

        for (ApiParameter parameter : element.getParameters()) {
            out.write(", ");
            out.write(getSafeName(parameter.getName().toLowerCase(Locale.ROOT)));
            out.write(": String");
        }
        out.write(") -> Result<Value, ZapApiError> {\n");

        if (paramCount > 0) {
            out.write("    let mut params = HashMap::new();\n");
        } else {
            out.write("    let params = HashMap::new();\n");
        }

        for (ApiParameter parameter : element.getParameters()) {
            String name = parameter.getName();
            String varName = getSafeName(name.toLowerCase(Locale.ROOT));
            out.write("    params.insert(\"" + name + "\".to_string(), " + varName + ");\n");
        }

        out.write(
                "    super::call(service, \""
                        + component
                        + "\", \""
                        + type
                        + "\", \""
                        + element.getName()
                        + "\", params)\n");
        out.write("}\n\n");
    }

    private static String getSafeName(String name) {
        if (nameMap.containsKey(name)) {
            name = nameMap.get(name);
        }
        return removeAllFullStopCharacters(camelCaseToLcUnderscores(name));
    }

    private static String removeAllFullStopCharacters(String string) {
        return string.replaceAll("\\.", "");
    }

    public static String camelCaseToLcUnderscores(String s) {
        // Ripped off / inspired by
        // http://stackoverflow.com/questions/2559759/how-do-i-convert-camelcase-into-human-readable-names-in-java
        return s.replaceAll(
                        String.format(
                                "%s|%s|%s",
                                "(?<=[A-Z])(?=[A-Z][a-z])",
                                "(?<=[^A-Z])(?=[A-Z])",
                                "(?<=[A-Za-z])(?=[^A-Za-z])"),
                        "_")
                .replaceAll("__", "_")
                .toLowerCase();
    }

    @Override
    protected void generateAPIFiles(ApiImplementor imp) throws IOException {
        Path file = getDirectory().resolve(getSafeName(imp.getPrefix()) + ".rs");
        // System.out.println("Generating " + file.toAbsolutePath());
        System.out.println("pub mod " + getSafeName(imp.getPrefix()) + ";");

        try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            out.write(HEADER);

            out.write("use super::ZapApiError;\n");
            out.write("use super::ZapService;\n");
            out.write("use serde_json::Value;\n");
            out.write("use std::collections::HashMap;\n");
            out.write("\n");

            out.write("\n");
            out.write("/**\n");
            out.write(" * This file was automatically generated.\n");
            out.write(" */\n");

            for (ApiElement view : imp.getApiViews()) {
                this.generateRustElement(view, imp.getPrefix(), VIEW_ENDPOINT, out);
            }
            for (ApiElement action : imp.getApiActions()) {
                this.generateRustElement(action, imp.getPrefix(), ACTION_ENDPOINT, out);
            }
            for (ApiElement other : imp.getApiOthers()) {
                this.generateRustElement(other, imp.getPrefix(), OTHER_ENDPOINT, out);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        // Command for generating a rust version of the ZAP API

        if (!Files.exists(Paths.get(DEFAULT_OUTPUT_DIR))) {
            System.err.println(
                    "The directory does not exist: "
                            + Paths.get(DEFAULT_OUTPUT_DIR).toAbsolutePath());
            System.exit(1);
        }

        RustAPIGenerator wapi = new RustAPIGenerator();
        wapi.generateCoreAPIFiles();
    }
}
