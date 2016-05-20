function FindProxyForURL(url, host) {

    if (isPlainHostName(host))
        return "SUCCESS";
    else
        return "FAILURE";
}