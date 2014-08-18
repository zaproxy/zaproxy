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

import java.awt.Component;
import java.util.ArrayList;

import org.zaproxy.zap.extension.httppanel.Message;
/**
 * Interface for UI components which display target messages and highlight fuzzing targets within them.
 * 
 * @param <M>	the message type
 * @param <L>	the associated {@link FuzzLocation} implementation
 * @param <G>	the associated {@link FuzzGap} implementation
 */
public interface FuzzComponent<M extends Message, L extends FuzzLocation<M>, G extends FuzzGap<M, L, ?>> {
	/**
	 * Returns the {@link FuzzLocation} in the message that has been selected by the user in the component.
	 * @return	the FuzzLocation
	 */
	L selection();
	/**
	 * Highlights a new set of locations in the component.
	 * @param allLocs	list of locations to be highlighted
	 */
	void highlight(ArrayList<G> allLocs);
	/**
	 * The component itself
	 * @return	the component
	 */
	Component messageView();
	/**
	 * Changes the user selection in the Component to a specific {@link FuzzLocation} in the message.
	 * @param f	the target {@link FuzzLocation}
	 */
	void markUp(L f);
	/**
	 * Searches through the message and cycles through parts matching a certain String representation. The current match is being selected.
	 * @param text	search String
	 */
	void search(String text);
	/**
	 * Sets the message displayed
	 * @param message the new message
	 */
	void setMessage(M message);
}
