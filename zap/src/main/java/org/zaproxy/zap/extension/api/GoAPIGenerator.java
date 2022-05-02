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
package org.zaproxy.zap.extension.api;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class GoAPIGenerator extends AbstractAPIGenerator {

    private boolean addImports = false;

    private final String HEADER =
            "// Zed Attack Proxy (ZAP) and its related class files.\n"
                    + "//\n"
                    + "// ZAP is an HTTP/HTTPS proxy for assessing web application security.\n"
                    + "//\n"
                    + "// Copyright 2017 the ZAP development team\n"
                    + "//\n"
                    + "// Licensed under the Apache License, Version 2.0 (the \"License\");\n"
                    + "// you may not use this file except in compliance with the License.\n"
                    + "// You may obtain a copy of the License at\n"
                    + "//\n"
                    + "//   http://www.apache.org/licenses/LICENSE-2.0\n"
                    + "//\n"
                    + "// Unless required by applicable law or agreed to in writing, software\n"
                    + "// distributed under the License is distributed on an \"AS IS\" BASIS,\n"
                    + "// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
                    + "// See the License for the specific language governing permissions and\n"
                    + "// limitations under the License.\n";

    /** Map any names which are reserved in Go to something legal */
    private static final Map<String, String> nameMap;

    static {
        Map<String, String> initMap = new HashMap<>();
        initMap.put("break", "brk");
        initMap.put("continue", "cont");
        nameMap = Collections.unmodifiableMap(initMap);
    }

    public GoAPIGenerator() {
        super("../zap-api-go/zap/");
    }

    public GoAPIGenerator(String path, boolean optional) {
        super(path, optional);
    }

    public GoAPIGenerator(String path, boolean optional, ResourceBundle resourceBundle) {
        super(path, optional, resourceBundle);
    }

    @Override
    protected void generateAPIFiles(ApiImplementor imp) throws IOException {
        String className =
                imp.getPrefix().substring(0, 1).toUpperCase() + imp.getPrefix().substring(1);
        String pkgName = safeName(camelCaseToLowerCaseDash(className));

        Path file = getDirectory().resolve(pkgName + "_generated.go");
        createDirAndFile(file);
        System.out.println("Generating " + file.toAbsolutePath());
        try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            out.write(HEADER);
            out.write("//\n");
            out.write("// *** This file was automatically generated. ***\n");
            out.write("//\n\n");
            out.write("package zap\n\n");
            out.write("_imports_");
            out.write("type " + className + " struct {\n\tc *Client\n}" + "\n\n");

            for (ApiElement view : imp.getApiViews()) {
                this.generateGoElement(view, className, imp.getPrefix(), VIEW_ENDPOINT, out);
            }
            for (ApiElement action : imp.getApiActions()) {
                this.generateGoElement(action, className, imp.getPrefix(), ACTION_ENDPOINT, out);
            }
            for (ApiElement other : imp.getApiOthers()) {
                this.generateGoElement(other, className, imp.getPrefix(), OTHER_ENDPOINT, out);
            }
        }
        addImports(file, addImports);
        addImports = false;
    }

    private void generateGoElement(
            ApiElement element, String className, String component, String type, Writer out)
            throws IOException {

        boolean typeOther = type.equals(OTHER_ENDPOINT);
        boolean hasParams = !element.getParameters().isEmpty();

        // Add description if defined
        String descTag = element.getDescriptionTag();
        try {
            String desc = getMessages().getString(descTag);
            out.write("// " + desc + "\n");
            if (isOptional()) {
                out.write("//\n");
                out.write("// " + OPTIONAL_MESSAGE + "\n");
            }
        } catch (Exception e) {
            // Might not be set, so just print out the ones that are missing
            System.out.println("No i18n for: " + descTag);
            if (isOptional()) {
                out.write("// " + OPTIONAL_MESSAGE + "\n");
            }
        }

        // Function declaration
        out.write("func (" + className.substring(0, 1).toLowerCase() + " " + className + ") ");
        out.write(toTitleCase(createMethodName(element.getName())));
        out.write("(");

        // Iterate through and write out the function arguments
        writeOutArgs(element.getParameters(), out);

        out.write(")");

        // Function return types
        if (typeOther) {
            out.write(" ([]byte, error) ");
        } else {
            out.write(" (map[string]interface{}, error) ");
        }
        out.write("{\n");

        // Function content
        if (hasParams) {
            out.write("\tm := map[string]string{");

            // Iterate through and write out the request parameters
            writeOutRequestParams(element.getParameters(), out);

            out.write("\n\t}\n");
        }

        String method = className.substring(0, 1).toLowerCase() + ".c.Request";
        if (typeOther) {
            method += "Other";
        }
        out.write(
                "\treturn "
                        + method
                        + "(\""
                        + component
                        + "/"
                        + type
                        + "/"
                        + element.getName()
                        + "/\"");

        if (hasParams) {
            out.write(", m");
        } else {
            out.write(", nil");
        }
        out.write(")\n");
        out.write("}\n\n");
    }

    // create dir/files if not exist
    private void createDirAndFile(Path path) throws IOException {
        // create dir and file first if dir/file not exist
        File file = new File(path.toAbsolutePath().toString());
        File dir = file.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    // Writes the function arguments
    private void writeOutArgs(List<ApiParameter> parameters, Writer out) throws IOException {
        ArrayList<String> args = new ArrayList<>();
        for (ApiParameter parameter : parameters) {
            String name = parameter.getName();
            if (name.equalsIgnoreCase("boolean")) {
                args.add("boolean bool");
            } else if (name.equalsIgnoreCase("integer")) {
                args.add("i int");
            } else if (name.equalsIgnoreCase("string")) {
                args.add("str string");
            } else if (name.equalsIgnoreCase("type")) {
                args.add("t string");
            } else {
                args.add(name.toLowerCase(Locale.ROOT) + " string");
            }
        }
        out.write(String.join(", ", args));
    }

    // Writes the request parameters
    private void writeOutRequestParams(List<ApiParameter> parameters, Writer out)
            throws IOException {
        for (ApiParameter parameter : parameters) {
            String name = parameter.getName();
            out.write("\n\t\t\"" + name + "\": ");
            if (name.equalsIgnoreCase("boolean")) {
                addImports = true;
                out.write("strconv.FormatBool(boolean)");
            } else if (name.equalsIgnoreCase("integer")) {
                addImports = true;
                out.write("strconv.Itoa(i)");
            } else if (name.equalsIgnoreCase("string")) {
                out.write("str");
            } else if (name.equalsIgnoreCase("type")) {
                out.write("t");
            } else {
                out.write(name.toLowerCase(Locale.ROOT));
            }
            out.write(",");
        }
    }

    private static String safeName(String name) {
        if (nameMap.containsKey(name)) {
            return nameMap.get(name);
        }
        return name;
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

    public static String camelCaseToLowerCaseDash(String s) {
        // Ripped off / inspired by
        // http://stackoverflow.com/questions/2559759/how-do-i-convert-camelcase-into-human-readable-names-in-java
        return safeName(s)
                .replaceAll(
                        String.format(
                                "%s|%s|%s",
                                "(?<=[A-Z])(?=[A-Z][a-z])",
                                "(?<=[^A-Z])(?=[A-Z])",
                                "(?<=[A-Za-z])(?=[^A-Za-z])"),
                        "-")
                .toLowerCase();
    }

    private static String toTitleCase(String input) {
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;
        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }
            titleCase.append(c);
        }
        return titleCase.toString();
    }

    // It replaces the placeholder _imports_ with the proper import packages,
    // or removes it in case there isn't any packages to import
    private static void addImports(Path file, boolean addImports) throws IOException {
        Charset charset = StandardCharsets.UTF_8;
        String content = new String(Files.readAllBytes(file), charset);
        if (addImports) {
            content = content.replaceAll("_imports_", "import \"strconv\"\n\n");
        } else {
            content = content.replaceAll("_imports_", "");
        }
        Files.write(file, content.getBytes(charset));
    }

    public static void main(String[] args) throws Exception {
        // Command for generating a java version of the ZAP API
        GoAPIGenerator gapi = new GoAPIGenerator();
        gapi.generateCoreAPIFiles();
    }
}
