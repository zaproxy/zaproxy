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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.zaproxy.zap.GitHubRepo;
import org.zaproxy.zap.GitHubUser;

/** A task that sends a {@code repository_dispatch} to a GitHub repo. */
public abstract class SendRepositoryDispatch extends DefaultTask {

    private static final int EXPECTED_STATUS_CODE = HttpURLConnection.HTTP_NO_CONTENT;
    private static final int NO_STATUS_CODE = -1;

    @Input
    public abstract Property<GitHubUser> getGitHubUser();

    @Input
    public abstract Property<GitHubRepo> getGitHubRepo();

    @Input
    public abstract Property<String> getEventType();

    @Input
    @Optional
    public abstract MapProperty<String, Object> getClientPayload();

    @TaskAction
    void send() {
        HttpURLConnection connection = createConnection();
        writeRepositoryDispatch(connection);

        int statusCode = getStatusCode(connection);
        if (statusCode == EXPECTED_STATUS_CODE) {
            return;
        }

        if (statusCode == NO_STATUS_CODE) {
            throw new BuildException("Unable to get a response.");
        }

        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append(
                String.format(
                        "Repository dispatch was not successful, expected status code %s received %s.",
                        EXPECTED_STATUS_CODE, statusCode));

        String response = readResponse(connection);
        if (response != null && !response.isEmpty()) {
            errorMessage.append("\nResponse:\n").append(response);
        }
        throw new BuildException(errorMessage.toString());
    }

    private HttpURLConnection createConnection() {
        String url =
                String.format(
                        "https://api.github.com/repos/%s/dispatches",
                        getGitHubRepo().get().toString());
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException e) {
            throw new BuildException("Failed to create the connection:", e);
        }
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        try {
            connection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            throw new BuildException("Failed to create the connection:", e);
        }
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setRequestProperty("Content-Type", "application/json");

        GitHubUser user = getGitHubUser().get();
        String userName = user.getName();
        String token = user.getAuthToken();
        byte[] usernameAuthToken = (userName + ":" + token).getBytes(StandardCharsets.UTF_8);
        String authorization = "Basic " + Base64.getEncoder().encodeToString(usernameAuthToken);
        connection.setRequestProperty("Authorization", authorization);

        return connection;
    }

    private void writeRepositoryDispatch(HttpURLConnection connection) {
        byte[] repositoryDispatch;
        try {
            repositoryDispatch = createRepositoryDispatch();
        } catch (JsonProcessingException e) {
            throw new BuildException("Failed to create the request body:", e);
        }

        try (OutputStream os = connection.getOutputStream()) {
            os.write(repositoryDispatch);
        } catch (IOException e) {
            throw new BuildException("Failed to write the repository dispatch:", e);
        }
    }

    private byte[] createRepositoryDispatch() throws JsonProcessingException {
        Map<String, Object> repositoryDispatch = new LinkedHashMap<>();
        repositoryDispatch.put("event_type", getEventType().get());

        Map<String, Object> clientPayload = getClientPayload().getOrNull();
        if (clientPayload != null && !clientPayload.isEmpty()) {
            repositoryDispatch.put("client_payload", clientPayload);
        }

        return new ObjectMapper().writeValueAsBytes(repositoryDispatch);
    }

    private int getStatusCode(HttpURLConnection connection) {
        try {
            return connection.getResponseCode();
        } catch (IOException e) {
            getLogger()
                    .warn("Failed to read the repository dispatch status code: " + e.getMessage());
        }
        return NO_STATUS_CODE;
    }

    private String readResponse(HttpURLConnection connection) {
        try {
            return IOUtils.toString(connection.getErrorStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            getLogger().warn("Failed to read the repository dispatch response: " + e.getMessage());
        }
        return null;
    }
}
