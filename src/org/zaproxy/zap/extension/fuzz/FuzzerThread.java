/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.owasp.jbrofuzz.core.Fuzzer;
import org.parosproxy.paros.common.ThreadPool;

public class FuzzerThread implements Runnable {

    private static final Logger log = Logger.getLogger(FuzzerThread.class);
	
	private List<FuzzerListener> listenerList = new ArrayList<>();

	private Fuzzer[] fuzzers;
	private FileFuzzer[] customFuzzers;
	
	FuzzProcessFactory fuzzProcessFactory;

	private boolean pause = false;
    private boolean isStop = false;
    
    private ThreadPool pool = null;
    private int delayInMs = 0;

    /**
     * 
     */
    public FuzzerThread(FuzzerParam fuzzerParam) {
	    pool = new ThreadPool(fuzzerParam.getThreadPerScan());
	    delayInMs = fuzzerParam.getDelayInMs();
    }
    
    
    public void start() {
        isStop = false;
        
        Thread thread = new Thread(this, "ZAP-FuzzerThread");
        thread.setPriority(Thread.NORM_PRIORITY-2);
        thread.start();
    }
    
    public void stop() {
        isStop = true;
    }
   
	public void addFuzzerListener(FuzzerListener listener) {
		listenerList.add(listener);		
	}

	public void removeFuzzerListener(FuzzerListener listener) {
		listenerList.remove(listener);
	}

	private void notifyFuzzerComplete() {
		for (FuzzerListener listener : listenerList) {
			listener.notifyFuzzerComplete();
		}
	}


	public void setTarget(Fuzzer[] fuzzers, FileFuzzer[] customFuzzers, FuzzProcessFactory fuzzProcessFactory) {
		this.fuzzers = fuzzers;
		this.customFuzzers = customFuzzers;
		this.fuzzProcessFactory = fuzzProcessFactory;
	}

    @Override
    public void run() {
        log.info("fuzzer started");
        
    	if (customFuzzers != null) {
    		this.fuzz(customFuzzers);
    	} else {
    		this.fuzz(fuzzers);
    	}
	    
	    pool.waitAllThreadComplete(0);
	    notifyFuzzerComplete();

        log.info("fuzzer stopped");
	}
	
	private void fuzz(FileFuzzer[] customFuzzers) {
		
		int total = 0;
		for (FileFuzzer fuzzer : customFuzzers) {
			total += fuzzer.getLength();
		}
        for (FuzzerListener listener : listenerList) {
            listener.notifyFuzzerStarted(total);
        }
		
		for (FileFuzzer fuzzer : customFuzzers) {
			fuzz(fuzzer.getIterator());
			
			if (isStop()) {
				break;
			}
		}
	}

    private void fuzz(Fuzzer[] fuzzers) {
        
        int total = 0;
        for (Fuzzer fuzzer : fuzzers) {
            total += (int)fuzzer.getMaximumValue();
        }

        for (FuzzerListener listener : listenerList) {
            listener.notifyFuzzerStarted(total);
        }
        
        for (Fuzzer fuzzer : fuzzers) {
            fuzz(fuzzer);
            
            if (isStop()) {
                break;
            }
        }
    }

	private void fuzz(Iterator<String> it) {
	    while (it.hasNext()) {
            
            while (pause && ! isStop()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
            if (isStop()) {
                break;
            }
            if (delayInMs > 0) {
                try {
                    Thread.sleep(delayInMs);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
            
            String fuzz = it.next();
            
            FuzzProcess fp = fuzzProcessFactory.getFuzzProcess(fuzz);
            
            for (FuzzerListener listener : listenerList) {
                fp.addFuzzerListener(listener);
            }

            Thread thread;
            do { 
                thread = pool.getFreeThreadAndRun(fp);
                if (thread == null) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
            } while (thread == null && !isStop());

        }
	}
	
	public boolean isStop() {
	    
	    return isStop;
	}
	
	public void pause() {
		this.pause = true;
	}
	
	public void resume () {
		this.pause = false;
	}
	
	public boolean isPaused() {
		return pause;
	}

}
