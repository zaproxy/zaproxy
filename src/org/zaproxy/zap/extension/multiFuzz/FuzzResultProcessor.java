package org.zaproxy.zap.extension.multiFuzz;

public interface FuzzResultProcessor<R extends FuzzResult> {
	public R process(R orig);
}
