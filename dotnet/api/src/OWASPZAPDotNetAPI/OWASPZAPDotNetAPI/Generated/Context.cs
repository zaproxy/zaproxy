/* Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright the ZAP development team
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



using System;
using System.Collections.Generic;
using System.Text;


/*
 * This file was automatically generated.
 */
namespace OWASPZAPDotNetAPI.Generated
{
	public class Context 
	{
		private ClientApi api = null;

		public Context(ClientApi api) 
		{
			this.api = api;
		}

		/// <summary>
		///List context names of current session
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse contextList()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("context", "view", "contextList", parameters);
		}

		/// <summary>
		///List excluded regexs for context
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse excludeRegexs(string contextname)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			parameters.Add("contextName", contextname);
			return api.CallApi("context", "view", "excludeRegexs", parameters);
		}

		/// <summary>
		///List included regexs for context
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse includeRegexs(string contextname)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			parameters.Add("contextName", contextname);
			return api.CallApi("context", "view", "includeRegexs", parameters);
		}

		/// <summary>
		///List the information about the named context
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse context(string contextname)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			parameters.Add("contextName", contextname);
			return api.CallApi("context", "view", "context", parameters);
		}

		/// <summary>
		///Add exclude regex to context
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse excludeFromContext(string apikey, string contextname, string regex)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("contextName", contextname);
			parameters.Add("regex", regex);
			return api.CallApi("context", "action", "excludeFromContext", parameters);
		}

		/// <summary>
		///Add include regex to context
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse includeInContext(string apikey, string contextname, string regex)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("contextName", contextname);
			parameters.Add("regex", regex);
			return api.CallApi("context", "action", "includeInContext", parameters);
		}

		/// <summary>
		///Creates a new context in the current session
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse newContext(string apikey, string contextname)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("contextName", contextname);
			return api.CallApi("context", "action", "newContext", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse exportContext(string apikey, string contextname, string contextfile)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("contextName", contextname);
			parameters.Add("contextFile", contextfile);
			return api.CallApi("context", "action", "exportContext", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse importContext(string apikey, string contextfile)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("contextFile", contextfile);
			return api.CallApi("context", "action", "importContext", parameters);
		}

		/// <summary>
		///Sets a context to in scope (contexts are in scope by default)
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setContextInScope(string apikey, string contextname, string booleaninscope)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("contextName", contextname);
			parameters.Add("booleanInScope", booleaninscope);
			return api.CallApi("context", "action", "setContextInScope", parameters);
		}

	}
}
