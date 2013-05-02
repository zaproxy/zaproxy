/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension;

import org.parosproxy.paros.model.SiteNode;

/**
 * This interface should be implemented by extensions that wish to write data to 
 * XML report. getXML() method should return well-formed XML fragment (without the
 * <?xml> declaration) . This method is called by ReportLastScan class
 * @author alla
 */
public interface XmlReporterExtension {
    
    String getXml(SiteNode site);
    
}
