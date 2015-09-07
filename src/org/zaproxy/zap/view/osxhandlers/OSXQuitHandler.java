package org.zaproxy.zap.view.osxhandlers;

import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.view.View;

import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

public class OSXQuitHandler implements QuitHandler {
    @Override
    public void handleQuitRequestWith(QuitEvent qe, QuitResponse qr) {

        // Quitting is way better when you're saving your state
        Control.getSingleton().getMenuFileControl().exit();
    }
    
    public void removeZAPViewItem(View view) {
        // Ideally, construct some crazy way to remove the File -> Exit command
    }
}