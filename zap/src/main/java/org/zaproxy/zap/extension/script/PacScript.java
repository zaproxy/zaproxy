/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.script;

import delight.nashornsandbox.NashornSandbox;
import delight.nashornsandbox.NashornSandboxes;
import delight.nashornsandbox.exceptions.ScriptAbuseException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.script.Invocable;
import javax.script.ScriptException;
import org.apache.commons.io.IOUtils;

/**
 * Represents a PAC script file and methods to evaluate its content.
 *
 * @author aine-rb
 */
public class PacScript {

    private static final String GMT_TIME_ZONE = "GMT";
    private static final List<String> MONTHS =
            Collections.unmodifiableList(
                    Arrays.asList(
                            "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT",
                            "NOV", "DEC"));
    static final List<String> DAYS =
            Collections.unmodifiableList(
                    Arrays.asList("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"));

    private static final String SETTINGS_SEPARATOR = ";";
    private static final String TYPE_PROXY_DATA_SEPARATOR = " ";
    private static final String HOST_PORT_SEPARATOR = ":";

    private final Invocable pacImpl;

    private Clock baseClock;

    /**
     * Constructs a {@code PacScript} from the given URL (read with UTF-8 charset).
     *
     * @param scriptUrl the URL corresponding to the PAC script location.
     * @throws IOException if an error occurred while connecting to or reading from the URL.
     * @throws ScriptException if an error occurred while parsing the script.
     * @throws IllegalArgumentException if the read the script is empty.
     * @throws NullPointerException if the {@code scriptURL} is {@code null}.
     */
    public PacScript(URL scriptUrl) throws IOException, ScriptException {
        this(readUrl(Objects.requireNonNull(scriptUrl)));
    }

    private static String readUrl(URL scriptUrl) throws IOException {
        return IOUtils.toString(scriptUrl.openStream(), StandardCharsets.UTF_8);
    }

    /**
     * Constructs a {@code PacScript} from the given file (read with UTF-8 charset).
     *
     * @param file the file with the PAC script.
     * @throws IOException if an error occurred while reading the file.
     * @throws ScriptException if an error occurred while parsing the script.
     * @throws IllegalArgumentException if the file is empty.
     * @throws NullPointerException if the {@code file} is {@code null}.
     */
    public PacScript(Path file) throws IOException, ScriptException {
        this(new String(Files.readAllBytes(Objects.requireNonNull(file)), StandardCharsets.UTF_8));
    }

    /**
     * Constructs a {@code PacScript} from the given string.
     *
     * @param scriptContent the contents of the PAC script.
     * @throws ScriptException if an error occurred while parsing the script.
     * @throws IllegalArgumentException if {@code scriptContent} is {@code null} or empty.
     */
    public PacScript(String scriptContent) throws ScriptException {
        if (scriptContent == null || scriptContent.isEmpty()) {
            throw new IllegalArgumentException("The PAC script content must not be null or empty.");
        }
        this.baseClock = Clock.systemDefaultZone();

        NashornSandbox sandbox = NashornSandboxes.create("-nse");

        sandbox.inject("dateRange", (StringPredicate) this::dateRange);
        sandbox.inject("dnsDomainIs", (BiFunction<String, String, Boolean>) PacScript::dnsDomainIs);
        sandbox.inject("dnsDomainLevels", (Function<String, Integer>) PacScript::dnsDomainLevels);
        sandbox.inject("dnsResolve", (Function<String, String>) PacScript::dnsResolve);
        sandbox.inject("isInNet", (StringPredicate) PacScript::isInNet);
        sandbox.inject("isPlainHostName", (Function<String, Boolean>) PacScript::isPlainHostName);
        sandbox.inject("isResolvable", (Function<String, Boolean>) PacScript::isResolvable);
        sandbox.inject(
                "localHostOrDomainIs",
                (BiFunction<String, String, Boolean>) PacScript::localHostOrDomainIs);
        sandbox.inject("myIpAddress", (Supplier<String>) PacScript::myIpAddress);
        sandbox.inject("shExpMatch", (BiFunction<String, String, Boolean>) PacScript::shExpMatch);
        sandbox.inject("timeRange", (StringPredicate) this::timeRange);
        sandbox.inject("weekdayRange", (StringPredicate) this::weekdayRange);

        try {
            sandbox.eval(scriptContent);
        } catch (ScriptAbuseException e) {
            throw new ScriptException(e);
        }
        pacImpl = sandbox.getSandboxedInvocable();
    }

