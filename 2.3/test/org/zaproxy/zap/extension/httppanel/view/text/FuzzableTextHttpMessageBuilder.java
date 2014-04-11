package org.zaproxy.zap.extension.httppanel.view.text;

import org.parosproxy.paros.network.HttpMessage;

public class FuzzableTextHttpMessageBuilder {

    private HttpMessage httpMessage = new HttpMessage();
    private FuzzableTextHttpMessage.Location location = FuzzableTextHttpMessage.Location.BODY;
    private int start = 0;
    private int end = 0;

    public static FuzzableTextHttpMessageBuilder aFuzzableTextHttpMessage() {
        return new FuzzableTextHttpMessageBuilder();
    }

    public FuzzableTextHttpMessage build() {
        return new FuzzableTextHttpMessage(httpMessage, location, start, end);
    }

    public FuzzableTextHttpMessageBuilder withHttpMessage(HttpMessage httpMessage) {
        this.httpMessage = httpMessage;
        return this;
    }

    public FuzzableTextHttpMessageBuilder withFuzzLocation(FuzzableTextHttpMessage.Location location) {
        this.location = location;
        return this;
    }

    public FuzzableTextHttpMessageBuilder withFuzzingFromToPosition(int start, int end) {
        this.start = start;
        this.end = end;
        return this;
    }


}
