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
class Script {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	/**
	 * Lists the script engines available
	 */
	public function listEngines() {
		return $this->zap->request($this->zap->base . 'script/view/listEngines/')->{'listEngines'};
	}

	/**
	 * Lists the scripts available, with its engine, name, description, type and error state.
	 */
	public function listScripts() {
		return $this->zap->request($this->zap->base . 'script/view/listScripts/')->{'listScripts'};
	}

	/**
	 * Enables the script with the given name
	 */
	public function enable($scriptname, $apikey='') {
		return $this->zap->request($this->zap->base . 'script/action/enable/', array('scriptName' => $scriptname, 'apikey' => $apikey));
	}

	/**
	 * Disables the script with the given name
	 */
	public function disable($scriptname, $apikey='') {
		return $this->zap->request($this->zap->base . 'script/action/disable/', array('scriptName' => $scriptname, 'apikey' => $apikey));
	}

	/**
	 * Loads a script into ZAP from the given local file, with the given name, type and engine, optionally with a description
	 */
	public function load($scriptname, $scripttype, $scriptengine, $filename, $scriptdescription=NULL, $apikey='') {
		$params = array('scriptName' => $scriptname, 'scriptType' => $scripttype, 'scriptEngine' => $scriptengine, 'fileName' => $filename, 'apikey' => $apikey);
		if ($scriptdescription !== NULL) {
			$params['scriptDescription'] = $scriptdescription;
		}
		return $this->zap->request($this->zap->base . 'script/action/load/', $params);
	}

	/**
	 * Removes the script with the given name
	 */
	public function remove($scriptname, $apikey='') {
		return $this->zap->request($this->zap->base . 'script/action/remove/', array('scriptName' => $scriptname, 'apikey' => $apikey));
	}

	/**
	 * Runs the stand alone script with the give name
	 */
	public function runStandAloneScript($scriptname, $apikey='') {
		return $this->zap->request($this->zap->base . 'script/action/runStandAloneScript/', array('scriptName' => $scriptname, 'apikey' => $apikey));
	}

}
