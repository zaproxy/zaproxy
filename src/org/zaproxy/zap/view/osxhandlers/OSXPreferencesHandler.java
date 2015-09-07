package org.zaproxy.zap.view.osxhandlers;

import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.PreferencesHandler;
import org.parosproxy.paros.control.Control;

// import org.parosproxy.paros.view.MainMenuBar;

public class OSXPreferencesHandler implements PreferencesHandler {
    @Override
    public void handlePreferences(PreferencesEvent pe) {

        // Set it up the same way that MainMenuBar does it; there's no money in reinvented wheels
        Control.getSingleton().getMenuToolsControl().options();
    }
}