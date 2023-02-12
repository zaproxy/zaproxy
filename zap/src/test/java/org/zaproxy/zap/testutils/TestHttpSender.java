/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Path;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.network.HttpRequestConfig;
import org.zaproxy.zap.network.HttpSenderContext;
import org.zaproxy.zap.network.HttpSenderImpl;
import org.zaproxy.zap.network.HttpSenderListener;

public class TestHttpSender implements HttpSenderImpl<TestHttpSender.TestHttpSenderCtx> {

    private HttpMessageHandler messageHandler;
    private FileHandler fileHandler;

    void setMessageHandler(HttpMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    void setFileHandler(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    @Override
    public boolean isGlobalStateEnabled() {
        return false;
    }

    @Override
    public void addListener(HttpSenderListener listener) {
        // Nothing to do.
    }

    @Override
    public void removeListener(HttpSenderListener listener) {
        // Nothing to do.
    }

    @Override
    public TestHttpSenderCtx createContext(HttpSender parent, int initiator) {
        return mock(TestHttpSenderCtx.class);
    }

    @Override
    public void sendAndReceive(
            TestHttpSenderCtx ctx, HttpRequestConfig config, HttpMessage msg, Path file)
            throws IOException {
        try {
            if (file != null) {
                fileHandler.handle(msg, file);
            } else {
                messageHandler.handle(msg);
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public interface HttpMessageHandler {

        void handle(HttpMessage msg) throws Exception;
    }

    public interface FileHandler {

        void handle(HttpMessage msg, Path file) throws Exception;
    }

    public abstract static class TestHttpSenderCtx implements HttpSenderContext {}
}
