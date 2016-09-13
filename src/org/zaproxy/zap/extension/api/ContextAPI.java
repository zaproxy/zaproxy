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
package org.zaproxy.zap.extension.api;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.authentication.AuthenticationMethod;
import org.zaproxy.zap.authentication.AuthenticationMethodType;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiException.Type;
import org.zaproxy.zap.extension.authorization.AuthorizationDetectionMethod;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.Tech;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.utils.ApiUtils;

public class ContextAPI extends ApiImplementor {

    private static final Logger log = Logger.getLogger(ContextAPI.class);

    private static final String PREFIX = "context";
    private static final String TECH_NAME = "technologyName";
    private static final String EXCLUDE_FROM_CONTEXT_REGEX = "excludeFromContext";
    private static final String INCLUDE_IN_CONTEXT_REGEX = "includeInContext";
    private static final String ACTION_NEW_CONTEXT = "newContext";
    private static final String ACTION_REMOVE_CONTEXT = "removeContext";
    private static final String ACTION_SET_CONTEXT_IN_SCOPE = "setContextInScope";
    private static final String ACTION_EXPORT_CONTEXT = "exportContext";
    private static final String ACTION_IMPORT_CONTEXT = "importContext";
    private static final String ACTION_INCLUDE_TECHS = "includeContextTechnologies";
	private static final String ACTION_INCLUDE_ALL_TECHS = "includeAllContextTechnologies";
	private static final String ACTION_EXCLUDE_TECHS = "excludeContextTechnologies";
	private static final String ACTION_EXCLUDE_ALL_TECHS = "excludeAllContextTechnologies";
    private static final String VIEW_EXCLUDE_REGEXS = "excludeRegexs";
    private static final String VIEW_INCLUDE_REGEXS = "includeRegexs";
    private static final String VIEW_CONTEXT_LIST = "contextList";
    private static final String VIEW_CONTEXT = "context";
    private static final String VIEW_ALL_TECHS = "technologyList";
	private static final String VIEW_INCLUDED_TECHS = "includedTechnologyList";
	private static final String VIEW_EXCLUDED_TECHS = "excludedTechnologyList";
    private static final String REGEX_PARAM = "regex";
    private static final String CONTEXT_NAME = "contextName";
    private static final String IN_SCOPE = "booleanInScope";
    private static final String CONTEXT_FILE_PARAM = "contextFile";
    private static final String CONTEXT_ID = "contextId";
    private static final String PARAM_TECH_NAMES = "technologyNames";

    public ContextAPI() {
        List<String> contextNameAndRegexParam = new ArrayList<>(2);
        contextNameAndRegexParam.add(CONTEXT_NAME);
        contextNameAndRegexParam.add(REGEX_PARAM);
        List<String> contextNameOnlyParam = new ArrayList<>(1);
        contextNameOnlyParam.add((CONTEXT_NAME));
        String[] contextNameAndTechNames = new String[] { CONTEXT_NAME, PARAM_TECH_NAMES };

        this.addApiAction(new ApiAction(EXCLUDE_FROM_CONTEXT_REGEX, contextNameAndRegexParam));
        this.addApiAction(new ApiAction(INCLUDE_IN_CONTEXT_REGEX, contextNameAndRegexParam));
        this.addApiAction(new ApiAction(ACTION_NEW_CONTEXT, contextNameOnlyParam));
        this.addApiAction(new ApiAction(ACTION_REMOVE_CONTEXT, contextNameOnlyParam));
        this.addApiAction(new ApiAction(ACTION_EXPORT_CONTEXT, new String[] {CONTEXT_NAME, CONTEXT_FILE_PARAM}, null));
        this.addApiAction(new ApiAction(ACTION_IMPORT_CONTEXT, new String[] {CONTEXT_FILE_PARAM}, null));
        this.addApiAction(new ApiAction(ACTION_INCLUDE_TECHS, contextNameAndTechNames));
		this.addApiAction(new ApiAction(ACTION_INCLUDE_ALL_TECHS, contextNameOnlyParam));
		this.addApiAction(new ApiAction(ACTION_EXCLUDE_TECHS, contextNameAndTechNames));
		this.addApiAction(new ApiAction(ACTION_EXCLUDE_ALL_TECHS, contextNameOnlyParam));
		
        List<String> contextInScopeParams = new ArrayList<>(2);
        contextInScopeParams.add(CONTEXT_NAME);
        contextInScopeParams.add(IN_SCOPE);
        this.addApiAction(new ApiAction(ACTION_SET_CONTEXT_IN_SCOPE, contextInScopeParams));

        this.addApiView(new ApiView(VIEW_CONTEXT_LIST));
        this.addApiView(new ApiView(VIEW_EXCLUDE_REGEXS, contextNameOnlyParam));
        this.addApiView(new ApiView(VIEW_INCLUDE_REGEXS, contextNameOnlyParam));
        this.addApiView(new ApiView(VIEW_CONTEXT, contextNameOnlyParam));
        this.addApiView(new ApiView(VIEW_ALL_TECHS));
		this.addApiView(new ApiView(VIEW_INCLUDED_TECHS, contextNameOnlyParam));
		this.addApiView(new ApiView(VIEW_EXCLUDED_TECHS, contextNameOnlyParam));
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
        log.debug("handleApiAction " + name + " " + params.toString());
        
        Context context;
        TechSet techSet;
        String[] techNames;
        String filename;
        File f;
        
        switch(name) {
        case EXCLUDE_FROM_CONTEXT_REGEX:
        	try {
				addExcludeToContext(getContext(params), params.getString(REGEX_PARAM));
			} catch (IllegalArgumentException e) {
	            throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, REGEX_PARAM, e);
			}
        	break;
        case INCLUDE_IN_CONTEXT_REGEX:
            try {
                addIncludeToContext(getContext(params), params.getString(REGEX_PARAM));
            } catch (IllegalArgumentException e) {
                throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, REGEX_PARAM, e);
            }
        	break;
        case ACTION_NEW_CONTEXT:
            context = Model.getSingleton().getSession().getNewContext(params.getString(CONTEXT_NAME));
            Model.getSingleton().getSession().saveContext(context);
            return new ApiResponseElement(CONTEXT_ID, String.valueOf(context.getIndex()));
        case ACTION_REMOVE_CONTEXT:
        	context = getContext(params);
        	Model.getSingleton().getSession().deleteContext(context);
        	break;
        case ACTION_SET_CONTEXT_IN_SCOPE: 
            context = getContext(params);
            context.setInScope(params.getBoolean(IN_SCOPE));
            Model.getSingleton().getSession().saveContext(context);
            break;
        case ACTION_IMPORT_CONTEXT:
        	filename = params.getString(CONTEXT_FILE_PARAM);
            f = new File(filename);
            if (! f.exists()) {
            	// Try relative to the contexts dir
            	f = new File(Constant.getContextsDir(), filename);
            }
            if (! f.exists()) {
	            throw new ApiException(ApiException.Type.DOES_NOT_EXIST, f.getAbsolutePath());
            } else {
            	try {
					context = Model.getSingleton().getSession().importContext(f);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
		            throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
				}
            }
            return new ApiResponseElement(CONTEXT_ID, String.valueOf(context.getIndex()));
        case ACTION_EXPORT_CONTEXT:
        	filename = params.getString(CONTEXT_FILE_PARAM);
            context = getContext(params);
            
