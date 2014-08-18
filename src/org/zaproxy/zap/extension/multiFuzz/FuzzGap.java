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
import org.zaproxy.zap.extension.httppanel.Message;
/**
 * A representation of a fuzz target consisting of a message, a location and a list of payloads.
 *
 * @param <M>	the message type.
 * @param <L>	the corresponding {@link FuzzLocation} type.
 * @param <P>	the corresponding {@link Payload} type.
 */
public class FuzzGap<M extends Message, L extends FuzzLocation<M>, P extends Payload> {
	private ArrayList<P> payloads;
	private L fuzzLoc;
	private M msg;
	/**
	 * The standard constructor.
	 * @param msg	the initial message.
	 * @param loc	the initial {@link FuzzLocation}.
	 */
	protected FuzzGap(M msg, L loc) {
		fuzzLoc = loc;
		this.msg = msg;
		payloads = new ArrayList<>();
	}
	/**
	 * Generates a String representation of the message content at the fuzzlocation.
	 * @return the representation
	 */
	public String orig() {
		return fuzzLoc.getRepresentation(msg);
	}
	/**
	 * Adds a payload to the list of payloads to be inserted in this FuzzGap.
	 * @param p	the payload
	 */
	public void addPayload(P p) {
		if (p != null) {
			payloads.add(p);
		}
	}
	/**
	 * Adds a list of payloads to be inserted in this FuzzGap.
	 * @param pays	the payload list
	 */
	public void addPayloads(ArrayList<P> pays) {
		payloads.addAll(pays);
	}
	/**
	 * Overwrites the list of payloads to be inserted in this FuzzGap.
	 * @param pays	the new payload list
	 */
	public void setPayloads(ArrayList<P> pays) {
		payloads = pays;
	}
	/**
	 * Removes a payload from the list of payloads to be inserted in this FuzzGap.
	 * @param p	the payload
	 */
	public void removePayload(P p) {
		payloads.remove(p);
	}
	/**
	 * Gets the payloads defined for this FuzzGap
	 * @return the list of payloads.
	 */
	public ArrayList<P> getPayloads() {
		return this.payloads;
	}
	/**
	 * Gets the location in the message.
	 * @return the location
	 */
	public L getLocation() {
		return fuzzLoc;
	}


}