/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 ZAP development team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 */
package org.zaproxy.zap.extension.spider;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.spider.SpiderParam;
import org.zaproxy.zap.spider.filters.MaxChildrenFetchFilter;
import org.zaproxy.zap.spider.filters.MaxChildrenParseFilter;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.view.StandardFieldsDialog;

public class SpiderDialog extends StandardFieldsDialog {

    private static final String FIELD_START = "spider.custom.label.start";
    private static final String FIELD_CONTEXT = "spider.custom.label.context";
    private static final String FIELD_USER = "spider.custom.label.user";
    private static final String FIELD_RECURSE = "spider.custom.label.recurse";
    private static final String FIELD_ADVANCED = "spider.custom.label.adv"; 
    private static final String FIELD_MAX_DEPTH = "spider.custom.label.maxDepth"; 
    private static final String FIELD_MAX_CHILDREN = "spider.custom.label.maxChildren"; 
    private static final String FIELD_SEND_REFERER = "spider.custom.label.sendReferer";
    private static final String FIELD_PROCESS_FORMS = "spider.custom.label.processForms"; 
    private static final String FIELD_POST_FORMS = "spider.custom.label.postForms"; 
    private static final String FIELD_PARSE_COMMENTS = "spider.custom.label.parseComments"; 
    private static final String FIELD_PARSE_ROBOTS = "spider.custom.label.parseRobots"; 
    private static final String FIELD_PARSE_SITEMAP = "spider.custom.label.sitemap"; 
    private static final String FIELD_PARSE_SVN = "spider.custom.label.parseSvn"; 
    private static final String FIELD_PARSE_GIT = "spider.custom.label.parseGit"; 
    private static final String FIELD_HANDLE_ODATA = "spider.custom.label.handleOdata"; 

    private static Logger logger = Logger.getLogger(SpiderDialog.class);

    private static final long serialVersionUID = 1L;

    private JButton[] extraButtons = null;

    private ExtensionSpider extension = null;
	private SpiderParam spiderParam = null;
    
    private ExtensionUserManagement extUserMgmt = (ExtensionUserManagement) Control.getSingleton().getExtensionLoader()
			.getExtension(ExtensionUserManagement.NAME);
    
    private Target target = null;
    private int maxChildrenToCrawl = 0;	// This is not persisted anywhere

    public SpiderDialog(ExtensionSpider ext, Frame owner, Dimension dim) {
        super(owner, "spider.custom.title", dim, new String[]{
            "spider.custom.tab.scope",
            "spider.custom.tab.adv"
        });
        this.extension = ext;

        // The first time init to the default options set, after that keep own copies
        reset(false);
    }

