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
package org.zaproxy.zap.scan;

/**
 * The common interface for options provided to {@link BaseScannerThread scanner threads} at startup
 * that should be used for fully specifying the configuration for a particular scan. The scan
 * options should be provided to the Scanner Thread before being started via the
 * {@link BaseScannerThread#setStartOptions(ScanStartOptions)} method.
 * <p/>
 * Examples of configuration required might include: context and/or site node to scan, user to scan
 * as, policy etc.
 * 
 * @see BaseScannerThread
 */
public interface ScanStartOptions {

}
