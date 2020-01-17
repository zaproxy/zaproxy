function FindProxyForURL(url, host) {

    if (dnsResolve(host) == "127.0.0.1")
        return "SUCCESS";
    else
        return "FAILURE";
}