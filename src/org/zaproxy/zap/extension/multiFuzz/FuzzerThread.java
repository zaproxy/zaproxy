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

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;

public class FuzzerThread<PL extends Payload, M extends Message, L extends FuzzLocation<M>, R extends FuzzResult<M, L>, G extends FuzzGap<M, L, PL>, P extends FuzzProcess<R, PL, M, L>>
		implements Runnable {

	private static final Logger log = Logger.getLogger(FuzzerThread.class);

	private List<FuzzerListener<Integer, R>> listenerList = new ArrayList<>();
	private FuzzerListener<Integer, Boolean> handlerListener;
	private ArrayList<G> gaps;
	private ArrayList<PayloadProcessor<PL>> payprocessors;
	private ArrayList<FuzzMessagePreProcessor<M, L, PL>> preprocessors;
	private ArrayList<FuzzResultProcessor<R>> postprocessors;
	FuzzProcessFactory<P, PL, L> fuzzProcessFactory;
	private ThreadPoolExe threadPool;

	private volatile boolean pause = false;
	private volatile boolean isStop = false;

	private int comb_count = 0;
	private int threadCount = 1;
	private int delayInMs = 0;

	public FuzzerThread(FuzzerParam fuzzerParam) {
		delayInMs = fuzzerParam.getDelayInMs();
		threadCount = fuzzerParam.getThreadPerScan();
		payprocessors = new ArrayList<>();
		preprocessors = new ArrayList<>();
		postprocessors = new ArrayList<>();
	}

	public void start() {
		isStop = false;
		Thread thread = new Thread(this, "ZAP-FuzzerThread");
		thread.setPriority(Thread.NORM_PRIORITY - 2);
		thread.start();
	}

	public void stop() {
		threadPool.shutdownNow();
		try {
			threadPool.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			if (log.isDebugEnabled()) {
				log.debug(e.getMessage());
			}
		}
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

	public void addPreprocessor(FuzzMessagePreProcessor<M, L, PL> pre) {
		this.preprocessors.add(pre);
	}

	public void addPostprocessor(FuzzResultProcessor<R> post) {
		this.postprocessors.add(post);
	}

	public void setTarget(ArrayList<G> gaps,
			FuzzProcessFactory<P, PL, L> fuzzProcessFactory) {
		this.gaps = gaps;
		this.fuzzProcessFactory = fuzzProcessFactory;
	}

	@Override
	public void run() {
		log.info("fuzzer started");
		this.fuzz(gaps);

		while (threadPool.getCompletedTaskCount() < comb_count && !isStop) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				if (log.isDebugEnabled()) {
					log.debug(e.getMessage());
				}
			}
		}

		handlerListener.notifyFuzzerComplete(true);
		log.info("fuzzer stopped");
		isStop = true;
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
		comb_count = total;
		int core = (total < threadCount) ? total : threadCount;
		this.threadPool = new ThreadPoolExe(core, threadCount, 100,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		for (FuzzerListener<Integer, R> listener : listenerList) {
			listener.notifyFuzzerStarted(total);
		}
		log.info(total + " Fuzz Combinations");
		for (int nr = 0; nr < total; nr++) {
			HashMap<L, PL> subs = new HashMap<>();
			for (int g = 0; g < gaps.size(); g++) {
				L fl = gaps.get(g).getLocation();
				PL sub = gaps.get(g).getPayloads().get((nr / mod[g]) % lens[g]);
				subs.put(fl, sub);
			}
			fuzz(subs, nr);
			if (isStop()) {
				break;
			}
		}
	}

	private void fuzz(HashMap<L, PL> subs, int id) {
		while (pause && !isStop()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}

		if (delayInMs > 0) {
			try {
				Thread.sleep(delayInMs);
			} catch (InterruptedException e) {
			}
		}

		P fp = fuzzProcessFactory.getFuzzProcess(subs, id);
		fp.setPayloadProcessors(payprocessors);
		fp.setPreProcessors(preprocessors);
		fp.setPostProcessors(postprocessors);
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

	public void importScripts() {
		ExtensionScript extension = (ExtensionScript) Control.getSingleton()
				.getExtensionLoader().getExtension(ExtensionScript.NAME);
		if (extension != null) {
			List<ScriptWrapper> scripts = extension
					.getScripts(ExtensionFuzz.SCRIPT_TYPE_FUZZ);
			for (final ScriptWrapper script : scripts) {
				try {
					if (script.isEnabled()) {
						final FuzzScript s = extension.getInterface(script,
								FuzzScript.class);

						if (s != null) {
							payprocessors.add(new PayloadProcessor<PL>() {

								@Override
								public PL process(PL orig) {
									try {
										s.processPayload(orig);
									} catch (ScriptException e) {
										try {
											if (script.getWriter() != null) {
												script.getWriter().append(
														e.toString());
											}
										} catch (Exception e1) {
											log.debug(e1.getMessage());
										}
									}
									return orig;
								}
							});
							preprocessors
									.add(new FuzzMessagePreProcessor<M, L, PL>() {

										@Override
										public M process(M orig,
												Map<L, PL> payMap) {
											try {
												s.preProcess(orig, payMap);
											} catch (ScriptException e) {
												try {
													if (script.getWriter() != null) {
														script.getWriter()
																.append(e
																		.toString());
													}
												} catch (Exception e1) {
													log.debug(e1.getMessage());
												}
											}
											return orig;
										}
									});
							postprocessors.add(new FuzzResultProcessor<R>() {
								@Override
								public R process(R orig) {
									try {
										s.postProcess(orig);
									} catch (Exception e) {
										try {
											if (script.getWriter() != null) {
												script.getWriter().append(
														e.toString());
											}
										} catch (Exception e1) {
											log.debug(e1.getMessage());
										}
									}
									return orig;
								}
							});

						} else {
							if(script.getWriter() != null){
							script.getWriter()
									.append(Constant.messages
											.getString("scripts.interface.active.error"));
							}
							extension.setError(script, script.getWriter()
									.toString());
							extension.setEnabled(script, false);
						}
					}

				} catch (Exception e) {
					try {
						if (script.getWriter() != null) {
							script.getWriter().append(e.toString());
						}
					} catch (Exception e1) {
						log.debug(e1.getMessage());
					}
					extension.setError(script, e);
					extension.setEnabled(script, false);
				}
			}
		}
	}

}
