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
	public class Ascan 
	{
		private ClientApi api = null;

		public Ascan(ClientApi api) 
		{
			this.api = api;
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse status(string scanid)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			parameters.Add("scanId", scanid);
			return api.CallApi("ascan", "view", "status", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse scanProgress(string scanid)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			parameters.Add("scanId", scanid);
			return api.CallApi("ascan", "view", "scanProgress", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse messagesIds(string scanid)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			parameters.Add("scanId", scanid);
			return api.CallApi("ascan", "view", "messagesIds", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse alertsIds(string scanid)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			parameters.Add("scanId", scanid);
			return api.CallApi("ascan", "view", "alertsIds", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse scans()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "scans", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse scanPolicyNames()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "scanPolicyNames", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse excludedFromScan()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "excludedFromScan", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse scanners(string scanpolicyname, string policyid)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			parameters.Add("scanPolicyName", scanpolicyname);
			parameters.Add("policyId", policyid);
			return api.CallApi("ascan", "view", "scanners", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse policies(string scanpolicyname, string policyid)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			parameters.Add("scanPolicyName", scanpolicyname);
			parameters.Add("policyId", policyid);
			return api.CallApi("ascan", "view", "policies", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse attackModeQueue()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "attackModeQueue", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse optionShowAdvancedDialog()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "optionShowAdvancedDialog", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse optionPromptToClearFinishedScans()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "optionPromptToClearFinishedScans", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse optionTargetParamsEnabledRPC()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "optionTargetParamsEnabledRPC", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse optionTargetParamsInjectable()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "optionTargetParamsInjectable", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse optionAllowAttackOnStart()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "optionAllowAttackOnStart", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse optionMaxScansInUI()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "optionMaxScansInUI", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse optionPromptInAttackMode()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "optionPromptInAttackMode", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse optionHandleAntiCSRFTokens()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "optionHandleAntiCSRFTokens", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse optionRescanInAttackMode()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "optionRescanInAttackMode", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse optionMaxResultsToList()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "optionMaxResultsToList", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse optionInjectPluginIdInHeader()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "optionInjectPluginIdInHeader", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse optionExcludedParamList()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "optionExcludedParamList", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse optionHostPerScan()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "optionHostPerScan", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse optionDelayInMs()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "optionDelayInMs", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse optionDefaultPolicy()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "optionDefaultPolicy", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse optionThreadPerHost()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "optionThreadPerHost", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse optionAttackPolicy()
		{
			Dictionary<string, string> parameters = null;
			return api.CallApi("ascan", "view", "optionAttackPolicy", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse scan(string apikey, string url, string recurse, string inscopeonly, string scanpolicyname, string method, string postdata)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("url", url);
			parameters.Add("recurse", recurse);
			parameters.Add("inScopeOnly", inscopeonly);
			parameters.Add("scanPolicyName", scanpolicyname);
			parameters.Add("method", method);
			parameters.Add("postData", postdata);
			return api.CallApi("ascan", "action", "scan", parameters);
		}

		/// <summary>
		///Active Scans from the perspective of an User, obtained using the given Context ID and User ID. See 'scan' action for more details.
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse scanAsUser(string apikey, string url, string contextid, string userid, string recurse, string scanpolicyname, string method, string postdata)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("url", url);
			parameters.Add("contextId", contextid);
			parameters.Add("userId", userid);
			parameters.Add("recurse", recurse);
			parameters.Add("scanPolicyName", scanpolicyname);
			parameters.Add("method", method);
			parameters.Add("postData", postdata);
			return api.CallApi("ascan", "action", "scanAsUser", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse pause(string apikey, string scanid)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("scanId", scanid);
			return api.CallApi("ascan", "action", "pause", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse resume(string apikey, string scanid)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("scanId", scanid);
			return api.CallApi("ascan", "action", "resume", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse stop(string apikey, string scanid)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("scanId", scanid);
			return api.CallApi("ascan", "action", "stop", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse removeScan(string apikey, string scanid)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("scanId", scanid);
			return api.CallApi("ascan", "action", "removeScan", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse pauseAllScans(string apikey)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			return api.CallApi("ascan", "action", "pauseAllScans", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse resumeAllScans(string apikey)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			return api.CallApi("ascan", "action", "resumeAllScans", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse stopAllScans(string apikey)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			return api.CallApi("ascan", "action", "stopAllScans", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse removeAllScans(string apikey)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			return api.CallApi("ascan", "action", "removeAllScans", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse clearExcludedFromScan(string apikey)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			return api.CallApi("ascan", "action", "clearExcludedFromScan", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse excludeFromScan(string apikey, string regex)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("regex", regex);
			return api.CallApi("ascan", "action", "excludeFromScan", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse enableAllScanners(string apikey, string scanpolicyname)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("scanPolicyName", scanpolicyname);
			return api.CallApi("ascan", "action", "enableAllScanners", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse disableAllScanners(string apikey, string scanpolicyname)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("scanPolicyName", scanpolicyname);
			return api.CallApi("ascan", "action", "disableAllScanners", parameters);
		}

		/// <summary>
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
			return api.CallApi("ascan", "action", "enableScanners", parameters);
		}

		/// <summary>
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
			return api.CallApi("ascan", "action", "disableScanners", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setEnabledPolicies(string apikey, string ids)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("ids", ids);
			return api.CallApi("ascan", "action", "setEnabledPolicies", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setPolicyAttackStrength(string apikey, string id, string attackstrength, string scanpolicyname)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("id", id);
			parameters.Add("attackStrength", attackstrength);
			parameters.Add("scanPolicyName", scanpolicyname);
			return api.CallApi("ascan", "action", "setPolicyAttackStrength", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setPolicyAlertThreshold(string apikey, string id, string alertthreshold, string scanpolicyname)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("id", id);
			parameters.Add("alertThreshold", alertthreshold);
			parameters.Add("scanPolicyName", scanpolicyname);
			return api.CallApi("ascan", "action", "setPolicyAlertThreshold", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setScannerAttackStrength(string apikey, string id, string attackstrength, string scanpolicyname)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("id", id);
			parameters.Add("attackStrength", attackstrength);
			parameters.Add("scanPolicyName", scanpolicyname);
			return api.CallApi("ascan", "action", "setScannerAttackStrength", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setScannerAlertThreshold(string apikey, string id, string alertthreshold, string scanpolicyname)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("id", id);
			parameters.Add("alertThreshold", alertthreshold);
			parameters.Add("scanPolicyName", scanpolicyname);
			return api.CallApi("ascan", "action", "setScannerAlertThreshold", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse addScanPolicy(string apikey, string scanpolicyname)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("scanPolicyName", scanpolicyname);
			return api.CallApi("ascan", "action", "addScanPolicy", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse removeScanPolicy(string apikey, string scanpolicyname)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("scanPolicyName", scanpolicyname);
			return api.CallApi("ascan", "action", "removeScanPolicy", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setOptionDefaultPolicy(string apikey, string str)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("String", str);
			return api.CallApi("ascan", "action", "setOptionDefaultPolicy", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setOptionAttackPolicy(string apikey, string str)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("String", str);
			return api.CallApi("ascan", "action", "setOptionAttackPolicy", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setOptionShowAdvancedDialog(string apikey, bool boolean)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("Boolean", Convert.ToString(boolean));
			return api.CallApi("ascan", "action", "setOptionShowAdvancedDialog", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setOptionAllowAttackOnStart(string apikey, bool boolean)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("Boolean", Convert.ToString(boolean));
			return api.CallApi("ascan", "action", "setOptionAllowAttackOnStart", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setOptionTargetParamsInjectable(string apikey, int i)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("Integer", Convert.ToString(i));
			return api.CallApi("ascan", "action", "setOptionTargetParamsInjectable", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setOptionPromptInAttackMode(string apikey, bool boolean)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("Boolean", Convert.ToString(boolean));
			return api.CallApi("ascan", "action", "setOptionPromptInAttackMode", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setOptionTargetParamsEnabledRPC(string apikey, int i)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("Integer", Convert.ToString(i));
			return api.CallApi("ascan", "action", "setOptionTargetParamsEnabledRPC", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setOptionPromptToClearFinishedScans(string apikey, bool boolean)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("Boolean", Convert.ToString(boolean));
			return api.CallApi("ascan", "action", "setOptionPromptToClearFinishedScans", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setOptionMaxScansInUI(string apikey, int i)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("Integer", Convert.ToString(i));
			return api.CallApi("ascan", "action", "setOptionMaxScansInUI", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setOptionInjectPluginIdInHeader(string apikey, bool boolean)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("Boolean", Convert.ToString(boolean));
			return api.CallApi("ascan", "action", "setOptionInjectPluginIdInHeader", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setOptionRescanInAttackMode(string apikey, bool boolean)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("Boolean", Convert.ToString(boolean));
			return api.CallApi("ascan", "action", "setOptionRescanInAttackMode", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setOptionHandleAntiCSRFTokens(string apikey, bool boolean)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("Boolean", Convert.ToString(boolean));
			return api.CallApi("ascan", "action", "setOptionHandleAntiCSRFTokens", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setOptionMaxResultsToList(string apikey, int i)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("Integer", Convert.ToString(i));
			return api.CallApi("ascan", "action", "setOptionMaxResultsToList", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setOptionDelayInMs(string apikey, int i)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("Integer", Convert.ToString(i));
			return api.CallApi("ascan", "action", "setOptionDelayInMs", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setOptionThreadPerHost(string apikey, int i)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("Integer", Convert.ToString(i));
			return api.CallApi("ascan", "action", "setOptionThreadPerHost", parameters);
		}

		/// <summary>
		///This component is optional and therefore the API will only work if it is installed
		/// </summary>
		/// <returns></returns>
		public IApiResponse setOptionHostPerScan(string apikey, int i)
		{
			Dictionary<string, string> parameters = null;
			parameters = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(apikey)){
				parameters.Add("apikey", apikey);
			}
			parameters.Add("Integer", Convert.ToString(i));
			return api.CallApi("ascan", "action", "setOptionHostPerScan", parameters);
		}

	}
}
