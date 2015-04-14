/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The Zed Attack Proxy Team
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
package org.zaproxy.clientapi.core;

import java.io.File;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;

public class ClientApiMain {

    private HashMap<String, Object> params = new HashMap<>();
    private String zapaddr = "localhost";
    private int zapport = 8090;
    private Task task;
    private ClientApi api;
    private boolean debug = false;

    private enum Task{
        stop, showAlerts, checkAlerts, saveSession, newSession, activeScanUrl, activeScanSiteInScope,
        addExcludeRegexToContext, addIncludeRegexToContext, addIncludeOneMatchingNodeToContext
    }

    public static void main(String[] args){
        new ClientApiMain(args);
    }

    public ClientApiMain(String[] args){
        initialize(args);
        try {
            executeTask();
        }catch (Exception e){
            e.printStackTrace();
            showHelp();
        }
    }

    private void executeTask() throws Exception {
        try {
            switch(task){
                case stop:
                    api.core.shutdown((String)params.get("apikey"));
                    break;
                case checkAlerts:
                    if (params.get("alertsFile") == null){
                        System.out.println("No Alerts File Path Supplied\n");
                        showHelp();
                        System.exit(1);
                    }
                    File alertsFile = (File)params.get("alertsFile");
                    if (!alertsFile.exists()){
                        System.out.println("File not Found: "+alertsFile.getAbsolutePath());
                        showHelp();
                        System.exit(1);
                    }
                    if (params.get("outputFile") == null){
                        api.checkAlerts(
                                AlertsFile.getAlertsFromFile(alertsFile, "ignoreAlert"),
                                AlertsFile.getAlertsFromFile(alertsFile, "requireAlert"));
                    }else{
                        File outFile = (File)params.get("outputFile");
                        try {
                            api.checkAlerts(
                                    AlertsFile.getAlertsFromFile(alertsFile, "ignoreAlert"),
                                    AlertsFile.getAlertsFromFile(alertsFile, "requireAlert"),
                                    outFile);
                        } catch (AssertionError e){
                            System.out.println(e.getMessage());
                            System.exit(1);
                        }
                    }
                    break;
                case showAlerts:
                	List<Alert> alerts = api.getAlerts(null, -1, -1);
                	for (Alert alert : alerts) {
                        System.out.println(alert.toString());
                	}
                    break;
                case saveSession:
                    if (params.get("sessionName") == null){
                        System.out.println("No session name supplied\n");
                        showHelp();
                        System.exit(1);
                    }
                    api.core.saveSession((String)params.get("apikey"), (String)params.get("sessionName"), "true");
                    break;
                case newSession:
                    if (params.get("sessionName") == null){
                        api.core.newSession((String)params.get("apikey"), "", "true");
                    }else{
                        api.core.newSession((String)params.get("apikey"), (String)params.get("sessionName"), "true");
                    }
                    break;
                case activeScanUrl:
                    if (params.get("url") == null){
                        System.out.println("No url supplied\n");
                        showHelp();
                        System.exit(1);
                    }else{
                        api.ascan.scan((String)params.get("apikey"), (String)params.get("url"), "true", "false", "", "", "");
                    }
                    break;
                case activeScanSiteInScope:
                    checkForUrlParam();
                    api.activeScanSiteInScope((String)params.get("apikey"), (String)params.get("url"));
                    break;
                case addExcludeRegexToContext:
                    checkForContextNameParam();
                    checkForRegexParam();
                    api.addExcludeFromContext((String)params.get("apikey"), (String)params.get("contextName"), (String)params.get("regex"));
                    break;
                case addIncludeRegexToContext:
                    checkForContextNameParam();
                    checkForRegexParam();
                    api.addIncludeInContext((String)params.get("apikey"), (String)params.get("contextName"), (String)params.get("regex"));
                    break;
                case addIncludeOneMatchingNodeToContext:
                    checkForContextNameParam();
                    checkForRegexParam();
                    api.includeOneMatchingNodeInContext((String)params.get("apikey"), (String)params.get("contextName"), (String)params.get("regex"));
                    break;
            }
        } catch (ConnectException e){
            System.out.println(e.getMessage()+String.format(": zapaddr=%s, zapport=%d\n", zapaddr, zapport));
            showHelp();
            System.exit(1);
        }
    }

