function FindProxyForURL(url, host) {

    if (dnsDomainIs(host, "example.com"))
        return "SUCCESS";
    else
        return "FAILURE";
}