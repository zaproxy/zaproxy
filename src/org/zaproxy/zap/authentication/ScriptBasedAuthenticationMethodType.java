package org.zaproxy.zap.authentication;

import java.sql.SQLException;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.extension.api.ApiDynamicActionImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptType;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.WebSession;
import org.zaproxy.zap.users.User;

public class ScriptBasedAuthenticationMethodType extends AuthenticationMethodType {

	private static final int METHOD_IDENTIFIER = 4;

	private static final Logger log = Logger.getLogger(ScriptBasedAuthenticationMethodType.class);

	/** The Constant SCRIPT_TYPE_AUTH. */
	private static final String SCRIPT_TYPE_AUTH = "authentication";

	/** The SCRIPT ICON. */
	private static final ImageIcon SCRIPT_ICON_AUTH = new ImageIcon(
			ZAP.class.getResource("/resource/icon/16/script-auth.png"));

	/** The Authentication method's name. */
	private static final String METHOD_NAME = Constant.messages
			.getString("authentication.method.script.name");
	
	public class ScriptBasedAuthenticationMethod extends AuthenticationMethod {

		@Override
		public boolean isConfigured() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected AuthenticationMethod duplicate() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public AuthenticationCredentials createAuthenticationCredentials() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public AuthenticationMethodType getType() {
			return new ScriptBasedAuthenticationMethodType();
		}

		@Override
		public WebSession authenticate(SessionManagementMethod sessionManagementMethod,
				AuthenticationCredentials credentials, User user)
				throws UnsupportedAuthenticationCredentialsException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ApiResponse getApiResponseRepresentation() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		// Hook up the Script Type
		ExtensionScript extScript = (ExtensionScript) Control.getSingleton().getExtensionLoader()
				.getExtension(ExtensionScript.NAME);
		if (extScript != null) {
			log.debug("Registering Script...");
			extScript.registerScriptType(new ScriptType(SCRIPT_TYPE_AUTH,
					"authentication.method.script.type", SCRIPT_ICON_AUTH, false));
		}
	}

	@Override
	public AuthenticationMethod createAuthenticationMethod(int contextId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return METHOD_NAME;
	}

	@Override
	public int getUniqueIdentifier() {
		return METHOD_IDENTIFIER;
	}

	@Override
	public AbstractAuthenticationMethodOptionsPanel buildOptionsPanel(Context uiSharedContext) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasOptionsPanel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AbstractCredentialsOptionsPanel<? extends AuthenticationCredentials> buildCredentialsOptionsPanel(
			AuthenticationCredentials credentials, Context uiSharedContext) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasCredentialsOptionsPanel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTypeForMethod(AuthenticationMethod method) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AuthenticationMethod loadMethodFromSession(Session session, int contextId) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void persistMethodToSession(Session session, int contextId, AuthenticationMethod authMethod)
			throws UnsupportedAuthenticationMethodException, SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public AuthenticationCredentials createAuthenticationCredentials() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApiDynamicActionImplementor getSetMethodForContextApiAction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApiDynamicActionImplementor getSetCredentialsForUserApiAction() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * The Interface that needs to be implemented by an Authentication Script.
	 */
	public interface AuthenticationScript {

	}

}