    public void init(Target target) {
        if (target != null) {
        	// If one isnt specified then leave the previously selected one
        	this.target = target;
        }
        logger.debug("init " + this.target);

        this.removeAllFields();

        this.addTargetSelectField(0, FIELD_START, this.target, false, false);
        this.addComboField(0, FIELD_CONTEXT, new String[] {}, "");
        this.addComboField(0, FIELD_USER, new String[] {}, "");
        this.addCheckBoxField(0, FIELD_RECURSE, true);
        // This option is always read from the 'global' options
        this.addCheckBoxField(0, FIELD_ADVANCED, getSpiderParam().isShowAdvancedDialog());
        this.addPadding(0);

        // Advanced options
        this.addNumberField(1, FIELD_MAX_DEPTH, 1, 19, getSpiderParam().getMaxDepth());
        this.addNumberField(1, FIELD_MAX_CHILDREN, 0, Integer.MAX_VALUE, maxChildrenToCrawl);
        this.addCheckBoxField(1, FIELD_SEND_REFERER, getSpiderParam().isSendRefererHeader());
        this.addCheckBoxField(1, FIELD_PROCESS_FORMS, getSpiderParam().isProcessForm());
        this.addCheckBoxField(1, FIELD_POST_FORMS, getSpiderParam().isPostForm());
        this.addCheckBoxField(1, FIELD_PARSE_COMMENTS, getSpiderParam().isParseComments());
        this.addCheckBoxField(1, FIELD_PARSE_ROBOTS, getSpiderParam().isParseRobotsTxt());
        this.addCheckBoxField(1, FIELD_PARSE_SITEMAP, getSpiderParam().isParseSitemapXml());
        this.addCheckBoxField(1, FIELD_PARSE_SVN, getSpiderParam().isParseSVNEntries());
        this.addCheckBoxField(1, FIELD_PARSE_GIT, getSpiderParam().isParseGit());
        this.addCheckBoxField(1, FIELD_HANDLE_ODATA, getSpiderParam().isHandleODataParametersVisited());
        this.addPadding(1);

    	if (! getBoolValue(FIELD_PROCESS_FORMS)) {
        	setFieldValue(FIELD_POST_FORMS, false);
        	getField(FIELD_POST_FORMS).setEnabled(false);
    	}

        this.addFieldListener(FIELD_CONTEXT, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUsers();
            }
        });
        this.addFieldListener(FIELD_PROCESS_FORMS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (getBoolValue(FIELD_PROCESS_FORMS)) {
                	getField(FIELD_POST_FORMS).setEnabled(true);
            	} else {
                	setFieldValue(FIELD_POST_FORMS, false);
                	getField(FIELD_POST_FORMS).setEnabled(false);
            	}
            }
        });
        this.addFieldListener(FIELD_ADVANCED, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAdvancedTabs(getBoolValue(FIELD_ADVANCED));
            }
        });

        if (target != null) {
	        // Set up the fields if a node has been specified, otherwise leave as previously set
	        this.targetSelected(FIELD_START, this.target);
	        this.setUsers();
        }
        
        if ( ! extension.getSpiderParam().isShowAdvancedDialog()) {
        	// Remove all but the first tab
        	this.setAdvancedTabs(false);
        }
        
        this.pack();
    }
    
	private SpiderParam getSpiderParam() {
		if (spiderParam == null) {
			// First time in clone the global options, after that keep the last ones the user set
			spiderParam = (SpiderParam) extension.getSpiderParam().clone();
		}
		return spiderParam;
	}


	private void setAdvancedTabs(boolean visible) {
		// Show/hide all except from the first tab
		this.setTabsVisible (new String[] {
	            "spider.custom.tab.adv"
	        }, visible);
	}
	
    @Override
	public String getHelpIndex() {
		return "ui.dialogs.spider";
	}

    @Override
    public void targetSelected(String field, Target node) {
        List<String> ctxNames = new ArrayList<String>();
        if (node != null) {
            // The user has selected a new node
            this.target = node;
            if (node.getStartNode() != null) {
                Session session = Model.getSingleton().getSession();
                List<Context> contexts = session.getContextsForNode(node.getStartNode());
                for (Context context : contexts) {
                	ctxNames.add(context.getName());
                }
            	
            } else if (node.getContext() != null) {
            	ctxNames.add(node.getContext().getName());
            }
        }
        this.setComboFields(FIELD_CONTEXT, ctxNames, "");
       	this.getField(FIELD_CONTEXT).setEnabled(ctxNames.size() > 0);
    }
    
    private Context getSelectedContext() {
    	String ctxName = this.getStringValue(FIELD_CONTEXT);
    	if (this.extUserMgmt != null && ! this.isEmptyField(FIELD_CONTEXT)) {
            Session session = Model.getSingleton().getSession();
            return session.getContext(ctxName);
    	}
    	return null;
    }

    private User getSelectedUser() {
    	Context context = this.getSelectedContext();
    	if (context != null) {
        	String userName = this.getStringValue(FIELD_USER);
        	List<User> users = this.extUserMgmt.getContextUserAuthManager(context.getIndex()).getUsers();
        	for (User user : users) {
        		if (userName.equals(user.getName())) {
        			return user;
        		}
            }
    	}
    	return null;
    }

    private void setUsers() {
    	Context context = this.getSelectedContext();
        List<String> userNames = new ArrayList<String>();
    	if (context != null) {
        	List<User> users = this.extUserMgmt.getContextUserAuthManager(context.getIndex()).getUsers();
        	userNames.add("");	// The default should always be 'not specified'
        	for (User user : users) {
        		userNames.add(user.getName());
            }
    	}
        this.setComboFields(FIELD_USER, userNames, "");
       	this.getField(FIELD_USER).setEnabled(userNames.size() > 1);	// Theres always 1..
    }

    private void reset(boolean refreshUi) {
    	// Reset to the global options
		spiderParam = null;
    	
        if (refreshUi) {
            init(target);
            repaint();
        }
    }

    @Override
    public String getSaveButtonText() {
        return Constant.messages.getString("spider.custom.button.scan");
    }

    @Override
    public JButton[] getExtraButtons() {
        if (extraButtons == null) {
            JButton resetButton = new JButton(Constant.messages.getString("spider.custom.button.reset"));
            resetButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    reset(true);
                }
            });

            extraButtons = new JButton[]{resetButton};
        }
        return extraButtons;
    }

    @Override
    public void save() {
        Object[] contextSpecificObjects = null;
        if (this.getBoolValue(FIELD_ADVANCED)) {
        	// Set the advanced options
        	spiderParam.setMaxDepth(this.getIntValue(FIELD_MAX_DEPTH));
        	spiderParam.setSendRefererHeader(this.getBoolValue(FIELD_SEND_REFERER));
        	spiderParam.setProcessForm(this.getBoolValue(FIELD_PROCESS_FORMS));
        	spiderParam.setPostForm(this.getBoolValue(FIELD_POST_FORMS));
        	spiderParam.setParseComments(this.getBoolValue(FIELD_PARSE_COMMENTS));
        	spiderParam.setParseRobotsTxt(this.getBoolValue(FIELD_PARSE_ROBOTS));
        	spiderParam.setParseSitemapXml(this.getBoolValue(FIELD_PARSE_SITEMAP));
        	spiderParam.setParseSVNEntries(this.getBoolValue(FIELD_PARSE_SVN));
        	spiderParam.setParseGit(this.getBoolValue(FIELD_PARSE_GIT));
        	spiderParam.setHandleODataParametersVisited(this.getBoolValue(FIELD_HANDLE_ODATA));
        	spiderParam.setThreadCount(extension.getSpiderParam().getThreadCount());
        	
        	maxChildrenToCrawl = this.getIntValue(FIELD_MAX_CHILDREN);
        	
        	if (maxChildrenToCrawl > 0) {
        		// Add the filters to filter on maximum number of children
        		MaxChildrenFetchFilter maxChildrenFetchFilter = new MaxChildrenFetchFilter();
        		maxChildrenFetchFilter.setMaxChildren(maxChildrenToCrawl);
        		maxChildrenFetchFilter.setModel(extension.getModel());
        		
        		MaxChildrenParseFilter maxChildrenParseFilter = new MaxChildrenParseFilter();
        		maxChildrenParseFilter.setMaxChildren(maxChildrenToCrawl);
        		maxChildrenParseFilter.setModel(extension.getModel());
        		
		        contextSpecificObjects = new Object[]{
			            spiderParam,
			            maxChildrenFetchFilter,
			            maxChildrenParseFilter
			        };
        	} else {
		        contextSpecificObjects = new Object[]{
			            spiderParam
			        };
        	}
    	}
        
        // Save the adv option permanently for next time
        extension.getSpiderParam().setShowAdvancedDialog(this.getBoolValue(FIELD_ADVANCED));
        
        target.setRecurse(this.getBoolValue(FIELD_RECURSE));

        this.extension.startScan(
        		target.getDisplayName(),
                target,
                getSelectedUser(), 
                contextSpecificObjects);
    }

    @Override
    public String validateFields() {
        if (this.target == null || !this.target.isValid()) {
            return Constant.messages.getString("spider.custom.nostart.error");
        }
        
        return null;
    }
}
