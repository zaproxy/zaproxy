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
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.model.Context;

public class ContextAPI extends ApiImplementor {

    private static Logger log = Logger.getLogger(ContextAPI.class);

    private static final String PREFIX = "context";
    private static final String EXCLUDE_FROM_CONTEXT_REGEX = "excludeFromContext";
    private static final String INCLUDE_IN_CONTEXT_REGEX = "includeInContext";
    private static final String ACTION_NEW_CONTEXT = "newContext";
    private static final String ACTION_SET_CONTEXT_IN_SCOPE = "setContextInScope";
    private static final String ACTION_EXPORT_CONTEXT = "exportContext";
    private static final String ACTION_IMPORT_CONTEXT = "importContext";
    private static final String VIEW_EXCLUDE_REGEXS = "excludeRegexs";
    private static final String VIEW_INCLUDE_REGEXS = "includeRegexs";
    private static final String VIEW_CONTEXT_LIST = "contextList";
    private static final String REGEX_PARAM = "regex";
    private static final String CONTEXT_NAME = "contextName";
    private static final String IN_SCOPE = "booleanInScope";
    private static final String CONTEXT_FILE_PARAM = "contextFile";

    public ContextAPI() {
        List<String> contextNameAndRegexParam = new ArrayList<>(2);
        contextNameAndRegexParam.add(CONTEXT_NAME);
        contextNameAndRegexParam.add(REGEX_PARAM);
        List<String> contextNameOnlyParam = new ArrayList<>(1);
        contextNameOnlyParam.add((CONTEXT_NAME));

        this.addApiAction(new ApiAction(EXCLUDE_FROM_CONTEXT_REGEX, contextNameAndRegexParam));
        this.addApiAction(new ApiAction(INCLUDE_IN_CONTEXT_REGEX, contextNameAndRegexParam));
        this.addApiAction(new ApiAction(ACTION_NEW_CONTEXT, null, contextNameOnlyParam));
        this.addApiAction(new ApiAction(ACTION_EXPORT_CONTEXT, new String[] {CONTEXT_NAME, CONTEXT_FILE_PARAM}, null));
        this.addApiAction(new ApiAction(ACTION_IMPORT_CONTEXT, new String[] {CONTEXT_FILE_PARAM}, null));

        List<String> contextInScopeParams = new ArrayList<>(2);
        contextInScopeParams.add(CONTEXT_NAME);
        contextInScopeParams.add(IN_SCOPE);
        this.addApiAction(new ApiAction(ACTION_SET_CONTEXT_IN_SCOPE, contextInScopeParams));

        this.addApiView(new ApiView(VIEW_CONTEXT_LIST));
        this.addApiView(new ApiView(VIEW_EXCLUDE_REGEXS, contextNameOnlyParam));
        this.addApiView(new ApiView(VIEW_INCLUDE_REGEXS, contextNameOnlyParam));
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
        log.debug("handleApiAction " + name + " " + params.toString());
        if (EXCLUDE_FROM_CONTEXT_REGEX.equals(name)) {
            addExcludeToContext(getContext(params), getRegex(params));
        } else if (INCLUDE_IN_CONTEXT_REGEX.equals(name)) {
            addIncludeToContext(getContext(params), getRegex(params));
        } else if (ACTION_NEW_CONTEXT.equals(name)){
            Context context = Model.getSingleton().getSession().getNewContext();
            String contextName = params.getString(CONTEXT_NAME);
            if (contextName != null && contextName.length() > 0){
                context.setName(contextName);
                Model.getSingleton().getSession().saveContext(context);
            }
        } else if (ACTION_SET_CONTEXT_IN_SCOPE.equals(name)){
            if (params.getString(IN_SCOPE) == null || params.getString(IN_SCOPE).length()==0){
                throw new ApiException(ApiException.Type.MISSING_PARAMETER, IN_SCOPE);
            }
            getContext(params).setInScope(params.getBoolean(IN_SCOPE));
            Model.getSingleton().getSession().saveContext(getContext(params));
        } else if (ACTION_IMPORT_CONTEXT.equals(name)){
            String filename = params.getString(CONTEXT_FILE_PARAM);
            File f = new File(filename);
            if (! f.exists()) {
            	// Try relative to the contexts dir
            	f = new File(Constant.getContextsDir(), filename);
            }
            if (! f.exists()) {
	            throw new ApiException(ApiException.Type.DOES_NOT_EXIST, f.getAbsolutePath());
            } else {
            	try {
					Model.getSingleton().getSession().importContext(f);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
		            throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
				}
            }
        } else if (ACTION_EXPORT_CONTEXT.equals(name)){
            String filename = params.getString(CONTEXT_FILE_PARAM);
            String ctxname = params.getString(CONTEXT_NAME);
            
            Context ctx = Model.getSingleton().getSession().getContext(ctxname);
            if (ctx == null) {
	            throw new ApiException(ApiException.Type.DOES_NOT_EXIST, ctxname);
            }
            
            File f = new File(filename);
            if (! f.getAbsolutePath().equals(filename)) {
            	// Not an absolute filename, use one relative to the contexts dir
            	f = new File(Constant.getContextsDir(), filename);
            }
            if (! f.getParentFile().canWrite()) {
            	// Cant write to the parent dir so not looking good
	            throw new ApiException(ApiException.Type.NO_ACCESS, f.getAbsolutePath());
            } else {
            	try {
					Model.getSingleton().getSession().exportContext(ctx, f);
				} catch (Exception e) {
		            throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
				}
            }
        } else {
            throw new ApiException(ApiException.Type.BAD_ACTION);
        }
        return ApiResponseElement.OK;
    }

