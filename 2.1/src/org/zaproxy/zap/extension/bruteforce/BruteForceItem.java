package org.zaproxy.zap.extension.bruteforce;

public class BruteForceItem implements Comparable<BruteForceItem> {
	private int historyId;
	private String url;
	private int statusCode;
	private String reason;
	
	public BruteForceItem(String url, int statusCode, String reason, int historyId) {
		super();
		this.url = url;
		this.statusCode = statusCode;
		this.reason = reason;
		this.historyId = historyId;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}

	public int getHistoryId() {
		return historyId;
	}

	@Override
	public int compareTo(BruteForceItem o) {
		return this.url.compareTo(o.getUrl());
	}

}
