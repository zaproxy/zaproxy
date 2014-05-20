package org.zaproxy.zap.extension.authorization;

import java.util.regex.Pattern;

import org.parosproxy.paros.network.HttpMessage;

/**
 * A simple authorization detection method based on matching the status code of the response and identifying
 * patterns in the response's body and header.
 */
public class BasicAuthorizationDetectionMethod implements AuthorizationDetectionMethod {

	public static final int NO_STATUS_CODE = -1;

	/**
	 * Defines how the conditions are composed one with another to obtain the final result.
	 */
	public enum LogicalOperator {
		AND, OR
	};

	protected LogicalOperator logicalOperator;
	protected int statusCode;
	protected Pattern headerPattern;
	protected Pattern bodyPattern;

	public BasicAuthorizationDetectionMethod(Integer statusCode, String headerRegex, String bodyRegex,
			LogicalOperator logicalOperator) {
		this.headerPattern = buildPattern(headerRegex);
		this.bodyPattern = buildPattern(bodyRegex);
		this.logicalOperator = logicalOperator;
		this.statusCode = statusCode != null ? statusCode : NO_STATUS_CODE;
	}

	private BasicAuthorizationDetectionMethod(int statusCode, Pattern headerPattern, Pattern bodyPattern,
			LogicalOperator composition) {
		this.headerPattern = headerPattern;
		this.bodyPattern = bodyPattern;
		this.logicalOperator = composition;
		this.statusCode = statusCode;
	}

	private static Pattern buildPattern(String regex) {
		if (regex == null || regex.isEmpty())
			return null;
		return Pattern.compile(regex);
	}

	@Override
	public boolean isResponseForUnauthorizedRequest(HttpMessage message) {
		// NOTE: In case nothing is configured, we default to "not match" when composition is "OR" and
		// "matches" when composition is "AND" so not configuring
		boolean statusCodeMatch = message.getResponseHeader().getStatusCode() == statusCode;
		boolean headerMatch = headerPattern != null ? headerPattern.matcher(
				message.getResponseHeader().toString()).find() : false;
		boolean bodyMatch = bodyPattern != null ? bodyPattern.matcher(message.getResponseBody().toString())
				.find() : false;

		switch (logicalOperator) {
		case AND:
			// If nothing is set, we default to false so we get the expected behavior
			if (statusCode == NO_STATUS_CODE && headerPattern == null && bodyPattern == null)
				return false;
			// All of them must match or not be set
			return (statusCodeMatch || statusCode == NO_STATUS_CODE)
					&& (headerPattern == null || headerMatch) && (bodyPattern == null || bodyMatch);
		case OR:
			// At least one of them must match
			return statusCodeMatch || headerMatch || bodyMatch;
		default:
			return false;
		}
	}

	@Override
	public String toString() {
		return "BasicAuthorizationDetectionMethod [" + logicalOperator + ": code=" + statusCode + ", header="
				+ headerPattern + ", body=" + bodyPattern + "]";
	}

	@Override
	public AuthorizationDetectionMethod clone() {
		return new BasicAuthorizationDetectionMethod(this.statusCode, this.headerPattern, this.bodyPattern,
				this.logicalOperator);
	}
}
