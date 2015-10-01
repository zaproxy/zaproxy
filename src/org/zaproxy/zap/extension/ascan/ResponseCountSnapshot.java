/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2015 The ZAP development team
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
package org.zaproxy.zap.extension.ascan;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a snapshot of response counts, grouped by response code groups
 * @author simon
 *
 */
public class ResponseCountSnapshot {
	public enum RESPONSE_CODE_RANGE {RC_100, RC_200, RC_300, RC_400, RC_500};

	private Date date;
	private int period;
	private AtomicInteger resp100;
	private AtomicInteger resp200;
	private AtomicInteger resp300;
	private AtomicInteger resp400;
	private AtomicInteger resp500;
	
	public ResponseCountSnapshot() {
		this.date = new Date();
		this.period = -1;
		this.resp100 = new AtomicInteger();
		this.resp200 = new AtomicInteger();
		this.resp300 = new AtomicInteger();
		this.resp400 = new AtomicInteger();
		this.resp500 = new AtomicInteger();
	}

	public ResponseCountSnapshot(Date date, int period, int resp100,
			int resp200, int resp300, int resp400, int resp500) {
		super();
		this.date = date;
		this.period = period;
		this.resp100 = new AtomicInteger(resp100);
		this.resp200 = new AtomicInteger(resp200);
		this.resp300 = new AtomicInteger(resp300);
		this.resp400 = new AtomicInteger(resp400);
		this.resp500 = new AtomicInteger(resp500);
	}
	
	public ResponseCountSnapshot getDifference(ResponseCountSnapshot previous) {
		return new ResponseCountSnapshot(this.date, getPeriod(),
				getResp100() - previous.getResp100(),
				getResp200() - previous.getResp200(),
				getResp300() - previous.getResp300(),
				getResp400() - previous.getResp400(),
				getResp500() - previous.getResp500());
	}

	public Date getDate() {
		return date;
	}
	
	public int getResponseCodeCount(RESPONSE_CODE_RANGE rcr) {
		switch (rcr) {
		case RC_100:	return getResp100();
		case RC_200:	return getResp200();
		case RC_300:	return getResp300();
		case RC_400:	return getResp400();
		case RC_500:	return getResp500();
		}
		return -1;
	}

	public void incResponseCodeCount(int responseCode) {
		switch (responseCode / 100) {
		case 1:	incResponseCodeCount(RESPONSE_CODE_RANGE.RC_100); break;
		case 2:	incResponseCodeCount(RESPONSE_CODE_RANGE.RC_200); break;
		case 3:	incResponseCodeCount(RESPONSE_CODE_RANGE.RC_300); break;
		case 4:	incResponseCodeCount(RESPONSE_CODE_RANGE.RC_400); break;
		case 5:	incResponseCodeCount(RESPONSE_CODE_RANGE.RC_500); break;
		default:	// Ignore
		}
	}

	public void incResponseCodeCount(RESPONSE_CODE_RANGE rcr) {
		switch (rcr) {
		case RC_100:	resp100.incrementAndGet(); break;
		case RC_200:	resp200.incrementAndGet(); break;
		case RC_300:	resp300.incrementAndGet(); break;
		case RC_400:	resp400.incrementAndGet(); break;
		case RC_500:	resp500.incrementAndGet(); break;
		}
	}

	public int getPeriod() {
		return period;
	}

	public int getResp100() {
		return resp100.get();
	}

	public int getResp200() {
		return resp200.get();
	}

	public int getResp300() {
		return resp300.get();
	}

	public int getResp400() {
		return resp400.get();
	}

	public int getResp500() {
		return resp500.get();
	}
	
	public int getTotal() {
		return this.getResp100() + this.getResp200() + this.getResp300() + this.getResp400() + this.getResp500(); 
	}
	
	@Override
	public ResponseCountSnapshot clone() {
		return new ResponseCountSnapshot(new Date(), period, 
				resp100.get(), resp200.get(), resp300.get(), resp400.get(), resp500.get());
	}
}
