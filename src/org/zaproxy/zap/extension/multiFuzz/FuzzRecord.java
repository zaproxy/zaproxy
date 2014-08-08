/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.zaproxy.zap.extension.multiFuzz;

import java.util.List;

import javax.swing.ImageIcon;

import org.zaproxy.zap.utils.Pair;

public interface FuzzRecord {
	public void setName(String s);
	public String getName();
	public Pair<String, ImageIcon> getResult();
	public List<String> getPayloads();
	public Boolean isIncluded();
	public void setIncluded(Boolean b);
	public String getCustom();
}
