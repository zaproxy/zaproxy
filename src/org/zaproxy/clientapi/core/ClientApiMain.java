package org.zaproxy.clientapi.core;

import java.io.File;
import java.net.ConnectException;
import java.util.HashMap;

public class ClientApiMain {

    private HashMap<String, Object> params = new HashMap<>();
    private String zapaddr = "localhost";
    private int zapport = 8090;
    private Task task;
    private ClientApi api;
    private boolean debug = false;

    private enum Task{
        stop, checkAlerts, saveSession, newSession, activeScanUrl
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
                    api.stopZap();
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
                case saveSession:
                    if (params.get("sessionName") == null){
                        System.out.println("No session name supplied\n");
                        showHelp();
                        System.exit(1);
                    }
                    api.saveSession((String)params.get("sessionName"));
                    break;
                case newSession:
                    if (params.get("sessionName") == null){
                        api.newSession();
                    }else{
                        api.newSession((String)params.get("sessionName"));
                    }
                    break;
                case activeScanUrl:
                    if (params.get("url") == null){
                        System.out.println("No url supplied\n");
                        showHelp();
                        System.exit(1);
                    }else{
                        api.activeScanUrl((String)params.get("url"));
                    }
                    break;
            }
        } catch (ConnectException e){
            System.out.println(e.getMessage()+String.format(": zapaddr=%s, zapport=%d\n", zapaddr, zapport));
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
                "\tsaveSession\n"+
                "\tnewSession\n";
        } else{
            switch (task){
                case stop:
                    help = "usage: stop [zapaddr={ip}] [zapport={port}]\n\n" +
                            "Examples:\n\t" +
                                "1. Type 'java -jar zap-api.jar stop' \n\t\t" +
                                    "Stop zap listening on default settings (localhost:8090)\n\t" +
                                "2. Type 'java -jar zap-api.jar stop zapaddr=192.168.1.1' \n\t\t" +
                                    "Stop zap listening on 192.168.1.1:8090\n\t" +
                                "3. Type 'java -jar zap-api.jar stop zapport=7080' \n\t\t" +
                                    "Stop zap listening on localhost:7080\n\t" +
                                "4. Type 'java -jar zap-api.jar stop zapaddr=192.168.1.1 zapport=7080' \n\t\t" +
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
                case activeScanUrl:
                    help = "usage: activeScanUrl url={url} [zapaddr={ip}] [zapport={port}]\n\n" +
                            "Examples:\n\t" +
                            "1. Type 'java -jar zap-api.jar activeScanUrl url=http://myurl.com/' \n\t\t" +
                            "Execute and active scan on http://myurl.com/ using zap listening on localhost:8090\n\t" +
                            "2. Type 'java -jar zap-api.jar activeScanUrl url=http://myurl.com/' zapaddr=192.168.1.1 zapport=7080' \n\t\t" +
                            "Execute and active scan on http://myurl.com/ using zap listening on 192.168.1.1:7080\n\t";
                    break;
            }
        }
        System.out.println(help);
    }
}
