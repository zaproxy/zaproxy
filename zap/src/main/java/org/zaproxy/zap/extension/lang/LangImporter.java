/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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
package org.zaproxy.zap.extension.lang;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.LocaleUtils;

public final class LangImporter {

    private static final Logger logger = LogManager.getLogger(LangImporter.class);

    private static final String MSG_SUCCESS = "options.lang.importer.dialog.message.success";
    private static final String MSG_ERROR = "options.lang.importer.dialog.message.error";
    private static final String MSG_FILE_NOT_FOUND =
            "options.lang.importer.dialog.message.filenotfound";

    private LangImporter() {}

    public static String importLanguagePack(String languagePack) {
        Pattern includedFilesPattern = createIncludedFilesPattern();

        int langFileCount = 0;
        String message = "";

        File F = new File(languagePack);
        try (ZipFile zipFile = new ZipFile(F.getAbsolutePath())) {
            Enumeration<? extends ZipEntry> enumeration = zipFile.entries();

            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = enumeration.nextElement();

                if (!zipEntry.isDirectory()) {
                    try (BufferedInputStream bis =
                            new BufferedInputStream(zipFile.getInputStream(zipEntry))) {

                        int size;
                        byte[] buffer = new byte[2048];
                        String name = zipEntry.getName();

                        if (includedFilesPattern.matcher(name).find()) {
                            langFileCount++;

                            Path outputFile = Paths.get(Constant.getZapInstall(), name);
                            try (BufferedOutputStream bos =
                                    new BufferedOutputStream(
                                            Files.newOutputStream(outputFile), buffer.length)) {

                                while ((size = bis.read(buffer, 0, buffer.length)) != -1) {
                                    bos.write(buffer, 0, size);
                                }

                                bos.flush();
                            }
                        }
                    }
                }
            }

            message = (langFileCount > 0) ? MSG_SUCCESS : MSG_ERROR;

        } catch (IOException e) {
            message = MSG_FILE_NOT_FOUND;
            logger.error(e.getMessage(), e);
        }

        if (View.isInitialised()) {
            JOptionPane.showMessageDialog(
                    null,
                    Constant.messages.getString(message, langFileCount),
                    Constant.messages.getString("options.lang.importer.dialog.title"),
                    (langFileCount > 0)
                            ? JOptionPane.INFORMATION_MESSAGE
                            : JOptionPane.ERROR_MESSAGE);
        }

        return message;
    }

    /**
     * Creates a {@code Pattern} to match filenames of, source and translated, resource files
     * Messages.properties and vulnerabilities.xml.
     *
     * @return the {@code Pattern} to match the resource files
     * @since 2.4.0
     */
    // Relaxed visibility to allow unit test
    static Pattern createIncludedFilesPattern() {
        String messagesFilesRegex =
                LocaleUtils.createResourceFilesRegex(
                        Constant.MESSAGES_PREFIX, Constant.MESSAGES_EXTENSION);
        String vulnerabilitiesFilesRegex =
                LocaleUtils.createResourceFilesRegex(
                        Constant.VULNERABILITIES_PREFIX, Constant.VULNERABILITIES_EXTENSION);
        StringBuilder strBuilder =
                new StringBuilder(
                        messagesFilesRegex.length() + vulnerabilitiesFilesRegex.length() + 1);
        strBuilder.append(messagesFilesRegex).append('|').append(vulnerabilitiesFilesRegex);
        return Pattern.compile(strBuilder.toString());
    }
}
