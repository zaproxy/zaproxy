package org.zaproxy.zap.extension.authorization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseSet;

/**
 * A simple authorization detection method based on matching the status code of the response and
 * identifying patterns in the response's body and header.
 */
public class BasicAuthorizationDetectionMethod implements AuthorizationDetectionMethod {

	public static final int METHOD_UNIQUE_ID = 0;
	public static final int NO_STATUS_CODE = -1;

	public static final String CONTEXT_CONFIG_AUTH_BASIC = AuthorizationDetectionMethod.CONTEXT_CONFIG_AUTH
			+ ".basic";
	public static final String CONTEXT_CONFIG_AUTH_BASIC_HEADER = CONTEXT_CONFIG_AUTH_BASIC + ".header";
	public static final String CONTEXT_CONFIG_AUTH_BASIC_BODY = CONTEXT_CONFIG_AUTH_BASIC + ".body";
	public static final String CONTEXT_CONFIG_AUTH_BASIC_LOGIC = CONTEXT_CONFIG_AUTH_BASIC + ".logic";
	public static final String CONTEXT_CONFIG_AUTH_BASIC_CODE = CONTEXT_CONFIG_AUTH_BASIC + ".code";

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

	public BasicAuthorizationDetectionMethod(Configuration config) throws ConfigurationException {
		this.headerPattern = buildPattern(config.getString(CONTEXT_CONFIG_AUTH_BASIC_HEADER));
		this.bodyPattern = buildPattern(config.getString(CONTEXT_CONFIG_AUTH_BASIC_BODY));
		this.logicalOperator = LogicalOperator.valueOf(config.getString(CONTEXT_CONFIG_AUTH_BASIC_LOGIC));
		this.statusCode = config.getInt(CONTEXT_CONFIG_AUTH_BASIC_CODE);
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

	private static String getPatternString(Pattern pattern) {
		if (pattern == null) {
			return "";
		}
		return pattern.pattern();
	}

	@Override
	public boolean isResponseForUnauthorizedRequest(HttpMessage message) {
		// NOTE: In case nothing is configured, we default to "not match" when composition is "OR"
		// and
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

	@Override
	public int getMethodUniqueIdentifier() {
		return METHOD_UNIQUE_ID;
	}

	@Override
	public void persistMethodToSession(Session session, int contextId) throws DatabaseException {
		session.setContextData(contextId, RecordContext.TYPE_AUTHORIZATION_METHOD_FIELD_1,
				Integer.toString(statusCode));
		// Add the patterns, making sure we delete existing data if there's are no patterns,
		// otherwise old data would get loaded
		if (headerPattern != null)
			session.setContextData(contextId, RecordContext.TYPE_AUTHORIZATION_METHOD_FIELD_2,
					headerPattern.pattern());
		else
			session.clearContextDataForType(contextId, RecordContext.TYPE_AUTHORIZATION_METHOD_FIELD_2);

		if (bodyPattern != null)
			session.setContextData(contextId, RecordContext.TYPE_AUTHORIZATION_METHOD_FIELD_3,
					bodyPattern.pattern());
		else
			session.clearContextDataForType(contextId, RecordContext.TYPE_AUTHORIZATION_METHOD_FIELD_3);

		session.setContextData(contextId, RecordContext.TYPE_AUTHORIZATION_METHOD_FIELD_4,
				logicalOperator.name());
	}

	/**
	 * Creates a {@link BasicAuthorizationDetectionMethod} object based on data loaded from the
	 * session database for a given context. For proper results, data should have been saved to the
	 * session using the {@link #persistMethodToSession(Session, int)} method.
	 * 
	 * @throws DatabaseException if an error occurred while reading from the database
	 */
	public static BasicAuthorizationDetectionMethod loadMethodFromSession(Session session, int contextId)
			throws DatabaseException {

		int statusCode = NO_STATUS_CODE;
		try {
			List<String> statusCodeL = session.getContextDataStrings(contextId,
					RecordContext.TYPE_AUTHORIZATION_METHOD_FIELD_1);
			statusCode = Integer.parseInt(statusCodeL.get(0));
		} catch (NullPointerException | IndexOutOfBoundsException | NumberFormatException ex) {
			// There was no valid data so use the defaults
		}

		String headerRegex = null;
		try {
			List<String> loadedData = session.getContextDataStrings(contextId,
					RecordContext.TYPE_AUTHORIZATION_METHOD_FIELD_2);
			headerRegex = loadedData.get(0);
		} catch (NullPointerException | IndexOutOfBoundsException ex) {
			// There was no valid data so use the defaults
		}

		String bodyRegex = null;
		try {
			List<String> loadedData = session.getContextDataStrings(contextId,
					RecordContext.TYPE_AUTHORIZATION_METHOD_FIELD_3);
			bodyRegex = loadedData.get(0);
		} catch (NullPointerException | IndexOutOfBoundsException ex) {
			// There was no valid data so use the defaults
		}

		LogicalOperator operator = LogicalOperator.OR;
		try {
			List<String> loadedData = session.getContextDataStrings(contextId,
					RecordContext.TYPE_AUTHORIZATION_METHOD_FIELD_4);
			operator = LogicalOperator.valueOf(loadedData.get(0));
		} catch (NullPointerException | IndexOutOfBoundsException | IllegalArgumentException ex) {
			// There was no valid data so use the defaults
		}

		return new BasicAuthorizationDetectionMethod(statusCode, headerRegex, bodyRegex, operator);
	}

	@Override
	public void exportMethodData(Configuration config) {
		config.setProperty(CONTEXT_CONFIG_AUTH_BASIC_HEADER, getPatternString(this.headerPattern));
		config.setProperty(CONTEXT_CONFIG_AUTH_BASIC_BODY, getPatternString(this.bodyPattern));
		config.setProperty(CONTEXT_CONFIG_AUTH_BASIC_LOGIC, this.logicalOperator.name());
		config.setProperty(CONTEXT_CONFIG_AUTH_BASIC_CODE, this.statusCode);
	}

	@Override
	public ApiResponse getApiResponseRepresentation() {
		Map<String, String> values = new HashMap<>();
		values.put(AuthorizationAPI.PARAM_HEADER_REGEX, headerPattern == null ? "" : headerPattern.pattern());
		values.put(AuthorizationAPI.PARAM_BODY_REGEX, bodyPattern == null ? "" : bodyPattern.pattern());
		values.put(AuthorizationAPI.PARAM_STATUS_CODE, Integer.toString(this.statusCode));
		values.put(AuthorizationAPI.PARAM_LOGICAL_OPERATOR, this.logicalOperator.name());
		values.put(AuthorizationAPI.RESPONSE_TYPE, "basic");
		return new ApiResponseSet(AuthorizationAPI.RESPONSE_TAG, values);
	}
}
