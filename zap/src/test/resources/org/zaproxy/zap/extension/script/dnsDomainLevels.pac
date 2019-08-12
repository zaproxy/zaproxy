function FindProxyForURL(url, host) {

    if (dnsDomainLevels(host) == 0)
        return "0 LEVEL";
    else if (dnsDomainLevels(host) == 1)
        return "1 LEVEL";
    else
        return "MORE THAN 1 LEVEL";
}