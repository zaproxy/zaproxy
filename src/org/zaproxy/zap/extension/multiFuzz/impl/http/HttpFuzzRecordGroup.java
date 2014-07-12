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
package org.zaproxy.zap.extension.multiFuzz.impl.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;

import org.zaproxy.zap.utils.Pair;

public class HttpFuzzRecordGroup implements HttpFuzzRecord {
	
	private String name;
	private String custom;
	private ArrayList<HttpFuzzRecord> members = new ArrayList<HttpFuzzRecord>();
	private String method;
	private String URI;
	private int avgRTT;
	private int avgSize;
	private int status;
	private String reason;
	private Pair<String, ImageIcon> result;
	private boolean included = true;
	
	public HttpFuzzRecordGroup(String name){
		this.name = name;
		members = new ArrayList<HttpFuzzRecord>();
	}
	
	public HttpFuzzRecordGroup(String name, HttpFuzzRecord r){
		this.name = name;
		members.add(r);
		update();
	}
	
	private void update() {
		this.avgRTT = members.get(0).getRTT();
		this.avgSize = members.get(0).getSize();
		
		this.method = members.get(0).getMethod();
		this.URI = members.get(0).getURI();
		this.status = members.get(0).getState();
		this.reason = members.get(0).getReason();
		this.result = members.get(0).getResult();
		for(int i = 1;i < members.size(); i++){
			this.avgRTT += members.get(i).getRTT();
			this.avgSize += members.get(i).getSize();
			if(!members.get(i).getMethod().equals(method)){
				method = "";
			}
			if(!members.get(i).getURI().equals(URI)){
				URI = "";
			}
			if(members.get(i).getState() != status){
				status = -1;
			}
			if(!members.get(i).getReason().equals(reason)){
				reason = "";
			}
			if(!members.get(i).getResult().first.equals(result.first)){
				result = null;
			}
		}
		avgRTT /= members.size();
		avgSize /= members.size();
	}
	public ArrayList<HttpFuzzRecord> getMembers(){
		return members;
	}
	@Override
	public String getName() {
		return name + " (" + members.size() + ")";
	}

	@Override
	public Pair<String, ImageIcon> getResult() {
		return result;
	}

	@Override
	public List<String> getPayloads() {
		return Collections.emptyList();
	}

	@Override
	public Boolean isIncluded() {
		return included;
	}
	@Override
	public void setIncluded(Boolean i){
		this.included = i;
		for(HttpFuzzRecord r : members){
			r.setIncluded(i);
		}
	}
	@Override
	public String getMethod() {
		return method;
	}
	@Override
	public String getURI() {
		return URI;
	}
	@Override
	public int getRTT() {
		return avgRTT;
	}
	@Override
	public int getSize() {
		return avgSize;
	}
	@Override
	public int getState() {
		return status;
	}
	@Override
	public String getReason() {
		return reason;
	}
	@Override
	public void setName(String s) {
		this.name = s;
		update();
	}

	public void add(HttpFuzzRecord entry) {
		this.members.add(entry);
		update();
	}

	public String getCustom() {
		return custom;
	}

	public void setCustom(String custom) {
		this.custom = custom;
	}

}