    private void checkForRegexParam() {
        if(params.get("regex") == null){
            System.out.println("No regex supplied\n");
            showHelp();
            System.exit(1);
        }
    }

    private void checkForContextNameParam() {
        if (params.get("contextName") == null){
            System.out.println("No context name supplied\n");
            showHelp();
            System.exit(1);
        }
    }

    private void checkForUrlParam() {
        if (params.get("url") == null){
            System.out.println("No url supplied\n");
            showHelp();
            System.exit(1);
        }
    }

    private void initialize(String[] args) {
        if (args.length > 0){
            if (args[0].equalsIgnoreCase("help")){
                try {
                    setTask(args[1]);
                }catch (IndexOutOfBoundsException e){
                    showHelp();
                    System.exit(1);
                }
                showHelp();
                System.exit(0);
            }
            setTask(args[0]);
            for (String arg: args){
                String[] pair = arg.split("=");
                if (pair.length == 2){
                    if (pair[0].equalsIgnoreCase("zapaddr")){
                        zapaddr = pair[1];
                    } else if(pair[0].equalsIgnoreCase("zapport")){
                        try {
                            zapport = Integer.parseInt(pair[1]);
                        } catch (NumberFormatException e){
                            System.out.println("Invalid value to zapport, must be in integer: "+pair[1]);
                            showHelp();
                            System.exit(1);
                        }
                    }else if(pair[0].equalsIgnoreCase("debug") && pair[1].equalsIgnoreCase("true")){
                        debug = true;
                    }else if(pair[0].contains("File")){
                        params.put(pair[0], new File(pair[1]));
                    }
                    else{
                        params.put(pair[0], pair[1]);
                    }
                }
            }
        } else {
            showHelp();
            System.exit(1);
        }
        api = new ClientApi(zapaddr, zapport, debug);
    }

    private void setTask(String arg) {
        try {
            task = Task.valueOf(arg);
        } catch (IllegalArgumentException e){
            System.out.println("Unknown Task: "+arg);
            showHelp();
            System.exit(1);
        }
    }

