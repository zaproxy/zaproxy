function FindProxyForURL(url, host) { // host is equal to "" or to "GMT" for testing purpose

    var result = '';
    var hour = 0;
    var min = 0;
    var sec = 0;

    for (i = 0; i < 2; i++) { // In case where the time changed during the execution of the following loops
        for (h = 0; h < 24; h++) {
            if (timeRange(h, host)) {
                result = h + '-';
                hour = h;
                break;
            }
        }
        for (m = 0; m < 60; m++) {
            if (timeRange(hour, m, hour, m + 1, host)) {
                result = result + m + '-';
                min = m;
                break;
            }
        }
        for (s = 0; s < 60; s++) {
            if (timeRange(hour, min, s, host)) {
                result = result + s;
                sec = s;
                i = 2; // to break the i loop
                break;
            }
        }
    }

    if (timeRange(hour, hour + 2, host) && timeRange(hour - 1, hour + 2, host) && !timeRange(hour - 1, hour, host)
        && timeRange(hour, min, hour, min + 2, host) && !timeRange(hour, min - 1, hour, min, host)
        && timeRange(hour, min, sec, hour, min + 1, sec, host) && !timeRange(hour, min, sec - 1, hour, min, sec, host)
        && !timeRange("test is false", host)) {
        return "SUCCESS";
    }

    return "FAILURE";
}