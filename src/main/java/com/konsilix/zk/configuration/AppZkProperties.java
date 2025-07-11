package com.konsilix.zk.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j // this gives access to a logger called "log"
@ConfigurationProperties(prefix="zk")
@Configuration
@Getter
@Setter
public class AppZkProperties {
    // TODO rob - add NumberFormatException handling
    private String port;

    private int sessionTimeout;

    private int connectionTimeout;

    private String storageLocation;

    private String servers;

    private String mode;
}
