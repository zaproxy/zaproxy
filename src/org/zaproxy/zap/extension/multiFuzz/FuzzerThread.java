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
package org.zaproxy.zap.extension.multiFuzz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.zaproxy.zap.extension.httppanel.Message;

public class FuzzerThread<PL extends Payload, M extends Message,  L extends FuzzLocation<M>, R extends FuzzResult<M, L>, G extends FuzzGap<M, L, PL>, P extends FuzzProcess<R, PL, L>>
		implements Runnable {

	private static final Logger log = Logger.getLogger(FuzzerThread.class);

	private List<FuzzerListener<Integer, R>> listenerList = new ArrayList<>();
	private FuzzerListener<Integer, Boolean> handlerListener;
	private ArrayList<G> gaps;
	private ArrayList<FuzzMessageProcessor<M>> preprocessors;
	private ArrayList<FuzzMessageProcessor<M>> postprocessors;
	FuzzProcessFactory<P, PL, L> fuzzProcessFactory;
	private ThreadPoolExe threadPool;

	private boolean pause = false;
	private boolean isStop = false;

	private int delayInMs = 0;

	public FuzzerThread(FuzzerParam fuzzerParam) {
		delayInMs = fuzzerParam.getDelayInMs();
	}

	public void start() {
		isStop = false;
		Thread thread = new Thread(this, "ZAP-FuzzerThread");
		thread.setPriority(Thread.NORM_PRIORITY - 2);
		thread.start();
	}

	public void stop() {
		threadPool.shutdown();
		isStop = true;
	}
	public void addHandlerListener(FuzzerListener<Integer, Boolean> listener) {
		this.handlerListener = listener;
	}
	public void addFuzzerListener(FuzzerListener<Integer, R> listener) {
		listenerList.add(listener);
	}

	public void removeFuzzerListener(FuzzerListener<Integer, R> listener) {
		listenerList.remove(listener);
	}

	public void addPreprocessor(FuzzMessageProcessor<M> pre) {
		this.preprocessors.add(pre);
	}

	public void addPostprocessor(FuzzMessageProcessor<M> post) {
		this.postprocessors.add(post);
	}

	public void setTarget(ArrayList<G> gaps,
			FuzzProcessFactory<P, PL, L> fuzzProcessFactory) {
		this.gaps = gaps;
		this.fuzzProcessFactory = fuzzProcessFactory;
	}

	@Override
	public void run() {
		while(!isStop){
			log.info("fuzzer started");

			this.fuzz(gaps);
			
			while(threadPool.getCompletedTaskCount() < threadPool.getMaximumPoolSize()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					log.info(e.getMessage());
				}
			}
			
			handlerListener.notifyFuzzerComplete(true);
			log.info("fuzzer stopped");
			isStop = true;
		}
	}

	private void fuzz(ArrayList<G> gaps) {
		int total = 1;
		int[] lens = new int[gaps.size()];
		int[] mod = new int[gaps.size()];
		for (G gap : gaps) {
			total *= gap.getPayloads().size();
		}

		for (int i = 0; i < mod.length; i++) {
			mod[i] = 1;
			lens[i] = gaps.get(i).getPayloads().size();
		}
		for (int i = lens.length - 1; i >= 0; i--) {
			for (int j = 0; j < i; j++) {
				mod[j] *= lens[i];
			}
		}
		this.threadPool = new ThreadPoolExe(total, total, 100, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<P>());
		for (FuzzerListener<Integer, R> listener : listenerList) {
			listener.notifyFuzzerStarted(total);
		}
		log.info(total + "Fuzz Combinations");
		for (int nr = 0; nr < total; nr++) {
			HashMap<L, PL> subs = new HashMap<L, PL>();
			for (int g = 0; g < gaps.size(); g++) {
				L fl = gaps.get(g).getLocation();
				PL sub = gaps.get(g).getPayloads().get((nr / mod[g]) % lens[g]);
				subs.put(fl, sub);
			}
			fuzz(subs);
			if (isStop()) {
				break;
			}
		}
	}

	private void fuzz(HashMap<L, PL> subs) {
		while (pause && !isStop()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// Ignore
			}
		}

		if (delayInMs > 0) {
			try {
				Thread.sleep(delayInMs);
			} catch (InterruptedException e) {
				// Ignore
			}
		}

		P fp = fuzzProcessFactory.getFuzzProcess(subs);

		fp.addFuzzerListener(new FuzzerListener<Integer, R>() {
			@Override
			public void notifyFuzzerStarted(Integer process) {
			}

			@Override
			public void notifyFuzzerPaused(Integer process) {
			}

			@Override
			public void notifyFuzzerComplete(R result) {
				for (FuzzerListener<Integer, R> f : listenerList) {
					f.notifyFuzzerComplete(result);
				}
			}
		});
		threadPool.execute(fp);

	}

	public boolean isStop() {
		return isStop;
	}

	public void pause() {
		this.threadPool.pause();
		this.pause = true;
	}

	public void resume() {
		this.threadPool.resume();
		this.pause = false;
	}

	public boolean isPaused() {
		return pause;
	}

}
