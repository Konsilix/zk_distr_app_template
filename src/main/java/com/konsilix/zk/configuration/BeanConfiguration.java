package com.konsilix.zk.configuration;

import com.konsilix.zk.ZkService;
import com.konsilix.zk.ZkServiceImpl;
import com.konsilix.zk.watchers.*;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Slf4j
@Configuration
public class BeanConfiguration {

    // Inject the standard Spring Cloud Zookeeper property.
    // This value comes directly from the environment variable in your deployment-chatbot.yaml.
    @Value("${spring.cloud.zookeeper.connect-string}")
    private String zookeeperConnectString;

    private final AppZkProperties appProperties;

    @Getter
    String zkHostPort;

    @Getter
    String storageLocation;

    @Autowired
    public BeanConfiguration(@Qualifier("appZkProperties") AppZkProperties appZkProperties) {
        this.appProperties = appZkProperties;
        log.debug("**** BeanConfiguration constructor");
    }

    /**
     * This method runs after dependency injection is complete.
     * It's a safer place to use injected values than the constructor.
     */
    @PostConstruct
    private void initialize() {
        // Use the standard property injected by Spring Cloud.
        // This makes the custom code compatible with the k8s environment variable.
        this.zkHostPort = this.zookeeperConnectString;

        int sessionTimeout = appProperties.getSessionTimeout();
        int connectionTimeout = appProperties.getConnectionTimeout();
        this.storageLocation = appProperties.getStorageLocation();

        log.info("Zookeeper host port: {}", this.zkHostPort);
        log.info("Zookeeper session timeout: {}", sessionTimeout);
        log.info("Zookeeper connection timeout: {}", connectionTimeout);
    }


    @Bean(name = "zkService")
    @Scope("singleton")
    public ZkService zkService() {
        // Now this.zkHostPort will have the correct value from the environment variable.
        return new ZkServiceImpl(this.zkHostPort, appProperties.getSessionTimeout(), appProperties.getConnectionTimeout());
    }

    // ... rest of the file is unchanged ...
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