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
	public class AjaxSpider 
	{
		private ClientApi api = null;

		public AjaxSpider(ClientApi api) 
		{
			this.api = api;
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse status()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ajaxSpider", "view", "status", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse results(string start, string count)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			parameters.Add("start", start);
			parameters.Add("count", count);
			return api.CallApi("ajaxSpider", "view", "results", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse numberOfResults()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ajaxSpider", "view", "numberOfResults", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse scan(string apikey, string url, string inscope)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("url", url);
			parameters.Add("inScope", inscope);
			return api.CallApi("ajaxSpider", "action", "scan", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse stop(string apikey)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			return api.CallApi("ajaxSpider", "action", "stop", parameters);
		}

	}
}
