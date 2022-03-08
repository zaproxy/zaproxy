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

public class JavaAPIGenerator extends AbstractAPIGenerator {

    /** The path of the package where the generated classes are deployed. */
    private static final String TARGET_PACKAGE = "org/zaproxy/clientapi/gen";

    /**
     * Default output directory is the "gen" package of subproject zap-clientapi (of zap-api-java
     * project).
     */
    private static final String DEFAULT_OUTPUT_DIR =
            "../zap-api-java/subprojects/zap-clientapi/src/main/java/" + TARGET_PACKAGE;

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
        nameMap = Collections.unmodifiableMap(initMap);
    }

    public JavaAPIGenerator() {
        super(DEFAULT_OUTPUT_DIR);
    }

    public JavaAPIGenerator(String path, boolean optional) {
        super(path, optional);
    }

    public JavaAPIGenerator(String path, boolean optional, ResourceBundle resourceBundle) {
        super(path, optional, resourceBundle);
    }

    private void generateJavaElement(ApiElement element, String component, String type, Writer out)
            throws IOException {

        // Add description if defined
        String descTag = element.getDescriptionTag();
        try {
            String desc = getMessages().getString(descTag);
            out.write("\t/**\n");
            out.write("\t * " + desc + "\n");
            if (isOptional()) {
                out.write("\t * <p>\n");
                out.write("\t * " + OPTIONAL_MESSAGE + "\n");
            }

            if (element.isDeprecated()) {
                out.write("\t * @deprecated");
                String deprecationDesc = element.getDeprecatedDescription();
                if (deprecationDesc != null && !deprecationDesc.isEmpty()) {
                    out.write(" " + deprecationDesc);
                }
                out.write("\n");
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

        if (element.isDeprecated()) {
            out.write("\t@Deprecated\n");
        }

        if (type.equals(OTHER_ENDPOINT)) {
            out.write("\tpublic byte[] " + createMethodName(element.getName()) + "(");
        } else {
            out.write("\tpublic ApiResponse " + createMethodName(element.getName()) + "(");
        }

        boolean hasParams = false;
        for (ApiParameter parameter : element.getParameters()) {
            if (!hasParams) {
                hasParams = true;
            } else {
                out.write(", ");
            }
            String name = parameter.getName();
            if (name.equalsIgnoreCase("boolean")) {
                out.write("boolean bool");
            } else if (name.equalsIgnoreCase("integer")) {
                out.write("int i");
            } else {
                out.write("String " + name.toLowerCase(Locale.ROOT));
            }
        }
        out.write(") throws ClientApiException {\n");

        if (hasParams) {
            out.write("\t\tMap<String, String> map = new HashMap<>();\n");
            for (ApiParameter parameter : element.getParameters()) {
                String name = parameter.getName();
                if (!parameter.isRequired()) {
                    out.write("\t\tif (");
                    out.write(name.toLowerCase(Locale.ROOT));
                    out.write(" != null) {\n\t");
                }
                out.write("\t\tmap.put(\"" + name + "\", ");
                if (name.equalsIgnoreCase("boolean")) {
                    out.write("Boolean.toString(bool)");
                } else if (name.equalsIgnoreCase("integer")) {
                    out.write("Integer.toString(i)");
                } else {
                    out.write(name.toLowerCase(Locale.ROOT));
                }
                out.write(");\n");
                if (!parameter.isRequired()) {
                    out.write("\t\t}\n");
                }
            }
        }

        out.write("\t\treturn api.callApi");
        if (type.equals(OTHER_ENDPOINT)) {
            out.write("Other");
        }
        out.write("(\"" + component + "\", \"" + type + "\", \"" + element.getName() + "\"");

        if (hasParams) {
            out.write(", map);\n");
        } else {
            out.write(", null);\n");
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
                imp.getPrefix().substring(0, 1).toUpperCase() + imp.getPrefix().substring(1);

        Path file = getDirectory().resolve(className + ".java");
        System.out.println("Generating " + file.toAbsolutePath());
        try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            out.write(HEADER);
            out.write("package org.zaproxy.clientapi.gen;\n\n");

            out.write("import java.util.HashMap;\n");
            out.write("import java.util.Map;\n");
            out.write("import org.zaproxy.clientapi.core.ApiResponse;\n");
            out.write("import org.zaproxy.clientapi.core.ClientApi;\n");
            out.write("import org.zaproxy.clientapi.core.ClientApiException;\n");
            out.write("\n");

            out.write("\n");
            out.write("/**\n");
            out.write(" * This file was automatically generated.\n");
            out.write(" */\n");
            out.write("@SuppressWarnings(\"javadoc\")\n");
            out.write("public class " + className);
            boolean extendsClass = false;
            if (Files.exists(
                    file.resolveSibling(Paths.get("deprecated", className + "Deprecated.java")))) {
                out.write(
                        " extends org.zaproxy.clientapi.gen.deprecated."
                                + className
                                + "Deprecated");
                extendsClass = true;
            }
            out.write(" {\n\n");

            out.write("\tprivate final ClientApi api;\n\n");
            out.write("\tpublic " + className + "(ClientApi api) {\n");
            if (extendsClass) {
                out.write("\t\tsuper(api);\n");
            }
            out.write("\t\tthis.api = api;\n");
            out.write("\t}\n\n");

            for (ApiElement view : imp.getApiViews()) {
                this.generateJavaElement(view, imp.getPrefix(), VIEW_ENDPOINT, out);
            }
            for (ApiElement action : imp.getApiActions()) {
                this.generateJavaElement(action, imp.getPrefix(), ACTION_ENDPOINT, out);
            }
            for (ApiElement other : imp.getApiOthers()) {
                this.generateJavaElement(other, imp.getPrefix(), OTHER_ENDPOINT, out);
            }
            out.write("}\n");
        }
    }

    public static void main(String[] args) throws Exception {
        // Command for generating a java version of the ZAP API

        if (!Files.exists(Paths.get(DEFAULT_OUTPUT_DIR))) {
            System.err.println(
                    "The directory does not exist: "
                            + Paths.get(DEFAULT_OUTPUT_DIR).toAbsolutePath());
            System.exit(1);
        }

        JavaAPIGenerator wapi = new JavaAPIGenerator();
        wapi.generateCoreAPIFiles();
    }
}
