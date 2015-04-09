package org.zaproxy.zap.authentication;

import java.io.IOException;

import org.apache.commons.httpclient.HttpState;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.users.User;

public class AuthenticationHelper {

	private HttpSender httpSender;
	private SessionManagementMethod sessionManagementMethod;
	private User user;

	public AuthenticationHelper(HttpSender httpSender, SessionManagementMethod sessionManagementMethod, User user) {
		super();
		this.httpSender = httpSender;
		this.sessionManagementMethod = sessionManagementMethod;
		this.user = user;
	}

	private static final Logger log = Logger.getLogger(AuthenticationHelper.class);

	private static final String HISTORY_TAG_AUTHENTICATION = "Authentication";

	public static void notifyOutputAuthSuccessful() {
		// Let the user know it worked
		if (View.isInitialised()) {
			View.getSingleton().getOutputPanel()
					.append(Constant.messages.getString("authentication.output.success") + "\n");
		}
	}

	public HttpState getCorrespondingHttpState() {
		if (user.getAuthenticatedSession() == null)
			user.setAuthenticatedSession(sessionManagementMethod.createEmptyWebSession());
		return user.getCorrespondingHttpState();
	}

	public static void addAuthMessageToHistory(HttpMessage msg) {
		// Add message to history
		try {
			HistoryReference ref = new HistoryReference(Model.getSingleton().getSession(),
					HistoryReference.TYPE_AUTHENTICATION, msg);
			ref.addTag(HISTORY_TAG_AUTHENTICATION);
			ExtensionHistory extHistory = Control.getSingleton().getExtensionLoader()
					.getExtension(ExtensionHistory.class);
			if (extHistory != null) {
				extHistory.addHistory(ref);
			}
		} catch (Exception ex) {
			log.error("Cannot add authentication message to History tab.", ex);
		}
	}

	public HttpMessage prepareMessage() {
		return prepareMessage(this.sessionManagementMethod, this.user);
	}

	public static HttpMessage prepareMessage(SessionManagementMethod sessionManagementMethod, User user) {
		HttpMessage msg = new HttpMessage();
		// Make sure the message will be sent with a good WebSession that can record the changes
		if (user.getAuthenticatedSession() == null)
			user.setAuthenticatedSession(sessionManagementMethod.createEmptyWebSession());
		msg.setRequestingUser(user);

		return msg;
	}

	public User getRequestingUser() {
		// Make sure the message will be sent with a good WebSession that can record the changes
		if (user.getAuthenticatedSession() == null)
			user.setAuthenticatedSession(sessionManagementMethod.createEmptyWebSession());
		return user;
	}

	public void sendAndReceive(HttpMessage msg) throws IOException {
		this.httpSender.sendAndReceive(msg);
	}

	public void sendAndReceive(HttpMessage msg, boolean followRedirect) throws IOException {
		this.httpSender.sendAndReceive(msg, followRedirect);
	}

}
