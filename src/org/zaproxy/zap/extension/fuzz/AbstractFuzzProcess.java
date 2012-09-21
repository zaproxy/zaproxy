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
package org.zaproxy.zap.extension.fuzz;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFuzzProcess implements FuzzProcess {

    private String fuzz;
    
    
    private List<FuzzerListener> listeners = new ArrayList<>(1);
    
    @Override
    public final void setFuzz(String fuzz) {
        this.fuzz = fuzz;
    }

    @Override
    public final void run() {
        for (FuzzerListener listener : listeners) {
            listener.notifyFuzzProcessStarted(this);
        }

        FuzzResult fuzzResult = fuzz(fuzz);
        
        for (FuzzerListener listener : listeners) {
            listener.notifyFuzzProcessComplete(fuzzResult);
        }
    }

    @Override
    public final void addFuzzerListener(FuzzerListener listener) {
        listeners.add(listener);
    }

    @Override
    public final void removeFuzzerListener(FuzzerListener listener) {
        listeners.remove(listener);
    }

    protected abstract FuzzResult fuzz(String fuzz);

}
