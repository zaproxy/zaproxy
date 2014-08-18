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

/**
 * Listener to propagate state of {@link FuzzProcess}
 *
 * @param <P>	the {@link FuzzProcess} type
 * @param <R>	the {@link FuzzResult} type
 */
public interface FuzzerListener<P, R> {
	/**
	 * Notification that a process has started
	 * @param process the {@link FuzzProcess}
	 */
	void notifyFuzzerStarted(P process);
	/**
	 * Notification that a process has been paused
	 * @param process the {@link FuzzProcess}
	 */
	void notifyFuzzerPaused(P process);
	/**
	 * Notification that a process has finished 
	 * @param result the {@link FuzzProcess}
	 */
	void notifyFuzzerComplete(R result);

}
