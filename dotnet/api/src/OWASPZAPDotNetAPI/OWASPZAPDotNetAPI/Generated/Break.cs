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
	public class Break 
	{
		private ClientApi api = null;

		public Break(ClientApi api) 
		{
			this.api = api;
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse brk(string apikey, string type, string scope, string state)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("type", type);
			parameters.Add("scope", scope);
			parameters.Add("state", state);
			return api.CallApi("break", "action", "break", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse addHttpBreakpoint(string apikey, string str, string location, string match, string inverse, string ignorecase)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("string", str);
			parameters.Add("location", location);
			parameters.Add("match", match);
			parameters.Add("inverse", inverse);
			parameters.Add("ignorecase", ignorecase);
			return api.CallApi("break", "action", "addHttpBreakpoint", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse removeHttpBreakpoint(string apikey, string str, string location, string match, string inverse, string ignorecase)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("string", str);
			parameters.Add("location", location);
			parameters.Add("match", match);
			parameters.Add("inverse", inverse);
			parameters.Add("ignorecase", ignorecase);
			return api.CallApi("break", "action", "removeHttpBreakpoint", parameters);
		}

	}
}
