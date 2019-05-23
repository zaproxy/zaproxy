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
package org.zaproxy.zap.tasks;

import de.undercouch.gradle.tasks.download.DownloadAction;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.zaproxy.zap.internal.AddOnDownloadData;

public class DownloadAddOns extends DefaultTask {

    private final RegularFileProperty addOnsData;
    private final DirectoryProperty outputDir;

    public DownloadAddOns() {
        ObjectFactory objects = getProject().getObjects();
        this.addOnsData = objects.fileProperty();
        this.outputDir = objects.directoryProperty();
    }

    @InputFile
    public RegularFileProperty getAddOnsData() {
        return addOnsData;
    }

    @OutputDirectory
    public DirectoryProperty getOutputDir() {
        return outputDir;
    }

    @TaskAction
    public void download() throws IOException {
        Set<AddOnDownloadData> downloads = parseAddOnsData();
        checkExistingFiles(downloads);

        for (AddOnDownloadData downloadData : downloads) {
            downloadFile(downloadData);
        }
    }

    private void checkExistingFiles(Set<AddOnDownloadData> downloads) {
        File[] files = getOutputDir().get().getAsFile().listFiles();
        if (files == null) {
            return;
        }

        List<File> existingFiles = new ArrayList<>(Arrays.asList(files));
        for (Iterator<AddOnDownloadData> it = downloads.iterator(); it.hasNext(); ) {
            AddOnDownloadData downloadData = it.next();
            File file = downloadData.getOutputFile();
            if (existingFiles.contains(file) && hasSameHash(file, downloadData.getHash())) {
                existingFiles.remove(file);
                it.remove();
            }
        }

        for (File file : existingFiles) {
            getProject().delete(file);
        }
    }

    private void downloadFile(AddOnDownloadData downloadData) throws IOException {
        File file = downloadData.getOutputFile();
        DownloadAction downloadAction = new DownloadAction(getProject());
        downloadAction.src(downloadData.getUrl());
        downloadAction.dest(file);
        downloadAction.execute();

        String computedHash = hash(file);
        if (!computedHash.equalsIgnoreCase(downloadData.getHash())) {
            throw new IOException(
                    "Hash mismatch for file "
                            + file
                            + " expected "
                            + downloadData.getHash()
                            + " but got "
                            + computedHash);
        }
    }

    private static boolean hasSameHash(File file, String hash) {
        try {
            return hash(file).equalsIgnoreCase(hash);
        } catch (IOException ignore) {
            // Ignore
        }
        return false;
    }

    private static String hash(File file) throws IOException {
        try (InputStream is = Files.newInputStream(file.toPath())) {
            return DigestUtils.sha256Hex(is);
        }
    }

    private Set<AddOnDownloadData> parseAddOnsData() throws IOException {
        File outputDirectory = getOutputDir().get().getAsFile();
        List<String> lines = Files.readAllLines(addOnsData.get().getAsFile().toPath());
        return lines.stream()
                .map(String::trim)
                .filter(line -> !(line.isEmpty() || line.startsWith("#")))
                .map(
                        line -> {
                            String[] lineData = line.split(" ", 2);
                            String url = lineData[0];
                            String hash = lineData[1];
                            File file = new File(outputDirectory, getFileName(url));
                            return new AddOnDownloadData(url, hash, file);
                        })
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static String getFileName(String url) {
        int index = url.lastIndexOf("/");
        if (index == -1) {
            throw new InvalidUserDataException("The url does not contain a file name: " + url);
        }
        return url.substring(index + 1);
    }
}
