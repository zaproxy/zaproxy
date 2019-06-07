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
package org.zaproxy.zap.utils;

public interface StatsListener {

    public void counterInc(String key);

    public void counterInc(String site, String key);

    public void counterInc(String key, long inc);

    public void counterInc(String site, String key, long inc);

    public void counterDec(String key);

    public void counterDec(String site, String key);

    public void counterDec(String key, long dec);

    public void counterDec(String site, String key, long dec);

    public void highwaterMarkSet(String key, long value);

    public void highwaterMarkSet(String site, String key, long value);

    public void lowwaterMarkSet(String key, long value);

    public void lowwaterMarkSet(String site, String key, long value);

    public void allCleared();

    public void allCleared(String site);

    public void cleared(String keyPrefix);

    public void cleared(String site, String keyPrefix);
}
