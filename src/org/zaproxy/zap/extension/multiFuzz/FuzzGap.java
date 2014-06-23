package org.zaproxy.zap.extension.multiFuzz;

import java.util.ArrayList;
import org.zaproxy.zap.extension.httppanel.Message;

public class FuzzGap<M extends Message, L extends FuzzLocation<M>, P extends Payload> {
	private ArrayList<P> payloads;
	private L fuzzLoc;
	private M msg;

	protected FuzzGap(M msg, L loc) {
		fuzzLoc = loc;
		this.msg = msg;
		payloads = new ArrayList<P>();
	}

	public String orig() {
		return fuzzLoc.getRepresentation(msg);
	}

	public void addPayload(P p) {
		if (p != null) {
			payloads.add(p);
		}
	}

	public void addPayloads(ArrayList<P> pays) {
		payloads.addAll(pays);
	}

	public void setPayloads(ArrayList<P> pays) {
		payloads = pays;
	}

	public void removePayload(P p) {
		payloads.remove(p);
	}

	public L getLocation() {
		return fuzzLoc;
	}

	public ArrayList<P> getPayloads() {
		return this.payloads;
	}

}