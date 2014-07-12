/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
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

public class FuzzResult<M extends Message, L extends FuzzLocation<M>> {

	public enum State {
		SUCCESSFUL, REFLECTED, ERROR, CUSTOM
	}
	
	private String name;
	private String custom;
	private ArrayList<String> payloads;
	private State state;
	private Message message;

	public FuzzResult() {
		state = State.SUCCESSFUL;
	}
	
	public void setName(String n){
		name = n;
	}
	public String getName(){
		return name;
	}
	public void setPayloads(ArrayList<String> pay){
		payloads = pay;
	}
	public ArrayList<String> getPayloads(){
		return payloads;
	}
	public void setState(State aState) {
		state = aState;
	}

	public State getState() {
		return state;
	}

	public void setMessage(Message aMessage) {
		this.message = aMessage;
	}

	public Message getMessage() {
		return message;
	}

	public String getCustom() {
		return custom;
	}

	public void setCustom(String custom) {
		this.custom = custom;
	}

}
