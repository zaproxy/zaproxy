package org.zaproxy.zap.extension.multiFuzz;

public interface Payload {
	public int getLength();

	public void setLength(int len);

	public boolean getRecursive();

	public void setRecursive(boolean rec);

	public int getLimit();

	public void setLimit(int l);
}
