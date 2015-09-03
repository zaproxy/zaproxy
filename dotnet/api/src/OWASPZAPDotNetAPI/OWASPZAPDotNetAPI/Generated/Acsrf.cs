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
	public class Acsrf 
	{
		private ClientApi api = null;

		public Acsrf(ClientApi api) 
		{
			this.api = api;
		}

		/// <summary>
		///Lists the names of all anti CSRF tokens
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse optionTokensNames()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("acsrf", "view", "optionTokensNames", parameters);
		}

		/// <summary>
		///Removes the anti CSRF token with the given name
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse removeOptionToken(string apikey, string str)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("String", str);
			return api.CallApi("acsrf", "action", "removeOptionToken", parameters);
		}

		/// <summary>
		///Adds an anti CSRF token with the given name, enabled by default
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse addOptionToken(string apikey, string str)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("String", str);
			return api.CallApi("acsrf", "action", "addOptionToken", parameters);
		}

		/// <summary>
		///Generate a form for testing lack of anti CSRF tokens - typically invoked via ZAP
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public byte[] genForm(string apikey, string hrefid)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("hrefId", hrefid);
			return api.CallApiOther("acsrf", "other", "genForm", parameters);
		}

	}
}
