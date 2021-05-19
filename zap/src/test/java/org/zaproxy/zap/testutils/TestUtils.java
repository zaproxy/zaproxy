/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
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
package org.zaproxy.zap.testutils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;

/** Class with utility/helper methods for general tests. */
public class TestUtils {

    static final String DEFAULT_CONTENT_TYPE = "text/html;charset=ISO-8859-1";

    /**
     * A HTTP test server.
     *
     * <p>The server is {@code null} if not started.
     *
     * @see #startServer()
     */
    protected HTTPDTestServer nano;

    /**
     * Gets the (file system) path to the given resource.
     *
     * <p>The resource path is obtained with the caller class using {@link
     * Class#getResource(String)}.
     *
     * @param resourcePath the path to the resource.
     * @return the path, never {@code null}.
     */
    protected Path getResourcePath(String resourcePath) {
        return getResourcePath(resourcePath, getClass());
    }

    /**
     * Gets the (file system) path to the given resource using the given class.
     *
     * <p>The resource path is obtained with the given class using {@link
     * Class#getResource(String)}.
     *
     * @param resourcePath the path to the resource.
     * @param clazz the class used to obtain the resource.
     * @return the path, never {@code null}.
     */
    protected static <T> Path getResourcePath(String resourcePath, Class<T> clazz) {
        try {
            URL url = clazz.getResource(resourcePath);
            if (url == null) {
                throw new RuntimeException("Resource not found: " + resourcePath);
            }
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Starts the HTTP test server with a random port.
     *
     * <p>The port can be obtained with the method {@link HTTPDTestServer#getListeningPort()} from
     * the {@link #nano test server}.
     *
     * @throws IOException if an error occurred while starting the server.
     * @see #stopServer()
     */
    protected void startServer() throws IOException {
        startServer(getRandomPort());
    }

    /**
     * Starts the HTTP test server with the specified port.
     *
     * <p>It's recommended to use {@link #startServer()} instead, using a fixed port might lead to
     * random failures when the port is already in use.
     *
     * @param port the port to listen to.
     * @throws IOException if an error occurred while starting the server.
     * @see #stopServer()
     */
    protected void startServer(int port) throws IOException {
        stopServer();

        nano = new HTTPDTestServer(port);
        nano.start();
    }

    private static int getRandomPort() throws IOException {
        try (ServerSocket server = new ServerSocket(0)) {
            return server.getLocalPort();
        }
    }

    /**
     * Stops the HTTP test server.
     *
     * @see #startServer()
     */
    protected void stopServer() {
        if (nano == null) {
            return;
        }
        nano.stop();
    }

    /**
     * Creates a (GET) HTTP message with the given path, for the {@link #nano test server}.
     *
     * <p>The response contains empty HTML tags, {@code <html></html>}.
     *
     * @param path the path component of the request-target, for example, {@code /dir/file.txt}.
     * @return the HTTP message, never {@code null}.
     * @throws IllegalStateException if the server was not {@link #startServer() started} prior
     *     calling this method.
     * @throws HttpMalformedHeaderException if an error occurred while creating the HTTP message.
     */
    protected HttpMessage getHttpMessage(String path) throws HttpMalformedHeaderException {
        return this.getHttpMessage("GET", DEFAULT_CONTENT_TYPE, path, "<html></html>");
    }

    /**
     * Creates a (GET) HTTP message with the given path, for the {@link #nano test server}.
     *
     * <p>The response contains empty HTML tags, {@code <html></html>}.
     *
     * @param path the path component of the request-target, for example, {@code /dir/file.txt}.
     * @return the HTTP message, never {@code null}.
     * @throws IllegalStateException if the server was not {@link #startServer() started} prior
     *     calling this method.
     * @throws HttpMalformedHeaderException if an error occurred while creating the HTTP message.
     */
    protected HttpMessage getHttpMessage(String path, String contentType)
            throws HttpMalformedHeaderException {
        return this.getHttpMessage("GET", contentType, path, "<html></html>");
    }

    /**
     * Creates a HTTP message with the given data, for the {@link #nano test server}.
     *
     * @param method the HTTP method.
     * @param path the path component of the request-target, for example, {@code /dir/file.txt}.
     * @param responseBody the body of the response.
     * @return the HTTP message, never {@code null}.
     * @throws IllegalStateException if the server was not {@link #startServer() started} prior
     *     calling this method.
     * @throws HttpMalformedHeaderException if an error occurred while creating the HTTP message.
     */
    protected HttpMessage getHttpMessage(String method, String path, String responseBody)
            throws HttpMalformedHeaderException {
        return getHttpMessage(method, DEFAULT_CONTENT_TYPE, path, responseBody);
    }
    /**
     * Creates a HTTP message with the given data, for the {@link #nano test server}.
     *
     * @param method the HTTP method.
     * @param contentType the Content-Type header
     * @param path the path component of the request-target, for example, {@code /dir/file.txt}.
     * @param responseBody the body of the response.
     * @return the HTTP message, never {@code null}.
     * @throws IllegalStateException if the server was not {@link #startServer() started} prior
     *     calling this method.
     * @throws HttpMalformedHeaderException if an error occurred while creating the HTTP message.
     */
    protected HttpMessage getHttpMessage(
            String method, String contentType, String path, String responseBody)
            throws HttpMalformedHeaderException {
        if (nano == null) {
            throw new IllegalStateException("The HTTP test server was not started.");
        }

        HttpMessage msg = new HttpMessage();
        StringBuilder reqHeaderSB = new StringBuilder();
        reqHeaderSB.append(method);
        reqHeaderSB.append(" http://localhost:");
        reqHeaderSB.append(nano.getListeningPort());
        reqHeaderSB.append(path);
        reqHeaderSB.append(" HTTP/1.1\r\n");
        reqHeaderSB.append("Host: localhost:").append(nano.getListeningPort()).append("\r\n");
        reqHeaderSB.append("User-Agent: ZAP\r\n");
        reqHeaderSB.append("Pragma: no-cache\r\n");
        msg.setRequestHeader(reqHeaderSB.toString());

        msg.setResponseBody(responseBody);

        StringBuilder respHeaderSB = new StringBuilder();
        respHeaderSB.append("HTTP/1.1 200 OK\r\n");
        respHeaderSB.append("Server: Apache-Coyote/1.1\r\n");
        respHeaderSB.append("Content-Type: ");
        respHeaderSB.append(contentType);
        respHeaderSB.append("\r\n");
        respHeaderSB.append("Content-Length: ");
        respHeaderSB.append(msg.getResponseBody().length());
        respHeaderSB.append("\r\n");
        msg.setResponseHeader(respHeaderSB.toString());

        return msg;
    }

    /**
     * Gets the contents of the file with the given path.
     *
     * @param resourcePath the path to the resource.
     * @return the contents of the file.
     * @see #getResourcePath(String)
     */
    protected String getContent(String resourcePath) {
        return this.getContent(resourcePath, (Map<String, String>) null);
    }

    /**
     * Gets the contents of the file with the given path, replaced with the given parameters.
     *
     * @param resourcePath the path to the resource.
     * @param params the parameters to replace in the contents, might be {@code null}.
     * @return the contents of the file.
     * @see #getResourcePath(String)
     */
    protected String getContent(String resourcePath, String[][] params) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < params.length; i++) {
            map.put(params[i][0], params[i][1]);
        }
        return this.getContent(resourcePath, map);
    }

    /**
     * Gets the contents of the file with the given path, replaced with the given parameters.
     *
     * @param resourcePath the path to the resource.
     * @param params the parameters to replace in the contents, might be {@code null}.
     * @return the contents of the file.
     * @see #getResourcePath(String)
     */
    protected String getContent(String resourcePath, Map<String, String> params) {
        Path file = getResourcePath(resourcePath);
        try {
            String html = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            if (params != null) {
                // Replace all of the supplied parameters
                for (Entry<String, String> entry : params.entrySet()) {
                    html = html.replaceAll("@@@" + entry.getKey() + "@@@", entry.getValue());
                }
            }
            return html;
        } catch (IOException e) {
            System.err.println("Failed to read file " + file.toAbsolutePath());
            throw new RuntimeException(e);
        }
    }
}