    void setBaseClock(Clock baseClock) {
        this.baseClock = baseClock;
    }

    /**
     * Calls the FindProxyForURL function of this PAC script.
     *
     * @param url the url param of FindProxyForURL(url, host)
     * @param host the host param of FindProxyForURL(url, host)
     * @return a String representation of the return value of FindProxyForURL(url, host)
     * @throws ScriptException if an error occurred while calling the {@code FindProxyForURL}
     *     function.
     */
    String evaluate(String url, String host) throws ScriptException {
        try {
            return (String) pacImpl.invokeFunction("FindProxyForURL", url, host);
        } catch (NoSuchMethodException | ScriptAbuseException e) {
            throw new ScriptException(e);
        }
    }

    /**
     * Finds the proxy settings for the given url and host.
     *
     * @param url the URL.
     * @param host the host.
     * @return a list of proxy settings, empty if none.
     * @throws ScriptException if an error occurred while calling the {@code FindProxyForURL}
     *     function or parsing its result.
     */
    public List<Setting> findProxyForUrl(String url, String host) throws ScriptException {
        String result = evaluate(url, host);
        if (result == null || result.isEmpty()) {
            return Collections.emptyList();
        }

        List<Setting> settings = new ArrayList<>();
        for (String entry : result.split(SETTINGS_SEPARATOR, -1)) {
            String value = entry.trim();
            if (value.isEmpty()) {
                continue;
            }

            Setting setting = createSetting(value);
            settings.add(setting);
            if (setting.getType() == Setting.Type.DIRECT) {
                break;
            }
        }
        return settings;
    }

    private static Setting createSetting(String value) throws ScriptException {
        if (Setting.Type.DIRECT.name().equals(value)) {
            return Setting.DIRECT;
        }

        String[] elements = value.split(TYPE_PROXY_DATA_SEPARATOR, 2);
        if (elements.length != 2) {
            throw new ScriptException(
                    "Invalid proxy setting format, expected \"<TYPE> <HOST>:<PORT>\" got: "
                            + value);
        }

        Setting.Type type;
        try {
            type = Setting.Type.valueOf(elements[0]);
        } catch (IllegalArgumentException e) {
            throw new ScriptException(
                    "Invalid proxy setting type, expected \"PROXY\" or \"SOCKS\" got: "
                            + elements[0]);
        }
        if (type == Setting.Type.DIRECT) {
            throw new ScriptException(
                    "Invalid proxy setting, expected \"PROXY\" or \"SOCKS\" type in: " + value);
        }

        String[] proxy = elements[1].split(HOST_PORT_SEPARATOR, 2);
        if (proxy.length != 2) {
            throw new ScriptException(
                    "Invalid proxy setting data, expected \"<HOST>:<PORT>\" got: " + elements[1]);
        }

        String host = proxy[0];
        if (host.isEmpty()) {
            throw new ScriptException(
                    "Invalid proxy setting host, expected non empty host in: " + elements[1]);
        }

        int port;
        try {
            port = Integer.parseInt(proxy[1]);
        } catch (NumberFormatException e) {
            throw new ScriptException(
                    "Invalid proxy setting port, expected an integer got: " + proxy[1]);
        }
        if (port <= 0 || port > 65535) {
            throw new ScriptException(
                    "Invalid proxy setting port, expected an integer between 1 and 65535 got: "
                            + port);
        }

        return new Setting(type, host, port);
    }

    private static boolean isDay(String value) {
        int day;
        try {
            day = Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return false;
        }

        return day > 0 && day <= 31;
    }

    private static boolean isMonth(String value) {
        return MONTHS.contains(value);
    }

