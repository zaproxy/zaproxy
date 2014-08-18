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
 * Interface for factories creating {@link Payload} objects of a certain type
 *
 * @param <P>	the {@link Payload} type generated
 */
public interface PayloadFactory<P extends Payload> {
	/**
	 * Creates a simple String {@link Payload} with the content specified.
	 * @param data	String content
	 * @return	the {@link Payload} object created
	 */
	P createPayload(String data);
	/**
	 * Creates a {@link Payload} of the type specified.
	 * @param type	payload type to be generated
	 * @param data	String content, file path or regular expression depending on the type parameter
	 * @return the {@link Payload} object created
	 */
	P createPayload(Payload.Type type, String data);
	/**
	 * Creates a {@link Payload} of the type specified.
	 * @param type	payload type to be generated
	 * @param data	String content, file path or regular expression depending on the type parameter
	 * @param limit	limit parameter for Regex payloads
	 */
	P createPayload(Payload.Type type, String data, int limit);
}
