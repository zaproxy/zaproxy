<?php
/**
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 the ZAP development team
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


namespace Zap;


/**
 * This file was automatically generated.
 */
class ImportLogFiles {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function ImportZAPLogFromFile($filepath) {
		return $this->zap->request($this->zap->base . 'importLogFiles/view/ImportZAPLogFromFile/', array('FilePath' => $filepath))->{'ImportZAPLogFromFile'};
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function ImportModSecurityLogFromFile($filepath) {
		return $this->zap->request($this->zap->base . 'importLogFiles/view/ImportModSecurityLogFromFile/', array('FilePath' => $filepath))->{'ImportModSecurityLogFromFile'};
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function ImportZAPHttpRequestResponsePair($httprequest, $httpresponse) {
		return $this->zap->request($this->zap->base . 'importLogFiles/view/ImportZAPHttpRequestResponsePair/', array('HTTPRequest' => $httprequest, 'HTTPResponse' => $httpresponse))->{'ImportZAPHttpRequestResponsePair'};
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function PostModSecurityAuditEvent($auditeventstring=NULL, $apikey='') {
		$params = array('apikey' => $apikey);
		if ($auditeventstring !== NULL) {
			$params['AuditEventString'] = $auditeventstring;
		}
		return $this->zap->request($this->zap->base . 'importLogFiles/action/PostModSecurityAuditEvent/', $params);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public function OtherPostModSecurityAuditEvent($auditeventstring, $apikey='') {
		return $this->zap->requestother($this->zap->base_other . 'importLogFiles/other/OtherPostModSecurityAuditEvent/', array('AuditEventString' => $auditeventstring, 'apikey' => $apikey));
	}

}