            f = new File(filename);
            if (! f.getAbsolutePath().equals(filename)) {
            	// Not an absolute filename, use one relative to the contexts dir
            	f = new File(Constant.getContextsDir(), filename);
            }
            if (! f.getParentFile().canWrite()) {
            	// Cant write to the parent dir so not looking good
	            throw new ApiException(ApiException.Type.NO_ACCESS, f.getAbsolutePath());
            } else {
            	try {
					Model.getSingleton().getSession().exportContext(context, f);
				} catch (Exception e) {
		            throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
				}
            }
            break;
        case ACTION_INCLUDE_TECHS:
        	context = getContext(params);
        	techSet = context.getTechSet();
        	techNames = getParam(params, PARAM_TECH_NAMES, "").split(",");
        	for (String techName : techNames) {
        		techSet.include(getTech(techName));
        	}
        	context.save();
        	break;
        case ACTION_INCLUDE_ALL_TECHS:
        	context = getContext(params);
        	techSet = new TechSet(Tech.builtInTech);
        	context.setTechSet(techSet);
        	context.save();
        	break;
        case ACTION_EXCLUDE_TECHS:
        	context = getContext(params);
        	techSet = context.getTechSet();
        	techNames = getParam(params, PARAM_TECH_NAMES, "").split(",");
        	for(String techName : techNames) {
        		techSet.exclude(getTech(techName));
        	}
        	context.save();
        	break;
        case ACTION_EXCLUDE_ALL_TECHS:
        	context = getContext(params);
        	techSet = context.getTechSet();
        	for (Tech tech : Tech.builtInTech) {
        		techSet.exclude(tech);
        	}
        	context.save();
        	break;
        default:
            throw new ApiException(Type.BAD_ACTION);
        }
        
        return ApiResponseElement.OK;
    }

    private void addExcludeToContext(Context context, String regex) {
    	List<String> incRegexes = new ArrayList<String>(context.getIncludeInContextRegexs());
    	if (incRegexes.remove(regex)) {
    		// Its already explicitly included, removing it from the include list is safer and more useful
    		context.setIncludeInContextRegexs(incRegexes);
    	} else {
            context.addExcludeFromContextRegex(regex);
    	}
        Model.getSingleton().getSession().saveContext(context);
    }

    private void addIncludeToContext(Context context, String regex) {
        context.addIncludeInContextRegex(regex);
        Model.getSingleton().getSession().saveContext(context);
    }

    @Override
    public ApiResponse handleApiView(String name, JSONObject params)
            throws ApiException {
    	log.debug("handleApiView " + name + " " + params.toString());
    	
        ApiResponse result;
        ApiResponseList resultList;
        TechSet techSet;
        
        switch(name) {
        case VIEW_EXCLUDE_REGEXS:
        	result = new ApiResponseElement(name, getContext(params).getExcludeFromContextRegexs().toString());
        	break;
        case VIEW_INCLUDE_REGEXS:
        	result = new ApiResponseElement(name, getContext(params).getIncludeInContextRegexs().toString());
        	break;
        case VIEW_CONTEXT_LIST:
        	List<String> contextNames = new ArrayList<>();
            List<Context> contexts = Model.getSingleton().getSession().getContexts();
            for (Context context : contexts){
                contextNames.add(context.getName());
            }
            result = new ApiResponseElement(name, contextNames.toString());
            break;
        case VIEW_CONTEXT:
        	result = new ApiResponseElement(buildResponseFromContext(getContext(params)));
        	break;
        case VIEW_ALL_TECHS:
        	resultList = new ApiResponseList(name);
        	for(Tech tech : Tech.builtInTech) {
        		resultList.addItem(new ApiResponseElement(TECH_NAME, tech.toString()));
        	}
        	result = resultList;
        	break;
        case VIEW_INCLUDED_TECHS:
        	resultList = new ApiResponseList(name);
        	techSet = getContext(params).getTechSet();
        	for(Tech tech : techSet.getIncludeTech()) {
        		resultList.addItem(new ApiResponseElement(TECH_NAME, tech.toString()));
        	}
        	result = resultList;
        	break;
		case VIEW_EXCLUDED_TECHS:
			resultList = new ApiResponseList(name);
			techSet = getContext(params).getTechSet();
			for(Tech tech : techSet.getExcludeTech()) {
				resultList.addItem(new ApiResponseElement(TECH_NAME, tech.toString()));
			}
			result = resultList;
			break;
        default:
        	throw new ApiException(Type.BAD_VIEW);
        }
        return result;
    }

    /**
     * Returns the {@code Context} with the given name. The context's name is obtained from the given parameters, whose name is
     * {@value #CONTEXT_NAME}.
     * <p>
     * The parameter must exist, that is, it should be a mandatory parameter, otherwise a runtime exception is thrown.
     *
     * @param params the parameters that contain the context's name
     * @return the {@code Context} with the given name
     * @throws ApiException If the context with the given name does not exist
     * @see JSONObject#getString(String)
     */
    private Context getContext(JSONObject params) throws ApiException {
        return ApiUtils.getContextByName(params, CONTEXT_NAME);
    }

    /**
	 * Builds the response describing an Context.
	 * 
	 * @param c the context
	 * @return the api response
	 */
	private ApiResponse buildResponseFromContext(Context c) {
		Map<String, String> fields = new HashMap<>();
		fields.put("name", c.getName());
		fields.put("id", Integer.toString(c.getIndex()));
		fields.put("description", c.getDescription());
		fields.put("inScope", Boolean.toString(c.isInScope()));
		fields.put("excludeRegexs", c.getExcludeFromContextRegexs().toString());
		fields.put("includeRegexs", c.getIncludeInContextRegexs().toString());
		
		AuthenticationMethod authenticationMethod = c.getAuthenticationMethod();
		if(authenticationMethod != null){
			Pattern pattern = authenticationMethod.getLoggedInIndicatorPattern();
			fields.put("loggedInPattern",  pattern == null ? "" : pattern.toString());
			pattern = authenticationMethod.getLoggedOutIndicatorPattern();
			fields.put("loggedOutPattern", pattern == null ? "" : pattern.toString());
			AuthenticationMethodType type = authenticationMethod.getType();
			fields.put("authType", type == null ? "" : type.getName());
		}
		
		AuthorizationDetectionMethod authorizationDetectionMethod = c.getAuthorizationDetectionMethod();
		if(authorizationDetectionMethod != null){
			fields.put("authenticationDetectionMethodId",String.valueOf(authorizationDetectionMethod.getMethodUniqueIdentifier()));
		}
		
		fields.put("urlParameterParserClass", c.getUrlParamParser().getClass().getCanonicalName());
		fields.put("urlParameterParserConfig", c.getUrlParamParser().getConfig());
		fields.put("postParameterParserClass", c.getPostParamParser().getClass().getCanonicalName());
		fields.put("postParameterParserConfig", c.getPostParamParser().getConfig());
		
		return new ApiResponseSet("context", fields);
	}
	
	/**
	 * Gets the tech that matches the techName
	 * or throws an exception if no tech matches
	 * 
	 * @param techName the name of the tech
	 * @return the matching tech
	 * @throws ApiException the api exception
	 */
	private Tech getTech(String techName) throws ApiException {
		for(Tech tech : Tech.builtInTech) {
			if (tech.toString().equalsIgnoreCase(techName))
				return tech;
		}
		throw new ApiException(Type.ILLEGAL_PARAMETER, 
				"The tech " + techName + " does not exist");
	}
}