    private String getRegex(JSONObject params) throws ApiException {
        String regex = params.getString(REGEX_PARAM);
        if (regex == null || regex.length() == 0) {
            throw new ApiException(ApiException.Type.MISSING_PARAMETER, REGEX_PARAM);
        }
        return regex;
    }

    private void addExcludeToContext(Context context, String regex) {
        context.addExcludeFromContextRegex(regex);
        Model.getSingleton().getSession().saveContext(context);
    }

    private void addIncludeToContext(Context context, String regex) {
        context.addIncludeInContextRegex(regex);
        Model.getSingleton().getSession().saveContext(context);
    }

    @Override
    public ApiResponse handleApiView(String name, JSONObject params)
            throws ApiException {
        ApiResponseElement result = new ApiResponseElement(name, "");
        if (VIEW_EXCLUDE_REGEXS.equals(name)){
            result = new ApiResponseElement(name, getContext(params).getExcludeFromContextRegexs().toString());
        } else if(VIEW_INCLUDE_REGEXS.equals(name)){
            result = new ApiResponseElement(name, getContext(params).getIncludeInContextRegexs().toString());
        } else if (VIEW_CONTEXT_LIST.equals(name)){
            List<String> contextNames = new ArrayList<>();
            List<Context> contexts = Model.getSingleton().getSession().getContexts();
            for (Context context : contexts){
                contextNames.add(context.getName());
            }
            result = new ApiResponseElement(name, contextNames.toString());
        }
        return result;
    }

    private Context getContext(JSONObject params) throws ApiException {
        return getContext(getContextName(params));
    }

    private String getContextName(JSONObject params) throws ApiException {
        String contextName = params.getString(CONTEXT_NAME);
        if (contextName == null || contextName.length() == 0){
            throw new ApiException(ApiException.Type.MISSING_PARAMETER, CONTEXT_NAME);
        }
        return contextName;
    }

    private Context getContext(String contextName) throws ApiException {
        List<Context> contexts = Model.getSingleton().getSession().getContexts();
        for (Context context : contexts){
            if (context.getName().equals(contextName)){
                return context;
            }
        }
        throw new ApiException(ApiException.Type.DOES_NOT_EXIST, contextName);
    }

}
