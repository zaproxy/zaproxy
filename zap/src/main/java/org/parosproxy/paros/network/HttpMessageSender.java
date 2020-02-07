package org.parosproxy.paros.network;

import java.io.IOException;

public interface HttpMessageSender {
    void sendAndReceive(HttpMessage message) throws IOException;
}
