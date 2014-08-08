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
package org.zaproxy.zap.extension.multiFuzz;

import java.util.ArrayList;
import java.util.Map;

import org.zaproxy.zap.extension.httppanel.Message;

public interface FuzzProcess<R extends FuzzResult<?, ?>, P extends Payload, M extends Message, L extends FuzzLocation<M>>
		extends Runnable {
	@Override
	public void run();

	public void stop();

	public void pause();

	public void resume();

	public void addFuzzerListener(FuzzerListener<Integer, R> listener);

	public void removeFuzzerListener(FuzzerListener<Integer, R> listener);

	public void setPayloadProcessors(ArrayList<PayloadProcessor<P>> post);
	
	public void setPreProcessors(ArrayList<FuzzMessagePreProcessor<M,L,P>> post);

	public void setPostProcessors(ArrayList<FuzzResultProcessor<R>> post);

	void setPayload(Map<L, P> subs);
}