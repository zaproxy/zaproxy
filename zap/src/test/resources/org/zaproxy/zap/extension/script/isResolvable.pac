function FindProxyForURL(url, host) {

    if (isResolvable(host))
        return "SUCCESS";
    else
        return "FAILURE";
}