package org.zaproxy.clientapi.core;

import java.util.ArrayList;
import java.util.List;

import org.zaproxy.clientapi.core.Alert.Reliability;
import org.zaproxy.clientapi.core.Alert.Risk;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		List<Alert> ignoreAlerts = new ArrayList<Alert>();
		ignoreAlerts.add(new Alert("Cookie set without HttpOnly flag", null, Risk.Low, Reliability.Warning, null, null));
		ignoreAlerts.add(new Alert(null, null, Risk.Low, Reliability.Warning, null, null));
		
		try {
			(new ClientApi("localhost", 8090)).checkAlerts(ignoreAlerts, null );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<Alert> requireAlerts = new ArrayList<Alert>();
		//ignoreAlerts.add(new Alert(null, null, null, null, null, null));
		requireAlerts.add(new Alert("Not present", null, Risk.Low, Reliability.Warning, null, null));
		try {
			(new ClientApi("localhost", 8090)).checkAlerts(ignoreAlerts, requireAlerts);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}

}
