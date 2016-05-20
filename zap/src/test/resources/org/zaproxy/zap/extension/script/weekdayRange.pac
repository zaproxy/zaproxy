function FindProxyForURL(url, host) {

    if (shExpMatch(url, "GMT") && weekdayRange(host, "GMT"))
        return "SUCCESS GMT";
    else if (shExpMatch(url, "locale") && weekdayRange(host))
        return "SUCCESS LOCALE";
    else if (shExpMatch(url, "GMTRange") && weekdayRange(host, "FRI", "GMT"))
        return "SUCCESS GMT RANGE";
    else if (shExpMatch(url, "localeRange") && weekdayRange(host, "FRI"))
        return "SUCCESS LOCALE RANGE";
    else if (shExpMatch(url, "FAILURE") && !weekdayRange("FAILURE"))
        return "FAILURE"
}