    private static boolean isYear(String value) {
        try {
            Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * Determines if the date given in parameter matches the current one, in the locale time zone or
     * in the GMT time zone if the {@code timeZone} is equals to "GMT". <br>
     * <br>
     * Every parameter can be the empty String.
     *
     * @param day a number representing the day to test
     * @param month a number representing the month to test
     * @param year a number representing the year to test
     * @param timeZone "GMT" or anything else (the locale time zone will be taken into account in
     *     this case)
     * @return {@code true} if every parameter given is equal to the current Date; {@code false}
     *     otherwise.
     */
    private boolean currentDateIs(String day, String month, String year, String timeZone) {
        LocalDate currentDate = currentDate(timeZone);
        LocalDate givenDate = createDate(day, month, year, currentDate);
        return currentDate.equals(givenDate);
    }

    /**
     * Determines if the current date is between the ones given in parameter, in the locale time
     * zone or in the GMT time zone if the {@code timeZone} is equals to "GMT". <br>
     * <br>
     * Every parameter can be the empty String.
     *
     * @param day1 a number representing the day of the inferior boundary date
     * @param month1 a number representing the month of the inferior boundary date
     * @param year1 a number representing the year of the inferior boundary date
     * @param day2 a number representing the day of the superior boundary date
     * @param month2 a number representing the month of the superior boundary date
     * @param year2 a number representing the year of the superior boundary date
     * @param timeZone "GMT" or anything else (the locale time zone will be taken into account in
     *     this case)
     * @return {@code true} if the current date is superior or equal to the inferior boundary date
     *     and inferior or equal to the superior boundary date; {@code false} otherwise.
     */
    private boolean currentDateIsBetween(
            String day1,
            String month1,
            String year1,
            String day2,
            String month2,
            String year2,
            String timeZone) {
        LocalDate currentDate = currentDate(timeZone);
        if (day1.equals("") || day2.equals("")) {
            String currentDay = String.valueOf(currentDate.getDayOfMonth());
            day1 = currentDay;
            day2 = currentDay;
        }

        if (month1.equals("") || month2.equals("")) {
            String currentMonth = String.valueOf(currentDate.getMonth().getValue());
            month1 = currentMonth;
            month2 = currentMonth;
        }

        if (year1.equals("") || year2.equals("")) {
            String currentYear = String.valueOf(currentDate.getYear());
            year1 = currentYear;
            year2 = currentYear;
        }
        LocalDate dateBefore = createDate(day1, month1, year1, currentDate);
        LocalDate dateAfter = createDate(day2, month2, year2, currentDate);
        return dateBefore.compareTo(currentDate) <= 0 && dateAfter.compareTo(currentDate) >= 0;
    }

    /**
     * Determines if the current date matches the date(s) given in parameter, in the locale time
     * zone or in the GMT time zone if the {@code timeZone} is equals to "GMT". <br>
     * <br>
     * This method is the implementation of the PAC file function dateRange. It implements the 11
     * cases (GMT not taken into account) presented by the Netscape reference documentation and its
     * examples (see the link bellow), that's to say: <br>
     *
     * <ul>
     *   <li>dateRange(day)
     *   <li>dateRange(day, day)
     *   <li>dateRange(day, month)
     *   <li>dateRange(day, month, day, month)
     *   <li>dateRange(day, month, year)
     *   <li>dateRange(day, month, year, day, month, year)
     *   <li>dateRange(month)
     *   <li>dateRange(month, month)
     *   <li>dateRange(month, year, month, year)
     *   <li>dateRange(year)
     *   <li>dateRange(year, year)
     * </ul>
     *
     * plus 1 case corresponding to the dateRange(month, year) case. <br>
     *
     * @param args
     *     <ol>
     *       <li>day1 - a number representing a day or a year, or a 3 letter String representation
     *           of a month
     *       <li>month1 - a number representing a day or a year, or a 3 letter String representation
     *           of a month, or "GMT"
     *       <li>year1 - a number representing a day or a year, or a 3 letter String representation
     *           of a month, or "GMT"
     *       <li>day2 - a number representing a day or a year, or a 3 letter String representation
     *           of a month, or "GMT"
     *       <li>month2 - a 3 letter String representation of a month, or "GMT"
     *       <li>year2 - a number representing a year, or "GMT"
     *       <li>timeZone - "GMT" or anything else (the locale time zone will be taken into account
     *           in this case)
     *     </ol>
     *
     * @return {@code true} if the current Date matches the parameter, according to the PAC file
     *     function documentation; {@code false} otherwise.
     * @see <a href="http://findproxyforurl.com/netscape-documentation/#dateRange">Netscape
     *     documentation</a>
     */
    private boolean dateRange(String... args) {
        String day1 = extractArg(args, 0);
        String month1 = extractArg(args, 1);
        String year1 = extractArg(args, 2);
        String day2 = extractArg(args, 3);
        String month2 = extractArg(args, 4);
        String year2 = extractArg(args, 5);
        String timeZone = extractArg(args, 6);

        // Big if statement parsing parameters one by one and calling the appropriate method with
        // the appropriate
        // parameter order
        if (isDay(day1)) {
            if (isDay(month1)) {
                return currentDateIsBetween(day1, "", "", month1, "", "", year1);
            } else if (isMonth(month1)) {
                if (isDay(year1) && isMonth(day2)) {
                    return currentDateIsBetween(
                            day1,
                            String.valueOf(MONTHS.indexOf(month1) + 1),
                            "",
                            year1,
                            String.valueOf(MONTHS.indexOf(day2) + 1),
                            "",
                            month2);
                } else if (isYear(year1)) {
                    if (isDay(day2) && isMonth(month2) && isYear(year2)) {
                        return currentDateIsBetween(
                                day1,
                                String.valueOf(MONTHS.indexOf(month1) + 1),
                                year1,
                                day2,
                                String.valueOf(MONTHS.indexOf(month2) + 1),
                                year2,
                                timeZone);
                    } else {
                        return currentDateIs(
                                day1, String.valueOf(MONTHS.indexOf(month1) + 1), year1, day2);
                    }
                } else {
                    return currentDateIs(
                            day1, String.valueOf(MONTHS.indexOf(month1) + 1), "", year1);
                }
            } else {
                return currentDateIs(day1, "", "", month1);
            }
        } else if (isMonth(day1)) {
            if (isMonth(month1)) {
                return currentDateIsBetween(
                        "",
                        String.valueOf(MONTHS.indexOf(day1) + 1),
                        "",
                        "",
                        String.valueOf(MONTHS.indexOf(month1) + 1),
                        "",
                        year1);
            } else if (isYear(month1)) {
                if (isMonth(year1) && isYear(day2)) {
                    return currentDateIsBetween(
                            "",
                            String.valueOf(MONTHS.indexOf(day1) + 1),
                            month1,
                            "",
                            String.valueOf(MONTHS.indexOf(year1) + 1),
                            day2,
                            month2);
                } else {
                    return currentDateIs(
                            "", String.valueOf(MONTHS.indexOf(day1) + 1), month1, year1);
                }
            } else {
                return currentDateIs("", String.valueOf(MONTHS.indexOf(day1) + 1), "", month1);
            }
        } else if (isYear(day1)) {
            if (isYear(month1)) {
                return currentDateIsBetween("", "", day1, "", "", month1, year1);
            } else {
                return currentDateIs("", "", day1, month1);
            }
        }

        return false; // At least one of the argument is not valid
    }

    /**
     * Determines if {@code domainName} is the domain name of {@code hostName}. <br>
     * <br>
     * This method is the implementation of the PAC file function dnsDomainIs, see the link bellow
     * for the Netscape reference documentation.
     *
     * @param hostName a String representation of the host name
     * @param domainName a String representation of the domain name
     * @return {@code true} if the domain name of the given {@code hostName} is {@code domainName};
     *     {@code false} otherwise.
     * @see <a href="http://findproxyforurl.com/netscape-documentation/#dnsDomainIs">Netscape
     *     documentation</a>
     */
    private static boolean dnsDomainIs(String hostName, String domainName) {
        return hostName.endsWith(domainName);
    }

    /**
     * Determines the domain level of {@code hostName}. <br>
     * <br>
     * This method is the implementation of the PAC file function dnsDomainLevels, see the link
     * bellow for the Netscape reference documentation.
     *
     * @param hostName a String representation of the host name
     * @return the number of occurrences of the character "." in {@code hostName}
     * @see <a href="http://findproxyforurl.com/netscape-documentation/#dnsDomainLevels">Netscape
     *     documentation</a>
     */
    private static int dnsDomainLevels(String hostName) {
        int hostLength = hostName.length();
        int count = 0;
        for (int i = 0; i < hostLength; i++) {
            if (hostName.charAt(i) == '.') count++;
        }

        return count;
    }

    /**
     * Determines the ip address of {@code hostName} via a DNS lookup. <br>
     * <br>
     * This method is the implementation of the PAC file function dnsResolve, see the link bellow
     * for the Netscape reference documentation.
     *
     * @param hostName a String representation of the host name
     * @return a String representation of the IPv4 or IPv6 address of {@code hostName}
     * @see <a href="http://findproxyforurl.com/netscape-documentation/#dnsResolve">Netscape
     *     documentation</a>
     */
    private static String dnsResolve(String hostName) {
        InetAddress address;
        try {
            address = InetAddress.getByName(hostName);
        } catch (UnknownHostException e) {
            return ""; // Unknown host
        }

        return address.getHostAddress();
    }

    /**
     * Determines if {@code host} belongs to {@code network}, according to {@code mask}. Only
     * supports IPv4 addresses. <br>
     * <br>
     * This method is the implementation of the PAC file function isInNet, see the link bellow for
     * the Netscape reference documentation.
     *
     * @param args
     *     <ol>
     *       <li>host - a String representation of the host name, or a String representation of the
     *           host IPv4 address
     *       <li>network - a String representation of the network IPv4 address
     *       <li>mask - a String representation of the mask IPv4 address
     *     </ol>
     *
     * @return {@code true} if {@code host} belongs to {@code network}, according to {@code mask};
     *     {@code false} otherwise.
     * @see <a href="http://findproxyforurl.com/netscape-documentation/#isInNet">Netscape
     *     documentation</a>
     * @see PacScript#dnsResolve(String)
     */
    private static boolean isInNet(String... args) {
        String host = extractArg(args, 0);
        String network = extractArg(args, 1);
        String mask = extractArg(args, 2);

        // Retrieves ip addresses in a String array format
        String[] hostBytes = dnsResolve(host).split("\\.");
        String[] networkBytes = network.split("\\.");
        String[] maskBytes = mask.split("\\.");

        if (hostBytes.length != 4 || networkBytes.length != 4 || maskBytes.length != 4)
            return false; // One of the argument does not lead to a valid IPv4 address

        for (int i = 0; i < 4; i++) {
            int hostByte;
            int networkByte;
            int maskByte;

            try {
                hostByte = Integer.valueOf(hostBytes[i]);
                networkByte = Integer.valueOf(networkBytes[i]);
                maskByte = Integer.valueOf(maskBytes[i]);
            } catch (NumberFormatException e) {
                return false; // One of the argument does not lead to a valid IPv4 address
            }

            if (hostByte < 0
                    || hostByte > 255
                    || networkByte < 0
                    || networkByte > 255
                    || maskByte < 0
                    || maskByte > 255)
                return false; // One of the argument does not lead to a valid IPv4 address

            if ((hostByte & maskByte) != (networkByte & maskByte))
                return false; // host is not in the same network that network, according to mask
        }

        return true;
    }

    /**
     * Determines if {@code hostName} is a plain host name, that's to say if it does not contain any
     * domain name. <br>
     * <br>
     * This method is the implementation of the PAC file function isPlainHostName, see the link
     * bellow for the Netscape reference documentation.
     *
     * @param hostName a String representation of the host name
     * @return {@code true} if {@code hostName} contains no dots; {@code false} otherwise.
     * @see <a href="http://findproxyforurl.com/netscape-documentation/#isPlainHostName">Netscape
     *     documentation</a>
     */
    private static boolean isPlainHostName(String hostName) {
        return !hostName.contains(".");
    }

    /**
     * Determines if {@code hostName} is resolvable via a DNS lookup. <br>
     * <br>
     * This method is the implementation of the PAC file function isResolvable, see the link bellow
     * for the Netscape reference documentation.
     *
     * @param hostName a String representation of the host name
     * @return {@code true} if {@code hostName} is resolved by a valid IPv4 or IPv6 address; {@code
     *     false} otherwise.
     * @see <a href="http://findproxyforurl.com/netscape-documentation/#isResolvable">Netscape
     *     documentation</a>
     */
    private static boolean isResolvable(String hostName) {
        try {
            InetAddress.getByName(hostName);
        } catch (UnknownHostException e) {
            return false; // Unknown host
        }

        return true;
    }

    /**
     * Determines if {@code hostName} is the host name of the Fully Qualified Host Name {@code
     * FQHN}. <br>
     * <br>
     * This method is the implementation of the PAC file function localHostOrDomainIs, see the link
     * bellow for the Netscape reference documentation.
     *
     * @param hostName a String representation of the host name
     * @param FQHN a String representation of the Fully Qualified Host Name
     * @return {@code true} if {@code hostName} and {@code FQHN} are equals or if the host name of
     *     {@code FQHN} is {@code hostName}; {@code false} otherwise.
     * @see <a
     *     href="http://findproxyforurl.com/netscape-documentation/#localHostOrDomainIs">Netscape
     *     documentation</a>
     */
    private static boolean localHostOrDomainIs(String hostName, String FQHN) {
        return hostName.equals(FQHN) || FQHN.startsWith(hostName + ".");
    }

    /**
     * Determines the ip address of the current host. <br>
     * <br>
     * This method is the implementation of the PAC file function myIpAddress, see the link bellow
     * for the Netscape reference documentation.
     *
     * @return a String representation of the first non-loopback IPv4 or IPv6 address found for the
     *     current host
     * @see <a href="http://findproxyforurl.com/netscape-documentation/#myIpAddress">Netscape
     *     documentation</a>
     */
    private static String myIpAddress() {
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return "";
        }
        while (interfaces.hasMoreElements()) {
            Enumeration<InetAddress> interface_addresses =
                    interfaces.nextElement().getInetAddresses();

            while (interface_addresses.hasMoreElements()) {
                InetAddress ip_address = interface_addresses.nextElement();
                if (!ip_address.isLoopbackAddress())
                    return ip_address.getHostAddress().replaceAll("%.*", "");
            }
        }

        return "";
    }

    /**
     * Determines if {@code name} matches the shell expression {@code expression}. <br>
     * <br>
     * In fact, the {@code expression} is not going to be evaluated as a true shell expression. Some
     * character replacement is done before using the {@link
     * java.util.regex.Pattern#compile(String)} method to compile the {@code expression}. Then, this
     * method should be able to evaluate every shell expression as mentioned in the Netscape
     * reference documentation, plus some {@link java.util.regex} regular expressions. See the
     * "Summary of regular-expression constructs" of the {@link java.util.regex.Pattern} class for
     * more details. <br>
     * <br>
     * This method is the implementation of the PAC file function shExpMatch, see the link bellow
     * for the Netscape reference documentation.
     *
     * @param name a String representation of the name to parse
     * @param expression a String representation of the shell expression to match
     * @return {@code true} if {@code name} matches {@code expression}; {@code false} otherwise.
     * @see <a href="http://findproxyforurl.com/netscape-documentation/#shExpMatch">Netscape
     *     documentation</a>
     * @see java.util.regex.Pattern
     */
    private static boolean shExpMatch(String name, String expression) {
        expression = expression.replace(".", "\\.");
        expression = expression.replace("*", ".*");
        expression = expression.replace("?", ".");
        Pattern pattern;
        try {
            pattern = Pattern.compile(expression);
        } catch (PatternSyntaxException e) {
            return false; // The syntax for this expression is not valid
        }

        return pattern.matcher(name).matches();
    }

    private static boolean isHour(String val) {
        int hour;
        try {
            hour = Integer.valueOf(val);
        } catch (NumberFormatException e) {
            return false;
        }

        return hour >= 0 && hour < 24;
    }

    private static boolean isMinOrSec(String val) {
        int minOrSec;
        try {
            minOrSec = Integer.valueOf(val);
        } catch (NumberFormatException e) {
            return false;
        }

        return minOrSec >= 0 && minOrSec < 60;
    }

    /**
     * Determines if the time given in parameter matches the current one, in the locale time zone or
     * in the GMT time zone if the {@code timeZone} is equals to "GMT". <br>
     * <br>
     * Every parameter can be the empty String.
     *
     * @param hour a number representing the hour to test
     * @param min a number representing the minute to test
     * @param sec a number representing the second to test
     * @param timeZone "GMT" or anything else (the locale time zone will be taken into account in
     *     this case)
     * @return {@code true} if every parameter given is equal to the current time; {@code false}
     *     otherwise.
     */
    private boolean currentTimeIs(String hour, String min, String sec, String timeZone) {
        LocalTime currentTime = currentTime(timeZone);
        LocalTime givenTime = createLocalTime(hour, min, sec, currentTime);
        return currentTime.equals(givenTime);
    }

    /**
     * Determines if the current time is between the ones given in parameter, in the locale time
     * zone or in the GMT time zone if the {@code timeZone} is equals to "GMT". <br>
     * <br>
     * Every parameter can be the empty String.
     *
     * @param hour1 a number representing the hour of the inferior boundary time
     * @param min1 a number representing the minute of the inferior boundary time
     * @param sec1 a number representing the second of the inferior boundary time
     * @param hour2 a number representing the hour of the superior boundary time
     * @param min2 a number representing the minute of the superior boundary time
     * @param sec2 a number representing the second of the superior boundary time
     * @param timeZone "GMT" or anything else (the locale time zone will be taken into account in
     *     this case)
     * @return {@code true} if the current time is superior or equal to the inferior boundary time
     *     and is strictly inferior to the superior boundary time; {@code false} otherwise.
     */
    private boolean currentTimeIsBetween(
            String hour1,
            String min1,
            String sec1,
            String hour2,
            String min2,
            String sec2,
            String timeZone) {
        LocalTime currentTime = currentTime(timeZone);
        if (min1.equals("") || min2.equals("")) {
            String currentMin = String.valueOf(currentTime.getMinute());
            min1 = currentMin;
            min2 = currentMin;
        }
        if (sec1.equals("") || sec2.equals("")) {
            String currentSec = String.valueOf(currentTime.getSecond());
            sec1 = currentSec;
            sec2 = currentSec;
        }
        LocalTime beforeTime = createLocalTime(hour1, min1, sec1, currentTime);
        LocalTime afterTime = createLocalTime(hour2, min2, sec2, currentTime);
        return beforeTime.compareTo(currentTime) <= 0 && afterTime.compareTo(currentTime) > 0;
    }

    /**
     * Determines if the current time matches the time(s) given in parameter, in the locale time
     * zone or in the GMT time zone if the {@code timeZone} is equals to "GMT". <br>
     * <br>
     * This method is the implementation of the PAC file function timeRange. It implements the 4
     * cases (GMT not taken into account) presented by the Netscape reference documentation and its
     * examples (see the link bellow), that's to say: <br>
     *
     * <ul>
     *   <li>timeRange(hour)
     *   <li>timeRange(hour, hour)
     *   <li>timeRange(hour, minute, hour, minute)
     *   <li>timeRange(hour, minute, second, hour, minute, second)
     * </ul>
     *
     * plus 1 case corresponding to the timeRange(hour, minute, second) case. <br>
     *
     * @param args
     *     <ol>
     *       <li>hour1 - a number representing an hour (from 0 to 23)
     *       <li>min1 - a number representing an hour (from 0 to 23) or a minute (from 0 to 59) or
     *           "GMT"
     *       <li>sec1 - a number representing an hour (from 0 to 23) or a second (from 0 to 59) or
     *           "GMT"
     *       <li>hour2 - a number representing an hour (from 0 to 23) or a minute (from 0 to 59) or
     *           "GMT"
     *       <li>min2 - a number representing a minute (from 0 to 59) or "GMT"
     *       <li>sec2 - a number representing a second (from 0 to 59)
     *       <li>timeZone - "GMT" or anything else (the locale time zone will be taken into account
     *           in this case)
     *     </ol>
     *
     * @return {@code true} if the current time matches the parameter, according to the PAC file
     *     function documentation; {@code false} otherwise.
     * @see <a href="http://findproxyforurl.com/netscape-documentation/#timeRange">Netscape
     *     documentation</a>
     */
    private boolean timeRange(String... args) {
        String hour1 = extractArg(args, 0);
        String min1 = extractArg(args, 1);
        String sec1 = extractArg(args, 2);
        String hour2 = extractArg(args, 3);
        String min2 = extractArg(args, 4);
        String sec2 = extractArg(args, 5);
        String timeZone = extractArg(args, 6);

        List<String> argList = Arrays.asList(args);

        // Count the number of parameters corresponding to numbers
        int numberCount = 0;
        for (String arg : argList) {
            try {
                Integer.valueOf(arg);
            } catch (NumberFormatException e) {
                continue;
            }
            numberCount++;
        }

        boolean hour1IsAnHour = isHour(hour1);
        boolean min1IsAMin = isMinOrSec(min1);
        boolean sec1IsASec = isMinOrSec(sec1);

        // According to the number of parameters corresponding to numbers, call the appropriate
        // method
        if (numberCount == 1 && hour1IsAnHour) return currentTimeIs(hour1, "", "", min1);

        if (numberCount == 2 && hour1IsAnHour && isHour(min1))
            return currentTimeIsBetween(hour1, "", "", min1, "", "", sec1);

        if (numberCount == 3 && hour1IsAnHour && min1IsAMin && sec1IsASec)
            return currentTimeIs(hour1, min1, sec1, hour2);

        if (numberCount == 4 && hour1IsAnHour && min1IsAMin && isHour(sec1) && isMinOrSec(hour2))
            return currentTimeIsBetween(hour1, min1, "", sec1, hour2, "", min2);

        if (numberCount == 6
                && hour1IsAnHour
                && min1IsAMin
                && sec1IsASec
                && isHour(hour2)
                && isMinOrSec(min2)
                && isMinOrSec(sec2))
            return currentTimeIsBetween(hour1, min1, sec1, hour2, min2, sec2, timeZone);

        return false;
    }

    /**
     * Determines if the current day matches the day(s) given in parameter, in the locale time zone
     * or in the GMT time zone if the {@code timeZone} is equals to "GMT". <br>
     * <br>
     * This method is the implementation of the PAC file function weekdayRange. It implements the 2
     * cases (GMT not taken into account) presented by the Netscape reference documentation and its
     * examples (see the link bellow), that's to say: <br>
     *
     * <ul>
     *   <li>weekdayRange(day)
     *   <li>weekdayRange(day, day)
     * </ul>
     *
     * @param args
     *     <ol>
     *       <li>day1 - a String representation of the 3 first letters of a day
     *       <li>day2 - a String representation of the 3 first letters of a day or "GMT"
     *       <li>timeZone - "GMT" or anything else (the locale time zone will be taken into account
     *           in this case)
     *     </ol>
     *
     * @return {@code true} if the current day matches the parameters, according to the PAC file
     *     function documentation; {@code false} otherwise.
     * @see <a href="http://findproxyforurl.com/netscape-documentation/#weekdayRange">Netscape
     *     documentation</a>
     */
    private boolean weekdayRange(String... args) {
        String day1 = extractArg(args, 0);
        String day2 = extractArg(args, 1);
        String timeZone = extractArg(args, 2);

        if (!DAYS.contains(day1)) return false; // day1 is not a well-formed day

        if (!DAYS.contains(day2)) {
            return DAYS.indexOf(day1) == currentDayOfWeek(day2);
        }

        int currentDay = currentDayOfWeek(timeZone);

        int index1 = DAYS.indexOf(day1);
        int index2 = DAYS.indexOf(day2);

        // The day of today is comprised between day1 and day2
        return index1 <= currentDay && index2 >= currentDay
                // or between day2 and day1
                || index2 < index1 && (index2 >= currentDay || index1 <= currentDay);
    }

    private int currentDayOfWeek(String timezone) {
        DayOfWeek dayOfWeek = currentDate(timezone).getDayOfWeek();
        return dayOfWeek.get(WeekFields.SUNDAY_START.dayOfWeek()) - 1;
    }

    private LocalDate currentDate(String timeZone) {
        return LocalDate.now(getClock(timeZone));
    }

    private Clock getClock(String timeZone) {
        if (GMT_TIME_ZONE.equals(timeZone)) {
            return baseClock.withZone(ZoneId.of(GMT_TIME_ZONE));
        }
        return baseClock;
    }

    private LocalTime currentTime(String timeZone) {
        return LocalTime.now(getClock(timeZone)).truncatedTo(ChronoUnit.SECONDS);
    }

    private static LocalDate createDate(
            String day, String month, String year, LocalDate defaultDate) {
        return LocalDate.of(
                getInt(year, defaultDate.getYear()),
                getInt(month, defaultDate.getMonth().getValue()),
                getInt(day, defaultDate.getDayOfMonth()));
    }

    private static LocalTime createLocalTime(
            String hour, String min, String sec, LocalTime defaultTime) {
        return LocalTime.of(
                getInt(hour, defaultTime.getHour()),
                getInt(min, defaultTime.getMinute()),
                getInt(sec, defaultTime.getSecond()));
    }

    private static int getInt(String value, int defaultValue) {
        return value.isEmpty() ? defaultValue : Integer.parseInt(value);
    }

    private static String extractArg(String[] args, int index) {
        if (args == null || index >= args.length) {
            return "";
        }
        return args[index];
    }

    @FunctionalInterface
    public interface StringPredicate {
        public boolean apply(String... args);
    }

    /** A proxy setting, result from calling the {@code FindProxyForURL} function. */
    public static final class Setting {
        enum Type {
            DIRECT,
            PROXY,
            SOCKS;
        }

        private static final Setting DIRECT = new Setting(Type.DIRECT, null, 0);

        private final Type type;
        private final String host;
        private final int port;

        private Setting(Type type, String host, int port) {
            this.type = type;
            this.host = host;
            this.port = port;
        }

        public Type getType() {
            return type;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }
}