    private void showHelp() {
        String help = "";
        if (task == null){
            help = "usage: java -jar zap-api.jar <subcommand> [args]\n\n"+
            "Type 'java -jar zap-api.jar help <subcommand>' for help on a specific subcommand.\n\n" +
            "Available subcommands:\n"+
                "\tstop\n"+
                "\tcheckAlerts\n"+
                "\tshowAlerts\n"+
                "\tsaveSession\n"+
                "\tnewSession\n";
        } else{
        	// TODO add case for activeScanSiteInScope
            switch (task){
                case stop:
                    help = "usage: stop [zapaddr={ip}] [zapport={port}]\n\n" +
                            "Examples:\n\t" +
                                "1. Type 'java -jar zap-api.jar stop' \n\t\t" +
                                    "Stop zap listening on default settings (localhost:8090)\n\t" +
                                "2. Type 'java -jar zap-api.jar stop zapaddr=192.168.1.1 apikey=1234' \n\t\t" +
                                    "Stop zap listening on 192.168.1.1:8090\n\t" +
                                "3. Type 'java -jar zap-api.jar stop zapport=7080 apikey=1234' \n\t\t" +
                                    "Stop zap listening on localhost:7080\n\t" +
                                "4. Type 'java -jar zap-api.jar stop zapaddr=192.168.1.1 zapport=7080 apikey=1234' \n\t\t" +
                                    "Stop zap listening on 192.168.1.1:7080\n\n";
                    break;
                case checkAlerts:
                    help = "usage: checkAlerts alertsFile={PATH} [outputFile={PATH}] [zapaddr={ip}] [zapport={port}]\n\n" +
                            "Examples\n\t" +
                                "1. Type 'java -jar zap-api.jar checkAlerts alertsFile=\"C:\\Users\\me\\My Documents\\alerts.xml\"' \n\t\t" +
                                    "Check alerts ignoring alerts from alertsFile, looking for required alerts from alertsFile, using zap listening on localhost:8090\n\t" +
                                "2. Type 'java -jar zap-api.jar checkAlerts alertsFile=\"C:\\Users\\me\\My Documents\\alerts.xml\" outputFile=\"C:\\Users\\me\\My Documents\\report.xml\"' \n\t\t" +
                                    "Check alerts ignoring alerts from alertsFile, looking for required alerts from alertsFile. Outputting results to report.xml, using zap listening on localhost:8090\n\t" +
                                "3. Type 'java -jar zap-api.jar checkAlerts alertsFile=\"C:\\Users\\me\\My Documents\\alerts.xml\" outputFile=\"C:\\Users\\me\\My Documents\\report.xml\"' zapaddr=192.168.1.1 zapport=7080' \n\t\t" +
                                    "Check alerts ignoring alerts from alertsFile, looking for required alerts from alertsFile. Outputting results to report.xml, using zap listening on 192.168.1.1:7080\n" +
                            "Note: for paths containing spaces ensure path is enclosed in quotes\n\n";
                    break;
                case showAlerts:
                    help = "usage: showAlerts [zapaddr={ip}] [zapport={port}]\n\n" +
                            "Examples:\n\t" +
                                "1. Type 'java -jar zap-api.jar showAlerts' \n\t\t" +
                                    "Show alerts, using zap listening on default settings (localhost:8090)\n\t" +
                                "2. Type 'java -jar zap-api.jar showAlerts zapaddr=192.168.1.1' \n\t\t" +
                                    "Show alerts, using zap listening on 192.168.1.1:8090\n\t" +
                                "3. Type 'java -jar zap-api.jar showAlerts zapport=7080' \n\t\t" +
                                    "Show alerts, using zap listening on localhost:7080\n\t" +
                                "4. Type 'java -jar zap-api.jar showAlerts zapaddr=192.168.1.1 zapport=7080' \n\t\t" +
                                    "Show alerts, using zap listening on 192.168.1.1:7080\n\n";
                    break;
                case saveSession:
                    help = "usage: saveSession sessionName={PATH} [zapaddr={ip}] [zapport={port}]\n\n" +
                            "Examples:\n\t" +
                                "1. Type 'java -jar zap-api.jar saveSession sessionName=\"Users/me/My Documents/mysession/mysessionfile\"' \n\t\t" +
                                    "Save zap session using zap listening on localhost:8090\n\t" +
                                "2. Type 'java -jar zap-api.jar saveSession sessionName=\"Users/me/My Documents/mysession/mysessionfile\" zapaddr=192.168.1.1 zapport=7080' \n\t\t" +
                                    "Save zap session using zap listening on 192.168.1.1:7080\nNote: for paths containing spaces ensure path is enclosed in quotes\n\n";
                    break;
                case newSession:
                    help = "usage: newSession [sessionName={PATH}] [zapaddr={ip}] [zapport={port}]\n\n" +
                            "Examples:\n\t" +
                                "1. Type 'java -jar zap-api.jar newSession' \n\t\t" +
                                    "Start new session using zap listening on localhost:8090\n\t" +
                                "2. Type 'java -jar zap-api.jar newSession zapaddr=192.168.1.1 zapport=7080' \n\t\t" +
                                    "Start new session using zap listening on 192.168.1.1:7080\n\t" +
                                "3. Type 'java -jar zap-api.jar newSession sessionName=\"Users/me/My Documents/mysession/newsession\"' \n\t\t" +
                                    "Start new session using zap listening on localhost:8090, creating session files at /Users/me/My Documents/mysession/newsession\n\t" +
                                "4. Type 'java -jar zap-api.jar newSession sessionName=\"Users/me/My Documents/mysession/mysessionfile\" zapaddr=192.168.1.1 zapport=7080' \n\t\t" +
                                    "Start new session using zap listening on 192.168.1.1:7080, creating session files at /Users/me/My Documents/mysession/newsession\n" +
                            "Note: for paths containing spaces ensure path is enclosed in quotes";
                    break;
                case activeScanUrl:
                    help = "usage: activeScanUrl url={url} [zapaddr={ip}] [zapport={port}]\n\n" +
                            "Examples:\n\t" +
                            "1. Type 'java -jar zap-api.jar activeScanUrl url=http://myurl.com/' \n\t\t" +
                            "Execute and active scan on http://myurl.com/ using zap listening on localhost:8090\n\t" +
                            "2. Type 'java -jar zap-api.jar activeScanUrl url=http://myurl.com/' zapaddr=192.168.1.1 zapport=7080' \n\t\t" +
                            "Execute and active scan on http://myurl.com/ using zap listening on 192.168.1.1:7080\n\t";
                    break;
                case addExcludeRegexToContext:
                    help = "usage: addExcludeRegexToContext contextName={contextName} regex={regex} [zapaddr={ip}] [zapport={port}]\n\n" +
                            "Examples:\n\t" +
                            "1. Type 'java -jar zap-api.jar addExcludeRegexToContext contextName=1 regex=\\Qhttp://example.com/area\\E.* \n\t\t" +
                            "Urls that match the regex will be excluded from scope using context '1' using zap listening on localhost:8090\n\t" +
                            "2. Type 'java -jar zap-api.jar addExcludeRegexToContext url=http://myurl.com/' zapaddr=192.168.1.1 zapport=7080' \n\t\t" +
                            "Urls that match the regex will be excluded from scope using context '1' using zap listening on 192.168.1.1:7080\n\t";
                    break;
                case addIncludeRegexToContext:
                    help = "usage: addIncludeRegexToContext contextName={contextName} regex={regex} [zapaddr={ip}] [zapport={port}]\n\n" +
                            "Examples:\n\t" +
                            "1. Type 'java -jar zap-api.jar addIncludeRegexToContext contextName=1 regex=\\Qhttp://example.com/area\\E.* \n\t\t" +
                            "Urls that match the regex will be included in scope using context '1' using zap listening on localhost:8090\n\t" +
                            "2. Type 'java -jar zap-api.jar addIncludeRegexToContext url=http://myurl.com/' zapaddr=192.168.1.1 zapport=7080' \n\t\t" +
                            "Urls that match the regex will be included in scope using context '1' using zap listening on 192.168.1.1:7080\n\t";
                    break;
                case addIncludeOneMatchingNodeToContext:
                    help = "usage: addIncludeOneMatchingNodeToContext contextName={contextName} regex={regex} [zapaddr={ip}] [zapport={port}]\n\n" +
                            "Examples:\n\t" +
                            "1. Type 'java -jar zap-api.jar addIncludeOneMatchingNodeToContext contextName=1 regex=\\Qhttp://example.com/area\\E.* \n\t\t" +
                            "The first url from the current session that matches the regex will be included in scope using context '1'. Any other matching url will be excluded from scope using zap listening on localhost:8090\n\t" +
                            "2. Type 'java -jar zap-api.jar addIncludeOneMatchingNodeToContext url=http://myurl.com/' zapaddr=192.168.1.1 zapport=7080' \n\t\t" +
                            "The first url from the current session that matches the regex will be included in scope using context '1'. Any other matching url will be excluded from scope using context '1' using zap listening on 192.168.1.1:7080\n\t";
                    break;
            }
        }
        System.out.println(help);
    }
}
