package org.zaproxy.zap.view.osxhandlers;

import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.AboutDialog;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;

public class OSXAboutHandler implements AboutHandler {
    @Override
    public void handleAbout(AboutEvent ae) {
        AboutDialog dialog = new AboutDialog(View.getSingleton().getMainFrame(), true);
        dialog.setVisible(true);
    }
}