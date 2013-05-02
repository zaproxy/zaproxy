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
package org.parosproxy.paros.core.proxy;

/**
 * Defines a listener instance, who's listener order can be defined.
 */
public interface ArrangeableProxyListener {

    /**
     * Gets the order of when this listener should be notified.
     * 
     * <p>
     * The listeners are ordered in a natural order, the greater the order the
     * later it will be notified.
     * </p>
     * 
     * <p>
     * <strong>Note:</strong> If two or more listeners have the same order, the
     * order that those listeners will be notified is undefined.
     * </p>
     * 
     * @return an {@code int} with the value of the order that this listener
     *         should be notified about
     */
    int getArrangeableListenerOrder();
}
