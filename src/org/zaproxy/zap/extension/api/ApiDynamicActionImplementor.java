package org.zaproxy.zap.extension.api;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

/**
 * The ApiDynamicActionImplementor is used for actions that are loaded dynamically.
 */
public abstract class ApiDynamicActionImplementor extends ApiElement {

	/**
	 * Instantiates a new api dynamic action implementor.
	 * 
	 * @param name the name
	 * @param mandatoryParamNames the mandatory param names, or <code>null</code> if there are no
	 *            mandatory parameters
	 * @param optionalParamNames the optional param names, or <code>null</code> if there are no
	 *            optional parameters
	 */
	public ApiDynamicActionImplementor(String name, String[] mandatoryParamNames, String[] optionalParamNames) {
		super(name, mandatoryParamNames, optionalParamNames);
	}

	/**
	 * Handle the execution of the action.
	 * 
	 * @param params the params
	 * @throws ApiException the api exception
	 */
	public abstract void handleAction(JSONObject params) throws ApiException;

	/**
	 * Builds an {@link ApiResponse} describing the parameters of this action.
	 * 
	 * @param paramName the param name
	 * @param mandatory the mandatory
	 * @return the api response set
	 */
	public ApiResponse buildParamsDescription() {
		ApiResponseList configParams = new ApiResponseList("methodConfigParams");
		for (String param : this.getMandatoryParamNames())
			configParams.addItem(buildParamMap(param, true));
		for (String param : this.getOptionalParamNames())
			configParams.addItem(buildParamMap(param, false));
		return configParams;
	}

	private static ApiResponseSet buildParamMap(String paramName, boolean mandatory) {
		Map<String, String> m = new HashMap<>();
		m.put("name", paramName);
		m.put("mandatory", mandatory ? "true" : "false");
		return new ApiResponseSet("param", m);
	}

}
