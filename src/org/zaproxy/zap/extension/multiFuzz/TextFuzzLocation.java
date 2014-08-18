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

import org.zaproxy.zap.extension.httppanel.Message;
/**
 * Extension of the {@link FuzzLocation} interfaced for text-based message types 
 *
 * @param <M> the underlying message type
 */
public interface TextFuzzLocation<M extends Message> extends FuzzLocation<M> {
	/**
	 * Indicates the starting position of the target section in an underlying text.
	 * @return the starting position
	 */
	public int begin();
	/**
	 * Indicates the end position of the target section in an underlying text.
	 * @return the end position
	 */
	public int end();
}
