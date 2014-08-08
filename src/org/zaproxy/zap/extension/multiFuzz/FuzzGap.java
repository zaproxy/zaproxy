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

public class FuzzGap<M extends Message, L extends FuzzLocation<M>, P extends Payload> {
	private ArrayList<P> payloads;
	private L fuzzLoc;
	private M msg;

	protected FuzzGap(M msg, L loc) {
		fuzzLoc = loc;
		this.msg = msg;
		payloads = new ArrayList<>();
	}

	public String orig() {
		return fuzzLoc.getRepresentation(msg);
	}

	public void addPayload(P p) {
		if (p != null) {
			payloads.add(p);
		}
	}

	public void addPayloads(ArrayList<P> pays) {
		payloads.addAll(pays);
	}

	public void setPayloads(ArrayList<P> pays) {
		payloads = pays;
	}

	public void removePayload(P p) {
		payloads.remove(p);
	}

	public L getLocation() {
		return fuzzLoc;
	}

	public ArrayList<P> getPayloads() {
		return this.payloads;
	}

}