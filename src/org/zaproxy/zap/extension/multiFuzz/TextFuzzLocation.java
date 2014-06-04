package org.zaproxy.zap.extension.multiFuzz;

import org.zaproxy.zap.extension.httppanel.Message;

public interface TextFuzzLocation<M extends Message> extends FuzzLocation<M>{
	public int begin();
	public int end();
}
