/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
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
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.gradle.api.DefaultTask;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.zaproxy.zap.GitHubRepo;
import org.zaproxy.zap.GitHubUser;

/** A task that checks for modifications in a git repo, commits, and creates a pull request. */
public abstract class CreatePullRequest extends DefaultTask {

    private static final String GITHUB_BASE_URL = "https://github.com/";

    private static final String GIT_REMOTE_ORIGIN = "origin";

    private static final String DEFAULT_GIT_BASE_BRANCH_NAME = "main";

    private final Property<String> baseBranchName;

    public CreatePullRequest() {
        ObjectFactory objects = getProject().getObjects();
        this.baseBranchName =
                objects.property(String.class).convention(DEFAULT_GIT_BASE_BRANCH_NAME);

        setGroup("ZAP");
        setDescription("Creates a pull request with modifications done in a repo.");
    }

    @Internal
    public Property<String> getBaseBranchName() {
        return baseBranchName;
    }

    @Internal
    public abstract Property<String> getBranchName();

    @Internal
    public abstract Property<GitHubUser> getUser();

    @Internal
    public abstract Property<GitHubRepo> getRepo();

    @Internal
    public abstract Property<String> getCommitSummary();

    @Internal
    public abstract Property<String> getCommitDescription();

    @Internal
    public abstract Property<String> getPullRequestTitle();

    @Internal
    public abstract Property<String> getPullRequestDescription();

    @TaskAction
    public void pullRequest() throws Exception {
        GitHubRepo ghRepo = getRepo().get();
        Repository repository =
                new FileRepositoryBuilder().setGitDir(new File(ghRepo.getDir(), ".git")).build();
        try (Git git = new Git(repository)) {
            if (git.status().call().getModified().isEmpty()) {
                return;
            }

            GitHubUser ghUser = getUser().get();

            URIish originUri =
                    new URIish(GITHUB_BASE_URL + ghUser.getName() + "/" + ghRepo.getName());
            git.remoteSetUrl().setRemoteName(GIT_REMOTE_ORIGIN).setRemoteUri(originUri).call();

            git.checkout()
                    .setCreateBranch(true)
                    .setName(getBranchName().get())
                    .setStartPoint(GIT_REMOTE_ORIGIN + "/" + baseBranchName.get())
                    .call();

            PersonIdent personIdent = new PersonIdent(ghUser.getName(), ghUser.getEmail());
            git.commit()
                    .setAll(true)
                    .setSign(false)
                    .setAuthor(personIdent)
                    .setCommitter(personIdent)
                    .setMessage(
                            getCommitSummary().get()
                                    + "\n\n"
                                    + getCommitDescription().get()
                                    + signedOffBy(personIdent))
                    .call();

            git.push()
                    .setCredentialsProvider(
                            new UsernamePasswordCredentialsProvider(
                                    ghUser.getName(), ghUser.getAuthToken()))
                    .setForce(true)
                    .add(getBranchName().get())
                    .call();

            GHRepository ghRepository =
                    GitHub.connect(ghUser.getName(), ghUser.getAuthToken())
                            .getRepository(ghRepo.toString());

            List<GHPullRequest> pulls =
                    ghRepository
                            .queryPullRequests()
                            .base(baseBranchName.get())
                            .head(ghUser.getName() + ":" + getBranchName().get())
                            .state(GHIssueState.OPEN)
                            .list()
                            .asList();
            String description =
                    getPullRequestDescription().getOrElse(getCommitDescription().get());
            if (pulls.isEmpty()) {
                String title = getPullRequestTitle().getOrElse(getCommitSummary().get());
                createPullRequest(ghRepository, title, description);
            } else {
                pulls.get(0).setBody(description);
            }
        }
    }

    private static String signedOffBy(PersonIdent personIdent) {
        return "\n\nSigned-off-by: "
                + personIdent.getName()
                + " <"
                + personIdent.getEmailAddress()
                + ">";
    }

    private void createPullRequest(GHRepository ghRepo, String title, String description)
            throws IOException {
        ghRepo.createPullRequest(
                title,
                getUser().get().getName() + ":" + getBranchName().get(),
                baseBranchName.get(),
                description);
    }
}
