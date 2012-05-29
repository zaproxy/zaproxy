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
// ZAP: 2011/11/20 Explicitly depreciated
// ZAP: 2012/02/18 Rationalised session handling
// ZAP: 2012/03/17 Issue 282 Added getAuthor()
// ZAP: 2012/04/25 Added type argument to generic type, removed unnecessary
// cast and added @Override annotation to all appropriate methods.
// ZAP: 2012/05/29 Changed to use the new SpiderParam

package org.parosproxy.paros.extension.spider;

import java.awt.EventQueue;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JTree;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.spider.Spider;
import org.parosproxy.paros.core.spider.SpiderListener;
import org.parosproxy.paros.extension.CommandLineArgument;
import org.parosproxy.paros.extension.CommandLineListener;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.spider.SpiderParam;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionSpider extends ExtensionAdaptor implements SpiderListener, SessionChangedListener, CommandLineListener {
    
    // ZAP: Added logger
    private static Logger log = Logger.getLogger(ExtensionSpider.class);

    private static final int ARG_SPIDER_IDX = 0;
    private static final int ARG_URL_IDX = 1;
	private JMenuItem menuItemSpider = null;  //  @jve:decl-index=0:
	private SpiderDialog dialog = null;  //  @jve:decl-index=0:
	private Spider spider = null;
	private SiteMap siteTree = null;
	private SiteNode startNode = null;
	
	
	private PopupMenuSpider popupMenuSpider = null;  //  @jve:decl-index=0:visual-constraint="161,133"
	private SpiderPanel spiderPanel = null;
	private OptionsSpiderPanel optionsSpiderPanel = null;
	private SpiderParam spiderParam = null;   //  @jve:decl-index=0:
	private CommandLineArgument[] arguments = new CommandLineArgument[2];
	
    /**
     * 
     */
    public ExtensionSpider() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionSpider(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionSpider");
			
	}
	/**
	 * This method initializes menuItemSpider	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	JMenuItem getMenuItemSpider() {
		if (menuItemSpider == null) {
			menuItemSpider = new JMenuItem();
			menuItemSpider.setText(Constant.messages.getString("menu.analyse.spider"));	// ZAP: i18n
			menuItemSpider.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    JTree siteTree = getView().getSiteTreePanel().getTreeSite();
		            SiteNode node = (SiteNode) siteTree.getLastSelectedPathComponent();
		            HttpMessage msg = null;
		            if (node == null) {
		                getView().showWarningDialog(Constant.messages.getString("spider.emptyView.warning"));	// ZAP: i18n
		                return;
		            }
	                setStartNode(node);
	                if (node.isRoot()) {
	                    showDialog(Constant.messages.getString("spider.rootNode.warning"));	// ZAP: i18n
	                } else {
                        try {
                            msg = node.getHistoryReference().getHttpMessage();
                        } catch (Exception e1) {
                        	// ZAP: Log exceptions
                        	log.warn(e1.getMessage(), e1);
                            return;
                        }
                        String tmp = msg.getRequestHeader().getURI().toString();
                        showDialog(tmp);
	                }
	                
	                
				}
			});

		}
		return menuItemSpider;
	}
	
	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    if (getView() != null) {
	        extensionHook.getHookMenu().addAnalyseMenuItem(getMenuItemSpider());
            extensionHook.getHookMenu().addAnalyseMenuItem(extensionHook.getHookMenu().getMenuSeparator());

            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuSpider());
	        extensionHook.getHookView().addStatusPanel(getSpiderPanel());
	        extensionHook.getHookView().addOptionPanel(getOptionsSpiderPanel());
	    }
        extensionHook.addSessionListener(this);
        extensionHook.addOptionsParamSet(getSpiderParam());

        
        extensionHook.addCommandLine(getCommandLineArguments());
        
	}
	
	public void startSpider() {
        siteTree = getModel().getSession().getSiteTree();

	    if (startNode == null) {
	        startNode = (SiteNode) siteTree.getRoot();
	    }
        startSpider(startNode);

	}
	
	private void startSpider(SiteNode startNode) {

	    if (spider == null) {
	        try {
                getModel().getDb().getTableHistory().deleteHistoryType(getModel().getSession().getSessionId(), HistoryReference.TYPE_SPIDER_SEED);
    	        getModel().getDb().getTableHistory().deleteHistoryType(getModel().getSession().getSessionId(), HistoryReference.TYPE_SPIDER_VISITED);

	        } catch (SQLException e) {
            	// ZAP: Log exceptions
            	log.warn(e.getMessage(), e);
            }
	        
	        spider = new Spider(getSpiderParam(), getModel().getOptionsParam().getConnectionParam(), getModel());
	        spider.addSpiderListener(this);

	        inOrderSeed(spider, startNode);

	    }
	    
	    getSpiderPanel().setTabFocus();

		try {
			spider.start();
		    
        } catch (NullPointerException e1) {
        	// ZAP: Log exceptions
        	log.warn(e1.getMessage(), e1);
        }
	}
	
	private void inOrderSeed(Spider spider, SiteNode node) {

	    try {
	        if (!node.isRoot()) {
	            HttpMessage msg = node.getHistoryReference().getHttpMessage();
	            if (msg != null) {
	                if (!msg.getResponseHeader().isImage()) {
	                    spider.addSeed(msg);
	                }
	            }
	        }
	    } catch (Exception e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
	    }
	    
	    if (!node.isLeaf()) {
	        for (int i=0; i<node.getChildCount(); i++) {
	            try {
	                inOrderSeed(spider, (SiteNode) node.getChildAt(i));
	            } catch (Exception e) {
                	// ZAP: Log exceptions
                	log.warn(e.getMessage(), e);
	            }
	        }
	    }
	}
	
	@Override
	public void spiderComplete() {

        try {
            getModel().getDb().getTableHistory().deleteHistoryType(getModel().getSession().getSessionId(), HistoryReference.TYPE_SPIDER_SEED);
	        getModel().getDb().getTableHistory().deleteHistoryType(getModel().getSession().getSessionId(), HistoryReference.TYPE_SPIDER_VISITED);

        } catch (SQLException e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
        }

	    if (getView() != null) {
	        getMenuItemSpider().setEnabled(true);
	        getPopupMenuSpider().setEnabled(true);

	    }

	    try {
	        Thread.sleep(3000);
	    } catch (Exception e) {}
	    
	    if (getView() != null && dialog != null) {
	        if (EventQueue.isDispatchThread()) {
	            dialog.dispose();
	            return;
	        }
	        try {
	            EventQueue.invokeAndWait(new Runnable() {
	                @Override
	                public void run() {
	                    dialog.dispose();
	                }
	            });
	        } catch (Exception e) {
            	// ZAP: Log exceptions
            	log.warn(e.getMessage(), e);
	        }
	    }
	}
	
	@Override
	public void foundURI(HttpMessage msg, boolean isSkip) {
	    if (getView() != null) {
	        if (isSkip) {
	            getSpiderPanel().appendFoundButSkip(msg.getRequestHeader().getURI().toString() + "\n");
	        } else {
	            getSpiderPanel().appendFound(msg.getRequestHeader().getURI().toString() + "\n");
	        }
        }
	}
	
	@Override
	public void readURI(HttpMessage msg) {

	    SiteMap siteTree = getModel().getSession().getSiteTree();

	    // record into sitemap if not exist
		HttpMessage existing = siteTree.pollPath(msg);

//		always add to tree		
//		if (existing != null) {
//		    return;
//		}
		
		HistoryReference historyRef = null;
        try {
            historyRef = new HistoryReference(getModel().getSession(), HistoryReference.TYPE_SPIDER, msg);
        } catch (Exception e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
        }
        siteTree.addPath(historyRef, msg);

        
	}

    public Spider getSpider() {
	    return spider;
	}
	
	@Override
	public void spiderProgress(final URI uri, final int percentageComplete, final int numberCrawled, final int numberToCrawl) {
	    String uriString= "";
	    
	    if (dialog != null) {
	        if (EventQueue.isDispatchThread()) {
	            dialog.getTxtNumCrawled().setText(Integer.toString(numberCrawled));
	            dialog.getTxtOutstandingCrawl().setText(Integer.toString(numberToCrawl));

	            dialog.getProgressBar().setValue(percentageComplete);
	            uriString = "";
	            if (uri != null) {
	                uriString = uri.toString();
	            }
	            dialog.getTxtDisplay().setText(uriString);
	            //dialog.getTxtDisplay().setCaretPosition(0);
	            
	            return;
	        }
	        try {
	            EventQueue.invokeAndWait(new Runnable() {
	                @Override
	                public void run() {
	                    String uriString = "";
	    	            dialog.getTxtNumCrawled().setText(Integer.toString(numberCrawled));
	    	            dialog.getTxtOutstandingCrawl().setText(Integer.toString(numberToCrawl));

	    	            dialog.getProgressBar().setValue(percentageComplete);
	    	            if (uri != null) {
	    	                uriString = uri.toString();
	    	            }
	    	            dialog.getTxtDisplay().setText(uriString);
	    	            //dialog.getTxtDisplay().setCaretPosition(0);
	    	            
	                }
	            });
	        } catch (Exception e) {
            	// ZAP: Log exceptions
            	log.warn(e.getMessage(), e);
	        }
	        
	    }

	    
	}
    /**
     * @return Returns the startNode.
     */
    public SiteNode getStartNode() {
        return startNode;
    }
    /**
     * @param startNode The startNode to set.
     */
    public void setStartNode(SiteNode startNode) {
        this.startNode = startNode;
    }
    
    void showDialog(String msg) {
		dialog = new SpiderDialog(getView().getMainFrame(), false);
		dialog.setPlugin(ExtensionSpider.this);
		dialog.setVisible(true);
		dialog.getTxtDisplay().setText(msg);
		spider = null;

    }
    
	/**
	 * This method initializes popupMenuSpider	
	 * 	
	 * @return org.parosproxy.paros.plugin.Spider.PopupMenuSpider	
	 */    
	PopupMenuSpider getPopupMenuSpider() {
		if (popupMenuSpider == null) {
			popupMenuSpider = new PopupMenuSpider();


			popupMenuSpider.setExtension(this);
		}
		return popupMenuSpider;
	}
	
	void clear() {
	    spider = null;
	    System.gc();
	}
	
	/**
	 * This method initializes spiderPanel	
	 * 	
	 * @return org.parosproxy.paros.extension.spider.SpiderPanel	
	 */    
	private SpiderPanel getSpiderPanel() {
		if (spiderPanel == null) {
			spiderPanel = new SpiderPanel();
		}
		return spiderPanel;
	}

    /* (non-Javadoc)
     * @see org.parosproxy.paros.extension.SessionChangedListener#sessionChanged(org.parosproxy.paros.model.Session)
     */
    @Override
    public void sessionChanged(Session session) {
        getSpiderPanel().clear();
        
    }


	/**
	 * This method initializes optionsSpiderPanel	
	 * 	
	 * @return org.parosproxy.paros.extension.spider.OptionsSpiderPanel	
	 */    
	private OptionsSpiderPanel getOptionsSpiderPanel() {
		if (optionsSpiderPanel == null) {
			optionsSpiderPanel = new OptionsSpiderPanel();
		}
		return optionsSpiderPanel;
	}
	/**
	 * This method initializes spiderParam	
	 * 	
	 * @return org.parosproxy.paros.core.spider.SpiderParam	
	 */    
	private SpiderParam getSpiderParam() {
		if (spiderParam == null) {
			spiderParam = new SpiderParam();
		}
		return spiderParam;
	}

    /* (non-Javadoc)
     * @see org.parosproxy.paros.extension.CommandLineListener#execute(org.parosproxy.paros.extension.CommandLineArgument[])
     */
    @Override
    public void execute(CommandLineArgument[] args) {
        String uri = null;

        if (!arguments[ARG_URL_IDX].isEnabled() && (arguments[ARG_SPIDER_IDX].isEnabled())) {
            return;
        }
        
        spider = new Spider(getSpiderParam(), getModel().getOptionsParam().getConnectionParam(), getModel());
        spider.addSpiderListener(this);

        if (arguments[ARG_URL_IDX].isEnabled()) {
            // ZAP: Added type argument.
            Vector<String> v = arguments[ARG_URL_IDX].getArguments();
            for (int i=0; i<v.size(); i++) {
                // ZAP: Removed unnecessary cast.
                uri = v.get(i);
                try {
                    System.out.println("Adding seed " + uri);
                    spider.addSeed(new URI(uri, true));
                } catch (URIException e) {
                	// ZAP: Log exceptions
                	log.warn(e.getMessage(), e);
                }
            }
        }
        
        if (arguments[ARG_SPIDER_IDX].isEnabled()) {
            System.out.println("Starting spider...");
            spider.start();
        }

        while (!spider.isStop()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            	// ZAP: Log exceptions
            	log.warn(e.getMessage(), e);
            }
        }
        System.out.println("Spider completed.");

    }

    private CommandLineArgument[] getCommandLineArguments() {
        arguments[ARG_SPIDER_IDX] = new CommandLineArgument("-spider", 0, null, "", "-spider : run spider.  See other parameters");
        arguments[ARG_URL_IDX] = new CommandLineArgument("-seed", -1, "https{0,1}://\\S+", "Seed should be a URL", "-seed {URL1} {URL2} ... : Add seeds to the spider for crawling.");
        return arguments;
    }

    @Override
    public boolean isDepreciated () {
    	return true;
    }

	@Override
	public void sessionAboutToChange(Session session) {
	}
	@Override
	public String getAuthor() {
		return Constant.PAROS_TEAM;
	}
}  //  @jve:decl-index=0:
