package com.konsilix.theApp;

import com.konsilix.zk.configuration.AppZkProperties;
import com.konsilix.utils.Utils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import lombok.AccessLevel;
import lombok.Getter;

import java.io.IOException;

import static com.konsilix.zk.ZkDemoUtil.getMyHostname;

@SpringBootApplication(scanBasePackages={"com.konsilix"})
@EnableConfigurationProperties(AppZkProperties.class)
public class DocChatBot {
    static String PID_FILE_NAME = "app.pid";

    @Getter(AccessLevel.PUBLIC)
    static final long pid = Utils.fetchPid();

    static {
        try {
            Utils.writePidToLocalFile(PID_FILE_NAME + "." + pid+".pid", pid);
        } catch (IOException ex) {
            System.err.println("Cannot write pid file for this pid = " + pid);
        }
    }

    public static void main(String[] args) {
        System.out.println("hostname: " + getMyHostname());
        SpringApplication application = new SpringApplication(DocChatBot.class);
//		application.addListeners(new SpringAppEventsListener());
        application.run(args);
    }


}
