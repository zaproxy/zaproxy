function FindProxyForURL(url, host) {

    if (shExpMatch(host, "*example.com"))
        return "MATCH 1";
    else if (shExpMatch(host, "localhost"))
        return "MATCH 2";
    else if (shExpMatch(host, "www.examp*.org"))
        return "MATCH 3";
    else if (shExpMatch(host, "so?e*e"))
        return "MATCH 4";
    else
        return "FAILURE"
}