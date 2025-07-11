package com.konsilix.utils;

import static com.konsilix.zk.ZkDemoUtil.getMyHostname;

public class WhoAmI {
    public static int main() {
        System.out.println("hostname: " + getMyHostname());
        return 0;
    }
}
