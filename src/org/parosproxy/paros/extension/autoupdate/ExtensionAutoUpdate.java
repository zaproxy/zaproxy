/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.parosproxy.paros.extension.autoupdate;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookMenu;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.network.HttpStatusCode;
import org.parosproxy.paros.view.WaitMessageDialog;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionAutoUpdate extends ExtensionAdaptor{

	private JMenuItem menuItemCheckUpdate = null;
	private ExtensionHookMenu extensionMenu = null;
    private static final String SF_PAROS_FILES = "http://sourceforge.net/project/showfiles.php?group_id=84378";
	private HttpSender httpSender = null;
//    private Pattern patternNewestVersion = Pattern.compile("http://prdownloads\\.sourceforge\\.net/paros/paros-(\\d+)\\.(\\d+)\\.(\\d+)-win\\.bin", Pattern.MULTILINE);
    private Pattern patternNewestVersionWindows = Pattern.compile("paros-(\\d+)\\.(\\d+)\\.(\\d+)-win\\.dat", Pattern.MULTILINE);
    private Pattern patternNewestVersionLinux = Pattern.compile("paros-(\\d+)\\.(\\d+)\\.(\\d+)-unix\\.zip", Pattern.MULTILINE);
    
    String newestVersionName = null;
    private boolean manualCheckStarted = false;
    // ZAP: Added logger
    private Logger logger = Logger.getLogger(ExtensionAutoUpdate.class);
    
    private final String header = "POST http://prdownloads.sourceforge.net/redir.php HTTP/1.1\r\n"
                                + "Host: prdownloads.sourceforge.net\r\n"
                                + "User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0;)\r\n"
                                + "Pragma: no-cache\r\n"
                                + "Content-Type: application/x-www-form-urlencoded\r\n"
                                + "\r\n";
    
    private String getBody(String mirror, String fileName) {
        String body = "mirror=" + mirror + "&path=/paros/" + fileName;
        return body;
    }
    
    private WaitMessageDialog waitDialog = null;

    private String[] mirrorList = {
//            "kent",
//            "heanet",
//            "puzzle",
//            "nchc",
//            "optusnet",
//            "jaist"
            "osdn"
    };
    
    /**
     * 
     */
    public ExtensionAutoUpdate() {
        super();
 		initialize();
   }   

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionAutoUpdate");
			
	}

	/**
	 * This method initializes menuItemEncoder	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private JMenuItem getMenuItemCheckUpdate() {
		if (menuItemCheckUpdate == null) {
			menuItemCheckUpdate = new JMenuItem();
			menuItemCheckUpdate.setText("Check for Updates...");
            if (!Constant.isWindows() && !Constant.isLinux()) {
                menuItemCheckUpdate.setEnabled(false);
            }
			menuItemCheckUpdate.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    

                    

                    Thread t = new Thread(new Runnable() {
                        public void run() {
                            manualCheckStarted = true;
                            newestVersionName = getNewestVersionName();
                            
                            if (waitDialog != null) {
                                waitDialog.hide();
                                waitDialog = null;
                            }
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    
                                    if (newestVersionName == null) {
                                        getView().showMessageDialog("There is no new update available.  Paros may periodically check for update.");
                                    } else if (newestVersionName.equals("")) {
                                        getView().showWarningDialog("Error encountered.  Please check manually for new updates.");
                                        
                                    } else {
                                        int result = getView().showConfirmDialog("There is a newer version of Paros: " + newestVersionName.replaceAll("\\.dat","") + "\nProceed to download?");
                                        if (result == JOptionPane.OK_OPTION) {
                                            waitDialog = getView().getWaitMessageDialog("Download in progress...");
                                            Thread t = new Thread(new Runnable() {
                                                public void run() {
                                                    ExtensionAutoUpdate.this.download(false);
                                                }
                                            });
                                            t.start();
                                            waitDialog.show();
                                            
                                        }
                                    }
                                    
                                }
                            });
                            
                            
                        }
                    });
                    waitDialog = getView().getWaitMessageDialog("Checking if newer version exists...");
                    t.start();
                    waitDialog.show();
				}

			});

		}
		return menuItemCheckUpdate;
	}

    public void start() {
        
        if (!Constant.isWindows()) {
            return;
        }
        
        // check 1 in 30 cases to avoid too frequent check.
        if (getRandom(30) != 1) {
            return;
        }
        
        Thread t = new Thread(new Runnable() {
            public void run() {

                newestVersionName = getNewestVersionName();
                if (newestVersionName == null || newestVersionName.length() ==0) {
                    return;
                } else {

                    ExtensionAutoUpdate.this.download(true);
                }
                    
            }
               
        });
        t.start();
            
    }
    
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    if (getView() != null) {
	        extensionHook.getHookMenu().addToolsMenuItem(getMenuItemCheckUpdate());
	    }
	}
    
    public void download(final boolean silent) {
        
        if (newestVersionName == null) {
            return;
        }
        
        HttpMessage msg = new HttpMessage();
        try {
            msg.setRequestHeader(header);
            msg.setRequestBody(getBody(mirrorList[getRandom(mirrorList.length)], newestVersionName));
            
            getHttpSender().sendAndReceive(msg,true);
            
            if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK) {
                throw new IOException();
            }

            if (silent && manualCheckStarted) {
                return;
            }
            
            File file = null;
            if (Constant.isWindows()) {
                file = new File("parosnew.exe");
            } else if (Constant.isLinux()) {
                file = new File("parosnew.zip");
            }
            
            FileOutputStream os = new FileOutputStream(file);
            os.write(msg.getResponseBody().getBytes());
            os.close();


            try {
                final File updateFile = file;
                EventQueue.invokeAndWait(new Runnable() {
                    public void run() {

                        if (waitDialog != null) {
                            waitDialog.hide();
                        }

                        if (!silent) {
                            String s = "A newer verison has been downloaded.  It will be installed the \nnext time Paros is started.";
                            if (Constant.isLinux()) {
                                s = s + "  Note: Use startserver.sh to run Paros.";
                            }
                            getView().showMessageDialog(s);
                        } else {
                            String s = "A newer version is available.  Install the new version \nnext time Paros is started?";
                            if (Constant.isLinux()) {
                                s = s + "  Note: Use startserver.sh to run Paros.";
                            }
                            int result = getView().showConfirmDialog(s);
                            if (result != JOptionPane.OK_OPTION) {
                                if (! updateFile.delete()) {
                                	// ZAP: Log failure to delete file
                                	logger.error("Failed to delete file " + updateFile.getAbsolutePath());
                                }
                            }
                        }
                    }
                });
            } catch (Exception e) {
            }
        } catch (IOException e) {

            try {
                EventQueue.invokeAndWait(new Runnable() {
                    public void run() {
                        if (waitDialog != null) {
                            waitDialog.hide();
                        }

                        if (!silent) {
                            
                            getView().showWarningDialog("Error encountered.  Please download new updates manually");
                        }
                    }
                });
            } catch (Exception e1) {
            }

            
            if (waitDialog != null) {
                waitDialog.hide();
            }
        } finally {           
            httpSender.shutdown();
            httpSender = null;
        }
    }
    
    private String getNewestVersionName() {
        String newVersionName = null;
        HttpMessage msg = null;
        String resBody = null;
        
        try {
            msg = new HttpMessage(new URI(SF_PAROS_FILES, true));
            getHttpSender().sendAndReceive(msg,true);
            if (msg.getResponseHeader().getStatusCode() != HttpStatusCode.OK) {
                throw new IOException();
            }
            resBody = msg.getResponseBody().toString();
            Matcher matcher = null;
            if (Constant.isWindows()) {
                matcher = patternNewestVersionWindows.matcher(resBody);
            } else {
                matcher = patternNewestVersionLinux.matcher(resBody);                
            }
            if (matcher.find()) {
                int ver_major = Integer.parseInt(matcher.group(1));
                int ver_minor = Integer.parseInt(matcher.group(2));
                int ver_release = Integer.parseInt(matcher.group(3));
                long version = 10000000 * ver_major + 10000 * ver_minor + ver_release;
                if (version > Constant.VERSION_TAG) {
                    newVersionName = matcher.group(0);
                }
            }
        } catch (Exception e) {
            newVersionName = "";
        } finally {
            httpSender.shutdown();
            httpSender = null;

        }
        
        return newVersionName;
    }
    
    private HttpSender getHttpSender() {
        if (httpSender == null) {
            httpSender = new HttpSender(getModel().getOptionsParam().getConnectionParam(), true);
        }
        return httpSender;
    }
    
    private int getRandom(int max) {
        int result = (int) (max * Math.random()) ;
        return result;
    }
  }
