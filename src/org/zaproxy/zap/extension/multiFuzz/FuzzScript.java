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

import java.util.Map;

import javax.script.ScriptException;

import org.zaproxy.zap.extension.httppanel.Message;
/**
 * Interface for the inclusion of fuzzing scripts.
 */
public interface FuzzScript{
	/**
	 * Called to alter each {@link Payload} before insertion into a target message.
	 * @param p	the {@link Payload}
	 * @throws ScriptException
	 */
	void processPayload(Payload p) throws ScriptException;
	/**
	 * Called for the manipulation of a message after payload insertion before sending it.
	 * @param msg	the message
	 * @param paymap	a map between {@link FuzzLocation} and inserted {@link Payload}
	 * @throws ScriptException
	 */
	void preProcess(Message msg, Map<?,?> paymap) throws ScriptException;
	/**
	 * Called for the manipulation of a {@link FuzzResult} generated after sending and receiving a message
	 * @param msg	the generated {@link FuzzResult}
	 * @throws ScriptException
	 */
	void postProcess(FuzzResult<?,?> msg) throws ScriptException;
}
