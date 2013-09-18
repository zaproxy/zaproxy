package org.zaproxy.zap.extension.api;

import net.sf.json.JSONObject;

/**
 * The ApiDynamicActionImplementor is used for actions that are loaded dynamically.
 */
public abstract class ApiDynamicActionImplementor extends ApiElement {

	/**
	 * Instantiates a new api dynamic action implementor.
	 * 
	 * @param name the name
	 * @param mandatoryParamNames the mandatory param names
	 * @param optionalParamNames the optional param names
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

}
