/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2016 The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.view;

import org.zaproxy.zap.control.AddOn;

/**
 * A utility class for handling various UI elements related to status/quality
 * related to extensions and extension components
 * @since 2.5.0
 */
public class StatusUI implements Comparable<StatusUI> {

        private final AddOn.Status status;
        private final String stringRepresentation;
/**
 * 
 * @param status the AddOn.Status for which the StatusUI should be created
 * @param stringRepresentation the internationalized status string that corresponds 
 * to the AddOn.Status for which the StatusUI is being constructed.
 * If either parameter is null then "unknown" status will be assumed. 
 */
        public StatusUI(AddOn.Status status, String stringRepresentation) {
            if (status == null) {
        		this.status = AddOn.Status.unknown;
	        } else {
	        	this.status = status;
	        }
            
            if (stringRepresentation == null || stringRepresentation.isEmpty()) {
            	this.stringRepresentation = status.toString();
            } else {
            	this.stringRepresentation = stringRepresentation;
            }
        }
        
        @Override
        public int compareTo(StatusUI o) {
            if (o == null) {
                return 1;
            }
            return status.compareTo(o.status);
        }

        @Override
        public String toString() {
            return stringRepresentation;
        }
    }
