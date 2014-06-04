package org.zaproxy.zap.extension.multiFuzz;

import java.util.ArrayList;
import org.zaproxy.zap.extension.httppanel.Message;

public class FuzzGap<M extends Message, L extends FuzzLocation<M>, P extends Payload> {
	private ArrayList<P> payloads;
	private ArrayList<String> payloadSign;
	private L fuzzLoc;
	private M msg;

	protected FuzzGap( M msg, L loc) {
		fuzzLoc = loc;
		this.msg = msg;
		payloads = new ArrayList<P>();
		payloadSign = new ArrayList<String>();
	}

	public String orig() {
		return fuzzLoc.getRepresentation(msg);
	}

	public void addPayload(P p) {
		if(p != null){
			payloads.add(p);
			payloadSign.add(p.toString());
		}
	}

	public void removePayload(P p) {
		payloads.remove(p);
		payloadSign.remove(p.toString());
	}

	public L getLocation() {
		return fuzzLoc;
	}

	public ArrayList<P> getPayloads(){
		return this.payloads;
	}
	public ArrayList<String> getPayloadSignatures() {
		if(payloadSign == null){
			ArrayList<String> payloadSign = new ArrayList<String>();
			for(P pay : payloads){
				payloadSign.add(pay.toString());
			}
		}
		return payloadSign;
	}
}