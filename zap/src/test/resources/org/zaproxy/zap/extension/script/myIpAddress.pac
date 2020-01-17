function FindProxyForURL(url, host) {

    var ip = myIpAddress();
    if (!shExpMatch(ip, "127.*") && !shExpMatch(ip, "0:0:0:0:0:0:0:1"))
        return "SUCCESS";
    else
        return "FAILURE";
}