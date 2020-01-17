function FindProxyForURL(url, host) {

    if (isInNet(host, "127.0.0.0", "255.0.0.0"))
        return "SUCCESS";
    else
        return "FAILURE";
}