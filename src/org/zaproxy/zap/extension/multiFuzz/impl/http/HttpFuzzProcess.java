/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
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
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.extension.multiFuzz.FuzzMessageProcessor;
import org.zaproxy.zap.extension.multiFuzz.FuzzProcess;
import org.zaproxy.zap.extension.multiFuzz.FuzzResult.State;
import org.zaproxy.zap.extension.multiFuzz.FuzzerListener;

public class HttpFuzzProcess implements
		FuzzProcess<HttpFuzzResult, HttpPayload, HttpMessage, HttpFuzzLocation> {

	private static final Logger logger = Logger
			.getLogger(HttpFuzzProcess.class);
	private HashMap<HttpFuzzLocation, HttpPayload> payloads;
	private HttpSender httpSender;
	private HttpMessage orig;
	private HttpFuzzResult result;
	private boolean paused = false;
	private ArrayList<FuzzMessageProcessor<HttpMessage>> preprocessors;
	private ArrayList<FuzzMessageProcessor<HttpMessage>> postprocessors;
	private ArrayList<FuzzerListener<Integer, HttpFuzzResult>> listeners;

	public HttpFuzzProcess(HttpSender httpSender, HttpMessage msg) {
		this.httpSender = httpSender;
		this.orig = msg;
		listeners = new ArrayList<FuzzerListener<Integer, HttpFuzzResult>>();
		this.preprocessors = new ArrayList<FuzzMessageProcessor<HttpMessage>>();
		this.postprocessors = new ArrayList<FuzzMessageProcessor<HttpMessage>>();
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
		HttpMessage request = orig.cloneRequest();

		request = inject(request);

		for (FuzzMessageProcessor<HttpMessage> pre : preprocessors) {
			try {
				request = pre.process(request);
			} catch (Exception e) {
				logger.error("Pre processor error:", e);
			}
		}
		HttpFuzzResult fuzzResult = new HttpFuzzResult();

		request.getRequestHeader().setContentLength(
				request.getRequestBody().length());

		try {
			httpSender.sendAndReceive(request);

			if (isFuzzStringReflected(request, payloads)) {
				fuzzResult.setState(State.REFLECTED);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			fuzzResult.setState(State.ERROR);
		}
		for (FuzzMessageProcessor<HttpMessage> post : postprocessors) {
			try {
				request = post.process(request);
			} catch (Exception e) {
				logger.error("Post processor error:", e);
			}
		}
		fuzzResult.setMessage(request);
		this.result = fuzzResult;
		this.stop();
	}

	@Override
	public void setPreProcessors(
			ArrayList<FuzzMessageProcessor<HttpMessage>> pre) {
		this.preprocessors = pre;
	}

	@Override
	public void setPostProcessors(
			ArrayList<FuzzMessageProcessor<HttpMessage>> post) {
		this.postprocessors = post;
	}

	private HttpMessage inject(HttpMessage request) {
		ArrayList<HttpFuzzLocation> intervals = new ArrayList<HttpFuzzLocation>();
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
			if (fuzzLoc.begin() <= origHead.length()) {
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
					head.append(payloads.get(fuzzLoc).getData());
					currPosHead = fuzzLoc.end + hl;
				}
			} else {
				int start = fuzzLoc.begin();
				int end = fuzzLoc.end();
				if (start > origBody.length()) {
					start -= origHead.length();
					end -= origHead.length();
				}
				body.append(origBody.substring(currPosBody, start));
				body.append(payloads.get(fuzzLoc).getData());
				currPosBody = end;
			}
			note += payloads.get(fuzzLoc).getData();
		}
		head.append(origHead.substring(currPosHead));
		body.append(origBody.substring(currPosBody));

		try {
			fuzzedHttpMessage.setRequestHeader(head.toString());
		} catch (HttpMalformedHeaderException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e.getMessage());
			}
		}
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
		this.paused = true;
	}

	@Override
	public void resume() {
		this.paused = false;
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
