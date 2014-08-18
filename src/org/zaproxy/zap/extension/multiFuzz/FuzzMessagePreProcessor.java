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

import org.zaproxy.zap.extension.httppanel.Message;
/**
 * Interface for the manipulation messages after payload insertion and previous to generation of a {@link FuzzResult}.
 *
 * @param <FM> type of message to be processed
 * @param <L>  type of {@link FuzzLocation} associated
 * @param <P>  type of {@link Payload} associated
 */
public interface FuzzMessagePreProcessor<FM extends Message, L extends FuzzLocation<FM>, P extends Payload> {
	/**
	 * Processing routine for a single message
	 * @param orig	the original message
	 * @param payMap	the map of {@link FuzzLocation} and {@link Payload}
	 * @return		the resulting message
	 */
	public FM process(FM orig, Map<L, P> payMap);
}
