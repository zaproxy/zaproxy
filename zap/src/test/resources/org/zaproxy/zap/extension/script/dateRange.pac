function FindProxyForURL(url, host) { // host is equal to "" or to "GMT" for testing purpose

    var result = '';
    var year = 0;
    var month = 0;
    var day = 0;

    for (y = 2016; y < 100000; y++) {
        if (dateRange(y, host)) {
            result = result + y + '-';
            year = y;
            break;
        }
    }

    var months = ["JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"];
    for (m = 0; m < 12; m++) {
        if (dateRange(months[m], host)) {
            result = result + (m + 1) + '-';
            month = m;
            break;
        }
    }

    for (d = 1; d <= 31; d++) {
        if (dateRange(d, host)) {
            result += d;
            day = d;
            break;
        }
    }

    if (dateRange(day, months[month], host) && dateRange(months[month], year, host)
        && dateRange(day, months[month], year, host) && dateRange(day, day + 1, host)
        && dateRange(months[month - 1], months[month], host) && dateRange(year, year, host)
        && dateRange(day, months[month - 1], day, months[month], host)
        && dateRange(months[month - 1], year, months[month], year, host)
        && dateRange(day, months[month - 1], year, day, months[month], year + 1, host)
        && dateRange(year - 1, year + 1, host) && dateRange("test is false", host) == false) {
        return result;
    }

    return "FAILURE"
}