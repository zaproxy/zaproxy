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
/**
 * A separate process that executes the following steps in order:
 * <ol>
 * <li>Applies all {@link PayloadProcessor} to a given combination of {@link Payload}</li>
 * <li>Applies all {@link FuzzMessagePreProcessor} to a given message</li>
 * <li>Inserts the payloads into the message</li>
 * <li>Sends and receives the message to generate a {@link FuzzResult}</li>
 * <li>Applies all {@link FuzzResultProcessor} to a the result</li>
 * </ol>
 *
 * @param <R>	the type of {@link FuzzResult} generated.
 * @param <P>	the type of {@link Payload} inserted.
 * @param <M>	the type of {@link Message} sent.
 * @param <L>	the type of {@link FuzzLocation} associated.
 */
public interface FuzzProcess<R extends FuzzResult<?, ?>, P extends Payload, M extends Message, L extends FuzzLocation<M>>
		extends Runnable {
	/**
	 * Executes the fuzzing process
	 */
	@Override
	public void run();
	/**
	 * Stops this process and notifies listeners accordingly
	 */
	public void stop();
	/**
	 * Pauses this process and notifies listeners accordingly
	 */
	public void pause();
	/**
	 * Resumes this process and notifies listeners accordingly
	 */
	public void resume();
	/**
	 * Adds a listener monitoring this FuzzProcess
	 * @param listener the FuzzListener
	 */
	public void addFuzzerListener(FuzzerListener<Integer, R> listener);
	/**
	 * Removes a listener monitoring this FuzzProcess
	 * @param listener the FuzzListener
	 */
	public void removeFuzzerListener(FuzzerListener<Integer, R> listener);
	/**
	 * Defines a set of {@link PayloadProcessor} to be applied during execution.
	 * @param post the set of {@link PayloadProcessor}
	 */
	public void setPayloadProcessors(ArrayList<PayloadProcessor<P>> post);
	/**
	 * Defines a set of {@link FuzzMessagePreProcessor} to be applied during execution.
	 * @param post the set of {@link FuzzMessagePreProcessor}
	 */
	public void setPreProcessors(ArrayList<FuzzMessagePreProcessor<M,L,P>> post);
	/**
	 * Defines a set of {@link FuzzResultProcessor} to be applied during execution.
	 * @param post the set of {@link FuzzResultProcessor}
	 */
	public void setPostProcessors(ArrayList<FuzzResultProcessor<R>> post);
	/**
	 * Defines the fuzzing targets and their substitutions.
	 * @param subs a map between {@link FuzzLocation} and {@link Payload}
	 */
	void setPayload(Map<L, P> subs);
}