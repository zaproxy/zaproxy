/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.zaproxy.zap.extension.multiFuzz.impl.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.extension.multiFuzz.FuzzMessagePreProcessor;
import org.zaproxy.zap.extension.multiFuzz.FuzzProcess;
import org.zaproxy.zap.extension.multiFuzz.FuzzResult;
import org.zaproxy.zap.extension.multiFuzz.FuzzResultProcessor;
import org.zaproxy.zap.extension.multiFuzz.FuzzerListener;
import org.zaproxy.zap.extension.multiFuzz.PayloadProcessor;

public class HttpFuzzProcess implements
		FuzzProcess<HttpFuzzResult, HttpPayload, HttpMessage, HttpFuzzLocation> {

	private static final Logger logger = Logger
			.getLogger(HttpFuzzProcess.class);
	private HashMap<HttpFuzzLocation, HttpPayload> payloads;
	private final int id;
	private HttpSender httpSender;
	private HttpMessage orig;
	private HttpFuzzResult result;
	private ArrayList<PayloadProcessor<HttpPayload>> payloadprocessors;
	private ArrayList<FuzzMessagePreProcessor<HttpMessage, HttpFuzzLocation, HttpPayload>> preprocessors;
	private ArrayList<FuzzResultProcessor<HttpFuzzResult>> postprocessors;
	private ArrayList<FuzzerListener<Integer, HttpFuzzResult>> listeners;

	public HttpFuzzProcess(HttpSender httpSender, HttpMessage msg, int id) {
		this.httpSender = httpSender;
		this.orig = msg;
		this.id = id;
		listeners = new ArrayList<>();
		this.payloadprocessors = new ArrayList<>();
		this.preprocessors = new ArrayList<>();
		this.postprocessors = new ArrayList<>();
	}

	private boolean isFuzzStringReflected(HttpMessage msg,
			HashMap<HttpFuzzLocation, HttpPayload> subs) {
		HttpMessage originalMessage = msg;
		boolean reflected = false;
		for (HttpFuzzLocation fuzzLoc : subs.keySet()) {
			final int pos = originalMessage.getResponseBody().toString()
					.indexOf(subs.get(fuzzLoc).getData());
			reflected |= msg.getResponseBody().toString()
					.indexOf(subs.get(fuzzLoc).getData(), pos) != -1;
		}
		return reflected;
	}

	@Override
	public void setPayload(Map<HttpFuzzLocation, HttpPayload> subs) {
		this.payloads = (HashMap<HttpFuzzLocation, HttpPayload>) subs;
	}

	@Override
	public void run() {
		for (FuzzerListener<Integer, HttpFuzzResult> listener : listeners) {
			listener.notifyFuzzerStarted(null);
		}
		HttpFuzzResult fuzzResult = new HttpFuzzResult();
		HttpMessage request = orig.cloneRequest();
		try {
			request = inject(request);
		} catch (HttpMalformedHeaderException e1) {
			fuzzResult.setMessage(request);
			fuzzResult.setName(Constant.messages.getString("fuzz.http.name") + id);
			fuzzResult.setState(FuzzResult.STATE_ERROR);
			ArrayList<String> paySig = new ArrayList<>();
			for (HttpPayload p : payloads.values()) {
				paySig.add(p.getData());
			}
			fuzzResult.setPayloads(paySig);
			this.result = fuzzResult;
			this.stop();
		}
		for (FuzzMessagePreProcessor<HttpMessage, HttpFuzzLocation, HttpPayload> pre : preprocessors) {
			try {
				request = pre.process(request, payloads);
			} catch (Exception e) {
				logger.error("Pre processor error:", e);
			}
		}
		

		request.getRequestHeader().setContentLength(
				request.getRequestBody().length());

		try {
			httpSender.sendAndReceive(request);

			if (isFuzzStringReflected(request, payloads)) {
				fuzzResult.setState(HttpFuzzResult.STATE_REFLECTED);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			fuzzResult.setState(HttpFuzzResult.STATE_ERROR);
		}
		
		fuzzResult.setMessage(request);
		fuzzResult.setName(Constant.messages.getString("fuzz.http.name") + id);
		ArrayList<String> paySig = new ArrayList<>();
		for (HttpPayload p : payloads.values()) {
			paySig.add(p.getData());
		}
		fuzzResult.setPayloads(paySig);
		for (FuzzResultProcessor<HttpFuzzResult> post : postprocessors) {
			try {
				fuzzResult = post.process(fuzzResult);
			} catch (Exception e) {
				logger.error("Post processor error:", e);
			}
		}
		this.result = fuzzResult;
		this.stop();
	}

	@Override
	public void setPayloadProcessors(
			ArrayList<PayloadProcessor<HttpPayload>> pay) {
		this.payloadprocessors = pay;
	}

	@Override
	public void setPreProcessors(
			ArrayList<FuzzMessagePreProcessor<HttpMessage, HttpFuzzLocation, HttpPayload>> pre) {
		this.preprocessors = pre;
	}

	@Override
	public void setPostProcessors(
			ArrayList<FuzzResultProcessor<HttpFuzzResult>> post) {
		this.postprocessors = post;
	}

	private HttpMessage inject(HttpMessage request)
			throws HttpMalformedHeaderException {
		ArrayList<HttpFuzzLocation> intervals = new ArrayList<>();
		for (HttpFuzzLocation fl : payloads.keySet()) {
			intervals.add(fl);
		}
		Collections.sort(intervals);
		HttpMessage fuzzedHttpMessage = request.cloneRequest();

		String origHead = fuzzedHttpMessage.getRequestHeader().toString();
		String origBody = fuzzedHttpMessage.getRequestBody().toString();
		StringBuilder head = new StringBuilder();
		StringBuilder body = new StringBuilder();
		int currPosHead = 0;
		int currPosBody = 0;
		String note = "";
		for (HttpFuzzLocation fuzzLoc : intervals) {
			if (fuzzLoc.begin() < origHead.length()) {
				if (fuzzLoc.begin() >= currPosHead) {
					int hl = 0;
					int pos = 0;
					while (((pos = origHead.indexOf("\r\n", pos)) != -1)
							&& (pos <= fuzzLoc.start + hl)) {
						pos += 2;
						++hl;
					}
					head.append(origHead.substring(currPosHead, fuzzLoc.begin()
							+ hl));
					HttpPayload payload = payloads.get(fuzzLoc);
					for (PayloadProcessor<HttpPayload> p : payloadprocessors) {
						payload = p.process(payload);
					}
					head.append(payload.getData());
					currPosHead = fuzzLoc.end + hl;
				}
			} else {
				int start = fuzzLoc.begin();
				int end = fuzzLoc.end();
				if (start > origBody.length()) {
					start -= origHead.length() + 1;
					end -= origHead.length() + 1;
				}
				body.append(origBody.substring(currPosBody, start));
				HttpPayload payload = payloads.get(fuzzLoc);
				for (PayloadProcessor<HttpPayload> p : payloadprocessors) {
					payload = p.process(payload);
				}
				body.append(payload.getData());
				currPosBody = end;
			}
			note += payloads.get(fuzzLoc).getData();
		}
		head.append(origHead.substring(currPosHead));
		body.append(origBody.substring(currPosBody));

		fuzzedHttpMessage.setRequestHeader(head.toString());
		fuzzedHttpMessage.setRequestBody(body.toString());
		fuzzedHttpMessage.setNote(note);
		return fuzzedHttpMessage;
	}

	@Override
	public void stop() {
		for (FuzzerListener<Integer, HttpFuzzResult> f : listeners) {
			f.notifyFuzzerComplete(this.result);
		}
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void addFuzzerListener(
			FuzzerListener<Integer, HttpFuzzResult> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeFuzzerListener(
			FuzzerListener<Integer, HttpFuzzResult> listener) {
		listeners.remove(listener);
	}

}
