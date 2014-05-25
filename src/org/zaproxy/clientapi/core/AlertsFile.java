package org.zaproxy.clientapi.core;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AlertsFile {
    public static void saveAlertsToFile(List<Alert> requireAlerts, List<Alert> reportAlerts, List<Alert> ignoredAlerts, File outputFile) throws JDOMException, IOException {
        Element alerts = new Element("alerts");
        Document alertsDocument = new Document(alerts);
        alertsDocument.setRootElement(alerts);
        if (reportAlerts.size() > 0){
            Element alertsFound = new Element("alertsFound");
            alertsFound.setAttribute("alertsFound", Integer.toString(reportAlerts.size()));
            for (Alert alert : reportAlerts){
               createAlertXMLElements(alertsFound, alert);
            }
            alertsDocument.getRootElement().addContent(alertsFound);
        }

        if (requireAlerts.size() > 0){
            Element alertsNotFound = new Element("alertsNotFound");
            alertsNotFound.setAttribute("alertsNotFound", Integer.toString(requireAlerts.size()));
            for (Alert alert : requireAlerts){
                createAlertXMLElements(alertsNotFound, alert);
            }
            alertsDocument.getRootElement().addContent(alertsNotFound);
        }

        if (ignoredAlerts.size() > 0){
            Element ignoredAlertsFound = new Element("ignoredAlertsFound");
            ignoredAlertsFound.setAttribute("ignoredAlertsFound", Integer.toString(ignoredAlerts.size()));
            for (Alert alert : ignoredAlerts){
                createAlertXMLElements(ignoredAlertsFound, alert);
            }
            alertsDocument.getRootElement().addContent(ignoredAlertsFound);
        }

        writeAlertsToFile(outputFile, alertsDocument);
    }
    
    private static void writeAlertsToFile(File outputFile, Document doc) {

        XMLOutputter xmlOutput = new XMLOutputter();

        xmlOutput.setFormat(Format.getPrettyFormat());
        try {
            xmlOutput.output(doc, new FileWriter(outputFile));
            System.out.println("alert xml report saved to: "+outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createAlertXMLElements(Element alertsFound, Alert alert) {
        Element alertElement = new Element("alert");
        if (alert.getAlert() != null)
            alertElement.setAttribute("alert", alert.getAlert());
        if (alert.getRisk() != null)
            alertElement.setAttribute("risk", alert.getRisk().name());
        if (alert.getUrl() != null)
            alertElement.setAttribute("confidence", alert.getConfidence().name());
        if (alert.getUrl() != null)
            alertElement.setAttribute("url", alert.getUrl());
        if (alert.getParam() != null)
            alertElement.setAttribute("param", alert.getParam());
        if (alert.getOther() != null)
            alertElement.setAttribute("other", alert.getOther());
        if (alert.getAttack() != null)
            alertElement.setAttribute("attack", alert.getAttack());
        if (alert.getDescription() != null)
            alertElement.setAttribute("description", alert.getDescription());
        if (alert.getSolution() != null)
            alertElement.setAttribute("solution", alert.getSolution());
        if (alert.getReference() != null)
            alertElement.setAttribute("reference", alert.getReference());
        alertsFound.addContent(alertElement);
    }

    public static List<Alert> getAlertsFromFile(File file, String alertType) throws JDOMException, IOException {
        List<Alert> alerts =  new ArrayList<>();
        SAXBuilder parser = new SAXBuilder();
        Document alertsDoc = parser.build(file);
        @SuppressWarnings("unchecked")
        List<Element> alertElements = alertsDoc.getRootElement().getChildren(alertType);
        for (Element element: alertElements){
            Alert alert = new Alert(
                    element.getAttributeValue("alert"),
                    element.getAttributeValue("url"),
                    element.getAttributeValue("risk"),
                    element.getAttributeValue("confidence"),
                    element.getAttributeValue("param"),
                    element.getAttributeValue("other"));
            alerts.add(alert);
        }
        return alerts;
    }
}
