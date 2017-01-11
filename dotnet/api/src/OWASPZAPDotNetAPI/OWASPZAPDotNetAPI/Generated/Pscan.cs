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
	public class Pscan 
	{
		private ClientApi api = null;

		public Pscan(ClientApi api) 
		{
			this.api = api;
		}

		/// <summary>
		///The number of records the passive scanner still has to scan
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse recordsToScan()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("pscan", "view", "recordsToScan", parameters);
		}

		/// <summary>
		///Lists all passive scanners with its ID, name, enabled state and alert threshold.
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse scanners()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("pscan", "view", "scanners", parameters);
		}

		/// <summary>
		///Sets whether or not the passive scanning is enabled
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setEnabled(string apikey, string enabled)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("enabled", enabled);
			return api.CallApi("pscan", "action", "setEnabled", parameters);
		}

		/// <summary>
		///Enables all passive scanners
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse enableAllScanners(string apikey)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			return api.CallApi("pscan", "action", "enableAllScanners", parameters);
		}

		/// <summary>
		///Disables all passive scanners
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse disableAllScanners(string apikey)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			return api.CallApi("pscan", "action", "disableAllScanners", parameters);
		}

		/// <summary>
		///Enables all passive scanners with the given IDs (comma separated list of IDs)
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse enableScanners(string apikey, string ids)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("ids", ids);
			return api.CallApi("pscan", "action", "enableScanners", parameters);
		}

		/// <summary>
		///Disables all passive scanners with the given IDs (comma separated list of IDs)
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse disableScanners(string apikey, string ids)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("ids", ids);
			return api.CallApi("pscan", "action", "disableScanners", parameters);
		}

		/// <summary>
		///Sets the alert threshold of the passive scanner with the given ID, accepted values for alert threshold: OFF, DEFAULT, LOW, MEDIUM and HIGH
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setScannerAlertThreshold(string apikey, string id, string alertthreshold)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("id", id);
			parameters.Add("alertThreshold", alertthreshold);
			return api.CallApi("pscan", "action", "setScannerAlertThreshold", parameters);
		}

	}
}
