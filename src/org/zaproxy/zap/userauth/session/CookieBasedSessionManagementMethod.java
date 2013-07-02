package org.zaproxy.zap.userauth.session;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.httpsessions.HttpSession;

public class CookieBasedSessionManagementMethod extends SessionManagementMethod {

	private HttpSession session;

	public static class CookieBasedSessionManagementMethodFactory extends
			SessionManagementMethodFactory<CookieBasedSessionManagementMethod> {

		private static final String METHOD_NAME = Constant.messages.getString("userauth.session.cb.name");

		@Override
		public CookieBasedSessionManagementMethod buildAuthenticationMethod() {
			return new CookieBasedSessionManagementMethod();
		}

		@Override
		public String getName() {
			return METHOD_NAME;
		}

		@Override
		public AbstractSessionManagementMethodOptionsPanel<CookieBasedSessionManagementMethod> buildOptionsPanel() {
			return null;
		}

	}

}
