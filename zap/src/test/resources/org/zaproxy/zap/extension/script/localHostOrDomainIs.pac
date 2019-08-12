function FindProxyForURL(url, host) {

    if (localHostOrDomainIs(host, "www.example.com"))
        return "SUCCESS";
    else
        return "FAILURE";
}