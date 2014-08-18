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

import java.util.HashMap;
/**
 * Interface for factories generating {@link FuzzProcess} of the same type sharing certain parameters
 *
 * @param <P>	the type of {@link FuzzProcess} generated
 * @param <PL>	the type of {@link PayLoad} to be inserted.
 * @param <L>	the type of {@link FuzzLocation denoting} target locations.
 */
public interface FuzzProcessFactory<P extends FuzzProcess<?, ?, ?, ?>, PL extends Payload, L extends FuzzLocation<?>> {
	/**
	 * Generates and returns a new {@link FuzzProcess} with all message specific parameters set for this factory
	 * and defining target sections and their payloads. 
	 * @param hm	mapping between {@link FuzzLocation} targets and the {@link Payload} to be inserted
	 * @param id	the unique process id to be set
	 * @return		the generated {@link FuzzProcess}
	 */
	P getFuzzProcess(HashMap<L, PL> hm, int id);

}
