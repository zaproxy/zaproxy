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
	public class HttpSessions 
	{
		private ClientApi api = null;

		public HttpSessions(ClientApi api) 
		{
			this.api = api;
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse sessions(string site, string session)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			parameters.Add("site", site);
			parameters.Add("session", session);
			return api.CallApi("httpSessions", "view", "sessions", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse activeSession(string site)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			parameters.Add("site", site);
			return api.CallApi("httpSessions", "view", "activeSession", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse sessionTokens(string site)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			parameters.Add("site", site);
			return api.CallApi("httpSessions", "view", "sessionTokens", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse createEmptySession(string apikey, string site, string session)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("site", site);
			parameters.Add("session", session);
			return api.CallApi("httpSessions", "action", "createEmptySession", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse removeSession(string apikey, string site, string session)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("site", site);
			parameters.Add("session", session);
			return api.CallApi("httpSessions", "action", "removeSession", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setActiveSession(string apikey, string site, string session)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("site", site);
			parameters.Add("session", session);
			return api.CallApi("httpSessions", "action", "setActiveSession", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse unsetActiveSession(string apikey, string site)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("site", site);
			return api.CallApi("httpSessions", "action", "unsetActiveSession", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse addSessionToken(string apikey, string site, string sessiontoken)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("site", site);
			parameters.Add("sessionToken", sessiontoken);
			return api.CallApi("httpSessions", "action", "addSessionToken", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse removeSessionToken(string apikey, string site, string sessiontoken)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("site", site);
			parameters.Add("sessionToken", sessiontoken);
			return api.CallApi("httpSessions", "action", "removeSessionToken", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setSessionTokenValue(string apikey, string site, string session, string sessiontoken, string tokenvalue)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("site", site);
			parameters.Add("session", session);
			parameters.Add("sessionToken", sessiontoken);
			parameters.Add("tokenValue", tokenvalue);
			return api.CallApi("httpSessions", "action", "setSessionTokenValue", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse renameSession(string apikey, string site, string oldsessionname, string newsessionname)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("site", site);
			parameters.Add("oldSessionName", oldsessionname);
			parameters.Add("newSessionName", newsessionname);
			return api.CallApi("httpSessions", "action", "renameSession", parameters);
		}

	}
}
