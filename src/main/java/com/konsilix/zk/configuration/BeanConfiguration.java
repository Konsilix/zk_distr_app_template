package com.konsilix.zk.configuration;

import com.konsilix.zk.ZkService;
import com.konsilix.zk.ZkServiceImpl;
import com.konsilix.zk.watchers.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.net.InetAddress;
import java.net.UnknownHostException;

/** @author "Bikas Katwal" 2019/03/26 - initial */
/** @author "Rob Marano" 2023/05/01 - updated, expanded */
@Slf4j // this gives access to a logger called "log"
@Configuration
public class BeanConfiguration {
    @Autowired
    public BeanConfiguration(@Qualifier("appZkProperties") AppZkProperties appZkProperties) {
        // TODO rob - incorporate exponential backoff for retries
        this.appProperties = appZkProperties;
        log.debug("**** BeanConfiguration constructor");
        String ipAddress = "localhost";
        try {
            InetAddress ip = InetAddress.getLocalHost();
            ipAddress = (ip.getHostAddress()).trim();
        } catch (UnknownHostException e) {
            log.debug("UnknownHostException occurred: ", e);
        }
        String thePort = appProperties.getPort();
        int sessionTimeout = appProperties.getSessionTimeout();
        int connectionTimeout = appProperties.getConnectionTimeout();
        this.storageLocation = appProperties.getStorageLocation();

        if ("single".equals(appProperties.getMode())) {
            this.zkHostPort = String.format("%s:%s", ipAddress, thePort);
        } else {
            this.zkHostPort = appProperties.getServers();
        }
        log.info(String.format("Zookeeper host port: %s\n", this.zkHostPort));
        log.info(String.format("Zookeeper session timeout: %d\n", sessionTimeout));
        log.info(String.format("Zookeeper connection timeout: %d\n", connectionTimeout));
    }

    private final AppZkProperties appProperties;

    @Getter
    String zkHostPort;

    @Getter
    String storageLocation;

    @Bean(name = "zkService")
    @Scope("singleton")
    public ZkService zkService() {
        return new ZkServiceImpl(this.zkHostPort, appProperties.getSessionTimeout(), appProperties.getConnectionTimeout());
    }

    @Bean(name = "allNodesChangeListener")
    @Scope("singleton")
    public IZkChildListener allNodesChangeListener() {
        return new AllNodesChangeListener();
    }

    @Bean(name = "liveNodeChangeListener")
    @Scope("singleton")
    public IZkChildListener liveNodeChangeListener() {
        return new LiveNodeChangeListener();
    }

    @Bean(name = "masterChangeListener")
    @ConditionalOnProperty(name = "leader.algo", havingValue = "1")
    @Scope("singleton")
    public IZkChildListener masterChangeListener() {
        MasterChangeListener masterChangeListener = new MasterChangeListener();
        masterChangeListener.setZkService(zkService());
        return masterChangeListener;
    }

    @Bean(name = "masterChangeListener")
    @ConditionalOnProperty(name = "leader.algo", havingValue = "2", matchIfMissing = true)
    @Scope("singleton")
    public IZkChildListener masterChangeListener2() {
        MasterChangeListenerApproach2 masterChangeListener = new MasterChangeListenerApproach2();
        masterChangeListener.setZkService(zkService());
        return masterChangeListener;
    }

    @Bean(name = "connectStateChangeListener")
    @Scope("singleton")
    public IZkStateListener connectStateChangeListener() {
        ConnectStateChangeListener connectStateChangeListener = new ConnectStateChangeListener();
        connectStateChangeListener.setZkService(zkService());
        return connectStateChangeListener;
    }

    @Bean(name = "dataChangeListener")
    @Scope("singleton")
    public IZkDataListener dataChangeListener() {
        DataChangeListener dataChangeListener = new DataChangeListener();
        dataChangeListener.setZkService(zkService());
        return dataChangeListener;
    }
}
