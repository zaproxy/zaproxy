/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2023 The ZAP Development Team
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.zaproxy.zap.GitHubUser;

/** A task that uploads assets to a GitHub release. */
public abstract class UploadAssetsGitHubRelease extends DefaultTask {

    private static final int PAGE_SIZE = 10;

    private static final String TABLE_HEADER_END = "|---|---|\n";

    private final Property<String> repo;
    private final Property<String> tag;
    private final Property<Boolean> addChecksums;
    private final Property<String> checksumAlgorithm;
    private NamedDomainObjectContainer<Asset> assets;

    public UploadAssetsGitHubRelease() {
        ObjectFactory objects = getProject().getObjects();
        this.repo = objects.property(String.class);
        this.tag = objects.property(String.class);
        this.addChecksums = objects.property(Boolean.class).value(true);
        this.checksumAlgorithm = objects.property(String.class);
        this.assets = getProject().container(Asset.class, label -> new Asset(label, getProject()));

        setGroup("ZAP Misc");
        setDescription("Uploads assets to a GitHub release.");
    }

    @Input
    public Property<String> getRepo() {
        return repo;
    }

    @Input
    public abstract Property<GitHubUser> getUser();

    @Input
    public Property<String> getTag() {
        return tag;
    }

    @Input
    public Property<Boolean> getAddChecksums() {
        return addChecksums;
    }

    @Input
    public Property<String> getChecksumAlgorithm() {
        return checksumAlgorithm;
    }

    @Nested
    @Optional
    public Iterable<Asset> getAssets() {
        return new ArrayList<>(assets);
    }

    public void setAssets(NamedDomainObjectContainer<Asset> assets) {
        this.assets = assets;
    }

    public void assets(Action<? super NamedDomainObjectContainer<Asset>> action) {
        action.execute(assets);
    }

    @TaskAction
    public void createRelease() throws IOException {
        String tagName = tag.get();
        if (tagName.endsWith("-SNAPSHOT")) {
            getLogger().lifecycle("Ignoring, version is still SNAPSHOT: {}", tagName);
            return;
        }

        if (checksumAlgorithm.get().isEmpty()) {
            throw new IllegalArgumentException("The checksum algorithm must not be empty.");
        }

        GitHubUser ghUser = getUser().get();
        GHRepository ghRepo =
                GitHub.connect(ghUser.getName(), ghUser.getAuthToken()).getRepository(repo.get());

        GHRelease release = ghRepo.getReleaseByTagName(tagName);
        if (release == null) {
            getLogger()
                    .lifecycle(
                            "Release not found for tag: {}. Searching releases by name instead.",
                            tagName);
            release = getRelease(ghRepo, tagName);
        }

        String releaseBody = release.getBody();
        if (addChecksums.get()) {
            releaseBody = updateChecksumsTable(releaseBody);
            release.update().body(releaseBody).update();
        }

        for (Asset asset : assets) {
            release.uploadAsset(asset.getFile().getAsFile().get(), asset.getContentType().get());
        }
    }

    private static GHRelease getRelease(GHRepository ghRepo, String tagName) throws IOException {
        int i = 0;
        for (var release : ghRepo.listReleases().withPageSize(PAGE_SIZE)) {
            if (tagName.equals(release.getName())) {
                return release;
            }

            i++;
            if (i > PAGE_SIZE) {
                break;
            }
        }

        throw new InvalidUserDataException(
                "Release with name "
                        + tagName
                        + " not found after searching the latest "
                        + PAGE_SIZE
                        + " releases.");
    }

    private String updateChecksumsTable(String previousBody) throws IOException {
        int idx = previousBody.indexOf(TABLE_HEADER_END);
        if (idx == -1) {
            getProject().getLogger().warn("Table header not found, not adding checksums.");
            return previousBody;
        }

        idx += TABLE_HEADER_END.length();

        StringBuilder body = new StringBuilder(previousBody.length() + 400);
        body.append(previousBody.substring(0, idx));

        String baseDownloadLink =
                "https://github.com/" + repo.get() + "/releases/download/" + tag.get() + "/";
        DigestUtils digestUtils = new DigestUtils(checksumAlgorithm.get());

        List<File> files =
                assets.stream()
                        .map(e -> e.getFile().getAsFile().get())
                        .sorted((a, b) -> a.getName().compareTo(b.getName()))
                        .collect(Collectors.toList());
        for (File file : files) {
            String fileName = file.getName();
            body.append("| [")
                    .append(fileName)
                    .append("](")
                    .append(baseDownloadLink)
                    .append(fileName)
                    .append(") | `")
                    .append(digestUtils.digestAsHex(file))
                    .append("` |\n");
        }
        body.append(previousBody.substring(idx));
        return body.toString();
    }
}
