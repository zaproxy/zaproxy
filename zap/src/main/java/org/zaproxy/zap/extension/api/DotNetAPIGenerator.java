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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class DotNetAPIGenerator extends AbstractAPIGenerator {

    /**
     * Default output directory is the "gen" package of subproject zap-clientapi (of zap-api-java
     * project).
     */
    private static final String DEFAULT_OUTPUT_DIR =
            "../zap-api-dotnet/src/OWASPZAPDotNetAPI/OWASPZAPDotNetAPI/Generated";

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
                    + "\n";

    /** Map any names which are reserved in CSharp (or Dot Net) to something legal */
    private static final Map<String, String> nameMap;

    static {
        Map<String, String> initMap = new HashMap<>();
        initMap.put("break", "brk");
        initMap.put("continue", "cont");
        initMap.put("string", "str");
        initMap.put("params", "parameters");
        initMap.put("bool", "boolean");
        nameMap = Collections.unmodifiableMap(initMap);
    }

    public DotNetAPIGenerator() {
        super(DEFAULT_OUTPUT_DIR);
    }

    public DotNetAPIGenerator(String path, boolean optional) {
        super(path, optional);
    }

    public DotNetAPIGenerator(String path, boolean optional, ResourceBundle resourceBundle) {
        super(path, optional, resourceBundle);
    }

    private void generateCSharpElement(
            ApiElement element, String component, String type, Writer out) throws IOException {
        boolean hasParams = false;

        // Add description if defined
        String descTag = element.getDescriptionTag();
        try {
            String desc = getMessages().getString(descTag);
            out.write("\t\t/// <summary>\n");
            out.write("\t\t///" + desc + "\n");
            if (isOptional()) {
                out.write("\t\t///" + OPTIONAL_MESSAGE + "\n");
            }
            if (element.isDeprecated()) {
                out.write("\t\t/// [Obsolete]");
                String deprecationDesc = element.getDeprecatedDescription();
                if (deprecationDesc != null && !deprecationDesc.isEmpty()) {
                    out.write(" " + deprecationDesc);
                }
                out.write("\n");
            }
            out.write("\t\t/// </summary>\n");
            out.write("\t\t/// <returns></returns>\n");

        } catch (Exception e) {
            // Might not be set, so just print out the ones that are missing
            System.out.println("No i18n for: " + descTag);
            if (isOptional()) {
                out.write("\t\t/// <summary>\n");
                out.write("\t\t///" + OPTIONAL_MESSAGE + "\n");
                out.write("\t\t/// </summary>\n");
                out.write("\t\t/// <returns></returns>\n");
            }
        }

        if (element.isDeprecated()) {
            String deprecationDesc = element.getDeprecatedDescription();
            if (deprecationDesc != null && !deprecationDesc.isEmpty()) {
                out.write("\t\t[Obsolete(\"");
                out.write(deprecationDesc);
                out.write("\")]\n");
            } else {
                out.write("\t\t[Obsolete]\n");
            }
        }

        if (type.equals(OTHER_ENDPOINT)) {
            out.write("\t\tpublic byte[] " + createMethodName(element.getName()) + "(");
        } else {
            out.write("\t\tpublic IApiResponse " + createMethodName(element.getName()) + "(");
        }

        for (ApiParameter parameter : element.getParameters()) {
            if (!hasParams) {
                hasParams = true;
            } else {
                out.write(", ");
            }
            String name = parameter.getName();
            if (name.equalsIgnoreCase("boolean")) {
                out.write("bool boolean");
            } else if (name.equalsIgnoreCase("integer")) {
                out.write("int i");
            } else {
                out.write("string ");
                out.write(createParameterName(name));
            }
        }
        out.write(")\n\t\t{\n");

        out.write("\t\t\tDictionary<string, string> parameters = null;\n");

        if (hasParams) {
            out.write("\t\t\tparameters = new Dictionary<string, string>();\n");

            for (ApiParameter parameter : element.getParameters()) {
                String name = parameter.getName();
                out.write("\t\t\tparameters.Add(\"" + name + "\", ");
                if (name.equalsIgnoreCase("boolean")) {
                    out.write("Convert.ToString(boolean)");
                } else if (name.equalsIgnoreCase("integer")) {
                    out.write("Convert.ToString(i)");
                } else {
                    out.write(createParameterName(name));
                }
                out.write(");\n");
            }
        }

        if (type.equals(OTHER_ENDPOINT)) {
            out.write(
                    "\t\t\treturn api.CallApiOther(\""
                            + component
                            + "\", \""
                            + type
                            + "\", \""
                            + element.getName()
                            + "\", parameters);\n");
        } else {
            out.write(
                    "\t\t\treturn api.CallApi(\""
                            + component
                            + "\", \""
                            + type
                            + "\", \""
                            + element.getName()
                            + "\", parameters);\n");
        }

        out.write("\t\t}\n\n");
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

    private static String createParameterName(String paramName) {
        String name = paramName.toLowerCase(Locale.ROOT);
        if (nameMap.containsKey(name)) {
            name = nameMap.get(name);
        }
        return removeAllFullStopCharacters(name);
    }

    @Override
    protected void generateAPIFiles(ApiImplementor imp) throws IOException {
        String className =
                imp.getPrefix().substring(0, 1).toUpperCase() + imp.getPrefix().substring(1);

        Path file = getDirectory().resolve(className + ".cs");
        System.out.println("Generating " + file.toAbsolutePath());
        try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            out.write(HEADER);
            out.write("\n\n");

            out.write("using System;\n");
            out.write("using System.Collections.Generic;\n");
            out.write("using System.Text;\n");
            out.write("\n");

            out.write("\n");
            out.write("/*\n");
            out.write(" * This file was automatically generated.\n");
            out.write(" */\n");
            out.write("namespace OWASPZAPDotNetAPI.Generated\n");
            out.write("{\n");
            out.write("\tpublic class " + className + " \n\t{");

            out.write("\n\t\tprivate ClientApi api = null;\n\n");
            out.write("\t\tpublic " + className + "(ClientApi api) \n\t\t{\n");
            out.write("\t\t\tthis.api = api;\n");
            out.write("\t\t}\n\n");

            for (ApiElement view : imp.getApiViews()) {
                this.generateCSharpElement(view, imp.getPrefix(), VIEW_ENDPOINT, out);
            }
            for (ApiElement action : imp.getApiActions()) {
                this.generateCSharpElement(action, imp.getPrefix(), ACTION_ENDPOINT, out);
            }
            for (ApiElement other : imp.getApiOthers()) {
                this.generateCSharpElement(other, imp.getPrefix(), OTHER_ENDPOINT, out);
            }
            out.write("\t}\n");
            out.write("}\n");
        }
    }

    public static void main(String[] args) throws Exception {
        // Command for generating a DotNet version of the ZAP API
        DotNetAPIGenerator dnapi = new DotNetAPIGenerator(DEFAULT_OUTPUT_DIR, false);
        dnapi.generateCoreAPIFiles();
    }
}
