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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.owasp.jbrofuzz.core.Fuzzer;

public class FuzzerThread implements Runnable {

    private static final Logger log = Logger.getLogger(FuzzerThread.class);
	
	private List<FuzzerListener> listenerList = new ArrayList<>();

	private Fuzzer[] fuzzers;
	private FileFuzzer[] customFuzzers;
	
	FuzzProcessFactory fuzzProcessFactory;

	private volatile boolean pause = false;
    private volatile boolean isStop = false;
    
    private int delayInMs = 0;
    private FuzzExecutor pool;
    private int threadsPerScan;

    /**
     * 
     */
    public FuzzerThread(FuzzerParam fuzzerParam) {
	    delayInMs = fuzzerParam.getDelayInMs();
	    threadsPerScan = fuzzerParam.getThreadPerScan();
    }
    
    
    public void start() {
        isStop = false;
        
        Thread thread = new Thread(this, "ZAP-FuzzerThread");
        thread.setPriority(Thread.NORM_PRIORITY-2);
        thread.start();
    }
    
    public void stop() {
        if (isStop) {
            return;
        }
        if (pool != null) {
            pool.shutdownNow();
        }
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
        
        pool = new FuzzExecutor(threadsPerScan);

    	if (customFuzzers != null) {
    		this.fuzz(customFuzzers);
    	} else {
    		this.fuzz(fuzzers);
    	}
	    
        while (!isStop && !isCompleted()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignore) {
            }
        }

        if (isStop && !isCompleted()) {
            boolean terminated = false;
            try {
                terminated = pool.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignore) {
            }
            if (!terminated) {
                log.warn("Failed to await for all fuzz threads to stop in the given time (5s).");
            }
        } else {
            pool.shutdown();
        }
        pool = null;

	    notifyFuzzerComplete();

        log.info("fuzzer stopped");
	}

    private boolean isCompleted() {
        if (pool == null) {
            return true;
        }
        return pool.getCompletedTaskCount() == pool.getTaskCount();
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
	    while (it.hasNext() && !isStop()) {
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

            if (isStop()) {
                break;
            }
            pool.submit(fp);

        }
	}
	
	public boolean isStop() {
	    
	    return isStop;
	}
	
	public void pause() {
		this.pause = true;
		pool.pause();
	}
	
	public void resume () {
		this.pause = false;
		pool.resume();
	}
	
	public boolean isPaused() {
		return pause;
	}

	private static class FuzzExecutor extends ThreadPoolExecutor {

		private boolean paused;
		private ReentrantLock pauseLock = new ReentrantLock();
		private Condition unpaused = pauseLock.newCondition();

		public FuzzExecutor(int numberOfThreads) {
			super(numberOfThreads,
				  numberOfThreads,
				  0L,
				  TimeUnit.MILLISECONDS,
				  new LinkedBlockingQueue<Runnable>(),
				  new FuzzerThreadFactory());
		}

		@Override
		protected void beforeExecute(Thread t, Runnable r) {
			super.beforeExecute(t, r);
			pauseLock.lock();
			try {
				while (paused) {
					unpaused.await();
				}
			} catch (InterruptedException ie) {
				t.interrupt();
			} finally {
				pauseLock.unlock();
			}
		}

		public void pause() {
			pauseLock.lock();
			try {
				paused = true;
			} finally {
				pauseLock.unlock();
			}
		}

		public void resume() {
			pauseLock.lock();
			try {
				paused = false;
				unpaused.signalAll();
			} finally {
				pauseLock.unlock();
			}
		}
	}
	
	private static class FuzzerThreadFactory implements ThreadFactory {

		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		FuzzerThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = "ZAP-FuzzerPool-" + poolNumber.getAndIncrement() + "-thread-";
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon()) {
				t.setDaemon(false);
			}
			if (t.getPriority() != Thread.NORM_PRIORITY) {
				t.setPriority(Thread.NORM_PRIORITY);
			}
			return t;
		}
	}
}
