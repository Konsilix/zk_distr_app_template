package com.konsilix.zk;

import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/** @author "Bikas Katwal" 27/03/19 */
@Slf4j // Lombok annotation for automatic logging setup
public final class ZkDemoUtil {

    public static final String ELECTION_MASTER = "/election/master";
    public static final String ELECTION_NODE = "/election";
    public static final String ELECTION_NODE_2 = "/election2";
    public static final String LIVE_NODES = "/liveNodes";
    public static final String ALL_NODES = "/allNodes";

    public static final String DATA = "/data";

    public static final String FILES_NODE = "/files";
    public static final String KNOWLEDGE_BASE_NODE = "/knowledge_base";

    public static final String APP = "/app";

    @Value("${server.port}")
    private static String ipPort;

    public static String getMyIpAddress() {
        try {
            String localHost = InetAddress.getLocalHost().getHostAddress();
            log.debug(String.format("My IP Address: %s", localHost));
            return localHost;
        } catch (UnknownHostException e) {
            log.error("failed to fetch my IP address!", e);
            throw new RuntimeException("failed to fetch my IP address!", e);
        }
    }

    public static String getMyHostname() {
        try {
//            String localHost = InetAddress.getLocalHost().getCanonicalHostName();
            String localHost = InetAddress.getLocalHost().getHostName();
            log.debug(String.format("Hostname: %s", localHost));
            return localHost;
//            InetAddress localHost = InetAddress.getLocalHost();
//            if (localHost.getAddress().length == 4) { // Check for IPv4 (4 bytes)
//                String hostname = localHost.getHostName();
//                log.debug(String.format("Hostname: %s", hostname));
//                log.debug(String.format("IPv4 Address: %s", localHost.getHostAddress()));
//                return hostname;
//            } else {
//                System.err.println("No IPv4 address found.");
//            }
        } catch (UnknownHostException e) {
            log.error("failed to fetch hostname!", e);
            throw new RuntimeException("failed to fetch hostname!", e);
        }
    }

    public static String getIpFromHostPort(String hostPort) {
//        return hostPort.split(":")[0];
        // TODO: rewrite properly with exception handling
        String ip = hostPort.split(":")[0];
        try {
            InetAddress address = InetAddress.getByName(ip);
            if (address instanceof Inet4Address) {
                return(address.getHostAddress()); // Output: 1.2.3.4
            }
        } catch (UnknownHostException e) {
            log.error(String.format("Invalid IP address: %s: %s", hostPort, e.getMessage()));
            throw new RuntimeException(String.format("Invalid IP address: %s: %s", hostPort, e.getMessage()));
        }
        return ip;
    }

    public static String getHostPortOfServer() {
//        if (ipPort != null) {
        return ipPort;
//        }
//        String ip;
//        try {
//            ip = InetAddress.getLocalHost().getHostAddress();
//        } catch (UnknownHostException e) {
//            throw new RuntimeException("failed to fetch Ip!", e);
//        }
//        int port = Integer.parseInt(System.getProperty("server.port"));
//        ipPort = ip.concat(":").concat(String.valueOf(port));
//        return ipPort;
    }

    public static String getHostnameFromIpAddress(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.getHostName();
        } catch (UnknownHostException e) {
            log.error(String.format("Invalid IP address: %s: %s", ip, e.getMessage()));
            throw new RuntimeException(String.format("Invalid IP address: %s: %s", ip, e.getMessage()));
        }
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    private ZkDemoUtil() {
        log.debug("ZkDemoUtil class is not meant to be instantiated");
    }
}
